package fmagic.test;

import java.security.KeyPair;

import org.apache.commons.codec.binary.Base64;

import fmagic.application.seniorcitizen.client.ClientSeniorCitizen;
import fmagic.application.seniorcitizen.server.ServerSeniorCitizen;
import fmagic.basic.Context;
import fmagic.basic.EncodingHandler;
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
		ServerManager server = null;

		try
		{
			// Instantiate server
			server = ServerSeniorCitizen.getInstance("ap1", 8090, 1000000, true);

			// Start server and testing
			if (server != null)
			{
				server.startApplication();

				ApplicationTest.mediaTest(server, server.getContext());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		/*
		 * Start client
		 */
		ClientManager client = null;

		try
		{
			if (server != null)
			{
				// Instantiate client
				client = ClientSeniorCitizen.getInstance("cl1", true);

				// Start client and testing
				if (client != null)
				{
					// Start client
					client.startApplication();
					client.setSocketConnectionParameter("localhost", 8090, 1000000);

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
		if (server != null)
		{
			server.stopApplication();
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
			ServerTestMedia testMedia;

			testMedia = new ServerTestMedia(server, server.getContext(), "Apartment", "Room");
			new Thread(testMedia).start();

			testMedia = new ServerTestMedia(server, server.getContext(), "Apartment", "Floor");
			new Thread(testMedia).start();

			testMedia = new ServerTestMedia(server, server.getContext(), "Apartment", "Bedroom");
			new Thread(testMedia).start();

			testMedia = new ServerTestMedia(server, server.getContext(), "Apartment", "Kitchen");
			new Thread(testMedia).start();

			testMedia = new ServerTestMedia(server, server.getContext(), "Apartment", "Bathroom");
			new Thread(testMedia).start();

			testMedia = new ServerTestMedia(server, server.getContext(), "Apartment", "Room");
			new Thread(testMedia).start();
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
