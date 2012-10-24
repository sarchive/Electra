package net.electra.services.login.events.handlers;

import net.electra.events.EventHandler;
import net.electra.net.ClientAdapter;
import net.electra.net.events.DisconnectEvent;
import net.electra.services.login.PotentialPlayer;

public class DisconnectEventHandler extends EventHandler<DisconnectEvent, ClientAdapter>
{
	@Override
	public void handle(DisconnectEvent event, ClientAdapter context)
	{
		if (context instanceof PotentialPlayer)
		{
			System.out.println("Potential player (" + context.client().socketChannel().socket() + ") disconnected: " + event.reason());
			((PotentialPlayer)context).service().unregister((PotentialPlayer)context);
		}
	}
}
