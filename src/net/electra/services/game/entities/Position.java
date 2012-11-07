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
	
	public void copy(Position position)
	{
		this.x = position.x;
		this.y = position.y;
		this.z = position.z;
		refresh();
	}
	
	public void base(int width, int height)
	{
		x = x / width * width;
		y = y / height * height;
	}

	public void refresh()
	{
		regionX = ((x >> 3) - 6); // tmyk: shifting a number 3 right is the same as dividing by 8
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
	public int hashCode()
	{
		return ((z << 30) & 0xC0000000) | ((y << 15) & 0x3FFF8000) | (x & 0x7FFF); // stolen from apollo :)
	}
	
	@Override
	public String toString()
	{
		return "Position[x: " + x + ", y: " + y + ", z: " + z + "]";
	}
	
	@Override
	public Object clone()
	{
		return new Position(x, y, z);
	}
	
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		if (obj != null && obj instanceof Position)
		{
			Position other = (Position)obj;
			
			if (other.x == x && other.y == y && other.z == z)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void step(int x, int y)
	{
		move(this.x + x, this.y + y);
	}
	
	public void step(Direction direction)
	{
		int x = 0;
		int y = 0;
		
		switch (direction)
		{
			case NORTH_WEST:
				x--;
				y++;
				break;
			case NORTH_EAST:
				x++;
				y++;
				break;
			case NORTH:
				y++;
				break;
			case EAST:
				x++;
				break;
			case SOUTH_WEST:
				x--;
				y--;
				break;
			case SOUTH_EAST:
				x++;
				y--;
				break;
			case SOUTH:
				y--;
				break;
			case WEST:
				x--;
				break;
			default:
				break;
		}
		
		step(x, y);
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
		return x - 8 * regionX; // tmyk: you could also left shift by 3 to multiply by 8
	}
	
	public int localY()
	{
		return y - 8 * regionY;
	}
}
