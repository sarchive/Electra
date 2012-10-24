package net.electra.services.game.entities;

import java.lang.ref.SoftReference;

public class UpdateState<T extends Entity>
{
	private final SoftReference<T> entity;
	private int appearanceNumber = 0;
	private long lastSeen = 0;
	
	public UpdateState(SoftReference<T> entity)
	{
		this.entity = entity;
	}
	
	public boolean valid()
	{
		return get() != null;
	}
	
	public void updateLastSeen()
	{
		lastSeen = System.currentTimeMillis();
	}
	
	public long lastSeen()
	{
		return lastSeen;
	}
	
	public int appearanceUpdateCount()
	{
		return appearanceNumber;
	}
	
	public T get()
	{
		return entity.get();
	}
	
	public SoftReference<T> reference()
	{
		return entity;
	}
}
