package fmagic.server.media;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import fmagic.basic.command.ConnectionContainer;
import fmagic.basic.context.Context;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.file.FileLocationFunctions;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.media.MediaManager;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.command.ClientCommandMediaFileInfo;

/**
 * This class implements the management of media of server applications.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 30.03.2013 - Created
 * 
 */
public class ServerMediaManager extends MediaManager
{
	// Configuration parameter
	private boolean enableLocalRepository = true;

	// Settings for Media Server Pool
	private final HashMap<Integer, ConnectionContainer> mediaPoolList = new HashMap<Integer, ConnectionContainer>();
	private int poolMainServerNumber = 0;
	private boolean enableMediaPool = false;
	private int maximumNuOfItemsInCommandQueue = 0;
	private int secondsToWaitBetweenCommandProcessing = 0;

	// List of media server commands (requests) to process
	private Queue<ServerMediaPoolCommand> commandMainQueue = new LinkedList<ServerMediaPoolCommand>();
	private Queue<ServerMediaPoolCommand> commandSecondaryQueue = new LinkedList<ServerMediaPoolCommand>();
	private Queue<ServerMediaPoolCommand> commandSynchronizingQueue = new LinkedList<ServerMediaPoolCommand>();

	/**
	 * Constructor
	 */
	public ServerMediaManager()
	{
		super();
	}

	@Override
	public boolean readConfiguration(Context context)
	{
		boolean isError = super.readConfiguration(context);

		if (readConfigurationEncodingKeyList(context) == true) isError = true;
		if (readConfigurationEncodingKeyNumber(context) == true) isError = true;
		if (readConfigurationEncodingEnabled(context) == true) isError = true;
		if (readConfigurationLocalMediaRepository(context) == true) isError = true;
		if (readConfigurationMediaPool(context) == true) isError = true;

		return isError;
	}

