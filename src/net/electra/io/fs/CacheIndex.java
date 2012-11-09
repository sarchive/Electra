package net.electra.io.fs;

import java.util.HashMap;

import net.electra.io.DataBuffer;

public class CacheIndex extends HashMap<Integer, CacheFileDescriptor>
{
	private static final long serialVersionUID = 7242514620276803136L;
	public static int DATA_SIZE = 6;
	
	private final int id;
	
	public CacheIndex(int id)
	{
		this.id = id;
	}
	
	public void build(DataBuffer buffer)
	{
		while (buffer.hasRemaining())
		{
			int file = (int)buffer.position() / DATA_SIZE;
			put(file, new CacheFileDescriptor(this, file, buffer.getTribyte(), buffer.getTribyte()));
		}
	}
	
	public int id()
	{
		return id;
	}
}
