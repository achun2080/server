package fmagic.test.suite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import fmagic.basic.file.FileLocationFunctions;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.test.application.TestManager;
import fmagic.test.runner.TestRunner;

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
	// Identification
	private final String testSuiteName;

	// List of test runners
	private Queue<TestRunner> testRunnerList = new LinkedList<TestRunner>();

	// Error protocol
	private final HashMap<String, String> assertionErrorProtocol = new HashMap<String, String>();
	private int assertionNumberOfErrors = 0;

	// Processing status
	public static enum ProcessingStatusEnum
	{
		STARTED, STOPPED, PROGRESS, RESULT
	}

	/**
	 * Constructor
	 */
	public TestSuite()
	{
		Date messageDate = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
		this.testSuiteName = simpleDateFormat.format(messageDate);
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
			this.writeProcessStatusToFile(ProcessingStatusEnum.PROGRESS, test, "executeComponentTests()");

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
			this.writeProcessStatusToFile(ProcessingStatusEnum.PROGRESS, test, "executeConcurrentTests()");

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
			this.writeProcessStatusToFile(ProcessingStatusEnum.PROGRESS, test, "executeStressTests()");

			test.setup();
			test.executeStressTest();
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
			this.writeProcessStatusToFile(ProcessingStatusEnum.PROGRESS, test, "executeIntegerationTests()");
			
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
			this.writeProcessStatusToFile(ProcessingStatusEnum.STARTED, null, null);
			
			this.toBeExecuted();
			
			this.writeProcessStatusToFile(ProcessingStatusEnum.STOPPED, null, null);
			this.writeProcessStatusToFile(ProcessingStatusEnum.RESULT, null, null);

		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	/**
	 * Getter
	 */
	public HashMap<String, String> getAssertionErrorProtocol()
	{
		return assertionErrorProtocol;
	}

	/**
	 * Getter
	 */
	public int getAssertionNumberOfErrors()
	{
		return assertionNumberOfErrors;
	}

	/**
	 * Setter
	 */
	public void increaseAssertionNumberOfErrors()
	{
		this.assertionNumberOfErrors++;
	}

	/**
	 * Print the error protocol of a test suite.
	 * 
	 * @return Returns the content of the error protocol as string or
	 *         <TT>null</TT> if no errors were found.
	 */
	public String printAssertionErrorProtocol()
	{
		return TestManager.printAssertionErrorProtocol(this.assertionNumberOfErrors, this.assertionErrorProtocol);
	}

	/**
	 * Write processing status to the protocol file of the test suite.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param processingStatus
	 *            The processing status to notify.
	 */
	private void writeProcessStatusToFile(ProcessingStatusEnum processingStatus, TestRunner testRunner, String progressText)
	{
		// Initialize variables
		String messageText = "";
		String fileName = "";

		try
		{
			// Compose header of message and get file name
			Date messageDate = new Date();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

			if (processingStatus == ProcessingStatusEnum.STARTED)
			{
				messageText += "Test suite started at " + simpleDateFormat.format(messageDate);
				fileName = this.testSuiteName + "-1-STARTED.log";
			}
			else if (processingStatus == ProcessingStatusEnum.PROGRESS)
			{
				messageText += "At " + simpleDateFormat.format(messageDate);
				
				if (testRunner != null) messageText += " [" + testRunner.getTestRunnerName() + "][" + testRunner.getTestSessionName() + "]";				if (progressText != null) messageText += " " +  progressText;
				
				fileName = this.testSuiteName + "-2-PROGRESS.log";
			}
			else if (processingStatus == ProcessingStatusEnum.STOPPED)
			{
				messageText += "Test suite stopped at " + simpleDateFormat.format(messageDate);
				fileName = this.testSuiteName + "-3-STOPPED.log";
			}
			else if (processingStatus == ProcessingStatusEnum.RESULT)
			{
				if (this.getAssertionErrorProtocol() != null && this.getAssertionErrorProtocol().size() > 0)
				{
					messageText += String.valueOf(this.getAssertionNumberOfErrors()) + " errors found\n\n";
					messageText += this.printAssertionErrorProtocol();
					fileName = this.testSuiteName + "-4-ERROR.log";
				}
				else
				{
					messageText += "No errors found";
					fileName = this.testSuiteName + "-4-OK.log";
				}
			}
			else
			{
				return;
			}

			// Normalize new line
			String normalizedText = FileUtilFunctions.generalNormalizeNewLine(messageText + "\n");

			// Write message to the protocol file
			File directory;
			PrintWriter output;

			// Gets file path and file name
			String filePath = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getTestSubPath());

			// Create directory
			directory = new File(filePath);
			directory.mkdirs();

			// Write to log file
			output = new PrintWriter(new FileOutputStream(new File(filePath, fileName), true));
			output.append(normalizedText);
			output.flush();
			output.close();
		}
		catch (Exception exception)
		{
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			exception.printStackTrace(printWriter);
			String exceptionText = writer.toString();
			System.out.println(exceptionText);
		}
	}
}
