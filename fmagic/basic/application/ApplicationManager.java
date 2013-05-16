package fmagic.basic.application;

import java.util.HashMap;

import fmagic.basic.context.Context;
import fmagic.basic.file.FileLocationFunctions;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceContainer.OriginEnum;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.application.ClientManager;
import fmagic.client.context.ClientContext;
import fmagic.server.application.ServerManager;
import fmagic.server.context.ServerContext;
import fmagic.server.media.ServerMediaPoolServer;
import fmagic.server.watchdog.WatchdogServer;
import fmagic.test.application.TestManager;

/**
 * This class implements common data needed to organize client/server FMAGIC
 * applications.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public abstract class ApplicationManager implements ManagerInterface
{
	// Application Server Instance
	protected ApplicationServer applicationServer = null;
	final private ApplicationManager.ApplicationIdentifierEnum applicationIdentifier;
	final private int applicationVersion;
	final private String codeName;
	final private boolean serverApplication;
	final private boolean clientApplication;

	// Sets context of server
	protected Context context;

	// Flag if processing is to be continued
	private boolean stopRunning = false;

	// Language data
	private HashMap<String, String> supportedLanguages = new HashMap<String, String>();
	private String mainLanguage = null;
	private String secondaryLanguage = null;
	private boolean notifyLabelEvents = true;

	// Integrated server
	protected WatchdogServer watchdogServer;
	protected ServerMediaPoolServer mediaServer;

	// Shut down flag: If this flag is set, the server was not started because
	// of severe errors during initialization
	private boolean shutdown = false;

	// Shut down configuration
	protected Integer maximumWaitingTimeForPendingThreadsInSeconds = null;

	// Application codes
	public static enum ApplicationIdentifierEnum
	{
		Basic,
		Common,
		Extension,
		Test,
		ReferenceApplication,
		SeniorCitizenXXX,
		SteriManagement
	}

	/**
	 * Constructor
	 * 
	 * @param applicationIdentifier
	 *            Identifier of the application.
	 * 
	 * @param applicationVersion
	 *            Software version of the application server.
	 * 
	 * @param codeName
	 *            Code name of the application.
	 * 
	 * @param origin
	 *            Origin "Server" or "Client".
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
	protected ApplicationManager(
			ApplicationManager.ApplicationIdentifierEnum applicationIdentifier,
			int applicationVersion, String codeName, OriginEnum origin,
			boolean runningInTestMode, String testCaseName,
			String testSessionName)
	{
		// Create default context
		if (origin.toString().equals(OriginEnum.Server.toString()))
		{
			this.context = new ServerContext(codeName, applicationIdentifier.toString(), applicationVersion, (ServerManager) this, runningInTestMode, testCaseName, testSessionName);
			this.serverApplication = true;
			this.clientApplication = false;
		}
		else
		{
			this.context = new ClientContext(codeName, applicationIdentifier.toString(), applicationVersion, (ClientManager) this, runningInTestMode, testCaseName, testSessionName);
			this.serverApplication = false;
			this.clientApplication = true;
		}

		// Adopt constructor data
		this.applicationIdentifier = applicationIdentifier;
		this.codeName = codeName;
		this.applicationVersion = applicationVersion;

		// Create a silent dump context for initializing application server
		ResourceContainer contextResourceContainer = this.context.getResourceManager().getProvisionalResourceContainer("Context.Common.All.Identifier.Overall.Initialization");
		this.context = this.context.createSilentDumpContext(contextResourceContainer);
	}

	/**
	 * Creates all resources needed by an application.
	 * 
	 * @return Returns <TT>true</TT> if an error occurred, otherwise
	 *         <TT>false</TT>.
	 */
	protected abstract void initialize();

	/**
	 * Creates all resources needed by an application.
	 * 
	 * @return Returns <TT>true</TT> if an error occurred, otherwise
	 *         <TT>false</TT>.
	 */
	protected boolean initializeCriticalPath()
	{
		// Process critical path
		boolean isError = false;

		try
		{
			// Read configuration properties
			if (this.getContext().getConfigurationManager().loadPropertiesFile(this.getContext()) == false) isError = true;

			// Read resource files
			if (this.readResourceFiles() == false) isError = true;

			// Read language token from configuration
			if (this.readLanguageSettings() == false) isError = true;

			// Read label resource files
			if (this.readLabelResourceFiles() == false) isError = true;

			// Write configuration template files
			if (this.getContext().getConfigurationManager().createTemplateConfigurationFile(this.getContext(), this.getApplicationIdentifier().toString(), this.getContext().getOriginName(), true) == false) isError = true;

			// Read configuration items of all interfaces
			if (this.readConfigurationAll(this.getContext()) == true) isError = true;

			// Check integrity of all read resources
			if (this.validateResourcesAll(this.getContext()) == true) isError = true;

			// Read security keys from configuration
			if (this.readSecurityKeys() == false) isError = true;

			// Read translated label files
			if (this.readTranslatedLabelFiles() == false) isError = true;

			// Write label template files
			if (this.getContext().getLabelManager().createTemplateLabelFiles(this.getContext(), this.getApplicationIdentifier().toString(), applicationVersion) == false) isError = true;

			// Assign right groups
			if (this.assignRightGroups(this.getContext()) == false) isError = true;
			this.getContext().getRightManager().printDistributionConfiguration(this.getContext());

			// Assign license models
			if (this.assignLicenseModels(this.getContext()) == false) isError = true;

			// Write license template files
			if (this.getContext().getLicenseManager().createTemplateLicenseFiles(this.getContext(), this.getApplicationIdentifier().toString(), applicationVersion) == false) isError = true;

			// Read real customer license files
			if (this.getContext().getLicenseManager().loadLicenseFiles(this.getContext(), this.getApplicationIdentifier().toString(), applicationVersion) == false) isError = true;

			// Initialize application settings
			if (this.readConfiguration(this.getContext()) == true) isError = true;
			if (this.validateResources(this.getContext()) == true) isError = true;
			if (this.cleanEnvironment(this.getContext()) == true) isError = true;
		}
		catch (Exception e)
		{
			this.context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnStartingServer"), null, e);
			isError = true;
		}

		// Return
		return isError;
	}

	/**
	 * Read and check all security keys.
	 * 
	 * @return Returns <TT>true</TT> if the keys could be read and checked
	 *         positive, otherwise <TT>false</TT>.
	 */
	public abstract boolean readSecurityKeys();

	/**
	 * Release all resources needed by the application server.
	 */
	protected abstract void releaseResources();

	/**
	 * Defining the right groups of the application.
	 * <p>
	 * Please use the method <TT>addRightItem()</TT> in the concrete application
	 * to assign a <TT>Right Item</TT> to a <TT>Right Group</TT>.
	 * 
	 * @return Returns <TT>true</TT> if the right groups could be assigned
	 *         successfully, otherwise <TT>false</TT>.
	 */
	protected abstract boolean assignRightGroups(Context context);

	/**
	 * Defining all license models of the application.
	 * <p>
	 * Please use the method <TT>addLicenseItem()</TT> in the concrete
	 * application to assign a <TT>License Item</TT> to a <TT>License Model</TT>.
	 * 
	 * @return Returns <TT>true</TT> if the license models could be defined
	 *         successfully, otherwise <TT>false</TT>.
	 */
	protected abstract boolean assignLicenseModels(Context context);

	/**
	 * Start application server.
	 * 
	 * @return Returns <TT>true</TT> if the server could be started, otherwise
	 *         <TT>false</TT>.
	 */
	protected abstract boolean startApplication();

	/**
	 * Stop application server.
	 */
	protected abstract void stopApplication();

	/**
	 * Bind all resources the application server needs.
	 * 
	 * @return Returns <TT>true</TT> if the resources could be binded, otherwise
	 *         <TT>false</TT>.
	 */
	protected abstract boolean bindResources();

	@Override
	public boolean readConfiguration(Context context)
	{
		try
		{
			// Read parameter: MaximumWaitingTimeForPendingThreadsInSeconds
			this.maximumWaitingTimeForPendingThreadsInSeconds = context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "Shutdown", "MaximumWaitingTimeForPendingThreadsInSeconds"), false);

			// Return
			return false;
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), null, e);
			return true;
		}
	}

	/**
	 * Read all resource files.
	 * 
	 * @return Returns <TT>true</TT> if the resource files could be read
	 *         successfully, otherwise <TT>false</TT>.
	 */
	public boolean readResourceFiles()
	{
		// Initialize
		boolean isSuccessful = true;

		// Read and check
		try
		{
			// Read regular resource files
			if (this.getContext().getResourceManager().loadResourceFile(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Basic.toString(), this.applicationVersion, null) == false) isSuccessful = false;
			if (this.getContext().getResourceManager().loadResourceFile(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Common.toString(), this.applicationVersion, null) == false) isSuccessful = false;
			if (this.getContext().getResourceManager().loadResourceFile(this.getContext(), this.getApplicationIdentifier().toString(), this.applicationVersion, null) == false) isSuccessful = false;
			if (this.getContext().getResourceManager().loadResourceFile(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Extension.toString(), this.applicationVersion, null) == false) isSuccessful = false;

			// Read sub resource files
			if (this.getContext().getResourceManager().loadResourceSubFiles(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Basic.toString(), this.applicationVersion) == false) isSuccessful = false;
			if (this.getContext().getResourceManager().loadResourceSubFiles(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Common.toString(), this.applicationVersion) == false) isSuccessful = false;
			if (this.getContext().getResourceManager().loadResourceSubFiles(this.getContext(), this.getApplicationIdentifier().toString(), this.applicationVersion) == false) isSuccessful = false;
			if (this.getContext().getResourceManager().loadResourceSubFiles(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Extension.toString(), this.applicationVersion) == false) isSuccessful = false;

			// Read test resources. if the application is running in
			// "test mode", the test resources are loaded additionally.
			if (context.isRunningInTestMode())
			{
				String fileName = FileLocationFunctions.compileFilePath(TestManager.getTestResourceFilePath(this.getContext()), FileLocationFunctions.getResourceFileName());
				fileName = FileLocationFunctions.replacePlaceholder(this.getContext(), fileName, ApplicationManager.ApplicationIdentifierEnum.Test.toString(), null);

				if (this.getContext().getResourceManager().loadResourceFile(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Test.toString(), this.applicationVersion, fileName) == false) isSuccessful = false;
			}

		}
		catch (Exception e)
		{
			return false;
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Read all label resource files.
	 * 
	 * @return Returns <TT>true</TT> if the resource files could be read
	 *         successfully, otherwise <TT>false</TT>.
	 */
	public boolean readLabelResourceFiles()
	{
		// Initialize
		boolean isSuccessful = true;

		// Read and check
		try
		{
			if (this.getContext().getLabelManager().loadLabelResourceFile(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Basic.toString(), applicationVersion) == false) isSuccessful = false;
			if (this.getContext().getLabelManager().loadLabelResourceFile(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Common.toString(), applicationVersion) == false) isSuccessful = false;
			if (this.getContext().getLabelManager().loadLabelResourceFile(this.getContext(), this.getApplicationIdentifier().toString(), applicationVersion) == false) isSuccessful = false;
			if (this.getContext().getLabelManager().loadLabelResourceFile(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Extension.toString(), applicationVersion) == false) isSuccessful = false;
		}
		catch (Exception e)
		{
			return false;
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Read translated label files.
	 * 
	 * @return Returns <TT>true</TT> if the resource files could be read
	 *         successfully, otherwise <TT>false</TT>.
	 */
	public boolean readTranslatedLabelFiles()
	{
		// Initialize
		boolean isSuccessful = true;

		// Read and check
		try
		{
			for (String language : this.supportedLanguages.values())
			{
				if (this.getContext().getLabelManager().loadTranslatedLabelFile(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Basic.toString(), applicationVersion, language) == false) isSuccessful = false;
				if (this.getContext().getLabelManager().loadTranslatedLabelFile(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Common.toString(), applicationVersion, language) == false) isSuccessful = false;
				if (this.getContext().getLabelManager().loadTranslatedLabelFile(this.getContext(), this.getApplicationIdentifier().toString(), applicationVersion, language) == false) isSuccessful = false;
				if (this.getContext().getLabelManager().loadTranslatedLabelFile(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Extension.toString(), applicationVersion, language) == false) isSuccessful = false;
			}
		}
		catch (Exception e)
		{
			return false;
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Read and check all language settings.
	 * 
	 * @return Returns <TT>true</TT> if the settings could be read and checked
	 *         positive, otherwise <TT>false</TT>.
	 */
	public boolean readLanguageSettings()
	{
		// Initialize
		boolean isSuccessful = true;
		String supportedLanguagesString = null;

		// Read and check
		try
		{
			// Read configuration setting of "NotifyLabelEvents"
			this.notifyLabelEvents = this.getContext().getConfigurationManager().getPropertyAsBooleanValue(this.getContext(), ResourceManager.configuration(context, "Label", "NotifyLabelEvents"), false);

			// Read configuration parameter
			supportedLanguagesString = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(context, "Application", "SupportedLanguages"), true);
			if (supportedLanguagesString == null || supportedLanguagesString.length() == 0) isSuccessful = false;

			this.mainLanguage = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(context, "Application", "MainLanguage"), true);
			this.mainLanguage.trim();
			if (this.mainLanguage == null || this.mainLanguage.length() == 0) isSuccessful = false;

			this.secondaryLanguage = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(context, "Application", "SecondaryLanguage"), false);
			this.secondaryLanguage.trim();
			if (this.secondaryLanguage == null || this.secondaryLanguage.length() == 0) isSuccessful = false;

			if (isSuccessful == false) return isSuccessful;

			// Check configuration
			String[] supportedLanguagesParts = supportedLanguagesString.split(",");

			for (int i = 0; i < supportedLanguagesParts.length; i++)
			{
				this.supportedLanguages.put(supportedLanguagesParts[i].trim(), supportedLanguagesParts[i].trim());
			}

			if (this.supportedLanguages.get(this.mainLanguage) == null)
			{
				String errorString = "--> ApplicationMain language '" + this.mainLanguage + "' not found in the list of supported languages.";
				errorString += "\n--> Supported languages: '" + supportedLanguagesString + "'";
				errorString += "\n--> Please add the main language to the list of supported languages.";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

				isSuccessful = false;
			}

			if (this.supportedLanguages.get(this.secondaryLanguage) == null)
			{
				String errorString = "--> Secondary language '" + this.secondaryLanguage + "' not found in the list of supported languages.";
				errorString += "\n--> Supported languages: '" + supportedLanguagesString + "'";
				errorString += "\n--> Please add the secondary language to the list of supported languages.";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

				isSuccessful = false;
			}
		}
		catch (Exception e)
		{
			return false;
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Shut down the application.
	 */
	protected void shutdown()
	{
		try
		{
			// Set shut down notification text
			String shutdownNotificationText = "Shutdown SERVER application [" + this.getCodeName() + "] because of severe errors on initialization";
			this.getContext().setDumpFirstErrorShutdownNotificationText(shutdownNotificationText);

			// Get and save dump text
			String dumpText = this.getContext().getNotificationManager().getDump(this.getContext());

			// Flush dump
			this.getContext().getNotificationManager().flushDump(this.getContext());

			// Prepare shutdown text
			String shutdownText = "\n\n";
			shutdownText += "\n##############################################################";
			shutdownText += "\n##############################################################";
			shutdownText += "\n###  " + shutdownNotificationText;
			shutdownText += "\n##############################################################";
			shutdownText += "\n##############################################################";

			// Notify shutdown event
			this.getContext().getNotificationManager().notifyEvent(this.getContext(), ResourceManager.notification(context, "Application", "ForcedShutdownDueToErrorOnInitialization"), shutdownText + "\n\n" + dumpText, null);
			this.getContext().resetDumpList();

			// Notify initializing error
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(context, "Application", "ErrorOnInitializing"), shutdownText + "\n\n" + dumpText, null);
			this.getContext().resetDumpList();

			// Release resources
			this.releaseResources();

			// Notify shutdown on console. Wait for 2 seconds to have the error
			// messages as the last messages on console.
			FileUtilFunctions.generalSleepSeconds(2);
			System.err.println(shutdownText);
			System.err.println(dumpText);

			// Initiate Shutdown
			this.shutdown = true;
		}
		catch (Exception e)
		{
			// Be silent
		}
	}

	/**
	 * Check on integrity errors.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean validateResourcesAll(Context context)
	{
		// Variables
		boolean isIntegrityError = false;

		// Check for integrity errors (Named resource identifiers)
		try
		{
			if (context.validateResources(context) == true) isIntegrityError = true;
			if (context.getCommandManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getNotificationManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getConfigurationManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getLabelManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getLocaldataManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getRightManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getLicenseManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getMediaManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getTestManager().validateResources(context) == true) isIntegrityError = true;
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isIntegrityError;
	}

	/**
	 * Clean environment.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean cleanEnvironmentAll(Context context)
	{
		// Variables
		boolean isIntegrityError = false;

		// Clean environment
		try
		{
			if (context.cleanEnvironment(context) == true) isIntegrityError = true;
			if (context.getCommandManager().cleanEnvironment(context) == true) isIntegrityError = true;
			if (context.getNotificationManager().cleanEnvironment(context) == true) isIntegrityError = true;
			if (context.getConfigurationManager().cleanEnvironment(context) == true) isIntegrityError = true;
			if (context.getLabelManager().cleanEnvironment(context) == true) isIntegrityError = true;
			if (context.getLocaldataManager().cleanEnvironment(context) == true) isIntegrityError = true;
			if (context.getRightManager().cleanEnvironment(context) == true) isIntegrityError = true;
			if (context.getLicenseManager().cleanEnvironment(context) == true) isIntegrityError = true;
			if (context.getMediaManager().cleanEnvironment(context) == true) isIntegrityError = true;
			if (context.getTestManager().cleanEnvironment(context) == true) isIntegrityError = true;
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isIntegrityError;
	}

	/**
	 * Read configuration items of all interfaces.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean readConfigurationAll(Context context)
	{
		// Variables
		boolean isError = false;

		// Read configuration items of all interfaces
		try
		{
			if (context.readConfiguration(context) == true) isError = true;
			if (context.getCommandManager().readConfiguration(context) == true) isError = true;
			if (context.getNotificationManager().readConfiguration(context) == true) isError = true;
			if (context.getConfigurationManager().readConfiguration(context) == true) isError = true;
			if (context.getLabelManager().readConfiguration(context) == true) isError = true;
			if (context.getLocaldataManager().readConfiguration(context) == true) isError = true;
			if (context.getRightManager().readConfiguration(context) == true) isError = true;
			if (context.getLicenseManager().readConfiguration(context) == true) isError = true;
			if (context.getMediaManager().readConfiguration(context) == true) isError = true;
			if (context.getTestManager().readConfiguration(context) == true) isError = true;
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isError;
	}

	/**
	 * Getter
	 */
	public Context getContext()
	{
		return this.context;
	}

	/**
	 * Getter
	 */
	public String getCodeName()
	{
		return this.codeName;
	}

	/**
	 * Getter
	 */
	public ApplicationManager.ApplicationIdentifierEnum getApplicationIdentifier()
	{
		return this.applicationIdentifier;
	}

	/**
	 * Getter
	 */
	public boolean isShutdown()
	{
		return this.shutdown;
	}

	/**
	 * Getter
	 */
	public boolean isStopRunning()
	{
		return this.stopRunning;
	}

	/**
	 * Setter
	 */
	public void setStopRunning(boolean stopRunning)
	{
		this.stopRunning = stopRunning;
	}

	/**
	 * Getter
	 */
	public int getApplicationVersion()
	{
		return this.applicationVersion;
	}

	/**
	 * Getter
	 */
	public HashMap<String, String> getSupportedLanguages()
	{
		return this.supportedLanguages;
	}

	/**
	 * Getter
	 */
	public String getSupportedLanguagesString()
	{
		String supportedLanguagesString = "";

		for (String language : this.supportedLanguages.values())
		{
			if (supportedLanguagesString.length() > 0) supportedLanguagesString += ", ";
			supportedLanguagesString += language;
		}

		return supportedLanguagesString;
	}

	/**
	 * Getter
	 */
	public String getMainLanguage()
	{
		return mainLanguage;
	}

	/**
	 * Getter
	 */
	public String getSecondaryLanguage()
	{
		return secondaryLanguage;
	}

	/**
	 * Getter
	 */
	public boolean isNotifyLabelEvents()
	{
		return notifyLabelEvents;
	}

	/**
	 * Getter
	 */
	public boolean isServerApplication()
	{
		return serverApplication;
	}

	/**
	 * Getter
	 */
	public boolean isClientApplication()
	{
		return clientApplication;
	}

	/**
	 * Getter
	 */
	public Integer getMaximumWaitingTimeForPendingThreadsInSeconds()
	{
		return maximumWaitingTimeForPendingThreadsInSeconds;
	}
}
