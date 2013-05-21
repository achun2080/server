package fmagic.test.container;

import java.util.List;

import fmagic.basic.context.Context;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.media.MediaContainer;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.application.ClientManager;
import fmagic.server.application.ServerManager;
import fmagic.test.application.TestManager;
import fmagic.test.runner.TestRunner;

/**
 * This class implements testing functionality regarding the
 * <TT>Media Manager</TT> using a media commands.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 29.04.2013 - Created
 * 
 */
public class TestContainerMediaCommand extends TestContainer
{
	// Organization
	private int parameterPlainNumberOfMediaToBeUploaded = 50;
	private String parameterPlainResourceGroup = "Factory";
	private String parameterPlainResourceName = "Doorway";
	private String parameterPlainDataIdentifierTestUpload = "9001";

	private int parameterCycleNumberOfFilesToBeUploaded = 200;
	private String parameterCycleResourceGroup = "Factory";
	private String parameterCycleResourceName = "Hall";
	private int parameterCycleDataIdentifierFrom = 9100;
	private int parameterCycleDataIdentifierToo = 9200;

	// Command properties
	private ClientManager parameterClient = null;
	private ServerManager parameterServer = null;

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
	public TestContainerMediaCommand(Context context, TestRunner testRunner,
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
	public TestContainerMediaCommand(TestRunner testRunner)
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
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
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
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
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
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Component Test: Setup environment
	 */
	private void setupComponentTestIntern()
	{
		// Setup
		try
		{
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
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
			this.testPlainFileFromClientToServer();
			this.testMaximumMediaSize();
			this.testCycleUploadMediaFile();

			// Cleanup
			this.cleanupComponentTestIntern();
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
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
			this.doRemoveAllFilesInRegularDirectoryOnServerSide(this.parameterPlainResourceGroup, this.parameterPlainResourceName);
			this.doRemoveAllFilesInPendingDirectoryOnServerSide(this.parameterPlainResourceGroup, this.parameterPlainResourceName);

			this.doRemoveAllFilesInRegularDirectoryOnServerSide(this.parameterCycleResourceGroup, this.parameterCycleResourceName);
			this.doRemoveAllFilesInPendingDirectoryOnServerSide(this.parameterCycleResourceGroup, this.parameterCycleResourceName);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
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
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Upload File
	 */
	public void testPlainFileFromClientToServer()
	{
		try
		{
			TestManager.servicePrintHeader(parameterClient.getContext(), "===> testPlainFileFromClientToServer()", null);

			// Create media resource
			ResourceContainerMedia mediaResource = ResourceManager.media(parameterClient.getContext(), this.parameterPlainResourceGroup, this.parameterPlainResourceName);

			// Get file directory
			ResourceContainer configuration = ResourceManager.configuration(parameterClient.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = parameterClient.getContext().getConfigurationManager().getProperty(parameterClient.getContext(), configuration, true);

			String additionalText = "--> Tried to read the directory for the media files to process during test";
			additionalText += "\n--> Please set the test configuration parameter '" + configuration.getRecourceIdentifier() + "' for the application '" + parameterClient.getContext().getCodeName() + "'";
			TestManager.assertNotNull(parameterClient.getContext(), this, additionalText, uploadFilePath);

			// Get file List
			List<String> fileList = FileUtilFunctions.directorySearchForFiles(uploadFilePath, "*.jpg");

			additionalText = "--> Tried to read media files in directory '" + uploadFilePath + "'";
			additionalText = "--> No appropriate files found in this directory, or directory doesn't exist";
			TestManager.assertNotNull(parameterClient.getContext(), this, additionalText, fileList);

			if (fileList == null) return;

			TestManager.assertGreaterThan(parameterClient.getContext(), this, additionalText, fileList.size(), 0);

			// Try some uploads
			for (int i = 0; i < this.parameterPlainNumberOfMediaToBeUploaded; i++)
			{
				// Get random index of file item in list
				int index = FileUtilFunctions.generalGetRandomValue(0, fileList.size() - 1);

				// Process files only that matches the maximum media size
				String fileToBeUploaded = fileList.get(index);
				if (FileUtilFunctions.fileGetFileSize(fileToBeUploaded) > (mediaResource.attributeGetMaximumMediaSize(parameterClient.getContext()) * 1024L)) continue;
				if (FileUtilFunctions.fileGetFileSize(fileToBeUploaded) > (parameterClient.getContext().getClientMediaManagerTest().getMaximumMediaSize() * 1024L)) continue;

				// Push file
				this.doPushFileFromClientToServer(this.parameterPlainResourceGroup, this.parameterPlainResourceName, this.parameterPlainDataIdentifierTestUpload, fileToBeUploaded);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Maximum Media Size
	 */
	public void testMaximumMediaSize()
	{
		// Do nothing if the test is running in concurrent mode
		if (this.isConcurrentAccess()) return;

		TestManager.servicePrintHeader(parameterClient.getContext(), "===> testMaximumMediaSize()", null);

		try
		{
			/*
			 * Get file directory
			 */
			ResourceContainer configuration = ResourceManager.configuration(parameterClient.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = parameterClient.getContext().getConfigurationManager().getProperty(parameterClient.getContext(), configuration, true);

			String additionalText = "--> Tried to read the directory for the media files to process during test";
			additionalText += "\n--> Please set the test configuration parameter '" + configuration.getRecourceIdentifier() + "' for the application '" + parameterClient.getContext().getCodeName() + "'";
			TestManager.assertNotNull(parameterClient.getContext(), this, additionalText, uploadFilePath);

			/*
			 * Get file List
			 */
			List<String> fileList = FileUtilFunctions.directorySearchForFiles(uploadFilePath, "*.jpg");

			additionalText = "--> Tried to read media files in directory '" + uploadFilePath + "'";
			TestManager.assertNotNull(parameterClient.getContext(), this, additionalText, fileList);
			if (fileList != null) TestManager.assertGreaterThan(parameterClient.getContext(), this, additionalText, fileList.size(), 0);

			if (fileList == null) return;
			if (fileList.size() == 0) return;

			/*
			 * Get current configuration property
			 */
			Integer lastValueOfMaximumMediaSize = parameterClient.getContext().getClientMediaManagerTest().getMaximumMediaSize();
			if (lastValueOfMaximumMediaSize == null) lastValueOfMaximumMediaSize = 0;

			/*
			 * Check a couple of media resource settings
			 */
			parameterClient.getContext().getClientMediaManagerTest().setMaximumMediaSize(parameterClient.getContext(), 5000);
			this.doMaximumMediaSizeAttribute(fileList, "Test", "Size50", 50);
			this.doMaximumMediaSizeAttribute(fileList, "Test", "Size500", 500);
			this.doMaximumMediaSizeAttribute(fileList, "Test", "Size3000", 3000);

			/*
			 * Check the configuration setting
			 */
			parameterClient.getContext().getClientMediaManagerTest().setMaximumMediaSize(parameterClient.getContext(), 200);
			this.doMaximumMediaSizeConfiguration(fileList, "Test", "Size3000", 200);

			parameterClient.getContext().getClientMediaManagerTest().setMaximumMediaSize(parameterClient.getContext(), 700);
			this.doMaximumMediaSizeConfiguration(fileList, "Test", "Size3000", 700);

			parameterClient.getContext().getClientMediaManagerTest().setMaximumMediaSize(parameterClient.getContext(), 1400);
			this.doMaximumMediaSizeConfiguration(fileList, "Test", "Size3000", 1400);

			/*
			 * Reset current configuration property
			 */
			parameterClient.getContext().getClientMediaManagerTest().setMaximumMediaSize(parameterClient.getContext(), lastValueOfMaximumMediaSize);

		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Cycle upload media files
	 */
	public void testCycleUploadMediaFile()
	{
		try
		{
			TestManager.servicePrintHeader(parameterClient.getContext(), "===> testCycleUploadMediaFile()", null);

			// Create media resource
			ResourceContainerMedia mediaResource = ResourceManager.media(parameterClient.getContext(), this.parameterCycleResourceGroup, this.parameterCycleResourceName);

			// Get file directory
			ResourceContainer configuration = ResourceManager.configuration(parameterClient.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = parameterClient.getContext().getConfigurationManager().getProperty(parameterClient.getContext(), configuration, true);

			String additionalText = "--> Tried to read the directory for the media files to process during test";
			additionalText += "\n--> Please set the test configuration parameter '" + configuration.getRecourceIdentifier() + "' for the application '" + parameterClient.getContext().getCodeName() + "'";
			TestManager.assertNotNull(parameterClient.getContext(), this, additionalText, uploadFilePath);

			// Get file List
			List<String> fileList = FileUtilFunctions.directorySearchForFiles(uploadFilePath, "*.jpg");

			additionalText = "--> Tried to read media files in directory '" + uploadFilePath + "'";
			additionalText = "--> No appropriate files found in this directory, or directory doesn't exist";
			TestManager.assertNotNull(parameterClient.getContext(), this, additionalText, fileList);

			if (fileList == null) return;

			TestManager.assertGreaterThan(parameterClient.getContext(), this, additionalText, fileList.size(), 0);

			// Try some uploads
			for (int i = 0; i < this.parameterCycleNumberOfFilesToBeUploaded; i++)
			{
				// Get random index of file item in list
				int index = FileUtilFunctions.generalGetRandomValue(0, fileList.size() - 1);

				// Get random data identifier
				int dataIdentifierInteger = FileUtilFunctions.generalGetRandomValue(this.parameterCycleDataIdentifierFrom, this.parameterCycleDataIdentifierToo);
				String dataIdentifierString = String.valueOf(dataIdentifierInteger);
				String fileToBeUploaded = fileList.get(index);

				// Process files only that matches the maximum media size
				if (FileUtilFunctions.fileGetFileSize(fileToBeUploaded) > (mediaResource.attributeGetMaximumMediaSize(parameterClient.getContext()) * 1024L)) continue;
				if (FileUtilFunctions.fileGetFileSize(fileToBeUploaded) > (parameterClient.getContext().getClientMediaManagerTest().getMaximumMediaSize() * 1024L)) continue;

				// Push file
				this.doPushFileFromClientToServer(this.parameterCycleResourceGroup, this.parameterCycleResourceName, dataIdentifierString, fileToBeUploaded);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Maximum Media Size
	 */
	private void doMaximumMediaSizeConfiguration(List<String> fileList, String group, String name, int maximumConfigurationMediaSize)
	{
		TestManager.servicePrintSubLine(parameterClient.getContext(), "Check configuration setting: 'MaximumMediaSize' (" + String.valueOf(maximumConfigurationMediaSize) + " Kilobytes)");

		try
		{
			// Prepare resource item
			ResourceContainerMedia mediaResource = ResourceManager.media(parameterClient.getContext(), group, name);
			Integer settingOfAttributeMediaSize = mediaResource.attributeGetMaximumMediaSize(parameterClient.getContext());
			TestManager.assertNotNull(parameterClient.getContext(), this, null, settingOfAttributeMediaSize);
			if (settingOfAttributeMediaSize != null) TestManager.assertGreaterThan(parameterClient.getContext(), this, null, settingOfAttributeMediaSize, maximumConfigurationMediaSize);

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
						String errorIdentifier = ResourceManager.notification(parameterClient.getContext(), "Media", "MaximumMediaSizeExceeded").getRecourceIdentifier();
						TestManager.errorSuppressErrorMessageOnce(parameterClient.getContext(), errorIdentifier);
						boolean resultBoolean = parameterClient.getContext().getClientMediaManagerTest().commandUploadToServer(parameterClient.getContext(), parameterClient.getConnectionContainer(), mediaResource, fileList.get(i), name);
						TestManager.assertRuntimeErrorCode(parameterClient.getContext(), this, null, errorIdentifier);
						TestManager.assertFalse(parameterClient.getContext(), this, null, resultBoolean);
					}
					// Upload regularly
					else
					{
						boolean resultBoolean = parameterClient.getContext().getClientMediaManagerTest().commandUploadToServer(parameterClient.getContext(), parameterClient.getConnectionContainer(), mediaResource, fileList.get(i), name);
						TestManager.assertTrue(parameterClient.getContext(), this, null, resultBoolean);
					}
				}
			}

			// Clean directories on server side
			this.doRemoveAllFilesInRegularDirectoryOnServerSide(group, name);
			this.doRemoveAllFilesInPendingDirectoryOnServerSide(group, name);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Maximum Media Size
	 */
	private void doMaximumMediaSizeAttribute(List<String> fileList, String group, String name, int maximumAttributeMediaSize)
	{
		TestManager.servicePrintSubLine(parameterClient.getContext(), "Check media attribute: '" + group + "/" + name + "'" + " (" + String.valueOf(maximumAttributeMediaSize) + " Kilobytes)");

		try
		{
			// Prepare resource item
			ResourceContainerMedia mediaResource = ResourceManager.media(parameterClient.getContext(), group, name);
			Integer settingOfAttributeMediaSize = mediaResource.attributeGetMaximumMediaSize(parameterClient.getContext());
			TestManager.assertNotNull(parameterClient.getContext(), this, null, settingOfAttributeMediaSize);
			if (settingOfAttributeMediaSize != null) TestManager.assertEquals(parameterClient.getContext(), this, null, settingOfAttributeMediaSize, maximumAttributeMediaSize);

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
						String errorIdentifier = ResourceManager.notification(parameterClient.getContext(), "Media", "MaximumMediaSizeExceeded").getRecourceIdentifier();
						TestManager.errorSuppressErrorMessageOnce(parameterClient.getContext(), errorIdentifier);
						boolean resultBoolean = parameterClient.getContext().getClientMediaManagerTest().commandUploadToServer(parameterClient.getContext(), parameterClient.getConnectionContainer(), mediaResource, fileList.get(i), name);
						TestManager.assertRuntimeErrorCode(parameterClient.getContext(), this, null, errorIdentifier);
						TestManager.assertFalse(parameterClient.getContext(), this, null, resultBoolean);
					}
					// Upload regularly
					else
					{
						boolean resultBoolean = parameterClient.getContext().getClientMediaManagerTest().commandUploadToServer(parameterClient.getContext(), parameterClient.getConnectionContainer(), mediaResource, fileList.get(i), name);
						TestManager.assertTrue(parameterClient.getContext(), this, null, resultBoolean);
					}
				}
			}

			// Clean directories on server side
			this.doRemoveAllFilesInRegularDirectoryOnServerSide(group, name);
			this.doRemoveAllFilesInPendingDirectoryOnServerSide(group, name);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Util: Remove all files from the 'pending' directory, regarding a specific
	 * media resource container, on server side (remote side).
	 */
	private void doRemoveAllFilesInPendingDirectoryOnServerSide(String group, String name)
	{
		try
		{
			Context serverContext = this.parameterServer.getContext();

			ResourceContainerMedia mediaResource = ResourceManager.media(serverContext, group, name);
			String pendingDirectory = mediaResource.mediaFileGetPendingFilePath(serverContext);
			TestManager.assertNotNull(parameterClient.getContext(), this, null, pendingDirectory);

			if (FileUtilFunctions.directoryExists(pendingDirectory))
			{
				boolean isSuccessful = FileUtilFunctions.directoryDeleteAllFiles(pendingDirectory);
				TestManager.assertTrue(parameterClient.getContext(), this, "--> Error on cleaning 'pending' directory on SERVER side '" + pendingDirectory + "'", isSuccessful);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Util: Remove all files from the 'regular' directory, regarding a specific
	 * media resource container, on server side (remote side).
	 */
	private void doRemoveAllFilesInRegularDirectoryOnServerSide(String group, String name)
	{
		try
		{
			Context serverContext = this.parameterServer.getContext();

			ResourceContainerMedia mediaResource = ResourceManager.media(serverContext, group, name);
			String regularDirectory = mediaResource.mediaFileGetRegularFilePath(serverContext);
			TestManager.assertNotNull(parameterClient.getContext(), this, null, regularDirectory);

			if (FileUtilFunctions.directoryExists(regularDirectory))
			{
				boolean isSuccessful = FileUtilFunctions.directoryDeleteAllFiles(regularDirectory);
				TestManager.assertTrue(parameterClient.getContext(), this, "--> Error on cleaning 'regular' directory on SERVER side '" + regularDirectory + "'", isSuccessful);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Upload File
	 */
	private void doPushFileFromClientToServer(String resourceGroup, String resourceName, String dataIdentifierString, String uploadFileName)
	{
		try
		{
			// Create resource container
			ResourceContainerMedia mediaResource = ResourceManager.media(parameterClient.getContext(), resourceGroup, resourceName);

			// Get some information of the file to be uploaded
			String fileType = FileUtilFunctions.fileGetFileTypePart(uploadFileName);
			String hashValue = FileUtilFunctions.fileGetHashValue(uploadFileName);

			/*
			 * Upload the media file to a server
			 */
			String additionalText = "--> Upload media file to server";
			additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
			additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";

			boolean resultBoolean = parameterClient.getContext().getClientMediaManagerTest().commandUploadToServer(parameterClient.getContext(), parameterClient.getConnectionContainer(), mediaResource, uploadFileName, dataIdentifierString);
			TestManager.assertTrue(parameterClient.getContext(), this, additionalText, resultBoolean);

			if (resultBoolean == false) return;

			/*
			 * Check if exactly the same file (with the same hash value) really exists on server
			 */
			if (!this.isConcurrentAccess())
			{
				additionalText = "--> Check if exactly the same media file (with the same hash value) exists on server";
				additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
				additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
				additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";

				resultBoolean = parameterClient.getContext().getClientMediaManagerTest().commandCheckOnServer(parameterClient.getContext(), parameterClient.getConnectionContainer(), mediaResource, fileType, dataIdentifierString, hashValue);
				TestManager.assertTrue(parameterClient.getContext(), this, additionalText, resultBoolean);

				if (resultBoolean == false) return;
			}

			/*
			 * Read the media file from server and store it in the local media
			 * repository
			 */
			additionalText = "--> Media file couldn't be read from server";
			additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
			additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";

			String pendingFileName = parameterClient.getContext().getClientMediaManagerTest().commandReadOnServer(parameterClient.getContext(), parameterClient.getConnectionContainer(), mediaResource, dataIdentifierString);
			TestManager.assertNotNull(parameterClient.getContext(), this, additionalText, pendingFileName);

			// Return after error
			if (pendingFileName == null) return;
			
			// Store media file in local media repository
			boolean booleanResult = parameterClient.getContext().getClientMediaManagerTest().localStoreMediaFile(parameterClient.getContext(), mediaResource, pendingFileName, dataIdentifierString);
			TestManager.assertTrue(parameterClient.getContext(), this, additionalText + "\n--> Error on storing file to local media repository", booleanResult);
			
			// Delete pending file
			booleanResult = FileUtilFunctions.fileDelete(pendingFileName);
			TestManager.assertTrue(parameterClient.getContext(), this, additionalText + "\n--> Error on deleting pending media file", booleanResult);
			
			/*
			 * Check if file content can be read
			 */
			additionalText = "--> Tried to read file content of an uploaded file";
			additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
			additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier? '" + dataIdentifierString + "'";

			MediaContainer mediaContainer = new MediaContainer(parameterClient.getContext(), mediaResource, dataIdentifierString);
			TestManager.assertNotNull(parameterClient.getContext(), this, additionalText, mediaContainer);

			// Bind media object
			booleanResult = mediaContainer.bindMedia();
			TestManager.assertTrue(parameterClient.getContext(), this, additionalText + "\n--> Error on binding media file", booleanResult);

			// Compare check sum of source file and destination file
			if (!this.isConcurrentAccess())
			{
				// Check always: If the checksum of the uploaded file and
				// the working file are the same, all the same if the media
				// files are encoded.
				TestManager.assertEqualsFile(parameterClient.getContext(), this, additionalText, uploadFileName, mediaContainer.getWorkingMediaFilePath());

				// Check if encoding is enabled: If the checksum of
				// the uploaded file and the original file are different.
				if (parameterClient.getContext().getMediaManager().isEncodingEnabled(parameterClient.getContext(), mediaResource) == true)
				{
					TestManager.assertNotEqualsFile(parameterClient.getContext(), this, additionalText, uploadFileName, mediaContainer.getOriginalMediaFilePath());
				}
			}

			// Read file content
			byte[] contentAsByteBuffer = mediaContainer.readMediaContentAsByteArray();
			TestManager.assertNotNull(parameterClient.getContext(), this, additionalText + "\n--> Error on reading media file content", contentAsByteBuffer);
			TestManager.assertGreaterThan(parameterClient.getContext(), this, additionalText, contentAsByteBuffer.length, 0);

			// Release media file
			booleanResult = mediaContainer.releaseMedia();
			TestManager.assertTrue(parameterClient.getContext(), this, additionalText + "\n--> Error on releasing media file", booleanResult);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterClient.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Setter
	 */
	public void setParameterPlainResourceGroup(String parameterPlainResourceGroup)
	{
		this.parameterPlainResourceGroup = parameterPlainResourceGroup;
	}

	/**
	 * Setter
	 */
	public void setParameterPlainResourceName(String parameterPlainResourceName)
	{
		this.parameterPlainResourceName = parameterPlainResourceName;
	}

	/**
	 * Setter
	 */
	public void setParameterPlainDataIdentifierTestUpload(String parameterPlainDataIdentifierTestUpload)
	{
		this.parameterPlainDataIdentifierTestUpload = parameterPlainDataIdentifierTestUpload;
	}

	/**
	 * Setter
	 */
	public void setParameterPlainNumberOfMediaToBeUploaded(int parameterPlainNumberOfMediaToBeUploaded)
	{
		this.parameterPlainNumberOfMediaToBeUploaded = parameterPlainNumberOfMediaToBeUploaded;
	}

	/**
	 * Setter
	 */
	public void setParameterCycleNumberOfFilesToBeUploaded(int parameterCycleNumberOfFilesToBeUploaded)
	{
		this.parameterCycleNumberOfFilesToBeUploaded = parameterCycleNumberOfFilesToBeUploaded;
	}

	/**
	 * Setter
	 */
	public void setParameterCycleResourceGroup(String parameterCycleResourceGroup)
	{
		this.parameterCycleResourceGroup = parameterCycleResourceGroup;
	}

	/**
	 * Setter
	 */
	public void setParameterCycleResourceName(String parameterCycleResourceName)
	{
		this.parameterCycleResourceName = parameterCycleResourceName;
	}

	/**
	 * Setter
	 */
	public void setParameterCycleDataIdentifierFrom(int parameterCycleDataIdentifierFrom)
	{
		this.parameterCycleDataIdentifierFrom = parameterCycleDataIdentifierFrom;
	}

	/**
	 * Setter
	 */
	public void setParameterCycleDataIdentifierToo(int parameterCycleDataIdentifierToo)
	{
		this.parameterCycleDataIdentifierToo = parameterCycleDataIdentifierToo;
	}

	/**
	 * Setter
	 */
	public void setParameterClientServer(ClientManager parameterClient, ServerManager parameterServer)
	{
		this.parameterClient = parameterClient;
		this.parameterServer = parameterServer;
	}
}
