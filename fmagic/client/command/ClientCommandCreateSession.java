package fmagic.client.command;

import fmagic.basic.application.ApplicationManager;
import fmagic.basic.command.ConnectionContainer;
import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * COMMAND: Force the server to create a new session using the session
 * identifier the client set.
 * <p>
 * The function is the same as in the COMMAND <TT>ClientCommandHandshake</TT>.
 * Thats why it inherits this COMMAND class and does not modify anything.
 * <p>
 * You can find a difference on server side only: If the server finds this
 * COMMAND to execute, the test regarding the client session is switched off
 * automatically.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 23.12.2012 - Created
 */
public class ClientCommandCreateSession extends ClientCommand
{
	private String keyRemotePublicKey = null;
	private Boolean isSuccessful = null;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Current context.
	 * 
	 * @param application
	 *            Application client.
	 */
	public ClientCommandCreateSession(Context context, ApplicationManager application, ConnectionContainer connectionContainer)
	{
		super(context, application, ResourceManager.command(context, "CreateSession").getRecourceIdentifier(), connectionContainer, context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "CommandCreateSession", "SocketTimeoutInMilliseconds"), false));
	}

	@Override
	protected boolean prepareRequestContainer()
	{
		try
		{
			// Set parameter: ClientPublicKey
			String keyApplicationPublicKey = this.context.getConfigurationManager().getProperty(this.context, ResourceManager.configuration(this.context, "Application", "PublicKey"), true);

			if (keyApplicationPublicKey != null && !keyApplicationPublicKey.equals(""))
			{
				this.requestContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "CreateSession", "ClientPublicKey").getAliasName(), keyApplicationPublicKey);
			}
			else
			{
				ResourceContainer errorCode = ResourceManager.notification(this.context, "Application", "PublicKeyOnClientNotSet");
				this.context.getNotificationManager().notifyError(this.context, errorCode, null, null);
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
	protected boolean evaluateResults()
	{
		try
		{
			// Get result: IsSuccessful
			String result = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "CreateSession", "IsSuccessful").getAliasName(), "false");
			if (result != null && result.equalsIgnoreCase("true")) this.isSuccessful = true;
			if (result != null && result.equalsIgnoreCase("false")) this.isSuccessful = false;

			// Get result: ServerPublicKey
			this.keyRemotePublicKey = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "CreateSession", "ServerPublicKey").getAliasName(), null);

			if (this.keyRemotePublicKey == null || this.keyRemotePublicKey.length() == 0)
			{
				ResourceContainer errorCode = ResourceManager.notification(this.context, "Application", "PublicKeyOnServerNotSet");
				this.context.getNotificationManager().notifyError(this.context, errorCode, null, null);
				this.responseContainer.setErrorCode(errorCode.getRecourceIdentifier());
				return false;
			}
			else
			{
				if (!this.context.getApplicationManager().isServerApplication())
				{
					this.getContext().getLocaldataManager().writeProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "ServerPublicKey"), this.keyRemotePublicKey);
				}
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
	 * Get the result value of the command: Information if the handshake was
	 * processed successfully.
	 * 
	 * @return Returns <TT>null</TT> if the command wasn't processed yet or an
	 *         error occurred, otherwise <TT>true</TT> or <TT>false</TT>.
	 */
	public Boolean isHandshakeSuccessful()
	{
		return this.isSuccessful;
	}

	/**
	 * Get the result value of the command: The public key of the server to be used for all commands sent to the server.
	 * 
	 * @return Returns <TT>null</TT> if the command wasn't processed yet or an
	 *         error occurred, otherwise <TT>true</TT> or <TT>false</TT>.
	 */
	public String getKeyRemotePublicKey()
	{
		return this.keyRemotePublicKey;
	}
}