	/**
	 * Read configuration parameters regarding th media pool.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean readConfigurationMediaPool(Context context)
	{
		// Initialize variables
		boolean isError = false;
		ResourceContainer resourceContainer = null;

		try
		{
			// Read configuration parameter: MediaPoolList
			if (this.readConfigurationMediaPoolList(context) == true) isError = true;

			// Read configuration parameter: PoolMainServerNumber
			if (this.readConfigurationPoolMainServerNumber(context) == true) isError = true;

			// Read configuration parameter: EnableMediaPool
			resourceContainer = ResourceManager.configuration(context, "MediaPool", "EnableMediaPool");
			this.enableMediaPool = context.getConfigurationManager().getPropertyAsBooleanValue(context, resourceContainer, false);

			// Read configuration parameter: MaximumNuOfItemsInCommandQueue
			resourceContainer = ResourceManager.configuration(context, "MediaPool", "MaximumNuOfItemsInCommandQueue");
			this.maximumNuOfItemsInCommandQueue = context.getConfigurationManager().getPropertyAsIntegerValue(context, resourceContainer, false);

			// Read configuration parameter:
			// SecondsToWaitBetweenCommandProcessing
			resourceContainer = ResourceManager.configuration(context, "MediaPool", "SecondsToWaitBetweenCommandProcessing");
			this.secondsToWaitBetweenCommandProcessing = context.getConfigurationManager().getPropertyAsIntegerValue(context, resourceContainer, false);

			// Validate settings
			if (this.enableMediaPool == true && this.mediaPoolList.size() == 0)
			{
				String errorString = "--> There are no hosts set for the media pool, though the media pool is enabled.";
				errorString += "\n--> Please set the media pool host list explicitly.";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
				return true;
			}

			// Validate settings
			if (this.enableMediaPool == true && this.poolMainServerNumber <= 0)
			{
				String errorString = "--> There are no main server number set for the media pool, though the media pool is enabled.";
				errorString += "\n--> Please set the main server number explicitly.";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
				return true;
			}
		}
		catch (Exception e)
		{
			String errorString = "--> Unexpected error on reading media configuration properties";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, e);
			return true;
		}

		// Return
		return isError;
	}

	/**
	 * Read the current 'LocalMediaFilePathRoot' value of server or client.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean readConfigurationLocalMediaRepository(Context context)
	{
		ResourceContainer resourceContainer = null;

		try
		{
			// Read configuration parameter: EnableLocalRepository
			resourceContainer = ResourceManager.configuration(context, "MediaPool", "EnableLocalRepository");
			this.enableLocalRepository = context.getConfigurationManager().getPropertyAsBooleanValue(context, resourceContainer, false);

			// Read configuration parameter: ServerLocalMediaFilePathRoot
			resourceContainer = ResourceManager.configuration(context, "Media", "ServerLocalMediaFilePathRoot");
			this.mediaRootFilePath = context.getConfigurationManager().getProperty(context, resourceContainer, false);

			// Validate settings
			if (this.enableLocalRepository == true && (this.getMediaRootFilePath() == null || this.getMediaRootFilePath().length() == 0))
			{
				String errorString = "--> Media configuration property 'ServerLocalMediaFilePathRoot' is not set.";
				errorString += "\n--> Please set the media root file path explicitly.";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
				return true;
			}
		}
		catch (Exception e)
		{
			String errorString = "--> Unexpected error on reading media configuration properties";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, e);
			return true;
		}

		// Return
		return false;
	}

	/**
	 * Read the current 'EncodingKeyList' value of server or client.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean readConfigurationEncodingKeyList(Context context)
	{
		// Initialize variables
		boolean isError = false;

		// Read configuration item
		ResourceContainer resourceContainer = ResourceManager.configuration(context, "Media", "ServerEncodingKeyList");
		String serverMediaKeyListString = context.getConfigurationManager().getProperty(context, resourceContainer, false);

		// Get key list
		if (serverMediaKeyListString != null && serverMediaKeyListString.length() > 0)
		{
			try
			{
				String keyListParts[] = serverMediaKeyListString.split(",");

				if (keyListParts.length > 0)
				{
					for (int i = 0; i < keyListParts.length; i++)
					{
						String listItemParts[] = keyListParts[i].split(":");

						if (listItemParts.length != 2)
						{
							isError = true;
						}
						else
						{
							int number = Integer.parseInt(listItemParts[0].trim());
							if (number < 1) isError = true;

							String key = listItemParts[1];
							if (key == null || key.trim().length() == 0) isError = true;

							if (isError == false) this.encodingKeyList.put(number, key.trim());
						}

						if (isError == true) break;
					}
				}
			}
			catch (Exception e)
			{
				isError = true;
			}
		}

		// Process error message
		if (isError == true)
		{
			String errorString = "--> Error on parsing server encoding key list.";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Value parsed: '" + serverMediaKeyListString + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "ErrorOnParsingConfigurationList"), errorString, null);
		}

		// Check size of key, there must be at least 8 characters
		if (isError == false)
		{
			for (int keyNumber : this.encodingKeyList.keySet())
			{
				String keyValue = this.encodingKeyList.get(keyNumber);

				if (keyValue == null || keyValue.length() < 8)
				{
					String errorString = "--> Error on server encoding key list, on key number '" + String.valueOf(keyNumber) + "'.";
					errorString += "\n--> Key value must be at least 8 characters long.";
					errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
					errorString += "\n--> Key value: '" + keyValue + "'";

					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
					isError = true;
				}
			}
		}

		// Return
		return isError;
	}

	/**
	 * Read the current 'EncodingKeyNumber' value of server or client.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean readConfigurationEncodingKeyNumber(Context context)
	{
		// Read parameter value
		ResourceContainer resourceContainer = ResourceManager.configuration(context, "Media", "ServerEncodingKeyNumber");
		String configurationServerEncodingKeyNumberString = context.getConfigurationManager().getProperty(context, resourceContainer, false);

		// Key number is not set
		if (configurationServerEncodingKeyNumberString == null) return false;

		// Convert key number to integer
		int configurationServerEncodingKeyNumberInteger = 0;

		try
		{
			configurationServerEncodingKeyNumberInteger = Integer.parseInt(configurationServerEncodingKeyNumberString);
		}
		catch (Exception e)
		{
			String errorString = "--> Server encoding key number is not an integer value.";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Server encoding key number: '" + String.valueOf(configurationServerEncodingKeyNumberString) + "'";
			errorString += "\n--> Available key numbers of server encoding keys: '" + this.encodingKeyList.keySet().toString() + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
			return true;
		}

		// Check parameter value
		if (this.encodingKeyList.get(configurationServerEncodingKeyNumberInteger) == null)
		{
			String errorString = "--> Server encoding key number is not part of the key list.";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Server encoding key number: '" + String.valueOf(configurationServerEncodingKeyNumberString) + "'";
			errorString += "\n--> Available key numbers of server encoding keys: '" + this.encodingKeyList.keySet().toString() + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
			return true;
		}

		this.encodingKeyNumber = configurationServerEncodingKeyNumberInteger;

		return false;
	}

	/**
	 * Read the current 'EncodingEnabled' value of server or client.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean readConfigurationEncodingEnabled(Context context)
	{
		ResourceContainer resourceContainer = ResourceManager.configuration(context, "Media", "ServerEncodingEnabled");
		this.encodingEnabled = context.getConfigurationManager().getPropertyAsBooleanValue(context, resourceContainer, false);

		// Check parameter value
		if (this.encodingEnabled == true && this.encodingKeyNumber == 0)
		{
			String errorString = "--> Server encoding is enabled but no encoding key is set.";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Please set encoding key, or disable server encoding.";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
			return true;
		}

		return false;
	}

	@Override
	public boolean isEncodingEnabled(Context context, ResourceContainerMedia mediaResourceContainer)
	{
		boolean encodingEnabled = true;

		if (mediaResourceContainer.attributeIsServerEncoding(context) == false) encodingEnabled = false;
		if (this.encodingEnabled == false) encodingEnabled = false;
		if (this.getEncodingValue(context) == null) encodingEnabled = false;

		return encodingEnabled;
	}

	/**
	 * Read media pool list from configuration.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean readConfigurationMediaPoolList(Context context)
	{
		// Initialize variables
		boolean isError = false;
		String errorString = "--> Error on parsing media pool list.";

		// Read configuration item
		ResourceContainer resourceContainer = ResourceManager.configuration(context, "MediaPool", "MediaPoolList");
		String mediaPoolListString = context.getConfigurationManager().getProperty(context, resourceContainer, false);

		// Get key list
		if (mediaPoolListString != null && mediaPoolListString.length() > 0)
		{
			try
			{
				String keyListParts[] = mediaPoolListString.split(",");

				if (keyListParts.length > 0)
				{
					for (int i = 0; i < keyListParts.length; i++)
					{
						String listItemParts[] = keyListParts[i].split(":");

						if (listItemParts.length != 3)
						{
							errorString += "\n--> Value of 'Number', 'Host' or 'Port' not set";
							isError = true;
						}
						else
						{
							// Read number
							String numberString = listItemParts[0].trim();
							Integer number = null;

							if (numberString == null || numberString.trim().length() == 0)
							{
								errorString += "\n--> Number not set";
								isError = true;
								continue;
							}

							try
							{
								number = Integer.valueOf(numberString);
							}
							catch (Exception e)
							{
								errorString += "\n--> Number '" + numberString + "' must be an integer value";
								isError = true;
								continue;
							}

							if (number != null && number < 1)
							{
								errorString += "\n--> Number '" + String.valueOf(number) + "' must be greater than 0";
								isError = true;
								continue;
							}

							// Read host
							String host = listItemParts[1];

							if (host == null || host.trim().length() == 0)
							{
								errorString += "\n--> Host not set";
								isError = true;
								continue;
							}

							// Read port
							String portString = listItemParts[2];
							Integer port = null;

							if (portString == null || portString.trim().length() == 0)
							{
								errorString += "\n--> Port not set";
								isError = true;
								continue;
							}
							else
							{
								try
								{
									port = Integer.valueOf(portString);
								}
								catch (Exception e)
								{
									errorString += "\n--> Port '" + portString + "' must be an integer value";
									isError = true;
									continue;
								}
							}

							// Save to media pool list
							ConnectionContainer container = new ConnectionContainer(number, host, port);
							this.mediaPoolList.put(number, container);
						}
					}
				}
			}
			catch (Exception e)
			{
				errorString += "\n--> Unexpected exception";
				errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Value parsed: '" + mediaPoolListString + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "ErrorOnParsingConfigurationList"), errorString, e);
				isError = true;
			}
		}

		// Process error message
		if (isError == true)
		{
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Value parsed: '" + mediaPoolListString + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "ErrorOnParsingConfigurationList"), errorString, null);
		}

		// Return
		return isError;
	}

	/**
	 * Read the configuration property 'Main Server Number' that is supposed to
	 * be starting uploading or reading media files in a pool.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean readConfigurationPoolMainServerNumber(Context context)
	{
		// Read parameter value
		ResourceContainer resourceContainer = ResourceManager.configuration(context, "MediaPool", "PoolMainServerNumber");
		String poolMainServerNumberString = context.getConfigurationManager().getProperty(context, resourceContainer, false);

		// Number is not set
		if (poolMainServerNumberString == null) return false;

		// Convert key number to integer
		int poolMainServerNumberInteger = 0;

		try
		{
			poolMainServerNumberInteger = Integer.parseInt(poolMainServerNumberString);
		}
		catch (Exception e)
		{
			String errorString = "--> Main server number for media pool access is not an integer value.";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Main server number: '" + String.valueOf(poolMainServerNumberString) + "'";
			errorString += "\n--> Available item numbers of media pool: '" + this.mediaPoolList.keySet().toString() + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
			return true;
		}

		// Check parameter value
		if (this.mediaPoolList.get(poolMainServerNumberInteger) == null)
		{
			String errorString = "--> Main server number for media pool access is not part of the media server host list.";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Main server number: '" + String.valueOf(poolMainServerNumberString) + "'";
			errorString += "\n--> Available item numbers of media pool: '" + this.mediaPoolList.keySet().toString() + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
			return true;
		}

		// Set result
		this.poolMainServerNumber = poolMainServerNumberInteger;

		// Return
		return false;
	}

	/**
	 * Wait for the end of processing of all orders of all media server command
	 * queues.
	 */
	void waitForCompletingMediaServerCommandQueues()
	{
		while (true)
		{
			if (this.getNumberOfCommandsInMainQueue() <= 0 && this.getNumberOfCommandsInSecondaryQueue() <= 0 && this.getNumberOfCommandsInSynchronizingQueue() <= 0) break;

			try
			{
				Thread.sleep(1000);
			}
			catch (Exception e)
			{
			}
		}

		return;
	}

