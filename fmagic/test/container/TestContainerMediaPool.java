package fmagic.test.container;

import fmagic.basic.context.Context;
import fmagic.basic.media.MediaManager;
import fmagic.server.media.ServerMediaManager;
import fmagic.test.application.TestManager;
import fmagic.test.runner.TestRunner;

/**
 * This class implements testing functionality regarding the
 * <TT>Media Manager</TT> using a media pool.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 15.05.2013 - Created
 * 
 */
public class TestContainerMediaPool extends TestContainer
{
	/**
	 * Constructor 1
	 * 
	 * @param context
	 *            The application context.
	 * 
	 * @param testRunner
	 *            The test runner that holds this container, or <TT>null</TT> if
	 *            no test runner is available.
	 * 
	 * @param concurrentAccess
	 *            Set to <TT>true</TT> if the test container is supposed to run
	 *            in a concurrent environment with other parallel threads or
	 *            applications, otherwise to <TT>false</TT>.
	 */
	public TestContainerMediaPool(Context context, TestRunner testRunner,
			boolean concurrentAccess)
	{
		super(context, testRunner, concurrentAccess);
	}

	/**
	 * Constructor 2
	 * 
	 * @param testRunner
	 *            The test runner that holds this container, or <TT>null</TT> if
	 *            no test runner is available.
	 */
	public TestContainerMediaPool(TestRunner testRunner)
	{
		super(null, testRunner, false);
	}

	@Override
	public void executeComponentTest()
	{
		try
		{
			this.componentTestExecuteIntern();
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	@Override
	public void setupComponentTest()
	{
		try
		{
			this.setupComponentTestIntern();
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	@Override
	public void cleanupComponentTest()
	{
		try
		{
			this.cleanupComponentTestIntern();
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Component Test: Setup environment
	 */
	private void setupComponentTestIntern()
	{
		// Setup
		try
		{
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Component Test: Execute
	 */
	private void componentTestExecuteIntern()
	{
		try
		{
			// Setup
			this.setupComponentTestIntern();

			// Test

			// Cleanup
			this.cleanupComponentTestIntern();
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Component Test: Cleanup environment
	 */
	private void cleanupComponentTestIntern()
	{
		// Do nothing if the test is running in concurrent mode
		if (this.isConcurrentAccess()) return;

		// Cleanup
		try
		{
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	@Override
	public void run()
	{
		try
		{
			this.componentTestExecuteIntern();
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}

	/**
	 * Test: xxxxxxxxxx
	 */
	public void xxxxxxxxxx()
	{
		try
		{
			ServerMediaManager mediaManager = (ServerMediaManager) this.getContext().getMediaManager();
			
			System.out.println(mediaManager);
			
		}
		catch (Exception e)
		{
			TestManager.servicePrintException(this.getContext(), this, "Unexpected Exception", e);
		}
	}
}
