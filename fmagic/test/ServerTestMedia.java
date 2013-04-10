package fmagic.test;

import java.util.ArrayList;
import java.util.List;

import fmagic.basic.Context;
import fmagic.basic.FileLocationManager;
import fmagic.basic.MediaContainer;
import fmagic.basic.ResourceContainer;
import fmagic.basic.ResourceContainerMedia;
import fmagic.basic.ResourceManager;
import fmagic.basic.FileUtil;
import fmagic.server.ServerManager;

public class ServerTestMedia implements Runnable
{
	private final Context context;
	private final ServerManager server;
	private final boolean concurrentAccess;

	private String parameterResourceGroup = "Apartment";
	private String parameterResourceName = "Room";
	private String parameterDataIdentifierTestUpload = "1234";
	private String parameterDataIdentifierTestObsolete = "1235";
	private int parameterTestCycleNumber = 1;
	private int parameterTestCycleDataIdentifierFrom = 1;
	private int parameterTestCycleDataIdentifierToo = 10;

	/**
	 * Constructor
	 */
	public ServerTestMedia(ServerManager server, Context context,
			boolean concurrentAccess)
	{
		this.server = server;
		this.context = context.createSilentDumpContext(ResourceManager.context(context, "Media", "Processing"));
		this.concurrentAccess = concurrentAccess;

	}

	/**
	 * Setup test environment
	 */
	public void doSetup()
	{
		if (this.server == null) return;

		// Clean 'pending' directory
		this.doRemoveAllFilesInPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);

		// Clean 'regular' directory
		this.doRemoveAllFilesInRegularDirectory(this.parameterResourceGroup, this.parameterResourceName);

