package net.electra.services.ondemand;

import net.electra.Settings;
import net.electra.io.fs.CacheFile;

public class OnDemandRequest implements Comparable<OnDemandRequest>
{
	private final CacheFile file;
	private final int priority;
	private int current = 0;
	
	public OnDemandRequest(CacheFile file, int priority)
	{
		this.priority = priority;
		this.file = file;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof OnDemandRequest)
		{
			OnDemandRequest o = (OnDemandRequest)other;
			return o.file.descriptor().equals(file.descriptor());
		}
		
		return false;
	}
	
	public OnDemandBlock nextBlock()
	{
		int amount = (file.buffer().remaining() >= Settings.ONDEMAND_BLOCK_SIZE ? Settings.ONDEMAND_BLOCK_SIZE : file.buffer().remaining());
		return new OnDemandBlock(current++, file.buffer().get(amount));
	}
	
	public int totalBlocks()
	{
		return file.descriptor().size() / Settings.ONDEMAND_BLOCK_SIZE + (file.descriptor().size() % Settings.ONDEMAND_BLOCK_SIZE > 0 ? 1 : 0);
	}
	
	public int currentBlock()
	{
		return current;
	}
	
	public int priority()
	{
		return priority;
	}
	
	public CacheFile file()
	{
		return file;
	}

	@Override
	public int compareTo(OnDemandRequest arg0)
	{
		return priority - arg0.priority;
	}
}
