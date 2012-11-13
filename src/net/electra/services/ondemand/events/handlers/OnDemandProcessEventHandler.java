package net.electra.services.ondemand.events.handlers;

import net.electra.events.EventHandler;
import net.electra.io.DataBuffer;
import net.electra.services.ondemand.OnDemandBlock;
import net.electra.services.ondemand.OnDemandClient;
import net.electra.services.ondemand.events.OnDemandProcessEvent;

public class OnDemandProcessEventHandler extends EventHandler<OnDemandProcessEvent, OnDemandClient>
{
	@Override
	public void handle(OnDemandProcessEvent event, OnDemandClient context)
	{
		OnDemandBlock block = event.request().nextBlock();
		DataBuffer buffer = new DataBuffer(new byte[block.data().length + 6]);
		buffer.put(event.request().file().descriptor().index().id() - 1);
		buffer.putShort(event.request().file().descriptor().id());
		buffer.putShort(event.request().file().descriptor().size());
		buffer.put(block.id());
		buffer.put(block.data());
		buffer.flip();
		context.client().write(buffer);
	}
}
