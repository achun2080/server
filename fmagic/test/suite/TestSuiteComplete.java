package fmagic.test.suite;

import fmagic.test.runner.TestRunnerMediaClient;
import fmagic.test.runner.TestRunnerMediaCommand;
import fmagic.test.runner.TestRunnerMediaPool;
import fmagic.test.runner.TestRunnerMediaServer;


/**
 * This class summarizes all tests of the system.
 * 
 * @author F.Wuensche (FW)
 * 
 * @changed AG 26.04.2013 - Created
 * 
 */
public class TestSuiteComplete extends TestSuite
{
	/**
	 * Constructor
	 */
	public TestSuiteComplete()
	{
		super();
	}

	@Override
	protected void toBeExecuted()
	{
		try
		{
			this.addTestRunner(new TestRunnerMediaServer(this, "server"));
			this.addTestRunner(new TestRunnerMediaClient(this, "client"));
			this.addTestRunner(new TestRunnerMediaCommand(this, "command"));
			this.addTestRunner(new TestRunnerMediaPool(this, "pool"));

			this.executeComponentTests();
			this.executeConcurrentTests();
			this.executeStressTests();
			this.executeIntegerationTests();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
