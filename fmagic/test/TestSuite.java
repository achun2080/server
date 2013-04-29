package fmagic.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class implements test suites, that bundle a bunch of tests.
 * 
 * @author F.Wuensche (FW)
 * 
 * @changed AG 27.03.2009 - Created
 * 
 */
public abstract class TestSuite
{
	// List of test runners
	private Set<TestRunner> testRunnerList = new HashSet<TestRunner>();

	// Error protocol
	private final HashMap<String, String> assertionErrorProtocol = new HashMap<String, String>();
	private int assertionNumberOfErrors = 0;

	/**
	 * Constructor
	 */
	public TestSuite()
	{
	}

	/**
	 * This method contains the implementation of the test suite that is to be
	 * executed later.
	 * <p>
	 * Please notice: If you want to execute the actual test you have to invoke
	 * the method <TT>execute()</TT>. The latter automatically invokes this
	 * method <TT>toBeExecuted()</TT> after preparing some test stuff.
	 */
	protected abstract void toBeExecuted();

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
	protected void executeComponentTests()
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
	protected void executeConcurrentTests()
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
	protected void executeStressTests()
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
	protected void executeIntegerationTests()
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

	/**
	 * Print the error protocol of a test suite.
	 * 
	 * @return Returns the content of the error protocol list as string or
	 *         <TT>null</TT> if no errors are found.
	 */
	public String printAssertionErrorProtocol()
	{
		// Validate
		if (this.assertionErrorProtocolGetNumberOfErrors() == 0) return null;
		if (this.assertionErrorProtocolGetMap() == null) return null;
		if (this.assertionErrorProtocolGetMap().size() == 0) return null;

		// Initialize
		String returnText = "";

		// Sort list on file names
		List<String> sortedList = new ArrayList<String>(this.assertionErrorProtocolGetMap().keySet());
		Collections.sort(sortedList);

		// Print all items into list
		Iterator<String> iterItems = sortedList.iterator();

		while (iterItems.hasNext())
		{
			String protocolFileName = iterItems.next();
			if (protocolFileName == null) continue;
			if (protocolFileName.length() == 0) continue;

			String errortext = this.assertionErrorProtocolGetMap().get(protocolFileName);
			if (errortext == null) continue;
			if (errortext.length() == 0) continue;

			returnText += "\n:::::::::: '" + protocolFileName + "'\n";
			returnText += errortext;
		}

		// Return
		return returnText;
	}

	/**
	 * Executes a test suite once.
	 * <p>
	 * Please notice: The implementation of the test suite has to be done in the
	 * method <TT>toBeExecuted()</TT> first. The <TT>execute()</TT> method runs
	 * that function automatically after preparing some test stuff.
	 */
	public void execute()
	{
		try
		{
			this.toBeExecuted();
			
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	/**
	 * Getter
	 */
	public HashMap<String, String> assertionErrorProtocolGetMap()
	{
		return assertionErrorProtocol;
	}

	/**
	 * Getter
	 */
	public int assertionErrorProtocolGetNumberOfErrors()
	{
		return assertionNumberOfErrors;
	}

	/**
	 * Setter
	 */
	public void assertionErrorProtocolIncreaseNumberOfErrors()
	{
		this.assertionNumberOfErrors++;
	}
}
