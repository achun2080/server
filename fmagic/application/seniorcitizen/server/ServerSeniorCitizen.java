package fmagic.application.seniorcitizen.server;

import fmagic.basic.ApplicationManager;
import fmagic.basic.Context;
import fmagic.basic.LicenseManager;
import fmagic.basic.ResourceContainer;
import fmagic.basic.ManagerInterface;
import fmagic.basic.ResourceManager;
import fmagic.basic.RightManager;
import fmagic.server.ServerManager;

/**
 * This class implements the server of the FMAGIC application "Senior Citizen".
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2009 - Created
 * 
 */
public class ServerSeniorCitizen extends ServerManager implements
		ManagerInterface
{
	// Server version
	final static private int serverVersionSeniorCitizen = 1;

	/**
	 * Constructor
	 * 
	 * @param codeName
	 *            Code name of the application.
	 * 
	 * @param serverSocketPort
	 *            Port number of server socket to use by the server application.
	 * 
	 * @param timeoutTimeInMilliseconds
	 *            Timeout to use as a socket connection timeout.
	 */
	private ServerSeniorCitizen(String codeName, int serverSocketPort,
			int timeoutTimeInMilliseconds)
	{
		// Invoke super class
		super(ApplicationManager.ApplicationIdentifierEnum.SeniorCitizen, ServerSeniorCitizen.serverVersionSeniorCitizen, codeName, serverSocketPort, timeoutTimeInMilliseconds);
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = null;
		String applicationCriteria[] = { ApplicationManager.ApplicationIdentifierEnum.SeniorCitizen.toString() };
		String originCriteria[] = { "Server", "All" };
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
		String originCriteria[] = { "Server", "All" };
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
		String originCriteria[] = { "Server", "All" };
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceIdentifierList(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public boolean ckeckOnResourceIdentifierIntegrityError(Context context)
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
		boolean isSuccessful = true;

		ResourceContainer gCustomer = ResourceManager.right(context, "UserProfile", "Customer");
		ResourceContainer gSeller = ResourceManager.right(context, "UserProfile", "Seller");
		ResourceContainer gManager = ResourceManager.right(context, "UserProfile", "Manager");
		ResourceContainer gAdmin = ResourceManager.right(context, "UserProfile", "Administrator");

		ResourceContainer iProcess = ResourceManager.right(context, "ArticleData", "Process");
		ResourceContainer iCreate = ResourceManager.right(context, "ArticleData", "Create");
		ResourceContainer iUpdate = ResourceManager.right(context, "ArticleData", "Update");
		ResourceContainer iRead = ResourceManager.right(context, "ArticleData", "Read");
		ResourceContainer iDelete = ResourceManager.right(context, "ArticleData", "Delete");
		ResourceContainer iOrganize = ResourceManager.right(context, "ArticleData", "Organize");
		ResourceContainer iLogging = ResourceManager.right(context, "ArticleData", "Logging");

		RightManager rightManager = context.getRightManager();

		if (rightManager.addRightItem(context, gCustomer, iProcess) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gCustomer, iRead) == false) isSuccessful = false;

		if (rightManager.addRightItem(context, gSeller, iProcess) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gSeller, iCreate) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gSeller, iUpdate) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gSeller, iRead) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gSeller, iDelete) == false) isSuccessful = false;

		if (rightManager.addRightItem(context, gManager, iProcess) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gManager, iCreate) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gManager, iUpdate) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gManager, iRead) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gManager, iDelete) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gManager, iOrganize) == false) isSuccessful = false;

		if (rightManager.addRightItem(context, gAdmin, iProcess) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gAdmin, iCreate) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gAdmin, iUpdate) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gAdmin, iRead) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gAdmin, iDelete) == false) isSuccessful = false;
		if (rightManager.addRightItem(context, gAdmin, iLogging) == false) isSuccessful = false;
		
		return isSuccessful;
	}

	@Override
	protected boolean assignLicenseModels(Context context)
	{
		boolean isSuccessful = true;

		ResourceContainer mStarter = ResourceManager.license(context, "SellerProfile", "Starter");
		ResourceContainer mPremium = ResourceManager.license(context, "SellerProfile", "Premium");
		ResourceContainer mPower = ResourceManager.license(context, "SellerProfile", "Power");

		ResourceContainer iProvisionRate = ResourceManager.license(context, "Service", "ProvisionRate");
		ResourceContainer iNuOfArticlesInStock = ResourceManager.license(context, "Service", "NuOfArticlesInStock");
		ResourceContainer iBudgetPerMonth = ResourceManager.license(context, "Service", "BudgetPerMonth");
		ResourceContainer iOnlineBilling = ResourceManager.license(context, "Service", "OnlineBilling");
		ResourceContainer iNuOfFreeArticles = ResourceManager.license(context, "Service", "NuOfFreeArticles");
		ResourceContainer iNuOfSoldArticles = ResourceManager.license(context, "Service", "NuOfSoldArticles");
		
		ResourceContainer iSiteName = ResourceManager.license(context, "Organize", "SiteName");
		ResourceContainer iCopyrightText = ResourceManager.license(context, "Organize", "CopyrightText");
		ResourceContainer iApplyFromDate = ResourceManager.license(context, "Organize", "ApplyFromDate");
		ResourceContainer iApplyTooDate = ResourceManager.license(context, "Organize", "ApplyTooDate");

		LicenseManager licenseManager = context.getLicenseManager();

		if (licenseManager.addLicenseItem(context, mStarter, iProvisionRate, 30, 20) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mStarter, iNuOfArticlesInStock, 100) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mStarter, iBudgetPerMonth, 5000) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mStarter, iOnlineBilling, false) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mStarter, iNuOfFreeArticles, 10) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mStarter, iNuOfSoldArticles) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mStarter, iSiteName, "Starter Site") == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mStarter, iCopyrightText, "Copyright 2013") == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mStarter, iApplyFromDate) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mStarter, iApplyTooDate) == false) isSuccessful = false;

		if (licenseManager.addLicenseItem(context, mPremium, iProvisionRate, 20, 10) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPremium, iNuOfArticlesInStock, 1000) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPremium, iBudgetPerMonth, 75000) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPremium, iOnlineBilling, true) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPremium, iNuOfFreeArticles, 50) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPremium, iNuOfSoldArticles) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPremium, iSiteName, "Premium Site") == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPremium, iCopyrightText, "Copyright 2013") == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPremium, iApplyFromDate) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPremium, iApplyTooDate) == false) isSuccessful = false;

		if (licenseManager.addLicenseItem(context, mPower, iProvisionRate, 20, 10) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPower, iNuOfArticlesInStock, -1) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPower, iBudgetPerMonth, -1) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPower, iOnlineBilling, true) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPower, iNuOfFreeArticles, 100) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPower, iNuOfSoldArticles) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPower, iSiteName, "Power Site") == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPower, iCopyrightText, "Copyright 2013") == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPower, iApplyFromDate) == false) isSuccessful = false;
		if (licenseManager.addLicenseItem(context, mPower, iApplyTooDate) == false) isSuccessful = false;

		return isSuccessful;
	}

	/**
	 * Factory method to create an instance of the Senior Citizen SERVER
	 * application.
	 * 
	 * @param codeName
	 *            Code name of the application.
	 * 
	 * @param serverSocketPort
	 *            Port number of server socket to use by the server application.
	 * 
	 * @param timeoutTimeInMilliseconds
	 *            Timeout to use as a socket connection timeout.
	 * 
	 * @return Returns the created instance or <TT>null</TT> if the instance
	 *         couldn't be created. In this case please see the log files or the
	 *         console for further information.
	 */
	public static ServerSeniorCitizen getInstance(String codeName, int serverSocketPort, int timeoutTimeInMilliseconds)
	{
		// Instance will always be build because it is a regular constructor
		ServerSeniorCitizen instance = new ServerSeniorCitizen(codeName, serverSocketPort, serverSocketPort);

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
