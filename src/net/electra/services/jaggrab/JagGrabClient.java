package net.electra.services.jaggrab;

import net.electra.net.Client;
import net.electra.net.ClientAdapter;
import net.electra.services.Servicable;

public class JagGrabClient extends Servicable<JagGrabService> implements ClientAdapter
{
	private final Client client;
	
	public JagGrabClient(Client client, JagGrabService service)
	{
		super(service);
		this.client = client;
		client.associate(this);
	}

	@Override
	public Client client()
	{
		return client;
	}
}
