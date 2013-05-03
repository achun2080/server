package fmagic.server.command;

import fmagic.basic.command.Command;
import fmagic.basic.command.RequestContainer;
import fmagic.basic.command.ResponseContainer;
import fmagic.basic.context.Context;
import fmagic.server.application.ServerManager;

/**
 * This class defines common functions needed by all application servers.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 */
public abstract class ServerCommand extends Command
{
	// Current server application
	protected ServerManager serverManager = null;

	/**
	 * Constructor 1
	 */
	public ServerCommand()
	{
		super();
	}

	/**
	 * Constructor 2
	 */
	public ServerCommand(Context context, String commandIdentifier)
	{
		super(context, commandIdentifier);
	}

	/**
	 * Set command identifier from outside. Please implement this method with
	 * the current command resource identifier.
	 * 
	 * @param context
	 *            The context to use.
	 */
	public abstract void setCommandIdentifier(Context context);

	/**
	 * Execute the command.
	 */
	public ResponseContainer execute()
	{
		try
		{
			if (this.validateRequestContainer() == false)
			{
				this.notifyError("Application", "ErrorOnValidatingClientCommandOnServer", null, null);
				return this.responseContainer;
			}

			if (this.processOnServer() == false)
			{
				this.notifyError("Application", "ErrorOnProcessingRequestFromClient", null, null);
				return this.responseContainer;
			}

			if (this.arrangeResults() == false)
			{
				this.notifyError("Application", "ErrorOnEvaluatingServerResults", null, null);
				return this.responseContainer;
			}

			// Validate all return values of the command
			if (this.validateCommandResults() == false) { return this.responseContainer; }
		}
		catch (Exception e)
		{
			this.notifyError("Command", "ErrorOnProcessingCommand", null, e);
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
	abstract protected boolean arrangeResults();

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
	 * Factory method for creating a response container.
	 * <p>
	 * The created response container is directly assigned to the class variable <TT>this.responseContainer</TT>.
	 * 
	 * @param serverApplicationIdentifier
	 *            The identifier of the application.
	 * 
	 * @param serverVersion
	 *            The version of the application.
	 */
	public void createResponseContainer(String serverApplicationIdentifier, int serverVersion, String commandIdentifier)
	{
		this.responseContainer = new ResponseContainer(serverApplicationIdentifier, serverVersion, commandIdentifier);
	}
}
