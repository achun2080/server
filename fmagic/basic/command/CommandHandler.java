package fmagic.basic.command;

import java.net.SocketTimeoutException;

import fmagic.basic.context.Context;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceManager;

/**
 * This class executes a command on client side including calling it on server.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 15.05.2013 - Created
 * 
 */
public class CommandHandler
{
	private final ConnectionContainer connectionContainer;
	private final int socketTimeoutInMilliseconds;
	
	/**
	 * Constructor
	 */
	public CommandHandler(ConnectionContainer connectionContainer, int socketTimeoutInMilliseconds)
	{
		this.connectionContainer = connectionContainer;
		this.socketTimeoutInMilliseconds = socketTimeoutInMilliseconds;
	}

	/**
	 * Executes a command on server and waits for response.
	 * 
	 * @param requestContainer
	 *            Request container that hold all data to send to the server.
	 */
	public ResponseContainer execute(Context executingContext, RequestContainer requestContainer)
	{
		// Logging on start
		executingContext.getNotificationManager().notifyLogMessage(executingContext, NotificationManager.SystemLogLevelEnum.NOTICE, "Client request to server started.");
		if (requestContainer != null) executingContext.getNotificationManager().notifyLogMessage(executingContext, NotificationManager.SystemLogLevelEnum.CODE, requestContainer.toString());

		// Create server response container as default response
		ResponseContainer responseContainer = new ResponseContainer(null, 0, null);

		// Transfer some client request data to the server response data
		this.workstepTransferContainerData(requestContainer, responseContainer);

		// Get public key of the server
		String serverPublicKey = null;

		// Encode request container
		StringBuffer commandEncrypted = workstepConvertRequestContainerObjectToSocketData(executingContext, requestContainer, serverPublicKey, responseContainer);

		// Open a socket connection to the server
		SocketHandler socketHandler = workstepOpenSocketConnectionToServer(executingContext, responseContainer);
		
		if (socketHandler == null )
		{
			responseContainer.setErrorCode(ResourceManager.notification(executingContext, "Socket", "ErrorOnSocketConnection").getRecourceIdentifier());
			return responseContainer;
		}

		// Do handshake
		if (socketHandler != null && commandEncrypted != null)
		{
			// Write request container to the socket
			socketHandler.writeData(commandEncrypted);

			// Read raw response data from socket
			String responseData = null;

			try
			{
				responseData = socketHandler.readData();
			}
			catch (SocketTimeoutException socketTimeoutException)
			{
				String errorText = "--> Timeout value: '" + socketHandler.getTimeoutTimeInMilliseconds() + "' Milliseconds";
				responseContainer.notifyError(executingContext, "Application", "SocketTimeout", errorText, socketTimeoutException);
			}

			// Decode raw response data onto a response container
			responseContainer = workstepConvertSocketDataToResponseContainer(executingContext, responseData, responseContainer);
		}

		// Close socket
		if (socketHandler != null) socketHandler.closeSocket();

		// Logging on stop
		executingContext.getNotificationManager().notifyLogMessage(executingContext, NotificationManager.SystemLogLevelEnum.NOTICE, "Client request to server ended.");

		// Provoking error to see all messages on console and in log files
		// executingContext.getNotificationManager().flushDump(executingContext);

		// Return
		return responseContainer;
	}

	/**
	 * Convert a request container to a raw socket data string, in order to send
	 * it to the server.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param executingContext
	 *            The context to use.
	 * 
	 * @param requestContainer
	 *            The request container to encode.
	 * 
	 * @param serverPublicKey
	 *            The public key of the server to use for encoding.
	 * 
	 * @param responseContainer
	 *            The response container to hold the return value if an error
	 *            occurs.
	 * 
	 * @return Returns the encoded string, or <TT>null</TT> if an error
	 *         occurred.
	 * 
	 */
	private StringBuffer workstepConvertRequestContainerObjectToSocketData(Context executingContext, RequestContainer requestContainer, String serverPublicKey, ResponseContainer responseContainer)
	{
		// Validate parameter
		if (executingContext == null) return null;
		if (requestContainer == null) return null;
		if (responseContainer == null) return null;

		// Convert a request container
		StringBuffer commandEncrypted = null;
		EncodingHandler encodingUitility = new EncodingHandler();

		try
		{
			commandEncrypted = encodingUitility.encodeRequestContainer(executingContext, requestContainer, serverPublicKey);
		}
		catch (Exception exception)
		{
			responseContainer.notifyError(executingContext, "Application", "ErrorOnProcessingRequestToServer", null, exception);
		}

		// Return
		return commandEncrypted;
	}

