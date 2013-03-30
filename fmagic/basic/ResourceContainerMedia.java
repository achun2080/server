package fmagic.basic;

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
	 * @return Returns <TT>true</TT> if the Media Type is defined as "Image",
	 *         otherwise <TT>false</TT>.
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

}
