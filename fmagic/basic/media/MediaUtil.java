package fmagic.basic.media;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fmagic.basic.context.Context;
import fmagic.basic.file.FileLocationFunctions;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * This class implements UTIL functions for the media manager.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 17.05.2013 - Created
 * 
 */
public class MediaUtil
{
	// Media Manager object that holds the UTIL class
	private final MediaManager mediaManager;
	
	// Cleaning parameter
	protected int cleanPendingDaysToKeep = 0;
	protected int cleanDeletedDaysToKeep = 0;
	protected int cleanObsoleteDaysToKeep = 0;

	/**
	 * Constructor
	 * 
	 * @param mediaManager
	 *            The media manager object that holds the UTIL class.
	 */
	public MediaUtil(MediaManager mediaManager)
	{
		this.mediaManager = mediaManager;
	}
	
	/**
	 * Check on integrity errors of resource identifiers.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	boolean validateResources(Context context)
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
				if (validateResourcesCkeckOnAliasName(context, resourceContainer) == true) isIntegrityError = true;

				// The attribute 'MediaType' must be set
				if (validateResourcesCkeckOnAttributeMediaType(context, resourceContainer) == true) isIntegrityError = true;

				// The attribute 'FileTypes' must be set
				if (validateResourcesCkeckOnAttributeFileTypes(context, resourceContainer) == true) isIntegrityError = true;

				// The attribute 'StorageLocation' must be set
				if (validateResourcesCkeckOnAttributeStorageLocation(context, resourceContainer) == true) isIntegrityError = true;

				// The attribute 'LogicalPath' must be set
				if (validateResourcesCkeckOnAttributeLogicalPath(context, resourceContainer) == true) isIntegrityError = true;

				// The attribute 'ServerEncoding' is optional
				if (validateResourcesCkeckOnAttributeServerEncoding(context, resourceContainer) == true) isIntegrityError = true;

				// The attribute 'ClientEncoding' is optional
				if (validateResourcesCkeckOnAttributeClientEncoding(context, resourceContainer) == true) isIntegrityError = true;
			}

		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isIntegrityError;
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
	private boolean validateResourcesCkeckOnAliasName(Context context, ResourceContainer resourceContainer)
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
	private boolean validateResourcesCkeckOnAttributeMediaType(Context context, ResourceContainer resourceContainer)
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
	private boolean validateResourcesCkeckOnAttributeFileTypes(Context context, ResourceContainer resourceContainer)
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
	private boolean validateResourcesCkeckOnAttributeStorageLocation(Context context, ResourceContainer resourceContainer)
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
	private boolean validateResourcesCkeckOnAttributeLogicalPath(Context context, ResourceContainer resourceContainer)
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
						String normalizedMediaLogicalPathValue = FileUtilFunctions.generalFitToFileNameCompatibility(partsOfMediaLogicalPathValue[i]);

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
	private boolean validateResourcesCkeckOnAttributeServerEncoding(Context context, ResourceContainer resourceContainer)
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
	private boolean validateResourcesCkeckOnAttributeClientEncoding(Context context, ResourceContainer resourceContainer)
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
	 * Remove all media files in the 'pending' directory of a media resource
	 * that are older than a specific number of days.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param daysToKeep
	 *            All files that are older than this number of days (resp. 1440
	 *            minutes, from <TT>now</TT>) are removed. Please set at least
	 *            <TT>1</TT> day to keep. If the parameter is set lower than 1
	 *            it is set to one day automatically.
	 * 
	 * @return Returns the number of deleted files.
	 */
	public int cleanPendingDirectory(Context context, ResourceContainerMedia mediaResourceContainer, int daysToKeep)
	{
		// Validate parameter
		if (mediaResourceContainer == null) return 0;

		try
		{
			// Get 'pending' directory of the media resource
			String directoryPath = mediaResourceContainer.mediaFileGetPendingFilePath(context);

			// / Logging
			String logText = "\n--> CLEAN 'PENDING' MEDIA DIRECTORY: Begin of cleaning (pending directory of a specific media resource)";
			logText += "\n--> Media directory: '" + directoryPath + "'";
			logText += "\n--> Days to keep: '" + String.valueOf(daysToKeep) + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Get file list of expired files
			List<String> files = FileUtilFunctions.directorySearchOnExpiredFiles(directoryPath, "*", daysToKeep);

			if (files == null) return 0;
			if (files.size() == 0) return 0;

			// Logging
			logText = "\n--> CLEAN 'PENDING' MEDIA DIRECTORY: List of media files to delete:";

			int nuOfFilesToDelete = 0;

			for (String file : files)
			{
				logText += "\n(" + String.valueOf(++nuOfFilesToDelete) + ") [" + file + "]";
			}

			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Delete files
			int nuOfRemovedFiles = FileUtilFunctions.fileDelete(files);

			// Logging
			logText = "\n--> CLEAN 'PENDING' MEDIA DIRECTORY: End of cleaning (pending directory of a specific media resource)";
			logText += "\n--> Total number of deleted files: '" + String.valueOf(nuOfRemovedFiles) + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Return
			return nuOfRemovedFiles;
		}
		catch (Exception e)
		{
			String errorString = "--> CLEAN 'PENDING' MEDIA DIRECTORY: Error on processing cleaning.";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, e);
			return 0;
		}
	}

	/**
	 * Remove all media files in the 'deleted' directory of a media resource
	 * that are older than a specific number of days.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param daysToKeep
	 *            All files that are older than this number of days (resp. 1440
	 *            minutes, from <TT>now</TT>) are removed. Please set at least
	 *            <TT>1</TT> day to keep. If the parameter is set lower than 1
	 *            it is set to one day automatically.
	 * 
	 * @return Returns the number of deleted files.
	 */
	public int cleanDeletedDirectory(Context context, ResourceContainerMedia mediaResourceContainer, int daysToKeep)
	{
		// Validate parameter
		if (mediaResourceContainer == null) return 0;

		try
		{
			// Get 'deleted' directory of the media resource
			String directoryPath = mediaResourceContainer.mediaFileGetDeletedFilePath(context);

			// / Logging
			String logText = "\n--> CLEAN 'DELETED' MEDIA DIRECTORY: Begin of cleaning (deleted directory of a specific media resource)";
			logText += "\n--> Media directory: '" + directoryPath + "'";
			logText += "\n--> Days to keep: '" + String.valueOf(daysToKeep) + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Get file list of expired files
			List<String> files = FileUtilFunctions.directorySearchOnExpiredFiles(directoryPath, "*", daysToKeep);

			if (files == null) return 0;
			if (files.size() == 0) return 0;

			// Logging
			logText = "\n--> CLEAN 'DELETED' MEDIA DIRECTORY: List of media files to delete:";

			int nuOfFilesToDelete = 0;

			for (String file : files)
			{
				logText += "\n(" + String.valueOf(++nuOfFilesToDelete) + ") [" + file + "]";
			}

			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Delete files
			int nuOfRemovedFiles = FileUtilFunctions.fileDelete(files);

			// Logging
			logText = "\n--> CLEAN 'DELETED' MEDIA DIRECTORY: End of cleaning (deleted directory of a specific media resource)";
			logText += "\n--> Total number of deleted files: '" + String.valueOf(nuOfRemovedFiles) + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Return
			return nuOfRemovedFiles;
		}
		catch (Exception e)
		{
			String errorString = "--> CLEAN 'DELETED' MEDIA DIRECTORY: Error on processing cleaning.";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, e);
			return 0;
		}
	}

	/**
	 * Move all obsolete media files, regarding a media resource, that are older
	 * than a specific number of days from the 'regular' media directory to the
	 * 'deleted' directory.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param daysToKeep
	 *            All files that are older than this number of days (resp. 1440
	 *            minutes, from <TT>now</TT>), and that are obsolete, are moved.
	 *            Please set at least <TT>1</TT> day to keep. If the parameter
	 *            is set lower than 1 it is set to one day automatically.
	 * 
	 * @return Returns the number of moved files.
	 */
	public int cleanRegularDirectory(Context context, ResourceContainerMedia mediaResourceContainer, int daysToKeep)
	{
		// Initialize
		Set<String> usedDataIdentifiers = new HashSet<String>();
		int nuOfMovedFiles = 0;

		// Validate parameter
		if (mediaResourceContainer == null) return 0;

		// Get directories to consider
		String regularMediaFilesDirectory = mediaResourceContainer.mediaFileGetRegularFilePath(context);
		String deletedMediaFilesDirectory = mediaResourceContainer.mediaFileGetDeletedFilePath(context);

		// / Logging
		String logText = "\n--> CLEAN 'REGULAR' MEDIA DIRECTORY: Begin of cleaning (regular directory of a specific media resource)";
		logText += "\n--> Media directory: '" + regularMediaFilesDirectory + "'";
		logText += "\n--> Days to keep: '" + String.valueOf(daysToKeep) + "'";
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

		/*
		 * Get List of all different data identifiers that are real used in the
		 * 'regular' directory.
		 */
		try
		{
			// Logging
			logText = "\n--> CLEAN 'REGULAR' MEDIA DIRECTORY: List of data identifiers found:";
			logText += "\n--> ";

			// Get file list of all files
			List<String> allFiles = FileUtilFunctions.directorySearchForFiles(regularMediaFilesDirectory, "*");

			if (allFiles == null) return 0;
			if (allFiles.size() == 0) return 0;

			// Go through the list and collect all data identifiers
			for (String filePath : allFiles)
			{
				if (filePath == null || filePath.length() == 0) continue;

				String dataIdentifier = mediaResourceContainer.mediaFileGetFileNamePartDataIdentifier(context, filePath);
				if (dataIdentifier == null || dataIdentifier.length() == 0) continue;

				usedDataIdentifiers.add(dataIdentifier);

				logText += "[" + dataIdentifier + "] ";
			}

			// Check list
			if (usedDataIdentifiers.size() == 0) return 0;

			// / Logging
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);
		}
		catch (Exception e)
		{
			String errorString = "--> CLEAN 'REGULAR' MEDIA DIRECTORY: Error on processing cleaning.";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, e);
			return 0;
		}

		/*
		 * Move all obsolete files.
		 */
		try
		{
			// Logging
			logText = "\n--> CLEAN 'REGULAR' MEDIA DIRECTORY: List of media files moved:";

			// Go through the list of data identifiers and move obsolete files
			for (String dataIdentifier : usedDataIdentifiers)
			{
				// Check value
				if (dataIdentifier == null || dataIdentifier.length() == 0) continue;

				// Get file filter mask of the specific data identifier
				String fileFilterMask = mediaResourceContainer.mediaFileGetNameMask(context, dataIdentifier);

				// Get file list of obsolete files
				List<String> obsoleteFiles = FileUtilFunctions.directorySearchOnObsoleteFiles(regularMediaFilesDirectory, fileFilterMask, daysToKeep);
				if (obsoleteFiles == null || obsoleteFiles.size() == 0) continue;

				// Move files
				for (String filePath : obsoleteFiles)
				{
					String originalFileName = FileUtilFunctions.fileGetFileNamePart(filePath);
					if (originalFileName == null || originalFileName.length() == 0) continue;

					String fileType = FileUtilFunctions.fileGetFileTypePart(filePath);
					if (fileType == null || fileType.length() == 0) continue;

					String deletedFilePath = FileLocationFunctions.compileFilePath(deletedMediaFilesDirectory, mediaResourceContainer.mediaFileGetDeletedFileName(context, originalFileName, fileType));

					if (FileUtilFunctions.fileMove(filePath, deletedFilePath) == true)
					{
						nuOfMovedFiles++;

						logText += "\n(" + String.valueOf(nuOfMovedFiles) + ") [" + filePath + "] moved to [" + deletedFilePath + "]";
					}
				}
			}

			// / Logging
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			logText = "\n--> CLEAN 'REGULAR' MEDIA DIRECTORY: End of cleaning (regular directory of a specific media resource)";
			logText += "\n--> Total number of moved files: '" + String.valueOf(nuOfMovedFiles) + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Return
			return nuOfMovedFiles;
		}
		catch (Exception e)
		{
			String errorString = "--> CLEAN 'REGULAR' MEDIA DIRECTORY: Error on processing cleaning.";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, e);
			return 0;
		}
	}

	/**
	 * Clean all media directories of a specific media resources.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @return Returns the number of deleted files.
	 */
	public int cleanAllDirectories(Context context, ResourceContainerMedia mediaResourceContainer)
	{
		// Validate parameter
		if (mediaResourceContainer == null) return 0;

		try
		{
			int nuOfMovedFiles = 0;

			// / Logging
			String logText = "\n--> CLEAN MEDIA DIRECTORIES: Begin of cleaning (all directories of a specific media resource)";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Cleaning
			nuOfMovedFiles = nuOfMovedFiles + this.cleanRegularDirectory(context, mediaResourceContainer, this.cleanObsoleteDaysToKeep);
			nuOfMovedFiles = nuOfMovedFiles + this.cleanPendingDirectory(context, mediaResourceContainer, this.cleanPendingDaysToKeep);
			nuOfMovedFiles = nuOfMovedFiles + this.cleanDeletedDirectory(context, mediaResourceContainer, this.cleanDeletedDaysToKeep);

			// / Logging
			logText = "\n--> CLEAN MEDIA DIRECTORIES: End of cleaning (all directories of a specific media resource)";
			logText += "\n--> Total number of cleaned files: '" + String.valueOf(nuOfMovedFiles) + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Return
			return nuOfMovedFiles;
		}
		catch (Exception e)
		{
			String errorString = "--> CLEAN MEDIA DIRECTORIES: Error on processing cleaning.";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, e);
			return 0;
		}
	}

	/**
	 * Clean all media directories for all media resources.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the number of deleted files.
	 */
	public int cleanAll(Context context)
	{
		try
		{
			// Initialize
			int nuOfMovedFiles = 0;

			// / Logging
			String logText = "\n--> CLEAN ALL MEDIA: Begin of cleaning (all directories of all media resources)";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Get all resource identifiers
			String typeCriteria[] = { "Media" };
			String applicationCriteria[] = { context.getApplicationName() };
			String originCriteria[] = { "All", "Server", "Client" };
			String usageCriteria[] = null;
			String groupCriteria[] = null;
			List<String> mediaResourceIdentifiers = context.getResourceManager().getResourceIdentifierList(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

			// Clean all resources
			for (String identifier : mediaResourceIdentifiers)
			{
				if (identifier == null || identifier.length() == 0) continue;

				ResourceContainer mediaContainerProvisional = new ResourceContainer(identifier);

				ResourceContainerMedia mediaContainer = ResourceManager.media(context, mediaContainerProvisional.getGroup(), mediaContainerProvisional.getName());
				if (mediaContainer == null) continue;

				nuOfMovedFiles = nuOfMovedFiles + this.cleanAllDirectories(context, mediaContainer);
			}

			// / Logging
			logText = "\n--> CLEAN ALL MEDIA: End of cleaning (all directories of all media resources)";
			logText += "\n--> Total number of cleaned files: '" + String.valueOf(nuOfMovedFiles) + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Return
			return nuOfMovedFiles;
		}
		catch (Exception e)
		{
			String errorString = "--> CLEAN ALL MEDIA: Error on processing cleaning.";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, e);
			return 0;
		}
	}

	/**
	 * Push a media file into the system.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param dataIdentifier
	 *            The data identifier of the media.
	 * 
	 * @param fileType
	 *            The file type to consider.
	 * 
	 * @return Returns <TT>true</TT> if the media file could be pushed,
	 *         otherwise <TT>false</TT>.
	 * 
	 */
	public boolean writeMediaContentToMediaFile(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier, String fileType, String mediaContent)
	{
		try
		{
			/*
			 * Extract media content into a pending file
			 */
			String pendingFileName = FileLocationFunctions.compileFilePath(mediaResourceContainer.mediaFileGetPendingFilePath(context), mediaResourceContainer.mediaFileGetPendingFileName(context, fileType));

			String logText = "\n--> PUSH MEDIA CONTENT: Pending file name created";
			logText += "\n--> Pending file name: '" + pendingFileName + "'";
			logText += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			if (FileUtilFunctions.fileWriteFromString(pendingFileName, mediaContent) == false)
			{
				String errorString = "--> PUSH MEDIA CONTENT: Error on writing media content into the pending file";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Pending file name: '" + pendingFileName + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
				return false;
			}

			logText = "\n--> PUSH MEDIA CONTENT: Media content stored into pending file";
			logText += "\n--> Pending file name: '" + pendingFileName + "'";
			logText += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			logText += "\n--> Data identifier: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			/*
			 * Upload pending file into the system as a regular media file
			 */
			if (context.getMediaManager().localStoreMediaFile(context, mediaResourceContainer, pendingFileName, dataIdentifier) == false)
			{
				String errorString = "--> PUSH MEDIA CONTENT: Error on storing pending file as regular media file";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Pending file to be stored: '" + pendingFileName + "'";
				errorString += "\n--> Data identifier: '" + dataIdentifier + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
				return false;
			}

			logText = "\n--> PUSH MEDIA CONTENT: Pending file stored into regular media file";
			logText += "\n--> Pending file name: '" + pendingFileName + "'";
			logText += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			logText += "\n--> Data identifier: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			/*
			 * Delete pending file
			 */
			if (FileUtilFunctions.fileDelete(pendingFileName) == false)
			{
				String errorString = "--> PUSH MEDIA CONTENT: Error on deleting pending file";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Pending file to be deleted: '" + pendingFileName + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
				// No return, because the media content could be pushed
				// successfully
			}

			logText = "\n--> PUSH MEDIA CONTENT: Pending file deleted";
			logText += "\n--> Pending file name: '" + pendingFileName + "'";
			logText += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			/*
			 * Return
			 */
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Read the content of a media file as a string.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param dataIdentifier
	 *            The data identifier of the media.
	 * 
	 * @return Returns the media content as string, or <TT>null</TT> if the
	 *         media file doesn't exist or couldn't be read.
	 * 
	 */
	public String readMediaContentFromMediaFile(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		try
		{
			// Create media container
			MediaContainer mediaContainer = new MediaContainer(context, mediaResourceContainer, dataIdentifier);

			if (mediaContainer.bindMedia() == false)
			{
				String errorString = "--> READ MEDIA CONTENT: Error on binding media container";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Data identifier: '" + dataIdentifier + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
				return null;
			}

			// Read file content
			String mediaContent = mediaContainer.readMediaContentAsString();

			if (mediaContent == null)
			{
				String errorString = "--> READ MEDIA CONTENT: Error on reading media content";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Data identifier: '" + dataIdentifier + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
				return null;
			}

			// Release media file
			if (mediaContainer.releaseMedia() == false)
			{
				String errorString = "--> READ MEDIA CONTENT: Error on releasing media container";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Data identifier: '" + dataIdentifier + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
				// No return, because the media content could be read
				// successfully
			}

			// Return
			return mediaContent;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Check if the maximum size of the media file is exceeded (related to the
	 * general configuration parameter)
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param mediaFileName
	 *            The media file to analyze.
	 * 
	 * @return Returns <TT>true</TT> if the maximum size is exceeded or an error
	 *         occurred, otherwise <TT>false</TT>.
	 */
	boolean isMaximumFileSizeExceeded(Context context, String mediaFileName)
	{
		// Validate parameter
		if (mediaFileName == null || mediaFileName.length() == 0) return true;

		// No constraint set
		Integer maximumMediaSizeInKilobyte = this.mediaManager.getMaximumMediaSize();
		if (maximumMediaSizeInKilobyte == null) return false;

		// Check file size
		try
		{
			Long fileSizeInByte = FileUtilFunctions.fileGetFileSize(mediaFileName);

			if (fileSizeInByte > (maximumMediaSizeInKilobyte * 1024)) return true;
			return false;
		}
		catch (Exception e)
		{
			return true;
		}
	}

	/**
	 * Check if a media file already exists, using the following criteria: media
	 * resource container and data identifier.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param dataIdentifier
	 *            The data identifier of the media.
	 * 
	 * @return Returns <TT>true</TT> if the media file exists, otherwise
	 *         <TT>false</TT>.
	 * 
	 */
	public boolean isMediaFileExists(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		try
		{
			// Get all values from server
			String currentFileName = mediaResourceContainer.mediaFileGetRealFileName(context, dataIdentifier);

			if (currentFileName == null || currentFileName.length() == 0) { return false; }

			// Return
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Check if a media file already exists, using the following criteria: media
	 * resource container, data identifier, file type and hash value.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param dataIdentifier
	 *            The data identifier of the media.
	 * 
	 * @param fileType
	 *            The file type to consider.
	 * 
	 * @param hashValue
	 *            The hash value to consider.
	 * 
	 * @return Returns <TT>true</TT> if the media file exists, otherwise
	 *         <TT>false</TT>.
	 * 
	 */
	public boolean isMediaFileExists(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier, String fileType, String hashValue)
	{
		try
		{
			// Get all values from server
			String currentFileName = mediaResourceContainer.mediaFileGetRealFileName(context, dataIdentifier);

			if (currentFileName == null || currentFileName.length() == 0) { return false; }

			// Compare file type
			String currentFileType = FileUtilFunctions.fileGetFileTypePart(currentFileName);

			if (currentFileType == null || !currentFileType.equals(fileType)) { return false; }

			// Compare hash value
			String currentHashValue = mediaResourceContainer.mediaFileGetFileNamePartHashValue(context, currentFileName);

			if (currentHashValue == null || !currentHashValue.equals(hashValue)) { return false; }

			// Return
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Get the file type of a media file.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param dataIdentifier
	 *            The data identifier of the media.
	 * 
	 * @return Returns the file type as string, or <TT>null</TT> if the media
	 *         file doesn't exist.
	 * 
	 */
	public String getMediaFileType(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		try
		{
			// Get the current file name of the most recent media file
			String currentFileName = mediaResourceContainer.mediaFileGetRealFileName(context, dataIdentifier);

			if (currentFileName == null || currentFileName.length() == 0) { return null; }

			// Extract the file type
			String fileType = FileUtilFunctions.fileGetFileTypePart(currentFileName);

			if (fileType == null || fileType.length() == 0) { return null; }

			// Return
			return fileType;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Getter
	 */
	public int getCleanPendingDaysToKeep()
	{
		return this.cleanPendingDaysToKeep;
	}

	/**
	 * Getter
	 */
	public int getCleanDeletedDaysToKeep()
	{
		return this.cleanDeletedDaysToKeep;
	}

	/**
	 * Getter
	 */
	public int getCleanObsoleteDaysToKeep()
	{
		return this.cleanObsoleteDaysToKeep;
	}

	/**
	 * TEST Setter
	 * 
	 * Please notice: This setter is supposed test purposes only. It doesn't
	 * work if it runs in productive environment.
	 */
	public void testSetCleanDeletedDaysToKeep(Context context, int cleanDeletedDaysToKeep)
	{
		if (!context.isRunningInTestMode()) return;

		this.cleanDeletedDaysToKeep = cleanDeletedDaysToKeep;
	}
}