	/**
	 * Get number of elements in media server <TT>main</TT> command queue.
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @return Returns the number of elements in media server <TT>main</TT>
	 *         command queue.
	 */
	int getNumberOfCommandsInMainQueue()
	{
		int size = 0;

		synchronized (this.commandMainQueue)
		{
			size = this.commandMainQueue.size();
		}

		return size;
	}

	/**
	 * Get number of elements in media server <TT>secondary</TT> command queue.
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @return Returns the number of elements in media server <TT>secondary</TT>
	 *         command queue.
	 */
	int getNumberOfCommandsInSecondaryQueue()
	{
		int size = 0;

		synchronized (this.commandSecondaryQueue)
		{
			size = this.commandSecondaryQueue.size();
		}

		return size;
	}

	/**
	 * Get number of elements in media server <TT>synchronizing</TT> command
	 * queue.
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @return Returns the number of elements in media server
	 *         <TT>synchronizing</TT> command queue.
	 */
	int getNumberOfCommandsInSynchronizingQueue()
	{
		int size = 0;

		synchronized (this.commandSynchronizingQueue)
		{
			size = this.commandSynchronizingQueue.size();
		}

		return size;
	}

	/**
	 * Get the next element of media server <TT>main</TT> command queue.
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @return Returns the next media server <TT>main</TT> command to process,
	 *         or <TT>null</TT>
	 */
	ServerMediaPoolCommand pollNextCommandFromMainQueue()
	{
		ServerMediaPoolCommand command = null;

		synchronized (this.commandMainQueue)
		{
			command = this.commandMainQueue.poll();
		}

		return command;
	}

	/**
	 * Add a command to the media server <TT>main</TT> command queue.
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @param context
	 *            The application context.
	 * 
	 * @param mediaResourceIdentifier
	 *            The media resource identifier of the media to be uploaded.
	 * 
	 * @param mediaPendingFileName
	 *            The full path of the pending media file. After processing this
	 *            file by the media server into the media pool, this file will
	 *            be deleted automatically. Please do not hand over a source
	 *            media file directly.
	 * 
	 * @param dataIdentifier
	 *            The data identifier of the media to be added.
	 * 
	 * @return Returns <TT>true</TT> if the command could be added, otherwise
	 *         <TT>false</TT>
	 */
	private boolean addCommandToMainQueue(Context context, String mediaResourceIdentifier, String mediaPendingFileName, String dataIdentifier)
	{
		// Check parameter
		if (mediaResourceIdentifier == null || mediaResourceIdentifier.length() == 0) return false;
		if (mediaPendingFileName == null || mediaPendingFileName.length() == 0) return false;
		if (dataIdentifier == null || dataIdentifier.length() == 0) return false;

		// Process
		try
		{
			// Check if there are more than the maximum allowed number of
			// elements in the queue.
			if (this.getNumberOfCommandsInMainQueue() >= this.maximumNuOfItemsInCommandQueue)
			{
				// Error: MaximumNumberOfCommandsExceeded
				String errorText = "--> Maximum number of allowed items in media server MAIN command queue exceeded";
				errorText += "--> Maximum number allowed: '" + String.valueOf(this.maximumNuOfItemsInCommandQueue) + "'";
				errorText += "\n--> Media resource identifier to be added: '" + mediaResourceIdentifier + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "MediaServer", "MaximumNumberOfCommandsExceeded"), errorText, null);

				// Error: WatchdogItemLost
				errorText = "--> Because the maximum of allowed items of '" + String.valueOf(this.maximumNuOfItemsInCommandQueue) + "' in media server MAIN command queue was exceeded.";
				errorText += "\n--> Media resource identifier to be added: '" + mediaResourceIdentifier + "'";
				context.getNotificationManager().notifyWatchdogError(context, ResourceManager.notification(context, "MediaServer", "MediaCommandItemLost"), errorText, null);

				// Return
				return false;
			}

			// Create command object
			ServerMediaPoolCommand command = new ServerMediaPoolCommand(context, mediaResourceIdentifier, mediaPendingFileName, dataIdentifier);

