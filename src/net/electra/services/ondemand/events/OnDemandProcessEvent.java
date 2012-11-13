package net.electra.services.ondemand.events;

import net.electra.events.Event;
import net.electra.services.ondemand.OnDemandRequest;

public class OnDemandProcessEvent extends Event
{
	private final OnDemandRequest request;
	
	public OnDemandProcessEvent(OnDemandRequest request)
	{
		this.request = request;
	}
	
	public OnDemandRequest request()
	{
		return request;
	}
}
