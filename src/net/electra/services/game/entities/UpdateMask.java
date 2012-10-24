package net.electra.services.game.entities;

public class UpdateMask implements Cloneable
{
	public static final int DEFAULT = 0;
	
	public static final int PLAYER_FACE = 0x1;
	public static final int PLAYER_FOCUS_POINT = 0x2;
	public static final int PLAYER_FORCED_CHAT = 0x4;
	public static final int PLAYER_ANIMATION = 0x8;
	public static final int PLAYER_APPEARANCE = 0x10;
	public static final int PLAYER_FIRST_HIT = 0x20;
	public static final int PLAYER_WORD_SIZE = 0x40;
	public static final int PLAYER_CHAT = 0x80;
	public static final int PLAYER_GRAPHICS = 0x100;
	public static final int PLAYER_SECOND_HIT = 0x200;
	public static final int PLAYER_FORCE_MOVEMENT = 0x400;
	
	private int mask;
	
	public UpdateMask(int mask)
	{
		this.mask = mask;
	}
	
	@Override
	public Object clone()
	{
		return new UpdateMask(mask);
	}
	
	public void set(int value)
	{
		mask = value;
	}
	
	public void clear()
	{
		set(DEFAULT);
	}
	
	public void add(int flag)
	{
		mask |= flag;
	}
	
	public void clear(int flag)
	{
		mask &= ~flag;
	}
	
	public boolean has(int flag)
	{
		return (mask & flag) != 0;
	}
	
	public boolean is(int flag)
	{
		return mask == flag;
	}
	
	public boolean isEmpty()
	{
		return is(DEFAULT);
	}
	
	@Override
	public boolean equals(Object flag)
	{
		if (flag instanceof Integer)
		{
			return mask == (int)flag;
		}
		else if (flag instanceof UpdateMask)
		{
			return mask == ((UpdateMask)flag).mask();
		}
		
		return super.equals(flag);
	}
	
	public int mask()
	{
		return mask;
	}
}
