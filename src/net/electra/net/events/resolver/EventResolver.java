package net.electra.net.events.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.electra.net.events.NetworkEvent;

public class EventResolver
{
	private final HashMap<String, HashMap<Integer, NetworkEventData>> events = new HashMap<String, HashMap<Integer, NetworkEventData>>();
	
	@SuppressWarnings("unchecked")
	public EventResolver(List<Map<String, Object>> events)
	{
		for (Map<String, Object> event : events)
		{
			String namespace = (String)event.get("namespace");
			String name = (String)event.get("name");
			int operator = (int)event.get("operator");
			int length = (int)event.get("length");
			
			try
			{
				add(namespace, new NetworkEventData(operator, length, (Class<NetworkEvent>)Class.forName(namespace + "." + name)));
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}

	}
	
	private void add(String namespace, NetworkEventData data)
	{
		if (!events.containsKey(namespace))
		{
			events.put(namespace, new HashMap<Integer, NetworkEventData>());
		}
		
		events.get(namespace).put(data.operator(), data);
	}
	
	public NetworkEventData resolve(String namespace, int operator)
	{
		return events.get(namespace).get(operator);
	}
	
	public HashMap<Integer, NetworkEventData> resolve(String namespace)
	{
		return events.get(namespace);
	}
}
