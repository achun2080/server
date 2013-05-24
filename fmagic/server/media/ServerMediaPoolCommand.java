package fmagic.server.media;

import java.util.Date;

import fmagic.basic.context.Context;

/**
 * This class implements a container used for media pool commands to be executed
 * on media pool.
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
	private final String dataIdentifier;
	private final Date requestDate;
	private final String commandHashKey;

	/**
	 * Constructor
	 */
	public ServerMediaPoolCommand(Context context,
			String mediaResourceIdentifier, String mediaFilePendingName,
			String dataIdentifier)
	{
		this.context = context;
		this.mediaResourceIdentifier = mediaResourceIdentifier;
		this.mediaFilePendingName = mediaFilePendingName;
		this.dataIdentifier = dataIdentifier;
		this.requestDate = new Date();

		if (mediaResourceIdentifier != null && dataIdentifier != null)
		{
			this.commandHashKey = mediaResourceIdentifier.trim() + ":" + dataIdentifier.trim();
		}
		else
		{
			this.commandHashKey = null;
		}
	}

	/**
	 * Getter
	 */
	public Context getContext()
	{
		return this.context;
	}

	/**
	 * Getter
	 */
	public String getMediaResourceIdentifier()
	{
		return this.mediaResourceIdentifier;
	}

	/**
	 * Getter
	 */
	public String getMediaFilePendingName()
	{
		return this.mediaFilePendingName;
	}

	/**
	 * Getter
	 */
	public Date getRequestDate()
	{
		return this.requestDate;
	}

	/**
	 * Getter
	 */
	public String getDataIdentifier()
	{
		return this.dataIdentifier;
	}

	/**
	 * Getter
	 */
	public String getCommandHashKey()
	{
		return this.commandHashKey;
	}

	@Override
	public String toString()
	{
		String outputString = "";

		// Headline
		outputString += "\n[\n+++ Media Pool Command" + "\n";

		// Common value
		outputString += "----------" + "\n";
		if (this.mediaResourceIdentifier != null) outputString += "Media resource identifier: '" + this.mediaResourceIdentifier + "'\n";
		if (this.mediaFilePendingName != null) outputString += "Media file pending name: '" + this.mediaFilePendingName + "'\n";
		if (this.dataIdentifier != null) outputString += "Data identifier: '" + this.dataIdentifier + "'\n";
		if (this.commandHashKey != null) outputString += "Command hash key: '" + this.commandHashKey + "'\n";
		outputString += "----------" + "\n";

		// End of output string
		outputString += "\n]\n";

		// Return
		return outputString;
	}
}
