package fmagic.test;

import fmagic.basic.Context;
import fmagic.basic.ResourceManager;
import fmagic.basic.ResponseContainer;
import fmagic.client.ClientCommand;
import fmagic.client.ClientCommandCreateSession;
import fmagic.client.ClientCommandHandshake;
import fmagic.client.ClientManager;

public class TestContainerConnection implements Runnable
{
	private final Context context;
	private final ClientManager client;

	/**
	 * Constructor
	 */
	public TestContainerConnection(ClientManager client, Context context)
	{
		this.client = client;
		this.context = context.createSilentDumpContext(ResourceManager.context(context, "Media", "Processing"));
		
	}

	private void testSetup()
	{
		if (this.client == null) return;
	}

	private void testCleanup()
	{
		if (this.client == null) return;
	}

	@Override
	public void run()
	{
		// Check if client is instantiated
		if (this.client == null) return;
		
		// Setup test environment
		this.testSetup();

		// Test

		// Do something
		ClientCommand command;
		ResponseContainer responseContainer;

		// COMMAND Create Session 1
		command = new ClientCommandCreateSession(client.getContext(), client);
		responseContainer = command.execute();

		// COMMAND Handshake 1
		command = new ClientCommandHandshake(client.getContext(), client);
		responseContainer = command.execute();

		// COMMAND Handshake 2
		// command = new ClientCommandHandshake(client.getContext(),
		// client);
		// responseContainer = command.execute();

		// COMMAND Create Session 2
		// command = new
		// ClientCommandCreateSession(client.getContext(),
		// client);
		// responseContainer = command.execute();
		
		// Cleanup test environment
		this.testCleanup();
	}
}
