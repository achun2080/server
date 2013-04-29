package fmagic.test;

/**
 * This class executes tests regarding media.
 * 
 * @author F.Wuensche (FW)
 * 
 * @changed AG 26.04.2013 - Created
 * 
 */
public class TestSuiteMedia extends TestSuite
{
	/**
	 * Constructor
	 */
	public TestSuiteMedia()
	{
		super();
	}

	@Override
	protected void toBeExecuted()
	{
		try
		{
			TestRunnerMediaClient testRunner = new TestRunnerMediaClient(this, "aaaaaaaaaa");

			testRunner.setup();

			TestContainerMedia testContainer = new TestContainerMedia();

			// testContainer.setParameterResourceGroup("Apartment");
			// testContainer.setParameterResourceName("Room");
			// testContainer.setParameterDataIdentifierTestUpload("5678");
			// testContainer.setParameterDataIdentifierTestObsolete("5679");
			// testContainer.setParameterTestCycleNumberOfFiles(100);
			// testContainer.setParameterTestCycleDataIdentifierFrom(1);
			// testContainer.setParameterTestCycleDataIdentifierToo(40);
			//
			// testRunner.executeComponentTest();
			// testRunner.executeSingleFunctionTest(testContainer,
			// "testExpiredPendingFiles");
			// testRunner.executeSingleFunctionTest(testContainer,
			// "testExpiredDeletedFiles");
			// testRunner.executeSingleFunctionTest(testContainer,
			// "testExpiredObsoleteFiles");
			// testRunner.executeSingleFunctionTest(testContainer,
			// "testCleaningAll");

			testRunner.executeSingleFunctionTest(testContainer, "xxxxxxxxxx");

			testRunner.cleanup();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
