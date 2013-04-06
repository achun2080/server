package fmagic.basic;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * This class implements the management of media used by servers and clients.
 * <p>
 * Media data are all kinds of media types, that are usually provided as files,
 * like images, audio media, videos, documents and others. Those files have to
 * be stored on server or client, and they need to be transferred form server to
 * client and contrariwise. Last not least they are to be shown on screen.
 * <p>
 * Each logical media data is defined as a resource item and is given some
 * parameters to describe its main behavior. For example, if you want to use
 * images for user accounts, say to show the user portrait on screen, you are
 * requested to define a media resource item first. There are some parameters to
 * consider, like the internal name of the media, the media type (image, video,
 * audio, document, or others), the allowed file types (jpg, png, mkv, flv, or
 * others), the origin of the media (client or server), the logical path to
 * store media files, the storage location, or security settings.
 * <p>
 * Once you have determined the media resource item and the media parameters,
 * you can handle the media file itself. You may download it from the server to
 * the client, or upload it from client to server, or store it internally in the
 * resp. path structure of client or server, or show it on screen.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 30.03.2013 - Created
 * 
 */
public class MediaManager implements ManagerInterface
{
	// Settings for Server Encoding
	private final HashMap<Integer, String> serverMediaKeyList = new HashMap<Integer, String>();
	private int serverMediaKeyNumber = 0;
	private boolean serverEncodingEnabled = false;

	// Media root file path
	String mediaRootFilePath = null;

