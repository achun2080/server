package fmagic.test.runner;

import java.util.List;

import fmagic.server.application.ServerManager;
import fmagic.test.container.TestContainer;
import fmagic.test.container.TestContainerMediaPool;
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
public class TestRunnerMediaPool extends TestRunner
{
	private static final String TEST_RUNNER_NAME = "mediapool";

	private ServerManager serverAp1 = null;

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
	public TestRunnerMediaPool(TestSuite testSuite, String testSessionName)
	{
		// Call super class
		super(testSuite, TEST_RUNNER_NAME, testSessionName);
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
		try
		{
			TestContainerMediaPool testContainer;
			
			int plainNumberOfMediaToBeUploaded = 100;
			
			/*
			 * Please notice that each application is configured with specific
			 * settings via configuration files.
			 */

			/*
			 * Test case 1
			 * 
			 * One application server refers too four media server, combined to
			 * a media pool. All servers running, that means are online.
			 */
			
			// Create and start servers
			ServerManager serverAp1234 = this.createApplicationServer("ap1234", 8010);
			ServerManager serverMs1 = this.createApplicationServer("ms1", 8021);
			ServerManager serverMs2 = this.createApplicationServer("ms2", 8022);
			ServerManager serverMs3 = this.createApplicationServer("ms3", 8023);
			ServerManager serverMs4 = this.createApplicationServer("ms4", 8024);
			
			// Cleanup media directories
			this.cleanTestMediaDirectory(serverAp1234.getContext());
			this.cleanTestMediaDirectory(serverMs1.getContext());
			this.cleanTestMediaDirectory(serverMs2.getContext());
			this.cleanTestMediaDirectory(serverMs3.getContext());
			this.cleanTestMediaDirectory(serverMs4.getContext());
		
			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp1234.getContext(), this, false);
			testContainer.setParameterServer(serverAp1234);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Doorway");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(3001);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.executeComponentTest();
			
			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp1234.getContext(), this, false);
			testContainer.setParameterServer(serverAp1234);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Hall");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(3001);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.executeComponentTest();
			
			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp1234.getContext(), this, false);
			testContainer.setParameterServer(serverAp1234);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Office");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(3001);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.executeComponentTest();
			
			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp1234.getContext(), this, false);
			testContainer.setParameterServer(serverAp1234);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Canteen");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(3001);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.executeComponentTest();
			
			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp1234.getContext(), this, false);
			testContainer.setParameterServer(serverAp1234);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Doorway");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(3001);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.executeComponentTest();

			// Release all servers
			if (serverAp1234 != null) this.releaseApplicationServer(serverAp1234);
			if (serverMs1 != null) this.releaseApplicationServer(serverMs1);
			if (serverMs2 != null) this.releaseApplicationServer(serverMs2);
			if (serverMs3 != null) this.releaseApplicationServer(serverMs3);
			if (serverMs4 != null) this.releaseApplicationServer(serverMs4);
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
