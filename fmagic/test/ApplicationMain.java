package fmagic.test;

import java.security.KeyPair;

import org.apache.commons.codec.binary.Base64;

import fmagic.basic.EncodingHandler;

public class ApplicationMain
{

	/**
	 * main() function for invoking all tests
	 */
	public static void main(String[] args)
	{
//		TestRunnerMediaClient testRunner = new TestRunnerMediaClient("aaaaaaaaaa");
//
//		testRunner.setup();
//
//		TestContainerMedia testContainer = new TestContainerMedia();
//		testContainer.setParameterResourceGroup("Apartment");
//		testContainer.setParameterResourceName("Room");
//		testContainer.setParameterDataIdentifierTestUpload("5678");
//		testContainer.setParameterDataIdentifierTestObsolete("5679");
//		testContainer.setParameterTestCycleNumberOfFiles(100);
//		testContainer.setParameterTestCycleDataIdentifierFrom(1);
//		testContainer.setParameterTestCycleDataIdentifierToo(40);
//
//		testRunner.executeComponentTest();
//		testRunner.executeSingleFunctionTest(testContainer, "testExpiredPendingFiles");
//		testRunner.executeSingleFunctionTest(testContainer, "testExpiredDeletedFiles");
//		testRunner.executeSingleFunctionTest(testContainer, "testExpiredObsoleteFiles");
//		testRunner.executeSingleFunctionTest(testContainer, "testCleaningAll");
//		testRunner.executeSingleFunctionTest(testContainer, "xxxxxxxxxx");
//
//		testRunner.cleanup();

		try
		{
			ApplicationTest testApplication = new ApplicationTest();

			testApplication.addTestRunner(new TestRunnerMediaServer("server-a-component"));
			testApplication.addTestRunner(new TestRunnerMediaServer("server-b-concurrent"));
			testApplication.addTestRunner(new TestRunnerMediaServer("server-c-stress"));
			testApplication.addTestRunner(new TestRunnerMediaServer("server-d-integeration"));

			testApplication.addTestRunner(new TestRunnerMediaClient("client-a-component"));
			testApplication.addTestRunner(new TestRunnerMediaClient("client-b-concurrent"));
			testApplication.addTestRunner(new TestRunnerMediaClient("client-c-stress"));
			testApplication.addTestRunner(new TestRunnerMediaClient("client-d-integeration"));

			testApplication.executeComponentTests();
			testApplication.executeConcurrentTests();
			testApplication.executeStressTests();
			testApplication.executeIntegerationTests();
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
