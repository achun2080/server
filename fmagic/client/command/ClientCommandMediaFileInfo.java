package fmagic.client.command;

import java.text.SimpleDateFormat;
import java.util.Date;

import fmagic.basic.application.ApplicationManager;
import fmagic.basic.command.ConnectionContainer;
import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * COMMAND: Get information about a media file on a remote server.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 22.05.2013 - Created
 */
public class ClientCommandMediaFileInfo extends ClientCommand
{
	private final String mediaResourceIdentifier;
	private final String dataIdentifier;

	private String fileType = null;
	private String hashValue = null;
	private String lastModifiedDate = null;
	private Long fileSize = null;
	private Boolean isEncoded = null;
	private Boolean isExisting = null;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Current context.
	 * 
	 * @param application
	 *            Application client.
	 */
	public ClientCommandMediaFileInfo(Context context,
			ApplicationManager application,
			ConnectionContainer connectionContainer,
			String mediaResourceIdentifier, String dataIdentifier)
	{
		super(context, application, ResourceManager.command(context, "MediaFileInfo").getRecourceIdentifier(), connectionContainer, context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "CommandMediaFileInfo", "SocketTimeoutInMilliseconds"), false));

		this.mediaResourceIdentifier = mediaResourceIdentifier;
		this.dataIdentifier = dataIdentifier;
	}

	@Override
	protected boolean prepareRequestContainer()
	{
		try
		{
			// Set parameter: MediaResourceIdentifier
			this.requestContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "MediaResourceIdentifier").getAliasName(), this.mediaResourceIdentifier);

			// Set parameter: DataIdentifier
			this.requestContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "DataIdentifier").getAliasName(), this.dataIdentifier);
		}
		catch (Exception e)
		{
			ResourceContainer errorCode = ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand");
			this.context.getNotificationManager().notifyError(this.context, errorCode, null, e);
			this.responseContainer.setErrorCode(errorCode.getRecourceIdentifier());
			return false;
		}

		// Return
		return true;
	}

	@Override
	protected boolean evaluateResults()
	{
		try
		{
			String errorText = "--> Error on validating command result values";
			boolean isError = false;

			// Get result: fileType
			this.fileType = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "FileType").getAliasName(), null);

			if (this.fileType == null || this.fileType.length() == 0)
			{
				errorText += "\n--> Missing value 'FileType'";
				isError = true;
			}

			// Get result: HashValue
			this.hashValue = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "HashValue").getAliasName(), null);

			if (this.hashValue == null || this.hashValue.length() == 0)
			{
				errorText += "\n--> Missing value 'HashValue'";
				isError = true;
			}

			// Get result: LastModifiedDate
			this.lastModifiedDate = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "LastModifiedDate").getAliasName(), null);

			if (this.lastModifiedDate == null || this.lastModifiedDate.length() != 14)
			{
				errorText += "\n--> Missing or incorrect value 'LastModifiedDate'";
				isError = true;
			}

			// Get result: FileSize
			String fileSizeString = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "FileSize").getAliasName(), null);

			try
			{
				this.fileSize = Long.valueOf(fileSizeString);
			}
			catch (Exception e)
			{
				this.fileSize = null;
			}

			if (this.fileSize == null)
			{
				errorText += "\n--> Missing value 'FileSize'";
				isError = true;
			}

			// Get result: IsExisting
			String result = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "IsExisting").getAliasName(), "false");
			if (result != null && result.equalsIgnoreCase("true")) this.isExisting = true;
			if (result != null && result.equalsIgnoreCase("false")) this.isExisting = false;

			// Get result: IsEncoded
			result = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileInfo", "IsEncoded").getAliasName(), "false");
			if (result != null && result.equalsIgnoreCase("true")) this.isEncoded = true;
			if (result != null && result.equalsIgnoreCase("false")) this.isEncoded = false;

			// Fire error message
			if (isError == true)
			{
				ResourceContainer errorCode = ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand");
				this.context.getNotificationManager().notifyError(this.context, errorCode, errorText, null);
				this.responseContainer.setErrorCode(errorCode.getRecourceIdentifier());
				return false;
			}
		}
		catch (Exception e)
		{
			ResourceContainer errorCode = ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand");
			this.context.getNotificationManager().notifyError(this.context, errorCode, null, e);
			this.responseContainer.setErrorCode(errorCode.getRecourceIdentifier());
			return false;
		}

		// Return
		return true;
	}

	@Override
	protected boolean processResults()
	{
		try
		{
		}
		catch (Exception e)
		{
			ResourceContainer errorCode = ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand");
			this.context.getNotificationManager().notifyError(this.context, errorCode, null, e);
			this.responseContainer.setErrorCode(errorCode.getRecourceIdentifier());
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Get the result value of the command: Information if the media files
	 * exists on server, or not.
	 * 
	 * @return Returns <TT>null</TT> if the command wasn't processed yet or an
	 *         error occurred, otherwise <TT>true</TT> or <TT>false</TT>.
	 */
	public Boolean isExisting()
	{
		return this.isExisting;
	}

	/**
	 * Get the result value of the command: The file type of the media file on
	 * server.
	 * 
	 * @return Returns <TT>null</TT> if the command wasn't processed yet or an
	 *         error occurred, otherwise the file type of the media as a string.
	 */
	public String getFileType()
	{
		return this.fileType;
	}

	/**
	 * Get the result value of the command: The hash value of the media file on
	 * server.
	 * 
	 * @return Returns <TT>null</TT> if the command wasn't processed yet or an
	 *         error occurred, otherwise the file type of the media as a string.
	 */
	public String getHashValue()
	{
		return this.hashValue;
	}

	/**
	 * Get the result value of the command: The last modified date of the media
	 * file on server.
	 * 
	 * @return Returns <TT>null</TT> if the command wasn't processed yet or an
	 *         error occurred, otherwise the file type of the media as a string.
	 */
	public Date getLastModifiedDate()
	{
		if (this.lastModifiedDate == null || this.lastModifiedDate.length() != 14) { return null; }

		try
		{
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
			return dateFormatter.parse(lastModifiedDate);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Get the result value of the command: The file size of the media file on
	 * server.
	 * 
	 * @return Returns <TT>null</TT> if the command wasn't processed yet or an
	 *         error occurred, otherwise the file type of the media as a string.
	 */
	public Long getFileSize()
	{
		return this.fileSize;
	}

	/**
	 * Get the result value of the command: Information if the media file is
	 * encoded on server, or not.
	 * 
	 * @return Returns <TT>null</TT> if the command wasn't processed yet or an
	 *         error occurred, otherwise <TT>true</TT> or <TT>false</TT>.
	 */
	public Boolean isEncoded()
	{
		return this.isEncoded;
	}
}
