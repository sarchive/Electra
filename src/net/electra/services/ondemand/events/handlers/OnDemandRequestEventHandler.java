package net.electra.services.ondemand.events.handlers;

import java.io.IOException;

import net.electra.events.EventHandler;
import net.electra.services.ondemand.OnDemandClient;
import net.electra.services.ondemand.OnDemandRequest;
import net.electra.services.ondemand.events.OnDemandRequestEvent;

public class OnDemandRequestEventHandler extends EventHandler<OnDemandRequestEvent, OnDemandClient>
{
	@Override
	public void handle(OnDemandRequestEvent event, OnDemandClient context)
	{
		try
		{
			context.submit(new OnDemandRequest(context.service().server().cache().get(event.index() + 1, event.file()), event.priority()));
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
