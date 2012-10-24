package net.electra.services.events;

import net.electra.events.Event;

public class TickEvent extends Event
{
	private final long currentTick;
	
	public TickEvent(long currentTick)
	{
		this.currentTick = currentTick;
	}
	
	public long tick()
	{
		return currentTick;
	}
}
