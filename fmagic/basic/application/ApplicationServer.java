package fmagic.basic.application;

import java.net.Socket;
import java.net.SocketTimeoutException;

import fmagic.basic.command.SocketHandler;
import fmagic.basic.context.Context;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceManager;
import fmagic.server.application.ServerManager;
import fmagic.server.application.ServerWorkerThread;

/**
 * This class implements an application server or client running all time.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 13.02.2013 - Created
 * 
 */
public class ApplicationServer extends Thread
{
	final private ServerManager server;
	final private Context context;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param applicationManager
	 *            The application manager to use.
	 */
	public ApplicationServer(Context context,
			ServerManager serverManager)
	{
		this.server = serverManager;
		this.context = context;
	}

	/**
	 * Thread running method.
	 */
	@Override
	public void run()
	{
		// Fire Event
		this.context.getNotificationManager().notifyEvent(this.context, ResourceManager.notification(context, "Application", "ApplicationServerStarted"), null, null);

		// Processing requests
		while (true)
		{
			// Delay a little bit
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{
				break;
			}

			// Check shutdown flag
			if (this.getServerManager().isShutdown() == true) break;

			// Check if the thread is forced to end
			if (this.isInterrupted()) break;

			// Check stop running flag
			if (this.getServerManager().isStopRunning() == true) break;

			// Accept a client request via socket
			try
			{
				// Waiting for next client request
				Socket clientSocket = null;
				clientSocket = this.getServerManager().getServerSocket().accept();

				// Check stop running flag
				if (this.getServerManager().isStopRunning() == true)
				{
					clientSocket.close();
					break;
				}

				// Execute inside the thread pool
				if (clientSocket != null)
				{
					// Create a SILENT dump context regarding the executing
					// of a command on server
					Context newContext = this.context.createSilentDumpContext(ResourceManager.context(this.context, "Processing", "ProcessingClientCommand"));

					SocketHandler socketConnector = new SocketHandler(newContext);
					socketConnector.adoptSocket(clientSocket, this.getServerManager().getTimeoutTimeInMilliseconds());

					ServerWorkerThread worker = new ServerWorkerThread(newContext, this.getServerManager(), socketConnector, this.getServerManager().getServerPrivateKey());

					this.execute(newContext, worker);
				}
			}
			catch (SocketTimeoutException e)
			{
				// Be silent
			}
			catch (Exception e)
			{
				if (this.getServerManager().isStopRunning() == false) this.context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnAcceptingClientSocket"), null, e);
			}
		}

		// Fire Event
		this.context.getNotificationManager().notifyEvent(this.context, ResourceManager.notification(context, "Application", "ApplicationServerStopped"), null, null);
	}

	/**
	 * Execute a socket request invoked by a client.
	 * 
	 * @return Returns <TT>true</TT> if the command could be invoked, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean execute(Context context, Runnable commandObject)
	{
		// Check variables
		if (getContext() == null) return false;

		if (commandObject == null)
		{
			this.getContext().getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnInvokingCommand"), null, null);
			return false;
		}

		// Execute the command via the thread pool
		try
		{
			this.getServerManager().threadPoolExecuteNewThread(commandObject);
			return true;
		}
		catch (Exception e)
		{
			this.getContext().getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnInvokingCommand"), null, e);
			return false;
		}
	}

	/**
	 * Interrupts (stops) the thread.
	 */
	@Override
	public void interrupt()
	{
		super.interrupt();
	}

	/**
	 * Start application server.
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
		// Start server
		try
		{
			this.start();
		}
		catch (Exception e)
		{
			this.context.getNotificationManager().notifyError(this.context, ResourceManager.notification(this.context, "Application", "ErrorOnStartingServer"), null, e);
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Stop application server.
	 * 
	 * @param context
	 *            The context to use for shutdown phase. This context is to be
	 *            set by the application because of the WATCHDOG has is own
	 *            context independently from the outside world.
	 */
	public void stopServer(Context context)
	{
		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Stopping application server [" + context.getCodeName() + "]: " + this.toString());

		// Stop application server
		try
		{
			context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "Application", "ApplicationServerInterrupted"), null, null);
			this.interrupt();
		}
		catch (Exception e)
		{
			String errorText = "--> Error on stopping application server";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnStoppingServer"), errorText, e);
		}
	}

	/**
	 * Getter
	 */
	public ServerManager getServerManager()
	{
		return server;
	}

	/**
	 * Getter
	 */
	public Context getContext()
	{
		return context;
	}
	
}
