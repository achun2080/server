package fmagic.test;

import java.security.KeyPair;

import org.apache.commons.codec.binary.Base64;

import fmagic.application.seniorcitizen.client.ClientSeniorCitizen;
import fmagic.application.seniorcitizen.server.ServerSeniorCitizen;
import fmagic.basic.Context;
import fmagic.basic.EncodingHandler;
import fmagic.basic.FileUtil;
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
	public static void main(String[] args)
	{
		/*
		 * Start Application server
		 */
		ServerManager serverAp1 = null;
		ServerManager serverAp2 = null;
		ServerManager serverAp3 = null;

		try
		{
			// Instantiate server AP1
			serverAp1 = ServerSeniorCitizen.getTestInstance("ap1", 8090, 1000000, "mediatest");
			if (serverAp1 == null) System.exit(-1);
			serverAp1.startApplication();
			ApplicationTest.mediaTest(serverAp1, serverAp1.getContext());

			// Instantiate server AP2
			serverAp2 = ServerSeniorCitizen.getTestInstance("ap2", 8091, 1000000, "mediatest");
			if (serverAp2 == null) System.exit(-1);
			serverAp2.startApplication();
			ApplicationTest.mediaTest(serverAp2, serverAp2.getContext());

			// Instantiate server AP3
			serverAp3 = ServerSeniorCitizen.getTestInstance("ap3", 8092, 1000000, "mediatest");
			if (serverAp3 == null) System.exit(-1);
			serverAp3.startApplication();
			ApplicationTest.mediaTest(serverAp3, serverAp3.getContext());

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		/*
		 *  Shutdown Application server
		 */
		try
		{
			serverAp1.stopApplication();
			serverAp2.stopApplication();
			serverAp3.stopApplication();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Media Test
	 */
	public static void mediaTest(ServerManager server, Context context)
	{
		// Run test
		if (server != null && context != null)
		{
			/*
			 * Test specific media resource item (not concurrent)
			 */

			// Room
			ServerTestMedia testMediaSingle = new ServerTestMedia(server, server.getContext(), false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Room");
			testMediaSingle.setParameterDataIdentifierTestUpload("1234");
			testMediaSingle.setParameterDataIdentifierTestObsolete("1235");
			testMediaSingle.setParameterTestCycleNumber(1);
			testMediaSingle.setParameterTestCycleDataIdentifierFrom(1);
			testMediaSingle.setParameterTestCycleDataIdentifierToo(40);
			testMediaSingle.testAll();
			testMediaSingle.doSetup();

			/*
			 * Test concurrent access to files of a specific media resource item
			 */

			ServerTestMedia testMediaConcurrent;

			// Room 1
			testMediaConcurrent = new ServerTestMedia(server, server.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2345");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2346");
			testMediaConcurrent.setParameterTestCycleNumber(5);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			new Thread(testMediaConcurrent).start();

			// Room 2
			testMediaConcurrent = new ServerTestMedia(server, server.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2347");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2348");
			testMediaConcurrent.setParameterTestCycleNumber(5);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			new Thread(testMediaConcurrent).start();

			// Room 3
			testMediaConcurrent = new ServerTestMedia(server, server.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2349");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2350");
			testMediaConcurrent.setParameterTestCycleNumber(5);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			new Thread(testMediaConcurrent).start();

			/*
			 * Add other concurrent media resource items
			 */

			// Floor
			testMediaConcurrent = new ServerTestMedia(server, server.getContext(), false);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Floor");
			testMediaConcurrent.testAll();
			testMediaConcurrent.doSetup();

			testMediaConcurrent = new ServerTestMedia(server, server.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Floor");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1236");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1237");
			testMediaConcurrent.setParameterTestCycleNumber(5);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(10);
			new Thread(testMediaConcurrent).start();

			// Bedroom
			testMediaConcurrent = new ServerTestMedia(server, server.getContext(), false);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bedroom");
			testMediaConcurrent.testAll();
			testMediaConcurrent.doSetup();

			testMediaConcurrent = new ServerTestMedia(server, server.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bedroom");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1238");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1239");
			testMediaConcurrent.setParameterTestCycleNumber(5);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(2);
			new Thread(testMediaConcurrent).start();

			// Kitchen
			testMediaConcurrent = new ServerTestMedia(server, server.getContext(), false);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Kitchen");
			testMediaConcurrent.testAll();
			testMediaConcurrent.doSetup();

			testMediaConcurrent = new ServerTestMedia(server, server.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Kitchen");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1240");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1241");
			testMediaConcurrent.setParameterTestCycleNumber(5);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(10000);
			new Thread(testMediaConcurrent).start();

			// Bathroom
			testMediaConcurrent = new ServerTestMedia(server, server.getContext(), false);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bathroom");
			testMediaConcurrent.testAll();
			testMediaConcurrent.doSetup();

			testMediaConcurrent = new ServerTestMedia(server, server.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bathroom");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1242");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1243");
			testMediaConcurrent.setParameterTestCycleNumber(5);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(500);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(1000);
			new Thread(testMediaConcurrent).start();
		}
	}

	/**
	 * Create keys
	 */
	public void createKeys()
	{
		EncodingHandler encodingHandler = new EncodingHandler();
		KeyPair keyPair = encodingHandler.getPublicPrivateKeyPair();

		System.out.println(Base64.encodeBase64String(keyPair.getPublic().getEncoded()));
		System.out.println(Base64.encodeBase64String(keyPair.getPrivate().getEncoded()));
	}

}
