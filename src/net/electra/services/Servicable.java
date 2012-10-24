package net.electra.services;

import net.electra.events.Event;

// this is what our service is servicing, our servicee knows what it's service is.
// the servicee may not be a network client, so i don't make that assumption.
// anything servicable can be used as context for an event
public abstract class Servicable<T extends Service<?>>
{
	private final T service;
	
	public Servicable(T service)
	{
		this.service = service;
	}
	
	public <E extends Event> void fire(E event)
	{
		service.server().eventManager().fire(event, this);
	}
	
	public T service()
	{
		return service;
	}
}
