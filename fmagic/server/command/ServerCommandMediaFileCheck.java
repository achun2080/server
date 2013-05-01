package fmagic.server.command;

import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * COMMAND: Check if a media file already exists on a remote server.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 30.04.2013 - Created
 */
public class ServerCommandMediaFileCheck extends ServerCommand
{
	protected ResourceContainerMedia mediaResourceContainer;
	protected String fileType;
	protected String dataIdentifier;
	protected String hashValue;

	private Boolean isExisting = null;

	/**
	 * Constructor
	 */
	public ServerCommandMediaFileCheck()
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
			String mediaResourceIdentifier = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileCheck", "MediaResourceIdentifier").getAliasName(), null);

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

			// Get: File type
			this.fileType = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileCheck", "FileType").getAliasName(), null);

			if (this.fileType == null || this.fileType.length() == 0)
			{
				errorText += "\n--> Missing value 'FileType'";
				isError = true;
			}

			// Get: Data identifier
			this.dataIdentifier = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileCheck", "DataIdentifier").getAliasName(), null);

			if (this.dataIdentifier == null || this.dataIdentifier.length() == 0)
			{
				errorText += "\n--> Missing value 'DataIdentifier'";
				isError = true;
			}

			// Get: File type
			this.hashValue = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileCheck", "HashValue").getAliasName(), null);

			if (this.hashValue == null || this.hashValue.length() == 0)
			{
				errorText += "\n--> Missing value 'HashValue'";
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
			// Organizational stuff
			this.responseContainer.clearErrorCode();
			this.responseContainer.setCommandIdentifier(this.requestContainer.getCommandIdentifier());

			// Get all values from server
			String currentFileName = mediaResourceContainer.getMediaRealFileName(this.getContext(), this.dataIdentifier);
			
			if (currentFileName == null || currentFileName.length() == 0)
			{
				this.isExisting = false;
				return true;
			}
			
			// Compare file type
			String currentFileType = FileUtilFunctions.fileGetFileTypePart(currentFileName);
			
			if (currentFileType == null || !currentFileType.equals(this.fileType))
			{
				this.isExisting = false;
				return true;
			}
			
			// Compare hash value
			String currentHashValue = mediaResourceContainer.getMediaPartHashValue(this.getContext(), currentFileName);
			
			if (currentHashValue == null || !currentHashValue.equals(this.hashValue))
			{
				this.isExisting = false;
				return true;
			}
			
			// File already exists
			this.isExisting = true;
		
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
			// Set parameter: MediaResourceIdentifier
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileCheck", "IsExisting").getAliasName(), this.isExisting.toString());
			
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
