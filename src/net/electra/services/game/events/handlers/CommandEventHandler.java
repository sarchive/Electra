package net.electra.services.game.events.handlers;

import net.electra.events.EventHandler;
import net.electra.services.game.entities.players.Player;
import net.electra.services.game.events.CommandEvent;
import net.electra.services.game.events.SendMessageEvent;

public class CommandEventHandler extends EventHandler<CommandEvent, Player>
{
	@Override
	public void handle(CommandEvent event, Player player)
	{
		player.client().write(new SendMessageEvent("Command: " + event.command()));
	}
}
