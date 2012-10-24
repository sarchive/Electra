package net.electra.net;

import java.util.HashMap;
import java.util.Map;

import net.electra.Server;
import net.electra.net.events.resolver.NetworkEventData;
import net.electra.services.Service;

public abstract class NetworkService<H, A> extends Service<H>
{
	private final Map<Integer, NetworkEventData> collection = new HashMap<Integer, NetworkEventData>();
	
	public NetworkService(Server server)
	{
		super(server);
	}
	
	public abstract H register(A client);
	public abstract void unregister(H client);
	
	public Map<Integer, NetworkEventData> networkEvents()
	{
		return collection;
	}
}
