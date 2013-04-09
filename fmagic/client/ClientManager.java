package fmagic.client;

import java.net.SocketTimeoutException;

import fmagic.basic.ApplicationManager;
import fmagic.basic.Context;
import fmagic.basic.EncodingHandler;
import fmagic.basic.LabelManager;
import fmagic.basic.NotificationManager;
import fmagic.basic.RequestContainer;
import fmagic.basic.ResourceContainer;
import fmagic.basic.ResourceManager;
import fmagic.basic.ResponseContainer;
import fmagic.basic.SocketHandler;
import fmagic.basic.ResourceContainer.OriginEnum;

/**
 * This class implements common functions used by all clients of the FMAGIC
 * system.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public abstract class ClientManager extends ApplicationManager
{
	// Encoding data
	private String clientPublicKey = null;
	private String clientPrivateKey = null;

	// Socket data
	private String serverSocketHost = null;
	private int serverSocketPort = 0;
	private int timeoutTimeInMilliseconds = 0;

	/**
	 * Constructor
	 * 
	 * @param applicationIdentifier
	 *            Identifier of the application.
	 * 
	 * @param applicationVersion
	 *            Software version of the client application.
	 * 
	 * @param codeName
	 *            Name for identifying the name from outside.
	 * 
	 * @param runningInTestMode
	 *            Set to TRUE if the application is running in test mode.
	 * 
	 * @param testCaseName
	 *            Is to be set to the name of the test case, if the application
	 *            is running in test mode, or <TT>null</TT> if the application
	 *            is running in productive mode.
	 */
	protected ClientManager(
			ApplicationManager.ApplicationIdentifierEnum applicationIdentifier,
			int applicationVersion, String codeName, boolean runningInTestMode,
			String testCaseName)
	{
		// Instantiate super class
		super(applicationIdentifier, applicationVersion, codeName, OriginEnum.Client, runningInTestMode, testCaseName);
	}

	@Override
	public boolean readSecurityKeys()
	{
		// Initialize
		boolean isSuccessful = true;

		// Read and check
		try
		{
			this.clientPublicKey = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(this.getContext(), "Application", "PublicKey"), "", true);
			if (this.clientPublicKey == null || this.clientPublicKey.length() == 0) isSuccessful = false;

			this.clientPrivateKey = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(this.getContext(), "Application", "PrivateKey"), "", true);
			if (this.clientPrivateKey == null || this.clientPrivateKey.length() == 0) isSuccessful = false;
		}
		catch (Exception e)
		{
			return false;
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Set connection parameter for the socket connection to a server.
	 * 
	 * @param serverSocketHost
	 *            Host name or IP address to connect with.
	 * 
	 * @param serverSocketPort
	 *            Port number to connect with.
	 * 
	 * @param timeoutTimeInMilliseconds
	 *            Time out time for waiting for server response in milliseconds.
	 * 
	 */
	public void setSocketConnectionParameter(String serverSocketHost, int serverSocketPort, int timeoutTimeInMilliseconds)
	{
		this.serverSocketHost = serverSocketHost;
		this.serverSocketPort = serverSocketPort;
		this.timeoutTimeInMilliseconds = timeoutTimeInMilliseconds;
	}

	/**
	 * Getter
	 */
	public int getServerSocketPort()
	{
		return serverSocketPort;
	}

	/**
	 * Getter
	 */
	public String getServerSocketHost()
	{
		return serverSocketHost;
	}

	@Override
	protected boolean bindResources()
	{
		// Return
		return true;
	}

	@Override
	protected void releaseResources()
	{
	}

	/**
	 * Start client.
	 * 
	 * @return Returns <TT>true</TT> if the client could be started, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean startApplication()
	{
		// Check shutdown flag
		if (this.isShutdown() == true) return false;

		// Bind all resources
		if (this.bindResources() == false) return false;

		// Logging
		this.getContext().getNotificationManager().notifyLogMessage(this.getContext(), NotificationManager.SystemLogLevelEnum.NOTICE, "Starting client application [" + this.getCodeName() + "]: " + this.toString());

		// Fire Event
		this.getContext().getNotificationManager().notifyEvent(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ClientStarted"), null, null);

		// Create event: ConfigurationSettingsNotification
		String configurationText = this.getContext().getConfigurationManager().printConfigurationSettings(this.getContext());
		this.getContext().getNotificationManager().notifyEvent(this.getContext(), ResourceManager.notification(this.getContext(), "Configuration", "ConfigurationSettingsNotification"), configurationText, null);

		// Return
		return true;
	}

	/**
	 * Stop client.
	 */
	public void stopApplication()
	{
		// Logging
		this.getContext().getNotificationManager().notifyLogMessage(this.getContext(), NotificationManager.SystemLogLevelEnum.NOTICE, "Stopping client application [" + this.getCodeName() + "]: " + this.toString());

		// Release all resources
		this.releaseResources();

		// Fire Event
		this.getContext().getNotificationManager().notifyEvent(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ClientStopped"), null, null);

		// Release all WATCHDOG
		this.releaseWatchdog();

		// Return
		return;
	}

	/**
	 * Executes a command on server and waits for response.
	 * 
	 * @param requestContainer
	 *            Request container that hold all data to send to the server.
	 */
	public ResponseContainer execute(Context executingContext, RequestContainer requestContainer)
	{
		// Logging on start
		executingContext.getNotificationManager().notifyLogMessage(executingContext, NotificationManager.SystemLogLevelEnum.NOTICE, "Client request to server started.");
		if (requestContainer != null) executingContext.getNotificationManager().notifyLogMessage(executingContext, NotificationManager.SystemLogLevelEnum.CODE, requestContainer.toString());

		// Create server response container as default response
		ResponseContainer responseContainer = new ResponseContainer(null, 0);

		// Transfer some client request data to the server response data
		this.workstepTransferContainerData(requestContainer, responseContainer);

		// Get public key of the server
		String serverPublicKey = null;

		// Encode request container
		String commandEncrypted = workstepConvertRequestContainerObjectToSocketData(executingContext, requestContainer, serverPublicKey, responseContainer);

		// Open a socket connection to the server
		SocketHandler socketHandler = workstepOpenSocketConnectionToServer(executingContext, responseContainer);

		// Do handshake
		if (socketHandler != null && commandEncrypted != null)
		{
			// Write request container to the socket
			socketHandler.writeData(commandEncrypted);

			// Read raw response data from socket
			String responseData = null;

			try
			{
				responseData = socketHandler.readData();
			}
			catch (SocketTimeoutException socketTimeoutException)
			{
				// Get resource container
				ResourceContainer resourceContainer = ResourceManager.notification(this.getContext(), "Application", "SocketTimeout");
				String enumIdentifier = "";
				if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

				// Notify error
				String errorText = "--> Timeout value: '" + socketHandler.getTimeoutTimeInMilliseconds() + "' Milliseconds";
				executingContext.getNotificationManager().notifyError(executingContext, resourceContainer, errorText, socketTimeoutException);
				this.setErrorMessageOnSocketConnection(executingContext, responseContainer, enumIdentifier);
			}

			// Decode raw response data onto a response container
			responseContainer = workstepConvertSocketDataToResponseContainer(executingContext, responseData, responseContainer);
		}

		// Close socket
		if (socketHandler != null) socketHandler.closeSocket();

		// Logging on stop
		executingContext.getNotificationManager().notifyLogMessage(executingContext, NotificationManager.SystemLogLevelEnum.NOTICE, "Client request to server ended.");

		// Provoking error to see all messages on console and in log files
		// executingContext.getNotificationManager().flushDump(executingContext);

		// Return
		return responseContainer;
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
	private void setErrorMessageOnSocketConnection(Context context, ResponseContainer responseContainer, String errorCode)
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
	 * Getter
	 */
	public String getClientPublicKey()
	{
		return clientPublicKey;
	}

	/**
	 * Getter
	 */
	public String getClientPrivateKey()
	{
		return clientPrivateKey;
	}

	/**
	 * Convert a request container to a raw socket data string, in order to send
	 * it to the server.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param executingContext
	 *            The context to use.
	 * 
	 * @param requestContainer
	 *            The request container to encode.
	 * 
	 * @param serverPublicKey
	 *            The public key of the server to use for encoding.
	 * 
	 * @param responseContainer
	 *            The response container to hold the return value if an error
	 *            occurs.
	 * 
	 * @return Returns the encoded string, or <TT>null</TT> if an error
	 *         occurred.
	 * 
	 */
	private String workstepConvertRequestContainerObjectToSocketData(Context executingContext, RequestContainer requestContainer, String serverPublicKey, ResponseContainer responseContainer)
	{
		// Validate parameter
		if (executingContext == null) return null;
		if (requestContainer == null) return null;
		if (responseContainer == null) return null;

		// Convert a request container
		String commandEncrypted = null;
		EncodingHandler encodingUitility = new EncodingHandler();

		try
		{
			commandEncrypted = encodingUitility.encodeRequestContainer(executingContext, requestContainer, serverPublicKey);
		}
		catch (Exception exception)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.getContext(), "Application", "ErrorOnProcessingRequestToServer");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			executingContext.getNotificationManager().notifyError(executingContext, resourceContainer, null, exception);
			this.setErrorMessageOnSocketConnection(executingContext, responseContainer, enumIdentifier);
		}

		// Return
		return commandEncrypted;
	}

	/**
	 * Open a socket connection to the server.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param executingContext
	 *            The context to use.
	 * 
	 * @param responseContainer
	 *            The response container to hold the return value if an error
	 *            occurs.
	 * 
	 * @return Returns the the socket handler object, or <TT>null</TT> if an
	 *         error occurred.
	 * 
	 */
	private SocketHandler workstepOpenSocketConnectionToServer(Context executingContext, ResponseContainer responseContainer)
	{
		// Validate parameter
		if (executingContext == null) return null;
		if (responseContainer == null) return null;

		// Open a socket connection
		SocketHandler socketHandler = null;

		try
		{
			socketHandler = new SocketHandler(executingContext, this.serverSocketHost, this.serverSocketPort, this.timeoutTimeInMilliseconds);

			if (socketHandler.openSocket() == false)
			{
				// Get resource container
				ResourceContainer resourceContainer = ResourceManager.notification(this.getContext(), "Application", "ErrorOnProcessingRequestToServer");
				String enumIdentifier = "";
				if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

				// Notify error
				String errorText = "--> on opening a socket connection to the server";
				executingContext.getNotificationManager().notifyError(executingContext, resourceContainer, errorText, null);
				this.setErrorMessageOnSocketConnection(executingContext, responseContainer, enumIdentifier);

				// Reset and return
				socketHandler = null;
				return null;
			}
		}
		catch (Exception exception)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.getContext(), "Application", "ErrorOnProcessingRequestToServer");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			String errorText = "--> on opening a socket connection to the server";
			executingContext.getNotificationManager().notifyError(executingContext, resourceContainer, errorText, exception);
			this.setErrorMessageOnSocketConnection(executingContext, responseContainer, enumIdentifier);

			// Return
			return null;
		}

		// Return
		return socketHandler;
	}

	/**
	 * Decode server data, read by socket, onto a response container object.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param executingContext
	 *            The context to use.
	 * 
	 * @param responseData
	 *            The raw text data read from socket, containing all information
	 *            to recreate the response container object, sent by the server.
	 * 
	 * @param parameterResponseContainer
	 *            The response container to work with.
	 * 
	 * @return Returns response container object, or <TT>null</TT> if an error
	 *         occurred.
	 */
	private ResponseContainer workstepConvertSocketDataToResponseContainer(Context executingContext, String responseData, ResponseContainer parameterResponseContainer)
	{
		// Validate parameter
		if (responseData == null) return parameterResponseContainer;
		if (parameterResponseContainer == null) return parameterResponseContainer;

		// Convert socket data onto response container object
		ResponseContainer newResponseContainer = null;

		try
		{
			EncodingHandler encodingUitility = new EncodingHandler();
			newResponseContainer = encodingUitility.decodeResponseContainer(executingContext, responseData, this.getClientPrivateKey());
		}
		catch (Exception exception)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.getContext(), "Application", "ErrorOnProcessingRequestToServer");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			String errorText = "--> on decoding response container";
			executingContext.getNotificationManager().notifyError(executingContext, resourceContainer, errorText, exception);
			this.setErrorMessageOnSocketConnection(executingContext, parameterResponseContainer, enumIdentifier);

			// Return
			return parameterResponseContainer;
		}

		if (newResponseContainer == null)
		{
			// Get resource container
			ResourceContainer resourceContainer = ResourceManager.notification(this.getContext(), "Application", "ErrorOnProcessingRequestToServer");
			String enumIdentifier = "";
			if (resourceContainer != null) enumIdentifier = resourceContainer.getRecourceIdentifier();

			// Notify error
			String errorText = "--> on decoding response container";
			executingContext.getNotificationManager().notifyError(executingContext, resourceContainer, errorText, null);
			this.setErrorMessageOnSocketConnection(executingContext, parameterResponseContainer, enumIdentifier);

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
			responseContainer.setCommandIdentifier(requestContainer.getCommandIdentifier());
		}
		catch (Exception exception)
		{
			// Be silent
			return;
		}

		// Return
		return;
	}

	/**
	 * Read the last used session identifier from the persistence manager.
	 */
	public String readLastKnownClientSessionIdentifier()
	{
		return this.getContext().getLocaldataManager().readProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "ClientSessionIdentifier"), "");
	}

	/**
	 * Save the currently used session identifier to the persistence manager.
	 */
	public void saveLastKnownClientSessionIdentifier(String clientSessionIdentifier)
	{
		this.getContext().getLocaldataManager().writeProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "ClientSessionIdentifier"), clientSessionIdentifier);
	}

	/**
	 * Read the last known "Host" the client was connected to from the
	 * persistance manager.
	 */
	public String readLastKnownHost()
	{
		return this.getContext().getLocaldataManager().readProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "Host"), "");
	}

	/**
	 * Save the last known "Host" the client was connected to to the persistence
	 * manager.
	 */
	public void saveLastKnownHost(String host)
	{
		this.getContext().getLocaldataManager().writeProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "Host"), host);
	}

	/**
	 * Read the last known "Port" the client was connected to from the
	 * persistence manager.
	 */
	public int readLastKnownPort()
	{
		return this.getContext().getLocaldataManager().readPropertyAsIntegerValue(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "Port"), 0);
	}

	/**
	 * Save the last known "Port" the client was connected to to the persistence
	 * manager.
	 */
	public void saveLastKnownPort(int port)
	{
		this.getContext().getLocaldataManager().writeProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "Port"), String.valueOf(port));
	}

	/**
	 * Read the last used public key of the server from the persistence manager.
	 */
	public String readLastKnownServerPublicKey()
	{
		return this.getContext().getLocaldataManager().readProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "ServerPublicKey"), "");
	}

	/**
	 * Save the currently used public key of the server to the persistance
	 * manager.
	 */
	public void saveLastKnownServerPublicKey(String ServerPublicKey)
	{
		this.getContext().getLocaldataManager().writeProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "ServerPublicKey"), ServerPublicKey);
	}
}
