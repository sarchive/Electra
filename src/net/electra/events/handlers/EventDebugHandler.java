package net.electra.events.handlers;

import java.lang.reflect.Field;

import net.electra.events.Event;
import net.electra.events.EventHandler;

public final class EventDebugHandler extends EventHandler<Event, Object>
{
	@Override
	public void handle(Event event, Object context)
	{
		System.out.println("[" + event.getClass().getSimpleName() + "->" + context.getClass().getSimpleName() + "]");
		
		Field[] fields = event.getClass().getDeclaredFields();
		
		try
		{
			for (Field field : fields)
			{
				if (field.getName().equals("context"))
				{
					continue;
				}
				
				boolean access = field.isAccessible();
				field.setAccessible(true); // afaik this is the only way to safely do this
				System.out.println("\t" + field.getType().getName() + " " + field.getName() + " = " + field.get(event));
				field.setAccessible(access);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
