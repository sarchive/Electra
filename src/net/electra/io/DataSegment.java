package net.electra.io;

public abstract class DataSegment<T>
{
	public abstract void build(T buffer);
}
