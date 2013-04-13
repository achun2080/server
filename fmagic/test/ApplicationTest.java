package fmagic.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private Set<TestRunner> testRunnerList = new HashSet<TestRunner>();

	/**
	 * Constructor
	 */
	public ApplicationTest()
	{
	}

	/**
	 * Add a test runner to the list of test runners.
	 */
	public void addTestRunner(TestRunner testRunner)
	{
		this.testRunnerList.add(testRunner);
	}

	/**
	 * Execute all component tests on test runner list.
	 */
	public void executeComponentTests()
	{
		for (TestRunner test : this.testRunnerList)
		{
			test.setup();
			test.executeComponentTest();
			test.cleanup();
		}
	}

	/**
	 * Execute all concurrent tests on test runner list.
	 */
	public void executeConcurrentTests()
	{
		for (TestRunner test : this.testRunnerList)
		{
			test.setup();
			test.executeConcurrentTest();
			test.cleanup();
		}
	}

	/**
	 * Execute all stress tests on test runner list.
	 */
	public void executeStressTests()
	{
		for (TestRunner test : this.testRunnerList)
		{
			test.setup();
			test.executeConcurrentTest();
			test.cleanup();
		}
	}

	/**
	 * Execute integration tests on test runner list. All test tasks are
	 * collected first, and then started, each task as a thread.
	 */
	public void executeIntegerationTests()
	{
		List<Thread> threadList = new ArrayList<Thread>();

		// Go through all test runners and collect all test containers to be
		// tested in the integration test.
		for (TestRunner test : this.testRunnerList)
		{
			// Setup test runner
			test.setup();

			// Collect test container
			test.collectIntegrationTestContainer(threadList);
		}

		// Start all collected threads parallel
		TestManager.threadListStart(threadList);

		// Wait for the end of all threads
		TestManager.threadListJoin(threadList);

		// Clean up all test runners
		for (TestRunner test : this.testRunnerList)
		{
			test.cleanup();
		}
	}
}
