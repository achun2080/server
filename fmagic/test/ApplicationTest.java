package fmagic.test;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import fmagic.application.seniorcitizen.client.ClientSeniorCitizen;
import fmagic.application.seniorcitizen.server.ServerSeniorCitizen;
import fmagic.basic.Context;
import fmagic.basic.EncodingHandler;
import fmagic.basic.ResourceContainer;
import fmagic.basic.ResourceManager;
import fmagic.basic.ResponseContainer;
import fmagic.basic.RightManager;
import fmagic.basic.SessionContainer;
import fmagic.basic.Util;
import fmagic.client.ClientCommand;
import fmagic.client.ClientCommandCreateSession;
import fmagic.client.ClientCommandHandshake;
import fmagic.client.ClientManager;
import fmagic.server.ServerManager;

/**
 * This class implements integration test against the fmagic applications.
 * 
 * There are two instances to activate:
 * <ul>
 * <li>Application server</li>
 * <li>Client application</li>
 * </ul>
 * 
 * @author F.Wuensche (FW)
 * 
 * @changed AG 27.03.2009 - Created
 * 
 */
public class ApplicationTest
{

	/**
	 * main() function for invoking all tests
	 */
	public static void mainXXX(String[] args)
	{
		EncodingHandler encodingHandler = new EncodingHandler();
		KeyPair keyPair = encodingHandler.getPublicPrivateKeyPair();

		System.out.println(Base64.encodeBase64String(keyPair.getPublic().getEncoded()));
		System.out.println(Base64.encodeBase64String(keyPair.getPrivate().getEncoded()));
	}

	/**
	 * main() function for invoking all tests
	 */
	public static void main(String[] args)
	{
		// Start Application server
		ServerManager server = null;

		try
		{
			server = ServerSeniorCitizen.getInstance("ap1", 8090, 1000000);

			if (server != null) server.startApplication();

			if (server != null)
			{
				// Test preparing
				Context context = server.getContext();

				ResourceContainer licenseItemProvisionRate = ResourceManager.license(context, "Service", "ProvisionRate");

				ResourceContainer licenseModelStarter = ResourceManager.license(context, "SellerProfile", "Starter");

				// Right test 1
				String documentation = licenseItemProvisionRate.printManual(context);

				System.out.println("");
				System.out.println("111111111111111111111111111111");
				System.out.println(documentation);
				System.out.println("111111111111111111111111111111");

				// Right test 2
				documentation = licenseModelStarter.printManual(context);

				System.out.println("");
				System.out.println("222222222222222222222222222222");
				System.out.println(documentation);
				System.out.println("222222222222222222222222222222");

				// Right test 3
				documentation = context.getLicenseManager().printDistributionConfiguration(context.getLicenseManager().getAssignedLicenseItemToLicenseModel());

				System.out.println("");
				System.out.println("333333333333333333333333333333");
				System.out.println(documentation);
				System.out.println("333333333333333333333333333333");

				// Right test 4
				String licenseValue1234 = licenseItemProvisionRate.getAttributeValue(context, 1, "1234");
				String licenseValue5678 = licenseItemProvisionRate.getAttributeValue(context, 1, "5678");

				System.out.println("");
				System.out.println("444444444444444444444444444444");
				System.out.println("License Value 1234 = '" + licenseValue1234);
				System.out.println("License Value 5678 = '" + licenseValue5678);
				System.out.println("444444444444444444444444444444");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// Start client
		ClientManager client;

		try
		{
			if (server != null)
			{
				// Create client
				client = ClientSeniorCitizen.getInstance("cl1");

				if (client != null)
				{
					// Start client
					client.startApplication();
					client.setSocketConnectionParameter("localhost", 8090, 1000000);

					// Do something
					ClientCommand command;
					ResponseContainer responseContainer;

					// COMMAND Create Session 1
					command = new ClientCommandCreateSession(client.getContext(), client);
					responseContainer = command.execute();

					// COMMAND Handshake 1
					command = new ClientCommandHandshake(client.getContext(), client);
					responseContainer = command.execute();

					// COMMAND Handshake 2
					// command = new ClientCommandHandshake(client.getContext(),
					// client);
					// responseContainer = command.execute();

					// COMMAND Create Session 2
					// command = new
					// ClientCommandCreateSession(client.getContext(),
					// client);
					// responseContainer = command.execute();

					// Stop client
					client.stopApplication();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// Shutdown Application server
		try
		{
			if (server != null)
			{
				// Test preparing
				Context context = server.getContext();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{

			// Stop server
			Util.sleepSeconds(1);
			if (server != null) server.stopApplication();
		}

		// Stage Test
		// and number 2
	}
}
