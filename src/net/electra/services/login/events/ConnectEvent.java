package net.electra.services.login.events;

import java.math.BigInteger;

import net.electra.Settings;
import net.electra.io.DataBuffer;
import net.electra.net.events.NetworkEvent;

public class ConnectEvent extends NetworkEvent
{
	private boolean reconnecting;
	private int clientVersion;
	private boolean lowMemory;
	private int[] randomSeed;
	private int[] checksums;
	private String username;
	private String password;
	private int uid;
	
	public ConnectEvent()
	{
		
	}
	
	public ConnectEvent(boolean reconnecting)
	{
		this.reconnecting = reconnecting;
	}
	
	public ConnectEvent(boolean reconnecting, int clientVersion, boolean lowMemory, int[] randomSeed, int[] checksums, String username, String password, int uid)
	{
		this.reconnecting = reconnecting;
		this.clientVersion = clientVersion;
		this.lowMemory = lowMemory;
		this.randomSeed = randomSeed;
		this.checksums = checksums;
		this.username = username;
		this.password = password;
		this.uid = uid;
	}
	
	public ConnectEvent(int clientVersion, boolean lowMemory, int[] randomSeed, int[] checksums, String username, String password, int uid)
	{
		this(false, clientVersion, lowMemory, randomSeed, checksums, username, password, uid);
	}
	
	@Override
	public void parse(DataBuffer buffer)
	{
		int expected = buffer.length() - (36 + 1 + 1 + 2 + 1); // checksums, client version, magic, memory, encryption length
		
		if (buffer.getUnsigned() != 255)
		{
			throw new IllegalArgumentException("The first value of the login block must be 255.");
		}
		
		this.clientVersion = buffer.getShort();
		this.lowMemory = buffer.getBoolean();
		this.checksums = new int[9];
		
		for (int i = 0; i < checksums.length; i++)
		{
			checksums[i] = buffer.getInt();
			//System.out.println("Checksum " + i + ": " + checksums[i]);
		}
		
		// check the length reported by the client with our expected size based on how big the packet actually is
		if (buffer.getUnsigned() != expected)
		{
			throw new IllegalArgumentException("The expected encrypted block size does not equal the expected block size.");
		}
		
		DataBuffer decryptedBlock = new DataBuffer(new BigInteger(buffer.get(expected)).modPow(Settings.PRIVATE_RSA_EXPONENT, Settings.PRIVATE_RSA_MODULUS).toByteArray());
		
		// if this value isn't 10 then we can assume that decrypting the data didn't work. this is just a byte used for verification of the data's integrity
		if (decryptedBlock.get() != 10)
		{
			throw new IllegalArgumentException("Encrypted block failed to decrypt, probably invalid public/private keyset.");
		}
		
		this.randomSeed = new int[4];
		
		for (int i = 0; i < randomSeed.length; i++)
		{
			randomSeed[i] = decryptedBlock.getInt();
		}
		
		this.uid = decryptedBlock.getInt(); // the uid is used only for reconnection, nothing else.
		this.username = decryptedBlock.getString();
		this.password = decryptedBlock.getString();
	}
	
	@Override
	public void build(DataBuffer buffer)
	{
		throw new UnsupportedOperationException(); // TODO: add this for fun. really to just meet the standard of other network events.
	}
	
	public boolean reconnecting()
	{
		return reconnecting;
	}
	
	public int clientVersion()
	{
		return clientVersion;
	}
	
	public boolean lowMemory()
	{
		return lowMemory;
	}
	
	public int[] randomSeed()
	{
		return randomSeed;
	}
	
	public int[] checksums()
	{
		return checksums;
	}
	
	public String username()
	{
		return username;
	}
	
	public String password()
	{
		return password;
	}
	
	public int uid()
	{
		return uid;
	}
	
	@Override
	public int length()
	{
		return -1;
	}
	
	@Override
	public int id()
	{
		return 16;
	}
}
