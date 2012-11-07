package net.electra.services.game.events.handlers;

import net.electra.events.EventHandler;
import net.electra.services.game.entities.players.Player;
import net.electra.services.game.events.LoadRegionEvent;
import net.electra.services.game.events.RegionInvalidatedEvent;

public class RegionInvalidatedEventHandler extends EventHandler<RegionInvalidatedEvent, Player>
{
	@Override
	public void handle(RegionInvalidatedEvent event, Player player)
	{
		player.position().refresh();
		player.client().write(new LoadRegionEvent((short)(player.position().regionX() + 6), (short)(player.position().regionY() + 6)));
	}
}
