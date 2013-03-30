package fmagic.basic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class implements the management of licenses used by servers and clients.
 * <p>
 * The license management let you handle <TT>License Items</TT> that are
 * combined to <TT>License Models</TT>. Each license item is related to a
 * specific domain functionality. A user, logged to the system, can be assigned
 * to a license model and its specific settings. At runtime the system checks if
 * the logged user actually is allowed to use the specific functionality or data
 * from the point of view of licenses.
 * <p>
 * The license functionality also provides the organization of budgets that are
 * to be consumed, something like credits. You can fill credits and consume
 * credits via the license manager. Additionally, all activities related to
 * license items can be tracked either to analyze them later or to account them
 * to the customer. At the end of the day you might print billings and provide
 * sophisticated payed services. Credits are stored in the underlying database
 * automatically by the system. You don't have to care about details. All
 * bookings regarding the credits are documented in the database including
 * additional information.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 11.03.2013 - Created
 * 
 */
public class LicenseManager implements ResourceInterface
{
	// License Item / License Model settings
	final private HashMap<String, Set<String>> assignedLicenseItemToLicenseModel = new HashMap<String, Set<String>>();
	final private HashMap<String, List<String>> defaultValuesOfLicense = new HashMap<String, List<String>>();

	// Constants for data
	final private static String LICENSE_KEY_PLACEHOLDER = "${LicenseKey}";

