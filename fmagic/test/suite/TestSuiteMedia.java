package fmagic.test.suite;

import fmagic.test.runner.TestRunnerMediaClient;
import fmagic.test.runner.TestRunnerMediaCommand;
import fmagic.test.runner.TestRunnerMediaServer;

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
			this.addTestRunner(new TestRunnerMediaServer(this, "server"));
			this.addTestRunner(new TestRunnerMediaClient(this, "client"));
			this.addTestRunner(new TestRunnerMediaCommand(this, "command"));
			
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
