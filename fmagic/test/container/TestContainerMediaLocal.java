package fmagic.test.container;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fmagic.basic.context.Context;
import fmagic.basic.file.FileLocationFunctions;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.media.MediaContainer;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;
import fmagic.test.application.TestManager;
import fmagic.test.runner.TestRunner;

public class TestContainerMediaLocal extends TestContainer
{
	private String parameterResourceGroup = "Apartment";
	private String parameterResourceName = "Room";
	private int parameterNumberOfMediaToBeUploaded = 50;
	private String parameterDataIdentifierTestUpload = "1234";
	private String parameterDataIdentifierTestObsolete = "1235";
	private int parameterTestCycleNumberOfFiles = 100;
	private int parameterTestCycleDataIdentifierFrom = 1;
	private int parameterTestCycleDataIdentifierToo = 10;

	// Check cleaning function codes
	public static enum CleaningFunctionEnum
	{
		PENDING, DELETED
	}

	/**
	 * Constructor 1
	 * 
	 * @param context
	 *            The application context.
	 * 
	 * @param testRunner
	 *            The test runner that holds this container, or <TT>null</TT> if
	 *            no test runner is available.
	 * 
	 * @param concurrentAccess
	 *            Set to <TT>true</TT> if the test container is supposed to run
	 *            in a concurrent environment with other parallel threads or
	 *            applications, otherwise to <TT>false</TT>.
	 */
	public TestContainerMediaLocal(Context context, TestRunner testRunner,
			boolean concurrentAccess)
	{
		super(context, testRunner, concurrentAccess);
	}

