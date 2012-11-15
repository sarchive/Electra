package net.electra.services.game.events.handlers;

import net.electra.events.EventHandler;
import net.electra.services.game.entities.UpdateMask;
import net.electra.services.game.entities.players.Player;
import net.electra.services.game.events.ChatMessageEvent;

public class ChatMessageEventHandler extends EventHandler<ChatMessageEvent, Player>
{
	@Override
	public void handle(ChatMessageEvent event, Player context)
	{
		context.mask().add(UpdateMask.PLAYER_CHAT);
		context.message(event);
	}
}
