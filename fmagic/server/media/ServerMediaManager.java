package fmagic.server.media;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import fmagic.basic.context.Context;
import fmagic.basic.media.MediaManager;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;
import fmagic.basic.watchdog.WatchdogCommand;

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
	// Settings for Media Server Pool
	private final HashMap<Integer, ServerMediaPoolHostContainer> mediaPoolList = new HashMap<Integer, ServerMediaPoolHostContainer>();
	private int poolMainServerNumber = 0;
	private boolean enableLocalRepository = true;
	private boolean enableMediaPool = false;
	private int maximumNuOfItemsInCommandQueue = 0;
	private int secondsToWaitBetweenCommandProcessing = 0;

	// List of media server commands (requests) to process
	private Queue<ServerMediaPoolCommand> commandQueue = new LinkedList<ServerMediaPoolCommand>();

	/**
	 * Constructor
	 */
	public ServerMediaManager()
	{
		super();
	}

	@Override
	protected boolean readConfigurationLocalMediaRepository(Context context)
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
			if (this.enableLocalRepository == true && (this.mediaRootFilePath == null || this.mediaRootFilePath.length() == 0))
			{
				String errorString = "--> Media configuration property 'ServerLocalMediaFilePathRoot' is not set.";
				errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
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

	@Override
	protected boolean readConfigurationEncodingKeyList(Context context)
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

	@Override
	protected boolean readConfigurationEncodingKeyNumber(Context context)
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

	@Override
	protected boolean readConfigurationEncodingEnabled(Context context)
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
	protected boolean readConfigurationSpecificParameter(Context context)
	{
		return false;
	}

	@Override
	protected boolean readConfigurationMediaPoolParameter(Context context)
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

			// Read configuration parameter: SecondsToWaitBetweenCommandProcessing
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
							ServerMediaPoolHostContainer container = new ServerMediaPoolHostContainer(number, host, port);
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
	 * Wait for the end of processing of all commands of the media server, but after
	 * maximum of x seconds the method always returns.
	 * 
	 * @param maxTimeToWaitInSeconds
	 *            Maximum number of seconds to wait.
	 */
	public void waitForCompletingMediaServerCommandQueue(int maxTimeToWaitInSeconds)
	{
		int counter = maxTimeToWaitInSeconds * 10;

		while (counter-- > 0)
		{
			if (this.getNumberOfMediaServerCommandElements() <= 0) break;

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
	 * Get number of elements in media server command queue.
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @return Returns the number of elements in media server command queue.
	 */
	int getNumberOfMediaServerCommandElements()
	{
		int size = 0;

		synchronized (this.commandQueue)
		{
			size = this.commandQueue.size();
		}

		return size;
	}

	/**
	 * Get the next element of media server command queue.
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @return Returns the next media server command to process or <TT>null</TT>.
	 */
	ServerMediaPoolCommand getMediaServerCommand()
	{
		ServerMediaPoolCommand command = null;

		synchronized (this.commandQueue)
		{
			command = this.commandQueue.poll();
		}

		return command;
	}

	/**
	 * Getter
	 */
	public HashMap<Integer, ServerMediaPoolHostContainer> getMediaPoolList()
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
	public boolean isEnableLocalRepository()
	{
		return enableLocalRepository;
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

	/**
	 * Getter
	 */
	public Queue<ServerMediaPoolCommand> getCommandQueue()
	{
		return commandQueue;
	}
}
