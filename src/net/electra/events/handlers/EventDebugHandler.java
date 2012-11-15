package net.electra.events.handlers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
				if (field.getName().equals("context") || Modifier.isStatic(field.getModifiers()))
				{
					continue;
				}
				
				boolean access = field.isAccessible();
				field.setAccessible(true); // afaik this is the only way to safely do this
				Object f = field.get(event);
				System.out.println("\t" + field.getType().getName() + " " + field.getName() + " = " + f);
				
				if (f.getClass().isArray())
				{
					Object[] fA = (Object[])f;
					
					for (Object a : fA)
					{
						System.out.println("\t\t" + a.getClass().getName() + " = " + a);
					}
				}
				
				field.setAccessible(access);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
