package fmagic.client;

import fmagic.basic.ApplicationManager;
import fmagic.basic.Context;
import fmagic.basic.ResourceContainer;

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
			int applicationVersion, ApplicationManager applicationManager)
	{
		super(codeName, applicationName, applicationVersion, ResourceContainer.OriginEnum.Client.toString(), applicationManager);
	}

	@Override
	public boolean ckeckOnResourceIdentifierIntegrityError(Context context)
	{
		boolean isError = super.ckeckOnResourceIdentifierIntegrityError(context);
		return isError;
	}
	
	@Override
	public boolean readConfiguration(Context context)
	{
		return false;
	}
}
