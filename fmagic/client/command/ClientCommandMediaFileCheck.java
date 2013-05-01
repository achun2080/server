package fmagic.client.command;

import fmagic.basic.application.ApplicationManager;
import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.application.ClientManager;

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
	 * @param client
	 *            Application client.
	 */
	public ClientCommandMediaFileCheck(Context context,
			ApplicationManager client, String  mediaResourceIdentifier, String fileType, String dataIdentifier,
			String hashValue)
	{
		super(context, (ClientManager) client, ResourceManager.command(context, "Processing", "MediaFileCheck").getRecourceIdentifier());

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
			this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), null, e);
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
			this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), null, e);
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

	@Override
	protected boolean processResults()
	{
		try
		{
		}
		catch (Exception e)
		{
			this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), null, e);
			return false;
		}

		// Return
		return true;
	}
}