package fmagic.basic;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;

/**
 * This class contains UTIL functions needed in the FMAGIC system.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 19.12.2012 - Created
 */
public class Util
{
	/**
	 * Normalize new line to the system standards.
	 * 
	 * @param messageText
	 *            Text to normalize.
	 * 
	 * @return Returns the normalized string.
	 */
	public static String normalizeNewLine(String messageText)
	{
		if (messageText == null) return null;

		String normalizedText = messageText.replace("\r\n", "\n");
		normalizedText = normalizedText.replace("\n", "\r\n");

		return normalizedText;
	}

	/**
	 * Convert regular wildcards "*" and "?" to <TT>regex</TT> wildcards.
	 * 
	 * @param regularPattern
	 *            Text to be converted.
	 * 
	 * @return Returns the converted string, or <TT>null</TT> if an error
	 *         occurred.
	 */
	public static String convertRegularWildcardsToRegexWildcards(String regularPattern)
	{
		if (regularPattern == null) return null;
		
		String regexPattern = null;

		try
		{
			regexPattern = regularPattern.replace("*", ".*").replace("?", ".");
		}
		catch (Exception e)
		{
			return null;
		}

		return regexPattern;
	}

	/**
	 * Checks a string if it contains special characters and transforms them to
	 * underline characters.
	 * 
	 * @param String
	 *            The string to be transformed.
	 * 
	 * @return Returns the normalized string.
	 */
	public static String fitToFileNameCompatibility(String inputString)
	{
		String outputString;
		outputString = inputString.replaceAll("[^a-zA-Z0-9-.\\[\\]]+", "_");
		return outputString;
	}

	/**
	 * Get time difference of to Date objects as number of seconds.
	 * 
	 * @param earlyDate
	 *            The first Date to compare with.
	 * 
	 * @param laterDate
	 *            The other Date to compare with.
	 * 
	 * @return Returns time difference in seconds
	 */
	public static long getTimeDifferenceInSeconds(Date earlyDate, Date laterDate)
	{
		long earlyDateSeconds = earlyDate.getTime() / 1000;
		long laterDateSeconds = laterDate.getTime() / 1000;
		long timeDiff = laterDateSeconds - earlyDateSeconds;
		return timeDiff;
	}

	/**
	 * Wait for the end of a thread, but after maximum of x seconds the method
	 * always returns.
	 * 
	 * @param thread
	 *            The thread to consider.
	 * 
	 * @param maxTimeToWaitInSeconds
	 *            Maximum number of seconds to wait.
	 */
	public static void waitForThreadTerminating(Thread thread, int maxTimeToWaitInSeconds)
	{
		int counter = maxTimeToWaitInSeconds * 10;

		while (counter-- > 0)
		{
			if (!thread.isAlive()) break;

			try
			{
				Thread.sleep(100);
			}
			catch (Exception e)
			{
			}
		}

		return;
	}

	/**
	 * Executing a pause for some seconds.
	 * 
	 * @param seconds
	 *            Number of seconds to pause.
	 */
	public static void sleepSeconds(int seconds)
	{
		try
		{
			Thread.sleep(seconds * 1000);
		}
		catch (InterruptedException e)
		{
		}
	}

