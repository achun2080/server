package fmagic.test;

import java.security.KeyPair;

import org.apache.commons.codec.binary.Base64;

import fmagic.application.seniorcitizen.client.ClientSeniorCitizen;
import fmagic.application.seniorcitizen.server.ServerSeniorCitizen;
import fmagic.basic.Context;
import fmagic.basic.EncodingHandler;
import fmagic.basic.ResponseContainer;
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
	public static void main(String[] args)
	{
		/*
		 * Start Application server
		 */
		ServerManager server = null;

		try
		{
			// Instantiate and Start
			server = ServerSeniorCitizen.getInstance("ap1", 8090, 1000000);
			if (server != null) server.startApplication();

			ApplicationTest.mediaTest(server, server.getContext());
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
		if (server != null) server.stopApplication();
	}

	/**
	 * Media Test
	 */
	public static void mediaTest(ServerManager server, Context context)
	{
		// Run test
		if (server != null && context != null)
		{
			ApplicationTestMedia testMedia;

			testMedia = new ApplicationTestMedia(server, server.getContext(), "Apartment", "Room");
			server.getExecutorService().execute(testMedia);
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
