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
import fmagic.test.application.TestManager;
import fmagic.test.runner.TestRunner;

/**
 * This class implements testing functionality regarding the
 * <TT>Media Manager</TT> using a media pool.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 29.04.2013 - Created
 * 
 */
public class TestContainerMediaCommand extends TestContainer
{
	// Organization
	private String parameterResourceGroup = "Factory";
	private String parameterResourceName = "Doorway";
	private String parameterDataIdentifierTestUpload = "9001";
	private int parameterTestCycleNumberOfFiles = 1;
	private int parameterTestCycleDataIdentifierFrom = 600;
	private int parameterTestCycleDataIdentifierToo = 610;

	// Command properties
	private ClientManager parameterClient = null;

	/**
	 * Constructor
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
	 * Constructor
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
		// Do nothing if the test is running in concurrent mode
		if (this.isConcurrentAccess()) return;

		// Setup
		try
		{
			this.doConnection();
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
			this.testPushFileFromClientToServer();

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
	 * Test: Upload File
	 */
	public void testPushFileFromClientToServer()
	{
		try
		{
			TestManager.servicePrintHeader(this.getContext(), "===> testUploadFileFromClientToServer()", null);

			// Get file directory
			ResourceContainer configuration = ResourceManager.configuration(this.getContext(), "MediaTest", "DirectoryToSearchForMediaFiles");
			String uploadFilePath = this.getContext().getConfigurationManager().getProperty(this.getContext(), configuration, null, true);

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
			for (int i = 0; i < 100000; i++)
			{
				int index = FileUtilFunctions.generalGetRandomValue(0, fileList.size() - 1);
				this.doPushFileFromClientToServer(this.parameterResourceGroup, this.parameterResourceName, this.parameterDataIdentifierTestUpload, fileList.get(index));
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
	private void doConnection()
	{
		try
		{
			// General variables
			ClientCommand command;
			ResponseContainer responseContainer;

			// Delete old session
			this.cleanClientSession();

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
	 * Start connection to the application server
	 */
	private void cleanClientSession()
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
			 *  Upload the media file to a server
			 */
			String additionalText = "--> Upload media file to server";
			additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
			additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";

			boolean resultBoolean = this.getContext().getMediaManager().commandUploadToServer(this.getContext(), mediaResource, uploadFileName, dataIdentifierString);
			TestManager.assertTrue(this.getContext(), this, additionalText, resultBoolean);

			/*
			 *  Check if uploaded file really exists on server
			 */
			additionalText = "--> Check if media file exists on server";
			additionalText += "\n--> Media resource: '" + mediaResource.getRecourceIdentifier() + "'";
			additionalText += "\n--> Upload file name: '" + uploadFileName + "'";
			additionalText += "\n--> Data identifier: '" + dataIdentifierString + "'";

			resultBoolean = this.getContext().getMediaManager().commandCheckOnServer(this.getContext(), mediaResource, fileType, dataIdentifierString, hashValue);
			TestManager.assertTrue(this.getContext(), this, additionalText, resultBoolean);

			/*
			 *  Read the uploaded file from server and store it in the local media repository
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
	public void setParameterClient(ClientManager parameterClient)
	{
		this.parameterClient = parameterClient;
	}
}
