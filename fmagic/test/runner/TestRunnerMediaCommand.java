package fmagic.test.runner;

import java.util.ArrayList;
import java.util.List;

import fmagic.client.application.ClientManager;
import fmagic.server.application.ServerManager;
import fmagic.test.application.TestManager;
import fmagic.test.container.TestContainer;
import fmagic.test.container.TestContainerMediaCommand;
import fmagic.test.suite.TestSuite;

/**
 * This class implements testing functionality regarding the
 * <TT>Media Manager</TT> using a media pool.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 29.04.2013 - Created
 * 
 */
public class TestRunnerMediaCommand extends TestRunner
{
	private static final String TEST_RUNNER_NAME = "mediacommand";

	private ServerManager serverAp1 = null;
	private ServerManager serverAp2 = null;
	private ServerManager serverAp3 = null;

	private ClientManager clientAp1 = null;
	private ClientManager clientAp2 = null;
	private ClientManager clientAp3 = null;
	private ClientManager clientAp4 = null;
	private ClientManager clientAp5 = null;
	private ClientManager clientAp6 = null;
	private ClientManager clientAp7 = null;
	private ClientManager clientAp8 = null;
	private ClientManager clientAp9 = null;

	/**
	 * Constructor
	 * 
	 * @param testSuite
	 *            The test suite that holds this test runner, or <TT>null</TT>
	 *            if no test suite is available.
	 * 
	 * @param testSessionName
	 *            The name of the test session.
	 */
	public TestRunnerMediaCommand(TestSuite testSuite, String testSessionName)
	{
		// Call super class
		super(testSuite, TEST_RUNNER_NAME, testSessionName);

		// Clear test session directory
		TestManager.cleanTestSessionDirectory(this);
	}

