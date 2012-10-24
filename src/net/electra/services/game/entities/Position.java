package net.electra.services.game.entities;

public class Position implements Cloneable
{
	private int regionX = 0;
	private int regionY = 0;
	private int x = 0;
	private int y = 0;
	private int z = 0;
	
	public Position(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		refresh();
	}
	
	public Position(int x, int y)
	{
		this(x, y, 0);
	}

	public void refresh()
	{
		regionX = ((x >> 3) - 6);
		regionY = ((y >> 3) - 6);
	}
	
	public Position delta(Position other)
	{
		return new Position(other.x() - x, other.y() - y);
	}
	
	public int distance(Position other)
	{
		return (int)Math.abs(Math.sqrt(((other.x() - x) ^ 2) + ((other.y() - y) ^ 2)));
	}
	
	public void move(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	@Override
	public Object clone()
	{
		return new Position(x, y, z);
	}
	
	public void step(int x, int y)
	{
		move(this.x + x, this.y + y);
	}
	
	public int x()
	{
		return x;
	}
	
	public int y()
	{
		return y;
	}
	
	public int z()
	{
		return z;
	}
	
	public int regionX()
	{
		return regionX;
	}
	
	public int regionY()
	{
		return regionY;
	}
	
	public int localX()
	{
		return x - 8 * regionX;
	}
	
	public int localY()
	{
		return y - 8 * regionY;
	}
}
