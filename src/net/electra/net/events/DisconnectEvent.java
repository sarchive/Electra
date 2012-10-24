package net.electra.net.events;

import net.electra.events.Event;
import net.electra.net.DisconnectReason;

public class DisconnectEvent extends Event
{
	private final DisconnectReason reason;
	
	public DisconnectEvent(DisconnectReason reason)
	{
		this.reason = reason;
	}
	
	public DisconnectReason reason()
	{
		return reason;
	}
}
