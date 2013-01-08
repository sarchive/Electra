package net.electra.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An {@link EventHandler} with the capability of handling multiple events of the same kind.
 * @author Supah Fly
 *
 * @param <T> The type of {@link Event}
 * @param <C> The type of the context.
 */
public class EventChain<T extends Event, C> extends EventHandler<T, C>
{
	private List<EventHandler<T, C>> handlers;
	
	/**
	 * Intializes an {@link EventChain} with an empty ArrayList<{@link T}, {@link C}>
	 */
	public EventChain()
	{
		this.handlers = new ArrayList<EventHandler<T, C>>();
	}
	
	/**
	 * Initializes an {@link EventChain} with an ArrayList<{@link T}, {@link C}> populated with the specified handlers.
	 * @param handlers The {@link EventHandler}s that belong to this {@link Event}'s ({@link T}) chain.
	 */
	public EventChain(EventHandler<T, C>[] handlers)
	{
		this.handlers = new ArrayList<EventHandler<T, C>>(Arrays.asList(handlers));
	}
	
	/**
	 * Handles all the {@link Event}s in this {@link EventChain}.
	 */
	@Override
	public boolean handle(T event, C context)
	{
		for (EventHandler<T, C> handler : handlers)
		{
			if (handler.handle(event, context))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * @return A list of {@link EventHandler}<{@link T}, {@link C}> that this {@link EventChain} handles.
	 */
	public List<EventHandler<T, C>> handlers()
	{
		return handlers;
	}
}
