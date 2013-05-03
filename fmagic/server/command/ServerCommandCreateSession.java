package fmagic.server.command;

import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceManager;

/**
 * COMMAND: Force the server to create a new session using the session
 * identifier the client set.
 * <p>
 * The basic functionality of this class is the same as in the COMMAND
 * <TT>ServerCommandHandshake</TT>. Thats why it inherits this COMMAND class,
 * but extends it with the function of creating a new session.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 23.12.2012 - Created
 */
public class ServerCommandCreateSession extends ServerCommand
{
	private String clientPublicKey = null;

	private String serverPublicKey = null;
	private Boolean isSuccessful = null;

	/**
	 * Constructor 1
	 */
	public ServerCommandCreateSession()
	{
		super();
	}

	/**
	 * Constructor 2
	 */
	public ServerCommandCreateSession(Context context, String commandIdentifier)
	{
		super(context, commandIdentifier);
	}

	@Override
	public void setCommandIdentifier(Context context)
	{
		this.commandIdentifier = ResourceManager.command(context, "CreateSession").getRecourceIdentifier();
	}

	@Override
	protected boolean validateRequestContainer()
	{
		try
		{
			// Get parameter: ClientPublicKey
			this.clientPublicKey = this.requestContainer.getProperty(ResourceManager.commandParameter(this.getContext(), "CreateSession", "ClientPublicKey").getAliasName(), null);

			if (this.clientPublicKey == null || this.clientPublicKey.length() == 0)
			{
				String errorText = "--> Error on validating command parameter";
				errorText += "\n--> Missing client public key";
				this.notifyError("Command", "IntegrityError", errorText, null);
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
			// Create a new session
			String clientSessionIdentifier = this.requestContainer.getClientSessionIdentifier();

			if (this.serverManager.sessionAddClientSession(clientSessionIdentifier, this.clientPublicKey) == false)
			{
				String errorText = "--> Client session identifier requested '" + clientSessionIdentifier + "'";
				this.notifyError("Application", "ClientSessionAlreadyExistsOnServer", errorText, null);

				this.isSuccessful = false;
			}
			else
			{
				this.isSuccessful = true;
			}

			// Get public key of the server
			this.serverPublicKey = this.context.getConfigurationManager().getProperty(this.context, ResourceManager.configuration(this.context, "Application", "PublicKey"), null, true);

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
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "CreateSession", "IsSuccessful").getAliasName(), this.isSuccessful.toString());

			// Set parameter: ServerPublicKey
			this.responseContainer.addProperty(ResourceManager.commandParameter(this.getContext(), "CreateSession", "ServerPublicKey").getAliasName(), this.serverPublicKey);

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