	/**
	 * Constructor
	 */
	public LicenseManager()
	{
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = { "License" };
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

		String typeCriteria[] = { "License" };
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

		String typeCriteria[] = { "License" };
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

				// LicenseManager: Alias name must be set
				if (resourceContainer.getType().equalsIgnoreCase("license") && resourceContainer.getUsage().equalsIgnoreCase("model") && resourceContainer.checkAliasName() == false)
				{
					String errorString = "--> Alias name for license model identifier is not set.";
					String fileName = resourceManager.getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
					if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
					errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
					errorString += "\n--> The Alias name is used for differentiating model names.";
					errorString += "\n--> Please set an Alias name that correspondences with the license model name.";

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

	/**
	 * Composes the path of the data directory of license files.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the directory were the license files are stored.
	 * 
	 */
	private String getLicenseFilePath(Context context)
	{
		return FileLocationManager.getRootPath() + FileLocationManager.getLicenseSubPath() + "\\";
	}

	/**
	 * Get the file name type of license files.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the license file name type.
	 * 
	 */
	private String getLicenseFileNameType(Context context)
	{
		return FileLocationManager.getLicenseFileNameType();
	}

	/**
	 * Load all license files of the system.
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
	 * @return Returns <TT>true</TT> if the license file could be read
	 *         successfully, otherwise <TT>false</TT>.
	 */
	public boolean loadLicenseFiles(Context context, String applicationIdentifier, int applicationVersion)
	{
		// Get all files of the license directory as a list
		String licenseFilePath = this.getLicenseFilePath(context);
		File[] fileList = null;

		try
		{
			File dir = new File(licenseFilePath);
			fileList = dir.listFiles();
		}
		catch (Exception e)
		{
			String additionalText = "--> Error on parsing license file path";
			additionalText += "\n--> Searched in path: '" + licenseFilePath + "'";
			context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "License", "ErrorOnReadingLicenseFile"), additionalText, e);
			return false;
		}

		if (fileList == null) return false;

		// Go through the list of license files
		String licenseFileNameType = this.getLicenseFileNameType(context);
		String licenseFileName = null;
		boolean isSuccessful = true;

		try
		{
			for (File file : fileList)
			{
				HashMap<String, String> organizationalProperties = new HashMap<String, String>();

				licenseFileName = file.getAbsolutePath();

				if (!licenseFileName.endsWith(licenseFileNameType)) continue;

				if (context.getResourceManager().loadResourceFile(context, applicationIdentifier, null, licenseFileName, null, true, organizationalProperties) == false) isSuccessful = false;
			}
		}
		catch (Exception e)
		{
			String additionalText = "--> Error on reading license file";
			additionalText += "\n--> On license file: '" + "*." + licenseFileName + "'";
			context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "License", "ErrorOnReadingLicenseFile"), additionalText, e);
			isSuccessful = false;
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Notify the WATCHDOG about license item access.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param resourceIdentifier
	 *            The resource identifier of the license item.
	 * 
	 * @param additionalTextParameter
	 *            Additional text to notify, or <TT>null</TT>.
	 */
	private void notifyWatchdog(Context context, String identifier, String additionalTextParameter)
	{
		try
		{
			// Set message Text
			String messageText = "Access to License Item";

			// Set additional text
			String additionalText = "--> Access to License Item";
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
	 * Add a license item to a license model, without any settings for values.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param licenseModel
	 *            The license model to be considered.
	 * 
	 * @param licenseItem
	 *            The license item to be considered.
	 * 
	 * @return Returns <TT>true</TT> if the license item could be added
	 *         successfully, otherwise <TT>false</TT>.
	 */
	public boolean addLicenseItem(Context context, ResourceContainer licenseModel, ResourceContainer licenseItem)
	{
		// Initialize variables
		boolean isSuccessful = true;
		String errorText = "--> Error on assigning licenses";

		// Check license model
		String licenseModelIdentifier = null;

		if (licenseModel == null)
		{
			errorText += "\n--> License model not set: NULL value found";
			isSuccessful = false;
		}
		else
		{
			if (!licenseModel.getUsage().equals("Model"))
			{
				errorText += "\n--> Expecting a license model";
				errorText += "\n--> But usage of resource identifier is not typed as 'Model'";
				errorText += "\n--> Identifier: '" + licenseModel.getRecourceIdentifier() + "'";
				isSuccessful = false;
			}
			else
			{
				licenseModelIdentifier = licenseModel.getRecourceIdentifier();
			}
		}

		// Check license item
		String licenseItemIdentifier = null;

		if (licenseItem == null)
		{
			errorText += "\n--> License item not set: NULL value found";
			isSuccessful = false;
		}
		else
		{
			if (!licenseItem.getUsage().equals("Item"))
			{
				errorText += "\n--> Expecting a license item";
				errorText += "\n--> But usage of resource identifier is not typed as 'Item'";
				errorText += "\n--> Identifier: '" + licenseItem.getRecourceIdentifier() + "'";
				isSuccessful = false;
			}
			else
			{
				licenseItemIdentifier = licenseItem.getRecourceIdentifier();
			}
		}

		// Add license item to the model
		try
		{
			if (isSuccessful == true)
			{
				// Read list of current items of the license model
				Set<String> licenseItemList = this.assignedLicenseItemToLicenseModel.get(licenseModelIdentifier);
				if (licenseItemList == null) licenseItemList = new HashSet<String>();

				// Check if the license item is already part of the list
				if (licenseItemList.contains(licenseItemIdentifier))
				{
					errorText += "\n--> Duplicate assigning of a license item to a license model";
					errorText += "\n--> License model identifier: '" + licenseModel.getRecourceIdentifier() + "'";
					errorText += "\n--> Duplicate license item identifier: '" + licenseItem.getRecourceIdentifier() + "'";
					isSuccessful = false;
				}
				else
				{
					// Add license item to the list of the license model
					licenseItemList.add(licenseItemIdentifier);
					this.assignedLicenseItemToLicenseModel.put(licenseModelIdentifier, licenseItemList);

					// Set default values of the license item
					List<String> valueSet = new ArrayList<String>();

					for (int valueNumber = 0; valueNumber < 100; valueNumber++)
					{
						String value = licenseItem.getAttributeValue(context, valueNumber, null);
						if (value == null) break;

						valueSet.add(value);
					}

					if (valueSet.size() > 0)
					{
						String licenseIdentifier = licenseModel.getRecourceIdentifier() + "|" + licenseItem.getRecourceIdentifier();
						this.defaultValuesOfLicense.put(licenseIdentifier, valueSet);
					}
				}
			}
		}
		catch (Exception e)
		{
			errorText += "\n--> Unexpected error on assigning licenses";
			errorText += "\n--> License model identifier: '" + licenseModel.getRecourceIdentifier() + "'";
			errorText += "\n--> License item identifier: '" + licenseItem.getRecourceIdentifier() + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "License", "ErrorOnAssigningLicense"), errorText, e);
			isSuccessful = false;
		}

		// Notify error message
		if (isSuccessful == false)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "License", "ErrorOnAssigningLicense"), errorText, null);
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Get the default values of a specific license combination
	 * <TT>License Model</TT> and <TT>License Item</TT> as a list of strings
	 * <TT>Set&lt;String&gt;</TT>.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param licenseModelIdentifier
	 *            The license model to be considered.
	 * 
	 * @param licenseItemIdentifier
	 *            The license item to be considered.
	 * 
	 * @return Returns a list of strings or <TT>null</TT> if no values are set.
	 */
	private List<String> getLicenseDefaultValue(String licenseModelIdentifier, String licenseItemIdentifier)
	{
		// Check if values are set
		if (licenseModelIdentifier == null) return null;
		if (licenseModelIdentifier.length() == 0) return null;
		if (licenseItemIdentifier == null) return null;
		if (licenseItemIdentifier.length() == 0) return null;

		// Get values of the license combination
		try
		{
			String licenseIdentifier = licenseModelIdentifier.trim() + "|" + licenseItemIdentifier.trim();

			List<String> valueList = this.defaultValuesOfLicense.get(licenseIdentifier);

			if (valueList == null) return null;
			if (valueList.size() == 0) return null;

			return valueList;

		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return null;
	}

	/**
	 * Check if the values set as parameter in the method
	 * <TT>addLicenseItem()</TT> and the values <TT>Value.x</TT> set in the
	 * resource file are synchronized.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param licenseModel
	 *            The license model to be considered.
	 * 
	 * @param licenseItem
	 *            The license item to be considered.
	 * 
	 * @param valueSet
	 *            The default values of the license item, as a set of string
	 *            values.
	 * 
	 * @return Returns <TT>true</TT> if the license item values are
	 *         synchronized, otherwise <TT>false</TT>.
	 */
	private boolean checkLicenseItemValues(Context context, ResourceContainer licenseModel, ResourceContainer licenseItem, List<String> valueSet)
	{
		// Validate parameter
		if (licenseItem == null) return true;
		if (licenseModel == null) return true;

		// Check if the values of the parameter are set in the resource file too
		boolean isSuccessful = true;

		if (valueSet != null && valueSet.size() > 0)
		{
			int valueNumber = 0;
			if (valueSet.size() > 1) valueNumber = 1;

			for (String valueOfParameter : valueSet)
			{
				String valueOfResourceFile = licenseItem.getAttributeValue(context, valueNumber, null);

				if (valueOfResourceFile == null)
				{
					String errorText = "--> Error on assigning licenses";
					errorText += "\n--> Value '" + String.valueOf(valueNumber) + "' was set to '" + valueOfParameter + "' as parameter of the method 'addLicenseItem()',";
					errorText += "\n--> But was not found in the resource file of the application, as '" + licenseItem.getAttributeResourceIdentifier(context, valueNumber, null) + "'.";
					errorText += "\n--> Please synchronize the settings.";
					errorText += "\n--> License model identifier: '" + licenseModel.getRecourceIdentifier() + "'";
					errorText += "\n--> License item identifier: '" + licenseItem.getRecourceIdentifier() + "'";
					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "License", "ErrorOnAssigningLicense"), errorText, null);
					isSuccessful = false;
				}

				valueNumber++;
			}
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Add a license item to a license model, including default values, handed
	 * over as a list of strings <TT>List&lt;String&gt;</TT>.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param licenseModel
	 *            The license model to be considered.
	 * 
	 * @param licenseItem
	 *            The license item to be considered.
	 * 
	 * @param valueSet
	 *            The default values of the license item, as a set of string
	 *            values.
	 * 
	 * @return Returns <TT>true</TT> if the license item could be added
	 *         successfully, otherwise <TT>false</TT>.
	 */
	private boolean addLicenseItem(Context context, ResourceContainer licenseModel, ResourceContainer licenseItem, List<String> valueSet)
	{
		// Validate parameter
		if (licenseItem == null) return false;
		if (licenseModel == null) return false;

		// Initialize variables
		boolean isSuccessful = true;

		// Check if the values of the parameter are set in the resource file too
		if (checkLicenseItemValues(context, licenseModel, licenseItem, valueSet) == false) isSuccessful = false;

		// Add license item to the model
		if (this.addLicenseItem(context, licenseModel, licenseItem) == false) return false;

		// Check if values are set
		if (valueSet == null) return isSuccessful;
		if (valueSet.size() <= 0) return isSuccessful;

		// Save values as default values of the license
		try
		{
			String licenseIdentifier = licenseModel.getRecourceIdentifier() + "|" + licenseItem.getRecourceIdentifier();
			this.defaultValuesOfLicense.put(licenseIdentifier, valueSet);

		}
		catch (Exception e)
		{
			String errorText = "\n--> Unexpected error on assigning license values";
			errorText += "\n--> License model identifier: '" + licenseModel.getRecourceIdentifier() + "'";
			errorText += "\n--> License item identifier: '" + licenseItem.getRecourceIdentifier() + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "License", "ErrorOnAssigningLicense"), errorText, e);
			return false;
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Add a license item to a license model, including default values, handed
	 * over as a variable parameter list of strings.
	 * <p>
	 * Please notice: This method is overloaded by several signatures, each of
	 * them handling a specific value type, like <TT>String</TT>, <TT>int</TT>
	 * and <TT>boolean</TT>.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param licenseModel
	 *            The license model to be considered.
	 * 
	 * @param licenseItem
	 *            The license item to be considered.
	 * 
	 * @param valueParameterList
	 *            The default values of the license item, as a variable
	 *            parameter list of <TT>String</TT> values.
	 * 
	 * @return Returns <TT>true</TT> if the license item could be added
	 *         successfully, otherwise <TT>false</TT>.
	 */
	public boolean addLicenseItem(Context context, ResourceContainer licenseModel, ResourceContainer licenseItem, String... valueParameterList)
	{
		try
		{
			List<String> valueSet = new ArrayList<String>();

			for (int i = 0; i < valueParameterList.length; i++)
			{
				valueSet.add(valueParameterList[i]);
			}

			return this.addLicenseItem(context, licenseModel, licenseItem, valueSet);

		}
		catch (Exception e)
		{
			String errorText = "\n--> Unexpected error on assigning license values";
			errorText += "\n--> License model identifier: '" + licenseModel.getRecourceIdentifier() + "'";
			errorText += "\n--> License item identifier: '" + licenseItem.getRecourceIdentifier() + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "License", "ErrorOnAssigningLicense"), errorText, e);
			return false;
		}
	}

	/**
	 * Add a license item to a license model, including default values, handed
	 * over as a variable parameter list of <TT>integer</TT>.
	 * <p>
	 * Please notice: This method is overloaded by several signatures, each of
	 * them handling a specific value type, like <TT>String</TT>, <TT>int</TT>
	 * and <TT>boolean</TT>.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param licenseModel
	 *            The license model to be considered.
	 * 
	 * @param licenseItem
	 *            The license item to be considered.
	 * 
	 * @param valueParameterList
	 *            The default values of the license item, as a variable
	 *            parameter list of <TT>int</TT> values.
	 * 
	 * @return Returns <TT>true</TT> if the license item could be added
	 *         successfully, otherwise <TT>false</TT>.
	 */
	public boolean addLicenseItem(Context context, ResourceContainer licenseModel, ResourceContainer licenseItem, int... valueParameterList)
	{
		try
		{
			List<String> valueSet = new ArrayList<String>();

			for (int i = 0; i < valueParameterList.length; i++)
			{
				valueSet.add(String.valueOf(valueParameterList[i]));
			}

			return this.addLicenseItem(context, licenseModel, licenseItem, valueSet);

		}
		catch (Exception e)
		{
			String errorText = "\n--> Unexpected error on assigning license values";
			errorText += "\n--> License model identifier: '" + licenseModel.getRecourceIdentifier() + "'";
			errorText += "\n--> License item identifier: '" + licenseItem.getRecourceIdentifier() + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "License", "ErrorOnAssigningLicense"), errorText, e);
			return false;
		}
	}

	/**
	 * Add a license item to a license model, including default values, handed
	 * over as a variable parameter list of <TT>boolean</TT>.
	 * <p>
	 * Please notice: This method is overloaded by several signatures, each of
	 * them handling a specific value type, like <TT>String</TT>, <TT>int</TT>
	 * and <TT>boolean</TT>.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param licenseModel
	 *            The license model to be considered.
	 * 
	 * @param licenseItem
	 *            The license item to be considered.
	 * 
	 * @param valueParameterList
	 *            The default values of the license item, as a variable
	 *            parameter list of <TT>boolean</TT> values.
	 * 
	 * @return Returns <TT>true</TT> if the license item could be added
	 *         successfully, otherwise <TT>false</TT>.
	 */
	public boolean addLicenseItem(Context context, ResourceContainer licenseModel, ResourceContainer licenseItem, boolean... valueParameterList)
	{
		try
		{
			List<String> valueSet = new ArrayList<String>();

			for (int i = 0; i < valueParameterList.length; i++)
			{
				valueSet.add(String.valueOf(valueParameterList[i]));
			}

			return this.addLicenseItem(context, licenseModel, licenseItem, valueSet);

		}
		catch (Exception e)
		{
			String errorText = "\n--> Unexpected error on assigning license values";
			errorText += "\n--> License model identifier: '" + licenseModel.getRecourceIdentifier() + "'";
			errorText += "\n--> License item identifier: '" + licenseItem.getRecourceIdentifier() + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "License", "ErrorOnAssigningLicense"), errorText, e);
			return false;
		}
	}

	/**
	 * Print license model configuration.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param licenseSettingSortedByLicenseModels
	 *            The mailing list of configuration.
	 * 
	 * @return Returns the formatted distribution list as string.
	 */
	public String printDistributionConfiguration(HashMap<String, Set<String>> licenseSettingSortedByLicenseModels)
	{
		// Initialize
		String resultString = "";
		String noSettingsFoundString = "*** There was no license set (empty list) ***";
		HashMap<String, Set<String>> licenseSettingSortedByLicenseItems = new HashMap<String, Set<String>>();

		// Check parameter
		if (licenseSettingSortedByLicenseModels == null) return noSettingsFoundString;
		if (licenseSettingSortedByLicenseModels.size() == 0) return noSettingsFoundString;

		/*
		 * Print list ordered by license models
		 */
		try
		{
			// Headline
			resultString += "\n### License settings sorted by license models\n";

			// Sorting the keys alphabetically
			List<String> sortedListManual = new ArrayList<String>();
			sortedListManual.addAll(licenseSettingSortedByLicenseModels.keySet());
			Collections.sort(sortedListManual);

			// List all items
			Iterator<String> iterManual = sortedListManual.iterator();

			while (iterManual.hasNext())
			{
				// Get resource identifier
				String identifier = iterManual.next();
				Set<String> licenseItemList = licenseSettingSortedByLicenseModels.get(identifier);

				if (licenseItemList != null && licenseItemList.size() > 0)
				{
					resultString += "\n" + identifier.trim() + "\n";

					List<String> sortedLicenseItemList = new ArrayList<String>(licenseItemList);
					Collections.sort(sortedLicenseItemList);

					for (String licenseItem : sortedLicenseItemList)
					{
						if (licenseItem != null && licenseItem.length() > 0)
						{
							// Get default values
							List<String> valueSet = this.getLicenseDefaultValue(identifier, licenseItem);

							String valueList = "";

							if (valueSet != null && valueSet.size() > 0)
							{
								for (String value : valueSet)
								{
									valueList += " [" + value.trim() + "]";
								}
							}

							// Print on list
							resultString += "  --> " + licenseItem + valueList + "\n";

							// Save license items in second hash map
							Set<String> licenseModelList = licenseSettingSortedByLicenseItems.get(licenseItem);
							if (licenseModelList == null) licenseModelList = new HashSet<String>();
							licenseModelList.add(identifier.trim());
							licenseSettingSortedByLicenseItems.put(licenseItem, licenseModelList);
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
		 * Print list ordered by license items
		 */
		try
		{
			// Headline
			resultString += "\n\n### License settings sorted by license items\n";

			// Sorting the keys alphabetically
			List<String> sortedListManual = new ArrayList<String>();
			sortedListManual.addAll(licenseSettingSortedByLicenseItems.keySet());
			Collections.sort(sortedListManual);

			// List all items
			Iterator<String> iterManual = sortedListManual.iterator();

			while (iterManual.hasNext())
			{
				// Get resource identifier
				String licenseItem = iterManual.next();
				Set<String> licenseModelList = licenseSettingSortedByLicenseItems.get(licenseItem);

				if (licenseModelList != null && licenseModelList.size() > 0)
				{
					resultString += "\n" + licenseItem.trim() + "\n";

					List<String> sortedLicenseModelList = new ArrayList<String>(licenseModelList);
					Collections.sort(sortedLicenseModelList);

					for (String licenseModel : sortedLicenseModelList)
					{
						if (licenseModel != null && licenseModel.length() > 0)
						{
							// Get default values
							List<String> valueSet = this.getLicenseDefaultValue(licenseModel, licenseItem);

							String valueList = "";

							if (valueSet != null && valueSet.size() > 0)
							{
								for (String value : valueSet)
								{
									valueList += " [" + value.trim() + "]";
								}
							}

							// Print out
							resultString += "  --> " + licenseModel + valueList + "\n";
						}
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
	 * Check if a single license is defined.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param licenseKey
	 *            The license key of the current user to check for.
	 * 
	 * @param licenseItemToCheck
	 *            The license item that is to be checked, if access is granted.
	 * 
	 * @return Returns <TT>true</TT> if the license is defined, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean checkLicense(Context context, String licenseKey, ResourceContainer licenseItemToCheck)
	{
		boolean isDefined = false;

		// Check if license is defined
		while (true)
		{
			// Validate parameter
			if (licenseKey == null || licenseKey.length() == 0) break;
			if (licenseItemToCheck == null) break;

			// Check license
			String value0 = licenseItemToCheck.getAttributeValue(context, 0, licenseKey);
			String value1 = licenseItemToCheck.getAttributeValue(context, 1, licenseKey);
			if (value0 == null && value1 == null) break;
			isDefined = true;

			// Break
			break;
		}

		// Notify WATCHDOG
		String additionalText = "--> License checked for license key: '" + licenseKey + "'";
		additionalText += "\n--> License item to test: '" + licenseItemToCheck.getRecourceIdentifier() + "'";

		if (isDefined == true)
		{
			additionalText += "\n--> Result of test: defined";
		}
		else
		{
			additionalText += "\n--> Result of test: NOT defined";
		}

		this.notifyWatchdog(context, licenseItemToCheck.getRecourceIdentifier(), additionalText);

		// Return
		return isDefined;
	}

	/**
	 * Get a value from a license item.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param licenseKey
	 *            The license key of the current user to check for.
	 * 
	 * @param licenseItemToCheck
	 *            The license item that is to be checked, if access is granted.
	 * 
	 * @param valueNumber
	 *            The number of the "Value" attribute to be read.
	 * 
	 * @return Returns the value read, or <TT>null</TT>, if no value is
	 *         available.
	 */
	public String getValue(Context context, String licenseKey, ResourceContainer licenseItemToCheck, int valueNumber)
	{
		String value = null;

		// Check if license is defined
		while (true)
		{
			// Validate parameter
			if (licenseKey == null || licenseKey.length() == 0) break;
			if (licenseItemToCheck == null) break;
			if (valueNumber < 0) break;

			// Read license value
			value = licenseItemToCheck.getAttributeValue(context, valueNumber, licenseKey);

			// Break
			break;
		}

		// Notify WATCHDOG
		String additionalText = "--> License value read for license key: '" + licenseKey + "'";
		additionalText += "\n--> License item: '" + licenseItemToCheck.getRecourceIdentifier() + "'";

		if (value != null)
		{
			additionalText += "\n--> Result value: '" + value + "'";
		}
		else
		{
			additionalText += "\n--> Result value: NULL";
		}

		this.notifyWatchdog(context, licenseItemToCheck.getRecourceIdentifier(), additionalText);

		// Return
		return value;
	}

	/**
	 * Composes the path of the data directory of template license files.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the directory were the data files are stored.
	 * 
	 */
	private String getLicenseTemplateFilePath(Context context)
	{
		return FileLocationManager.getRootPath() + FileLocationManager.getLicenseTemplateSubPath() + "\\";
	}

	/**
	 * Composes the file name of template license files.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param application
	 *            The application to consider.
	 * 
	 * @param licenseModel
	 *            The license model to consider.
	 * 
	 * @return Returns the file name of the data files, or <TT>null</TT>.
	 * 
	 */
	private String getLicenseTemplateFileName(Context context, String application, String licenseModel)
	{
		try
		{
			String fileName = FileLocationManager.getLicenseTemplateFileName();
			fileName = FileLocationManager.replacePlaceholder(context, fileName, application, null);
			fileName = fileName.replace("${licensemodel}", licenseModel.toLowerCase());
			return fileName;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Creates all template license files for an application.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param application
	 *            The application to consider.
	 * 
	 * @param version
	 *            The version to consider.
	 * 
	 * @return Returns <TT>true</TT> if the license template files could be
	 *         created, otherwise <TT>false</TT>.
	 */
	public boolean createTemplateLicenseFiles(Context context, String application, int version)
	{
		// Initialization
		boolean isSuccessful = true;

		// Write all license template files
		try
		{
			// Go through the list of all license models
			List<String> sortedListOfLicenseModels = new ArrayList<String>();
			sortedListOfLicenseModels.addAll(this.getAssignedLicenseItemToLicenseModel().keySet());
			Collections.sort(sortedListOfLicenseModels);

			// List all items
			Iterator<String> iterManual = sortedListOfLicenseModels.iterator();

			while (iterManual.hasNext())
			{
				// Get resource identifier
				String licenseModelIdentifier = iterManual.next();
				if (licenseModelIdentifier == null) continue;

				// Create license template file
				if (this.createTemplateLicenseFile(context, application, version, licenseModelIdentifier) == false) isSuccessful = false;
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Creates a template license file for a specific application and license
	 * model.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param application
	 *            The application to consider.
	 * 
	 * @param version
	 *            The version to consider.
	 * 
	 * @param licenseModelIdentifier
	 *            The resource identifier of the license model to consider.
	 * 
	 * @return Returns <TT>true</TT> if the license template file could be
	 *         created, otherwise <TT>false</TT>.
	 */
	private synchronized boolean createTemplateLicenseFile(Context context, String application, int version, String licenseModelIdentifier)
	{
		// Check parameter
		if (application == null || application.length() == 0) return false;
		if (licenseModelIdentifier == null || licenseModelIdentifier.length() == 0) return false;

		// Define variables
		String fileName = null;
		PrintWriter output;
		String pathName = null;

		// Create file directory for template files
		try
		{
			pathName = this.getLicenseTemplateFilePath(context);

			File directory = new File(pathName);
			directory.mkdirs();
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "License", "ErrorOnWritingLicenseTemplateFile"), "--> Path name: " + pathName, e);
			return false;
		}

		// Write text to the template file
		try
		{
			// Get the alias name of the license model
			ResourceContainer licenseModelResourceContainer = context.getResourceManager().getResource(context, licenseModelIdentifier);
			if (licenseModelResourceContainer == null) return false;

			String licenseModelName = licenseModelResourceContainer.getAliasName();
			if (licenseModelName == null || licenseModelName.length() == 0) return false;

			// Compose file name
			fileName = this.getLicenseTemplateFilePath(context) + "\\" + this.getLicenseTemplateFileName(context, application, licenseModelName);

			// Open file
			output = new PrintWriter(new FileOutputStream(new File(fileName), false));

			// Write common attributes first
			String language = context.getResourceManager().getCommonResourceFileLanguage(context, application);
			String commonAttributes = ResourceManager.getCommonAttributeList(context, application, version, language, LICENSE_KEY_PLACEHOLDER);

			if (commonAttributes != null && commonAttributes.length() > 0)
			{
				output.append(Util.normalizeNewLine(commonAttributes));
			}

			// Get a list of all license items of the license model and go
			// through
			Set<String> licenseItemList = this.getAssignedLicenseItemToLicenseModel().get(licenseModelIdentifier);

			if (licenseItemList != null && licenseItemList.size() > 0)
			{
				List<String> sortedLicenseItemList = new ArrayList<String>(licenseItemList);
				Collections.sort(sortedLicenseItemList);

				for (String licenseItem : sortedLicenseItemList)
				{
					String licenseTemplateText = "";

					if (licenseItem != null && licenseItem.length() > 0)
					{
						// Get and print template documentation
						String documentationText = null;
						ResourceContainer licenseItemResourceContainer = context.getResourceManager().getResource(context, licenseItem);
						if (licenseItemResourceContainer != null) documentationText = licenseItemResourceContainer.printTemplate(context, false);

						if (documentationText != null)
						{
							licenseTemplateText += "" + documentationText;
						}
						else
						{
							licenseTemplateText += "\n";
						}

						// Get and print default values
						List<String> valueSet = this.getLicenseDefaultValue(licenseModelIdentifier, licenseItem);

						if (valueSet != null && valueSet.size() > 0)
						{
							int valueNumber = 0;

							for (String value : valueSet)
							{
								valueNumber++;

								// There is only one value set --> "|Value="
								if (valueNumber == 1 && valueSet.size() == 1)
								{
									licenseTemplateText += licenseItemResourceContainer.getRecourceIdentifier() + ResourceManager.getAttributeDelimiterString() + licenseItemResourceContainer.getAttributeResourceIdentifier(context, 0, LICENSE_KEY_PLACEHOLDER) + "=" + value.trim() + "\n";
								}
								// There are several values set --> "|Value.1=",
								// "|Value.2=", "|Value.3="
								else
								{
									licenseTemplateText += licenseItemResourceContainer.getRecourceIdentifier() + ResourceManager.getAttributeDelimiterString() + licenseItemResourceContainer.getAttributeResourceIdentifier(context, valueNumber, LICENSE_KEY_PLACEHOLDER) + "=" + value.trim() + "\n";
								}
							}
						}

						licenseTemplateText += "\n";

						// Print out to file
						output.append(Util.normalizeNewLine(licenseTemplateText));
					}
				}
			}

			// Close file
			output.close();
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "License", "ErrorOnWritingLicenseTemplateFile"), "--> File name: " + fileName, e);
		}

		// Return
		return true;
	}

	/**
	 * Getter
	 */
	public HashMap<String, Set<String>> getAssignedLicenseItemToLicenseModel()
	{
		return assignedLicenseItemToLicenseModel;
	}
}
