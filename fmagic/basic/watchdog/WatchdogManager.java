package fmagic.basic.watchdog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.internet.InternetAddress;

import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * This class implements the functionality of the WATCHDOG. Please invoke the
 * method <TT>addWatchdogCommand()</TT> to notify an event, error, configuration
 * event, persistence event, command event, or others. All WATCHDOG commands
 * were written to a FIFO queue. The queue itself is processed by an autonomous
 * function running in an own thread.
 * <p>
 * Each notification is executed as a single Email sent to a specific addressee.
 * All data necessary to determine the distribution list and the connection data
 * to the SMTP server you can set in the properties file of a given application.
 * Please use the properties parameters of WaTCHDOG and EMAIL to arrange the
 * system.
 * <p>
 * Please pay attention to the tread safety of this class, because there are
 * many threads using one and the same instance.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 14.01.2013 - Created
 * 
 */
public class WatchdogManager
{
	// Specific context of WATCHDOG manager
	private Context context = null;

	// List of WATCHDOG commands (requests) to process
	private Queue<WatchdogCommand> commandQueue = new LinkedList<WatchdogCommand>();

	// Email connection
	private EmailConnector emailConnection = null;

	// Check and set if the configuration is already done. Otherwise no WATCHDOG
	// commands can't be processed yet.
	private boolean configurationDone = false;

	// WATCHDOG configuration parameter
	private boolean watchdogActive = false;
	private HashMap<String, Set<String>> watchdogDistributionList = null;
	private boolean emailActive = false;

	// Configuration parameter: MaxNuOfItemsInCommandQueue
	private int watchdogMaxNuOfItemsInCommandQueue = 10000;

	// Configuration parameter: SecondsToWaitBetweenWatchdogProcessing
	private int secondsToWaitBetweenWatchdogProcessing = 5;

	// EMAIL configuration parameter
	private String smtpHost = "";
	private int smtpPort = 0;
	private String accountName = "";
	private String accountPassword = "";
	private String cc = "";
	private String bcc = "";
	private String returnAddress = "";

	// Flags for synchronizing the locking of messages
	private ConcurrentHashMap<String, Boolean> processingActive = new ConcurrentHashMap<String, Boolean>();
	private int messageLostCounter = 0;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context to use.
	 */
	public WatchdogManager(Context context)
	{
		this.context = context;
	}

