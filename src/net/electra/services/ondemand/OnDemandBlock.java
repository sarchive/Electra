package net.electra.services.ondemand;

public class OnDemandBlock
{
	private final byte[] data;
	private final int id;
	
	public OnDemandBlock(int id, byte[] data)
	{
		this.id = id;
		this.data = data;
	}
	
	public byte[] data()
	{
		return data;
	}
	
	public int id()
	{
		return id;
	}
}
