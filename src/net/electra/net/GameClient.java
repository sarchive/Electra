package net.electra.net;

import java.nio.channels.SelectionKey;

public class GameClient extends Client
{
	public GameClient(SelectionKey selectionKey)
	{
		super(selectionKey);
	}

	@Override
	public boolean readPacket()
	{
		int id = inbound.getHeader();
		//NetworkEvent event = ((NetworkService<?, ?>)receiver.service()).networkEvents().get(id).newInstance();
		int length = event.length();
		// TODO: static hiding, create static methods in NetworkEvent for id and length and then hide them with static methods in the "event." this is how you implement
		// static method inheritence. one annoyance is that you have to suppress the warnings to reference "NetworkEvent" statically instead of through instances
		// how it works now is fine, though. this is just a thought for better design.
		
		if (length == -1 && inbound.hasRemaining())
		{
			length = inbound.getUnsigned();
		}
		
		if (length == -1 || inbound.remaining() < length)
		{
			return false;
			break;
		}
		
		//System.out.println("Received " + event.getClass().getSimpleName() + " (id: " + id + ", len: " + length + ")");
		event.parse(new DataBuffer(inbound.get(length)));
		receiver.fire(event);
		return true;
	}
}
