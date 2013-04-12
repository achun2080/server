package fmagic.basic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import fmagic.test.TestManager;

/**
 * This class contains context data needed by all classes and functions of a
 * server or client. All managers are bind with one instance to a given context,
 * e. g. <TT>ConfigurationManager</TT>, <TT>ErrorManager</TT>,
 * <TT>LogManager</TT>, <TT>EventManager</TT>, <TT>NotificationManager</TT> or
 * <TT>WatchdogManager</TT>.
 * <p>
 * One important feature provides a specific notification service for log
 * messages and error messages. You have to copy a given context to create
 * another specific context type. There are three types of notification:
 * <ul>
 * <li><TT>Tracking</TT>: Each message is delivered immediately to the
 * destination interface, e. g. a log file. Please use the method
 * <TT>createTrackingContext()</TT> to create a "tracking" context.</li>
 * <li><TT>Dump</TT>: All log messages and error messages will be stored
 * internal until an error occurs. If an error occurs the stored messages were
 * notified automatically. If no error occurs all messages were rejected after
 * lifetime of this context. Please use the method <TT>createDumpContext()</TT>
 * to create a "dump" context.</li>
 * <li><TT>Silent dump</TT>: All log messages and error messages were stored
 * internally. They only are notified after invoking the method
 * <TT>flushDump()</TT>, otherwise they were rejected after lifetime of this
 * context. Please use the method <TT>createSilentDumpContext()</TT> to create a
 * "silent dump" context.</li>
 * </ul>
 * <p>
 * For each coherent task or function you should create a new context of a
 * appropriate type, e. g. <TT>newContext = oldContext.createDumpContext()</TT>.
 * There are a lot of advantages of this concept of notification:
 * <ul>
 * <li>It is thread save because each new context is created in a local thread
 * and is only single used by this thread.</li>
 * <li>There is no information overflow by using the context type "dump" because
 * log messages are only shown if any error message occurred. So if no error
 * occurs than no messages will be notified. On the other hand if an error
 * occurs than you have more information of the given context, e. g. all log
 * messages that around the error.</li>
 * <li>You can nest contexts.</li>
 * <li>You can entitle contexts and find specific dumps in the log files more
 * comfortable.</li>
 * <li>You are secured to memory leaks because the contexts appear in local
 * visibility only and were cleared automatically by the garbage collector after
 * leaving the local memory area.</li>
 * </ul>
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public abstract class Context implements Cloneable, ManagerInterface
{
	// Bind final managers
	private final ApplicationManager applicationManager;
	private final NotificationManager notificationManager;
	private WatchdogManager watchdogManager = null;
	private final ConfigurationManager configurationManager;
	private final LocaldataManager localdataManager;
	private final LabelManager labelManager;
	private final ResourceManager resourceManager;
	private final CommandManager commandManager;
	private final RightManager rightManager;
	private final LicenseManager licenseManager;
	private final MediaManager mediaManager;
	private final TestManager testManager;

	// Common data of context
	private final String codeName;
	private final String applicationName;
	private final String originName;
	private final int applicationVersion;

	// Session data, containing relevant user data
	private SessionContainer sessionContainer = null;

	// Context type
	public static enum ContextTypeEnum
	{
		TRACKING, DUMP, SILENT_DUMP, TEST
	}

	private ContextTypeEnum contextType = Context.ContextTypeEnum.TRACKING;
	private ResourceContainer contextResourceContainer = null;

	// Test mode
	private final String testSessionName;
	private final boolean runningInTestMode;
	private final String testCaseName;

	// List of messages that are stored in the dump list of the current context
	private List<String> dumpListMessages = new ArrayList<String>();
	private HashMap<String, String> dumpListResourceDocumentation = new HashMap<String, String>();

	private String dumpFirstErrorIdentifier = null;
	private String dumpFirstErrorResourceDocumentationText = null;
	private String dumpFirstErrorAdditionalText = null;
	private Exception dumpFirstErrorException = null;
	private String dumpFirstErrorShutdownNotificationText = null;

	final private int MAX_NU_OF_DUMP_ITEMS_MINIMUM = 1000;
	final private int MAX_NU_OF_DUMP_ITEMS_MAXIMUM = 5000;
	final private int MAX_NU_OF_DUMP_ITEMS_DEFAULT = 2000;
	private Integer dumpMaxNuOfDumpItems = null;

	// WATCHDOG mode
	private boolean suspendWatchdog = false;

	/**
	 * Constructor
	 * 
	 * @param codeName
	 *            Code name of the application.
	 * 
	 * @param applicationName
	 *            The name of the application.
	 * 
	 * @param applicationVersion
	 *            Software version of the application.
	 * 
	 * @param originName
	 *            Origin name "Server" or "Client".
	 * 
	 * @param applicationManager
	 *            The instance of the application manager that is to be assigned
	 *            to the context.
	 * 
	 * @param testManager
	 *            The instance of the current test manager that is to be
	 *            assigned to the context.
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
	public Context(String codeName, String applicationName,
			int applicationVersion, String originName,
			ApplicationManager applicationManager, TestManager testManager,
			boolean runningInTestMode, String testCaseName, String testSessionName)
	{
		this.codeName = FileUtil.fitToFileNameCompatibility(codeName);
		this.applicationName = FileUtil.fitToFileNameCompatibility(applicationName);
		this.applicationVersion = applicationVersion;
		this.originName = FileUtil.fitToFileNameCompatibility(originName);
		this.applicationManager = applicationManager;

		this.notificationManager = new NotificationManager();
		this.configurationManager = new ConfigurationManager();
		this.localdataManager = new LocaldataManager();
		this.labelManager = new LabelManager();
		this.resourceManager = new ResourceManager();
		this.commandManager = new CommandManager();
		this.rightManager = new RightManager();
		this.licenseManager = new LicenseManager();
		this.mediaManager = new MediaManager();
		this.testManager = testManager;

		// If the application is running in "test mode", the context type is set
		// to TEST, and it is not possible to change it anymore. Additionally a
		// test ticket number is created
		if (runningInTestMode == true)
		{
			this.runningInTestMode = true;

			if (testCaseName == null || testCaseName.length() == 0)
			{
				this.testCaseName = "default";
			}
			else
			{
				this.testCaseName = FileUtil.fitToFileNameCompatibility(testCaseName);
			}

			this.contextType = Context.ContextTypeEnum.TEST;


			if (testSessionName == null || testSessionName.length() == 0)
			{
				Date messageDate = new Date();
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.getDefault());
				this.testSessionName = simpleDateFormat.format(messageDate);
			}
			else
			{
				this.testSessionName = FileUtil.fitToFileNameCompatibility(testSessionName);
			}
		}
		else
		{
			this.runningInTestMode = false;
			this.testCaseName = null;
			this.testSessionName = null;
		}

		// Get a provisional resource container because the resource files are
		// not read yet
		this.contextResourceContainer = this.getResourceManager().getProvisionalResourceContainer("Context.Common.All.Identifier.Overall.Tracking");
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = { "Context" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = { "Identifier" };
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceTemplate(context, includingResourceIdentifiers, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public String printManual(Context context)
	{
		String dumpText = "";

		String typeCriteria[] = { "Context" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = { "Identifier" };
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceManual(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public String printIdentifierList(Context context)
	{
		String dumpText = "";

		String typeCriteria[] = { "Context" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = { "Identifier" };
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceIdentifierList(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public boolean validateResources(Context context)
	{
		// Variables
		boolean isIntegrityError = false;
		ResourceManager resourceManager = context.getResourceManager();

		// Check for integrity errors (Named resource identifiers)
		try
		{
			for (ResourceContainer resourceContainer : resourceManager.getResources().values())
			{
				// Check if name is set
				String name = resourceContainer.getName();
				if (name == null || name.length() == 0) continue;

				// Context: Alias name must be set
				if (resourceContainer.getType().equalsIgnoreCase("context") && resourceContainer.checkAliasName() == false)
				{
					String errorString = "--> Alias name for context identifier is not set.";
					String fileName = resourceManager.getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
					if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
					errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
					errorString += "\n--> The Alias name is used as a part of log files. Please set an Alias name.";

					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

					isIntegrityError = true;
				}
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isIntegrityError;
	}

	/**
	 * Getter
	 */
	public String getCodeName()
	{
		return codeName;
	}

	/**
	 * Getter
	 */
	public ContextTypeEnum getContextType()
	{
		return contextType;
	}

	/**
	 * Setter
	 */
	private void setContextType(ContextTypeEnum contextType)
	{
		this.contextType = contextType;
	}

	/**
	 * Getter
	 */
	public ResourceContainer getContextResourceContainer()
	{
		return contextResourceContainer;
	}

	/**
	 * Setter
	 */
	private void setContextResourceContainer(ResourceContainer contextResourceContainer)
	{
		this.contextResourceContainer = contextResourceContainer;
	}

	/**
	 * Getter
	 */
	public List<String> getDumpListMessages()
	{
		return this.dumpListMessages;
	}

	/**
	 * Getter
	 */
	public HashMap<String, String> getDumpListResourceDocumentation()
	{
		return this.dumpListResourceDocumentation;
	}

	/**
	 * Getter
	 */
	public boolean isSuspendWatchdog()
	{
		return this.suspendWatchdog;
	}

	/**
	 * Cloner
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	/**
	 * Creates a new context generally.
	 * 
	 * @param contextType
	 *            The context type to create.
	 * 
	 * @param resourceContextString
	 *            The context usage identifier (ENUM) of the dump for using as
	 *            identification and as part of the log file name.
	 * 
	 * @param suspendWatchdog
	 *            Optional: If you want to inhibit invoking of WATCHDOG
	 *            functions in this context you can set this flag to
	 *            <TT>true</TT>.
	 * 
	 * @return Returns the copied context.
	 */
	private Context createContext(ContextTypeEnum contextType, ResourceContainer contextResourceContainer, boolean suspendWatchdog)
	{
		Context context = null;

		try
		{
			context = (Context) this.clone();
			context.setContextType(contextType);
			context.setContextResourceContainer(contextResourceContainer);
			context.setSuspendWatchdog(suspendWatchdog);
			context.resetDumpList();
		}
		catch (Exception e)
		{
		}

		return context;
	}

	/**
	 * Creates a new context of the type "Dump" and copy the old one to it.
	 * <p>
	 * The context type "Dump" means that all log messages and error messages
	 * will be stored internal until an error occurs. If an error occurs during
	 * lifetime of this context the stored messages will be notified. If no
	 * error occurs all messages will be rejected.
	 * 
	 * @param resourceContextString
	 *            The context usage identifier (ENUM) of the dump for using as
	 *            identification and as part of the log file name.
	 * 
	 * @param suspendWatchdog
	 *            Optional: If you want to inhibit invoking of WATCHDOG
	 *            functions in this context you can set this flag to
	 *            <TT>true</TT>.
	 * 
	 * @return Returns the copied context.
	 */
	public Context createDumpContext(ResourceContainer contextResourceContainer)
	{
		// If the context type is set to TEST it must not be changed anymore
		if (this.getContextType() == ContextTypeEnum.TEST) return this;

		// Set new context type
		return createContext(ContextTypeEnum.DUMP, contextResourceContainer, false);
	}

	/**
	 * @see Context#createDumpContext(String)
	 */
	public Context createDumpContext(ResourceContainer contextResourceContainer, boolean suspendWatchdog)
	{
		// If the context type is set to TEST it must not be changed anymore
		if (this.getContextType() == ContextTypeEnum.TEST) return this;

		// Set new context type
		return createContext(ContextTypeEnum.DUMP, contextResourceContainer, suspendWatchdog);
	}

	/**
	 * Creates a new context of the type "Silent Dump" and copy the old one to
	 * it.
	 * <p>
	 * The context type "Silent Dump" means that all log messages and error
	 * messages will be stored internally. They only will be notified by
	 * invoking the method <TT>flushDump()</TT>, otherwise they will be rejected
	 * after lifetime of this context.
	 * 
	 * @param resourceContextString
	 *            The context usage identifier (enum) of the dump for using as
	 *            identification and as part of the log file name.
	 * 
	 * @param suspendWatchdog
	 *            Optional: If you want to inhibit invoking of WATCHDOG
	 *            functions in this context you can set this flag to
	 *            <TT>true</TT>.
	 * 
	 * @return Returns the copied context.
	 */
	public Context createSilentDumpContext(ResourceContainer contextResourceContainer)
	{
		// If the context type is set to TEST it must not be changed anymore
		if (this.getContextType() == ContextTypeEnum.TEST) return this;

		// Set new context type
		return createContext(ContextTypeEnum.SILENT_DUMP, contextResourceContainer, false);
	}

	/**
	 * @see Context#createSilentDumpContext(String)
	 */
	public Context createSilentDumpContext(ResourceContainer contextResourceContainer, boolean suspendWatchdog)
	{
		// If the context type is set to TEST it must not be changed anymore
		if (this.getContextType() == ContextTypeEnum.TEST) return this;

		// Set new context type
		return createContext(ContextTypeEnum.SILENT_DUMP, contextResourceContainer, suspendWatchdog);
	}

	/**
	 * Creates a new context of the type "Tracking" and copy the old one to it.
	 * <p>
	 * The context type "Tracking" means that all log messages and error
	 * messages were notified immediately.
	 * 
	 * @param resourceContextString
	 *            The context usage identifier (enum) of the dump for using as
	 *            identification and as part of the log file name.
	 * 
	 * @param suspendWatchdog
	 *            Optional: If you want to inhibit invoking of WATCHDOG
	 *            functions in this context you can set this flag to
	 *            <TT>true</TT>.
	 * 
	 * @return Returns the copied context.
	 */
	public Context createTrackingContext(ResourceContainer contextResourceContainer)
	{
		// If the context type is set to TEST it must not be changed anymore
		if (this.getContextType() == ContextTypeEnum.TEST) return this;

		// Set new context type
		return createContext(ContextTypeEnum.TRACKING, contextResourceContainer, false);
	}

	/**
	 * @see Context#createTrackingContext(String)
	 */
	public Context createTrackingContext(ResourceContainer contextResourceContainer, boolean suspendWatchdog)
	{
		// If the context type is set to TEST it must not be changed anymore
		if (this.getContextType() == ContextTypeEnum.TEST) return this;

		// Set new context type
		return createContext(ContextTypeEnum.TRACKING, contextResourceContainer, suspendWatchdog);
	}

	/**
	 * Notifies all log messages and error messages that were stored in dump
	 * list.
	 */
	public void flushDump()
	{
		this.getNotificationManager().flushDump(this);
	}

	/**
	 * Add an element (message) to the dump list of messages.
	 * 
	 * @param messageText
	 *            The message to be added.
	 */
	public void addDumpMessageElement(String messageText)
	{
		// Check variables
		if (this.dumpListMessages == null) return;

		// Load configuration parameter if not done yet
		if (this.dumpMaxNuOfDumpItems == null)
		{
			int iValue = this.getConfigurationManager().getPropertyAsIntegerValue(this, ResourceManager.configuration(this, "Context", "MaxNuOfDumpItems"), MAX_NU_OF_DUMP_ITEMS_DEFAULT, false);
			if (iValue < MAX_NU_OF_DUMP_ITEMS_MINIMUM) iValue = MAX_NU_OF_DUMP_ITEMS_MINIMUM;
			if (iValue > MAX_NU_OF_DUMP_ITEMS_MAXIMUM) iValue = MAX_NU_OF_DUMP_ITEMS_MAXIMUM;
			this.dumpMaxNuOfDumpItems = iValue;
		}

		// Check if the maximum number of allowed list items is exceeded
		if (this.dumpListMessages.size() >= this.dumpMaxNuOfDumpItems)
		{
			// Reset dump list
			this.resetDumpList();

			// Notify event
			String eventText = "--> Number of allowed items: '" + String.valueOf(this.dumpMaxNuOfDumpItems) + "'";
			this.getNotificationManager().notifyEvent(this, ResourceManager.notification(this, "Context", "MaxNuOfDumpItemsExceeded"), eventText, null);
		}

		// Add message to the dump list
		this.dumpListMessages.add(messageText);
	}

	/**
	 * Add an element (ENUM documentation) to the dump list of ENUM
	 * documentations.
	 * 
	 * @param enumIdentifier
	 *            The resource identifier that is to be added as string.
	 * 
	 * @param messageText
	 *            The documentation string to be added.
	 */
	public void addDumpResourceDocumentationElement(String enumIdentifier, String messageText)
	{
		if (this.dumpListResourceDocumentation != null) this.dumpListResourceDocumentation.put(enumIdentifier, messageText);
	}

	/**
	 * Reset (clear) the dump list.
	 * 
	 * @param String
	 *            The message to be added.
	 */
	public void resetDumpList()
	{
		this.dumpListMessages = new ArrayList<String>();
		this.dumpListResourceDocumentation = new HashMap<String, String>();

		this.dumpFirstErrorIdentifier = null;
		this.dumpFirstErrorAdditionalText = null;
		this.dumpFirstErrorResourceDocumentationText = null;
		this.dumpFirstErrorException = null;
		this.dumpFirstErrorShutdownNotificationText = null;
	}

	/**
	 * Set the first error message of a dump list. Usually the first error
	 * message triggers a dump. But at the end there might be more than one
	 * error message in a dump list. Therefore it is nice to see, which message
	 * triggered the dump.
	 * 
	 * @param dumpFirstErrorIdentifier
	 *            The error resource identifier as string.
	 * 
	 * @param dumpFirstErrorAdditionalText
	 *            The additional text of the error, if available, or
	 *            <TT>null</TT>.
	 * 
	 * @param dumpFirstErrorResourceDocumentationText
	 *            The documentation text of the error resource, if available, or
	 *            <TT>null</TT>.
	 * 
	 * @param dumpFirstErrorException
	 *            The exception object of the error, if available, or
	 *            <TT>null</TT>.
	 */
	public void setDumpFirstError(String dumpFirstErrorIdentifier, String dumpFirstErrorAdditionalText, String dumpFirstErrorResourceDocumentationText, Exception dumpFirstErrorException)
	{
		// Check parameter
		if (dumpFirstErrorIdentifier == null) return;
		if (dumpFirstErrorIdentifier.length() == 0) return;

		// Set only if there is no setting yet
		if (this.dumpFirstErrorIdentifier != null) return;

		// Take over the parameters
		this.dumpFirstErrorIdentifier = dumpFirstErrorIdentifier;
		this.dumpFirstErrorAdditionalText = dumpFirstErrorAdditionalText;
		this.dumpFirstErrorResourceDocumentationText = dumpFirstErrorResourceDocumentationText;
		this.dumpFirstErrorException = dumpFirstErrorException;
	}

	/**
	 * Setter
	 */
	public void setDumpFirstErrorShutdownNotificationText(String dumpFirstErrorShutdownNotificationText)
	{
		this.dumpFirstErrorShutdownNotificationText = dumpFirstErrorShutdownNotificationText;
	}

	/**
	 * Getter
	 */
	public String getDumpFirstErrorShutdownNotificationText()
	{
		return dumpFirstErrorShutdownNotificationText;
	}

	/**
	 * Getter
	 */
	public String getDumpFirstErrorIdentifier()
	{
		return dumpFirstErrorIdentifier;
	}

	/**
	 * Getter
	 */
	public String getDumpFirstErrorResourceDocumentationText()
	{
		return dumpFirstErrorResourceDocumentationText;
	}

	/**
	 * Getter
	 */
	public String getDumpFirstErrorAdditionalText()
	{
		return dumpFirstErrorAdditionalText;
	}

	/**
	 * Getter
	 */
	public Exception getDumpFirstErrorException()
	{
		return dumpFirstErrorException;
	}

	/**
	 * Setter
	 */
	private void setSuspendWatchdog(boolean suspendWatchdog)
	{
		this.suspendWatchdog = suspendWatchdog;
	}

	/**
	 * Getter
	 */
	public ApplicationManager getApplicationManager()
	{
		return applicationManager;
	}

	/**
	 * Getter
	 */
	public NotificationManager getNotificationManager()
	{
		return this.notificationManager;
	}

	/**
	 * Getter
	 */
	public WatchdogManager getWatchdogManager()
	{
		return this.watchdogManager;
	}

	/**
	 * Getter
	 */
	public ConfigurationManager getConfigurationManager()
	{
		return this.configurationManager;
	}

	/**
	 * Getter
	 */
	public LocaldataManager getLocaldataManager()
	{
		return this.localdataManager;
	}

	/**
	 * Getter
	 */
	public LabelManager getLabelManager()
	{
		return this.labelManager;
	}

	/**
	 * Getter
	 */
	public ResourceManager getResourceManager()
	{
		return this.resourceManager;
	}

	/**
	 * Getter
	 */
	public CommandManager getCommandManager()
	{
		return commandManager;
	}

	/**
	 * Getter
	 */
	public RightManager getRightManager()
	{
		return this.rightManager;
	}

	/**
	 * Getter
	 */
	public LicenseManager getLicenseManager()
	{
		return this.licenseManager;
	}

	/**
	 * Getter
	 */
	public MediaManager getMediaManager()
	{
		return this.mediaManager;
	}

	/**
	 * Getter
	 */
	public TestManager getTestManager()
	{
		return this.testManager;
	}

	/**
	 * Getter
	 */
	public String getApplicationName()
	{
		return applicationName;
	}

	/**
	 * Getter
	 */
	public String getOriginName()
	{
		return originName;
	}

	/**
	 * Getter
	 */
	public int getApplicationVersion()
	{
		return applicationVersion;
	}

	/**
	 * Setter
	 */
	public void setWatchdogManager(WatchdogManager watchdogManager)
	{
		this.watchdogManager = watchdogManager;
	}

	/**
	 * Getter
	 */
	public SessionContainer getServerSession()
	{
		return sessionContainer;
	}

	/**
	 * Setter
	 */
	public void setServerSession(SessionContainer sessionContainer)
	{
		this.sessionContainer = sessionContainer;
	}

	/**
	 * Check if there is an error message in the dump list.
	 * 
	 * @return Returns <TT>true</TT> if there is at least one error messages
	 *         dumped, otherwise <TT>false</TT>.
	 */
	public boolean isErrorInDumpList()
	{
		if (this.getDumpFirstErrorIdentifier() == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Getter
	 */
	public String getTestSessionName()
	{
		return this.testSessionName;
	}

	/**
	 * Getter
	 */
	public boolean isRunningInTestMode()
	{
		return this.runningInTestMode;
	}

	/**
	 * Getter
	 */
	public String getTestCaseName()
	{
		return this.testCaseName;
	}
}
