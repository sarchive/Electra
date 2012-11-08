package net.electra;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import net.electra.services.Service;

public class ServerManager implements Runnable
{
	private final HashMap<Class<? extends Server>, Server> servers = new HashMap<Class<? extends Server>, Server>();

	@Override
	public void run()
	{
		try
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
			
			for (Server server : servers.values())
			{
				System.out.println("Setting up " + server.getClass().getSimpleName());
				server.setup();
				System.out.println("Services active:");
				
				for (Service<?> service : server.serviceCache)
				{
					System.out.println("\t" + service.getClass().getSimpleName());
				}
				
				server.thread(new Thread(server));
				server.thread().setName(Settings.SERVER_NAME + "/" + server.getClass().getSimpleName());
				server.thread().start();
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
	
	@SuppressWarnings("unchecked")
	public <T extends Server> T server(Class<T> c)
	{
		return (T)servers.get(c);
	}
}
