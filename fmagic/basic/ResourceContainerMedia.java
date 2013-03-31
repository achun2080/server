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
	 * Check if the Origin of the media resource item is set to specific value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param originNumber
	 *            The value number the specific origin is assigned to.
	 * 
	 * @return Returns <TT>true</TT> if the origin is defined, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean isOrigin(Context context, int originNumber)
	{
		if (originNumber < 1) return false;

		try
		{
			ResourceContainer attributeResourceContainer = ResourceManager.attribute(context, "Media", "Origin");
			String attributeName = attributeResourceContainer.getAliasName();
			String attributeOriginValue = attributeResourceContainer.getAttributeValue(context, originNumber, null);
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
	 * Check if the Origin of the media resource item is set to "Server".
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if the Origin is defined as "Server",
	 *         otherwise <TT>false</TT>.
	 */
	public boolean isOriginServer(Context context)
	{
		return this.isOrigin(context, 1);
	}

	/**
	 * Check if the Origin of the media resource item is set to "Client".
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if the Origin is defined as "Client",
	 *         otherwise <TT>false</TT>.
	 */
	public boolean isOriginClient(Context context)
	{
		return this.isOrigin(context, 2);
	}

	/**
	 * Check if the Origin of the media resource item is set to "All".
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if the Origin is defined as "All",
	 *         otherwise <TT>false</TT>.
	 */
	public boolean isOriginAll(Context context)
	{
		return this.isOrigin(context, 3);
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
}
