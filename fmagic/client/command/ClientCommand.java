package fmagic.client.command;

import fmagic.basic.command.CommandManager;
import fmagic.basic.command.RequestContainer;
import fmagic.basic.command.ResponseContainer;
import fmagic.basic.context.Context;
import fmagic.basic.label.LabelManager;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.application.ClientManager;

/**
 * This class defines common functions needed by all client applications.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 */
public abstract class ClientCommand extends CommandManager
{
	// Current context
	final protected Context context;

	// Current client application
	final protected ClientManager client;

	// Request container
	protected RequestContainer requestContainer = null;

	// Response container
	protected ResponseContainer responseContainer = null;

	/**
	 * Constructor
	 */
	public ClientCommand(Context context, ClientManager client,
			String commandIdentifier)
	{
		// Call super class
		super();

		// Create a SILENT dump context regarding the executing of a command on
		// a client
		this.context = context.createSilentDumpContext(ResourceManager.context(context, "Processing", "ClientRequestToServer"));

		// Save client object
		this.client = client;

		// Create a request container
		this.requestContainer = new RequestContainer(client.getApplicationIdentifier().toString(), client.getApplicationVersion(), commandIdentifier);
		this.setClientSessionIdentifier();

		// Create a response container with a default error message
		this.responseContainer = new ResponseContainer(null, 0);
	}

	@Override
	public boolean validateResources(Context context)
	{
		boolean isError = super.validateResources(context);
		return isError;
	}

	/**
	 * Prepare all parameters and resources of the command
	 * 
	 * @return Returns <TT>true</TT> if the command could be prepared, or
	 *         <TT>false</TT> if an error occurred.
	 */
	abstract protected boolean prepareRequestContainer();

	/**
	 * Evaluates the results of the remote call.
	 */
	abstract protected boolean evaluateResults();

	/**
	 * Process results of the remote call on the client.
	 */
	abstract protected boolean processResults();

	/**
	 * Get the last known client session identifier from the persistence manager
	 * and set it as the current session identifier.
	 * <p>
	 * If no valid client session identifier was found, a new one will be
	 * created.
	 */
	private void setClientSessionIdentifier()
	{
		// Validate parameter
		if (this.requestContainer == null) return;

		// Get last known client session identifier from persistance manager
		String clientSessionIdentifier = client.readLastKnownClientSessionIdentifier();

		// Create a new identifier and save it to the persistance manager
		if (clientSessionIdentifier == null || clientSessionIdentifier.equals(""))
		{
			clientSessionIdentifier = this.requestContainer.createClientSessionIdentifier();
			client.saveLastKnownClientSessionIdentifier(clientSessionIdentifier);
			return;
		}

		// Set known the known identifier
		this.requestContainer.setClientSessionIdentifier(clientSessionIdentifier);

		// Return
		return;
	}

	/**
	 * Create a new client session identifier and save it to the persistance
	 * manager.
	 */
	public void resetClientSessionIdentifier()
	{
		// Validate parameter
		if (this.requestContainer == null) return;

		// Create a new identifier
		String clientSessionIdentifier = this.requestContainer.createClientSessionIdentifier();
		
		// Save the identifier to the persistence manager
		client.saveLastKnownClientSessionIdentifier(clientSessionIdentifier);

		// Set the known identifier on the request container
		this.requestContainer.setClientSessionIdentifier(clientSessionIdentifier);

		// Return
		return;
	}

	/**
	 * Prepare, execute and validate the command.
	 */
	public ResponseContainer execute()
	{
		// Prepare request container
		if (this.prepareRequestContainer() == false)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ErrorOnPreparingCommandOnClient");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			this.setErrorMessage(this.context, this.responseContainer, enumIdentifier);
			if (this.responseContainer.getErrorCode() != null) this.context.getNotificationManager().flushDump(this.context);
			
			// Return
			return this.responseContainer;
		}

		// Process request container on server as a COMMAND
		if (this.processOnServer() == false)
		{
			if (this.responseContainer.getErrorCode() != null) this.context.getNotificationManager().flushDump(this.context);
			return this.responseContainer;
		}

		// Check if there is an error code on the response container
		if (this.responseContainer.getErrorCode() != null) { return this.responseContainer; }

		if (this.evaluateResults() == false)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ErrorOnEvaluatingCommandOnClient");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			this.setErrorMessage(this.context, this.responseContainer, enumIdentifier);
			if (this.responseContainer.getErrorCode() != null) this.context.getNotificationManager().flushDump(this.context);
			return this.responseContainer;
		}

		// Process the results of the command call on client side.
		if (this.responseContainer.getErrorCode() != null) { return this.responseContainer; }

		if (this.processResults() == false)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ErrorOnProcessingCommandOnClient");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			this.setErrorMessage(this.context, this.responseContainer, enumIdentifier);
			if (this.responseContainer.getErrorCode() != null) this.context.getNotificationManager().flushDump(this.context);
			return this.responseContainer;
		}

		return this.responseContainer;
	}

	/**
	 * Process the command on application server.
	 */
	protected boolean processOnServer()
	{
		ResponseContainer serverResponse = this.client.execute(this.context, this.requestContainer);

		if (serverResponse != null)
		{
			this.responseContainer = serverResponse;
			return true;
		}

		// An error occurred
		return false;
	}

	/**
	 * Getter
	 */
	public RequestContainer getRequestContainer()
	{
		return requestContainer;
	}

	/**
	 * Getter
	 */
	public ResponseContainer getResponseContainer()
	{
		return this.responseContainer;
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
	private ResponseContainer setErrorMessage(Context context, ResponseContainer responseContainer, String errorCode)
	{
		responseContainer.clearErrorCode();
		responseContainer.setErrorCode(errorCode);
		responseContainer.setErrorHeadLine(LabelManager.getLabelText(context, ResourceManager.label(context, "CommonError", "errorHeadLine")));
		responseContainer.setErrorMessagePart1(LabelManager.getLabelText(context, ResourceManager.label(context, "CommonError", "errorMessagePart1")));
		responseContainer.setErrorMessagePart2(LabelManager.getLabelText(context, ResourceManager.label(context, "CommonError", "errorMessagePart2")));
		responseContainer.setErrorMessagePart3(LabelManager.getLabelText(context, ResourceManager.label(context, "Basic", "Contact")));
		responseContainer.setErrorTechnicalDescription(context.getNotificationManager().getDump(context));

		return responseContainer;
	}

	/**
	 * Getter
	 */
	public Context getContext()
	{
		return context;
	}
}
