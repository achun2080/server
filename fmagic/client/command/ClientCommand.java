package fmagic.client.command;

import fmagic.basic.application.ApplicationManager;
import fmagic.basic.command.Command;
import fmagic.basic.command.CommandHandler;
import fmagic.basic.command.ConnectionContainer;
import fmagic.basic.command.RequestContainer;
import fmagic.basic.command.ResponseContainer;
import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceManager;

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
	final protected ApplicationManager application;
	final protected ConnectionContainer connectionContainer;
	final protected int socketTimeoutInMilliseconds;

	/**
	 * Constructor
	 */
	public ClientCommand(Context context, ApplicationManager application,
			String commandIdentifier, ConnectionContainer connectionContainer,
			int socketTimeoutInMilliseconds)
	{
		// Call super class
		super(context, commandIdentifier);

		// Create a SILENT dump context regarding the executing of a command on
		// a client
		this.context = context.createSilentDumpContext(ResourceManager.context(context, "Processing", "ClientRequestToServer"));

		// Get data
		this.application = application;
		this.connectionContainer = connectionContainer;
		this.socketTimeoutInMilliseconds = socketTimeoutInMilliseconds;

		// Create a request container
		this.requestContainer = new RequestContainer(application.getApplicationIdentifier().toString(), application.getApplicationVersion(), context.getCodeName(), commandIdentifier);

		// Set session identifier
		this.requestContainer.setClientSessionIdentifier(connectionContainer.getSessionIdentifier());

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
	 * Prepare, execute and validate the command.
	 */
	public ResponseContainer execute()
	{
		try
		{
			// Establish connection
			if (this.establishConnection() == false)
			{
				this.notifyError("Application", "ErrorOnEstablishingConnection", null, null);
				this.context.getNotificationManager().flushDump(this.context);
				return this.responseContainer;
			}

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
	 * 
	 * @return Returns <TT>true</TT> if the command was processed successfully,
	 *         otherwise <TT>false</TT>.
	 */
	protected boolean processOnServer()
	{
		// Execute command on server
		CommandHandler commandHandler = new CommandHandler(connectionContainer, this.socketTimeoutInMilliseconds);
		ResponseContainer serverResponse = commandHandler.execute(this.context, this.requestContainer);

		if (serverResponse != null)
		{
			this.responseContainer = serverResponse;
			return true;
		}

		// An error occurred
		return false;
	}

	/**
	 * Prepare all parameters and resources of the command
	 * 
	 * @return Returns <TT>true</TT> if the connection could be established, or
	 *         <TT>false</TT> if an error occurred.
	 */
	public boolean establishConnection()
	{
		try
		{
			if (this.commandIdentifier.equals(ResourceManager.command(this.context, "Handshake").getRecourceIdentifier())) return true;
			if (this.commandIdentifier.equals(ResourceManager.command(this.context, "CreateSession").getRecourceIdentifier())) return true;
			return this.connectionContainer.establishConnection(this.context);
		}
		catch (Exception e)
		{
			return false;
		}
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
