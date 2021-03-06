package fmagic.basic.notification;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import fmagic.basic.application.ManagerInterface;
import fmagic.basic.context.Context;
import fmagic.basic.file.FileLocationFunctions;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;
import fmagic.test.application.TestManager;

/**
 * This class implements an interface for providing functions for notification.
 * You can notify messages, errors and events to log files and to a WATCHDOG.
 * Please invoke <TT>notifyMessage()</TT> to notify a log message or an error
 * message to log files and to the WATCHDOG. Please invoke
 * <TT>notifyEvent()</TT> to notify events to log files or to the WATCHDOG.
 * Please invoke <TT>flushDump()</TT> to notify all stored messages of a dump
 * context.
 * <p>
 * Please notice: If you use the context type <TT>DUMP</TT> or
 * <TT>SILENT DUMP</TT> messages are notified only if an error occurred or if
 * you flush the stored messages manually. Otherwise the stored messages will be
 * rejected. At the end of the day it means that no log messages will be written
 * if there were no errors detected.
 * <p>
 * There are three types of log files were messages are written to. Each log
 * message or error message or event message is written parallel in three
 * different log directories and log files. The log files are stored in the same
 * directory as the JAR files.
 * <ul>
 * <li><TT>DATE_CONTEXT</TT>: For each date a new sub directory is created. In
 * the daily directory you find several log files respectively with the name of
 * the application (<TT>Code name</TT>) plus the name of a context plus a date
 * string, e. g. <TT>apps1-CONTEXT_CONFIGURATION_DUMP-20090403.log</TT>. In each
 * file you only find messages of the given application and of the given context
 * and the given date. All messages of this type are written under the root
 * directory <TT>fmagic.logging.context</TT>.</li>
 * <li><TT>APPLICATION_DATE</TT>: For each application (<TT>Code name</TT>) a
 * new sub directory is created. In the application directories you find sub
 * directories containing the messages of a given date. The log file names
 * contain the code name of the application plus the date, e. g.
 * <TT>apps1-20090403.log</TT>. In each file you only find messages of the given
 * application and of the given date. All messages of this type are written
 * under the root directory <TT>fmagic.logging.application</TT>.</li>
 * <li><TT>FLAT</TT>: All log files are written to a single directory regardless
 * of application or date. The log file names contain a constant prefix plus the
 * date, e. g. <TT>prefix-20090403.log</TT>. In each file you only find messages
 * of the given date. All messages of this type are written under the root
 * directory <TT>fmagic.logging.flat</TT>.</li>
 * </ul>
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class NotificationManager implements ManagerInterface
{
	// Log file type
	public static enum LogfileTypeEnum
	{
		DATE_CONTEXT,
		APPLICATION_DATE,
		FLAT,
		TICKET_DOC,
		TICKET_LOG,
		TEST_LOGGING
	}

	// Log level
	public static enum SystemLogLevelEnum
	{
		NOTICE, WARNING, ERROR, TRACKING, CODE
	}

	// Flags for synchronizing the locking of messages
	private ConcurrentHashMap<String, Boolean> processingActive = new ConcurrentHashMap<String, Boolean>();
	private int messageLostCounter = 0;

	// Configuration parameter
	private int cleanDaysToKeep = 0;

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = { "Notification" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceTemplate(context, includingResourceIdentifiers, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public String printManual(Context context)
	{
		String dumpText = "";

		String typeCriteria[] = { "Notification" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceManual(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public String printIdentifierList(Context context)
	{
		String dumpText = "";

		String typeCriteria[] = { "Notification" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceIdentifierList(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public boolean validateResources(Context context)
	{
		return false;
	}

	@Override
	public boolean readConfiguration(Context context)
	{
		boolean isError = false;

		if (readConfigurationCleaningConfigurationParameter(context) == true) isError = true;

		/*
		 * Return
		 */
		return isError;
	}

	@Override
	public boolean cleanEnvironment(Context context)
	{
		// Clean environment
		this.cleanAll(context);

		// Return
		return false;
	}

	/**
	 * Format a log message or error message to a common string format.
	 * 
	 * @param messageDate
	 *            Date of the message.
	 * 
	 * @param codeName
	 *            Code name of the application.
	 * 
	 * @param messageText
	 *            Message to notify.
	 * 
	 * @return Returns the formatted string.
	 */
	private String messageFormatter(Date messageDate, String codeName, String messageText)
	{
		// Initialize variables
		String formattedString = "\n...........................................\n";

		// Add date string
		if (messageDate == null) messageDate = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		formattedString += simpleDateFormat.format(messageDate);

		// Add code name
		formattedString += " [" + codeName + "]";

		// Add thread identification
		formattedString += " [" + String.format("%04d", Thread.currentThread().getId()) + "]";

		// Add free memory
		formattedString += " {" + String.valueOf(Runtime.getRuntime().freeMemory() / (1024 * 1024)) + "}";

		// Add message text
		formattedString += " " + messageText + "";

		return formattedString;
	}

	/**
	 * Notify a log message.
	 * 
	 * @param context
	 *            The context to use.
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
	 * @param exception
	 *            The exception to notify or <TT>null</TT>.
	 * 
	 * @param isEvent
	 *            Information about event status of message, if it is an event
	 *            message or not.
	 * 
	 * @param isError
	 *            Information about error status of message, if it is an error
	 *            message or not.
	 * 
	 * @param isWatchdogMessage
	 *            If this flag is set to <TT>true</TT> a WATCHDOG message is to
	 *            be notified. In this case there is no check on recursivity (no
	 *            LOCK), and the <TT>addWatchdogCommand()</TT> is not invoked.
	 */
	private void notifyMessage(Context context, String resourceIdentifier, String messageText, String additionalText, String resourceDocumentationText, Exception exception, boolean isEvent, boolean isError, boolean isWatchdogMessage)
	{
		// Get message date
		Date messageDate = new Date();

		/*
		 * Write resource documentation to the dump list of resource
		 * documentation
		 */
		if (resourceIdentifier != null && resourceDocumentationText != null && resourceDocumentationText.length() > 0)
		{
			context.addDumpResourceDocumentationElement(resourceIdentifier, resourceDocumentationText);
		}

		/*
		 * Get formatted message text
		 */
		String formattedString = messageFormatter(messageDate, context.getCodeName(), messageText);

		/*
		 * Create exception text
		 */
		String exceptionText = "";

		if (exception != null)
		{
			// Prepare as internal string
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			exception.printStackTrace(printWriter);
			exceptionText = writer.toString();

			// Print always on console
			exception.printStackTrace(System.err);
		}

		/*
		 * Get formatted exception text
		 */
		if (exceptionText != null && exceptionText.length() > 0) exceptionText = "\n" + exceptionText;

		/*
		 * If the message is an error message
		 */
		if (isError == true)
		{
			// Set first error message
			context.setDumpFirstError(resourceIdentifier, additionalText, resourceDocumentationText, exception);
		}

		/*
		 * Print messages depending on the context type
		 */
		if (context.getContextType() == Context.ContextTypeEnum.DUMP)
		{
			// Register messages in dump list
			context.addDumpMessageElement(formattedString);
			if (additionalText != null && additionalText.length() > 0) context.addDumpMessageElement(additionalText);
			if (exceptionText != null && exceptionText.length() > 0) context.addDumpMessageElement(exceptionText);

			// Print header of event notification always on console
			if (isEvent == true)
			{
				System.out.println(formattedString);
			}

			// Firing dump
			if (isError == true) this.flushDump(context);
		}
		else if (context.getContextType() == Context.ContextTypeEnum.SILENT_DUMP)
		{
			// Register messages in dump list
			context.addDumpMessageElement(formattedString);
			if (additionalText != null && additionalText.length() > 0) context.addDumpMessageElement(additionalText);
			if (exceptionText != null && exceptionText.length() > 0) context.addDumpMessageElement(exceptionText);

			// Print event notification on console
			if (isEvent == true)
			{
				System.out.println(formattedString);
				if (additionalText != null && additionalText.length() > 0) System.out.println(additionalText);
				if (exceptionText != null && exceptionText.length() > 0) System.out.println(exceptionText);
			}
		}
		else
		{
			// Processing message text
			if (isError == true)
			{
				System.err.println(formattedString);
			}
			else
			{
				System.out.println(formattedString);
			}

			context.getNotificationManager().writeMessageToLogfile(context, "\n" + formattedString);

			// Processing additional text
			if (additionalText != null && additionalText.length() > 0)
			{
				if (isError == true)
				{
					System.err.println(additionalText);
				}
				else
				{
					System.out.println(additionalText);
				}

				context.getNotificationManager().writeMessageToLogfile(context, "\n\n" + additionalText);
			}

			// Processing exceptional text
			if (exceptionText != null && exceptionText.length() > 0)
			{
				System.err.println(exceptionText);
				context.getNotificationManager().writeMessageToLogfile(context, "\n\n" + exceptionText);
			}
		}

		// Flush console
		System.out.flush();

		// Notifies WATCHDOG
		if (!isWatchdogMessage && (isEvent || isError)) this.notifyWatchdog(context, resourceIdentifier, messageText, additionalText, resourceDocumentationText, exceptionText, messageDate);
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
	 * Notify an event.
	 * <p>
	 * Please notice: It is recommended to invoke the static method
	 * <TT>ResourceManager.notification(context, group, name)</TT> to get a
	 * resource container. The signature <TT>notification</TT> sets the
	 * <TT>type</TT> of the resource. Together with the parameters
	 * <TT>group</TT> and <TT>name</TT> you have an unique identification.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param resourceContainer
	 *            The resource container that sets the property identifier.
	 * 
	 * @param additionalText
	 *            Additional text of the event.
	 * 
	 * @param exception
	 *            Exception object containing detail information or
	 *            <TT>null</TT>, if not available.
	 */
	public void notifyEvent(Context context, ResourceContainer resourceContainer, String additionalText, Exception exception)
	{
		this.notifyEvent(context, resourceContainer, additionalText, exception, false);
	}

	/**
	 * Notify an event.
	 * <p>
	 * Please notice: You have to avoid recursive calls of this method.
	 * Recursive calls can occur if an event is fired during a running event.
	 * Therefore the methods <TT>lockMessageHandling()</TT> and
	 * <TT>unlockMessageHandling()</TT> are used in a thread safe mode.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param resourceContainer
	 *            The resource container that sets the property identifier.
	 * 
	 * @param additionalText
	 *            Additional text of the event.
	 * 
	 * @param exception
	 *            Exception object containing detail information or
	 *            <TT>null</TT>, if not available.
	 * 
	 * @param isWatchdogMessage
	 *            If this flag is set to <TT>true</TT> a WATCHDOG message is to
	 *            be notified. In this case there is no check on recursivity (no
	 *            LOCK), and the <TT>addWatchdogCommand()</TT> is not invoked.
	 */
	private void notifyEvent(Context context, ResourceContainer resourceContainer, String additionalText, Exception exception, boolean isWatchdogMessage)
	{
		// Check parameter
		if (resourceContainer == null) return;

		// Lock message processing
		if (!isWatchdogMessage)
		{
			if (this.lockMessageHandling("Event", resourceContainer.getRecourceIdentifier()) == true) return;
		}

		// Notify the event
		try
		{
			while (true)
			{
				// Check parameters
				if (resourceContainer == null) break;

				// Get event identifier
				String enumIdentifier = resourceContainer.getRecourceIdentifier();
				if (enumIdentifier == null) break;

				// Get resource documentation
				String enumDocumentationText = resourceContainer.printManual(context);

				// Create message text
				String messageText = "Event {" + enumIdentifier + "}";

				// Notify message
				context.getNotificationManager().notifyMessage(context, enumIdentifier, messageText, additionalText, enumDocumentationText, exception, true, false, isWatchdogMessage);

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
			if (!isWatchdogMessage)
			{
				this.unlockMessageHandling("Event");
			}
		}

		// Return
		return;
	}

	/**
	 * Notify the WATCHDOG about errors, events or context usages.
	 * <p>
	 * Notice: The method <TT>notifyWatchdog()</TT> is overloaded and used for
	 * the message types EVENT, ERROR and CONTEXT.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param messageText
	 *            The message to notify.
	 * 
	 * @param additionalText
	 *            Additional text of the message or <TT>null</TT>.
	 * 
	 * @param notificationResourceIdentifier
	 *            The resource identifier of the error or event.
	 */
	private void notifyWatchdog(Context context, String resourceIdentifier, String messageText, String additionalText, String resourceDocumentationText, String exceptionText, Date messageDate)
	{
		if (context.getWatchdogManager() != null) context.getWatchdogManager().addWatchdogCommand(context, resourceIdentifier, messageText, additionalText, resourceDocumentationText, exceptionText, messageDate);
	}

	/**
	 * Flush a dump list and initializes it again.
	 * 
	 * @param context
	 *            Application context.
	 */
	public void flushDump(Context context)
	{
		/*
		 * Empty dump list
		 */
		if (context.getDumpListMessages() == null || context.getDumpListMessages().size() == 0) return;

		/*
		 * Preparing main error information
		 */
		String dumpMessageText = "";

		if (context.getDumpFirstErrorIdentifier() != null)
		{
			try
			{
				// Prepare the header of the block
				dumpMessageText += "\n------------------------------";
				dumpMessageText += "\n ERROR on application [" + context.getCodeName() + "]";
				dumpMessageText += "\n------------------------------";
				dumpMessageText += "\n\n" + context.getDumpFirstErrorIdentifier();

				// Add Additional text of the error
				String additionalText = context.getDumpFirstErrorAdditionalText();

				if (additionalText != null && additionalText.length() > 0)
				{
					dumpMessageText += "\n" + additionalText;
				}

				// Add exception text of the error
				Exception exception = context.getDumpFirstErrorException();

				if (exception != null)
				{
					if (exception != null)
					{
						Writer writer = new StringWriter();
						PrintWriter printWriter = new PrintWriter(writer);
						exception.printStackTrace(printWriter);
						String exceptionText = writer.toString();

						dumpMessageText += "\n\n" + exceptionText;
					}
				}

				// Add shot down notification text
				String shutDownNotificationText = context.getDumpFirstErrorShutdownNotificationText();

				if (shutDownNotificationText != null && shutDownNotificationText.length() > 0)
				{
					dumpMessageText += "\n\n";
					dumpMessageText += "\n##############################################################";
					dumpMessageText += "\n##############################################################";
					dumpMessageText += "\n###  " + shutDownNotificationText;
					dumpMessageText += "\n##############################################################";
					dumpMessageText += "\n##############################################################";
					dumpMessageText += "\n\n";
				}

				// Add resource documentation text of the error
				String documentationText = context.getDumpFirstErrorResourceDocumentationText();

				if (documentationText != null && documentationText.length() > 0)
				{
					dumpMessageText += "\n\n--------------------------";
					dumpMessageText += "\n" + documentationText;
					dumpMessageText += "\n\n--------------------------";
				}

				dumpMessageText += "\n\n\n";
			}
			catch (Exception e)
			{
				// Be silent
			}
		}

		/*
		 * Preparing the message dump block
		 */
		try
		{
			dumpMessageText += "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
			dumpMessageText += "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
			dumpMessageText += "\n+++ Dump: " + context.getContextResourceContainer().getAliasName();
			dumpMessageText += "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
			dumpMessageText += "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
			dumpMessageText += "\n";

			for (String messageText : context.getDumpListMessages())
			{
				if (messageText != null) dumpMessageText += "\n" + messageText;
			}

			dumpMessageText += "\n";
			dumpMessageText += "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
			dumpMessageText += "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
			dumpMessageText += "\n+++ End of Dump";
			dumpMessageText += "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
			dumpMessageText += "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
			dumpMessageText += "\n";
		}
		catch (Exception e)
		{
			// Be silent
		}

		/*
		 * Preparing the documentation dump block
		 */
		String dumpDocumentationText = "";

		try
		{
			dumpDocumentationText += "++++++++++++++++++++++++++++++";
			dumpDocumentationText += "\n++++++++++++++++++++++++++++++";
			dumpDocumentationText += "\n+++ resource Documentation";
			dumpDocumentationText += "\n++++++++++++++++++++++++++++++";
			dumpDocumentationText += "\n++++++++++++++++++++++++++++++";

			// Sorting the keys alphabetically
			List<String> sortedListManual = new ArrayList<String>();
			sortedListManual.addAll(context.getDumpListResourceDocumentation().keySet());
			Collections.sort(sortedListManual);

			// List all items
			Iterator<String> iterManual = sortedListManual.iterator();

			if (iterManual.hasNext())
			{
				String separatorString = "......................................................................................";

				while (iterManual.hasNext())
				{
					// Get ENUM
					String identifier = iterManual.next();
					String messageText = context.getDumpListResourceDocumentation().get(identifier);
					if (messageText != null)
					{
						dumpDocumentationText += "\n\n\n" + separatorString;
						dumpDocumentationText += "\n" + messageText;
					}
				}

				dumpDocumentationText += "\n\n\n" + separatorString;
			}

			dumpDocumentationText += "\n\n\n";
			dumpDocumentationText += "\n------------------------------";
			dumpDocumentationText += "\n------------------------------";
			dumpDocumentationText += "\n--- End of Documentation";
			dumpDocumentationText += "\n------------------------------";
			dumpDocumentationText += "\n------------------------------";
			dumpDocumentationText += "\n";
		}
		catch (Exception e)
		{
			// Be silent
		}

		/*
		 * Create ticket number for the dump
		 */
		String ticket = this.createErrorDumpTicketNumber(context);

		/*
		 * Write ticket file: Message dump
		 */
		String fileNameMessageDump = this.getLogfileName(context, NotificationManager.LogfileTypeEnum.TICKET_LOG, ticket, null, null);

		try
		{
			// Gets file path of the ticket files
			String pathDirectoryMessageDump = this.getLogfilePath(context, NotificationManager.LogfileTypeEnum.TICKET_LOG);
			String pathFileNameMessageDump = FileLocationFunctions.compileFilePath(this.getLogfilePath(context, NotificationManager.LogfileTypeEnum.TICKET_LOG), fileNameMessageDump);

			// Create directory
			File directory = new File(pathDirectoryMessageDump);
			directory.mkdirs();

			// Write to log file
			PrintWriter output = new PrintWriter(new FileOutputStream(new File(pathFileNameMessageDump), false));
			output.append(FileUtilFunctions.generalNormalizeNewLine(dumpMessageText));
			output.close();
		}
		catch (Exception e)
		{
			// Be silent
		}

		/*
		 * Write ticket file: Documentation dump
		 */
		String fileNameDocumentationDump = this.getLogfileName(context, NotificationManager.LogfileTypeEnum.TICKET_DOC, ticket, null, null);

		try
		{
			// Gets file path of the ticket files
			String pathDirectoryDocumentationDump = this.getLogfilePath(context, NotificationManager.LogfileTypeEnum.TICKET_DOC);
			String pathFileNameDocumentationDump = FileLocationFunctions.compileFilePath(this.getLogfilePath(context, NotificationManager.LogfileTypeEnum.TICKET_DOC), fileNameDocumentationDump);

			// Create directory
			File directory = new File(pathDirectoryDocumentationDump);
			directory.mkdirs();

			// Write to log file
			PrintWriter output = new PrintWriter(new FileOutputStream(new File(pathFileNameDocumentationDump), false));
			output.append(FileUtilFunctions.generalNormalizeNewLine(dumpDocumentationText));
			output.close();
		}
		catch (Exception e)
		{
			// Be silent
		}

		/*
		 * Fire event: ErrorDumpNotification
		 */

		try
		{
			String additionalText = "";
			additionalText += "--> Ticket: '" + ticket + "'";
			if (context.getDumpFirstErrorIdentifier() != null) additionalText += "\n--> Error: '" + context.getDumpFirstErrorIdentifier() + "'";
			additionalText += "\n--> See dump file: '" + fileNameMessageDump + "'";
			additionalText += "\n--> See resource documentation file: '" + fileNameDocumentationDump + "'";

			this.notifyEvent(context, ResourceManager.notification(context, "Notification", "ErrorDumpNotification"), additionalText, null);
		}
		catch (Exception e)
		{
			// Be silent
		}

		/*
		 * Notifies log files
		 */

		try
		{
			String messageText = "";
			messageText += "\n\n" + this.messageFormatter(new Date(), context.getCodeName(), "Dump on context: " + context.getContextResourceContainer().getAliasName());
			messageText += "\n" + dumpMessageText;

			this.writeMessageToLogfile(context, messageText);
		}
		catch (Exception e)
		{
			// Be silent
		}

		/*
		 * Notifies WATCHDOG: ErrorDumpContent
		 */

		try
		{
			ResourceContainer resourceNotifyDump = ResourceManager.notification(context, "Notification", "ErrorDumpContent");
			this.notifyWatchdog(context, resourceNotifyDump.getRecourceIdentifier(), "Dump on context: " + context.getContextResourceContainer().getAliasName(), dumpMessageText, resourceNotifyDump.printManual(context), null, new Date());
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Reset dump list
		context.resetDumpList();
	}

	/**
	 * Get dump list as a string.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the dump text as string.
	 */
	public String getDump(Context context)
	{
		String dumpText = "";

		// Empty dump list
		if (context.getDumpListMessages() == null || context.getDumpListMessages().size() == 0) return "";

		// Collect all messages
		dumpText += "";

		for (String messageText : context.getDumpListMessages())
		{
			dumpText += messageText + "\n";
		}

		// Return
		return dumpText;
	}

	/**
	 * Composes the name of a log file directory depending on the current date.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param logfileType
	 *            Log file type to consider (ENUM).
	 * 
	 * @return Returns the file path of the log files.
	 * 
	 */
	private String getLogfilePath(Context context, LogfileTypeEnum logfileType)
	{
		String fileName = "";

		if (logfileType == NotificationManager.LogfileTypeEnum.DATE_CONTEXT)
		{
			fileName = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getLogContextSubPath(), FileLocationFunctions.getLogContextSubSubPath());
		}
		else if (logfileType == NotificationManager.LogfileTypeEnum.APPLICATION_DATE)
		{
			fileName = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getLogApplicationSubPath(), FileLocationFunctions.getLogApplicationSubSubPath());
		}
		else if (logfileType == NotificationManager.LogfileTypeEnum.TICKET_LOG)
		{
			fileName = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getLogTicketSubPath(), FileLocationFunctions.getLogTicketSubSubPath());
		}
		else if (logfileType == NotificationManager.LogfileTypeEnum.TICKET_DOC)
		{
			fileName = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getLogTicketSubPath(), FileLocationFunctions.getLogTicketSubSubPath());
		}
		else if (logfileType == NotificationManager.LogfileTypeEnum.TEST_LOGGING)
		{
			fileName = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getTestLoggingSubPath(), FileLocationFunctions.getTestLoggingSubSubPath());
		}
		else
		{
			fileName = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getLogFlatSubPath());
		}

		// Replace place holder
		fileName = FileLocationFunctions.replacePlacholder(context, fileName);

		// Return
		return fileName;
	}

	/**
	 * Compose the name of a log file depending on the current date, the code
	 * name of the application and the dump name of the context.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param logfileType
	 *            Log file type (ENUM).
	 * 
	 * @param ticket
	 *            Ticket string or <TT>null</TT>.
	 * 
	 * @param application
	 *            Application name or <TT>null</TT>.
	 * 
	 * @param origin
	 *            Origin (e. g. Server,Client, All) or <TT>null</TT>.
	 * 
	 * @param type
	 *            Type (e. g. DOC, LOG) or <TT>null</TT>.
	 * 
	 * @return Returns the file name of the log file.
	 * 
	 */
	private String getLogfileName(Context context, NotificationManager.LogfileTypeEnum logfileType, String ticket, String application, String origin)
	{
		String fileName = "";

		// Get file name
		if (logfileType == NotificationManager.LogfileTypeEnum.DATE_CONTEXT)
		{
			fileName = FileLocationFunctions.getLogContextFileName();
		}
		else if (logfileType == NotificationManager.LogfileTypeEnum.APPLICATION_DATE)
		{
			fileName = FileLocationFunctions.getLogApplicationFileName();
		}
		else if (logfileType == NotificationManager.LogfileTypeEnum.FLAT)
		{
			fileName = FileLocationFunctions.getLogFlatFileName();
		}
		else if (logfileType == NotificationManager.LogfileTypeEnum.TICKET_LOG)
		{
			if (ticket != null)
			{
				fileName = ticket;
			}
			else
			{
				fileName = FileLocationFunctions.getLogTicketComposer();
			}

			fileName += "-LOG" + FileLocationFunctions.getLogTicketFileType();
		}
		else if (logfileType == NotificationManager.LogfileTypeEnum.TICKET_DOC)
		{
			if (ticket != null)
			{
				fileName = ticket;
			}
			else
			{
				fileName = FileLocationFunctions.getLogTicketComposer();
			}

			fileName += "-DOC" + FileLocationFunctions.getLogTicketFileType();
		}
		else if (logfileType == NotificationManager.LogfileTypeEnum.TEST_LOGGING)
		{
			fileName = FileLocationFunctions.getTestLoggingLogFileName();
		}
		else
		{
			fileName = FileLocationFunctions.getLogFlatFileName();
		}

		// Replace place holder in file name
		fileName = FileLocationFunctions.replacePlacholder(context, fileName);

		// Return
		return fileName;
	}

	/**
	 * Append a single text block to a log file.
	 * <p>
	 * Please note: The appending to the log file is done with the help of the
	 * synchronized method appendStringToLogFile(), in order to ensure that a
	 * message block is written coherently.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param logText
	 *            Text of the message to be logged.
	 */
	private synchronized void appendStringToLogFile(PrintWriter output, String logText)
	{
		// Check variables
		if (output == null) return;
		if (logText == null) return;
		if (logText.length() == 0) return;

		// Write to log file
		output.append(logText);
		output.flush();
	}

	/**
	 * Write a text to a log file.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param logText
	 *            Text of the message to be logged.
	 */
	private void writeMessageToLogfile(Context context, String logText)
	{
		// Normalize new line first
		String normalizedText = FileUtilFunctions.generalNormalizeNewLine(logText);

		// Write message to log file
		try
		{
			while (true)
			{
				String pathName;
				String fileName;
				File directory;
				PrintWriter output;

				/**
				 * Log file type: Test logging
				 * 
				 * If the application is running in test mode, all log file
				 * messages are stored in the specific log files instead of the
				 * regular log files.
				 */
				if (context.isRunningInTestMode())
				{
					// Gets file path and file name
					pathName = getLogfilePath(context, NotificationManager.LogfileTypeEnum.TEST_LOGGING);
					fileName = getLogfileName(context, NotificationManager.LogfileTypeEnum.TEST_LOGGING, null, null, null);

					// Create directory
					directory = new File(pathName);
					directory.mkdirs();

					// Write to log file
					output = new PrintWriter(new FileOutputStream(new File(pathName, fileName), true));
					this.appendStringToLogFile(output, normalizedText);
					output.close();

					// No other log messages
					break;
				}

				/**
				 * Log file type: Date Application
				 */

				// Gets file path and file name
				pathName = getLogfilePath(context, NotificationManager.LogfileTypeEnum.DATE_CONTEXT);
				fileName = getLogfileName(context, NotificationManager.LogfileTypeEnum.DATE_CONTEXT, null, null, null);

				// Create directory
				directory = new File(pathName);
				directory.mkdirs();

				// Write to log file
				output = new PrintWriter(new FileOutputStream(new File(pathName, fileName), true));
				this.appendStringToLogFile(output, normalizedText);
				output.close();

				/**
				 * Log file type: Application Date
				 */

				// Gets file path and file name
				pathName = getLogfilePath(context, NotificationManager.LogfileTypeEnum.APPLICATION_DATE);
				fileName = getLogfileName(context, NotificationManager.LogfileTypeEnum.APPLICATION_DATE, null, null, null);

				// Create directory
				directory = new File(pathName);
				directory.mkdirs();

				// Write to log file
				output = new PrintWriter(new FileOutputStream(new File(pathName, fileName), true));
				this.appendStringToLogFile(output, normalizedText);
				output.close();

				/**
				 * Log file type: FLAT
				 */

				// Gets file path and file name
				pathName = getLogfilePath(context, NotificationManager.LogfileTypeEnum.FLAT);
				fileName = getLogfileName(context, NotificationManager.LogfileTypeEnum.FLAT, null, null, null);

				// Create directory
				directory = new File(pathName);
				directory.mkdirs();

				// Write to log file
				output = new PrintWriter(new FileOutputStream(new File(pathName, fileName), true));
				this.appendStringToLogFile(output, normalizedText);
				output.close();

				// Break
				break;
			}
		}
		catch (Exception exception)
		{
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			exception.printStackTrace(printWriter);
			String exceptionText = writer.toString();
			System.out.println(exceptionText);
		}
	}

	/**
	 * Notify an error message.
	 * <p>
	 * Please notice: It is recommended to invoke the static method
	 * <TT>ResourceManager.notification(context, group, name)</TT> to get a
	 * resource container. The signature <TT>notification</TT> sets the
	 * <TT>type</TT> of the resource. Together with the parameters
	 * <TT>group</TT> and <TT>name</TT> you have an unique identification.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param resourceContainer
	 *            The resource container that sets the property identifier.
	 * 
	 * @param additionalText
	 *            Additional text of the event.
	 * 
	 * @param exception
	 *            Exception object containing detail information or
	 *            <TT>null</TT>, if not available.
	 */
	public void notifyError(Context context, ResourceContainer resourceContainer, String additionalText, Exception exception)
	{
		this.notifyError(context, resourceContainer, additionalText, exception, false);
	}

	/**
	 * Notify an error message.
	 * <p>
	 * Please notice: You have to avoid recursive calls of this method.
	 * Recursive calls can occur if an error is fired during a running error.
	 * Therefore the methods <TT>lockMessageHandling()</TT> and
	 * <TT>unlockMessageHandling()</TT> are used in a thread safe mode.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param resourceContainer
	 *            The resource container that sets the property identifier.
	 * 
	 * @param additionalText
	 *            Additional text of the event.
	 * 
	 * @param exception
	 *            Exception object containing detail information or
	 *            <TT>null</TT>, if not available.
	 * 
	 * @param isWatchdogMessage
	 *            If this flag is set to <TT>true</TT> a WATCHDOG message is to
	 *            be notified. In this case there is no check on recursivity (no
	 *            LOCK), and the <TT>addWatchdogCommand()</TT> is not invoked.
	 */
	private void notifyError(Context context, ResourceContainer resourceContainer, String additionalText, Exception exception, boolean isWatchdogMessage)
	{
		// Check parameter
		if (resourceContainer == null) return;

		// Handle error messages during test mode
		if (context.isRunningInTestMode())
		{
			// First: Notify the error message for the test manager
			TestManager.errorNotifyErrorMessage(context, resourceContainer.getRecourceIdentifier(), additionalText, exception);

			// Then: Check if the error message is to be suppressed
			if (TestManager.errorIsSuppressedErrorMessage(context, resourceContainer.getRecourceIdentifier())) return;
		}

		// Lock message processing
		if (!isWatchdogMessage)
		{
			if (this.lockMessageHandling("Error", resourceContainer.getRecourceIdentifier()) == true) return;
		}

		// Notify the error
		try
		{
			while (true)
			{
				// Check parameters
				if (resourceContainer == null) break;

				// Get event identifier
				String enumIdentifier = resourceContainer.getRecourceIdentifier();
				if (enumIdentifier == null) break;

				// Get resource documentation
				String enumDocumentationText = resourceContainer.printManual(context);

				// Create message text
				String messageText = "Error {" + enumIdentifier + "}";

				// Notify message
				context.getNotificationManager().notifyMessage(context, enumIdentifier, messageText, additionalText, enumDocumentationText, exception, false, true, isWatchdogMessage);

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
			if (!isWatchdogMessage)
			{
				// Unlock message processing
				this.unlockMessageHandling("Error");
			}
		}

		// Return
		return;
	}

	/**
	 * Notify a log message.
	 * <p>
	 * Please notice: You have to avoid recursive calls of this method.
	 * Recursive calls can occur if an log message is processed during a running
	 * log message. Therefore the methods <TT>lockMessageHandling()</TT> and
	 * <TT>unlockMessageHandling()</TT> are used in a thread safe mode.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param logLevel
	 *            The log level as resource.
	 * 
	 * @param logText
	 *            Text to be logged.
	 * 
	 */
	public void notifyLogMessage(Context context, SystemLogLevelEnum logLevel, String logText)
	{
		// Lock message processing
		if (this.lockMessageHandling("Log", null) == true) return;

		// Notify the log message
		try
		{
			while (true)
			{
				// Register if it is an error log code
				boolean isError = false;
				if (logLevel == SystemLogLevelEnum.ERROR) isError = true;

				// Create message text
				String messageText = "";

				if (logLevel == SystemLogLevelEnum.CODE)
				{
					messageText = "Log {" + logLevel.toString() + "} " + "\n\n" + logText + "\n";
				}
				else
				{
					messageText = "Log {" + logLevel.toString() + "} " + logText;
				}

				// notify the message
				context.getNotificationManager().notifyMessage(context, null, messageText, null, null, null, false, isError, false);

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
			this.unlockMessageHandling("Log");
		}

		// Return
		return;
	}

	/**
	 * Create an error ticket number.
	 * 
	 * @param context
	 *            Application context.
	 */
	private String createErrorDumpTicketNumber(Context context)
	{
		String ticket = FileLocationFunctions.getLogTicketComposer();
		ticket = FileLocationFunctions.replacePlacholder(context, ticket);
		return ticket;
	}

	/**
	 * Notify a WATCHDOG error message.
	 * <p>
	 * Please notice: This method should be invoked by the
	 * <TT>WatchdogManager</TT> only, because there are specific demands to
	 * consider: (1) There may not check on recursivity (no LOCK), and (2) the
	 * <TT>addWatchdogCommand()</TT> may not be invoked.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param resourceContainer
	 *            The resource container that sets the property identifier.
	 * 
	 * @param additionalText
	 *            Additional text of the event.
	 * 
	 * @param exception
	 *            Exception object containing detail information or
	 *            <TT>null</TT>, if not available.
	 */
	public void notifyWatchdogError(Context context, ResourceContainer resourceContainer, String additionalText, Exception exception)
	{
		this.notifyError(context, resourceContainer, additionalText, exception, true);
	}

	/**
	 * Notify a WATCHDOG event.
	 * <p>
	 * Please notice: This method should be invoked by the
	 * <TT>WatchdogManager</TT> only, because there are specific demands to
	 * consider: (1) There may not check on recursivity (no LOCK), and (2) the
	 * <TT>addWatchdogCommand()</TT> may not be invoked.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param resourceContainer
	 *            The resource container that sets the property identifier.
	 * 
	 * @param additionalText
	 *            Additional text of the event.
	 * 
	 * @param exception
	 *            Exception object containing detail information or
	 *            <TT>null</TT>, if not available.
	 */
	public void notifyWatchdogEvent(Context context, ResourceContainer resourceContainer, String additionalText, Exception exception)
	{
		this.notifyEvent(context, resourceContainer, additionalText, exception, true);
	}

	/**
	 * Clean all log directories.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the number of deleted files.
	 */
	public int cleanAll(Context context)
	{
		// Initialize
		Integer nuOfMovedFiles = 0;

		// Clean productive environment
		nuOfMovedFiles = nuOfMovedFiles + this.cleanProductiveEnvironment(context);

		// Clean testing environment
		nuOfMovedFiles = nuOfMovedFiles + this.cleanTestingEnvironment(context);

		// Return
		return nuOfMovedFiles;
	}

	/**
	 * Clean all log directories in the productive environment.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the number of deleted files.
	 */
	private int cleanProductiveEnvironment(Context context)
	{
		try
		{
			// Initialize
			Integer nuOfMovedFiles = 0;

			// Logging
			String logText = "\n--> CLEAN PRODUCTIVE LOG DIRECTORIES: Begin of cleaning";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Get the root directory of log files
			String directory = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getLogSubPath());
			nuOfMovedFiles = FileUtilFunctions.directoryDeleteExpiredFiles(directory, "*.log", this.cleanDaysToKeep);

			if (nuOfMovedFiles == null)
			{
				String errorString = "--> CLEAN PRODUCTIVE LOG DIRECTORIES: Error on processing cleaning.";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Notification", "ErrorOnProcessingFile"), errorString, null);
				return 0;
			}

			// / Logging
			logText = "\n--> CLEAN PRODUCTIVE LOG DIRECTORIES: End of cleaning";
			logText += "\n--> Total number of cleaned files: '" + String.valueOf(nuOfMovedFiles) + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Return
			return nuOfMovedFiles;
		}
		catch (Exception e)
		{
			String errorString = "--> CLEAN PRODUCTIVE LOG DIRECTORIES: Error on processing cleaning.";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Notification", "ErrorOnProcessingFile"), errorString, e);
			return 0;
		}
	}

	/**
	 * Clean all log directories in the testing environment.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the number of deleted files.
	 */
	private int cleanTestingEnvironment(Context context)
	{
		try
		{
			// Initialize
			Integer nuOfMovedFiles = 0;

			// Logging
			String logText = "\n--> CLEAN TESTING LOG DIRECTORIES: Begin of cleaning";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Get the root directory of log files
			String directory = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getTestSubPath());

			// Clean ASSERT files
			Integer fileCounter = FileUtilFunctions.directoryDeleteExpiredFiles(directory, "ASSERT-*.log", this.cleanDaysToKeep);

			if (fileCounter == null)
			{
				String errorString = "--> CLEAN TESTING LOG DIRECTORIES: Error on processing cleaning ASSERT files.";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Notification", "ErrorOnProcessingFile"), errorString, null);
				return 0;
			}

			nuOfMovedFiles = nuOfMovedFiles + fileCounter;

			// Clean LOGGING files
			fileCounter = FileUtilFunctions.directoryDeleteExpiredFiles(directory, "LOGGING-*.log", this.cleanDaysToKeep);

			if (fileCounter == null)
			{
				String errorString = "--> CLEAN TESTING LOG DIRECTORIES: Error on processing cleaning LOGGING files.";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Notification", "ErrorOnProcessingFile"), errorString, null);
				return 0;
			}

			nuOfMovedFiles = nuOfMovedFiles + fileCounter;

			// Clean ERROR files
			fileCounter = FileUtilFunctions.directoryDeleteExpiredFiles(directory, "ERROR-*.log", this.cleanDaysToKeep);

			if (fileCounter == null)
			{
				String errorString = "--> CLEAN TESTING LOG DIRECTORIES: Error on processing cleaning ERROR files.";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Notification", "ErrorOnProcessingFile"), errorString, null);
				return 0;
			}

			nuOfMovedFiles = nuOfMovedFiles + fileCounter;

			// / Logging
			logText = "\n--> CLEAN TESTING LOG DIRECTORIES: End of cleaning";
			logText += "\n--> Total number of cleaned files: '" + String.valueOf(nuOfMovedFiles) + "'";
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, logText);

			// Return
			return nuOfMovedFiles;
		}
		catch (Exception e)
		{
			String errorString = "--> CLEAN TESTING LOG DIRECTORIES: Error on processing cleaning.";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Notification", "ErrorOnProcessingFile"), errorString, e);
			return 0;
		}
	}

	/**
	 * Read configuration parameters regarding the cleaning services for log
	 * directories.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean readConfigurationCleaningConfigurationParameter(Context context)
	{
		try
		{
			// Read parameter: CleanDaysToKeep
			this.cleanDaysToKeep = context.getConfigurationManager().getPropertyAsIntegerValue(context, ResourceManager.configuration(context, "Notification", "CleanDaysToKeep"), false);

			// Return
			return false;
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Configuration", "IntegrityError"), null, e);
			return true;
		}
	}
}
