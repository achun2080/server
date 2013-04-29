package fmagic.client.command;

import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.application.ClientManager;

/**
 * COMMAND: Force the server to create a new session using the session
 * identifier the client set.
 * <p>
 * The function is the same as in the COMMAND <TT>ClientCommandHandshake</TT>.
 * Thats why it inherits this COMMAND class and does not modify anything.
 * <p>
 * You can find a difference on server side only: If the server finds this
 * COMMAND to execute, the test regarding the client session is switched off
 * automatically.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 23.12.2012 - Created
 */
public class ClientCommandCreateSession extends ClientCommandHandshake
{

	public ClientCommandCreateSession(Context context, ClientManager client)
	{
		super(context, client, ResourceManager.command(context, "Processing", "CreateSession").getRecourceIdentifier());
	}
}
