package fmagic.basic.media;

import java.util.List;

import fmagic.basic.context.Context;
import fmagic.basic.file.FileLocationFunctions;
import fmagic.basic.file.FileUtilFunctions;
import fmagic.basic.resource.ResourceContainer;
import fmagic.basic.resource.ResourceManager;

/**
 * This class extends resource items to specific media functionality.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 30.03.2013 - Created
 * 
 */
public class ResourceContainerMedia extends ResourceContainer
{
	/**
	 * Constructor
	 */
	public ResourceContainerMedia(ResourceContainer resourceContainer)
	{
		super(resourceContainer.getRecourceIdentifier());

		this.copy(resourceContainer);
	}

	/**
	 * Check if the Media Type of the media resource item is set to specific
	 * value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param mediaNumber
	 *            The value number the specific Media Type is assigned to.
	 * 
	 * @return Returns <TT>true</TT> if the media type is defined, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean isMediaType(Context context, int mediaNumber)
	{
		if (mediaNumber < 1) return false;

		try
		{
			ResourceContainer attributeResourceContainer = ResourceManager.attribute(context, "Media", "MediaType");
			String attributeName = attributeResourceContainer.getAliasName();
			String attributeMediaValue = attributeResourceContainer.getAttributeValue(context, mediaNumber, null);
			if (attributeMediaValue == null) return false;
			if (attributeMediaValue.length() == 0) return false;

			String attributeValue = this.getAttribute(attributeName);
			if (attributeValue == null) return false;
			if (attributeValue.length() == 0) return false;

			if (attributeValue.equals(attributeMediaValue)) return true;
		}
		catch (Exception e)
		{
			// Be silent
		}

		return false;
	}

	/**
	 * Check if the Media Type of the media resource item is set to "Image".
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if the Media Type is defined as "Image",
	 *         otherwise <TT>false</TT>.
	 */
	public boolean isMediaTypeImage(Context context)
	{
		return isMediaType(context, 1);
	}

	/**
	 * Check if the Media Type of the media resource item is set to "Video".
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if the Media Type is defined as "Video",
	 *         otherwise <TT>false</TT>.
	 */
	public boolean isMediaTypeVideo(Context context)
	{
		return isMediaType(context, 2);
	}

	/**
	 * Check if the Media Type of the media resource item is set to "Audio".
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if the Media Type is defined as "Audio",
	 *         otherwise <TT>false</TT>.
	 */
	public boolean isMediaTypeAudio(Context context)
	{
		return isMediaType(context, 3);
	}

	/**
	 * Check if the Media Type of the media resource item is set to "Document".
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if the Media Type is defined as "Document",
	 *         otherwise <TT>false</TT>.
	 */
	public boolean isMediaTypeDocument(Context context)
	{
		return isMediaType(context, 4);
	}

