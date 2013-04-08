package fmagic.test;

import java.util.ArrayList;
import java.util.List;

import fmagic.basic.Context;
import fmagic.basic.MediaContainer;
import fmagic.basic.ResourceContainer;
import fmagic.basic.ResourceContainerMedia;
import fmagic.basic.ResourceManager;
import fmagic.basic.Util;
import fmagic.server.ServerManager;

public class ApplicationTestMedia implements Runnable
{
	private final Context context;
	private final ServerManager server;
	
	private final String mediaResourceGroup;
	private final String mediaResourceName;

	/**
	 * Constructor
	 */
	public ApplicationTestMedia(ServerManager server, Context context, String mediaResourceGroup, String mediaResourceName)
	{
		this.server = server;
		this.context = context.createSilentDumpContext(ResourceManager.context(context, "Media", "Processing"));
		this.mediaResourceGroup = mediaResourceGroup;
		this.mediaResourceName = mediaResourceName;
		
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
//		this.testMediaAttribute("Media", "ClientEncoding");
//		this.testMediaResource(mediaResourceGroup, mediaResourceName);
//		this.testMediaFileManagement(mediaResourceGroup, mediaResourceName);
		this.testUploadFile(mediaResourceGroup, mediaResourceName);
		this.testObsoleteFile(mediaResourceGroup, mediaResourceName);
//		this.testStressTestUploadFiles(mediaResourceGroup, mediaResourceName, 2, 1, 40);
		
		// Cleanup test environment
		this.testCleanup();
	}

	/**
	 * Test: Media Attribute
	 */
	private void testMediaAttribute(String mediaResourceGroup, String mediaResourceName)
	{
		ResourceContainer attributeResourceContainer = ResourceManager.attribute(context, mediaResourceGroup, mediaResourceName);
		String documentation = attributeResourceContainer.printManual(context);

		System.out.println("");
		System.out.println("===> [" + this.server.getCodeName() + "] testMediaAttribute()");
		System.out.println("");

		System.out.println(documentation);
	}

	/**
	 * Test: Media Resource
	 */
	private void testMediaResource(String mediaResourceGroup, String mediaResourceName)
	{
		System.out.println("");
		System.out.println("===> [" + this.server.getCodeName() + "] testMediaResource()");
		System.out.println("");

		ResourceContainerMedia mediaResourceContainer = ResourceManager.media(context, mediaResourceGroup, mediaResourceName);
		String documentation = mediaResourceContainer.printManual(context);

		System.out.println("");
		System.out.println(documentation);

		System.out.println("");
		System.out.println("Is IMAGE? " + mediaResourceContainer.isMediaTypeImage(context));
		System.out.println("Is VIDEO? " + mediaResourceContainer.isMediaTypeVideo(context));
		System.out.println("Is AUDIO? " + mediaResourceContainer.isMediaTypeAudio(context));
		System.out.println("Is DOCUMENT? " + mediaResourceContainer.isMediaTypeDocument(context));
		System.out.println("JPG supported? " + mediaResourceContainer.isFileTypeSupported(context, "JPG"));
		System.out.println("jpg supported? " + mediaResourceContainer.isFileTypeSupported(context, "jpg"));
		System.out.println("mkv supported? " + mediaResourceContainer.isFileTypeSupported(context, "mkv"));
		System.out.println("AAA supported? " + mediaResourceContainer.isFileTypeSupported(context, "aaa"));
		System.out.println("StorageLocation is SERVER? " + mediaResourceContainer.isStorageLocationServer(context));
		System.out.println("StorageLocation is CLIENT? " + mediaResourceContainer.isStorageLocationClient(context));
		System.out.println("StorageLocation is SYNCHRONIZED? " + mediaResourceContainer.isStorageLocationSynchronize(context));
		System.out.println("LogicalPath? " + mediaResourceContainer.getLogicalPath(context));
		System.out.println("ServerEncoding? " + mediaResourceContainer.isServerEncoding(context));
		System.out.println("ClientEncoding? " + mediaResourceContainer.isClientEncoding(context));
	}

	/**
	 * Test: Media File Management
	 */
	private void testMediaFileManagement(String mediaResourceGroup, String mediaResourceName)
	{
		System.out.println("");
		System.out.println("===> [" + this.server.getCodeName() + "] testMediaFileManagement()");
		System.out.println("");

		ResourceContainerMedia mediaResourceContainer = ResourceManager.media(context, mediaResourceGroup, mediaResourceName);

		System.out.println("Local media file path? " + mediaResourceContainer.getMediaRegularFilePath(context));
		System.out.println("Media file name mask? " + mediaResourceContainer.getMediaFileNameMask(context, "1234"));
		System.out.println("Temp file name? " + mediaResourceContainer.getMediaPendingFileName(context, "png"));
		System.out.println("Pending file path? " + mediaResourceContainer.getMediaPendingFilePath(context));
		System.out.println("Deleted file path? " + mediaResourceContainer.getMediaDeletedFilePath(context));
	}

