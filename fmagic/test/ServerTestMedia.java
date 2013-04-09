package fmagic.test;

import java.util.ArrayList;
import java.util.List;

import fmagic.basic.Context;
import fmagic.basic.FileLocationManager;
import fmagic.basic.MediaContainer;
import fmagic.basic.ResourceContainer;
import fmagic.basic.ResourceContainerMedia;
import fmagic.basic.ResourceManager;
import fmagic.basic.Util;
import fmagic.server.ServerManager;

public class ServerTestMedia implements Runnable
{
	private final Context context;
	private final ServerManager server;

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
	public ServerTestMedia(ServerManager server, Context context)
	{
		this.server = server;
		this.context = context.createSilentDumpContext(ResourceManager.context(context, "Media", "Processing"));

	}

	private void testSetup()
	{
		if (this.server == null) return;
	}

	private void testCleanup()
	{
		if (this.server == null) return;
	}

	@Override
	public void run()
	{
		// Check if server is instantiated
		if (this.server == null) return;

		// Setup test environment
		this.testSetup();

		// Test
		this.testMediaAttribute();
		this.testMediaResource();
		this.testUploadFile(this.parameterResourceGroup, this.parameterResourceName, this.parameterDataIdentifierTestUpload);
		this.testObsoleteFile(this.parameterResourceGroup, this.parameterResourceName, this.parameterDataIdentifierTestObsolete);
		this.testStressTestUploadFiles(this.parameterResourceGroup, this.parameterResourceName, this.parameterTestCycleNumber, this.parameterTestCycleDataIdentifierFrom, this.parameterTestCycleDataIdentifierToo);

		// Cleanup test environment
		this.testCleanup();
	}

	/**
	 * Test: Media Attribute
	 */
	private void testMediaAttribute()
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
	private void testMediaResource()
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
	private void testUploadFile(String group, String name, String dataIdentifierString)
	{
		TestManager.assertPrint(context, "===> testUploadFile()", null);

		// Clear (delete) media files
		ResourceContainerMedia mediaResource = ResourceManager.media(context, group, name);
		String mediaFileNameMask = mediaResource.getMediaFileNameMask(context, dataIdentifierString);
		String mediaFilePath = mediaResource.getMediaRegularFilePath(context);
		List<String> mediaFiles = Util.fileSearchDirectory(mediaFilePath, mediaFileNameMask);

		if (mediaFiles != null && mediaFiles.size() > 0)
		{
			int nuOfDeletedFiles = Util.fileDelete(mediaFiles);
			TestManager.assertEquals(context, null, mediaFiles.size(), nuOfDeletedFiles);
		}

		// Get file List
		String uploadFilePath = TestManager.getTestStuffFilePath(context) + FileLocationManager.getPathElementDelimiterString() + "Images";
		List<String> fileNameList = Util.fileSearchDirectory(uploadFilePath, "*.jpg");
		
		// Try some uploads
		this.doUploadFile(group, name, dataIdentifierString, fileNameList.get(0));
		this.doUploadFile(group, name, dataIdentifierString, fileNameList.get(1));
		this.doUploadFile(group, name, dataIdentifierString, fileNameList.get(2));
		this.doUploadFile(group, name, dataIdentifierString, fileNameList.get(0));
		this.doUploadFile(group, name, dataIdentifierString, fileNameList.get(1));
		this.doUploadFile(group, name, dataIdentifierString, fileNameList.get(2));
		this.doUploadFile(group, name, dataIdentifierString, fileNameList.get(3));
	}

	/**
	 * Test: Obsolete File
	 */
	private void testObsoleteFile(String group, String name, String dataIdentifierString)
	{
		TestManager.assertPrint(context, "===> testObsoleteFile()", null);

		try
		{
			// Initialize variables
			ResourceContainerMedia mediaResource = ResourceManager.media(context, group, name);
			int nuOfObsoleteFiles = 0;

			String uploadFilePath = TestManager.getTestStuffFilePath(context) + FileLocationManager.getPathElementDelimiterString() + "Images";
			List<String> fileNameList = Util.fileSearchDirectory(uploadFilePath, "*.jpg");

			// Clear (delete) media files
			String mediaFileNameMask = mediaResource.getMediaFileNameMask(context, dataIdentifierString);
			String mediaFilePath = mediaResource.getMediaRegularFilePath(context);
			List<String> mediaFiles = Util.fileSearchDirectory(mediaFilePath, mediaFileNameMask);

			if (mediaFiles != null)
			{
				nuOfObsoleteFiles = mediaFiles.size();

				if (nuOfObsoleteFiles > 0)
				{
					int nuOfDeletedFiles = Util.fileDelete(mediaFiles);
					TestManager.assertEquals(context, null, mediaFiles.size(), nuOfDeletedFiles);
				}
			}

			nuOfObsoleteFiles = -1;

			// Override the file and check number of obsolete files again
			for (int i = 0; i < 10; i++)
			{
				// Override (upload) media file
				this.doUploadFile(group, name, dataIdentifierString, fileNameList.get(i));

				// Get list of obsolete files
				List<String> obsoleteFiles = Util.fileSearchDirectoryOnObsoleteFiles(mediaFilePath, mediaFileNameMask);
				TestManager.assertNotNull(context, null, obsoleteFiles);
				TestManager.assertEquals(context, null, obsoleteFiles.size(), nuOfObsoleteFiles + i + 1);

				// The new uploaded file name must not be part of the list
				String recentFileName = mediaResource.getMediaRealFileName(context, dataIdentifierString);
				TestManager.assertFalse(context, null, obsoleteFiles.contains(recentFileName));
			}
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
					List<String> fileList = Util.fileSearchDirectory(filePath, "*.jpg");

					if (fileList != null)
					{
						for (String fileName : fileList)
						{
							// Upload file
							if (dataIdentifierInteger >= dataIdentifierToo) dataIdentifierInteger = dataIdentifierFrom;
							this.doUploadFile(mediaResourceGroup, mediaResourceName, String.valueOf(dataIdentifierInteger), fileName);
							dataIdentifierInteger++;
						}
					}
				}
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
				booleanResult = mediaContainer.bindMedia();
				TestManager.assertTrue(context, additionalText, booleanResult);

				byte[] contentAsByteBuffer = mediaContainer.readMediaContentAsByteArray();
				TestManager.assertNotNull(context, additionalText, contentAsByteBuffer);
				TestManager.assertGreaterThan(context, additionalText, contentAsByteBuffer.length, 0);

				booleanResult = mediaContainer.releaseMedia();
				TestManager.assertTrue(context, additionalText, booleanResult);
			}
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
}