	/**
	 * Check if a file type is supported by the media resource.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param parameterFileType
	 *            The file type to check.
	 * 
	 * @return Returns <TT>true</TT> if the file type is supported by the media
	 *         resource item, otherwise <TT>false</TT>.
	 */
	public boolean isFileTypeSupported(Context context, String parameterFileType)
	{
		if (parameterFileType == null) return false;
		if (parameterFileType.length() == 0) return false;

		String normalizedParameterFileType = parameterFileType.toLowerCase().trim();

		try
		{
			// Check general settings of the FileTypes attribute
			ResourceContainer fileTypesAttributeResourceContainer = ResourceManager.attribute(context, "Media", "FileTypes");
			List<String> mediaFileTypesList = fileTypesAttributeResourceContainer.getValueList(context, null);

			boolean isFileTypeFound = false;

			for (String attributeMediaFileType : mediaFileTypesList)
			{
				if (attributeMediaFileType == null || attributeMediaFileType.length() == 0) continue;

				String normalizedAttributeMediaFileType = attributeMediaFileType.toLowerCase().trim();
				if (normalizedParameterFileType.equals(normalizedAttributeMediaFileType)) isFileTypeFound = true;
			}

			if (isFileTypeFound == false) return false;

			// Check media item settings
			String fileTypesValue = this.getAttribute(fileTypesAttributeResourceContainer.getAliasName());
			String fileTypesListOfMediaResourceItem[] = fileTypesValue.split(",");

			if (fileTypesListOfMediaResourceItem == null) return false;
			if (fileTypesListOfMediaResourceItem.length == 0) return false;

			isFileTypeFound = false;

			for (int i = 0; i < fileTypesListOfMediaResourceItem.length; i++)
			{
				if (fileTypesListOfMediaResourceItem[i] == null || fileTypesListOfMediaResourceItem[i].length() == 0) continue;

				String normalizedMediaFileType = fileTypesListOfMediaResourceItem[i].toLowerCase().trim();
				if (normalizedParameterFileType.equals(normalizedMediaFileType)) isFileTypeFound = true;
			}

			if (isFileTypeFound == false) return false;

			// File type was found
			return true;
		}
		catch (Exception e)
		{
			// Be silent
		}

		return false;
	}

	/**
	 * Check if the storage location of the media resource item is set to
	 * a specific value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param storageLocationNumber
	 *            The value number the specific storage location is assigned to.
	 * 
	 * @return Returns <TT>true</TT> if the storage location is defined,
	 *         otherwise <TT>false</TT>.
	 */
	private boolean isStorageLocation(Context context, int storageLocationNumber)
	{
		if (storageLocationNumber < 1) return false;

		try
		{
			ResourceContainer attributeResourceContainer = ResourceManager.attribute(context, "Media", "StorageLocation");
			String attributeName = attributeResourceContainer.getAliasName();
			String attributeStorageLocationValue = attributeResourceContainer.getAttributeValue(context, storageLocationNumber, null);
			if (attributeStorageLocationValue == null) return false;
			if (attributeStorageLocationValue.length() == 0) return false;

			String attributeValue = this.getAttribute(attributeName);
			if (attributeValue == null) return false;
			if (attributeValue.length() == 0) return false;

			if (attributeValue.equals(attributeStorageLocationValue)) return true;
		}
		catch (Exception e)
		{
			// Be silent
		}

		return false;
	}

	/**
	 * Check if the storage location of the media resource item is set to
	 * "Server".
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if the storage location is defined as
	 *         "Server", otherwise <TT>false</TT>.
	 */
	public boolean isStorageLocationServer(Context context)
	{
		return this.isStorageLocation(context, 1);
	}

	/**
	 * Check if the storage location of the media resource item is set to
	 * "Client".
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if the storage location is defined as
	 *         "Client", otherwise <TT>false</TT>.
	 */
	public boolean isStorageLocationClient(Context context)
	{
		return this.isStorageLocation(context, 2);
	}

	/**
	 * Check if the storage location of the media resource item is set to
	 * "Synchronize".
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if the storage location is defined as
	 *         "Synchronize", otherwise <TT>false</TT>.
	 */
	public boolean isStorageLocationSynchronize(Context context)
	{
		return this.isStorageLocation(context, 3);
	}

	/**
	 * Get the logical path of the media resource item.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the logical path, or <TT>null</TT> if no path isn't
	 *         available, or if an error occurred.
	 */
	public String getLogicalPath(Context context)
	{
		try
		{
			ResourceContainer attributeResourceContainer = ResourceManager.attribute(context, "Media", "LogicalPath");
			String attributeName = attributeResourceContainer.getAliasName();

			String attributeValue = this.getAttribute(attributeName);
			if (attributeValue == null) return null;
			if (attributeValue.length() == 0) return null;

			String normalizedPath = "";
			String partsOfLogicalPath[] = attributeValue.trim().toLowerCase().split("/");

			if (partsOfLogicalPath == null) return null;

			for (int i = 0; i < partsOfLogicalPath.length; i++)
			{
				if (partsOfLogicalPath[i] == null || partsOfLogicalPath[i].length() == 0) continue;

				if (normalizedPath.length() > 0) normalizedPath += "/";
				normalizedPath += FileUtilFunctions.generalFitToFileNameCompatibility(partsOfLogicalPath[i]);
			}

			return normalizedPath;
		}
		catch (Exception e)
		{
			// Be silent
		}

		return null;
	}

