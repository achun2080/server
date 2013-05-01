package fmagic.client.command;

import fmagic.basic.context.Context;
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
	/**
	 * Constructor 1
	 * 
	 * @param context
	 *            Current context.
	 * 
	 * @param client
	 *            Application client.
	 */
	public ClientCommandHandshake(Context context, ClientManager client)
	{
		super(context, client, ResourceManager.command(context, "Processing", "Handshake").getRecourceIdentifier());
	}

	/**
	 * Constructor2
	 * 
	 * @param context
	 *            Current context.
	 * 
	 * @param client
	 *            Application client.
	 * 
	 * @param commandIdentifier
	 *            CommandManager identifier to set.
	 */
	public ClientCommandHandshake(Context context, ClientManager client,
			String commandIdentifier)
	{
		super(context, client, commandIdentifier);
	}

	@Override
	protected boolean prepareRequestContainer()
	{
		// Get and check public key of the client
		String clientPublicKey = this.context.getConfigurationManager().getProperty(this.context, ResourceManager.configuration(this.context, "Application", "PublicKey"), null, true);

		if (clientPublicKey != null && !clientPublicKey.equals(""))
		{
			this.requestContainer.addProperty("ClientPublicKey", clientPublicKey);
		}
		else
		{
			this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Application", "PublicKeyOnClientNotSet"), null, null);
			return false;
		}

		// Return
		return true;
	}

	@Override
	protected boolean evaluateResults()
	{
		// Check if public key of the server is set
		if (this.responseContainer.getProperty("ServerPublicKey", null) == null)
		{
			this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Application", "PublicKeyOnServerNotSet"), null, null);
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
			this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Command", "ErrorOnProcessingCommand"), null, e);
			return false;
		}

		// Return
		return true;
	}
}
