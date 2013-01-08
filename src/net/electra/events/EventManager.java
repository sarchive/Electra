package net.electra.events;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unchecked")
public class EventManager
{
	static
	{
		try
		{
    		load((List<Map<String, Object>>)new Yaml().load(new FileInputStream(new File("./config/handlers.yml"))));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private static final HashMap<Class<? extends Event>, EventHandler<?, ?>> handlers = new HashMap<Class<? extends Event>, EventHandler<?, ?>>();
	
	@SuppressWarnings("rawtypes")
	public static void load(List<Map<String, Object>> containers) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		for (Map<String, Object> container : containers)
		{
			//Class<?> context = Class.forName((String)container.get("context")); // TODO: verify context types
			List<Map<String, Object>> handlerList = (List<Map<String, Object>>)container.get("handlers");
			
			for (Map<String, Object> handler : handlerList)
			{
				Class<? extends Event> event = (Class<? extends Event>)Class.forName((String)handler.get("event"));
				List<Map<String, String>> chain = (List<Map<String, String>>)handler.get("chain");
				// TODO: add chain[chain[chain[chain]]] (for lack of better explanation because i cbf)
				ArrayList<EventHandler<?, ?>> chainEventList = new ArrayList<EventHandler<?, ?>>();
				
				for (Map<String, String> linkElement : chain)
				{
					Class<? extends EventHandler<? extends Event, ?>> link = (Class<? extends EventHandler<? extends Event, ?>>)Class.forName(linkElement.get("link"));
					chainEventList.add(link.newInstance());
				}
				
				handlers.put(event, new EventChain(chainEventList.toArray(new EventHandler<?, ?>[0])));
			}
		}
	}
	
	public static <T extends Event, C> boolean fire(T event, C context)
	{
		try
		{
			return ((EventHandler<T, C>)handler(event.getClass(), context.getClass())).handle(event, context);
		}
		catch (ClassCastException | NullPointerException ex)
		{
			//System.err.println("No event handler found for " + event.getClass() + " or invalid context " + context.getClass() + "[" + ex.getClass() + "]");
			return false;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	public static <T extends Event, C> EventChain<T, C> chain(Class<T> c, Class<C> t)
	{
		return (EventChain<T, C>)handler(c, t);
	}
	
	public static <T extends Event, C> EventHandler<T, C> handler(Class<T> c, Class<C> t)
	{
		return (EventHandler<T, C>)handlers.get(c);
	}
}
