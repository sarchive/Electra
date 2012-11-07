package net.electra;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import net.electra.events.EventManager;

public class ServerManager implements Runnable
{
	private final HashMap<Class<? extends Server>, Server> servers = new HashMap<Class<? extends Server>, Server>();
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args)
	{
		System.setOut(new TimeLogger(System.out, new SimpleDateFormat("hh:mm:ss a")));
		System.setErr(new TimeLogger(System.out, new SimpleDateFormat("hh:mm:ss a")));
		Settings.load(new File("./server.conf"));
		ServerManager serverManager = new ServerManager();

		try
		{
			EventManager eventManager = new EventManager((List<Map<String, Object>>)new Yaml().load(new FileInputStream(new File("./handlers.yml"))));
			serverManager.register(new GameServer(eventManager));
			serverManager.run();
		}
		catch (Exception ex)
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		try
		{
			System.out.println(Settings.SERVER_NAME + " ----------------------------------------------");
			System.out.println("Server started:  " + new Date());
			System.out.println("Build date:      " + new Date(new File(getClass().getClassLoader().getResource(getClass().getCanonicalName().replace('.', '/') + ".class").toURI()).lastModified()));
			System.out.println("Client version:  " + Settings.MINIMUM_CLIENT_VERSION);
			System.out.println("Server version:  " + Settings.SERVER_VERSION);
			System.out.println("---------------------------------------------- " + Settings.SERVER_NAME);
			
			for (Server server : servers.values())
			{
				server.setup();
				server.thread(new Thread(server));
				server.thread().start();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		System.out.println(Settings.SERVER_NAME + " server terminated");
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
