package fmagic.basic.command;

import fmagic.basic.context.Context;
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
	private int timeoutTimeInMilliseconds = 0;
	private String privateKey;
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
	 * @param privateKey
	 *            Private key of the caller.
	 */
	public ConnectionContainer(int number, String host, int port,
			int timeoutTimeInMilliseconds, String privateKey)
	{
		this.number = number;
		this.host = host;
		this.port = port;
		this.timeoutTimeInMilliseconds = timeoutTimeInMilliseconds;
		this.privateKey = privateKey;
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
	 * Establish a connection by executing the commands 'CreateSession' and
	 * 'Handshake', if not done yet for the current connection container.
	 * 
	 * @return Returns <TT>true</TT> if the connection could be established,
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
			// COMMAND Create Session
			ClientCommand command = new ClientCommandCreateSession(context, context.getApplicationManager(), this);
			ResponseContainer responseContainer = command.execute();

			if (responseContainer == null || responseContainer.isError())
			{
				this.initialized = false;
				this.error = true;
				return false;
			}

			// COMMAND Handshake
			command = new ClientCommandHandshake(context, context.getApplicationManager(), this);
			responseContainer = command.execute();

			if (responseContainer == null || responseContainer.isError())
			{
				this.initialized = false;
				this.error = true;
				return false;
			}
			
			// Set last used session identifier
			this.setSessionIdentifier(responseContainer.getClientSessionIdentifier());

			// Set status
			this.initialized = true;
			this.error = false;

			// Logging
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, this.toString());

			// Return
			return true;
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

			if (this.timeoutTimeInMilliseconds <= 0)
			{
				errorText += "\n--> Parameter 'TimeoutTimeInMilliseconds' not set";
				isSuccessful = false;
			}

			if (this.privateKey == null || this.privateKey.length() == 0)
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
	public int getTimeoutTimeInMilliseconds()
	{
		return timeoutTimeInMilliseconds;
	}

	/**
	 * Getter
	 */
	public void setTimeoutTimeInMilliseconds(int timeoutTimeInMilliseconds)
	{
		this.timeoutTimeInMilliseconds = timeoutTimeInMilliseconds;
	}

	/**
	 * Getter
	 */
	public String getPrivateKey()
	{
		return privateKey;
	}

	/**
	 * Setter
	 */
	public void setPrivateKey(String privateKey)
	{
		this.privateKey = privateKey;
	}

	/**
	 * Setter
	 */
	public boolean isInitialized()
	{
		return initialized;
	}

	/**
	 * Setter
	 */
	public boolean isError()
	{
		return error;
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
		outputString += "Host: '" + this.getHost() + "'\n";
		outputString += "Port: '" + String.valueOf(this.getPort()) + "'\n";
		outputString += "Socket timeout in Milliseconds: '" + String.valueOf(this.getTimeoutTimeInMilliseconds()) + "'\n";
		outputString += "Private key: '" + this.getPrivateKey().substring(0, Math.min(10, this.getPrivateKey().length())) + "'\n";
		outputString += "Session identifier: '" + this.getSessionIdentifier() + "'\n";
		outputString += "Is initialized: '" + String.valueOf(this.isInitialized()) + "'\n";
		outputString += "Is error: '" + String.valueOf(this.isError()) + "'\n";
		outputString += "----------" + "\n";

		// End of output string
		outputString += "\n]\n";

		// Return
		return outputString;
	}
}
