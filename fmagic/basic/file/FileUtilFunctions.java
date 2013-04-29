package fmagic.basic.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * This class contains UTIL functions needed in the FMAGIC system.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 19.12.2012 - Created
 */
public class FileUtilFunctions
{
	//
	private static int defaultNuOfRetrials = 20;
	private static int defaultIdleTimeBetweenRetrialsInMilliseconds = 1000;

	private final static long SECOND_MILLISECONDS = 1000 * 60;
	private final static long MINUTE_MILLISECONDS = 1000 * 60;
	private final static long HOUR_MILLISECONDS = 1000 * 60 * 60;
	private final static long DAY_MILLISECONDS = 1000 * 60 * 60 * 24;
	private final static long MINUTES_PER_DAY = 1440;

	/**
	 * Get a random value between a range in <TT>integer</TT> format.
	 * 
	 * @param from
	 *            The value the value can start.
	 * 
	 * @param too
	 *            The value the value has to stop.
	 * 
	 * @return Returns the normalized string.
	 */
	public static int generalGetRandomValue(int from, int too)
	{
		// Validate parameter
		if (from == too) return from;

		if (from < too)
		{
			int temp = from;
			from = too;
			too = temp;
		}

		int range = too - from;

		// Compute
		int index = (int) (Math.random() * range);
		index = index + from;

		// Return
		return index;
	}

	/**
	 * Add some days, hours, minutes and seconds to a date value.
	 * 
	 * @param dateValue
	 *            The date value modify.
	 * 
	 * @param days
	 *            The number of days to add, positive or negative or <TT>0</TT>.
	 * 
	 * @param hours
	 *            The number of hours to add, positive or negative or <TT>0</TT>
	 *            .
	 * 
	 * @param minutes
	 *            The number of minutes to add, positive or negative or
	 *            <TT>0</TT>.
	 * 
	 * @param seconds
	 *            The number of seconds to add, positive or negative or
	 *            <TT>0</TT>.
	 * 
	 * @return Returns the modified data value, or <TT>null</TT> if an error
	 *         occurred.
	 */
	public static Date generalAddTimeDiff(Date dateValue, int days, int hours, int minutes, int seconds)
	{
		// Validate parameter
		if (dateValue == null) return null;

		// Get time value
		long timeValue = dateValue.getTime();

		// Compute
		timeValue = timeValue + (days * FileUtilFunctions.DAY_MILLISECONDS) + (hours * FileUtilFunctions.HOUR_MILLISECONDS) + (minutes * FileUtilFunctions.MINUTE_MILLISECONDS) + (seconds * FileUtilFunctions.SECOND_MILLISECONDS);

		// Return
		return new Date(timeValue);
	}

