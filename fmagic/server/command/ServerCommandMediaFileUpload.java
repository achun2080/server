package fmagic.server.command;

import fmagic.basic.context.Context;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * COMMAND: Upload a media file from client to server.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 30.04.2013 - Created
 */
public class ServerCommandMediaFileUpload extends ServerCommand
{
	protected ResourceContainerMedia mediaResourceContainer;
	protected String fileType;
	protected String dataIdentifier;
	protected String hashValue;
	private String mediaContent = null;

	private Boolean isExisting = null;
	private Boolean isUploaded = null;

	/**
	 * Constructor 1
	 */
	public ServerCommandMediaFileUpload()
	{
		super();
	}

	/**
	 * Constructor 2
	 */
	public ServerCommandMediaFileUpload(Context context, String commandIdentifier)
	{
		super(context, commandIdentifier);
	}
	
	@Override
	public void setCommandIdentifier(Context context)
	{
		this.commandIdentifier = ResourceManager.command(context, "MediaFileUpload").getRecourceIdentifier();
	}

	@Override
	protected boolean validateRequestContainer()
	{
		try
		{
			String errorText = "--> Error on validating command parameter";
			boolean isError = false;

			// Get: Media Resource Identifier
			String mediaResourceIdentifier = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileUpload", "MediaResourceIdentifier").getAliasName(), null);

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
			this.fileType = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileUpload", "FileType").getAliasName(), null);

			if (this.fileType == null || this.fileType.length() == 0)
			{
				errorText += "\n--> Missing value 'FileType'";
				isError = true;
			}

			// Get: Data identifier
			this.dataIdentifier = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileUpload", "DataIdentifier").getAliasName(), null);

			if (this.dataIdentifier == null || this.dataIdentifier.length() == 0)
			{
				errorText += "\n--> Missing value 'DataIdentifier'";
				isError = true;
			}

			// Get: Hash value
			this.hashValue = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileUpload", "HashValue").getAliasName(), null);

			if (this.hashValue == null || this.hashValue.length() == 0)
			{
				errorText += "\n--> Missing value 'HashValue'";
				isError = true;
			}

			// Get: Media Content
			this.mediaContent = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileUpload", "MediaContent").getAliasName(), null);

			if (this.mediaContent == null || this.mediaContent.length() == 0)
			{
				errorText += "\n--> Missing value 'MediaContent'";
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
			// Check if file already exists
			this.isExisting = this.businessIsMediaFileExists(this.context, this.mediaResourceContainer, this.dataIdentifier, this.fileType, this.hashValue);
			
			// Get media content
			this.isUploaded = this.getContext().getMediaManager().localWriteMediaContentToMediaFile(this.getContext(), this.mediaResourceContainer, this.dataIdentifier, this.fileType, this.mediaContent);
		
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
			// Set result value: IsExisting
			String isExistingString = null;
			if (this.isExisting != null) isExistingString = this.isExisting.toString();
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileUpload", "IsExisting").getAliasName(), isExistingString);
			
			// Set result value: IsUploaded
			String isUploadedString = null;
			if (this.isUploaded != null) isUploadedString = this.isUploaded.toString();
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFile", "IsUploaded").getAliasName(), isUploadedString);
			
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
	private boolean businessIsMediaFileExists(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier, String fileType, String hashValue)
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
}
