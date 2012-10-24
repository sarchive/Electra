package net.electra.services.game.events;

import net.electra.services.events.TickEvent;

public class PlayerTickEvent extends TickEvent
{
	public PlayerTickEvent(long tick)
	{
		super(tick);
	}
}
