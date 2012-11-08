package net.electra.io;

// essentially just for lots and lots of single bit flags.
public class BitStore
{
	private int count = 0;
	private byte[] store;
	
	public BitStore(int size)
	{
		store = new byte[(size + 7) >> 3];
	}
	
	public void clear()
	{
		store = new byte[(store.length + 7) >> 3];
		count = 0;
	}
	
	public boolean get(int index)
	{
		return (store[index >> 3] & (1 << (index & 7))) != 0;
	}
	
	public void toggle(int index)
	{
		set(index, get(index));
	}
	
	public void set(int index, boolean value)
	{
		boolean got = get(index);
		
		if (value)
		{
			if (!got)
			{
				count++;
			}
			
			store[index >> 3] |= (byte)(1 << (index & 7));
		}
		else
		{
			if (got)
			{
				count--;
			}
			
			store[index >> 3] &= (byte)~(1 << (index & 7));
		}
	}
	
	public int count()
	{
		return count;
	}
}
