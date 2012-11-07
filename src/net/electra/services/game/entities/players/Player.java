package net.electra.services.game.entities.players;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.electra.net.Client;
import net.electra.net.ClientAdapter;
import net.electra.services.game.GameService;
import net.electra.services.game.entities.Character;
import net.electra.services.game.entities.Position;

public class Player extends Character implements ClientAdapter
{
	private final List<WeakReference<Player>> localPlayers = new LinkedList<WeakReference<Player>>();
	private final Queue<WeakReference<Player>> newPlayers = new LinkedList<WeakReference<Player>>();
	private final String username;
	private final int uid;
	private Client client;
	
	public Player(int id, String username, int uid, GameService service)
	{
		super(id, new Position(3222 + id, 3222 + id), service); // just for testing 1-2 people for now
		this.username = username;
		this.uid = uid;
	}
	
	public void associate(Client client)
	{
		this.client = client;
		client.associate(this);
	}
	
	public List<WeakReference<Player>> localPlayers()
	{
		return localPlayers;
	}
	
	public Queue<WeakReference<Player>> newPlayers()
	{
		return newPlayers;
	}
	
	public String username()
	{
		return username;
	}
	
	public long usernameHash()
	{
		 long l = 0L;

		 for (int i = 0; i < username.length(); i++)
		 {
			 char c = username.charAt(i);

			 l *= 37L;

			 if (c >= 'A' && c <= 'Z')
			 {
				 l += (1 + c) - 65;
			 }
			 else if (c >= 'a' && c <= 'z')
			 {
				 l += (1 + c) - 97;
			 }
			 else if (c >= '0' && c <= '9')
			 {
				 l += (27 + c) - 48;
			 }
		 }

		 while (l % 37L == 0L && l != 0L)
		 {
			 l /= 37L;
		 }

		 return l;
	}
	
	public int uid()
	{
		return uid;
	}
	
	@Override
	public Client client()
	{
		return client;
	}
}