	/**
	 * Test: Upload File
	 */
	private void testUploadFile(String mediaResourceGroup, String mediaResourceName)
	{
		System.out.println("");
		System.out.println("===> [" + this.server.getCodeName() + "] testUploadFile()");
		System.out.println("");

		ResourceContainerMedia mediaResourceContainer = ResourceManager.media(context, mediaResourceGroup, mediaResourceName);

		String uploadFileName = "E:/Bücher/Active/Busch, Rotes Kreuz über Stalingrad 1 + 2/Images/DoktorBarnickel/Apotheker_Reinsdorf.jpg";
		String dataIdentifierString = "1234";

		System.out.println("Upload file? " + uploadFileName);
		System.out.println("Data identifier? " + dataIdentifierString);
		System.out.println("Upload result? " + context.getMediaManager().mediaFileOperationUpload(context, mediaResourceContainer, uploadFileName, dataIdentifierString));

		MediaContainer uploadMedia = new MediaContainer(context, mediaResourceContainer, dataIdentifierString);
		uploadMedia.bindMedia();
		System.out.println(uploadMedia);
		uploadMedia.releaseMedia();
	}

	/**
	 * Test: Obsolete File
	 */
	private void testObsoleteFile(String mediaResourceGroup, String mediaResourceName)
	{
		System.out.println("");
		System.out.println("===> [" + this.server.getCodeName() + "] testObsoleteFile()");
		System.out.println("");

		ResourceContainerMedia mediaResourceContainer = ResourceManager.media(context, mediaResourceGroup, mediaResourceName);
		String dataIdentifierString = "1234";

		String mediaFileNameMask = mediaResourceContainer.getMediaFileNameMask(context, dataIdentifierString);
		String mediaFilePath = mediaResourceContainer.getMediaRegularFilePath(context);
		List<String> obsoleteFiles = Util.fileSearchDirectoryOnObsoleteFiles(mediaFilePath, mediaFileNameMask);

		if (obsoleteFiles != null && obsoleteFiles.size() > 0)
		{
			System.out.println("");
			System.out.println("List of obsolete file:");

			for (String obsoleteFilePath : obsoleteFiles)
			{
				System.out.println("Obsolete?: " + obsoleteFilePath);
			}
		}
	}

	/**
	 * Test: Stress Test Upload Files
	 */
	private void testStressTestUploadFiles(String mediaResourceGroup, String mediaResourceName, int nuOfTestCycles, int dataIdentifierFrom, int dataIdentifierToo)
	{
		System.out.println("");
		System.out.println("===> [" + this.server.getCodeName() + "] testStressTestUploadFiles()");
		System.out.println("");

		ResourceContainerMedia mediaResourceContainer = ResourceManager.media(context, mediaResourceGroup, mediaResourceName);

		try
		{
			List<String> directoryList = new ArrayList<String>();
			directoryList.add("E:/Bücher/Active/Busch, Rotes Kreuz über Stalingrad 1 + 2/Images/Stalingrad15-a");
			directoryList.add("E:/Bücher/Active/Busch, Rotes Kreuz über Stalingrad 1 + 2/Images/Stalingrad15-b");
			directoryList.add("E:/Bücher/Active/Busch, Rotes Kreuz über Stalingrad 1 + 2/Images/Stalingrad15-c");
			directoryList.add("E:/Bücher/Active/Busch, Rotes Kreuz über Stalingrad 1 + 2/Images/WilhelmGrosse");
			directoryList.add("E:/Bücher/Active/Busch, Rotes Kreuz über Stalingrad 1 + 2/Images/Zusatzbilder");
			directoryList.add("E:/Bücher/Active/Busch, Rotes Kreuz über Stalingrad 1 + 2/Images/DoktorBarnickel");
			directoryList.add("E:/Bücher/Active/Busch, Rotes Kreuz über Stalingrad 1 + 2/Images/DoktorKluger");
			directoryList.add("E:/Bücher/Active/Busch, Rotes Kreuz über Stalingrad 1 + 2/Images/JosefLeitner");
			directoryList.add("E:/Bücher/Active/Busch, Rotes Kreuz über Stalingrad 1 + 2/Images/RudolfBöker");

			int dataIdentifierInteger = dataIdentifierFrom;

			for (int testCounter = 1; testCounter <= nuOfTestCycles; testCounter++)
			{
				System.out.println("");
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				System.out.println("$ Test cycle: " + String.valueOf(testCounter));
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				System.out.println("");

				for (String filePath : directoryList)
				{
					List<String> fileList = Util.fileSearchDirectory(filePath, "*.jpg");

					if (fileList != null)
					{
						for (String fileName : fileList)
						{
							if (dataIdentifierInteger >= dataIdentifierToo) dataIdentifierInteger = dataIdentifierFrom;
							dataIdentifierInteger++;

							System.out.println("");
							System.out.println("Test cycle [" + this.server.getCodeName() + "]? " + String.valueOf(testCounter));
							System.out.println("File? " + fileName);
							System.out.println("Data identifier? " + String.valueOf(dataIdentifierInteger));
							System.out.println("Upload result? " + context.getMediaManager().mediaFileOperationUpload(context, mediaResourceContainer, fileName, String.valueOf(dataIdentifierInteger)));

							MediaContainer media = new MediaContainer(context, mediaResourceContainer, String.valueOf(dataIdentifierInteger));
							media.bindMedia();
							byte[] contentAsByteBuffer = media.readMediaContentAsByteArray();
							if (contentAsByteBuffer != null) System.out.println("Size of media byte? " + String.valueOf(contentAsByteBuffer.length));
							String contentAsString = media.readMediaContentAsString();
							if (contentAsString != null) System.out.println("Content? " + contentAsString.substring(0, 10));
							media.releaseMedia();
						}
					}
				}

				// Dump errors
				if (context.isErrorInDumpList()) context.flushDump();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return;
	}
}
