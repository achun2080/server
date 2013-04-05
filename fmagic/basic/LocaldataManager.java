package fmagic.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements the management of data that clients or servers have to
 * persist for there own disposition.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 */
public class LocaldataManager implements ManagerInterface
{
	// List of local data properties
	private Properties localdataProperties = new Properties();

	// Flags for synchronizing the locking of messages
	private ConcurrentHashMap<String, Boolean> processingActive = new ConcurrentHashMap<String, Boolean>();
	private int messageLostCounter = 0;

	/**
	 * Constructor
	 */
	public LocaldataManager()
	{
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = { "LocalData" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceTemplate(context, includingResourceIdentifiers, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public String printManual(Context context)
	{
		String dumpText = "";

		String typeCriteria[] = { "LocalData" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceManual(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public String printIdentifierList(Context context)
	{
		String dumpText = "";

		String typeCriteria[] = { "LocalData" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceIdentifierList(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public boolean ckeckOnResourceIdentifierIntegrityError(Context context)
	{
		return false;
	}
	
	@Override
	public boolean readConfiguration(Context context)
	{
		return false;
	}

	/**
	 * Lock the processing of an event, a log message or an error message to
	 * avoid recursive calls. Recursive calls can occur if a message is
	 * processed during a running processing.
	 * <p>
	 * The locking is done for each thread and message type separately.
	 * <p>
	 * Be careful!
	 * 
	 * @param messageType
	 *            The type of the message to lock.
	 * 
	 * @param enumIdentifier
	 *            The identifier of the message.
	 * 
	 * @return Returns <TT>true</TT> if the message processing is locked,
	 *         otherwise <TT>false</TT>.
	 */
	private boolean lockMessageHandling(String messageType, String enumIdentifier)
	{
		// Validate parameter
		if (messageType == null) return true;

		// Lock the message handling
		try
		{
			synchronized (this.processingActive)
			{
				String threadIdentifier = String.valueOf(Thread.currentThread().getId()) + "#" + messageType;
				Boolean isActive = this.processingActive.get(threadIdentifier);

				if (isActive != null && isActive == true)
				{
					this.messageLostCounter++;
					System.out.println("\n*** " + messageType + " message lost (" + String.valueOf(this.messageLostCounter) + ")");
					if (enumIdentifier != null) System.out.println("*** " + enumIdentifier);
					return true;
				}

				isActive = true;
				this.processingActive.put(threadIdentifier, isActive);

				// System.out.println("----------");
				// for (String value : this.processingActive.keySet())
				// {
				// System.out.println("--> " + value);
				// }
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Clear the Map if it has become too big. The should never happen.
		try
		{
			synchronized (this.processingActive)
			{
				if (this.processingActive.size() > 10000)
				{
					System.out.println("\n*** Processing active MESSAGE map has more than 10000 items and was cleared.");
					this.processingActive.clear();
				}
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return false;
	}

	/**
	 * Unlock the processing of an event, a log message or an error message to
	 * avoid recursive calls. Recursive calls can occur if an message is
	 * processed during a running processing.
	 * <p>
	 * The unlocking is done for each thread and message type separately.
	 * <p>
	 * Be careful!
	 * 
	 * @param messageType
	 *            The type of the message to lock.
	 */
	private void unlockMessageHandling(String messageType)
	{
		try
		{
			synchronized (this.processingActive)
			{
				String threadIdentifier = String.valueOf(Thread.currentThread().getId()) + "#" + messageType;
				this.processingActive.remove(threadIdentifier);
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return;
	}

	/**
	 * Add a property to the list of properties.
	 * <p>
	 * Please notice: It is recommended to invoke the static method
	 * <TT>ResourceManager.localdata(context, group, name)</TT> to get a
	 * resource container. The signature <TT>localdata</TT> sets the
	 * <TT>type</TT> of the resource. Together with the parameters
	 * <TT>group</TT> and <TT>name</TT> you have an unique identification.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param resourceContainer
	 *            The resource container that sets the property identifier.
	 * 
	 * @param value
	 *            Value to set to the property.
	 */
	public void writeProperty(Context context, ResourceContainer resourceContainer, String value)
	{
		// Check parameters
		if (resourceContainer == null) return;

		// Lock configuration processing
		if (this.lockMessageHandling("LocalDataAdd", resourceContainer.getRecourceIdentifier()) == true) return;

		// Add property
		String identifier = null;

		try
		{
			while (true)
			{
				// Get property identifier
				identifier = resourceContainer.getRecourceIdentifier();
				if (identifier == null) break;

				// Add the localdata value
				this.writeProperty(context, identifier, value);

				// Notify WATCHDOG
				this.notifyWatchdog(context, identifier, value, null, "Local Data property was added/written");

				// Break
				break;
			}
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "LocalData", "ErrorOnAddingProperty"), "--> Identifier: '" + identifier + "'", e);
		}
		finally
		{
			// Unlock message processing
			this.unlockMessageHandling("LocalDataAdd");
		}
	}

	/**
	 * Notify the WATCHDOG about local data access.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param resourceIdentifier
	 *            The resource identifier of the configuration property.
	 * 
	 * @param value
	 *            The value that was read resp. set.
	 * 
	 * @param defaultValue
	 *            Default value of the property.
	 * 
	 * @param messageText
	 *            The message text to notify.
	 */
	private void notifyWatchdog(Context context, String identifier, String value, String defaultValue, String messageText)
	{
		try
		{
			// Set additional text
			String additionalText = "--> " + messageText;
			additionalText += "\n--> Identifier: '" + identifier + "'";

			if (defaultValue != null)
			{
				additionalText += "\n--> Default value was: '" + ResourceManager.hideSecurityValue(context, identifier, defaultValue) + "'";
			}

			if (value == null)
			{
				additionalText += "\n--> Local Data value result: '<NULL>'";
			}
			else
			{
				additionalText += "\n--> Local Data value result: '" + ResourceManager.hideSecurityValue(context, identifier, value) + "'";
			}

			// Set resource identifier documentation
			String resourceDocumentationText = null;
			resourceDocumentationText = context.getResourceManager().getResource(context, identifier).printManual(context);

			if (context.getWatchdogManager() != null) context.getWatchdogManager().addWatchdogCommand(context, identifier, messageText, additionalText, resourceDocumentationText, null, new Date());
		}
		catch (Exception e)
		{
			// Be silent
		}
	}

	/**
	 * Add a property to the list of properties.
	 * <p>
	 * Please notice: This method is overloaded with two different parameter
	 * settings. The first function expects a resource container
	 * <TT>ResourceContainer</TT> to define the property identifier. This method
	 * is set to <TT>public</TT> and should be invoked by all classes. The other
	 * function needs the property identifier directly as a <TT>String</TT>.
	 * This method is set to <TT>private</TT> use only.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param identifier
	 *            Identifier of the property.
	 * 
	 * @param value
	 *            Value to set to the property.
	 */
	private synchronized void writeProperty(Context context, String identifier, String value)
	{
		// Reads the current properties
		this.readPropertiesFile(context);

		// Adds a new or modified property
		this.localdataProperties.put(identifier, value);

		// Writes data back to file
		this.writePropertiesFile(context);
	}

	/**
	 * Get a property from the list of properties.
	 * <p>
	 * Please notice: It is recommended to invoke the static method
	 * <TT>ResourceManager.localdata(context, group, name)</TT> to get a
	 * resource container. The signature <TT>localdata</TT> sets the
	 * <TT>type</TT> of the resource. Together with the parameters
	 * <TT>group</TT> and <TT>name</TT> you have an unique identification.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param resourceContainer
	 *            The resource container that sets the property identifier.
	 * 
	 * @param defaultValue
	 *            Default value of property if it could not be found.
	 * 
	 * @return Returns the read property value or the default value, if the
	 *         property was not found.
	 * 
	 */
	public String readProperty(Context context, ResourceContainer resourceContainer, String defaultValue)
	{
		// Check parameters
		if (resourceContainer == null) return defaultValue;

		// Get property identifier
		String identifier = resourceContainer.getRecourceIdentifier();
		if (identifier == null) return defaultValue;

		// add the local data value
		return this.readProperty(context, identifier, defaultValue);
	}

	/**
	 * Get a property from the list of properties.
	 * <p>
	 * Please notice: This method is overloaded with two different parameter
	 * settings. The first function expects a resource container
	 * <TT>ResourceContainer</TT> to define the property identifier. This method
	 * is set to <TT>public</TT> and should be invoked by all classes. The other
	 * function needs the property identifier directly as a <TT>String</TT>.
	 * This method is set to <TT>private</TT> use only.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param identifier
	 *            Identifier of property.
	 * 
	 * @param defaultValue
	 *            Default value of property if it could not be found.
	 * 
	 * @return Returns the read property value or the default value, if the
	 *         property was not found.
	 * 
	 */
	private String readProperty(Context context, String identifier, String defaultValue)
	{
		// Lock configuration processing
		if (this.lockMessageHandling("LocalDataGet", identifier) == true) return defaultValue;

		// Variables
		String value = null;

		// Get property
		try
		{
			while (true)
			{
				// Reads the current properties
				this.readPropertiesFile(context);

				// Reads property value
				value = (String) this.localdataProperties.get(identifier);
				if (value == null) value = defaultValue;
				value = value.trim();

				// Notify WATCHDOG
				this.notifyWatchdog(context, identifier, value, defaultValue, "Local Data property was read");

				// Break
				break;
			}
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "LocalData", "ErrorOnAddingProperty"), "--> Identifier: '" + identifier + "'", e);
		}
		finally
		{
			// Unlock message processing
			this.unlockMessageHandling("LocalDataGet");
		}

		// Return
		return value;
	}

	/**
	 * Get a property from the list of properties as an <TT>integer</TT> value.
	 * <p>
	 * Please notice: It is recommended to invoke the static method
	 * <TT>ResourceManager.localdata(context, group, name)</TT> to get a
	 * resource container. The signature <TT>localdata</TT> sets the
	 * <TT>type</TT> of the resource. Together with the parameters
	 * <TT>group</TT> and <TT>name</TT> you have an unique identification.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param resourceContainer
	 *            The resource container that sets the property identifier.
	 * 
	 * @param defaultValue
	 *            Default value of property if it could not be found.
	 * 
	 * @return Returns the read property value or the default value, if the
	 *         property was not found, or <TT>0</TT>, if the value could not be
	 *         converted to an integer value.
	 */
	public int readPropertyAsIntegerValue(Context context, ResourceContainer resourceContainer, int defaultValue)
	{
		// Check parameters
		if (resourceContainer == null) return defaultValue;

		// Get property identifier
		String identifier = resourceContainer.getRecourceIdentifier();
		if (identifier == null) return defaultValue;

		// add the local data value
		return this.readPropertyAsIntegerValue(context, identifier, defaultValue);
	}

	/**
	 * Get a property from the list of properties as an <TT>integer</TT> value.
	 * <p>
	 * Please notice: This method is overloaded with two different parameter
	 * settings. The first function expects a resource container
	 * <TT>ResourceContainer</TT> to define the property identifier. This method
	 * is set to <TT>public</TT> and should be invoked by all classes. The other
	 * function needs the property identifier directly as a <TT>String</TT>.
	 * This method is set to <TT>private</TT> use only.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param identifier
	 *            Identifier of property.
	 * 
	 * @param defaultValue
	 *            Default value of property if it could not be found.
	 * 
	 * @return Returns the read property value or the default value, if the
	 *         property was not found, or <TT>0</TT>, if the value could not be
	 *         converted to an integer value.
	 */
	private int readPropertyAsIntegerValue(Context context, String identifier, int defaultValue)
	{
		// Reads the current properties
		String valueAsString = readProperty(context, identifier, String.valueOf(defaultValue));

		// Validate value
		if (valueAsString == null) return defaultValue;
		if (valueAsString.equals("")) return defaultValue;

		// Convert to integer
		int valueAsInteger = defaultValue;

		try
		{
			Integer currentValue = Integer.valueOf(valueAsString);
			valueAsInteger = currentValue;
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return valueAsInteger;
	}

	/**
	 * Composes the path of the data directory of local data.
	 * <p>
	 * The data files were written into the sub directory
	 * <TT>fmagic.localdata</TT>.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the directory were the data files are stored.
	 * 
	 */
	private String getLocalDataFilePath(Context context)
	{
		return FileLocationManager.getRootPath() + FileLocationManager.getPathElementDelimiterString() + FileLocationManager.getLocaldataSubPath();
	}

	/**
	 * Composes the file name of local data.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the file name of the data files.
	 * 
	 */
	private String getLocaldataFileName(Context context)
	{
		String fileName = FileLocationManager.getLocaldataFileName();
		fileName = FileLocationManager.replacePlacholder(context, fileName);
		return fileName;
	}

	/**
	 * Read local data from properties file.
	 * 
	 * @param context
	 *            The context to use.
	 */
	private void readPropertiesFile(Context context)
	{
		// Composes file name
		String fileName = getLocalDataFilePath(context) + FileLocationManager.getPathElementDelimiterString() + getLocaldataFileName(context);

		try
		{
			localdataProperties.load(new FileInputStream(fileName));
		}
		catch (Exception e)
		{
			// No error message because of this file is optional
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.WARNING, "Local Data data not found in: '" + fileName + "'");
		}
	}

	/**
	 * Write local data to properties file.
	 * 
	 * @param context
	 *            The context to use.
	 */
	private void writePropertiesFile(Context context)
	{
		// Composes file name
		String path = getLocalDataFilePath(context);
		String fileName = getLocaldataFileName(context);
		String fullFileName = path + FileLocationManager.getPathElementDelimiterString() + fileName;

		// Creates directory
		File directory = new File(path);
		directory.mkdirs();

		// Writes local data to file
		try
		{
			localdataProperties.store(new FileOutputStream(fullFileName), "Data of: " + context.getCodeName());
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "LocalData", "ErrorOnWritingToPropertiesFile"), "--> File name searched for: '" + fullFileName + "'", e);
		}
	}
}
