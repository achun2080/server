package fmagic.basic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import fmagic.basic.ResourceContainer.OriginEnum;
import fmagic.basic.ResourceContainer.TypeEnum;
import fmagic.basic.ResourceContainer.UsageEnum;
import fmagic.test.TestManager;

/**
 * This class implements the management of configuration of a the fmagic system.
 * <p>
 * The configuration files are expected in the sub directory
 * <TT>fmagic.configuration</TT>. For each application (<TT>Code name</TT>) the
 * system searches for a properties file named
 * <TT>fmagic.*.configuration.properties</TT>, e. g.
 * <TT>fmagic.ap1.configuration.properties</TT>.
 * <p>
 * If there are running multiple applications on the same server you may use a
 * default properties file named
 * <TT>fmagic.default.configuration.properties</TT> containing the basic
 * configuration of all applications. If a property is not found in the
 * application properties file the system tries to find the property in the
 * default properties file. Even if you would set all properties in the default
 * properties file you have to create a configuration file for each running
 * application, at least as an empty file, because of the system expects a
 * properties file for each application.
 * <p>
 * Sometimes a property value contains a comma separated list. Instead of using
 * a comma separated list you may store all parameters in a configuration file,
 * each item written in a new line. You can name the configuration file
 * practically, e. g. 'something.conf', and refer to it with the name in curly
 * bracket, e. g. '{something.conf}'.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class ConfigurationManager implements ManagerInterface
{
	// List of properties
	private Properties applicationProperties = new Properties();
	private Properties defaultProperties = new Properties();
	private HashMap<String, String> notFoundProperties = new HashMap<String, String>();

	// Flags for synchronizing the locking of messages
	private ConcurrentHashMap<String, Boolean> processingActive = new ConcurrentHashMap<String, Boolean>();
	private int messageLostCounter = 0;

	/**
	 * Constructor
	 */
	public ConfigurationManager()
	{
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = { "Configuration" };
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

		String typeCriteria[] = { "Configuration" };
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

		String typeCriteria[] = { "Configuration" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceIdentifierList(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public boolean validateResources(Context context)
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
	 * 
	 * @param identifier
	 *            Identifier of property.
	 * 
	 * @param value
	 *            Value of property.
	 * 
	 */
	private void addProperty(String identifier, String value)
	{
		this.applicationProperties.put(identifier, value);
	}

	/**
	 * Get a configuration property by resource container.
	 * <p>
	 * Please notice: It is recommended to invoke the static method
	 * <TT>ResourceManager.configuration(context, group, name)</TT> to get a
	 * resource container. The signature <TT>configuration</TT> sets the
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
	 *            Default value of the property if it could not be found.
	 * 
	 * @param mandatory
	 *            Flag if the parameter must be set in the configuration file,
	 *            that means if it is mandatory. Please choose <TT>true</TT> or
	 *            <TT>false</TT>.
	 * 
	 * @return Returns value of property or <TT>null</TT>.
	 * 
	 */
	public String getProperty(Context context, ResourceContainer resourceContainer, String defaultValue, boolean mandatory)
	{
		// Check parameters
		if (resourceContainer == null) return defaultValue;

		// Get property identifier
		String identifier = resourceContainer.getRecourceIdentifier();
		if (identifier == null) return defaultValue;

		// Get the property value
		return this.getProperty(context, identifier, defaultValue, mandatory);
	}

	/**
	 * Get a configuration property by resource identifier.
	 * <p>
	 * Please notice: This method is overloaded with two different parameter
	 * settings. The first function expects a resource container
	 * <TT>ResourceContainer</TT> to define the property identifier. This method
	 * is set to <TT>public</TT> and should be invoked by all classes. The other
	 * function needs the property identifier directly as a <TT>String</TT>.
	 * This method is set to <TT>private</TT> use only.
	 * <p>
	 * Please notice: You have to avoid recursive calls of this method.
	 * Recursive calls can occur if configuration parameter is read during a
	 * running reading. Therefore the methods <TT>lockMessageHandling()</TT> and
	 * <TT>unlockMessageHandling()</TT> are used in a thread safe mode.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param identifier
	 *            The identifier of the property.
	 * 
	 * @param defaultValue
	 *            Default value of the property if it could not be found.
	 * 
	 * @param mandatory
	 *            Flag if the parameter must be set in the configuration file,
	 *            that means if it is mandatory. Please choose <TT>true</TT> or
	 *            <TT>false</TT>.
	 * 
	 * @return Returns value of property or <TT>null</TT>.
	 * 
	 */
	private String getProperty(Context context, String identifier, String defaultValue, boolean mandatory)
	{
		// Lock configuration processing
		if (this.lockMessageHandling("Configuration", identifier) == true) return defaultValue;

		// Variables
		String value = null;

		try
		{
			while (true)
			{
				// Create event if property was not found
				String applicationProperty = (String) this.applicationProperties.get(identifier);
				String defaultProperty = (String) this.defaultProperties.get(identifier);

				if ((applicationProperty == null || applicationProperty.length() == 0) && (defaultProperty == null || defaultProperty.length() == 0))
				{
					// The event is only to be notified once
					if (this.notFoundProperties.get(identifier) == null)
					{
						this.notFoundProperties.put(identifier, "");

						String additionaltext = "";
						additionaltext += "\n--> Identifier: " + identifier;

						if (defaultValue == null)
						{
							additionaltext += "\n--> No default value available; value was set to NULL";
						}
						else if (defaultValue.length() == 0)
						{
							additionaltext += "\n--> No default value available; value was set to EMPTY";
						}
						else
						{
							additionaltext += "\n--> Value was set according to the default to '" + defaultValue + "'";
						}

						/*
						 * Note: Please use the method getResourceContainer()
						 * instead of ResourceManager.notification() because the
						 * resource file is loaded after the configuration file
						 * and there are some problems if the configuration is
						 * not available.
						 */
						if (mandatory == true)
						{
							additionaltext = "--> Tried to read a mandatory configuration property, but it wasn't set in any configuration file." + additionaltext;
							context.getNotificationManager().notifyEvent(context, context.getResourceManager().getResourceContainer(context, TypeEnum.Notification, ApplicationManager.ApplicationIdentifierEnum.Basic, OriginEnum.All, UsageEnum.Event, "Configuration", "ConfigurationPropertyNotSet"), additionaltext, null);
						}
						else
						{
							additionaltext = "Tried to read configuration property, but it wasn't set in any configuration file." + additionaltext;
							context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, additionaltext);
						}
					}
				}

				// Read property value
				value = (String) this.applicationProperties.get(identifier);
				if (value == null || value.equals("")) value = (String) this.defaultProperties.get(identifier);
				if (value == null) value = defaultValue;
				if (value != null) value = value.trim();

				// Read additional configuration file if it is linked via
				// "${...}"
				if (value != null && value.startsWith("${") && value.endsWith("}"))
				{
					// If the application is running in "test mode", the test
					// environment is used instead of the regular environment.
					String fileName = null;

					if (context.isRunningInTestMode())
					{
						fileName = FileLocationFunctions.compileFilePath(TestManager.getTestConfigurationFilePath(context), value.substring(2, value.length() - 1));
					}
					else
					{
						fileName = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getConfigurationSubPath(), value.substring(2, value.length() - 1));
					}

					// Logging
					context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Tries to read addtional configuration file '" + value + "' of property key '" + identifier + "'");
					context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Full file name: '" + fileName + "'");

					// Read file content
					try
					{
						FileReader fileReader = new FileReader(fileName);

						BufferedReader bufferedReader = new BufferedReader(fileReader);

						String line = "";
						value = "";

						while ((line = bufferedReader.readLine()) != null)
						{
							// Jump comment "#"
							if (line.trim().startsWith("#")) continue;

							// Compose result string
							value += line;
						}

						fileReader.close();

						// Replace the old value of the property with the new
						// one to
						// avoid multiple searches in properties files.
						this.addProperty(identifier, value);

					}
					catch (Exception e)
					{
						context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "ErrorOnReadingParameterFile"), "--> File name: " + fileName, e);
						break;
					}
				}

				// Notify WATCHDOG
				this.notifyWatchdog(context, identifier, value, defaultValue);

				// Break
				break;
			}
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "ErrorOnReadingConfigurationProperty"), "--> Identifier: " + identifier, e);
		}
		finally
		{
			// Unlock message processing
			this.unlockMessageHandling("Configuration");
		}

		// Return
		return value;
	}

	/**
	 * Notify the WATCHDOG about configuration access.
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
	 */
	private void notifyWatchdog(Context context, String identifier, String value, String defaultValue)
	{
		try
		{
			// Set message Text
			String messageText = "Configuration property was read";

			// Set additional text
			String additionalText = "--> Configuration property was read";
			additionalText += "\n--> Identifier: '" + identifier + "'";

			if (defaultValue == null)
			{
				additionalText += "\n--> Default value was: '<NULL>'";
			}
			else
			{
				additionalText += "\n--> Default value was: '" + ResourceManager.hideSecurityValue(context, identifier, defaultValue) + "'";
			}

			if (value == null)
			{
				additionalText += "\n--> Configuration value result: '<NULL>'";
			}
			else
			{
				additionalText += "\n--> Configuration value result: '" + ResourceManager.hideSecurityValue(context, identifier, value) + "'";
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
	 * Get a configuration property by resource container as an <TT>integer</TT>
	 * value.
	 * <p>
	 * Please notice: It is recommended to invoke the static method
	 * <TT>ResourceManager.configuration(context, group, name)</TT> to get a
	 * resource container. The signature <TT>configuration</TT> sets the
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
	 *            Default value of the property if it could not be found.
	 * 
	 * @param mandatory
	 *            Flag if the parameter must be set in the configuration file,
	 *            that means if it is mandatory. Please choose <TT>true</TT> or
	 *            <TT>false</TT>.
	 * 
	 * @return Returns the read property value or the default value, if the
	 *         property was not found, or <TT>0</TT>, if the value could not be
	 *         converted to an integer value.
	 */
	public int getPropertyAsIntegerValue(Context context, ResourceContainer resourceContainer, int defaultValue, boolean mandatory)
	{
		// Check parameters
		if (resourceContainer == null) return defaultValue;

		// Get property identifier
		String identifier = resourceContainer.getRecourceIdentifier();
		if (identifier == null) return defaultValue;

		// Get the property value
		return getPropertyAsIntegerValue(context, identifier, defaultValue, mandatory);
	}

	/**
	 * Get a configuration property by resource identifier as an
	 * <TT>integer</TT> value.
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
	 * @param defaultValue
	 *            Default value of property if it could not be found.
	 * 
	 * @param mandatory
	 *            Flag if the parameter must be set in the configuration file,
	 *            that means if it is mandatory. Please choose <TT>true</TT> or
	 *            <TT>false</TT>.
	 * 
	 * @return Returns the read property value or the default value, if the
	 *         property was not found, or <TT>0</TT>, if the value could not be
	 *         converted to an integer value.
	 */
	private int getPropertyAsIntegerValue(Context context, String identifier, int defaultValue, boolean mandatory)
	{
		// Reads the current properties
		String valueAsString = getProperty(context, identifier, String.valueOf(defaultValue), mandatory);

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
	 * Get a configuration property by resource container as an <TT>boolean</TT>
	 * value.
	 * <p>
	 * Please notice: It is recommended to invoke the static method
	 * <TT>ResourceManager.configuration(context, group, name)</TT> to get a
	 * resource container. The signature <TT>configuration</TT> sets the
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
	 *            Default value of the property if it could not be found.
	 * 
	 * @param mandatory
	 *            Flag if the parameter must be set in the configuration file,
	 *            that means if it is mandatory. Please choose <TT>true</TT> or
	 *            <TT>false</TT>.
	 * 
	 * @return Returns the read property value or the default value, if the
	 *         property was not found, or <TT>0</TT>, if the value could not be
	 *         converted to an integer value.
	 */
	public boolean getPropertyAsBooleanValue(Context context, ResourceContainer resourceContainer, boolean defaultValue, boolean mandatory)
	{
		// Check parameters
		if (resourceContainer == null) return defaultValue;

		// Get property identifier
		String identifier = resourceContainer.getRecourceIdentifier();
		if (identifier == null) return defaultValue;

		// Get the property value
		return getPropertyAsBooleanValue(context, identifier, defaultValue, mandatory);
	}

	/**
	 * Get a configuration property by resource identifier as an
	 * <TT>boolean</TT> value.
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
	 * @param defaultValue
	 *            Default value of property if it could not be found.
	 * 
	 * @param mandatory
	 *            Flag if the parameter must be set in the configuration file,
	 *            that means if it is mandatory. Please choose <TT>true</TT> or
	 *            <TT>false</TT>.
	 * 
	 * @return Returns the read property value or the default value, if the
	 *         property was not found, or <TT>0</TT>, if the value could not be
	 *         converted to an integer value.
	 */
	private boolean getPropertyAsBooleanValue(Context context, String identifier, boolean defaultValue, boolean mandatory)
	{
		// Reads the current properties
		String valueAsString = getProperty(context, identifier, String.valueOf(defaultValue), mandatory);

		// Validate value
		if (valueAsString == null) return defaultValue;
		if (valueAsString.equals("")) return defaultValue;

		// Convert to integer
		boolean valueAsBoolean = defaultValue;

		try
		{
			Boolean currentValue = Boolean.valueOf(valueAsString);
			valueAsBoolean = currentValue;
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return valueAsBoolean;
	}

	/**
	 * Composes the path of the data directory of configuration properties
	 * files.
	 * <p>
	 * The data files were written into the sub directory
	 * <TT>fmagic.configuration</TT>.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the directory were the data files are stored.
	 * 
	 */
	private String getConfigurationFilePath(Context context)
	{
		return FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getConfigurationSubPath());
	}

	/**
	 * Composes the file name of configuration properties file.
	 * <p>
	 * The file name contains the prefix "fmagic", the code name of the
	 * application and the post fix "configuration.properties", e. g.
	 * <TT>fmagic.c1.configuration.properties</TT> for the code name "c1".
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the file name of the data files.
	 * 
	 */
	private String getConfigurationFileName(Context context)
	{
		String fileName = FileLocationFunctions.getConfigurationFileName();
		fileName = FileLocationFunctions.replacePlacholder(context, fileName);
		return fileName;
	}

	/**
	 * Composes the path of the data directory of template configuration
	 * properties files.
	 * <p>
	 * The data files were written into the sub directory
	 * <TT>fmagic.configuration</TT>.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the directory were the data files are stored.
	 * 
	 */
	private String getConfigurationTemplateFilePath(Context context)
	{
		return FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getConfigurationTemplateSubPath());
	}

	/**
	 * Composes the file name of template configuration properties file.
	 * <p>
	 * The file name contains the prefix "fmagic.template", the name of the
	 * application, the attribute "client" or "server", and the post fix
	 * "configuration.properties", e. g.
	 * <TT>fmagic.template.seniorcitizen.server.configuration.properties</TT>
	 * for the application "Senior Citizen".
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the file name of the data files, or <TT>null</TT>.
	 * 
	 */
	private String getConfigurationTemplateFileName(Context context, String application, String origin)
	{
		try
		{
			String fileName = FileLocationFunctions.getConfigurationTemplateFileName();
			fileName = FileLocationFunctions.replacePlacholder(context, fileName);
			return fileName;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Load configuration properties from properties file.
	 * <p>
	 * The properties files were searched in the sub directory
	 * "fmagic.configuration".
	 * <p>
	 * The name of the properties files are composed by the prefix "fmagic", the
	 * code name of the application and the post fix "configuration.properties",
	 * e. g. <TT>fmagic.c1.configuration.properties</TT> for code name "c1".
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns <TT>true</TT> if the configuration file could be read,
	 *         otherwise <TT>false</TT>.
	 */
	public boolean loadPropertiesFile(Context context)
	{
		String fileName = null;

		// Read application properties. If the application is running in "test
		// mode", the test environment is used instead of the regular
		// environment.
		if (context.isRunningInTestMode())
		{
			fileName = FileLocationFunctions.compileFilePath(TestManager.getTestConfigurationFilePath(context), this.getConfigurationFileName(context));
		}
		else
		{
			fileName = FileLocationFunctions.compileFilePath(this.getConfigurationFilePath(context), this.getConfigurationFileName(context));
		}

		try
		{
			applicationProperties.load(new FileInputStream(fileName));
		}
		catch (Exception e)
		{
			/*
			 * Note: Please use the method getResourceContainer() instead of
			 * ResourceManager.notification() because the resource file is
			 * loaded after the configuration file and there are some problems
			 * if the configuration is not available.
			 */
			context.getNotificationManager().notifyError(context, context.getResourceManager().getResourceContainer(context, TypeEnum.Notification, ApplicationManager.ApplicationIdentifierEnum.Basic, OriginEnum.All, UsageEnum.Error, "Configuration", "ErrorOnReadingConfigurationFile"), "--> File name: " + fileName, e);
			return false;
		}

		// Read default properties. If the application is running in "test
		// mode", the test environment is used instead of the regular
		// environment.
		if (context.isRunningInTestMode())
		{
			fileName = FileLocationFunctions.compileFilePath(TestManager.getTestConfigurationFilePath(context), FileLocationFunctions.getConfigurationDefaultFileName());
		}
		else
		{
			fileName = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getConfigurationDefaultSubPath(), FileLocationFunctions.getConfigurationDefaultFileName());
		}

		try
		{
			defaultProperties.load(new FileInputStream(fileName));
		}
		catch (Exception e)
		{
			/*
			 * Note: Please use the method getResourceContainer() instead of
			 * ResourceManager.notification() because the resource file is
			 * loaded after the configuration file and there are some problems
			 * if the configuration is not available.
			 */
			context.getNotificationManager().notifyError(context, context.getResourceManager().getResourceContainer(context, TypeEnum.Notification, ApplicationManager.ApplicationIdentifierEnum.Basic, OriginEnum.All, UsageEnum.Error, "Configuration", "ErrorOnReadingConfigurationFile"), "--> File name: " + fileName, e);
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Create a template properties files for a specific application.
	 * <p>
	 * The name of the template file is composed by the prefix <TT>template</TT>
	 * , the name of the application, the origin <TT>Client</TT> or
	 * <TT>Server</TT>, and the post fix "fmagic.properties", e. g.
	 * <TT>template.seniorcitizen.server.fmagic.properties</TT>.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns <TT>true</TT> if the configuration template file could be
	 *         created, otherwise <TT>false</TT>.
	 */
	public synchronized boolean createTemplateConfigurationFile(Context context, String application, String origin, boolean includingResourceIdentifiers)
	{
		// If the application is running in "test
		// mode", the test template files are not created.
		if (context.isRunningInTestMode()) return true;

		// Check parameter
		boolean isError = false;
		if (application == null) isError = true;
		if (application.length() == 0) isError = true;
		if (origin == null) isError = true;
		if (origin.length() == 0) isError = true;

		if (isError == true)
		{
			context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "Configuration", "ErrorOnWritingConfigurationTemplateFile"), "--> Parameter missing: Application [" + application + "], Origin [" + origin + "]", null);
			return false;
		}

		// Define variables
		String fileName = null;
		String pathName = null;
		PrintWriter output;

		// Create file directory for template files
		try
		{
			pathName = this.getConfigurationTemplateFilePath(context);

			File directory = new File(pathName);
			directory.mkdirs();
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "Configuration", "ErrorOnWritingConfigurationTemplateFile"), "--> Path name: " + pathName, e);
			return false;
		}

		// Write text to the template file
		try
		{
			// Create template text
			fileName = FileLocationFunctions.compileFilePath(this.getConfigurationTemplateFilePath(context), this.getConfigurationTemplateFileName(context, application, origin));

			String typeCriteria[] = { "Configuration" };
			String applicationCriteria[] = { "Basic", "Common", application };
			String originCriteria[] = { origin, "All" };
			String usageCriteria[] = { "Property" };
			String groupCriteria[] = null;
			String templateText = context.getResourceManager().printResourceTemplate(context, includingResourceIdentifiers, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

			output = new PrintWriter(new FileOutputStream(new File(fileName), false));
			output.append(templateText);
			output.close();
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "Configuration", "ErrorOnWritingConfigurationTemplateFile"), "--> File name: " + fileName, e);
		}

		// Return
		return true;
	}

	/**
	 * Print configuration settings.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the formatted configuration settings list.
	 */
	public String printConfigurationSettings(Context context)
	{
		// Initialize
		String resultString = "";

		// Print complete list of properties
		try
		{
			// Headline
			resultString += "\n\n\n### Configuration settings\n";

			// Sorting the keys alphabetically
			Set<String> listConfigurationItems = new HashSet<String>();

			for (Object identifier : this.defaultProperties.keySet())
			{
				if (identifier != null)
				{
					listConfigurationItems.add((String) identifier);
				}
			}

			for (Object identifier : this.applicationProperties.keySet())
			{
				if (identifier != null)
				{
					listConfigurationItems.add((String) identifier);
				}
			}

			List<String> sortedListConfigurationItems = new ArrayList<String>(listConfigurationItems);
			Collections.sort(sortedListConfigurationItems);

			// Print all items into list
			Iterator<String> iterConfigurationItems = sortedListConfigurationItems.iterator();

			while (iterConfigurationItems.hasNext())
			{
				String identifier = iterConfigurationItems.next();
				String value = (String) this.applicationProperties.get(identifier);
				if (value == null || value.length() == 0) value = (String) this.defaultProperties.get(identifier);

				if (value != null && value.length() > 0)
				{
					resultString += "\n" + identifier.trim() + " = " + ResourceManager.hideSecurityValue(context, identifier, value);
				}
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Print list set in application properties
		try
		{
			// Headline
			resultString += "\n\n\n### Therefrom directly defined in application [" + context.getCodeName() + "] configuration file\n";

			// Sorting the keys alphabetically
			List<String> sortedListConfigurationItems = new ArrayList<String>();

			for (Object identifier : this.applicationProperties.keySet())
			{
				if (identifier != null)
				{
					sortedListConfigurationItems.add((String) identifier);
				}
			}

			Collections.sort(sortedListConfigurationItems);

			// Print all items into list
			Iterator<String> iterConfigurationItems = sortedListConfigurationItems.iterator();

			while (iterConfigurationItems.hasNext())
			{
				String identifier = iterConfigurationItems.next();
				String value = (String) this.applicationProperties.get(identifier);

				if (value != null && value.length() > 0)
				{
					resultString += "\n" + identifier.trim() + " = " + ResourceManager.hideSecurityValue(context, identifier, value);
				}
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Print list set in default properties
		try
		{
			// Headline
			resultString += "\n\n\n### Therefrom indirectly defined in default configuration file\n";

			// Sorting the keys alphabetically
			List<String> sortedListConfigurationItems = new ArrayList<String>();

			for (Object identifier : this.defaultProperties.keySet())
			{
				if (identifier != null)
				{
					sortedListConfigurationItems.add((String) identifier);
				}
			}

			Collections.sort(sortedListConfigurationItems);

			// Print all items into list
			Iterator<String> iterConfigurationItems = sortedListConfigurationItems.iterator();

			while (iterConfigurationItems.hasNext())
			{
				String identifier = iterConfigurationItems.next();
				String value = (String) this.defaultProperties.get(identifier);

				if (value != null && value.length() > 0)
				{
					resultString += "\n" + identifier.trim() + " = " + ResourceManager.hideSecurityValue(context, identifier, value);
				}
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return resultString;
	}
}
