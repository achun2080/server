package fmagic.client.command;

import fmagic.basic.application.ApplicationManager;
import fmagic.basic.context.Context;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.application.ClientManager;

/**
 * COMMAND: Upload a media file from client to server.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 29.04.2013 - Created
 */
public class ClientCommandMediaFileUpload extends ClientCommandMediaFileCheck
{
	private final String fileToBeUploaded;
	
	private Boolean isUploaded = null;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Current context.
	 * 
	 * @param client
	 *            Application client.
	 */
	public ClientCommandMediaFileUpload(Context context,
			ApplicationManager client, String fileToBeUploaded, String  mediaResourceIdentifier, String fileType, String dataIdentifier,
			String hashValue)
	{
		super(context, (ClientManager) client,mediaResourceIdentifier , fileType, dataIdentifier, hashValue);
		
		this.requestContainer.setCommandIdentifier(ResourceManager.command(context, "Processing", "MediaFileUpload").getRecourceIdentifier());
		
		this.fileToBeUploaded = fileToBeUploaded;
	}

	@Override
	protected boolean prepareRequestContainer()
	{
		// Process functions of 'ClientCommandMediaFileCheck'
		super.prepareRequestContainer();
		
		// Handle own stuff
		try
		{
			if (this.fileToBeUploaded == null || this.fileToBeUploaded.length() == 0)
			{
				String additionalText = "--> Missing file name for media file to be uploaded";
				this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), additionalText, null);
				return false;
			}

			String mediaContent = FileUtilFunctions.fileReadToString(this.fileToBeUploaded);

			if (mediaContent == null)
			{
				String additionalText = "--> Error on reading data from media file '" + this.fileToBeUploaded + "'";
				this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), additionalText, null);
				return false;
			}
			
			this.requestContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFile", "MediaContent").getAliasName(), mediaContent);
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
		// Return
		return true;
	}

	/**
	 * Get the result value of the command: Information if the media file
	 * could be uploaded successfully on server, or not.
	 * 
	 * @return Returns <TT>null</TT> if the command wasn't processed yet or an
	 *         error occurred, otherwise <TT>true</TT> or <TT>false</TT>.
	 */
	public Boolean isMediaFileUploaded()
	{
		return this.isUploaded;
	}
}
