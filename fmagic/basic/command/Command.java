package fmagic.basic.command;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import fmagic.basic.application.DocumentationInterface;
import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * This class defines common functions needed by all commands.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 02.05.2013 - Created
 */
public abstract class Command implements DocumentationInterface
{
	// Current context
	protected Context context;

	// Current command identifier
	protected String commandIdentifier;

	// Request container
	protected RequestContainer requestContainer = null;

	// Response container
	protected ResponseContainer responseContainer = null;

	/**
	 * Constructor 1
	 */
	public Command()
	{
	}

	/**
	 * Constructor 2
	 */
	public Command(Context context, String commandIdentifier)
	{
		this.context = context;
		this.commandIdentifier = commandIdentifier;
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String resultText = "";

		// Get group name of the command
		ResourceContainer command = new ResourceContainer(this.getCommandIdentifier());

		// Set criteria
		String typeCriteria[] = { "Command" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = { command.getGroup() };
		resultText += context.getResourceManager().printResourceTemplate(context, includingResourceIdentifiers, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return resultText;
	}

	@Override
	public String printManual(Context context)
	{
		String resultText = "";

		// Get group name of the command
		ResourceContainer command = new ResourceContainer(this.getCommandIdentifier());

		// Set basic criteria
		String typeCriteria[] = { "Command" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String groupCriteria[] = { command.getGroup() };

		// Get identifier of the command
		String usageCriteriaIdentifier[] = { "Identifier" };

		resultText += "\n";
		resultText += "\n----------------------------------------";
		resultText += "\n--> COMMAND Identifier";
		resultText += "\n-->";
		resultText += "\n";
		resultText += context.getResourceManager().printResourceManual(context, typeCriteria, applicationCriteria, originCriteria, usageCriteriaIdentifier, groupCriteria);

		// Get parameter of the command
		String usageCriteriaParameter[] = { "Parameter" };

		resultText += "\n";
		resultText += "\n";
		resultText += "\n----------------------------------------";
		resultText += "\n--> COMMAND Parameter";
		resultText += "\n-->";
		resultText += "\n";
		resultText += context.getResourceManager().printResourceManual(context, typeCriteria, applicationCriteria, originCriteria, usageCriteriaParameter, groupCriteria);

		// Get return values of the command
		String usageCriteriaResult[] = { "Result" };

		resultText += "\n";
		resultText += "\n";
		resultText += "\n----------------------------------------";
		resultText += "\n--> COMMAND Result (Return values)";
		resultText += "\n-->";
		resultText += "\n";
		resultText += context.getResourceManager().printResourceManual(context, typeCriteria, applicationCriteria, originCriteria, usageCriteriaResult, groupCriteria);

		// Return
		return resultText;
	}

	@Override
	public String printIdentifierList(Context context)
	{
		String resultText = "";

		// Get group name of the command
		ResourceContainer command = new ResourceContainer(this.getCommandIdentifier());

		// Set criteria
		String typeCriteria[] = { "Command" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = { command.getGroup() };
		resultText += context.getResourceManager().printResourceIdentifierList(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return resultText;
	}

	/**
	 * Validate if all command parameters are set by the client.
	 * 
	 * @return Returns <TT>true</TT> if all parameters are set, otherwise
	 *         <TT>false</TT>.
	 */
	protected boolean validateCommandParameters()
	{
		try
		{
			// Get group name of the command
			ResourceContainer command = new ResourceContainer(this.getCommandIdentifier());
			if (command.getGroup() == null) return true;

			// Set criteria
			String typeCriteria[] = { "Command" };
			String applicationCriteria[] = null;
			String originCriteria[] = null;
			String usageCriteria[] = { "Parameter" };
			String groupCriteria[] = { command.getGroup() };

			// Create a copy of all request properties identifiers
			Set<String> parameterKeyList = new HashMap<String, String>(this.requestContainer.getProperties()).keySet();

			// Get list of parameters as a list of resource identifiers
			List<String> parameterIdentifiers = context.getResourceManager().getResourceIdentifierList(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);
			if (parameterIdentifiers == null) return true;

			// Prepare error settings
			String errorText = "--> Error on validating command parameters";
			boolean isError = false;

			// Go through the list and look for missing parameters
			for (String identifier : parameterIdentifiers)
			{
				ResourceContainer resource = new ResourceContainer(identifier);
				String aliasName = ResourceManager.commandParameter(this.context, resource.getGroup(), resource.getName()).getAliasName();

				if (!parameterKeyList.contains(aliasName))
				{
					errorText += "\n--> Missing parameter: '" + aliasName + "'";
					isError = true;
				}
				else
				{
					parameterKeyList.remove(aliasName);
				}
			}

			// Go through the list of remaining keys and mark them as unused
			// parameters
			for (String identifier : parameterKeyList)
			{
				errorText += "\n--> Uncalled parameter: '" + identifier + "'";
				isError = true;
			}

			// Fire error message
			if (isError == true)
			{
				this.notifyError("Command", "IntegrityError", errorText, null);
				return false;
			}
		}
		catch (Exception e)
		{
			this.notifyError("Command", "IntegrityError", null, e);
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Validate if all command results (return values) are set by the server.
	 * 
	 * @return Returns <TT>true</TT> if all results are set, otherwise
	 *         <TT>false</TT>.
	 */
	protected boolean validateCommandResults()
	{
		try
		{
			// Get group name of the command
			ResourceContainer command = new ResourceContainer(this.getCommandIdentifier());
			if (command.getGroup() == null) return true;

			// Set criteria
			String typeCriteria[] = { "Command" };
			String applicationCriteria[] = null;
			String originCriteria[] = null;
			String usageCriteria[] = { "Result" };
			String groupCriteria[] = { command.getGroup() };

			// Create a copy of all request properties identifiers
			Set<String> parameterKeyList = new HashMap<String, String>(this.responseContainer.getProperties()).keySet();

			// Get list of results as a list of resource identifiers
			List<String> resultIdentifiers = context.getResourceManager().getResourceIdentifierList(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);
			if (resultIdentifiers == null) return true;

			// Prepare error settings
			String errorText = "--> Error on validating command results (return values)";
			boolean isError = false;

			// Go through the list and look for missing results (return values)
			for (String identifier : resultIdentifiers)
			{
				ResourceContainer resource = new ResourceContainer(identifier);
				String aliasName = ResourceManager.commandParameter(this.context, resource.getGroup(), resource.getName()).getAliasName();

				if (!parameterKeyList.contains(aliasName))
				{
					errorText += "\n--> Missing result (return value): '" + aliasName + "'";
					isError = true;
				}
				else
				{
					parameterKeyList.remove(aliasName);
				}
			}

			// Go through the list of remaining keys and mark them as unused
			// results (return values)
			for (String identifier : parameterKeyList)
			{
				errorText += "\n--> Uncalled result (return value): '" + identifier + "'";
				isError = true;
			}

			// Fire error message
			if (isError == true)
			{
				this.notifyError("Command", "IntegrityError", errorText, null);
				return false;
			}
		}
		catch (Exception e)
		{
			this.notifyError("Command", "IntegrityError", null, e);
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Notify an error message for command processing.
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
	protected void notifyError(String resourceGroup, String resourceName, String additionalText, Exception exception)
	{
		// Validate parameter
		if (resourceGroup == null || resourceGroup.length() == 0) return;
		if (resourceName == null || resourceName.length() == 0) return;

		// Execute error message
		ResourceContainer notification = ResourceManager.notification(this.context, resourceGroup, resourceName);
		this.responseContainer.setErrorCode(notification.getRecourceIdentifier());
		
		try
		{
			if (exception == null)
			{
				throw new Exception();
			}
			else
			{
				this.context.getNotificationManager().notifyError(this.context, notification, additionalText, exception);
			}
		}
		catch (Exception e)
		{
			this.context.getNotificationManager().notifyError(this.context, notification, additionalText, e);
		}
	}

	/**
	 * Getter
	 */
	public String getCommandIdentifier()
	{
		return commandIdentifier;
	}
}
