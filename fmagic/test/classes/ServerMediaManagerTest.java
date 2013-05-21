package fmagic.test.classes;

import fmagic.basic.command.ConnectionContainer;
import fmagic.basic.context.Context;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.server.media.ServerMediaManager;

/**
 * This class implements a test extension for the server media manager.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 21.05.2013 - Created
 * 
 */
public class ServerMediaManagerTest extends ServerMediaManager
{
	/**
	 * Constructor
	 */
	public ServerMediaManagerTest()
	{
		super();
	}

	/**
	 * Specific test method
	 */
	public int cleanAll(Context context)
	{
		if (!context.isRunningInTestMode()) return 0;
		return super.cleanAll(context);
	}

	/**
	 * Specific test method
	 */
	public int cleanDeletedDirectory(Context context, ResourceContainerMedia mediaResourceContainer, int daysToKeep)
	{
		if (!context.isRunningInTestMode()) return 0;
		return super.cleanDeletedDirectory(context, mediaResourceContainer, daysToKeep);
	}

	/**
	 * Specific test method
	 */
	public int cleanPendingDirectory(Context context, ResourceContainerMedia mediaResourceContainer, int daysToKeep)
	{
		if (!context.isRunningInTestMode()) return 0;
		return super.cleanPendingDirectory(context, mediaResourceContainer, daysToKeep);
	}

	/**
	 * Specific test method
	 */
	public int cleanRegularDirectory(Context context, ResourceContainerMedia mediaResourceContainer, int daysToKeep)
	{
		if (!context.isRunningInTestMode()) return 0;
		return super.cleanRegularDirectory(context, mediaResourceContainer, daysToKeep);
	}

	/**
	 * Specific test method
	 */
	public boolean commandCheckOnServer(Context context, ConnectionContainer connectionContainer, ResourceContainerMedia mediaResourceContainer, String fileType, String dataIdentifier, String hashValue)
	{
		if (!context.isRunningInTestMode()) return false;
		return super.commandCheckOnServer(context, connectionContainer, mediaResourceContainer, fileType, dataIdentifier, hashValue);
	}

	/**
	 * Specific test method
	 */
	public String commandReadOnServer(Context context, ConnectionContainer connectionContainer, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		if (!context.isRunningInTestMode()) return null;
		return super.commandReadOnServer(context, connectionContainer, mediaResourceContainer, dataIdentifier);
	}

	/**
	 * Specific test method
	 */
	public boolean commandUploadToServer(Context context, ConnectionContainer connectionContainer, ResourceContainerMedia mediaResourceContainer, String uploadFileNamePath, String dataIdentifier)
	{
		if (!context.isRunningInTestMode()) return false;
		return super.commandUploadToServer(context, connectionContainer, mediaResourceContainer, uploadFileNamePath, dataIdentifier);
	}

	/**
	 * Specific test method
	 */
	public int getCleanDeletedDaysToKeep()
	{
		return super.getCleanDeletedDaysToKeep();
	}

	/**
	 * Specific test method
	 */
	public int getCleanObsoleteDaysToKeep()
	{
		return super.getCleanObsoleteDaysToKeep();
	}

	/**
	 * Specific test method
	 */
	public int getCleanPendingDaysToKeep()
	{
		return super.getCleanPendingDaysToKeep();
	}

	/**
	 * Specific test method
	 */
	public Integer getMaximumMediaSize()
	{
		return super.getMaximumMediaSize();
	}

	/**
	 * Specific test method
	 */
	public boolean localStoreMediaFile(Context context, ResourceContainerMedia mediaResourceContainer, String uploadFileNamePath, String dataIdentifier)
	{
		if (!context.isRunningInTestMode()) return false;
		return super.localStoreMediaFile( context,  mediaResourceContainer,  uploadFileNamePath,  dataIdentifier);
	}

	/**
	 * Specific test method
	 */
	public void setCleanDeletedDaysToKeep(Context context, int cleanDeletedDaysToKeep)
	{
		if (!context.isRunningInTestMode()) return;
		super.cleanDeletedDaysToKeep = cleanDeletedDaysToKeep;
	}

	/**
	 * Specific test method
	 */
	public void setMaximumMediaSize(Context context, int maximumMediaSize)
	{
		if (!context.isRunningInTestMode()) return;
		super.maximumMediaSize = maximumMediaSize;
	}

	/**
	 * Specific test method
	 */
	public boolean poolCheckMediaFileOnPool(Context context, ResourceContainerMedia mediaResourceContainer, String fileType, String dataIdentifier, String hashValue)
	{
		if (!context.isRunningInTestMode()) return false;
		return super.poolCheckMediaFileOnPool(context, mediaResourceContainer, fileType, dataIdentifier, hashValue);
	}

	/**
	 * Specific test method
	 */
	public String poolReadMediaFileOnPool(Context context, ResourceContainerMedia mediaResourceContainer, String dataIdentifier)
	{
		if (!context.isRunningInTestMode()) return null;
		return super.poolReadMediaFileOnPool(context, mediaResourceContainer, dataIdentifier);
	}

	/**
	 * Specific test method
	 */
	public boolean poolUploadMediaFileToPool(Context context, ResourceContainerMedia mediaResourceContainer, String uploadFileNamePath, String dataIdentifier)
	{
		if (!context.isRunningInTestMode()) return false;
		return super.poolUploadMediaFileToPool(context, mediaResourceContainer, uploadFileNamePath, dataIdentifier);
	}
}