	/**
	 * Check if the server encoding attribute of the media resource item is set
	 * to <TT>true</TT>.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if the server encoding is set to
	 *         <TT>true</TT>, otherwise <TT>false</TT>.
	 */
	public boolean isServerEncoding(Context context)
	{
		try
		{
			ResourceContainer attributeResourceContainer = ResourceManager.attribute(context, "Media", "ServerEncoding");
			String attributeName = attributeResourceContainer.getAliasName();
			String attributeOriginValue = attributeResourceContainer.getAttributeValue(context, 1, null);
			if (attributeOriginValue == null) return false;
			if (attributeOriginValue.length() == 0) return false;

			String attributeValue = this.getAttribute(attributeName);
			if (attributeValue == null) return false;
			if (attributeValue.length() == 0) return false;

			if (attributeValue.equals(attributeOriginValue)) return true;
		}
		catch (Exception e)
		{
			// Be silent
		}

		return false;
	}

	/**
	 * Check if the client encoding attribute of the media resource item is set
	 * to <TT>true</TT>.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if the client encoding is set to
	 *         <TT>true</TT>, otherwise <TT>false</TT>.
	 */
	public boolean isClientEncoding(Context context)
	{
		try
		{
			ResourceContainer attributeResourceContainer = ResourceManager.attribute(context, "Media", "ClientEncoding");
			String attributeName = attributeResourceContainer.getAliasName();
			String attributeOriginValue = attributeResourceContainer.getAttributeValue(context, 1, null);
			if (attributeOriginValue == null) return false;
			if (attributeOriginValue.length() == 0) return false;

			String attributeValue = this.getAttribute(attributeName);
			if (attributeValue == null) return false;
			if (attributeValue.length() == 0) return false;

			if (attributeValue.equals(attributeOriginValue)) return true;
		}
		catch (Exception e)
		{
			// Be silent
		}

		return false;
	}

	/**
	 * Get the file path of a local media file.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the file path of a media file stored locally.
	 * 
	 */
	public String getMediaRegularFilePath(Context context)
	{
		return FileLocationFunctions.compileFilePath(context.getMediaManager().mediaFileGetRootFilePath(context), FileUtilFunctions.generalFitToFileNameCompatibility(context.getApplicationName()), this.getLogicalPath(context));
	}

	/**
	 * Fit a identifier string to be file name compatible for media files.
	 * 
	 * @param identifier
	 *            The identifier to consider.
	 * 
	 * @return Returns the fitted identifier, or <TT>00000000000000</TT> if it
	 *         couldn't be fitted.
	 * 
	 */
	private String fitIdentifierToFileName(String identifier)
	{
		if (identifier == null) return "00000000000000";
		if (identifier.length() == 0) return "00000000000000";

		String normalizedIdentifier = identifier.trim();

		for (int i = normalizedIdentifier.length(); i < 14; i++)
		{
			normalizedIdentifier = "0".concat(normalizedIdentifier);
		}

		normalizedIdentifier = FileUtilFunctions.generalFitToFileNameCompatibility(normalizedIdentifier);

		return normalizedIdentifier;
	}

