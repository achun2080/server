package fmagic.test.runner;

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
	private static final String TESTCASE_NAME = "mediatestcommand";

	private ServerManager serverAp1 = null;
	private ServerManager serverAp2 = null;
	private ServerManager serverAp3 = null;

	private ClientManager clientAp1 = null;
	private ClientManager clientAp2 = null;
	private ClientManager clientAp3 = null;

	/**
	 * Constructor
	 * 
	 * @param testSuite
	 *            The test suite that holds this test runner, or <TT>null</TT> if
	 *            no test suite is available.
	 * 
	 * @param testSessionName
	 *            The name of the test session.
	 */
	public TestRunnerMediaCommand(TestSuite testSuite, String testSessionName)
	{
		// Call super class
		super(testSuite, TESTCASE_NAME, testSessionName);

		// Clear test session directory
		TestManager.cleanTestSessionDirectory(this);
	}

	@Override
	public void setup()
	{
		try
		{
			/*
			 * Please notice that each application is configured with
			 * specific settings via configuration files.
			 */

			// Create application servers for the test
			serverAp1 = this.createApplicationServer("ap1");
			serverAp2 = this.createApplicationServer("ap2");
			serverAp3 = this.createApplicationServer("ap3");
			
			// Create client applications for the test
			clientAp1 = this.createApplicationClient("cl1");
			clientAp2 = this.createApplicationClient("cl2");
			clientAp3 = this.createApplicationClient("cl3");
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

		try
		{
			// Prepare server and client
			ClientManager client = this.clientAp1;
			ServerManager server = this.serverAp1;
			
			client.setSocketConnectionParameter("localhost", server.getServerSocketPort(), 10000);
			
			// Prepare test container
			testContainer = new TestContainerMediaCommand(client.getContext(), this, false);
			testContainer.setParameterClientServer(client, server);
			testContainer.setParameterResourceGroup("Factory");
			testContainer.setParameterResourceName("Doorway");
			testContainer.setParameterDataIdentifierTestUpload("9001");
			testContainer.setParameterNumberOfMediaToBeUploaded(100);
			
			// Execute test
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
		try
		{
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
		try
		{
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
