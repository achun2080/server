package fmagic.client.command;

import fmagic.basic.application.ApplicationManager;
import fmagic.basic.command.ConnectionContainer;
import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * COMMAND: Read a media file from a remote server.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 01.05.2013 - Created
 */
public class ClientCommandMediaFileRead extends ClientCommand
{
	private final String mediaResourceIdentifier;
	private final String dataIdentifier;

	private String fileType = null;
	private Boolean isExisting = null;
	private Boolean isRead = null;
	private String mediaContent = null;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Current context.
	 * 
	 * @param application
	 *            Application client.
	 */
	public ClientCommandMediaFileRead(Context context,
			ApplicationManager application, ConnectionContainer connectionContainer, String mediaResourceIdentifier,
			String dataIdentifier)
	{
		super(context, application, ResourceManager.command(context, "MediaFileRead").getRecourceIdentifier(), connectionContainer, context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "CommandMediaFileRead", "SocketTimeoutInMilliseconds"), false));

		this.mediaResourceIdentifier = mediaResourceIdentifier;
		this.dataIdentifier = dataIdentifier;
	}

	@Override
	protected boolean prepareRequestContainer()
	{
		try
		{
			// Set parameter: MediaResourceIdentifier
			this.requestContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileRead", "MediaResourceIdentifier").getAliasName(), this.mediaResourceIdentifier);

			// Set parameter: DataIdentifier
			this.requestContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileRead", "DataIdentifier").getAliasName(), this.dataIdentifier);
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
			this.fileType = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileRead", "FileType").getAliasName(), null);
			
			if (this.fileType == null || this.fileType.length() == 0)
			{
				errorText += "\n--> Missing value 'FileType'";
				isError = true;
			}

			// Get result: IsExisting
			String result = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileRead", "IsExisting").getAliasName(), "false");
			if (result != null && result.equalsIgnoreCase("true")) this.isExisting = true;
			if (result != null && result.equalsIgnoreCase("false")) this.isExisting = false;

			// Get result: IsRead
			result = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileRead", "IsRead").getAliasName(), "false");
			if (result != null && result.equalsIgnoreCase("true")) this.isRead = true;
			if (result != null && result.equalsIgnoreCase("false")) this.isRead = false;
			
			// Get result: MediaContent
			this.mediaContent = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileRead", "MediaContent").getAliasName(), null); 
			
			if (this.isRead == true && (this.mediaContent == null || this.mediaContent.length() == 0))
			{
				errorText += "\n--> Missing value 'MediaContent'";
				isError = true;
			}
			
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
	public Boolean isMediaFileExisting()
	{
		return this.isExisting;
	}

	/**
	 * Get the result value of the command: Information if the media files could
	 * be read, or not.
	 * 
	 * @return Returns <TT>null</TT> if the command wasn't processed yet or an
	 *         error occurred, otherwise <TT>true</TT> or <TT>false</TT>.
	 */
	public Boolean isMediaFileRead()
	{
		return this.isRead;
	}

	/**
	 * Get the result value of the command: The content of the read media file.
	 * 
	 * @return Returns <TT>null</TT> if the command wasn't processed yet or an
	 *         error occurred, otherwise the content of the media as a string.
	 */
	public String getMediaContent()
	{
		return this.mediaContent;
	}

	/**
	 * Get the result value of the command: The file type of the media file read on server.
	 * 
	 * @return Returns <TT>null</TT> if the command wasn't processed yet or an
	 *         error occurred, otherwise the file type of the media as a string.
	 */
	public String getFileType()
	{
		return this.fileType;
	}
}