	@Override
	public void setup()
	{
		try
		{
			/*
			 * Please notice that each application is configured with specific
			 * settings via configuration files.
			 */

			// Create application servers for the test
			serverAp1 = this.createApplicationServer("ap1");
			serverAp2 = this.createApplicationServer("ap2");
			serverAp3 = this.createApplicationServer("ap3");

			// Create client applications for the test
			clientAp1 = this.createApplicationClient("cl1");
			clientAp2 = this.createApplicationClient("cl2");
			clientAp3 = this.createApplicationClient("cl3");
			clientAp4 = this.createApplicationClient("cl4");
			clientAp5 = this.createApplicationClient("cl5");
			clientAp6 = this.createApplicationClient("cl6");
			clientAp7 = this.createApplicationClient("cl7");
			clientAp8 = this.createApplicationClient("cl8");
			clientAp9 = this.createApplicationClient("cl9");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void cleanup()
	{
		try
		{
			// Release application servers
			if (serverAp1 != null) this.releaseApplicationServer(serverAp1);
			if (serverAp2 != null) this.releaseApplicationServer(serverAp2);
			if (serverAp3 != null) this.releaseApplicationServer(serverAp3);

			// Release client applications
			if (clientAp1 != null) this.releaseApplicationClient(clientAp1);
			if (clientAp2 != null) this.releaseApplicationClient(clientAp2);
			if (clientAp3 != null) this.releaseApplicationClient(clientAp3);
			if (clientAp4 != null) this.releaseApplicationClient(clientAp4);
			if (clientAp5 != null) this.releaseApplicationClient(clientAp5);
			if (clientAp6 != null) this.releaseApplicationClient(clientAp6);
			if (clientAp7 != null) this.releaseApplicationClient(clientAp7);
			if (clientAp8 != null) this.releaseApplicationClient(clientAp8);
			if (clientAp9 != null) this.releaseApplicationClient(clientAp9);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void executeSingleFunctionTest(TestContainer testContainer, String methodName)
	{
		this.doSingleFunctionTest(serverAp1.getContext(), testContainer, methodName);
	}

	@Override
	public void executeComponentTest()
	{
		try
		{
			this.doComponentTest();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void executeConcurrentTest()
	{
		try
		{
			this.doConcurrentTest();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void executeStressTest()
	{
		try
		{
			this.doStressTest();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void collectIntegrationTestContainer(List<Thread> threadList)
	{
		try
		{
			this.defineIntegrationTest(threadList);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Component Test
	 */
	private void doComponentTest()
	{
		TestContainerMediaCommand testContainer;

		int plainNumberOfMediaToBeUploaded = 100;
		int cycleNumberOfFilesToBeUploaded = 100;

		try
		{
			// Prepare server and client
			ClientManager client = this.clientAp1;
			ServerManager server = this.serverAp1;

			client.setSocketConnectionParameter("localhost", server.getServerSocketPort());

			// Execute test 1 (Doorway)
			testContainer = new TestContainerMediaCommand(client.getContext(), this, false);
			testContainer.setParameterClientServer(client, server);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Doorway");
			testContainer.setParameterPlainDataIdentifierTestUpload("9001");
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);

			testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
			testContainer.setParameterCycleResourceGroup("Factory");
			testContainer.setParameterCycleResourceName("Doorway");
			testContainer.setParameterCycleDataIdentifierFrom(9101);
			testContainer.setParameterCycleDataIdentifierToo(9200);

			testContainer.executeComponentTest();

			// Execute test 2 (Hall)
			testContainer = new TestContainerMediaCommand(client.getContext(), this, false);
			testContainer.setParameterClientServer(client, server);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Hall");
			testContainer.setParameterPlainDataIdentifierTestUpload("9001");
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);

			testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
			testContainer.setParameterCycleResourceGroup("Factory");
			testContainer.setParameterCycleResourceName("Hall");
			testContainer.setParameterCycleDataIdentifierFrom(9101);
			testContainer.setParameterCycleDataIdentifierToo(9200);

			testContainer.executeComponentTest();

			// Execute test 3 (Office)
			testContainer = new TestContainerMediaCommand(client.getContext(), this, false);
			testContainer.setParameterClientServer(client, server);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Office");
			testContainer.setParameterPlainDataIdentifierTestUpload("9001");
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);

			testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
			testContainer.setParameterCycleResourceGroup("Factory");
			testContainer.setParameterCycleResourceName("Office");
			testContainer.setParameterCycleDataIdentifierFrom(9101);
			testContainer.setParameterCycleDataIdentifierToo(9200);

			testContainer.executeComponentTest();

			// Execute test 4 (Canteen)
			testContainer = new TestContainerMediaCommand(client.getContext(), this, false);
			testContainer.setParameterClientServer(client, server);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Canteen");
			testContainer.setParameterPlainDataIdentifierTestUpload("9001");
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);

			testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
			testContainer.setParameterCycleResourceGroup("Factory");
			testContainer.setParameterCycleResourceName("Canteen");
			testContainer.setParameterCycleDataIdentifierFrom(9101);
			testContainer.setParameterCycleDataIdentifierToo(9200);

			testContainer.executeComponentTest();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Concurrent Test
	 */
	private void doConcurrentTest()
	{
		TestContainerMediaCommand testContainer;
		List<Thread> threadList = new ArrayList<Thread>();

		int plainNumberOfMediaToBeUploaded = 100;
		int cycleNumberOfFilesToBeUploaded = 100;

		try
		{
			/*
			 * 3 Clients request against 1 server
			 */

			// Server 1
			ServerManager server1 = this.serverAp1;

			// Client 1
			ClientManager client1 = this.clientAp1;
			client1.setSocketConnectionParameter("localhost", server1.getServerSocketPort());

			testContainer = new TestContainerMediaCommand(client1.getContext(), this, true);
			testContainer.setParameterClientServer(client1, server1);

			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Hall");
			testContainer.setParameterPlainDataIdentifierTestUpload("9001");

			testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
			testContainer.setParameterCycleResourceGroup("Factory");
			testContainer.setParameterCycleResourceName("Hall");
			testContainer.setParameterCycleDataIdentifierFrom(9101);
			testContainer.setParameterCycleDataIdentifierToo(9200);

			threadList.add(new Thread(testContainer));

			// Client 2
			ClientManager client2 = this.clientAp2;
			client2.setSocketConnectionParameter("localhost", server1.getServerSocketPort());

			testContainer = new TestContainerMediaCommand(client2.getContext(), this, true);
			testContainer.setParameterClientServer(client2, server1);

			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Office");
			testContainer.setParameterPlainDataIdentifierTestUpload("9002");

			testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
			testContainer.setParameterCycleResourceGroup("Factory");
			testContainer.setParameterCycleResourceName("Office");
			testContainer.setParameterCycleDataIdentifierFrom(9201);
			testContainer.setParameterCycleDataIdentifierToo(9300);

			threadList.add(new Thread(testContainer));

			// Client 3
			ClientManager client3 = this.clientAp3;
			client3.setSocketConnectionParameter("localhost", server1.getServerSocketPort());

			testContainer = new TestContainerMediaCommand(client3.getContext(), this, true);
			testContainer.setParameterClientServer(client3, server1);

			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Canteen");
			testContainer.setParameterPlainDataIdentifierTestUpload("9003");

			testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
			testContainer.setParameterCycleResourceGroup("Factory");
			testContainer.setParameterCycleResourceName("Canteen");
			testContainer.setParameterCycleDataIdentifierFrom(9301);
			testContainer.setParameterCycleDataIdentifierToo(9400);

			threadList.add(new Thread(testContainer));

			/*
			 * 6 Clients request against 1 server
			 */

			// Server 2
			ServerManager server2 = this.serverAp2;

			// Client 4
			ClientManager client4 = this.clientAp4;
			client4.setSocketConnectionParameter("localhost", server2.getServerSocketPort());

			testContainer = new TestContainerMediaCommand(client4.getContext(), this, true);
			testContainer.setParameterClientServer(client4, server2);

			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Hall");
			testContainer.setParameterPlainDataIdentifierTestUpload("9004");

			testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
			testContainer.setParameterCycleResourceGroup("Factory");
			testContainer.setParameterCycleResourceName("Hall");
			testContainer.setParameterCycleDataIdentifierFrom(9500);
			testContainer.setParameterCycleDataIdentifierToo(9520);

			threadList.add(new Thread(testContainer));

			// Client 5
			ClientManager client5 = this.clientAp5;
			client5.setSocketConnectionParameter("localhost", server2.getServerSocketPort());

			testContainer = new TestContainerMediaCommand(client5.getContext(), this, true);
			testContainer.setParameterClientServer(client5, server2);

			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Hall");
			testContainer.setParameterPlainDataIdentifierTestUpload("9005");

			testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
			testContainer.setParameterCycleResourceGroup("Factory");
			testContainer.setParameterCycleResourceName("Hall");
			testContainer.setParameterCycleDataIdentifierFrom(9521);
			testContainer.setParameterCycleDataIdentifierToo(9540);

			threadList.add(new Thread(testContainer));

			// Client 6
			ClientManager client6 = this.clientAp6;
			client6.setSocketConnectionParameter("localhost", server2.getServerSocketPort());

			testContainer = new TestContainerMediaCommand(client6.getContext(), this, true);
			testContainer.setParameterClientServer(client6, server2);

			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Hall");
			testContainer.setParameterPlainDataIdentifierTestUpload("9006");

			testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
			testContainer.setParameterCycleResourceGroup("Factory");
			testContainer.setParameterCycleResourceName("Hall");
			testContainer.setParameterCycleDataIdentifierFrom(9541);
			testContainer.setParameterCycleDataIdentifierToo(9560);

			threadList.add(new Thread(testContainer));

			// Client 7
			ClientManager client7 = this.clientAp7;
			client7.setSocketConnectionParameter("localhost", server2.getServerSocketPort());

			testContainer = new TestContainerMediaCommand(client7.getContext(), this, true);
			testContainer.setParameterClientServer(client7, server2);

			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Hall");
			testContainer.setParameterPlainDataIdentifierTestUpload("9007");

			testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
			testContainer.setParameterCycleResourceGroup("Factory");
			testContainer.setParameterCycleResourceName("Hall");
			testContainer.setParameterCycleDataIdentifierFrom(9561);
			testContainer.setParameterCycleDataIdentifierToo(9580);

			threadList.add(new Thread(testContainer));

			// Client 8
			ClientManager client8 = this.clientAp8;
			client8.setSocketConnectionParameter("localhost", server2.getServerSocketPort());

			testContainer = new TestContainerMediaCommand(client8.getContext(), this, true);
			testContainer.setParameterClientServer(client8, server2);

			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Hall");
			testContainer.setParameterPlainDataIdentifierTestUpload("9008");

			testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
			testContainer.setParameterCycleResourceGroup("Factory");
			testContainer.setParameterCycleResourceName("Hall");
			testContainer.setParameterCycleDataIdentifierFrom(9581);
			testContainer.setParameterCycleDataIdentifierToo(9590);

			threadList.add(new Thread(testContainer));

			// Client 9
			ClientManager client9 = this.clientAp9;
			client9.setSocketConnectionParameter("localhost", server2.getServerSocketPort());

			testContainer = new TestContainerMediaCommand(client9.getContext(), this, true);
			testContainer.setParameterClientServer(client9, server2);

			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Hall");
			testContainer.setParameterPlainDataIdentifierTestUpload("9009");

			testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
			testContainer.setParameterCycleResourceGroup("Factory");
			testContainer.setParameterCycleResourceName("Hall");
			testContainer.setParameterCycleDataIdentifierFrom(9591);
			testContainer.setParameterCycleDataIdentifierToo(9599);

			threadList.add(new Thread(testContainer));

			/*
			 * Execute test
			 */

			// Start all threads parallel
			TestManager.threadListStart(threadList);

			// Wait for the end of all threads
			TestManager.threadListJoin(threadList);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Concurrent Test
	 */
	private void doStressTest()
	{
		TestContainerMediaCommand testContainer;
		List<Thread> threadList = new ArrayList<Thread>();

		int plainNumberOfMediaToBeUploaded = 100;
		int cycleNumberOfFilesToBeUploaded = 100;
		int commonDataIdentifier = 9999;

		/*
		 * Please notice: In the stress test one and the same data identifier is
		 * overridden by all 9 clients against one server.
		 */

		try
		{
			/*
			 *  Server 3
			 */
			ServerManager server3 = this.serverAp3;

			/*
			 *  9 x Client
			 */
			ClientManager client = null;
			
			for (int i = 1; i <= 9; i++)
			{
				if (i == 1) client = this.clientAp1;
				if (i == 2) client = this.clientAp2;
				if (i == 3) client = this.clientAp3;
				if (i == 4) client = this.clientAp4;
				if (i == 5) client = this.clientAp5;
				if (i == 6) client = this.clientAp6;
				if (i == 7) client = this.clientAp7;
				if (i == 8) client = this.clientAp8;
				if (i == 9) client = this.clientAp9;

				client.setSocketConnectionParameter("localhost", server3.getServerSocketPort());

				testContainer = new TestContainerMediaCommand(client.getContext(), this, true);
				testContainer.setParameterClientServer(client, server3);

				testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
				testContainer.setParameterPlainResourceGroup("Factory");
				testContainer.setParameterPlainResourceName("Hall");
				testContainer.setParameterPlainDataIdentifierTestUpload(String.valueOf(commonDataIdentifier));

				testContainer.setParameterCycleNumberOfFilesToBeUploaded(cycleNumberOfFilesToBeUploaded);
				testContainer.setParameterCycleResourceGroup("Factory");
				testContainer.setParameterCycleResourceName("Hall");
				testContainer.setParameterCycleDataIdentifierFrom(commonDataIdentifier);
				testContainer.setParameterCycleDataIdentifierToo(commonDataIdentifier);

				threadList.add(new Thread(testContainer));
			}

			/*
			 * Execute test
			 */

			// Start all threads parallel
			TestManager.threadListStart(threadList);

			// Wait for the end of all threads
			TestManager.threadListJoin(threadList);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Integration Test
	 */
	private void defineIntegrationTest(List<Thread> threadList)
	{
		try
		{
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
