package net.electra.services.game.events.handlers;

import net.electra.events.EventHandler;
import net.electra.services.game.entities.players.Player;
import net.electra.services.game.events.CommandEvent;

public class CommandEventHandler extends EventHandler<CommandEvent, Player>
{
	@Override
	public void handle(CommandEvent event, Player player)
	{
		System.out.println("command: " + event.command());
	}
}
