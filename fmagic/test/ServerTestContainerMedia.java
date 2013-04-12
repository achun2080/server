package fmagic.test;

import java.util.ArrayList;
import java.util.List;

import fmagic.basic.Context;
import fmagic.basic.FileLocationManager;
import fmagic.basic.FileUtil;
import fmagic.basic.MediaContainer;
import fmagic.basic.ResourceContainer;
import fmagic.basic.ResourceContainerMedia;
import fmagic.basic.ResourceManager;

public class ServerTestContainerMedia extends ServerTestContainer
{
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
	public ServerTestContainerMedia(Context context, boolean concurrentAccess)
	{
		super(context, concurrentAccess);
	}

	@Override
	public void executeComponentTest()
	{
		try
		{
			this.componentTestExecuteIntern();
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
		}
	}

	@Override
	public void setupComponentTest()
	{
		try
		{
			this.setupComponentTestIntern();
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
		}
	}

	@Override
	public void cleanupComponentTest()
	{
		try
		{
			this.cleanupComponentTestIntern();
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
		}
	}

	/**
	 * Component Test: Setup environment
	 */
	private void setupComponentTestIntern()
	{
		// Do nothing if the test is running in concurrent mode
		if (this.isConcurrentAccess()) return;

		// Setup
		try
		{
			// Clean 'pending' directory
			this.doRemoveAllFilesInPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);

			// Clean 'regular' directory
			this.doRemoveAllFilesInRegularDirectory(this.parameterResourceGroup, this.parameterResourceName);

			// Clean 'deleted' directory
			this.doRemoveAllFilesInDeletedDirectory(this.parameterResourceGroup, this.parameterResourceName);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
		}
	}

	/**
	 * Component Test: Execute
	 */
	private void componentTestExecuteIntern()
	{
		try
		{
			// Setup
			this.setupComponentTestIntern();

			// Test
			this.testMediaAttribute();
			this.testMediaResource();
			this.testUploadFile();
			this.testObsoleteFile();
			this.testUploadCycleFiles();

			// Cleanup
			this.cleanupComponentTestIntern();
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
		}
	}

	/**
	 * Component Test: Cleanup environment
	 */
	private void cleanupComponentTestIntern()
	{
		// Do nothing if the test is running in concurrent mode
		if (this.isConcurrentAccess()) return;

		// Cleanup
		try
		{
			// Clean 'pending' directory
			this.doRemoveAllFilesInPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);

			// Clean 'regular' directory
			this.doRemoveAllFilesInRegularDirectory(this.parameterResourceGroup, this.parameterResourceName);

			// Clean 'deleted' directory
			this.doRemoveAllFilesInDeletedDirectory(this.parameterResourceGroup, this.parameterResourceName);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
		}
	}

	@Override
	public void run()
	{
		try
		{
			this.componentTestExecuteIntern();
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Media Attribute
	 */
	public void testMediaAttribute()
	{
		TestManager.servicePrintHeader(this.getContext(), "===> testMediaAttribute()", null);

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
				ResourceContainer attribute = ResourceManager.attribute(this.getContext(), group, name);

				String additionalText = "--> Media attribute: '" + group + "." + name + "' is not defined in the resource files.";

				String documentation = attribute.printManual(this.getContext());
				TestManager.assertNotContains(this.getContext(), additionalText, documentation, "Resource.*.*.*.");
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Media Resource
	 */
	public void testMediaResource()
	{
		TestManager.servicePrintHeader(this.getContext(), "===> testMediaResource()", null);

		try
		{
			ResourceContainerMedia media = ResourceManager.media(this.getContext(), "Test", "TestMediaResource");

			TestManager.assertTrue(this.getContext(), null, media.isMediaTypeImage(this.getContext()));
			TestManager.assertFalse(this.getContext(), null, media.isMediaTypeVideo(this.getContext()));
			TestManager.assertFalse(this.getContext(), null, media.isMediaTypeAudio(this.getContext()));
			TestManager.assertFalse(this.getContext(), null, media.isMediaTypeDocument(this.getContext()));
			TestManager.assertTrue(this.getContext(), null, media.isFileTypeSupported(this.getContext(), "JPG"));
			TestManager.assertTrue(this.getContext(), null, media.isFileTypeSupported(this.getContext(), "jpg"));
			TestManager.assertTrue(this.getContext(), null, media.isFileTypeSupported(this.getContext(), "png"));
			TestManager.assertFalse(this.getContext(), null, media.isFileTypeSupported(this.getContext(), "gif"));
			TestManager.assertFalse(this.getContext(), null, media.isFileTypeSupported(this.getContext(), "MKV"));
			TestManager.assertFalse(this.getContext(), null, media.isFileTypeSupported(this.getContext(), "mkv"));
			TestManager.assertFalse(this.getContext(), null, media.isFileTypeSupported(this.getContext(), "aaa"));
			TestManager.assertFalse(this.getContext(), null, media.isStorageLocationServer(this.getContext()));
			TestManager.assertFalse(this.getContext(), null, media.isStorageLocationClient(this.getContext()));
			TestManager.assertTrue(this.getContext(), null, media.isStorageLocationSynchronize(this.getContext()));
			TestManager.assertEqualsIgnoreCase(this.getContext(), null, media.getLogicalPath(this.getContext()), "test/media/resource");
			TestManager.assertTrue(this.getContext(), null, media.isServerEncoding(this.getContext()));
			TestManager.assertFalse(this.getContext(), null, media.isClientEncoding(this.getContext()));

			TestManager.assertEndsWith(this.getContext(), null, media.getMediaRegularFilePath(this.getContext()), "test/media/resource");
			TestManager.assertEndsWith(this.getContext(), null, media.getMediaFileNameMask(this.getContext(), "1234"), "-testmediaresource-00000000001234-*-*.*");
			TestManager.assertEndsWith(this.getContext(), null, media.getMediaPendingFileName(this.getContext(), "png"), ".png");
			TestManager.assertEndsWith(this.getContext(), null, media.getMediaPendingFilePath(this.getContext()), "test/media/resource/pending");
			TestManager.assertEndsWith(this.getContext(), null, media.getMediaDeletedFilePath(this.getContext()), "test/media/resource/deleted");
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Upload File
	 */
	public void testUploadFile()
	{
		TestManager.servicePrintHeader(this.getContext(), "===> testUploadFile()", null);

		// Clean files and directories
		if (!this.isConcurrentAccess())
		{
			this.doRemoveMediaFiles(this.parameterResourceGroup, this.parameterResourceName, this.parameterDataIdentifierTestUpload);
			this.doRemoveAllFilesInPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);
		}

		// Get file List
		String uploadFilePath = FileLocationManager.compileFilePath(TestManager.getTestStuffFilePath(this.getContext()), "Images");
		List<String> fileNameList = FileUtil.fileSearchDirectoryForFiles(uploadFilePath, "*.jpg");

		// Try some uploads
		for (int i = 0; i < 20; i++)
		{
			int index = (int) (Math.random() * 16);
			this.doUploadFile(this.parameterResourceGroup, this.parameterResourceName, this.parameterDataIdentifierTestUpload, fileNameList.get(index));
		}

		// Check pending directory
		if (!this.isConcurrentAccess())
		{
			this.doCheckClearedPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);
		}
	}

	/**
	 * Test: Obsolete File
	 */
	public void testObsoleteFile()
	{
		TestManager.servicePrintHeader(this.getContext(), "===> testObsoleteFile()", null);

		if (this.isConcurrentAccess()) return;

		try
		{
			// Clean files and directories
			this.doRemoveMediaFiles(this.parameterResourceGroup, this.parameterResourceName, this.parameterDataIdentifierTestObsolete);
			this.doRemoveAllFilesInPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);

			// Initialize variables
			ResourceContainerMedia mediaResource = ResourceManager.media(this.getContext(), this.parameterResourceGroup, this.parameterResourceName);
			String mediaFileNameMask = mediaResource.getMediaFileNameMask(this.getContext(), this.parameterDataIdentifierTestObsolete);
			String mediaFilePath = mediaResource.getMediaRegularFilePath(this.getContext());

			String uploadFilePath = FileLocationManager.compileFilePath(TestManager.getTestStuffFilePath(this.getContext()), "Images");
			List<String> fileNameList = FileUtil.fileSearchDirectoryForFiles(uploadFilePath, "*.jpg");

			// Override media files and count number of obsolete files
			int nuOfObsoleteFiles = -1;

			for (int i = 0; i < 10; i++)
			{
				// Override (upload) media file
				this.doUploadFile(this.parameterResourceGroup, this.parameterResourceName, this.parameterDataIdentifierTestObsolete, fileNameList.get(i));

				// Get list of obsolete files
				List<String> obsoleteFiles = FileUtil.fileSearchDirectoryOnObsoleteFiles(mediaFilePath, mediaFileNameMask);
				TestManager.assertNotNull(this.getContext(), null, obsoleteFiles);
				TestManager.assertEquals(this.getContext(), null, obsoleteFiles.size(), nuOfObsoleteFiles + i + 1);

				// The new uploaded file name must not be part of the list
				String recentFileName = mediaResource.getMediaRealFileName(this.getContext(), this.parameterDataIdentifierTestObsolete);
				TestManager.assertFalse(this.getContext(), null, obsoleteFiles.contains(recentFileName));
			}

			// Check pending directory
			this.doCheckClearedPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Stress Test Upload Files
	 */
	public void testUploadCycleFiles()
	{
		TestManager.servicePrintHeader(this.getContext(), "===> testUploadCycleFiles()", null);

		try
		{
			String uploadFilePath = FileLocationManager.compileFilePath(TestManager.getTestStuffFilePath(this.getContext()), "Images");
			List<String> directoryList = new ArrayList<String>();
			directoryList.add(uploadFilePath);

			int dataIdentifierInteger = this.parameterTestCycleDataIdentifierFrom;

			for (int testCounter = 1; testCounter <= this.parameterTestCycleNumber; testCounter++)
			{
				String cycleText = "Test cycle " + String.valueOf(testCounter) + " of " + String.valueOf(this.parameterTestCycleNumber);
				TestManager.servicePrintSubLine(this.getContext(), cycleText);

				for (String filePath : directoryList)
				{
					List<String> fileList = FileUtil.fileSearchDirectoryForFiles(filePath, "*.jpg");

					if (fileList != null)
					{
						for (String fileName : fileList)
						{
							// Upload file
							if (dataIdentifierInteger > this.parameterTestCycleDataIdentifierToo) dataIdentifierInteger = this.parameterTestCycleDataIdentifierFrom;
							this.doUploadFile(this.parameterResourceGroup, this.parameterResourceName, String.valueOf(dataIdentifierInteger), fileName);
							dataIdentifierInteger++;
						}
					}
				}
			}

			// Check pending directory
			if (!this.isConcurrentAccess())
			{
				this.doCheckClearedPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
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
			ResourceContainerMedia mediaResource = ResourceManager.media(this.getContext(), ResourceGroup, resourceName);

			// Upload a file
			String additionalText = "--> Tried to upload a file";
			additionalText += "\n--> Upload file? '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier? '" + dataIdentifierString + "'";

			boolean booleanResult = this.getContext().getMediaManager().mediaFileOperationUpload(this.getContext(), mediaResource, uploadFileName, dataIdentifierString);
			TestManager.assertTrue(this.getContext(), additionalText, booleanResult);

			// Check if file content can be read
			additionalText = "--> Tried to read file content of an uploaded file";
			additionalText += "\n--> Upload file? '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier? '" + dataIdentifierString + "'";

			MediaContainer mediaContainer = new MediaContainer(this.getContext(), mediaResource, dataIdentifierString);
			TestManager.assertNotNull(this.getContext(), additionalText, mediaContainer);

			if (mediaContainer != null)
			{
				// Bind media object
				booleanResult = mediaContainer.bindMedia();
				TestManager.assertTrue(this.getContext(), additionalText + "\n--> Error on binding media file", booleanResult);

				// Compare check sum of source file and destination file
				if (!this.isConcurrentAccess())
				{
					TestManager.assertEqualsFile(this.getContext(), additionalText, uploadFileName, mediaContainer.getWorkingMediaFilePath());
				}

				// Read file content
				byte[] contentAsByteBuffer = mediaContainer.readMediaContentAsByteArray();
				TestManager.assertNotNull(this.getContext(), additionalText + "\n--> Error on reading media file content", contentAsByteBuffer);
				TestManager.assertGreaterThan(this.getContext(), additionalText, contentAsByteBuffer.length, 0);

				// Release media file
				booleanResult = mediaContainer.releaseMedia();
				TestManager.assertTrue(this.getContext(), additionalText + "\n--> Error on releasing media file", booleanResult);
			}

		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
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
			ResourceContainerMedia mediaResource = ResourceManager.media(this.getContext(), group, name);
			String mediaFileNameMask = mediaResource.getMediaFileNameMask(this.getContext(), dataIdentifierString);
			String mediaFilePath = mediaResource.getMediaRegularFilePath(this.getContext());
			List<String> mediaFiles = FileUtil.fileSearchDirectoryForFiles(mediaFilePath, mediaFileNameMask);

			if (mediaFiles != null && mediaFiles.size() > 0)
			{
				int nuOfDeletedFiles = FileUtil.fileDelete(mediaFiles);
				TestManager.assertEquals(this.getContext(), null, mediaFiles.size(), nuOfDeletedFiles);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
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
			ResourceContainerMedia mediaResource = ResourceManager.media(this.getContext(), group, name);
			String pendingDirectory = mediaResource.getMediaPendingFilePath(this.getContext());
			TestManager.assertNotNull(this.getContext(), null, pendingDirectory);

			boolean isSuccessful = FileUtil.fileCleanDirectory(pendingDirectory);
			TestManager.assertTrue(this.getContext(), "--> Error on cleaning 'pending' directory '" + pendingDirectory + "'", isSuccessful);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
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
			ResourceContainerMedia mediaResource = ResourceManager.media(this.getContext(), group, name);
			String regularDirectory = mediaResource.getMediaRegularFilePath(this.getContext());
			TestManager.assertNotNull(this.getContext(), null, regularDirectory);

			boolean isSuccessful = FileUtil.fileCleanDirectory(regularDirectory);
			TestManager.assertTrue(this.getContext(), "--> Error on cleaning 'regular' directory '" + regularDirectory + "'", isSuccessful);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
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
			ResourceContainerMedia mediaResource = ResourceManager.media(this.getContext(), group, name);
			String deletedDirectory = mediaResource.getMediaDeletedFilePath(this.getContext());
			TestManager.assertNotNull(this.getContext(), null, deletedDirectory);

			boolean isSuccessful = FileUtil.fileCleanDirectory(deletedDirectory);
			TestManager.assertTrue(this.getContext(), "--> Error on cleaning 'deleted' directory '" + deletedDirectory + "'", isSuccessful);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
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
			ResourceContainerMedia mediaResource = ResourceManager.media(this.getContext(), group, name);
			String pendingDirectory = mediaResource.getMediaPendingFilePath(this.getContext());
			TestManager.assertNotNull(this.getContext(), null, pendingDirectory);

			List<String> filelist = FileUtil.fileSearchDirectoryForFiles(pendingDirectory, "*");
			TestManager.assertNotNull(this.getContext(), null, filelist);
			TestManager.assertEquals(this.getContext(), "--> Files found in 'pending' directory '" + pendingDirectory + "'", filelist.size(), 0);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), "Unexpected Exception", e);
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
}
