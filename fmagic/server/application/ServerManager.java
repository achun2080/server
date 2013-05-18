package fmagic.server.application;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import fmagic.basic.application.ApplicationManager;
import fmagic.basic.application.ApplicationServer;
import fmagic.basic.command.SessionContainer;
import fmagic.basic.context.Context;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceContainer.OriginEnum;
import fmagic.basic.resource.ResourceManager;
import fmagic.server.media.ServerMediaManager;
import fmagic.server.media.ServerMediaServer;
import fmagic.server.watchdog.WatchdogManager;
import fmagic.server.watchdog.WatchdogServer;

/**
 * This class implements common functions of the application servers of the
 * FMAGIC system.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 17.01.2012 - Created
 * 
 */
public abstract class ServerManager extends ApplicationManager
{
	// Specific data
	private final int serverSocketPort;

	// Socket data
	private ServerSocket serverSocket = null;
	private int socketTimeoutInMilliseconds = 120000;

	// Encoding data
	private String serverPublicKey = null;
	private String serverPrivateKey = null;

	// Thread pool
	private final List<Thread> threadPool = new ArrayList<Thread>();

	// Session configuration
	private final HashMap<String, SessionContainer> sessions = new HashMap<String, SessionContainer>();
	private Integer maxNuOfActiveSessions = null;
	private Integer percentageRateForCleaning = null;

