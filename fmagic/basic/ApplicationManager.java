package fmagic.basic;

import java.util.HashMap;

import fmagic.basic.ResourceContainer.OriginEnum;
import fmagic.client.ClientContext;
import fmagic.server.ServerContext;

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

	// Sets context of server
	private Context context;

	// Flag if processing is to be continued
	private boolean stopRunning = false;

	// Language data
	private HashMap<String, String> supportedLanguages = new HashMap<String, String>();
	private String mainLanguage = null;
	private String secondaryLanguage = null;
	private boolean notifyLabelEvents = true;

	// WATCHDOG
	protected WatchdogServer watchdogServer;

	// Shut down flag: If this flag is set, the server was not started because
	// of severe errors during initialization
	private boolean shutdown = false;

	// Application codes
	public static enum ApplicationIdentifierEnum
	{
		Basic, Common, Extension, SeniorCitizen, SteriManagement
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
	 */
	protected ApplicationManager(
			ApplicationManager.ApplicationIdentifierEnum applicationIdentifier,
			int applicationVersion, String codeName, OriginEnum origin)
	{
		// Create default context
		if (origin.toString().equals(OriginEnum.Server.toString()))
		{
			this.context = new ServerContext(codeName, applicationIdentifier.toString(), applicationVersion, this);
		}
		else
		{
			this.context = new ClientContext(codeName, applicationIdentifier.toString(), applicationVersion, this);
		}

		// Adopt constructor data
		this.applicationIdentifier = applicationIdentifier;
		this.codeName = codeName;
		this.applicationVersion = applicationVersion;

		// Create a silent dump context for initializing application server
		ResourceContainer contextResourceContainer = this.context.getResourceManager().getProvisionalResourceContainer("Context.Common.All.Identifier.Overall.Initialization");
		this.context = this.context.createSilentDumpContext(contextResourceContainer);

		// Process critical path
		boolean isError = false;

		try
		{
			while (true)
			{
				// Instantiate WATCHDOG
				WatchdogManager watchdogManager = new WatchdogManager(this.getContext());
				this.getContext().setWatchdogManager(watchdogManager);

				// Read configuration properties
				if (this.getContext().getConfigurationManager().loadPropertiesFile(this.getContext()) == false) isError = true;

				// Read resource files
				if (this.readResourceFiles() == false) isError = true;

				// Read language token from configuration
				if (this.readLanguageSettings() == false) isError = true;

				// Read label resource files
				if (this.readLabelResourceFiles() == false) isError = true;

				// Write configuration template files
				if (this.getContext().getConfigurationManager().createTemplateConfigurationFile(this.getContext(), this.getApplicationIdentifier().toString(), origin.toString(), true) == false) isError = true;

				// Read configuration items of all interfaces
				if (this.getContext().getResourceManager().readConfiguration(this.getContext(), this) == true) isError = true;

				// Check integrity of all read resources
				if (this.getContext().getResourceManager().validateResources(this.getContext(), this) == true) isError = true;

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

				// Start WATCHDOG
				this.watchdogServer = new WatchdogServer(this.getContext(), this.getContext().getWatchdogManager());
				if (watchdogServer.startServer(this.getContext()) == false) isError = true;

				// End of critical path
				break;
			}
		}
		catch (Exception e)
		{
			this.context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnStartingServer"), null, e);
			isError = true;
		}

		// Initiate shutdown if an error occurred
		if (isError == true)
		{
			this.shutdown();
			this.releaseWatchdog();
		}

		// Go back to the tracking context after initializing application server
		this.context = this.context.createTrackingContext(ResourceManager.context(this.context, "Overall", "Tracking"));
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
			if (this.getContext().getResourceManager().loadCommonResourceFile(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Basic.toString(), applicationVersion) == false) isSuccessful = false;
			if (this.getContext().getResourceManager().loadCommonResourceFile(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Common.toString(), applicationVersion) == false) isSuccessful = false;
			if (this.getContext().getResourceManager().loadCommonResourceFile(this.getContext(), this.getApplicationIdentifier().toString(), applicationVersion) == false) isSuccessful = false;
			if (this.getContext().getResourceManager().loadCommonResourceFile(this.getContext(), ApplicationManager.ApplicationIdentifierEnum.Extension.toString(), applicationVersion) == false) isSuccessful = false;
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
				if (this.getContext().getLabelManager().loadTranslatedLabelFile(this.getContext(),this.getApplicationIdentifier().toString(), applicationVersion, language) == false) isSuccessful = false;
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
			this.notifyLabelEvents = this.getContext().getConfigurationManager().getPropertyAsBooleanValue(this.getContext(), ResourceManager.configuration(context, "Label", "NotifyLabelEvents"), true, false);

			// Read configuration parameter
			supportedLanguagesString = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(context, "Application", "SupportedLanguages"), "", true);
			if (supportedLanguagesString == null || supportedLanguagesString.length() == 0) isSuccessful = false;

			this.mainLanguage = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(context, "Application", "MainLanguage"), "", true);
			this.mainLanguage.trim();
			if (this.mainLanguage == null || this.mainLanguage.length() == 0) isSuccessful = false;

			this.secondaryLanguage = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(context, "Application", "SecondaryLanguage"), "", false);
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
				String errorString = "--> Main language '" + this.mainLanguage + "' not found in the list of supported languages.";
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
	private void shutdown()
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
			Util.sleepSeconds(2);
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
	 * Release WATCHDOG separately because of it should be notified even the
	 * last messages of the application.
	 */
	protected void releaseWatchdog()
	{
		// Stop WATCHDOG
		try
		{
			if (this.watchdogServer != null)
			{
				this.watchdogServer.stopServer(this.context);
			}
		}
		catch (Exception e)
		{
			String errorText = "->> On WATCHDOG server";
			this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Watchdog", "ErrorOnStoppingServer"), errorText, e);
		}
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
}