	/**
	 * Constructor 2
	 * 
	 * @param testRunner
	 *            The test runner that holds this container, or <TT>null</TT> if
	 *            no test runner is available.
	 */
	public TestContainerMediaLocal(TestRunner testRunner)
	{
		super(null, testRunner, false);
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
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			this.testUploadFileLocal();
			this.testMaximumMediaSize();
			this.testObsoleteFile();
			this.testExpiredPendingFiles();
			this.testExpiredDeletedFiles();
			this.testExpiredObsoleteFiles();
			this.testCleaningAll();
			this.testUploadCycleFiles();

			// Cleanup
			this.cleanupComponentTestIntern();
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			attributeList.add("MaximumMediaSize");

			String group = "Media";

			for (String name : attributeList)
			{
				ResourceContainer attribute = ResourceManager.attribute(this.getContext(), group, name);

				String additionalText = "--> Media attribute: '" + group + "." + name + "' is not defined in the resource files.";

				String documentation = attribute.printManual(this.getContext());
				TestManager.assertNotContains(this.getContext(), this, additionalText, documentation, "Resource.*.*.*.");
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: XXXXXXXXXX
	 */
	public void xxxxxxxxxx()
	{
		TestManager.servicePrintHeader(this.getContext(), "===> xxxxxxxxxx()", null);

		try
		{
			// ResourceContainerMedia media =
			// ResourceManager.media(this.getContext(), "Test",
			// "TestMediaResource");
			// Integer maximumMediaSize =
			// media.getMaximumMediaSize(this.getContext());
			// TestManager.assertNotNull(this.getContext(), this, null,
			// maximumMediaSize);
			// TestManager.assertEquals(this.getContext(), this, null,
			// media.getMaximumMediaSize(this.getContext()), 99);
			this.testMediaResource();
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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

			TestManager.assertTrue(this.getContext(), this, null, media.attributeIsMediaTypeImage(this.getContext()));
			TestManager.assertFalse(this.getContext(), this, null, media.attributeIsMediaTypeVideo(this.getContext()));
			TestManager.assertFalse(this.getContext(), this, null, media.attributeIsMediaTypeAudio(this.getContext()));
			TestManager.assertFalse(this.getContext(), this, null, media.attributeIsMediaTypeDocument(this.getContext()));
			TestManager.assertTrue(this.getContext(), this, null, media.attributeIsFileTypeSupported(this.getContext(), "JPG"));
			TestManager.assertTrue(this.getContext(), this, null, media.attributeIsFileTypeSupported(this.getContext(), "jpg"));
			TestManager.assertTrue(this.getContext(), this, null, media.attributeIsFileTypeSupported(this.getContext(), "png"));
			TestManager.assertFalse(this.getContext(), this, null, media.attributeIsFileTypeSupported(this.getContext(), "gif"));
			TestManager.assertFalse(this.getContext(), this, null, media.attributeIsFileTypeSupported(this.getContext(), "MKV"));
			TestManager.assertFalse(this.getContext(), this, null, media.attributeIsFileTypeSupported(this.getContext(), "mkv"));
			TestManager.assertFalse(this.getContext(), this, null, media.attributeIsFileTypeSupported(this.getContext(), "aaa"));
			TestManager.assertFalse(this.getContext(), this, null, media.attributeIsStorageLocationServer(this.getContext()));
			TestManager.assertFalse(this.getContext(), this, null, media.attributeIsStorageLocationClient(this.getContext()));
			TestManager.assertTrue(this.getContext(), this, null, media.attributeIsStorageLocationSynchronize(this.getContext()));
			TestManager.assertEqualsIgnoreCase(this.getContext(), this, null, media.attributeGetLogicalPath(this.getContext()), "test/media/resource");
			TestManager.assertTrue(this.getContext(), this, null, media.attributeIsServerEncoding(this.getContext()));
			TestManager.assertFalse(this.getContext(), this, null, media.attributeIsClientEncoding(this.getContext()));

			Integer maximumMediaSize = media.attributeGetMaximumMediaSize(this.getContext());
			TestManager.assertNotNull(this.getContext(), this, null, maximumMediaSize);
			if (maximumMediaSize != null) TestManager.assertEquals(this.getContext(), this, null, maximumMediaSize, 99);

			TestManager.assertEndsWith(this.getContext(), this, null, media.mediaFileGetRegularFilePath(this.getContext()), "test/media/resource");
			TestManager.assertEndsWith(this.getContext(), this, null, media.mediaFileGetNameMask(this.getContext(), "1234"), "-testmediaresource-00000000001234-*-*.*");
			TestManager.assertEndsWith(this.getContext(), this, null, media.mediaFileGetPendingFileName(this.getContext(), "png"), ".png");
			TestManager.assertEndsWith(this.getContext(), this, null, media.mediaFileGetPendingFilePath(this.getContext()), "test/media/resource/pending");
			TestManager.assertEndsWith(this.getContext(), this, null, media.mediaFileGetDeletedFilePath(this.getContext()), "test/media/resource/deleted");
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Upload File
	 */
	public void testUploadFileLocal()
	{
		TestManager.servicePrintHeader(this.getContext(), "===> testUploadFile()", null);

		try
		{
			// Clean files and directories
			if (!this.isConcurrentAccess())
			{
				this.doRemoveMediaFiles(this.parameterResourceGroup, this.parameterResourceName, this.parameterDataIdentifierTestUpload);
				this.doRemoveAllFilesInPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);
			}

			// Get file directory
			ResourceContainer configuration = ResourceManager.configuration(this.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = this.getContext().getConfigurationManager().getProperty(this.getContext(), configuration, true);

			String additionalText = "--> Tried to read the directory for the media files to process during test";
			additionalText += "\n--> Please set the test configuration parameter '" + configuration.getRecourceIdentifier() + "' for the application '" + this.getContext().getCodeName() + "'";
			TestManager.assertNotNull(this.getContext(), this, additionalText, uploadFilePath);

			// Get file List
			List<String> fileList = FileUtilFunctions.directorySearchForFiles(uploadFilePath, "*.jpg");

			additionalText = "--> Tried to read media files in directory '" + uploadFilePath + "'";
			TestManager.assertNotNull(this.getContext(), this, additionalText, fileList);
			if (fileList != null) TestManager.assertGreaterThan(this.getContext(), this, additionalText, fileList.size(), 0);

			// Try some uploads
			for (int i = 0; i < this.parameterNumberOfMediaToBeUploaded; i++)
			{
				int index = FileUtilFunctions.generalGetRandomValue(0, fileList.size() - 1);
				this.doUploadFile(this.parameterResourceGroup, this.parameterResourceName, this.parameterDataIdentifierTestUpload, fileList.get(index));
			}

			// Check pending directory
			if (!this.isConcurrentAccess())
			{
				this.doCheckClearedPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Maximum Media Size
	 */
	public void testMaximumMediaSize()
	{
		// Do nothing if the test is running in concurrent mode
		if (this.isConcurrentAccess()) return;
		
		TestManager.servicePrintHeader(this.getContext(), "===> testMaximumMediaSize()", null);

		try
		{
			/*
			 * Get file directory
			 */
			ResourceContainer configuration = ResourceManager.configuration(this.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = this.getContext().getConfigurationManager().getProperty(this.getContext(), configuration, true);

			String additionalText = "--> Tried to read the directory for the media files to process during test";
			additionalText += "\n--> Please set the test configuration parameter '" + configuration.getRecourceIdentifier() + "' for the application '" + this.getContext().getCodeName() + "'";
			TestManager.assertNotNull(this.getContext(), this, additionalText, uploadFilePath);

			/*
			 * Get file List
			 */
			List<String> fileList = FileUtilFunctions.directorySearchForFiles(uploadFilePath, "*.jpg");

			additionalText = "--> Tried to read media files in directory '" + uploadFilePath + "'";
			TestManager.assertNotNull(this.getContext(), this, additionalText, fileList);
			if (fileList != null) TestManager.assertGreaterThan(this.getContext(), this, additionalText, fileList.size(), 0);

			if (fileList == null) return;
			if (fileList.size() == 0) return;

			/*
			 *  Get current configuration property
			 */
			Integer lastValueOfMaximumMediaSize = this.getContext().getMediaManager().getMaximumMediaSize();
			if (lastValueOfMaximumMediaSize == null) lastValueOfMaximumMediaSize = 0;

			/*
			 *  Check a couple of media resource settings
			 */
			this.getContext().getMediaManager().testSetMaximumMediaSize(this.getContext(), 5000);
			
			this.doMaximumMediaSizeAttribute(fileList, "Test", "Size50", 50);
			this.doMaximumMediaSizeAttribute(fileList, "Test", "Size500", 500);
			this.doMaximumMediaSizeAttribute(fileList, "Test", "Size3000", 3000);

			/*
			 *  Check the configuration setting
			 */
			this.getContext().getMediaManager().testSetMaximumMediaSize(this.getContext(), 200);
			this.doMaximumMediaSizeConfiguration(fileList, "Test", "Size3000", 200);

			this.getContext().getMediaManager().testSetMaximumMediaSize(this.getContext(), 700);
			this.doMaximumMediaSizeConfiguration(fileList, "Test", "Size3000", 700);

			this.getContext().getMediaManager().testSetMaximumMediaSize(this.getContext(), 1400);
			this.doMaximumMediaSizeConfiguration(fileList, "Test", "Size3000", 1400);

			/*
			 *  Reset current configuration property
			 */
			this.getContext().getMediaManager().testSetMaximumMediaSize(this.getContext(), lastValueOfMaximumMediaSize);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			String mediaFileNameMask = mediaResource.mediaFileGetNameMask(this.getContext(), this.parameterDataIdentifierTestObsolete);
			String mediaFilePath = mediaResource.mediaFileGetRegularFilePath(this.getContext());

			// Get file directory
			ResourceContainer configuration = ResourceManager.configuration(this.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = this.getContext().getConfigurationManager().getProperty(this.getContext(), configuration, true);

			String additionalText = "--> Tried to read the directory for the media files to process during test";
			additionalText += "\n--> Please set the test configuration parameter '" + configuration.getRecourceIdentifier() + "' for the application '" + this.getContext().getCodeName() + "'";
			TestManager.assertNotNull(this.getContext(), this, additionalText, uploadFilePath);

			// Get file List
			List<String> fileList = FileUtilFunctions.directorySearchForFiles(uploadFilePath, "*.jpg");

			additionalText = "--> Tried to read media files in directory '" + uploadFilePath + "'";
			TestManager.assertNotNull(this.getContext(), this, additionalText, fileList);
			if (fileList != null) TestManager.assertGreaterThan(this.getContext(), this, additionalText, fileList.size(), 0);

			// Override media files and count number of obsolete files
			int nuOfObsoleteFiles = -1;

			for (int i = 0; i < 10; i++)
			{
				// Override (upload) media file
				this.doUploadFile(this.parameterResourceGroup, this.parameterResourceName, this.parameterDataIdentifierTestObsolete, fileList.get(i));

				// Get list of obsolete files
				List<String> obsoleteFiles = FileUtilFunctions.directorySearchOnObsoleteFiles(mediaFilePath, mediaFileNameMask, null);
				TestManager.assertNotNull(this.getContext(), this, null, obsoleteFiles);
				TestManager.assertEquals(this.getContext(), this, null, obsoleteFiles.size(), nuOfObsoleteFiles + i + 1);

				// The new uploaded file name must not be part of the list
				String recentFileName = mediaResource.mediaFileGetRealFileName(this.getContext(), this.parameterDataIdentifierTestObsolete);
				TestManager.assertFalse(this.getContext(), this, null, obsoleteFiles.contains(recentFileName));
			}

			// Check pending directory
			this.doCheckClearedPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Expired pending files
	 */
	public void testExpiredPendingFiles()
	{
		TestManager.servicePrintHeader(this.getContext(), "===> testExpiredPendingFiles()", null);

		if (this.isConcurrentAccess()) return;

		// Clean 'pending' directory
		this.doRemoveAllFilesInPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);

		// Execute function
		this.doExpiredFiles(this.parameterResourceGroup, this.parameterResourceName, CleaningFunctionEnum.PENDING);

		// Clean 'pending' directory
		this.doRemoveAllFilesInPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);
	}

	/**
	 * Test: Expired deleted files
	 */
	public void testExpiredDeletedFiles()
	{
		TestManager.servicePrintHeader(this.getContext(), "===> testExpiredDeletedFiles()", null);

		if (this.isConcurrentAccess()) return;

		// Clean 'deleted' directory
		this.doRemoveAllFilesInDeletedDirectory(this.parameterResourceGroup, this.parameterResourceName);

		// Execute function
		this.doExpiredFiles(this.parameterResourceGroup, this.parameterResourceName, CleaningFunctionEnum.DELETED);

		// Clean 'deleted' directory
		this.doRemoveAllFilesInDeletedDirectory(this.parameterResourceGroup, this.parameterResourceName);
	}

	/**
	 * Test: Expired obsolete files
	 */
	public void testExpiredObsoleteFiles()
	{
		TestManager.servicePrintHeader(this.getContext(), "===> testExpiredObsoleteFiles()", null);

		if (this.isConcurrentAccess()) return;

		// Clean directories
		this.doRemoveAllFilesInDeletedDirectory(this.parameterResourceGroup, this.parameterResourceName);
		this.doRemoveAllFilesInRegularDirectory(this.parameterResourceGroup, this.parameterResourceName);

		// Execute function
		this.doExpiredObsoleteFiles(this.parameterResourceGroup, this.parameterResourceName);

		// Clean directories
		this.doRemoveAllFilesInDeletedDirectory(this.parameterResourceGroup, this.parameterResourceName);
		this.doRemoveAllFilesInRegularDirectory(this.parameterResourceGroup, this.parameterResourceName);
	}

	/**
	 * Test: Cleaning all resources
	 */
	public void testCleaningAll()
	{
		TestManager.servicePrintHeader(this.getContext(), "===> testCleaningAll()", null);

		if (this.isConcurrentAccess()) return;

		// Clean directories
		this.doRemoveAllFilesInPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);
		this.doRemoveAllFilesInDeletedDirectory(this.parameterResourceGroup, this.parameterResourceName);
		this.doRemoveAllFilesInRegularDirectory(this.parameterResourceGroup, this.parameterResourceName);

		// Execute function
		this.doCleaningAll();

		// Clean directories
		this.doRemoveAllFilesInPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);
		this.doRemoveAllFilesInDeletedDirectory(this.parameterResourceGroup, this.parameterResourceName);
		this.doRemoveAllFilesInRegularDirectory(this.parameterResourceGroup, this.parameterResourceName);
	}

	/**
	 * Test: Stress Test Upload Files
	 */
	public void testUploadCycleFiles()
	{
		TestManager.servicePrintHeader(this.getContext(), "===> testUploadCycleFiles()", null);

		try
		{
			// Get file directory
			ResourceContainer configuration = ResourceManager.configuration(this.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = this.getContext().getConfigurationManager().getProperty(this.getContext(), configuration, true);

			String additionalText = "--> Tried to read the directory for the media files to process during test";
			additionalText += "\n--> Please set the test configuration parameter '" + configuration.getRecourceIdentifier() + "' for the application '" + this.getContext().getCodeName() + "'";
			TestManager.assertNotNull(this.getContext(), this, additionalText, uploadFilePath);

			// Get file List
			List<String> fileList = FileUtilFunctions.directorySearchForFiles(uploadFilePath, "*.jpg");

			additionalText = "--> Tried to read media files in directory '" + uploadFilePath + "'";
			TestManager.assertNotNull(this.getContext(), this, additionalText, fileList);
			if (fileList != null) TestManager.assertGreaterThan(this.getContext(), this, additionalText, fileList.size(), 0);

			// Get parameter
			int dataIdentifierInteger = this.parameterTestCycleDataIdentifierFrom;
			TestManager.assertGreaterThan(this.getContext(), this, null, dataIdentifierInteger, 0);

			int nuOfFilesToTest = this.parameterTestCycleNumberOfFiles;
			TestManager.assertGreaterThan(this.getContext(), this, null, nuOfFilesToTest, 0);
			TestManager.servicePrintSubLine(this.getContext(), "Files to process: '" + String.valueOf(nuOfFilesToTest) + "'");

			// Initialize
			int fileListCounter = 0;

			// Test cycle
			for (int fileTestCounter = 1; fileTestCounter <= nuOfFilesToTest; fileTestCounter++)
			{
				// Get next file path from list
				String filePath = fileList.get(fileListCounter++);
				if (fileListCounter >= fileList.size()) fileListCounter = 0;
				TestManager.assertNotNull(this.getContext(), this, null, filePath);

				// Upload file
				if (dataIdentifierInteger > this.parameterTestCycleDataIdentifierToo) dataIdentifierInteger = this.parameterTestCycleDataIdentifierFrom;
				this.doUploadFile(this.parameterResourceGroup, this.parameterResourceName, String.valueOf(dataIdentifierInteger), filePath);
				dataIdentifierInteger++;
			}

			// Check pending directory
			if (!this.isConcurrentAccess())
			{
				this.doCheckClearedPendingDirectory(this.parameterResourceGroup, this.parameterResourceName);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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

			/*
			 * Upload a file
			 */
			String additionalText = "--> Tried to upload a file";
			additionalText += "\n--> Upload file? '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier? '" + dataIdentifierString + "'";

			boolean booleanResult = this.getContext().getMediaManager().operationStoreLocal(this.getContext(), mediaResource, uploadFileName, dataIdentifierString);
			TestManager.assertTrue(this.getContext(), this, additionalText, booleanResult);

			/*
			 * Check if file content can be read
			 */
			additionalText = "--> Tried to read file content of an uploaded file";
			additionalText += "\n--> Upload file? '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier? '" + dataIdentifierString + "'";

			MediaContainer mediaContainer = new MediaContainer(this.getContext(), mediaResource, dataIdentifierString);
			TestManager.assertNotNull(this.getContext(), this, additionalText, mediaContainer);

			// Bind media object
			booleanResult = mediaContainer.bindMedia();
			TestManager.assertTrue(this.getContext(), this, additionalText + "\n--> Error on binding media file", booleanResult);

			// Compare check sum of source file and destination file
			if (!this.isConcurrentAccess())
			{
				// Check always: If the checksum of the uploaded file and
				// the working file are the same, all the same if the media
				// files are encoded.
				TestManager.assertEqualsFile(this.getContext(), this, additionalText, uploadFileName, mediaContainer.getWorkingMediaFilePath());

				// Check if encoding is enabled: If the checksum of
				// the uploaded file and the original file are different.
				if (this.getContext().getMediaManager().isEncodingEnabled(this.getContext(), mediaResource) == true)
				{
					TestManager.assertNotEqualsFile(this.getContext(), this, additionalText, uploadFileName, mediaContainer.getOriginalMediaFilePath());
				}
			}

			// Read file content
			byte[] contentAsByteBuffer = mediaContainer.readMediaContentAsByteArray();
			TestManager.assertNotNull(this.getContext(), this, additionalText + "\n--> Error on reading media file content", contentAsByteBuffer);
			TestManager.assertGreaterThan(this.getContext(), this, additionalText, contentAsByteBuffer.length, 0);

			// Release media file
			booleanResult = mediaContainer.releaseMedia();
			TestManager.assertTrue(this.getContext(), this, additionalText + "\n--> Error on releasing media file", booleanResult);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			String mediaFileNameMask = mediaResource.mediaFileGetNameMask(this.getContext(), dataIdentifierString);
			String mediaFilePath = mediaResource.mediaFileGetRegularFilePath(this.getContext());
			List<String> mediaFiles = FileUtilFunctions.directorySearchForFiles(mediaFilePath, mediaFileNameMask);

			if (mediaFiles != null && mediaFiles.size() > 0)
			{
				int nuOfDeletedFiles = FileUtilFunctions.fileDelete(mediaFiles);
				TestManager.assertEquals(this.getContext(), this, null, mediaFiles.size(), nuOfDeletedFiles);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			String pendingDirectory = mediaResource.mediaFileGetPendingFilePath(this.getContext());
			TestManager.assertNotNull(this.getContext(), this, null, pendingDirectory);

			if (FileUtilFunctions.directoryExists(pendingDirectory))
			{
				boolean isSuccessful = FileUtilFunctions.directoryDeleteAllFiles(pendingDirectory);
				TestManager.assertTrue(this.getContext(), this, "--> Error on cleaning 'pending' directory '" + pendingDirectory + "'", isSuccessful);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			String regularDirectory = mediaResource.mediaFileGetRegularFilePath(this.getContext());
			TestManager.assertNotNull(this.getContext(), this, null, regularDirectory);

			if (FileUtilFunctions.directoryExists(regularDirectory))
			{
				boolean isSuccessful = FileUtilFunctions.directoryDeleteAllFiles(regularDirectory);
				TestManager.assertTrue(this.getContext(), this, "--> Error on cleaning 'regular' directory '" + regularDirectory + "'", isSuccessful);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			String deletedDirectory = mediaResource.mediaFileGetDeletedFilePath(this.getContext());
			TestManager.assertNotNull(this.getContext(), this, null, deletedDirectory);

			if (FileUtilFunctions.directoryExists(deletedDirectory))
			{
				boolean isSuccessful = FileUtilFunctions.directoryDeleteAllFiles(deletedDirectory);
				TestManager.assertTrue(this.getContext(), this, "--> Error on cleaning 'deleted' directory '" + deletedDirectory + "'", isSuccessful);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			String pendingDirectory = mediaResource.mediaFileGetPendingFilePath(this.getContext());
			TestManager.assertNotNull(this.getContext(), this, null, pendingDirectory);

			List<String> filelist = FileUtilFunctions.directorySearchForFiles(pendingDirectory, "*");
			TestManager.assertNotNull(this.getContext(), this, null, filelist);
			TestManager.assertEquals(this.getContext(), this, "--> Files found in 'pending' directory '" + pendingDirectory + "'", filelist.size(), 0);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Expired 'pending' and 'deleted' files
	 */
	private void doExpiredFiles(String group, String name, CleaningFunctionEnum cleaningFunction)
	{
		try
		{
			/*
			 * Prepare
			 */

			// Initialize variables
			ResourceContainerMedia mediaResource = ResourceManager.media(this.getContext(), group, name);

			int daysToKeep = 0;
			int dayRange = 0;

			if (cleaningFunction == CleaningFunctionEnum.PENDING)
			{
				daysToKeep = this.getContext().getMediaManager().getCleanPendingDaysToKeep();
				dayRange = daysToKeep * 2;
			}
			else if (cleaningFunction == CleaningFunctionEnum.DELETED)
			{
				daysToKeep = this.getContext().getMediaManager().getCleanDeletedDaysToKeep();
				dayRange = daysToKeep * 2;
			}
			else
			{
				TestManager.servicePrintError(this.getContext(), this, "CleaningFunctionEnum not supported", null);
				return;
			}

			Date currentDate = new Date();
			Date daysToKeepDate = FileUtilFunctions.generalAddTimeDiff(currentDate, -daysToKeep, 0, 0, 0);

			// Get file directory of images
			ResourceContainer configuration = ResourceManager.configuration(this.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = this.getContext().getConfigurationManager().getProperty(this.getContext(), configuration, true);

			String additionalText = "--> Tried to read the directory for the media files to process during test";
			additionalText += "\n--> Please set the test configuration parameter '" + configuration.getRecourceIdentifier() + "' for the application '" + this.getContext().getCodeName() + "'";
			TestManager.assertNotNull(this.getContext(), this, additionalText, uploadFilePath);

			// Get file List
			List<String> fileList = FileUtilFunctions.directorySearchForFiles(uploadFilePath, "*.jpg");
			additionalText = "--> Tried to read media files in directory '" + uploadFilePath + "'";

			// No file list
			if (fileList == null)
			{
				TestManager.assertNotNull(this.getContext(), this, additionalText, fileList);
				return;
			}

			if (fileList.size() == 0)
			{
				TestManager.assertGreaterThan(this.getContext(), this, additionalText, fileList.size(), 0);
				return;
			}

			/*
			 * Creating a bunch of media files in the directory to test
			 * (pending, deleted)
			 */
			int nuOfFilesCreated = 40;
			int minuteCounter = 0;
			int secondCounter = 0;

			for (int i = 0; i < nuOfFilesCreated; i++)
			{
				// Get media file to copy
				int index = FileUtilFunctions.generalGetRandomValue(0, fileList.size() - 1);
				TestManager.assertEqualsRange(this.getContext(), this, null, 0, fileList.size() - 1, index);

				String mediaFilePathToCopy = fileList.get(index);
				TestManager.assertNotEmpty(this.getContext(), this, null, mediaFilePathToCopy);

				String fileType = FileUtilFunctions.fileGetFileTypePart(mediaFilePathToCopy);
				TestManager.assertNotEmpty(this.getContext(), this, null, fileType);

				// Get file name of the file to be created
				String filePathToCreate = null;

				if (cleaningFunction == CleaningFunctionEnum.PENDING)
				{
					filePathToCreate = FileLocationFunctions.compileFilePath(mediaResource.mediaFileGetPendingFilePath(this.getContext()), mediaResource.mediaFileGetPendingFileName(this.getContext(), fileType));
				}
				else if (cleaningFunction == CleaningFunctionEnum.DELETED)
				{
					filePathToCreate = FileLocationFunctions.compileFilePath(mediaResource.mediaFileGetDeletedFilePath(this.getContext()), mediaResource.mediaFileGetPendingFileName(this.getContext(), fileType));
				}
				else
				{
					TestManager.servicePrintError(this.getContext(), this, "CleaningFunctionEnum not supported", null);
					return;
				}

				TestManager.assertNotEmpty(this.getContext(), this, null, filePathToCreate);

				// Create file
				int resultInteger = FileUtilFunctions.fileCopyRetry(mediaFilePathToCopy, filePathToCreate);
				TestManager.assertGreaterThan(this.getContext(), this, null, resultInteger, 0);

				// Set a 'modified date' in a range of x days
				int daySetting = FileUtilFunctions.generalGetRandomValue(0, dayRange);
				TestManager.assertEqualsRange(this.getContext(), this, null, 0, dayRange, daySetting);

				Date modifiedDate = FileUtilFunctions.generalAddTimeDiff(currentDate, -daySetting, -1, minuteCounter--, secondCounter--);
				TestManager.assertNotEmpty(this.getContext(), this, null, fileType);

				resultInteger = FileUtilFunctions.fileSetLastModifiedRetry(filePathToCreate, modifiedDate);
				TestManager.assertGreaterThan(this.getContext(), this, null, resultInteger, 0);
			}

			/*
			 * Validate
			 */

			// Invoke regular clean function of the media manager
			TestManager.assertGreaterThan(this.getContext(), this, null, daysToKeep, 1);

			String remainingFilePath = "";
			int nuOfDeletedFiles = 0;

			if (cleaningFunction == CleaningFunctionEnum.PENDING)
			{
				remainingFilePath = mediaResource.mediaFileGetPendingFilePath(this.getContext());
				nuOfDeletedFiles = this.getContext().getMediaManager().cleanPendingDirectory(this.getContext(), mediaResource, daysToKeep);
			}
			else if (cleaningFunction == CleaningFunctionEnum.DELETED)
			{
				remainingFilePath = mediaResource.mediaFileGetDeletedFilePath(this.getContext());
				nuOfDeletedFiles = this.getContext().getMediaManager().cleanDeletedDirectory(this.getContext(), mediaResource, daysToKeep);
			}
			else
			{
				TestManager.servicePrintError(this.getContext(), this, "CleaningFunctionEnum not supported", null);
				return;
			}

			TestManager.assertGreaterThan(this.getContext(), this, null, nuOfDeletedFiles, 0);
			TestManager.assertLowerThan(this.getContext(), this, null, nuOfDeletedFiles, nuOfFilesCreated);

			// Get list of all files remained
			List<String> fileRemainingList = FileUtilFunctions.directorySearchForFiles(remainingFilePath, "*");
			TestManager.assertNotNull(this.getContext(), this, null, fileRemainingList);

			// Go through the list and check 'modified date'
			if (fileRemainingList != null)
			{
				int nuOfRemainingFiles = fileRemainingList.size();
				TestManager.assertGreaterThan(this.getContext(), this, null, nuOfRemainingFiles, 0);

				additionalText = "--> Mismatch about the number of created files, the number of deleted files and the number of remaining files.";
				additionalText += "\n--> The relation must be: nuOfFilesCreated = nuOfRemainingFiles + nuOfDeletedFiles";
				additionalText += "\n--> nuOfFilesCreated: '" + String.valueOf(nuOfFilesCreated) + "'";
				additionalText += "\n--> nuOfDeletedFiles: '" + String.valueOf(nuOfDeletedFiles) + "'";
				additionalText += "\n--> nuOfRemainingFiles: '" + String.valueOf(nuOfRemainingFiles) + "'";
				TestManager.assertEquals(this.getContext(), this, additionalText, nuOfRemainingFiles + nuOfDeletedFiles, nuOfFilesCreated);

				for (String filePath : fileRemainingList)
				{
					TestManager.assertNotNull(this.getContext(), this, null, filePath);

					File file = new File(filePath);
					TestManager.assertNotNull(this.getContext(), this, null, file);

					Date fileDate = new Date(file.lastModified());
					TestManager.assertNotNull(this.getContext(), this, null, fileDate);

					boolean resultBoolean = fileDate.after(daysToKeepDate);
					TestManager.assertTrue(this.getContext(), this, null, resultBoolean);
				}
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Obsolete 'regular' files
	 */
	private void doExpiredObsoleteFiles(String group, String name)
	{
		try
		{
			/*
			 * Prepare
			 */

			// Initialize variables
			ResourceContainerMedia mediaResource = ResourceManager.media(this.getContext(), group, name);

			int daysToKeep = this.getContext().getMediaManager().getCleanObsoleteDaysToKeep();
			int dayRange = daysToKeep * 2;

			Date currentDate = new Date();
			Date daysToKeepDate = FileUtilFunctions.generalAddTimeDiff(currentDate, -daysToKeep, 0, 0, 0);

			// Get file directory of images
			ResourceContainer configuration = ResourceManager.configuration(this.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = this.getContext().getConfigurationManager().getProperty(this.getContext(), configuration, true);

			String additionalText = "--> Tried to read the directory for the media files to process during test";
			additionalText += "\n--> Please set the test configuration parameter '" + configuration.getRecourceIdentifier() + "' for the application '" + this.getContext().getCodeName() + "'";
			TestManager.assertNotNull(this.getContext(), this, additionalText, uploadFilePath);

			// Get file List
			List<String> fileList = FileUtilFunctions.directorySearchForFiles(uploadFilePath, "*.jpg");
			additionalText = "--> Tried to read media files in directory '" + uploadFilePath + "'";

			// No file list
			if (fileList == null)
			{
				TestManager.assertNotNull(this.getContext(), this, additionalText, fileList);
				return;
			}

			if (fileList.size() == 0)
			{
				TestManager.assertGreaterThan(this.getContext(), this, additionalText, fileList.size(), 0);
				return;
			}

			/*
			 * Upload a bunch of media files of one data identifier
			 */
			int nuOfFilesToUpload = 20;
			int minuteCounter = 0;
			int secondCounter = 0;

			for (int i = 0; i < 2; i++)
			{
				String dataIdentifier = this.parameterDataIdentifierTestUpload;
				if (i == 1) dataIdentifier = this.parameterDataIdentifierTestObsolete;

				for (int j = 0; j < nuOfFilesToUpload; j++)
				{
					// Get media file to copy
					int index = FileUtilFunctions.generalGetRandomValue(0, fileList.size() - 1);
					TestManager.assertEqualsRange(this.getContext(), this, null, 0, fileList.size() - 1, index);

					String uploadFileName = fileList.get(index);
					TestManager.assertNotEmpty(this.getContext(), this, null, uploadFileName);

					String fileType = FileUtilFunctions.fileGetFileTypePart(uploadFileName);
					TestManager.assertNotEmpty(this.getContext(), this, null, fileType);

					// Upload the file
					this.doUploadFile(group, name, dataIdentifier, uploadFileName);

					// Set a 'modified date' in a range of x days
					int daySetting = FileUtilFunctions.generalGetRandomValue(0, dayRange);
					TestManager.assertEqualsRange(this.getContext(), this, null, 0, dayRange, daySetting);

					Date modifiedDate = FileUtilFunctions.generalAddTimeDiff(currentDate, -daySetting, -1, minuteCounter--, secondCounter--);
					TestManager.assertNotEmpty(this.getContext(), this, null, fileType);

					MediaContainer mediaContainer = new MediaContainer(this.getContext(), mediaResource, dataIdentifier);
					TestManager.assertNotNull(this.getContext(), this, additionalText, mediaContainer);

					if (mediaContainer != null)
					{
						// Bind media object
						boolean booleanResult = mediaContainer.bindMedia();
						TestManager.assertTrue(this.getContext(), this, additionalText + "\n--> Error on binding media file", booleanResult);

						// Get regular file name
						String uploadedFilePath = mediaContainer.getOriginalMediaFilePath();
						TestManager.assertNotEmpty(this.getContext(), this, null, uploadedFilePath);

						// Set modified date
						int resultInteger = FileUtilFunctions.fileSetLastModifiedRetry(uploadedFilePath, modifiedDate);
						TestManager.assertGreaterThan(this.getContext(), this, null, resultInteger, 0);

						// Release media file
						booleanResult = mediaContainer.releaseMedia();
						TestManager.assertTrue(this.getContext(), this, additionalText + "\n--> Error on releasing media file", booleanResult);
					}
				}
			}

			/*
			 * Validate
			 */

			// Count the real number of different files in the 'regular'
			// directory
			String regularMediaFilesDirectory = mediaResource.mediaFileGetRegularFilePath(this.getContext());
			List<String> fileRegularList = FileUtilFunctions.directorySearchForFiles(regularMediaFilesDirectory, "*");
			TestManager.assertNotNull(this.getContext(), this, null, fileRegularList);
			TestManager.assertGreaterThan(this.getContext(), this, null, fileRegularList.size(), 0);
			int nuOfFilesCreated = 0;
			if (fileRegularList != null) nuOfFilesCreated = fileRegularList.size();

			// Invoke regular clean function of the media manager
			TestManager.assertGreaterThan(this.getContext(), this, null, daysToKeep, 1);

			String remainingFilePath = mediaResource.mediaFileGetRegularFilePath(this.getContext());
			int nuOfDeletedFiles = this.getContext().getMediaManager().cleanRegularDirectory(this.getContext(), mediaResource, daysToKeep);
			TestManager.assertGreaterThan(this.getContext(), this, null, nuOfDeletedFiles, 0);
			TestManager.assertLowerThan(this.getContext(), this, null, nuOfDeletedFiles, nuOfFilesCreated);

			// Get list of all files remained
			List<String> fileRemainingList = FileUtilFunctions.directorySearchForFiles(remainingFilePath, "*");
			TestManager.assertNotNull(this.getContext(), this, null, fileRemainingList);

			// Go through the list and check 'modified date'
			if (fileRemainingList != null)
			{
				int nuOfRemainingFiles = fileRemainingList.size();
				TestManager.assertGreaterThan(this.getContext(), this, null, nuOfRemainingFiles, 0);

				additionalText = "--> Mismatch about the number of created files, the number of deleted files and the number of remaining files.";
				additionalText += "\n--> The relation must be: nuOfFilesCreated = nuOfRemainingFiles + nuOfDeletedFiles";
				additionalText += "\n--> nuOfFilesCreated: '" + String.valueOf(nuOfFilesCreated) + "'";
				additionalText += "\n--> nuOfDeletedFiles: '" + String.valueOf(nuOfDeletedFiles) + "'";
				additionalText += "\n--> nuOfRemainingFiles: '" + String.valueOf(nuOfRemainingFiles) + "'";
				TestManager.assertEquals(this.getContext(), this, additionalText, nuOfRemainingFiles + nuOfDeletedFiles, nuOfFilesCreated);

				for (String filePath : fileRemainingList)
				{
					TestManager.assertNotNull(this.getContext(), this, null, filePath);

					File file = new File(filePath);
					TestManager.assertNotNull(this.getContext(), this, null, file);

					Date fileDate = new Date(file.lastModified());
					TestManager.assertNotNull(this.getContext(), this, null, fileDate);

					boolean resultBoolean = fileDate.after(daysToKeepDate);
					TestManager.assertTrue(this.getContext(), this, null, resultBoolean);
				}
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Clean all media directories
	 */
	private void doCleaningAll()
	{
		List<ResourceContainerMedia> mediaResources = new ArrayList<ResourceContainerMedia>();

		try
		{
			/*
			 * Prepare
			 */

			// Get all resource identifiers
			String typeCriteria[] = { "Media" };
			String applicationCriteria[] = { this.getContext().getApplicationName() };
			String originCriteria[] = null;
			String usageCriteria[] = null;
			String groupCriteria[] = { "Apartment" };
			List<String> mediaResourceIdentifiers = this.getContext().getResourceManager().getResourceIdentifierList(this.getContext(), typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

			// Create a list of media resources
			for (String identifier : mediaResourceIdentifiers)
			{
				TestManager.assertNotNull(this.getContext(), this, null, identifier);
				if (identifier == null || identifier.length() == 0) continue;

				ResourceContainer mediaContainerProvisional = new ResourceContainer(identifier);
				TestManager.assertNotNull(this.getContext(), this, null, mediaContainerProvisional);

				ResourceContainerMedia mediaContainer = ResourceManager.media(this.getContext(), mediaContainerProvisional.getGroup(), mediaContainerProvisional.getName());
				TestManager.assertNotNull(this.getContext(), this, null, mediaContainer);
				if (mediaContainer == null) continue;

				mediaResources.add(mediaContainer);
			}

			// Initialize day range
			int daysToKeep = this.getContext().getMediaManager().getCleanObsoleteDaysToKeep();
			int dayRange = daysToKeep * 2;

			Date currentDate = new Date();

			// Get file directory of images
			ResourceContainer configuration = ResourceManager.configuration(this.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = this.getContext().getConfigurationManager().getProperty(this.getContext(), configuration, true);

			String additionalText = "--> Tried to read the directory for the media files to process during test";
			additionalText += "\n--> Please set the test configuration parameter '" + configuration.getRecourceIdentifier() + "' for the application '" + this.getContext().getCodeName() + "'";
			TestManager.assertNotNull(this.getContext(), this, additionalText, uploadFilePath);

			// Get file List
			List<String> fileList = FileUtilFunctions.directorySearchForFiles(uploadFilePath, "*.jpg");
			additionalText = "--> Tried to read media files in directory '" + uploadFilePath + "'";

			// No file list
			if (fileList == null)
			{
				TestManager.assertNotNull(this.getContext(), this, additionalText, fileList);
				return;
			}

			if (fileList.size() == 0)
			{
				TestManager.assertGreaterThan(this.getContext(), this, additionalText, fileList.size(), 0);
				return;
			}

			/*
			 * Upload a bunch of media files of one data identifier
			 */
			int nuOfFilesToUpload = 10;
			int minuteCounter = 0;
			int secondCounter = 0;

			for (int k = 0; k < mediaResources.size(); k++)
			{
				ResourceContainerMedia mediaResource = mediaResources.get(k);

				for (int i = 0; i < 2; i++)
				{
					String dataIdentifier = this.parameterDataIdentifierTestUpload;
					if (i == 1) dataIdentifier = this.parameterDataIdentifierTestObsolete;

					for (int j = 0; j < nuOfFilesToUpload; j++)
					{
						// Get media file to copy
						int index = FileUtilFunctions.generalGetRandomValue(0, fileList.size() - 1);
						TestManager.assertEqualsRange(this.getContext(), this, null, 0, fileList.size() - 1, index);

						String uploadFileName = fileList.get(index);
						TestManager.assertNotEmpty(this.getContext(), this, null, uploadFileName);

						String fileType = FileUtilFunctions.fileGetFileTypePart(uploadFileName);
						TestManager.assertNotEmpty(this.getContext(), this, null, fileType);

						// Upload the file
						this.doUploadFile(mediaResource.getGroup(), mediaResource.getName(), dataIdentifier, uploadFileName);

						// Set a 'modified date' in a range of x days
						int daySetting = FileUtilFunctions.generalGetRandomValue(0, dayRange);
						TestManager.assertEqualsRange(this.getContext(), this, null, 0, dayRange, daySetting);

						Date modifiedDate = FileUtilFunctions.generalAddTimeDiff(currentDate, -daySetting, -1, minuteCounter--, secondCounter--);
						TestManager.assertNotEmpty(this.getContext(), this, null, fileType);

						MediaContainer mediaContainer = new MediaContainer(this.getContext(), mediaResource, dataIdentifier);
						TestManager.assertNotNull(this.getContext(), this, additionalText, mediaContainer);

						if (mediaContainer != null)
						{
							// Bind media object
							boolean booleanResult = mediaContainer.bindMedia();
							TestManager.assertTrue(this.getContext(), this, additionalText + "\n--> Error on binding media file", booleanResult);

							// Get regular file name
							String uploadedFilePath = mediaContainer.getOriginalMediaFilePath();
							TestManager.assertNotEmpty(this.getContext(), this, null, uploadedFilePath);

							// Set modified date
							int resultInteger = FileUtilFunctions.fileSetLastModifiedRetry(uploadedFilePath, modifiedDate);
							TestManager.assertGreaterThan(this.getContext(), this, null, resultInteger, 0);

							// Release media file
							booleanResult = mediaContainer.releaseMedia();
							TestManager.assertTrue(this.getContext(), this, additionalText + "\n--> Error on releasing media file", booleanResult);
						}
					}
				}
			}

			/*
			 * Validate
			 */

			// Count the real number of different files in the 'regular'
			// directory

			int nuOfFilesCreated = 0;

			for (int k = 0; k < mediaResources.size(); k++)
			{
				ResourceContainerMedia mediaResource = mediaResources.get(k);

				String regularMediaFilesDirectory = mediaResource.mediaFileGetRegularFilePath(this.getContext());
				List<String> fileRegularList = FileUtilFunctions.directorySearchForFiles(regularMediaFilesDirectory, "*");
				TestManager.assertNotNull(this.getContext(), this, null, fileRegularList);
				TestManager.assertGreaterThan(this.getContext(), this, null, fileRegularList.size(), 0);
				if (fileRegularList != null) nuOfFilesCreated = nuOfFilesCreated + fileRegularList.size();
			}

			TestManager.assertGreaterThan(this.getContext(), this, null, nuOfFilesCreated, 0);

			// Modify this parameter to ensure that no 'deleted' files were
			// removed in this test case.
			int currentCleanDeletedDaysToKeep = this.getContext().getMediaManager().getCleanDeletedDaysToKeep();
			this.getContext().getMediaManager().testSetCleanDeletedDaysToKeep(this.getContext(), daysToKeep * 3);

			// Invoke regular clean function of the media manager
			int nuOfDeletedFiles = this.getContext().getMediaManager().cleanAll(this.getContext());

			TestManager.assertGreaterThan(this.getContext(), this, null, nuOfDeletedFiles, 0);
			TestManager.assertLowerThan(this.getContext(), this, null, nuOfDeletedFiles, nuOfFilesCreated);

			TestManager.assertGreaterThan(this.getContext(), this, null, nuOfDeletedFiles, 0);
			TestManager.assertLowerThan(this.getContext(), this, null, nuOfDeletedFiles, nuOfFilesCreated);

			// Undo modifying this parameter to ensure that no 'deleted' files
			// were removed in this test case.
			this.getContext().getMediaManager().testSetCleanDeletedDaysToKeep(this.getContext(), currentCleanDeletedDaysToKeep);

			// Count the real number of remaining files in the 'regular'
			// directories

			int nuOfRemainingFiles = 0;

			for (int k = 0; k < mediaResources.size(); k++)
			{
				ResourceContainerMedia mediaResource = mediaResources.get(k);

				String regularMediaFilesDirectory = mediaResource.mediaFileGetRegularFilePath(this.getContext());
				List<String> fileRegularList = FileUtilFunctions.directorySearchForFiles(regularMediaFilesDirectory, "*");
				TestManager.assertNotNull(this.getContext(), this, null, fileRegularList);
				TestManager.assertGreaterThan(this.getContext(), this, null, fileRegularList.size(), 0);
				if (fileRegularList != null) nuOfRemainingFiles = nuOfRemainingFiles + fileRegularList.size();
			}

			TestManager.assertGreaterThan(this.getContext(), this, null, nuOfRemainingFiles, 0);
			TestManager.assertLowerThan(this.getContext(), this, null, nuOfRemainingFiles, nuOfFilesCreated);

			// Check result
			additionalText = "--> Mismatch about the number of created files, the number of deleted files and the number of remaining files.";
			additionalText += "\n--> The relation must be: nuOfFilesCreated = nuOfRemainingFiles + nuOfDeletedFiles";
			additionalText += "\n--> nuOfFilesCreated: '" + String.valueOf(nuOfFilesCreated) + "'";
			additionalText += "\n--> nuOfDeletedFiles: '" + String.valueOf(nuOfDeletedFiles) + "'";
			additionalText += "\n--> nuOfRemainingFiles: '" + String.valueOf(nuOfRemainingFiles) + "'";
			TestManager.assertEquals(this.getContext(), this, additionalText, nuOfRemainingFiles + nuOfDeletedFiles, nuOfFilesCreated);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Maximum Media Size
	 */
	private void doMaximumMediaSizeConfiguration(List<String> fileList, String group, String name, int maximumConfigurationMediaSize)
	{
		TestManager.servicePrintSubLine(this.getContext(), "Check configuration setting: 'MaximumMediaSize' (" + String.valueOf(maximumConfigurationMediaSize) + " Kilobytes)");

		try
		{
			// Prepare resource item
			ResourceContainerMedia media = ResourceManager.media(this.getContext(), group, name);
			Integer settingOfAttributeMediaSize = media.attributeGetMaximumMediaSize(this.getContext());
			TestManager.assertNotNull(this.getContext(), this, null, settingOfAttributeMediaSize);
			if (settingOfAttributeMediaSize != null) TestManager.assertGreaterThan(this.getContext(), this, null, settingOfAttributeMediaSize, maximumConfigurationMediaSize);

			// Go through all media files of the list
			if (settingOfAttributeMediaSize != null)
			{
				for (int i = 0; i < fileList.size(); i++)
				{
					Long currentMediaFileSize = FileUtilFunctions.fileGetFileSize(fileList.get(i));
					Long maximumMediaFileSize = (maximumConfigurationMediaSize * 1024L);

					// Provoke error
					if (currentMediaFileSize > maximumMediaFileSize)
					{
						String errorIdentifier = ResourceManager.notification(this.getContext(), "Media", "MaximumMediaSizeExceeded").getRecourceIdentifier();
						TestManager.errorSuppressErrorMessageOnce(this.getContext(), errorIdentifier);
						boolean booleanResult = this.getContext().getMediaManager().operationStoreLocal(this.getContext(), media, fileList.get(i), name);
						TestManager.assertErrorCode(this.getContext(), this, null, errorIdentifier);
						TestManager.assertFalse(this.getContext(), this, null, booleanResult);
					}
					// Upload regularly
					else
					{
						boolean booleanResult = this.getContext().getMediaManager().operationStoreLocal(this.getContext(), media, fileList.get(i), name);
						TestManager.assertTrue(this.getContext(), this, null, booleanResult);
					}
				}
			}
			
			// Clean directories on server side
			this.doRemoveAllFilesInRegularDirectory(group, name);
			this.doRemoveAllFilesInPendingDirectory(group, name);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Maximum Media Size
	 */
	private void doMaximumMediaSizeAttribute(List<String> fileList, String group, String name, int maximumAttributeMediaSize)
	{
		TestManager.servicePrintSubLine(this.getContext(), "Check media attribute: '" + group + "/" + name + "'" + " (" + String.valueOf(maximumAttributeMediaSize) + " Kilobytes)");

		try
		{
			// Prepare resource item
			ResourceContainerMedia media = ResourceManager.media(this.getContext(), group, name);
			Integer settingOfAttributeMediaSize = media.attributeGetMaximumMediaSize(this.getContext());
			TestManager.assertNotNull(this.getContext(), this, null, settingOfAttributeMediaSize);
			if (settingOfAttributeMediaSize != null) TestManager.assertEquals(this.getContext(), this, null, settingOfAttributeMediaSize, maximumAttributeMediaSize);

			// Go through all media files of the list
			if (settingOfAttributeMediaSize != null)
			{
				for (int i = 0; i < fileList.size(); i++)
				{
					Long currentMediaFileSize = FileUtilFunctions.fileGetFileSize(fileList.get(i));
					Long maximumMediaFileSize = (settingOfAttributeMediaSize * 1024L);

					// Provoke error
					if (currentMediaFileSize > maximumMediaFileSize)
					{
						String errorIdentifier = ResourceManager.notification(this.getContext(), "Media", "MaximumMediaSizeExceeded").getRecourceIdentifier();
						TestManager.errorSuppressErrorMessageOnce(this.getContext(), errorIdentifier);
						boolean booleanResult = this.getContext().getMediaManager().operationStoreLocal(this.getContext(), media, fileList.get(i), name);
						TestManager.assertErrorCode(this.getContext(), this, null, errorIdentifier);
						TestManager.assertFalse(this.getContext(), this, null, booleanResult);
					}
					// Upload regularly
					else
					{
						boolean booleanResult = this.getContext().getMediaManager().operationStoreLocal(this.getContext(), media, fileList.get(i), name);
						TestManager.assertTrue(this.getContext(), this, null, booleanResult);
					}
				}
			}
			
			// Clean directories on server side
			this.doRemoveAllFilesInRegularDirectory(group, name);
			this.doRemoveAllFilesInPendingDirectory(group, name);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
	public void setParameterTestCycleNumberOfFiles(int parameterTestCycleNumberOfFiles)
	{
		this.parameterTestCycleNumberOfFiles = parameterTestCycleNumberOfFiles;
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
	 * Setter
	 */
	public void setParameterNumberOfMediaToBeUploaded(int parameterNumberOfMediaToBeUploaded)
	{
		this.parameterNumberOfMediaToBeUploaded = parameterNumberOfMediaToBeUploaded;
	}
}
