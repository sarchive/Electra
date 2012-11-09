package net.electra.io.fs;

import net.electra.io.DataBuffer;

public class CacheFile
{
	protected final CacheFileDescriptor descriptor;
	protected DataBuffer buffer;
	
	public CacheFile(CacheFileDescriptor descriptor, DataBuffer buffer)
	{
		this.descriptor = descriptor;
		this.buffer = buffer;
	}
	
	public CacheFileDescriptor descriptor()
	{
		return descriptor;
	}
	
	public DataBuffer buffer()
	{
		return buffer;
	}
}
