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
import fmagic.basic.ResourceContainerLicense;
import fmagic.basic.ResourceContainerMedia;
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

				// Test 1
				ResourceContainer attributeResourceContainer = ResourceManager.attribute(context, "Media", "ClientEncoding");
				String documentation = attributeResourceContainer.printManual(context);

				System.out.println("");
				System.out.println("111111111111111111111111111111");
				System.out.println(documentation);
				System.out.println("111111111111111111111111111111");

				// Test 2
				ResourceContainerMedia mediaResourceContainer = ResourceManager.media(context, "Apartment", "Room");
				documentation = mediaResourceContainer.printManual(context);

				System.out.println("");
				System.out.println("222222222222222222222222222222");
				System.out.println(documentation);
				System.out.println("222222222222222222222222222222");

				// Test 3
				System.out.println("");
				System.out.println("333333333333333333333333333333");
				System.out.println("Is IMAGE? " + mediaResourceContainer.isMediaTypeImage(context));
				System.out.println("Is VIDEO? " + mediaResourceContainer.isMediaTypeVideo(context));
				System.out.println("Is AUDIO? " + mediaResourceContainer.isMediaTypeAudio(context));
				System.out.println("Is DOCUMENT? " + mediaResourceContainer.isMediaTypeDocument(context));
				System.out.println("333333333333333333333333333333");

				// Test 4
				System.out.println("");
				System.out.println("444444444444444444444444444444");
				System.out.println("JPG supported? " + mediaResourceContainer.isFileTypeSupported(context, "JPG"));
				System.out.println("jpg supported? " + mediaResourceContainer.isFileTypeSupported(context, "jpg"));
				System.out.println("mkv supported? " + mediaResourceContainer.isFileTypeSupported(context, "mkv"));
				System.out.println("AAA supported? " + mediaResourceContainer.isFileTypeSupported(context, "aaa"));
				System.out.println("444444444444444444444444444444");

				// Test 5
				System.out.println("");
				System.out.println("555555555555555555555555555555");
				System.out.println("Origin is SERVER? " + mediaResourceContainer.isOriginServer(context));
				System.out.println("Origin is CLIENT? " + mediaResourceContainer.isOriginClient(context));
				System.out.println("Origin is ALL? " + mediaResourceContainer.isOriginAll(context));
				System.out.println("555555555555555555555555555555");

				// Test 6
				System.out.println("");
				System.out.println("666666666666666666666666666666");
				System.out.println("StorageLocation is SERVER? " + mediaResourceContainer.isStorageLocationServer(context));
				System.out.println("StorageLocation is CLIENT? " + mediaResourceContainer.isStorageLocationClient(context));
				System.out.println("StorageLocation is SYNCHRONIZED? " + mediaResourceContainer.isStorageLocationSynchronize(context));
				System.out.println("666666666666666666666666666666");

				// Test 7
				System.out.println("");
				System.out.println("777777777777777777777777777777");
				System.out.println("LogicalPath? " + mediaResourceContainer.getLogicalPath(context));
				System.out.println("777777777777777777777777777777");

				// Test 8
				System.out.println("");
				System.out.println("888888888888888888888888888888");
				System.out.println("ServerEncoding? " + mediaResourceContainer.isServerEncoding(context));
				System.out.println("888888888888888888888888888888");

				// Test 9
				System.out.println("");
				System.out.println("999999999999999999999999999999");
				System.out.println("ClientEncoding? " + mediaResourceContainer.isClientEncoding(context));
				System.out.println("999999999999999999999999999999");
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
