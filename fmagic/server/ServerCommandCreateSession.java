package fmagic.server;

import fmagic.basic.ResourceContainer;
import fmagic.basic.ResourceManager;

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
public class ServerCommandCreateSession extends ServerCommandHandshake
{
	/**
	 * Constructor
	 */
	public ServerCommandCreateSession()
	{
		super();
	}

	@Override
	protected boolean validateRequestContainer()
	{
		return super.validateRequestContainer();
	}

	@Override
	protected boolean processOnServer()
	{
		// Process functions of the Handshake CommandManager
		if (super.processOnServer() == false) return false;

		// Create a new session
		String clientPublicKey = this.getRequestContainer().getProperty("ClientPublicKey", null);
		String clientSessionIdentifier = this.requestContainer.getClientSessionIdentifier();
		
		if (this.serverManager.sessionAddClientSession(clientSessionIdentifier, clientPublicKey) == false)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ClientSessionAlreadyExistsOnServer");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();
			
			// Notify error
			String errorText = "--> Client session identifier requested '" + clientSessionIdentifier + "'";
			this.context.getNotificationManager().notifyError(this.context, resourceContainer, errorText, null);
			this.setErrorMessageRuntimeError(this.context, this.responseContainer, enumIdentifier, null);
			
			// Return
			return false;
		}

		// Return
		return true;
	}

	@Override
	protected boolean evaluateResults()
	{
		return super.evaluateResults();
	}
}
