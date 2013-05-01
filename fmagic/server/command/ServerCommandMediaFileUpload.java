package fmagic.server.command;

import fmagic.basic.file.FileLocationFunctions;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceManager;

/**
 * COMMAND: Upload a media file from client to server.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 30.04.2013 - Created
 */
public class ServerCommandMediaFileUpload extends ServerCommandMediaFileCheck
{
	private String mediaContent = null;
	
	private Boolean isUploaded = null;

	/**
	 * Constructor
	 */
	public ServerCommandMediaFileUpload()
	{
		super();
	}

	@Override
	protected boolean validateRequestContainer()
	{
		// Process functions of 'ServerCommandMediaFileCheck'
		super.validateRequestContainer();
		
		// Handle own stuff
		try
		{
			String errorText = "--> Error on validating command parameter";
			boolean isError = false;

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
		// Process functions of 'ServerCommandMediaFileCheck'
		super.processOnServer();
		
		// Handle own stuff
		try
		{
			/*
			 *  Extract media content into a pending file
			 */
			String pendingFileName = FileLocationFunctions.compileFilePath(this.mediaResourceContainer.getMediaPendingFilePath(this.context), this.mediaResourceContainer.getMediaPendingFileName(this.context, this.fileType));
			
			String logText = "\n--> COMMAND SERVER MEDIA FILE UPLOAD: Pending file name created";
			logText += "\n--> Pending file name: '" + pendingFileName + "'";
			logText += "\n--> Media resource identifier: '" + this.mediaResourceContainer.getRecourceIdentifier() + "'";
			this.context.getNotificationManager().notifyLogMessage(this.context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);
			
			if (FileUtilFunctions.fileWriteFromString(pendingFileName, this.mediaContent) == false)
			{
				String errorString = "--> COMMAND SERVER MEDIA FILE UPLOAD: Error on writing media content into the pending file";
				errorString += "\n--> Media resource identifier: '" + this.mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Pending file name: '" + pendingFileName + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Command", "ErrorOnProcessingCommand"), errorString, null);
				return false;
			}
			
			logText = "\n--> COMMAND SERVER MEDIA FILE UPLOAD: Media content stored in pending file";
			logText += "\n--> Pending file name: '" + pendingFileName + "'";
			logText += "\n--> Media resource identifier: '" + this.mediaResourceContainer.getRecourceIdentifier() + "'";
			logText += "\n--> Data identifier: '" + this.dataIdentifier + "'";
			logText += "\n--> Hash value: '" + this.hashValue + "'";
			this.context.getNotificationManager().notifyLogMessage(this.context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);
			
			/*
			 *  Upload pending file into the system as regular media file
			 */
			if (this.context.getMediaManager().operationStoreLocal(this.context, this.mediaResourceContainer, pendingFileName, this.dataIdentifier) == false)
			{
				String errorString = "--> COMMAND SERVER MEDIA FILE UPLOAD: Error on storing pending file as regular media file";
				errorString += "\n--> Media resource identifier: '" + this.mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Pending file to be stored: '" + pendingFileName + "'";
				errorString += "\n--> Data identifier: '" + this.dataIdentifier + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Command", "ErrorOnProcessingCommand"), errorString, null);
				return false;
			}
			
			
			logText = "\n--> COMMAND SERVER MEDIA FILE UPLOAD: Pending file stored in regular media file";
			logText += "\n--> Pending file name: '" + pendingFileName + "'";
			logText += "\n--> Media resource identifier: '" + this.mediaResourceContainer.getRecourceIdentifier() + "'";
			logText += "\n--> Data identifier: '" + this.dataIdentifier + "'";
			this.context.getNotificationManager().notifyLogMessage(this.context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);
			
			/*
			 *  Delete pending file
			 */
			if (FileUtilFunctions.fileDelete(pendingFileName) == false)
			{
				String errorString = "--> COMMAND SERVER MEDIA FILE UPLOAD: Error on deleting pending file";
				errorString += "\n--> Media resource identifier: '" + this.mediaResourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> Pending file to be deleted: '" + pendingFileName + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Command", "ErrorOnProcessingCommand"), errorString, null);
				return false;
			}
			
			logText = "\n--> COMMAND SERVER MEDIA FILE UPLOAD: Pending file deleted";
			logText += "\n--> Pending file name: '" + pendingFileName + "'";
			logText += "\n--> Media resource identifier: '" + this.mediaResourceContainer.getRecourceIdentifier() + "'";
			this.context.getNotificationManager().notifyLogMessage(this.context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);
			
			// File already exists
			this.isUploaded = true;
		
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
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "MediaFile", "IsUploaded").getAliasName(), this.isUploaded.toString());
			
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
