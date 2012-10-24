package net.electra.services.game.events.handlers;

import net.electra.Settings;
import net.electra.events.EventHandler;
import net.electra.services.game.entities.UpdateMask;
import net.electra.services.game.entities.players.Player;
import net.electra.services.game.events.PlayerInitializeEvent;
import net.electra.services.game.events.SendMessageEvent;

public class PlayerInitializeEventHandler extends EventHandler<PlayerInitializeEvent, Player>
{
	@Override
	public void handle(PlayerInitializeEvent event, Player player)
	{
		player.client().write(new SendMessageEvent("Welcome to " + Settings.SERVER_NAME + "."));
		player.mask().set(UpdateMask.PLAYER_APPEARANCE);
	}
}
