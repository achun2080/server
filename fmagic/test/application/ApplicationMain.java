package fmagic.test.application;

import java.security.KeyPair;

import org.apache.commons.codec.binary.Base64;

import fmagic.basic.command.EncodingHandler;
import fmagic.test.suite.TestSuite;
import fmagic.test.suite.TestSuiteMedia;

public class ApplicationMain
{

	/**
	 * main() function for invoking all tests
	 */
	public static void main(String[] args)
	{
		try
		{
//			TestSuite testSuite = new TestSuiteComplete();
			TestSuite testSuite = new TestSuiteMedia();

			testSuite.execute();
			
			String errorProtocol = testSuite.printAssertionErrorProtocol();
			if (errorProtocol != null) System.err.println(errorProtocol);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Create keys
	 */
	public void createKeys()
	{
		EncodingHandler encodingHandler = new EncodingHandler();
		KeyPair keyPair = encodingHandler.getPublicPrivateKeyPair();

		System.out.println(Base64.encodeBase64String(keyPair.getPublic().getEncoded()));
		System.out.println(Base64.encodeBase64String(keyPair.getPrivate().getEncoded()));
	}

}
