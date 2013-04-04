package fmagic.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.channels.FileChannel;
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
	 * @param inputString
	 *            The string to be transformed.
	 * 
	 * @return Returns the normalized string.
	 */
	public static String fitToFileNameCompatibility(String inputString)
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
	 * Check if a file exists physically.
	 * 
	 * @param filePath
	 *            The full file path of the file.
	 * 
	 * @return Returns <TT>true</TT> if the file exists and is accessible,
	 *         otherwise <TT>false</TT>.
	 */
	public static boolean fileExists(String filePath)
	{
		if (filePath == null) return false;
		if (filePath.length() == 0) return false;

		boolean isAccessable = true;

		try
		{
			File file = new File(filePath);

			if (file.exists() == false) isAccessable = false;
			if (file.canRead() == false) isAccessable = false;
			if (file.isFile() == false) isAccessable = false;
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
		if (Util.fileExists(filePath) == false) return null;

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
	 * @param fileNameMask
	 *            The file name mask to be searched for, including wildcards (*,
	 *            ?).
	 * 
	 * @return Returns a list of files that were found, or <TT>null</TT>, if an
	 *         error occurred.
	 */
	public static List<String> fileSearchDirectory(String filePath, String fileNameMask)
	{
		class UtilFileFilter implements FilenameFilter
		{
			File directory;
			String fileNameMask;

			public UtilFileFilter(File directory, String fileNameMask)
			{
				this.directory = directory;
				this.fileNameMask = Util.convertRegularWildcardsToRegexWildcards(fileNameMask);
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
		if (fileNameMask == null || fileNameMask.length() == 0) return null;

		// Initialize variables
		List<String> fileList = new ArrayList<String>();

		// Search for files that matches
		try
		{
			File directory = new File(filePath);
			if (directory.isDirectory() == false) return null;

			File[] files = directory.listFiles(new UtilFileFilter(directory, fileNameMask));

			for (File file : files)
			{
				fileList.add(file.getAbsolutePath());
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
		// Check parameters
		if (sourceFilePath == null || sourceFilePath.length() == 0) return false;
		if (destinationFilePath == null || destinationFilePath.length() == 0) return false;
		if (Util.fileExists(sourceFilePath) == false) return false;

		// Create FILE objects
		File sourceFile = new File(sourceFilePath);
		File destinationFile = new File(destinationFilePath);

		// Copy file
		try
		{
			FileUtils.copyFile(sourceFile, destinationFile);
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
		if (Util.fileExists(sourceFilePath) == false) return false;
		if (Util.fileExists(destinationFilePath) == true) return false;

		// Create FILE objects
		File sourceFile = new File(sourceFilePath);
		File destinationFile = new File(destinationFilePath);

		// Rename file
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
	 * Delete a file.
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
		if (Util.fileExists(filePath) == false) return false;

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
}
