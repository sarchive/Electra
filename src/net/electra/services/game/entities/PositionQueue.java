package net.electra.services.game.entities;

import java.util.ArrayDeque;
import java.util.Deque;

public class PositionQueue extends Position
{
	private final Deque<Position> waypoints = new ArrayDeque<Position>();
	private boolean runPath = false;
	private boolean running = false;
	
	public PositionQueue(Position position)
	{
		super(position.x(), position.y(), position.z());
	}
	
	public Direction advance()
	{
		Direction direction = Direction.NONE;
		Position newPoint = null;
		
		while (newPoint == null && waypoints.size() > 0)
		{
			newPoint = waypoints.poll();
		}
		
		if (newPoint != null)
		{
			direction = Direction.fromDelta(delta(newPoint));
			step(direction);
		}

		if (waypoints.size() == 0)
		{
			runPath = false;
		}
		
		return direction;
	}
	
	public void queue(Direction direction)
	{
		Position newPos = (Position)waypoints.getLast().clone();
		newPos.step(direction);
		queue(newPos);
	}
	
	public void queue(Position pos)
	{
		waypoints.add(pos);
	}
	
	public void reset()
	{
		waypoints.clear();
	}
	
	public void queuePath(Position to)
	{
		Position last = this;
		
		if (waypoints.size() > 0)
		{
			last = waypoints.getLast();
		}
		
		int deltaX = to.x() - last.x();
		int deltaY = to.y() - last.y();
		int steps = Math.max(Math.abs(deltaX), Math.abs(deltaY));
		
		for (int i = 0; i < steps; i++)
		{
			if (deltaX < 0)
			{
				deltaX++;
			}
			else if (deltaX > 0)
			{
				deltaX--;
			}

			if (deltaY < 0)
			{
				deltaY++;
			}
			else if (deltaY > 0)
			{
				deltaY--;
			}
			
			queue(new Position(to.x() - deltaX, to.y() - deltaY));
		}
	}
	
	public boolean runPath()
	{
		return runPath;
	}
	
	public void runPath(boolean value)
	{
		runPath = value;
	}
	
	public boolean running()
	{
		return running;
	}
	
	public void running(boolean value)
	{
		running = value;
	}
}
