package fmagic.server.application;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;

import fmagic.basic.command.EncodingHandler;
import fmagic.basic.command.RequestContainer;
import fmagic.basic.command.ResponseContainer;
import fmagic.basic.command.SessionContainer;
import fmagic.basic.command.SocketHandler;
import fmagic.basic.context.Context;
import fmagic.basic.label.LabelManager;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;
import fmagic.server.command.ServerCommand;

/**
 * This class implements the processing of a socket request sent from a client
 * to a server.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class ServerWorkerThread implements Runnable
{
	// Sets context of server
	private final Context context;

	// Link to calling application server
	final private ServerManager serverManager;

	// Client socket that requested the command
	final private SocketHandler socketConnector;

	// Private key of the server application
	final private String serverPrivateKey;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param socketConnector
	 *            Socket connector Client socket that requested the command.
	 */
	public ServerWorkerThread(Context context, ServerManager serverManager,
			SocketHandler socketConnector, String serverPrivateKey)
	{
		this.context = context;
		this.serverManager = serverManager;
		this.socketConnector = socketConnector;
		this.serverPrivateKey = serverPrivateKey;
	}

	@Override
	public void run()
	{
		// Logging on starting request
		this.context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Server request started.");

		// Create server response container as default response
		ResponseContainer responseContainer = new ResponseContainer(serverManager.getApplicationIdentifier().toString(), serverManager.getApplicationVersion());

		// Read raw client data from the socket
		String commandToDecrypt = this.workstepReadSocketData(responseContainer);

		// Decode raw data onto a client request container
		RequestContainer requestContainer = this.workstepConvertSocketDataToRequestContainer(commandToDecrypt, responseContainer);

		// Instantiate server command object
		ServerCommand serverCommand = workstepGetServerCommandObjectInstance(requestContainer, responseContainer);

		// Check if the server is responsible for the client
		boolean checkValidation = this.workstepCheckResponsibilityAndPreconditions(requestContainer, responseContainer);

		// Execute server command object function on server
		if (checkValidation == true) responseContainer = this.workstepExecuteServerCommand(serverCommand, responseContainer);

		// Transfer some client request data to the server response data
		this.workstepTransferContainerData(requestContainer, responseContainer);

		// Encode server response container
		String commandEncoded = this.workstepConvertResponseContainerObjectToSocketData(responseContainer);

		// Write response container to the socket
		if (commandEncoded != null) socketConnector.writeData(commandEncoded);

		// Close socket
		socketConnector.closeSocket();

		// Logging on stopping request
		this.context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Server request ended.");
		this.context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, responseContainer.toString());

		// Flush the SILENT dump if an error occurred
		if (responseContainer.getErrorCode() != null) context.getNotificationManager().flushDump(context);

		// Notify WATCHDOG
		this.notifyWatchdog(this.context, requestContainer, responseContainer);

		// End of processing
		return;
	}

	/**
	 * Notify the WATCHDOG about command access.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param requestContainer
	 *            The request container of the client request.
	 * 
	 * @param responseContainer
	 *            The response container of the client request.
	 */
	private void notifyWatchdog(Context context, RequestContainer requestContainer, ResponseContainer responseContainer)
	{
		// Check variables
		if (requestContainer == null) return;
		if (responseContainer == null) return;

		// Notify WATCHDOG
		try
		{
			// Message text
			String messageText = "--> CommandManager was executed on server";

			// Set additional text
			String additionalText = "--> CommandManager was executed on server";
			additionalText += "\n--> Identifier: '" + requestContainer.getCommandIdentifier() + "'";

			additionalText += "\n\n\n";
			additionalText += requestContainer.toString();

			additionalText += "\n\n\n";
			additionalText += responseContainer.toString();
			additionalText += "\n\n\n";

			// Set resource identifier documentation
			String resourceDocumentationText = null;
			resourceDocumentationText = context.getResourceManager().getResource(context, requestContainer.getCommandIdentifier()).printManual(context);

			if (context.getWatchdogManager() != null) context.getWatchdogManager().addWatchdogCommand(context, requestContainer.getCommandIdentifier(), messageText, additionalText, resourceDocumentationText, null, new Date());
		}
		catch (Exception e)
		{
			// Be silent
		}
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
	private void setErrorMessageOnTechnicalError(Context context, ResponseContainer responseContainer, String errorCode)
	{
		responseContainer.clearErrorCode();
		responseContainer.setErrorCode(errorCode);
		responseContainer.setErrorHeadLine(LabelManager.getLabelText(context, ResourceManager.label(context, "CommonError", "errorHeadLine")));
		responseContainer.setErrorMessagePart1(LabelManager.getLabelText(context, ResourceManager.label(context, "CommonError", "errorMessagePart1")));
		responseContainer.setErrorMessagePart2(LabelManager.getLabelText(context, ResourceManager.label(context, "CommonError", "errorMessagePart2")));
		responseContainer.setErrorMessagePart3(LabelManager.getLabelText(context, ResourceManager.label(context, "Basic", "Contact")));
		responseContainer.setErrorTechnicalDescription(context.getNotificationManager().getDump(context));
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
	private void setErrorMessageOnValidationError(Context context, ResponseContainer responseContainer, String errorCode)
	{
		responseContainer.clearErrorCode();
		responseContainer.setErrorCode(errorCode);
		responseContainer.setErrorHeadLine(LabelManager.getLabelText(context, ResourceManager.label(context, "OnConnectingToServer", "ErrorHeadLine")));
		responseContainer.setErrorMessagePart1(LabelManager.getLabelText(context, ResourceManager.label(context, "OnConnectingToServer", "ErrorMessagePart1")));
		responseContainer.setErrorMessagePart2(LabelManager.getLabelText(context, ResourceManager.label(context, "OnConnectingToServer", "ErrorMessagePart2")));
		responseContainer.setErrorMessagePart3(LabelManager.getLabelText(context, ResourceManager.label(context, "OnConnectingToServer", "ErrorMessagePart3")));
		responseContainer.setErrorTechnicalDescription(context.getNotificationManager().getDump(context));
	}

	/**
	 * Read raw data from socket.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param responseContainer
	 *            The response container to work with.
	 * 
	 * @return Returns text data read from socket, or <TT>null</TT> if an error
	 *         occurred.
	 */
	private String workstepReadSocketData(ResponseContainer responseContainer)
	{
		// Validate parameter
		if (responseContainer == null) return null;

		// Read data from socket
		String commandToDecrypt = null;

		try
		{
			commandToDecrypt = this.socketConnector.readData();
		}
		catch (Exception exception)
		{
			// Be silent
		}

		// An error occurred
		if (commandToDecrypt == null || commandToDecrypt.length() == 0)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ErrorOnProcessingRequestFromClient");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			this.setErrorMessageOnTechnicalError(this.context, responseContainer, enumIdentifier);

			// Return
			return null;
		}

		// Return
		return commandToDecrypt;
	}

	/**
	 * Decode client data, read by socket, onto a request container object.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param commandToDecrypt
	 *            The raw text data read from socket, containing all information
	 *            to recreate the request container object, sent by the client.
	 * 
	 * @param responseContainer
	 *            The response container to work with.
	 * 
	 * @return Returns request container object, or <TT>null</TT> if an error
	 *         occurred.
	 */
	private RequestContainer workstepConvertSocketDataToRequestContainer(String commandToDecrypt, ResponseContainer responseContainer)
	{
		// Validate parameter
		if (commandToDecrypt == null) return null;
		if (responseContainer == null) return null;

		// Convert socket data onto request container object
		RequestContainer requestContainer = null;
		EncodingHandler encodingUitility = new EncodingHandler();

		try
		{
			requestContainer = encodingUitility.decodeRequestContainer(this.context, commandToDecrypt, this.serverPrivateKey);
		}
		catch (Exception exception)
		{
			// Be silent
		}

		// An error occurred
		if (requestContainer == null)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ErrorOnProcessingRequestFromClient");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			this.setErrorMessageOnTechnicalError(this.context, responseContainer, enumIdentifier);

			// Return
			return null;
		}

		// Return
		return requestContainer;
	}

	/**
	 * Instantiate server command object using the class name set by the request
	 * container. The class is loaded generically by name.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param requestContainer
	 *            The request container to work with.
	 * 
	 * @param responseContainer
	 *            The response container to work with.
	 * 
	 * @return Returns server command object, or <TT>null</TT> if an error
	 *         occurred.
	 */
	private ServerCommand workstepGetServerCommandObjectInstance(RequestContainer requestContainer, ResponseContainer responseContainer)
	{
		// Validate parameter
		if (requestContainer == null) return null;
		if (responseContainer == null) return null;

		// Instantiate Server CommandManager Object
		ServerCommand serverCommand = null;
		String commandClazzName = null;

		try
		{
			// Get class name of class to invoke
			commandClazzName = "fmagic.server." + this.context.getResourceManager().getResource(context, requestContainer.getCommandIdentifier()).getAliasName();

			// Set an own class loader to define specific directory to search
			// for
			// classes
			ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

			String serverClassPath = "file://" + ServerCommand.class.getResource("").getPath();
			URL classes = new URL(serverClassPath);

			ClassLoader custom = new URLClassLoader(new URL[] { classes }, systemClassLoader);
			Class<?> serverCommandClass = custom.loadClass(commandClazzName);

			// Create new instance of the class to invoke
			serverCommand = (ServerCommand) serverCommandClass.newInstance();

			// Set specific session context
			String clientSessionIdentifier = requestContainer.getClientSessionIdentifier();
			SessionContainer sessionContainer = this.serverManager.sessionGetClientSession(clientSessionIdentifier);
			this.context.setServerSession(sessionContainer);
			serverCommand.setContext(this.context);

			// Initialize server command
			serverCommand.createResponseContainer(serverManager.getApplicationIdentifier().toString(), this.serverManager.getApplicationVersion());
			serverCommand.setServer(this.serverManager);
			serverCommand.setRequestContainer(requestContainer);
		}
		catch (Exception exception)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ErrorOnInvokingCommand");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			String errorText = "--> CommandManager class not found";
			errorText += "\n--> CommandManager class name: '" + commandClazzName + "'";
			this.context.getNotificationManager().notifyError(this.context, resourceContainer, errorText, exception);
			this.setErrorMessageOnTechnicalError(this.context, responseContainer, enumIdentifier);

			// Return
			return null;
		}

		// Return
		return serverCommand;
	}

	/**
	 * Execute server command object function on server.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param serverCommand
	 *            The server command object to execute.
	 * 
	 * @param parameterResponseContainer
	 *            The response container to work with.
	 * 
	 * @return Returns new response container, built up by the execute function,
	 *         or <TT>null</TT> if an error occurred.
	 */
	private ResponseContainer workstepExecuteServerCommand(ServerCommand serverCommand, ResponseContainer parameterResponseContainer)
	{
		// Validate parameter
		if (serverCommand == null) return parameterResponseContainer;
		if (parameterResponseContainer == null) return parameterResponseContainer;

		// Execute server command function
		ResponseContainer newResponseContainer = null;

		try
		{
			newResponseContainer = serverCommand.execute();
		}
		catch (Exception exception)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ErrorOnInvokingCommand");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			String errorText = "--> Error on executing command class";
			errorText += "\n--> CommandManager class name: '" + serverCommand.toString() + "'";
			this.context.getNotificationManager().notifyError(this.context, resourceContainer, errorText, exception);
			this.setErrorMessageOnTechnicalError(this.context, parameterResponseContainer, enumIdentifier);

			// Return
			return parameterResponseContainer;
		}

		if (newResponseContainer == null)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ErrorOnProcessingRequestFromClient");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			this.setErrorMessageOnTechnicalError(context, parameterResponseContainer, enumIdentifier);

			// Return
			return parameterResponseContainer;
		}

		// Return
		return newResponseContainer;
	}

	/**
	 * Transfer some client request data to the server response data.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param requestContainer
	 *            The request container to work with.
	 * 
	 * @param responseContainer
	 *            The response container to work with.
	 */
	private void workstepTransferContainerData(RequestContainer requestContainer, ResponseContainer responseContainer)
	{
		// Validate parameter
		if (requestContainer == null) return;
		if (responseContainer == null) return;

		// Transfer data
		try
		{
			responseContainer.setSession(requestContainer.getClientSessionIdentifier());
		}
		catch (Exception exception)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ErrorOnInvokingCommand");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			String errorText = "--> Error on executing command class";
			errorText += "\n--> CommandManager class name: '" + requestContainer.getCommandIdentifier() + "'";
			this.context.getNotificationManager().notifyError(this.context, resourceContainer, errorText, exception);
			this.setErrorMessageOnTechnicalError(this.context, responseContainer, enumIdentifier);

			// Return
			return;
		}

		// Return
		return;
	}

	/**
	 * Convert a response container to a raw socket data string, in order to
	 * send it back to the client.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param responseContainer
	 *            The response container to convert.
	 * 
	 * @return Returns the encoded string, or <TT>null</TT> if an error
	 *         occurred.
	 * 
	 */
	private String workstepConvertResponseContainerObjectToSocketData(ResponseContainer responseContainer)
	{
		// Validate parameter
		if (responseContainer == null) return null;

		// Convert a response container
		String commandEncoded = null;
		EncodingHandler encodingUitility = new EncodingHandler();

		try
		{
			commandEncoded = encodingUitility.encodeResponseContainer(this.context, responseContainer, null);
		}
		catch (Exception exception)
		{
			// Silent
		}

		// Return
		return commandEncoded;
	}

	/**
	 * Check if the server is responsible for the client and validate other
	 * preconditions.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param requestContainer
	 *            The request container to work with.
	 * 
	 * @param responseContainer
	 *            The response container to work with.
	 * 
	 * @return Returns <TT>true</TT> if the check was successful, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean workstepCheckResponsibilityAndPreconditions(RequestContainer requestContainer, ResponseContainer responseContainer)
	{
		// Validate parameter
		if (requestContainer == null) return false;
		if (responseContainer == null) return false;

		// Check client application identifier
		try
		{
			if (!requestContainer.getClientApplicationIdentifier().equals(responseContainer.getServerApplicationIdentifier()))
			{
				// Get resource container
				ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "WrongClientApplication");
				String enumIdentifier = "";
				if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

				// Notify error
				String errorText = "--> Requesting client application: '" + requestContainer.getClientApplicationIdentifier() + "'";
				this.context.getNotificationManager().notifyError(this.context, resourceContainer, errorText, null);
				this.setErrorMessageOnValidationError(this.context, responseContainer, enumIdentifier);

				// Return
				return false;
			}
		}
		catch (Exception exception)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "WrongClientApplication");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			String errorText = "--> Requesting client application: '" + requestContainer.getClientApplicationIdentifier() + "'";
			this.context.getNotificationManager().notifyError(this.context, resourceContainer, errorText, exception);
			this.setErrorMessageOnValidationError(this.context, responseContainer, enumIdentifier);

			// Return
			return false;
		}

		// Check client version
		if (requestContainer.getClientVersion() != responseContainer.getServerVersion())
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "WrongClientVersion");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			String errorText = "--> Requesting client version: '" + requestContainer.getClientVersion() + "'";
			this.context.getNotificationManager().notifyError(this.context, resourceContainer, errorText, null);
			this.setErrorMessageOnValidationError(this.context, responseContainer, enumIdentifier);

			// Return
			return false;
		}

		// Check if the client session already exists
		try
		{
			/*
			 * Note: If the server finds the COMMAND
			 * 'ClientCommandCreateSession' to execute, the test regarding the
			 * client session is switched off.
			 */
			if (!requestContainer.getCommandIdentifier().equals(ResourceManager.command(context, "Processing", "CreateSession").getRecourceIdentifier()))
			{
				if (this.serverManager.sessionCheckClientSession(requestContainer.getClientSessionIdentifier()) == false)
				{
					// Get resource container
					ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ClientSessionDoesNotExistOnServer");
					String enumIdentifier = "";
					if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

					// Notify error
					String errorText = "--> Requesting client session identifier: '" + requestContainer.getClientSessionIdentifier() + "'";
					this.context.getNotificationManager().notifyError(this.context, resourceContainer, errorText, null);
					this.setErrorMessageOnValidationError(this.context, responseContainer, enumIdentifier);

					// Return
					return false;
				}
			}
		}
		catch (Exception exception)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.context, "Application", "ClientSessionDoesNotExistOnServer");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			String errorText = "--> Requesting client session identifier: '" + requestContainer.getClientSessionIdentifier() + "'";
			this.context.getNotificationManager().notifyError(this.context, resourceContainer, errorText, exception);
			this.setErrorMessageOnValidationError(this.context, responseContainer, enumIdentifier);

			// Return
			return false;
		}

		// Return
		return true;
	}
}
