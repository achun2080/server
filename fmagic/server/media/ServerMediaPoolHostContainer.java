package fmagic.server.media;

/**
 * This class contains all data needed to define a host for a media pool.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 15.05.2013 - Created
 * 
 */
public class ServerMediaPoolHostContainer
{
	// Common data
	final private int number;
	final private String host;
	final private int port;

	/**
	 * Constructor
	 * 
	 * @param number
	 *            Number of the host in the lis.
	 * 
	 * @param host
	 *            Host name of the media server.
	 * 
	 * @param port
	 *            Port number of the media server.
	 */
	public ServerMediaPoolHostContainer(int number, String host, int port)
	{
		this.number = number;
		this.host = host;
		this.port = port;
	}

	/**
	 * Getter
	 */
	public int getNumber()
	{
		return number;
	}

	/**
	 * Getter
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Getter
	 */
	public int getPort()
	{
		return port;
	}
}
