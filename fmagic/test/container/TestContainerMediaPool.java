package fmagic.test.container;

import java.util.List;

import fmagic.basic.context.Context;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.media.MediaContainer;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.command.ClientCommandMediaFileInfo;
import fmagic.server.application.ServerManager;
import fmagic.server.media.ServerMediaManager;
import fmagic.test.application.TestManager;
import fmagic.test.runner.TestRunner;

/**
 * This class implements testing functionality regarding the
 * <TT>Media Manager</TT> using a media pool.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 15.05.2013 - Created
 * 
 */
public class TestContainerMediaPool extends TestContainer
{
	// Organization
	private int parameterPlainNumberOfMediaToBeUploaded = 50;
	private int parameterPlainDataIdentifierTestUploadStartFrom = 3001;
	private String parameterPlainResourceGroup = "Factory";
	private String parameterPlainResourceName = "Doorway";

	// Executing application
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
	public TestContainerMediaPool(Context context, TestRunner testRunner,
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
	public TestContainerMediaPool(TestRunner testRunner)
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
		// Setup
		try
		{
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
			this.testPlainFileFromServerToMediaPool();

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
	 * Test: xxxxxxxxxx
	 */
	public void xxxxxxxxxx()
	{
		try
		{
			ServerMediaManager mediaManager = (ServerMediaManager) this.getContext().getMediaManager();

			System.out.println(mediaManager);

		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Upload File
	 */
	public void testPlainFileFromServerToMediaPool()
	{
		try
		{
			TestManager.servicePrintHeader(parameterServer.getContext(), "===> testPlainFileFromServerToMediaPool()", null);

			// Create media resource
			ResourceContainerMedia mediaResource = ResourceManager.media(parameterServer.getContext(), this.parameterPlainResourceGroup, this.parameterPlainResourceName);

			// Get file directory
			ResourceContainer configuration = ResourceManager.configuration(parameterServer.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = parameterServer.getContext().getConfigurationManager().getProperty(parameterServer.getContext(), configuration, true);

			String additionalText = "--> Tried to read the directory for the media files to process during test";
			additionalText += "\n--> Please set the test configuration parameter '" + configuration.getRecourceIdentifier() + "' for the application '" + parameterServer.getContext().getCodeName() + "'";
			TestManager.assertNotNull(parameterServer.getContext(), this, additionalText, uploadFilePath);

			// Get file List
			List<String> fileList = FileUtilFunctions.directorySearchForFiles(uploadFilePath, "*.jpg");

			additionalText = "--> Tried to read media files in directory '" + uploadFilePath + "'";
			additionalText = "--> No appropriate files found in this directory, or directory doesn't exist";
			TestManager.assertNotNull(parameterServer.getContext(), this, additionalText, fileList);

			if (fileList == null) return;

			TestManager.assertGreaterThan(parameterServer.getContext(), this, additionalText, fileList.size(), 0);

			int currentDataIdentifier = this.parameterPlainDataIdentifierTestUploadStartFrom;

			// Try some uploads
			while (true)
			{
				// Get random index of file item in list
				int index = FileUtilFunctions.generalGetRandomValue(0, fileList.size() - 1);

				// Process files only that matches the maximum media size
				String fileToBeUploaded = fileList.get(index);
				if (FileUtilFunctions.fileGetFileSize(fileToBeUploaded) > (mediaResource.attributeGetMaximumMediaSize(parameterServer.getContext()) * 1024L)) continue;
				if (FileUtilFunctions.fileGetFileSize(fileToBeUploaded) > (parameterServer.getContext().getServerMediaManagerTest().getMaximumMediaSize() * 1024L)) continue;

				// Push file
				this.doPushFileFromClientToServer(this.parameterPlainResourceGroup, this.parameterPlainResourceName, String.valueOf(currentDataIdentifier++), fileToBeUploaded);

				// End of cycle
				if (currentDataIdentifier > this.parameterPlainNumberOfMediaToBeUploaded) break;
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterServer.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Upload File
	 */
	private void doPushFileFromClientToServer(String resourceGroup, String resourceName, String dataIdentifierString, String uploadFileName)
	{
		try
		{
			boolean resultBoolean = false;

			// Create resource container
			ResourceContainerMedia mediaResource = ResourceManager.media(parameterServer.getContext(), resourceGroup, resourceName);

			// Get some information of the file to be uploaded
			String fileType = FileUtilFunctions.fileGetFileTypePart(uploadFileName);
			String hashValue = FileUtilFunctions.fileGetHashValue(uploadFileName);

			/*
			 * Upload the media file to the media pool
			 */

			String additionalText = "--> Upload media file to media pool";
			additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
			additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";

			resultBoolean = parameterServer.getContext().getServerMediaManagerTest().poolUploadMediaFileToPool(parameterServer.getContext(), mediaResource, uploadFileName, dataIdentifierString);
			TestManager.assertTrue(parameterServer.getContext(), this, additionalText, resultBoolean);

			if (resultBoolean == false) return;

			/*
			 * Check if exactly the same file (with the same hash value) really
			 * exists on the media pool
			 */
			if (!this.isConcurrentAccess())
			{
				additionalText = "--> Check if exactly the same media file (with the same hash value) exists on server";
				additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
				additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
				additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";

				// Try to check the media file for 30 seconds (Waiting for
				// processing the main queue)
				int counter = 30;

				while (counter-- >= 0)
				{
					FileUtilFunctions.generalSleepSeconds(1);

					resultBoolean = parameterServer.getContext().getServerMediaManagerTest().poolCheckMediaFileOnPool(parameterServer.getContext(), mediaResource, fileType, dataIdentifierString, hashValue);
					if (resultBoolean == true) break;
				}

				TestManager.assertTrue(parameterServer.getContext(), this, additionalText, resultBoolean);
				if (resultBoolean == false) return;
			}

			/*
			 * Get file information
			 */
			if (!this.isConcurrentAccess())
			{
				// Execute COMMAND on media pool
				additionalText = "--> Get media file information";
				additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
				additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
				additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";

				ClientCommandMediaFileInfo command = parameterServer.getContext().getServerMediaManagerTest().poolInfoMediaFileOnPool(parameterServer.getContext(), mediaResource, dataIdentifierString);
				TestManager.assertNotNull(parameterServer.getContext(), this, additionalText, command);

				// Return on error
				if (command == null) return;

				// Compare existing
				additionalText = "--> File does not exist";
				additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
				additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
				additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";
				TestManager.assertTrue(parameterServer.getContext(), this, additionalText, command.isExisting());

				if (command.isExisting())
				{
					// Compare file type
					additionalText = "--> File type missmatch";
					additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
					additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
					additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";
					TestManager.assertEquals(parameterServer.getContext(), this, additionalText, fileType, command.getFileType());

					// Compare hash value
					additionalText = "--> Hash value missmatch";
					additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
					additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
					additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";
					TestManager.assertEquals(parameterServer.getContext(), this, additionalText, hashValue, command.getHashValue());

					// Compare last modified date
					additionalText = "--> Last modified date not set";
					additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
					additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
					additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";
					TestManager.assertNotNull(parameterServer.getContext(), this, additionalText, command.getLastModifiedDate());

					// Compare file size
					additionalText = "--> File size missmatch";
					additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
					additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
					additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";

					if (command.isEncoded())
					{
						// If the file is encoded on server the file size is
						// modified to a value that is divisible by 8
						TestManager.assertEquals(parameterServer.getContext(), this, additionalText, (((FileUtilFunctions.fileGetFileSize(uploadFileName) / 8L) + 1L) * 8L), command.getFileSize());
					}
					else
					{
						TestManager.assertEquals(parameterServer.getContext(), this, additionalText, FileUtilFunctions.fileGetFileSize(uploadFileName), command.getFileSize());
					}
				}
			}

			/*
			 * Read the media file from the media pool and store it in the local
			 * media repository.
			 */
			additionalText = "--> Media file couldn't be read from media pool";
			additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
			additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";

			// Try to read the media file for 30 seconds (Waiting for
			// processing the main queue)
			int counter = 30;

			String pendingFileName = null;

			while (counter-- >= 0)
			{
				pendingFileName = parameterServer.getContext().getServerMediaManagerTest().poolReadMediaFileOnPool(parameterServer.getContext(), mediaResource, dataIdentifierString);
				if (pendingFileName != null) break;

				FileUtilFunctions.generalSleepSeconds(1);
			}

			TestManager.assertNotNull(parameterServer.getContext(), this, additionalText, pendingFileName);

			// Return after error
			if (pendingFileName == null) return;

			// Store media file in local media repository
			boolean booleanResult = parameterServer.getContext().getServerMediaManagerTest().localStoreMediaFile(parameterServer.getContext(), mediaResource, pendingFileName, dataIdentifierString);
			TestManager.assertTrue(parameterServer.getContext(), this, additionalText + "\n--> Error on storing file to local media repository", booleanResult);

			// Delete pending file
			booleanResult = FileUtilFunctions.fileDelete(pendingFileName);
			TestManager.assertTrue(parameterServer.getContext(), this, additionalText + "\n--> Error on deleting pending media file", booleanResult);

			/*
			 * Check if file content can be read
			 */
			additionalText = "--> Tried to read file content of an uploaded file";
			additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
			additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier? '" + dataIdentifierString + "'";

			MediaContainer mediaContainer = new MediaContainer(parameterServer.getContext(), mediaResource, dataIdentifierString);
			TestManager.assertNotNull(parameterServer.getContext(), this, additionalText, mediaContainer);

			// Bind media object
			booleanResult = mediaContainer.bindMedia();
			TestManager.assertTrue(parameterServer.getContext(), this, additionalText + "\n--> Error on binding media file", booleanResult);

			// Compare check sum of source file and destination file
			if (!this.isConcurrentAccess())
			{
				// Check always: If the checksum of the uploaded file and
				// the working file are the same, all the same if the media
				// files are encoded.
				TestManager.assertEqualsFile(parameterServer.getContext(), this, additionalText, uploadFileName, mediaContainer.getWorkingMediaFilePath());

				// Check if encoding is enabled: If the checksum of
				// the uploaded file and the original file are different.
				if (parameterServer.getContext().getMediaManager().isEncodingEnabled(parameterServer.getContext(), mediaResource) == true)
				{
					TestManager.assertNotEqualsFile(parameterServer.getContext(), this, additionalText, uploadFileName, mediaContainer.getOriginalMediaFilePath());
				}
			}

			// Read file content
			byte[] contentAsByteBuffer = mediaContainer.readMediaContentAsByteArray();
			TestManager.assertNotNull(parameterServer.getContext(), this, additionalText + "\n--> Error on reading media file content", contentAsByteBuffer);
			TestManager.assertGreaterThan(parameterServer.getContext(), this, additionalText, contentAsByteBuffer.length, 0);

			// Release media file
			booleanResult = mediaContainer.releaseMedia();
			TestManager.assertTrue(parameterServer.getContext(), this, additionalText + "\n--> Error on releasing media file", booleanResult);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterServer.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Read File
	 */
	public void functionReadFileFromMediaPool(String resourceGroup, String resourceName, int dataIdentifierFrom, int dataIdentifierToo)
	{
		try
		{
			// Create resource container
			ResourceContainerMedia mediaResource = ResourceManager.media(parameterServer.getContext(), resourceGroup, resourceName);

			/*
			 * Go through the data identifier range
			 */
			for (int i = dataIdentifierFrom; i <= dataIdentifierToo; i++)
			{
				String dataIdentifierString = String.valueOf(i);

				/*
				 * Execute INFO command on media pool
				 */
				String additionalText = "--> Get media file information";
				additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
				additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";

				ClientCommandMediaFileInfo command = parameterServer.getContext().getServerMediaManagerTest().poolInfoMediaFileOnPool(parameterServer.getContext(), mediaResource, dataIdentifierString);
				TestManager.assertNotNull(parameterServer.getContext(), this, additionalText, command);

				// Return on error
				if (command == null) continue;

				// Check if media file exists on media pool
				additionalText = "--> File does not exist";
				additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
				additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";
				TestManager.assertTrue(parameterServer.getContext(), this, additionalText, command.isExisting());

				/*
				 * Read media file from media pool
				 */

				additionalText = "--> Media file couldn't be read from media pool";
				additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
				additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";

				String pendingFileName = parameterServer.getContext().getServerMediaManagerTest().poolReadMediaFileOnPool(parameterServer.getContext(), mediaResource, dataIdentifierString);
				TestManager.assertNotNull(parameterServer.getContext(), this, additionalText, pendingFileName);

				if (pendingFileName == null) continue;

				/*
				 * Store media file local
				 */

				boolean booleanResult = parameterServer.getContext().getServerMediaManagerTest().localStoreMediaFile(parameterServer.getContext(), mediaResource, pendingFileName, dataIdentifierString);
				TestManager.assertTrue(parameterServer.getContext(), this, additionalText + "\n--> Error on storing file to local media repository", booleanResult);

				// Delete pending file
				booleanResult = FileUtilFunctions.fileDelete(pendingFileName);
				TestManager.assertTrue(parameterServer.getContext(), this, additionalText + "\n--> Error on deleting pending media file", booleanResult);

				// Check if file content can be read
				additionalText = "--> Tried to read file content of an uploaded file";
				additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
				additionalText += "\n--> Data identifier? '" + dataIdentifierString + "'";

				MediaContainer mediaContainer = new MediaContainer(parameterServer.getContext(), mediaResource, dataIdentifierString);
				TestManager.assertNotNull(parameterServer.getContext(), this, additionalText, mediaContainer);

				// Bind media object
				booleanResult = mediaContainer.bindMedia();
				TestManager.assertTrue(parameterServer.getContext(), this, additionalText + "\n--> Error on binding media file", booleanResult);

				// Read file content
				byte[] contentAsByteBuffer = mediaContainer.readMediaContentAsByteArray();
				TestManager.assertNotNull(parameterServer.getContext(), this, additionalText + "\n--> Error on reading media file content", contentAsByteBuffer);
				TestManager.assertGreaterThan(parameterServer.getContext(), this, additionalText, contentAsByteBuffer.length, 0);

				// Release media file
				booleanResult = mediaContainer.releaseMedia();
				TestManager.assertTrue(parameterServer.getContext(), this, additionalText + "\n--> Error on releasing media file", booleanResult);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(parameterServer.getContext(), this, "Unexpected Exception", e);
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
	public void setParameterPlainNumberOfMediaToBeUploaded(int parameterPlainNumberOfMediaToBeUploaded)
	{
		this.parameterPlainNumberOfMediaToBeUploaded = parameterPlainNumberOfMediaToBeUploaded;
	}

	/**
	 * Setter
	 */
	public void setParameterPlainDataIdentifierTestUploadStartFrom(int parameterPlainDataIdentifierTestUploadStartFrom)
	{
		this.parameterPlainDataIdentifierTestUploadStartFrom = parameterPlainDataIdentifierTestUploadStartFrom;
	}

	/**
	 * Setter
	 */
	public void setParameterServer(ServerManager parameterServer)
	{
		this.parameterServer = parameterServer;
	}
}
