package net.electra.services.game.entities.players;

public enum PlayerRights
{
	NORMAL (0),
	MODERATOR (1),
	ADMIN (2),
	OWNER (3);
	
	private final byte id;
	
	private PlayerRights(int id)
	{
		this.id = (byte)id;
	}
	
	public byte id()
	{
		return id;
	}
}
