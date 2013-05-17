package fmagic.server.media;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import fmagic.basic.command.ConnectionContainer;
import fmagic.basic.command.ResponseContainer;
import fmagic.basic.context.Context;
import fmagic.basic.media.MediaManager;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.command.ClientCommandMediaFileCheck;

/**
 * This class implements UTIL functions for the media pool.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 17.05.2013 - Created
 * 
 */
public class ServerMediaPoolUtil
{
	// Media Manager object that holds the UTIL class
	private final MediaManager mediaManager;

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
	 * 
	 * @param mediaManager
	 *            The media manager object that holds the UTIL class.
	 */
	public ServerMediaPoolUtil(MediaManager mediaManager)
	{
		this.mediaManager = mediaManager;
	}

	/**
	 * Read configuration parameters and check them.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean readConfiguration(Context context)
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
	 * Wait for the end of processing of all commands of the media server, but
	 * after maximum of x seconds the method always returns.
	 * 
	 * @param maxTimeToWaitInSeconds
	 *            Maximum number of seconds to wait.
	 */
	public void waitForCompletingMediaServerCommandQueue(int maxTimeToWaitInSeconds)
	{
		int counter = maxTimeToWaitInSeconds * 10;

		while (counter-- > 0)
		{
			if (this.getNumberOfCommandsInMainQueue() <= 0 && this.getNumberOfCommandsInSecondaryQueue() <= 0) break;

			try
			{
				Thread.sleep(100);
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
	ServerMediaPoolCommand getNextCommandFromMainQueue()
	{
		ServerMediaPoolCommand command = null;

		synchronized (this.commandMainQueue)
		{
			command = this.commandMainQueue.poll();
		}

		return command;
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
	ServerMediaPoolCommand getNextCommandFromSecondaryQueue()
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
	ServerMediaPoolCommand getNextCommandFromSynchronizingQueue()
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
	public boolean poolCheckMediaFileOnPool(Context context, ResourceContainerMedia mediaResourceContainer, String fileType, String dataIdentifier, String hashValue)
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
				int mainServerNumber = this.getPoolMainServerNumber();

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
		// Prepare connection
		if (this.establishConnection(context, connectionContainer) == false)
		{
			String errorString = "--> CHECK ON POOL: Error on connecting to a media pool server";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (fileType != null) errorString += "\n--> File type of media: '" + fileType + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			if (hashValue != null) errorString += "\n--> Hash value of media: '" + hashValue + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return false;
		}
		
		// Execute command
		return this.mediaManager.commandCheckOnServer(context, connectionContainer, mediaResourceContainer, fileType, dataIdentifier, hashValue);
	}

	/**
	 * Set connection parameter for the socket connection to a server and tries
	 * to establish the connection.
	 * <p>
	 * If the connection already is established nothing is executed.
	 * 
	 * @param context
	 *            The application context to use.
	 * 
	 * @param connectionContainer
	 *            The connection container to use.
	 * 
	 * @return Returns <TT>true</TT> if the connection could be established resp. is already established,
	 *         otherwise <TT>false</TT>.
	 */
	private boolean establishConnection(Context context, ConnectionContainer connectionContainer)
	{
		// Set private key of server application
		if (connectionContainer.getServerPublicKey() == null || connectionContainer.getServerPublicKey().length() == 0) connectionContainer.setPrivateKey(context.getServerManager().getServerPrivateKey());

		// Create a new client session identifier if not available yet
		if (connectionContainer.getSessionIdentifier() == null || connectionContainer.getSessionIdentifier().length() == 0) connectionContainer.setSessionIdentifier(ConnectionContainer.createClientSessionIdentifier());

		// Try to establish the connection
		return connectionContainer.establishConnection(context);
	}

	/**
	 * Getter
	 */
	public HashMap<Integer, ConnectionContainer> getMediaPoolList()
	{
		return mediaPoolList;
	}

	/**
	 * Getter
	 */
	public int getPoolMainServerNumber()
	{
		return poolMainServerNumber;
	}

	/**
	 * Getter
	 */
	public boolean isEnableMediaPool()
	{
		return enableMediaPool;
	}

	/**
	 * Getter
	 */
	public int getMaximumNuOfItemsInCommandQueue()
	{
		return maximumNuOfItemsInCommandQueue;
	}

	/**
	 * Getter
	 */
	public int getSecondsToWaitBetweenCommandProcessing()
	{
		return secondsToWaitBetweenCommandProcessing;
	}

}
