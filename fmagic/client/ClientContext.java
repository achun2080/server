package fmagic.client;

import fmagic.basic.ApplicationManager;
import fmagic.basic.Context;
import fmagic.basic.ResourceContainer;
import fmagic.test.TestManagerClient;

/**
 * This class contains common context functions used by all client applications.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class ClientContext extends Context
{
	/**
	 * Constructor
	 * 
	 * @param codeName
	 *            Code name of the application.
	 * 
	 * @param applicationName
	 *            The name of the application.
	 * 
	 * @param applicationVersion
	 *            Software version of the application.
	 * 
	 * @param applicationManager
	 *            The instance of the application manager that is to be assigned
	 *            to the context.
	 * 
	 * @param mediaManager
	 *            The instance of the media manager that is to be assigned
	 *            to the context.
	 * 
	 * @param testManager
	 *            The instance of the test manager that is to be assigned
	 *            to the context.
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
	public ClientContext(String codeName, String applicationName,
			int applicationVersion, ApplicationManager applicationManager,
			boolean runningInTestMode, String testCaseName, String testSessionName)
	{
		super(codeName, applicationName, applicationVersion, ResourceContainer.OriginEnum.Client.toString(), applicationManager, new ClientMediaManager(), new TestManagerClient(), runningInTestMode, testCaseName, testSessionName);
	}

	@Override
	public boolean validateResources(Context context)
	{
		boolean isError = super.validateResources(context);
		return isError;
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
