package fmagic.basic.watchdog;

import java.util.Set;

import fmagic.basic.context.Context;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * This class implements the WATCHDOG server running all time, checks for new
 * commands to send, and sends the WATCHDOG notifications via Email.
 * <p>
 * Please pay attention to the tread safety of this class, because there are
 * many threads using one and the same instance.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 16.01.2013 - Created
 * 
 */
public class WatchdogServer extends Thread
{
	final private WatchdogManager watchdogManager;
	final private Context initializingContext;

	// Flag if processing is to be continued
	private boolean stopRunning = false;

	/**
	 * Constructor
	 * 
	 * @param watchdogContext
	 *            The watchdogContext to use.
	 * 
	 * @param watchdogManager
	 *            The WATCHDOG manager to use.
	 */
	public WatchdogServer(Context initializingContext,
			WatchdogManager watchdogManager)
	{
		this.watchdogManager = watchdogManager;
		this.initializingContext = initializingContext;
	}

	/**
	 * Thread running method.
	 */
	@Override
	public void run()
	{
		// Fire Event: Server started
		initializingContext.getNotificationManager().notifyEvent(initializingContext, ResourceManager.notification(initializingContext, "Watchdog", "WatchdogServerStarted"), null, null);

		// Processing WATCHDOG commands
		while (true)
		{
			// Check if the thread is forced to end
			if (this.isInterrupted()) break;
			if (this.stopRunning == true)  break;

			// Sleep x seconds
			try
			{
				Thread.sleep(this.watchdogManager.getSecondsToWaitBetweenWatchdogProcessing() * 1000);
			}
			catch (InterruptedException e)
			{
				break;
			}

			// Check COMMAND queue
			try
			{
				this.process();
			}
			catch (Exception e)
			{
				String errorText = "--> On running WATCHDOG processing cycle";
				this.watchdogManager.getContext().getNotificationManager().notifyError(this.watchdogManager.getContext(), ResourceManager.notification(this.watchdogManager.getContext(), "Watchdog", "ErrorOnProcessingWatchdog"), errorText, e);
			}

			// Check on error (Silent dump)
			try
			{
				String dumpFirstErrorIdentifier = this.watchdogManager.getContext().getDumpFirstErrorIdentifier();

				if (dumpFirstErrorIdentifier != null && dumpFirstErrorIdentifier.length() > 0)
				{
					this.watchdogManager.getContext().flushDump();
				}
			}
			catch (Exception e)
			{
				String errorText = "--> On flushing dump";
				this.watchdogManager.getContext().getNotificationManager().notifyError(this.watchdogManager.getContext(), ResourceManager.notification(this.watchdogManager.getContext(), "Watchdog", "ErrorOnProcessingWatchdog"), errorText, e);
			}
		}

		// Fire Event: Server stopped
		initializingContext.getNotificationManager().notifyEvent(initializingContext, ResourceManager.notification(initializingContext, "Watchdog", "WatchdogServerStopped"), null, null);
	}

	/**
	 * Interrupts (stops) the thread.
	 */
	@Override
	public void interrupt()
	{
		super.interrupt();
		this.stopRunning = true;
	}