	/**
	 * Get the file name mask of a local media file, based an the
	 * <TT>Application Name</TT>, the <TT>Alias Name</TT> and the
	 * <TT>Identifier</TT> of the media item.
	 * <p>
	 * Example: <TT>seniorcitizen-room-00001234-*-*-*.*</TT>
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param dataIdentifier
	 *            The data identifier to consider.
	 * 
	 * @return Returns the file name mask of a media file, including wildcards
	 *         for the parts <TT>Server Encoding</TT>, <TT>Client Encoding</TT>
	 *         , <TT>Hash Value</TT> and <TT>File Type</TT>.
	 * 
	 */
	public String getMediaFileNameMask(Context context, String dataIdentifier)
	{
		String mediaFileNameMask = FileLocationFunctions.getMediaFileName();

		mediaFileNameMask = FileLocationFunctions.replacePlacholder(context, mediaFileNameMask);

		if (this.getAliasName() != null) mediaFileNameMask = mediaFileNameMask.replace("${alias}", FileUtilFunctions.generalFitToFileNameCompatibility(this.getAliasName()));
		mediaFileNameMask = mediaFileNameMask.replace("${identifier}", this.fitIdentifierToFileName(dataIdentifier));
		mediaFileNameMask = mediaFileNameMask.replace("${encodingkey}", "*");
		mediaFileNameMask = mediaFileNameMask.replace("${hashvalue}", "*");
		mediaFileNameMask = mediaFileNameMask.replace("${filetype}", "*");

		return mediaFileNameMask;
	}

	/**
	 * Create a name for a 'pending' file to be used to save pending files, and
	 * returns the name.
	 * <p>
	 * Example: <TT>20130403-155909-625-123-seniorcitizen-ap1-[0001].png</TT>
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param fileType
	 *            The file type to set.
	 * 
	 * @return Returns the file name created.
	 * 
	 */
	public String getMediaPendingFileName(Context context, String fileType)
	{
		String mediaTempFileName = FileLocationFunctions.getMediaPendingFileName();

		mediaTempFileName = FileLocationFunctions.replacePlacholder(context, mediaTempFileName);

		if (fileType != null) mediaTempFileName = mediaTempFileName.replace("${filetype}", fileType);

		return mediaTempFileName;
	}

	/**
	 * Create a name for a 'deleted' file to be used to save obsolete files, and
	 * returns the name.
	 * <p>
	 * Example:
	 * <TT>seniorcitizen-room-00001234-s00-24df3a-20130403-155909-625-123.jpg</TT>
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param fileType
	 *            The file type to set.
	 * 
	 * @return Returns the file name created.
	 * 
	 */
	public String getMediaDeletedFileName(Context context, String originalFileName, String fileType)
	{
		String mediaDeletedFileName = FileLocationFunctions.getMediaDeletedFileName();

		mediaDeletedFileName = FileLocationFunctions.replacePlacholder(context, mediaDeletedFileName);

		if (originalFileName != null) mediaDeletedFileName = mediaDeletedFileName.replace("${originalname}", originalFileName);
		if (fileType != null) mediaDeletedFileName = mediaDeletedFileName.replace("${filetype}", fileType);

		return mediaDeletedFileName;
	}

	/**
	 * Get the path of temporary media files (pending files), depending on the
	 * concrete media resource item.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the file path to be used for temporary pending media
	 *         files of the given media resource type.
	 * 
	 */
	public String getMediaPendingFilePath(Context context)
	{
		return FileLocationFunctions.compileFilePath(this.getMediaRegularFilePath(context), FileLocationFunctions.getMediaPendingSubPath());
	}

	/**
	 * Get the path of deleted media files, depending on the concrete media
	 * resource item.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the file path to be used for deleted media files of the
	 *         given media resource type.
	 * 
	 */
	public String getMediaDeletedFilePath(Context context)
	{
		return FileLocationFunctions.compileFilePath(this.getMediaRegularFilePath(context), FileLocationFunctions.getMediaDeletedSubPath());
	}

