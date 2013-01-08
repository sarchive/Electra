package net.electra.net;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import net.electra.Settings;
import net.electra.io.DataBuffer;
import net.electra.net.events.NetworkEvent;
import net.electra.util.Timer;

public abstract class Client
{
	protected final LinkedList<NetworkEvent> events = new LinkedList<NetworkEvent>();
	private DisconnectReason disconnectReason;
	private final SelectionKey selectionKey;
	private final ByteBuffer inboundTemp;
	protected final DataBuffer outbound;
	protected final DataBuffer inbound;
	private final Timer timeoutTimer;
	
	public Client(SelectionKey selectionKey)
	{
		this.selectionKey = selectionKey;
		this.inboundTemp = ByteBuffer.allocateDirect(512);
		this.inbound = new DataBuffer(new byte[512]);
		this.outbound = new DataBuffer(new byte[4096]);
		this.timeoutTimer = new Timer();
		selectionKey.attach(this);
	}
	
	public void disconnect(DisconnectReason reason)
	{
		synchronized (disconnectReason) // nothing else should be locking to this, so we'll use this.
		{
			if (disconnectReason != null)
			{
				return;
			}
			
			disconnectReason = reason;
			flush(true);
			System.out.println("Client (" + socketChannel().socket() + ") disconnecting: " + reason);
			
			try
			{
				socketChannel().close();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			
			selectionKey.cancel();
		}
	}
	
	public void flush()
	{
		flush(false);
	}
	
	public void flush(boolean disconnecting)
	{
		try
		{
			outbound.flip(); // back to the beginning
			
			if (outbound.hasRemaining())
			{
				// for testing purposes, TODO: change?
				//int writeAmount = (outbound.remaining() >= Settings.CLIENT_BUFFER_SIZE ? Settings.CLIENT_BUFFER_SIZE : outbound.remaining());
				int amount = socketChannel().write(outbound.buffer()); // read writeAmount bytes and write
				// for some reason jaggrab doesn't do well with large amounts of data, TODO: look into that.
				
				if (amount > 0)
				{
					timeoutTimer.reset();
				}
			}
			
			if (outbound.hasRemaining()) // if stuff is still remaining
			{
				outbound.compact(); // if there's still data left then move it to the front of the buffer
			}
			else
			{
				outbound.clear();
			}
		}
		catch (Exception ex)
		{
			if (!disconnecting)
			{
				disconnect(DisconnectReason.DATA_TRANSFER_ERROR);
			}
		}
	}
	
	/**
	 * Reads a packet with the 
	 * @return True if the a packet was read completely, false if there was not enough data.
	 */
	public abstract boolean readPacket();
	
	public void read()
	{
		if (!connected())
		{
			disconnect(DisconnectReason.DISCONNECTED);
			return;
		}
		
		if (timeoutTimer.elapsed() >= Settings.CLIENT_TIMEOUT)
		{
			disconnect(DisconnectReason.DATA_TRANSFER_TIMEOUT);
			return;
		}
		
		int readAmount = -1;
		
		try
		{
			if ((readAmount = socketChannel().read(inboundTemp)) == -1) // read into temp (pos = readAmount)
			{
				disconnect(DisconnectReason.DATA_TRANSFER_ERROR);
				return;
			}
			
			if (readAmount > 0)
			{
				timeoutTimer.reset();
			}
			
			boolean hasEnoughData = true;
			
			inboundTemp.flip(); // start reading from the beginning (pos = 0)
			inbound.put(inboundTemp); // put all the data into the inbound buffer (pod = inboundTemp length)
			inbound.flip(); // flip it so we can read from the beginning (pos = 0)
			//inbound.compact(); // doubt i even need this
			inboundTemp.clear(); // clear temporary buffer
			
			while (hasEnoughData && inbound.hasRemaining())
			{
				inbound.mark();
				hasEnoughData = readPacket();
			}
			
			if (!hasEnoughData)
			{
				inbound.reset(); // go back to where we started
				inbound.compact(); // move everything to the front
			}
			else
			{
				inbound.clear();
			}
		}
		catch (Exception ex)
		{
			System.err.println("Error parsing (read: " + readAmount + ")");
			//ex.printStackTrace();
			disconnect(DisconnectReason.DATA_TRANSFER_ERROR);
		}
	}
	
	public SocketChannel socketChannel()
	{
		return (SocketChannel)selectionKey().channel();
	}
	
	public SelectionKey selectionKey()
	{
		return selectionKey;
	}
	
	public DisconnectReason disconnectReason()
	{
		return disconnectReason;
	}
	
	public boolean connected()
	{
		return socketChannel().isConnected() && disconnectReason == null;
	}
	
	public LinkedList<NetworkEvent> events()
	{
		return events;
	}
}