	/**
	 * Constructor
	 * 
	 * @param applicationIdentifier
	 *            Identifier of the application.
	 * 
	 * @param applicationVersion
	 *            Software version of the application.
	 * 
	 * @param codeName
	 *            Code name of the application.
	 * 
	 * @param serverSocketPort
	 *            Port number of server socket to use by the server application.
	 * 
	 * @param timeoutTimeInMilliseconds
	 *            Timeout to use as a socket connection timeout.
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
	protected ServerManager(
			ApplicationManager.ApplicationIdentifierEnum applicationIdentifier,
			int applicationVersion, String codeName, int serverSocketPort,
			boolean runningInTestMode, String testCaseName,
			String testSessionName)
	{
		// Instantiate super class
		super(applicationIdentifier, applicationVersion, codeName, OriginEnum.Server, runningInTestMode, testCaseName, testSessionName);

		// Adopt constructor data
		this.serverSocketPort = serverSocketPort;
	}

	@Override
	protected void initialize()
	{
		boolean isError = false;

		if (this.initializeWatchdogManager() == true) isError = true;
		if (this.initializeCriticalPath() == true) isError = true;
		if (this.initializeIntegratedServer() == true) isError = true;

		// Initiate shutdown if an error occurred
		if (isError == true)
		{
			this.shutdown();
			this.releaseWatchdog();
		}

		// Go back to the tracking context after initializing application server
		this.context = this.getContext().createTrackingContext(ResourceManager.context(this.getContext(), "Overall", "Tracking"));
	}

	/**
	 * Creates and initialize the WATCHDOG Manager.
	 * 
	 * @return Returns <TT>true</TT> if an error occurred, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean initializeWatchdogManager()
	{
		try
		{
			WatchdogManager watchdogManager = new WatchdogManager(this.getContext());
			this.getContext().setWatchdogManager(watchdogManager);

			return false;
		}
		catch (Exception e)
		{
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnStartingServer"), null, e);
			return true;
		}
	}

	/**
	 * Creates and initialize all integrated servers.
	 * 
	 * @return Returns <TT>true</TT> if an error occurred, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean initializeIntegratedServer()
	{
		boolean isError = false;

		try
		{
			// Start WATCHDOG
			this.watchdogServer = new WatchdogServer(this.getContext(), this.getContext().getWatchdogManager());
			if (watchdogServer.startServer(this.getContext()) == false) isError = true;

			// Start media server
			this.mediaServer = new ServerMediaServer(this.getContext(), (ServerMediaManager) this.getContext().getMediaManager());
			if (this.mediaServer.startServer(this.getContext()) == false) isError = true;
		}
		catch (Exception e)
		{
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnStartingServer"), null, e);
			isError = true;
		}

		// Return
		return isError;
	}

	/**
	 * Release all integrated server.
	 * 
	 * @return Returns <TT>true</TT> if an error occurred, otherwise
	 *         <TT>false</TT>.
	 */
	private void releaseIntegratedServer()
	{
		// Stop media server
		try
		{
			if (this.mediaServer != null)
			{
				this.mediaServer.stopServer(this.getContext());
			}
		}
		catch (Exception e)
		{
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "MediaServer", "ErrorOnStoppingServer"), null, e);
		}
	}

	@Override
	public boolean readConfiguration(Context context)
	{
		if (super.readConfiguration(context) == true) return true;

		try
		{
			// Read parameter: MaxNuOfActiveSessions
			this.maxNuOfActiveSessions = context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "Session", "MaxNuOfActiveSessions"), false);

			// Read parameter: PercentageRateForCleaning
			this.percentageRateForCleaning = context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "Session", "PercentageRateForCleaning"), false);

			// Read parameter: SocketTimeoutInMilliseconds
			this.socketTimeoutInMilliseconds = context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "Application", "SocketTimeoutInMilliseconds"), false);

			// Return
			return false;
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), null, e);
			return true;
		}
	}

	@Override
	public boolean readSecurityKeys()
	{
		// Initialize
		boolean isSuccessful = true;

		// Read and check
		try
		{
			this.serverPublicKey = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(getContext(), "Application", "PublicKey"), true);
			if (this.serverPublicKey == null || this.serverPublicKey.length() == 0) isSuccessful = false;

			this.serverPrivateKey = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(getContext(), "Application", "PrivateKey"), true);
			if (this.serverPrivateKey == null || this.serverPrivateKey.length() == 0) isSuccessful = false;
		}
		catch (Exception e)
		{
			return false;
		}

		// Return
		return isSuccessful;
	}

	@Override
	protected boolean bindResources()
	{
		// Open server socket
		try
		{
			// Open server socket
			this.serverSocket = new ServerSocket(this.getServerSocketPort());
		}
		catch (Exception e)
		{
			this.getContext().getNotificationManager().notifyError(getContext(), ResourceManager.notification(getContext(), "Application", "ErrorOnServerSocket"), "--> on opening server socket port: '" + String.valueOf(this.getServerSocketPort()) + "'", e);
			return false;
		}

		// Return
		return true;
	}

	@Override
	public boolean startApplication()
	{
		// Check shutdown flag
		if (this.isShutdown() == true) return false;

		// Logging
		this.getContext().getNotificationManager().notifyLogMessage(this.getContext(), NotificationManager.SystemLogLevelEnum.NOTICE, "Starting application server [" + this.getCodeName() + "]: " + this.toString());

		// Bind all resources
		if (this.bindResources() == false) return false;

		// Instantiate Application server and start it
		this.applicationServer = new ApplicationServer(this.getContext(), this);
		this.applicationServer.startServer(this.getContext());

		// Create event: ConfigurationSettingsNotification
		String configurationText = this.getContext().getConfigurationManager().printConfigurationSettings(this.getContext());
		this.getContext().getNotificationManager().notifyEvent(this.getContext(), ResourceManager.notification(this.getContext(), "Configuration", "ConfigurationSettingsNotification"), configurationText, null);

		// Return
		return true;
	}

	@Override
	public void stopApplication()
	{
		// Logging
		this.getContext().getNotificationManager().notifyLogMessage(this.getContext(), NotificationManager.SystemLogLevelEnum.NOTICE, "Stopping application server [" + this.getCodeName() + "]: " + this.toString());

		// Set stop running flag
		this.setStopRunning(true);

		// Let the server some time to end running client requests
		this.threadPoolShutDown();

		// Release all resources
		this.releaseResources();

		// Stop application server
		try
		{
			this.getContext().getNotificationManager().notifyEvent(this.getContext(), ResourceManager.notification(getContext(), "Application", "ApplicationServerInterrupted"), null, null);
			this.applicationServer.interrupt();
		}
		catch (Exception e)
		{
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnStoppingServer"), null, e);
		}

		// Wait till end of thread
		FileUtilFunctions.generalWaitForThreadTerminating(this.applicationServer, 60);

		// Release WATCHDOG
		this.releaseWatchdog();
	}

	/**
	 * Release WATCHDOG separately because of it should be notified even the
	 * last messages of the application.
	 */
	private void releaseWatchdog()
	{
		// Stop WATCHDOG
		try
		{
			if (this.watchdogServer != null)
			{
				this.watchdogServer.stopServer(this.getContext());
			}
		}
		catch (Exception e)
		{
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Watchdog", "ErrorOnStoppingServer"), null, e);
		}
	}

	@Override
	public boolean validateResources(Context context)
	{
		return false;
	}

	@Override
	public boolean cleanEnvironment(Context context)
	{
		return false;
	}

	/**
	 * Check if a client session is already known on the server.
	 * <p>
	 * If it is known the modification date will be updated to the current
	 * date/time automatically.
	 * 
	 * @param clientSessionIdentifier
	 *            The identifier of the client session.
	 * 
	 * @return Returns <TT>true</TT> if the client session identifier exists,
	 *         otherwise <TT>false</TT>.
	 */
	public synchronized boolean sessionCheckClientSession(String clientSessionIdentifier)
	{
		// Validate data
		String clientSessionIdentifierNormalized = clientSessionIdentifier.trim();

		// Check if session already exists on the server
		SessionContainer session = this.sessions.get(clientSessionIdentifierNormalized);
		if (session == null) return false;

		// Update modification date of the session
		session.setLastModificationDate(new Date());
		this.sessions.put(clientSessionIdentifierNormalized, session);

		// Return
		return true;
	}

	/**
	 * Get a client session object from the list of client sessions the server
	 * holds.
	 * <p>
	 * If the session exists the modification date will be updated to the
	 * current date/time automatically.
	 * 
	 * @param clientSessionIdentifier
	 *            The identifier of the client session.
	 * 
	 * @return Returns the requested session object, or <TT>null</TT> if the
	 *         client session identifier was not found in the list.
	 */
	public synchronized SessionContainer sessionGetClientSession(String clientSessionIdentifier)
	{
		// Initialize data
		SessionContainer session = null;

		// Validate data
		String clientSessionIdentifierNormalized = clientSessionIdentifier.trim();

		// Check if session already exists on the server
		session = this.sessions.get(clientSessionIdentifierNormalized);
		if (session == null) return null;

		// Update modification date of the session
		session.setLastModificationDate(new Date());
		this.sessions.put(clientSessionIdentifierNormalized, session);

		// Return
		return session;
	}

	/**
	 * Clean an appointed percentage rate of older client sessions from the list
	 * of sessions.
	 * <p>
	 * As basis of comparison the last modification date is taken.
	 * <p>
	 * The percentage rate is read from the configuration file. If there is no
	 * value found the default value will be set to 10 percent.
	 * 
	 * @return Returns <TT>true</TT> if at least one client session was deleted,
	 *         otherwise <TT>false</TT>.
	 */
	public synchronized boolean sessionCleanClientSessionList()
	{
		// Validate parameter
		if (this.percentageRateForCleaning == null)
		{
			String errorString = "--> Configuration parameter 'Session/PercentageRateForCleaning' is not defined";
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnHandlingSessionList"), errorString, null);
			return false;
		}

		// Get percentage rate of sessions to clean
		int numberOfSessions = this.sessions.size();
		int numberOfSessionsToClean = 0;

		try
		{
			numberOfSessionsToClean = (int) ((double) numberOfSessions * (double) this.percentageRateForCleaning / 100.0);
		}
		catch (Exception e)
		{
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnHandlingSessionList"), null, e);
			return false;
		}

		// Validate parameter
		if (numberOfSessionsToClean == 0) return false;
		if (numberOfSessions == 0) return false;
		if (numberOfSessionsToClean == 0) return false;
		if (numberOfSessionsToClean > numberOfSessions) numberOfSessionsToClean = numberOfSessions;

		// Create a list of sorted sessions sorted by modification date/time
		SortedMap<String, String> sortedSessions = new TreeMap<String, String>();

		for (SessionContainer session : this.sessions.values())
		{
			sortedSessions.put(String.valueOf(session.getLastModificationDate().getTime()), session.getClientSessionIdentifier());
		}

		// Delete the first x sessions of the session list
		try
		{
			Iterator<String> iterator = sortedSessions.keySet().iterator();

			for (int i = 0; i < numberOfSessionsToClean; i++)
			{
				String modificationDateTimeString = iterator.next();
				String clientSessionIdentifier = sortedSessions.get(modificationDateTimeString);
				this.sessions.remove(clientSessionIdentifier);
			}
		}
		catch (Exception e)
		{
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnHandlingSessionList"), null, e);
			return false;
		}

		// Fire an event
		this.getContext().getNotificationManager().notifyEvent(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "SessionCleaned"), "--> Number of deleted sessions: '" + String.valueOf(numberOfSessionsToClean) + "'\n--> Number of active sessions now: '" + this.sessions.size() + "'", null);

		// Return
		return true;
	}

	/**
	 * Add a client session to the server.
	 * <p>
	 * The max number of allowed sessions is read from the configuration file.
	 * If there is no value found the default value will be set to 1000.
	 * 
	 * @param clientSessionIdentifier
	 *            The identifier of the client session.
	 * 
	 * @param clientPublicKey
	 *            The public key of the client.
	 * 
	 * @return Returns <TT>true</TT> if the client session could be added and
	 *         did not exist before, otherwise <TT>false</TT>.
	 */
	public synchronized boolean sessionAddClientSession(String clientSessionIdentifier, String clientPublicKey)
	{
		// Validate parameter
		if (this.maxNuOfActiveSessions == null)
		{
			String errorString = "--> Configuration parameter 'Session/MaxNuOfActiveSessions' is not defined";
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnHandlingSessionList"), errorString, null);
			this.maxNuOfActiveSessions = 5000;
		}

		// Validate data
		String clientSessionIdentifierToAdd = clientSessionIdentifier.trim();

		// Check if session already exists on the server
		SessionContainer session = this.sessions.get(clientSessionIdentifierToAdd);

		if (session != null) { return false; }

		if (this.sessions.size() >= this.maxNuOfActiveSessions)
		{
			// Fire an event
			this.getContext().getNotificationManager().notifyEvent(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "MaximumNumberOfSessionsExceeded"), "--> Max nu of sessions: '" + String.valueOf(this.maxNuOfActiveSessions) + "'", null);

			// Clear session list
			this.sessionCleanClientSessionList();
		}

		// Create and add a new session to the server
		session = new SessionContainer(clientSessionIdentifierToAdd, clientPublicKey);
		this.sessions.put(clientSessionIdentifierToAdd, session);

		// Return
		return true;
	}

	@Override
	protected void releaseResources()
	{
		// Shutdown all running command threads.
		// Let the server some time to end running client requests.
		this.threadPoolShutDown();

		// Close server socket
		try
		{
			if (this.serverSocket != null) this.serverSocket.close();
		}
		catch (Exception e)
		{
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnServerSocket"), "--> on closing server socket port: '" + String.valueOf(this.getServerSocketPort()) + "'", e);
		}

		// Release integrated server
		this.releaseIntegratedServer();
	}

	/**
	 * Wait for the end of all treads of a thread list.
	 */
	public void threadPoolShutDown()
	{
		// Validate parameter
		if (this.getMaximumWaitingTimeForPendingThreadsInSeconds() == null)
		{
			String errorString = "--> Configuration parameter 'Shutdown/MaximumWaitingTimeForPendingThreadsInSeconds' is not defined";
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnShutdownThreadPool"), errorString, null);
			this.maximumWaitingTimeForPendingThreadsInSeconds = 120;
		}

		// Check parameter
		if (this.threadPool == null) return;
		if (this.threadPool.size() == 0) return;

		for (Thread thread : this.threadPool)
		{
			try
			{
				FileUtilFunctions.generalWaitForThreadTerminating(thread, this.getMaximumWaitingTimeForPendingThreadsInSeconds());
			}
			catch (Exception exception)
			{
				// Be silent
			}
		}
	}

	/**
	 * Create and start a new thread within the thread pool.
	 * 
	 * @param runable
	 *            The object to start within a new thread.
	 */
	public void threadPoolExecuteNewThread(Runnable runnable)
	{
		if (this.threadPool == null) return;

		// Remove an expired thread from the list of threads
		try
		{
			for (Thread thread : new ArrayList<Thread>(this.threadPool))
			{
				if (!thread.isAlive())
				{
					this.threadPool.remove(thread);
				}
			}
		}
		catch (Exception exception)
		{
			String errorText = "--> Error on removing expired threads from thread pool";
			errorText += "\n--> Actual number of threads in pool: '" + String.valueOf(this.threadPool.size()) + "'";
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnServerSocket"), errorText, exception);
		}

		// Start new thread
		try
		{
			Thread newTread = new Thread(runnable);
			this.threadPool.add(newTread);
			newTread.start();
		}
		catch (Exception exception)
		{
			String errorText = "--> Error on starting new thread within the thread pool";
			errorText += "\n--> Actual number of threads in pool: '" + String.valueOf(this.threadPool.size()) + "'";
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnServerSocket"), errorText, exception);
		}
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
	public ServerSocket getServerSocket()
	{
		return serverSocket;
	}

	/**
	 * Getter
	 */
	public int getSocketTimeoutInMilliseconds()
	{
		return socketTimeoutInMilliseconds;
	}

	/**
	 * Get server public key.
	 */
	public String getServerPublicKey()
	{
		return serverPublicKey;
	}

	/**
	 * Get server private key.
	 */
	public String getServerPrivateKey()
	{
		return serverPrivateKey;
	}
}
