package fmagic.basic;

import java.util.List;

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
	ResourceContainerMedia(ResourceContainer resourceContainer)
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
	 * specific value.
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
				normalizedPath += Util.fitToFileNameCompatibility(partsOfLogicalPath[i]);
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
		String mediaFilePath = context.getMediaManager().getMediaRootFilePath(context) + FileLocationManager.getPathElementDelimiterString() + Util.fitToFileNameCompatibility(context.getApplicationName()) + FileLocationManager.getPathElementDelimiterString() + this.getLogicalPath(context);
		return mediaFilePath;
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

		normalizedIdentifier = Util.fitToFileNameCompatibility(normalizedIdentifier);

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
		String mediaFileNameMask = FileLocationManager.getMediaFileName();

		mediaFileNameMask = FileLocationManager.replacePlacholder(context, mediaFileNameMask);

		if (this.getAliasName() != null) mediaFileNameMask = mediaFileNameMask.replace("${alias}", Util.fitToFileNameCompatibility(this.getAliasName()));
		mediaFileNameMask = mediaFileNameMask.replace("${identifier}", this.fitIdentifierToFileName(dataIdentifier));
		mediaFileNameMask = mediaFileNameMask.replace("${encodingkey}", "*");
		mediaFileNameMask = mediaFileNameMask.replace("${hashvalue}", "*");
		mediaFileNameMask = mediaFileNameMask.replace("${filetype}", "*");

		return mediaFileNameMask;
	}

	/**
	 * Create a name for a temporary file to be used to save pending files, and
	 * returns the name.
	 * <p>
	 * Example: <TT>20130403-155909-625-seniorcitizen-ap1-[0001].png</TT>
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
		String mediaTempFileName = FileLocationManager.getMediaPendingFileName();

		mediaTempFileName = FileLocationManager.replacePlacholder(context, mediaTempFileName);

		if (fileType != null) mediaTempFileName = mediaTempFileName.replace("${filetype}", fileType);

		return mediaTempFileName;
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
		return this.getMediaRegularFilePath(context) + FileLocationManager.getPathElementDelimiterString() + FileLocationManager.getMediaPendingSubPath();
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
		return this.getMediaRegularFilePath(context) + FileLocationManager.getPathElementDelimiterString() + FileLocationManager.getMediaDeletedSubPath();
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
	public List<String> getMediaRealFileName(Context context, String dataIdentifier)
	{
		String mediaFileNameMask = this.getMediaFileNameMask(context, dataIdentifier);
		String mediaFilePath = this.getMediaRegularFilePath(context);
		return Util.fileSearchDirectory(mediaFilePath, mediaFileNameMask);
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
	private String getServerEncodingKeyAsString(Context context)
	{
		// Get key number
		int serverMediaKeyNumber = 0;

		if (context.getMediaManager().checkServerEncoding(context, this))
		{
			serverMediaKeyNumber = context.getMediaManager().getServerMediaKeyNumber();
		}

		// Compose string

		String resultString = String.valueOf(serverMediaKeyNumber);

		for (int i = resultString.length(); i < 2; i++)
		{
			resultString = "0".concat(resultString);
		}

		resultString = "s".concat(resultString);

		return resultString;
	}

	/**
	 * Get the current client encoding key to be used for encoding on client
	 * side.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns the client encoding key as string.
	 * 
	 */
	private String getClientEncodingKeyAsString(Context context)
	{
		return "c00";
	}

	/**
	 * Get the file name of a local media file, based on
	 * <TT>Application Name</TT>, <TT>Alias Name</TT>, <TT>Data Identifier</TT>,
	 * <TT>Server Encoding</TT>, <TT>Client Encoding</TT> , <TT>Hash Value</TT>
	 * and <TT>File Type</TT>.
	 * <p>
	 * Example: <TT>seniorcitizen-room-00001234-01-00-a6gt8e.jpg</TT>
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
		String mediaFileName = FileLocationManager.getMediaFileName();

		mediaFileName = FileLocationManager.replacePlacholder(context, mediaFileName);

		if (this.getAliasName() != null) mediaFileName = mediaFileName.replace("${alias}", Util.fitToFileNameCompatibility(this.getAliasName()));
		mediaFileName = mediaFileName.replace("${identifier}", this.fitIdentifierToFileName(dataIdentifier));
		mediaFileName = mediaFileName.replace("${encodingkey}", this.getServerEncodingKeyAsString(context));
		if (hashValue != null) mediaFileName = mediaFileName.replace("${hashvalue}", hashValue.trim());
		if (fileType != null) mediaFileName = mediaFileName.replace("${filetype}", fileType);

		return this.getMediaRegularFilePath(context) + FileLocationManager.getPathElementDelimiterString() + mediaFileName;
	}

}
