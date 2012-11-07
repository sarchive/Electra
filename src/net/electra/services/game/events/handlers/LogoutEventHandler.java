package net.electra.services.game.events.handlers;

import net.electra.events.EventHandler;
import net.electra.services.game.entities.players.Player;
import net.electra.services.game.events.LogoutEvent;

public class LogoutEventHandler extends EventHandler<LogoutEvent, Player>
{
	@Override
	public void handle(LogoutEvent event, Player context)
	{
		// code to save player, etc.
	}
}
