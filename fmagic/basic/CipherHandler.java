package fmagic.basic;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * This class provides methods for encrypting and decrypting files. It uses the
 * "DES" (Data Encryption Standard) for ciphering. DES requires a key (password)
 * that is at least 8 characters long.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 05.04.2013 - Created
 * 
 */
public class CipherHandler
{
	private final Context context;

	/**
	 * Constructor
	 */
	public CipherHandler(Context context)
	{
		this.context = context;
	}

	/**
	 * Encrypt a file.
	 * 
	 * @param keyValue
	 *            The key (password) to use.
	 * 
	 * @param sourceFilePath
	 *            The file path of the file to be encrypted.
	 * 
	 * @param destinationFilePath
	 *            The file path of the file to be created as new.
	 * 
	 * @return Returns <TT>true</TT> if the file could be encrypted, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean encrypt(String keyValue, String sourceFilePath, String destinationFilePath)
	{
		return this.encryptOrDecrypt(keyValue, Cipher.ENCRYPT_MODE, sourceFilePath, destinationFilePath);
	}

	/**
	 * Decrypt a file.
	 * 
	 * @param keyValue
	 *            The key (password) to use.
	 * 
	 * @param sourceFilePath
	 *            The file path of the file to be decrypted.
	 * 
	 * @param destinationFilePath
	 *            The file path for the decrypted file to be created as new.
	 * 
	 * @return Returns <TT>true</TT> if the file could be decrypted, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean decrypt(String keyValue, String sourceFilePath, String destinationFilePath)
	{
		return this.encryptOrDecrypt(keyValue, Cipher.DECRYPT_MODE, sourceFilePath, destinationFilePath);
	}

	/**
	 * Process ciphering, encrypting or decrypting.
	 * 
	 * @param keyValue
	 *            The key (password) to use.
	 * 
	 * @param mode
	 *            Mode of ciphering, encrypting or decrypting.
	 * 
	 * @param sourceFilePath
	 *            The file path of the file to be decrypted.
	 * 
	 * @param destinationFilePath
	 *            The file path for the decrypted file to be created as new.
	 * 
	 * @return Returns <TT>true</TT> if the file could be processed, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean encryptOrDecrypt(String keyValue, int mode, String sourceFilePath, String destinationFilePath)
	{
		// Check parameters
		if (sourceFilePath == null || sourceFilePath.length() == 0) return false;
		if (destinationFilePath == null || destinationFilePath.length() == 0) return false;

		// Process ciphering
		try
		{
			FileInputStream fileInputStream = new FileInputStream(sourceFilePath);
			FileOutputStream fileOutputStream = new FileOutputStream(destinationFilePath);

			DESKeySpec dks = new DESKeySpec(keyValue.getBytes());
			SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
			SecretKey desKey = skf.generateSecret(dks);
			Cipher cipher = Cipher.getInstance("DES");

			if (mode == Cipher.ENCRYPT_MODE)
			{
				cipher.init(Cipher.ENCRYPT_MODE, desKey);
				CipherInputStream cipherInputStream = new CipherInputStream(fileInputStream, cipher);
				this.doCopy(cipherInputStream, fileOutputStream);
			}
			else if (mode == Cipher.DECRYPT_MODE)
			{
				cipher.init(Cipher.DECRYPT_MODE, desKey);
				CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, cipher);
				this.doCopy(fileInputStream, cipherOutputStream);
			}
		}
		catch (Exception e)
		{
			String errorString = "--> Error on encrypting or decrypting a file.";
			errorString += "\n--> Source file: '" + sourceFilePath + "'";
			errorString += "\n--> Destination file: '" + destinationFilePath + "'";

			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Cipher", "ErrorOnCiphering"), errorString, e);
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Copy file streams.
	 * 
	 * @param inputStream
	 *            The file input stream.
	 * 
	 * @param outputStream
	 *            The file output stream.
	 * 
	 * @return Returns <TT>true</TT> if the file could be processed, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean doCopy(InputStream inputStream, OutputStream outputStream)
	{
		byte[] bytes = new byte[64];
		int numBytes;

		try
		{
			while ((numBytes = inputStream.read(bytes)) != -1)
			{
				outputStream.write(bytes, 0, numBytes);
			}
			
			outputStream.flush();
			outputStream.close();
			inputStream.close();
		}
		catch (Exception e)
		{
			String errorString = "--> Error on encrypting or decrypting a file.";
			this.getContext().getNotificationManager().notifyError(this.getContext(), ResourceManager.notification(this.getContext(), "Cipher", "ErrorOnCiphering"), errorString, e);
			return false;
		}

		// Return
		return true;
	}

	/**
	 * Getter
	 */
	public Context getContext()
	{
		return context;
	}
}