		// Clean 'deleted' directory
		this.doRemoveAllFilesInDeletedDirectory(this.parameterResourceGroup, this.parameterResourceName);
	}

	/**
	 * Cleanup test environment
	 */
	public void doCleanup()
	{
		if (this.server == null) return;

		if (!this.isConcurrentAccess())
		{
		}
	}

	/**
	 * Test all functions
	 */
	public void testAll()
	{
		if (this.server == null) return;

		this.run();
	}

	@Override
	public void run()
	{
		// Check if server is instantiated
		if (this.server == null) return;

		// Setup
		if (!this.isConcurrentAccess()) this.doSetup();

		// Test
		this.testMediaAttribute();
		this.testMediaResource();
		this.testUploadFile(this.parameterResourceGroup, this.parameterResourceName, this.parameterDataIdentifierTestUpload);
		this.testObsoleteFile(this.parameterResourceGroup, this.parameterResourceName, this.parameterDataIdentifierTestObsolete);
		this.testStressTestUploadFiles(this.parameterResourceGroup, this.parameterResourceName, this.parameterTestCycleNumber, this.parameterTestCycleDataIdentifierFrom, this.parameterTestCycleDataIdentifierToo);

		// Cleanup
		if (!this.isConcurrentAccess()) this.doCleanup();
	}

	/**
	 * Test: Media Attribute
	 */
	public void testMediaAttribute()
	{
		TestManager.assertPrint(context, "===> testMediaAttribute()", null);

		try
		{
			List<String> attributeList = new ArrayList<String>();
			attributeList.add("MediaType");
			attributeList.add("FileTypes");
			attributeList.add("StorageLocation");
			attributeList.add("LogicalPath");
			attributeList.add("ServerEncoding");
			attributeList.add("ClientEncoding");

			String group = "Media";

			for (String name : attributeList)
			{
				ResourceContainer attribute = ResourceManager.attribute(context, group, name);

				String additionalText = "--> Media attribute: '" + group + "." + name + "' is not defined in the resource files.";

				String documentation = attribute.printManual(context);
				TestManager.assertNotContains(context, additionalText, documentation, "Resource.*.*.*.");
			}
		}
		catch (Exception e)
		{
			TestManager.assertPrintException(context, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Media Resource
	 */
	public void testMediaResource()
	{
		TestManager.assertPrint(context, "===> testMediaResource()", null);

		try
		{
			ResourceContainerMedia media = ResourceManager.media(context, "Test", "TestMediaResource");

			TestManager.assertTrue(context, null, media.isMediaTypeImage(context));
			TestManager.assertFalse(context, null, media.isMediaTypeVideo(context));
			TestManager.assertFalse(context, null, media.isMediaTypeAudio(context));
			TestManager.assertFalse(context, null, media.isMediaTypeDocument(context));
			TestManager.assertTrue(context, null, media.isFileTypeSupported(context, "JPG"));
			TestManager.assertTrue(context, null, media.isFileTypeSupported(context, "jpg"));
			TestManager.assertTrue(context, null, media.isFileTypeSupported(context, "png"));
			TestManager.assertFalse(context, null, media.isFileTypeSupported(context, "gif"));
			TestManager.assertFalse(context, null, media.isFileTypeSupported(context, "MKV"));
			TestManager.assertFalse(context, null, media.isFileTypeSupported(context, "mkv"));
			TestManager.assertFalse(context, null, media.isFileTypeSupported(context, "aaa"));
			TestManager.assertFalse(context, null, media.isStorageLocationServer(context));
			TestManager.assertFalse(context, null, media.isStorageLocationClient(context));
			TestManager.assertTrue(context, null, media.isStorageLocationSynchronize(context));
			TestManager.assertEqualsIgnoreCase(context, null, media.getLogicalPath(context), "test/media/resource");
			TestManager.assertTrue(context, null, media.isServerEncoding(context));
			TestManager.assertFalse(context, null, media.isClientEncoding(context));

			TestManager.assertEndsWith(context, null, media.getMediaRegularFilePath(context), "test/media/resource");
			TestManager.assertEndsWith(context, null, media.getMediaFileNameMask(context, "1234"), "-testmediaresource-00000000001234-*-*.*");
			TestManager.assertEndsWith(context, null, media.getMediaPendingFileName(context, "png"), ".png");
			TestManager.assertEndsWith(context, null, media.getMediaPendingFilePath(context), "test/media/resource/pending");
			TestManager.assertEndsWith(context, null, media.getMediaDeletedFilePath(context), "test/media/resource/deleted");
		}
		catch (Exception e)
		{
			TestManager.assertPrintException(context, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Upload File
	 */
	public void testUploadFile(String group, String name, String dataIdentifierString)
	{
		TestManager.assertPrint(context, "===> testUploadFile()", null);

		// Clean files and directories
		if (!this.isConcurrentAccess())
		{
			this.doRemoveMediaFiles(group, name, dataIdentifierString);
			this.doRemoveAllFilesInPendingDirectory(group, name);
		}

		// Get file List
		String uploadFilePath = TestManager.getTestStuffFilePath(context) + FileLocationManager.getPathElementDelimiterString() + "Images";
		List<String> fileNameList = FileUtil.fileSearchDirectoryForFiles(uploadFilePath, "*.jpg");

		// Try some uploads
		for (int i = 0; i < 20; i++)
		{
			int index = (int) (Math.random() * 14);
			this.doUploadFile(group, name, dataIdentifierString, fileNameList.get(index));
		}

		// Check pending directory
		if (!this.isConcurrentAccess())
		{
			this.doCheckClearedPendingDirectory(group, name);
		}
	}

	/**
	 * Test: Obsolete File
	 */
	public void testObsoleteFile(String group, String name, String dataIdentifierString)
	{
		TestManager.assertPrint(context, "===> testObsoleteFile()", null);

		if (this.isConcurrentAccess()) return;

		try
		{
			// Clean files and directories
			this.doRemoveMediaFiles(group, name, dataIdentifierString);
			this.doRemoveAllFilesInPendingDirectory(group, name);

			// Initialize variables
			ResourceContainerMedia mediaResource = ResourceManager.media(context, group, name);
			String mediaFileNameMask = mediaResource.getMediaFileNameMask(context, dataIdentifierString);
			String mediaFilePath = mediaResource.getMediaRegularFilePath(context);

			String uploadFilePath = TestManager.getTestStuffFilePath(context) + FileLocationManager.getPathElementDelimiterString() + "Images";
			List<String> fileNameList = FileUtil.fileSearchDirectoryForFiles(uploadFilePath, "*.jpg");

			// Override media files and count number of obsolete files
			int nuOfObsoleteFiles = -1;

			for (int i = 0; i < 10; i++)
			{
				// Override (upload) media file
				this.doUploadFile(group, name, dataIdentifierString, fileNameList.get(i));

				// Get list of obsolete files
				List<String> obsoleteFiles = FileUtil.fileSearchDirectoryOnObsoleteFiles(mediaFilePath, mediaFileNameMask);
				TestManager.assertNotNull(context, null, obsoleteFiles);
				TestManager.assertEquals(context, null, obsoleteFiles.size(), nuOfObsoleteFiles + i + 1);

				// The new uploaded file name must not be part of the list
				String recentFileName = mediaResource.getMediaRealFileName(context, dataIdentifierString);
				TestManager.assertFalse(context, null, obsoleteFiles.contains(recentFileName));
			}

			// Check pending directory
			this.doCheckClearedPendingDirectory(group, name);
		}
		catch (Exception e)
		{
			TestManager.assertPrintException(context, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Stress Test Upload Files
	 */
	private void testStressTestUploadFiles(String mediaResourceGroup, String mediaResourceName, int nuOfTestCycles, int dataIdentifierFrom, int dataIdentifierToo)
	{
		TestManager.assertPrint(context, "===> testStressTestUploadFiles()", null);

		try
		{
			String uploadFilePath = TestManager.getTestStuffFilePath(context) + FileLocationManager.getPathElementDelimiterString() + "Images";
			List<String> directoryList = new ArrayList<String>();
			directoryList.add(uploadFilePath);

			int dataIdentifierInteger = dataIdentifierFrom;

			for (int testCounter = 1; testCounter <= nuOfTestCycles; testCounter++)
			{
				String cycleText = "Test cycle " + String.valueOf(testCounter) + " of " + String.valueOf(nuOfTestCycles);
				TestManager.assertPrint(context, cycleText, null);

				for (String filePath : directoryList)
				{
					List<String> fileList = FileUtil.fileSearchDirectoryForFiles(filePath, "*.jpg");

					if (fileList != null)
					{
						for (String fileName : fileList)
						{
							// Upload file
							if (dataIdentifierInteger > dataIdentifierToo) dataIdentifierInteger = dataIdentifierFrom;
							this.doUploadFile(mediaResourceGroup, mediaResourceName, String.valueOf(dataIdentifierInteger), fileName);
							dataIdentifierInteger++;
						}
					}
				}
			}

			// Check pending directory
			if (!this.isConcurrentAccess())
			{
				this.doCheckClearedPendingDirectory(mediaResourceGroup, mediaResourceName);
			}
		}
		catch (Exception e)
		{
			TestManager.assertPrintException(context, "Unexpected Exception", e);
		}

		return;
	}

	/**
	 * Test: Upload File
	 */
	private void doUploadFile(String ResourceGroup, String resourceName, String dataIdentifierString, String uploadFileName)
	{
		try
		{
			ResourceContainerMedia mediaResource = ResourceManager.media(context, ResourceGroup, resourceName);

			// Upload a file
			String additionalText = "--> Tried to upload a file";
			additionalText += "\n--> Upload file? '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier? '" + dataIdentifierString + "'";

			boolean booleanResult = context.getMediaManager().mediaFileOperationUpload(context, mediaResource, uploadFileName, dataIdentifierString);
			TestManager.assertTrue(context, additionalText, booleanResult);

			// Check if file content can be read
			additionalText = "--> Tried to read file content of an uploaded file";
			additionalText += "\n--> Upload file? '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier? '" + dataIdentifierString + "'";

			MediaContainer mediaContainer = new MediaContainer(context, mediaResource, dataIdentifierString);
			TestManager.assertNotNull(context, additionalText, mediaContainer);

			if (mediaContainer != null)
			{
				// Bind media object
				booleanResult = mediaContainer.bindMedia();
				TestManager.assertTrue(context, additionalText + "\n--> Error on binding media file", booleanResult);

				// Compare check sum of source file and destination file
				if (!this.isConcurrentAccess())
				{
					long checksumSourceFile = FileUtil.fileGetChecksum(uploadFileName);
					TestManager.assertGreaterThan(context, additionalText + "\n--> Error on computing checksum of file '" + uploadFileName + "'", checksumSourceFile, 0);

					long checksumDestinationFile = FileUtil.fileGetChecksum(mediaContainer.getWorkingMediaFilePath());
					TestManager.assertGreaterThan(context, additionalText + "\n--> Error on computing checksum of file '" + mediaContainer.getWorkingMediaFilePath() + "'", checksumDestinationFile, 0);

					TestManager.assertEquals(context, additionalText + "\n--> Checksum mismatch", checksumSourceFile, checksumDestinationFile);
				}

				// Read file content
				byte[] contentAsByteBuffer = mediaContainer.readMediaContentAsByteArray();
				TestManager.assertNotNull(context, additionalText + "\n--> Error on reading media file content", contentAsByteBuffer);
				TestManager.assertGreaterThan(context, additionalText, contentAsByteBuffer.length, 0);

				// Release media file
				booleanResult = mediaContainer.releaseMedia();
				TestManager.assertTrue(context, additionalText + "\n--> Error on releasing media file", booleanResult);
			}

		}
		catch (Exception e)
		{
			TestManager.assertPrintException(context, "Unexpected Exception", e);
		}
	}

	/**
	 * Util: Remove media files, regarding a specific media resource container
	 * and a specific data identifier.
	 */
	private void doRemoveMediaFiles(String group, String name, String dataIdentifierString)
	{
		try
		{
			ResourceContainerMedia mediaResource = ResourceManager.media(context, group, name);
			String mediaFileNameMask = mediaResource.getMediaFileNameMask(context, dataIdentifierString);
			String mediaFilePath = mediaResource.getMediaRegularFilePath(context);
			List<String> mediaFiles = FileUtil.fileSearchDirectoryForFiles(mediaFilePath, mediaFileNameMask);

			if (mediaFiles != null && mediaFiles.size() > 0)
			{
				int nuOfDeletedFiles = FileUtil.fileDelete(mediaFiles);
				TestManager.assertEquals(context, null, mediaFiles.size(), nuOfDeletedFiles);
			}
		}
		catch (Exception e)
		{
			TestManager.assertPrintException(context, "Unexpected Exception", e);
		}
	}

	/**
	 * Util: Remove all files from the 'pending' directory, regarding a specific
	 * media resource container.
	 */
	private void doRemoveAllFilesInPendingDirectory(String group, String name)
	{
		try
		{
			ResourceContainerMedia mediaResource = ResourceManager.media(context, group, name);
			String pendingDirectory = mediaResource.getMediaPendingFilePath(context);
			TestManager.assertNotNull(context, null, pendingDirectory);

			boolean isSuccessful = FileUtil.fileCleanDirectory(pendingDirectory);
			TestManager.assertTrue(context, "--> Error on cleaning 'pending' directory '" + pendingDirectory + "'", isSuccessful);
		}
		catch (Exception e)
		{
			TestManager.assertPrintException(context, "Unexpected Exception", e);
		}
	}

	/**
	 * Util: Remove all files from the 'regular' directory, regarding a specific
	 * media resource container.
	 */
	private void doRemoveAllFilesInRegularDirectory(String group, String name)
	{
		try
		{
			ResourceContainerMedia mediaResource = ResourceManager.media(context, group, name);
			String regularDirectory = mediaResource.getMediaRegularFilePath(context);
			TestManager.assertNotNull(context, null, regularDirectory);

			boolean isSuccessful = FileUtil.fileCleanDirectory(regularDirectory);
			TestManager.assertTrue(context, "--> Error on cleaning 'regular' directory '" + regularDirectory + "'", isSuccessful);
		}
		catch (Exception e)
		{
			TestManager.assertPrintException(context, "Unexpected Exception", e);
		}
	}

	/**
	 * Util: Remove all files from the 'deleted' directory, regarding a specific
	 * media resource container.
	 */
	private void doRemoveAllFilesInDeletedDirectory(String group, String name)
	{
		try
		{
			ResourceContainerMedia mediaResource = ResourceManager.media(context, group, name);
			String deletedDirectory = mediaResource.getMediaDeletedFilePath(context);
			TestManager.assertNotNull(context, null, deletedDirectory);

			boolean isSuccessful = FileUtil.fileCleanDirectory(deletedDirectory);
			TestManager.assertTrue(context, "--> Error on cleaning 'deleted' directory '" + deletedDirectory + "'", isSuccessful);
		}
		catch (Exception e)
		{
			TestManager.assertPrintException(context, "Unexpected Exception", e);
		}
	}

	/**
	 * Util: Check if the pending directory is empty, regarding a specific media
	 * resource container.
	 */
	private void doCheckClearedPendingDirectory(String group, String name)
	{
		try
		{
			ResourceContainerMedia mediaResource = ResourceManager.media(context, group, name);
			String pendingDirectory = mediaResource.getMediaPendingFilePath(context);
			TestManager.assertNotNull(context, null, pendingDirectory);

			List<String> filelist = FileUtil.fileSearchDirectoryForFiles(pendingDirectory, "*");
			TestManager.assertNotNull(context, null, filelist);
			TestManager.assertEquals(context, "--> Files found in 'pending' directory '" + pendingDirectory + "'", filelist.size(), 0);
		}
		catch (Exception e)
		{
			TestManager.assertPrintException(context, "Unexpected Exception", e);
		}
	}

	/**
	 * Setter
	 */
	public void setParameterResourceGroup(String parameterResourceGroup)
	{
		this.parameterResourceGroup = parameterResourceGroup;
	}

	/**
	 * Setter
	 */
	public void setParameterResourceName(String parameterResourceName)
	{
		this.parameterResourceName = parameterResourceName;
	}

	/**
	 * Setter
	 */
	public void setParameterDataIdentifierTestUpload(String parameterDataIdentifierTestUpload)
	{
		this.parameterDataIdentifierTestUpload = parameterDataIdentifierTestUpload;
	}

	/**
	 * Setter
	 */
	public void setParameterDataIdentifierTestObsolete(String parameterDataIdentifierTestObsolete)
	{
		this.parameterDataIdentifierTestObsolete = parameterDataIdentifierTestObsolete;
	}

	/**
	 * Setter
	 */
	public void setParameterTestCycleNumber(int parameterTestCycleNumber)
	{
		this.parameterTestCycleNumber = parameterTestCycleNumber;
	}

	/**
	 * Setter
	 */
	public void setParameterTestCycleDataIdentifierFrom(int parameterTestCycleDataIdentifierFrom)
	{
		this.parameterTestCycleDataIdentifierFrom = parameterTestCycleDataIdentifierFrom;
	}

	/**
	 * Setter
	 */
	public void setParameterTestCycleDataIdentifierToo(int parameterTestCycleDataIdentifierToo)
	{
		this.parameterTestCycleDataIdentifierToo = parameterTestCycleDataIdentifierToo;
	}

	/**
	 * Getter
	 */
	public boolean isConcurrentAccess()
	{
		return concurrentAccess;
	}
}
