package fmagic.server.media;

import fmagic.basic.context.Context;
import fmagic.basic.media.MediaManager;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

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
	// Media UTIL instance
	private final ServerMediaPoolUtil mediaPoolUtil;

	// Configuration parameter
	private boolean enableLocalRepository = true;

	/**
	 * Constructor
	 */
	public ServerMediaManager()
	{
		super();
		
		this.mediaPoolUtil = new ServerMediaPoolUtil(this);
	}
	
	@Override
	public boolean readConfiguration(Context context)
	{
		boolean isError = super.readConfiguration(context);

		if (this.mediaPoolUtil.readConfiguration(context) == true) isError = true;

		if (readConfigurationEncodingKeyList(context) == true) isError = true;
		if (readConfigurationEncodingKeyNumber(context) == true) isError = true;
		if (readConfigurationEncodingEnabled(context) == true) isError = true;
		if (readConfigurationLocalMediaRepository(context) == true) isError = true;

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
	 * Getter
	 */
	public ServerMediaPoolUtil getMediaPoolUtil()
	{
		return mediaPoolUtil;
	}

	/**
	 * Getter
	 */
	public boolean isEnableLocalRepository()
	{
		return enableLocalRepository;
	}
	
}
