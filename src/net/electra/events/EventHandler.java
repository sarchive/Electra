package net.electra.events;

/**
 * Definition of an {@link EventHandler}.
 * @author Supah Fly
 *
 * @param <T> The type of {@link Event}
 * @param <C> The type of the context.
 */
public abstract class EventHandler<T extends Event, C>
{
	/**
	 * Specifies how an {@link Event}'s data ({@link T}) is supposed to be used when applied to the context ({@link C}).
	 * @param event The {@link Event} object containing event data.
	 * @param context The object we are applying the data from our {@link Event} to.
	 * @return True on success, false if the ({@link EventChain}) <strong>should</strong> be broken. Typically, the chain is broken due to an error, failed validation, invalid access, etc.
	 */
	public abstract boolean handle(T event, C context);
}
