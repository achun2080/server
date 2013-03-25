package fmagic.basic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * This class implements all functions for connecting to a server via socket.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class SocketHandler
{
	// Error codes: For some specific errors this class returns with an error
	// code instead of <TT>null</TT>.
	final public String SOCKET_HANDLER_ERROR_CODE_TIMEOUT = "{TIMEOUT}";

	// Sets context of server
	private final Context context;

	// Socket data
	private String host = null;
	private int port = 0;

	// Connection data
	private Socket clientSocket = null;
	private BufferedReader bufferedReader = null;
	private BufferedWriter bufferedWriter = null;

	// Connection status. First setting must be null in order to register the
	// starting of the system.
	private boolean connectionStatus = false;

	// Socket TIMEOUT time
	private int timeoutTimeInMilliseconds = 10000;

	/**
	 * Constructor using connection parameters.
	 * 
	 * @param context
	 *            The current context.
	 * @param host
	 *            Name or IP address of host.
	 * 
	 * @param port
	 *            Port number to connect with.
	 * 
	 * @param timeoutTimeInMilliseconds
	 *            Time out time for waiting for server response in milliseconds.
	 */
	public SocketHandler(Context context, String host, int port,
			int timeoutTimeInMilliseconds)
	{
		this.context = context;
		this.host = host;
		this.port = port;
		this.timeoutTimeInMilliseconds = timeoutTimeInMilliseconds;
	}

	/**
	 * Constructor without connection parameter. Should be used if you want to
	 * adopt a client socket from a socket server.
	 * 
	 * @param context
	 *            The current context.
	 */
	public SocketHandler(Context context)
	{
		this.context = context;
	}

	/**
	 * Open all connections and streams you need for using the socket.
	 * 
	 * @return Returns <TT>true</TT> if the function could be executed
	 *         successfully, otherwise <TT>false</TT>.
	 */
	public boolean openSocket()
	{
		// Check parameters
		if (this.host == null) return false;
		if (this.host.length() == 0) return false;
		if (this.port <= 0) return false;

		// Open socket
		try
		{
			if (this.clientSocket != null) this.clientSocket.close();
			this.clientSocket = new Socket(host, port);
		}
		catch (Exception e)
		{
			String errorString = "--> on opening socket connection";
			errorString += "\n--> Host '" + this.host + "', Port '" + String.valueOf(this.port) + "'";
			this.context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Socket", "ErrorOnSocketConnection"), errorString, e);
			return false;
		}

		// Open input/output stream
		if (this.openStreams() == false) return false;

		// Set timeout time
		if (this.setSocketTimeout() == false) return false;

		// Set setConnectionStatus
		this.setConnectionStatus(true);

		// Return
		return true;
	}

	/**
	 * Set timeout time for a socket.
	 * 
	 * @return Returns <TT>true</TT> if the function could be executed
	 *         successfully, otherwise <TT>false</TT>.
	 */
	private boolean setSocketTimeout()
	{
		// Set time out
		try
		{
			this.clientSocket.setSoTimeout(this.getTimeoutTimeInMilliseconds());
		}
		catch (Exception e)
		{
			String errorString = "--> on setting socket timeout";
			errorString += "\n--> Host '" + this.host + "', Port '" + String.valueOf(this.port) + "'";
			this.context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Socket", "ErrorOnSocketConnection"), errorString, e);
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Opens the input and output reader of the socket.
	 * 
	 * @return Returns <TT>true</TT> if the function could be executed
	 *         successfully, otherwise <TT>false</TT>.
	 */
	private boolean openStreams()
	{
		// Check socket connection
		if (this.clientSocket == null) return false;

		// Open output stream
		try
		{
			if (this.bufferedWriter != null) this.bufferedWriter.close();
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()));
		}
		catch (Exception e)
		{
			String errorString = "--> on opening output stream";
			errorString += "\n--> Host '" + this.host + "', Port '" + String.valueOf(this.port) + "'";
			this.context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Socket", "ErrorOnSocketConnection"), errorString, e);
			return false;
		}

		// Open input stream
		try
		{
			if (this.bufferedReader != null) this.bufferedReader.close();
			this.bufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
		}
		catch (Exception e)
		{
			String errorString = "--> on opening input stream";
			errorString += "\n--> Host '" + this.host + "', Port '" + String.valueOf(this.port) + "'";
			this.context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Socket", "ErrorOnSocketConnection"), errorString, e);
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Closes all connections and streams you need for using a socket.
	 */
	public void closeSocket()
	{
		// Set setConnectionStatus
		this.setConnectionStatus(false);

		// Close input stream (response)
		try
		{
			if (this.bufferedReader != null) this.bufferedReader.close();
		}
		catch (Exception e)
		{
			String errorString = "--> on closing input stream";
			errorString += "\n--> Host '" + this.host + "', Port '" + String.valueOf(this.port) + "'";
			this.context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Socket", "ErrorOnSocketConnection"), errorString, e);
		}
		finally
		{
			this.bufferedReader = null;
		}

		// Close output stream (command)
		try
		{
			if (this.bufferedWriter != null) this.bufferedWriter.close();
		}
		catch (Exception e)
		{
			String errorString = "--> on closing output stream";
			errorString += "\n--> Host '" + this.host + "', Port '" + String.valueOf(this.port) + "'";
			this.context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Socket", "ErrorOnSocketConnection"), errorString, e);
		}
		finally
		{
			this.bufferedWriter = null;
		}

		// Close socket connection
		try
		{
			if (this.clientSocket != null) this.clientSocket.close();
		}
		catch (Exception e)
		{
			String errorString = "--> on closing socket connection";
			errorString += "\n--> Host '" + this.host + "', Port '" + String.valueOf(this.port) + "'";
			this.context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Socket", "ErrorOnSocketConnection"), errorString, e);
		}
		finally
		{
			this.clientSocket = null;
		}
	}

	/**
	 * Writes data to the socket.
	 * 
	 * @param data
	 *            String that contains the data.
	 * 
	 * @return Returns <TT>true</TT> if the function could be executed
	 *         successfully, otherwise <TT>false</TT>.
	 */
	public boolean writeData(String data)
	{
		// Check parameters
		if (!this.isConnected()) return false;
		if (data == null) return false;

		// Write data
		try
		{
			// Add NEWLINE as mark for End of Data package
			data += "\n\n";

			// Write data
			this.bufferedWriter.append(data);
			this.bufferedWriter.flush();

		}
		catch (Exception e)
		{
			String errorString = "--> on writing data to socket";
			errorString += "\n--> Host '" + this.host + "', Port '" + String.valueOf(this.port) + "'";
			this.context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Socket", "ErrorOnSocketConnection"), errorString, e);
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Read data from socket.
	 * 
	 * @return Returns the data read.
	 */
	public String readData() throws SocketTimeoutException
	{
		// Check parameters
		if (!this.isConnected()) return null;

		// Read data
		try
		{
			// Read data stream
			char[] responseBuffer = new char[5000];
			int charCount = 0;
			String responseString = "";

			while (true)
			{
				charCount = this.bufferedReader.read(responseBuffer, 0, 5000);

				// No data read
				if (charCount == -1) break;

				// Transform data to string
				responseString += new String(responseBuffer, 0, charCount);

				// EOD
				if (charCount < 5000) break;
			}

			// Check for NEWLINE as EOD
			if (!responseString.endsWith("\n\n")) return null;

			// Return
			return responseString.substring(0, responseString.length() - 2);
		}
		catch (SocketTimeoutException socketTimeoutException)
		{
			String errorString = "--> socket timeout";
			errorString += "\n--> Host '" + this.host + "', Port '" + String.valueOf(this.port) + "'";
			this.context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Socket", "ErrorOnSocketConnection"), errorString, socketTimeoutException);
			throw socketTimeoutException;
		}
		catch (Exception e)
		{
			String errorString = "--> on reading data from socket";
			errorString += "\n--> Host '" + this.host + "', Port '" + String.valueOf(this.port) + "'";
			this.context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Socket", "ErrorOnSocketConnection"), errorString, e);
			return null;
		}
	}

	/**
	 * Getter
	 */
	public boolean isConnected()
	{
		return connectionStatus;
	}

	/**
	 * Setter
	 */
	public void setConnectionStatus(boolean connectionStatus)
	{
		this.connectionStatus = connectionStatus;
	}

	/**
	 * Getter
	 */
	public BufferedWriter getBufferedWriter()
	{
		return bufferedWriter;
	}

	/**
	 * Getter
	 */
	public BufferedReader getBufferedReader()
	{
		return bufferedReader;
	}

	/**
	 * Getter
	 */
	public int getTimeoutTimeInMilliseconds()
	{
		return timeoutTimeInMilliseconds;
	}

	/**
	 * Adopt an open socket, e. g. opened by a server socket <TT>accept()</TT>,
	 * to integrate it in this connector object.
	 * 
	 * @param socketToAdopt
	 *            The socket connection to adopt.
	 * 
	 * @return Returns <TT>true</TT> if the action was successful, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean adoptSocket(Socket socketToAdopt, int timeoutTimeInMilliseconds)
	{
		// Set connection status
		this.setConnectionStatus(false);

		// No socket set
		if (socketToAdopt == null) return false;

		// Closes an active socket to ensure an initialized state
		this.closeSocket();

		// Adopts the socket
		this.clientSocket = socketToAdopt;
		this.host = socketToAdopt.getLocalAddress().toString();
		this.port = socketToAdopt.getLocalPort();
		this.timeoutTimeInMilliseconds = timeoutTimeInMilliseconds;
		this.setSocketTimeout();

		// Opens streams
		if (this.openStreams() == false) return false;

		// Set timeout time
		if (this.setSocketTimeout() == false) return false;

		// Set connection status
		this.setConnectionStatus(true);

		// Return
		return true;
	}

	/**
	 * Getter
	 */
	public Socket getClientSocket()
	{
		return clientSocket;
	}
}
