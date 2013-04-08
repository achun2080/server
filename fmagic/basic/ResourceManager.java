package fmagic.basic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import fmagic.basic.ResourceContainer.OriginEnum;
import fmagic.basic.ResourceContainer.TypeEnum;
import fmagic.basic.ResourceContainer.UsageEnum;

/**
 * This class implements the management of resources of the system.
 * <p>
 * The resource files must be saved in the directory <TT>fmagic.resource</TT>.
 * There are four different files the system tries to read when it starts.
 * <p>
 * The first file, called <TT>Basic</TT> file, contains all basic resources of
 * the system. The second file, called <TT>Common</TT>, contains all common
 * resources of the system. The third file, called <TT>Application</TT> file,
 * contains resources of a specific application, e. g. of the application
 * <TT>SeniorCitizen</TT>. The fourth file, called <TT>Extension</TT> file,
 * contains additional resources that are not part of the main system.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 28.12.2012 - Created
 * 
 */
public class ResourceManager implements ManagerInterface
{
	// List of resources
	final private HashMap<String, ResourceContainer> resources = new HashMap<String, ResourceContainer>();

	// Constants for data
	final public static String RESOURCE_FILE_APPLCIATION = "ResourceFile.Application";
	final public static String RESOURCE_FILE_VERSION = "ResourceFile.Version";
	final public static String RESOURCE_FILE_LANGUAGE = "ResourceFile.Language";
	final public static String RESOURCE_FILE_LICENSEKEY = "ResourceFile.LicenseKey";

	// List of additional information
	private HashMap<String, String> notFoundProperties = new HashMap<String, String>();
	private HashMap<String, String> readResourceIdentifiersList = new HashMap<String, String>();
	private HashMap<String, String> typeGroupNameIdentifierList = new HashMap<String, String>();
	private HashMap<String, String> aliasNameIdentifierList = new HashMap<String, String>();
	private HashMap<String, String> commonIdentifiersProperties = new HashMap<String, String>();

	// Flags for synchronizing the locking of messages
	private ConcurrentHashMap<String, Boolean> processingActive = new ConcurrentHashMap<String, Boolean>();
	private int messageLostCounter = 0;

