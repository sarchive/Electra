package net.electra;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;

import net.electra.io.fs.Cache;
import net.electra.services.Service;

public class ServerManager implements Runnable
{
	private final HashMap<Class<? extends Server>, Server> servers = new HashMap<Class<? extends Server>, Server>();
	private final ThreadGroup serverThreadGroup = new ThreadGroup("SRVDS");
	private final Cache cache; // TODO: find a place to move this to
	
	public ServerManager() throws IOException, URISyntaxException
	{
		Runtime runtime = Runtime.getRuntime();
		System.out.println(Settings.SERVER_NAME + " ----------------------------------------------");
		System.out.println("Server started:     " + new Date());
		System.out.println("Build date:         " + new Date(new File(getClass().getClassLoader().getResource(getClass().getCanonicalName().replace('.', '/') + ".class").toURI()).lastModified()));
		System.out.println("Client version:     " + Settings.MINIMUM_CLIENT_VERSION);
		System.out.println("Server version:     " + Settings.SERVER_VERSION);
		System.out.println("Memory used:        " + (runtime.totalMemory() - runtime.freeMemory()) + " bytes");
		System.out.println("Memory (total/max): " + runtime.totalMemory() + "/" + runtime.maxMemory() + " bytes");
		System.out.println("---------------------------------------------- " + Settings.SERVER_NAME);
		cache = new Cache(new File(Settings.GAME_CACHE_PATH));
		serverThreadGroup.setDaemon(true);
		serverThreadGroup.setMaxPriority(Thread.MAX_PRIORITY);
	}

	@Override
	public void run()
	{
		try
		{
			for (Server server : servers.values())
			{
				System.out.println("Setting up " + server.getClass().getSimpleName());
				server.setup();
				System.out.println("Services active:");
				
				for (Service<?> service : server.serviceCache)
				{
					System.out.println("\t" + service.getClass().getSimpleName());
				}
				
				server.thread(new Thread(serverThreadGroup, server, Settings.SERVER_NAME + "/" + server.getClass().getSimpleName()));
				server.thread().setPriority(Thread.MAX_PRIORITY);
				server.thread().start();
			}
			
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			
			while (true)
			{
				Thread.sleep(1);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.out.println(Settings.SERVER_NAME + " server terminated");
		}
	}
	
	public void register(Server server)
	{
		servers.put(server.getClass(), server);
	}
	
	public void unregister(Server server)
	{
		servers.remove(server.getClass());
	}
	
	public Cache cache()
	{
		return cache;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Server> T server(Class<T> c)
	{
		return (T)servers.get(c);
	}
}
