package net.electra.services.game.entities;

import net.electra.services.game.GameService;

public abstract class Character extends Entity
{
	private final UpdateMask mask = new UpdateMask(UpdateMask.DEFAULT);
	private Direction secondDirection = Direction.NONE;
	private Direction firstDirection = Direction.NONE;
	private final PositionQueue position;
	private boolean placementRequired = true;
	
	public Character(int id, Position position, GameService service)
	{
		super(id, service);
		this.position = new PositionQueue(position);
	}
	
	public Direction firstDirection()
	{
		return firstDirection;
	}
	
	public Direction secondDirection()
	{
		return secondDirection;
	}
	
	public void advancePosition()
	{
		firstDirection = position.advance();
		
		if (position.running() || position.runPath())
		{
			secondDirection = position.advance();
		}
		else
		{
			secondDirection = Direction.NONE;
		}

		int deltaX = position.x() - position.regionX() * 8;
		int deltaY = position.y() - position.regionY() * 8;
		
		if (deltaX < 16 || deltaX >= 88 || deltaY < 16 || deltaY > 88)
		{
			position.refresh();
			placementRequired = true;
		}
	}
	
	public Position position()
	{
		return position;
	}
	
	public boolean movementRequired()
	{
		return firstDirection != Direction.NONE;
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