	/**
	 * Constructor
	 */
	public MediaManager()
	{
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = { "Media" };
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

		String typeCriteria[] = { "Media" };
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

		String typeCriteria[] = { "Media" };
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
		// Variables
		boolean isIntegrityError = false;
		ResourceManager resourceManager = context.getResourceManager();

		// Check for integrity errors (Named resource identifiers)
		try
		{
			// Go through all resource items
			for (ResourceContainer resourceContainer : resourceManager.getResources().values())
			{
				// Check if resource item is typed as "Media"
				if (!resourceContainer.getType().equalsIgnoreCase("media")) continue;

				// Check if name is set
				String name = resourceContainer.getName();
				if (name == null || name.length() == 0) continue;

				// Check: Alias name must be set
				if (ckeckOnAliasName(context, resourceContainer) == true) isIntegrityError = true;

				// The attribute 'MediaType' must be set
				if (ckeckOnAttributeMediaType(context, resourceContainer) == true) isIntegrityError = true;

				// The attribute 'FileTypes' must be set
				if (ckeckOnAttributeFileTypes(context, resourceContainer) == true) isIntegrityError = true;

				// The attribute 'StorageLocation' must be set
				if (ckeckOnAttributeStorageLocation(context, resourceContainer) == true) isIntegrityError = true;

				// The attribute 'LogicalPath' must be set
				if (ckeckOnAttributeLogicalPath(context, resourceContainer) == true) isIntegrityError = true;

				// The attribute 'ServerEncoding' is optional
				if (ckeckOnAttributeServerEncoding(context, resourceContainer) == true) isIntegrityError = true;

				// The attribute 'ClientEncoding' is optional
				if (ckeckOnAttributeClientEncoding(context, resourceContainer) == true) isIntegrityError = true;
			}

		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isIntegrityError;
	}

	@Override
	public boolean readConfiguration(Context context)
	{
		boolean isError = false;

		if (readLocalMediaFilePathRoot(context) == true) isError = true;
		if (readServerEncodingKeyList(context) == true) isError = true;
		if (readServerEncodingKeyNumber(context) == true) isError = true;
		if (readServerEncodingEnabled(context) == true) isError = true;

		/*
		 * Return
		 */
		return isError;
	}

	/**
	 * Read configuration parameter 'LocalMediaFilePathRoot'.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean readLocalMediaFilePathRoot(Context context)
	{
		this.mediaRootFilePath = FileLocationManager.getRootPath() + FileLocationManager.getPathElementDelimiterString() + FileLocationManager.getMediaSubPath();

		String configurationMediaRootFilePath = context.getConfigurationManager().getProperty(context, ResourceManager.configuration(context, "Media", "LocalMediaFilePathRoot"), null, false);

		if (configurationMediaRootFilePath != null && configurationMediaRootFilePath.length() > 0)
		{
			this.mediaRootFilePath = configurationMediaRootFilePath;
		}

		return false;
	}

	/**
	 * Read configuration parameter 'ServerEncodingKeyList'.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean readServerEncodingKeyList(Context context)
	{
		// Initialize variables
		boolean isError = false;

		// Read configuration item
		ResourceContainer resourceContainer = ResourceManager.configuration(context, "Media", "ServerEncodingKeyList");
		String serverMediaKeyListString = context.getConfigurationManager().getProperty(context, resourceContainer, null, false);

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

							if (isError == false) this.serverMediaKeyList.put(number, key.trim());
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
			for (int keyNumber : this.serverMediaKeyList.keySet())
			{
				String keyValue = this.serverMediaKeyList.get(keyNumber);

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
	 * Read configuration parameter 'ServerEncodingKeyNumber'.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean readServerEncodingKeyNumber(Context context)
	{
		// Read parameter value
		ResourceContainer resourceContainer = ResourceManager.configuration(context, "Media", "ServerEncodingKeyNumber");
		String configurationServerEncodingKeyNumberString = context.getConfigurationManager().getProperty(context, resourceContainer, null, false);

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
			errorString += "\n--> Available key numbers of server encoding keys: '" + this.serverMediaKeyList.keySet().toString() + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
			return true;
		}

		// Check parameter value
		if (this.serverMediaKeyList.get(configurationServerEncodingKeyNumberInteger) == null)
		{
			String errorString = "--> Server encoding key number is not part of the key list.";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Server encoding key number: '" + String.valueOf(configurationServerEncodingKeyNumberString) + "'";
			errorString += "\n--> Available key numbers of server encoding keys: '" + this.serverMediaKeyList.keySet().toString() + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
			return true;
		}

		this.serverMediaKeyNumber = configurationServerEncodingKeyNumberInteger;

		return false;
	}

	/**
	 * Read configuration parameter 'ServerEncodingEnabled'.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean readServerEncodingEnabled(Context context)
	{
		ResourceContainer resourceContainer = ResourceManager.configuration(context, "Media", "ServerEncodingEnabled");
		this.serverEncodingEnabled = context.getConfigurationManager().getPropertyAsBooleanValue(context, resourceContainer, false, false);

		// Check parameter value
		if (this.serverEncodingEnabled == true && this.serverMediaKeyNumber == 0)
		{
			String errorString = "--> Server encoding is enabled but no encoding key is set.";
			errorString += "\n--> Configuration property: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Please set encoding key, or disable server encoding.";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), errorString, null);
			return true;
		}

		return false;
	}

	/**
	 * Check if an Alias name is set to a resource item.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param resourceContainer
	 *            The resource container to be checked.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean ckeckOnAliasName(Context context, ResourceContainer resourceContainer)
	{
		// Validate variables
		if (resourceContainer == null) return true;

		// Check on error
		if (resourceContainer.checkAliasName() == false)
		{
			String errorString = "--> Alias name for media item is not set.";
			String fileName = context.getResourceManager().getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
			if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
			errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> The Alias name is used as a part of the file name of media files.";
			errorString += "\n--> Please set an Alias that is unique to all media of an application.";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

			return true;
		}

		// Return
		return false;
	}

	/**
	 * Check if the attribute "MediaType" is set.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param resourceContainer
	 *            The resource container to be checked.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean ckeckOnAttributeMediaType(Context context, ResourceContainer resourceContainer)
	{
		// Validate variables
		if (resourceContainer == null) return true;

		// Variables
		boolean isIntegrityError = false;

		// Check on error
		ResourceContainer mediaTypeAttributeResourceContainer = ResourceManager.attribute(context, "Media", "MediaType");
		List<String> mediaTypeList = mediaTypeAttributeResourceContainer.getValueList(context, null);
		String mediaTypeValue = resourceContainer.getAttribute(mediaTypeAttributeResourceContainer.getAliasName());

		if (mediaTypeValue == null)
		{
			String errorString = "--> Media Type for media item is not set.";
			String fileName = context.getResourceManager().getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
			if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
			errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> The attribute Media Type is used to differentiate the types 'Image', 'Video', 'Audio' and 'Document'.";
			errorString += "\n--> Please set a Media Type corresponding to the allowed types.";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

			isIntegrityError = true;
		}
		/*
		 * Check if the Media Type is one of the allowed media types
		 */
		else
		{
			String flatListOfAllowedMediaTypes = "";
			boolean isMediaTypeAllowed = false;

			for (String allowedMediaType : mediaTypeList)
			{
				flatListOfAllowedMediaTypes += "[" + allowedMediaType + "]";
				if (allowedMediaType.equals(mediaTypeValue)) isMediaTypeAllowed = true;
			}

			if (isMediaTypeAllowed == false)
			{
				String errorString = "--> Media Type '" + mediaTypeValue + "' is not allowed.";
				errorString += "\n--> List of allowed Media Types: " + flatListOfAllowedMediaTypes;
				errorString += "\n--> Please pay attention to lower and upper cases.";
				String fileName = context.getResourceManager().getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
				if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
				errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> The attribute Media Type is used to differentiate the types 'Image', 'Video', 'Audio' and 'Document'.";
				errorString += "\n--> Please set a Media Type corresponding to the allowed types.";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

				isIntegrityError = true;
			}
		}

		// Return
		return isIntegrityError;
	}

