package fmagic.server;

import java.net.ServerSocket;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import fmagic.basic.ApplicationManager;
import fmagic.basic.ApplicationServer;
import fmagic.basic.NotificationManager;
import fmagic.basic.ResourceManager;
import fmagic.basic.SessionContainer;
import fmagic.basic.FileUtilFunctions;
import fmagic.basic.ResourceContainer.OriginEnum;

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
	final private int serverSocketPort;

	// Socket data
	private ServerSocket serverSocket = null;
	private int timeoutTimeInMilliseconds = 10000;

	// Encoding data
	private String serverPublicKey = null;
	private String serverPrivateKey = null;

	// Thread pool
	private ExecutorService executorService = null;

	// Sessions
	final private HashMap<String, SessionContainer> sessions = new HashMap<String, SessionContainer>();
	private Integer maxNuOfActiveSessions = null;
	private Integer percentageRateOfSessionsToClean = null;

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
	 *            Is to be set to the name of the test session, if the application
	 *            is running in test mode, or <TT>null</TT> if the application
	 *            is running in productive mode.
	 */
	protected ServerManager(
			ApplicationManager.ApplicationIdentifierEnum applicationIdentifier,
			int applicationVersion, String codeName, int serverSocketPort,
			int timeoutTimeInMilliseconds, boolean runningInTestMode, String testCaseName, String testSessionName)
	{
		// Instantiate super class
		super(applicationIdentifier, applicationVersion, codeName, OriginEnum.Server, runningInTestMode, testCaseName, testSessionName);

		// Adopt constructor data
		this.serverSocketPort = serverSocketPort;
		this.timeoutTimeInMilliseconds = timeoutTimeInMilliseconds;
	}

	@Override
	public boolean readSecurityKeys()
	{
		// Initialize
		boolean isSuccessful = true;

		// Read and check
		try
		{
			this.serverPublicKey = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(getContext(), "Application", "PublicKey"), "", true);
			if (this.serverPublicKey == null || this.serverPublicKey.length() == 0) isSuccessful = false;

			this.serverPrivateKey = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(getContext(), "Application", "PrivateKey"), "", true);
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
		// Create Thread pool
		this.executorService = Executors.newCachedThreadPool();
		

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
		try
		{
			if (this.executorService != null)
			{
				this.executorService.shutdown();
				this.executorService.awaitTermination(60, TimeUnit.SECONDS);
				this.executorService.shutdownNow();
			}
		}
		catch (InterruptedException e)
		{
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnShutdownThreadPool"), null, e);
		}

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
		FileUtilFunctions.waitForThreadTerminating(this.applicationServer, 60);

		// Release WATCHDOG
		this.releaseWatchdog();
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
		// Get percentage rate of sessions to clean
		if (this.percentageRateOfSessionsToClean == null) this.percentageRateOfSessionsToClean = this.getContext().getConfigurationManager().getPropertyAsIntegerValue(this.getContext(), ResourceManager.configuration(this.getContext(), "Session", "PercentageRateForCleaning"), 10, false);
		if (percentageRateOfSessionsToClean > 100) percentageRateOfSessionsToClean = 100;
		int numberOfSessions = this.sessions.size();
		int numberOfSessionsToClean = (int) ((double) numberOfSessions * (double) percentageRateOfSessionsToClean / 100.0);

		// Validate parameter
		if (percentageRateOfSessionsToClean == 0) return false;
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
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnCleaningSessionList"), null, e);
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
		// Validate data
		String clientSessionIdentifierToAdd = clientSessionIdentifier.trim();

		// Check if session already exists on the server
		SessionContainer session = this.sessions.get(clientSessionIdentifierToAdd.trim());
		if (session != null) return false;

		// Check max number of sessions
		if (this.maxNuOfActiveSessions == null) this.maxNuOfActiveSessions = this.getContext().getConfigurationManager().getPropertyAsIntegerValue(this.getContext(), ResourceManager.configuration(this.getContext(), "Session", "MaxNuOfActiveSessions"), 1000, false);

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
		// Shutdown all running command threads
		try
		{
			if (this.executorService != null) this.executorService.shutdown();
		}
		catch (Exception e)
		{
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnShutdownThreadPool"), null, e);
		}

		// Close server socket
		try
		{
			if (this.serverSocket != null) this.serverSocket.close();
		}
		catch (Exception e)
		{
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Application", "ErrorOnServerSocket"), "--> on closing server socket port: '" + String.valueOf(this.getServerSocketPort()) + "'", e);
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
	public int getTimeoutTimeInMilliseconds()
	{
		return timeoutTimeInMilliseconds;
	}

	/**
	 * Getter
	 */
	public String getServerPublicKey()
	{
		return serverPublicKey;
	}

	/**
	 * Getter
	 */
	public String getServerPrivateKey()
	{
		return serverPrivateKey;
	}

	/**
	 * Getter
	 */
	public ExecutorService getExecutorService()
	{
		return executorService;
	}
}