	/**
	 * Lock the processing of an event, a log message or an error message to
	 * avoid recursive calls. Recursive calls can occur if a message is
	 * processed during a running processing.
	 * <p>
	 * The locking is done for each thread and message type separately.
	 * <p>
	 * Be careful!
	 * 
	 * @param messageType
	 *            The type of the message to lock.
	 * 
	 * @param enumIdentifier
	 *            The identifier of the message.
	 * 
	 * @return Returns <TT>true</TT> if the message processing is locked,
	 *         otherwise <TT>false</TT>.
	 */
	private boolean lockMessageHandling(String messageType, String enumIdentifier)
	{
		// Validate parameter
		if (messageType == null) return true;

		// Lock the message handling
		try
		{
			synchronized (this.processingActive)
			{
				String threadIdentifier = String.valueOf(Thread.currentThread().getId()) + "#" + messageType;
				Boolean isActive = this.processingActive.get(threadIdentifier);

				if (isActive != null && isActive == true)
				{
					this.messageLostCounter++;
					System.out.println("\n*** " + messageType + " message lost (" + String.valueOf(this.messageLostCounter) + ")");
					if (enumIdentifier != null) System.out.println("*** " + enumIdentifier);
					return true;
				}

				isActive = true;
				this.processingActive.put(threadIdentifier, isActive);

				// System.out.println("----------");
				// for (String value : this.processingActive.keySet())
				// {
				// System.out.println("--> " + value);
				// }
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Clear the Map if it has become too big. The should never happen.
		try
		{
			synchronized (this.processingActive)
			{
				if (this.processingActive.size() > 10000)
				{
					System.out.println("\n*** Processing active MESSAGE map has more than 10000 items and was cleared.");
					this.processingActive.clear();
				}
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return false;
	}

	/**
	 * Unlock the processing of an event, a log message or an error message to
	 * avoid recursive calls. Recursive calls can occur if an message is
	 * processed during a running processing.
	 * <p>
	 * The unlocking is done for each thread and message type separately.
	 * <p>
	 * Be careful!
	 * 
	 * @param messageType
	 *            The type of the message to lock.
	 */
	private void unlockMessageHandling(String messageType)
	{
		try
		{
			synchronized (this.processingActive)
			{
				String threadIdentifier = String.valueOf(Thread.currentThread().getId()) + "#" + messageType;
				this.processingActive.remove(threadIdentifier);
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return;
	}

	/**
	 * Get the email list of all recipients to notify about specific resource
	 * identifier or a context identifier.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param watchdogDistributionList
	 *            The mailing list of configuration.
	 * 
	 * @param resourceIdentifier
	 *            The resource identifier to check.
	 * 
	 * @param contextIdentifier
	 *            The context identifier to check.
	 * 
	 * @return Returns the mailing addresses as a Set list or an empty list.
	 */
	static Set<String> getEmailDistributionListToNotifiy(Context context, HashMap<String, Set<String>> watchdogDistributionList, String resourceIdentifier, String contextIdentifier)
	{
		// Initialize
		Set<String> distributionList = new HashSet<String>();

		// Check parameter
		if (resourceIdentifier == null || resourceIdentifier.length() == 0) return distributionList;
		if (contextIdentifier == null || contextIdentifier.length() == 0) return distributionList;
		if (watchdogDistributionList == null) return distributionList;

		// Split resource identifier
		String[] resourceIdentifierSplit = resourceIdentifier.split("\\.");
		if (resourceIdentifierSplit.length != 6) return distributionList;

		// Split context resource identifier
		String[] contextIdentifierSplit = contextIdentifier.split("\\.");
		if (contextIdentifierSplit.length != 6) return distributionList;

		// Get Email list
		try
		{
			// Search all items of the WATCHDOG mailing list
			for (String identifier : watchdogDistributionList.keySet())
			{
				// Split identifier
				String[] identifierSplit = identifier.split("\\.");
				if (identifierSplit.length != 6) continue;

				// Compare identifiers
				while (true)
				{
					// Check resource identifier
					boolean resourceIdentifierCheckResult = true;

					for (int i = 0; i < 6; i++)
					{
						if (!(identifierSplit[i].equals("*") || identifierSplit[i].equalsIgnoreCase(resourceIdentifierSplit[i]))) resourceIdentifierCheckResult = false;
					}

					// Check context resource identifier
					boolean contextIdentifierCheckResult = true;

					if (identifierSplit[0].equalsIgnoreCase("Context"))
					{
						for (int i = 0; i < 6; i++)
						{
							if (!(identifierSplit[i].equals("*") || identifierSplit[i].equalsIgnoreCase(contextIdentifierSplit[i]))) contextIdentifierCheckResult = false;
						}
					}
					else
					{
						contextIdentifierCheckResult = false;
					}

					// Add to list
					if (resourceIdentifierCheckResult == true || contextIdentifierCheckResult == true)
					{
						Set<String> tempEmailList = watchdogDistributionList.get(identifier);

						if (tempEmailList != null && tempEmailList.size() > 0)
						{
							for (String email : tempEmailList)
							{
								if (email != null && email.length() > 0) distributionList.add(email);
							}
						}
					}

					// Break
					break;
				}
			}
		}
		catch (Exception e)
		{
			String errorText = "--> Please see the actual mailing list in the next line";
			errorText += "\n--> '" + watchdogDistributionList.toString() + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Watchdog", "DistributionListParsingError"), errorText, e);
		}

		// Return
		return distributionList;
	}

	/**
	 * Reset the list of WATCHDOG commands (requests).
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @return
	 */
	void resetWatchdogCommands()
	{
		synchronized (this.commandQueue)
		{
			this.commandQueue.clear();
		}
	}

	/**
	 * Get number of elements in WATCHDOG command queue.
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @return Returns the number of elements in WATCHDOG command queue.
	 */
	int getNumberOfWatchdogElements()
	{
		int size = 0;

		synchronized (this.commandQueue)
		{
			size = this.commandQueue.size();
		}

		return size;
	}

	/**
	 * Get the next element of WATCHDOG command queue.
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @return Returns the next WATCHDOG command to process or <TT>null</TT>.
	 */
	WatchdogCommand getWatchdogCommand()
	{
		WatchdogCommand command = null;

		synchronized (this.commandQueue)
		{
			command = this.commandQueue.poll();
		}

		return command;
	}

	/**
	 * Load all configuration parameter regarding the WATCHDOG functions.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param distributionList
	 *            The mailing list to transform as String.
	 * 
	 * @return Returns the transformed mailing list as HashMap or <TT>null</TT>
	 *         if a parsing error occurred.
	 */
	private HashMap<String, Set<String>> configurationComposeDistributionList(String distributionList)
	{
		// Initialize variables
		HashMap<String, Set<String>> map = new HashMap<String, Set<String>>();
		String parsingErrorText = "";

		// Compose HashMap
		try
		{
			// Check parameter
			if (distributionList == null) return map;
			if (distributionList.length() == 0) return map;

			// Go through all main items (Email addresses). These items are
			// comma
			// separated.
			String[] emailItems = distributionList.split(",");
			if (emailItems == null) return map;
			if (emailItems.length == 0) return map;

			for (int i = 0; i < emailItems.length; i++)
			{
				// Go through all sub items (Resource identifiers). These items
				// are colon separated.
				String[] resourceItems = emailItems[i].split(":");
				if (resourceItems == null) continue;
				if (resourceItems.length == 0) continue;

				// Get Email address
				String emailItem = resourceItems[0].trim();

				if (emailItem == null || emailItem.length() == 0)
				{
					parsingErrorText += "--> Email address not set for mailing list item '" + String.valueOf(i + 1) + "': '" + emailItems[i] + "'" + "\n";
					continue;
				}

				// Check if at least one resource identifier is set
				if (resourceItems.length < 2 || resourceItems[1] == null || resourceItems[1].trim().length() == 0)
				{
					parsingErrorText += "--> No resource identifier set for Email address '" + emailItem + "' of mailing list item '" + String.valueOf(i + 1) + "'" + "\n";
					continue;
				}

				String resourceItem = resourceItems[1].trim();

				// Go through all Email addresses of the current Email item.
				// These items are pipe separated.
				String[] emailItemList = emailItem.split("\\|");
				if (emailItemList == null) continue;
				if (emailItemList.length == 0) continue;

				// Get first Email address
				emailItem = emailItemList[0].trim();
				if (emailItem == null || emailItem.length() == 0) continue;

				// Process all sub Email items, departed by a colon
				for (int k = 0; k < emailItemList.length; k++)
				{
					// Get next email item
					emailItem = emailItemList[k].trim();
					if (emailItem == null || emailItem.length() == 0) continue;

					// Process all resource identifiers
					for (int j = 1; j < resourceItems.length; j++)
					{
						// Get next resource identifier
						resourceItem = resourceItems[j].trim();
						if (resourceItem == null || resourceItem.length() == 0) continue;

						// Check if the resource identifier has 5 points
						// precisely
						if (resourceItem.split("\\.").length != 6)
						{
							parsingErrorText += "--> Resource identifier '" + resourceItem + "' is not valid." + "\n";
							continue;
						}

						// Check validity of Email address
						try
						{
							InternetAddress emailAddr = new InternetAddress(emailItem);
							emailAddr.validate();
						}
						catch (Exception e)
						{
							parsingErrorText += "--> The Email address '" + emailItem + "' of mailing list item '" + String.valueOf(i + 1) + "' is not valid." + "\n";
							continue;
						}

						// Save email items and resource items in hash map
						Set<String> emailList = map.get(resourceItem);
						if (emailList == null) emailList = new HashSet<String>();
						emailList.add(emailItem);
						map.put(resourceItem, emailList);
					}
				}
			}
		}
		catch (Exception e)
		{
			String errorText = "--> Distribution list: " + distributionList;
			this.getContext().getNotificationManager().notifyError(context, ResourceManager.notification(this.getContext(), "Watchdog", "DistributionListParsingError"), errorText, e);
			return null;
		}

		// Notify parsing error
		if (parsingErrorText.length() > 0)
		{
			String errorText = parsingErrorText;
			errorText += "--> Distribution list: " + distributionList;
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Watchdog", "DistributionListParsingError"), errorText, null);
			return null;
		}

		// Return
		return map;
	}

	/**
	 * Read all configuration parameter regarding the WATCHDOG functions.
	 * 
	 * @return Returns <TT>true</TT> if all parameter could be read, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean readConfiguration()
	{
		// Initialize
		String errorText = "";
		boolean isError = false;

		// Read all parameters from configuration
		try
		{
			// Parameter: WATCHDOG Active
			errorText = "--> On processing parameter: Watchdog Active";
			this.watchdogActive = this.getContext().getConfigurationManager().getPropertyAsBooleanValue(this.getContext(), ResourceManager.configuration(this.getContext(), "Watchdog", "Active"), false);

			// Parameter: WATCHDOG Mailing List
			errorText = "--> On processing parameter: Watchdog DistributionList";
			String watchdogDistributionListString = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(this.getContext(), "Watchdog", "DistributionList"), (this.watchdogActive == true));
			this.watchdogDistributionList = this.configurationComposeDistributionList(watchdogDistributionListString);

			if (this.watchdogDistributionList == null)
			{
				isError = true;
			}

			// Parameter: WATCHDOG Max number of items in CommandQueue
			this.watchdogMaxNuOfItemsInCommandQueue = this.getContext().getConfigurationManager().getPropertyAsIntegerValue(this.getContext(), ResourceManager.configuration(context, "Watchdog", "MaximumNuOfItemsInCommandQueue"), false);

			// Parameter: WATCHDOG SecondsToWaitBetweenWatchdogProcessing
			this.secondsToWaitBetweenWatchdogProcessing = this.getContext().getConfigurationManager().getPropertyAsIntegerValue(this.getContext(), ResourceManager.configuration(context, "Watchdog", "SecondsToWaitBetweenWatchdogProcessing"), false);

			// Parameter: EMAIL Active
			errorText = "--> On processing parameter: Email Active";
			this.emailActive = this.getContext().getConfigurationManager().getPropertyAsBooleanValue(this.getContext(), ResourceManager.configuration(this.getContext(), "Email", "Active"), false);

			// Parameter: EMAIL SMTP host
			errorText = "--> On processing parameter: Email SMTP host";
			this.smtpHost = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(this.getContext(), "Email", "SmtpHost"), (this.emailActive == true));

			// Parameter: EMAIL SMTP port
			errorText = "--> On processing parameter: Email SMTP port";
			Integer iValue = this.getContext().getConfigurationManager().getPropertyAsIntegerValue(this.getContext(), ResourceManager.configuration(this.getContext(), "Email", "SmtpPort"), (this.emailActive == true));
			if (iValue != null) this.smtpPort = iValue;

			// Parameter: EMAIL Account name
			errorText = "--> On processing parameter: EMAIL Account name";
			this.accountName = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(this.getContext(), "Email", "Account"), (this.emailActive == true));

			// Parameter: EMAIL Account password
			errorText = "--> On processing parameter: EMAIL Account password";
			this.accountPassword = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(this.getContext(), "Email", "Password"), (this.emailActive == true));

			// Parameter: EMAIL CC
			errorText = "--> On processing parameter: EMAIL CC";
			this.cc = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(this.getContext(), "Email", "CC"), false);

			// Parameter: EMAIL BCC
			errorText = "--> On processing parameter: EMAIL BCC";
			this.bcc = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(this.getContext(), "Email", "BCC"), false);

			// Parameter: EMAIL Return address
			errorText = "--> On processing parameter: EMAIL Return address";
			this.returnAddress = this.getContext().getConfigurationManager().getProperty(this.getContext(), ResourceManager.configuration(this.getContext(), "Email", "ReturnAddress"), (this.emailActive == true));
		}
		catch (Exception e)
		{
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Watchdog", "ErrorOnReadingConfigurationParameter"), errorText, e);
			return false;
		}
		finally
		{
			this.configurationDone = true;
		}

		// Return
		if (isError == true)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Check if a resource identifier or a context identifier matches the
	 * WATCHDOG conditions.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param watchdogDistributionList
	 *            The mailing list of configuration as HashMap.
	 * 
	 * @param resourceIdentifier
	 *            The resource identifier to check.
	 * 
	 * @param contextIdentifier
	 *            The context identifier to check.
	 * 
	 * @return Returns <TT>true</TT> if the condition is met, otherwise
	 *         <TT>false</TT>.
	 */
	private static boolean checkWatchdogCondition(Context context, HashMap<String, Set<String>> watchdogDistributionList, String resourceIdentifier, String contextIdentifier)
	{
		Set<String> emailList = WatchdogManager.getEmailDistributionListToNotifiy(context, watchdogDistributionList, resourceIdentifier, contextIdentifier);

		if (emailList == null || emailList.isEmpty())
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Add an element to the list of WATCHDOG commands (requests).
	 * <p>
	 * Please pay attention to the tread safety of this class, because there are
	 * many threads using one and the same instance.
	 * 
	 * @param callerContext
	 *            The context of the calling (invoking) process.
	 * 
	 * @param resourceIdentifier
	 *            The resource identifier of the message or <TT>null</TT>.
	 * 
	 * @param messageText
	 *            Message text to notify or <TT>null</TT>.
	 * 
	 * @param additionalText
	 *            Additional text to notify or <TT>null</TT>.
	 * 
	 * @param resourceDocumentationText
	 *            The documentation text of the resource identifier or
	 *            <TT>null</TT> .
	 * 
	 * @param exceptionText
	 *            The exception text to notify or <TT>null</TT>.
	 */
	public void addWatchdogCommand(Context callerContext, String resourceIdentifier, String messageText, String additionalText, String resourceDocumentationText, String exceptionText, Date messageDate)
	{
		// Lock message processing
		if (this.lockMessageHandling("Watchdog", resourceIdentifier) == true) return;

		// Notify the WATCHDOG command
		try
		{
			while (true)
			{
				// Check if WATCHDOG can be processed. If the configuration is
				// not loaded yet, the commands are always add to the WATCHDOG
				// queue, because they could be requested after loading
				// configuration.
				if (this.configurationDone == true && (this.watchdogActive == false || this.emailActive == false || this.watchdogDistributionList.size() == 0))
				{
					break;
				}

				// Invoke a new WATCHDOG command to process and executes
				// WATCHDOG function. If the context is set to "suspend mode" no
				// WATCHDOG events will be processed. Please notice: The error
				// message fired inside this block can't be notified via
				// WATCHDOG. You only can see it in log files and on console.
				if (callerContext.isSuspendWatchdog() == true)
				{
					String errorText = "--> Because of suspend mode the WATCHDOG item was refused.";
					errorText += "\n--> Resource identifier to be watched: '" + resourceIdentifier + "'";
					this.getContext().getNotificationManager().notifyWatchdogError(this.getContext(), ResourceManager.notification(this.getContext(), "Watchdog", "WatchdogItemLost"), errorText, null);
					break;
				}

				// Check if the the resource identifier or the context
				// identifier are to be WATCHED. If the configuration is
				// not loaded yet, the commands are always add to the WATCHDOG
				// queue, because they could be requested after loading
				// configuration.
				if (this.configurationDone == true && (WatchdogManager.checkWatchdogCondition(this.getContext(), this.watchdogDistributionList, resourceIdentifier, context.getContextResourceContainer().getRecourceIdentifier()) == false))
				{
					break;
				}

				// Check if there are more than the maximum allowed number of
				// elements in the queue. the next element will be refused to
				// avoid WATCHDOG overflow. Please notice: The error messages
				// fired inside this block can't be notified via WATCHDOG.
				// You only can see them in log files and on console.
				if (this.configurationDone == true && this.getNumberOfWatchdogElements() >= this.watchdogMaxNuOfItemsInCommandQueue)
				{
					// Error: MaximumNumberOfCommandsExceeded
					String errorText = "--> Maximum number allowed: '" + String.valueOf(this.watchdogMaxNuOfItemsInCommandQueue) + "'";
					errorText += "\n--> Resource identifier to be watched: '" + resourceIdentifier + "'";
					this.getContext().getNotificationManager().notifyWatchdogError(this.getContext(), ResourceManager.notification(this.getContext(), "Watchdog", "MaximumNumberOfCommandsExceeded"), errorText, null);

					// Error: WatchdogItemLost
					errorText = "--> Because the maximum of allowed items of '" + String.valueOf(this.watchdogMaxNuOfItemsInCommandQueue) + "' in command queue was exceeded.";
					errorText += "\n--> Resource identifier to be watched: '" + resourceIdentifier + "'";
					this.getContext().getNotificationManager().notifyWatchdogError(this.getContext(), ResourceManager.notification(this.getContext(), "Watchdog", "WatchdogItemLost"), errorText, null);

					// Return
					break;
				}

				// Create a new WATCHDOG command
				WatchdogCommand watchdogCommand = new WatchdogCommand(callerContext, resourceIdentifier, messageText, additionalText, resourceDocumentationText, exceptionText, messageDate);

				// Add a WATCHDOG command to queue
				synchronized (this.commandQueue)
				{
					this.commandQueue.add(watchdogCommand);
				}

				// Break
				break;
			}
		}
		catch (Exception e)
		{
			// Be silent
		}
		finally
		{
			// Unlock message processing
			this.unlockMessageHandling("Watchdog");
		}
	}

	/**
	 * Open Email connection.
	 * 
	 * @return Returns <TT>true</TT> if the action was successful, otherwise
	 *         <TT>false</TT>.
	 */
	boolean openEmailConnection()
	{
		// Close email connection, if opened
		if (this.emailConnection != null) this.closeEmailConnection();

		// Open connection
		try
		{
			// Create new instance of Email Connector class, if not done yet
			if (this.emailConnection == null) this.emailConnection = new EmailConnector(context);

			// Open connection
			if (this.emailConnection.openConnection(this.smtpHost, this.smtpPort, this.accountName, this.accountPassword) == false) return false;
		}
		catch (Exception e)
		{
			String errorText = "--> Connection parameters: SmtpHost [" + this.smtpHost + "], SmtpPort [" + String.valueOf(this.smtpPort) + "], Account [" + this.accountName + "] Password [******]";
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Watchdog", "ErrorOnSendingEmail"), errorText, e);
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Close Email connection.
	 * 
	 * @return Returns <TT>true</TT> if the action was successful, otherwise
	 *         <TT>false</TT>.
	 */
	boolean closeEmailConnection()
	{
		try
		{
			if (this.emailConnection != null) this.emailConnection.closeConnection();
		}
		catch (Exception e)
		{
			String errorText = "--> On closing Email connection";
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Watchdog", "ErrorOnSendingEmail"), errorText, e);
			return false;
		}

		return true;
	}

	/**
	 * Sends an Email.
	 * 
	 * @param emailAddress
	 *            Recipient of email.
	 * 
	 * @param ServerwatchdogCommand
	 *            WATCHDOG command object containing all information to send via
	 *            email.
	 * 
	 * @param subjectText
	 *            Text of email subject.
	 * 
	 * @return <b>boolean</b> Returns <TT>true</TT> if the action was
	 *         successful, otherwise<TT>false</TT>.
	 */
	boolean sendEmail(String emailAddress, WatchdogCommand watchdogCommand)
	{
		if (this.emailConnection == null) return false;

		String subjectText = "";
		String bodyText = "";
		String parameterText = "";
		String separatingLine = "--------------------------------------------------------";

		try
		{
			// Subject
			subjectText += "Watchdog";
			subjectText += " [" + watchdogCommand.getContext().getApplicationName() + "]";
			subjectText += " [" + String.valueOf(watchdogCommand.getContext().getApplicationVersion()) + "]";
			subjectText += " [" + watchdogCommand.getContext().getCodeName() + "]";
			ResourceContainer resourceContainer = context.getResourceManager().getProvisionalResourceContainer(watchdogCommand.getResourceIdentifier());
			subjectText += " on Event {" + resourceContainer.getType() + " " + resourceContainer.getGroup() + " " + resourceContainer.getName() + "}";

			// Line
			bodyText += separatingLine + "\n" + "Watchdog Notification" + "\n" + separatingLine;

			// Application name
			parameterText = watchdogCommand.getContext().getApplicationName();
			if (parameterText != null && parameterText.length() > 0) bodyText += "\n" + watchdogCommand.getContext().getOriginName() + " " + "Application: " + parameterText;

			// Application version
			parameterText = String.valueOf(watchdogCommand.getContext().getApplicationVersion());
			if (parameterText != null && parameterText.length() > 0) bodyText += "\nVersion: " + parameterText;

			// Code name
			parameterText = watchdogCommand.getContext().getCodeName();
			if (parameterText != null && parameterText.length() > 0) bodyText += "\nCode name: " + parameterText;

			// Context
			resourceContainer = context.getResourceManager().getProvisionalResourceContainer(watchdogCommand.getContextIdentifier());
			parameterText = resourceContainer.getGroup() + " " + resourceContainer.getName();
			if (parameterText != null && parameterText.length() > 0) bodyText += "\nContext: " + parameterText;

			// Event
			resourceContainer = context.getResourceManager().getProvisionalResourceContainer(watchdogCommand.getResourceIdentifier());
			parameterText = resourceContainer.getType() + " " + resourceContainer.getGroup() + " " + resourceContainer.getName();
			if (parameterText != null && parameterText.length() > 0) bodyText += "\nRegarding: " + parameterText;

			// Date
			if (watchdogCommand.getMessageDate() != null)
			{
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
				bodyText += "\nDate: " + simpleDateFormat.format(watchdogCommand.getMessageDate());
			}

			// Resource identifier
			parameterText = watchdogCommand.getResourceIdentifier();
			if (parameterText != null && parameterText.length() > 0) bodyText += "\nIdentifier to watch: " + parameterText;

			// Line
			bodyText += "\n" + separatingLine + "\n";

			// Additional text
			parameterText = watchdogCommand.getAdditionalText();

			if (parameterText != null && parameterText.length() > 0)
			{
				bodyText += "\n\n" + separatingLine + "\n" + "Additional information" + "\n" + separatingLine;
				bodyText += "\n" + parameterText;
				bodyText += "\n" + separatingLine + "\n";
			}

			// Exception text
			parameterText = watchdogCommand.getExceptionText();

			if (parameterText != null && parameterText.length() > 0)
			{
				bodyText += "\n\n" + separatingLine + "\n" + "Exception" + "\n" + separatingLine;
				bodyText += parameterText;
				bodyText += "\n" + separatingLine + "\n\n";
			}

			// Resource documentation
			parameterText = watchdogCommand.getResourceDocumentationText();

			if (parameterText != null && parameterText.length() > 0)
			{
				bodyText += "\n\n" + separatingLine + "\n" + "Explanation" + "\n" + separatingLine;
				bodyText += parameterText;
				bodyText += "\n" + separatingLine + "\n\n";
			}

			// Send Email via email connector
			if (this.emailConnection.sendEmail(this.getContext(), emailAddress, this.cc, this.bcc, this.returnAddress, subjectText, bodyText) == false) return false;

			// Fire event
			String eventText = "--> Email address [" + emailAddress + "], CC [" + this.cc + "], BCC [" + this.bcc + "], ReturnAddress [" + this.returnAddress + "]";
			eventText += "\n--> Subject: '" + subjectText + "'";
			this.getContext().getNotificationManager().notifyWatchdogEvent(this.getContext(), ResourceManager.notification(this.getContext(), "Watchdog", "EmailSent"), eventText, null);
		}
		catch (Exception e)
		{
			String errorText = "--> Email parameters: Email address [" + emailAddress + "], CC [" + this.cc + "], BCC [" + this.bcc + "], ReturnAddress [" + this.returnAddress + "]";
			errorText += "\n--> Subject: '" + subjectText + "'";
			this.getContext().getNotificationManager().notifyWatchdogError(this.getContext(), ResourceManager.notification(this.getContext(), "Watchdog", "ErrorOnSendingEmail"), errorText, e);
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Getter
	 */
	public int getSecondsToWaitBetweenWatchdogProcessing()
	{
		return secondsToWaitBetweenWatchdogProcessing;
	}

	/**
	 * Getter
	 */
	public HashMap<String, Set<String>> getWatchdogDistributionList()
	{
		return watchdogDistributionList;
	}

	/**
	 * Wait for the end of processing of all commands of the WATCHDOG, but after
	 * maximum of x seconds the method always returns.
	 * 
	 * @param maxTimeToWaitInSeconds
	 *            Maximum number of seconds to wait.
	 */
	public void waitForCompletingWatchdogQueue(int maxTimeToWaitInSeconds)
	{
		int counter = maxTimeToWaitInSeconds * 10;

		while (counter-- > 0)
		{
			if (this.getNumberOfWatchdogElements() <= 0) break;

			try
			{
				Thread.sleep(100);
			}
			catch (Exception e)
			{
			}
		}

		return;
	}

	/**
	 * Print Email distribution configuration.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param watchdogDistributionListResourceIdentifierEmail
	 *            The mailing list of configuration.
	 * 
	 * @return Returns the formatted distribution list as string.
	 */
	public static String printDistributionConfiguration(HashMap<String, Set<String>> watchdogDistributionListResourceIdentifierEmail)
	{
		// Initialize
		String resultString = "";
		HashMap<String, Set<String>> watchdogDistributionListEmailResourceIdentifier = new HashMap<String, Set<String>>();

		// Check parameter
		if (watchdogDistributionListResourceIdentifierEmail == null) return resultString;
		if (watchdogDistributionListResourceIdentifierEmail.size() == 0) return resultString;

		/*
		 * Print list ordered by resource identifier
		 */
		try
		{
			// Headline
			resultString += "\n### Email distribution list sorted by resource identifiers\n";

			// Sorting the keys alphabetically
			List<String> sortedListManual = new ArrayList<String>();
			sortedListManual.addAll(watchdogDistributionListResourceIdentifierEmail.keySet());
			Collections.sort(sortedListManual);

			// List all items
			Iterator<String> iterManual = sortedListManual.iterator();

			while (iterManual.hasNext())
			{
				// Get resource identifier
				String identifier = iterManual.next();
				Set<String> emailList = watchdogDistributionListResourceIdentifierEmail.get(identifier);

				if (emailList != null && emailList.size() > 0)
				{
					resultString += "\n" + identifier.trim() + "\n";

					List<String> sortedEmailList = new ArrayList<String>(emailList);
					Collections.sort(sortedEmailList);

					for (String email : sortedEmailList)
					{
						if (email != null && email.length() > 0)
						{
							// Print on list
							resultString += "  --> " + email + "\n";

							// Save email items and resource items in second
							// hash map
							Set<String> resourceIdentifierList = watchdogDistributionListEmailResourceIdentifier.get(email);
							if (resourceIdentifierList == null) resourceIdentifierList = new HashSet<String>();
							resourceIdentifierList.add(identifier.trim());
							watchdogDistributionListEmailResourceIdentifier.put(email, resourceIdentifierList);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		/*
		 * Print list ordered by Email address
		 */
		try
		{
			// Headline
			resultString += "\n\n### Email distribution list sorted by Email addresses\n";

			// Sorting the keys alphabetically
			List<String> sortedListManual = new ArrayList<String>();
			sortedListManual.addAll(watchdogDistributionListEmailResourceIdentifier.keySet());
			Collections.sort(sortedListManual);

			// List all items
			Iterator<String> iterManual = sortedListManual.iterator();

			while (iterManual.hasNext())
			{
				// Get resource identifier
				String email = iterManual.next();
				Set<String> resourceIdentifierList = watchdogDistributionListEmailResourceIdentifier.get(email);

				if (resourceIdentifierList != null && resourceIdentifierList.size() > 0)
				{
					resultString += "\n" + email.trim() + "\n";

					List<String> sortedResourceIdentifierList = new ArrayList<String>(resourceIdentifierList);
					Collections.sort(sortedResourceIdentifierList);

					for (String resourceIdentifier : sortedResourceIdentifierList)
					{
						if (resourceIdentifier != null && resourceIdentifier.length() > 0) resultString += "  --> " + resourceIdentifier + "\n";
					}
				}
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return resultString;
	}

	/**
	 * Getter
	 */
	public Context getContext()
	{
		return context;
	}

	/**
	 * Getter
	 */
	public void setContext(Context context)
	{
		this.context = context;
	}

	/**
	 * Getter
	 */
	public boolean isWatchdogActive()
	{
		return watchdogActive;
	}

	/**
	 * Getter
	 */
	public boolean isEmailActive()
	{
		return emailActive;
	}

	/**
	 * Getter
	 */
	public boolean isConfigurationDone()
	{
		return configurationDone;
	}
}
