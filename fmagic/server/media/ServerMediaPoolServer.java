package fmagic.server.media;

import fmagic.basic.context.Context;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * This class implements the media pool server running all time, checks for new
 * commands to execute on media pool.
 * <p>
 * Please pay attention to the tread safety of this class, because there are
 * many threads using one and the same instance.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 15.05.2013 - Created
 * 
 */
public class ServerMediaPoolServer extends Thread
{
	final private ServerMediaManager mediaManager;
	final private Context initializingContext;
	private Context processingContext;

	// Flag if processing is to be continued
	private boolean stopRunning = false;

	/**
	 * Constructor
	 * 
	 * @param initializingContext
	 *            The context to use regarding initialization.
	 * 
	 * @param mediaManager
	 *            The media manager that holds the media server.
	 */
	public ServerMediaPoolServer(Context initializingContext,
			ServerMediaManager mediaManager)
	{
		this.mediaManager = mediaManager;
		this.initializingContext = initializingContext;
		this.processingContext = initializingContext;
	}

	/**
	 * Thread running method.
	 */
	@Override
	public void run()
	{
		// Fire Event: Server started
		initializingContext.getNotificationManager().notifyEvent(initializingContext, ResourceManager.notification(initializingContext, "MediaServer", "MediaServerStarted"), null, null);

		// Processing media commands
		while (true)
		{
			// Check if the thread is forced to end
			if (this.isInterrupted()) break;
			if (this.stopRunning == true) break;

			// Sleep x seconds
			try
			{
				Thread.sleep(this.mediaManager.getSecondsToWaitBetweenCommandProcessing() * 1000);
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
				String errorText = "--> Error on running media server processing cycle";
				this.processingContext.getNotificationManager().notifyError(this.processingContext, ResourceManager.notification(this.processingContext, "MediaServer", "ErrorOnProcessingServer"), errorText, e);
			}

			// Check on error (Silent dump)
			try
			{
				String dumpFirstErrorIdentifier = this.processingContext.getDumpFirstErrorIdentifier();

				if (dumpFirstErrorIdentifier != null && dumpFirstErrorIdentifier.length() > 0)
				{
					this.processingContext.flushDump();
				}
			}
			catch (Exception e)
			{
				String errorText = "--> Error on flushing dump";
				this.processingContext.getNotificationManager().notifyError(this.processingContext, ResourceManager.notification(this.processingContext, "MediaServer", "ErrorOnProcessingServer"), errorText, e);
			}
		}

		// Fire Event: Server stopped
		initializingContext.getNotificationManager().notifyEvent(initializingContext, ResourceManager.notification(initializingContext, "MediaServer", "MediaServerStopped"), null, null);
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
	 * Start media server.
	 * 
	 * @param context
	 *            The context to use for initialization phase. This context is
	 *            to be set by the application because of the media server has
	 *            its own context independently from the outside world.
	 * 
	 * @return Returns <TT>true</TT> if the server could be started, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean startServer(Context context)
	{
		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Starting media server [" + context.getCodeName() + "]: " + this.toString());

		// Start server
		try
		{
			this.start();
		}
		catch (Exception e)
		{
			String errorText = "--> Error nn starting media server";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "MediaServer", "ErrorOnProcessingServer"), errorText, e);
			return false;
		}

		// Create event: DistributionListNotification
		// String distributionText =
		// WatchdogManager.printDistributionConfiguration(context.getWatchdogManager().getWatchdogDistributionList());
		// context.getNotificationManager().notifyEvent(context,
		// ResourceManager.notification(context, "MediaServer",
		// "DistributionListNotification"), distributionText, null);

		// Set processing context to a specific media server context
		try
		{
			ResourceContainer contextResource = ResourceManager.context(context, "MediaServer", "Processing");
			this.processingContext = context.createSilentDumpContext(contextResource, true);
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "MediaServer", "ErrorOnProcessingServer"), null, e);
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Stop media server.
	 * 
	 * @param context
	 *            The context to use for shutdown phase. This context is to be
	 *            set by the application because of the media server has its own
	 *            context independently from the outside world.
	 */
	public void stopServer(Context context)
	{
		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Stopping media server [" + context.getCodeName() + "]: " + this.toString());

		// Reset WATCHDOG context back to the context of the outside world
		this.processingContext = context;

		// Stop media server
		try
		{
			this.mediaManager.waitForCompletingMediaServerCommandQueue(60);
			context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "MediaServer", "MediaServerInterrupted"), null, null);
			this.interrupt();
		}
		catch (Exception e)
		{
			String errorText = "--> Error on stopping media server";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "MediaServer", "ErrorOnProcessingServer"), errorText, e);
		}
	}

	/**
	 * Processing of the media command queue.
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
		/*
		 * Start of processing
		 */

		// Something to do?
		if (this.mediaManager.getNumberOfMediaServerCommandElements() <= 0) return true;

		// Check if media server processing context is already set
		if (this.processingContext == null) return false;

		// Logging
		this.processingContext.getNotificationManager().notifyLogMessage(this.processingContext, NotificationManager.SystemLogLevelEnum.NOTICE, "Media server processing has started next cycle");

		// Check if the thread is forced to end
		if (this.isInterrupted()) return true;

		/*
		 * Get through the media server command list
		 */
		try
		{
			while (this.mediaManager.getNumberOfMediaServerCommandElements() > 0)
			{
				// Check if the thread is forced to end
				if (this.isInterrupted()) break;

				// Read media server command from queue
				ServerMediaPoolCommand mediaServerCommand = this.mediaManager.getMediaServerCommand();
				if (mediaServerCommand == null) break;

				// Check if media pool is active
				if (this.mediaManager.isEnableMediaPool() == false) continue;
			}
		}
		catch (Exception e)
		{
			this.processingContext.getNotificationManager().notifyError(this.processingContext, ResourceManager.notification(this.processingContext, "MediaServer", "ErrorOnProcessingServer"), null, e);
		}

		/*
		 * Stop of processing
		 */

		// Logging
		this.processingContext.getNotificationManager().notifyLogMessage(this.processingContext, NotificationManager.SystemLogLevelEnum.NOTICE, "Media server processing has ended actual cycle");

		// Return
		return true;
	}
}
