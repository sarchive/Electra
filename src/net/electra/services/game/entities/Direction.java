package net.electra.services.game.entities;

public enum Direction
{
	NORTH(1),
	NORTH_EAST(2),
	EAST(4),
	SOUTH_EAST(7),
	SOUTH(6),
	SOUTH_WEST(5),
	WEST(3),
	NORTH_WEST(0),
	NONE(-1);

	private final int value;
	
	private Direction(int value)
	{
		this.value = value;
	}

	public int value()
	{
		return value;
	}
	
	public static boolean isConnectable(int deltaX, int deltaY)
	{
		return Math.abs(deltaX) == Math.abs(deltaY) || deltaX == 0 || deltaY == 0;
	}
	
	public static Direction forValue(int value)
	{
		for (Direction direction : Direction.values())
		{
			if (direction.value() == value)
			{
				return direction;
			}
		}
		
		return Direction.NONE;
	}
	
	public static Direction fromPositions(Position src, Position dst)
	{
		// TODO: make this work
		double dx = ((double)dst.x() - (double)src.x());
		double dy = ((double)dst.y() - (double)src.y());
		double angle = (Math.atan(dy / dx) * 180) / Math.PI;
		int direction = -1;
		
		if (Double.isNaN(angle))
		{
			return Direction.NONE;
		}
		
		if (Math.signum(dx) < 0)
		{
			angle += 180.0;
		}
		
		direction = (int)((((90 - angle) / 22.5) + 16) % 16);
		
		if (direction > -1)
		{
			direction >>= 1;
		}
		
		return Direction.forValue(direction);
	}
	
	public static Direction fromDelta(Position delta)
	{
		return fromDelta(delta.x(), delta.y());
	}
	
	public static Direction fromDelta(int deltaX, int deltaY)
	{
		if (deltaY == 1)
		{
			if (deltaX == 1)
			{
				return Direction.NORTH_EAST;
			}
			else if (deltaX == 0)
			{
				return Direction.NORTH;
			}
			else
			{
				return Direction.NORTH_WEST;
			}
		}
		else if (deltaY == -1)
		{
			if (deltaX == 1)
			{
				return Direction.SOUTH_EAST;
			}
			else if (deltaX == 0)
			{
				return Direction.SOUTH;
			}
			else
			{
				return Direction.SOUTH_WEST;
			}
		}
		else
		{
			if (deltaX == 1)
			{
				return Direction.EAST;
			}
			else if (deltaX == -1)
			{
				return Direction.WEST;
			}
		}
		
		return Direction.NONE;
	}
}
