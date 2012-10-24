package net.electra.services;

import net.electra.Processable;
import net.electra.Server;

// everything in a server is a service, a server is just a provider for the service
// H is the thing it's servicing
public abstract class Service<H> implements Processable
{
	public static final int LOGIN = 14;
	public static final int UPDATE = 15;
	public static final int GAME = 16;
	
	private final Server server;
	
	public Service(Server server)
	{
		this.server = server;
	}
	
	public Server server()
	{
		return server;
	}
}
