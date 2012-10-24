package net.electra.events;

public abstract class EventHandler<T extends Event, C>
{
	public abstract void handle(T event, C context);
}
