package net.electra.net.events.resolver;

import net.electra.net.events.NetworkEvent;

public class NetworkEventData
{
	private final Class<NetworkEvent> event;
	private final int operator;
	private final int length;
	
	public NetworkEventData(int operator, int length, Class<NetworkEvent> event)
	{
		this.operator = operator;
		this.length = length;
		this.event = event;
	}
	
	public NetworkEvent newInstance() throws InstantiationException, IllegalAccessException
	{
		return event.newInstance();
	}
	
	public Class<NetworkEvent> event()
	{
		return event;
	}
	
	public int operator()
	{
		return operator;
	}
	
	public int length()
	{
		return length;
	}
}
