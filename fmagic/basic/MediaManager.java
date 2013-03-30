package fmagic.basic;

import java.util.Date;
import java.util.List;

/**
 * This class implements the management of media used by servers and clients.
 * <p>
 * Media data are all kinds of media types, that are usually provided as files,
 * like images, audio media, videos, documents and others. Those files have to
 * be stored on server or client, and they need to be transferred form server to
 * client and contrariwise. Last not least they are to be shown on screen.
 * <p>
 * Each logical media data is defined as a resource item and is given some
 * parameters to describe its main behavior. For example, if you want to use
 * images for user accounts, say to show the user portrait on screen, you are
 * requested to define a media resource item first. There are some parameters to
 * consider, like the internal name of the media, the media type (image, video,
 * audio, document, or others), the allowed file types (jpg, png, mkv, flv, or
 * others), the origin of the media (client or server), the logical path to
 * store media files, the storage location, or security settings.
 * <p>
 * Once you have determined the media resource item and the media parameters,
 * you can handle the media file itself. You may download it from the server to
 * the client, or upload it from client to server, or store it internally in the
 * resp. path structure of client or server, or show it on screen.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 30.03.2013 - Created
 * 
 */
public class MediaManager implements ResourceInterface
{
	/**
	 * Constructor
	 */
	public MediaManager()
	{
	}

