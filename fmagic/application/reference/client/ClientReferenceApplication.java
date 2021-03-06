package fmagic.application.reference.client;

import fmagic.basic.application.ApplicationManager;
import fmagic.basic.context.Context;
import fmagic.client.application.ClientManager;

/**
 * This class implements the client of the FMAGIC application "Reference Application".
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class ClientReferenceApplication extends ClientManager
{
	// Server version
	final static private int clientVersion = 1;

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
	 * 
	 * @param testSessionName
	 *            Is to be set to the name of the test session, if the application
	 *            is running in test mode, or <TT>null</TT> if the application
	 *            is running in productive mode.
	 */
	private ClientReferenceApplication(String codeName, boolean runningInTestMode,
			String testCaseName, String testSessionName)
	{
		// Invoke super class
		super(ApplicationManager.ApplicationIdentifierEnum.ReferenceApplication, ClientReferenceApplication.clientVersion, codeName, runningInTestMode, testCaseName, testSessionName);
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = null;
		String applicationCriteria[] = { ApplicationManager.ApplicationIdentifierEnum.ReferenceApplication.toString() };
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
		String applicationCriteria[] = { ApplicationManager.ApplicationIdentifierEnum.ReferenceApplication.toString() };
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
		String applicationCriteria[] = { ApplicationManager.ApplicationIdentifierEnum.ReferenceApplication.toString() };
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
	public boolean cleanEnvironment(Context context)
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
	public static ClientReferenceApplication getTestInstance(String codeName, String testCaseName, String testSessionName)
	{
		// Instance will always be build because it is a regular constructor
		ClientReferenceApplication instance = new ClientReferenceApplication(codeName, true, testCaseName, testSessionName);
		
		// Initialize application
		instance.initialize();

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
	public static ClientReferenceApplication getProductiveInstance(String codeName)
	{
		// Instance will always be build because it is a regular constructor
		ClientReferenceApplication instance = new ClientReferenceApplication(codeName, false, null, null);
		
		// Initialize application
		instance.initialize();

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
