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
	 */
	public ClientContext(String codeName, String applicationName,
			int applicationVersion, ApplicationManager applicationManager,
			boolean runningInTestMode)
	{
		super(codeName, applicationName, applicationVersion, ResourceContainer.OriginEnum.Client.toString(), applicationManager, new TestManagerClient(), runningInTestMode);
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
