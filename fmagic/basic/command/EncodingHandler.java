package fmagic.basic.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;

import fmagic.basic.context.Context;
import fmagic.basic.notification.NotificationManager;
import fmagic.basic.resource.ResourceManager;

/**
 * This class contains functions to encode and decode request containers (client
 * to server) and response containers (server to client).
 * <p>
 * The default work flow for encoding a container is:
 * <OL>
 * <LI>Convert the class object to a JSON string.</LI>
 * <LI>ZIP the JSON string to a BASE64 string.</LI>
 * <LI>ENCRYPT the result string to a BASE64 string, using a public key.</LI>
 * </OL>
 * Only the first step is mandatory.
 * <p>
 * A JSON string will be ZIPPED only if it is longer than a specific value,
 * because otherwise the resulting string could be longer than the ZIPPED
 * string.
 * <p>
 * Each step is notified by a single character (ENCODING_CODE) that is used as a
 * prefix of an encoded string:
 * <UL>
 * <LI>1 = JSON format</LI>
 * <LI>2 = ZIPPED string</LI>
 * <LI>3 = ENCRYPTED string</LI>
 * </UL>
 * 
 * <p>
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.11.2012 - Created
 * 
 */
public class EncodingHandler
{
	// Encoding codes: If you analyze a command string you can see according to
	// the first character (ENCODING_CODE) if it is converted to JSON, or
	// ZIPPED, or ENCRYPTED.
	final private String ENCODING_CODE_PLAIN_JSON_ONLY = "1";
	final private String ENCODING_CODE_ZIPPED = "2";
	final private String ENCODING_CODE_CRYPTED = "3";

	// CommandManager strings were only be ZIPPED if they are longer than this value.
	final private int ZIPPING_MINIMUM_LENGTH_OF_STRING = 600;

	/**
	 * Constructor
	 */
	public EncodingHandler()
	{
	}

	/**
	 * Convert a client request container (class <TT>RequestContainer</TT>) to
	 * JSON.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param container
	 *            The request container to process.
	 * 
	 * @return Returns the JSON code of the request container, or <TT>null</TT>
	 *         if an error occurred.
	 * 
	 */
	private String convertRequestContainerToJson(Context context, RequestContainer container)
	{
		String commandJson = null;
		Gson gson = new Gson();

		try
		{
			commandJson = gson.toJson(container);
			commandJson = ENCODING_CODE_PLAIN_JSON_ONLY + commandJson;
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, "JSON command STRING (" + commandJson.length() + ")\n[\n" + commandJson.substring(0, Math.min(commandJson.length() - 1, 1000)) + "\n]");
		}
		catch (Exception e)
		{
			String errorString = "--> on converting to JSON";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Encoding", "ErrorOnEncodingCommandContainer"), errorString, e);
		}

