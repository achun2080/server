package fmagic.server.command;

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
	 * Constructor
	 */
	public ServerCommandMediaFileRead()
	{
		super();
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
				this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), errorText, null);
				this.setErrorMessageTechnicalError(this.context, this.responseContainer, errorText);
				return false;
			}
		}
		catch (Exception e)
		{
			this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), null, e);
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
			/*
			 * Organizational stuff
			 */
			this.responseContainer.clearErrorCode();
			this.responseContainer.setCommandIdentifier(this.requestContainer.getCommandIdentifier());

			/*
			 * Get recent media file name, if existing
			 */
			String currentFileName = this.mediaResourceContainer.getMediaRealFileName(this.getContext(), this.dataIdentifier);

			if (currentFileName == null)
			{
				this.isExisting = false;
				this.isRead = false;
				return true;
			}

			// File exists on server
			this.isExisting = true;

			/*
			 * Get file type of media file
			 */
			this.fileType = FileUtilFunctions.fileGetFileTypePart(currentFileName);

			if (this.fileType == null || this.fileType.length() == 0)
			{
				String errorText = "\n--> Media file has no file type";
				errorText += "\n--> Media resource identifier: '" + this.mediaResourceContainer.getRecourceIdentifier() + "'";
				errorText += "\n--> Data identifier: '" + this.dataIdentifier + "'";
				this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), errorText, null);
				this.setErrorMessageTechnicalError(this.context, this.responseContainer, errorText);
				
				this.isRead = false;
				
				return false;
			}

			/*
			 * Read file content from media file
			 */
			MediaContainer mediaContainer = new MediaContainer(this.getContext(), this.mediaResourceContainer, this.dataIdentifier);

			if (mediaContainer.bindMedia() == false)
			{
				String errorText = "\n--> Error on binding media file";
				errorText += "\n--> Media resource identifier: '" + this.mediaResourceContainer.getRecourceIdentifier() + "'";
				errorText += "\n--> Data identifier: '" + this.dataIdentifier + "'";
				this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), errorText, null);
				this.setErrorMessageTechnicalError(this.context, this.responseContainer, errorText);
				return false;
			}

			// Read file content
			this.mediaContent = mediaContainer.readMediaContentAsString();

			if (this.mediaContent == null)
			{
				String errorText = "\n--> Error on reading media file content";
				errorText += "\n--> Media resource identifier: '" + this.mediaResourceContainer.getRecourceIdentifier() + "'";
				errorText += "\n--> Data identifier: '" + this.dataIdentifier + "'";
				this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), errorText, null);
				this.setErrorMessageTechnicalError(this.context, this.responseContainer, errorText);
				return false;
			}

			// Release media file

			if (mediaContainer.releaseMedia() == false)
			{
				String errorText = "\n--> Error on releasing media file";
				errorText += "\n--> Media resource identifier: '" + this.mediaResourceContainer.getRecourceIdentifier() + "'";
				errorText += "\n--> Data identifier: '" + this.dataIdentifier + "'";
				this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), errorText, null);
				this.setErrorMessageTechnicalError(this.context, this.responseContainer, errorText);
				return false;
			}

			// File could be read
			this.isRead = true;

			// Return
			return true;
		}
		catch (Exception e)
		{
			this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), null, e);
			return false;
		}
	}

	@Override
	protected boolean evaluateResults()
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
			this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), null, e);
			return false;
		}
	}
}
