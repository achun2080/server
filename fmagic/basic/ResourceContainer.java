package fmagic.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class contains all attributes and functions needed to describe the
 * values of a resource item.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 28.12.2012 - Created
 * 
 */
public class ResourceContainer implements Cloneable
{
	// Identifier settings
	private String type = null;
	private String application = null;
	private String origin = null;
	private String usage = null;
	private String group = null;
	private String name = null;

	private String resourceIdentifier = null;
	private String resourceGroupIdentifier = null;
	private String resourceAliasName = null;

	// Attributes
	final private HashMap<String, String> attributes = new HashMap<String, String>();

	/**
	 * Copy all values and attributes from the resource object handed over as
	 * parameter to the current resource object.
	 * 
	 * @param resourceContainer
	 *            The resource container to copy.
	 */
	protected void copy(ResourceContainer resourceContainer)
	{
		try
		{
			this.type = resourceContainer.type;
			this.application = resourceContainer.application;
			this.origin = resourceContainer.origin;
			this.usage = resourceContainer.usage;
			this.group = resourceContainer.group;
			this.name = resourceContainer.name;

			this.resourceIdentifier = resourceContainer.resourceIdentifier;
			this.resourceGroupIdentifier = resourceContainer.resourceGroupIdentifier;
			this.resourceAliasName = resourceContainer.resourceAliasName;

			this.attributes.clear();
			for (String attributeName : resourceContainer.attributes.keySet())
			{
				this.attributes.put(attributeName, resourceContainer.attributes.get(attributeName));
			}
		}
		catch (Exception e)
		{
			// Be silent
		}
	}

	// resource for type
	public static enum TypeEnum
	{
		Configuration,
		Notification,
		LocalData,
		Command,
		Container,
		Context,
		Resource,
		Label,
		Right,
		License,
		Media
	}

	// resource for origin
	public static enum OriginEnum
	{
		Client, Server, All
	}

	// resource for usage
	public static enum UsageEnum
	{
		Identifier, Error, Event, Property
	}

	// Constants for attributes
	// final private static String CREATED_BY = "CreatedBy";
	// final private static String CREATED_DATE = "CreatedDate";
	// final private static String CREATED_VERSION = "CreatedVersion";
	// final private static String MODIFIED_BY = "ModifiedBy";
	// final private static String MODIFIED_DATE = "ModifiedDate";
	// final private static String MODIFIED_VERSION = "ModifiedVersion";
	// final private static String MANUAL = "Manual";
	// final private static String USAGE = "Usage";
	// final private static String HEADER = "Header";

