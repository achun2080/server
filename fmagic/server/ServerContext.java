package fmagic.server;

import fmagic.basic.ApplicationManager;
import fmagic.basic.Context;
import fmagic.basic.ResourceContainer;
import fmagic.test.TestManagerServer;

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
	 */
	public ServerContext(String codeName, String applicationName,
			int applicationVersion, ApplicationManager applicationManager,
			boolean runningInTestMode)
	{
		super(codeName, applicationName, applicationVersion, ResourceContainer.OriginEnum.Server.toString(), applicationManager, new TestManagerServer(), runningInTestMode);
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
}
