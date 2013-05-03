package fmagic.basic.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

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
	final private String commandIdentifier;
	final private HashMap<String, String> properties;

	// Error code and description
	private String errorCode = null;
	private String errorDump = null;

	/**
	 * 
	 * Constructor
	 * 
	 * You can add result values to be sent to the client using the function
	 * <TT>addProperty()</TT>.
	 * <p>
	 * 
	 * @param serverApplicationIdentifier
	 *            Identifier (Name) of the application.
	 * 
	 * @param serverVersion
	 *            The software version of the client.
	 * 
	 * @param commandIdentifier
	 *            The command identifier of the triggering command.
	 */
	public ResponseContainer(String serverApplicationIdentifier,
			int serverVersion, String commandIdentifier)
	{
		this.serverApplicationIdentifier = serverApplicationIdentifier;
		this.serverVersion = serverVersion;
		this.properties = new HashMap<String, String>();
		this.commandIdentifier = commandIdentifier;
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
	 * Notify error messages for a response container.
	 * 
	 * @param resourceGroup
	 *            The group of the resource that describes the error message.
	 * 
	 * @param resourceName
	 *            The name of the resource that describes the error message.
	 * 
	 * @param additionalText
	 *            Additional text to notify or <TT>null</TT>.
	 * 
	 * @param exception
	 *            Exception to notify or <TT>null</TT>..
	 */
	public void notifyError(Context context, String resourceGroup, String resourceName, String additionalText, Exception exception)
	{
		// Validate parameter
		if (resourceGroup == null || resourceGroup.length() == 0) return;
		if (resourceName == null || resourceName.length() == 0) return;

		// Get notification resource
		ResourceContainer notification = ResourceManager.notification(context, resourceGroup, resourceName);

		// Only the first error is stored explicitly as the triggering error
		if (this.getErrorCode() == null)
		{
			this.setErrorCode(notification.getRecourceIdentifier());
		}

		// Execute error message
		try
		{
			if (exception == null)
			{
				throw new Exception();
			}
			else
			{
				// Fire regular error notification
				context.getNotificationManager().notifyError(context, notification, additionalText, exception);
			}
		}
		catch (Exception e)
		{
			// Fire exception error notification
			context.getNotificationManager().notifyError(context, notification, additionalText, e);
		}
		finally
		{
			// Get dump of all accumulated errors
			this.setErrorDump(context);
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
	 * Setter
	 */
	private void setErrorDump(Context context)
	{
		this.errorDump = context.getNotificationManager().getDump(context);
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

		// Error code
		if (errorCode != null)
		{
			outputString += "\n" + "----------";
			outputString += "\n" + "Error code: " + this.errorCode;

			if (this.errorDump != null)
			{
				outputString += "\n" + "----------";
				outputString += "\n" + this.errorDump;
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
	 * Getter
	 */
	public String getServerApplicationIdentifier()
	{
		return serverApplicationIdentifier;
	}

	/**
	 * Getter
	 */
	public boolean isError()
	{
		if (this.getErrorCode() == null) return false;
		if (this.getErrorCode().length() == 0) return false;
		return true;
	}
}
