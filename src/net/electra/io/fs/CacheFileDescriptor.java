package net.electra.io.fs;

public class CacheFileDescriptor
{
	private final CacheIndex index;
	private final int startBlock;
	private final int identifier;
	private final int size;
	
	public CacheFileDescriptor(CacheIndex index, int identifier, int size, int startBlock)
	{
		this.index = index;
		this.identifier = identifier;
		this.size = size;
		this.startBlock = startBlock;
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
	
	public int identifier()
	{
		return identifier;
	}
}
