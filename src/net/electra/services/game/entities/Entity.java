package net.electra.services.game.entities;

import net.electra.services.Servicable;
import net.electra.services.game.GameService;

public abstract class Entity extends Servicable<GameService>
{
	private final int id;
	
	public Entity(int id, GameService service)
	{
		super(service);
		this.id = id;
	}
	
	public int id()
	{
		return id;
	}
	
	public abstract Position position();
}
