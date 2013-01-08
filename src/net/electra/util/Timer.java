package net.electra.util;

public class Timer
{
	private long markedTime = 0;
	
	public Timer()
	{
		reset();
	}
	
	public void reset()
	{
		markedTime = System.currentTimeMillis();
	}
	
	public long elapsed()
	{
		return System.currentTimeMillis() - markedTime;
	}
}