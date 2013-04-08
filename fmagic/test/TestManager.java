package fmagic.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fmagic.basic.Context;
import fmagic.basic.FileLocationManager;
import fmagic.basic.ManagerInterface;
import fmagic.basic.Util;

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
	private boolean errorFound = false;

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

	/**
	 * Getter
	 */
	public boolean isErrorFound()
	{
		return errorFound;
	}

	/**
	 * Setter
	 */
	public void setErrorFound()
	{
		this.errorFound = true;
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
	private String assertFormatter(String assertText, String additionalText)
	{
		// Initialize variables
		String formattedString = "\n\n";

		// Add date string
		Date messageDate = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		formattedString += simpleDateFormat.format(messageDate);

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
		// Normalize new line first
		String normalizedText = null;

		if ((assertText == null || assertText.length() == 0) && (additionalText == null || additionalText.length() == 0))
		{
			normalizedText = ".";
		}
		else
		{
			normalizedText = Util.normalizeNewLine(assertFormatter(assertText, additionalText));
		}

		// Write message to assert file
		try
		{
			String pathName;
			String fileName;
			File directory;
			PrintWriter output;

			// Gets file path and file name
			pathName = FileLocationManager.getRootPath() + FileLocationManager.getPathElementDelimiterString() + FileLocationManager.getTestSubPath() + FileLocationManager.getPathElementDelimiterString() + FileLocationManager.getTestSubSubPath();
			pathName = FileLocationManager.replacePlacholder(context, pathName);

			fileName = FileLocationManager.getTestAssertFileName();
			fileName = FileLocationManager.replacePlacholder(context, fileName);

			// Create directory
			directory = new File(pathName);
			directory.mkdirs();

			// Write to log file
			output = new PrintWriter(new FileOutputStream(new File(pathName + FileLocationManager.getPathElementDelimiterString() + fileName), true));
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
	public static void assertPrint(Context context, String assertText, String additionalText)
	{
		if (assertText == null) assertText = "";
		context.getTestManager().writeMessageToAssertfile(context, assertText, additionalText);
	}

	/**
	 * Assert: Print error message to the assertion file.
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
	public static void assertPrintError(Context context, String assertText, String additionalText)
	{
		if (assertText == null || assertText.length() == 0) assertText = "Assertion message";

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
				if (additionalText == null) additionalText = "";
				additionalText += "\n\n" + stackTraceText;
			}
		}

		context.getTestManager().writeMessageToAssertfile(context, assertText, additionalText);
	}

	/**
	 * Assert: Print exception to the assertion file.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param exception
	 *            Exception to be printed.
	 */
	public static void assertPrintException(Context context, String additionalText, Exception exception)
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
	public static void assertProgress(Context context)
	{
		context.getTestManager().writeMessageToAssertfile(context, null, null);
	}

	/**
	 * Assert: Compare if <TT>String</TT> values are equal, with considering
	 * lower and upper cases.
	 * 
	 * @param context
	 *            Application context of the message.
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
	public static void assertEquals(Context context, String additionalText, String value1, String value2)
	{
		if (!value1.equals(value2))
		{
			context.getTestManager().setErrorFound();

			String assertText = "Assertion failed: Comparing string (considering lower and upper cases)";
			assertText += "\n--> String value 1: '" + value1 + "'";
			assertText += "\n--> String value 2: '" + value2 + "'";

			TestManager.assertPrintError(context, assertText, additionalText);
		}
		else
		{
			TestManager.assertProgress(context);
		}
	}

	/**
	 * Assert: Compare if <TT>String</TT> values are equal, ignoring lower and
	 * upper cases.
	 * 
	 * @param context
	 *            Application context of the message.
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
	public static void assertEqualsIgnoreCase(Context context, String additionalText, String value1, String value2)
	{
		if (!value1.equalsIgnoreCase(value2))
		{
			context.getTestManager().setErrorFound();

			String assertText = "Assertion failed: Comparing string (ignoring lower and upper cases)";
			assertText += "\n--> String value 1: '" + value1 + "'";
			assertText += "\n--> String value 2: '" + value2 + "'";

			TestManager.assertPrintError(context, assertText, additionalText);
		}
		else
		{
			TestManager.assertProgress(context);
		}
	}

	/**
	 * Assert: Compare if <TT>String</TT> value 1 contains <TT>String</TT> value
	 * 2.
	 * 
	 * @param context
	 *            Application context of the message.
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
	public static void assertContains(Context context, String additionalText, String value1, String value2)
	{
		if (!value1.contains(value2))
		{
			context.getTestManager().setErrorFound();

			String assertText = "Assertion failed: String value 1 doesn't contain string value 2";
			assertText += "\n--> String value 1: '" + value1 + "'";
			assertText += "\n--> String value 2: '" + value2 + "'";

			TestManager.assertPrintError(context, assertText, additionalText);
		}
		else
		{
			TestManager.assertProgress(context);
		}
	}

	/**
	 * Assert: Compare if <TT>boolean</TT> values are equal.
	 * 
	 * @param context
	 *            Application context of the message.
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
	public static void assertEquals(Context context, String additionalText, boolean value1, boolean value2)
	{
		if (value1 != value2)
		{
			context.getTestManager().setErrorFound();

			String assertText = "Assertion failed: Comparing boolean values";
			assertText += "\n--> Boolean value 1: '" + String.valueOf(value1) + "'";
			assertText += "\n--> Boolean value 2: '" + String.valueOf(value2) + "'";

			TestManager.assertPrintError(context, assertText, additionalText);
		}
		else
		{
			TestManager.assertProgress(context);
		}
	}

	/**
	 * Assert: Check if a <TT>boolean</TT> value is <TT>true</TT>.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value
	 *            The value to assert.
	 */
	public static void assertTrue(Context context, String additionalText, boolean value)
	{
		if (value != true)
		{
			context.getTestManager().setErrorFound();

			String assertText = "Assertion failed: Check if a boolean value is TRUE";
			assertText += "\n--> Boolean value: '" + String.valueOf(value) + "'";

			TestManager.assertPrintError(context, assertText, additionalText);
		}
		else
		{
			TestManager.assertProgress(context);
		}
	}

	/**
	 * Assert: Check if a <TT>boolean</TT> value is <TT>false</TT>.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value
	 *            The value to assert.
	 */
	public static void assertFalse(Context context, String additionalText, boolean value)
	{
		if (value != false)
		{
			context.getTestManager().setErrorFound();

			String assertText = "Assertion failed: Check if a boolean value is FALSE";
			assertText += "\n--> Boolean value: '" + String.valueOf(value) + "'";

			TestManager.assertPrintError(context, assertText, additionalText);
		}
		else
		{
			TestManager.assertProgress(context);
		}
	}

	/**
	 * Assert: Check if there is a <TT>null</TT> value set.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value
	 *            The value to assert.
	 */
	public static void assertNull(Context context, String additionalText, Object object)
	{
		if (object != null)
		{
			context.getTestManager().setErrorFound();

			String assertText = "Assertion failed: Check if a NULL value is set";
			assertText += "\n--> Value: '" + object.toString() + "'";

			TestManager.assertPrintError(context, assertText, additionalText);
		}
		else
		{
			TestManager.assertProgress(context);
		}
	}

	/**
	 * Assert: Check if there is an <TT>Object</TT> instantiated.
	 * 
	 * @param context
	 *            Application context of the message.
	 * 
	 * @param additionalText
	 *            Additional text of the message.
	 * 
	 * @param value
	 *            The value to assert.
	 */
	public static void assertNotNull(Context context, String additionalText, Object object)
	{
		if (object == null)
		{
			context.getTestManager().setErrorFound();

			String assertText = "Assertion failed: Check if there is an Object instantiated";
			assertText += "\n--> Value: '" + "NULL" + "'";

			TestManager.assertPrintError(context, assertText, additionalText);
		}
		else
		{
			TestManager.assertProgress(context);
		}
	}

	/**
	 * Assert: Compare if an <TT>integer</TT> value 1 is greater than value 2.
	 * 
	 * @param context
	 *            Application context of the message.
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
	public static void assertGreaterThan(Context context, String additionalText, int value1, int value2)
	{
		if (!(value1 > value2))
		{
			context.getTestManager().setErrorFound();

			String assertText = "Assertion failed:  Compare if value 1 is greater than a value 2";
			assertText += "\n--> Integer value 1: '" + String.valueOf(value1) + "'";
			assertText += "\n--> Integer value 2: '" + String.valueOf(value2) + "'";

			TestManager.assertPrintError(context, assertText, additionalText);
		}
		else
		{
			TestManager.assertProgress(context);
		}
	}

}
