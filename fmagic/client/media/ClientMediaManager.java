package fmagic.client.media;

import fmagic.basic.context.Context;
import fmagic.basic.media.MediaManager;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * This class implements the management of media of client applications.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.04.2013 - Created
 * 
 */
public class ClientMediaManager extends MediaManager
{
	/**
	 * Constructor
	 */
	public ClientMediaManager()
	{
		super();
	}

	@Override
	protected boolean readConfigurationLocalMediaFilePathRoot(Context context)
	{
		ResourceContainer resourceContainer = ResourceManager.configuration(context, "Media", "ClientLocalMediaFilePathRoot");
		this.mediaRootFilePath = context.getConfigurationManager().getProperty(context, resourceContainer, null, true);

		if (this.mediaRootFilePath == null || this.mediaRootFilePath.length() == 0)
		{
			String errorString = "--> Media configuration property 'ClientLocalMediaFilePathRoot' is not set.";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Please set the media root file path explicitly.";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
			return true;
		}

		return false;
	}

	@Override
	protected boolean readConfigurationEncodingKeyList(Context context)
	{
		// Initialize variables
		boolean isError = false;

		// Read configuration item
		ResourceContainer resourceContainer = ResourceManager.localdata(context, "Media", "ClientEncodingKeyList");
		String encodingKeyList = context.getLocaldataManager().readProperty(context, resourceContainer, null);

		// Get key list
		if (encodingKeyList != null && encodingKeyList.length() > 0)
		{
			try
			{
				String keyListParts[] = encodingKeyList.split(",");

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
			String errorString = "--> Error on parsing client encoding key list.";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Value parsed: '" + encodingKeyList + "'";
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
					String errorString = "--> Error on client encoding key list, on key number '" + String.valueOf(keyNumber) + "'.";
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
		ResourceContainer resourceContainer = ResourceManager.localdata(context, "Media", "ClientEncodingKeyNumber");
		String encodingKeyNumber = context.getLocaldataManager().readProperty(context, resourceContainer, null);

		// Key number is not set
		if (encodingKeyNumber == null) return false;

		// Convert key number to integer
		int encodingKeyNumberInteger = 0;

		try
		{
			encodingKeyNumberInteger = Integer.parseInt(encodingKeyNumber);
		}
		catch (Exception e)
		{
			String errorString = "--> Client encoding key number is not an integer value.";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Client encoding key number: '" + String.valueOf(encodingKeyNumber) + "'";
			errorString += "\n--> Available key numbers of client encoding keys: '" + this.encodingKeyList.keySet().toString() + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
			return true;
		}

		// Check parameter value
		if (this.encodingKeyList.get(encodingKeyNumberInteger) == null)
		{
			String errorString = "--> Client encoding key number is not part of the key list.";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Client encoding key number: '" + String.valueOf(encodingKeyNumber) + "'";
			errorString += "\n--> Available key numbers of client encoding keys: '" + this.encodingKeyList.keySet().toString() + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
			return true;
		}

		this.encodingKeyNumber = encodingKeyNumberInteger;

		return false;
	}

	@Override
	protected boolean readConfigurationEncodingEnabled(Context context)
	{
		ResourceContainer resourceContainer = ResourceManager.configuration(context, "Media", "ClientEncodingEnabled");
		this.encodingEnabled = context.getConfigurationManager().getPropertyAsBooleanValue(context, resourceContainer, false, false);

		// Check parameter value
		if (this.encodingEnabled == true && this.encodingKeyNumber == 0)
		{
			String errorString = "--> Client encoding is enabled but no encoding key is set.";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Please set encoding key, or disable client encoding.";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
			return true;
		}

		return false;
	}

	@Override
	public boolean isEncodingEnabled(Context context, ResourceContainerMedia mediaResourceContainer)
	{
		boolean encodingEnabled = true;

		if (mediaResourceContainer.isClientEncoding(context) == false) encodingEnabled = false;
		if (this.encodingEnabled == false) encodingEnabled = false;
		if (this.getEncodingValue(context) == null) encodingEnabled = false;

		return encodingEnabled;
	}
}
