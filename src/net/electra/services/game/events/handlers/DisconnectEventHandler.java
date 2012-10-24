package net.electra.services.game.events.handlers;

import net.electra.events.EventHandler;
import net.electra.net.ClientAdapter;
import net.electra.net.DisconnectReason;
import net.electra.net.events.DisconnectEvent;
import net.electra.services.game.entities.players.Player;

public class DisconnectEventHandler extends EventHandler<DisconnectEvent, ClientAdapter>
{
	@Override
	public void handle(DisconnectEvent event, ClientAdapter context)
	{
		if (context instanceof Player)
		{
			System.out.println("Player \"" + ((Player)context).username() + "\" disconnected: " + event.reason());
			
			if (event.reason() != DisconnectReason.RECONNECTION)
			{
				((Player)context).service().unregister((Player)context);
			}
		}
	}
}
