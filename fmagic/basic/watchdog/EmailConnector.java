package fmagic.basic.watchdog;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import fmagic.basic.context.Context;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceManager;


/**
 * This class implements a connection to a SMTP email server and provides
 * sending of EMAILs.
 * <p>
 * After creating an instance of this class you have to invoke the method
 * <TT>openConnection()</TT> to connect to the SMTP server. Please use
 * <TT>sendEmail()</TT> to send a single email including all CC and BCC
 * recipients. Please close the connection by invoking
 * <TT>closeConnection()</TT>.
 * 
 * @see Web: <a href=
 *      "http://www.tutorials.de/content/1177-e-mails-mit-javamail-versenden.html"
 *      >Using java email API tutorial</a>
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class EmailConnector
{
	// Context data
	private final Context context;

	// Connection data for email server
	private String smtpHost = "";
	private int smtpPort = -1;
	private String accountName = "";
	private String accountPassword = "";

	// Connection to email server via session
	Session session = null;
	Transport transport = null;

	// Connection status
	private Boolean isConnected = false;

	/**
	 * Constructor
	 */
	public EmailConnector(Context context)
	{
		this.context = context;
	}

	/**
	 * Opens a connection to a SMTP email server.
	 * 
	 * @param smtpHost
	 *            Name or IP address of SMTP host.
	 * 
	 * @param smtpPort
	 *            Port number of SMTP host.
	 * 
	 * @param accountName
	 *            Account name for login.
	 * 
	 * @param accountPassword
	 *            Account password for login.
	 */
	public boolean openConnection(String smtpHost, int smtpPort, String accountName, String accountPassword)
	{
		/*
		 * Closes connection to ensure an initialized state.
		 */
		this.closeConnection();

		/*
		 * Opens connection
		 */
		this.smtpHost = smtpHost.trim();
		this.smtpPort = smtpPort;
		this.accountName = accountName.trim();
		this.accountPassword = accountPassword.trim();

		// Using
		Properties parameterSettings = new Properties();
		parameterSettings.put("mail.smtp.host", this.smtpHost);
		parameterSettings.put("mail.smtp.socketFactory.port", String.valueOf(this.smtpPort));
		parameterSettings.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		parameterSettings.put("mail.smtp.auth", "true");
		parameterSettings.put("mail.smtp.port", String.valueOf(this.smtpPort));
		parameterSettings.put("mail.smtp.timeout", "10000");
		parameterSettings.put("mail.smtp.connectiontimeout", "10000");

		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Trying to connect to SMTP server.");
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Smtp host: '" + this.smtpHost + "', Smtp port: '" + Integer.toString(this.smtpPort) + "'");

		try
		{
			if (this.session == null && this.transport == null)
			{
				// Open session to email server
				this.session = Session.getInstance(parameterSettings);

				// Set transport object to "smtp" protocol
				this.transport = session.getTransport("smtp");

				// Open connection
				transport.connect(this.accountName, this.accountPassword);
			}
		}
		catch (Exception e)
		{
			String errorString = "--> On opening SMTP connection: Host [" + smtpHost + "], Port [" + String.valueOf(smtpPort) + "], Account name [" + accountName + "], Password [******]";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Email", "ErrorOnConnectionToSmtpServer"), errorString, e);
			this.closeConnection();
			return false;
		}

		// Set connection status
		this.isConnected = true;

		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Connection to SMTP server opened.");

		// OK
		return true;
	}

	/**
	 * Closes a connection to a SMTP email server.
	 */
	public void closeConnection()
	{
		// Set connection status
		this.isConnected = false;

		// Close connection
		try
		{
			// Logging
			if (this.transport != null || this.session != null)
			{
				context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Connection to SMTP server closed.");
				context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Smtp host: '" + this.smtpHost + "', Smtp port: '" + Integer.toString(this.smtpPort) + "'");
			}

			// Closes all
			if (this.transport != null)
			{
				this.transport.close();
			}

			if (this.session != null)
			{
				this.session = null;
			}
		}
		catch (Exception e)
		{
			String errorString = "--> on closing the connection";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Email", "ErrorOnConnectionToSmtpServer"), errorString, e);
		}
		finally
		{
			this.session = null;
			this.transport = null;
		}
	}

	/**
	 * GETTER
	 */
	public Boolean getIsConnected()
	{
		return isConnected;
	}

	/**
	 * Opens a connection to a SMTP email server.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param emailAddress
	 *            Recipient of email.
	 * 
	 * @param cc
	 *            Recipient of carbon copy CC or empty.
	 * 
	 * @param bcc
	 *            Recipient of carbon copy BCC or empty.
	 * 
	 * @param returnAddress
	 *            Return address of email.
	 * 
	 * @param subjectText
	 *            Text to use as subject text of email.
	 * 
	 * @param bodyText
	 *            Text to use as body text of email.
	 * 
	 * @return <b>boolean</b> Returns <TT>true</TT> if the setting was
	 *         successful, otherwise<TT>false</TT>.
	 */
	public boolean sendEmail(Context context, String emailAddress, String cc, String bcc, String returnAddress, String subjectText, String bodyText)
	{
		// Checks parameter
		boolean isError = false;
		String errorText = "--> The following parameters are missing:";

		if (emailAddress == null || emailAddress.equals(""))
		{
			errorText += "\n--> [Email address] Email address (recipient) is not set.";
			isError = true;
		}

		if (returnAddress == null || returnAddress.equals(""))
		{
			errorText += "\n--> [Return address] Return address is not set.";
			isError = true;
		}

		if (subjectText == null || subjectText.equals(""))
		{
			errorText += "\n--> [Subject Text] Subject text is not set.";
			isError = true;
		}

		if (bodyText == null || bodyText.equals("")) bodyText = subjectText;

		if (isError == true)
		{
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Email", "EmailParameterMissing"), errorText, null);
			return false;
		}

		// Prepares and sends email
		try
		{
			if (this.session != null && this.transport != null)
			{
				Message message;

				/**
				 * Sends email to recipient directly
				 */

				// Creates new MIME message
				message = new MimeMessage(this.session);

				// Sets recipients and other addresses
				message.setFrom(new InternetAddress(returnAddress));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailAddress));
				if (!cc.equals("")) message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
				message.setReplyTo(InternetAddress.parse(returnAddress));

				// Sets subject and body
				message.setSubject(subjectText);
				message.setText(bodyText);

				// Sends email
				this.transport.sendMessage(message, InternetAddress.parse(emailAddress));

				/**
				 * Sends email to CC (Carbon copy)
				 */
				if (!cc.equals(""))
				{
					// Creates new MIME message
					message = new MimeMessage(this.session);

					// Sets recipients and other addresses
					message.setFrom(new InternetAddress(returnAddress));
					message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(cc));
					if (!cc.equals("")) message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(emailAddress));
					message.setReplyTo(InternetAddress.parse(returnAddress));

					// Sets subject and body
					message.setSubject(subjectText);
					message.setText("text/plain\n" + bodyText);

					// Sends email
					this.transport.sendMessage(message, InternetAddress.parse(cc));
				}

				/**
				 * Sends email to BCC (Blind carbon copy)
				 */
				if (!bcc.equals(""))
				{
					// Creates new MIME message
					message = new MimeMessage(this.session);

					// Sets recipients and other addresses
					message.setFrom(new InternetAddress(returnAddress));
					message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(bcc));
					if (!cc.equals("")) message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc + ", " + emailAddress));
					message.setReplyTo(InternetAddress.parse(returnAddress));

					// Sets subject and body
					message.setSubject(subjectText);
					message.setText(bodyText);

					// Sends email
					this.transport.sendMessage(message, InternetAddress.parse(bcc));
				}
			}
			else
			{
				String errorString = "--> On sendig an Email";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Email", "ErrorOnConnectionToSmtpServer"), errorString, null);
			}
		}
		catch (Exception e)
		{
			String errorString = "--> On sendig an Email";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Email", "ErrorOnConnectionToSmtpServer"), errorString, e);
		}

		// Logging
		context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.NOTICE, "Email was sent to: '" + emailAddress + "', CC: '" + cc + "', BCC: '" + bcc + "', Return address: '" + returnAddress + "', Subject text: '" + subjectText + "'");

		// Return
		return true;
	}
}