	/**
	 * Check if the attribute "FileTypes" is set.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param resourceContainer
	 *            The resource container to be checked.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean ckeckOnAttributeFileTypes(Context context, ResourceContainer resourceContainer)
	{
		// Validate variables
		if (resourceContainer == null) return true;

		// Variables
		boolean isIntegrityError = false;

		// Check on error
		ResourceContainer fileTypesAttributeResourceContainer = ResourceManager.attribute(context, "Media", "FileTypes");
		List<String> mediaFileTypesList = fileTypesAttributeResourceContainer.getValueList(context, null);
		String fileTypesValue = resourceContainer.getAttribute(fileTypesAttributeResourceContainer.getAliasName());

		if (fileTypesValue == null || fileTypesValue.length() == 0)
		{
			String errorString = "--> File types for media item are not set.";
			String fileName = context.getResourceManager().getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
			if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
			errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> The attribute 'FileTypes' is used to allow upload and usage of specific media files.";
			errorString += "\n--> Please set the file types corresponding to the allowed types.";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

			isIntegrityError = true;
		}

		/*
		 * Check if the File Types are allowed
		 */
		else
		{
			boolean isFileTypesAllowed = true;
			String flatListOfAllowedFileTypes = "";
			String flatListOfNotAllowedFileTypes = "";

			for (String allowedFileType : mediaFileTypesList)
			{
				flatListOfAllowedFileTypes += "[" + allowedFileType + "]";
			}

			String fileTypesListOfMediaResourceItem[] = fileTypesValue.split(",");

			if (fileTypesListOfMediaResourceItem != null)
			{
				for (int i = 0; i < fileTypesListOfMediaResourceItem.length; i++)
				{
					if (!mediaFileTypesList.contains(fileTypesListOfMediaResourceItem[i].trim()))
					{
						isFileTypesAllowed = false;
						flatListOfNotAllowedFileTypes += "[" + fileTypesListOfMediaResourceItem[i].trim() + "]";
					}
				}
			}

			if (isFileTypesAllowed == false)
			{
				String errorString = "--> File Type " + flatListOfNotAllowedFileTypes + " is not allowed.";
				errorString += "\n--> List of allowed File Types: " + flatListOfAllowedFileTypes;
				errorString += "\n--> Please pay attention to lower and upper cases.";
				String fileName = context.getResourceManager().getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
				if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
				errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> The atttribute 'FileTypes' is used to determine which media files can be uploaded for the given media item.";
				errorString += "\n--> Please set file types corresponding to the allowed types.";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

				isIntegrityError = true;
			}
		}

