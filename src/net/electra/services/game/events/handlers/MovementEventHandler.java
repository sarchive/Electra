package net.electra.services.game.events.handlers;

import net.electra.events.EventHandler;
import net.electra.services.game.entities.PositionQueue;
import net.electra.services.game.entities.players.Player;
import net.electra.services.game.events.MovementEvent;

public class MovementEventHandler extends EventHandler<MovementEvent, Player>
{
	@Override
	public void handle(MovementEvent event, Player player)
	{
		PositionQueue queue = (PositionQueue)player.position();
		queue.reset();
		
		for (int i = 0; i < event.steps().length; i++)
		{
			queue.queuePath(event.steps()[i]);
		}
		
		queue.runPath(event.running());
	}
}
