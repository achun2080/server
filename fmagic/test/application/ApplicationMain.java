package fmagic.test.application;

import java.security.KeyPair;

import org.apache.commons.codec.binary.Base64;

import fmagic.basic.command.EncodingHandler;
import fmagic.test.container.TestContainerMediaLocalServer;
import fmagic.test.container.TestContainerMediaPool;
import fmagic.test.runner.TestRunnerMediaCommand;
import fmagic.test.runner.TestRunnerMediaPool;
import fmagic.test.runner.TestRunnerMediaServer;
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

//			TestRunnerMediaPool testRunner = new TestRunnerMediaPool(null, "xxxxxxxxxx");
//			TestContainerMediaPool testContainer = new TestContainerMediaPool(testRunner);
//			
//			testRunner.setup();
//			testRunner.executeSingleFunctionTest(testContainer, "xxxxxxxxxx");
//			testRunner.cleanup();
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
