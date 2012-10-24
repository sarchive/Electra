package net.electra.events;

public abstract class Event
{
	private boolean chainBroken = false;
	
	public void breakChain()
	{
		chainBroken = true;
	}
	
	public boolean chainBroken()
	{
		return chainBroken;
	}
}
