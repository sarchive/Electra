package net.electra.services.jaggrab.events.handlers;

import java.io.IOException;

import net.electra.events.EventHandler;
import net.electra.io.DataBuffer;
import net.electra.io.fs.CacheFile;
import net.electra.services.jaggrab.JagGrabClient;
import net.electra.services.jaggrab.events.JagGrabRequestEvent;

public class JagGrabRequestEventHandler extends EventHandler<JagGrabRequestEvent, JagGrabClient>
{
	public static final String[] prefixes = { "crc", "title", "config", "interface", "media", "versionlist", "textures", "wordenc", "sounds" };
	
	@Override
	public void handle(JagGrabRequestEvent event, JagGrabClient context)
	{
		int prefix = -1;
		
		for (int i = 0; i < prefixes.length; i++)
		{
			if (event.request().startsWith("JAGGRAB /" + prefixes[i]))
			{
				prefix = i;
			}
		}
		
		try
		{
			if (prefix == 0)
			{
				context.client().write(new DataBuffer(context.service().server().cache().crcTable()));
			}
			else
			{
				CacheFile file = context.service().server().cache().get(0, prefix);
				context.client().write(new DataBuffer(file.buffer().array()));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
