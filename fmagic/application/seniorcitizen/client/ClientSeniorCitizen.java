package fmagic.application.seniorcitizen.client;

import fmagic.basic.ApplicationManager;
import fmagic.basic.Context;
import fmagic.client.ClientManager;

/**
 * This class implements the client of the FMAGIC application "Senior Citizen".
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class ClientSeniorCitizen extends ClientManager
{
	// Server version
	final static private int clientVersionSeniorCitizen = 1;

	/**
	 * Constructor
	 * 
	 * @param codeName
	 *            Code name of the application.
	 * 
	 * @param runningInTestMode
	 *            Set to TRUE if the application is running in test mode.
	 * 
	 * @param testCaseName
	 *            Is to be set to the name of the test case, if the application
	 *            is running in test mode, or <TT>null</TT> if the application
	 *            is running in productive mode.
	 */
	private ClientSeniorCitizen(String codeName, boolean runningInTestMode,
			String testCaseName)
	{
		// Invoke super class
		super(ApplicationManager.ApplicationIdentifierEnum.SeniorCitizen, ClientSeniorCitizen.clientVersionSeniorCitizen, codeName, runningInTestMode, testCaseName);
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = null;
		String applicationCriteria[] = { ApplicationManager.ApplicationIdentifierEnum.SeniorCitizen.toString() };
		String originCriteria[] = { "Client", "All" };
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

		String typeCriteria[] = null;
		String applicationCriteria[] = { ApplicationManager.ApplicationIdentifierEnum.SeniorCitizen.toString() };
		String originCriteria[] = { "Client", "All" };
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

		String typeCriteria[] = null;
		String applicationCriteria[] = { ApplicationManager.ApplicationIdentifierEnum.SeniorCitizen.toString() };
		String originCriteria[] = { "Client", "All" };
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
	protected boolean assignRightGroups(Context context)
	{
		return true;
	}

	@Override
	protected boolean assignLicenseModels(Context context)
	{
		return true;
	}

	/**
	 * Factory method to create a <TT>TEST</TT> instance of the Senior Citizen
	 * CLIENT application.
	 * 
	 * @param codeName
	 *            Code name of the application.
	 * 
	 * @param testCaseName
	 *            The name of the test case. This name is fit to a file
	 *            compatible format automatically
	 * 
	 * @return Returns the created instance or <TT>null</TT> if the instance
	 *         couldn't be created. In this case please see the log files or the
	 *         console for further information.
	 */
	public static ClientSeniorCitizen getTestInstance(String codeName, String testCaseName)
	{
		// Instance will always be build because it is a regular constructor
		ClientSeniorCitizen instance = new ClientSeniorCitizen(codeName, true, testCaseName);

		// If there was an error during building the application, the factory
		// method returns wit NULL
		if (instance.isShutdown())
		{
			return null;
		}
		else
		{
			return instance;
		}
	}

	/**
	 * Factory method to create a <TT>PRODUCTIVE</TT> instance of the Senior
	 * Citizen CLIENT application.
	 * 
	 * @param codeName
	 *            Code name of the application.
	 * 
	 * @return Returns the created instance or <TT>null</TT> if the instance
	 *         couldn't be created. In this case please see the log files or the
	 *         console for further information.
	 */
	public static ClientSeniorCitizen getProductiveInstance(String codeName)
	{
		// Instance will always be build because it is a regular constructor
		ClientSeniorCitizen instance = new ClientSeniorCitizen(codeName, false, null);

		// If there was an error during building the application, the factory
		// method returns wit NULL
		if (instance.isShutdown())
		{
			return null;
		}
		else
		{
			return instance;
		}
	}
}
