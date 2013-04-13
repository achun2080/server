package fmagic.test;

import fmagic.basic.Context;

abstract public class ServerTestContainer implements Runnable
{
	private Context context;
	private final boolean concurrentAccess;

	/**
	 * Constructor
	 */
	public ServerTestContainer(Context context, boolean concurrentAccess)
	{
		this.context = context;
		this.concurrentAccess = concurrentAccess;
	}

	/**
	 * Constructor
	 */
	public ServerTestContainer()
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
