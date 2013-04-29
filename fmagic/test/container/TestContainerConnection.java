package fmagic.test.container;

import fmagic.basic.context.Context;
import fmagic.test.runner.TestRunner;

public class TestContainerConnection extends TestContainer
{
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
	public TestContainerConnection(Context context, TestRunner testRunner, boolean concurrentAccess)
	{
		super(context, testRunner, concurrentAccess);
	}

	private void testSetup()
	{
	}

	private void testCleanup()
	{
	}

	@Override
	public void run()
	{
		// Setup test environment
		this.testSetup();

		/*

		// Do something
		ClientCommand command;
		ResponseContainer responseContainer;

		// COMMAND Create Session 1
		command = new ClientCommandCreateSession(client.getContext(), client);
		responseContainer = command.execute();

		// COMMAND Handshake 1
		command = new ClientCommandHandshake(client.getContext(), client);
		responseContainer = command.execute();

		 */
		
		// Cleanup test environment
		this.testCleanup();
	}

	@Override
	public void setupComponentTest()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeComponentTest()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanupComponentTest()
	{
		// TODO Auto-generated method stub
		
	}
}
