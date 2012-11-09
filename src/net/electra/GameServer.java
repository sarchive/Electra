package net.electra;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import net.electra.events.EventManager;
import net.electra.net.Client;
import net.electra.net.NetworkService;
import net.electra.net.events.resolver.EventResolver;
import net.electra.services.Service;
import net.electra.services.game.GameService;
import net.electra.services.login.LoginService;

public class GameServer extends Server
{
	public GameServer(EventManager eventManager) throws IOException
	{
		super(eventManager);
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args)
	{
		System.setOut(new TimeLogger(System.out, new SimpleDateFormat("hh:mm:ss a")));
		System.setErr(new TimeLogger(System.out, new SimpleDateFormat("hh:mm:ss a")));
		Settings.load(new File("./server.conf"));

		try
		{
			ServerManager serverManager = new ServerManager();
			EventManager eventManager = new EventManager((List<Map<String, Object>>)new Yaml().load(new FileInputStream(new File("./handlers.yml"))));
			serverManager.register(new GameServer(eventManager));
			serverManager.run();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	@Override
	public void run()
	{
		Runtime runtime = Runtime.getRuntime();
		long sleepTime = 0;
		long totalTime = 0;
		long interTime = 0;
		long totalCycles = 0;
		long interCycles = 0;
		
		System.out.println("Cycling server at a frequency of " + Settings.CYCLE_RATE + "ms");
		
		while (true)
		{
			executionTimer.reset();
			
			// begin cpu intensive stuff
			process();
			// end cpu intensive stuff
			
			if (interCycles >= 50 && totalTime > 0 && interTime > 0)
			{
				// every 50 cycles (30 seconds) print system information
				System.out.println("Average cycle time (span/total): " + (interTime / interCycles) + "/" + (totalTime / totalCycles) + "ms");
				System.out.println("Memory used:                     " + (runtime.totalMemory() - runtime.freeMemory()) + " bytes");
				System.out.println("Memory (total/max):              " + runtime.totalMemory() + "/" + runtime.maxMemory() + " bytes");
				System.out.println("Players online:                  " + this.<GameService>service(Service.GAME).count());
				
				interTime = 0;
				interCycles = 0;
			}
			
			totalCycles++;
			interCycles++;
			interTime += executionTimer.elapsed();
			totalTime += executionTimer.elapsed();
			sleepTime = Settings.CYCLE_RATE - executionTimer.elapsed();
			
			//System.out.println("NANO: " + (System.nanoTime() - sT) + ", MS: " + executionTimer.elapsed());
			
			if (sleepTime > 0 && sleepTime <= Settings.CYCLE_RATE && executionTimer.elapsed() <= Settings.CYCLE_RATE)
			{
				try
				{
					Thread.sleep(sleepTime);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean accept(Client client)
	{
		ByteBuffer service = ByteBuffer.allocate(1);
		int result = -1;
		
		try
		{
			result = client.socketChannel().read(service);
		}
		catch (IOException e)
		{
			// nothing to see here, bro
		}
		
		if (result == -1)
		{
			return false;
		}
		else
		{
			if (result >= 0)
			{
				service.flip();
				byte svcID = service.get();
				client.in().put(svcID);
				Service<?> svc = service(svcID);
				
				try
				{
					if (svc instanceof NetworkService)
					{
						((NetworkService<?, Client>)svc).register(client);
					}
				}
				catch (Exception ex)
				{
					return false; // doesn't accept clients
				}
			}
			else
			{
				service(LoginService.class).register(client);
			}
		}
		
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setup() throws IOException
	{
		register(Service.LOGIN, new LoginService(this));
		register(Service.GAME, new GameService(this));
		bind(new InetSocketAddress(Settings.ADDRESS, Settings.PORT));
		InputStream asdf = (InputStream)(new URL("jar:file:./lib/events.jar!/built-events.yml").openConnection()).getContent();
		byte[] allData;
		allData = new byte[asdf.available()];
		asdf.read(allData);
		EventResolver eventResolver = new EventResolver((List<Map<String, Object>>)new Yaml().load(new String(allData)));

		for (Service<?> service : serviceCache)
		{
			if (service instanceof NetworkService)
			{
				String name = service.getClass().getName();
				((NetworkService<?, ?>)service).networkEvents().putAll(eventResolver.resolve(name.substring(0, name.lastIndexOf(".")) + ".events"));
			}
		}
	}
}
