package fmagic.server.command;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import fmagic.basic.context.Context;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * COMMAND: Get information about a media file on a remote server.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 22.05.2013 - Created
 */
public class ServerCommandMediaFileInfo extends ServerCommand
{
	private ResourceContainerMedia mediaResourceContainer;
	private String dataIdentifier;

	private String fileType = null;
	private String hashValue = null;
	private String lastModifiedDate = null;
	private Long fileSize = null;
	private Boolean isEncoded = null;
	private Boolean isExisting = null;

	/**
	 * Constructor 1
	 */
	public ServerCommandMediaFileInfo()
	{
		super();
	}

	/**
	 * Constructor 2
	 */
	public ServerCommandMediaFileInfo(Context context, String commandIdentifier)
	{
		super(context, commandIdentifier);
	}

	@Override
	public void setCommandIdentifier(Context context)
	{
		this.commandIdentifier = ResourceManager.command(context, "MediaFileInfo").getRecourceIdentifier();
	}

	@Override
	protected boolean validateRequestContainer()
	{
		try
		{
			String errorText = "--> Error on validating command parameter";
			boolean isError = false;

			// Get: Media Resource Identifier
			String mediaResourceIdentifier = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "MediaResourceIdentifier").getAliasName(), null);

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
			this.dataIdentifier = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "DataIdentifier").getAliasName(), null);

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
			}
			else
			{
				this.isExisting = true;
			}

			// Get file type of media file
			this.fileType = this.businessGetMediaFileType(this.getContext(), this.mediaResourceContainer, this.dataIdentifier);

			// Get hash value of media file
			this.hashValue = this.businessGetMediaHashValue(this.getContext(), this.mediaResourceContainer, this.dataIdentifier);

			// Get last modified date of media file
			Date currentDate = this.businessGetMediaLastModifiedDate(this.getContext(), this.mediaResourceContainer, this.dataIdentifier);

			if (currentDate != null)
			{
				DateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
				this.lastModifiedDate = dateFormatter.format(currentDate);
			}

			// Get file size of media file
			this.fileSize = this.businessGetMediaFileSize(this.getContext(), this.mediaResourceContainer, this.dataIdentifier);
			
			// Check if media file is encoded
			if (this.businessIsMediaFileEncoded(this.getContext(), this.mediaResourceContainer, this.dataIdentifier) == false)
			{
				this.isEncoded = false;
			}
			else
			{
				this.isEncoded = true;
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
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "FileType").getAliasName(), this.fileType);
			
			// Set parameter: HashValue
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "HashValue").getAliasName(), this.hashValue);
			
			// Set parameter: LastModifiedDate
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "LastModifiedDate").getAliasName(), this.lastModifiedDate);
			
			// Set parameter: FileSize
			String fileSizeString = "";
			if (this.fileSize != null) fileSizeString = String.valueOf(this.fileSize);
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "FileSize").getAliasName(), fileSizeString);

			// Set parameter: IsEncoded
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "IsEncoded").getAliasName(), this.isEncoded.toString());

			// Set parameter: IsExisting
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "IsExisting").getAliasName(), this.isExisting.toString());

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

	/**
	 * Get the hash value of a media file.
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
	 * @return Returns the hash value as string, or <TT>null</TT> if the media
	 *         file doesn't exist.
	 * 
	 */
	private String businessGetMediaHashValue(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		try
		{
			// Get the current file name of the most recent media file
			String currentFileName = mediaResourceContainer.mediaFileGetRealFileName(context, dataIdentifier);

			if (currentFileName == null || currentFileName.length() == 0) { return null; }

			// Extract the hash value
			String hashValue = mediaResourceContainer.mediaFileGetFileNamePartHashValue(context, currentFileName);

			if (hashValue == null || hashValue.length() == 0) { return null; }

			// Return
			return hashValue;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Get the last modified date of a media file.
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
	 * @return Returns the last modified date, or <TT>null</TT> if the media
	 *         file doesn't exist.
	 * 
	 */
	private Date businessGetMediaLastModifiedDate(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		try
		{
			// Get the current file name of the most recent media file
			String currentFileName = mediaResourceContainer.mediaFileGetRealFileName(context, dataIdentifier);

			if (currentFileName == null || currentFileName.length() == 0) { return null; }

			// Extract the last modified date
			Date lastModifiedDate = FileUtilFunctions.fileGetLastModifiedDate(currentFileName);

			// Return
			return lastModifiedDate;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Get the file size of a media file.
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
	 * @return Returns the file size, or <TT>null</TT> if the media
	 *         file doesn't exist.
	 * 
	 */
	private Long businessGetMediaFileSize(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		try
		{
			// Get the current file name of the most recent media file
			String currentFileName = mediaResourceContainer.mediaFileGetRealFileName(context, dataIdentifier);

			if (currentFileName == null || currentFileName.length() == 0) { return null; }

			// Get the file size
			Long fileSize = FileUtilFunctions.fileGetFileSize(currentFileName);

			// Return
			return fileSize;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Check if media file is encoded on server.
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
	 * @return Returns <TT>true</TT> or <TT>false</TT>, or <TT>null</TT> if the media
	 *         file doesn't exist.
	 * 
	 */
	private Boolean businessIsMediaFileEncoded(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		try
		{
			// Get the current file name of the most recent media file
			String currentFileName = mediaResourceContainer.mediaFileGetRealFileName(context, dataIdentifier);

			if (currentFileName == null || currentFileName.length() == 0) { return null; }

			// Extract the encoding mark from file name
			int encodingKey = mediaResourceContainer.mediaFileGetEncodingKeyOfRealFileName(context, currentFileName);

			// Return
			return (encodingKey > 0);
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
