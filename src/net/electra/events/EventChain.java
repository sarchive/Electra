package net.electra.events;

public class EventChain<T extends Event, C> extends EventHandler<T, C>
{
	private EventHandler<T, C>[] handlers;
	
	public EventChain(EventHandler<T, C>[] handlers)
	{
		this.handlers = handlers;
	}
	
	@Override
	public void handle(T event, C context)
	{
		for (EventHandler<T, C> handler : handlers)
		{
			handler.handle(event, context);
			
			if (event.chainBroken())
			{
				break;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void register(EventHandler<T, C> handler)
	{
		EventHandler<T, C>[] old = handlers;
		handlers = (EventHandler<T, C>[])new EventHandler<?, ?>[old.length + 1];
		System.arraycopy(old, 0, handlers, 0, old.length);
		handlers[handlers.length - 1] = handler;
	}
	
	public int count()
	{
		return handlers.length;
	}
}