		return commandJson;
	}

	/**
	 * Convert a server response container (class <TT>ResponseContainer</TT>) to
	 * JSON.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param container
	 *            The request container to process.
	 * 
	 * @return Returns the JSON code of the request container, or <TT>null</TT>
	 *         if an error occurred.
	 * 
	 */
	private String convertResponseContainerToJson(Context context, ResponseContainer container)
	{
		String commandJson = null;
		Gson gson = new Gson();

		try
		{
			commandJson = gson.toJson(container);
			commandJson = ENCODING_CODE_PLAIN_JSON_ONLY + commandJson;

			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, "JSON command STRING (" + commandJson.length() + ")\n[\n" + commandJson.substring(0, Math.min(commandJson.length() - 1, 1000)) + "\n]");
		}
		catch (Exception e)
		{
			String errorString = "--> on converting to JSON";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Encoding", "ErrorOnEncodingCommandContainer"), errorString, e);
		}

		return commandJson;
	}

	/**
	 * ZIPPING a command string.
	 * 
	 * @see Web: <a href= "http://www.javaworld.com/community/node/8362" >Using
	 *      Java UTIL ZIP API tutorial</a>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param commandToZip
	 *            The string to be ZIPPED. If the string is shorter than the
	 *            minimum length <TT>ZIPPING_MINIMUM_LENGTH_OF_STRING</TT> the
	 *            string will not be ZIPPED.
	 * 
	 * @param doZipping
	 *            Switcher if the string has to be ZIPPED. If this value is set
	 *            to <TT>false</TT> the string will not be ZIPPED.
	 * 
	 * @return Returns the ZIPPED code of the command string, or the original
	 *         string, or <TT>null</TT> if an error occurred.
	 * 
	 */
	private String convertZip(Context context, String commandToZip, boolean doZipping)
	{
		// NO ZIPPING
		if (doZipping == false) return commandToZip;

		// String is not long enough for ZIPPING
		if (commandToZip.length() < ZIPPING_MINIMUM_LENGTH_OF_STRING) return commandToZip;

		// START ZIPPING
		String commandZipped = null;

		try
		{
			// Input stream
			ByteArrayInputStream inputStreamToZip = new ByteArrayInputStream(commandToZip.getBytes());
			int nuOfBytes = inputStreamToZip.available();
			byte[] buffer = new byte[nuOfBytes];
			inputStreamToZip.read(buffer, 0, nuOfBytes);

			// Output stream
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ZipOutputStream zippedOutputStream = new ZipOutputStream(byteArrayOutputStream);

			// New ZIP entry
			ZipEntry zipEntry = new ZipEntry("command");
			zippedOutputStream.putNextEntry(zipEntry);

			// Process ZIPPING
			zippedOutputStream.write(buffer, 0, buffer.length);

			// Close ZIP entry
			zippedOutputStream.closeEntry();
			zippedOutputStream.finish();

			// Close all streams
			inputStreamToZip.close();
			zippedOutputStream.close();

			// Get ZIP code as a BASE64 string
			commandZipped = Base64.encodeBase64String(byteArrayOutputStream.toByteArray());
			commandZipped = ENCODING_CODE_ZIPPED + commandZipped;

			// Logging
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, "ZIPPED command BASE64 (" + commandZipped.length() + ")\n[\n" + commandZipped.substring(0, Math.min(commandZipped.length() - 1, 20)) + "\n]");
		}
		catch (Exception e)
		{
			String errorString = "--> on ZIPPING the container";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Encoding", "ErrorOnEncodingCommandContainer"), errorString, e);
		}

		// Return
		return commandZipped;
	}

	/**
	 * ENCRYPTING a command string.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param commandToEncrypt
	 *            The string to ENCRYPT.
	 * 
	 * @param doEncrypting
	 *            Switcher if the string has to be ENCRYPTED. If this value is
	 *            set to <TT>false</TT> the string will not be ENCRYPTED.
	 * 
	 * @return Returns the ENCRYPTED code of the command string, or the original
	 *         string, or <TT>null</TT> if an error occurred.
	 * 
	 * @see Web: <a href=
	 *      "http://codeartisan.blogspot.de/2009/05/public-key-cryptography-in-java.html"
	 *      >Public Key Cryptography in Java</a>
	 * 
	 * @see Web: <a href= "http://tspycher.com/2012/10/rsa-encryption-in-java/"
	 *      >RSA Encryption in Java</a>
	 */
	private String convertEncrypt(Context context, String commandToEncrypt, boolean doEncrypting, String publicKeyBase64String)
	{
		// NO ENCRYPTING if the switcher is set to false
		if (doEncrypting == false) return commandToEncrypt;

		// NO ENCRYPTING if the public key is not set
		if (publicKeyBase64String == null || publicKeyBase64String.length() == 0) return commandToEncrypt;

		// START ENCRYPTING
		String commandEncrypted = null;

		try
		{
			// Prepare public key
			byte[] publicKeyByte = Base64.decodeBase64(publicKeyBase64String);
			X509EncodedKeySpec keySpecification = new X509EncodedKeySpec(publicKeyByte);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = keyFactory.generatePublic(keySpecification);

			// Encrypt command
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] commandEncryptedByteArray = cipher.doFinal(commandToEncrypt.getBytes());

			// Set result string
			commandEncrypted = Base64.encodeBase64String(commandEncryptedByteArray);
			commandEncrypted = ENCODING_CODE_CRYPTED + commandEncrypted;

			// Logging
			context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, "ENCRYPTED command BASE64 (" + commandEncrypted.length() + ")\n[\n" + commandEncrypted.substring(0, Math.min(commandEncrypted.length() - 1, 20)) + "\n]");
		}
		catch (Exception e)
		{
			String errorString = "--> on ENCRYPTING the container";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Encoding", "ErrorOnEncodingCommandContainer"), errorString, e);
		}

		return commandEncrypted;
	}

	/**
	 * DECRYPTING a command string.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param commandToDecryptParameter
	 *            The string to DECRYPT.
	 * 
	 * @return Returns the DECRYPTED code of the request container, or
	 *         <TT>null</TT> if an error occurred.
	 * 
	 * @see Web: <a href=
	 *      "http://codeartisan.blogspot.de/2009/05/public-key-cryptography-in-java.html"
	 *      >Public Key Cryptography in Java</a>
	 * 
	 * @see Web: <a href= "http://tspycher.com/2012/10/rsa-encryption-in-java/"
	 *      >RSA Encryption in Java</a>
	 */
	private String convertDecrypt(Context context, String commandToDecryptParameter, String privateKeyBase64String)
	{
		// Validate parameter
		if (commandToDecryptParameter == null) return null;

		// DECRYPT command
		String commandDecrypted = null;

		try
		{
			// DECRYPT the command string
			if (commandToDecryptParameter.startsWith(ENCODING_CODE_CRYPTED))
			{
				// Clear first character
				String commandToDecrypt = commandToDecryptParameter.substring(1);

				// Prepare private key
				byte[] privateKeyByte = Base64.decodeBase64(privateKeyBase64String);
				PKCS8EncodedKeySpec keySpecification = new PKCS8EncodedKeySpec(privateKeyByte);
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				PrivateKey privateKey = keyFactory.generatePrivate(keySpecification);

				// Decrypt command
				Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.DECRYPT_MODE, privateKey);
				byte[] commandToDecryptByteArray = cipher.doFinal(commandToDecrypt.getBytes());

				// Set result string
				commandDecrypted = Base64.encodeBase64String(commandToDecryptByteArray);

				// Logging
				context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, "DECRYPTED BASE64 command (" + commandDecrypted.length() + ")\n[\n" + commandDecrypted.substring(0, Math.min(commandDecrypted.length() - 1, 20)) + "\n]");
			}
			// Copy the command string only, because it is NOT encrypted
			else
			{
				commandDecrypted = commandToDecryptParameter;
			}
		}
		catch (Exception e)
		{
			String errorString = "--> on DECRYPTING the container";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Encoding", "ErrorOnEncodingCommandContainer"), errorString, e);
		}

		return commandDecrypted;
	}

	/**
	 * UNZIPPING a command string.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param commandToUnzipParameter
	 *            The string to UNZIP.
	 * 
	 * @return Returns the UNZIPPED string of the command string, or the
	 *         original string, or <TT>null</TT> if an error occurred.
	 * 
	 */
	private String convertUnzip(Context context, String commandToUnzipParameter)
	{
		String commandUnzipped = null;

		try
		{
			// First: Check if the string is really ZIPPED
			if (commandToUnzipParameter.startsWith(ENCODING_CODE_ZIPPED))
			{
				// Cut first character from input string
				String commandToUnzip = commandToUnzipParameter.substring(1);

				// Create input stream
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.decodeBase64(commandToUnzip));
				ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);

				// Create output stream
				ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();

				// UNZIP the string
				byte buffer[] = new byte[4096];
				int bytesRead;

				@SuppressWarnings("unused")
				ZipEntry entry;

				while ((entry = zipInputStream.getNextEntry()) != null)
				{
					while (true)
					{
						bytesRead = zipInputStream.read(buffer, 0, buffer.length);

						if (bytesRead <= 0) break;

						zipOutputStream.write(buffer, 0, bytesRead);
					}
				}

				// Get UNZIPPED value of the string
				zipOutputStream.flush();
				commandUnzipped = zipOutputStream.toString();

				// Close all streams
				zipInputStream.close();
				zipOutputStream.close();

				// Logging
				context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, "UNZIPPED STRING command (" + commandUnzipped.length() + ")\n[\n" + commandUnzipped.substring(0, Math.min(commandUnzipped.length() - 1, 20)) + "\n]");
			}
			// Copy the command string only, because it is NOT ZIPPED
			else
			{
				commandUnzipped = commandToUnzipParameter;
			}
		}
		catch (Exception e)
		{
			String errorString = "--> on UNZIPPING the container";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Encoding", "ErrorOnEncodingCommandContainer"), errorString, e);
		}

		// Return
		return commandUnzipped;
	}

	/**
	 * Convert from JSON format to a client request container (class
	 * <TT>RequestContainer</TT>).
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param commandJson
	 *            The string to be converted to a request container.
	 * 
	 * @return Returns the request container object, or <TT>null</TT> if an
	 *         error occurred.
	 * 
	 */
	private RequestContainer convertJsonToRequestContainer(Context context, String commandJson)
	{
		RequestContainer container = null;
		Gson gson = new Gson();

		try
		{
			// CONVERT the command string
			if (commandJson.startsWith(ENCODING_CODE_PLAIN_JSON_ONLY))
			{
				container = gson.fromJson(commandJson.substring(1), RequestContainer.class);

				context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, "JSON command STRING (" + commandJson.length() + ")\n[\n" + commandJson.substring(0, Math.min(commandJson.length() - 1, 1000)) + "\n]");
			}
			// All commands have to be set to JSON
			else
			{
				String errorString = "--> on converting from JSON";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Encoding", "ErrorOnEncodingCommandContainer"), errorString, null);
			}
		}
		catch (Exception e)
		{
			String errorString = "--> on converting from JSON";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Encoding", "ErrorOnEncodingCommandContainer"), errorString, e);
		}

		return container;
	}

	/**
	 * Convert from JSON format to a server response container (class
	 * <TT>ResponseContainer</TT>) .
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param commandJson
	 *            The string to be converted to a request container.
	 * 
	 * @return Returns the request container object, or <TT>null</TT> if an
	 *         error occurred.
	 * 
	 */
	private ResponseContainer convertJsonToResponseContainer(Context context, String commandJson)
	{
		ResponseContainer container = null;
		Gson gson = new Gson();

		try
		{
			// CONVERT the command string
			if (commandJson.startsWith(ENCODING_CODE_PLAIN_JSON_ONLY))
			{
				container = gson.fromJson(commandJson.substring(1), ResponseContainer.class);

				context.getNotificationManager().notifyLogMessage(context, NotificationManager.SystemLogLevelEnum.CODE, "JSON command STRING (" + commandJson.length() + ")\n[\n" + commandJson.substring(0, Math.min(commandJson.length() - 1, 1000)) + "\n]");
			}
			// All commands have to be set to JSON
			else
			{
				String errorString = "--> on converting from JSON";
				context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Encoding", "ErrorOnEncodingCommandContainer"), errorString, null);
			}
		}
		catch (Exception e)
		{
			String errorString = "--> on converting from JSON";
			context.getNotificationManager().notifyError(context, ResourceManager.notification(context, "Encoding", "ErrorOnEncodingCommandContainer"), errorString, e);
		}

		return container;
	}

	/**
	 * Encode a client request container (class <TT>RequestContainer</TT>) .
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param container
	 *            The string to be converted to a request container.
	 * 
	 * @param doZipping
	 *            Flag if the command has to be zipped.
	 * 
	 * @param doEncrypting
	 *            Flag if the command has to be encrypted.
	 * 
	 * @return Returns the encrypted string, or <TT>null</TT> if an error
	 *         occurred.
	 * 
	 */
	public StringBuffer encodeRequestContainer(Context context, RequestContainer container, boolean doZipping, boolean doEncrypting, String serverPublicKey)
	{
		// Convert to JSON
		String commandJson = this.convertRequestContainerToJson(context, container);
		if (commandJson == null) return null;

		// ZIP command
		String commandZipped = this.convertZip(context, commandJson, doZipping);
		if (commandZipped == null) return null;

		// ENCRYPT command
		String commandEncrypted = this.convertEncrypt(context, commandZipped, doEncrypting, serverPublicKey);
		if (commandEncrypted == null) return null;

		// Return
		return new StringBuffer(commandEncrypted);
	}

	/**
	 * Decode a server response container (class <TT>ResponseContainer</TT>) .
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param commandToDecrypt
	 *            The string to be converted to a response container.
	 * 
	 * @return Returns the resulting <TT>ResponseContainer</TT> object, or
	 *         <TT>null</TT> if an error occurred.
	 * 
	 */
	public ResponseContainer decodeResponseContainer(Context context, String commandToDecrypt, String clientPrivateKey)
	{
		String commandDecrypted = this.convertDecrypt(context, commandToDecrypt, clientPrivateKey);
		if (commandDecrypted == null) return null;

		String commandUnzipped = this.convertUnzip(context, commandDecrypted);
		if (commandUnzipped == null) return null;

		ResponseContainer responseContainer = this.convertJsonToResponseContainer(context, commandUnzipped);
		if (responseContainer == null) return null;

		return responseContainer;
	}

	/**
	 * Decode a client request container (class <TT>RequestContainer</TT>) .
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param commandToDecrypt
	 *            The string to be converted to a request container.
	 * 
	 * @return Returns the resulting <TT>RequestContainer</TT> object, or
	 *         <TT>null</TT> if an error occurred.
	 * 
	 */
	public RequestContainer decodeRequestContainer(Context context, String commandToDecrypt, String serverPrivateKey)
	{
		String commandDecrypted = this.convertDecrypt(context, commandToDecrypt, serverPrivateKey);
		if (commandDecrypted == null) return null;

		String commandUnzipped = this.convertUnzip(context, commandDecrypted);
		if (commandUnzipped == null) return null;

		RequestContainer requestContainer = this.convertJsonToRequestContainer(context, commandUnzipped);
		if (requestContainer == null) return null;

		return requestContainer;
	}

	/**
	 * Encode a server response container (class <TT>ResponseContainer</TT>) .
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param container
	 *            The string to be converted to a response container.
	 * 
	 * @param doZipping
	 *            Flag if the command has to be zipped.
	 * 
	 * @param doEncrypting
	 *            Flag if the command has to be encrypted.
	 * 
	 * @return Returns the encrypted string, or <TT>null</TT> if an error
	 *         occurred.
	 * 
	 */
	public StringBuffer encodeResponseContainer(Context context, ResponseContainer container, boolean doZipping, boolean doEncrypting, String clientPublicKey)
	{
		String commandJson = this.convertResponseContainerToJson(context, container);
		if (commandJson == null) return null;

		String commandZipped = this.convertZip(context, commandJson, doZipping);
		if (commandZipped == null) return null;

		String commandEncrypted = this.convertEncrypt(context, commandZipped, doEncrypting, clientPublicKey);
		if (commandEncrypted == null) return null;

		return new StringBuffer(commandEncrypted);
	}

	/**
	 * Encode a server response container (class <TT>ResponseContainer</TT>) .
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param container
	 *            The string to be converted to a response container.
	 * 
	 * @return Returns the encrypted string, or <TT>null</TT> if an error
	 *         occurred.
	 * 
	 */
	public StringBuffer encodeResponseContainer(Context context, ResponseContainer container, String clientPrivateKey)
	{
		return encodeResponseContainer(context, container, false, false, clientPrivateKey);
	}

	/**
	 * Encode a client request container (class <TT>RequestContainer</TT>) .
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param container
	 *            The string to be converted to a request container.
	 * 
	 * @return Returns the encrypted string, or <TT>null</TT> if an error
	 *         occurred.
	 * 
	 */
	public StringBuffer encodeRequestContainer(Context context, RequestContainer container, String serverPublicKey)
	{
		return encodeRequestContainer(context, container, false, false, serverPublicKey);
	}

	/**
	 * Create a key pair of private and public key.
	 * 
	 * @return Returns the created key pair, or <TT>null</TT> if an error
	 *         occurred.
	 * 
	 * @see Web: <a href=
	 *      "http://codeartisan.blogspot.de/2009/05/public-key-cryptography-in-java.html"
	 *      >Public Key Cryptography in Java</a>
	 */
	public KeyPair getPublicPrivateKeyPair()
	{
		KeyPairGenerator keyGen = null;
		KeyPair keyPair = null;

		try
		{
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
			keyPair = keyGen.genKeyPair();
		}
		catch (NoSuchAlgorithmException e)
		{
			// Be silent
			return null;
		}

		// PublicKey publicKey = kp.getPublic();
		// PrivateKey privateKey = kp.getPrivate();

		// Return
		return keyPair;
	}
}
