package fmagic.test.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import fmagic.basic.application.ManagerInterface;
import fmagic.basic.context.Context;
import fmagic.basic.file.FileLocationFunctions;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.test.container.TestContainer;
import fmagic.test.runner.TestRunner;
import fmagic.test.suite.TestSuite;

/**
 * This class implements the management of tests that are included in the
 * development environment directly.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 08.04.2013 - Created
 * 
 */
public class TestManager implements ManagerInterface
{
	private String runtimeErrorIdentifier = null;
	private String runtimeErrorAdditionalText = null;
	private Exception runtimeErrorException = null;

	private String errorToBeSuppressedErrorIdentifier = null;

	/**
	 * Constructor
	 */
	public TestManager()
	{
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		return "";
	}

	@Override
	public String printManual(Context context)
	{
		return "";
	}

	@Override
	public String printIdentifierList(Context context)
	{
		return "";
	}

	@Override
	public boolean validateResources(Context context)
	{
		return false;
	}

	@Override
	public boolean readConfiguration(Context context)
	{
		return false;
	}

	@Override
	public boolean cleanEnvironment(Context context)
	{
		return false;
	}

	/**
	 * Start all treads of a thread list.
	 * 
	 * @param threadList
	 *            The list of threads to consider.
	 */
	public static void threadListStart(List<Thread> threadList)
	{
		for (Thread thread : threadList)
		{
			thread.start();
		}
	}

	/**
	 * Wait for the end of all treads of a thread list.
	 * 
	 * @param threadList
	 *            The list of threads to consider.
	 */
	public static void threadListJoin(List<Thread> threadList)
	{
		for (Thread thread : threadList)
		{
			try
			{
				thread.join();
			}
			catch (Exception exception)
			{
				// Be silent
			}
		}
	}

	/**
	 * Push an error message to the error protocols of test container, test
	 * runner and test suite.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param assertText
	 *            Headline text that describes the assertion.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 */
	private static synchronized void addErrorToErrorProtocolLists(Context context, TestContainer testContainer, String assertText, String additionalText)
	{
		// Check parameter
		if (testContainer == null) return;
		if (assertText == null && additionalText == null) return;

		// Gets error file name as the identifier of the error protocol list
		String errorFileName = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getTestLoggingSubPath(), FileLocationFunctions.getTestLoggingSubSubPath(), FileLocationFunctions.getTestLoggingErrorFileName());
		errorFileName = FileLocationFunctions.replacePlacholder(context, errorFileName);

		// Add error messages to: Test Container
		String errorText = testContainer.getAssertionErrorProtocol().get(errorFileName);
		if (errorText == null) errorText = "";
		if (assertText != null && assertText.length() > 0) errorText += "\n" + assertText;
		if (additionalText != null && additionalText.length() > 0) errorText += "\n" + additionalText;
		testContainer.getAssertionErrorProtocol().put(errorFileName, errorText);
		testContainer.increaseAssertionNumberOfErrors();

		// Add error messages to: Test Runner
		TestRunner testRunner = testContainer.getTestRunner();
		if (testRunner == null) return;

		errorText = testRunner.getAssertionErrorProtocol().get(errorFileName);
		if (errorText == null) errorText = "";
		if (assertText != null && assertText.length() > 0) errorText += "\n" + assertText;
		if (additionalText != null && additionalText.length() > 0) errorText += "\n" + additionalText;
		testRunner.getAssertionErrorProtocol().put(errorFileName, errorText);
		testRunner.increaseAssertionNumberOfErrors();

		// Add error messages to: Test Suite
		TestSuite testSuite = testRunner.getTestSuite();
		if (testSuite == null) return;

