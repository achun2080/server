package fmagic.server.command;

import fmagic.basic.context.Context;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.media.MediaContainer;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * COMMAND: Read a media file from a remote server.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 01.05.2013 - Created
 */
public class ServerCommandMediaFileRead extends ServerCommand
{
	private ResourceContainerMedia mediaResourceContainer;
	private String dataIdentifier;

	private String fileType = null;
	private Boolean isExisting = null;
	private Boolean isRead = null;
	private String mediaContent = null;

	/**
	 * Constructor 1
	 */
	public ServerCommandMediaFileRead()
	{
		super();
	}

	/**
	 * Constructor 2
	 */
	public ServerCommandMediaFileRead(Context context, String commandIdentifier)
	{
		super(context, commandIdentifier);
	}
	
	@Override
	public void setCommandIdentifier(Context context)
	{
		this.commandIdentifier = ResourceManager.command(context, "MediaFileRead").getRecourceIdentifier();
	}

	@Override
	protected boolean validateRequestContainer()
	{
		try
		{
			String errorText = "--> Error on validating command parameter";
			boolean isError = false;

			// Get: Media Resource Identifier
			String mediaResourceIdentifier = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileRead", "MediaResourceIdentifier").getAliasName(), null);

			if (mediaResourceIdentifier == null || mediaResourceIdentifier.length() == 0)
			{
				errorText += "\n--> Missing value 'MediaResourceIdentifier'";
				isError = true;
			}

			ResourceContainer resourceContainer = this.getContext().getResourceManager().getResourceContainer(this.getContext(), mediaResourceIdentifier);

			if (resourceContainer == null)
			{
				errorText += "\n--> Error on creating media resource container, on using resource identifier: '" + mediaResourceIdentifier + "'";
				isError = true;
			}

			this.mediaResourceContainer = ResourceManager.media(this.getContext(), resourceContainer.getGroup(), resourceContainer.getName());

			if (this.mediaResourceContainer == null)
			{
				errorText += "\n--> Error on creating media resource container, on using resource identifier: '" + mediaResourceIdentifier + "'";
				isError = true;
			}

			// Get: Data identifier
			this.dataIdentifier = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileRead", "DataIdentifier").getAliasName(), null);

			if (this.dataIdentifier == null || this.dataIdentifier.length() == 0)
			{
				errorText += "\n--> Missing value 'DataIdentifier'";
				isError = true;
			}

			// Fire error message
			if (isError == true)
			{
				this.notifyError("Command", "IntegrityError", errorText, null);
				return false;
			}
		}
		catch (Exception e)
		{
			this.notifyError("Command", "ErrorOnProcessingCommand", null, e);
			return false;
		}

		// Return
		return true;
	}

	@Override
	protected boolean processOnServer()
	{
		try
		{
			// Check if media file exists
			if (this.businessIsMediaFileExists(this.getContext(), this.mediaResourceContainer, this.dataIdentifier) == false)
			{
				this.isExisting = false;
				this.isRead = false;
				return true;
			}
			else
			{
				this.isExisting = true;
			}

			// Get file type of media file
			this.fileType = this.businessGetMediaFileType(this.getContext(), this.mediaResourceContainer, this. dataIdentifier);

			if (this.fileType == null || this.fileType.length() == 0)
			{
				this.isRead = false;
				return true;
			}

			// Read content from media file
			this.mediaContent = this.businessReadMediaContentFromMediaFile(this.context, this.mediaResourceContainer, this.dataIdentifier);

			if (this.mediaContent == null)
			{
				this.isRead = false;
				return true;
			}
			else
			{
				this.isRead = true;
			}

			// Return
			return true;
		}
		catch (Exception e)
		{
			this.notifyError("Command", "ErrorOnProcessingCommand", null, e);
			return false;
		}
	}

	@Override
	protected boolean arrangeResults()
	{
		try
		{
			// Set parameter: FileType
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileRead", "FileType").getAliasName(), this.fileType);

			// Set parameter: IsExisting
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileRead", "IsExisting").getAliasName(), this.isExisting.toString());

			// Set parameter: IsRead
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileRead", "IsRead").getAliasName(), this.isRead.toString());

			// Set parameter: MediaContent
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileRead", "MediaContent").getAliasName(), this.mediaContent);

			// Return
			return true;
		}
		catch (Exception e)
		{
			this.notifyError("Command", "ErrorOnProcessingCommand", null, e);
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
	private String businessReadMediaContentFromMediaFile(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		try
		{
			// Create media container
			MediaContainer mediaContainer = new MediaContainer(context, mediaResourceContainer, dataIdentifier);

			if (mediaContainer.bindMedia() == false)
			{
				String errorString = "--> COMMAND MEDIA FILE READ: Error on binding media container";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Data identifier: '" + dataIdentifier + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Command", "ErrorOnProcessingCommand"), errorString, null);
				return null;
			}

			// Read file content
			String mediaContent = mediaContainer.readMediaContentAsString();

			if (mediaContent == null)
			{
				String errorString = "--> COMMAND MEDIA FILE READ: Error on reading media content";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Data identifier: '" + dataIdentifier + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Command", "ErrorOnProcessingCommand"), errorString, null);
				return null;
			}

			// Release media file
			if (mediaContainer.releaseMedia() == false)
			{
				String errorString = "--> COMMAND MEDIA FILE READ: Error on releasing media container";
				errorString += "\n--> Media resource identifier: '" + mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Data identifier: '" + dataIdentifier + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Command", "ErrorOnProcessingCommand"), errorString, null);
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
	private String businessGetMediaFileType(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
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
	private boolean businessIsMediaFileExists(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
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
}