	/**
	 * Get the name of the most current existing media file of the local media
	 * repository, regarding a specific media resource item and a specific data
	 * identifier.
	 * <p>
	 * Example: <TT>seniorcitizen-room-00001234-*-*-*.*</TT>
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param dataIdentifier
	 *            The data identifier to consider.
	 * 
	 * @return Returns the most current, existing media file, or <TT>null</TT>
	 *         if an error occurred, or no file could be found.
	 * 
	 */
	public String getMediaRealFileName(Context context, String dataIdentifier)
	{
		String mediaFileNameMask = this.getMediaFileNameMask(context, dataIdentifier);
		String mediaFilePath = this.getMediaRegularFilePath(context);
		return FileUtilFunctions.directorySearchOnMostRecentFile(mediaFilePath, mediaFileNameMask);
	}

	/**
	 * Get the names of an existing file of the local media repository,
	 * regarding a specific media resource item and a specific data identifier.
	 * <p>
	 * Example: <TT>seniorcitizen-room-00001234-*-*-*.*</TT>
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param dataIdentifier
	 *            The data identifier to consider.
	 * 
	 * @return Returns a list of files that were found, or <TT>null</TT> if an
	 *         error occurred.
	 * 
	 */
	public List<String> getMediaRealFileNameList(Context context, String dataIdentifier)
	{
		String mediaFileNameMask = this.getMediaFileNameMask(context, dataIdentifier);
		String mediaFilePath = this.getMediaRegularFilePath(context);
		return FileUtilFunctions.directorySearchForFiles(mediaFilePath, mediaFileNameMask);
	}

	/**
	 * Extract and get the data identifier of a real media file name.
	 * <p>
	 * Example: <TT>20130403-155909-625-seniorcitizen-ap1-[0001].png</TT>
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param filePath
	 *            The file path to analyze.
	 * 
	 * @return Returns the extracted data identifier or <TT>null</TT> if an
	 *         error occurred.
	 * 
	 */
	public String getMediaPartDataIdentifier(Context context, String filePath)
	{
		return getMediaPartByPosition(context, filePath, 2);
	}

