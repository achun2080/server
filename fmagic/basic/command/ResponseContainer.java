package fmagic.basic.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class contains all data needed for a server to response to a client
 * request via socket.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class ResponseContainer
{
	// Common data
	final private String serverApplicationIdentifier;
	final private int serverVersion;
	private String clientSessionIdentifier;

	// CommandManager data
	private String commandIdentifier;
	final private HashMap<String, String> properties;

	// Error code on client side
	private String errorCode = null;
	private String errorTicket = null;
	private String errorHeadLine = null;
	private String errorMessagePart1 = null;
	private String errorMessagePart2 = null;
	private String errorMessagePart3 = null;
	private String errorTechnicalDescription = null;

	/**
	 * 
	 * After construction of a server response container you might set other
	 * parameters via setters:
	 * <ul>
	 * <li>setXXX()</li>
	 * </ul>
	 * 
	 * You might add properties to send to the client using the function
	 * <TT>addProperty()</TT>.
	 * <p>
	 * 
	 * @param serverApplicationIdentifier
	 *            Identifier (Name) of the application.
	 * 
	 * @param serverVersion
	 *            The software version of the client.
	 */
	public ResponseContainer(String serverApplicationIdentifier,
			int serverVersion)
	{
		this.serverApplicationIdentifier = serverApplicationIdentifier;
		this.serverVersion = serverVersion;
		this.properties = new HashMap<String, String>();
	}

	/**
	 * Add a property to the list of properties.
	 * 
	 * @param identifier
	 *            Identifier of property.
	 * 
	 * @param value
	 *            Value of property.
	 * 
	 */
	public void addProperty(String identifier, String value)
	{
		this.properties.put(identifier, value);
	}

	/**
	 * Get a property from the list of properties.
	 * 
	 * @param identifier
	 *            Identifier of property.
	 * 
	 * @param defaultValue
	 *            Default Value of property.
	 */
	public String getProperty(String identifier, String defaultValue)
	{
		String value = this.properties.get(identifier);

		if (value == null)
		{
			return defaultValue;
		}
		else
		{
			return value;
		}
	}

	/**
	 * Getter
	 */
	public int getServerVersion()
	{
		return serverVersion;
	}

	/**
	 * Getter
	 */
	public String getClientSessionIdentifier()
	{
		return clientSessionIdentifier;
	}

	/**
	 * Getter
	 */
	public String getCommandIdentifier()
	{
		return commandIdentifier;
	}

	/**
	 * Getter
	 */
	public HashMap<String, String> getProperties()
	{
		return properties;
	}

	/**
	 * Getter
	 */
	public String getErrorCode()
	{
		return errorCode;
	}

	/**
	 * Setter
	 */
	public void setErrorCode(String errorCode)
	{
		this.errorCode = errorCode;
	}

	/**
	 * Getter
	 */
	public String getErrorHeadLine()
	{
		return errorHeadLine;
	}

	/**
	 * Setter
	 */
	public void setErrorHeadLine(String errorHeadLine)
	{
		this.errorHeadLine = errorHeadLine;
	}

	/**
	 * Getter
	 */
	public String getErrorMessagePart1()
	{
		return errorMessagePart1;
	}

	/**
	 * Setter
	 */
	public void setErrorMessagePart1(String errorMessagePart1)
	{
		this.errorMessagePart1 = errorMessagePart1;
	}

	/**
	 * Getter
	 */
	public String getErrorMessagePart2()
	{
		return errorMessagePart2;
	}

	/**
	 * Setter
	 */
	public void setErrorMessagePart2(String errorMessagePart2)
	{
		this.errorMessagePart2 = errorMessagePart2;
	}

	/**
	 * Getter
	 */
	public String getErrorMessagePart3()
	{
		return errorMessagePart3;
	}

	/**
	 * Setter
	 */
	public void setErrorMessagePart3(String errorMessagePart3)
	{
		this.errorMessagePart3 = errorMessagePart3;
	}

	/**
	 * Getter
	 */
	public String getErrorTechnicalDescription()
	{
		return errorTechnicalDescription;
	}

	/**
	 * Setter
	 */
	public void setErrorTechnicalDescription(String errorTechnicalDescription)
	{
		this.errorTechnicalDescription = errorTechnicalDescription;
	}

	/**
	 * Getter
	 */
	public String getErrorTicket()
	{
		return errorTicket;
	}

	/**
	 * Setter
	 */
	public void setErrorTicket(String errorTicket)
	{
		this.errorTicket = errorTicket;
	}

	/**
	 * To string
	 */
	@Override
	public String toString()
	{
		String outputString = "";

		// Headline
		outputString += "\n[\n+++ Response Container";

		// Common value
		outputString += "\n" + "----------";
		outputString += "\n" + "Application identifier: " + this.serverApplicationIdentifier;
		outputString += "\n" + "Server version: " + String.valueOf(this.serverVersion);
		outputString += "\n" + "Session: " + this.clientSessionIdentifier;
		outputString += "\n" + "CommandManager identifier: " + this.commandIdentifier;

		// Properties
		if (this.properties.size() > 0)
		{
			outputString += "\n" + "----------";

			// Sorting the keys alphabetically
			List<String> sortedProperties = new ArrayList<String>();
			sortedProperties.addAll(this.properties.keySet());
			Collections.sort(sortedProperties);

			// List all items
			Iterator<String> iterManual = sortedProperties.iterator();

			while (iterManual.hasNext())
			{
				String identifier = iterManual.next();
				String value = this.properties.get(identifier);
				outputString += "\n" + identifier + " = " + value;
			}
		}

		// Error code
		if (errorCode != null)
		{
			outputString += "\n" + "----------";
			outputString += "\n" + "Error code: " + this.errorCode ;
			outputString += "\n" + "Error ticket: " + this.errorTicket;
			outputString += "\n" + "Error headline: " + this.errorHeadLine;
			outputString += "\n" + "Error message part 1: " + this.errorMessagePart1;
			outputString += "\n" + "Error message part 2: " + this.errorMessagePart2;
			outputString += "\n" + "Error message part 3: " + this.errorMessagePart3;

			if (this.errorTechnicalDescription != null)
			{
				outputString += "\n" + "----------";
				outputString += "\n" + this.errorTechnicalDescription;
			}
		}

		// End of output string
		outputString += "\n]\n";

		// Return
		return outputString;
	}

	/**
	 * Setter
	 */
	public void setSession(String session)
	{
		this.clientSessionIdentifier = session;
	}

	/**
	 * Clear error code
	 */
	public void clearErrorCode()
	{
		this.setErrorCode(null);
		this.setErrorTicket(null);
		this.setErrorHeadLine(null);
		this.setErrorMessagePart1(null);
		this.setErrorMessagePart2(null);
		this.setErrorMessagePart3(null);
		this.setErrorTechnicalDescription(null);
	}

	/**
	 * Setter
	 */
	public void setCommandIdentifier(String commandIdentifier)
	{
		this.commandIdentifier = commandIdentifier;
	}

	/**
	 * Getter
	 */
	public String getServerApplicationIdentifier()
	{
		return serverApplicationIdentifier;
	}
}
