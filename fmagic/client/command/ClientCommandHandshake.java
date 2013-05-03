package fmagic.client.command;

import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.application.ClientManager;

/**
 * COMMAND: Check if the connection to a server is available and works fine.
 * <p>
 * The client sends its public key <TT>ClientPublicKey</TT> to the server and
 * get back the public key <TT>ServerPublicKey</TT> of the server.
 * <p>
 * Note: The handshake ends successfully only if the client session is already
 * known on the server. Please use the COMMAND
 * <TT>ClientCommandCreateSession</TT> first to create a client session on the
 * server.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 12.12.2012 - Created
 */
public class ClientCommandHandshake extends ClientCommand
{
	private String serverPublicKey = null;
	private Boolean isSuccessful = null;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Current context.
	 * 
	 * @param client
	 *            Application client.
	 */
	public ClientCommandHandshake(Context context, ClientManager client)
	{
		super(context, client, ResourceManager.command(context, "Handshake").getRecourceIdentifier());
	}

	@Override
	protected boolean prepareRequestContainer()
	{
		try
		{
			// Set parameter: ClientPublicKey
			String clientPublicKey = this.context.getConfigurationManager().getProperty(this.context, ResourceManager.configuration(this.context, "Application", "PublicKey"), null, true);

			if (clientPublicKey != null && !clientPublicKey.equals(""))
			{
				this.requestContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "Handshake", "ClientPublicKey").getAliasName(), clientPublicKey);
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
			String result = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "Handshake", "IsSuccessful").getAliasName(), "false");
			if (result != null && result.equalsIgnoreCase("true")) this.isSuccessful = true;
			if (result != null && result.equalsIgnoreCase("false")) this.isSuccessful = false;
			
			// Get result: ServerPublicKey
			this.serverPublicKey = this.responseContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "Handshake", "ServerPublicKey").getAliasName(), null);

			if (this.serverPublicKey == null || this.serverPublicKey.length() == 0)
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
					this.getContext().getLocaldataManager().writeProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "ServerPublicKey"), this.serverPublicKey);
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
	public String getServerPublicKey()
	{
		return this.serverPublicKey;
	}
}
