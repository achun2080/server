package fmagic.basic.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import fmagic.basic.file.FileUtilFunctions;

/**
 * This class contains all data needed for a client request to a server via
 * socket.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class RequestContainer
{
	// Common data
	final private String clientApplicationIdentifier;
	final private int clientVersion;
	final private String clientCodeName;
	private String clientSessionIdentifier = "";

	// CommandManager data
	final private String commandIdentifier;
	final private HashMap<String, String> properties;

	/**
	 * 
	 * After construction of a client request container you might set other
	 * parameters via setters.
	 * <ul>
	 * <li>setXXX()</li>
	 * </ul>
	 * 
	 * You might add properties to send to the server using the function
	 * <TT>addProperty()</TT>.
	 * <p>
	 * 
	 * @param clientApplicationIdentifier
	 *            Identifier (Name) of the application.
	 * 
	 * @param clientVersion
	 *            The software version of the client.
	 * 
	 * @param commandIdentifier
	 *            The alias name of the command identifier to be executed on
	 *            server.
	 */
	public RequestContainer(String clientApplicationIdentifier,
			int clientVersion, String clientCodeName, String commandIdentifier)
	{
		this.clientApplicationIdentifier = clientApplicationIdentifier;
		this.clientVersion = clientVersion;
		this.clientCodeName = clientCodeName;
		this.commandIdentifier = commandIdentifier;
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
	public int getClientVersion()
	{
		return clientVersion;
	}

	/**
	 * Getter
	 */
	public String getClientSessionIdentifier()
	{
		return clientSessionIdentifier;
	}

	/**
	 * Setter
	 */
	public void setClientSessionIdentifier(String clientSessionIdentifier)
	{
		if (clientSessionIdentifier == null)
		{
			this.clientSessionIdentifier = "";
		}
		else
		{
			this.clientSessionIdentifier = clientSessionIdentifier.trim();
		}
	}

	/**
	 * Create new client session identifier.
	 * 
	 * @return Returns the client session identifier that was created.
	 */
	public String createClientSessionIdentifier()
	{
		this.clientSessionIdentifier = String.valueOf(new Date().getTime()).trim() + String.valueOf(FileUtilFunctions.generalGetRandomValue(0, 100000));
		return this.clientSessionIdentifier;
	}

	/**
	 * Getter
	 */
	public String getClientApplicationIdentifier()
	{
		return clientApplicationIdentifier;
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
	public String getClientCodeName()
	{
		return clientCodeName;
	}

	@Override
	public String toString()
	{
		String outputString = "";

		// Headline
		outputString += "\n[\n+++ Request Container" + "\n";

		// Common value
		outputString += "----------" + "\n";
		outputString += "Application identifier: " + this.clientApplicationIdentifier + "\n";
		outputString += "Client version: " + String.valueOf(clientVersion) + "\n";
		outputString += "Client code name: " + this.clientCodeName + "\n";
		outputString += "Client session: " + String.valueOf(clientSessionIdentifier) + "\n";

		outputString += "----------" + "\n";
		outputString += "CommandManager identifier: " + commandIdentifier + "\n";

		// Properties
		if (properties.size() > 0)
		{
			outputString += "----------";

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
				outputString += "\n" + identifier + " = ";

				if (value != null)
				{
					if (value.length() > 100)
					{
						outputString += value.substring(0, 100) + " (...)";
					}
					else
					{
						outputString += value;
					}
				}
			}
		}

		// End of output string
		outputString += "\n]\n";

		// Return
		return outputString;
	}
}
