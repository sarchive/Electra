package net.electra.services.login;

import net.electra.net.Client;
import net.electra.net.ClientAdapter;
import net.electra.services.Servicable;

public class PotentialPlayer extends Servicable<LoginService> implements ClientAdapter
{
	private final Client client;
	private String username;
	private int uid;
	
	public PotentialPlayer(Client client, LoginService service)
	{
		super(service);
		this.client = client;
		client.associate(this);
	}

	public String username()
	{
		return username;
	}

	public void username(String username)
	{
		this.username = username;
	}

	public int uid()
	{
		return uid;
	}

	public void uid(int uid)
	{
		this.uid = uid;
	}
	
	@Override
	public Client client()
	{
		return client;
	}
}
