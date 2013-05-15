package fmagic.test.container;

import java.util.List;

import fmagic.basic.command.ResponseContainer;
import fmagic.basic.context.Context;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.media.MediaContainer;
import fmagic.basic.media.ResourceContainerMedia;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.application.ClientManager;
import fmagic.client.command.ClientCommand;
import fmagic.client.command.ClientCommandCreateSession;
import fmagic.client.command.ClientCommandHandshake;
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
			this.doInitializeConnection();
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
			this.testPlainFileFromClientToServer();
			this.testMaximumMediaSize();
			this.testCycleUploadMediaFile();

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
			this.doRemoveAllFilesInRegularDirectoryOnServerSide(this.parameterPlainResourceGroup, this.parameterPlainResourceName);
			this.doRemoveAllFilesInPendingDirectoryOnServerSide(this.parameterPlainResourceGroup, this.parameterPlainResourceName);

			this.doRemoveAllFilesInRegularDirectoryOnServerSide(this.parameterCycleResourceGroup, this.parameterCycleResourceName);
			this.doRemoveAllFilesInPendingDirectoryOnServerSide(this.parameterCycleResourceGroup, this.parameterCycleResourceName);
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
	 * Test: Upload File
	 */
	public void testPlainFileFromClientToServer()
	{
		try
		{
			TestManager.servicePrintHeader(this.getContext(), "===> testPlainFileFromClientToServer()", null);

			// Create media resource
			ResourceContainerMedia mediaResource = ResourceManager.media(this.getContext(), this.parameterPlainResourceGroup, this.parameterPlainResourceName);

			// Get file directory
			ResourceContainer configuration = ResourceManager.configuration(this.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = this.getContext().getConfigurationManager().getProperty(this.getContext(), configuration, true);

			String additionalText = "--> Tried to read the directory for the media files to process during test";
			additionalText += "\n--> Please set the test configuration parameter '" + configuration.getRecourceIdentifier() + "' for the application '" + this.getContext().getCodeName() + "'";
			TestManager.assertNotNull(this.getContext(), this, additionalText, uploadFilePath);

			// Get file List
			List<String> fileList = FileUtilFunctions.directorySearchForFiles(uploadFilePath, "*.jpg");

			additionalText = "--> Tried to read media files in directory '" + uploadFilePath + "'";
			additionalText = "--> No appropriate files found in this directory, or directory doesn't exist";
			TestManager.assertNotNull(this.getContext(), this, additionalText, fileList);

			if (fileList == null) return;

			TestManager.assertGreaterThan(this.getContext(), this, additionalText, fileList.size(), 0);

			// Try some uploads
			for (int i = 0; i < this.parameterPlainNumberOfMediaToBeUploaded; i++)
			{
				// Get random index of file item in list
				int index = FileUtilFunctions.generalGetRandomValue(0, fileList.size() - 1);

				// Process files only that matches the maximum media size
				String fileToBeUploaded = fileList.get(index);
				if (FileUtilFunctions.fileGetFileSize(fileToBeUploaded) > (mediaResource.attributeGetMaximumMediaSize(this.getContext()) * 1024L)) continue;
				if (FileUtilFunctions.fileGetFileSize(fileToBeUploaded) > (this.getContext().getMediaManager().getMaximumMediaSize() * 1024L)) continue;

				// Push file
				this.doPushFileFromClientToServer(this.parameterPlainResourceGroup, this.parameterPlainResourceName, this.parameterPlainDataIdentifierTestUpload, fileToBeUploaded);
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
			 * Get current configuration property
			 */
			Integer lastValueOfMaximumMediaSize = this.getContext().getMediaManager().getMaximumMediaSize();
			if (lastValueOfMaximumMediaSize == null) lastValueOfMaximumMediaSize = 0;

			/*
			 * Check a couple of media resource settings
			 */
			this.getContext().getMediaManager().testSetMaximumMediaSize(this.getContext(), 5000);
			this.doMaximumMediaSizeAttribute(fileList, "Test", "Size50", 50);
			this.doMaximumMediaSizeAttribute(fileList, "Test", "Size500", 500);
			this.doMaximumMediaSizeAttribute(fileList, "Test", "Size3000", 3000);

			/*
			 * Check the configuration setting
			 */
			this.getContext().getMediaManager().testSetMaximumMediaSize(this.getContext(), 200);
			this.doMaximumMediaSizeConfiguration(fileList, "Test", "Size3000", 200);

			this.getContext().getMediaManager().testSetMaximumMediaSize(this.getContext(), 700);
			this.doMaximumMediaSizeConfiguration(fileList, "Test", "Size3000", 700);

			this.getContext().getMediaManager().testSetMaximumMediaSize(this.getContext(), 1400);
			this.doMaximumMediaSizeConfiguration(fileList, "Test", "Size3000", 1400);

			/*
			 * Reset current configuration property
			 */
			this.getContext().getMediaManager().testSetMaximumMediaSize(this.getContext(), lastValueOfMaximumMediaSize);

		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: Cycle upload media files
	 */
	public void testCycleUploadMediaFile()
	{
		try
		{
			TestManager.servicePrintHeader(this.getContext(), "===> testCycleUploadMediaFile()", null);

			// Create media resource
			ResourceContainerMedia mediaResource = ResourceManager.media(this.getContext(), this.parameterCycleResourceGroup, this.parameterCycleResourceName);

			// Get file directory
			ResourceContainer configuration = ResourceManager.configuration(this.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = this.getContext().getConfigurationManager().getProperty(this.getContext(), configuration, true);

			String additionalText = "--> Tried to read the directory for the media files to process during test";
			additionalText += "\n--> Please set the test configuration parameter '" + configuration.getRecourceIdentifier() + "' for the application '" + this.getContext().getCodeName() + "'";
			TestManager.assertNotNull(this.getContext(), this, additionalText, uploadFilePath);

			// Get file List
			List<String> fileList = FileUtilFunctions.directorySearchForFiles(uploadFilePath, "*.jpg");

			additionalText = "--> Tried to read media files in directory '" + uploadFilePath + "'";
			additionalText = "--> No appropriate files found in this directory, or directory doesn't exist";
			TestManager.assertNotNull(this.getContext(), this, additionalText, fileList);

			if (fileList == null) return;

			TestManager.assertGreaterThan(this.getContext(), this, additionalText, fileList.size(), 0);

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
				if (FileUtilFunctions.fileGetFileSize(fileToBeUploaded) > (mediaResource.attributeGetMaximumMediaSize(this.getContext()) * 1024L)) continue;
				if (FileUtilFunctions.fileGetFileSize(fileToBeUploaded) > (this.getContext().getMediaManager().getMaximumMediaSize() * 1024L)) continue;

				// Push file
				this.doPushFileFromClientToServer(this.parameterCycleResourceGroup, this.parameterCycleResourceName, dataIdentifierString, fileToBeUploaded);
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
						boolean resultBoolean = this.getContext().getMediaManager().commandUploadToServer(this.getContext(), media, fileList.get(i), name);
						TestManager.assertRuntimeErrorCode(this.getContext(), this, null, errorIdentifier);
						TestManager.assertFalse(this.getContext(), this, null, resultBoolean);
					}
					// Upload regularly
					else
					{
						boolean resultBoolean = this.getContext().getMediaManager().commandUploadToServer(this.getContext(), media, fileList.get(i), name);
						TestManager.assertTrue(this.getContext(), this, null, resultBoolean);
					}
				}
			}

			// Clean directories on server side
			this.doRemoveAllFilesInRegularDirectoryOnServerSide(group, name);
			this.doRemoveAllFilesInPendingDirectoryOnServerSide(group, name);
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
						boolean resultBoolean = this.getContext().getMediaManager().commandUploadToServer(this.getContext(), media, fileList.get(i), name);
						TestManager.assertRuntimeErrorCode(this.getContext(), this, null, errorIdentifier);
						TestManager.assertFalse(this.getContext(), this, null, resultBoolean);
					}
					// Upload regularly
					else
					{
						boolean resultBoolean = this.getContext().getMediaManager().commandUploadToServer(this.getContext(), media, fileList.get(i), name);
						TestManager.assertTrue(this.getContext(), this, null, resultBoolean);
					}
				}
			}

			// Clean directories on server side
			this.doRemoveAllFilesInRegularDirectoryOnServerSide(group, name);
			this.doRemoveAllFilesInPendingDirectoryOnServerSide(group, name);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			TestManager.assertNotNull(this.getContext(), this, null, pendingDirectory);

			if (FileUtilFunctions.directoryExists(pendingDirectory))
			{
				boolean isSuccessful = FileUtilFunctions.directoryDeleteAllFiles(pendingDirectory);
				TestManager.assertTrue(this.getContext(), this, "--> Error on cleaning 'pending' directory on SERVER side '" + pendingDirectory + "'", isSuccessful);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			TestManager.assertNotNull(this.getContext(), this, null, regularDirectory);

			if (FileUtilFunctions.directoryExists(regularDirectory))
			{
				boolean isSuccessful = FileUtilFunctions.directoryDeleteAllFiles(regularDirectory);
				TestManager.assertTrue(this.getContext(), this, "--> Error on cleaning 'regular' directory on SERVER side '" + regularDirectory + "'", isSuccessful);
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Start connection to the application server
	 */
	private void doInitializeConnection()
	{
		try
		{
			// General variables
			ClientCommand command;
			ResponseContainer responseContainer;

			// Delete old session
			this.doCleanClientSession();

			// COMMAND Create Session
			command = new ClientCommandCreateSession(parameterClient.getContext(), parameterClient);
			responseContainer = command.execute();

			String additionalText = "--> Tried to create a session on application server";
			TestManager.assertNotNull(this.getContext(), this, additionalText, responseContainer);

			if (responseContainer != null)
			{
				additionalText = "--> Tried to create a session on application server";
				additionalText += "\n--> Application server replied an error code" + "\n";
				additionalText += responseContainer.toString();
				TestManager.assertFalse(this.getContext(), this, additionalText, responseContainer.isError());
			}

			// COMMAND Handshake
			command = new ClientCommandHandshake(parameterClient.getContext(), parameterClient);
			responseContainer = command.execute();

			additionalText = "--> Tried to handshake the application server";
			TestManager.assertNotNull(this.getContext(), this, additionalText, responseContainer);

			if (responseContainer != null)
			{
				additionalText = "--> Tried to handshake the application server";
				additionalText += "\n--> Application server replied an error code" + "\n";
				additionalText += responseContainer.toString();
				TestManager.assertFalse(this.getContext(), this, additionalText, responseContainer.isError());
			}
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Clean client session on client local data file.
	 */
	private void doCleanClientSession()
	{
		try
		{
			this.parameterClient.getContext().getLocaldataManager().writeProperty(this.getContext(), ResourceManager.localdata(this.getContext(), "LastValidServerConnection", "ClientSessionIdentifier"), null);
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
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
			ResourceContainerMedia mediaResource = ResourceManager.media(this.getContext(), resourceGroup, resourceName);

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

			boolean resultBoolean = this.getContext().getMediaManager().commandUploadToServer(this.getContext(), mediaResource, uploadFileName, dataIdentifierString);
			TestManager.assertTrue(this.getContext(), this, additionalText, resultBoolean);

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

				resultBoolean = this.getContext().getMediaManager().commandCheckOnServer(this.getContext(), mediaResource, fileType, dataIdentifierString, hashValue);
				TestManager.assertTrue(this.getContext(), this, additionalText, resultBoolean);

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

			boolean booleanResult = this.getContext().getMediaManager().commandReadOnServer(this.getContext(), mediaResource, dataIdentifierString);
			TestManager.assertTrue(this.getContext(), this, additionalText, booleanResult);

			// Return after error
			if (booleanResult == false) return;

			/*
			 * Check if file content can be read
			 */
			additionalText = "--> Tried to read file content of an uploaded file";
			additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
			additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
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
