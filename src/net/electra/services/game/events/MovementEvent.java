package net.electra.services.game.events;

import java.util.ArrayList;

import net.electra.io.DataBuffer;
import net.electra.net.events.NetworkEvent;
import net.electra.services.game.entities.Position;

public class MovementEvent extends NetworkEvent
{
	public static final int TYPE_REGULAR = 0;
	public static final int TYPE_MINIMAP = 1;
	public static final int TYPE_ENTITY_CLICK = 2;

	private Position[] steps;
	private boolean running;
	private Position first;
	private int type;
	
	public MovementEvent()
	{
		// for parsing
	}
	
	public MovementEvent(int type, Position first, Position[] steps, boolean running)
	{
		this.type = type;
		this.first = first;
		this.steps = steps;
		this.running = running;
	}
	
	@Override
	public void parse(DataBuffer buffer)
	{
		this.type = buffer.get();
		this.first = new Position(buffer.getShort(), buffer.getShort());
		this.running = buffer.getBoolean();
		ArrayList<Position> positions = new ArrayList<Position>();
		positions.add(first); // final position
		
		while (buffer.hasRemaining())
		{
			positions.add(new Position(first.x() + buffer.get(), first.y() + buffer.get())); // queued up (first step is the last one read)
		}
		
		this.steps = positions.toArray(new Position[0]);
	}
	
	@Override
	public void build(DataBuffer buffer)
	{
		buffer.put(type).putShort(first.x()).putShort(first.y()).putBoolean(running);
		
		for (Position pos : steps)
		{
			buffer.put(pos.x() - first.x()).put(pos.y() - first.y());
		}
	}
	
	public int type()
	{
		return type;
	}
	
	public boolean running()
	{
		return running;
	}
	
	public Position[] steps()
	{
		return steps;
	}
	
	public Position first()
	{
		return first;
	}
	
	@Override
	public int length()
	{
		return -1;
	}
	
	@Override
	public int id()
	{
		return 98;
	}
}
