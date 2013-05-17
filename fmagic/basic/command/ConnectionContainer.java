package fmagic.basic.command;

import java.util.Date;

import fmagic.basic.context.Context;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.command.ClientCommand;
import fmagic.client.command.ClientCommandCreateSession;
import fmagic.client.command.ClientCommandHandshake;

/**
 * This class contains all data needed to connect to an application server in
 * order to process a command.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 15.05.2013 - Created
 */
public class ConnectionContainer
{
	// Common data
	final private int number;
	final private String host;
	final private int port;
	private String clientPrivateKey;
	private String serverPublicKey;
	private String sessionIdentifier = null;

	// Processing
	private boolean initialized = false;
	private boolean error = false;

	/**
	 * Constructor 1
	 * 
	 * @param number
	 *            Number of the host in the list.
	 * 
	 * @param host
	 *            Host name of the media server.
	 * 
	 * @param port
	 *            Port number of the media server.
	 * 
	 * @param timeoutTimeInMilliseconds
	 *            Socket timeout in Milliseconds.
	 * 
	 * @param clientPrivateKey
	 *            Private key of the caller.
	 * 
	 * @param serverPublicKey
	 *            Private key of the server called for.
	 */
	public ConnectionContainer(int number, String host, int port, String clientPrivateKey, String serverPublicKey)
	{
		this.number = number;
		this.host = host;
		this.port = port;
		this.clientPrivateKey = clientPrivateKey;
		this.serverPublicKey = serverPublicKey;
	}

	/**
	 * Constructor 2
	 * 
	 * @param number
	 *            Number of the host in the list.
	 * 
	 * @param host
	 *            Host name of the media server.
	 * 
	 * @param port
	 *            Port number of the media server.
	 */
	public ConnectionContainer(int number, String host, int port)
	{
		this.number = number;
		this.host = host;
		this.port = port;
	}

	/**
	 * Create new client session identifier.
	 * 
	 * @return Returns the client session identifier that was created.
	 */
	public static String createClientSessionIdentifier()
	{
		return String.valueOf(new Date().getTime()).trim() + String.valueOf(FileUtilFunctions.generalGetRandomValue(0, 100000));
	}

	/**
	 * Establish a connection by executing the commands 'CreateSession' and
	 * 'Handshake', if not done yet for the current connection container.
	 * <p>
	 * If the connection already is established nothing is executed.
	 * 
	 * @return Returns <TT>true</TT> if the connection could be established resp. is already established,
	 *         otherwise <TT>false</TT>.
	 */
	public boolean establishConnection(Context context)
	{
		// Check if connection already is established
		if (this.isInitialized() && !this.isError()) return true;

		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, this.toString());

		// Validate parameter of the connection
		if (this.checkConnectionSettings(context) == false)
		{
			this.initialized = false;
			this.error = true;
			return false;
		}

