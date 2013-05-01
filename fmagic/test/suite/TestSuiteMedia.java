package fmagic.test.suite;

import fmagic.test.container.TestContainerMediaLocal;
import fmagic.test.runner.TestRunnerMediaClient;
import fmagic.test.runner.TestRunnerMediaCommand;

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
			TestRunnerMediaCommand testRunner = new TestRunnerMediaCommand(this, "command");

			testRunner.setup();
			testRunner.executeComponentTest();
			testRunner.cleanup();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
