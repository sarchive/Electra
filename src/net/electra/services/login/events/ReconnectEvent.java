package net.electra.services.login.events;

public class ReconnectEvent extends ConnectEvent
{
	public ReconnectEvent()
	{
		super(true);
	}
	
	public ReconnectEvent(int clientVersion, boolean lowMemory, int[] randomSeed, int[] checksums, String username, String password, int uid)
	{
		super(true, clientVersion, lowMemory, randomSeed, checksums, username, password, uid);
	}
	
	@Override
	public int id()
	{
		return 18;
	}
}
