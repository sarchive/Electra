package net.electra.io;

// we store bit data, and nothing else.
public class BitBuffer
{
	public static final int[] BITMASKS = {
		0, 0x1, 0x3, 0x7,
		0xf, 0x1f, 0x3f, 0x7f,
		0xff, 0x1ff, 0x3ff, 0x7ff,
		0xfff, 0x1fff, 0x3fff, 0x7fff,
		0xffff, 0x1ffff, 0x3ffff, 0x7ffff,
		0xfffff, 0x1fffff, 0x3fffff, 0x7fffff,
		0xffffff, 0x1ffffff, 0x3ffffff, 0x7ffffff,
		0xfffffff, 0x1fffffff, 0x3fffffff, 0x7fffffff
	};
	
	private int bitPosition;
	private int dataLength;
	private byte[] data;
	
	public BitBuffer()
	{
		clear();
	}
	
	public byte[] bytes()
	{
		dataLength = (bitPosition + 7) / 8;
		byte[] newData = new byte[dataLength];
		System.arraycopy(data, 0, newData, 0, dataLength);
		return newData;
	}
	
	public BitBuffer clear()
	{
		data = new byte[1];
		bitPosition = 0;
		dataLength = 0;
		return this;
	}
	
	public BitBuffer copy(BitBuffer to)
	{
		int position = 0;
		
		for (byte b : data)
		{
			for (int i = 7; i >= 0; i--)
			{
				if (bitPosition > position)
				{
					to.put((b & (1 << i)) > 0);
					position++;
					continue;
				}
				
				return this;
			}
		}
		
		return this;
	}
	
	public BitBuffer put(BitBuffer buffer)
	{
		buffer.copy(this);
		return this;
	}
	
	public BitBuffer put(boolean value)
	{
		return put(1, value ? 1 : 0);
	}
	
	public BitBuffer put(int value)
	{
		int tempValue = value;
		int count = 0;
		
		while (tempValue > 0)
		{
			count++;
			tempValue >>= 1;
		}
		
		return put(count, value);
	}
	
	public BitBuffer put(int amount, int value)
	{
		int bytePos = bitPosition >> 3;
		int bitOffset = 8 - (bitPosition & 7);
        bitPosition += amount;
        dataLength = (bitPosition + 7) / 8;
        ensure(dataLength);

        for (; amount > bitOffset; bitOffset = 8)
        {
            data[bytePos] &= (byte)~BITMASKS[bitOffset];
            data[bytePos++] |= (byte)((value >> (amount - bitOffset)) & BITMASKS[bitOffset]);
            amount -= bitOffset;
        }

        if (amount == bitOffset)
        {
        	data[bytePos] &= (byte)~BITMASKS[bitOffset];
        	data[bytePos] |= (byte)(value & BITMASKS[bitOffset]);
        }
        else
        {
        	data[bytePos] &= (byte)~(BITMASKS[amount] << (bitOffset - amount));
            data[bytePos] |= (byte)((value & BITMASKS[amount]) << (bitOffset - amount));
        }
        
        return this;
	}
	
	private void ensure(int minimum)
	{
		if (minimum >= data.length)
		{
			expand(minimum);
		}
	}
	
	private void expand(int amount)
	{
		int capacity = (data.length + 1) * 2;
		
		if (amount > capacity)
		{
			capacity = amount;
		}
		
		byte[] load = new byte[capacity];
		
		while (dataLength > data.length)
		{
			dataLength--; // there's some math for this, can't think right now
		}
		
		System.arraycopy(data, 0, load, 0, dataLength);
		data = load;
	}
}
