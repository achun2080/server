package fmagic.server.command;

import fmagic.basic.command.CommandManager;
import fmagic.basic.command.RequestContainer;
import fmagic.basic.command.ResponseContainer;
import fmagic.basic.context.Context;
import fmagic.basic.label.LabelManager;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;
import fmagic.server.application.ServerManager;

/**
 * This class defines common functions needed by all application servers.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 */
public abstract class ServerCommand extends CommandManager
{
	// Current context
	protected Context context = null;

	// Current server application
	protected ServerManager serverManager = null;

	// Request container
	protected RequestContainer requestContainer = null;

	// Response container
	protected ResponseContainer responseContainer = null;

	/**
	 * Constructor
	 */
	public ServerCommand()
	{
		// Call super class
		super();
	}

	@Override
	public boolean validateResources(Context context)
	{
		boolean isError = super.validateResources(context);
		return isError;
	}

	/**
	 * Execute the command.
	 */
	public ResponseContainer execute()
	{
		if (this.validateRequestContainer() == false)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ErrorOnValidatingClientCommandOnServer");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			return setErrorMessageTechnicalError(this.context, this.responseContainer, enumIdentifier);
		}

		if (this.processOnServer() == false) { return this.responseContainer; }

		if (this.evaluateResults() == false)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ErrorOnEvaluatingServerResults");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			return setErrorMessageTechnicalError(this.context, this.responseContainer, enumIdentifier);
		}

		// Return
		return this.responseContainer;
	}

	/**
	 * Prepare all parameters and resources of the command.
	 * 
	 * @return Returns <TT>true</TT> if the command could be prepared, or
	 *         <TT>false</TT> if an error occurred.
	 */
	abstract protected boolean validateRequestContainer();

	/**
	 * Process the command on application server.
	 */
	abstract protected boolean processOnServer();

	/**
	 * Evaluates the results of the processed command.
	 */
	abstract protected boolean evaluateResults();

	/**
	 * Getter
	 */
	public RequestContainer getRequestContainer()
	{
		return requestContainer;
	}

	/**
	 * Setter
	 */
	public void setRequestContainer(RequestContainer requestContainer)
	{
		this.requestContainer = requestContainer;
	}

	/**
	 * Getter
	 */
	public ResponseContainer getResponseContainer()
	{
		return responseContainer;
	}

	/**
	 * Getter
	 */
	public Context getContext()
	{
		return context;
	}

	/**
	 * Setter
	 */
	public void setContext(Context context)
	{
		this.context = context;
	}

	/**
	 * Getter
	 */
	public ServerManager getServer()
	{
		return serverManager;
	}

	/**
	 * Setter
	 */
	public void setServer(ServerManager server)
	{
		this.serverManager = server;
	}

	/**
	 * Setter
	 */
	public void createResponseContainer(String serverApplicationIdentifier, int serverVersion)
	{
		// Create a response container with a default error message
		this.responseContainer = new ResponseContainer(serverApplicationIdentifier, serverVersion);
	}

	/**
	 * Set error message on Response Container
	 * 
	 * @param context
	 *            Current context
	 * 
	 * @param responseContainer
	 *            Response container to fill with error message.
	 * 
	 * @param errorCode
	 *            Error code to notify.
	 */
	protected ResponseContainer setErrorMessageTechnicalError(Context context, ResponseContainer responseContainer, String errorCode)
	{
		responseContainer.clearErrorCode();
		responseContainer.setErrorCode(errorCode);
		responseContainer.setErrorHeadLine(LabelManager.getLabelText(context, ResourceManager.label(context, "CommonError", "errorHeadLine")));
		responseContainer.setErrorMessagePart1(LabelManager.getLabelText(context, ResourceManager.label(context, "CommonError", "errorMessagePart1")));
		responseContainer.setErrorMessagePart2(LabelManager.getLabelText(context, ResourceManager.label(context, "CommonError", "errorMessagePart2")));
		responseContainer.setErrorMessagePart3(LabelManager.getLabelText(context, ResourceManager.label(context, "Basic", "Contact")));
		this.responseContainer.setErrorTechnicalDescription(this.context.getNotificationManager().getDump(context));

		return responseContainer;
	}

	/**
	 * Set error message on Response Container
	 * 
	 * @param context
	 *            The current context
	 * 
	 * @param responseContainer
	 *            The response container to fill with the error message.
	 * 
	 * @param errorCode
	 *            The error code to notify.
	 * 
	 * @param commandIdentifier
	 *            The client command identifier (view) that has to be activated
	 *            on the client.
	 */
	protected ResponseContainer setErrorMessageRuntimeError(Context context, ResponseContainer responseContainer, String errorCode, String commandIdentifier)
	{
		responseContainer.setCommandIdentifier(commandIdentifier);

		responseContainer.clearErrorCode();
		responseContainer.setErrorCode(errorCode);
		responseContainer.setErrorHeadLine(LabelManager.getLabelText(context, ResourceManager.label(context, "CommonError", "errorHeadLine")));
		responseContainer.setErrorMessagePart1(LabelManager.getLabelText(context, ResourceManager.label(context, "CommonError", "errorMessagePart1")));
		responseContainer.setErrorMessagePart2(LabelManager.getLabelText(context, ResourceManager.label(context, "CommonError", "errorMessagePart2")));
		responseContainer.setErrorMessagePart3(LabelManager.getLabelText(context, ResourceManager.label(context, "Basic", "Contact")));
		this.responseContainer.setErrorTechnicalDescription(this.context.getNotificationManager().getDump(context));

		return responseContainer;
	}
}
