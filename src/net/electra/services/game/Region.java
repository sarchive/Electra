package net.electra.services.game;

import java.util.ArrayList;

import net.electra.services.Servicable;
import net.electra.services.game.entities.Position;
import net.electra.services.game.entities.players.Player;

public class Region extends Servicable<GameService>
{
	public static final int SIZE = 64;
	
	private final ArrayList<Player> players = new ArrayList<Player>();
	private final Position position;
	
	public Region(Position position, GameService service)
	{
		super(service);
		this.position = position;
	}
	
	public ArrayList<Player> players()
	{
		return players;
	}
	
	public Position position()
	{
		return position;
	}
}
