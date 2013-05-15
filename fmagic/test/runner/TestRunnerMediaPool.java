package fmagic.test.runner;

import java.util.List;

import fmagic.server.application.ServerManager;
import fmagic.test.application.TestManager;
import fmagic.test.container.TestContainer;
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