			// Add to queue
			synchronized (this.commandMainQueue)
			{
				this.commandMainQueue.add(command);
			}

			// Logging
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media pool command added to MAIN queue: " + this.getNumberOfCommandsInMainQueue() + " items");
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, command.toString());

			// Return
			return true;
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), null, e);
			return false;
		}
	}

	/**
	 * Add a command to the media server <TT>secondary</TT> command queue.
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @param context
	 *            The application context.
	 * 
	 * @param mediaResourceIdentifier
	 *            The media resource identifier of the media to be uploaded.
	 * 
	 * @param mediaPendingFileName
	 *            The full path of the pending media file. After processing this
	 *            file by the media server into the media pool, this file will
	 *            be deleted automatically. Please do not hand over a source
	 *            media file directly.
	 * 
	 * @param dataIdentifier
	 *            The data identifier of the media to be added.
	 * 
	 * @return Returns <TT>true</TT> if the command could be added, otherwise
	 *         <TT>false</TT>
	 */
	private boolean addCommandToSecondaryQueue(Context context, String mediaResourceIdentifier, String mediaPendingFileName, String dataIdentifier)
	{
		// Check parameter
		if (mediaResourceIdentifier == null || mediaResourceIdentifier.length() == 0) return false;
		if (mediaPendingFileName == null || mediaPendingFileName.length() == 0) return false;
		if (dataIdentifier == null || dataIdentifier.length() == 0) return false;

		// Process
		try
		{
			// Check if there are more than the maximum allowed number of
			// elements in the queue.
			if (this.getNumberOfCommandsInSecondaryQueue() >= this.maximumNuOfItemsInCommandQueue)
			{
				// Error: MaximumNumberOfCommandsExceeded
				String errorText = "--> Maximum number of allowed items in media server SECONDARY command queue exceeded";
				errorText += "--> Maximum number allowed: '" + String.valueOf(this.maximumNuOfItemsInCommandQueue) + "'";
				errorText += "\n--> Media resource identifier to be added: '" + mediaResourceIdentifier + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "MediaServer", "MaximumNumberOfCommandsExceeded"), errorText, null);

				// Error: WatchdogItemLost
				errorText = "--> Because the maximum of allowed items of '" + String.valueOf(this.maximumNuOfItemsInCommandQueue) + "' in media server SECONDARY command queue was exceeded.";
				errorText += "\n--> Media resource identifier to be added: '" + mediaResourceIdentifier + "'";
				context.getNotificationManager().notifyWatchdogError(context, ResourceManager.notification(context, "MediaServer", "MediaCommandItemLost"), errorText, null);

				// Return
				return false;
			}

			// Create command object
			ServerMediaPoolCommand command = new ServerMediaPoolCommand(context, mediaResourceIdentifier, mediaPendingFileName, dataIdentifier);

			// Add to queue
			synchronized (this.commandSecondaryQueue)
			{
				this.commandSecondaryQueue.add(command);
			}

			// Logging
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media pool command added to SECONDARY queue: " + this.getNumberOfCommandsInSecondaryQueue() + " items");
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, command.toString());

			// Return
			return true;
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), null, e);
			return false;
		}
	}

	/**
	 * Add a command to the media server <TT>synchronizing</TT> command queue.
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @param context
	 *            The application context.
	 * 
	 * @param mediaResourceIdentifier
	 *            The media resource identifier of the media to be uploaded.
	 * 
	 * @param dataIdentifier
	 *            The data identifier of the media to be added.
	 * 
	 * @return Returns <TT>true</TT> if the command could be added, otherwise
	 *         <TT>false</TT>
	 */
	private boolean addCommandToSynchronizingQueue(Context context, String mediaResourceIdentifier, String dataIdentifier)
	{
		// Check parameter
		if (mediaResourceIdentifier == null || mediaResourceIdentifier.length() == 0) return false;
		if (dataIdentifier == null || dataIdentifier.length() == 0) return false;

		// Process
		try
		{
			// Check if there are more than the maximum allowed number of
			// elements in the queue.
			if (this.getNumberOfCommandsInSynchronizingQueue() >= this.maximumNuOfItemsInCommandQueue)
			{
				// Error: MaximumNumberOfCommandsExceeded
				String errorText = "--> Maximum number of allowed items in media server SYNCHRONIZING command queue exceeded";
				errorText += "--> Maximum number allowed: '" + String.valueOf(this.maximumNuOfItemsInCommandQueue) + "'";
				errorText += "\n--> Media resource identifier to be added: '" + mediaResourceIdentifier + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "MediaServer", "MaximumNumberOfCommandsExceeded"), errorText, null);

				// Error: WatchdogItemLost
				errorText = "--> Because the maximum of allowed items of '" + String.valueOf(this.maximumNuOfItemsInCommandQueue) + "' in media server SYNCHRONIZING command queue was exceeded.";
				errorText += "\n--> Media resource identifier to be added: '" + mediaResourceIdentifier + "'";
				context.getNotificationManager().notifyWatchdogError(context, ResourceManager.notification(context, "MediaServer", "MediaCommandItemLost"), errorText, null);

				// Return
				return false;
			}

			// Create command object
			ServerMediaPoolCommand command = new ServerMediaPoolCommand(context, mediaResourceIdentifier, null, dataIdentifier);

			// Add to queue
			synchronized (this.commandSynchronizingQueue)
			{
				this.commandSynchronizingQueue.add(command);
			}

			// Logging
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media pool command added to SYNCHRONIZING queue: " + this.getNumberOfCommandsInSynchronizingQueue() + " items");
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, command.toString());

			// Return
			return true;
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), null, e);
			return false;
		}
	}

	/**
	 * Get the next element of media server <TT>secondary</TT> command queue.
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @return Returns the next media server <TT>secondary</TT> command to
	 *         process, or <TT>null</TT>
	 */
	ServerMediaPoolCommand pollNextCommandFromSecondaryQueue()
	{
		ServerMediaPoolCommand command = null;

		synchronized (this.commandSecondaryQueue)
		{
			command = this.commandSecondaryQueue.poll();
		}

		return command;
	}

	/**
	 * Get the next element of media server <TT>synchronizing</TT> command
	 * queue.
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @return Returns the next media server <TT>synchronizing</TT> command to
	 *         process, or <TT>null</TT>
	 */
	ServerMediaPoolCommand pollNextCommandFromSynchronizingQueue()
	{
		ServerMediaPoolCommand command = null;

		synchronized (this.commandSynchronizingQueue)
		{
			command = this.commandSynchronizingQueue.poll();
		}

		return command;
	}

	/**
	 * Check if a media file already exists on a media server pool. Only the
	 * most recent media file is searched for on server, not any obsolete files.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param fileType
	 *            File type of the file to check.
	 * 
	 * @param hashValue
	 *            Hash value of the file to check.
	 * 
	 * @param dataIdentifier
	 *            The identifier of the concrete media item to check.
	 * 
	 * @return Returns <TT>true</TT> if the media file exists, otherwise
	 *         <TT>false</TT>.
	 */
	protected boolean poolCheckMediaFileOnPool(Context context, ResourceContainerMedia mediaResourceContainer, String fileType, String dataIdentifier, String hashValue)
	{
		/*
		 * Check variables and conditions
		 */

		// Check if media pool is enabled
		if (!this.isEnableMediaPool()) { return false; }

		// Check media resource container
		if (mediaResourceContainer == null)
		{
			String errorString = "--> CHECK ON POOL: Media resource container not set (NULL value).";
			if (fileType != null) errorString += "\n--> File type of media: '" + fileType + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			if (hashValue != null) errorString += "\n--> Hash value of media: '" + hashValue + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return false;
		}

		// Check file type
		if (fileType == null || fileType.length() == 0)
		{
			String errorString = "--> CHECK ON POOL: Missing file type of the file to be checked (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (fileType != null) errorString += "\n--> File type of media: '" + fileType + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			if (hashValue != null) errorString += "\n--> Hash value of media: '" + hashValue + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return false;
		}

		// Check data identifier
		if (dataIdentifier == null || dataIdentifier.length() == 0)
		{
			String errorString = "--> CHECK ON POOL: Missing data identifier of media (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (fileType != null) errorString += "\n--> File type of media: '" + fileType + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			if (hashValue != null) errorString += "\n--> Hash value of media: '" + hashValue + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return false;
		}

		// Check hash value
		if (hashValue == null || hashValue.length() == 0)
		{
			String errorString = "--> CHECK ON POOL: Missing hash value of media (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (fileType != null) errorString += "\n--> File type of media: '" + fileType + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			if (hashValue != null) errorString += "\n--> Hash value of media: '" + hashValue + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return false;
		}

		// Checks if file type allowed for the given media resource item
		if (mediaResourceContainer.attributeIsFileTypeSupported(context, fileType) == false)
		{
			String errorString = "--> CHECK ON POOL: File type '" + fileType + "' is not supported by the current media resource item.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (fileType != null) errorString += "\n--> File type of media: '" + fileType + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			if (hashValue != null) errorString += "\n--> Hash value of media: '" + hashValue + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return false;
		}

		/*
		 * Check media file on media pool
		 */

		// Initialize
		boolean mediaFileExists = false;

		try
		{
			while (true)
			{
				// Ask main server of media pool
				int mainServerNumber = this.poolMainServerNumber;

				if (mainServerNumber > 0)
				{
					ConnectionContainer connectionContainer = this.mediaPoolList.get(mainServerNumber);

					if (connectionContainer != null)
					{
						if (this.doMediaFileCheckOnMediaPool(context, connectionContainer, mediaResourceContainer, fileType, dataIdentifier, hashValue))
						{
							mediaFileExists = true;
							break;
						}
					}
				}

				// Ask secondary servers of media pool
				int numberOfserverInPool = this.mediaPoolList.size();

				if (numberOfserverInPool > 0)
				{
					for (ConnectionContainer connectionContainer : this.mediaPoolList.values())
					{
						if (connectionContainer.getNumber() == mainServerNumber) continue;

						if (this.doMediaFileCheckOnMediaPool(context, connectionContainer, mediaResourceContainer, fileType, dataIdentifier, hashValue))
						{
							mediaFileExists = true;
							break;
						}
					}
				}

				// End of processing
				break;
			}
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), null, e);
			mediaFileExists = false;
		}

		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> CHECK ON POOL: Result of checking media file on pool: '" + String.valueOf(mediaFileExists) + "'");

		/*
		 * Return
		 */
		return mediaFileExists;
	}

	/**
	 * Get information of a media file on a media server pool. Only the most
	 * recent media file is searched for on server, not any obsolete files.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param dataIdentifier
	 *            The identifier of the concrete media item to check.
	 * 
	 * @return Returns the client command <<TT>ClientCommandMediaFileInfo</TT>,
	 *         or <TT>null</TT> if an error occurred.
	 */
	protected ClientCommandMediaFileInfo poolInfoMediaFileOnPool(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		/*
		 * Check variables and conditions
		 */

		// Check if media pool is enabled
		if (!this.isEnableMediaPool()) { return null; }

		// Check media resource container
		if (mediaResourceContainer == null)
		{
			String errorString = "--> INFO ON POOL: Media resource container not set (NULL value).";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return null;
		}

		// Check data identifier
		if (dataIdentifier == null || dataIdentifier.length() == 0)
		{
			String errorString = "--> INFO ON POOL: Missing data identifier of media (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return null;
		}

		/*
		 * Get info of media file on media pool
		 */

		// Initialize
		boolean mediaFileExists = false;
		ClientCommandMediaFileInfo command = null;

		try
		{
			while (true)
			{
				// Ask main server of media pool
				int mainServerNumber = this.poolMainServerNumber;

				if (mainServerNumber > 0)
				{
					ConnectionContainer connectionContainer = this.mediaPoolList.get(mainServerNumber);

					if (connectionContainer != null)
					{
						command = this.doMediaFileInfoOnMediaPool(context, connectionContainer, mediaResourceContainer, dataIdentifier);

						if (command != null && command.isExisting())
						{
							mediaFileExists = true;
							break;
						}
					}
				}

				// Ask secondary servers of media pool
				int numberOfserverInPool = this.mediaPoolList.size();

				if (numberOfserverInPool > 0)
				{
					for (ConnectionContainer connectionContainer : this.mediaPoolList.values())
					{
						if (connectionContainer.getNumber() == mainServerNumber) continue;

						command = this.doMediaFileInfoOnMediaPool(context, connectionContainer, mediaResourceContainer, dataIdentifier);

						if (command != null && command.isExisting())
						{
							mediaFileExists = true;
							break;
						}
					}
				}

				// End of processing
				break;
			}
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), null, e);
			mediaFileExists = false;
		}

		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> INFO ON POOL: Result of getting media file information on pool: '" + String.valueOf(mediaFileExists) + "'");

		/*
		 * Return
		 */
		return command;
	}

	/**
	 * Read a media file from media pool
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param dataIdentifier
	 *            The identifier of the concrete media item to read.
	 * 
	 * @return Returns the file path of the pending media file the read content
	 *         is stored to, or <TT>null</TT> if an error occurred.
	 *         <p>
	 *         <TT>Please notice:</TT> It's your concern to delete this pending
	 *         file after processing it. Please be careful, otherwise a bunch of
	 *         trash files could be left over in the pending directory.
	 */
	protected String poolReadMediaFileOnPool(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		/*
		 * Check variables and conditions
		 */

		// Check if media pool is enabled
		if (!this.isEnableMediaPool()) { return null; }

		// Check media resource container
		if (mediaResourceContainer == null)
		{
			String errorString = "--> READ ON POOL: Media resource container not set (NULL value).";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return null;
		}

		// Check data identifier
		if (dataIdentifier == null || dataIdentifier.length() == 0)
		{
			String errorString = "--> READ ON POOL: Missing data identifier of media (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return null;
		}

		/*
		 * Read media file from media pool
		 */

		// Initialize
		boolean mediaFileReadSuccessfully = false;
		boolean lostMediaFile = false;
		String pendingFileName = null;

		try
		{
			while (true)
			{
				// Ask main server of media pool first
				int mainServerNumber = this.poolMainServerNumber;

				if (mainServerNumber > 0)
				{
					ConnectionContainer connectionContainer = this.mediaPoolList.get(mainServerNumber);

					if (connectionContainer != null)
					{
						pendingFileName = this.doMediaFileReadOnMediaPool(context, connectionContainer, mediaResourceContainer, dataIdentifier);

						if (pendingFileName != null)
						{
							mediaFileReadSuccessfully = true;
							break;
						}
					}
				}

				// Media file is missing on main server
				lostMediaFile = true;

				// Ask secondary servers of media pool
				int numberOfserverInPool = this.mediaPoolList.size();

				if (numberOfserverInPool > 0)
				{
					for (ConnectionContainer connectionContainer : this.mediaPoolList.values())
					{
						if (connectionContainer.getNumber() == mainServerNumber) continue;

						pendingFileName = this.doMediaFileReadOnMediaPool(context, connectionContainer, mediaResourceContainer, dataIdentifier);

						if (pendingFileName != null)
						{
							mediaFileReadSuccessfully = true;
							break;
						}

						// Media file is missing on secondary server
						lostMediaFile = true;
					}
				}

				// Add order to media synchronizing queue, if the media file was
				// not found on one of the servers of the media pool
				if (lostMediaFile == true)
				{
					this.addCommandToSynchronizingQueue(context, mediaResourceContainer.getRecourceIdentifier(), dataIdentifier);
				}

				// End of processing
				break;
			}
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnReadingFileFromServer"), null, e);
			mediaFileReadSuccessfully = false;
		}

		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> READ ON POOL: Result of reading media file on pool: '" + String.valueOf(mediaFileReadSuccessfully) + "'");

		/*
		 * Return
		 */
		if (mediaFileReadSuccessfully == true && pendingFileName != null)
		{
			return pendingFileName;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Upload a media file via a media pool.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param uploadFileNamePath
	 *            The full path of the file to be uploaded.
	 * 
	 * @param dataIdentifier
	 *            The identifier of the concrete media item to use for the
	 *            destination file.
	 * 
	 * @return Returns <TT>true</TT> if the media file exists, otherwise
	 *         <TT>false</TT>.
	 */
	protected boolean poolUploadMediaFileToPool(Context context, ResourceContainerMedia mediaResourceContainer, String uploadFileNamePath, String dataIdentifier)
	{
		/*
		 * Check variables and conditions
		 */

		// Check if media pool is enabled
		if (!this.isEnableMediaPool()) { return false; }

		// Check media resource container
		if (mediaResourceContainer == null)
		{
			String errorString = "--> UPLOAD ON POOL: Media resource container not set (NULL value).";
			if (uploadFileNamePath != null) errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return false;
		}

		// Check data identifier
		if (dataIdentifier == null || dataIdentifier.length() == 0)
		{
			String errorString = "--> UPLOAD ON POOL: Missing data identifier of media (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (uploadFileNamePath != null) errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return false;
		}

		// Check if file exists
		if (FileUtilFunctions.fileExists(uploadFileNamePath) == false)
		{
			String errorString = "--> UPLOAD ON POOL: File to be uploaded doesn't exist or is not accessable.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Checks if file type is set
		String fileType = FileUtilFunctions.fileGetFileTypePart(uploadFileNamePath);

		if (fileType == null || fileType.length() == 0)
		{
			String errorString = "--> UPLOAD ON POOL: Missing file type.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Checks if file type allowed for the given media resource item
		if (mediaResourceContainer.attributeIsFileTypeSupported(context, fileType) == false)
		{
			String errorString = "--> UPLOAD ON POOL: File type '" + fileType + "' is not supported by the current media resource item.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Checks if the maximum size of the media file is exceeded (related to
		// the media resource attribute)
		if (mediaResourceContainer.mediaFileIsMaximumFileSizeExceeded(context, uploadFileNamePath) == true)
		{
			String errorString = "--> UPLOAD ON POOL: Maximum allowed media file size exceeded.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of the file to be uploaded: '" + uploadFileNamePath + "'";

			Integer maximumFileSize = mediaResourceContainer.attributeGetMaximumMediaSize(context);
			if (maximumFileSize != null) errorString += "\n--> Maximum allowed file size of media resource item: '" + String.valueOf(maximumFileSize * 1024L) + "' Byte = '" + String.valueOf(maximumFileSize) + "' Kilobyte";

			Long currentFileSize = FileUtilFunctions.fileGetFileSize(uploadFileNamePath);
			if (currentFileSize != null) errorString += "\n--> File size of media file: '" + String.valueOf(currentFileSize) + "' Byte = '" + String.valueOf(currentFileSize / 1024) + "' Kilobyte";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "MaximumMediaSizeExceeded"), errorString, null);
			return false;
		}

		// Checks if the maximum size of the media file is exceeded (related to
		// the general configuration parameter)
		if (this.isMaximumFileSizeExceeded(context, uploadFileNamePath) == true)
		{
			String errorString = "--> UPLOAD ON POOL: Maximum allowed media file size exceeded.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of the file to be uploaded: '" + uploadFileNamePath + "'";

			Integer maximumFileSize = this.getMaximumMediaSize();
			if (maximumFileSize != null) errorString += "\n--> Maximum allowed file size set by the media configuration: '" + String.valueOf(maximumFileSize * 1024L) + "' Byte = '" + String.valueOf(maximumFileSize) + "' Kilobyte";

			Long currentFileSize = FileUtilFunctions.fileGetFileSize(uploadFileNamePath);
			if (currentFileSize != null) errorString += "\n--> File size of media file: '" + String.valueOf(currentFileSize) + "' Byte = '" + String.valueOf(currentFileSize / 1024) + "' Kilobyte";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "MaximumMediaSizeExceeded"), errorString, null);
			return false;
		}

		/*
		 * Add upload commands to the command queues
		 */

		// Process
		try
		{
			/*
			 * Add command to main command queue
			 */

			// Get file path of pending media file
			String pendingFilePath = FileLocationFunctions.compileFilePath(mediaResourceContainer.mediaFileGetPendingFilePath(context), mediaResourceContainer.mediaFileGetPendingFileName(context, fileType));

			// Copy media file to pending directory (with Retry, because
			// it can take a longer time to copy large files).
			int nuOfAttempts = FileUtilFunctions.fileCopyRetry(uploadFileNamePath, pendingFilePath);

			if (nuOfAttempts <= 0)
			{
				String errorString = "--> UPLOAD ON POOL: Error on coping media file to pending file.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Source file name: '" + uploadFileNamePath + "'";
				errorString += "\n--> Destination file name: '" + pendingFilePath + "'";
				errorString += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
				return false;
			}

			// Logging
			String logText = "\n--> UPLOAD ON POOL: Media file copied: '" + uploadFileNamePath + "' --> '" + pendingFilePath + "'";
			if (nuOfAttempts > 1) logText += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Add to main command pool
			this.addCommandToMainQueue(context, mediaResourceContainer.getRecourceIdentifier(), pendingFilePath, dataIdentifier);

			/*
			 * Add command to secondary command queue
			 */

			// Get file path of pending media file
			pendingFilePath = FileLocationFunctions.compileFilePath(mediaResourceContainer.mediaFileGetPendingFilePath(context), mediaResourceContainer.mediaFileGetPendingFileName(context, fileType));

			// Copy media file to pending directory (with Retry, because
			// it can take a longer time to copy large files).
			nuOfAttempts = FileUtilFunctions.fileCopyRetry(uploadFileNamePath, pendingFilePath);

			if (nuOfAttempts <= 0)
			{
				String errorString = "--> UPLOAD ON POOL: Error on coping media file to pending file.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Source file name: '" + uploadFileNamePath + "'";
				errorString += "\n--> Destination file name: '" + pendingFilePath + "'";
				errorString += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
				return false;
			}

			// Logging
			logText = "\n--> UPLOAD ON POOL: Media file copied: '" + uploadFileNamePath + "' --> '" + pendingFilePath + "'";
			if (nuOfAttempts > 1) logText += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Add to main command pool
			this.addCommandToSecondaryQueue(context, mediaResourceContainer.getRecourceIdentifier(), pendingFilePath, dataIdentifier);
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), null, e);
			return false;
		}

		/*
		 * Return
		 */
		return true;
	}

	/**
	 * Check if a media file already exists on a media server pool. Only the
	 * most recent media file is searched for on server, not any obsolete files.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param connectionContainer
	 *            Connection container that holds all information of the
	 *            connection to use.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param fileType
	 *            File type of the file to check.
	 * 
	 * @param hashValue
	 *            Hash value of the file to check.
	 * 
	 * @param dataIdentifier
	 *            The identifier of the concrete media item to check.
	 * 
	 * @return Returns <TT>true</TT> if the media file exists, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean doMediaFileCheckOnMediaPool(Context context, ConnectionContainer connectionContainer, ResourceContainerMedia mediaResourceContainer, String fileType, String dataIdentifier, String hashValue)
	{
		// Execute command
		return this.commandCheckOnServer(context, connectionContainer, mediaResourceContainer, fileType, dataIdentifier, hashValue);
	}

	/**
	 * Get information of a media file on a media server pool. Only the most
	 * recent media file is searched for on server, not any obsolete files.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param connectionContainer
	 *            Connection container that holds all information of the
	 *            connection to use.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * @param dataIdentifier
	 *            The identifier of the concrete media item to check.
	 * 
	 * @return Returns the client command <TT>ClientCommandMediaFileInfo</TT>,
	 *         or <TT>null</TT> if an error occurred.
	 */
	private ClientCommandMediaFileInfo doMediaFileInfoOnMediaPool(Context context, ConnectionContainer connectionContainer, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		// Execute command
		return this.commandInfoOnServer(context, connectionContainer, mediaResourceContainer, dataIdentifier);
	}

	/**
	 * Read a media file from media pool.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param connectionContainer
	 *            Connection container that holds all information of the
	 *            connection to use.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param dataIdentifier
	 *            The identifier of the concrete media item to check.
	 * 
	 * @return Returns the file path of the pending media file the read content
	 *         is stored to, or <TT>null</TT> if an error occurred.
	 *         <p>
	 *         <TT>Please notice:</TT> It's your concern to delete this pending
	 *         file after processing it. Please be careful, otherwise a bunch of
	 *         trash files could be left over in the pending directory.
	 */
	private String doMediaFileReadOnMediaPool(Context context, ConnectionContainer connectionContainer, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		// Execute command
		return this.commandReadOnServer(context, connectionContainer, mediaResourceContainer, dataIdentifier);
	}

	/**
	 * Upload a media file from the media server MAIN command queue to a server
	 * of the media pool.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param mediaResourceIdentifier
	 *            The media resource identifier of the media.
	 * 
	 * @param uploadPendingFileNamePath
	 *            The full path of the file to be uploaded.
	 * 
	 * @param dataIdentifier
	 *            The identifier of the concrete media item to use for the
	 *            destination file.
	 * 
	 * @return Returns <TT>true</TT> if the file could be uploaded on server,
	 *         otherwise <TT>false</TT>.
	 */
	boolean poolExecuteQueueOrder(Context context, ServerMediaPoolCommand serverMediaPoolCommand, boolean processMainServerOnly)
	{
		/*
		 * Check variables and conditions
		 */

		// Check command
		if (serverMediaPoolCommand == null)
		{
			String errorString = "--> EXECUTE ON MEDIA POOL: Missing command object (NULL value or EMPTY).";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Get all relevant values
		String mediaResourceIdentifier = serverMediaPoolCommand.getMediaResourceIdentifier();
		String uploadPendingFileNamePath = serverMediaPoolCommand.getMediaFilePendingName();
		String dataIdentifier = serverMediaPoolCommand.getDataIdentifier();

		// Check media resource container
		if (mediaResourceIdentifier == null || mediaResourceIdentifier.length() == 0)
		{
			String errorString = "--> EXECUTE ON MEDIA POOL: Media resource identifier not set.";
			if (uploadPendingFileNamePath != null) errorString += "\n--> File name of file to be uploaded: '" + uploadPendingFileNamePath + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Check media file path
		if (uploadPendingFileNamePath == null || uploadPendingFileNamePath.length() == 0)
		{
			String errorString = "--> EXECUTE ON MEDIA POOL: Missing file name of the file to be uploaded (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceIdentifier + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Check data identifier
		if (dataIdentifier == null || dataIdentifier.length() == 0)
		{
			String errorString = "--> EXECUTE ON MEDIA POOL: Missing data identifier of media (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceIdentifier + "'";
			if (uploadPendingFileNamePath != null) errorString += "\n--> File name of file to be uploaded: '" + uploadPendingFileNamePath + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Check if file exists
		if (FileUtilFunctions.fileExists(uploadPendingFileNamePath) == false)
		{
			String errorString = "--> EXECUTE ON MEDIA POOL: File to be uploaded doesn't exist or is not accessable.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceIdentifier + "'";
			errorString += "\n--> File name of file to be uploaded: '" + uploadPendingFileNamePath + "'";
			errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Checks if file type is set
		String fileType = FileUtilFunctions.fileGetFileTypePart(uploadPendingFileNamePath);

		if (fileType == null || fileType.length() == 0)
		{
			String errorString = "--> EXECUTE ON MEDIA POOL: No valid file type.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceIdentifier + "'";
			errorString += "\n--> File name of file to be uploaded: '" + uploadPendingFileNamePath + "'";
			errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Get hash value of the media file
		String hashValue = FileUtilFunctions.fileGetHashValue(uploadPendingFileNamePath);

		if (hashValue == null)
		{
			String errorString = "--> EXECUTE ON MEDIA POOL: Error on computing hash code of the media file.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceIdentifier + "'";
			errorString += "\n--> File to be hashed: '" + uploadPendingFileNamePath + "'";
			errorString += "\n--> Original file to be uploaded: '" + uploadPendingFileNamePath + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> EXECUTE ON MEDIA POOL: Media file hash value [" + hashValue + "] computed for: '" + uploadPendingFileNamePath + "'");

		/*
		 * Upload the media file to the media pool
		 */
		try
		{
			// Create media resource container
			ResourceContainer resourceContainer = new ResourceContainer(mediaResourceIdentifier);
			ResourceContainerMedia mediaResourceContainer = ResourceManager.media(context, resourceContainer.getGroup(), resourceContainer.getName());

			// Get number of main server in pool
			int mainServerNumber = this.poolMainServerNumber;

			if (mainServerNumber <= 0)
			{
				String errorString = "--> EXECUTE ON MEDIA POOL: No main server for media pool defined.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceIdentifier + "'";
				errorString += "\n--> File name of file to be uploaded: '" + uploadPendingFileNamePath + "'";
				errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
				return false;
			}

			// Go through the list of media server of the media pool
			int numberOfserverInPool = this.mediaPoolList.size();

			if (numberOfserverInPool > 0)
			{
				for (ConnectionContainer connectionContainer : this.mediaPoolList.values())
				{
					if (processMainServerOnly == true)
					{
						if (connectionContainer.getNumber() != mainServerNumber) continue;
					}
					else
					{
						if (connectionContainer.getNumber() == mainServerNumber) continue;
					}

					// Check if file already exists
					if (this.commandCheckOnServer(context, connectionContainer, mediaResourceContainer, fileType, dataIdentifier, hashValue) == true) return true;

					// Upload file to media pool
					if (this.commandUploadToServer(context, connectionContainer, mediaResourceContainer, uploadPendingFileNamePath, dataIdentifier) == false)
					{
						context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, connectionContainer.toString());

						String errorString = "--> EXECUTE ON MEDIA POOL: Error on uploading media file to media pool.";
						errorString += "\n--> Media resource identifier: '" + mediaResourceIdentifier + "'";
						errorString += "\n--> File name of file to be uploaded: '" + uploadPendingFileNamePath + "'";
						errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
						context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);

						return false;
					}
				}
			}

			// Delete pending file
			FileUtilFunctions.fileDelete(uploadPendingFileNamePath);
		}
		catch (Exception e)
		{
			String errorString = "--> EXECUTE ON MEDIA POOL: Error on executing command.";
			errorString += "\n--> File name of file to be uploaded: '" + uploadPendingFileNamePath + "'";
			errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, e);
			return false;
		}

		/*
		 * Return
		 */
		return true;
	}

	/**
	 * Getter
	 */
	boolean isEnableMediaPool()
	{
		return this.enableMediaPool;
	}

	/**
	 * Getter
	 */
	int getSecondsToWaitBetweenCommandProcessing()
	{
		return this.secondsToWaitBetweenCommandProcessing;
	}

}