	/**
	 * Get all information about a class by using Java reflection.
	 * 
	 * @param classToRefer
	 *            The class to reflect.
	 * 
	 * @return Returns a text string containing all information.
	 */
	public static String getClassInfo(Class<?> classToRefer)
	{
		String returnString = "";

		// ------------------------------------
		// Return if there is a NULL reference
		if (classToRefer == null)
		{
			returnString += "\n" + "NULL reference";
			return returnString;
		}

		// ------------------------------------
		// Type of Class is CLASS
		boolean isClass = !(classToRefer.isInterface() || classToRefer.isPrimitive() || classToRefer.isArray());

		// ------------------------------------
		// Name of Class
		returnString += "\n" + "----------";

		returnString += "\n" + "Default name: " + classToRefer.toString();
		returnString += "\n" + "Simple name: " + classToRefer.getSimpleName();
		returnString += "\n" + "Name: " + classToRefer.getName();

		// ------------------------------------
		// List of Superclass of Class
		returnString += "\n" + "----------";

		if (isClass)
		{
			returnString += "\n" + "Superclasses";

			Class<?> subclass = classToRefer;
			Class<?> superclass = subclass.getSuperclass();

			while (superclass != null)
			{
				String className = superclass.getSimpleName();
				returnString += "\n" + "--> " + className;
				subclass = superclass;
				superclass = subclass.getSuperclass();
			}
		}
		else
		{
			returnString += "\n" + "Superclass: NONE";
		}

		// ------------------------------------
		// Type of Class
		returnString += "\n" + "----------";

		returnString += "\n" + "Is Interface: " + classToRefer.isInterface();
		returnString += "\n" + "Is Primitive: " + classToRefer.isPrimitive();
		returnString += "\n" + "Is Array: " + classToRefer.isArray();
		returnString += "\n" + "Is Class: " + isClass;

		if (classToRefer.isArray()) returnString += "\n" + "Komponententyp: " + classToRefer.getComponentType();
		else returnString += "\n" + "Komponententyp: NONE";

		// ------------------------------------
		// Get Instance and Assignable classes
		returnString += "\n" + "----------";

		if (Serializable.class.isAssignableFrom(classToRefer)) returnString += "\n" + "Is Assignable From Object: true";
		else returnString += "\n" + "Is Assignable From Object: false";

		// ------------------------------------
		// Get all interfaces
		if ((classToRefer.getInterfaces()).length != 0)
		{
			returnString += "\n" + "----------";
			returnString += "\n" + "Interfaces";

			for (Class<?> Interface : classToRefer.getInterfaces())
				returnString += "\n" + "--> " + Interface.getSimpleName();
		}

		// ------------------------------------
		// Get modifiers (public, private, protected)
		int modifiers = classToRefer.getModifiers();

		if (modifiers != 0)
		{
			returnString += "\n" + "----------";
			returnString += "\n" + "Modifiers: " + Modifier.toString(modifiers);
			if (Modifier.isPublic(modifiers)) returnString += "\n" + "PUBLIC";
			if (Modifier.isPrivate(modifiers)) returnString += "\n" + "PRIVATE";
			if (Modifier.isProtected(modifiers)) returnString += "\n" + "PROTECTED";
		}

		// ------------------------------------
		// Get public data fields of the class
		if ((classToRefer.getFields()).length > 0)
		{
			returnString += "\n" + "----------";
			returnString += "\n" + "Public data fields";

			for (Field publicField : classToRefer.getFields())
			{
				String fieldName = publicField.getName();
				String fieldType = publicField.getType().getSimpleName();
				String declaringClass = publicField.getDeclaringClass().getSimpleName();

				returnString += "\n" + String.format("--> %s %s [%s]%n", fieldType, fieldName, declaringClass);
			}
		}

		// ------------------------------------
		// Get public methods of class
		if ((classToRefer.getMethods()).length > 0)
		{
			returnString += "\n" + "----------";
			returnString += "\n" + "Methods";

			for (Method method : classToRefer.getMethods())
			{
				// Name and Type
				String methodName = method.getName();
				String methodType = method.getReturnType().getSimpleName();
				String declaringClass = method.getDeclaringClass().getSimpleName();

				// Parameters
				Class<?>[] parameterTypes = method.getParameterTypes();

				String parameterString = "";

				for (int k = 0; k < parameterTypes.length; k++)
				{
					if (k > 0) parameterString = parameterString + ", ";
					parameterString = parameterString + parameterTypes[k].getSimpleName();
				}

				// Exceptions
				Class<?>[] exceptions = method.getExceptionTypes();

				String exceptionString = "";

				if (exceptions.length > 0)
				{
					exceptionString = exceptionString + " throws ";

					for (int k = 0; k < exceptions.length; k++)
					{
						if (k > 0) exceptionString = exceptionString + ", ";
						exceptionString = exceptionString + exceptions[k].getName();
					}
				}

				// Print out for all native methods
				if (!declaringClass.equals("Object"))
				{
					returnString += "\n" + String.format("--> %s %s(%s) %s [%s]%n", methodType, methodName, parameterString, exceptionString, declaringClass);
				}
			}
		}

		// ------------------------------------
		// Get constructor methods of class
		if ((classToRefer.getConstructors()).length > 0)
		{
			returnString += "\n" + "----------";
			returnString += "\n" + "Constructors";

			for (Constructor<?> constructor : classToRefer.getConstructors())
			{
				// Name and Type
				String methodName = classToRefer.getSimpleName();
				String methodType = classToRefer.getSimpleName();

				// Parameters
				Class<?>[] parameterTypes = constructor.getParameterTypes();

				String parameterString = "";

				for (int k = 0; k < parameterTypes.length; k++)
				{
					if (k > 0) parameterString = parameterString + ", ";
					parameterString = parameterString + parameterTypes[k].getSimpleName();
				}

				// Exceptions
				Class<?>[] exceptions = constructor.getExceptionTypes();

				String exceptionString = "";

				if (exceptions.length > 0)
				{
					exceptionString = exceptionString + " throws ";

					for (int k = 0; k < exceptions.length; k++)
					{
						if (k > 0) exceptionString = exceptionString + ", ";
						exceptionString = exceptionString + exceptions[k].getName();
					}
				}

				// Print out for all constructor methods
				returnString += "\n" + String.format("--> %s %s(%s) %s%n", methodType, methodName, parameterString, exceptionString);
			}
		}

		// ------------------------------------
		// Get annotations of class
		if ((classToRefer.getAnnotations()).length > 0)
		{
			returnString += "\n" + "----------";
			returnString += "\n" + "Annotations";

			for (Annotation annotation : classToRefer.getAnnotations())
			{
				// Name and Type
				String annotationName = annotation.toString();

				// Print out for all annotations
				returnString += "\n" + String.format("--> %s%n", annotationName);
			}
		}

		// ------------------------------------
		// Return
		return returnString;
	}
}
