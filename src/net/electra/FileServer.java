package net.electra;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import net.electra.events.EventManager;
import net.electra.net.Client;
import net.electra.net.NetworkService;
import net.electra.net.events.resolver.EventResolver;
import net.electra.services.Service;
import net.electra.services.jaggrab.JagGrabService;
import net.electra.services.ondemand.OnDemandService;

import org.yaml.snakeyaml.Yaml;

public class FileServer extends Server
{
	public FileServer(EventManager eventManager) throws IOException
	{
		super(eventManager);
	}
	
	@Override
	public void run()
	{
		try
    	{
    		while (true)
    		{
    			process();
    			Thread.sleep(50);
    		}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
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
		}
		
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setup() throws IOException
	{
		register(Service.JAGGRAB, new JagGrabService(this));
		register(Service.ONDEMAND, new OnDemandService(this));
		bind(new InetSocketAddress(Settings.FILE_SERVER_ADDRESS, Settings.FILE_SERVER_PORT)); // TODO: REDO ALL OF THIS STUFF
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