	/**
	 * Open a socket connection to the server.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param executingContext
	 *            The context to use.
	 * 
	 * @param responseContainer
	 *            The response container to hold the return value if an error
	 *            occurs.
	 * 
	 * @return Returns the the socket handler object, or <TT>null</TT> if an
	 *         error occurred.
	 * 
	 */
	private SocketHandler workstepOpenSocketConnectionToServer(Context executingContext, ResponseContainer responseContainer)
	{
		// Validate parameter
		if (executingContext == null) return null;
		if (responseContainer == null) return null;

		// Open a socket connection
		SocketHandler socketHandler = null;

		try
		{
			socketHandler = new SocketHandler(executingContext, connectionContainer.getHost(), connectionContainer.getPort(), this.socketTimeoutInMilliseconds);

			if (socketHandler.openSocket() == false)
			{
				String errorText = "--> Error on opening a socket connection to the server";
				responseContainer.notifyError(executingContext, "Application", "ErrorOnProcessingRequestToServer", errorText, null);

				// Reset and return
				socketHandler = null;
				return null;
			}
		}
		catch (Exception exception)
		{
			String errorText = "--> Error on opening a socket connection to the server";
			responseContainer.notifyError(executingContext, "Application", "ErrorOnProcessingRequestToServer", errorText, exception);
			return null;
		}

		// Return
		return socketHandler;
	}

	/**
	 * Decode server data, read by socket, onto a response container object.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param executingContext
	 *            The context to use.
	 * 
	 * @param responseData
	 *            The raw text data read from socket, containing all information
	 *            to recreate the response container object, sent by the server.
	 * 
	 * @param parameterResponseContainer
	 *            The response container to work with.
	 * 
	 * @return Returns response container object, or <TT>null</TT> if an error
	 *         occurred.
	 */
	private ResponseContainer workstepConvertSocketDataToResponseContainer(Context executingContext, String responseData, ResponseContainer parameterResponseContainer)
	{
		// Validate parameter
		if (responseData == null) return parameterResponseContainer;
		if (parameterResponseContainer == null) return parameterResponseContainer;

		// Convert socket data onto response container object
		ResponseContainer newResponseContainer = null;

		try
		{
			EncodingHandler encodingUitility = new EncodingHandler();
			newResponseContainer = encodingUitility.decodeResponseContainer(executingContext, responseData, connectionContainer.getKeyApplicationPrivateKey());
		}
		catch (Exception exception)
		{
			String errorText = "--> Error on decoding response container";
			parameterResponseContainer.notifyError(executingContext, "Application", "ErrorOnProcessingRequestToServer", errorText, exception);
			return parameterResponseContainer;
		}

		if (newResponseContainer == null)
		{
			String errorText = "--> Error on decoding response container";
			parameterResponseContainer.notifyError(executingContext, "Application", "ErrorOnProcessingRequestToServer", errorText, null);
			return parameterResponseContainer;
		}

		// Return
		return newResponseContainer;
	}

	/**
	 * Transfer some client request data to the server response data.
	 * <p>
	 * if an error occurred, the error message is set automatically by this
	 * method.
	 * 
	 * @param requestContainer
	 *            The request container to work with.
	 * 
	 * @param responseContainer
	 *            The response container to work with.
	 */
	private void workstepTransferContainerData(RequestContainer requestContainer, ResponseContainer responseContainer)
	{
		// Validate parameter
		if (requestContainer == null) return;
		if (responseContainer == null) return;

		// Transfer data
		try
		{
			responseContainer.setSession(requestContainer.getClientSessionIdentifier());
		}
		catch (Exception exception)
		{
			// Be silent
			return;
		}

		// Return
		return;
	}
}
