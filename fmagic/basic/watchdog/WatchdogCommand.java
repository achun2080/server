package fmagic.basic.watchdog;

import java.util.Date;

import fmagic.basic.context.Context;


/**
 * This class implements a container used for WATCHDOG messages to be sent.
 * <p>
 * Please pay attention to the tread safety of this class, because there are
 * many threads using one and the same instance.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 14.01.2013 - Created
 * 
 */
public class WatchdogCommand
{
	private final Context context;
	private final String resourceIdentifier;
	private final String messageText;
	private final String additionalText;
	private final String resourceDocumentationText;
	private final String exceptionText;
	private final String contextIdentifier;
	private final Date messageDate;

	/**
	 * Constructor
	 */
	public WatchdogCommand(Context context, String resourceIdentifier, String messageText, String additionalText, String resourceDocumentationText, String exceptionText, Date messageDate)
	{
		this.context = context;
		this.resourceIdentifier = resourceIdentifier;
		this.messageText = messageText;
		this.additionalText = additionalText;
		this.resourceDocumentationText = resourceDocumentationText;
		this.exceptionText = exceptionText;
		this.contextIdentifier = context.getContextResourceContainer().getRecourceIdentifier();
		this.messageDate = messageDate;
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
	public String getResourceIdentifier()
	{
		return resourceIdentifier;
	}

	/**
	 * Getter
	 */
	public String getMessageText()
	{
		return messageText;
	}

	/**
	 * Getter
	 */
	public String getAdditionalText()
	{
		return additionalText;
	}

	/**
	 * Getter
	 */
	public String getResourceDocumentationText()
	{
		return resourceDocumentationText;
	}

	/**
	 * Getter
	 */
	public String getExceptionText()
	{
		return exceptionText;
	}

	/**
	 * Getter
	 */
	public String getContextIdentifier()
	{
		return contextIdentifier;
	}

	/**
	 * Getter
	 */
	public Date getMessageDate()
	{
		return messageDate;
	}
}
