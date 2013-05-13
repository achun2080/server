package fmagic.client.command;

import fmagic.basic.command.Command;
import fmagic.basic.command.RequestContainer;
import fmagic.basic.command.ResponseContainer;
import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.application.ClientManager;

/**
 * This class defines common functions needed by all client applications.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 */
public abstract class ClientCommand extends Command
{
	// Current client application
	final protected ClientManager client;

	/**
	 * Constructor
	 */
	public ClientCommand(Context context, ClientManager client,
			String commandIdentifier)
	{
		// Call super class
		super(context, commandIdentifier);

		// Create a SILENT dump context regarding the executing of a command on
		// a client
		this.context = context.createSilentDumpContext(ResourceManager.context(context, "Processing", "ClientRequestToServer"));

		// Save client object
		this.client = client;

		// Create a request container
		this.requestContainer = new RequestContainer(client.getApplicationIdentifier().toString(), client.getApplicationVersion(), context.getCodeName(), commandIdentifier);
		this.setClientSessionIdentifier();

		// Create a response container with a default error message
		this.responseContainer = new ResponseContainer(null, 0, null);
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
	 * Get the last known client session identifier from local data and set it
	 * as the current session identifier.
	 * <p>
	 * If no valid client session identifier was found, a new one will be
	 * created.
	 */
	private void setClientSessionIdentifier()
	{
		// Validate parameter
		if (this.requestContainer == null) return;

		try
		{
			// Get last known client session identifier from local data
			String clientSessionIdentifier = client.readLastKnownClientSessionIdentifier();

			// Create a new identifier and save it to the local data
			if (clientSessionIdentifier == null || clientSessionIdentifier.equals(""))
			{
				clientSessionIdentifier = this.requestContainer.createClientSessionIdentifier();
				client.saveLastKnownClientSessionIdentifier(clientSessionIdentifier);
				return;
			}

			// Set known the known identifier
			this.requestContainer.setClientSessionIdentifier(clientSessionIdentifier);
		}
		catch (Exception e)
		{
			this.notifyError("Command", "ErrorOnProcessingCommand", null, e);
		}
	}

	/**
	 * Create a new client session identifier and save it to local data.
	 */
	public void resetClientSessionIdentifier()
	{
		// Validate parameter
		if (this.requestContainer == null) return;

		try
		{
			// Create a new identifier
			String clientSessionIdentifier = this.requestContainer.createClientSessionIdentifier();

			// Save the identifier to the persistence manager
			client.saveLastKnownClientSessionIdentifier(clientSessionIdentifier);

			// Set the known identifier on the request container
			this.requestContainer.setClientSessionIdentifier(clientSessionIdentifier);
		}
		catch (Exception e)
		{
			this.notifyError("Command", "ErrorOnProcessingCommand", null, e);
		}
	}

	/**
	 * Prepare, execute and validate the command.
	 */
	public ResponseContainer execute()
	{
		try
		{
			// Prepare request container
			if (this.prepareRequestContainer() == false)
			{
				this.notifyError("Application", "ErrorOnPreparingCommandOnClient", null, null);
				this.context.getNotificationManager().flushDump(this.context);
				return this.responseContainer;
			}

			// Validate all parameters of the command
			if (this.validateCommandParameters() == false)
			{
				this.context.getNotificationManager().flushDump(this.context);
				return this.responseContainer;
			}

			// Process request container on a remote server as a COMMAND
			if (this.processOnServer() == false)
			{
				this.notifyError("Application", "ErrorOnProcessingRequestOnServer", null, null);
				this.context.getNotificationManager().flushDump(this.context);
				return this.responseContainer;
			}

			// Check if there is an error code on the response container
			if (this.responseContainer.getErrorCode() != null) { return this.responseContainer; }

			if (this.evaluateResults() == false)
			{
				this.notifyError("Application", "ErrorOnEvaluatingCommandOnClient", null, null);
				this.context.getNotificationManager().flushDump(this.context);
				return this.responseContainer;
			}

			// Process the results of the command call on client side.
			if (this.responseContainer.getErrorCode() != null)
			{
				this.context.getNotificationManager().flushDump(this.context);
				return this.responseContainer;
			}

			if (this.processResults() == false)
			{
				this.notifyError("Application", "ErrorOnProcessingCommandOnClient", null, null);
				this.context.getNotificationManager().flushDump(this.context);
				return this.responseContainer;
			}
		}
		catch (Exception e)
		{
			this.notifyError("Command", "ErrorOnProcessingCommand", null, e);
		}

		// Return
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
	 * Getter
	 */
	public Context getContext()
	{
		return context;
	}
}
