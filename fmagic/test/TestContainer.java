package fmagic.test;

import fmagic.basic.Context;

public abstract class TestContainer implements Runnable
{
	private Context context;
	private final boolean concurrentAccess;

	/**
	 * Constructor
	 */
	public TestContainer(Context context, boolean concurrentAccess)
	{
		this.context = context;
		this.concurrentAccess = concurrentAccess;
	}

	/**
	 * Constructor
	 */
	public TestContainer()
	{
		this.context = null;
		this.concurrentAccess = false;
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
}
