package fmagic.basic;

/**
 * This class contains information and common functions of a command that is
 * send from a client to a server, executed on the server and send back.
 * <p>
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class CommandManager implements ManagerInterface
{
	/**
	 * Constructor
	 */
	public CommandManager()
	{
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = { "CommandManager" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceTemplate(context, includingResourceIdentifiers, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public String printManual(Context context)
	{
		String dumpText = "";

		String typeCriteria[] = { "CommandManager" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceManual(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public String printIdentifierList(Context context)
	{
		String dumpText = "";

		String typeCriteria[] = { "CommandManager" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceIdentifierList(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public boolean validateResources(Context context)
	{
		// Variables
		boolean isIntegrityError = false;
		ResourceManager resourceManager = context.getResourceManager();

		// Check for integrity errors (Named resource identifiers)
		try
		{
			for (ResourceContainer resourceContainer : resourceManager.getResources().values())
			{
				// Check if name is set
				String name = resourceContainer.getName();
				if (name == null || name.length() == 0) continue;

				// CommandManager: Alias name must be set
				if (resourceContainer.getType().equalsIgnoreCase("command") && resourceContainer.checkAliasName() == false)
				{
					String errorString = "--> Alias name for command identifier is not set.";
					String fileName = resourceManager.getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
					if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
					errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
					errorString += "\n--> The Alias name is used for instantiating the executing command class on server.";
					errorString += "\n--> Please set an Alias name that correspondences with a real server class.";

					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

					isIntegrityError = true;
				}
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isIntegrityError;
	}
	
	@Override
	public boolean readConfiguration(Context context)
	{
		return false;
	}

	@Override
	public boolean cleanEnvironment(Context context)
	{
		return false;
	}
}
