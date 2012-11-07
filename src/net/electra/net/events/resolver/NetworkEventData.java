package net.electra.net.events.resolver;

import net.electra.net.events.NetworkEvent;

public class NetworkEventData
{
	private final Class<NetworkEvent> event;
	private final int operator;
	private final int length;
	
	@SuppressWarnings("unchecked")
	public NetworkEventData(int operator, int length, Class<?> event)
	{
		this.operator = operator;
		this.length = length;
		this.event = (Class<NetworkEvent>)event;
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
