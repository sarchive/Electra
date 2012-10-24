package net.electra;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

import net.electra.events.EventManager;
import net.electra.net.Client;
import net.electra.net.NetworkService;
import net.electra.net.events.resolver.EventResolver;
import net.electra.services.Service;
import net.electra.services.game.GameService;
import net.electra.services.login.LoginService;

public class Server
{
	private final HashMap<Integer, Service<?>> services = new HashMap<Integer, Service<?>>();
	private Service<?>[] serviceCache = new Service<?>[0];
    private final ServerSocketChannel serverChannel;
	private final EventResolver eventResolver;
	private final EventManager eventManager;
	private final Selector selector;
	private long tick = 0;
	
	public Server(EventResolver eventResolver, EventManager eventManager) throws IOException
	{
		this.eventResolver = eventResolver;
		this.eventManager = eventManager;
		this.selector = Selector.open(); // magically throwing ioexceptions
        this.serverChannel = ServerSocketChannel.open();
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] arguments)
	{
		System.setOut(new TimeLogger(System.out, new SimpleDateFormat("hh:mm:ss a")));
		System.setErr(new TimeLogger(System.out, new SimpleDateFormat("hh:mm:ss a")));
		
		try
		{
			Settings.load(new File("./server.conf"));
			InputStream asdf = (InputStream)(new URL("jar:file:./lib/events.jar!/built-events.yml").openConnection()).getContent();
			byte[] allData = new byte[asdf.available()];
			asdf.read(allData);
			EventResolver eventResolver = new EventResolver((List<Map<String, Object>>)new Yaml().load(new String(allData)));
			EventManager eventManager = new EventManager((List<Map<String, Object>>)new Yaml().load(new FileInputStream(new File("./handlers.yml"))));
			Server server = new Server(eventResolver, eventManager);
	        server.register(Service.LOGIN, new LoginService(server));
			server.register(Service.GAME, new GameService(server));
			server.run(); // blocks
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		System.out.println(Settings.SERVER_NAME + " Server terminated");
	}
	
	public void run() throws InterruptedException, IOException, URISyntaxException
	{
        Runtime runtime = Runtime.getRuntime();
		System.out.println(Settings.SERVER_NAME + " ----------------------------------------------");
		System.out.println("Server started:  " + new Date());
		System.out.println("Build date:      " + new Date(new File(getClass().getClassLoader().getResource(getClass().getCanonicalName().replace('.', '/') + ".class").toURI()).lastModified()));
		System.out.println("Client version:  " + Settings.MINIMUM_CLIENT_VERSION);
		System.out.println("Server version:  " + Settings.SERVER_VERSION);
		System.out.println("---------------------------------------------- " + Settings.SERVER_NAME);
		
		InetSocketAddress address = new InetSocketAddress(Settings.ADDRESS, Settings.PORT);
		System.out.println("Attempting to bind to " + address);
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(address);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Listening on " + serverChannel.getLocalAddress());
        System.out.println("Cycling server at a frequency of " + Settings.CYCLE_RATE + "ms");
        
        long sleepTime = 0;
        long totalTime = 0;
        long interTime = 0;
        long totalCycles = 0;
        long interCycles = 0;
        Timer executionTimer = new Timer();
        
		while (true)
		{
			executionTimer.reset();
			
			// begin cpu intensive stuff
			// TODO: separate networking to improve performance. (anyone viewing this: know that i do know how to program and i'll implement it correctly)
			// the reason it's not is because i wanted to keep it simple until the design is fully worked out
			network(executionTimer); // call another method because i believe it helps with memory usage, i could be wrong though.
			
			for (Service<?> svc : serviceCache)
			{
				svc.process();
			}
			// end cpu intensive stuff
			
			if (totalTime > 0 && totalCycles > 0 && totalCycles % 50 == 0)
			{
				// every 50 cycles (30 seconds) print system information
				System.out.println("Average cycle time (span/total): "
									+ (interTime / interCycles) + "/"
									+ (totalTime / totalCycles) + "ms");
				System.out.println("Memory used:                     "
									+ (runtime.totalMemory() - runtime.freeMemory()) + " bytes");
				System.out.println("Memory (total/max):              "
									+ runtime.totalMemory() + "/"
									+ runtime.maxMemory() + " bytes");
				System.out.println("Players online: " + this.<GameService>service(Service.GAME).count());
				
				interTime = 0;
				interCycles = 0;
			}

			totalCycles++;
			interCycles++;
			interTime += executionTimer.elapsed();
			totalTime += executionTimer.elapsed();
			sleepTime = Settings.CYCLE_RATE - executionTimer.elapsed();
			
			if (sleepTime > 0 && sleepTime <= Settings.CYCLE_RATE && executionTimer.elapsed() <= Settings.CYCLE_RATE)
            {
				Thread.sleep(sleepTime);
            }
		}
	}
	
	@SuppressWarnings("unchecked")
	public void network(Timer executionTimer)
	{
		try
		{
            selector.selectNow();
            
            for (SelectionKey selectionKey : selector.selectedKeys())
            {
            	if (!selectionKey.isValid())
            	{
            		selectionKey.cancel(); // make sure it's cancelled, no harm in making sure.
            		continue;
            	}
            	
                if (selectionKey.isValid() && selectionKey.isAcceptable())
                {
                	SocketChannel socket = null;
                	int amount = 0;
                	
                	// there's a limit much time of a cycle will be spent on networking
                	// also a limit on how many connections will be accepted per cycle
                	// both are configurable via server.conf
                	while (executionTimer.elapsed() < Settings.MAX_NEW_CONNECTION_TIME
                			&& (Settings.MAX_NEW_CONNECTIONS == 0 || (Settings.MAX_NEW_CONNECTIONS != 0 && amount < Settings.MAX_NEW_CONNECTIONS))
                			&& ((socket = serverChannel.accept()) != null))
                	{
                		try
                		{
                    		System.out.println("Accepting connection from " + socket.socket());
                    		socket.configureBlocking(false);
                    		ByteBuffer service = ByteBuffer.allocate(1);
                    		int result = socket.read(service);
                    		
                    		if (result == -1)
                    		{
                    			socket.close();
                    			selectionKey.cancel();
                    		}
                    		else
                    		{
                        		SelectionKey key = socket.register(selector, SelectionKey.OP_READ);
                        		Client client = new Client(key);
                        		
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
	                    				// doesn't accept clients
	                    			}
	                    		}
	                    		else
	                    		{
	                        		service(LoginService.class).register(client);
	                    		}
                    		}
                		}
                		catch (Exception ex)
                		{
                			//ex.printStackTrace();
                			selectionKey.cancel();
                		}
                		
                		amount++;
                	}
                }
                
                if (selectionKey.isValid() && selectionKey.isReadable())
                {
                	((Client)selectionKey.attachment()).read();
                }
            }
        }
		catch (Exception ex)
		{
			ex.printStackTrace();
        }
	}
	
	protected void rebuildServiceCache()
	{
		Set<Entry<Integer, Service<?>>> set = services.entrySet();
		Iterator<Entry<Integer, Service<?>>> iterator = set.iterator();
		ArrayList<Service<?>> cache = new ArrayList<Service<?>>();
		
		while (iterator.hasNext())
		{
			cache.add(iterator.next().getValue());
		}
		
		serviceCache = cache.toArray(new Service<?>[0]);
	}
	
	public void register(int id, Service<?> service)
	{
		services.put(id, service);
		rebuildServiceCache();
	}
	
	public void unregister(int id)
	{
		services.remove(id); // i doubt this method is safe, it's not used anyway.
		rebuildServiceCache();
	}
	
	public void unregister(Service<?> service)
	{
		Set<Entry<Integer, Service<?>>> set = services.entrySet();
		Iterator<Entry<Integer, Service<?>>> iterator = set.iterator();
		
		while (iterator.hasNext())
		{
			Entry<Integer, Service<?>> entry = iterator.next();
			
			if (entry.getValue().getClass() == service.getClass())
			{
				unregister(entry.getKey());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Service<?>> T service(int id, Class<T> c)
	{
		return (T)service(id);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Service<?>> T service(int id)
	{
		return (T)services.get(id);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Service<?>> T service(Class<T> c)
	{
		for (Service<?> svc : serviceCache)
		{
			if (svc.getClass() == c)
			{
				return (T)svc;
			}
		}
		
		return null;
	}
	
	public EventManager eventManager()
	{
		return eventManager;
	}
	
	public ServerSocketChannel channel()
	{
		return serverChannel;
	}
	
	public EventResolver resolver()
	{
		return eventResolver;
	}
	
	public Selector selector()
	{
		return selector;
	}
	
	public long tick()
	{
		return tick;
	}
}
