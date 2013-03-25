package fmagic.server;

import fmagic.basic.ResourceContainer;
import fmagic.basic.ResourceManager;

public class ServerCommandHandshake extends ServerCommand
{
	/**
	 * Constructor
	 */
	public ServerCommandHandshake()
	{
		super();
	}

	@Override
	protected boolean validateRequestContainer()
	{
		// Get and check public key of the client
		String clientPublicKey = this.getRequestContainer().getProperty("ClientPublicKey", null);

		if (clientPublicKey != null && !clientPublicKey.equals(""))
		{
		}
		else
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "PublicKeyOnClientNotSet");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			this.context.getNotificationManager().notifyError(this.context, resourceContainer, null, null);
			this.setErrorMessageTechnicalError(this.context, this.responseContainer, enumIdentifier);

			// Return
			return false;
		}

		// Return
		return true;
	}

	@Override
	protected boolean processOnServer()
	{
		this.responseContainer.clearErrorCode();

		this.responseContainer.setCommandIdentifier(this.requestContainer.getCommandIdentifier());

		// Get public key of the server
		String serverPublicKey = this.context.getConfigurationManager().getProperty(this.context, ResourceManager.configuration(this.context, "Application", "PublicKey"), null, true);
		if (serverPublicKey != null && !serverPublicKey.equals("")) this.responseContainer.addProperty("ServerPublicKey", serverPublicKey);

		// Return
		return true;
	}

	@Override
	protected boolean evaluateResults()
	{
		// Check if public key of the server is set
		if (this.responseContainer.getProperty("ServerPublicKey", null) == null)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "PublicKeyOnServerNotSet");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			this.context.getNotificationManager().notifyError(this.context, resourceContainer, null, null);
			this.setErrorMessageTechnicalError(this.context, this.responseContainer, enumIdentifier);

			// Return
			return false;
		}

		// Return
		return true;
	}
}