	/**
	 * Start WATCHDOG server.
	 * 
	 * @param context
	 *            The context to use for initialization phase. This context is
	 *            to be set by the application because of the WATCHDOG has is
	 *            own context independently from the outside world.
	 * 
	 * @return Returns <TT>true</TT> if the server could be started, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean startServer(Context context)
	{
		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Starting WATCHDOG server [" + context.getCodeName() + "]: " + this.toString());

		// Read configuration items
		if (context.getWatchdogManager().readConfiguration() == false) return false;

		// Start server
		try
		{
			this.start();
		}
		catch (Exception e)
		{
			String errorText = "--> On starting WATCHDOG server";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Watchdog", "ErrorOnProcessingWatchdog"), errorText, e);
			return false;
		}

		// Create event: DistributionListNotification
		String distributionText = WatchdogManager.printDistributionConfiguration(context.getWatchdogManager().getWatchdogDistributionList());
		context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "Watchdog", "DistributionListNotification"), distributionText, null);

		// Set processing context to a specific WATCHDOG context
		try
		{
			ResourceContainer contextResource = ResourceManager.context(context, "Watchdog", "Processing");
			this.watchdogManager.setContext(context.createSilentDumpContext(contextResource, true));
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Watchdog", "ErrorOnProcessingWatchdog"), null, e);
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Stop WATCHDOG server.
	 * 
	 * @param context
	 *            The context to use for shutdown phase. This context is to be
	 *            set by the application because of the WATCHDOG has is own
	 *            context independently from the outside world.
	 */
	public void stopServer(Context context)
	{
		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Stopping WATCHDOG server [" + context.getCodeName() + "]: " + this.toString());

		// Reset WATCHDOG context back to the context of the outside world
		this.watchdogManager.setContext(context);

		// Stop WATCHDOG server
		try
		{
			context.getWatchdogManager().waitForCompletingWatchdogQueue(60);
			context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "Watchdog", "WatchdogServerInterrupted"), null, null);
			this.interrupt();
		}
		catch (Exception e)
		{
			String errorText = "--> On stopping WATCHDOG server";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Watchdog", "ErrorOnProcessingWatchdog"), errorText, e);
		}
	}

	/**
	 * Processing of the WATCHDOG command queue.
	 * <p>
	 * Please notice: You have to check if the thread was interrupted meanwhile
	 * by the application, especially when you are processing cycle statements
	 * like <TT>for(...)</TT> or <TT>while(...)</TT>. Please be careful!
	 * 
	 * @return Returns <TT>true</TT> if the processing was successful, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean process()
	{
		// Initialize
		boolean emailConnectionError = false;
		boolean emailConnectionOpened = false;

		/*
		 * Start of processing
		 */

		// Configuration already read?
		if (!this.watchdogManager.isConfigurationDone()) return true;

		// Something to do?
		if (this.watchdogManager.getNumberOfWatchdogElements() <= 0) return true;

		// Check if watchdogContext is already set
		if (this.watchdogManager.getContext() == null) return false;

		// Logging
		this.watchdogManager.getContext().getNotificationManager().notifyLogMessage(this.watchdogManager.getContext(), NotificationManager.SystemLogLevelEnum.NOTICE, "WATCHDOG processing has started next cycle");

		// Check if the thread is forced to end
		if (this.isInterrupted()) return true;

		/*
		 * Get through the WATCHDOG command list
		 */
		try
		{
			while (this.watchdogManager.getNumberOfWatchdogElements() > 0)
			{
				// Check if the thread is forced to end
				if (this.isInterrupted()) break;

				// Read WATCHDOG command from queue
				WatchdogCommand watchdogCommand = this.watchdogManager.getWatchdogCommand();
				if (watchdogCommand == null) break;

				// Check if WATCHDOG and EMAIL is active 
				if (this.watchdogManager.isWatchdogActive() == false || this.watchdogManager.isEmailActive() == false) continue;

				// Get Email list to send
				Set<String> emailDistributionList = WatchdogManager.getEmailDistributionListToNotifiy(this.watchdogManager.getContext(), this.watchdogManager.getWatchdogDistributionList(), watchdogCommand.getResourceIdentifier(), watchdogCommand.getContextIdentifier());
				if (emailDistributionList == null || emailDistributionList.isEmpty()) continue;

				// Check Email list and processes all email recipients
				for (String emailAddress : emailDistributionList)
				{
					// Open Email connection, first time before an Email is
					// really to be sent
					if (emailConnectionOpened == false && emailConnectionError == false)
					{
						if (this.watchdogManager.openEmailConnection() == true)
						{
							emailConnectionOpened = true;
						}
						else
						{
							emailConnectionError = true;
						}
					}

					// Send next Email
					if (emailConnectionOpened == true)
					{
						this.watchdogManager.sendEmail(emailAddress, watchdogCommand);
					}
					else
					{
						String errorText = "--> Because of error on Email connection";
						errorText += "\n--> Email address to be notified: '" + emailAddress + "'";
						errorText += "\n--> Resource identifier to be watched: '" + watchdogCommand.getResourceIdentifier() + "'";
						this.watchdogManager.getContext().getNotificationManager().notifyError(this.watchdogManager.getContext(), ResourceManager.notification(this.watchdogManager.getContext(), "Watchdog", "WatchdogItemLost"), errorText, null);
					}
				}
			}
		}
		catch (Exception e)
		{
			this.watchdogManager.getContext().getNotificationManager().notifyError(this.watchdogManager.getContext(), ResourceManager.notification(this.watchdogManager.getContext(), "Watchdog", "ErrorOnProcessingWatchdog"), null, e);
		}

		/*
		 * Stop of processing
		 */

		// Close email connection
		if (this.watchdogManager.closeEmailConnection() == false) return false;

		// Logging
		this.watchdogManager.getContext().getNotificationManager().notifyLogMessage(this.watchdogManager.getContext(), NotificationManager.SystemLogLevelEnum.NOTICE, "WATCHDOG processing has ended actual cycle");

		// Return
		return true;
	}
}
