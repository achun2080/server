package fmagic.test;

import java.util.HashMap;

import fmagic.basic.Context;

public abstract class TestContainer implements Runnable
{
	// Organization
	private Context context;
	private final boolean concurrentAccess;
	private final TestRunner testRunner;
	
	// Error protocol
	private final HashMap<String, String> assertionErrorProtocol = new HashMap<String, String>();
	private int assertionNumberOfErrors = 0;

	/**
	 * Constructor
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
	public TestContainer(Context context, TestRunner testRunner,
			boolean concurrentAccess)
	{
		this.context = context;
		this.concurrentAccess = concurrentAccess;
		this.testRunner = testRunner;
	}

	/**
	 * Constructor
	 */
	public TestContainer()
	{
		this.context = null;
		this.concurrentAccess = false;
		this.testRunner = null;
	}

	/**
	 * Setup all resources for a component test.
	 */
	abstract public void setupComponentTest();

	/**
	 * Execute all tests of this component.
	 */
	abstract public void executeComponentTest();

	/**
	 * Cleanup all resources for a component test.
	 */
	abstract public void cleanupComponentTest();

	/**
	 * Getter
	 */
	public boolean isConcurrentAccess()
	{
		return concurrentAccess;
	}

	/**
	 * Getter
	 */
	public Context getContext()
	{
		return context;
	}

	/**
	 * Setter
	 */
	public void setContext(Context context)
	{
		this.context = context;
	}

	/**
	 * Getter
	 */
	public TestRunner getTestRunner()
	{
		return testRunner;
	}

	/**
	 * Getter
	 */
	public HashMap<String, String> getAssertionErrorProtocol()
	{
		return assertionErrorProtocol;
	}

	/**
	 * Getter
	 */
	public int getAssertionNumberOfErrors()
	{
		return assertionNumberOfErrors;
	}

	/**
	 * Setter
	 */
	public void increaseAssertionNumberOfErrors()
	{
		this.assertionNumberOfErrors++;
	}
}