		// Process
		try
		{
			// First try a handshake to see if the last known connection works
			// yet
			if (this.commandHandshake(context) == true)
			{
				this.initialized = true;
				this.error = false;
				return true;
			}

			// If handshake didn't work create a new session on server and
			// handshake again
			if (this.commandCreateSession(context) == true)
			{
				if (this.commandHandshake(context) == true)
				{
					this.initialized = true;
					this.error = false;
					return true;
				}
			}

			// Return
			this.initialized = false;
			this.error = true;
			return false;
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnProcessingCommandOnClient"), null, e);
			this.initialized = false;
			this.error = true;
			return false;
		}
	}

	/**
	 * Execute command 'Handshake'.
	 * 
	 * @return Returns <TT>true</TT> if the command was executed successful,
	 *         otherwise <TT>false</TT>.
	 */
	private boolean commandHandshake(Context context)
	{
		// Check if a session identifier is already available
		if (!isSessionIdentifier()) return false;

		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, this.toString());

		// Process
		try
		{
			// COMMAND Handshake
			ClientCommand command = new ClientCommandHandshake(context, context.getApplicationManager(), this);
			ResponseContainer responseContainer = command.execute();

			// Error occurred
			if (responseContainer == null || responseContainer.isError()) { return false; }

			// Return
			return true;
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnProcessingCommandOnClient"), null, e);
			return false;
		}
	}

	/**
	 * Execute command 'CreateSession'.
	 * 
	 * @return Returns <TT>true</TT> if the command was executed successful,
	 *         otherwise <TT>false</TT>.
	 */
	private boolean commandCreateSession(Context context)
	{
		// Check if a session identifier is already available
		if (!isSessionIdentifier()) this.setSessionIdentifier(ConnectionContainer.createClientSessionIdentifier());

		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, this.toString());

		// Process
		try
		{
			// COMMAND Create Session
			ClientCommand command = new ClientCommandCreateSession(context, context.getApplicationManager(), this);
			ResponseContainer responseContainer = command.execute();

			// Try again with a new session identifier if the session already
			// exists on server
			if (responseContainer != null && responseContainer.isError() && responseContainer.getErrorCode().equals(ResourceManager.notification(context, "Application", "ClientSessionAlreadyExistsOnServer").getRecourceIdentifier()))
			{
				this.setSessionIdentifier(ConnectionContainer.createClientSessionIdentifier());
				command = new ClientCommandCreateSession(context, context.getApplicationManager(), this);
				responseContainer = command.execute();

				// Try again with a new session identifier if the session
				// already exists on server
				if (responseContainer != null && responseContainer.isError() && responseContainer.getErrorCode().equals(ResourceManager.notification(context, "Application", "ClientSessionAlreadyExistsOnServer").getRecourceIdentifier()))
				{
					this.setSessionIdentifier(ConnectionContainer.createClientSessionIdentifier());
					command = new ClientCommandCreateSession(context, context.getApplicationManager(), this);
					responseContainer = command.execute();
				}
			}

			// Error
			if (responseContainer == null || responseContainer.isError()) { return false; }

			// Return
			return true;
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnProcessingCommandOnClient"), null, e);
			return false;
		}
	}

	/**
	 * Check if all parameters are set to establish a connection.
	 * 
	 * @return Returns <TT>true</TT> if all parameter are set, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean checkConnectionSettings(Context context)
	{
		// Check connection settings
		boolean isSuccessful = true;
		String errorText = "--> Missing connection settings on executing command on server";

		try
		{
			if (this.host == null || this.host.length() == 0)
			{
				errorText += "\n--> Parameter 'Host' not set";
				isSuccessful = false;
			}

			if (this.port <= 0)
			{
				errorText += "\n--> Parameter 'Port' not set";
				isSuccessful = false;
			}

			if (this.clientPrivateKey == null || this.clientPrivateKey.length() == 0)
			{
				errorText += "\n--> Parameter 'ClientPrivateKey' not set";
				isSuccessful = false;
			}

			if (isSuccessful == false)
			{
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnProcessingCommandOnClient"), errorText, null);
			}
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Application", "ErrorOnProcessingCommandOnClient"), errorText, e);
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Getter
	 */
	public int getNumber()
	{
		return number;
	}

	/**
	 * Getter
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Getter
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * Getter
	 */
	public String getSessionIdentifier()
	{
		return sessionIdentifier;
	}

	/**
	 * Setter
	 */
	public void setSessionIdentifier(String sessionIdentifier)
	{
		this.sessionIdentifier = sessionIdentifier;
	}

	/**
	 * Getter
	 */
	public String getClientPrivateKey()
	{
		return clientPrivateKey;
	}

	/**
	 * Setter
	 */
	public void setPrivateKey(String clientPrivateKey)
	{
		this.clientPrivateKey = clientPrivateKey;
	}

	/**
	 * Getter
	 */
	public String getServerPublicKey()
	{
		return serverPublicKey;
	}

	/**
	 * Setter
	 */
	public void setServerPublicKey(String serverPublicKey)
	{
		this.serverPublicKey = serverPublicKey;
	}

	/**
	 * Getter
	 */
	public boolean isInitialized()
	{
		return initialized;
	}

	/**
	 * Getter
	 */
	public boolean isError()
	{
		return error;
	}

	/**
	 * Getter
	 */
	public boolean isSessionIdentifier()
	{
		return !(this.getSessionIdentifier() == null || this.getSessionIdentifier().length() == 0);
	}

	@Override
	public String toString()
	{
		String outputString = "";

		// Headline
		outputString += "\n[\n+++ Connection Container" + "\n";

		// Common value
		outputString += "----------" + "\n";
		outputString += "Number: '" + String.valueOf(this.getNumber()) + "'\n";
		if (this.getHost() != null) outputString += "Host: '" + this.getHost() + "'\n";
		if (this.getHost() != null) outputString += "Port: '" + String.valueOf(this.getPort()) + "'\n";
		if (this.getClientPrivateKey() != null) outputString += "Private key of client: '" + this.getClientPrivateKey().substring(0, Math.min(10, this.getClientPrivateKey().length())) + "'\n";
		if (this.getServerPublicKey() != null) outputString += "Public key of server: '" + this.getServerPublicKey().substring(0, Math.min(10, this.getServerPublicKey().length())) + "'\n";
		if (this.getSessionIdentifier() != null) outputString += "Session identifier: '" + this.getSessionIdentifier() + "'\n";
		outputString += "Is initialized: '" + String.valueOf(this.isInitialized()) + "'\n";
		outputString += "Is error: '" + String.valueOf(this.isError()) + "'\n";
		outputString += "----------" + "\n";

		// End of output string
		outputString += "\n]\n";

		// Return
		return outputString;
	}
}