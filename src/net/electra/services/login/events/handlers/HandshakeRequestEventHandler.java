package net.electra.services.login.events.handlers;

import net.electra.events.EventHandler;
import net.electra.services.login.PotentialPlayer;
import net.electra.services.login.events.HandshakeRequestEvent;
import net.electra.services.login.events.HandshakeAcknowledgeEvent;

public class HandshakeRequestEventHandler extends EventHandler<HandshakeRequestEvent, PotentialPlayer>
{
	@Override
	public void handle(HandshakeRequestEvent event, PotentialPlayer context)
	{
		context.client().write(new HandshakeAcknowledgeEvent(0, ((long)(Math.random() * 99999999D) << 32) + (long)(Math.random() * 99999999D)));
	}
}
