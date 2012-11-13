package net.electra.services.ondemand.events.handlers;

import net.electra.events.EventHandler;
import net.electra.net.ClientAdapter;
import net.electra.net.events.DisconnectEvent;
import net.electra.services.ondemand.OnDemandClient;

public class DisconnectEventHandler extends EventHandler<DisconnectEvent, ClientAdapter>
{
	@Override
	public void handle(DisconnectEvent event, ClientAdapter context)
	{
		if (context instanceof OnDemandClient)
		{
			System.out.println("OnDemand Client (" + context.client().socketChannel().socket() + ") disconnected: " + event.reason());
			((OnDemandClient)context).service().unregister((OnDemandClient)context);
		}
	}
}
