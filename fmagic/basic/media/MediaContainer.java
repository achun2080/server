package fmagic.basic.media;

import fmagic.basic.context.Context;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.notification.NotificationManager;

/**
 * This class contains all data needed to manage and show media content.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 05.04.2013 - Created
 * 
 */
public class MediaContainer
{
	// Common data
	final private Context context;
	final private ResourceContainerMedia resourceContainerMedia;
	final private String dataIdentifier;

	// File data
	private String originalMediaFilePath = null;
	private String workingMediaFilePath = null;

	// Workflow
	private boolean boundMark = false;

	/**
	 * Constructor
	 * 
	 * @param resourceContainerMedia
	 *            The media resource container the media is based on.
	 */
	public MediaContainer(Context context,
			ResourceContainerMedia resourceContainerMedia, String dataIdentifier)
	{
		this.context = context;
		this.resourceContainerMedia = resourceContainerMedia;
		this.dataIdentifier = dataIdentifier;

		if (this.resourceContainerMedia != null && this.dataIdentifier != null)
		{
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> MEDIA CONTAINER: Instantiated for media container: '" + this.resourceContainerMedia.getRecourceIdentifier() + "' and data identifier '" + this.dataIdentifier + "'");
		}
	}

	/**
	 * Bind a media file to the media container. That means, all resources were
	 * checked and prepared. Media that are encrypted will be decrypted. But no
	 * media data will be read into the memory yet.
	 * <p>
	 * Please notice: There is a workflow to consider regarding the media
	 * container. First you have to bind a media, using <TT>bindMedia()</TT>,
	 * then you can read media content as a byte buffer
	 * <TT>readMediaContentAsByteBuffer()</TT> or as a string
	 * <TT>readMediaContentAsString()</TT>, then you should release all
	 * resources, using <TT>releaseMedia()</TT>.
	 * 
	 * @return Returns <TT>true</TT> if the media could be bound, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean bindMedia()
	{
		// Check if media is already bound
		if (this.boundMark == true) this.releaseMedia();

		// Get file path of the most recent original media file
		this.originalMediaFilePath = this.resourceContainerMedia.mediaFileGetRealFileName(this.context, this.dataIdentifier);
		if (this.originalMediaFilePath == null || this.originalMediaFilePath.length() == 0) return false;

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> MEDIA CONTAINER: Media was bound to original media file path: '" + this.originalMediaFilePath + "'");

		// Copy or decrypt media file to a pending (temporary) file
		this.workingMediaFilePath = this.context.getMediaManager().operationDecrypt(this.context, this.resourceContainerMedia, this.originalMediaFilePath);
		if (this.workingMediaFilePath == null || this.workingMediaFilePath.length() == 0) return false;

		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> MEDIA CONTAINER: Bounded media was copied/decrypted the the 'pending' directory: '" + this.workingMediaFilePath + "'");

		// Set bind mark
		this.boundMark = true;

		// Return
		return true;
	}

	/**
	 * Read media data and returns them as a buffer of bytes.
	 * <p>
	 * Please notice: There is a workflow to consider regarding the media
	 * container. First you have to bind a media, using <TT>bindMedia()</TT>,
	 * then you can read media content as a byte buffer
	 * <TT>readMediaContentAsByteBuffer()</TT> or as a string
	 * <TT>readMediaContentAsString()</TT>, then you should release all
	 * resources, using <TT>releaseMedia()</TT>.
	 * 
	 * @return Returns the content of the media file, or <TT>null</TT> if an
	 *         error occurred.
	 */
	public byte[] readMediaContentAsByteArray()
	{
		// Check if media is already bound
		if (this.boundMark == false) return null;

		// Check if working file is available
		if (this.workingMediaFilePath == null || this.workingMediaFilePath.length() == 0) return null;

		// Read working file
		return FileUtilFunctions.fileReadToByteArray(this.workingMediaFilePath);
	}

	/**
	 * Read media data and returns them as an UTF8 string, suitable to be used
	 * for server/client communication.
	 * <p>
	 * Please notice: There is a workflow to consider regarding the media
	 * container. First you have to bind a media, using <TT>bindMedia()</TT>,
	 * then you can read media content as a byte buffer
	 * <TT>readMediaContentAsByteBuffer()</TT> or as a string
	 * <TT>readMediaContentAsString()</TT>, then you should release all
	 * resources, using <TT>releaseMedia()</TT>.
	 * 
	 * @return Returns the content of the media file, or <TT>null</TT> if an
	 *         error occurred.
	 */
	public String readMediaContentAsString()
	{
		// Check if media is already bound
		if (this.boundMark == false) return null;

		// Check if working file is available
		if (this.workingMediaFilePath == null || this.workingMediaFilePath.length() == 0) return null;

		// Read working file
		return FileUtilFunctions.fileReadToString(this.workingMediaFilePath);
	}

	/**
	 * Release a media file of the media container. That means, all resources
	 * were deallocated.
	 * <p>
	 * Please notice: There is a workflow to consider regarding the media
	 * container. First you have to bind a media, using <TT>bindMedia()</TT>,
	 * then you can read media content as a byte buffer
	 * <TT>readMediaContentAsByteBuffer()</TT> or as a string
	 * <TT>readMediaContentAsString()</TT>, then you should release all
	 * resources, using <TT>releaseMedia()</TT>.
	 * 
	 * @return Returns <TT>true</TT> if the media could be released, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean releaseMedia()
	{
		// Check if media is already bound
		if (this.boundMark == false) return true;

		// Set bind mark
		this.boundMark = false;

		// Delete pending working file
		if (this.workingMediaFilePath != null && this.workingMediaFilePath.length() > 0)
		{
			FileUtilFunctions.fileDelete(this.workingMediaFilePath);
		}
		
		if (this.resourceContainerMedia != null && this.dataIdentifier != null)
		{
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "\n--> MEDIA CONTAINER: Media container released: '" + this.resourceContainerMedia.getRecourceIdentifier() + "', data identifier '" + this.dataIdentifier + "'");
		}

		// / Clear variables
		this.originalMediaFilePath = null;
		this.workingMediaFilePath = null;

		// Return
		return true;
	}

	/**
	 * To string
	 */
	@Override
	public String toString()
	{
		String outputString = "";

		// Headline
		outputString += "\n[\n+++ Media Container" + "\n";

		// Documentation
		if (resourceContainerMedia != null)
		{
			outputString += resourceContainerMedia.printManual(this.context) + "\n";
		}

		// Settings
		if (this.boundMark == true)
		{
			outputString += "\nBounded: '" + "TRUE" + "'";
		}
		else
		{
			outputString += "\nBounded: '" + "FALSE" + "'";
		}

		outputString += "\nData identifier: '" + dataIdentifier + "'";
		outputString += "\nOriginal media file: '" + originalMediaFilePath + "'";
		outputString += "\nWorking media file: '" + workingMediaFilePath + "'";

		// End of output string
		outputString += "\n]\n";

		// Return
		return outputString;
	}

	/**
	 * Getter
	 */
	public String getOriginalMediaFilePath()
	{
		return originalMediaFilePath;
	}

	/**
	 * Getter
	 */
	public String getWorkingMediaFilePath()
	{
		return workingMediaFilePath;
	}
}