	/**
	 * Normalize new line to the system standards.
	 * 
	 * @param messageText
	 *            Text to normalize.
	 * 
	 * @return Returns the normalized string.
	 */
	public static String generalNormalizeNewLine(String messageText)
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
	public static String generalConvertRegularWildcardsToRegexWildcards(String regularPattern)
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
	 * @param inputString
	 *            The string to be transformed.
	 * 
	 * @return Returns the normalized string.
	 */
	public static String generalFitToFileNameCompatibility(String inputString)
	{
		String outputString;
		outputString = inputString.toLowerCase().replaceAll("[^a-zA-Z0-9-.\\[\\]]+", "_");
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
	public static long generalGetTimeDifferenceInSeconds(Date earlyDate, Date laterDate)
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
	public static void generalWaitForThreadTerminating(Thread thread, int maxTimeToWaitInSeconds)
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
	 * Executing a pause for some Seconds.
	 * 
	 * @param seconds
	 *            Number of seconds to pause.
	 */
	public static void generalSleepSeconds(int seconds)
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
	 * Executing a pause for some Milliseconds.
	 * 
	 * @param milliseconds
	 *            Number of Milliseconds to pause.
	 */
	public static void generalSleepMilliseconds(int milliseconds)
	{
		try
		{
			Thread.sleep(milliseconds);
		}
		catch (InterruptedException e)
		{
		}
	}

	/**
	 * Check if a file exists physically and can be read.
	 * <p>
	 * In contrast to the similar method <TT>fileExists()</TT> the function is
	 * executed a bunch of times, if it fails the first time. This can happen if
	 * the file is touched by another process for a while, e. g. because it is
	 * just copied.
	 * 
	 * @param filePath
	 *            The file to consider.
	 * 
	 * @return Returns the number of attempts (at least 1) if the file exists
	 *         and is accessible, otherwise <TT>0</TT> if an error occurred, or
	 *         a value lower than <TT>0</TT> as the number of attempts that
	 *         failed.
	 */
	public static int fileExistsRetry(String filePath)
	{
		return FileUtilFunctions.fileExists(filePath, FileUtilFunctions.defaultNuOfRetrials, FileUtilFunctions.defaultIdleTimeBetweenRetrialsInMilliseconds);
	}

	/**
	 * Check if a file exists physically and can be read.
	 * <p>
	 * Please use the similar method <TT>fileExistsRetry()</TT> when you want to
	 * executed the function a bunch of times, if it fails the first time. This
	 * can happen if the file is touched by another process for a while, e. g.
	 * because it is just copied.
	 * 
	 * @param filePath
	 *            The file to consider.
	 * 
	 * @return Returns <TT>true</TT> if the file exists and is accessible,
	 *         otherwise <TT>false</TT>.
	 */
	public static boolean fileExists(String filePath)
	{
		int nuOfTrials = FileUtilFunctions.fileExists(filePath, 0, 0);

		if (nuOfTrials <= 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Check if a file exists physically and can be read.
	 * 
	 * @param filePath
	 *            The file to consider.
	 * 
	 * @param nuOfRetrials
	 *            The maximum number of trials, if the function false the first
	 *            time.
	 * 
	 * @param idleTimeBetweenRetrialsInMilliseconds
	 *            The idle time between trials, if the function false the first
	 *            time.
	 * 
	 * @return Returns the number of attempts (at least 1) if the file exists
	 *         and is accessible, otherwise <TT>0</TT> if an error occurred, or
	 *         a value lower than <TT>0</TT> as the number of attempts that
	 *         failed.
	 */
	private static int fileExists(String filePath, int nuOfRetrials, int idleTimeBetweenRetrialsInMilliseconds)
	{
		if (filePath == null) return 0;
		if (filePath.length() == 0) return 0;

		if (nuOfRetrials < 0) nuOfRetrials = 0;
		if (nuOfRetrials > 60) nuOfRetrials = 60;

		boolean isAccessable = true;

		try
		{
			// Create FILE objects
			File file = new File(filePath);

			// Check source file
			if (file.isFile() == false) return 0;

			// Check file
			if (file.exists() == false) isAccessable = false;
			if (file.canRead() == false) isAccessable = false;
			if (isAccessable == true) return 1;

			for (int i = 0; i < nuOfRetrials; i++)
			{
				FileUtilFunctions.generalSleepMilliseconds(idleTimeBetweenRetrialsInMilliseconds);

				file = new File(filePath);

				isAccessable = true;
				if (file.exists() == false) isAccessable = false;
				if (file.canRead() == false) isAccessable = false;
				if (isAccessable == true) return i + 2;
			}

			return -(nuOfRetrials + 1);

		}
		catch (Exception e)
		{
			return 0;
		}
	}

	/**
	 * Check if a directory exists physically.
	 * 
	 * @param filePath
	 *            The directory path.
	 * 
	 * @return Returns <TT>true</TT> if the directory exists and is accessible,
	 *         otherwise <TT>false</TT>.
	 */
	public static boolean directoryExists(String filePath)
	{
		if (filePath == null) return false;
		if (filePath.length() == 0) return false;

		boolean isAccessable = true;

		try
		{
			File file = new File(filePath);

			if (file.exists() == false) isAccessable = false;
			if (file.canRead() == false) isAccessable = false;
			if (file.isDirectory() == false) isAccessable = false;
		}
		catch (Exception e)
		{
			isAccessable = false;
		}

		return isAccessable;
	}

	/**
	 * Get hash value (MD5) of a file.
	 * 
	 * @param filePath
	 *            The path of the file to be hashed.
	 * 
	 * @return Returns the hash code (MD5) as hexadecimal string or
	 *         <TT>null</TT>, if an error occurred.
	 */
	public static String fileGetHashValue(String filePath)
	{
		// Check parameters
		if (filePath == null || filePath.length() == 0) return null;
		if (FileUtilFunctions.fileExists(filePath) == false) return null;

		// Compute hash value
		String md5 = null;

		try
		{
			FileInputStream fileInputStream = new FileInputStream(new File(filePath));
			md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fileInputStream);
			fileInputStream.close();
		}
		catch (Exception e)
		{
			return null;
		}

		// Return
		return md5;
	}

	/**
	 * Search for file name.
	 * 
	 * @param filePath
	 *            The path of the files to be searched for.
	 * 
	 * @param fileFilterMask
	 *            The file name mask to be searched for, including wildcards (*,
	 *            ?).
	 * 
	 * @return Returns a list of files that were found, or <TT>null</TT>, if an
	 *         error occurred.
	 */
	public static List<String> directorySearchForFiles(String filePath, String fileFilterMask)
	{
		class UtilFileFilter implements FilenameFilter
		{
			File directory;
			String fileNameMask;

			public UtilFileFilter(File directory, String fileNameMask)
			{
				this.directory = directory;
				this.fileNameMask = FileUtilFunctions.generalConvertRegularWildcardsToRegexWildcards(fileNameMask);
			}

			public boolean accept(File currentDirectory, String currentFileName)
			{
				if (directory == null) return false;
				if (fileNameMask == null) return false;

				if (!currentDirectory.equals(this.directory)) return false;
				if (!currentFileName.matches(fileNameMask)) return false;

				return true;
			}
		}

		// Check parameters
		if (filePath == null || filePath.length() == 0) return null;
		if (fileFilterMask == null || fileFilterMask.length() == 0) return null;

		// Initialize variables
		List<String> fileList = new ArrayList<String>();

		// Search for files that matches
		try
		{
			File directory = new File(filePath);
			if (directory.isDirectory() == false) return null;

			File[] files = directory.listFiles(new UtilFileFilter(directory, fileFilterMask));

			for (File file : files)
			{
				if (file.isFile()) fileList.add(file.getAbsolutePath());
			}
		}
		catch (Exception e)
		{
			return null;
		}

		// Return
		return fileList;
	}

	/**
	 * Search for files that are older than a specific number of days.
	 * 
	 * @param directoryPath
	 *            The path of the files to be searched for.
	 * 
	 * @param fileFilterMask
	 *            The file name mask to be searched for, including wildcards (*,
	 *            ?).
	 * 
	 * @param daysToKeep
	 *            All files that are older than this number of days (resp. 1440
	 *            minutes, from <TT>now</TT>) are collected into the result file
	 *            list. Please set at least <TT>1</TT> day to keep. If the
	 *            parameter is set lower than 1 it is set to one day
	 *            automatically.
	 * 
	 * @return Returns a list of files that are older than <TT>x</TT> days, or
	 *         <TT>null</TT>, if an error occurred.
	 */
	public static List<String> directorySearchOnExpiredFiles(String directoryPath, String fileFilterMask, int daysToKeep)
	{
		class UtilFileFilter implements FilenameFilter
		{
			File directory;
			String fileNameMask;
			int daysToKeep;

			public UtilFileFilter(File directory, String fileNameMask,
					int daysToKeep)
			{
				this.directory = directory;
				this.fileNameMask = FileUtilFunctions.generalConvertRegularWildcardsToRegexWildcards(fileNameMask);
				this.daysToKeep = daysToKeep;
			}

			public boolean accept(File currentDirectory, String currentFileName)
			{
				if (directory == null) return false;
				if (fileNameMask == null) return false;

				// Check directory
				if (!currentDirectory.equals(this.directory)) return false;

				// Check file name mask
				if (!currentFileName.matches(fileNameMask)) return false;

				// Check number of days
				try
				{
					File file = new File(currentDirectory + "/" + currentFileName);

					Date date1 = new Date();
					Date date2 = new Date(file.lastModified());

					long minutes1 = date1.getTime() / FileUtilFunctions.MINUTE_MILLISECONDS;
					long minutes2 = date2.getTime() / FileUtilFunctions.MINUTE_MILLISECONDS;
					if (Math.abs((minutes1 - minutes2)) <= (this.daysToKeep * FileUtilFunctions.MINUTES_PER_DAY)) return false;
				}
				catch (Exception e)
				{
					return false;
				}

				// Return
				return true;
			}
		}

		// Check parameters
		if (directoryPath == null || directoryPath.length() == 0) return null;
		if (fileFilterMask == null || fileFilterMask.length() == 0) return null;

		if (daysToKeep < 1) daysToKeep = 1;

		// Initialize variables
		List<String> fileList = new ArrayList<String>();

		// Search for files that matches
		try
		{
			File directory = new File(directoryPath);
			if (directory.isDirectory() == false) return null;

			File[] files = directory.listFiles(new UtilFileFilter(directory, fileFilterMask, daysToKeep));

			for (File file : files)
			{
				if (file.isFile()) fileList.add(file.getAbsolutePath());
			}
		}
		catch (Exception e)
		{
			return null;
		}

		// Return
		return fileList;
	}

	/**
	 * Copy a file.
	 * <p>
	 * In contrast to the similar method <TT>fileCopy()</TT> the function is
	 * executed a bunch of times, if it fails the first time. This can happen if
	 * the file is touched by another process for a while, e. g. because it is
	 * just copied.
	 * 
	 * @param sourceFilePath
	 *            The path of the file to be copied.
	 * 
	 * @param destinationFilePath
	 *            The path of the destination file.
	 * 
	 * @return Returns the number of attempts (at least 1) if the file could be
	 *         copied, otherwise <TT>0</TT> if an error occurred, or a value
	 *         lower than <TT>0</TT> as the number of attempts that failed.
	 */
	public static int fileCopyRetry(String sourceFilePath, String destinationFilePath)
	{
		return FileUtilFunctions.fileCopy(sourceFilePath, destinationFilePath, FileUtilFunctions.defaultNuOfRetrials, FileUtilFunctions.defaultIdleTimeBetweenRetrialsInMilliseconds);
	}

	/**
	 * Copy a file.
	 * <p>
	 * Please use the similar method <TT>fileCopyRetry()</TT> when you want to
	 * executed the function a bunch of times, if it fails the first time. This
	 * can happen if the file is touched by another process for a while, e. g.
	 * because it is just copied.
	 * 
	 * @param sourceFilePath
	 *            The path of the file to be copied.
	 * 
	 * @param destinationFilePath
	 *            The path of the destination file.
	 * 
	 * @return Returns <TT>true</TT> if the file could be copied, otherwise
	 *         <TT>false</TT>.
	 */
	public static boolean fileCopy(String sourceFilePath, String destinationFilePath)
	{
		int nuOfTrials = FileUtilFunctions.fileCopy(sourceFilePath, destinationFilePath, 0, 0);

		if (nuOfTrials <= 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Copy a file.
	 * 
	 * @param sourceFilePath
	 *            The path of the file to be copied.
	 * 
	 * @param destinationFilePath
	 *            The path of the destination file.
	 * 
	 * @param nuOfRetrials
	 *            The maximum number of trials, if the function false the first
	 *            time.
	 * 
	 * @param idleTimeBetweenRetrialsInMilliseconds
	 *            The idle time between trials, if the function false the first
	 *            time.
	 * 
	 * @return Returns the number of attempts (at least 1) if the file could be
	 *         copied, otherwise <TT>0</TT> if an error occurred, or a value
	 *         lower than <TT>0</TT> as the number of attempts that failed.
	 */
	private static int fileCopy(String sourceFilePath, String destinationFilePath, int nuOfRetrials, int idleTimeBetweenRetrialsInMilliseconds)
	{
		// Check parameters
		if (sourceFilePath == null || sourceFilePath.length() == 0) return 0;
		if (destinationFilePath == null || destinationFilePath.length() == 0) return 0;

		if (nuOfRetrials < 0) nuOfRetrials = 0;
		if (nuOfRetrials > 60) nuOfRetrials = 60;

		// Copy file
		boolean isSuccessful = false;

		try
		{
			// Create FILE objects
			File sourceFile = new File(sourceFilePath);
			File destinationFile = new File(destinationFilePath);

			// Check source file
			if (sourceFile.isFile() == false) return 0;

			// Copy file
			isSuccessful = FileUtilFunctions.fileCopyExecute(sourceFile, destinationFile);
			if (isSuccessful == true) return 1;

			for (int i = 0; i < nuOfRetrials; i++)
			{
				FileUtilFunctions.generalSleepMilliseconds(idleTimeBetweenRetrialsInMilliseconds);

				sourceFile = new File(sourceFilePath);
				destinationFile = new File(destinationFilePath);

				isSuccessful = FileUtilFunctions.fileCopyExecute(sourceFile, destinationFile);
				if (isSuccessful == true) return i + 2;
			}

			return -(nuOfRetrials + 1);
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	/**
	 * Execute copying a file, in order to catch the exception and to retry it.
	 * 
	 * @param sourceFile
	 *            The source file to be copied.
	 * 
	 * @param destinationFile
	 *            The destination file.
	 * 
	 * @return Returns <TT>true</TT> if the file could be copied, otherwise
	 *         <TT>false</TT>.
	 */
	private static boolean fileCopyExecute(File sourceFile, File destinationFile)
	{
		// Copy file
		try
		{
			FileUtils.copyFile(sourceFile, destinationFile, false);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Move a file.
	 * 
	 * @param sourceFilePath
	 *            The path of the file to be copied.
	 * 
	 * @param destinationFilePath
	 *            The path of the destination file.
	 * 
	 * @return Returns <TT>true</TT> if the file could be moved, otherwise
	 *         <TT>false</TT>.
	 */
	public static boolean fileMove(String sourceFilePath, String destinationFilePath)
	{
		// Check parameters
		if (sourceFilePath == null || sourceFilePath.length() == 0) return false;
		if (destinationFilePath == null || destinationFilePath.length() == 0) return false;
		if (FileUtilFunctions.fileExists(sourceFilePath) == false) return false;
		if (FileUtilFunctions.fileExists(destinationFilePath) == true) return false;

		// Create FILE objects
		File sourceFile = new File(sourceFilePath);
		File destinationFile = new File(destinationFilePath);

		// Move file
		try
		{
			FileUtils.moveFile(sourceFile, destinationFile);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Delete a single file.
	 * 
	 * @param filePath
	 *            The path of the file to be deleted.
	 * 
	 * @return Returns <TT>true</TT> if the file could be deleted, otherwise
	 *         <TT>false</TT>.
	 */
	public static boolean fileDelete(String filePath)
	{
		// Check parameters
		if (filePath == null || filePath.length() == 0) return false;
		if (FileUtilFunctions.fileExists(filePath) == false) return false;

		// Create FILE objects
		File file = new File(filePath);

		// Delete file
		try
		{
			boolean isSuccessful = file.delete();
			return isSuccessful;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Delete a list of files file.
	 * 
	 * @param fileList
	 *            The list of files to delete, each item containing a file path.
	 * 
	 * @return Returns the number of deleted files.
	 */
	public static int fileDelete(List<String> fileList)
	{
		// Check parameters
		if (fileList == null) return 0;
		if (fileList.size() == 0) return 0;

		// Initialize variables
		int nuOfDeletedFiles = 0;

		// Delete files
		try
		{
			for (String filePath : fileList)
			{
				File file = new File(filePath);

				if (file.isFile() == false) continue;
				if (FileUtilFunctions.fileExists(filePath) == false) continue;

				try
				{
					if (file.delete() == true) nuOfDeletedFiles++;
				}
				catch (Exception e)
				{
					// Be silent
				}
			}

			return nuOfDeletedFiles;
		}
		catch (Exception e)
		{
			return nuOfDeletedFiles;
		}
	}

	/**
	 * Get file name of a file path, without file type.
	 * 
	 * @param filePath
	 *            The path of the file to be analyzed.
	 * 
	 * @return Returns the file name, or <TT>null</TT> if file path couldn't be
	 *         separated.
	 */
	public static String fileGetFileNamePart(String filePath)
	{
		// Check parameters
		if (filePath == null || filePath.length() == 0) return null;

		// Create FILE objects
		File file = new File(filePath);
		if (file.isFile() == false) return null;

		// Get file name
		try
		{
			String fileName = file.getName();
			if (fileName == null || fileName.length() == 0) return null;

			int position = fileName.lastIndexOf('.');

			if (position > 0)
			{
				fileName = fileName.substring(0, position);
			}

			return fileName;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Get file type of a file path.
	 * 
	 * @param filePath
	 *            The path of the file to be analyzed.
	 * 
	 * @return Returns the file name, or <TT>null</TT> if file path couldn't be
	 *         separated.
	 */
	public static String fileGetFileTypePart(String filePath)
	{
		// Check parameters
		if (filePath == null || filePath.length() == 0) return null;

		// Create FILE objects
		File file = new File(filePath);
		if (file.isFile() == false) return null;

		// Get file name
		try
		{
			String fileName = file.getName();
			if (fileName == null || fileName.length() == 0) return null;

			String fileType = null;
			int position = fileName.lastIndexOf('.');

			if (position > 0)
			{
				fileType = fileName.substring(position + 1);
			}

			if (fileType == null || fileType.length() == 0) return null;

			return fileType;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Read file content and return as byte buffer.
	 * 
	 * @param filePath
	 *            The path of the file to be read.
	 * 
	 * @return Returns the content of the file, or <TT>null</TT> if an error
	 *         occurred, or the file doesn't exist.
	 */
	public static byte[] fileReadToByteArray(String filePath)
	{
		// Check parameters
		if (filePath == null || filePath.length() == 0) return null;
		if (FileUtilFunctions.fileExists(filePath) == false) return null;

		// Create FILE objects
		File file = new File(filePath);

		// Read file content
		try
		{
			return FileUtils.readFileToByteArray(file);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Read file content and return as a string, coded as "UTF-8".
	 * 
	 * @param filePath
	 *            The path of the file to be read.
	 * 
	 * @return Returns the content of the file, or <TT>null</TT> if an error
	 *         occurred, or the file doesn't exist.
	 */
	public static String fileReadToString(String filePath)
	{
		// Check parameters
		if (filePath == null || filePath.length() == 0) return null;
		if (FileUtilFunctions.fileExists(filePath) == false) return null;

		// Create FILE objects
		File file = new File(filePath);

		// Read file content
		try
		{
			return FileUtils.readFileToString(file, "UTF-8");
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Delete all files of a file list, but for the newest one. If some files
	 * have the same timestamp, delete all but the first one.
	 * 
	 * @param files
	 *            The list of files to consider.
	 * 
	 * @return Returns the number of deleted files.
	 */
	public static int fileDeleteFilesOfList(List<String> files)
	{
		// Check parameters
		if (files == null || files.size() == 0) return 0;

		// Initialize variable
		int nuOfFilesInList = files.size();
		int nuOfFilesDeleted = 0;

		// Go through the list of files and delete the older ones
		try
		{
			for (String filePath1 : files)
			{
				if (FileUtilFunctions.fileExists(filePath1) == false) continue;

				for (String filePath2 : files)
				{
					if (FileUtilFunctions.fileExists(filePath2) == false) continue;
					if (filePath1.equals(filePath2)) continue;

					File file1 = new File(filePath1);
					File file2 = new File(filePath2);

					if (FileUtils.isFileNewer(file1, file2))
					{
						file2.delete();
						nuOfFilesDeleted++;
					}
				}
			}

		}
		catch (Exception e)
		{
			// Be silent;
		}

		// Go through the list of files and delete the first one
		if ((nuOfFilesDeleted + 1) != nuOfFilesInList)
		{
			try
			{
				boolean firstFileMark = false;

				for (String filePath : files)
				{
					if (firstFileMark == false)
					{
						if (FileUtilFunctions.fileExists(filePath) == true)
						{
							firstFileMark = true;
						}
					}
					else
					{
						if (FileUtilFunctions.fileExists(filePath) == true)
						{
							File file = new File(filePath);
							file.delete();
							nuOfFilesDeleted++;
						}
					}
				}

			}
			catch (Exception e)
			{
				// Be silent;
			}
		}

		// Return
		return nuOfFilesDeleted;
	}

	/**
	 * Change the 'modified date' of a file.
	 * <p>
	 * In contrast to the similar method <TT>fileSetLastModified()</TT> the
	 * function is executed a bunch of times, if it fails the first time. This
	 * can happen if the file is touched by another process for a while, e. g.
	 * because it is just copied.
	 * 
	 * @param filePath
	 *            The path of the file to be considered.
	 * 
	 * @param modifiedDate
	 *            The modified date to set.
	 * 
	 * @return Returns the number of attempts (at least 1) if the modified date
	 *         could be changed, otherwise <TT>0</TT> if an error occurred, or a
	 *         value lower than <TT>0</TT> as the number of attempts that
	 *         failed.
	 */
	public static int fileSetLastModifiedRetry(String filePath, Date modifiedDate)
	{
		return FileUtilFunctions.fileSetLastModified(filePath, modifiedDate, FileUtilFunctions.defaultNuOfRetrials, FileUtilFunctions.defaultIdleTimeBetweenRetrialsInMilliseconds);
	}

	/**
	 * Change the 'modified date' of a file.
	 * <p>
	 * Please use the similar method <TT>fileSetLastModifiedRetry()</TT> when
	 * you want to executed the function a bunch of times, if it fails the first
	 * time. This can happen if the file is touched by another process for a
	 * while, e. g. because it is just copied.
	 * 
	 * @param filePath
	 *            The path of the file to be considered.
	 * 
	 * @param modifiedDate
	 *            The modified date to set.
	 * 
	 * @return Returns <TT>true</TT> if the modified date could be changed,
	 *         otherwise <TT>false</TT>.
	 */
	public static boolean fileSetLastModified(String filePath, Date modifiedDate)
	{
		int nuOfTrials = FileUtilFunctions.fileSetLastModified(filePath, modifiedDate, 0, 0);

		if (nuOfTrials <= 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Change the modified date of a file.
	 * 
	 * @param filePath
	 *            The path of the file to be considered.
	 * 
	 * @param modifiedDate
	 *            The modified date to set.
	 * 
	 * @param nuOfRetrials
	 *            The maximum number of trials, if the function false the first
	 *            time.
	 * 
	 * @param idleTimeBetweenRetrialsInMilliseconds
	 *            The idle time between trials, if the function false the first
	 *            time.
	 * 
	 * @return Returns the number of attempts (at least 1) if the modified date
	 *         could be changed, otherwise <TT>0</TT> if an error occurred, or a
	 *         value lower than <TT>0</TT> as the number of attempts that
	 *         failed.
	 */
	private static int fileSetLastModified(String filePath, Date modifiedDate, int nuOfRetrials, int idleTimeBetweenRetrialsInMilliseconds)
	{
		// Check parameters
		if (filePath == null || filePath.length() == 0) return 0;
		if (FileUtilFunctions.fileExists(filePath) == false) return 0;
		if (modifiedDate == null) return 0;

		if (nuOfRetrials < 0) nuOfRetrials = 0;
		if (nuOfRetrials > 60) nuOfRetrials = 60;

		if (idleTimeBetweenRetrialsInMilliseconds < 0) idleTimeBetweenRetrialsInMilliseconds = 0;
		if (idleTimeBetweenRetrialsInMilliseconds > 1000) idleTimeBetweenRetrialsInMilliseconds = 1000;

		// Set the modified date
		boolean isSuccessful = false;

		try
		{
			File file = new File(filePath);

			isSuccessful = file.setLastModified(modifiedDate.getTime());
			if (isSuccessful == true) return 1;

			for (int i = 0; i < nuOfRetrials; i++)
			{
				FileUtilFunctions.generalSleepMilliseconds(idleTimeBetweenRetrialsInMilliseconds);

				file = new File(filePath);

				isSuccessful = file.setLastModified(modifiedDate.getTime());
				if (isSuccessful == true) return i + 2;
			}

			return -(nuOfRetrials + 1);
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	/**
	 * Change the modified date of a file by touching it (opens the file and
	 * closes it).
	 * 
	 * @param filePath
	 *            The path of the file to be considered.
	 * 
	 * @return Returns <TT>true</TT> if the file could be touched, otherwise
	 *         <TT>false</TT>.
	 */
	public static boolean fileTouch(String filePath)
	{
		// Check parameters
		if (filePath == null || filePath.length() == 0) return false;
		if (FileUtilFunctions.fileExists(filePath) == false) return false;

		// Create FILE objects
		File file = new File(filePath);

		// Set the modified date
		try
		{
			FileUtils.touch(file);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Get the checksum (CRC32) of a file.
	 * 
	 * @param filePath
	 *            The path of the file to be considered.
	 * 
	 * @return Returns the checksum, or <TT>0</TT> if the file couldn't be read.
	 */
	public static long fileGetChecksum(String filePath)
	{
		// Check parameters
		if (filePath == null || filePath.length() == 0) return 0L;
		if (FileUtilFunctions.fileExists(filePath) == false) return 0L;

		// Create FILE objects
		File file = new File(filePath);
		if (file.isFile() == false) return 0L;

		// Set the modified date
		try
		{
			return FileUtils.checksumCRC32(file);
		}
		catch (Exception e)
		{
			return 0L;
		}
	}

	/**
	 * Search for file names in a directory and return with the file path of the
	 * most recent file, based on the 'modified date' of the file.
	 * 
	 * @param filePath
	 *            The path of the files to be searched for.
	 * 
	 * @param fileFilterMask
	 *            The file name mask to be searched for, including wildcards (*,
	 *            ?).
	 * 
	 * @return Returns the most recent, existing file, or <TT>null</TT> if an
	 *         error occurred, or no file could be found.
	 */
	public static String directorySearchOnMostRecentFile(String filePath, String fileFilterMask)
	{
		// Get list of files that matches the file filter mask
		List<String> filePathList = FileUtilFunctions.directorySearchForFiles(filePath, fileFilterMask);
		if (filePathList == null) return null;
		if (filePathList.size() == 0) return null;

		// Initialize variables
		String returnFilePath = null;

		// Go through the list of files to find the most current one
		try
		{
			returnFilePath = filePathList.get(0);

			for (String currentFilePath : filePathList)
			{
				File returnFile = new File(returnFilePath);
				File file = new File(currentFilePath);

				if (FileUtils.isFileNewer(file, returnFile))
				{
					returnFilePath = currentFilePath;
				}
			}

		}
		catch (Exception e)
		{
			return null;
		}

		// Return
		return returnFilePath;
	}

	/**
	 * Search for file names in a directory and return with a list of obsolete
	 * files, based on the 'modified date' of the file. That means the most
	 * recent media file of a given media identifier will not be part of the
	 * list.
	 * 
	 * @param filePath
	 *            The path of the files to be searched for.
	 * 
	 * @param fileNameMask
	 *            The file name mask to be searched for, including wildcards (*,
	 *            ?).
	 * 
	 * @param daysToKeep
	 *            All files that are older than this number of days (resp. 1440
	 *            minutes, from <TT>now</TT>) are collected into the result file
	 *            list. Please set at least <TT>1</TT> day to keep. If the
	 *            parameter is set lower than 1 it is set to one day
	 *            automatically.
	 * 
	 * @return Returns the list of obsolete files, or <TT>null</TT> if an error
	 *         occurred, or no files could be found.
	 */
	public static List<String> directorySearchOnObsoleteFiles(String filePath, String fileFilterMask, Integer daysToKeep)
	{
		/*
		 * Get list of files that matches the file filter mask
		 */

		List<String> filePathList = FileUtilFunctions.directorySearchForFiles(filePath, fileFilterMask);
		if (filePathList == null) return null;
		if (filePathList.size() == 0) return null;

		/*
		 * Get the most recent file that matches the file filter mask and remove
		 * it from the result list.
		 */

		String mostRecentFilePath = FileUtilFunctions.directorySearchOnMostRecentFile(filePath, fileFilterMask);
		if (mostRecentFilePath == null) return null;
		if (mostRecentFilePath.length() == 0) return null;

		try
		{
			filePathList.remove(mostRecentFilePath);
		}
		catch (Exception e)
		{
			return null;
		}

		/*
		 * Delete all files that are NOT older than x days.
		 */

		if (daysToKeep == null) return filePathList;

		try
		{
			// Compose expired date
			if (daysToKeep <= 1) daysToKeep = 1;
			Date daysToKeepDate = FileUtilFunctions.generalAddTimeDiff(new Date(), -daysToKeep, 0, 0, 0);

			// Go through the list
			for (String currentFilePath : new ArrayList<String>(filePathList))
			{
				File file = new File(currentFilePath);

				Date fileDate = new Date(file.lastModified());

				if (fileDate.after(daysToKeepDate))
				{
					filePathList.remove(currentFilePath);
				}
			}
		}
		catch (Exception e)
		{
			return null;
		}

		// Return
		return filePathList;
	}

	/**
	 * Remove all files in a directory.
	 * 
	 * @param directoryPath
	 *            The path of the directory to be considered.
	 * 
	 * @return Returns <TT>true</TT> if the file could be removed, otherwise
	 *         <TT>false</TT>.
	 */
	public static boolean directoryDeleteAllFiles(String directoryPath)
	{
		// Check parameters
		if (directoryPath == null || directoryPath.length() == 0) return false;
		if (FileUtilFunctions.directoryExists(directoryPath) == false) return false;

		// Create FILE objects
		File directory = new File(directoryPath);
		if (directory.isDirectory() == false) return false;

		// Search for files in the directory and delete them
		try
		{
			List<String> filesToDelete = FileUtilFunctions.directorySearchForFiles(directoryPath, "*");
			if (filesToDelete == null) return false;
			if (filesToDelete.size() == 0) return true;

			if (FileUtilFunctions.fileDelete(filesToDelete) > 0)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Delete all files that are older than <TT>x</TT> days in a directory and
	 * all sub directories.
	 * 
	 * @param directoryPath
	 *            The path of the directory to be considered. The path of the
	 *            files to be searched for.
	 * 
	 * @param fileFilterMask
	 *            The file name mask to be searched for, including wildcards (*,
	 *            ?).
	 * 
	 * @param daysToKeep
	 *            All files that are older than this number of days (resp. 1440
	 *            minutes, from <TT>now</TT>) will be deleted. Please set at
	 *            least <TT>1</TT> day to keep. If the parameter is set lower
	 *            than 1 it is set to one day automatically.
	 * 
	 * @return Returns the number of deleted files, or <TT>null</TT> if an error
	 *         occurred.
	 */
	public static Integer directoryDeleteExpiredFiles(String directoryPath, String fileFilterMask, int daysToKeep)
	{
		class UtilFileFilter implements FilenameFilter
		{
			File directory;
			String fileNameMask;
			int daysToKeep;

			public UtilFileFilter(File directory, String fileNameMask,
					int daysToKeep)
			{
				this.directory = directory;
				this.fileNameMask = FileUtilFunctions.generalConvertRegularWildcardsToRegexWildcards(fileNameMask);
				this.daysToKeep = daysToKeep;
			}

			public boolean accept(File currentDirectory, String currentFileName)
			{
				if (directory == null) return false;
				if (fileNameMask == null) return false;

				try
				{
					// Check number of days
					File file = new File(currentDirectory + "/" + currentFileName);
					
					// Pass through all sub directories
					if (file.isDirectory()) return true;
					
					// Check file name mask
					if (!currentFileName.matches(fileNameMask)) return false;

					Date date1 = new Date();
					Date date2 = new Date(file.lastModified());

					long minutes1 = date1.getTime() / FileUtilFunctions.MINUTE_MILLISECONDS;
					long minutes2 = date2.getTime() / FileUtilFunctions.MINUTE_MILLISECONDS;
					if (Math.abs((minutes1 - minutes2)) <= (this.daysToKeep * FileUtilFunctions.MINUTES_PER_DAY)) return false;
				}
				catch (Exception e)
				{
					return false;
				}

				// Return
				return true;
			}
		}

		// Check parameters
		if (directoryPath == null || directoryPath.length() == 0) return null;
		if (FileUtilFunctions.directoryExists(directoryPath) == false) return null;
		if (daysToKeep < 1) daysToKeep = 1;

		// Search for files in the directory and delete them
		try
		{
			// Check if it is a directory
			File directory = new File(directoryPath);
			if (!directory.isDirectory()) return null;

			// Initialize variables
			int nuOfDeletedFiles = 0;

			// Get the list of all files in the directory
			File[] listFiles = directory.listFiles(new UtilFileFilter(directory, fileFilterMask, daysToKeep));

			// Go through all files found
			for (File file : listFiles)
			{
				// If it is a sub directory, call this method recursively
				if (file.isDirectory())
				{
					nuOfDeletedFiles = nuOfDeletedFiles + FileUtilFunctions.directoryDeleteExpiredFiles(file.getAbsolutePath(), fileFilterMask, daysToKeep);
				}
				// If it is a file, delete it, if it is older than x days
				else
				{
					if (file.delete() == true) nuOfDeletedFiles++;
				}
			}

			// Return
			return nuOfDeletedFiles;
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
