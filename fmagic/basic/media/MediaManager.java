package fmagic.basic.media;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import fmagic.basic.application.ManagerInterface;
import fmagic.basic.command.ConnectionContainer;
import fmagic.basic.command.ResponseContainer;
import fmagic.basic.context.Context;
import fmagic.basic.file.FileLocationFunctions;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.command.ClientCommand;
import fmagic.client.command.ClientCommandMediaFileCheck;
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
	// Media UTIL instance
	private final MediaUtil mediaUtil;
	
	// Settings for Server Encoding
	protected final HashMap<Integer, String> encodingKeyList = new HashMap<Integer, String>();
	protected int encodingKeyNumber = 0;
	protected boolean encodingEnabled = false;

	// Common media file configuration properties
	protected String mediaRootFilePath = null;
	private Integer maximumMediaSize = null;

	/**
	 * Constructor
	 */
	public MediaManager()
	{
		this.mediaUtil = new MediaUtil(this);
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
		this.mediaUtil.cleanAll(context);

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
			this.mediaUtil.cleanPendingDaysToKeep = context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "Media", "CleanPendingDaysToKeep"), false);

			// Read parameter: CleanDeletedDaysToKeep
			this.mediaUtil.cleanDeletedDaysToKeep = context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "Media", "CleanDeletedDaysToKeep"), false);

			// Read parameter: CleanObsoleteDaysToKeep
			this.mediaUtil.cleanObsoleteDaysToKeep = context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "Media", "CleanObsoleteDaysToKeep"), false);

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
		return this.mediaUtil.validateResources(context);
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
	public String mediaFileGetRootFilePath(Context context)
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
	public String localEncryptMediaFile(Context context, ResourceContainerMedia mediaResourceContainer, String sourceFilePath, String destinationFilePath)
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
	public String localDecryptMediaFile(Context context, ResourceContainerMedia mediaResourceContainer, String sourceFilePath)
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
	public boolean localStoreMediaFile(Context context, ResourceContainerMedia mediaResourceContainer, String uploadFileNamePath, String dataIdentifier)
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
		if (this.mediaUtil.isMaximumFileSizeExceeded(context, uploadFileNamePath) == true)
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
	public boolean commandUploadToServer(Context context, ConnectionContainer connectionContainer, ResourceContainerMedia mediaResourceContainer, String uploadFileNamePath, String dataIdentifier)
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
		if (this.mediaUtil.isMaximumFileSizeExceeded(context, uploadFileNamePath) == true)
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
	public boolean commandCheckOnServer(Context context, ConnectionContainer connectionContainer, ResourceContainerMedia mediaResourceContainer, String fileType, String dataIdentifier, String hashValue)
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
		ClientCommandMediaFileCheck command = new ClientCommandMediaFileCheck(context, context.getApplicationManager(), connectionContainer, mediaResourceContainer.getRecourceIdentifier(), fileType, dataIdentifier, hashValue);
		ResponseContainer responseContainer = command.execute();

		if (responseContainer == null)
		{
			String errorString = "--> CHECK ON SERVER: Error on executing command 'ClientCommandMediaFileCheck' on server.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (fileType != null) errorString += "\n--> File type of media: '" + fileType + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			if (hashValue != null) errorString += "\n--> Hash value of media: '" + hashValue + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
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
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnUploadingFile"), errorString, null);
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
	 * @return Returns <TT>true</TT> if the media file could be read, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean commandReadOnServer(Context context, ConnectionContainer connectionContainer, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
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
			return false;
		}

		// Check data identifier
		if (dataIdentifier == null || dataIdentifier.length() == 0)
		{
			String errorString = "--> READ FROM SERVER: Missing data identifier of media (NULL value or EMPTY).";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnReadingFileFromServer"), errorString, null);
			return false;
		}

		/*
		 * COMMAND Media File Read
		 */
		ClientCommandMediaFileRead command = new ClientCommandMediaFileRead(context, context.getApplicationManager(), connectionContainer, mediaResourceContainer.getRecourceIdentifier(), dataIdentifier);
		ResponseContainer responseContainer = command.execute();

		if (responseContainer == null)
		{
			String errorString = "--> READ FROM SERVER: Error on executing command 'ClientCommandMediaFileRead' on server.";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnReadingFileFromServer"), errorString, null);
			return false;
		}

		if (responseContainer.isError())
		{
			String errorString = "--> READ FROM SERVER: Error on executing command 'ClientCommandMediaFileRead' on server.";
			errorString += "\n--> Application server replied with error code: '" + responseContainer.getErrorCode() + "'";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			if (dataIdentifier != null) errorString += "\n--> Data identifier of media: '" + dataIdentifier + "'";
			errorString += responseContainer.toString();
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Media", "ErrorOnReadingFileFromServer"), errorString, null);
			return false;
		}

		String logText = "\n--> READ FROM SERVER: Result values 'Media File exists?': '" + command.isMediaFileExisting() + "'";
		logText += "\n--> Media File exists on server: '" + command.isMediaFileExisting() + "'";
		logText += "\n--> Media File could be read: '" + command.isMediaFileRead() + "'";
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

		if (command.isMediaFileRead() == false) return false;

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
			return false;
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
			return false;
		}

		logText = "\n--> READ FROM SERVER: Media content stored in pending file";
		logText += "\n--> Pending file name: '" + pendingFileName + "'";
		logText += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
		logText += "\n--> Data identifier: '" + dataIdentifier + "'";
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

		/*
		 * Store media file in local media repository
		 */
		if (this.localStoreMediaFile(context, mediaResourceContainer, pendingFileName, dataIdentifier) == false)
		{
			String errorString = "--> READ FROM SERVER: Error on storing media content in local media repository";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Pending file name: '" + pendingFileName + "'";
			errorString += "\n--> Data identifier: '" + dataIdentifier + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Command", "ErrorOnProcessingCommand"), errorString, null);
			return false;
		}

		logText = "\n--> READ FROM SERVER: Media content stored in local media repository";
		logText += "\n--> Pending file name: '" + pendingFileName + "'";
		logText += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
		logText += "\n--> Data identifier: '" + dataIdentifier + "'";
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

		/*
		 * Delete pending file
		 */
		if (FileUtilFunctions.fileDelete(pendingFileName) == false)
		{
			String errorString = "--> READ FROM SERVER: Error on deleting pending file";
			errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
			errorString += "\n--> Pending file name: '" + pendingFileName + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Command", "ErrorOnProcessingCommand"), errorString, null);

			// Nevertheless return with 'true' because the media file could be
			// stored
			return true;
		}

		/*
		 * Return
		 */
		return true;
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
	public int getEncodingKeyNumber()
	{
		return this.encodingKeyNumber;
	}

	/**
	 * Getter
	 */
	public Integer getMaximumMediaSize()
	{
		return maximumMediaSize;
	}

	/**
	 * Getter
	 */
	public MediaUtil getMediaUtil()
	{
		return mediaUtil;
	}

	/**
	 * Getter
	 */
	public String getMediaRootFilePath()
	{
		return mediaRootFilePath;
	}

	/**
	 * TEST Setter
	 * 
	 * Please notice: This setter is supposed test purposes only. It doesn't
	 * work if it runs in productive environment.
	 */
	public void testSetMaximumMediaSize(Context context, int maximumMediaSize)
	{
		if (!context.isRunningInTestMode()) return;

		this.maximumMediaSize = maximumMediaSize;
	}
}
