package fmagic.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class implements the management of rights used by servers and clients.
 * <p>
 * The right management deals with two main issues. There can be set
 * <TT>Right Items</0000000TT>
 * relating to domain functions, sub functions or data, applying flexible
 * hierarchies. And, these <TT>Right Items</TT> are assigned to one or more
 * <TT>Right Groups</TT>. Each user has to become a member of just one
 * <TT>Right Group</TT> that determines his rights in detail. At runtime the
 * system checks if the logged user actually is allowed to use the specific
 * functionality or data.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 10.03.2013 - Created
 * 
 */
public class RightManager implements ResourceInterface
{
	final private HashMap<String, Set<String>> assignedRightItemToRightGroup = new HashMap<String, Set<String>>();

	/**
	 * Constructor
	 */
	public RightManager()
	{
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = { "Right" };
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

		String typeCriteria[] = { "Right" };
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

		String typeCriteria[] = { "Right" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceIdentifierList(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public boolean ckeckOnResourceIdentifierIntegrityError(Context context)
	{
		return false;
	}

	/**
	 * Notify the WATCHDOG about right item access.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param resourceIdentifier
	 *            The resource identifier of the right item.
	 * 
	 * @param additionalTextParameter
	 *            Additional text to notify, or <TT>null</TT>.
	 */
	private void notifyWatchdog(Context context, String identifier, String additionalTextParameter)
	{
		try
		{
			// Set message Text
			String messageText = "Access to Right Item";

			// Set additional text
			String additionalText = "--> Access to Right Item";
			if (additionalTextParameter != null) additionalText += "\n" + additionalTextParameter;
			additionalText += "\n--> Identifier: '" + identifier + "'";

			// Set resource identifier documentation
			String resourceDocumentationText = null;
			resourceDocumentationText = context.getResourceManager().getResource(context, identifier).printManual(context);

			if (context.getWatchdogManager() != null) context.getWatchdogManager().addWatchdogCommand(context, identifier, messageText, additionalText, resourceDocumentationText, null, new Date());
		}
		catch (Exception e)
		{
			// Be silent
		}
	}

	/**
	 * Add a right item to a right group.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param rightGroup
	 *            The right group to be considered.
	 * 
	 * @param rightItem
	 *            The right item to be considered.
	 * 
	 * @return Returns <TT>true</TT> if the right item could be added
	 *         successfully, otherwise <TT>false</TT>.
	 */
	public boolean addRightItem(Context context, ResourceContainer rightGroup, ResourceContainer rightItem)
	{
		// Initialize variables
		boolean isSuccessful = true;
		String errorText = "--> Error on assigning rights";

		// Check right group
		String rightGroupIdentifier = null;

		if (rightGroup == null)
		{
			errorText += "\n--> Right group not set: NULL value found";
			isSuccessful = false;
		}
		else
		{
			if (!rightGroup.getUsage().equals("Group"))
			{
				errorText += "\n--> Expecting a right group";
				errorText += "\n--> But usage of resource identifier is not typed as 'Group'";
				errorText += "\n--> Identifier: '" + rightGroup.getRecourceIdentifier() + "'";
				isSuccessful = false;
			}
			else
			{
				rightGroupIdentifier = rightGroup.getRecourceIdentifier();
			}
		}

		// Check right item
		String rightItemIdentifier = null;

		if (rightItem == null)
		{
			errorText += "\n--> Right item not set: NULL value found";
			isSuccessful = false;
		}
		else
		{
			if (!rightItem.getUsage().equals("Item"))
			{
				errorText += "\n--> Expecting a right item";
				errorText += "\n--> But usage of resource identifier is not typed as 'Item'";
				errorText += "\n--> Identifier: '" + rightItem.getRecourceIdentifier() + "'";
				isSuccessful = false;
			}
			else
			{
				rightItemIdentifier = rightItem.getRecourceIdentifier();
			}
		}

		// Add right
		try
		{
			if (isSuccessful == true)
			{
				// Read list of current items of the right group
				Set<String> rightItemList = this.assignedRightItemToRightGroup.get(rightGroupIdentifier);
				if (rightItemList == null) rightItemList = new HashSet<String>();

				// Add right item to the list of the right group
				if (rightItemList.contains(rightItemIdentifier))
				{
					errorText += "\n--> Duplicate assigning of a right item to a right group";
					errorText += "\n--> Right group identifier: '" + rightGroup.getRecourceIdentifier() + "'";
					errorText += "\n--> Duplicate right item identifier: '" + rightItem.getRecourceIdentifier() + "'";
					isSuccessful = false;
				}
				else
				{
					rightItemList.add(rightItemIdentifier);
					this.assignedRightItemToRightGroup.put(rightGroupIdentifier, rightItemList);
				}
			}
		}
		catch (Exception e)
		{
			errorText += "\n--> Unexpected error on assigning rights";
			errorText += "\n--> Right group identifier: '" + rightGroup.getRecourceIdentifier() + "'";
			errorText += "\n--> Right item identifier: '" + rightItem.getRecourceIdentifier() + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Right", "ErrorOnAssigningRights"), errorText, e);
			isSuccessful = false;
		}

		// Notify error message
		if (isSuccessful == false)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Right", "ErrorOnAssigningRights"), errorText, null);
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Print right setting configuration.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param rightSettingSortedByRightGroups
	 *            The mailing list of configuration.
	 * 
	 * @return Returns the formatted distribution list as string.
	 */
	public static String printDistributionConfiguration(HashMap<String, Set<String>> rightSettingSortedByRightGroups)
	{
		// Initialize
		String resultString = "";
		String noSettingsFoundString = "*** There was no right set (empty list) ***";
		HashMap<String, Set<String>> rightSettingSortedByRightItems = new HashMap<String, Set<String>>();

		// Check parameter
		if (rightSettingSortedByRightGroups == null) return noSettingsFoundString;
		if (rightSettingSortedByRightGroups.size() == 0) return noSettingsFoundString;

		/*
		 * Print list ordered by right groups
		 */
		try
		{
			// Headline
			resultString += "\n### Right settings sorted by right groups\n";

			// Sorting the keys alphabetically
			List<String> sortedListManual = new ArrayList<String>();
			sortedListManual.addAll(rightSettingSortedByRightGroups.keySet());
			Collections.sort(sortedListManual);

			// List all items
			Iterator<String> iterManual = sortedListManual.iterator();

			while (iterManual.hasNext())
			{
				// Get resource identifier
				String identifier = iterManual.next();
				Set<String> rightItemList = rightSettingSortedByRightGroups.get(identifier);

				if (rightItemList != null && rightItemList.size() > 0)
				{
					resultString += "\n" + identifier.trim() + "\n";

					List<String> sortedRightItemList = new ArrayList<String>(rightItemList);
					Collections.sort(sortedRightItemList);

					for (String rightItem : sortedRightItemList)
					{
						if (rightItem != null && rightItem.length() > 0)
						{
							// Print on list
							resultString += "  --> " + rightItem + "\n";

							// Save email items and resource items in second
							// hash map
							Set<String> rightGroupList = rightSettingSortedByRightItems.get(rightItem);
							if (rightGroupList == null) rightGroupList = new HashSet<String>();
							rightGroupList.add(identifier.trim());
							rightSettingSortedByRightItems.put(rightItem, rightGroupList);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		/*
		 * Print list ordered by right items
		 */
		try
		{
			// Headline
			resultString += "\n\n### Right settings sorted by right items\n";

			// Sorting the keys alphabetically
			List<String> sortedListManual = new ArrayList<String>();
			sortedListManual.addAll(rightSettingSortedByRightItems.keySet());
			Collections.sort(sortedListManual);

			// List all items
			Iterator<String> iterManual = sortedListManual.iterator();

			while (iterManual.hasNext())
			{
				// Get resource identifier
				String rightItem = iterManual.next();
				Set<String> rightGroupList = rightSettingSortedByRightItems.get(rightItem);

				if (rightGroupList != null && rightGroupList.size() > 0)
				{
					resultString += "\n" + rightItem.trim() + "\n";

					List<String> sortedRightGroupList = new ArrayList<String>(rightGroupList);
					Collections.sort(sortedRightGroupList);

					for (String rightGroup : sortedRightGroupList)
					{
						if (rightGroup != null && rightGroup.length() > 0) resultString += "  --> " + rightGroup + "\n";
					}
				}
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return resultString;
	}

	/**
	 * Notify right settings as an event.
	 * 
	 * @param context
	 *            Application context.
	 */
	public void printDistributionConfiguration(Context context)
	{
		String rightSettingText = RightManager.printDistributionConfiguration(this.getAssignedRightItemToRightGroup());
		context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "Right", "RightSettingsNotification"), rightSettingText, null);
	}

	/**
	 * Check if a single right is granted.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param userRightGroup
	 *            The right group of the current user to check for.
	 * 
	 * @param rightItemToCheck
	 *            The right item that is to be checked, if access is granted.
	 * 
	 * @return Returns <TT>true</TT> if the right is granted, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean checkRight(Context context, ResourceContainer userRightGroup, ResourceContainer rightItemToCheck)
	{
		boolean isGranted = false;

		// Check for granting
		while (true)
		{
			// Validate parameter
			if (userRightGroup == null) break;
			if (rightItemToCheck == null) break;

			// Check right
			Set<String> rightItemList = this.getAssignedRightItemToRightGroup().get(userRightGroup.getRecourceIdentifier());
			if (rightItemList == null) break;
			if (rightItemList.contains(rightItemToCheck.getRecourceIdentifier())) isGranted = true;

			// Break
			break;
		}

		// Notify WATCHDOG
		String additionalText = "--> Right checked for right group: '" + userRightGroup.getRecourceIdentifier() + "'";
		additionalText += "\n--> Right item to test: '" + rightItemToCheck.getRecourceIdentifier() + "'";

		if (isGranted == true)
		{
			additionalText += "\n--> Result of test: granted";
		}
		else
		{
			additionalText += "\n--> Result of test: NOT granted";
		}

		this.notifyWatchdog(context, rightItemToCheck.getRecourceIdentifier(), additionalText);

		// Return
		return isGranted;
	}

	/**
	 * Check if for the current session/user a single right is granted.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param rightItemToCheck
	 *            The right item that is to be checked, if access is granted.
	 * 
	 * @return Returns <TT>true</TT> if the right is granted, otherwise
	 *         <TT>false</TT>.
	 */
	public static boolean hasRight(Context context, ResourceContainer rightItemToCheck)
	{
		// Check parameters
		if (context == null) return false;
		if (rightItemToCheck == null) return false;

		// Initialize
		boolean isGranted = false;

		// Get right group of the session/user
		ResourceContainer userRightGroup = context.getServerSession().getUserRightGroup();
		if (userRightGroup == null) return false;

		// Check right
		isGranted = context.getRightManager().checkRight(context, userRightGroup, rightItemToCheck);

		// Return
		return isGranted;
	}

	/**
	 * Check if an AND-List of right items for the current session/user is
	 * granted.
	 * <p>
	 * Please notice: This method is overloaded several times with different
	 * parameters.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param rightItemsToCheck
	 *            The right items that are to be checked, as a variable list of
	 *            parameters.
	 * 
	 * @return Returns <TT>true</TT> if the right is granted, otherwise
	 *         <TT>false</TT>.
	 */
	public static boolean hasRightCombinationAnd(Context context, ResourceContainer... rightItemsToCheck)
	{
		// Check parameters
		if (context == null) return false;
		if (rightItemsToCheck == null) return false;

		// Initialize
		boolean isGranted = true;

		// Get right group of the session/user
		ResourceContainer userRightGroup = context.getServerSession().getUserRightGroup();
		if (userRightGroup == null) return false;

		// Check right
		for (int i = 0; i < rightItemsToCheck.length; i++)
		{
			if (context.getRightManager().checkRight(context, userRightGroup, rightItemsToCheck[i]) == false) isGranted = false;
		}

		// Return
		return isGranted;
	}

	/**
	 * Check if an AND-List of right items for the current session/user is
	 * granted.
	 * <p>
	 * Please notice: This method is overloaded several times with different
	 * parameters.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param rightItemsToCheck
	 *            The right items that are to be checked, as a
	 *            <TT>List&lt;ResourceContainer&gt;</TT>.
	 * 
	 * @return Returns <TT>true</TT> if the right is granted, otherwise
	 *         <TT>false</TT>.
	 */
	public static boolean hasRightCombinationAnd(Context context, List<ResourceContainer> rightItemsToCheck)
	{
		// Check parameters
		if (context == null) return false;
		if (rightItemsToCheck == null) return false;

		// Initialize
		boolean isGranted = true;

		// Get right group of the session/user
		ResourceContainer userRightGroup = context.getServerSession().getUserRightGroup();
		if (userRightGroup == null) return false;

		// Check right
		for (ResourceContainer rightItem : rightItemsToCheck)
		{
			if (context.getRightManager().checkRight(context, userRightGroup, rightItem) == false) isGranted = false;
		}

		// Return
		return isGranted;
	}

	/**
	 * Check if an OR-List of right items for the current session/user is
	 * granted.
	 * <p>
	 * Please notice: This method is overloaded several times with different
	 * parameters.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param rightItemsToCheck
	 *            The right items that are to be checked, as a variable list of
	 *            parameters.
	 * 
	 * @return Returns <TT>true</TT> if the right is granted, otherwise
	 *         <TT>false</TT>.
	 */
	public static boolean hasRightCombinationOr(Context context, ResourceContainer... rightItemsToCheck)
	{
		// Check parameters
		if (context == null) return false;
		if (rightItemsToCheck == null) return false;

		// Initialize
		boolean isGranted = false;

		// Get right group of the session/user
		ResourceContainer userRightGroup = context.getServerSession().getUserRightGroup();
		if (userRightGroup == null) return false;

		// Check right
		for (int i = 0; i < rightItemsToCheck.length; i++)
		{
			if (context.getRightManager().checkRight(context, userRightGroup, rightItemsToCheck[i]) == true) isGranted = true;
		}

		// Return
		return isGranted;
	}

	/**
	 * Check if an OR-List of right items for the current session/user is
	 * granted.
	 * <p>
	 * Please notice: This method is overloaded several times with different
	 * parameters.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param rightItemsToCheck
	 *            The right items that are to be checked, as a
	 *            <TT>List&lt;ResourceContainer&gt;</TT>.
	 * 
	 * @return Returns <TT>true</TT> if the right is granted, otherwise
	 *         <TT>false</TT>.
	 */
	public static boolean hasRightCombinationOr(Context context, List<ResourceContainer> rightItemsToCheck)
	{
		// Check parameters
		if (context == null) return false;
		if (rightItemsToCheck == null) return false;

		// Initialize
		boolean isGranted = false;

		// Get right group of the session/user
		ResourceContainer userRightGroup = context.getServerSession().getUserRightGroup();
		if (userRightGroup == null) return false;

		// Check right
		for (ResourceContainer rightItem : rightItemsToCheck)
		{
			if (context.getRightManager().checkRight(context, userRightGroup, rightItem) == true) isGranted = true;
		}

		// Return
		return isGranted;
	}

	/**
	 * Getter
	 */
	private HashMap<String, Set<String>> getAssignedRightItemToRightGroup()
	{
		return assignedRightItemToRightGroup;
	}
}
