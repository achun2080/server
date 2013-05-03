package fmagic.server.command;

import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceManager;

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
public class ServerCommandHandshake extends ServerCommand
{
	private String serverPublicKey = null;
	private Boolean isSuccessful = null;

	/**
	 * Constructor 1
	 */
	public ServerCommandHandshake()
	{
		super();
	}

	/**
	 * Constructor 2
	 */
	public ServerCommandHandshake(Context context, String commandIdentifier)
	{
		super(context, commandIdentifier);
	}

	@Override
	public void setCommandIdentifier(Context context)
	{
		this.commandIdentifier = ResourceManager.command(context, "Handshake").getRecourceIdentifier();
	}

	@Override
	protected boolean validateRequestContainer()
	{
		try
		{
			// Get parameter: ClientPublicKey
			String clientPublicKey = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "Handshake", "ClientPublicKey").getAliasName(), null);

			if (clientPublicKey == null || clientPublicKey.length() == 0)
			{
				this.notifyError("Application", "PublicKeyOnClientNotSet", null, null);
				return false;
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
	protected boolean processOnServer()
	{
		try
		{
			// Get public key of the server
			this.serverPublicKey = this.context.getConfigurationManager().getProperty(this.context, ResourceManager.configuration(this.context, "Application", "PublicKey"), null, true);

			// Set command to status successful
			this.isSuccessful = true;

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
			// Set parameter: IsSuccessful
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "Handshake", "IsSuccessful").getAliasName(), this.isSuccessful.toString());

			// Set parameter: ServerPublicKey
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "Handshake", "ServerPublicKey").getAliasName(), this.serverPublicKey);

			// Return
			return true;
		}
		catch (Exception e)
		{
			this.notifyError("Command", "ErrorOnProcessingCommand", null, e);
			return false;
		}
	}
}
