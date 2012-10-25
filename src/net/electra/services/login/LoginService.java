package net.electra.services.login;

import java.util.ArrayList;

import net.electra.Server;
import net.electra.net.*;
import net.electra.net.events.NetworkEvent;
import net.electra.net.events.resolver.NetworkEventData;
import net.electra.services.login.events.ConnectEvent;
import net.electra.services.login.events.ReconnectEvent;

public class LoginService extends NetworkService<PotentialPlayer, Client>
{
	private final ArrayList<PotentialPlayer> logins = new ArrayList<PotentialPlayer>();
	
	@SuppressWarnings("unchecked")
	public LoginService(Server server)
	{
		super(server);
		networkEvents().put(16, new NetworkEventData(16, -1, (Class<NetworkEvent>)((Class<?>)ConnectEvent.class)));
		networkEvents().put(18, new NetworkEventData(18, -1, (Class<NetworkEvent>)((Class<?>)ReconnectEvent.class)));
		networkEvents().putAll(server.resolver().resolve("net.electra.services.login.events"));
	}

	@Override
	public PotentialPlayer register(Client client)
	{
		PotentialPlayer player = new PotentialPlayer(client, this);
		logins.add(player);
		return player;
	}
	
	@Override
	public void unregister(PotentialPlayer player)
	{
		logins.remove(player);
	}

	@Override
	public void process()
	{
		for (PotentialPlayer player : logins)
		{
			if (player == null)
			{
				unregister(player);
				continue;
			}
			
			if (!player.client().connected())
			{
				player.client().disconnect(DisconnectReason.DISCONNECTED);
				continue;
			}
			
			player.client().flush();
		}
	}
}
