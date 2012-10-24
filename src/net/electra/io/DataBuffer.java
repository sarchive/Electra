package net.electra.io;

import java.nio.ByteBuffer;

import net.electra.math.ISAACRandomSequencer;

public final class DataBuffer
{
	private ISAACRandomSequencer sequencer;
	private int lengthPosition = 0;
	private int position = 0;
	private int length = 0;
	private byte[] buffer;
	private int mark = 0;
	
	public DataBuffer(byte[] buffer)
	{
		this.buffer = buffer;
		this.length = buffer.length;
	}
	
	public DataBuffer()
	{
		this(new byte[0]);
	}
	
	// it may be in our best interest to, at some point, remove this. maybe even write an optimizer so i can keep the simple code.
	// it's certainly better than having a bunch of copy and paste clutter.
	private long getNumber(int amountOfBytes)
	{
		if (amountOfBytes <= 0)
		{
			throw new IndexOutOfBoundsException("amountOfBytes must be greater than 0");
		}
		
		int offset = amountOfBytes * 8;
		long output = 0;
		
		if (offset > 8)
		{
			offset -= 8;
		}
		
		for (int i = 0; i < amountOfBytes; i++, offset -= 8)
		{
			output += (get() & 0xFF) << offset;
		}
		
		return output;
	}
	
	private DataBuffer putNumber(long value, int amountOfBytes)
	{
		if (amountOfBytes <= 0)
		{
			throw new IndexOutOfBoundsException("amountOfBytes must be greater than 0");
		}
		
		int offset = (amountOfBytes * 8) - 8;
		
		for (int i = 0; i < amountOfBytes; i++, offset -= 8)
		{
			put((byte)(value >> offset));
		}
		
		return this;
	}
	
	public byte get()
	{
		return buffer[position++];
	}
	
	public byte[] get(int amount)
	{
		byte[] buff = new byte[amount];
		
		for (int i = 0; i < buff.length; i++)
		{
			buff[i] = get();
		}
		
		return buff;
	}
	
	public DataBuffer put(int b)
	{
		return put((byte)b);
	}
	
	public DataBuffer put(byte b)
	{
		if (position >= buffer.length)
		{
			expand();
		}
		
		buffer[position++] = b;
		
		if (position >= length)
		{
			length = position;
		}
		
		return this;
	}
	
	public DataBuffer put(byte... b)
	{
		for (int i = 0; i < b.length; i++)
		{
			put(b[i]);
		}
		
		return this;
	}
	
	public DataBuffer put(DataBuffer src)
	{
		while (src.hasRemaining())
		{
			put(src.get());
		}
		
		return this;
	}
	
	public DataBuffer put(ByteBuffer src)
	{
		while (src.hasRemaining())
		{
			 put(src.get());
		}
		
		return this;
	}
	
	public int getHeader()
	{
		return (sequencer == null ? getUnsigned() : getUnsigned() - sequencer.next() & 0xFF);
	}
	
	public int getUnsigned()
	{
		return get() & 0xFF;
	}
	
	public boolean getBoolean()
	{
		return get() == 1;
	}

	public short getShort()
	{
		return (short)getNumber(2);
	}
	
	public int getTribyte()
	{
		return (int)getNumber(3);
	}
	
	public int getInt()
	{
		return (int)getNumber(4);
	}
	
	public long getLong()
	{
		return getNumber(8);
	}
	
	public int getSmart()
	{
		mark();
		int b = get();
		
		if (b < 128)
		{
			return b;
		}
		else
		{
			reset();
			return getShort() - 32768;
		}
	}
	
	public byte[] getReverse(int amount)
	{
		byte[] data = get(amount);
		int right = data.length - 1;
		int left = 0;

		while (left < right)
		{
			byte temp = data[left];
			data[left++] = data[right];
			data[right--] = temp;
		}

		return data;
	}
	
	public byte peek()
	{
		mark();
		byte b = get();
		reset();
		return b;
	}
	
	public String getString()
	{
		return getString(10);
	}
	
	public String getString(int terminator)
	{
		StringBuilder builder = new StringBuilder();
		byte b = 0;
		
		while ((b = get()) != terminator)
		{
			builder.append((char)b);
		}
		
		return builder.toString();
	}
	
	public DataBuffer putHeader(int value)
	{
		return put((byte)(sequencer == null ? value : value + sequencer.next()));
	}
	
	public DataBuffer putByteHeader(int value)
	{
		putHeader(value).put((byte)0);
		lengthPosition = position;
		return this;
	}
	
	public DataBuffer putShortHeader(int value)
	{
		putHeader(value).putShort((short)0);
		lengthPosition = position;
		return this;
	}
	
	public DataBuffer finishByteHeader()
	{
		mark();
		int length = position - lengthPosition;
		position(lengthPosition - 1);
		put(length);
		reset();
		return this;
	}
	
	public DataBuffer finishShortHeader()
	{
		mark();
		int length = position - lengthPosition;
		position(lengthPosition - 2);
		putShort(length);
		reset();
		return this;
	}
	
	public DataBuffer putBoolean(boolean value)
	{
		return put((byte)(value ? 1 : 0));
	}
	
	public DataBuffer putShort(int value)
	{
		return putNumber(value, 2);
	}
	
	public DataBuffer putInt(int value)
	{
		return putNumber(value, 4);
	}
	
	public DataBuffer putLong(long value)
	{
		return putNumber(value, 8);
	}
	
	public DataBuffer putString(String value)
	{
		return putString(value, 10);
	}
	
	public DataBuffer putString(String value, int terminator)
	{
		put(value.getBytes());
		put((byte)terminator);
		return this;
	}

	public DataBuffer expand()
	{
		byte[] oldBuffer = buffer;
		buffer = new byte[(oldBuffer.length + 1) * 2];
		System.arraycopy(oldBuffer, 0, buffer, 0, oldBuffer.length);
		return this;
	}
	
	public DataBuffer mark()
	{
		mark = position;
		return this;
	}
	
	public DataBuffer reset()
	{
		position = mark;
		return this;
	}
	
	public DataBuffer rewind()
	{
		position = 0;
		mark = 0;
		return this;
	}
	
	public int position()
	{
		return position;
	}
	
	public DataBuffer position(int to)
	{
		position = to;
		return this;
	}
	
	public DataBuffer flip()
	{
		length = position;
		position = 0;
		mark = 0;
		return this;
	}
	
	public DataBuffer compact()
	{
		int newLength = length - position;
		System.arraycopy(buffer, position, buffer, 0, newLength);
		length = newLength;
		return this;
	}
	
	public int capacity()
	{
		return buffer.length;
	}
	
	public int length()
	{
		return length;
	}
	
	public boolean hasRemaining()
	{
		return position < length;
	}
	
	public int remaining()
	{
		return length - position;
	}
	
	public DataBuffer sequencer(ISAACRandomSequencer sequencer)
	{
		this.sequencer = sequencer;
		return this;
	}
	
	public ISAACRandomSequencer sequencer()
	{
		return sequencer;
	}

	public ByteBuffer buffer()
	{
		return ByteBuffer.wrap(array());
	}
	
	public byte[] array()
	{
		byte[] out = new byte[length];
		System.arraycopy(buffer, 0, out, 0, out.length);
		return out;
	}
	
	public DataBuffer clear()
	{
		length = buffer.length;
		position = 0;
		mark = 0;
		return this;
	}
}
