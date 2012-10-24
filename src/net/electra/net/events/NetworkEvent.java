package net.electra.net.events;

import net.electra.events.Event;
import net.electra.io.DataBuffer;

public abstract class NetworkEvent extends Event
{
	public abstract void parse(DataBuffer buffer); // all network events have support for parsing and building. it's all automated.
	public abstract void build(DataBuffer buffer); // there's no reason for them not to, it only creates more of a mess.
	public abstract int length();
	public abstract int id();
}
