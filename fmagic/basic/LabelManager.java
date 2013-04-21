package fmagic.basic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class implements the management of labels used by servers and clients to
 * show messages to users.
 * <p>
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class LabelManager implements ManagerInterface
{
	final private HashMap<String, String> mainLanguageValueNotFoundProperties = new HashMap<String, String>();
	final private HashMap<String, String> secondaryLanguageValueNotFound = new HashMap<String, String>();
	final private HashMap<String, String> resourceValueNotFoundProperties = new HashMap<String, String>();
	final private HashMap<String, String> exceptionProperties = new HashMap<String, String>();

	/**
	 * Constructor
	 */
	public LabelManager()
	{
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = { "Label" };
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

		String typeCriteria[] = { "Label" };
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

		String typeCriteria[] = { "Label" };
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
		return false;
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

	/**
	 * Composes the path of the data directory of label resource files.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the directory were the data files are stored.
	 * 
	 */
	private String getResourceLabelFilePath(Context context)
	{
		return FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getResourceLabelSubPath());
	}

	/**
	 * Composes the path of the data directory of translated label resource
	 * files.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the directory were the data files are stored.
	 * 
	 */
	private String getTranslatedLabelFilePath(Context context)
	{
		return FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getResourceLabelTranslatedSubPath());
	}

	/**
	 * Composes the file name of the label resource file.
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
	private String getResourceLabelFileName(Context context, String applicationName)
	{
		String fileName = FileLocationFunctions.getResourceLabelFileName();
		fileName = FileLocationFunctions.replacePlaceholder(context, fileName, applicationName, null);
		return fileName;
	}

	/**
	 * Composes the file name of the translated label resource file.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param applicationName
	 *            The name of the application from the point of view of the
	 *            resource files, including Basic, Common and Extension.
	 * 
	 * @param language
	 *            The short name of the language.
	 * 
	 * @return Returns the file name of the data files.
	 * 
	 */
	private String getTranslatedLabelFileName(Context context, String applicationName, String language)
	{
		String fileName = FileLocationFunctions.getResourceLabelTranslatedFileName();
		fileName = FileLocationFunctions.replacePlaceholder(context, fileName, applicationName, language);
		return fileName;
	}

	/**
	 * Load label resources from a resource file.
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
	public boolean loadLabelResourceFile(Context context, String applicationIdentifier, int applicationVersion)
	{
		HashMap<String, String> organizationalProperties = new HashMap<String, String>();

		// Set File name
		String fileName = FileLocationFunctions.compileFilePath(this.getResourceLabelFilePath(context), this.getResourceLabelFileName(context, applicationIdentifier));

		// Invoke general function to read resource files
		boolean isSuccessful = context.getResourceManager().loadResourceFile(context, applicationIdentifier, applicationVersion, fileName, null, false, organizationalProperties);

		// Check language specific
		String propertyLanguageIdentifier = organizationalProperties.get(ResourceManager.RESOURCE_FILE_LANGUAGE);

		if (propertyLanguageIdentifier == null || context.getApplicationManager().getSupportedLanguages().get(propertyLanguageIdentifier) == null)
		{
			String errorString = "--> Language item missing or does not match the supported languages";
			errorString += "\n--> Supported languages: '" + context.getApplicationManager().getSupportedLanguagesString() + "'";
			errorString += "\n--> Resource file item [" + ResourceManager.RESOURCE_FILE_LANGUAGE + "] was set to '" + propertyLanguageIdentifier + "'";
			errorString += "\n--> Resource file name: '" + fileName + "'";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "ErrorOnReadingResourceFile"), errorString, null);
			isSuccessful = false;
		}

		// Return
		return isSuccessful;
	}

	/**
	 * Load translated labels from resource file.
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
	public boolean loadTranslatedLabelFile(Context context, String applicationIdentifier, int applicationVersion, String language)
	{
		HashMap<String, String> organizationalProperties = new HashMap<String, String>();

		// Set File name
		String fileName = FileLocationFunctions.compileFilePath(this.getTranslatedLabelFilePath(context), this.getTranslatedLabelFileName(context, applicationIdentifier, language));

		// Invoke general function to read resource files
		boolean isSuccessful = context.getResourceManager().loadResourceFile(context, applicationIdentifier, applicationVersion, fileName, language, true, organizationalProperties);

		// Return
		return isSuccessful;
	}

	/**
	 * Get the value (text) of a label.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param label
	 *            Resource container of the label.
	 * 
	 * @return Returns the value of the label or an <TT>empty</TT> string, if an
	 *         error occurred.
	 * 
	 */
	public static String getLabelText(Context context, ResourceContainer label)
	{
		return getLabelText(context, label, 0);
	}

	/**
	 * Get all values (texts) "0", "1", "2" ... of a label as an array of
	 * strings.
	 * 
	 * @param externalContext
	 *            The external context of the caller.
	 * 
	 * @param label
	 *            Resource container of the label.
	 * 
	 * @return Returns the values of the label as a list of strings or an
	 *         <TT>empty</TT> list, if no texts could be found.
	 * 
	 */
	public static List<String> getLabelTextArray(Context externalContext, ResourceContainer label)
	{
		// Define List of Strings
		List<String> textArray = new ArrayList<String>();

		// Validate parameter
		if (label == null) return textArray;

		// Create a specific label context
		Context labelContext = externalContext.createTrackingContext(ResourceManager.context(externalContext, "Label", "Processing"));

		// Read all values until the first empty value is found
		for (int number = 0; number < 100; number++)
		{
			// Checking if the "Value.x" attribute is defined in the label
			// resource file
			String labelResourceText = label.getAttributeValue(labelContext, number, null);
			if (labelResourceText == null || labelResourceText.length() == 0) break;

			String text = LabelManager.getLabelText(labelContext, label, number);
			if (text == null) text = "";
			textArray.add(text);
		}

		// Return
		return textArray;
	}

	/**
	 * Get the value (text) of a label.
	 * 
	 * @param externalContext
	 *            The external context of the caller.
	 * 
	 * @param label
	 *            Resource container of the label.
	 * 
	 * @param number
	 *            The number of the value to search for.
	 * 
	 * @return Returns the value of the label or an <TT>empty</TT> string, if an
	 *         error occurred.
	 */
	public static String getLabelText(Context externalContext, ResourceContainer label, int number)
	{
		// Validate parameter
		if (label == null) return "";
		if (number < 0) return "";

		// Create a specific label context
		Context labelContext = externalContext.createTrackingContext(ResourceManager.context(externalContext, "Label", "Processing"));

		// Get default values
		String identifier = label.getRecourceIdentifier();
		String labelText = "";

		try
		{
			// Search for the label text of the "ApplicationMain Language"
			String mainLanguage = labelContext.getApplicationManager().getMainLanguage();
			String secondaryLanguage = labelContext.getApplicationManager().getSecondaryLanguage();
			labelText = label.getAttributeValue(labelContext, number, mainLanguage);

			if (labelText == null || labelText.length() == 0)
			{
				// The event is only to be notified once
				if (labelContext.getApplicationManager().isNotifyLabelEvents())
				{
					if (labelContext.getLabelManager().getMainLanguageValueNotFoundProperties().get(identifier) == null)
					{
						labelContext.getLabelManager().getMainLanguageValueNotFoundProperties().put(identifier, "");

						String additionalText = "--> No label text found for the main language '" + mainLanguage + "'.";
						additionalText += "\n--> Trying to get the label text of the secondary language '" + secondaryLanguage + "'.";
						additionalText += "\n--> Label identifier: '" + identifier + "'";
						labelContext.getNotificationManager().notifyEvent(labelContext, ResourceManager.notification(labelContext, "Label", "LabelNotDefined"), additionalText, null);
					}
				}
			}
			else
			{
				// Notify WATCHDOG
				String additionalText = "--> Searched for 'Value." + String.valueOf(number) + "." + mainLanguage + "' (main language)";
				labelContext.getLabelManager().notifyWatchdog(labelContext, identifier, labelText, additionalText);

				// Return
				return labelText;
			}

			// Search for the label text of the "Secondary Language"
			labelText = label.getAttributeValue(labelContext, number, secondaryLanguage);

			if (labelText == null || labelText.length() == 0)
			{
				// The event is only to be notified once
				if (labelContext.getApplicationManager().isNotifyLabelEvents())
				{
					if (labelContext.getLabelManager().getSecondaryLanguageValueNotFound().get(identifier) == null)
					{
						labelContext.getLabelManager().getSecondaryLanguageValueNotFound().put(identifier, "");

						String additionalText = "--> No label text found of the secondary language '" + secondaryLanguage + "'.";
						additionalText += "\n--> Trying to get the value of the resource label definition.";
						additionalText += "\n--> Label identifier: '" + identifier + "'";
						labelContext.getNotificationManager().notifyEvent(labelContext, ResourceManager.notification(labelContext, "Label", "LabelNotDefined"), additionalText, null);
					}
				}
			}
			else
			{
				// Notify WATCHDOG
				String additionalText = "--> ApplicationMain language 'Value." + String.valueOf(number) + "." + mainLanguage + "' not found";
				additionalText += "\n--> Searched for 'Value." + String.valueOf(number) + "." + secondaryLanguage + "' (secondary language, fallback)";
				labelContext.getLabelManager().notifyWatchdog(labelContext, identifier, labelText, additionalText);

				// Return
				return labelText;
			}

			// Search for the label text of the resource definition
			labelText = label.getAttributeValue(labelContext, number, null);

			if (labelText == null || labelText.length() == 0)
			{
				// The event is only to be notified once
				if (labelContext.getApplicationManager().isNotifyLabelEvents())
				{
					if (labelContext.getLabelManager().getResourceValueNotFoundProperties().get(identifier) == null)
					{
						labelContext.getLabelManager().getResourceValueNotFoundProperties().put(identifier, "");

						String additionalText = "--> No label value found in the label resource file.";
						additionalText += "\n--> Trying to use the 'Group' and 'Name' part of the label identifier.";
						additionalText += "\n--> Label identifier: '" + identifier + "'";
						labelContext.getNotificationManager().notifyEvent(labelContext, ResourceManager.notification(labelContext, "Label", "LabelNotDefined"), additionalText, null);
					}
				}
			}
			else
			{
				// Notify WATCHDOG
				String additionalText = "--> ApplicationMain language 'Value." + String.valueOf(number) + "." + mainLanguage + "' not found";
				additionalText += "\n--> Secondary language 'Value." + String.valueOf(number) + "." + secondaryLanguage + "' not found";
				additionalText += "\n--> Searched for 'Value." + String.valueOf(number) + "'" + " (defining resource, fallback)";
				labelContext.getLabelManager().notifyWatchdog(labelContext, identifier, labelText, additionalText);

				// Return
				return labelText;
			}

			// Get the 'Group' and 'Name' part of the label identifier
			labelText = label.getGroup() + " " + label.getName();
			return labelText;
		}
		catch (Exception e)
		{
			// The event is only to be notified once
			if (labelContext.getLabelManager().getExceptionProperties().get(identifier) == null)
			{
				labelContext.getLabelManager().getExceptionProperties().put(identifier, "");

				String additionalText = "--> Error on processing labels";
				additionalText += "\n--> Label identifier: '" + identifier + "'";
				labelContext.getNotificationManager().notifyEvent(labelContext, ResourceManager.notification(labelContext, "Label", "LabelNotDefined"), additionalText, e);
				return null;
			}
		}

		// Return
		return labelText;
	}

	/**
	 * Notify the WATCHDOG about label access.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param resourceIdentifier
	 *            The resource identifier of the label item.
	 * 
	 * @param value
	 *            The value that was read.
	 */
	private void notifyWatchdog(Context context, String identifier, String value, String additionalTextParameter)
	{
		try
		{
			// Set message Text
			String messageText = "Label item was read";

			// Set additional text
			String additionalText = "--> Label item was read";
			additionalText += "\n" + additionalTextParameter;
			additionalText += "\n--> Identifier: '" + identifier + "'";
			additionalText += "\n--> Value: '" + ResourceManager.hideSecurityValue(context, identifier, value) + "'";

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
	 * Composes the path of the data directory of template label files.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the directory were the data files are stored.
	 * 
	 */
	private String getLabelTemplateFilePath(Context context)
	{
		return FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getResourceLabelTemplateSubPath());
	}

	/**
	 * Composes the file name of template label files.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param application
	 *            The application to consider.
	 * 
	 * @param language
	 *            The language to consider.
	 * 
	 * @return Returns the file name of the data files, or <TT>null</TT>.
	 * 
	 */
	private String getLabelTemplateFileName(Context context, String application, String language)
	{
		try
		{
			String fileName = FileLocationFunctions.getResourceLabelTemplateFileName();
			fileName = FileLocationFunctions.replacePlaceholder(context, fileName, application, language);
			return fileName;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Creates all template label files for an application.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param application
	 *            The application to consider.
	 * 
	 * @return Returns <TT>true</TT> if the label template files could be
	 *         created, otherwise <TT>false</TT>.
	 */
	public boolean createTemplateLabelFiles(Context context, String application, int version)
	{
		// Initialization
		boolean isSuccessful = true;

		// Write all label template files
		try
		{
			for (String destinationLanguage : context.getApplicationManager().getSupportedLanguages().values())
			{
				if (this.createTemplateLabelFile(context, ApplicationManager.ApplicationIdentifierEnum.Basic.toString(), version, destinationLanguage) == false) isSuccessful = false;
				if (this.createTemplateLabelFile(context, ApplicationManager.ApplicationIdentifierEnum.Common.toString(), version, destinationLanguage) == false) isSuccessful = false;
				if (this.createTemplateLabelFile(context, application, version, destinationLanguage) == false) isSuccessful = false;
				if (this.createTemplateLabelFile(context, ApplicationManager.ApplicationIdentifierEnum.Extension.toString(), version, destinationLanguage) == false) isSuccessful = false;
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
	 * Creates a template label file for a specific application and language.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param application
	 *            The application to consider.
	 * 
	 * @param sourceLanguage
	 *            The source language to consider.
	 * 
	 * @param destinationLanguage
	 *            The destination language to consider.
	 * 
	 * @return Returns <TT>true</TT> if the label template file could be
	 *         created, otherwise <TT>false</TT>.
	 */
	private synchronized boolean createTemplateLabelFile(Context context, String application, int version, String destinationLanguage)
	{
		// Check parameter
		boolean isError = false;
		if (application == null || application.length() == 0) isError = true;
		if (destinationLanguage == null || destinationLanguage.length() == 0) isError = true;

		// Define variables
		String sourceLanguage = context.getResourceManager().getCommonResourceFileLanguage(context, application);

		if (sourceLanguage == null || sourceLanguage.length() == 0 || (context.getApplicationManager().getSupportedLanguages().get(sourceLanguage) == null))
		{
			String errorString = "--> Language confusion, language is not supported";
			errorString += "\n--> Searched for language: '" + sourceLanguage + "'";
			errorString += "\n--> Please look at the resource file of the application: '" + application + "' in the sub directory '" + FileLocationFunctions.getResourceSubPath() + "'";
			errorString += "\n--> Supported languages are: " + context.getApplicationManager().getSupportedLanguagesString();
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Label", "ErrorOnWritingTemplateFile"), errorString, null);
			isError = true;
		}

		if (isError == true)
		{
			context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "Label", "ErrorOnWritingTemplateFile"), "--> Parameter missing: Application [" + application + "], Source Language [" + sourceLanguage + "], Destination Language [" + destinationLanguage + "]", null);
			return false;
		}

		// Define variables
		String fileName = null;
		PrintWriter output;
		String pathName = null;

		// Create file directory for template files
		try
		{
			pathName = this.getLabelTemplateFilePath(context);

			File directory = new File(pathName);
			directory.mkdirs();
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "Label", "ErrorOnWritingLabelTemplateFile"), "--> Path name: " + pathName, e);
			return false;
		}

		// Write text to the template file
		try
		{
			// Get a list of all relevant resource identifier items
			fileName = FileLocationFunctions.compileFilePath(this.getLabelTemplateFilePath(context), this.getLabelTemplateFileName(context, application, destinationLanguage));

			String typeCriteria[] = { "Label" };
			String applicationCriteria[] = { application };
			String originCriteria[] = null;
			String usageCriteria[] = null;
			String groupCriteria[] = null;
			List<String> resourceIdentifierList = context.getResourceManager().getResourceIdentifierList(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

			// Open file
			output = new PrintWriter(new FileOutputStream(new File(fileName), false));

			// Write common attributes first
			String commonAttributes = ResourceManager.getCommonAttributeList(context, application, version, destinationLanguage, null);

			if (commonAttributes != null && commonAttributes.length() > 0)
			{
				output.append(FileUtilFunctions.generalNormalizeNewLine(commonAttributes));
			}

			// Go through the list and get the manual for each resource item
			// List all items
			Iterator<String> iterIdentifier = resourceIdentifierList.iterator();

			while (iterIdentifier.hasNext())
			{
				// Get actual resource
				String identifier = iterIdentifier.next();
				ResourceContainer label = context.getResourceManager().getResource(context, identifier);

				// Generate default template text
				String resourceTemplateText = label.printTemplate(context, false);

				// Generate all values
				for (int number = 0; number < 100; number++)
				{
					// Checking if the "Value.x" attribute is defined in the
					// label resource file
					String labelGeneralResourceText = label.getAttributeValue(context, number, null);
					if (labelGeneralResourceText == null || labelGeneralResourceText.length() == 0) break;

					// Add source language value
					String text = label.getAttributeValue(context, number, null);
					if (text == null) text = "";
					
					String manualText = "";
					
					if (number == 0)
					{
						manualText += "# Value: \"" + text.trim() + "\"" + "\n";
					}
					else
					{
						manualText += "# Value " + String.valueOf(number) + ": \"" + text.trim() + "\"" + "\n";
					}
					
					manualText += identifier + ResourceManager.getAttributeDelimiterString() + label.getAttributeResourceIdentifier(context, number, sourceLanguage) + ResourceManager.getAttributeSkipString() + "=" + text.trim() + "\n";

					// Add destination language value
					text = label.getAttributeValue(context, number, destinationLanguage);
					if (text == null) text = "";
					manualText += identifier + ResourceManager.getAttributeDelimiterString() + label.getAttributeResourceIdentifier(context, number, destinationLanguage) + "=" + text.trim() + "\n\n";

					resourceTemplateText += FileUtilFunctions.generalNormalizeNewLine(manualText);
				}

				output.append(resourceTemplateText);
			}

			// Close file
			output.close();
		}
		catch (Exception e)
		{
			context.getNotificationManager().notifyEvent(context, ResourceManager.notification(context, "Label", "ErrorOnWritingLabelTemplateFile"), "--> File name: " + fileName, e);
		}

		// Return
		return true;
	}

	/**
	 * Getter
	 */
	private HashMap<String, String> getMainLanguageValueNotFoundProperties()
	{
		return mainLanguageValueNotFoundProperties;
	}

	/**
	 * Getter
	 */
	private HashMap<String, String> getSecondaryLanguageValueNotFound()
	{
		return secondaryLanguageValueNotFound;
	}

	/**
	 * Getter
	 */
	private HashMap<String, String> getResourceValueNotFoundProperties()
	{
		return resourceValueNotFoundProperties;
	}

	/**
	 * Getter
	 */
	private HashMap<String, String> getExceptionProperties()
	{
		return exceptionProperties;
	}
}
