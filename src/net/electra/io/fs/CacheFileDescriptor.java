package net.electra.io.fs;

public class CacheFileDescriptor
{
	private final CacheIndex index;
	private final int startBlock;
	private final int id;
	private final int size;
	
	public CacheFileDescriptor(CacheIndex index, int id, int size, int startBlock)
	{
		this.index = index;
		this.id = id;
		this.size = size;
		this.startBlock = startBlock;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof CacheFileDescriptor)
		{
			CacheFileDescriptor o = (CacheFileDescriptor)other;
			return o.id == id && o.size == size && o.startBlock == startBlock && o.index.id() == index.id();
		}
		
		return false;
	}

	public int size()
	{
		return size;
	}
	
	public int startBlock()
	{
		return startBlock;
	}
	
	public CacheIndex index()
	{
		return index;
	}
	
	public int id()
	{
		return id;
	}
}
