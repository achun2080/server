package fmagic.client.command;

import fmagic.basic.application.ApplicationManager;
import fmagic.basic.command.ConnectionContainer;
import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * COMMAND: Check if a media file already exists on a remote server.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 30.04.2013 - Created
 */
public class ClientCommandMediaFileCheck extends ClientCommand
{
	private final String mediaResourceIdentifier;
	private final String fileType;
	private final String dataIdentifier;
	private final String hashValue;

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
	public ClientCommandMediaFileCheck(Context context,
			ApplicationManager application, ConnectionContainer connectionContainer, String  mediaResourceIdentifier, String fileType, String dataIdentifier,
			String hashValue)
	{
		super(context, application, ResourceManager.command(context, "MediaFileCheck").getRecourceIdentifier(), connectionContainer, context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "CommandMediaFileCheck", "SocketTimeoutInMilliseconds"), false));

		this.mediaResourceIdentifier = mediaResourceIdentifier;
		this.fileType = fileType;
		this.dataIdentifier = dataIdentifier;
		this.hashValue = hashValue;
	}

	@Override
	protected boolean prepareRequestContainer()
	{
		try
		{
			// Set parameter: MediaResourceIdentifier
			this.requestContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileCheck", "MediaResourceIdentifier").getAliasName(), this.mediaResourceIdentifier);

			// Set parameter: FileType
			this.requestContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileCheck", "FileType").getAliasName(), this.fileType);

			// Set parameter: DataIdentifier
			this.requestContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileCheck", "DataIdentifier").getAliasName(), this.dataIdentifier);

			// Set parameter: HashValue
			this.requestContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileCheck", "HashValue").getAliasName(), this.hashValue);
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
			// Get result: IsExisting
			 String result = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "MediaFileCheck", "IsExisting").getAliasName(), "false");
			 if (result != null && result.equalsIgnoreCase("true")) this.isExisting = true;
			 if (result != null && result.equalsIgnoreCase("false")) this.isExisting = false;
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
	 * @return Returns <TT>true</TT> or <TT>false</TT>.
	 */
	public boolean isExisting()
	{
		if (this.isExisting == null) return false;
		return this.isExisting;
	}
}