	/**
	 * Constructor 1: Construction of a resource container via Type,
	 * Application, Origin, Usage, Group and Name.
	 * <p>
	 * You might qualify the resource by adding attributes to the container
	 * using the function <TT>addDataItem()</TT>.
	 * <p>
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param type
	 *            The type of the resource as String.
	 * 
	 * @param application
	 *            The application of the resource as String.
	 * 
	 * @param origin
	 *            The origin of the resource as String.
	 * 
	 * @param usage
	 *            The usage of the resource as String.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 */
	ResourceContainer(Context context, String type, String application,
			String origin, String usage, String group, String name)
	{
		// Copy parameter
		this.type = type.trim();
		this.application = application.trim();
		this.origin = origin.trim();
		this.usage = usage.trim();
		this.group = group.trim();
		this.name = name.trim();

		// Check parameter
		boolean error = false;
		if (type == null || type.length() == 0) error = true;
		if (application == null || application.length() == 0) error = true;
		if (origin == null || origin.length() == 0) error = true;
		if (usage == null || usage.length() == 0) error = true;
		if (group == null || group.length() == 0) error = true;
		if (name == null || name.length() == 0) error = true;

		if (error == true)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "MissingParameter"), "--> Type [" + type + "], " + "Application [" + application + "], " + "Origin [" + origin + "], " + "Usage [" + usage + "], " + "Group [" + group + "], " + "Name [" + name + "]", null);
			return;
		}

		// Set resource identifier
		this.resourceIdentifier = ResourceContainer.composeResourceIdentifierString(this.type, this.application, this.origin, this.usage, this.group, this.name);
	}

	/**
	 * Constructor 2: Construction of a resource container via Type,
	 * Application, Origin Usage and Group, without Name. This constructor is
	 * used for Group documentation.
	 * <p>
	 * You might qualify the resource by adding attributes to the container
	 * using the function <TT>addDataItem()</TT>.
	 * <p>
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param type
	 *            The type of the resource as String.
	 * 
	 * @param application
	 *            The application of the resource as String.
	 * 
	 * @param origin
	 *            The origin of the resource as String.
	 * 
	 * @param usage
	 *            The usage of the resource as String.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 */
	ResourceContainer(Context context, String type, String application,
			String origin, String usage, String group)
	{
		// Copy parameter
		this.type = type.trim();
		this.application = application.trim();
		this.origin = origin.trim();
		this.usage = usage.trim();
		this.group = group.trim();

		// Check parameter
		boolean error = false;
		if (type == null || type.length() == 0) error = true;
		if (application == null || application.length() == 0) error = true;
		if (origin == null || origin.length() == 0) error = true;
		if (usage == null || usage.length() == 0) error = true;
		if (group == null || group.length() == 0) error = true;

		if (error == true)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "MissingParameter"), "--> Type [" + type + "], " + "Application [" + application + "], " + "Origin [" + origin + "], " + "Usage [" + usage + "], " + "Group [" + group + "]", null);
			return;
		}

		// Set resource identifier
		this.resourceIdentifier = ResourceContainer.composeGroupIdentifierString(this.type, this.application, this.origin, this.usage, this.group);
	}

	/**
	 * Constructor 3: Construction of a provisional resource container via
	 * identifier.
	 * 
	 * @param identifier
	 *            The identifier of the resource.
	 */
	public ResourceContainer(String identifier)
	{
		// Check parameter
		if (identifier == null) return;
		if (identifier.length() == 0) return;

		// Split identifier
		String[] parts = identifier.split("\\.");
		if (parts.length < 5 || parts.length > 6) return;

		// Copy parameter
		this.type = parts[0];
		this.application = parts[1];
		this.origin = parts[2];
		this.usage = parts[3];
		this.group = parts[4];
		if (parts.length == 6) this.name = parts[5];

		// Set resource identifier
		this.resourceIdentifier = ResourceContainer.composeResourceIdentifierString(this.type, this.application, this.origin, this.usage, this.group, this.name);
	}

	/**
	 * Compose identifier string of a resource.
	 * 
	 * @param type
	 *            The type of the resource as String.
	 * 
	 * @param application
	 *            The application of the resource as String.
	 * 
	 * @param origin
	 *            The origin of the resource as String.
	 * 
	 * @param usage
	 *            The usage of the resource as String.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @param name
	 *            The name of the resource as String.
	 * 
	 * @return Returns the composed identifier or <TT>null</TT> if one of the
	 *         parameters was not set.
	 */
	public static String composeResourceIdentifierString(String type, String application, String origin, String usage, String group, String name)
	{
		// Check parameter
		if (type == null || type.length() == 0) return null;
		if (application == null || application.length() == 0) return null;
		if (origin == null || origin.length() == 0) return null;
		if (usage == null || usage.length() == 0) return null;
		if (group == null || group.length() == 0) return null;
		if (name == null || name.length() == 0) return null;

		// Set resource identifier
		String identifier;
		identifier = type + "." + application + "." + origin + "." + usage + "." + group + "." + name;

		// Return
		return identifier;
	}

	/**
	 * Compose a Type/Group/Name identifier string of a resource.
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
	 * @return Returns the composed identifier Type/Group/Name or <TT>null</TT>
	 *         if one of the parameters was not set.
	 */
	public static String composeTypeGroupNameIdentifierString(String type, String group, String name)
	{
		// Check parameter
		if (type == null || type.length() == 0) return null;
		if (group == null || group.length() == 0) return null;
		if (name == null || name.length() == 0) return null;

		// Set resource identifier
		String identifier;
		identifier = type + ".*.*.*." + group + "." + name;

		// Return
		return identifier;
	}

	/**
	 * Compose identifier string of a group of resource.
	 * 
	 * @param type
	 *            The type of the resource as String.
	 * 
	 * @param application
	 *            The application of the resource as String.
	 * 
	 * @param origin
	 *            The origin of the resource as String.
	 * 
	 * @param usage
	 *            The usage of the resource as String.
	 * 
	 * @param group
	 *            The group of the resource as String.
	 * 
	 * @return Returns the composed identifier or <TT>null</TT> if one of the
	 *         parameters was not set.
	 */
	public static String composeGroupIdentifierString(String type, String application, String origin, String usage, String group)
	{
		// Check parameter
		if (type == null || type.length() == 0) return null;
		if (application == null || application.length() == 0) return null;
		if (origin == null || origin.length() == 0) return null;
		if (usage == null || usage.length() == 0) return null;
		if (group == null || group.length() == 0) return null;

		// Set resource identifier
		String identifier;
		identifier = type + "." + application + "." + origin + "." + usage + "." + group;

		// Return
		return identifier;
	}

	/**
	 * Add an attribute to the list of attributes of the resource container.
	 * 
	 * @param identifier
	 *            Identifier of the attributes item.
	 * 
	 * @param value
	 *            Value of the attributes item.
	 * 
	 */
	public void addAttribute(String identifier, String value)
	{
		this.attributes.put(identifier, value);
	}

	/**
	 * Get an attribute from the list of attributes of a resource container.
	 * 
	 * @param identifier
	 *            Identifier of the attribute item.
	 * 
	 * @return Returns the value of the attribute, or <TT>null</TT> if no value
	 *         was found.
	 */
	public String getAttribute(String identifier)
	{
		return this.attributes.get(identifier);
	}

	/**
	 * Getter
	 */
	public String getType()
	{
		return type;
	}

	public String getApplication()
	{
		return application;
	}

	/**
	 * Getter
	 */
	public String getOrigin()
	{
		return origin;
	}

	/**
	 * Getter
	 */
	public String getUsage()
	{
		return usage;
	}

	/**
	 * Getter
	 */
	public String getGroup()
	{
		return group;
	}

	/**
	 * Getter
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Getter
	 */
	public String getRecourceIdentifier()
	{
		return this.resourceIdentifier;
	}

	/**
	 * Get Alias name of the resource.
	 * <p>
	 * Please note: This method always returns a value (at least the
	 * <TT>Name</TT> part of the resource identifier as a fall back value). If
	 * you want to check, if there is a real value set to the <TT>Alias</TT>
	 * name, please use the method <TT>checkAliasName()</TT>.
	 * 
	 * @return Returns the <TT>Alias</TT> name, if set, or the <TT>Name</TT> of
	 *         the resource identifier (as fall back setting).
	 */
	public String getAliasName()
	{
		if (this.resourceAliasName == null || this.resourceAliasName.length() == 0)
		{
			return this.name;
		}
		else
		{
			return this.resourceAliasName;
		}
	}

	/**
	 * Setter
	 */
	public void setAliasName(String resourceAliasName)
	{
		this.resourceAliasName = resourceAliasName;
	}

	/**
	 * Check if <TT>Alias</TT> name is really set.
	 * <p>
	 * Please use this method if you want to know if there is an <TT>Alias</TT>
	 * name set, because the Getter of the <TT>Alias</TT> name always returns a
	 * value (at least the <TT>Name</TT> part of the resource identifier as a
	 * fall back value).
	 * 
	 * @return Returns <TT>true</TT> if the <TT>Alias</TT> name is set,
	 *         otherwise <TT>false</TT>.
	 */
	public boolean checkAliasName()
	{
		if (this.resourceAliasName == null || this.resourceAliasName.length() == 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Getter
	 */
	public String getRecourceGroupIdentifier()
	{
		return this.resourceGroupIdentifier;
	}

	/**
	 * To string
	 */
	@Override
	public String toString()
	{
		String outputString = "";

		// Headline
		outputString += "\n[\n+++ Resource Container";
		outputString += "\n" + "----------";

		// Common values
		if (this.resourceAliasName == null)
		{
			// Group identifier or full identifier
			if (this.getName() == null)
			{
				outputString += "\n" + this.getType() + ": " + this.getGroup() + " (Group)";
			}
			else
			{
				outputString += "\n" + this.getType() + ": " + this.getGroup() + "." + this.getName();
			}
		}
		else
		{
			outputString += "\n" + this.getType() + ": " + this.getAliasName() + " (Alias)";
		}

		outputString += "\n" + "Used: in " + this.getApplication() + " (" + this.getOrigin() + ")" + " as " + this.getUsage();
		outputString += "\n" + "Full path: " + this.getRecourceIdentifier();

		// End of output string
		outputString += "\n]\n";

		// Return
		return outputString;
	}

	/**
	 * Print a Manual containing all information of a resource.
	 * 
	 * @return Returns the printed text as String.
	 */
	public String printManual(Context context)
	{
		String outputString = "";
		String tempString = "";

		HashMap<String, String> printedAttributes = new HashMap<String, String>();

		try
		{
			// Common values
			if (this.checkAliasName() == false)
			{
				// Group identifier or full identifier
				if (this.getName() == null)
				{
					outputString += "\n" + this.getType() + ": " + this.getGroup() + " (Group)";
				}
				else
				{
					outputString += "\n" + this.getType() + ": " + this.getGroup() + "." + this.getName();
				}
			}
			else
			{
				outputString += "\n" + this.getType() + ": " + this.getAliasName() + " (Alias)";
			}

			outputString += "\n" + "Used: in " + this.getApplication() + " (" + this.getOrigin() + ")" + " as " + this.getUsage();
			outputString += "\n" + "Full path: " + this.getRecourceIdentifier();

			// Created
			String createdBy = this.getAttributeMainValueByGroupName(context, "Documentation", "CreatedBy");
			String createdDate = this.getAttributeMainValueByGroupName(context, "Documentation", "CreatedDate");
			String createdVersion = this.getAttributeMainValueByGroupName(context, "Documentation", "CreatedVersion");
			tempString = "";

			if (createdBy != null)
			{
				tempString += " by " + createdBy;
				printedAttributes.put("CreatedBy", createdBy);
			}

			if (createdDate != null)
			{
				tempString += " at " + createdDate;
				printedAttributes.put("CreatedDate", createdDate);
			}

			if (createdVersion != null)
			{
				tempString += " (Version " + createdVersion + ")";
				printedAttributes.put("CreatedVersion", createdVersion);
			}

			if (tempString.length() > 0) outputString += "\n" + "Created:" + tempString;

			// Modified
			String modifiedBy = this.getAttributeMainValueByGroupName(context, "Documentation", "ModifiedBy");
			String modifiedDate = this.getAttributeMainValueByGroupName(context, "Documentation", "ModifiedDate");
			String modifiedVersion = this.getAttributeMainValueByGroupName(context, "Documentation", "ModifiedVersion");
			tempString = "";

			if (modifiedBy != null)
			{
				tempString += " by " + modifiedBy;
				printedAttributes.put("ModifiedBy", modifiedBy);
			}

			if (modifiedDate != null)
			{
				tempString += " at " + modifiedDate;
				printedAttributes.put("ModifiedDate", modifiedDate);
			}

			if (modifiedVersion != null)
			{
				tempString += " (Version " + modifiedVersion + ")";
				printedAttributes.put("ModifiedVersion", modifiedVersion);
			}

			if (tempString.length() > 0) outputString += "\n" + "Modified:" + tempString;

			// Manual description (regarding the resource itself)
			String manualHeaderAttributeName = ResourceManager.attribute(context, "Documentation", "ManualHeader").getAliasName();
			String manualHeader = this.getAttributeMainValueByGroupName(context, "Documentation", "ManualHeader");
			String manualParagraphItem = ResourceManager.attribute(context, "Documentation", "ManualParagraph").getAliasName();
			String manualItem = manualParagraphItem.replace("${paragraph}", "1");

			if (manualHeader != null || manualItem != null)
			{
				if (manualHeader != null && manualHeader.length() > 0)
				{
					outputString += "\n\n:::::::::: " + manualHeader;
					printedAttributes.put(manualHeaderAttributeName, manualHeader);
				}

				// List all manual items
				for (int i = 1; i < 1000; i++)
				{
					String manualParagraphAttribute = manualParagraphItem.replace("${paragraph}", String.valueOf(i));
					manualItem = this.getAttribute(manualParagraphAttribute);
					if (manualItem == null) break;

					outputString += "\n" + cutLines(manualItem, 60, "");
					printedAttributes.put(manualParagraphAttribute, manualItem);
				}
			}

			// Usage description (regarding the resource itself)
			String usageHeaderAttributeName = ResourceManager.attribute(context, "Documentation", "UsageHeader").getAliasName();
			String usageHeader = this.getAttributeMainValueByGroupName(context, "Documentation", "UsageHeader");
			String usageParagraphItem = ResourceManager.attribute(context, "Documentation", "UsageParagraph").getAliasName();
			String usageItem = usageParagraphItem.replace("${paragraph}", "1");

			if (usageHeader != null || usageItem != null)
			{
				if (usageHeader != null && usageHeader.length() > 0)
				{
					outputString += "\n\n:::::::::: " + usageHeader;
					printedAttributes.put(usageHeaderAttributeName, usageHeader);
				}

				// List all usage items
				for (int i = 1; i < 1000; i++)
				{
					String usageParagraphAttribute = usageParagraphItem.replace("${paragraph}", String.valueOf(i));
					usageItem = this.getAttribute(usageParagraphAttribute);
					if (usageItem == null) break;

					outputString += "\n" + cutLines(usageItem, 60, "");
					printedAttributes.put(usageParagraphAttribute, usageItem);
				}
			}

			// Value settings
			boolean valueHeadlinePrinted = false;

			for (int number = 0; number < 100; number++)
			{
				String value = this.getAttributeValue(context, number, null);
				String valueAttributeName = this.getAttributeResourceIdentifier(context, number, null);

				if (value == null) continue;

				if (valueHeadlinePrinted == false)
				{
					outputString += "\n\n:::::::::: " + "Value" + "\n";
					valueHeadlinePrinted = true;
				}

				if (number == 0)
				{
					outputString += cutLines(value, 60, "");
				}
				else
				{
					outputString += cutLines(String.valueOf(number) + ": " + value, 60, "");
				}

				printedAttributes.put(valueAttributeName, value);
			}

			// Print all attributes that were not printed yet
			boolean otherAttributesPrinted = false;

			List<String> sortedListManual = new ArrayList<String>();
			sortedListManual.addAll(this.attributes.keySet());
			Collections.sort(sortedListManual);

			// List all items
			Iterator<String> iterManual = sortedListManual.iterator();

			while (iterManual.hasNext())
			{
				String attributeName = iterManual.next();
				if (printedAttributes.containsKey(attributeName)) continue;

				String value = this.getAttribute(attributeName);
				if (value == null) continue;

				if (otherAttributesPrinted == false)
				{
					outputString += "\n\n:::::::::: " + "Settings" + "\n";
					otherAttributesPrinted = true;
				}

				outputString += cutLines(attributeName + ": " + value, 60, "");
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return FileUtilFunctions.normalizeNewLine(outputString);
	}

	/**
	 * Print a Template containing all information of a resource.
	 * 
	 * @return Returns the printed text as String.
	 */
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String outputString = "";

		try
		{
			// Group identifier or full identifier
			if (this.getName() == null)
			{
				outputString += "\n\n#\n# Group: " + this.getRecourceIdentifier() + "\n#";
			}
			else
			{
				outputString += "\n#\n# " + this.getType() + " " + this.getUsage() + ": " + this.getGroup() + "." + this.getName() + "\n#";
			}

			// Alias name
			if (this.checkAliasName() == true)
			{
				outputString += "\n# Alias: " + this.getAliasName() + "\n#";
			}

			// Manual description (regarding the resource itself)
			String manualHeader = this.getAttributeMainValueByGroupName(context, "Documentation", "ManualHeader");
			String manualParagraphItem = ResourceManager.attribute(context, "Documentation", "ManualParagraph").getAliasName();
			String manualItem = manualParagraphItem.replace("${paragraph}", "1");

			if (manualHeader != null || manualItem != null)
			{
				outputString += "\n# --------------------";

				if (manualHeader != null && manualHeader.length() > 0) outputString += "\n# " + manualHeader + "\n# --------------------\n#";

				// List all manual items
				for (int i = 1; i < 1000; i++)
				{
					manualItem = this.getAttribute(manualParagraphItem.replace("${paragraph}", String.valueOf(i)));
					if (manualItem == null) break;
					outputString += cutLines(manualItem, 60, "# ") + "\n#";
				}
			}

			// Usage description (regarding the resource itself)
			String usageHeader = this.getAttributeMainValueByGroupName(context, "Documentation", "UsageHeader");
			String usageParagraphItem = ResourceManager.attribute(context, "Documentation", "UsageParagraph").getAliasName();
			String usageItem = usageParagraphItem.replace("${paragraph}", "1");

			if (usageHeader != null || usageItem != null)
			{
				outputString += "\n# --------------------";

				if (usageHeader != null && usageHeader.length() > 0) outputString += "\n# " + usageHeader + "\n# --------------------\n#";

				// List all usage items
				for (int i = 1; i < 1000; i++)
				{
					usageItem = this.getAttribute(usageParagraphItem.replace("${paragraph}", String.valueOf(i)));
					if (usageItem == null) break;
					outputString += cutLines(usageItem, 60, "# ") + "\n#";
				}
			}

			// Identifier
			outputString += "\n" + "#";

			// Group identifier or full identifier
			if (this.getName() == null || includingResourceIdentifiers == false)
			{
				outputString += "\n";

			}
			else
			{
				outputString += "\n\n" + this.getRecourceIdentifier() + "=" + "\n\n\n";
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return FileUtilFunctions.normalizeNewLine(outputString);
	}

	/**
	 * Cut a long text line into several shorter lines separated by NEWLINE,
	 * beginning with a prefix string, like '# '.
	 * <p>
	 * Replaces the placeholder "${NewLine}" to its native value.
	 * 
	 * @param inputString
	 *            The text line to cut.
	 * 
	 * @param lineLength
	 *            The maximum length of cut lines.
	 * 
	 * @param prefixString
	 *            A string to set as prefix before each line.
	 * 
	 * @return Returns the cut line.
	 */
	public String cutLines(String inputString, int lineLength, String prefixString)
	{
		// Check parameters
		if (inputString == null) return "";
		if (inputString.length() == 0) return "";
		if (prefixString == null) prefixString = "";

		// Cut lines
		StringBuffer outputStringBuffer = new StringBuffer("");

		try
		{
			// Split the text
			inputString = inputString.replace("${NewLine}", "\n" + prefixString);
			int inputLength = inputString.length();
			StringBuffer inputStringBuffer = new StringBuffer(inputString);
			int partLength = 0;

			for (int index = 0; index < inputLength; index++)
			{
				if (index == 0)
				{
					outputStringBuffer.append("\n" + prefixString);
				}

				if (partLength > lineLength && inputStringBuffer.charAt(index) == ' ')
				{
					outputStringBuffer.append("\n" + prefixString);
					partLength = 0;
					continue;
				}

				outputStringBuffer.append(inputStringBuffer.charAt(index));
				partLength++;
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return outputStringBuffer.toString();
	}

	/**
	 * Get the attribute resource identifier of the "Value" attribute.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param number
	 *            The number of the value to search for.
	 * 
	 * @param attributeContext
	 *            The context to search for or <TT>null</TT> if no context is to
	 *            be considered.
	 * 
	 * @return Returns the attribute identifier of the attribute "Value", or
	 *         <TT>null</TT> if no value was found.
	 */
	public String getAttributeResourceIdentifier(Context context, int number, String attributeContext)
	{
		if (number < 0) return null;

		String attributeName = null;

		if (number == 0)
		{
			if (attributeContext == null || attributeContext.length() == 0)
			{
				ResourceContainer attributeResource = ResourceManager.attribute(context, "Common", "Value");
				if (attributeResource == null) return null;

				attributeName = attributeResource.getAliasName();
				if (attributeName == null || attributeName.length() == 0) return null;
			}
			else
			{
				ResourceContainer attributeResource = ResourceManager.attribute(context, "Common", "ValueContext");
				if (attributeResource == null) return null;

				attributeName = attributeResource.getAliasName();
				if (attributeName == null || attributeName.length() == 0) return null;

				attributeName = attributeName.replace("${context}", attributeContext);
			}
		}
		else
		{
			if (attributeContext == null || attributeContext.length() == 0)
			{
				ResourceContainer attributeResource = ResourceManager.attribute(context, "Common", "ValueNumber");
				if (attributeResource == null) return null;

				attributeName = attributeResource.getAliasName();
				if (attributeName == null || attributeName.length() == 0) return null;

				attributeName = attributeName.replace("${number}", String.valueOf(number).trim());
			}
			else
			{
				ResourceContainer attributeResource = ResourceManager.attribute(context, "Common", "ValueNumberContext");
				if (attributeResource == null) return null;

				attributeName = attributeResource.getAliasName();
				if (attributeName == null || attributeName.length() == 0) return null;

				attributeName = attributeName.replace("${number}", String.valueOf(number).trim());
				attributeName = attributeName.replace("${context}", attributeContext);
			}
		}

		return attributeName;
	}

	/**
	 * Get the value, of the specific resource attribute "Value".
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param number
	 *            The number of the value to search for. The number "Value.0" is
	 *            equivalent to the "Value" attribute without any number.
	 * 
	 * @param attributeContext
	 *            The context string to search for or <TT>null</TT> if there is
	 *            no context string to consider.
	 * 
	 * @return Returns the value of the attribute, or <TT>null</TT> if no value
	 *         was found.
	 */
	public String getAttributeValue(Context context, int number, String attributeContext)
	{
		if (number < 0) return null;

		String attributeName = this.getAttributeResourceIdentifier(context, number, attributeContext);
		if (attributeName == null || attributeName.length() == 0) return null;

		String attributeValue = this.attributes.get(attributeName);
		if (attributeValue == null || attributeValue.length() == 0) return null;

		return attributeValue;
	}

	/**
	 * Get all values "Value 0...x" of a resource item.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param attributeContext
	 *            The context string to search for or <TT>null</TT> if there is
	 *            no context string to consider.
	 * 
	 * @return Returns a <TT>List</TT> of <TT>Strings</TT> that can be empty.
	 */
	public List<String> getValueList(Context context, String attributeContext)
	{
		List<String> values = new ArrayList<String>();

		for (int i = 0; i < 100; i++)
		{
			String value = this.getAttributeValue(context, i, attributeContext);
			if (value == null) continue;
			if (value.length() == 0) continue;
			values.add(value);
		}

		return values;
	}

	/**
	 * Get the value of an attribute of a resource container, by <TT>Group</TT>
	 * and <TT>Name</TT> of the attribute.
	 * <p>
	 * Please note: This method is overloaded by several signatures.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param group
	 *            The <TT>Group</TT> the attribute belongs to.
	 * 
	 * @param name
	 *            The <TT>Name</TT> the attribute is assigned to.
	 * 
	 * @return Returns the value of the attribute, or <TT>null</TT> if no value
	 *         was found.
	 */
	private String getAttributeMainValueByGroupName(Context context, String group, String name)
	{
		ResourceContainer attributeResource = ResourceManager.attribute(context, group, name);
		if (attributeResource == null) return null;

		String attributeName = attributeResource.getAliasName();
		if (attributeName == null || attributeName.length() == 0) return null;

		String attributeValue = this.attributes.get(attributeName);
		if (attributeValue == null || attributeValue.length() == 0) return null;

		return attributeValue;
	}
}