		// Return
		return isIntegrityError;
	}

	/**
	 * Check if the attribute "StorageLocation" is set.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param resourceContainer
	 *            The resource container to be checked.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean ckeckOnAttributeStorageLocation(Context context, ResourceContainer resourceContainer)
	{
		// Validate variables
		if (resourceContainer == null) return true;

		// Variables
		boolean isIntegrityError = false;

		// Check on error
		ResourceContainer mediaStorageLocationAttributeResourceContainer = ResourceManager.attribute(context, "Media", "StorageLocation");
		List<String> mediaStorageLocationList = mediaStorageLocationAttributeResourceContainer.getValueList(context, null);
		String mediaStorageLocationValue = resourceContainer.getAttribute(mediaStorageLocationAttributeResourceContainer.getAliasName());

		if (mediaStorageLocationValue == null)
		{
			String errorString = "--> Storage location for media item is not set.";
			String fileName = context.getResourceManager().getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
			if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
			errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> The attribute 'StorageLocation' determines where the media files are to be stored: 'Server', 'Client' or 'Synchronize'.";
			errorString += "\n--> Please set a storage location corresponding to the allowed types.";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

			isIntegrityError = true;
		}
		/*
		 * Check if the Origin is one of the allowed values
		 */
		else
		{
			String flatListOfAllowedStorageLocations = "";
			boolean isStorageLocationAllowed = false;

			for (String allowedStorageLocation : mediaStorageLocationList)
			{
				flatListOfAllowedStorageLocations += "[" + allowedStorageLocation + "]";
				if (allowedStorageLocation.equals(mediaStorageLocationValue)) isStorageLocationAllowed = true;
			}

			if (isStorageLocationAllowed == false)
			{
				String errorString = "--> Storage location '" + mediaStorageLocationValue + "' is not allowed.";
				errorString += "\n--> List of allowed storage location values: " + flatListOfAllowedStorageLocations;
				errorString += "\n--> Please pay attention to lower and upper cases.";
				String fileName = context.getResourceManager().getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
				if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
				errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> The attribute 'StorageLocation' determines where the media files are to be stored: 'Server', 'Client' or 'Synchronize'.";
				errorString += "\n--> Please set a storage location corresponding to the allowed types.";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

				isIntegrityError = true;
			}
		}

		// Return
		return isIntegrityError;
	}

	/**
	 * Check if the attribute "LogicalPath" is set.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param resourceContainer
	 *            The resource container to be checked.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean ckeckOnAttributeLogicalPath(Context context, ResourceContainer resourceContainer)
	{
		// Validate variables
		if (resourceContainer == null) return true;

		// Variables
		boolean isIntegrityError = false;

		// Check on error
		ResourceContainer mediaLogicalPathAttributeResourceContainer = ResourceManager.attribute(context, "Media", "LogicalPath");
		String mediaLogicalPathValue = resourceContainer.getAttribute(mediaLogicalPathAttributeResourceContainer.getAliasName());

		if (mediaLogicalPathValue == null || mediaLogicalPathValue.length() == 0)
		{
			String errorString = "--> Logical path for media item is not set.";
			String fileName = context.getResourceManager().getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
			if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
			errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> The attribute 'LogicalPath' determines in which sub path media files are to be stored.";
			errorString += "\n--> Please set a logical path.";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

			isIntegrityError = true;
		}
		/*
		 * Check if the logical path is set right
		 */
		else
		{
			// Check if path contains back slash characters
			if (mediaLogicalPathValue.contains("\\"))
			{
				String errorString = "--> Logical path '" + mediaLogicalPathValue + "' contains backslash '\\' characters.";
				errorString += "\n--> Please use slash '/' characters only to separate the path elements.";
				String fileName = context.getResourceManager().getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
				if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
				errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

				isIntegrityError = true;
			}

			// Check if path starts or ends with a slash character
			if (isIntegrityError == false)
			{
				if (mediaLogicalPathValue.trim().startsWith("/") || mediaLogicalPathValue.trim().endsWith("/"))
				{
					String errorString = "--> Logical path '" + mediaLogicalPathValue + "' starts or ends with a slash.";
					errorString += "\n--> Please use slash '/' characters only to separate the path elements.";
					String fileName = context.getResourceManager().getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
					if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
					errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";

					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

					isIntegrityError = true;
				}
			}

			// Check compatibility to file name settings
			if (isIntegrityError == false)
			{
				boolean isCompatibleToFileName = true;
				String flatListOfIncompatibleParts = "";
				String partsOfMediaLogicalPathValue[] = mediaLogicalPathValue.split("/");

				if (partsOfMediaLogicalPathValue != null)
				{
					for (int i = 0; i < partsOfMediaLogicalPathValue.length; i++)
					{
						String normalizedMediaLogicalPathValue = Util.fitToFileNameCompatibility(partsOfMediaLogicalPathValue[i]);

						if (!normalizedMediaLogicalPathValue.equals(partsOfMediaLogicalPathValue[i]))
						{
							isCompatibleToFileName = false;
							flatListOfIncompatibleParts += "\n-->    [" + partsOfMediaLogicalPathValue[i] + "] --> [" + normalizedMediaLogicalPathValue + "]";
						}
					}
				}

				if (isCompatibleToFileName == false)
				{
					String errorString = "--> Logical path '" + mediaLogicalPathValue + "' is not compatible to file name conventions.";
					errorString += "\n--> List of problematical parts and there transformation to make them compatible: " + flatListOfIncompatibleParts;
					String fileName = context.getResourceManager().getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
					if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
					errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";

					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

					isIntegrityError = true;
				}
			}
		}

		// Return
		return isIntegrityError;
	}

	/**
	 * Check if the attribute "ServerEncoding" is set.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param resourceContainer
	 *            The resource container to be checked.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean ckeckOnAttributeServerEncoding(Context context, ResourceContainer resourceContainer)
	{
		// Validate variables
		if (resourceContainer == null) return true;

		// Variables
		boolean isIntegrityError = false;

		// Check on error
		ResourceContainer mediaServerEncodingAttributeResourceContainer = ResourceManager.attribute(context, "Media", "ServerEncoding");
		List<String> mediaServerEncodingList = mediaServerEncodingAttributeResourceContainer.getValueList(context, null);
		String mediaServerEncodingValue = resourceContainer.getAttribute(mediaServerEncodingAttributeResourceContainer.getAliasName());

		if (mediaServerEncodingValue != null && mediaServerEncodingValue.length() > 0)
		{
			String flatListOfAllowedServerEncodings = "";
			boolean isServerEncodingAllowed = false;

			for (String allowedStorageLocation : mediaServerEncodingList)
			{
				flatListOfAllowedServerEncodings += "[" + allowedStorageLocation + "]";
				if (allowedStorageLocation.equals(mediaServerEncodingValue)) isServerEncodingAllowed = true;
			}

			if (isServerEncodingAllowed == false)
			{
				String errorString = "--> Server encoding '" + mediaServerEncodingValue + "' is not allowed.";
				errorString += "\n--> List of allowed server encoding values: " + flatListOfAllowedServerEncodings;
				errorString += "\n--> Please pay attention to lower and upper cases.";
				String fileName = context.getResourceManager().getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
				if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
				errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> The attribute 'ServerEncoding' determines if the media are to be encoded on server site.";
				errorString += "\n--> This attribute is optional. The default value is 'false'.";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

				isIntegrityError = true;
			}
		}

		// Return
		return isIntegrityError;
	}

	/**
	 * Check if the attribute "ClientEncoding" is set.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param resourceContainer
	 *            The resource container to be checked.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean ckeckOnAttributeClientEncoding(Context context, ResourceContainer resourceContainer)
	{
		// Validate variables
		if (resourceContainer == null) return true;

		// Variables
		boolean isIntegrityError = false;

		// Check on error
		ResourceContainer mediaClientEncodingAttributeResourceContainer = ResourceManager.attribute(context, "Media", "ClientEncoding");
		List<String> mediaClientEncodingList = mediaClientEncodingAttributeResourceContainer.getValueList(context, null);
		String mediaClientEncodingValue = resourceContainer.getAttribute(mediaClientEncodingAttributeResourceContainer.getAliasName());

		if (mediaClientEncodingValue != null && mediaClientEncodingValue.length() > 0)
		{
			String flatListOfAllowedClientEncodings = "";
			boolean isClientEncodingAllowed = false;

			for (String allowedStorageLocation : mediaClientEncodingList)
			{
				flatListOfAllowedClientEncodings += "[" + allowedStorageLocation + "]";
				if (allowedStorageLocation.equals(mediaClientEncodingValue)) isClientEncodingAllowed = true;
			}

			if (isClientEncodingAllowed == false)
			{
				String errorString = "--> Client encoding '" + mediaClientEncodingValue + "' is not allowed.";
				errorString += "\n--> List of allowed client encoding values: " + flatListOfAllowedClientEncodings;
				errorString += "\n--> Please pay attention to lower and upper cases.";
				String fileName = context.getResourceManager().getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
				if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
				errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> The attribute 'ClientEncoding' determines if the media are to be encoded on client site.";
				errorString += "\n--> This attribute is optional. The default value is 'false'.";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

				isIntegrityError = true;
			}
		}

		// Return
		return isIntegrityError;
	}

	/**
	 * Notify the WATCHDOG about media item access.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param resourceIdentifier
	 *            The resource identifier of the media item.
	 * 
	 * @param additionalTextParameter
	 *            Additional text to notify, or <TT>null</TT>.
	 */
	private void notifyWatchdog(Context context, String identifier, String additionalTextParameter)
	{
		try
		{
			// Set message Text
			String messageText = "Access to Media Item";

			// Set additional text
			String additionalText = "--> Access to Media Item";
			if (additionalTextParameter != null) additionalText += "\n" + additionalTextParameter;
			additionalText += "\n--> Identifier: '" + identifier + "'";

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
	 * Get the root file path of local media files.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the root file path of media stored locally.
	 * 
	 */
	public String getMediaRootFilePath(Context context)
	{
		return this.mediaRootFilePath;
	}

	/**
	 * Check if server encoding is enabled and if all configuration parameter
	 * are available.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @return Returns <TT>true</TT> if server encoding is set.
	 * 
	 */
	public boolean checkServerEncoding(Context context, ResourceContainerMedia mediaResourceContainer)
	{
		boolean serverEncodingAvailable = true;

		if (mediaResourceContainer.isServerEncoding(context) == false) serverEncodingAvailable = false;
		if (context.getMediaManager().isServerEncodingEnabled() == false) serverEncodingAvailable = false;
		if (context.getMediaManager().getServerEncodingValue(context) == null) serverEncodingAvailable = false;

		return serverEncodingAvailable;
	}

	/**
	 * Encrypt a media file on server side.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param sourceFilePath
	 *            The full path of the file to be encrypted.
	 * 
	 * @param destinationfilePath
	 *            The full path of the file the encrypted code is to store.
	 * 
	 * @return Returns the full path of the encoded pending file that was
	 *         created, or <TT>null</TT> if an error occurred.
	 */
	public String encryptFile(Context context, ResourceContainerMedia mediaResourceContainer, String sourceFilePath, String destinationfilePath)
	{
		// Check if server encoding is enabled
		if (this.checkServerEncoding(context, mediaResourceContainer) == false) return sourceFilePath;

		// Get key value (password)
		String keyValue = this.getServerEncodingValue(context);
		if (keyValue == null) return sourceFilePath;

		// Encode media file
		CipherHandler cipherHandler = new CipherHandler(context);
		if (cipherHandler.encrypt(keyValue, sourceFilePath, destinationfilePath) == false) return null;

		// Return
		return destinationfilePath;
	}

	/**
	 * Decrypt a media file on server side.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param sourceFilePath
	 *            The full path of the file to be decrypted.
	 * 
	 * @return Returns the full path of the decoded pending file that was
	 *         created, or <TT>null</TT> if an error occurred.
	 */
	public String decryptFile(Context context, ResourceContainerMedia mediaResourceContainer, String sourceFilePath)
	{
		// Validate parameter
		if (mediaResourceContainer == null) return null;
		if (sourceFilePath == null || sourceFilePath.length() == 0) return null;

		// Get server encoding key number of the real file
		int keyNumber = mediaResourceContainer.getEncodingKeyOfRealFileName(context, sourceFilePath);

		// Get file type of media file
		String fileType = Util.fileGetFileTypePart(sourceFilePath);

		if (fileType == null || fileType.length() == 0)
		{
			String errorString = "--> Missing file type of media file.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of media file: '" + sourceFilePath + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
			return null;
		}

		// Get file path of working media file
		String destinationfilePath = mediaResourceContainer.getMediaPendingFilePath(context) + FileLocationManager.getPathElementDelimiterString() + mediaResourceContainer.getMediaPendingFileName(context, fileType);

		// Copy media file only
		if (keyNumber == 0)
		{
			if (Util.fileCopy(sourceFilePath, destinationfilePath) == false)
			{
				String errorString = "--> Error on coping media file.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Source file name: '" + sourceFilePath + "'";
				errorString += "\n--> Destination file name: '" + destinationfilePath + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
				return null;
			}
		}
		// Decrypt media file
		else
		{
			// Get key value (password)
			String keyValue = this.serverMediaKeyList.get(keyNumber);

			if (keyValue == null || keyValue.length() == 0)
			{
				String errorString = "--> Missing key value (password) for server encoding key '" + String.valueOf(keyNumber) + "'.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> File name of media file: '" + sourceFilePath + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
				return null;
			}

			// Decode media file
			CipherHandler cipherHandler = new CipherHandler(context);

			if (cipherHandler.decrypt(keyValue, sourceFilePath, destinationfilePath) == false)
			{
				String errorString = "--> Error on decrypting media file.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> File name of media file: '" + sourceFilePath + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
				return null;
			}

			// Compare hash value to ensure that decrypting worked well
			String hashValueSourceFile = mediaResourceContainer.getHashValueOfRealFileName(context, sourceFilePath);

			if (hashValueSourceFile == null)
			{
				String errorString = "--> Error on analyzing hash value in media file name.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> File name analyzed: '" + sourceFilePath + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);

				Util.fileDelete(destinationfilePath);

				return null;
			}

			String hashValueDestinationFile = Util.fileGetHashValue(destinationfilePath);

			if (hashValueDestinationFile == null)
			{
				String errorString = "--> Error on computing hash value of a media file.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> File to be hashed: '" + destinationfilePath + "'";
				errorString += "\n--> Please notice: The file was not deleted automatically from the 'pending' directory, in order to have a chance to analyze it later.";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
				return null;
			}

			if (!hashValueSourceFile.equals(hashValueDestinationFile))
			{
				String errorString = "--> Mismatch on hash value after decrypting media file.";
				errorString += "\n--> The key value (password) of the encoding key: '" + String.valueOf(keyNumber) + "' might be changed.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Encrypted file [1] before decrypting: '" + sourceFilePath + "'";
				errorString += "\n--> Hash value of file [1]: '" + hashValueSourceFile + "'";
				errorString += "\n--> Decrypted file [2] after decrypting: '" + destinationfilePath + "'";
				errorString += "\n--> Hash value of original file to be copied before: '" + hashValueDestinationFile + "'";
				errorString += "\n--> Please notice: The file [2] was not deleted automatically from the 'pending' directory, in order to have a chance to analyze it later.";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
				return null;
			}
		}

		// Return
		return destinationfilePath;
	}

	/**
	 * Upload a media file to the system.
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
	 * @return Returns <TT>true</TT> if the file could be stored, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean uploadFile(Context context, ResourceContainerMedia mediaResourceContainer, String uploadFileNamePath, String dataIdentifier)
	{
		/*
		 * Check variables and conditions
		 */

		// Check media resource container
		if (mediaResourceContainer == null)
		{
			String errorString = "--> Media resource container not set (NULL value).";
			if (uploadFileNamePath != null) errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Check file path
		if (uploadFileNamePath == null || uploadFileNamePath.length() == 0)
		{
			String errorString = "--> Missing file name of the file to be uploaded (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Check data identifier
		if (dataIdentifier == null || dataIdentifier.length() == 0)
		{
			String errorString = "--> Missing data identifier of media (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (uploadFileNamePath != null) errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Check if file exists
		if (Util.fileExists(uploadFileNamePath) == false)
		{
			String errorString = "--> File to be uploaded doesn't exist or is not accessable.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Checks if file type is set
		String fileType = Util.fileGetFileTypePart(uploadFileNamePath);

		if (fileType == null || fileType.length() == 0)
		{
			String errorString = "--> Missing file type.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Checks if file type allowed for the given media resource item
		if (mediaResourceContainer.isFileTypeSupported(context, fileType) == false)
		{
			String errorString = "--> File type '" + fileType + "' is not supported by the current media resource item.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		/*
		 * Create file directory for pending files
		 */
		String pendingFilePathDirectory = mediaResourceContainer.getMediaPendingFilePath(context);

		try
		{
			File directory = new File(pendingFilePathDirectory);
			directory.mkdirs();

			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media file directory 'pending' created: '" + pendingFilePathDirectory + "'");
		}
		catch (Exception e)
		{
			String errorString = "--> Error on creating directory for pending media files.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Directory to be created: '" + pendingFilePathDirectory + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, e);
			return false;
		}

		/*
		 * Create file directory for deleted files
		 */
		String deletedFilePathDirectory = mediaResourceContainer.getMediaDeletedFilePath(context);

		try
		{
			File directory = new File(deletedFilePathDirectory);
			directory.mkdirs();

			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media file directory 'deleted' created: '" + deletedFilePathDirectory + "'");
		}
		catch (Exception e)
		{
			String errorString = "--> Error on creating directory for deleted media files.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Directory to be created: '" + deletedFilePathDirectory + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, e);
			return false;
		}

		/*
		 * Copy file as pending file
		 */
		String pendingFileName = mediaResourceContainer.getMediaPendingFilePath(context) + FileLocationManager.getPathElementDelimiterString() + mediaResourceContainer.getMediaPendingFileName(context, fileType);

		if (Util.fileCopy(uploadFileNamePath, pendingFileName) == false)
		{
			String errorString = "--> Error on copying media file (to pending directory).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Source file: '" + uploadFileNamePath + "'";
			errorString += "\n--> Destination file: '" + pendingFileName + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media file copied: '" + uploadFileNamePath + "' --> '" + pendingFileName + "'");

		/*
		 * Get hash value of the original file
		 */
		String hashValue = Util.fileGetHashValue(pendingFileName);

		if (hashValue == null)
		{
			String errorString = "--> Error on computing hash code of the media file.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File to be hashed: '" + pendingFileName + "'";
			errorString += "\n--> Original file to be uploaded: '" + uploadFileNamePath + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media file hash value [" + hashValue + "] computed for: '" + pendingFileName + "'");

		/*
		 * Encrypt file on server side
		 */
		if (this.checkServerEncoding(context, mediaResourceContainer))
		{
			String encryptedPendingFileName = mediaResourceContainer.getMediaPendingFilePath(context) + FileLocationManager.getPathElementDelimiterString() + mediaResourceContainer.getMediaPendingFileName(context, fileType);

			encryptedPendingFileName = this.encryptFile(context, mediaResourceContainer, pendingFileName, encryptedPendingFileName);

			if (encryptedPendingFileName == null)
			{
				String errorString = "--> Error on encrypting media file (on server side).";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> File to be encrypted: '" + pendingFileName + "'";
				errorString += "\n--> Original file to be uploaded: '" + uploadFileNamePath + "'";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
				return false;
			}

			// Delete old pending file
			if (!encryptedPendingFileName.equals(pendingFileName))
			{
				// Delete file
				Util.fileDelete(pendingFileName);

				// Logging
				context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media file encrypted on server side: '" + encryptedPendingFileName + "'");
			}

			pendingFileName = encryptedPendingFileName;
		}

		/*
		 * Get file names of older version of the media file, in order to delete
		 * them later
		 */
		List<String> oldFileVersions = mediaResourceContainer.getMediaRealFileName(context, dataIdentifier);

		if (oldFileVersions == null)
		{
			String errorString = "--> Error on searching for media files.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		if (oldFileVersions != null && oldFileVersions.size() > 0)
		{
			for (String oldFilePath : oldFileVersions)
			{
				context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media file old version detected: '" + oldFilePath + "'");
			}
		}

		/*
		 * Move the temporary file in pending directory to the productive
		 * directory
		 */
		String sourceFileName = pendingFileName;
		String destinationFileName = mediaResourceContainer.getMediaRealFileName(context, dataIdentifier, hashValue, fileType);

		// If the destination file already exists, delete the source file only,
		// because source file and destination file are exactly the same files.
		if (Util.fileExists(destinationFileName) == true)
		{
			if (Util.fileDelete(sourceFileName) == false)
			{
				String errorString = "--> Error on deleting media file from 'pending' directory.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> File to be deleted: '" + sourceFileName + "'";
				errorString += "\n--> Original file to be uploaded: '" + uploadFileNamePath + "'";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
				return false;
			}

			// Logging
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media file 'pending' deleted: '" + sourceFileName + "'");

			// Stop processing, to avoid deleting of older files of variable
			// "oldFileVersions"
			return true;
		}
		// If the destination file doesn't exist, move the source file to the
		// regular media file directory.
		else
		{
			if (Util.fileMove(sourceFileName, destinationFileName) == false)
			{
				String errorString = "--> Error on moving media file from 'pending' directory to its 'regular' directory.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Source file name: '" + sourceFileName + "'";
				errorString += "\n--> Destination file name: '" + destinationFileName + "'";
				errorString += "\n--> Original file to be uploaded: '" + uploadFileNamePath + "'";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
				return false;
			}

			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media file moved from 'pending' directory to its 'regular' directory: '" + sourceFileName + "' --> '" + destinationFileName + "'");
		}

		/*
		 * Delete old media file versions logically
		 */
		if (oldFileVersions != null && oldFileVersions.size() > 0)
		{
			for (String oldFilePath : oldFileVersions)
			{
				if (oldFilePath == null || oldFilePath.length() == 0) continue;

				String deletedFileNamePart = Util.fileGetFileNamePart(oldFilePath);
				if (deletedFileNamePart == null || deletedFileNamePart.length() == 0) continue;

				String deletedFileTypePart = Util.fileGetFileTypePart(oldFilePath);
				if (deletedFileTypePart == null || deletedFileTypePart.length() == 0) continue;

				String deletedFilePath = mediaResourceContainer.getMediaDeletedFilePath(context) + FileLocationManager.getPathElementDelimiterString() + FileLocationManager.getMediaDeletedFileName();
				deletedFilePath = FileLocationManager.replacePlacholder(context, deletedFilePath);
				deletedFilePath = deletedFilePath.replace("${originalname}", deletedFileNamePart);
				deletedFilePath = deletedFilePath.replace("${filetype}", deletedFileTypePart);

				if (Util.fileMove(oldFilePath, deletedFilePath) == false)
				{
					String errorString = "--> Error on moving media file from 'regular' directory to the 'deleted' directory.";
					errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
					errorString += "\n--> Source file name: '" + oldFilePath + "'";
					errorString += "\n--> Destination file name: '" + deletedFilePath + "'";
					errorString += "\n--> Original file to be uploaded: '" + uploadFileNamePath + "'";

					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
					return false;
				}

				context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media file moved from 'regular' directory to its 'deleted' directory: '" + oldFilePath + "' --> '" + deletedFilePath + "'");
			}
		}

		/*
		 * Return
		 */
		return true;
	}

	/**
	 * Delete all files of a media file set, but for the newest one.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param files
	 *            The list of files to consider.
	 */
	public void cleanMediaFileList(Context context, ResourceContainerMedia resourceContainerMedia, List<String> files)
	{
		// Validate parameter
		if (files == null) return;
		if (files.size() == 0) return;

		// Clean files
		int nuOfFilesDeleted = Util.fileCleanFileList(files);

		// Notify error message
		String errorString = "--> Duplicated media files found, for one and the same media item.";
		errorString += "\n--> Media resource identifier: '" + resourceContainerMedia.getRecourceIdentifier() + "'";
		
		for (int i = 0; i < files.size(); i++)
		{
			errorString += "\n--> File path [" + String.valueOf(i+1) + "]: '" + files.get(i) + "'";
		}
		
		errorString += "\n--> Additional files were deleted automatically (" + String.valueOf(nuOfFilesDeleted) + " files). Only the newest file was kept.";
		
		context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);

		// Return
		return;
	}

	/**
	 * Get the current server encoding value to be used for real encoding on
	 * server side.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the server encoding value, or <TT>null</TT> if no value
	 *         is defined or available.
	 * 
	 */
	private String getServerEncodingValue(Context context)
	{
		try
		{
			String serverEncodingValue = this.serverMediaKeyList.get(this.serverMediaKeyNumber);
			if (serverEncodingValue == null || serverEncodingValue.length() == 0) return null;
			return serverEncodingValue;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Getter
	 */
	public boolean isServerEncodingEnabled()
	{
		return this.serverEncodingEnabled;
	}

	/**
	 * Getter
	 */
	public int getServerMediaKeyNumber()
	{
		return this.serverMediaKeyNumber;
	}
}
