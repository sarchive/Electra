package net.electra.net.events;

import net.electra.events.Event;
import net.electra.io.DataBuffer;

/**
 * The base class for all {@link NetworkEvent}s.
 * @author Supah Fly
 */
@NetworkEventHeader(id = 0, length = 0)
public abstract class NetworkEvent extends Event
{
	/**
	 * Parses a {@link DataBuffer} and uses the data to populate the fields of a class first implementing {@link NetworkEvent}.
	 * @param buffer The buffer to be read from.
	 */
	public abstract void parse(DataBuffer buffer);
	
	/**
	 * Writes any field data, in a class implementing {@link NetworkEvent}, to a given {@link DataBuffer}
	 * @param buffer The buffer to be written to.
	 */
	public abstract void build(DataBuffer buffer);
	
	// TODO: fix terrible documentation
	// all network events have support for parsing and building.
	// there's no reason for them not to, it only creates more of a mess.
	
	public static NetworkEventHeader getHeader(Class<NetworkEvent> clazz)
	{
		return clazz.getAnnotation(NetworkEventHeader.class);
	}
}
