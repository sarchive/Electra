package net.electra;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;

import net.electra.util.Timer;

public abstract class Server<C> implements Runnable
{
	private final ServerSocketChannel serverChannel;
	private final Selector selector;
	private final List<C> clients;
	private final Timer watch;
	
	public Server(List<C> clients) throws IOException
	{
		this.clients = clients;
		this.selector = Selector.open();
		this.serverChannel = ServerSocketChannel.open();
		this.watch = new Timer();
	}
	
	public Server() throws IOException
	{
		this(new ArrayList<C>());
	}
	
	public abstract void connect(SelectionKey client);
	public abstract void disconnect(C client);
	public abstract void receive(C client);
	public abstract void error(C client);
	
	private void accept(SelectionKey key)
	{
		if (key.isValid() && key.isAcceptable())
		{
			connect(key);
			return;
		}
		
		key.cancel();
	}
	
	@SuppressWarnings("unchecked")
	private void read(SelectionKey key)
	{
		if (key.isValid() && key.isReadable())
		{
			receive((C)key.attachment());
			return;
		}
		
		key.cancel();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void run()
	{
		while (true)
		{
    		try
    		{
    			selector.select();
    			
    			for (SelectionKey key : selector.selectedKeys())
    			{
    				try
    				{
    					accept(key);
    					read(key);
    				}
    				catch (Exception ex)
    				{
    					ex.printStackTrace();
    					
    					if (key.attachment() != null)
    					{
    						error((C)key.attachment());
    					}
    				}
    			}
    		}
    		catch (IOException ex)
    		{
    			ex.printStackTrace();
    		}
		}
	}
	
	public Timer watch()
	{
		return watch;
	}
	
	public List<C> clients()
	{
		return clients;
	}
	
	public ServerSocketChannel channel()
	{
		return serverChannel;
	}
	
	public Selector selector()
	{
		return selector;
	}
}
