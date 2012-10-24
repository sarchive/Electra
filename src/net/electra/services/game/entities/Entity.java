package net.electra.services.game.entities;

import net.electra.services.Servicable;
import net.electra.services.game.GameService;

public abstract class Entity extends Servicable<GameService>
{
	private final UpdateMask mask = new UpdateMask(UpdateMask.DEFAULT);
	private boolean placementRequired = true;
	private final Position position;
	private final int id;
	
	public Entity(int id, Position position, GameService service)
	{
		super(service);
		this.id = id;
		this.position = position;
	}
	
	public int id()
	{
		return id;
	}
	
	public Position position()
	{
		return position;
	}
	
	public void placementRequired(boolean value)
	{
		placementRequired = value;
	}
	
	public boolean placementRequired()
	{
		return placementRequired;
	}
	
	public UpdateMask mask()
	{
		return mask;
	}
}
