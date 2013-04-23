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
		TestRunnerMedia testRunner = new TestRunnerMedia("aaaaaaaaaa");

		testRunner.setup();

		ServerTestContainerMedia testContainer = new ServerTestContainerMedia();
//		testContainer.setParameterResourceGroup("Apartment");
//		testContainer.setParameterResourceName("Room");
//		testContainer.setParameterDataIdentifierTestUpload("1234");
//		testContainer.setParameterDataIdentifierTestObsolete("1235");
//		testContainer.setParameterTestCycleNumberOfFiles(100);
//		testContainer.setParameterTestCycleDataIdentifierFrom(1);
//		testContainer.setParameterTestCycleDataIdentifierToo(40);
//
//		testRunner.executeComponentTest();
//		testRunner.executeSingleFunctionTest(testContainer, "testExpiredPendingFiles");
//		testRunner.executeSingleFunctionTest(testContainer, "testExpiredDeletedFiles");
//		testRunner.executeSingleFunctionTest(testContainer, "testExpiredObsoleteFiles");
//		testRunner.executeSingleFunctionTest(testContainer, "testCleaningAll");
		testRunner.executeSingleFunctionTest(testContainer, "xxxxxxxxxx");

		testRunner.cleanup();

		try
		{
//			ApplicationTest testA = new ApplicationTest();
//			testA.addTestRunner(new TestRunnerMedia("a-component"));
//			testA.executeComponentTests();
//
//			ApplicationTest testB = new ApplicationTest();
//			testB.addTestRunner(new TestRunnerMedia("b-concurrent"));
//			testB.executeConcurrentTests();
//
//			ApplicationTest testC = new ApplicationTest();
//			testC.addTestRunner(new TestRunnerMedia("c-stress"));
//			testC.executeStressTests();
//
//			ApplicationTest testD = new ApplicationTest();
//			testD.addTestRunner(new TestRunnerMedia("d-integeration"));
//			testD.executeIntegerationTests();
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