	/**
	 * Constructor
	 */
	public ResourceManager()
	{
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = { "Resource" };
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

		String typeCriteria[] = { "Resource" };
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

		String typeCriteria[] = { "Resource" };
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

				// Resource Attribute: Alias name must be set
				if (resourceContainer.getType().equalsIgnoreCase("resource") & resourceContainer.getUsage().equalsIgnoreCase("attribute") && resourceContainer.checkAliasName() == false)
				{
					String errorString = "--> Alias name for resource attribute identifier is not set.";
					String fileName = resourceManager.getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
					if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
					errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
					errorString += "\n--> The Alias name is used to read attributes from a resource.";
					errorString += "\n--> Please set an Alias name.";

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

	/**
	 * Get a resource from the list of resources.
	 * <p>
	 * Please notice: You have to avoid recursive calls of this method.
	 * Recursive calls can occur if an event is fired during a running event. In
	 * this case the recursive call happens if the resource for the event
	 * <TT>ResourceNotFound</TT> is not available, because the method
	 * <TT>notifyEvent()</TT> would be fired in a circle.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param identifier
	 *            Identifier of the resource.
	 * 
	 * @return Returns the resource container <TT>ResourceContainer</TT> or
	 *         <TT>null</TT> if there was no resource found.
	 * 
	 */
	public ResourceContainer getResource(Context context, String identifier)
	{
		// Lock message processing
		if (this.lockMessageHandling("GetResourceItem", identifier) == true) return null;

		// Process function
		ResourceContainer resourceContainer = null;

		try
		{
			while (true)
			{
				// Get resource container
				resourceContainer = this.resources.get(identifier);

				/*
				 * Resource container not found
				 */
				if (resourceContainer == null)
				{
					// Get a provisional resource container
					resourceContainer = this.getProvisionalResourceContainer(identifier);
					if (resourceContainer == null) break;

					// Fire an event: The event is only to be notified once
					if (this.notFoundProperties.get(identifier) == null)
					{
						this.notFoundProperties.put(identifier, "");
						String errorString = "--> resource identifier searched for: '" + identifier + "'";
						context.getNotificationManager().notifyEvent(context, resourceContainer, errorString, null);
					}
				}

				// Break
				break;
			}
		}
		catch (Exception e)
		{
			// Be silent
		}
		finally
		{
			// Unlock message processing
			this.unlockMessageHandling("GetResourceItem");
		}

		// Return
		return resourceContainer;
	}

	/**
	 * Composes the path of the data directory of common resource files.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the directory were the data files are stored.
	 * 
	 */
	private String getResourceFilePath(Context context)
	{
		return FileLocationManager.getRootPath() + FileLocationManager.getPathElementDelimiterString() + FileLocationManager.getResourceSubPath();
	}

	/**
	 * Composes the file name of common resource files.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param applicationName
	 *            The name of the application from the point of view of the
	 *            resource files, including Basic, Common and Extension.
	 * 
	 * @return Returns the file name of the data files.
	 * 
	 */
	private String getResourceFileName(Context context, String applicationName)
	{
		String fileName = FileLocationManager.getResourceFileName();
		fileName = FileLocationManager.replacePlaceholder(context, fileName, applicationName, null);
		return fileName;
	}

	/**
	 * Check an item if it is a duplicate of Type/Group/Name of a resource
	 * identifier.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param fileName
	 *            The file name of the actual resource file.
	 * 
	 * @param resourceContainer
	 *            The resource container to check.
	 * 
	 * @param line
	 *            The text of the actual line of the resource file.
	 * 
	 * @param lineNumber
	 *            The line number of the actual line of the resource file.
	 * 
	 * @return Returns <TT>true</TT> if a duplicate was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean ckeckDuplicateTypeGroupName(Context context, String fileName, ResourceContainer resourceContainer, String line, int lineNumber)
	{
		// Check parameter
		if (resourceContainer == null) return false;

		// Variables
		boolean isDuplicateFound = false;

		// Check for duplicates
		try
		{
			// Check if name is set
			String name = resourceContainer.getName();
			if (name == null || name.length() == 0) return false;

			// Compose identifier
			String identifier = ResourceContainer.composeTypeGroupNameIdentifierString(resourceContainer.getType(), resourceContainer.getGroup(), resourceContainer.getName());

			// Check duplicate
			if (this.typeGroupNameIdentifierList.containsKey(identifier))
			{
				String errorString = "--> Duplicate resource Type/Group/Name found";
				errorString += "\n--> Identifier: '" + identifier + "'";
				errorString += "\n--> In file: '" + fileName + "'";
				errorString += "\n--> Line number: '" + String.valueOf(lineNumber) + "'";
				errorString += "\n--> Line text: '" + line + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "DuplicateOfTypeGroupNameIdentifier"), errorString, null);

				isDuplicateFound = true;
			}

			// Add to to the list of resources
			this.typeGroupNameIdentifierList.put(identifier, resourceContainer.getRecourceIdentifier());
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isDuplicateFound;
	}

	/**
	 * Check an item if it is a duplicate resource identifier.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param fileName
	 *            The file name of the actual resource file.
	 * 
	 * @param identifier
	 *            The resource identifier to check.
	 * 
	 * @param line
	 *            The text of the actual line of the resource file.
	 * 
	 * @param lineNumber
	 *            The line number of the actual line of the resource file.
	 * 
	 * @return Returns <TT>true</TT> if a duplicate was found, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean ckeckDuplicateIdentifier(Context context, String fileName, String identifier, String line, int lineNumber)
	{
		// Check parameter
		if (identifier == null) return false;

		// Variables
		boolean isDuplicateFound = false;

		// Check for duplicates
		try
		{
			if (this.readResourceIdentifiersList.containsKey(identifier))
			{
				String duplicatFileName = this.readResourceIdentifiersList.get(identifier);

				String errorString = "--> Duplicate resource identifier found";
				errorString += "\n--> Identifier: '" + identifier + "'";
				errorString += "\n--> In file: '" + fileName + "'";
				errorString += "\n--> Line number: '" + String.valueOf(lineNumber) + "'";
				errorString += "\n--> Line text: '" + line + "'";
				if (duplicatFileName != null && !fileName.equals(duplicatFileName)) errorString += "\n--> Duplicate file: '" + duplicatFileName + "'";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "DuplicateOfResourceIdentifier"), errorString, null);

				isDuplicateFound = true;
			}
			else
			{
				// Add to the list of read resources
				this.readResourceIdentifiersList.put(identifier, fileName);
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isDuplicateFound;
	}

	/**
	 * Check an item if there is a duplicate ALIAS name set in another resource
	 * identifier of the same Type/Usage.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param aliasName
	 *            The alias name to check.
	 * 
	 * @param fileName
	 *            The file name of the actual resource file.
	 * 
	 * @param resourceContainer
	 *            The resource container to check.
	 * 
	 * @param line
	 *            The text of the actual line of the resource file.
	 * 
	 * @param lineNumber
	 *            The line number of the actual line of the resource file.
	 * 
	 * @return Returns <TT>true</TT> if a duplicate ALIAS name was found,
	 *         otherwise <TT>false</TT>.
	 */
	private boolean ckeckDuplicateAliasName(Context context, String aliasName, String fileName, ResourceContainer resourceContainer, String line, int lineNumber)
	{
		// Check parameter
		if (resourceContainer == null) return false;
		if (aliasName == null) return false;
		if (aliasName.length() == 0) return false;

		// Variables
		boolean isDuplicateFound = false;

		// Check for duplicates
		try
		{
			// Check if name is set
			String name = resourceContainer.getName();
			if (name == null || name.length() == 0) return false;

			// Compose identifier
			String type = resourceContainer.getType();
			if (type == null || type.length() == 0) return false;

			String usage = resourceContainer.getUsage();
			if (usage == null || usage.length() == 0) return false;

			String identifier = type + "/" + usage + "=" + aliasName;

			// Check duplicate
			if (this.aliasNameIdentifierList.containsKey(identifier))
			{
				String errorString = "--> Duplicate Alias name found, based on the same Type/Usage.";
				errorString += "\n--> Type/Usage: '" + type + "/" + usage + "'";
				errorString += "\n--> Alias name: '" + aliasName + "'";
				errorString += "\n--> In file: '" + fileName + "'";
				errorString += "\n--> Line number: '" + String.valueOf(lineNumber) + "'";
				errorString += "\n--> Line text: '" + line + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "DuplicateOfTypeGroupNameIdentifier"), errorString, null);

				isDuplicateFound = true;
			}

			// Add to to the list of used alias names
			this.aliasNameIdentifierList.put(identifier, resourceContainer.getRecourceIdentifier());
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isDuplicateFound;
	}

	/**
	 * Check if the resource item is already set as a resource identifier.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param fileName
	 *            The file name of the actual resource file.
	 * 
	 * @param resourceContainer
	 *            The resource container to check.
	 * 
	 * @param line
	 *            The text of the actual line of the resource file.
	 * 
	 * @param lineNumber
	 *            The line number of the actual line of the resource file.
	 * 
	 * @return Returns <TT>true</TT> if the resource item was already set
	 *         before, otherwise <TT>false</TT>.
	 */
	private boolean ckeckTypeGroupNameIsAlreadySet(Context context, String fileName, ResourceContainer resourceContainer, String line, int lineNumber)
	{
		// Check parameter
		if (resourceContainer == null) return false;

		// Variables
		boolean isAlreadySet = true;

		// Check for duplicates
		try
		{
			// Check if name is set
			String name = resourceContainer.getName();
			if (name == null || name.length() == 0) return isAlreadySet;

			// Compose identifier
			String identifier = ResourceContainer.composeTypeGroupNameIdentifierString(resourceContainer.getType(), resourceContainer.getGroup(), resourceContainer.getName());

			// Check duplicate
			if (!this.typeGroupNameIdentifierList.containsKey(identifier))
			{
				String errorString = "--> Resource Type/Group/Name is not set yet.";
				errorString += "\n--> Please define a regular resource item first, before you use it, e. g. as label text.";
				errorString += "\n--> Identifier: '" + identifier + "'";
				errorString += "\n--> In file: '" + fileName + "'";
				errorString += "\n--> Line number: '" + String.valueOf(lineNumber) + "'";
				errorString += "\n--> Line text: '" + line + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "TypeGroupNameIdentifierIsNotSetYet"), errorString, null);

				isAlreadySet = false;
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isAlreadySet;
	}

	/**
	 * Check if the application of the resource identifier is defined.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param fileName
	 *            The file name of the actual resource file.
	 * 
	 * @param resourceContainer
	 *            The resource container to check.
	 * 
	 * @param line
	 *            The text of the actual line of the resource file.
	 * 
	 * @param lineNumber
	 *            The line number of the actual line of the resource file.
	 * 
	 * @return Returns <TT>true</TT> if the application is defined, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean ckeckApplicationOfIdentifier(Context context, String fileName, ResourceContainer resourceContainer, String line, int lineNumber)
	{
		// Check parameter
		if (resourceContainer == null) return false;

		// Variables
		boolean isApplicationFound = false;

		// Check for application
		try
		{
			// Get application part of the identifier
			String applicationName = resourceContainer.getApplication();

			// Search in application list
			for (ApplicationManager.ApplicationIdentifierEnum application : ApplicationManager.ApplicationIdentifierEnum.values())
			{
				if (application.name().equals(applicationName)) isApplicationFound = true;
			}

			// Fire error if the application was not found
			if (isApplicationFound == false)
			{
				String errorString = "--> Application of the resource identifier not defined";
				errorString += "\n--> Searched for application: '" + applicationName + "'";
				errorString += "\n--> Identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
				errorString += "\n--> In file: '" + fileName + "'";
				errorString += "\n--> Line number: '" + String.valueOf(lineNumber) + "'";
				errorString += "\n--> Line text: '" + line + "'";

				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "ApplicationNotDefined"), errorString, null);
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isApplicationFound;
	}

	/**
	 * Get resource object by Type, Application, Usage, Group and Name.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param typeEnum
	 *            The type of the resource as <TT>TypeEnum</TT>.
	 * 
	 * @param applicationEnum
	 *            The application of the resource as
	 *            <TT>ApplicationIdentifierEnum</TT>.
	 * 
	 * @param originEnum
	 *            The origin of the resource as <TT>OriginEnum</TT>.
	 * 
	 * @param usageEnum
	 *            The usage of the resource as <TT>UsageEnum</TT>.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 */
	public ResourceContainer getResourceContainer(Context context, TypeEnum typeEnum, ApplicationManager.ApplicationIdentifierEnum applicationEnum, OriginEnum originEnum, UsageEnum usageEnum, String group, String name)
	{
		// Check parameter
		boolean error = false;

		// Check Type
		String type = null;

		if (typeEnum == null)
		{
			error = true;
		}
		else
		{
			type = typeEnum.toString();
		}

		// Check Application
		String application = null;

		if (applicationEnum == null)
		{
			error = true;
		}
		else
		{
			application = applicationEnum.toString();
		}

		// Check Origin
		String origin = null;

		if (originEnum == null)
		{
			error = true;
		}
		else
		{
			origin = originEnum.toString();
		}

		// Check Usage
		String usage = null;

		if (usageEnum == null)
		{
			error = true;
		}
		else
		{
			usage = usageEnum.toString();
		}

		// Check Group
		if (group == null || group.length() == 0) error = true;

		// Check Name
		if (name == null || name.length() == 0) error = true;

		// Create error message
		if (error == true)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "MissingParameter"), "--> Type [" + type + "], " + "Application [" + application + "], " + "Origin [" + origin + "], " + "Usage [" + usage + "], " + "Group [" + group + "], " + "Name [" + name + "]", null);
			return null;
		}

		// Set resource identifier
		String resourceIdentifier = ResourceContainer.composeResourceIdentifierString(type, application, origin, usage, group, name);

		// Get resource from list
		ResourceContainer resourceContainer = this.getResource(context, resourceIdentifier);

		// Return
		return resourceContainer;
	}

	/**
	 * Get a provisional resource container via identifier.
	 * 
	 * @param identifier
	 *            The identifier of the resource.
	 */
	public ResourceContainer getProvisionalResourceContainer(String identifier)
	{
		// Check parameter
		if (identifier == null) return null;
		if (identifier.length() == 0) return null;

		// Split identifier
		String[] parts = identifier.split("\\.");
		if (parts.length < 5 || parts.length > 6) return null;

		// Get resource from list
		ResourceContainer resourceContainer = new ResourceContainer(identifier);

		// Return
		return resourceContainer;
	}

	/**
	 * Print a Manual containing all information of the resource properties.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param typeList
	 *            The list of resource Types that has to be considered, as
	 *            String array, or <TT>null</TT> if all Types are to be printed.
	 * 
	 * @param applicationList
	 *            The list of resource Applications that has to be considered,
	 *            as String array, or <TT>null</TT> if all Applications are to
	 *            be printed.
	 * 
	 * @param originList
	 *            The list of resource Origins that has to be considered, as
	 *            String array, or <TT>null</TT> if all Origins are to be
	 *            printed.
	 * 
	 * @param usageList
	 *            The list of resource Usages that has to be considered, as
	 *            String array, or <TT>null</TT> if all Usages are to be
	 *            printed.
	 * 
	 * @param groupList
	 *            The list of resource Groups that has to be considered, as
	 *            String array, or <TT>null</TT> if all Groups are to be
	 *            printed.
	 * 
	 * @return Returns the printed text as String.
	 */
	public String printResourceManual(Context context, String[] typeList, String[] applicationList, String[] originList, String[] usageList, String[] groupList)
	{
		String manualText = "";

		// Sorting the keys alphabetically
		List<String> sortedListManual = new ArrayList<String>();
		sortedListManual.addAll(this.resources.keySet());
		Collections.sort(sortedListManual);

		// List all items
		Iterator<String> iterManual = sortedListManual.iterator();

		while (iterManual.hasNext())
		{
			// Get ENUM
			String identifier = iterManual.next();
			ResourceContainer resourceContainer = this.resources.get(identifier);

			// Check typeList
			if (typeList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < typeList.length; i++)
				{
					if (typeList[i].equalsIgnoreCase(resourceContainer.getType()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check applicationList
			if (applicationList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < applicationList.length; i++)
				{
					if (applicationList[i].equalsIgnoreCase(resourceContainer.getApplication()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check originList
			if (originList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < originList.length; i++)
				{
					if (originList[i].equalsIgnoreCase(resourceContainer.getOrigin()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check usageList
			if (usageList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < usageList.length; i++)
				{
					if (usageList[i].equalsIgnoreCase(resourceContainer.getUsage()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check groupList
			if (groupList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < groupList.length; i++)
				{
					if (groupList[i].equalsIgnoreCase(resourceContainer.getGroup()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Print resource to manual
			if (resourceContainer != null)
			{
				manualText += "\n\n############################################################";
				manualText += "\n### " + resourceContainer.getRecourceIdentifier();
				manualText += "\n############################################################";
				manualText += resourceContainer.printManual(context);
			}
		}

		// Return
		return Util.normalizeNewLine(manualText);
	}

	/**
	 * Print a Template containing all information of the resource properties.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param typeList
	 *            The list of resource Types that has to be considered, as
	 *            String array, or <TT>null</TT> if all Types are to be printed.
	 * 
	 * @param applicationList
	 *            The list of resource Applications that has to be considered,
	 *            as String array, or <TT>null</TT> if all Applications are to
	 *            be printed.
	 * 
	 * @param originList
	 *            The list of resource Origins that has to be considered, as
	 *            String array, or <TT>null</TT> if all Origins are to be
	 *            printed.
	 * 
	 * @param usageList
	 *            The list of resource Usages that has to be considered, as
	 *            String array, or <TT>null</TT> if all Usages are to be
	 *            printed.
	 * 
	 * @param groupList
	 *            The list of resource Groups that has to be considered, as
	 *            String array, or <TT>null</TT> if all Groups are to be
	 *            printed.
	 * 
	 * @return Returns the printed text as String.
	 */
	public String printResourceTemplate(Context context, boolean includingResourceIdentifiers, String[] typeList, String[] applicationList, String[] originList, String[] usageList, String[] groupList)
	{
		String manualText = "";

		// Sorting the keys alphabetically
		List<String> sortedListManual = new ArrayList<String>();
		sortedListManual.addAll(this.resources.keySet());
		Collections.sort(sortedListManual);

		// List all items
		Iterator<String> iterManual = sortedListManual.iterator();

		while (iterManual.hasNext())
		{
			// Get ENUM
			String identifier = iterManual.next();
			ResourceContainer resourceContainer = this.resources.get(identifier);

			// Check typeList
			if (typeList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < typeList.length; i++)
				{
					if (typeList[i].equalsIgnoreCase(resourceContainer.getType()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check applicationList
			if (applicationList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < applicationList.length; i++)
				{
					if (applicationList[i].equalsIgnoreCase(resourceContainer.getApplication()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check originList
			if (originList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < originList.length; i++)
				{
					if (originList[i].equalsIgnoreCase(resourceContainer.getOrigin()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check usageList
			if (usageList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < usageList.length; i++)
				{
					if (usageList[i].equalsIgnoreCase(resourceContainer.getUsage()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check groupList
			if (groupList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < groupList.length; i++)
				{
					if (groupList[i].equalsIgnoreCase(resourceContainer.getGroup()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Print resource to manual
			if (resourceContainer != null)
			{
				manualText += resourceContainer.printTemplate(context, includingResourceIdentifiers);
			}
		}

		// Return
		return Util.normalizeNewLine(manualText);
	}

	/**
	 * Print a flat list of identifiers of the resource items as a new line
	 * separated string.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param typeList
	 *            The list of resource Types that has to be considered, as
	 *            String array, or <TT>null</TT> if all Types are to be printed.
	 * 
	 * @param applicationList
	 *            The list of resource Applications that has to be considered,
	 *            as String array, or <TT>null</TT> if all Applications are to
	 *            be printed.
	 * 
	 * @param originList
	 *            The list of resource Origins that has to be considered, as
	 *            String array, or <TT>null</TT> if all Origins are to be
	 *            printed.
	 * 
	 * @param usageList
	 *            The list of resource Usages that has to be considered, as
	 *            String array, or <TT>null</TT> if all Usages are to be
	 *            printed.
	 * 
	 * @param groupList
	 *            The list of resource Groups that has to be considered, as
	 *            String array, or <TT>null</TT> if all Groups are to be
	 *            printed.
	 * 
	 * @return Returns the printed text as String.
	 */
	public String printResourceIdentifierList(Context context, String[] typeList, String[] applicationList, String[] originList, String[] usageList, String[] groupList)
	{
		String listText = "";

		// Sorting the keys alphabetically
		List<String> sortedListManual = new ArrayList<String>();
		sortedListManual.addAll(this.resources.keySet());
		Collections.sort(sortedListManual);

		// List all items
		Iterator<String> iterManual = sortedListManual.iterator();

		while (iterManual.hasNext())
		{
			// Get ENUM
			String identifier = iterManual.next();
			ResourceContainer resourceContainer = this.resources.get(identifier);

			// Check typeList
			if (typeList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < typeList.length; i++)
				{
					if (typeList[i].equalsIgnoreCase(resourceContainer.getType()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check applicationList
			if (applicationList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < applicationList.length; i++)
				{
					if (applicationList[i].equalsIgnoreCase(resourceContainer.getApplication()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check originList
			if (originList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < originList.length; i++)
				{
					if (originList[i].equalsIgnoreCase(resourceContainer.getOrigin()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check usageList
			if (usageList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < usageList.length; i++)
				{
					if (usageList[i].equalsIgnoreCase(resourceContainer.getUsage()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check groupList
			if (groupList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < groupList.length; i++)
				{
					if (groupList[i].equalsIgnoreCase(resourceContainer.getGroup()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Print resource to manual
			if (resourceContainer != null)
			{
				listText += resourceContainer.getRecourceIdentifier() + "\n";
			}
		}

		// Return
		return Util.normalizeNewLine(listText);
	}

	/**
	 * Get a list of resource item identifiers.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param typeList
	 *            The list of resource Types that has to be considered, as
	 *            String array, or <TT>null</TT> if all Types are to be printed.
	 * 
	 * @param applicationList
	 *            The list of resource Applications that has to be considered,
	 *            as String array, or <TT>null</TT> if all Applications are to
	 *            be printed.
	 * 
	 * @param originList
	 *            The list of resource Origins that has to be considered, as
	 *            String array, or <TT>null</TT> if all Origins are to be
	 *            printed.
	 * 
	 * @param usageList
	 *            The list of resource Usages that has to be considered, as
	 *            String array, or <TT>null</TT> if all Usages are to be
	 *            printed.
	 * 
	 * @param groupList
	 *            The list of resource Groups that has to be considered, as
	 *            String array, or <TT>null</TT> if all Groups are to be
	 *            printed.
	 * 
	 * @return Returns the printed text as String.
	 */
	public List<String> getResourceIdentifierList(Context context, String[] typeList, String[] applicationList, String[] originList, String[] usageList, String[] groupList)
	{
		List<String> resultList = new ArrayList<String>();

		// Sorting the keys alphabetically
		List<String> sortedListManual = new ArrayList<String>();
		sortedListManual.addAll(this.resources.keySet());
		Collections.sort(sortedListManual);

		// List all items
		Iterator<String> iterManual = sortedListManual.iterator();

		while (iterManual.hasNext())
		{
			// Get ENUM
			String identifier = iterManual.next();
			ResourceContainer resourceContainer = this.resources.get(identifier);

			// Check typeList
			if (typeList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < typeList.length; i++)
				{
					if (typeList[i].equalsIgnoreCase(resourceContainer.getType()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check applicationList
			if (applicationList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < applicationList.length; i++)
				{
					if (applicationList[i].equalsIgnoreCase(resourceContainer.getApplication()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check originList
			if (originList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < originList.length; i++)
				{
					if (originList[i].equalsIgnoreCase(resourceContainer.getOrigin()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check usageList
			if (usageList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < usageList.length; i++)
				{
					if (usageList[i].equalsIgnoreCase(resourceContainer.getUsage()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Check groupList
			if (groupList != null)
			{
				boolean criteriaFound = false;

				for (int i = 0; i < groupList.length; i++)
				{
					if (groupList[i].equalsIgnoreCase(resourceContainer.getGroup()))
					{
						criteriaFound = true;
						break;
					}
				}

				if (criteriaFound == false) continue;
			}

			// Add to list
			if (resourceContainer != null)
			{
				resultList.add(resourceContainer.getRecourceIdentifier());
			}
		}

		// Return
		return resultList;
	}

	/**
	 * Lock the processing of a resource event to avoid recursive calls.
	 * Recursive calls can occur if a resource is processed during a running
	 * resource processing.
	 * <p>
	 * The locking is done for each thread and message type separately.
	 * <p>
	 * Be careful!
	 * 
	 * @param messageType
	 *            The type of the message to lock.
	 * 
	 * @param resourceIdentifier
	 *            The identifier of the message.
	 * 
	 * @return Returns <TT>true</TT> if the resource processing is locked,
	 *         otherwise <TT>false</TT>.
	 */
	private boolean lockMessageHandling(String messageType, String resourceIdentifier)
	{
		// Validate parameter
		if (messageType == null) return true;

		// Lock the message handling
		try
		{
			synchronized (this.processingActive)
			{
				String threadIdentifier = String.valueOf(Thread.currentThread().getId()) + "#" + messageType;
				Boolean isActive = this.processingActive.get(threadIdentifier);

				if (isActive != null && isActive == true)
				{
					this.messageLostCounter++;
					System.out.println("\n*** " + messageType + " resource lost (" + String.valueOf(this.messageLostCounter) + ")");
					if (resourceIdentifier != null) System.out.println("*** " + resourceIdentifier);
					return true;
				}

				isActive = true;
				this.processingActive.put(threadIdentifier, isActive);

				// System.out.println("----------");
				// for (String value : this.processingActive.keySet())
				// {
				// System.out.println("--> " + value);
				// }
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Clear the Map if it has become too big. The should never happen.
		try
		{
			synchronized (this.processingActive)
			{
				if (this.processingActive.size() > 10000)
				{
					System.out.println("\n*** Processing active resource map has more than 10000 items and was cleared.");
					this.processingActive.clear();
				}
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return false;
	}

	/**
	 * Unlock the processing of a resource event to avoid recursive calls.
	 * Recursive calls can occur if a resource is processed during a running
	 * ENUM processing.
	 * <p>
	 * The unlocking is done for each thread and message type separately.
	 * <p>
	 * Be careful!
	 * 
	 * @param messageType
	 *            The type of the message to lock.
	 */
	private void unlockMessageHandling(String messageType)
	{
		try
		{
			synchronized (this.processingActive)
			{
				String threadIdentifier = String.valueOf(Thread.currentThread().getId()) + "#" + messageType;
				this.processingActive.remove(threadIdentifier);
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return;
	}

	/**
	 * Get resource object from by Type, Group and Name.
	 * <p>
	 * Please notice: You have to avoid recursive calls of this method.
	 * Recursive calls can occur if an error or event is fired during a running
	 * error/event. In this case the recursive call happens if the resource for
	 * the error/event couldn't be found, because the methods
	 * <TT>notifyError()</TT> or <TT>notifyEvent()</TT> would be fired in a
	 * circle.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param type
	 *            The type of the resource as String.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 * 
	 * @return Returns <TT>true</TT> if the resource container was found,
	 *         otherwise <TT>false</TT>.
	 */
	private static ResourceContainer getResourceContainerByTypeGroupName(Context context, String type, String group, String name)
	{
		// Check parameter
		if (group == null) group = "";
		if (name == null) name = "";

		// Lock message processing
		if (context.getResourceManager().lockMessageHandling("getResourceContainerByTypeGroupName", type + "/" + group + "/" + name) == true) return null;

		/*
		 * Get resource container
		 */
		ResourceContainer resourceContainer = null;

		try
		{
			while (true)
			{
				// Check parameter
				boolean error = false;

				if (type == null) error = true;
				if (type.length() == 0) error = true;
				if (group == null) error = true;
				if (group.length() == 0) error = true;
				if (name == null) error = true;
				if (name.length() == 0) error = true;

				// Get Type/Group/Name resource identifier
				String tgnIdentifier = ResourceContainer.composeTypeGroupNameIdentifierString(type, group, name);
				if (tgnIdentifier == null) error = true;

				// Create error message: Missing parameter
				if (error == true)
				{
					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "MissingParameter"), "-->  Group [" + group + "], " + "Name [" + name + "]", null);
					break;
				}

				// Get full resource identifier
				String fullIdentifier = context.getResourceManager().typeGroupNameIdentifierList.get(tgnIdentifier);

				// Full identifier not found
				if (fullIdentifier == null)
				{
					// Get a provisional resource container
					resourceContainer = context.getResourceManager().getProvisionalResourceContainer(tgnIdentifier);
					if (resourceContainer == null) break;

					// Fire an event: The event is only to be notified once
					if (context.getResourceManager().notFoundProperties.get(tgnIdentifier) == null)
					{
						context.getResourceManager().notFoundProperties.put(tgnIdentifier, "");
						String errorString = "--> Resource identifier searched for: '" + tgnIdentifier + "'";
						context.getNotificationManager().notifyEvent(context, resourceContainer, errorString, null);
					}

					// Break
					break;
				}

				// Get resource container from list
				resourceContainer = context.getResourceManager().getResource(context, fullIdentifier);

				// Break
				break;
			}
		}
		catch (Exception e)
		{
			// Be silent
		}
		finally
		{
			// Unlock message processing
			context.getResourceManager().unlockMessageHandling("getResourceContainerByTypeGroupName");
		}

		// Return
		return resourceContainer;
	}

	/**
	 * Get resource object from Type "LocalData" by Group and Name.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 * 
	 * @return Returns the resource container, or <TT>null</TT> if an error
	 *         occurred.
	 */
	public static ResourceContainer localdata(Context context, String group, String name)
	{
		return ResourceManager.getResourceContainerByTypeGroupName(context, ResourceContainer.TypeEnum.LocalData.toString(), group, name);
	}

	/**
	 * Get resource object from Type "Notification" by Group and Name.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 * 
	 * @return Returns the resource container, or <TT>null</TT> if an error
	 *         occurred.
	 */
	public static ResourceContainer notification(Context context, String group, String name)
	{
		return ResourceManager.getResourceContainerByTypeGroupName(context, ResourceContainer.TypeEnum.Notification.toString(), group, name);
	}

	/**
	 * Get resource object from Type "Configuration" by Group and Name.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 * 
	 * @return Returns the resource container, or <TT>null</TT> if an error
	 *         occurred.
	 */
	public static ResourceContainer configuration(Context context, String group, String name)
	{
		return ResourceManager.getResourceContainerByTypeGroupName(context, ResourceContainer.TypeEnum.Configuration.toString(), group, name);
	}

	/**
	 * Get resource object from Type "Context" by Group and Name.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 * 
	 * @return Returns the resource container, or <TT>null</TT> if an error
	 *         occurred.
	 */
	public static ResourceContainer context(Context context, String group, String name)
	{
		return ResourceManager.getResourceContainerByTypeGroupName(context, ResourceContainer.TypeEnum.Context.toString(), group, name);
	}

	/**
	 * Get resource object from Type "CommandManager" by Group and Name.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 * 
	 * @return Returns the resource container, or <TT>null</TT> if an error
	 *         occurred.
	 */
	public static ResourceContainer command(Context context, String group, String name)
	{
		return ResourceManager.getResourceContainerByTypeGroupName(context, ResourceContainer.TypeEnum.Command.toString(), group, name);
	}

	/**
	 * Get resource object from Type "Resource Attribute" by Group and Name.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 * 
	 * @return Returns the resource container, or <TT>null</TT> if an error
	 *         occurred.
	 */
	public static ResourceContainer attribute(Context context, String group, String name)
	{
		return ResourceManager.getResourceContainerByTypeGroupName(context, ResourceContainer.TypeEnum.Resource.toString(), group, name);
	}

	/**
	 * Get resource object from Type "Label" by Group and Name.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 * 
	 * @return Returns the resource container, or <TT>null</TT> if an error
	 *         occurred.
	 */
	public static ResourceContainer label(Context context, String group, String name)
	{
		return ResourceManager.getResourceContainerByTypeGroupName(context, ResourceContainer.TypeEnum.Label.toString(), group, name);
	}

	/**
	 * Get resource object from Type "Right" by Group and Name.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 * 
	 * @return Returns the resource container, or <TT>null</TT> if an error
	 *         occurred.
	 */
	public static ResourceContainer right(Context context, String group, String name)
	{
		return ResourceManager.getResourceContainerByTypeGroupName(context, ResourceContainer.TypeEnum.Right.toString(), group, name);
	}

	/**
	 * Get resource object from Type "License" by Group and Name.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 * 
	 * @return Returns the resource container, or <TT>null</TT> if an error
	 *         occurred.
	 */
	public static ResourceContainerLicense license(Context context, String group, String name)
	{
		// Get native resource container
		ResourceContainer resourceContainer = ResourceManager.getResourceContainerByTypeGroupName(context, ResourceContainer.TypeEnum.License.toString(), group, name);
		if (resourceContainer == null) return null;

		// Get specific resource container
		ResourceContainerLicense resourceContainerLicense = new ResourceContainerLicense(resourceContainer);

		// Return
		return resourceContainerLicense;
	}

	/**
	 * Get resource object from Type "Media" by Group and Name.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 * 
	 * @return Returns the resource container, or <TT>null</TT> if an error
	 *         occurred.
	 */
	public static ResourceContainerMedia media(Context context, String group, String name)
	{
		// Get native resource container
		ResourceContainer resourceContainer = ResourceManager.getResourceContainerByTypeGroupName(context, ResourceContainer.TypeEnum.Media.toString(), group, name);
		if (resourceContainer == null) return null;

		// Get specific resource container
		ResourceContainerMedia resourceContainerMedia = new ResourceContainerMedia(resourceContainer);

		// Return
		return resourceContainerMedia;
	}

	/**
	 * Hide password and security information that are hold in a value of a
	 * resource identifier.
	 * <p>
	 * Each value is set to "******" if the resource identifier <TT>Name</TT>
	 * contains specific words like "Password" or "Private" or "Key".
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param resourceIdentifier
	 *            The resource identifier to check.
	 * 
	 * @param value
	 *            The value to override, if necessary.
	 * 
	 * @return Returns the original value if no suspicious key word was found,
	 *         otherwise "******".
	 */
	public static String hideSecurityValue(Context context, String resourceIdentifier, String value)
	{
		// Check parameter
		if (resourceIdentifier == null) return value;
		if (resourceIdentifier.length() == 0) return value;

		// Value hide string
		String valueHideString = "******";

		// Check suspicious key words
		try
		{
			ResourceContainer resourceContainer = context.getResourceManager().getProvisionalResourceContainer(resourceIdentifier);

			String name = resourceContainer.getName().toLowerCase();

			if (name.contains("password")) return valueHideString;
			if (name.contains("private")) return valueHideString;
			if (name.contains("key")) return valueHideString;
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return value;
	}

	/**
	 * Load common resources from a resource file.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param applicationResource
	 *            Application to consider.
	 * 
	 * @param applicationVersion
	 *            Current version of the application.
	 * 
	 * @return Returns <TT>true</TT> if the resource file could be read
	 *         successfully, otherwise <TT>false</TT>.
	 */
	public boolean loadCommonResourceFile(Context context, String applicationIdentifier, int applicationVersion)
	{
		HashMap<String, String> organizationalProperties = new HashMap<String, String>();

		// Set File name
		String fileName = this.getResourceFilePath(context) + FileLocationManager.getPathElementDelimiterString() + getResourceFileName(context, applicationIdentifier);

		// Invoke general function to read resource files
		boolean isSuccessful = loadResourceFile(context, applicationIdentifier, applicationVersion, fileName, null, false, organizationalProperties);

		// Save common property: application
		String propertyApplicationIdentifier = organizationalProperties.get(RESOURCE_FILE_APPLCIATION);

		if (propertyApplicationIdentifier != null && propertyApplicationIdentifier.equals(applicationIdentifier))
		{
			this.commonIdentifiersProperties.put(applicationIdentifier + "." + RESOURCE_FILE_APPLCIATION, propertyApplicationIdentifier);
		}

		// Save common property: version
		String propertyVersionIdentifier = organizationalProperties.get(RESOURCE_FILE_VERSION);

		if (propertyVersionIdentifier != null && propertyVersionIdentifier.equals(String.valueOf(applicationVersion)))
		{
			this.commonIdentifiersProperties.put(applicationIdentifier + "." + RESOURCE_FILE_VERSION, propertyVersionIdentifier);
		}

		// Save common property: language
		String propertyLanguageIdentifier = organizationalProperties.get(RESOURCE_FILE_LANGUAGE);

		if (propertyLanguageIdentifier != null)
		{
			this.commonIdentifiersProperties.put(applicationIdentifier + "." + RESOURCE_FILE_LANGUAGE, propertyLanguageIdentifier);
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Get the language setting of an common resource file <TT>Basic</TT>,
	 * <TT>Common</TT>, <TT>Application</TT> or <TT>Extension</TT>.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param applicationName
	 *            The name of the application to search for.
	 * 
	 * @return Returns the language identifier as String or <TT>null</TT>.
	 */
	public String getCommonResourceFileLanguage(Context context, String applicationName)
	{
		// Check parameter
		if (applicationName == null || applicationName.length() == 0) return null;

		// Save common property: application
		return this.commonIdentifiersProperties.get(applicationName + "." + RESOURCE_FILE_LANGUAGE);
	}

	/**
	 * Load resources from a resource file.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param applicationResource
	 *            The application to consider.
	 * 
	 * @param applicationVersion
	 *            Current version of the application, or <TT>null</TT> if the
	 *            version is not to be checked.
	 * 
	 * @param fileName
	 *            The file name to be read.
	 * 
	 * @param language
	 *            Language string to check for in the resource file, or
	 *            <TT>null</TT> if the language is not to be checked.
	 * 
	 * @param resourceItemAlreadyMustExist
	 *            Marks if the resource item already must exist in the resource
	 *            list.
	 * 
	 * 
	 * @param organizationalProperties
	 *            Hash map to be used for all read resource items.
	 * 
	 * @return Returns <TT>true</TT> if the resource file could be read
	 *         successfully, otherwise <TT>false</TT>.
	 */
	public boolean loadResourceFile(Context context, String applicationIdentifier, Integer applicationVersion, String fileName, String language, boolean resourceItemAlreadyMustExist, HashMap<String, String> organizationalProperties)
	{
		/*
		 * Check parameter
		 */
		if (applicationIdentifier == null)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "ErrorOnReadingResourceFile"), "--> Application identifier not set", null);
			return false;
		}

		/*
		 * Variables
		 */

		boolean isError = false;

		/*
		 * Read resources
		 */
		try
		{
			// Read all properties from resource file
			if (readResourceIdentifiersFromFile(context, fileName, organizationalProperties, resourceItemAlreadyMustExist) == false) isError = true;

			// Check application
			String propertyApplicationIdentifier = organizationalProperties.get(RESOURCE_FILE_APPLCIATION);

			if (propertyApplicationIdentifier == null || !propertyApplicationIdentifier.equals(applicationIdentifier))
			{
				String errorString = "--> Application confusion";
				errorString += "\n--> Searched for application '" + applicationIdentifier + "'";
				errorString += "\n--> But resource file item [" + RESOURCE_FILE_APPLCIATION + "] was set to '" + propertyApplicationIdentifier + "'";
				errorString += "\n--> resource file name: '" + fileName + "'";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "ErrorOnReadingResourceFile"), errorString, null);
				isError = true;
			}

			// Check version
			if (applicationVersion != null)
			{
				String propertyApplicationVersion = organizationalProperties.get(RESOURCE_FILE_VERSION);

				if (propertyApplicationVersion == null || !propertyApplicationVersion.equals(String.valueOf(applicationVersion)))
				{
					String errorString = "--> Version confusion";
					errorString += "\n--> On ressource type '" + applicationIdentifier + "'";
					errorString += "\n--> Searched for version '" + String.valueOf(applicationVersion) + "'";
					errorString += "\n--> But resource file item [" + RESOURCE_FILE_VERSION + "] was set to '" + propertyApplicationVersion + "'";
					errorString += "\n--> resource file name: '" + fileName + "'";
					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "ErrorOnReadingResourceFile"), errorString, null);
					isError = true;
				}
			}

			// Check a specific language, if there was handed over one as
			// parameter
			if (language != null)
			{
				String propertyLanguageIdentifier = organizationalProperties.get(RESOURCE_FILE_LANGUAGE);

				if (propertyLanguageIdentifier == null || !propertyLanguageIdentifier.equals(language))
				{
					String errorString = "--> Language confusion";
					errorString += "\n--> Searched for language '" + language + "'";
					errorString += "\n--> But resource file item [" + RESOURCE_FILE_LANGUAGE + "] was set to '" + propertyLanguageIdentifier + "'";
					errorString += "\n--> resource file name: '" + fileName + "'";
					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "ErrorOnReadingResourceFile"), errorString, null);
					isError = true;
				}
			}
			// Check if a language was set generally
			else
			{
				String propertyLanguageIdentifier = organizationalProperties.get(RESOURCE_FILE_LANGUAGE);

				if (propertyLanguageIdentifier == null || propertyLanguageIdentifier.length() == 0)
				{
					String errorString = "--> Language not set";
					errorString += "\n--> Resource file item [" + RESOURCE_FILE_LANGUAGE + "] is not set";
					errorString += "\n--> Resource file name: '" + fileName + "'";
					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "ErrorOnReadingResourceFile"), errorString, null);
					isError = true;
				}
			}
		}
		catch (Exception e)
		{
			String errorString = "--> File name searched for: '" + fileName + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "ErrorOnReadingResourceFile"), errorString, e);
			return false;
		}

		/*
		 * Return
		 */

		if (isError == true)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Read all resource identifiers directly from a resource file.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param fileName
	 *            The file name of the resource file.
	 * 
	 * @param organizationalProperties
	 *            Container for organizational properties.
	 * 
	 * @param resourceItemAlreadyMustExist
	 *            If this parameter is set to <TT>true</TT> the resource item
	 *            already must exist, e. g. to ensure that translated labels are
	 *            defined before by regular resource items.
	 * 
	 * @return Returns <TT>true</TT> if all resource identifiers could be read
	 *         successfully, otherwise <TT>false</TT>.
	 */
	private boolean readResourceIdentifiersFromFile(Context context, String fileName, HashMap<String, String> organizationalProperties, boolean resourceItemAlreadyMustExist)
	{
		/*
		 * Check parameter
		 */

		if (fileName == null) return false;
		if (fileName.length() == 0) return false;

		/*
		 * Variables
		 */

		boolean isError = false;
		String line = null;
		String value = null;
		String identifier = null;
		String attribute = null;
		String lastValidIdentifier = null;
		int lineNumber = 0;

		/*
		 * Read all lines
		 */

		try
		{
			// Open file
			FileReader fileReader = new FileReader(fileName);
			BufferedReader resourceFile = new BufferedReader(fileReader);

			while ((line = resourceFile.readLine()) != null)
			{
				// Line number
				lineNumber++;

				// Check for EMPTY line
				if (line.trim().length() == 0) continue;

				// Check for comment in line
				if (line.startsWith("#")) continue;

				// Insert the last valid identifier, if the short form of the
				// attribute setting was found.
				String rawLine = line;

				if (line.startsWith(ResourceManager.getAttributeDelimiterString()) && lastValidIdentifier != null)
				{
					line = lastValidIdentifier + line;
				}

				// Separate identifier from value (separated by equal sign
				// character "=" as delimiter)
				String[] equalSignParts = line.split("=", 2);
				if (equalSignParts == null) continue;
				if (equalSignParts.length == 0) continue;
				if (equalSignParts[0] == null) continue;

				// Get value
				if (equalSignParts.length > 1 && equalSignParts[1] != null)
				{
					value = equalSignParts[1].trim();
				}
				else
				{
					value = null;
				}

				// Get preliminary identifier
				identifier = equalSignParts[0];

				// Check if it is a organizational identifier that is used in
				// all resource files
				if (identifier.startsWith("ResourceFile."))
				{
					organizationalProperties.put(identifier, value);
					continue;
				}

				// Check if the resource identifier, including the attribute,
				// was already read, that means if it has a duplicate.
				//
				// If there is an attribute set and the last part of the
				// attribute is set to ".$" the attribute is not to check.
				if (!(identifier.contains(ResourceManager.getAttributeDelimiterString()) && identifier.endsWith(ResourceManager.getAttributeSkipString())))
				{
					if (this.ckeckDuplicateIdentifier(context, fileName, identifier, rawLine, lineNumber) == true) isError = true;
				}

				// Separate identifier from attribute (separated by pipe
				// character "|" as delimiter)
				String[] pipeParts = equalSignParts[0].split("\\|", 2);
				if (pipeParts == null) continue;
				if (pipeParts.length == 0) continue;
				if (pipeParts[0] == null) continue;

				// Get identifier
				identifier = pipeParts[0].trim();
				if (identifier == null) continue;
				if (identifier.length() == 0) continue;

				// Get attribute
				if (pipeParts.length > 1 && pipeParts[1] != null)
				{
					attribute = pipeParts[1].trim().replace(ResourceManager.getAttributeDelimiterString(), "");
				}
				else
				{
					attribute = null;
				}

				// Create a resource (Group or Name) if there are 5 or 6 comma
				// separated parts
				ResourceContainer resourceContainer = null;

				String[] identifierParts = identifier.split("\\.");

				// Create a regular resource (Group or Name)
				if (identifierParts.length == 5 || identifierParts.length == 6)
				{
					if (this.resources.containsKey(identifier))
					{
						resourceContainer = this.resources.get(identifier);
					}
					else
					{
						if (identifierParts.length == 5) resourceContainer = new ResourceContainer(context, identifierParts[0], identifierParts[1], identifierParts[2], identifierParts[3], identifierParts[4]);
						if (identifierParts.length == 6) resourceContainer = new ResourceContainer(context, identifierParts[0], identifierParts[1], identifierParts[2], identifierParts[3], identifierParts[4], identifierParts[5]);

						// Handle Alias name
						if (value != null && value.length() > 0)
						{
							// Assign Alias name to the resource
							resourceContainer.setAliasName(value);

							// Check if Alias name was used before for the same
							// Type/Usage
							if (this.ckeckDuplicateAliasName(context, value, fileName, resourceContainer, rawLine, lineNumber) == true) isError = true;
						}
					}
				}
				else
				{
					this.ckeckParsingErrorNotification(context, fileName, null, "No valid resource identifier", rawLine, lineNumber);
					isError = true;
					continue;
				}

				if (resourceContainer == null) continue;

				// Check if the "Application" coded in the resource identifier
				// is defined
				if (attribute == null)
				{
					if (this.ckeckApplicationOfIdentifier(context, fileName, resourceContainer, rawLine, lineNumber) == false) isError = true;
				}

				// Check if there is a duplicate on Type/Group/Name
				if (attribute == null)
				{
					if (this.ckeckDuplicateTypeGroupName(context, fileName, resourceContainer, rawLine, lineNumber) == true) isError = true;
				}

				// Check if the Type/Group/Name is already set before
				if (resourceItemAlreadyMustExist == true && attribute == null)
				{
					if (this.ckeckTypeGroupNameIsAlreadySet(context, fileName, resourceContainer, rawLine, lineNumber) == false) isError = true;
				}

				// Add an attribute to the resource container
				if (attribute != null)
				{
					// If the last part of the attribute is set to ".$" the
					// attribute is not to read in to the resource repository.
					if (!attribute.endsWith(ResourceManager.getAttributeSkipString()))
					{
						resourceContainer.addAttribute(attribute, value);
					}
				}

				// Save resource container
				this.resources.put(identifier, resourceContainer);

				// Save last valid identifier
				lastValidIdentifier = identifier;
			}

			// Close resource file
			resourceFile.close();
			fileReader.close();
		}
		catch (Exception e)
		{
			String errorString = "--> File name searched for: '" + fileName + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "ErrorOnReadingResourceFile"), errorString, e);
			isError = true;
		}

		/*
		 * Return
		 */

		if (isError == true)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Notifies an error message regarding a resource file parsing error.
	 * <p>
	 * Please note: This method only creates an error message. The parsing error
	 * itself is already found by the calling function.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param fileName
	 *            The file name of the actual resource file.
	 * 
	 * @param identifier
	 *            The resource identifier to check.
	 * 
	 * @param message
	 *            The message that names the detail error occurred.
	 * 
	 * @param line
	 *            The text of the actual line of the resource file.
	 * 
	 * @param lineNumber
	 *            The line number of the actual line of the resource file.
	 */
	private void ckeckParsingErrorNotification(Context context, String fileName, String identifier, String message, String line, int lineNumber)
	{
		String errorString = "Unknown parsing error";
		if (message != null) errorString = "--> " + message;
		if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
		errorString += "\n--> Line number: '" + String.valueOf(lineNumber) + "'";
		if (line != null) errorString += "\n--> Line text: '" + line + "'";
		context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "ErrorOnParsingResourceFile"), errorString, null);
	}

	/**
	 * Check on integrity errors.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean validateResources(Context context, ManagerInterface application)
	{
		// Variables
		boolean isIntegrityError = false;

		// Check for integrity errors (Named resource identifiers)
		try
		{
			if (this.validateResources(context) == true) isIntegrityError = true;
			if (context.validateResources(context) == true) isIntegrityError = true;
			if (context.getCommandManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getNotificationManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getConfigurationManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getLabelManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getLocaldataManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getRightManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getLicenseManager().validateResources(context) == true) isIntegrityError = true;
			if (context.getMediaManager().validateResources(context) == true) isIntegrityError = true;
			if (application.validateResources(context) == true) isIntegrityError = true;
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isIntegrityError;
	}

	/**
	 * Read configuration items of all interfaces.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean readConfiguration(Context context, ManagerInterface application)
	{
		// Variables
		boolean isError = false;

		// Read configuration items of all interfaces
		try
		{
			if (this.validateResources(context) == true) isError = true;
			if (context.readConfiguration(context) == true) isError = true;
			if (context.getCommandManager().readConfiguration(context) == true) isError = true;
			if (context.getNotificationManager().readConfiguration(context) == true) isError = true;
			if (context.getConfigurationManager().readConfiguration(context) == true) isError = true;
			if (context.getLabelManager().readConfiguration(context) == true) isError = true;
			if (context.getLocaldataManager().readConfiguration(context) == true) isError = true;
			if (context.getRightManager().readConfiguration(context) == true) isError = true;
			if (context.getLicenseManager().readConfiguration(context) == true) isError = true;
			if (context.getMediaManager().readConfiguration(context) == true) isError = true;
			if (application.readConfiguration(context) == true) isError = true;
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isError;
	}

	/**
	 * Getter
	 */
	public HashMap<String, ResourceContainer> getResources()
	{
		return this.resources;
	}

	/**
	 * Getter
	 */
	public HashMap<String, String> getReadResourceIdentifiersList()
	{
		return this.readResourceIdentifiersList;
	}

	/**
	 * Get the delimiter character that divides the main resource identifier
	 * from the resource attributes.
	 * 
	 * @return Returns always with <TT>|</TT>.
	 */
	public static String getAttributeDelimiterString()
	{
		return "|";
	}

	/**
	 * Get the delimiter character that skips the reading of a resource
	 * attribute.
	 * 
	 * @return Returns always with <TT>.$</TT>.
	 */
	public static String getAttributeSkipString()
	{
		return ".$";
	}

	/**
	 * Get a common attribute list of a resource file as a property string.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param application
	 *            The application name to consider as string.
	 * 
	 * @param version
	 *            The application version number to consider.
	 * 
	 * @param language
	 *            The language to consider.
	 * 
	 * @return Returns the properties list of all parameters that are handed
	 *         over and valid, or an <TT>empty</TT> string.
	 */
	public static String getCommonAttributeList(Context context, String application, int version, String language, String licenseKey)
	{
		// Initialize
		String commonAttributeList = "";

		// Application
		if (application != null && application.length() > 0)
		{
			commonAttributeList += RESOURCE_FILE_APPLCIATION + "=" + application.trim() + "\n";
		}

		// Version
		if (version > 0)
		{
			commonAttributeList += RESOURCE_FILE_VERSION + "=" + String.valueOf(version) + "\n";
		}

		// Language
		if (language != null && language.length() > 0)
		{
			commonAttributeList += RESOURCE_FILE_LANGUAGE + "=" + language.trim() + "\n";
		}

		// License Key
		if (licenseKey != null && licenseKey.length() > 0)
		{
			commonAttributeList += RESOURCE_FILE_LICENSEKEY + "=" + licenseKey.trim() + "\n";
		}

		// Return
		return commonAttributeList;
	}
}
