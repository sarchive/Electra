package net.electra.services.ondemand;

import java.util.ArrayList;

import net.electra.Server;
import net.electra.net.Client;
import net.electra.net.DisconnectReason;
import net.electra.net.NetworkService;

public class OnDemandService extends NetworkService<OnDemandClient, Client>
{
	private final ArrayList<OnDemandClient> clients = new ArrayList<OnDemandClient>();
	
	public OnDemandService(Server server)
	{
		super(server);
	}

	@Override
	public void process()
	{
		for (OnDemandClient reqClient : clients)
		{
			if (reqClient == null)
			{
				unregister(reqClient);
				continue;
			}
			
			if (!reqClient.client().connected())
			{
				reqClient.client().disconnect(DisconnectReason.DISCONNECTED);
				continue;
			}
			
			reqClient.process();
			reqClient.client().flush();
		}
	}

	@Override
	public OnDemandClient register(Client client)
	{
		OnDemandClient reqClient = new OnDemandClient(client, this);
		clients.add(reqClient);
		return reqClient;
	}

	@Override
	public void unregister(OnDemandClient client)
	{
		clients.remove(client);
	}
}
