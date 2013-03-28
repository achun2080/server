package fmagic.basic;

import java.util.Date;


/**
 * This class defines a container for sessions hold on the application server of
 * FMAGIC.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 22.12.2012 - Created
 */
public class SessionContainer
{
	// Session identifier of the client application
	final private String clientSessionIdentifier;

	// Public key of the client application
	final private String clientPublicKey;

	// Right group of the current user
	private ResourceContainer userRightGroup = null;

	// License key of the current user
	private String userLicenseKey = null;

	// Date of creation and modification
	final private Date creationDate;
	private Date lastModificationDate;

	/**
	 * Constructor
	 * 
	 * @param clientSessionIdentifier
	 *            Session identifier of the client application.
	 * 
	 * @param clientPublicKey
	 *            Public key of the client application.
	 */
	public SessionContainer(String clientSessionIdentifier, String clientPublicKey)
	{
		this.clientSessionIdentifier = clientSessionIdentifier;
		this.clientPublicKey = clientPublicKey;
		this.creationDate = new Date();
		this.lastModificationDate = new Date();
	}

	/**
	 * Getter
	 */
	public Date getLastModificationDate()
	{
		return lastModificationDate;
	}

	/**
	 * Setter
	 */
	public void setLastModificationDate(Date lastModificationDate)
	{
		this.lastModificationDate = lastModificationDate;
	}

	/**
	 * Getter
	 */
	public String getClientSessionIdentifier()
	{
		return clientSessionIdentifier;
	}

	/**
	 * Getter
	 */
	public String getClientPublicKey()
	{
		return clientPublicKey;
	}

	/**
	 * Getter
	 */
	public Date getCreationDate()
	{
		return creationDate;
	}

	/**
	 * Getter
	 */
	public ResourceContainer getUserRightGroup()
	{
		return userRightGroup;
	}

	/**
	 * Setter
	 */
	public void setUserRightGroup(ResourceContainer userRightGroup)
	{
		this.userRightGroup = userRightGroup;
	}

	/**
	 * Getter
	 */
	public String getUserLicenseKey()
	{
		return userLicenseKey;
	}

	/**
	 * Setter
	 */
	public void setUserLicenseKey(String userLicenseKey)
	{
		this.userLicenseKey = userLicenseKey;
	}	
}