		errorText = testSuite.getAssertionErrorProtocol().get(errorFileName);
		if (errorText == null) errorText = "";
		if (assertText != null && assertText.length() > 0) errorText += "\n" + assertText;
		if (additionalText != null && additionalText.length() > 0) errorText += "\n" + additionalText;
		testSuite.getAssertionErrorProtocol().put(errorFileName, errorText);
		testSuite.increaseAssertionNumberOfErrors();
	}

	/**
	 * Push an error message to the error protocols of a test runner and a test
	 * suite.
	 * 
	 * @param testRunner
	 *            The test runner that invoked this method, or <TT>null</TT> if
	 *            no test container is available.
	 * 
	 * @param assertText
	 *            Headline text that describes the assertion.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 */
	public static synchronized void addErrorToErrorProtocolLists(TestRunner testRunner, String assertText, String additionalText)
	{
		// Check parameter
		if (testRunner == null) return;
		if (assertText == null && additionalText == null) return;

		// Gets error file name as the identifier of the error protocol list
		String errorFileName = "Error on processing test runner";

		// Add error messages to: Test Runner
		String errorText = testRunner.getAssertionErrorProtocol().get(errorFileName);
		if (errorText == null) errorText = "";
		if (assertText != null && assertText.length() > 0) errorText += "\n" + assertText;
		if (additionalText != null && additionalText.length() > 0) errorText += "\n" + additionalText + "\n";
		testRunner.getAssertionErrorProtocol().put(errorFileName, errorText);
		testRunner.increaseAssertionNumberOfErrors();

		// Add error messages to: Test Suite
		TestSuite testSuite = testRunner.getTestSuite();
		if (testSuite == null) return;

		errorText = testSuite.getAssertionErrorProtocol().get(errorFileName);
		if (errorText == null) errorText = "";
		if (assertText != null && assertText.length() > 0) errorText += "\n" + assertText;
		if (additionalText != null && additionalText.length() > 0) errorText += "\n" + additionalText + "\n";
		testSuite.getAssertionErrorProtocol().put(errorFileName, errorText);
		testSuite.increaseAssertionNumberOfErrors();
	}

	/**
	 * Get the absolute file path for the <TT>Resource</TT> directory of the
	 * test environment, regarding a specific test case.
	 * <p>
	 * The result file path is combined of the root path of the development
	 * environment, and the sub path "test", and the name of the test case, and
	 * the <TT>Resource</TT> sub path.
	 * <p>
	 * For example, if the root path of the development environment is set to
	 * "c:/fmagic" and the test case is named "mediatest" you will get:
	 * <p>
	 * <TT>c:/fmagic/test/mediatest/fmagic.resource</TT>
	 * 
	 * @param context
	 *            The application context.
	 * 
	 * @return Returns the file path or <TT>null</TT> if an error occurred.
	 */
	public static String getTestResourceFilePath(Context context)
	{
		String filePath = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getTestResourceSubPath());
		filePath = FileLocationFunctions.replacePlacholder(context, filePath);

		return filePath;
	}

	/**
	 * Get the absolute file path for the <TT>Test Stuff</TT> directory of the
	 * test environment, regarding a specific test case. This directory holds
	 * additional files and information needed for running a test case, e. g.
	 * image files, documents and others.
	 * <p>
	 * The result file path is combined of the root path of the development
	 * environment, and the sub path "test", and the name of the test case, and
	 * the <TT>Test Stuff</TT> sub path.
	 * <p>
	 * For example, if the root path of the development environment is set to
	 * "c:/fmagic" and the test case is named "mediatest" you will get:
	 * <p>
	 * <TT>c:/fmagic/test/mediatest/fmagic.stuff</TT>
	 * 
	 * @param context
	 *            The application context.
	 * 
	 * @return Returns the file path or <TT>null</TT> if an error occurred.
	 */
	public static String getTestStuffFilePath(Context context)
	{
		String filePath = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getTestStuffSubPath());
		filePath = FileLocationFunctions.replacePlacholder(context, filePath);

		return filePath;
	}

	/**
	 * Get the absolute file path for the <TT>Configuration</TT> directory of
	 * the test environment, regarding a specific test case.
	 * <p>
	 * The result file path is combined of the root path of the development
	 * environment, and the sub path "test", and the name of the test case, and
	 * the <TT>Configuration</TT> sub path.
	 * <p>
	 * For example, if the root path of the development environment is set to
	 * "c:/fmagic" and the test case is named "mediatest" you will get:
	 * <p>
	 * <TT>c:/fmagic/test/mediatest/fmagic.configuration</TT>
	 * 
	 * @param context
	 *            The application context.
	 * 
	 * @return Returns the file path or <TT>null</TT> if an error occurred.
	 */
	public static String getTestConfigurationFilePath(Context context)
	{
		String filePath = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getTestConfigurationSubPath());
		filePath = FileLocationFunctions.replacePlacholder(context, filePath);

		return filePath;
	}

	/**
	 * Get the absolute file path for the <TT>License</TT> directory of the test
	 * environment, regarding a specific test case.
	 * <p>
	 * The result file path is combined of the root path of the development
	 * environment, and the sub path "test", and the name of the test case, and
	 * the <TT>License</TT> sub path.
	 * <p>
	 * For example, if the root path of the development environment is set to
	 * "c:/fmagic" and the test case is named "mediatest" you will get:
	 * <p>
	 * <TT>c:/fmagic/test/mediatest/fmagic.license</TT>
	 * 
	 * @param context
	 *            The application context.
	 * 
	 * @return Returns the file path or <TT>null</TT> if an error occurred.
	 */
	public static String getTestLicenseFilePath(Context context)
	{
		String filePath = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getTestLicenseSubPath());
		filePath = FileLocationFunctions.replacePlacholder(context, filePath);

		return filePath;
	}

	/**
	 * Get the absolute file path for the <TT>Local data</TT> directory of the
	 * test environment, regarding a specific test case.
	 * <p>
	 * The result file path is combined of the root path of the development
	 * environment, and the sub path "test", and the name of the test case, and
	 * the <TT>License</TT> sub path.
	 * <p>
	 * For example, if the root path of the development environment is set to
	 * "c:/fmagic" and the test case is named "mediatest" you will get:
	 * <p>
	 * <TT>c:/fmagic/test/mediatest/fmagic.localdata</TT>
	 * 
	 * @param context
	 *            The application context.
	 * 
	 * @return Returns the file path or <TT>null</TT> if an error occurred.
	 */
	public static String getTestLocaldataFilePath(Context context)
	{
		String filePath = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getTestLocaldataSubPath());
		filePath = FileLocationFunctions.replacePlacholder(context, filePath);

		return filePath;
	}

	/**
	 * Clean all files in the test session directory, if the directory already
	 * exists.
	 * <p>
	 * The file path of the test session directory is combined of the root path
	 * of the development environment, and the sub path "test", and the name of
	 * the test case, and the <TT>Logging</TT> sub path, and the name of the
	 * test session.
	 * <p>
	 * For example, if the root path of the development environment is set to
	 * "c:/fmagic" and the test case is named "mediatest", and the test session
	 * is named with "basic" you will get:
	 * <p>
	 * <TT>c:/fmagic/test/mediatest/fmagic.logging/basic</TT>
	 * 
	 * @param context
	 *            The application context.
	 */
	public static void cleanTestSessionDirectory(TestRunner testRunner)
	{
		String filePath = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getTestLoggingSubPath(), FileLocationFunctions.getTestLoggingSubSubPath());

		filePath = filePath.replace("${testcasename}", testRunner.getTestRunnerName());
		filePath = filePath.replace("${testsession}", testRunner.getTestSessionName());

		FileUtilFunctions.directoryDeleteAllFiles(filePath);
	}

	/**
	 * Format a assert message to a common string format.
	 * 
	 * @param assertText
	 *            Headline text that describes the assertion.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @return Returns the formatted string.
	 */
	private String serviceFormatter(String codeName, String assertText, String additionalText)
	{
		// Initialize variables
		String formattedString = "\n\n";

		// Add date string
		Date messageDate = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		formattedString += simpleDateFormat.format(messageDate);

		// Add code name
		formattedString += " [" + codeName + "]";

		// Add thread identification
		formattedString += " [" + String.format("%04d", Thread.currentThread().getId()) + "]";

		// Add assertion headline
		if (assertText == null) assertText = "";
		formattedString += " " + assertText + "\n";

		// Add additional text
		if (additionalText != null && additionalText.length() > 0)
		{
			formattedString += additionalText;
		}

		return formattedString;
	}

	/**
	 * Write an <TT>assert</TT> text to a test assertion file.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param assertText
	 *            Headline text that describes the assertion.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 */
	private void writeMessageToAssertfile(Context context, String assertText, String additionalText)
	{
		this.writeMessageToAssertFile(context, assertText, additionalText, true);
	}

	/**
	 * Write an <TT>assert</TT> text to a test assertion file.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param assertText
	 *            Headline text that describes the assertion.
	 * 
	 * @param formatterMark
	 *            Set to <TT>true</TT> if the <TT>assert</TT> message has to be
	 *            formatted with date, time and other information, otherwise set
	 *            to <TT>false</TT>.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 */
	private void writeMessageToAssertFile(Context context, String assertText, String additionalText, boolean formatterMark)
	{
		// Normalize new line first
		String normalizedText = null;

		if ((assertText == null || assertText.length() == 0) && (additionalText == null || additionalText.length() == 0))
		{
			normalizedText = ".";
		}
		else
		{
			if (formatterMark == true)
			{
				normalizedText = serviceFormatter(context.getCodeName(), assertText, additionalText);
			}
			else
			{
				normalizedText = FileUtilFunctions.generalNormalizeNewLine("\n" + assertText + "\n");
			}
		}

		// Write message to assert file
		try
		{
			String pathName;
			String fileName;
			File directory;
			PrintWriter output;

			// Gets file path and file name
			pathName = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getTestLoggingSubPath(), FileLocationFunctions.getTestLoggingSubSubPath());
			pathName = FileLocationFunctions.replacePlacholder(context, pathName);

			fileName = FileLocationFunctions.getTestLoggingAssertFileName();
			fileName = FileLocationFunctions.replacePlacholder(context, fileName);

			// Create directory
			directory = new File(pathName);
			directory.mkdirs();

			// Write to log file
			output = new PrintWriter(new FileOutputStream(new File(pathName, fileName), true));
			this.appendStringToLogFile(output, normalizedText);
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

	/**
	 * Write an <TT>assert error</TT> text to a test <TT>assertion error</TT>
	 * file.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param assertText
	 *            Headline text that describes the assertion.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 */
	private void writeMessageToErrorFile(Context context, String assertText, String additionalText)
	{
		// Normalize new line first
		String normalizedText = FileUtilFunctions.generalNormalizeNewLine(serviceFormatter(context.getCodeName(), assertText, additionalText));

		// Write message to assert file
		try
		{
			String pathName;
			String fileName;
			File directory;
			PrintWriter output;

			// Gets file path and file name
			pathName = FileLocationFunctions.compileFilePath(FileLocationFunctions.getRootPath(), FileLocationFunctions.getTestLoggingSubPath(), FileLocationFunctions.getTestLoggingSubSubPath());
			pathName = FileLocationFunctions.replacePlacholder(context, pathName);

			fileName = FileLocationFunctions.getTestLoggingErrorFileName();
			fileName = FileLocationFunctions.replacePlacholder(context, fileName);

			// Create directory
			directory = new File(pathName);
			directory.mkdirs();

			// Write to log file
			output = new PrintWriter(new FileOutputStream(new File(pathName, fileName), true));
			this.appendStringToLogFile(output, normalizedText);
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

	/**
	 * Append a single text block to a assert file.
	 * <p>
	 * Please note: The appending to the assert file is done with the help of
	 * the synchronized method appendStringToLogFile(), in order to ensure that
	 * a message block is written coherently.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param assertText
	 *            Text of the message to be logged.
	 */
	private synchronized void appendStringToLogFile(PrintWriter output, String assertText)
	{
		// Check variables
		if (output == null) return;
		if (assertText == null) return;
		if (assertText.length() == 0) return;

		// Write to log file
		output.append(assertText);
		output.flush();
	}

	/**
	 * Assert: Print text to the assertion file.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param assertText
	 *            Headline text that describes the assertion.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 */
	public static void servicePrintHeader(Context context, String assertText, String additionalText)
	{
		if (assertText == null) assertText = "";
		context.getTestManager().writeMessageToAssertfile(context, assertText, additionalText);
	}

	/**
	 * Assert: Print a sub line text to the assertion file.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param assertText
	 *            Headline text that describes the assertion.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 */
	public static void servicePrintSubLine(Context context, String assertText)
	{
		if (assertText == null) assertText = "";
		context.getTestManager().writeMessageToAssertFile(context, assertText, null, false);
	}

	/**
	 * Publish a runtime error message notified by the system.
	 * 
	 * @return Returns the error text of the error message of the system, if
	 *         available.
	 */
	private String servicePrintRuntimeError()
	{
		// Prepare variables
		String errorText = "";

		// Get error text, if available
		if (this.errorIsRuntimeError())
		{
			errorText += "\n";

			errorText += "\n--> Error code reported by the system: '" + this.runtimeErrorIdentifier + "'";

			if (this.runtimeErrorAdditionalText != null)
			{
				errorText += "\n" + this.runtimeErrorAdditionalText;
			}

			if (this.runtimeErrorException != null)
			{
				Writer writer = new StringWriter();
				PrintWriter printWriter = new PrintWriter(writer);
				this.runtimeErrorException.printStackTrace(printWriter);
				String stackTraceText = writer.toString();

				if (stackTraceText != null && stackTraceText.length() > 0)
				{
					errorText += "\n\n" + stackTraceText;
				}
			}
		}

		// Return
		return errorText;
	}

	/**
	 * Assert: Print error message to the assertion file.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param assertText
	 *            Headline text that describes the assertion.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 */
	public static void servicePrintError(Context context, TestContainer testContainer, String assertText, String additionalText)
	{
		// Check parameter
		if (assertText == null || assertText.length() == 0) assertText = "Assertion message";

		// Create exception
		if (additionalText == null) additionalText = "";

		try
		{
			throw new Exception();
		}
		catch (Exception exception)
		{
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			exception.printStackTrace(printWriter);
			String stackTraceText = writer.toString();

			if (stackTraceText != null && stackTraceText.length() > 0)
			{
				additionalText += "\n\n" + stackTraceText;
			}
		}

		// Publish runtime error message notified by the system
		if (context.getTestManager().errorIsRuntimeError())
		{
			additionalText += context.getTestManager().servicePrintRuntimeError();
		}

		// Print message to regular assert file
		context.getTestManager().writeMessageToAssertfile(context, assertText, additionalText);

		// Print message to error file
		context.getTestManager().writeMessageToErrorFile(context, assertText, additionalText);

		// Push message to the error protocols of test container, test runner
		// and test suite
		TestManager.addErrorToErrorProtocolLists(context, testContainer, assertText, additionalText);

		// Clear last error message
		context.getTestManager().errorClearErrorMessage();
		context.getTestManager().errorClearSuppressedErrorCode();
	}

	/**
	 * Assert: Print exception to the assertion file.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param exception
	 *            Exception to be printed.
	 */
	public static void servicePrintException(Context context, TestContainer testContainer, String additionalText, Exception exception)
	{
		if (exception == null) return;

		try
		{
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			exception.printStackTrace(printWriter);
			String stackTraceText = writer.toString();

			if (stackTraceText == null || stackTraceText.length() == 0) return;

			String errorText = "Exception" + "\n\n" + stackTraceText;
			context.getTestManager().writeMessageToAssertfile(context, additionalText, errorText);
		}
		catch (Exception e)
		{
			// Be silent
		}
	}

	/**
	 * Assert: Write a progress character to the assert file.
	 * 
	 * @param context
	 *            Application context of the message.
	 */
	public static void servicePrintProgress(Context context, TestContainer testContainer)
	{
		try
		{
			// Write progress to the assert file
			context.getTestManager().writeMessageToAssertfile(context, null, null);

			// Publish runtime error message notified by the system
			TestManager testManager = context.getTestManager();

			if (testManager.errorIsRuntimeError())
			{
				if (testManager.errorToBeSuppressedErrorIdentifier == null || !testManager.errorToBeSuppressedErrorIdentifier.equals(testManager.runtimeErrorIdentifier))
				{
					String assertText = "\n\nRUNTIME ERROR REPORTED";
					String errorText = context.getTestManager().servicePrintRuntimeError();

					// Print message to regular assert file
					context.getTestManager().writeMessageToAssertfile(context, assertText, errorText);

					// Print message to error file
					context.getTestManager().writeMessageToErrorFile(context, assertText, errorText);

					// Push message to the error protocols of test container,
					// test
					// runner and test suite
					TestManager.addErrorToErrorProtocolLists(context, testContainer, assertText, errorText);
				}
			}

			// Clear last error message
			context.getTestManager().errorClearErrorMessage();
			context.getTestManager().errorClearSuppressedErrorCode();
		}
		catch (Exception e)
		{
			// Be silent
		}
	}

	/**
	 * Assert: Compare if <TT>String</TT> values are equal, with considering
	 * lower and upper cases.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value1
	 *            The first value.
	 * 
	 * @param value2
	 *            The second value.
	 */
	public static void assertEquals(Context context, TestContainer testContainer, String additionalText, String value1, String value2)
	{
		if (!value1.equals(value2))
		{
			String assertText = "Assertion failed: Comparing string (considering lower and upper cases)";
			assertText += "\n--> String value 1: '" + value1 + "'";
			assertText += "\n--> String value 2: '" + value2 + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Compare if <TT>String</TT> values are <TT>NOT</TT>equal, with
	 * considering lower and upper cases.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value1
	 *            The first value.
	 * 
	 * @param value2
	 *            The second value.
	 */
	public static void assertNotEquals(Context context, TestContainer testContainer, String additionalText, String value1, String value2)
	{
		if (value1.equals(value2))
		{
			String assertText = "Assertion failed: Comparing string on diversity (considering lower and upper cases)";
			assertText += "\n--> String value 1: '" + value1 + "'";
			assertText += "\n--> String value 2: '" + value2 + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Compare if <TT>String</TT> values are <TT>NOT</TT>equal, ignoring
	 * lower and upper cases.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value1
	 *            The first value.
	 * 
	 * @param value2
	 *            The second value.
	 */
	public static void assertNotEqualsIgnoreCase(Context context, TestContainer testContainer, String additionalText, String value1, String value2)
	{
		if (value1.equalsIgnoreCase(value2))
		{
			String assertText = "Assertion failed: Comparing string on diversity (ignoring lower and upper cases)";
			assertText += "\n--> String value 1: '" + value1 + "'";
			assertText += "\n--> String value 2: '" + value2 + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Compare if <TT>String</TT> values are equal, ignoring lower and
	 * upper cases.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value1
	 *            The first value.
	 * 
	 * @param value2
	 *            The second value.
	 */
	public static void assertEqualsIgnoreCase(Context context, TestContainer testContainer, String additionalText, String value1, String value2)
	{
		if (!value1.equalsIgnoreCase(value2))
		{
			String assertText = "Assertion failed: Comparing string (ignoring lower and upper cases)";
			assertText += "\n--> String value 1: '" + value1 + "'";
			assertText += "\n--> String value 2: '" + value2 + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Check if <TT>String</TT> value 1 ends with value 2.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value1
	 *            The first value.
	 * 
	 * @param value2
	 *            The second value.
	 */
	public static void assertEndsWith(Context context, TestContainer testContainer, String additionalText, String value1, String value2)
	{
		if (!value1.endsWith(value2))
		{
			String assertText = "Assertion failed: String 1 doesn't end with string 2";
			assertText += "\n--> String value 1: '" + value1 + "'";
			assertText += "\n--> String value 2: '" + value2 + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Compare if <TT>String</TT> value 1 contains <TT>String</TT> value
	 * 2.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value1
	 *            The first value.
	 * 
	 * @param value2
	 *            The second value.
	 */
	public static void assertContains(Context context, TestContainer testContainer, String additionalText, String value1, String value2)
	{
		if (!value1.contains(value2))
		{
			String assertText = "Assertion failed: String value 1 doesn't contain string value 2";
			assertText += "\n--> String value 1:\n'" + value1 + "'\n";
			assertText += "\n--> String value 2:\n'" + value2 + "'\n";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Assert that <TT>String</TT> value 1 <TT>NOT</TT> contains
	 * <TT>String</TT> value 2.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value1
	 *            The first value.
	 * 
	 * @param value2
	 *            The second value.
	 */
	public static void assertNotContains(Context context, TestContainer testContainer, String additionalText, String value1, String value2)
	{
		if (value1.contains(value2))
		{
			String assertText = "Assertion failed: String value 1 contains string value 2";
			assertText += "\n--> String value 1:\n'" + value1 + "'\n";
			assertText += "\n--> String value 2:\n'" + value2 + "'\n";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Compare if <TT>boolean</TT> values are equal.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value1
	 *            The first value.
	 * 
	 * @param value2
	 *            The second value.
	 */
	public static void assertEquals(Context context, TestContainer testContainer, String additionalText, boolean value1, boolean value2)
	{
		if (value1 != value2)
		{
			String assertText = "Assertion failed: Comparing boolean values (equals)";
			assertText += "\n--> Boolean value 1: '" + String.valueOf(value1) + "'";
			assertText += "\n--> Boolean value 2: '" + String.valueOf(value2) + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Compare if <TT>integer</TT> values are equal.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value1
	 *            The first value.
	 * 
	 * @param value2
	 *            The second value.
	 */
	public static void assertEquals(Context context, TestContainer testContainer, String additionalText, int value1, int value2)
	{
		if (value1 != value2)
		{
			String assertText = "Assertion failed: Comparing integer values (equals)";
			assertText += "\n--> Integer value 1: '" + String.valueOf(value1) + "'";
			assertText += "\n--> Integer value 2: '" + String.valueOf(value2) + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Check if a range <TT>from</TT>/<TT>too</TT> is met.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param from
	 *            The value the range starts.
	 * 
	 * @param too
	 *            The value the range stops.
	 * 
	 * @param value
	 *            The value to check.
	 */
	public static void assertEqualsRange(Context context, TestContainer testContainer, String additionalText, int from, int too, int value)
	{
		if (value < from || value > too)
		{
			String assertText = "Assertion failed: Comparing integer range from/too";
			assertText += "\n--> Range from: '" + String.valueOf(from) + "'";
			assertText += "\n--> Range too: '" + String.valueOf(too) + "'";
			assertText += "\n--> Integer value: '" + String.valueOf(value) + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Compare if <TT>long</TT> values are equal.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value1
	 *            The first value.
	 * 
	 * @param value2
	 *            The second value.
	 */
	public static void assertEquals(Context context, TestContainer testContainer, String additionalText, long value1, long value2)
	{
		if (value1 != value2)
		{
			String assertText = "Assertion failed: Comparing long values (equals)";
			assertText += "\n--> Long value 1: '" + String.valueOf(value1) + "'";
			assertText += "\n--> Long value 2: '" + String.valueOf(value2) + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Check if a <TT>boolean</TT> value is <TT>true</TT>.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value
	 *            The value to assert.
	 */
	public static void assertTrue(Context context, TestContainer testContainer, String additionalText, boolean value)
	{
		if (value != true)
		{
			String assertText = "Assertion failed: Check if a boolean value is TRUE";
			assertText += "\n--> Boolean value: '" + String.valueOf(value) + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Check if a <TT>boolean</TT> value is <TT>false</TT>.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value
	 *            The value to assert.
	 */
	public static void assertFalse(Context context, TestContainer testContainer, String additionalText, boolean value)
	{
		if (value != false)
		{
			String assertText = "Assertion failed: Check if a boolean value is FALSE";
			assertText += "\n--> Boolean value: '" + String.valueOf(value) + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Check if there is a <TT>null</TT> value set.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value
	 *            The value to assert.
	 */
	public static void assertNull(Context context, TestContainer testContainer, String additionalText, Object object)
	{
		if (object != null)
		{
			String assertText = "Assertion failed: Check if a NULL value is set";
			assertText += "\n--> Value: '" + object.toString() + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Check if there is an <TT>Object</TT> instantiated.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param object
	 *            The value to assert.
	 */
	public static void assertNotNull(Context context, TestContainer testContainer, String additionalText, Object object)
	{
		if (object == null)
		{
			String assertText = "Assertion failed: Check if there is an Object instantiated";
			assertText += "\n--> Value: '" + "NULL" + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Check if a string is not <TT>null</TT> and not <TT>Empty</TT>.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value
	 *            The value to check.
	 */
	public static void assertNotEmpty(Context context, TestContainer testContainer, String additionalText, String value)
	{
		if (value == null || value.length() == 0)
		{
			String assertText = "Assertion failed: String is not NULL or EMPTY";
			assertText += "\n--> String value: '" + value + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Compare if an <TT>integer</TT> value 1 is greater than value 2.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value1
	 *            The first value.
	 * 
	 * @param value2
	 *            The second value.
	 */
	public static void assertGreaterThan(Context context, TestContainer testContainer, String additionalText, int value1, int value2)
	{
		if (!(value1 > value2))
		{
			String assertText = "Assertion failed:  Compare if value 1 is greater than a value 2";
			assertText += "\n--> Integer value 1: '" + String.valueOf(value1) + "'";
			assertText += "\n--> Integer value 2: '" + String.valueOf(value2) + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Compare if an <TT>integer</TT> value 1 is lower than value 2.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value1
	 *            The first value.
	 * 
	 * @param value2
	 *            The second value.
	 */
	public static void assertLowerThan(Context context, TestContainer testContainer, String additionalText, int value1, int value2)
	{
		if (!(value1 < value2))
		{
			String assertText = "Assertion failed:  Compare if value 1 is lower than a value 2";
			assertText += "\n--> Integer value 1: '" + String.valueOf(value1) + "'";
			assertText += "\n--> Integer value 2: '" + String.valueOf(value2) + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Compare if an <TT>long</TT> value 1 is greater than value 2.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value1
	 *            The first value.
	 * 
	 * @param value2
	 *            The second value.
	 */
	public static void assertGreaterThan(Context context, TestContainer testContainer, String additionalText, long value1, long value2)
	{
		if (!(value1 > value2))
		{
			String assertText = "Assertion failed:  Compare if value 1 is greater than a value 2";
			assertText += "\n--> Long value 1: '" + String.valueOf(value1) + "'";
			assertText += "\n--> Long value 2: '" + String.valueOf(value2) + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Compare two files via checksum, if they are identical.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param filePath1
	 *            The first file.
	 * 
	 * @param filePath2
	 *            The second file.
	 */
	public static void assertEqualsFile(Context context, TestContainer testContainer, String additionalText, String filePath1, String filePath2)
	{
		long checksumFile1 = FileUtilFunctions.fileGetChecksum(filePath1);
		TestManager.assertGreaterThan(context, testContainer, additionalText + "\n--> Error on computing checksum of file '" + filePath1 + "'", checksumFile1, 0);

		long checksumFile2 = FileUtilFunctions.fileGetChecksum(filePath2);
		TestManager.assertGreaterThan(context, testContainer, additionalText + "\n--> Error on computing checksum of file '" + filePath2 + "'", checksumFile2, 0);

		if (checksumFile1 != checksumFile2)
		{
			String assertText = "Assertion failed: Comparing two files on identity";
			assertText += "\n--> File path 1: '" + filePath1 + "'";
			assertText += "\n--> File path 2: '" + filePath2 + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Compare two files via checksum, if they are different.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param filePath1
	 *            The first file.
	 * 
	 * @param filePath2
	 *            The second file.
	 */
	public static void assertNotEqualsFile(Context context, TestContainer testContainer, String additionalText, String filePath1, String filePath2)
	{
		long checksumFile1 = FileUtilFunctions.fileGetChecksum(filePath1);
		TestManager.assertGreaterThan(context, testContainer, additionalText + "\n--> Error on computing checksum of file '" + filePath1 + "'", checksumFile1, 0);

		long checksumFile2 = FileUtilFunctions.fileGetChecksum(filePath2);
		TestManager.assertGreaterThan(context, testContainer, additionalText + "\n--> Error on computing checksum of file '" + filePath2 + "'", checksumFile2, 0);

		if (checksumFile1 == checksumFile2)
		{
			String assertText = "Assertion failed: Comparing two files on diversity";
			assertText += "\n--> File path 1: '" + filePath1 + "'";
			assertText += "\n--> File path 2: '" + filePath2 + "'";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Check if a specific runtime error occurred.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param identifier
	 *            The error code to be asserted, coded as a notification
	 *            resource item.
	 */
	public static void assertRuntimeErrorCode(Context context, TestContainer testContainer, String additionalText, String identifier)
	{
		if (identifier == null) return;

		if (context.getTestManager().runtimeErrorIdentifier != null && !context.getTestManager().runtimeErrorIdentifier.equals(identifier))
		{
			String assertText = "Assertion failed: Checking on a specific runtime error code";
			assertText += "\n--> Error code to check: '" + identifier + "'";

			if (context.getTestManager().runtimeErrorIdentifier == null)
			{
				assertText += "\n--> Runtime error code: ''";
			}
			else
			{
				assertText += "\n--> Runtime error code: '" + context.getTestManager().runtimeErrorIdentifier + "'";
			}

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Check if a specific runtime error NOT occurred.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param identifier
	 *            The error code to be asserted, coded as a notification
	 *            resource item.
	 */
	public static void assertNotRuntimeErrorCode(Context context, TestContainer testContainer, String additionalText, String identifier)
	{
		if (identifier == null) return;

		if (context.getTestManager().runtimeErrorIdentifier != null && context.getTestManager().runtimeErrorIdentifier.equals(identifier))
		{
			String assertText = "Assertion failed: Excepting a specific runtime error code";
			assertText += "\n--> Error code to except: '" + identifier + "'";

			if (context.getTestManager().runtimeErrorIdentifier == null)
			{
				assertText += "\n--> Runtime error code: ''";
			}
			else
			{
				assertText += "\n--> Runtime error code: '" + context.getTestManager().runtimeErrorIdentifier + "'";
			}

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Check if any runtime error occurred.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 */
	public static void assertRuntimeError(Context context, TestContainer testContainer, String additionalText)
	{
		if (!context.getTestManager().errorIsRuntimeError())
		{
			String assertText = "Assertion failed: Checking on any runtime error";
			assertText += "\n--> No runtime error occurred";

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Assert: Except that a runtime error occurred.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param testContainer
	 *            The test container that invoked this method, or <TT>null</TT>
	 *            if no test container is available.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 */
	public static void assertNotRuntimeError(Context context, TestContainer testContainer, String additionalText)
	{
		if (context.getTestManager().errorIsRuntimeError())
		{
			String assertText = "Assertion failed: Except any runtime error";

			if (context.getTestManager().runtimeErrorIdentifier != null)
			{
				assertText += "\n--> Runtime error code: '" + context.getTestManager().runtimeErrorIdentifier + "'";
			}

			TestManager.servicePrintError(context, testContainer, assertText, additionalText);
		}
		else
		{
			TestManager.servicePrintProgress(context, testContainer);
		}
	}

	/**
	 * Print the error protocol of a test container, test runner or test suite.
	 * 
	 * @param assertionNumberOfErrors
	 *            Number of errors that occurred.
	 * 
	 * @param assertionErrorProtocol
	 *            The assertion error protocol map.
	 * 
	 * @return Returns the content of the error protocol as string or
	 *         <TT>null</TT> if no errors were found.
	 */
	public static String printAssertionErrorProtocol(int assertionNumberOfErrors, HashMap<String, String> assertionErrorProtocol)
	{
		// Validate
		if (assertionNumberOfErrors <= 0) return null;
		if (assertionErrorProtocol == null) return null;
		if (assertionErrorProtocol.size() == 0) return null;

		// Initialize
		String returnText = "";

		// Sort list on file names
		List<String> sortedList = new ArrayList<String>(assertionErrorProtocol.keySet());
		Collections.sort(sortedList);

		// Print all items into list
		Iterator<String> iterItems = sortedList.iterator();

		while (iterItems.hasNext())
		{
			String protocolFileName = iterItems.next();
			if (protocolFileName == null) continue;
			if (protocolFileName.length() == 0) continue;

			String errortext = assertionErrorProtocol.get(protocolFileName);
			if (errortext == null) continue;
			if (errortext.length() == 0) continue;

			returnText += "\n:::::::::: '" + protocolFileName + "'\n";
			returnText += errortext;
		}

		// Return
		return returnText;
	}

	/**
	 * Error: Set an error message code that is to be suppressed once by the
	 * notification manager.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param identifier
	 *            The resource identifier of the error notification.
	 */
	public synchronized static void errorSuppressErrorMessageOnce(Context context, String identifier)
	{
		context.getTestManager().errorToBeSuppressedErrorIdentifier = identifier;
	}

	/**
	 * Error: Check if a specific error code is to be suppressed by the
	 * notification manager.
	 * <p>
	 * After checking the status the error code to be suppressed is reset by
	 * this method automatically.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param identifier
	 *            The resource identifier of the error notification.
	 * 
	 * @return Returns <TT>true</TT> if the error message has to be suppressed,
	 *         otherwise <TT>false</TT>.
	 */
	public synchronized static boolean errorIsSuppressedErrorMessage(Context context, String identifier)
	{
		if (context.getTestManager().errorToBeSuppressedErrorIdentifier == null) return false;
		if (identifier == null) return false;

		boolean isToBeSuppressed = false;

		if (context.getTestManager().errorToBeSuppressedErrorIdentifier.equals(identifier)) isToBeSuppressed = true;

		return isToBeSuppressed;
	}

	/**
	 * Error: Notify an error that occurred to the test manager.
	 * <p>
	 * After checking the status the error code to be suppressed is reset by
	 * this method automatically.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param identifier
	 *            The resource identifier of the error notification.
	 * 
	 * @param additionalText
	 *            An additional text to notify.
	 * 
	 * @param exception
	 *            An exception to notify.
	 */
	public synchronized static void errorNotifyErrorMessage(Context context, String identifier, String additionalText, Exception exception)
	{
		context.getTestManager().errorClearErrorMessage();

		if (identifier == null) return;

		context.getTestManager().runtimeErrorIdentifier = identifier;
		context.getTestManager().runtimeErrorAdditionalText = additionalText;
		context.getTestManager().runtimeErrorException = exception;
	}

	/**
	 * Error: Clear error notification in the test manager.
	 */
	private synchronized void errorClearErrorMessage()
	{
		this.runtimeErrorIdentifier = null;
		this.runtimeErrorAdditionalText = null;
		this.runtimeErrorException = null;
	}

	/**
	 * Error: Clear suppressed error code.
	 */
	private synchronized void errorClearSuppressedErrorCode()
	{
		this.errorToBeSuppressedErrorIdentifier = null;
	}

	/**
	 * Error: Check if runtime error occurred.
	 */
	private boolean errorIsRuntimeError()
	{
		if (this.runtimeErrorIdentifier != null && this.runtimeErrorIdentifier.length() > 0) return true;
		return false;
	}
}
