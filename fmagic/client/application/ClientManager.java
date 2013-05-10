package fmagic.client.application;

import java.net.SocketTimeoutException;

import fmagic.basic.application.ApplicationManager;
import fmagic.basic.command.EncodingHandler;
import fmagic.basic.command.RequestContainer;
import fmagic.basic.command.ResponseContainer;
import fmagic.basic.command.SocketHandler;
import fmagic.basic.context.Context;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceContainer.OriginEnum;
import fmagic.basic.resource.ResourceManager;

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
	 * 
	 * @param testSessionName
	 *            Is to be set to the name of the test session, if the application
	 *            is running in test mode, or <TT>null</TT> if the application
	 *            is running in productive mode.
	 */
	protected ClientManager(
			ApplicationManager.ApplicationIdentifierEnum applicationIdentifier,
			int applicationVersion, String codeName, boolean runningInTestMode,
			String testCaseName, String testSessionName)
	{
		// Instantiate super class
		super(applicationIdentifier, applicationVersion, codeName, OriginEnum.Client, runningInTestMode, testCaseName, testSessionName);
	}

	@Override
	public boolean readSecurityKeys()
	{
		// Initialize
		boolean isSuccessful = true;

		// Read and check
		try
		{
			this.clientPublicKey = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(this.getContext(), "Application", "PublicKey"), true);
			if (this.clientPublicKey == null || this.clientPublicKey.length() == 0) isSuccessful = false;

			this.clientPrivateKey = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(this.getContext(), "Application", "PrivateKey"), true);
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
		ResponseContainer responseContainer = new ResponseContainer(null, 0, null);

		// Transfer some client request data to the server response data
		this.workstepTransferContainerData(requestContainer, responseContainer);

		// Get public key of the server
		String serverPublicKey = null;

		// Encode request container
		StringBuffer commandEncrypted = workstepConvertRequestContainerObjectToSocketData(executingContext, requestContainer, serverPublicKey, responseContainer);

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
				String errorText = "--> Timeout value: '" + socketHandler.getTimeoutTimeInMilliseconds() + "' Milliseconds";
				responseContainer.notifyError(executingContext, "Application", "SocketTimeout", errorText, socketTimeoutException);
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
	private StringBuffer workstepConvertRequestContainerObjectToSocketData(Context executingContext, RequestContainer requestContainer, String serverPublicKey, ResponseContainer responseContainer)
	{
		// Validate parameter
		if (executingContext == null) return null;
		if (requestContainer == null) return null;
		if (responseContainer == null) return null;

		// Convert a request container
		StringBuffer commandEncrypted = null;
		EncodingHandler encodingUitility = new EncodingHandler();

		try
		{
			commandEncrypted = encodingUitility.encodeRequestContainer(executingContext, requestContainer, serverPublicKey);
		}
		catch (Exception exception)
		{
			responseContainer.notifyError(executingContext, "Application", "ErrorOnProcessingRequestToServer", null, exception);
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
				String errorText = "--> Error on opening a socket connection to the server";
				responseContainer.notifyError(executingContext, "Application", "ErrorOnProcessingRequestToServer", errorText, null);

				// Reset and return
				socketHandler = null;
				return null;
			}
		}
		catch (Exception exception)
		{
			String errorText = "--> Error on opening a socket connection to the server";
			responseContainer.notifyError(executingContext, "Application", "ErrorOnProcessingRequestToServer", errorText, exception);
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
			String errorText = "--> Error on decoding response container";
			parameterResponseContainer.notifyError(executingContext, "Application", "ErrorOnProcessingRequestToServer", errorText, exception);
			return parameterResponseContainer;
		}

		if (newResponseContainer == null)
		{
			String errorText = "--> Error on decoding response container";
			parameterResponseContainer.notifyError(executingContext, "Application", "ErrorOnProcessingRequestToServer", errorText, null);
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