	/**
	 * Extract and get a part of a real media file name.
	 * <p>
	 * Example: <TT>20130403-155909-625-seniorcitizen-ap1-[0001].png</TT>
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param filePath
	 *            The file path to analyze.
	 * 
	 * @param position
	 *            The position to extract.
	 * 
	 * @return Returns the extracted part or <TT>null</TT> if an error occurred.
	 * 
	 */
	private String getMediaPartByPosition(Context context, String filePath, int position)
	{
		// Validate parameter
		if (filePath == null || filePath.length() == 0) return null;
		if (position < 0) return null;

		try
		{
			// Extract the file name for the file path
			String fileName = FileUtilFunctions.fileGetFileNamePart(filePath);
			if (fileName == null || fileName.length() == 0) return null;

			// Split the file name
			String parts[] = fileName.split("-");
			if (parts == null || parts.length == 0) return null;
			if (parts.length < position) return null;

			// Get the part
			return parts[position];
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Get the current server encoding key to be used for encoding on server
	 * side.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the server encoding key as string.
	 * 
	 */
	private String getEncodingKeyAsString(Context context)
	{
		// Get key number
		int encodingKeyNumber = 0;

		if (context.getMediaManager().isEncodingEnabled(context, this))
		{
			encodingKeyNumber = context.getMediaManager().getEncodingKeyNumber();
		}

		// Compose string
		String resultString = String.valueOf(encodingKeyNumber);

		for (int i = resultString.length(); i < 2; i++)
		{
			resultString = "0".concat(resultString);
		}

		if (context.getApplicationManager().isServerApplication())
		{
			resultString = "s".concat(resultString);
		}
		else
		{
			resultString = "c".concat(resultString);
		}

		return resultString;
	}

	/**
	 * Get the file name of a local media file, based on
	 * <TT>Application Name</TT>, <TT>Alias Name</TT>, <TT>Data Identifier</TT>,
	 * <TT>Server Encoding</TT>, <TT>Client Encoding</TT> , <TT>Hash Value</TT>
	 * and <TT>File Type</TT>.
	 * <p>
	 * Example: <TT>seniorcitizen-room-00001234-s00-a6gt8e.jpg</TT>
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param dataIdentifier
	 *            The data identifier to consider.
	 * 
	 * @return Returns the file name mask of a media file, including wildcards
	 *         for the parts <TT>Server Encoding</TT>, <TT>Client Encoding</TT>
	 *         , <TT>Hash Value</TT> and <TT>File Type</TT>.
	 * 
	 */
	public String getMediaRealFileName(Context context, String dataIdentifier, String hashValue, String fileType)
	{
		String mediaFileName = FileLocationFunctions.getMediaFileName();

		mediaFileName = FileLocationFunctions.replacePlacholder(context, mediaFileName);

		if (this.getAliasName() != null) mediaFileName = mediaFileName.replace("${alias}", FileUtilFunctions.generalFitToFileNameCompatibility(this.getAliasName()));
		mediaFileName = mediaFileName.replace("${identifier}", this.fitIdentifierToFileName(dataIdentifier));
		mediaFileName = mediaFileName.replace("${encodingkey}", this.getEncodingKeyAsString(context));
		if (hashValue != null) mediaFileName = mediaFileName.replace("${hashvalue}", hashValue.trim());
		if (fileType != null) mediaFileName = mediaFileName.replace("${filetype}", fileType);

		return FileLocationFunctions.compileFilePath(this.getMediaRegularFilePath(context), mediaFileName);
	}

	/**
	 * Get encoding key (server or client), by analyzing the file name of a real
	 * media file.
	 * <p>
	 * Example: <TT>seniorcitizen-room-00001234-s00-a6gt8e.jpg</TT>
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param mediaFilePath
	 *            The file path to analyze.
	 * 
	 * @return Returns the encoding key number or <TT>0</TT>.
	 * 
	 */
	public int getEncodingKeyOfRealFileName(Context context, String mediaFilePath)
	{
		// Validate parameter
		if (mediaFilePath == null || mediaFilePath.length() == 0) return 0;

		// Get file name
		String fileName = FileUtilFunctions.fileGetFileNamePart(mediaFilePath);
		if (fileName == null || fileName.length() == 0) return 0;

		// Analyze file name
		try
		{
			String fileNameParts[] = fileName.split("-");
			if (fileNameParts == null || fileNameParts.length != 5) return 0;

			String encodingSetting = fileNameParts[3];
			if (encodingSetting == null || encodingSetting.length() == 0) return 0;

			if (!(encodingSetting.startsWith("s") || encodingSetting.startsWith("c"))) return 0;

			int keyNumber = Integer.parseInt(encodingSetting.substring(1));

			return keyNumber;
		}
		catch (Exception e)
		{
			// Be silent
			return 0;
		}
	}

	/**
	 * Get hash value, by analyzing the file name of a real media file.
	 * <p>
	 * Example: <TT>seniorcitizen-room-00001234-s00-a6gt8e.jpg</TT>
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param mediaFilePath
	 *            The file path to analyze.
	 * 
	 * @return Returns the hash value of the file or <TT>null</TT>, if an error
	 *         occurred.
	 * 
	 */
	public String getHashValueOfRealFileName(Context context, String mediaFilePath)
	{
		// Validate parameter
		if (mediaFilePath == null || mediaFilePath.length() == 0) return null;

		// Get file name
		String fileName = FileUtilFunctions.fileGetFileNamePart(mediaFilePath);
		if (fileName == null || fileName.length() == 0) return null;

		// Analyze file name
		try
		{
			String fileNameParts[] = fileName.split("-");
			if (fileNameParts == null || fileNameParts.length != 5) return null;

			String hashValue = fileNameParts[4];
			if (hashValue == null || hashValue.length() == 0) return null;

			return hashValue;
		}
		catch (Exception e)
		{
			// Be silent
			return null;
		}
	}
}
