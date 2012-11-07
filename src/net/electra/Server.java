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
	
	public abstract void setup();
	
	/*@SuppressWarnings({ "unchecked", "deprecation" })
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
			final Server server = new Server(eventResolver, eventManager);
			server.register(Service.LOGIN, new LoginService(server));
			server.register(Service.GAME, new GameService(server));
			Runnable r = new Runnable()
			{	
				@Override
				public void run()
				{
					try
					{
						server.run();
					}
					catch (InterruptedException ex)
					{
						ex.printStackTrace();
					}
				}
			};

			System.out.println(Settings.SERVER_NAME + " ----------------------------------------------");
			System.out.println("Server started:  " + new Date());
			System.out.println("Build date:	  " + new Date(new File(server.getClass().getClassLoader().getResource(server.getClass().getCanonicalName().replace('.', '/') + ".class").toURI()).lastModified()));
			System.out.println("Client version:  " + Settings.MINIMUM_CLIENT_VERSION);
			System.out.println("Server version:  " + Settings.SERVER_VERSION);
			System.out.println("---------------------------------------------- " + Settings.SERVER_NAME);
			server.bind();
			
			// this server tries to be resilient, automatically restarting itself if there's a problem.
			while (true)
			{
				try
				{
					boolean hanging = server.executionTimer().elapsed() > Settings.CYCLE_RATE * 10; // if we missed 10 cycles then we're probably hanging and should get down!
					
					if (!server.thread().isAlive() || hanging)
					{
						if (hanging)
						{
							System.err.println("Server thread seems to be hanging. Dumping thread data and restarting...");
							System.err.println("Name:        " + server.thread().getName());
							System.err.println("State:       " + server.thread().getState());
							System.err.println("Stack trace: ");
							
							for (StackTraceElement ele : server.thread().getStackTrace())
							{
								System.out.println("\t" + ele);
							}
							
							server.thread().interrupt();
							server.thread().stop();
							server.executionTimer.reset();
						}
						else
						{
							System.out.println("Server thread is not running. Starting...");
						}
						
						server.thread().start();
					}

					Thread.sleep(1);
				}
				catch (Exception ex)
				{
					//ex.printStackTrace();
					System.out.println("Server thread is not assigned. Assigning...");
					server.thread(new Thread(r));
				}
			}
		}
		catch (BindException ex)
		{
			System.err.println("Failed to bind to port. Exiting.");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		System.out.println(Settings.SERVER_NAME + " server terminated");
	}*/
	
	public void bind(InetSocketAddress address) throws IOException
	{
		//InetSocketAddress address = new InetSocketAddress(Settings.ADDRESS, Settings.PORT);
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
	
	private void network()
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
	
	/*public void run() throws InterruptedException
	{
		Runtime runtime = Runtime.getRuntime();
		long sleepTime = 0;
		long totalTime = 0;
		long interTime = 0;
		long totalCycles = 0;
		long interCycles = 0;
		long sT = 0;
		
		System.out.println("Cycling server at a frequency of " + Settings.CYCLE_RATE + "ms");
		
		while (true)
		{
			executionTimer.reset();
			sT = System.nanoTime();
			
			// begin cpu intensive stuff
			// TODO: separate networking to improve performance. (anyone viewing this: know that i do know how to program and i'll implement it correctly)
			// the reason it's not is because i wanted to keep it simple until the design is fully worked out
			network(executionTimer); // call another method because i believe it helps with memory usage, i could be wrong though.
			
			for (Service<?> svc : serviceCache)
			{
				svc.process();
			}
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
			
			System.out.println("NANO: " + (System.nanoTime() - sT) + ", MS: " + executionTimer.elapsed());
			
			if (sleepTime > 0 && sleepTime <= Settings.CYCLE_RATE && executionTimer.elapsed() <= Settings.CYCLE_RATE)
			{
				Thread.sleep(sleepTime);
			}
		}
	}*/
	
	/*@SuppressWarnings("unchecked")
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
	}*/
	
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
