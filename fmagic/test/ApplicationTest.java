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
public class ApplicationTest {

	/**
	 * main() function for invoking all tests
	 */
	public static void mainXXX(String[] args) {
		EncodingHandler encodingHandler = new EncodingHandler();
		KeyPair keyPair = encodingHandler.getPublicPrivateKeyPair();

		System.out.println(Base64.encodeBase64String(keyPair.getPublic()
				.getEncoded()));
		System.out.println(Base64.encodeBase64String(keyPair.getPrivate()
				.getEncoded()));
	}

	/**
	 * main() function for invoking all tests
	 */
	public static void main(String[] args) {
		// Start Application server
		ServerManager server = null;

		try {
			server = ServerSeniorCitizen.getInstance("ap1", 8090, 1000000);

			if (server != null)
				server.startApplication();

			if (server != null) {
				// Test preparing
				Context context = server.getContext();

				ResourceContainer licenseItemProvisionRate = ResourceManager
						.license(context, "Service", "ProvisionRate");

				ResourceContainer licenseModelStarter = ResourceManager
						.license(context, "SellerProfile", "Starter");

				// Right test 1
				String documentation = licenseItemProvisionRate
						.printManual(context);

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
				documentation = context
						.getLicenseManager()
						.printDistributionConfiguration(
								context.getLicenseManager()
										.getAssignedLicenseItemToLicenseModel());

				System.out.println("");
				System.out.println("333333333333333333333333333333");
				System.out.println(documentation);
				System.out.println("333333333333333333333333333333");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Start client
		ClientManager client;

		try {
			if (server != null) {
				// Create client
				client = ClientSeniorCitizen.getInstance("cl1");

				if (client != null) {
					// Start client
					client.startApplication();
					client.setSocketConnectionParameter("localhost", 8090,
							1000000);

					// Do something
					ClientCommand command;
					ResponseContainer responseContainer;

					// COMMAND Create Session 1
					command = new ClientCommandCreateSession(
							client.getContext(), client);
					responseContainer = command.execute();

					// COMMAND Handshake 1
					command = new ClientCommandHandshake(client.getContext(),
							client);
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
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Shutdown Application server
		try {
			if (server != null) {
				// Test preparing
				Context context = server.getContext();

				ResourceContainer rightItemUpdate = ResourceManager.right(
						context, "ArticleData", "Update");
				ResourceContainer rightItemProcess = ResourceManager.right(
						context, "ArticleData", "Process");
				ResourceContainer rightItemRead = ResourceManager.right(
						context, "ArticleData", "Read");
				ResourceContainer rightItemLogging = ResourceManager.right(
						context, "ArticleData", "Logging");

				ResourceContainer rightGroup = ResourceManager.right(context,
						"UserProfile", "Manager");

				SessionContainer sessionContainer = server
						.sessionGetClientSession("1364210073453");
				sessionContainer.setUserRightGroup(rightGroup);
				context.setServerSession(sessionContainer);

				// Right test 1
				boolean isGranted = RightManager.hasRight(context,
						rightItemUpdate);

				System.out.println("");
				System.out.println("111111111111111111111111111111");
				System.out.println("RightManager.hasRight() Granted: "
						+ String.valueOf(isGranted));
				System.out.println("111111111111111111111111111111");

				// Right test 2
				isGranted = RightManager.hasRightCombinationAnd(context,
						rightItemUpdate, rightItemProcess, rightItemRead);

				System.out.println("");
				System.out.println("222222222222222222222222222222");
				System.out
						.println("RightManager.hasRightCombinationAnd() Granted: "
								+ String.valueOf(isGranted));
				System.out.println("222222222222222222222222222222");

				// Right test 3
				List<ResourceContainer> clAnd = new ArrayList<ResourceContainer>();
				clAnd.add(rightItemUpdate);
				clAnd.add(rightItemProcess);
				clAnd.add(rightItemRead);

				isGranted = RightManager.hasRightCombinationAnd(context, clAnd);

				System.out.println("");
				System.out.println("333333333333333333333333333333");
				System.out
						.println("RightManager.hasRightCombinationAnd() Granted: "
								+ String.valueOf(isGranted));
				System.out.println("333333333333333333333333333333");

				// Right test 4
				isGranted = RightManager.hasRightCombinationOr(context,
						rightItemUpdate, rightItemLogging);

				System.out.println("");
				System.out.println("444444444444444444444444444444");
				System.out
						.println("RightManager.hasRightCombinationOr() Granted: "
								+ String.valueOf(isGranted));
				System.out.println("444444444444444444444444444444");

				// Right test 5
				List<ResourceContainer> clOr = new ArrayList<ResourceContainer>();
				clOr.add(rightItemUpdate);
				clOr.add(rightItemLogging);

				isGranted = RightManager.hasRightCombinationOr(context, clOr);

				System.out.println("");
				System.out.println("5555555555555555555555555555555");
				System.out
						.println("RightManager.hasRightCombinationOr() Granted: "
								+ String.valueOf(isGranted));
				System.out.println("5555555555555555555555555555555");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			// Stop server
			Util.sleepSeconds(1);
			server.stopApplication();
		}
		
		// Stage Test
		// and number 2
	}
}
