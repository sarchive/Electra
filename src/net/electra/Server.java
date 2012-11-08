package net.electra;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import net.electra.events.EventManager;
import net.electra.net.Client;
import net.electra.net.DisconnectReason;
import net.electra.services.Service;

public abstract class Server implements Processable, Runnable
{
	private final HashMap<Integer, Service<?>> services = new HashMap<Integer, Service<?>>();
	protected Service<?>[] serviceCache = new Service<?>[0];
	protected final ServerSocketChannel serverChannel;
	protected Timer executionTimer = new Timer();
	protected final EventManager eventManager;
	protected final Selector selector;
	protected Thread currentThread;
	protected long tick = 0;
	
	public Server(EventManager eventManager) throws IOException
	{
		this.eventManager = eventManager;
		this.selector = Selector.open();
		this.serverChannel = ServerSocketChannel.open();
	}
	
	public abstract void setup() throws IOException;
	
	public void bind(InetSocketAddress address) throws IOException
	{
		System.out.println("Attempting to bind to " + address);
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(address);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("Listening on " + serverChannel.getLocalAddress());
	}
	
	public abstract boolean accept(Client client);
	
	@Override
	public void process()
	{
		network();
		
		for (Service<?> svc : serviceCache)
		{
			svc.process();
		}
	}
	
	public void network()
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
					
					while ((socket = serverChannel.accept()) != null)
					{
						try
						{
							System.out.println("Accepting connection from " + socket.socket());
							socket.configureBlocking(false);
							SelectionKey key = socket.register(selector, SelectionKey.OP_READ);
							Client client = new Client(key);
							
							if (!accept(client))
							{
								client.disconnect(DisconnectReason.INVALID_LOGIN);
								selectionKey.cancel();
							}
						}
						catch (Exception ex)
						{
							//ex.printStackTrace();
							selectionKey.cancel();
						}
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
	
	public void rebuildServiceCache()
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
	
	public Thread thread()
	{
		return currentThread;
	}
	
	public void thread(Thread value)
	{
		currentThread = value;
	}
	
	public Timer executionTimer()
	{
		return executionTimer;
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
