package fmagic.basic;

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
	 * Check if a directory exists physically.
	 * 
	 * @param filePath
	 *            The directory path.
	 * 
	 * @return Returns <TT>true</TT> if the directory exists and is accessible,
	 *         otherwise <TT>false</TT>.
	 */
	public static boolean fileDirectoryExists(String filePath)
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
	 * @param fileFilterMask
	 *            The file name mask to be searched for, including wildcards (*,
	 *            ?).
	 * 
	 * @return Returns a list of files that were found, or <TT>null</TT>, if an
	 *         error occurred.
	 */
	public static List<String> fileSearchDirectory(String filePath, String fileFilterMask)
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
				if (Util.fileExists(filePath) == false) continue;

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
		if (Util.fileExists(filePath) == false) return null;

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
		if (Util.fileExists(filePath) == false) return null;

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
	 * have the same timestamp, delete all but thr first one.
	 * 
	 * @param files
	 *            The list of files to consider.
	 * 
	 * @return Returns the number of deleted files.
	 */
	public static int fileCleanFileList(List<String> files)
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
				if (Util.fileExists(filePath1) == false) continue;

				for (String filePath2 : files)
				{
					if (Util.fileExists(filePath2) == false) continue;
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
						if (Util.fileExists(filePath) == true)
						{
							firstFileMark = true;
						}
					}
					else
					{
						if (Util.fileExists(filePath) == true)
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
	 * Change the modified date of a file.
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
	public static boolean fileSetLastModified(String filePath)
	{
		// Check parameters
		if (filePath == null || filePath.length() == 0) return false;
		if (Util.fileExists(filePath) == false) return false;

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
	public static String fileSearchDirectoryOnMostRecentFile(String filePath, String fileFilterMask)
	{
		// Get list of files that matches the file filter mask
		List<String> filePathList = Util.fileSearchDirectory(filePath, fileFilterMask);
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
	 * @return Returns the list of obsolete files, or <TT>null</TT> if an error
	 *         occurred, or no files could be found.
	 */
	public static List<String> fileSearchDirectoryOnObsoleteFiles(String filePath, String fileFilterMask)
	{
		// Get list of files that matches the file filter mask
		List<String> filePathList = Util.fileSearchDirectory(filePath, fileFilterMask);
		if (filePathList == null) return null;
		if (filePathList.size() == 0) return null;

		// Get the most recent file that matches the file filter mask
		String mostRecentFilePath = Util.fileSearchDirectoryOnMostRecentFile(filePath, fileFilterMask);
		if (mostRecentFilePath == null) return null;
		if (mostRecentFilePath.length() == 0) return null;

		// Remove the most recent file from the list of files
		try
		{
			filePathList.remove(mostRecentFilePath);
		}
		catch (Exception e)
		{
			return null;
		}

		// Return
		return filePathList;
	}
}
