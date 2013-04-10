package fmagic.basic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileLocationManager
{
	// Root path set for testing purposes only.
	private static String rootPath = "E:/Gewerbe/Fmagic/Workspaces/fmagic/AppsServer/src";

	private static final String resourceSubPath = "configuration/fmagic.resource";
	private static final String resourceFileName = "fmagic.resource.${application}.properties";

	private static final String resourceLabelSubPath = "configuration/fmagic.resource";
	private static final String resourceLabelFileName = "fmagic.label.${application}.properties";

	private static final String resourceLabelTranslatedSubPath = "configuration/fmagic.label";
	private static final String resourceLabelTranslatedFileName = "fmagic.label.${application}.${language}.properties";

	private static final String resourceLabelTemplateSubPath = "configuration/fmagic.label/templates";
	private static final String resourceLabelTemplateFileName = "fmagic.template.label.${application}.${language}.properties";

	private static final String configurationSubPath = "configuration/fmagic.configuration";
	private static final String configurationFileName = "fmagic.${codename}.configuration.properties";

	private static final String configurationDefaultSubPath = "configuration/fmagic.configuration";
	private static final String configurationDefaultFileName = "fmagic.default.configuration.properties";

	private static final String configurationTemplateSubPath = "configuration/fmagic.configuration/templates";
	private static final String configurationTemplateFileName = "fmagic.template.configuration.${application}.${origin}.properties";

	private static final String localdataSubPath = "configuration/fmagic.localdata";
	private static final String localdataFileName = "fmagic.${codename}.localdata.properties";

	private static final String logContextSubPath = "fmagic.logging/fmagic.logging.context";
	private static final String logContextSubSubPath = "${sdate}";
	private static final String logContextFileName = "fmagic-${origin}-${codename}-${ndate}-${context}.log";

	private static final String logApplicationSubPath = "fmagic.logging/fmagic.logging.application";
	private static final String logApplicationSubSubPath = "${codename}/${sdate}";
	private static final String logApplicationFileName = "fmagic-${codename}-${ndate}.log";

	private static final String logFlatSubPath = "configuration/fmagic.logging/fmagic.logging.flat";
	private static final String logFlatFileName = "fmagic-flat-${ndate}.log";

	private static final String logTicketSubPath = "configuration/fmagic.logging/fmagic.logging.tickets";
	private static final String logTicketSubSubPath = "${sdate}";
	private static final String logTicketComposer = "fmagic-${codename}-${context}-${timestamp}-[${thread}]";
	private static final String logTicketFileType = ".log";

	private static final String licenseSubPath = "configuration/fmagic.license";
	private static final String licenseFileName = "fmagic.license.${application}.${licensekey}.license";
	private static final String licenseFileNameType = "license";

	private static final String licenseTemplateSubPath = "configuration/fmagic.license/templates";
	private static final String licenseTemplateFileName = "fmagic.template.license.${application}.${licensemodel}.properties";

	private static final String mediaFileName = "${application}-${alias}-${identifier}-${encodingkey}-${hashvalue}.${filetype}";
	private static final String mediaPendingFileName = "${timestamp}-${application}-${codename}-[${thread}].${filetype}";
	private static final String mediaPendingSubPath = "pending";
	private static final String mediaDeletedFileName = "${originalname}-${timestamp}-[${thread}].${filetype}";
	private static final String mediaDeletedSubPath = "deleted";

	private static final String testLoggingSubPath = "test/${testcasename}/fmagic.logging";
	private static final String testLoggingSubSubPath = "${application}-${origin}-${codename}-${testticket}";
	private static final String testLoggingLogFileName = "LOGGING-${application}-${origin}-${codename}-[${thread}].log";
	private static final String testLoggingAssertFileName = "ASSERT-${application}-${origin}-${codename}-[${thread}].log";

	private static final String testResourceSubPath = "test/${testcasename}/fmagic.resource";
	private static final String testStuffSubPath = "test/${testcasename}/fmagic.stuff";
	private static final String testConfigurationSubPath = "test/${testcasename}/fmagic.configuration";
	private static final String testLicenseSubPath = "test/${testcasename}/fmagic.license";

	/**
	 * Get the delimiter character that divides parts of path elements.
	 * 
	 * @return Returns always with <TT>|</TT>.
	 */
	public static String getPathElementDelimiterString()
	{
		return "/";
	}

	/**
	 * Replace place holder.
	 * 
	 * @param context
	 *            Context to use.
	 * 
	 * @param sourceString
	 *            Input string that contains place holder.
	 * 
	 * @param applicationName
	 *            The name of the application from the point of view of the
	 *            resource files, including Basic, Common and Extension, or
	 *            <TT>null</TT> if the real application name is to use.
	 * 
	 * @return Returns the replaced string.
	 * 
	 */
	public static String replacePlaceholder(Context context, String sourceString, String applicationName, String language)
	{
		// Check parameter
		if (sourceString == null) return null;

		// Initialize variables
		String timestampString = "";
		String dateNumericString = "";
		String dateSeparatedString = "";

		String resultString = sourceString;

		// Get date and time strings
		try
		{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
			dateNumericString = simpleDateFormat.format(new Date());

			simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			dateSeparatedString = simpleDateFormat.format(new Date());

			Date messageDate = new Date();
			simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.getDefault());
			timestampString = simpleDateFormat.format(messageDate);

			// Replace place holder in file name
			resultString = resultString.replace("${codename}", context.getCodeName().toLowerCase());
			resultString = resultString.replace("${ndate}", dateNumericString.toLowerCase());
			resultString = resultString.replace("${sdate}", dateSeparatedString.toLowerCase());
			resultString = resultString.replace("${date}", dateSeparatedString.toLowerCase());
			resultString = resultString.replace("${timestamp}", timestampString.toLowerCase());
			resultString = resultString.replace("${context}", context.getContextResourceContainer().getAliasName().toLowerCase());
			resultString = resultString.replace("${thread}", String.format("%04d", Thread.currentThread().getId()));
			resultString = resultString.replace("${origin}", context.getOriginName().toLowerCase());
			resultString = resultString.replace("${testticket}", context.getTicketNumberOfTest().toLowerCase());
			resultString = resultString.replace("${testcasename}", context.getTestCaseName().toLowerCase());

			if (language != null && language.length() > 0)
			{
				resultString = resultString.replace("${language}", language.trim().toLowerCase());
			}

			if (applicationName == null || applicationName.length() == 0)
			{
				resultString = resultString.replace("${application}", context.getApplicationName().toLowerCase());
			}
			else
			{
				resultString = resultString.replace("${application}", applicationName.toLowerCase());
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
	 * Replace place holder.
	 * 
	 * @param context
	 *            Context to use.
	 * 
	 * @param sourceString
	 *            Input string that contains place holder.
	 * 
	 * @return Returns the replaced string.
	 * 
	 */
	public static String replacePlacholder(Context context, String sourceString)
	{
		return replacePlaceholder(context, sourceString, null, null);
	}

	/**
	 * Getter
	 */
	public static String getRootPath()
	{
		return rootPath;
	}

	/**
	 * Getter
	 */
	public static String getResourceSubPath()
	{
		return resourceSubPath;
	}

	/**
	 * Getter
	 */
	public static String getConfigurationSubPath()
	{
		return configurationSubPath;
	}

	/**
	 * Getter
	 */
	public static String getLocaldataSubPath()
	{
		return localdataSubPath;
	}

	/**
	 * Getter
	 */
	public static String getLogContextSubPath()
	{
		return logContextSubPath;
	}

	/**
	 * Getter
	 */
	public static String getLogApplicationSubPath()
	{
		return logApplicationSubPath;
	}

	/**
	 * Getter
	 */
	public static String getLogFlatSubPath()
	{
		return logFlatSubPath;
	}

	/**
	 * Getter
	 */
	public static String getLogTicketSubPath()
	{
		return logTicketSubPath;
	}

	/**
	 * Getter
	 */
	public static String getLocaldataFileName()
	{
		return localdataFileName;
	}

	/**
	 * Getter
	 */
	public static String getLogContextFileName()
	{
		return logContextFileName;
	}

	/**
	 * Getter
	 */
	public static String getLogApplicationFileName()
	{
		return logApplicationFileName;
	}

	/**
	 * Getter
	 */
	public static String getLogFlatFileName()
	{
		return logFlatFileName;
	}

	/**
	 * Getter
	 */
	public static String getLogTicketComposer()
	{
		return logTicketComposer;
	}

	/**
	 * Getter
	 */
	public static String getLogTicketFileType()
	{
		return logTicketFileType;
	}

	/**
	 * Getter
	 */
	public static String getConfigurationTemplateSubPath()
	{
		return configurationTemplateSubPath;
	}

	/**
	 * Getter
	 */
	public static String getConfigurationTemplateFileName()
	{
		return configurationTemplateFileName;
	}

	/**
	 * Getter
	 */
	public static String getResourceFileName()
	{
		return resourceFileName;
	}

	/**
	 * Getter
	 */
	public static String getConfigurationFileName()
	{
		return configurationFileName;
	}

	/**
	 * Getter
	 */
	public static String getConfigurationDefaultSubPath()
	{
		return configurationDefaultSubPath;
	}

	/**
	 * Getter
	 */
	public static String getConfigurationDefaultFileName()
	{
		return configurationDefaultFileName;
	}

	/**
	 * Getter
	 */
	public static String getLogContextSubSubPath()
	{
		return logContextSubSubPath;
	}

	/**
	 * Getter
	 */
	public static String getLogApplicationSubSubPath()
	{
		return logApplicationSubSubPath;
	}

	/**
	 * Getter
	 */
	public static String getLogTicketSubSubPath()
	{
		return logTicketSubSubPath;
	}

	/**
	 * Getter
	 */
	public static String getResourceLabelSubPath()
	{
		return resourceLabelSubPath;
	}

	/**
	 * Getter
	 */
	public static String getResourceLabelFileName()
	{
		return resourceLabelFileName;
	}

	/**
	 * Getter
	 */
	public static String getResourceLabelTranslatedSubPath()
	{
		return resourceLabelTranslatedSubPath;
	}

	/**
	 * Getter
	 */
	public static String getResourceLabelTranslatedFileName()
	{
		return resourceLabelTranslatedFileName;
	}

	/**
	 * Getter
	 */
	public static String getResourceLabelTemplateSubPath()
	{
		return resourceLabelTemplateSubPath;
	}

	/**
	 * Getter
	 */
	public static String getResourceLabelTemplateFileName()
	{
		return resourceLabelTemplateFileName;
	}

	/**
	 * Getter
	 */
	public static String getLicenseSubPath()
	{
		return licenseSubPath;
	}

	/**
	 * Getter
	 */
	public static String getLicenseFileName()
	{
		return licenseFileName;
	}

	/**
	 * Getter
	 */
	public static String getLicenseFileNameType()
	{
		return licenseFileNameType;
	}

	/**
	 * Getter
	 */
	public static String getLicenseTemplateSubPath()
	{
		return licenseTemplateSubPath;
	}

	/**
	 * Getter
	 */
	public static String getLicenseTemplateFileName()
	{
		return licenseTemplateFileName;
	}

	/**
	 * Getter
	 */
	public static String getMediaFileName()
	{
		return mediaFileName;
	}

	/**
	 * Getter
	 */
	public static String getMediaDeletedSubPath()
	{
		return mediaDeletedSubPath;
	}

	/**
	 * Getter
	 */
	public static String getMediaPendingFileName()
	{
		return mediaPendingFileName;
	}

	/**
	 * Getter
	 */
	public static String getMediaDeletedFileName()
	{
		return mediaDeletedFileName;
	}

	/**
	 * Getter
	 */
	public static String getMediaPendingSubPath()
	{
		return mediaPendingSubPath;
	}

	public static String getTestLoggingSubPath()
	{
		return testLoggingSubPath;
	}

	public static String getTestLoggingSubSubPath()
	{
		return testLoggingSubSubPath;
	}

	public static String getTestLoggingLogFileName()
	{
		return testLoggingLogFileName;
	}

	public static String getTestLoggingAssertFileName()
	{
		return testLoggingAssertFileName;
	}

	public static String getTestResourceSubPath()
	{
		return testResourceSubPath;
	}

	public static String getTestStuffSubPath()
	{
		return testStuffSubPath;
	}

	public static String getTestConfigurationSubPath()
	{
		return testConfigurationSubPath;
	}

	public static String getTestLicenseSubPath()
	{
		return testLicenseSubPath;
	}
}