	@Override
	public String printTemplate(Context context, boolean includingResourceIdentifiers)
	{
		String dumpText = "";

		String typeCriteria[] = { "Media" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceTemplate(context, includingResourceIdentifiers, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public String printManual(Context context)
	{
		String dumpText = "";

		String typeCriteria[] = { "Media" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceManual(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public String printIdentifierList(Context context)
	{
		String dumpText = "";

		String typeCriteria[] = { "Media" };
		String applicationCriteria[] = null;
		String originCriteria[] = null;
		String usageCriteria[] = null;
		String groupCriteria[] = null;
		dumpText += context.getResourceManager().printResourceIdentifierList(context, typeCriteria, applicationCriteria, originCriteria, usageCriteria, groupCriteria);

		// Return
		return dumpText;
	}

	@Override
	public boolean ckeckOnResourceIdentifierIntegrityError(Context context)
	{
		// Variables
		boolean isIntegrityError = false;
		ResourceManager resourceManager = context.getResourceManager();

		// Check for integrity errors (Named resource identifiers)
		try
		{
			// Prepare data
			ResourceContainer mediaTypeAttributeResourceContainer = ResourceManager.attribute(context, "Media", "MediaType");
			List<String> mediaTypeList = mediaTypeAttributeResourceContainer.getValueList(context, null);

			ResourceContainer fileTypesAttributeResourceContainer = ResourceManager.attribute(context, "Media", "FileTypes");
			List<String> mediaFileTypesList = fileTypesAttributeResourceContainer.getValueList(context, null);

			ResourceContainer mediaOriginAttributeResourceContainer = ResourceManager.attribute(context, "Media", "Origin");
			List<String> mediaOriginList = mediaOriginAttributeResourceContainer.getValueList(context, null);

			// Go through all resource items
			for (ResourceContainer resourceContainer : resourceManager.getResources().values())
			{
				// Check if resource item is typed as "Media"
				if (!resourceContainer.getType().equalsIgnoreCase("media")) continue;

				// Check if name is set
				String name = resourceContainer.getName();
				if (name == null || name.length() == 0) continue;

				/*
				 * Alias name must be set
				 */
				if (resourceContainer.checkAliasName() == false)
				{
					String errorString = "--> Alias name for media item is not set.";
					String fileName = resourceManager.getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
					if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
					errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
					errorString += "\n--> The Alias name is used as a part of the file name of media files.";
					errorString += "\n--> Please set an Alias that is unique to all media of an application.";

					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

					isIntegrityError = true;
				}

				/*
				 * The attribute 'MediaType' must be set
				 */
				String mediaTypeValue = resourceContainer.getAttribute(mediaTypeAttributeResourceContainer.getAliasName());

				if (mediaTypeValue == null)
				{
					String errorString = "--> Media Type for media item is not set.";
					String fileName = resourceManager.getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
					if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
					errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
					errorString += "\n--> The attribute Media Type is used to differentiate the types 'Image', 'Video', 'Audio' and 'Document'.";
					errorString += "\n--> Please set a Media Type corresponding to the allowed types.";

					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

					isIntegrityError = true;
				}
				/*
				 * Check if the Media Type is one of the allowed media types
				 */
				else
				{
					String flatListOfAllowedMediaTypes = "";
					boolean isMediaTypeAllowed = false;

					for (String allowedMediaType : mediaTypeList)
					{
						flatListOfAllowedMediaTypes += "[" + allowedMediaType + "]";
						if (allowedMediaType.equals(mediaTypeValue)) isMediaTypeAllowed = true;
					}

					if (isMediaTypeAllowed == false)
					{
						String errorString = "--> Media Type '" + mediaTypeValue + "' is not allowed.";
						errorString += "\n--> List of allowed Media Types: " + flatListOfAllowedMediaTypes;
						errorString += "\n--> Please pay attention to lower and upper cases.";
						String fileName = resourceManager.getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
						if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
						errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
						errorString += "\n--> The attribute Media Type is used to differentiate the types 'Image', 'Video', 'Audio' and 'Document'.";
						errorString += "\n--> Please set a Media Type corresponding to the allowed types.";

						context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

						isIntegrityError = true;
					}
				}

				/*
				 * The attribute 'FileTypes' must be set
				 */
				String fileTypesValue = resourceContainer.getAttribute(fileTypesAttributeResourceContainer.getAliasName());

				if (fileTypesValue == null || fileTypesValue.length() == 0)
				{
					String errorString = "--> File types for media item are not set.";
					String fileName = resourceManager.getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
					if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
					errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
					errorString += "\n--> The attribute 'FileTypes' is used to allow upload and usage of specific media files.";
					errorString += "\n--> Please set the file types corresponding to the allowed types.";

					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

					isIntegrityError = true;
				}
				/*
				 * Check if the File Types are allowed
				 */
				else
				{
					boolean isFileTypesAllowed = true;
					String flatListOfAllowedFileTypes = "";
					String flatListOfNotAllowedFileTypes = "";

					for (String allowedFileType : mediaFileTypesList)
					{
						flatListOfAllowedFileTypes += "[" + allowedFileType + "]";
					}

					String fileTypesListOfMediaResourceItem[] = fileTypesValue.split(",");

					if (fileTypesListOfMediaResourceItem != null)
					{
						for (int i = 0; i < fileTypesListOfMediaResourceItem.length; i++)
						{
							if (!mediaFileTypesList.contains(fileTypesListOfMediaResourceItem[i].trim()))
							{
								isFileTypesAllowed = false;
								flatListOfNotAllowedFileTypes += "[" + fileTypesListOfMediaResourceItem[i].trim() + "]";
							}
						}
					}

					if (isFileTypesAllowed == false)
					{
						String errorString = "--> File Type " + flatListOfNotAllowedFileTypes + " is not allowed.";
						errorString += "\n--> List of allowed File Types: " + flatListOfAllowedFileTypes;
						errorString += "\n--> Please pay attention to lower and upper cases.";
						String fileName = resourceManager.getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
						if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
						errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
						errorString += "\n--> The atttribute 'FileTypes' is used to determine which media files can be uploaded for the given media item.";
						errorString += "\n--> Please set file types corresponding to the allowed types.";

						context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

						isIntegrityError = true;
					}
				}

				/*
				 * The attribute 'Origin' must be set
				 */
				String mediaOriginValue = resourceContainer.getAttribute(mediaOriginAttributeResourceContainer.getAliasName());

				if (mediaOriginValue == null)
				{
					String errorString = "--> Origin for media item is not set.";
					String fileName = resourceManager.getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
					if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
					errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
					errorString += "\n--> The attribute 'Origin' determines the source of uploads: 'Server', 'Client' or 'All'.";
					errorString += "\n--> Please set a Origin corresponding to the allowed types.";

					context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

					isIntegrityError = true;
				}
				/*
				 * Check if the Origin is one of the allowed values
				 */
				else
				{
					String flatListOfAllowedOrigins = "";
					boolean isOriginAllowed = false;

					for (String allowedOrigin : mediaOriginList)
					{
						flatListOfAllowedOrigins += "[" + allowedOrigin + "]";
						if (allowedOrigin.equals(mediaOriginValue)) isOriginAllowed = true;
					}

					if (isOriginAllowed == false)
					{
						String errorString = "--> Origin '" + mediaOriginValue + "' is not allowed.";
						errorString += "\n--> List of allowed origin values: " + flatListOfAllowedOrigins;
						errorString += "\n--> Please pay attention to lower and upper cases.";
						String fileName = resourceManager.getReadResourceIdentifiersList().get(resourceContainer.getRecourceIdentifier());
						if (fileName != null) errorString += "\n--> In file: '" + fileName + "'";
						errorString += "\n--> Full resource identifier: '" + resourceContainer.getRecourceIdentifier() + "'";
						errorString += "\n--> The attribute 'Origin' determines the source of uploads: 'Server', 'Client' or 'All'.";
						errorString += "\n--> Please set a Origin corresponding to the allowed types.";

						context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Resource", "IntegrityError"), errorString, null);

						isIntegrityError = true;
					}
				}
			}

		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return isIntegrityError;
	}

	/**
	 * Notify the WATCHDOG about media item access.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param resourceIdentifier
	 *            The resource identifier of the media item.
	 * 
	 * @param additionalTextParameter
	 *            Additional text to notify, or <TT>null</TT>.
	 */
	private void notifyWatchdog(Context context, String identifier, String additionalTextParameter)
	{
		try
		{
			// Set message Text
			String messageText = "Access to Media Item";

			// Set additional text
			String additionalText = "--> Access to Media Item";
			if (additionalTextParameter != null) additionalText += "\n" + additionalTextParameter;
			additionalText += "\n--> Identifier: '" + identifier + "'";

			// Set resource identifier documentation
			String resourceDocumentationText = null;
			resourceDocumentationText = context.getResourceManager().getResource(context, identifier).printManual(context);

			if (context.getWatchdogManager() != null) context.getWatchdogManager().addWatchdogCommand(context, identifier, messageText, additionalText, resourceDocumentationText, null, new Date());
		}
		catch (Exception e)
		{
			// Be silent
		}
	}
}
