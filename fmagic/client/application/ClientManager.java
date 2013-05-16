package fmagic.client.application;

import fmagic.basic.application.ApplicationManager;
import fmagic.basic.command.RequestContainer;
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
	 *            Is to be set to the name of the test session, if the
	 *            application is running in test mode, or <TT>null</TT> if the
	 *            application is running in productive mode.
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
	protected void initialize()
	{
		boolean isError = false;

		if (this.initializeCriticalPath() == true) isError = true;

		// Initiate shutdown if an error occurred
		if (isError == true)
		{
			this.shutdown();
		}

		// Go back to the tracking context after initializing application server
		this.context = this.getContext().createTrackingContext(ResourceManager.context(this.getContext(), "Overall", "Tracking"));
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

		// Return
		return;
	}
	/**
	 * Get the last known client session identifier from local data and set it
	 * as the current session identifier.
	 * <p>
	 * If no valid client session identifier was found, a new one will be
	 * created.
	 */
	public void setClientSessionIdentifier(RequestContainer requestContainer)
	{
		// Validate parameter
		if (requestContainer == null) return;

		// Process
		try
		{
			// Get last known client session identifier from local data
			String clientSessionIdentifier = this.readLastKnownClientSessionIdentifier();

			// Create a new identifier and save it to the local data
			if (clientSessionIdentifier == null || clientSessionIdentifier.equals(""))
			{
				clientSessionIdentifier = requestContainer.createClientSessionIdentifier();
				this.saveLastKnownClientSessionIdentifier(clientSessionIdentifier);
				return;
			}

			// Set known the known identifier
			requestContainer.setClientSessionIdentifier(clientSessionIdentifier);
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnProcessingCommandOnClient"), null, e);
		}
	}

	/**
	 * Create a new client session identifier and save it to local data.
	 */
	public void resetClientSessionIdentifier(RequestContainer requestContainer)
	{
		// Validate parameter
		if (requestContainer == null) return;

		// Process
		try
		{
			// Create a new identifier
			String clientSessionIdentifier = requestContainer.createClientSessionIdentifier();

			// Save the identifier to the persistence manager
			this.saveLastKnownClientSessionIdentifier(clientSessionIdentifier);

			// Set the known identifier on the request container
			requestContainer.setClientSessionIdentifier(clientSessionIdentifier);
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnProcessingCommandOnClient"), null, e);
		}
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
	 * Getter
	 */
	public int getTimeoutTimeInMilliseconds()
	{
		return timeoutTimeInMilliseconds;
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
