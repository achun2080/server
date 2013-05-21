package fmagic.client.application;

import fmagic.basic.application.ApplicationManager;
import fmagic.basic.command.ConnectionContainer;
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
	// Connection container with actual connection settings to the application
	// server the client deals with
	private ConnectionContainer connectionContainer = null;

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
	public void setSocketConnectionParameter(String serverSocketHost, int serverSocketPort)
	{
		this.connectionContainer = new ConnectionContainer(0, serverSocketHost, serverSocketPort);
		this.connectionContainer.setKeyApplicationPrivateKey(this.getKeyApplicationPrivateKey());
		this.connectionContainer.setSessionIdentifier(this.getClientSessionIdentifier());
		
		this.saveLastKnownConnectionData();
		
		this.connectionContainer.establishConnection(this.getContext());
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

		// Read last known connection data
		this.readLastKnownConnectionData();

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

		// Save last known connection data
		this.saveLastKnownConnectionData();

		// Release all resources
		this.releaseResources();

		// Fire Event
		this.getContext().getNotificationManager().notifyEvent(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ClientStopped"), null, null);

		// Return
		return;
	}

	/**
	 * Read all connection data that were used during the last known connection
	 * of this client to an application server.
	 * <p>
	 * The data directly are stored into the client <TT>connection container</TT>.
	 */
	private void readLastKnownConnectionData()
	{
		try
		{
			// Read data from local data
			String host = this.getContext().getLocaldataManager().readProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "Host"), "");
			int port = this.getContext().getLocaldataManager().readPropertyAsIntegerValue(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "Port"), 0);
			String serverPublicKey = this.getContext().getLocaldataManager().readProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "ServerPublicKey"), "");
			String clientSessionIdentifier = this.getContext().getLocaldataManager().readProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "ClientSessionIdentifier"), "");

			// Create a new client session identifier if not available yet
			if (clientSessionIdentifier == null || clientSessionIdentifier.length() == 0) clientSessionIdentifier = ConnectionContainer.createClientSessionIdentifier();

			// Create a new connection container with the read data
			this.connectionContainer = new ConnectionContainer(0, host, port, this.getKeyApplicationPrivateKey(), serverPublicKey);
			this.connectionContainer.setSessionIdentifier(clientSessionIdentifier);
			
			// Try to establish the connection automatically, if all settings are available
			while(true)
			{
				if (host == null || host.length() == 0) break;
				if (port <= 0) break;
				if (serverPublicKey == null || serverPublicKey.length() == 0) break;
				
				this.connectionContainer.establishConnection(this.getContext());
				
				break;
			}
		}
		catch (Exception e)
		{
			// Be silent
		}
	}

	/**
	 * Save all connection data that were used during the last known connection
	 * of this client to an application server.
	 * <p>
	 * The data directly are read from the client <TT>connection container</TT>.
	 */
	private void saveLastKnownConnectionData()
	{
		try
		{
			// Save data to local data
			this.getContext().getLocaldataManager().writeProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "Host"), this.connectionContainer.getHost());
			this.getContext().getLocaldataManager().writeProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "Port"), String.valueOf(this.connectionContainer.getPort()));
			this.getContext().getLocaldataManager().writeProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "ServerPublicKey"), this.connectionContainer.getKeyRemotePublicKey());
			this.getContext().getLocaldataManager().writeProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "ClientSessionIdentifier"), this.connectionContainer.getSessionIdentifier());
		}
		catch (Exception e)
		{
			// Be silent
		}
	}

	/**
	 * Get the last known client session identifier from local data and set it
	 * as the current session identifier.
	 * <p>
	 * If no valid client session identifier was found, a new one will be
	 * created.
	 * 
	 * @return Returns the client session identifier read resp. created, or
	 *         <TT>null</TT> if an error occurred..
	 */
	private String getClientSessionIdentifier()
	{
		try
		{
			// Get last known client session identifier from local data
			String clientSessionIdentifier = this.getContext().getLocaldataManager().readProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "ClientSessionIdentifier"), "");

			// Create a new identifier and save it to the local data
			if (clientSessionIdentifier == null || clientSessionIdentifier.equals(""))
			{
				clientSessionIdentifier = ConnectionContainer.createClientSessionIdentifier();
				this.getContext().getLocaldataManager().writeProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "ClientSessionIdentifier"), clientSessionIdentifier);
			}

			// Return
			return clientSessionIdentifier;
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnProcessingCommandOnClient"), null, e);
			return null;
		}
	}

	/**
	 * Getter
	 */
	public ConnectionContainer getConnectionContainer()
	{
		return connectionContainer;
	}
}
