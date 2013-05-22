package fmagic.basic.media;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fmagic.basic.application.ManagerInterface;
import fmagic.basic.command.ConnectionContainer;
import fmagic.basic.command.ResponseContainer;
import fmagic.basic.context.Context;
import fmagic.basic.file.FileLocationFunctions;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.command.ClientCommand;
import fmagic.client.command.ClientCommandMediaFileCheck;
import fmagic.client.command.ClientCommandMediaFileInfo;
import fmagic.client.command.ClientCommandMediaFileRead;
import fmagic.client.command.ClientCommandMediaFileUpload;

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
 * @changed FW 23.04.2013 - Created
 * 
 */
public abstract class MediaManager implements ManagerInterface
{
	// Settings for Server Encoding
	protected final HashMap<Integer, String> encodingKeyList = new HashMap<Integer, String>();
	protected int encodingKeyNumber = 0;
	protected boolean encodingEnabled = false;

	// Common media file configuration properties
	protected String mediaRootFilePath = null;
	protected Integer maximumMediaSize = null;

	// Cleaning parameter
	protected int cleanPendingDaysToKeep = 0;
	protected int cleanDeletedDaysToKeep = 0;
	protected int cleanObsoleteDaysToKeep = 0;

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
	public boolean cleanEnvironment(Context context)
	{
		// Clean environment
		this.cleanAll(context);

		// Return
		return false;
	}

	@Override
	public boolean readConfiguration(Context context)
	{
		boolean isError = false;

		try
		{
			// Read parameter: CleanPendingDaysToKeep
			this.cleanPendingDaysToKeep = context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "Media", "CleanPendingDaysToKeep"), false);

