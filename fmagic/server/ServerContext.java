package fmagic.server;

import fmagic.basic.ApplicationManager;
import fmagic.basic.Context;
import fmagic.basic.ResourceContainer;

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
			int applicationVersion, ApplicationManager applicationManager)
	{
		super(codeName, applicationName, applicationVersion, ResourceContainer.OriginEnum.Server.toString(), applicationManager);
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
