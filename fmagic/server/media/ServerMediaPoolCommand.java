package fmagic.server.media;

import java.util.Date;

import fmagic.basic.context.Context;


/**
 * This class implements a container used for media pool commands to be executed on media pool.
 * <p>
 * Please pay attention to the tread safety of this class, because there are
 * many threads using one and the same instance.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 15.05.2013 - Created
 * 
 */
public class ServerMediaPoolCommand
{
	private final Context context;
	private final String mediaResourceIdentifier;
	private final String mediaFilePendingName;
	private final Date requestDate;

	/**
	 * Constructor
	 */
	public ServerMediaPoolCommand(Context context, String mediaResourceIdentifier, String mediaFileName)
	{
		this.context = context;
		this.mediaResourceIdentifier = mediaResourceIdentifier;
		this.mediaFilePendingName = mediaFileName;
		this.requestDate = new Date();
	}

	/**
	 * Getter
	 */
	public Context getContext()
	{
		return context;
	}

	/**
	 * Getter
	 */
	public String getMediaResourceIdentifier()
	{
		return mediaResourceIdentifier;
	}

	/**
	 * Getter
	 */
	public String getMediaFilePendingName()
	{
		return mediaFilePendingName;
	}

	/**
	 * Getter
	 */
	public Date getRequestDate()
	{
		return requestDate;
	}
}
