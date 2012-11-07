package net.electra.services.game.events.handlers;

import net.electra.events.EventHandler;
import net.electra.net.DisconnectReason;
import net.electra.services.game.entities.players.Player;
import net.electra.services.game.events.ButtonClickEvent;
import net.electra.services.game.events.LogoutEvent;

public class ButtonClickEventHandler extends EventHandler<ButtonClickEvent, Player>
{
	@Override
	public void handle(ButtonClickEvent event, Player player)
	{
		switch (event.button())
		{
			case 2458:
				player.client().write(new LogoutEvent());
				player.client().disconnect(DisconnectReason.LOGGED_OUT);
				break;
		}
	}
}
