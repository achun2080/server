package fmagic.test;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import fmagic.application.seniorcitizen.client.ClientSeniorCitizen;
import fmagic.application.seniorcitizen.server.ServerSeniorCitizen;
import fmagic.basic.Context;
import fmagic.basic.EncodingHandler;
import fmagic.basic.LicenseManager;
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

				String licenseKey = "1234";
				SessionContainer sessionContainer = server.sessionGetClientSession("1364210073453");
				sessionContainer.setUserLicenseKey(licenseKey);
				context.setServerSession(sessionContainer);

				ResourceContainer licenseItemProvisionRate = ResourceManager.license(context, "Service", "ProvisionRate");

				// Check license: using checkLicense()
				boolean isDefined = context.getLicenseManager().checkLicense(context, licenseKey, licenseItemProvisionRate);

				System.out.println("");
				System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
				System.out.println("Check license: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Granted? '" + isDefined + "'");
				System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

				// Check license: using hasLicense()
				isDefined = LicenseManager.hasLicense(context, licenseItemProvisionRate);

				System.out.println("");
				System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
				System.out.println("Has license: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Granted? '" + isDefined + "'");
				System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");

				// Get license value: using getValue()
				String value = LicenseManager.getValue(context, licenseItemProvisionRate);

				System.out.println("");
				System.out.println("cccccccccccccccccccccccccccccc");
				System.out.println("Get license value 0 as STRING: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Value? '" + value + "'");
				System.out.println("cccccccccccccccccccccccccccccc");

				int valueNumber = 1;
				value = LicenseManager.getValue(context, licenseItemProvisionRate, valueNumber);

				System.out.println("");
				System.out.println("cccccccccccccccccccccccccccccc");
				System.out.println("Get license value " + String.valueOf(valueNumber) + " as STRING: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Value? '" + value + "'");
				System.out.println("cccccccccccccccccccccccccccccc");

				valueNumber = 2;
				value = LicenseManager.getValue(context, licenseItemProvisionRate, valueNumber);

				System.out.println("");
				System.out.println("cccccccccccccccccccccccccccccc");
				System.out.println("Get license value " + String.valueOf(valueNumber) + " as STRING: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Value? '" + value + "'");
				System.out.println("cccccccccccccccccccccccccccccc");

				// Get license value: using getValueAsInteger()
				Integer integerValue = LicenseManager.getValueAsInteger(context, licenseItemProvisionRate);

				System.out.println("");
				System.out.println("dddddddddddddddddddddddddddddd");
				System.out.println("Get license value 0 as INTEGER: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Value? '" + String.valueOf(integerValue) + "'");
				System.out.println("dddddddddddddddddddddddddddddd");

				valueNumber = 1;
				integerValue = LicenseManager.getValueAsInteger(context, licenseItemProvisionRate, valueNumber);

				System.out.println("");
				System.out.println("dddddddddddddddddddddddddddddd");
				System.out.println("Get license value " + String.valueOf(valueNumber) + " as INTEGER: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Value? '" + String.valueOf(integerValue) + "'");
				System.out.println("dddddddddddddddddddddddddddddd");

				valueNumber = 2;
				integerValue = LicenseManager.getValueAsInteger(context, licenseItemProvisionRate, valueNumber);

				System.out.println("");
				System.out.println("dddddddddddddddddddddddddddddd");
				System.out.println("Get license value " + String.valueOf(valueNumber) + " as INTEGER: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Value? '" + String.valueOf(integerValue) + "'");
				System.out.println("dddddddddddddddddddddddddddddd");

				// Get license value: using getValueAsBoolean()
				Boolean booleanValue = LicenseManager.getValueAsBoolean(context, licenseItemProvisionRate);

				System.out.println("");
				System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
				System.out.println("Get license value 0 as BOOLEAN: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Value? '" + String.valueOf(booleanValue) + "'");
				System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeeee");

				valueNumber = 1;
				booleanValue = LicenseManager.getValueAsBoolean(context, licenseItemProvisionRate, valueNumber);

				System.out.println("");
				System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
				System.out.println("Get license value " + String.valueOf(valueNumber) + " as BOOLEAN: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Value? '" + String.valueOf(booleanValue) + "'");
				System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeeee");

				valueNumber = 2;
				booleanValue = LicenseManager.getValueAsBoolean(context, licenseItemProvisionRate, valueNumber);

				System.out.println("");
				System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
				System.out.println("Get license value " + String.valueOf(valueNumber) + " as BOOLEAN: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Value? '" + String.valueOf(booleanValue) + "'");
				System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeeee");

				// Compare license value: using isGreaterThan()
				int valueToCompareWith = 10;
				booleanValue = LicenseManager.isGreaterThan(context, licenseItemProvisionRate, valueToCompareWith);

				System.out.println("");
				System.out.println("ffffffffffffffffffffffffffffff");
				System.out.println("Compare license value 0 to " + String.valueOf(valueToCompareWith) + ": '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is greater then? '" + String.valueOf(booleanValue) + "'");
				System.out.println("ffffffffffffffffffffffffffffff");

				valueNumber = 1;
				booleanValue = LicenseManager.isGreaterThan(context, licenseItemProvisionRate, valueToCompareWith, valueNumber);

				System.out.println("");
				System.out.println("ffffffffffffffffffffffffffffff");
				System.out.println("Compare license value " + String.valueOf(valueNumber) + " to " + String.valueOf(valueToCompareWith) + ": '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is greater then? '" + String.valueOf(booleanValue) + "'");
				System.out.println("ffffffffffffffffffffffffffffff");

				valueNumber = 2;
				booleanValue = LicenseManager.isGreaterThan(context, licenseItemProvisionRate, valueToCompareWith, valueNumber);

				System.out.println("");
				System.out.println("ffffffffffffffffffffffffffffff");
				System.out.println("Compare license value " + String.valueOf(valueNumber) + " to " + String.valueOf(valueToCompareWith) + ": '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is greater then? '" + String.valueOf(booleanValue) + "'");
				System.out.println("ffffffffffffffffffffffffffffff");

				// Compare license value: using isLowerThan()
				valueToCompareWith = 10;
				booleanValue = LicenseManager.isLowerThan(context, licenseItemProvisionRate, valueToCompareWith);

				System.out.println("");
				System.out.println("gggggggggggggggggggggggggggggg");
				System.out.println("Compare license value 0 to " + String.valueOf(valueToCompareWith) + ": '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is lower then? '" + String.valueOf(booleanValue) + "'");
				System.out.println("gggggggggggggggggggggggggggggg");

				valueNumber = 1;
				booleanValue = LicenseManager.isLowerThan(context, licenseItemProvisionRate, valueToCompareWith, valueNumber);

				System.out.println("");
				System.out.println("gggggggggggggggggggggggggggggg");
				System.out.println("Compare license value " + String.valueOf(valueNumber) + " to " + String.valueOf(valueToCompareWith) + ": '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is lower then? '" + String.valueOf(booleanValue) + "'");
				System.out.println("gggggggggggggggggggggggggggggg");

				valueNumber = 2;
				booleanValue = LicenseManager.isLowerThan(context, licenseItemProvisionRate, valueToCompareWith, valueNumber);

				System.out.println("");
				System.out.println("gggggggggggggggggggggggggggggg");
				System.out.println("Compare license value " + String.valueOf(valueNumber) + " to " + String.valueOf(valueToCompareWith) + ": '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is lower then? '" + String.valueOf(booleanValue) + "'");
				System.out.println("gggggggggggggggggggggggggggggg");

				// Compare license value: using isEqual()
				valueToCompareWith = 10;
				booleanValue = LicenseManager.isEqual(context, licenseItemProvisionRate, valueToCompareWith);

				System.out.println("");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
				System.out.println("Compare license value 0 to " + String.valueOf(valueToCompareWith) + ": '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is equal? '" + String.valueOf(booleanValue) + "'");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");

				valueNumber = 1;
				booleanValue = LicenseManager.isEqual(context, licenseItemProvisionRate, valueToCompareWith, valueNumber);

				System.out.println("");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
				System.out.println("Compare license value " + String.valueOf(valueNumber) + " to " + String.valueOf(valueToCompareWith) + ": '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is equal? '" + String.valueOf(booleanValue) + "'");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");

				valueNumber = 2;
				booleanValue = LicenseManager.isEqual(context, licenseItemProvisionRate, valueToCompareWith, valueNumber);

				System.out.println("");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
				System.out.println("Compare license value " + String.valueOf(valueNumber) + " to " + String.valueOf(valueToCompareWith) + ": '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is equal? '" + String.valueOf(booleanValue) + "'");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");

				// Compare license value: using isTrue()
				booleanValue = LicenseManager.isTrue(context, licenseItemProvisionRate);

				System.out.println("");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
				System.out.println("Compare license value 0 to TRUE: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is TRUE? '" + String.valueOf(booleanValue) + "'");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");

				valueNumber = 1;
				booleanValue = LicenseManager.isTrue(context, licenseItemProvisionRate, valueNumber);

				System.out.println("");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
				System.out.println("Compare license value " + String.valueOf(valueNumber) + " to TRUE: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is TRUE? '" + String.valueOf(booleanValue) + "'");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");

				valueNumber = 2;
				booleanValue = LicenseManager.isTrue(context, licenseItemProvisionRate, valueNumber);

				System.out.println("");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
				System.out.println("Compare license value " + String.valueOf(valueNumber) + " to TRUE: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is TRUE? '" + String.valueOf(booleanValue) + "'");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");

				// Compare license value: using isFalse()
				booleanValue = LicenseManager.isFalse(context, licenseItemProvisionRate);

				System.out.println("");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
				System.out.println("Compare license value 0 to FALSE: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is FALSE? '" + String.valueOf(booleanValue) + "'");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");

				valueNumber = 1;
				booleanValue = LicenseManager.isFalse(context, licenseItemProvisionRate, valueNumber);

				System.out.println("");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
				System.out.println("Compare license value " + String.valueOf(valueNumber) + " to FALSE: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is FALSE? '" + String.valueOf(booleanValue) + "'");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");

				valueNumber = 2;
				booleanValue = LicenseManager.isFalse(context, licenseItemProvisionRate, valueNumber);

				System.out.println("");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
				System.out.println("Compare license value " + String.valueOf(valueNumber) + " to FALSE: '" + licenseItemProvisionRate.getName() + "'");
				System.out.println("License Key: '" + licenseKey + "'");
				System.out.println("Is FALSE? '" + String.valueOf(booleanValue) + "'");
				System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
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
