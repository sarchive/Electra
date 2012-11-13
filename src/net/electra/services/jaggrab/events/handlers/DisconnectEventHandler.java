package net.electra.services.jaggrab.events.handlers;

import net.electra.events.EventHandler;
import net.electra.net.ClientAdapter;
import net.electra.net.events.DisconnectEvent;
import net.electra.services.jaggrab.JagGrabClient;

public class DisconnectEventHandler extends EventHandler<DisconnectEvent, ClientAdapter>
{
	@Override
	public void handle(DisconnectEvent event, ClientAdapter context)
	{
		if (context instanceof JagGrabClient)
		{
			System.out.println("JagGrab Client (" + context.client().socketChannel().socket() + ") disconnected: " + event.reason());
			((JagGrabClient)context).service().unregister((JagGrabClient)context);
		}
	}
}
