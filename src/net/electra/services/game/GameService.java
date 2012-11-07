package net.electra.services.game;

import java.util.HashMap;

import net.electra.Server;
import net.electra.Settings;
import net.electra.net.Client;
import net.electra.net.DisconnectReason;
import net.electra.net.NetworkService;
import net.electra.net.events.resolver.NetworkEventData;
import net.electra.services.game.entities.players.Player;
import net.electra.services.game.events.MovementEvent;
import net.electra.services.game.events.PlayerTickEvent;
import net.electra.services.login.PotentialPlayer;

public class GameService extends NetworkService<Player, PotentialPlayer>
{
	private final HashMap<String, Integer> usernames = new HashMap<String, Integer>(); // using hashmaps to match usernames to players is much faster than iterating through every player
	//private final HashMap<Position, Region> regions = new HashMap<Position, Region>();
	//private final HashMap<Position, ArrayList<Integer>> tiles = new HashMap<Position, ArrayList<Integer>>(); // just testing
	private final Player[] players = new Player[Settings.PLAYER_CAPACITY];
	private int playerCount = 0;
	
	public GameService(Server server)
	{
		super(server);
		setup(new NetworkEventData(98, -1, MovementEvent.class));
		//networkEvents().putAll(server.resolver().resolve("net.electra.services.game.events"));
	}
	
	/*public ArrayList<Integer> tile(Position position)
	{
		return tiles.get(position);
	}
	
	public Region region(Position position)
	{
		position = (Position)position.clone();
		position.base(Region.SIZE, Region.SIZE);
		
		if (!regions.containsKey(position))
		{
			regions.put(position, new Region(position, this));
		}
		
		return regions.get(position);
	}*/

	@Override
	public Player register(PotentialPlayer potential)
	{
		Player loggedIn = player(potential.username());
		
		if (loggedIn != null)
		{
			Client client = loggedIn.client();
			client.associate(null); // disassociate currently logged in client
			loggedIn.associate(potential.client()); // associate old player with new client
			client.disconnect(DisconnectReason.RECONNECTION); // disconnect old client
			return loggedIn; // new player is really old player
		}
		else
		{
			int idx = firstAvailableIndex();
			
			if (idx != -1)
			{
				Player player = new Player(idx, potential.username(), potential.uid(), this);
				player.associate(potential.client());
				players[player.id()] = player;
				usernames.put(player.username(), idx);
				playerCount++;
				return player;
			}
		}
		
		return null;
	}
	
	@Override
	public void unregister(Player player)
	{
		//region(player.position()).players().remove(player);
		players[player.id()] = null;
		usernames.remove(player.username());
		playerCount--;
	}

	@Override
	public void process()
	{
		PlayerTickEvent tickEvent = new PlayerTickEvent(server().tick());
		
		for (Player player : players)
		{
			if (player == null)
			{
				continue;
			}
			
			player.advancePosition();
		}
		
		for (Player player : players)
		{
			if (player == null)
			{
				continue;
			}
			
			if (!player.client().connected())
			{
				player.client().disconnect(DisconnectReason.DISCONNECTED);
				continue;
			}
			
			player.fire(tickEvent);
		}
		
		for (Player player : players)
		{
			if (player == null)
			{
				continue;
			}
			
			player.mask().clear();
			player.placementRequired(false);
			player.client().flush();
		}
	}
	
	public boolean online(String username)
	{
		return (player(username) != null);
	}
	
	public Player player(String username)
	{
		Integer id = usernames.get(username);
		
		if (id != null)
		{
			Player player = players[id];
			
			if (player != null && player.username().equalsIgnoreCase(username))
			{
				return player;
			}
			
			System.out.println("Invalid username-index mapping " + username + " to " + usernames.get(username));
			usernames.remove(username);
		}
		
		return null;
	}
	
	public Player[] players()
	{
		return players;
	}
	
	public Player player(int id)
	{
		return players[id];
	}
	
	public int count()
	{
		return playerCount;
	}
	
	private int firstAvailableIndex()
	{
		for (int i = 0; i < Settings.PLAYER_CAPACITY; i++)
		{
			if (players[i] == null)
			{
				return i;
			}
		}
		
		return -1;
	}
}