			// Read parameter: CleanDeletedDaysToKeep
			this.cleanDeletedDaysToKeep = context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "Media", "CleanDeletedDaysToKeep"), false);

			// Read parameter: CleanObsoleteDaysToKeep
			this.cleanObsoleteDaysToKeep = context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "Media", "CleanObsoleteDaysToKeep"), false);

			// Read parameter: MaximumMediaSize
			this.maximumMediaSize = context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "Media", "MaximumMediaSize"), false);
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), null, e);
			isError = true;
		}

		/*
		 * Return
		 */
		return isError;
	}

	@Override
	public boolean validateResources(Context context)
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
	private void notifyWatchdog(Context context, String resourceIdentifier, String additionalTextParameter)
	{
		try
		{
			// Set message Text
			String messageText = "Access to Media Item";

			// Set additional text
			String additionalText = "--> Access to Media Item";
			if (additionalTextParameter != null) additionalText += "\n" + additionalTextParameter;
			additionalText += "\n--> Resource identifier: '" + resourceIdentifier + "'";

			// Set resource identifier documentation
			String resourceDocumentationText = null;
			resourceDocumentationText = context.getResourceManager().getResourceContainer(context, resourceIdentifier).printManual(context);

			if (context.getWatchdogManager() != null) context.getWatchdogManager().addWatchdogCommand(context, resourceIdentifier, messageText, additionalText, resourceDocumentationText, null, new Date());
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
	public abstract boolean isEncodingEnabled(Context context, ResourceContainerMedia mediaResourceContainer);

	/**
	 * Encrypt a media file.
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
	 * @param destinationFilePath
	 *            The full path of the file the encrypted code is to store.
	 * 
	 * @return Returns the full path of the encoded pending file that was
	 *         created, or <TT>null</TT> if an error occurred.
	 */
	protected String localEncryptMediaFile(Context context, ResourceContainerMedia mediaResourceContainer, String sourceFilePath, String destinationFilePath)
	{
		// Check if server encoding is enabled
		if (this.isEncodingEnabled(context, mediaResourceContainer) == false) return sourceFilePath;

		// Get key value (password)
		String keyValue = this.getEncodingValue(context);
		if (keyValue == null) return sourceFilePath;

		// Encode media file
		CipherHandler cipherHandler = new CipherHandler(context);

		if (cipherHandler.encrypt(keyValue, sourceFilePath, destinationFilePath) == false)
		{
			String errorString = "--> ENCRYPT: Error on encrypting media file.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of original media file: '" + sourceFilePath + "'";
			errorString += "\n--> File name of destination media file: '" + destinationFilePath + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
			return null;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> ENCRYPT: Encrypt media file: '" + sourceFilePath + "' --> '" + destinationFilePath + "'");

		// Return
		return destinationFilePath;
	}

	/**
	 * Decrypt a media file.
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
	protected String localDecryptMediaFile(Context context, ResourceContainerMedia mediaResourceContainer, String sourceFilePath)
	{
		/*
		 * Validate parameter
		 */
		if (mediaResourceContainer == null) return null;
		if (sourceFilePath == null || sourceFilePath.length() == 0) return null;

		/*
		 * Get server encoding key number of the real file
		 */
		int keyNumber = mediaResourceContainer.mediaFileGetEncodingKeyOfRealFileName(context, sourceFilePath);

		/*
		 * Get file type of media file
		 */
		String fileType = FileUtilFunctions.fileGetFileTypePart(sourceFilePath);

		if (fileType == null || fileType.length() == 0)
		{
			String errorString = "--> DECRYPT: Missing file type of media file.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of media file: '" + sourceFilePath + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
			return null;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> DECRYPT: File type: '" + fileType + "'");

		/*
		 * Copy media file to pending directory
		 */

		// Get file path of pending media file
		String pendingFilePath = FileLocationFunctions.compileFilePath(mediaResourceContainer.mediaFileGetPendingFilePath(context), mediaResourceContainer.mediaFileGetPendingFileName(context, fileType));

		// Copy media file to pending directory (with Retry, because it can take
		// a longer time to copy large files).
		int nuOfAttempts = FileUtilFunctions.fileCopyRetry(sourceFilePath, pendingFilePath);

		if (nuOfAttempts <= 0)
		{
			String errorString = "--> DECRYPT: Error on coping media file.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Source file name: '" + sourceFilePath + "'";
			errorString += "\n--> Destination file name: '" + pendingFilePath + "'";
			errorString += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
			return null;
		}

		String logText = "\n--> DECRYPT: Media file copied: '" + sourceFilePath + "' --> '" + pendingFilePath + "'";
		if (nuOfAttempts > 1) logText += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

		/*
		 * Stop process if the source file isn't encrypted
		 */
		if (keyNumber == 0) return pendingFilePath;

		/*
		 * Decrypt media file
		 */

		// Get key value (password)
		String keyValue = this.encodingKeyList.get(keyNumber);

		if (keyValue == null || keyValue.length() == 0)
		{
			String errorString = "--> DECRYPT: Missing key value (password) for server encoding key '" + String.valueOf(keyNumber) + "'.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of media file: '" + sourceFilePath + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
			return null;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> DECRYPT: Key number for password: '" + keyNumber + "'");

		// Check if the current 'pending' file exists and can be accessed (with
		// Retry, because it can take a longer time to copy large files).
		nuOfAttempts = FileUtilFunctions.fileExistsRetry(pendingFilePath);

		if (nuOfAttempts <= 0)
		{
			String errorString = "--> DECRYPT: Error on checking 'pending' media file.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File path checked for: '" + pendingFilePath + "'";
			errorString += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
			return null;
		}

		logText = "\n--> DECRYPT: Checking 'pending' media file: '" + pendingFilePath + "'";
		if (nuOfAttempts > 1) logText += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

		// Get new 'pending' file path as destination file path for the file to
		// be decrypted
		String destinationFilePath = FileLocationFunctions.compileFilePath(mediaResourceContainer.mediaFileGetPendingFilePath(context), mediaResourceContainer.mediaFileGetPendingFileName(context, fileType));

		// Decrypt media file
		CipherHandler cipherHandler = new CipherHandler(context);

		if (cipherHandler.decrypt(keyValue, pendingFilePath, destinationFilePath) == false)
		{
			String errorString = "--> DECRYPT: Error on decrypting media file.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of original media file: '" + sourceFilePath + "'";
			errorString += "\n--> File name of decrypted media file (copy of original file): '" + pendingFilePath + "'";
			errorString += "\n--> File name of destination media file: '" + destinationFilePath + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
			return null;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> DECRYPT: Decrypt media file: '" + pendingFilePath + "' --> '" + destinationFilePath + "'");

		// Get hash value of source file (directly from file name)
		String hashValueSourceFile = mediaResourceContainer.mediaFileGetHashValueOfRealFileName(context, sourceFilePath);

		if (hashValueSourceFile == null)
		{
			String errorString = "--> DECRYPT: Error on analyzing hash value in media file name.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name analyzed: '" + sourceFilePath + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);

			FileUtilFunctions.fileDelete(pendingFilePath);

			return null;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> DECRYPT: Hash value [" + hashValueSourceFile + "] on media file '" + sourceFilePath + "'");

		// Get hash value of destination file (computed)
		String hashValueDestinationFile = FileUtilFunctions.fileGetHashValue(destinationFilePath);

		if (hashValueDestinationFile == null)
		{
			String errorString = "--> DECRYPT: Error on computing hash value of a media file.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File to be hashed: '" + destinationFilePath + "'";
			errorString += "\n--> Please notice: The file was not deleted automatically from the 'pending' directory, in order to have a chance to analyze it later.";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
			return null;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> DECRYPT: Hash value [" + hashValueDestinationFile + "] on media file '" + destinationFilePath + "'");

		// Compare hash value to ensure that decrypting worked well
		if (!hashValueSourceFile.equals(hashValueDestinationFile))
		{
			String errorString = "--> DECRYPT: Mismatch on hash value after decrypting media file.";
			errorString += "\n--> The key value (password) of the encoding key: '" + String.valueOf(keyNumber) + "' might be changed.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Encrypted file [1] before decrypting: '" + sourceFilePath + "'";
			errorString += "\n--> Hash value of file [1]: '" + hashValueSourceFile + "'";
			errorString += "\n--> Decrypted file [2] after decrypting: '" + destinationFilePath + "'";
			errorString += "\n--> Hash value of original file to be copied before: '" + hashValueDestinationFile + "'";
			errorString += "\n--> Please notice: The file [2] was not deleted automatically from the 'pending' directory, in order to have a chance to analyze it later.";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
			return null;
		}

		// Delete pending file, because destinationfilePath is the result file
		if (FileUtilFunctions.fileDelete(pendingFilePath) == false)
		{
			String errorString = "--> DECRYPT: Error on deleting media file from 'pending' directory.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Media file path: '" + pendingFilePath + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
			return null;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> DECRYPT: Pending media file deleted '" + pendingFilePath + "'");

		// Return
		return destinationFilePath;
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
	protected boolean localStoreMediaFile(Context context, ResourceContainerMedia mediaResourceContainer, String uploadFileNamePath, String dataIdentifier)
	{
		/*
		 * Check variables and conditions
		 */

		// Check media resource container
		if (mediaResourceContainer == null)
		{
			String errorString = "--> STORE LOCAL: Media resource container not set (NULL value).";
			if (uploadFileNamePath != null) errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
			return false;
		}

		// Check file path
		if (uploadFileNamePath == null || uploadFileNamePath.length() == 0)
		{
			String errorString = "--> STORE LOCAL: Missing file name of the file to be uploaded (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
			return false;
		}

		// Check data identifier
		if (dataIdentifier == null || dataIdentifier.length() == 0)
		{
			String errorString = "--> STORE LOCAL: Missing data identifier of media (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (uploadFileNamePath != null) errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
			return false;
		}

		// Check if file exists
		if (FileUtilFunctions.fileExists(uploadFileNamePath) == false)
		{
			String errorString = "--> STORE LOCAL: File to be uploaded doesn't exist or is not accessable.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
			return false;
		}

		// Checks if file type is set
		String fileType = FileUtilFunctions.fileGetFileTypePart(uploadFileNamePath);

		if (fileType == null || fileType.length() == 0)
		{
			String errorString = "--> STORE LOCAL: Missing file type.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
			return false;
		}

		// Checks if file type allowed for the given media resource item
		if (mediaResourceContainer.attributeIsFileTypeSupported(context, fileType) == false)
		{
			String errorString = "--> STORE LOCAL: File type '" + fileType + "' is not supported by the current media resource item.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
			return false;
		}

		// Checks if the maximum size of the media file is exceeded (related to
		// the media resource attribute)
		if (mediaResourceContainer.mediaFileIsMaximumFileSizeExceeded(context, uploadFileNamePath) == true)
		{
			String errorString = "--> STORE LOCAL: Maximum allowed media file size exceeded.";
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
			String errorString = "--> STORE LOCAL: Maximum allowed media file size exceeded.";
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
		 * Create file directory for pending files
		 */
		String pendingFilePathDirectory = mediaResourceContainer.mediaFileGetPendingFilePath(context);

		try
		{
			File directory = new File(pendingFilePathDirectory);
			directory.mkdirs();
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media file directory 'pending' created: '" + pendingFilePathDirectory + "'");
		}
		catch (Exception e)
		{
			String errorString = "--> STORE LOCAL: Error on creating directory for pending media files.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Directory to be created: '" + pendingFilePathDirectory + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, e);
			return false;
		}

		/*
		 * Create file directory for deleted files
		 */
		String deletedFilePathDirectory = mediaResourceContainer.mediaFileGetDeletedFilePath(context);

		try
		{
			File directory = new File(deletedFilePathDirectory);
			directory.mkdirs();
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media file directory 'deleted' created: '" + deletedFilePathDirectory + "'");
		}
		catch (Exception e)
		{
			String errorString = "--> STORE LOCAL: Error on creating directory for deleted media files.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Directory to be created: '" + deletedFilePathDirectory + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, e);
			return false;
		}

		/*
		 * Copy original file to the pending file directory
		 */
		String pendingFilePath = FileLocationFunctions.compileFilePath(mediaResourceContainer.mediaFileGetPendingFilePath(context), mediaResourceContainer.mediaFileGetPendingFileName(context, fileType));

		if (FileUtilFunctions.fileCopy(uploadFileNamePath, pendingFilePath) == false)
		{
			String errorString = "--> STORE LOCAL: Error on copying media file (to pending directory).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Source file: '" + uploadFileNamePath + "'";
			errorString += "\n--> Destination file: '" + pendingFilePath + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
			return false;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> STORE LOCAL: Media file copied: '" + uploadFileNamePath + "' --> '" + pendingFilePath + "'");

		/*
		 * Get hash value of the original file
		 */
		String hashValue = FileUtilFunctions.fileGetHashValue(pendingFilePath);

		if (hashValue == null)
		{
			String errorString = "--> STORE LOCAL: Error on computing hash code of the media file.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File to be hashed: '" + pendingFilePath + "'";
			errorString += "\n--> Original file to be uploaded: '" + uploadFileNamePath + "'";

			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
			return false;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> STORE LOCAL: Media file hash value [" + hashValue + "] computed for: '" + pendingFilePath + "'");

		/*
		 * Encrypt the file, if encoding is set
		 */
		if (this.isEncodingEnabled(context, mediaResourceContainer))
		{
			String encryptedPendingFileName = FileLocationFunctions.compileFilePath(mediaResourceContainer.mediaFileGetPendingFilePath(context), mediaResourceContainer.mediaFileGetPendingFileName(context, fileType));

			encryptedPendingFileName = this.localEncryptMediaFile(context, mediaResourceContainer, pendingFilePath, encryptedPendingFileName);

			if (encryptedPendingFileName == null)
			{
				String errorString = "--> STORE LOCAL: Error on encrypting media file (on server side).";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> File to be encrypted: '" + pendingFilePath + "'";
				errorString += "\n--> Original file to be uploaded: '" + uploadFileNamePath + "'";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
				return false;
			}

			// Delete old pending file
			if (!encryptedPendingFileName.equals(pendingFilePath))
			{
				// Delete file
				FileUtilFunctions.fileDelete(pendingFilePath);

				// Logging
				context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> STORE LOCAL: Media file encrypted on server side: '" + encryptedPendingFileName + "'");
			}

			pendingFilePath = encryptedPendingFileName;
		}

		// Check if pending file exists and can be accessed (with Retry,
		// because it can take a longer time to copy large files).
		int nuOfAttempts = FileUtilFunctions.fileExistsRetry(pendingFilePath);

		if (nuOfAttempts <= 0)
		{
			String errorString = "--> STORE LOCAL: Error on checking 'pending' media file.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File searched for: '" + pendingFilePath + "'";
			errorString += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
			errorString += "\n--> Original file to be uploaded: '" + uploadFileNamePath + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnProcessingFile"), errorString, null);
			return false;
		}

		String logText = "\n--> STORE LOCAL: Checking 'pending' media file: '" + pendingFilePath + "'";
		if (nuOfAttempts > 1) logText += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

		/*
		 * Move the temporary media file from the pending directory to the
		 * regular media file directory
		 */
		String destinationFileName = mediaResourceContainer.mediaFileGetRealFileName(context, dataIdentifier, hashValue, fileType);

		// Copy only if the destination file doesn't exist yet.
		if (FileUtilFunctions.fileExists(destinationFileName) == false)
		{
			nuOfAttempts = FileUtilFunctions.fileCopyRetry(pendingFilePath, destinationFileName);

			if (nuOfAttempts <= 0)
			{
				String errorString = "--> STORE LOCAL: Error on copying a media file from 'pending' directory to its 'regular' directory.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Source file name: '" + pendingFilePath + "'";
				errorString += "\n--> Destination file name: '" + destinationFileName + "'";
				errorString += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
				errorString += "\n--> Original file to be uploaded: '" + uploadFileNamePath + "'";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
				return false;
			}

			logText = "\n--> STORE LOCAL: Media file copied from 'pending' directory to its 'regular' directory: '" + pendingFilePath + "' --> '" + destinationFileName + "'";
			if (nuOfAttempts > 1) logText += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);
		}

		// Change the 'modified date', in order to set the current file as
		// up-to-date.
		else
		{
			nuOfAttempts = FileUtilFunctions.fileSetLastModifiedRetry(destinationFileName, new Date());

			if (nuOfAttempts <= 0)
			{
				String errorString = "--> STORE LOCAL: Error on setting 'last modified' date to a media file.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Media file path: '" + destinationFileName + "'";
				errorString += "\n--> Original file to be uploaded: '" + uploadFileNamePath + "'";
				errorString += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
				return false;
			}

			logText = "\n--> STORE LOCAL: Media file set 'last modified' date: '" + destinationFileName + "'";
			if (nuOfAttempts > 1) logText += "\n--> Number of attempts: '" + String.valueOf(Math.abs(nuOfAttempts)) + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);
		}

		/*
		 * Delete pending media file
		 */
		if (FileUtilFunctions.fileDelete(pendingFilePath) == false)
		{
			String errorString = "--> STORE LOCAL: Error on deleting media file from 'pending' directory.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Media file path: '" + pendingFilePath + "'";
			errorString += "\n--> Original file to be uploaded: '" + uploadFileNamePath + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnStoringFileLocally"), errorString, null);
			return false;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> STORE LOCAL: Media file deleted from 'pending' directory: '" + pendingFilePath + "'");

		/*
		 * Return
		 */
		return true;
	}

	/**
	 * Executes the Client/Server COMMAND <TT>CommandMediaFileUpload</TT>.
	 * <p>
	 * Upload a media file from client to server.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param connectionContainer
	 *            The connection container to use for the connection.
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
	 * @return Returns <TT>true</TT> if the file could be uploaded on server,
	 *         otherwise <TT>false</TT>.
	 */
	protected boolean commandUploadToServer(Context context, ConnectionContainer connectionContainer, ResourceContainerMedia mediaResourceContainer, String uploadFileNamePath, String dataIdentifier)
	{
		/*
		 * Check variables and conditions
		 */

		// Check media resource container
		if (mediaResourceContainer == null)
		{
			String errorString = "--> UPLOAD FROM CLIENT TO SERVER: Media resource container not set (NULL value).";
			if (uploadFileNamePath != null) errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Check file path
		if (uploadFileNamePath == null || uploadFileNamePath.length() == 0)
		{
			String errorString = "--> UPLOAD FROM CLIENT TO SERVER: Missing file name of the file to be uploaded (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Check data identifier
		if (dataIdentifier == null || dataIdentifier.length() == 0)
		{
			String errorString = "--> UPLOAD FROM CLIENT TO SERVER: Missing data identifier of media (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (uploadFileNamePath != null) errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Check if file exists
		if (FileUtilFunctions.fileExists(uploadFileNamePath) == false)
		{
			String errorString = "--> UPLOAD FROM CLIENT TO SERVER: File to be uploaded doesn't exist or is not accessable.";
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
			String errorString = "--> UPLOAD FROM CLIENT TO SERVER: Missing file type.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
			errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
			return false;
		}

		// Checks if file type allowed for the given media resource item
		if (mediaResourceContainer.attributeIsFileTypeSupported(context, fileType) == false)
		{
			String errorString = "--> UPLOAD FROM CLIENT TO SERVER: File type '" + fileType + "' is not supported by the current media resource item.";
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
			String errorString = "--> UPLOAD FROM CLIENT TO SERVER: Maximum allowed media file size exceeded.";
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
			String errorString = "--> UPLOAD FROM CLIENT TO SERVER: Maximum allowed media file size exceeded.";
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
		 * Create file directory for pending files
		 */
		String pendingFilePathDirectory = mediaResourceContainer.mediaFileGetPendingFilePath(context);

		try
		{
			File directory = new File(pendingFilePathDirectory);
			directory.mkdirs();
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Media file directory 'pending' created: '" + pendingFilePathDirectory + "'");
		}
		catch (Exception e)
		{
			String errorString = "--> UPLOAD FROM CLIENT TO SERVER: Error on creating directory for pending media files.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Directory to be created: '" + pendingFilePathDirectory + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, e);
			return false;
		}

		/*
		 * Copy original file to the pending file directory
		 */
		try
		{
			String pendingFileName = FileLocationFunctions.compileFilePath(mediaResourceContainer.mediaFileGetPendingFilePath(context), mediaResourceContainer.mediaFileGetPendingFileName(context, fileType));

			if (FileUtilFunctions.fileCopy(uploadFileNamePath, pendingFileName) == false)
			{
				String errorString = "--> UPLOAD FROM CLIENT TO SERVER: Error on copying media file (to pending directory).";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Source file: '" + uploadFileNamePath + "'";
				errorString += "\n--> Destination file: '" + pendingFileName + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
				return false;
			}

			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> UPLOAD FROM CLIENT TO SERVER: Media file copied: '" + uploadFileNamePath + "' --> '" + pendingFileName + "'");

			/*
			 * Get hash value of the original file
			 */
			String hashValue = FileUtilFunctions.fileGetHashValue(pendingFileName);

			if (hashValue == null)
			{
				String errorString = "--> UPLOAD FROM CLIENT TO SERVER: Error on computing hash code of the media file.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> File to be hashed: '" + pendingFileName + "'";
				errorString += "\n--> Original file to be uploaded: '" + uploadFileNamePath + "'";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
				return false;
			}

			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> UPLOAD FROM CLIENT TO SERVERL: Media file hash value [" + hashValue + "] computed for: '" + pendingFileName + "'");

			/*
			 * COMMAND Media File Upload
			 */

			// Execute command
			ClientCommand command = new ClientCommandMediaFileUpload(context, context.getApplicationManager(), connectionContainer, pendingFileName, mediaResourceContainer.getRecourceIdentifier(), fileType, dataIdentifier, hashValue);
			ResponseContainer responseContainer = command.execute();

			if (responseContainer == null)
			{
				String errorString = "--> UPLOAD FROM CLIENT TO SERVER: Error on executing command 'ClientCommandMediaFileUpload' on server.";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
				errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
				return false;
			}

			if (responseContainer.isError())
			{
				String errorString = "--> UPLOAD FROM CLIENT TO SERVER: Error on executing command 'ClientCommandMediaFileUpload' on server.";
				errorString += "\n--> Application server replied an error code: '" + responseContainer.getErrorCode() + "'";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> File name of file to be uploaded: '" + uploadFileNamePath + "'";
				errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
				errorString += responseContainer.toString();
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
				return false;
			}

			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> UPLOAD FROM CLIENT TO SERVER: Media file uploaded on server: '" + pendingFileName + "'");

			/*
			 * Delete pending file
			 */
			FileUtilFunctions.fileDelete(pendingFileName);
		}
		catch (Exception e)
		{
			String errorString = "--> UPLOAD FROM CLIENT TO SERVER: Error on executing command.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, e);
			return false;
		}

		/*
		 * Return
		 */
		return true;
	}

	/**
	 * Executes the Client/Server COMMAND <TT>CommandMediaFileCheck</TT>.
	 * <p>
	 * Check if a media file already exists on server. Only the most recent
	 * media file is searched for on server, not any obsolete files.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param connectionContainer
	 *            The connection container to use for the connection.
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
	protected boolean commandCheckOnServer(Context context, ConnectionContainer connectionContainer, ResourceContainerMedia mediaResourceContainer, String fileType, String dataIdentifier, String hashValue)
	{
		/*
		 * Check variables and conditions
		 */

		// Check media resource container
		if (mediaResourceContainer == null)
		{
			String errorString = "--> CHECK ON SERVER: Media resource container not set (NULL value).";
			if (fileType != null) errorString += "\n--> File type of media: '" + fileType + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			if (hashValue != null) errorString += "\n--> Hash value of media: '" + hashValue + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return false;
		}

		// Check file type
		if (fileType == null || fileType.length() == 0)
		{
			String errorString = "--> CHECK ON SERVER: Missing file type of the file to be checked (NULL value or EMPTY).";
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
			String errorString = "--> CHECK ON SERVER: Missing data identifier of media (NULL value or EMPTY).";
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
			String errorString = "--> CHECK ON SERVER: Missing hash value of media (NULL value or EMPTY).";
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
			String errorString = "--> CHECK ON SERVER: File type '" + fileType + "' is not supported by the current media resource item.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (fileType != null) errorString += "\n--> File type of media: '" + fileType + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			if (hashValue != null) errorString += "\n--> Hash value of media: '" + hashValue + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return false;
		}

		/*
		 * COMMAND Media File Check
		 */

		// Execute command
		ClientCommandMediaFileCheck command = new ClientCommandMediaFileCheck(context, context.getApplicationManager(), connectionContainer, mediaResourceContainer.getRecourceIdentifier(), fileType, dataIdentifier, hashValue);
		ResponseContainer responseContainer = command.execute();

		if (responseContainer == null)
		{
			String errorString = "--> CHECK ON SERVER: Error on executing command 'ClientCommandMediaFileCheck' on server.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (fileType != null) errorString += "\n--> File type of media: '" + fileType + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			if (hashValue != null) errorString += "\n--> Hash value of media: '" + hashValue + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return false;
		}

		if (responseContainer.isError())
		{
			String errorString = "--> CHECK ON SERVER: Error on executing command 'ClientCommandMediaFileCheck' on server.";
			errorString += "\n--> Application server replied with error code: '" + responseContainer.getErrorCode() + "'";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (fileType != null) errorString += "\n--> File type of media: '" + fileType + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			if (hashValue != null) errorString += "\n--> Hash value of media: '" + hashValue + "'";
			errorString += responseContainer.toString();
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return false;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> CHECK ON SERVER: Result of checking media file on server: '" + command.isMediaFileExisting() + "'");

		/*
		 * Return
		 */
		if (command.isMediaFileExisting() == null || command.isMediaFileExisting() == false) return false;
		return true;
	}

	/**
	 * Executes the Client/Server COMMAND <TT>CommandMediaFileInfo</TT>.
	 * <p>
	 * Get information of a media file on server. Only the most recent
	 * media file is searched for on server, not any obsolete files.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param connectionContainer
	 *            The connection container to use for the connection.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param dataIdentifier
	 *            The identifier of the concrete media item to consider.
	 * 
	 * @return Returns the command container of the requested command if the media file exists, otherwise
	 *         <TT>null</TT>.
	 */
	protected ClientCommandMediaFileInfo commandInfoOnServer(Context context, ConnectionContainer connectionContainer, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		/*
		 * Check variables and conditions
		 */

		// Check media resource container
		if (mediaResourceContainer == null)
		{
			String errorString = "--> INFO ON SERVER: Media resource container not set (NULL value).";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return null;
		}

		// Check data identifier
		if (dataIdentifier == null || dataIdentifier.length() == 0)
		{
			String errorString = "--> INFO ON SERVER: Missing data identifier of media (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return null;
		}

		/*
		 * COMMAND Media File info
		 */

		// Execute command
		ClientCommandMediaFileInfo command = new ClientCommandMediaFileInfo(context, context.getApplicationManager(), connectionContainer, mediaResourceContainer.getRecourceIdentifier(), dataIdentifier);
		ResponseContainer responseContainer = command.execute();

		if (responseContainer == null)
		{
			String errorString = "--> INFO ON SERVER: Error on executing command 'ClientCommandMediaFileInfo' on server.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return null;
		}

		if (responseContainer.isError())
		{
			String errorString = "--> INFO ON SERVER: Error on executing command 'ClientCommandMediaFileInfo' on server.";
			errorString += "\n--> Application server replied with error code: '" + responseContainer.getErrorCode() + "'";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			errorString += responseContainer.toString();
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnCheckingFile"), errorString, null);
			return null;
		}

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> CHECK ON SERVER: Result of checking media file on server: '" + command.isExisting() + "'");

		/*
		 * Return
		 */
		if (command.isExisting() == null || command.isExisting() == false) return null;
		return command;
	}

	/**
	 * Executes the Client/Server COMMAND <TT>CommandMediaFileRead</TT>.
	 * <p>
	 * Read the most recent media file from a server.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param connectionContainer
	 *            The connection container to use for the connection.
	 * 
	 * @param mediaResourceContainer
	 *            The media resource container to consider.
	 * 
	 * @param dataIdentifier
	 *            The identifier of the concrete media item to use for the
	 *            destination file.
	 * 
	 * @return Returns the file path of the pending media file the read content
	 *         is stored to, or <TT>null</TT> if an error occurred.
	 *         <p>
	 *         <TT>Please notice:</TT> It's your concern to delete this pending
	 *         file after processing it. Please be careful, otherwise a bunch of
	 *         trash files could be left over in the pending directory.
	 */
	protected String commandReadOnServer(Context context, ConnectionContainer connectionContainer, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		/*
		 * Check variables and conditions
		 */

		// Check media resource container
		if (mediaResourceContainer == null)
		{
			String errorString = "--> READ FROM SERVER: Media resource container not set (NULL value).";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnReadingFileFromServer"), errorString, null);
			return null;
		}

		// Check data identifier
		if (dataIdentifier == null || dataIdentifier.length() == 0)
		{
			String errorString = "--> READ FROM SERVER: Missing data identifier of media (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnReadingFileFromServer"), errorString, null);
			return null;
		}

		/*
		 * COMMAND Media File Read
		 */

		// Execute command
		ClientCommandMediaFileRead command = new ClientCommandMediaFileRead(context, context.getApplicationManager(), connectionContainer, mediaResourceContainer.getRecourceIdentifier(), dataIdentifier);
		ResponseContainer responseContainer = command.execute();

		if (responseContainer == null)
		{
			String errorString = "--> READ FROM SERVER: Error on executing command 'ClientCommandMediaFileRead' on server.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnReadingFileFromServer"), errorString, null);
			return null;
		}

		if (responseContainer.isError())
		{
			String errorString = "--> READ FROM SERVER: Error on executing command 'ClientCommandMediaFileRead' on server.";
			errorString += "\n--> Application server replied with error code: '" + responseContainer.getErrorCode() + "'";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			errorString += responseContainer.toString();
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnReadingFileFromServer"), errorString, null);
			return null;
		}

		String logText = "\n--> READ FROM SERVER: Result values 'Media File exists?': '" + command.isMediaFileExisting() + "'";
		logText += "\n--> Media File exists on server: '" + command.isMediaFileExisting() + "'";
		logText += "\n--> Media File could be read: '" + command.isMediaFileRead() + "'";
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

		if (command.isMediaFileRead() == false) return null;

		/*
		 * Extract media content into a pending file
		 */

		// Get file type
		String fileType = command.getFileType();

		if (fileType == null || fileType.length() == 0)
		{
			String errorString = "--> READ FROM SERVER: Missing file type of media file read";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Command", "ErrorOnProcessingCommand"), errorString, null);
			return null;
		}

		/*
		 * Extract media file to pending file
		 */
		String pendingFileName = FileLocationFunctions.compileFilePath(mediaResourceContainer.mediaFileGetPendingFilePath(context), mediaResourceContainer.mediaFileGetPendingFileName(context, fileType));

		logText = "\n--> READ FROM SERVER: Pending file name created";
		logText += "\n--> Pending file name: '" + pendingFileName + "'";
		logText += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

		// Store file content into the pending file
		if (FileUtilFunctions.fileWriteFromString(pendingFileName, command.getMediaContent()) == false)
		{
			String errorString = "--> READ FROM SERVER: Error on writing media content into the pending file";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Pending file name: '" + pendingFileName + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Command", "ErrorOnProcessingCommand"), errorString, null);
			return null;
		}

		logText = "\n--> READ FROM SERVER: Media content stored in pending file";
		logText += "\n--> Pending file name: '" + pendingFileName + "'";
		logText += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
		logText += "\n--> Data identifier: '" + dataIdentifier + "'";
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

		/*
		 * Return
		 */
		return pendingFileName;
	}

	/**
	 * Get the current encoding value to be used for real encoding.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the server encoding value, or <TT>null</TT> if no value
	 *         is defined or available.
	 * 
	 */
	protected String getEncodingValue(Context context)
	{
		try
		{
			String serverEncodingValue = this.encodingKeyList.get(this.encodingKeyNumber);
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
	protected int getEncodingKeyNumber()
	{
		return this.encodingKeyNumber;
	}

	/**
	 * Getter
	 */
	protected Integer getMaximumMediaSize()
	{
		return maximumMediaSize;
	}

	/**
	 * Getter
	 */
	public String getMediaRootFilePath()
	{
		return mediaRootFilePath;
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
	protected int cleanPendingDirectory(Context context, ResourceContainerMedia mediaResourceContainer, int daysToKeep)
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
	protected int cleanDeletedDirectory(Context context, ResourceContainerMedia mediaResourceContainer, int daysToKeep)
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
	protected int cleanRegularDirectory(Context context, ResourceContainerMedia mediaResourceContainer, int daysToKeep)
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
	protected int cleanAllDirectories(Context context, ResourceContainerMedia mediaResourceContainer)
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
	protected int cleanAll(Context context)
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
	public boolean localWriteMediaContentToMediaFile(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier, String fileType, String mediaContent)
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
	protected boolean isMaximumFileSizeExceeded(Context context, String mediaFileName)
	{
		// Validate parameter
		if (mediaFileName == null || mediaFileName.length() == 0) return true;

		// No constraint set
		Integer maximumMediaSizeInKilobyte = this.getMaximumMediaSize();
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
	 * Getter
	 */
	protected int getCleanPendingDaysToKeep()
	{
		return this.cleanPendingDaysToKeep;
	}

	/**
	 * Getter
	 */
	protected int getCleanDeletedDaysToKeep()
	{
		return this.cleanDeletedDaysToKeep;
	}

	/**
	 * Getter
	 */
	protected int getCleanObsoleteDaysToKeep()
	{
		return this.cleanObsoleteDaysToKeep;
	}
}
