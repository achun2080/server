package fmagic.server.context;

import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceContainer;
import fmagic.server.application.ServerManager;
import fmagic.server.media.ServerMediaManager;
import fmagic.test.application.TestManagerServer;

/**
 * This class contains a server specific context, extended from the default
 * context.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class ServerContext extends Context
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
	public ServerContext(String codeName, String applicationName,
			int applicationVersion, ServerManager applicationManager,
			boolean runningInTestMode, String testCaseName, String testSessionName)
	{
		super(codeName, applicationName, applicationVersion, ResourceContainer.OriginEnum.Server.toString(), applicationManager, new ServerMediaManager(), new TestManagerServer(), runningInTestMode, testCaseName, testSessionName);
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
