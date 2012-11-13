package net.electra.services.jaggrab;

import java.util.ArrayList;

import net.electra.Server;
import net.electra.net.Client;
import net.electra.net.DisconnectReason;
import net.electra.net.NetworkService;

public class JagGrabService extends NetworkService<JagGrabClient, Client>
{
	private final ArrayList<JagGrabClient> clients = new ArrayList<JagGrabClient>();
	
	public JagGrabService(Server server)
	{
		super(server);
	}

	@Override
	public void process()
	{
		for (JagGrabClient reqClient : clients)
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
			
			reqClient.client().flush();
		}
	}

	@Override
	public JagGrabClient register(Client client)
	{
		JagGrabClient reqClient = new JagGrabClient(client, this);
		clients.add(reqClient);
		return reqClient;
	}

	@Override
	public void unregister(JagGrabClient client)
	{
		clients.remove(client);
	}
}
