package net.electra.services.game.events;

import java.util.ArrayList;

import net.electra.io.DataBuffer;
import net.electra.net.events.NetworkEvent;

public class ChatMessageEvent extends NetworkEvent
{
	// stealing code/naming from apollo :3 - the code is really from big company J (sorta rhymes with chav diX copyrights and stuff)
	public static final char[] FREQUENCY_ORDERED_CHARS = {
		' ', 'e', 't', 'a', 'o', 'i', 'h', 'n', 's', 'r', 'd', 'l', 'u',
		'm', 'w', 'c', 'y', 'f', 'g', 'p', 'b', 'v', 'k', 'x', 'j', 'q',
		'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ', '!',
		'?', '.', ',', ':', ';', '(', ')', '-', '&', '*', '\\', '\'',
		'@', '#', '+', '=', '\243', '$', '%', '"', '[', ']'
	};
	
	public static final int COLOR_YELLOW = 0;
	public static final int COLOR_RED = 1;
	public static final int COLOR_GREEN = 2;
	public static final int COLOR_CYAN = 3;
	public static final int COLOR_PURPLE = 4;
	public static final int COLOR_WHITE = 5;
	public static final int COLOR_FLASH1 = 6;
	public static final int COLOR_FLASH2 = 7;
	public static final int COLOR_FLASH3 = 8;
	public static final int COLOR_GLOW1 = 9;
	public static final int COLOR_GLOW2 = 10;
	public static final int COLOR_GLOW3 = 11;
	
	public static final int EFFECT_NONE = 0;
	public static final int EFFECT_WAVE = 1;
	public static final int EFFECT_WAVE2 = 2;
	public static final int EFFECT_SHAKE = 3;
	public static final int EFFECT_SCROLL = 4;
	public static final int EFFECT_SLIDE = 5;
	
	private String message;
	private int rights;
	private int effect;
	private int color;
	
	public ChatMessageEvent()
	{
		
	}
	
	public ChatMessageEvent(String message, int color, int effect, int rights)
	{
		this.message = message;
		this.effect = effect;
		this.color = color;
		this.rights = rights;
	}
	
	public static String decompress(byte[] data)
	{
		byte[] out = new byte[data.length * 2];
		int pos = 0;
		int carry = -1;
		
		for (int i = 0; i < out.length; i++)
		{
			int tblPos = data[i / 2] >> (4 - 4 * (i % 2)) & 0xF;
		
			if (carry == -1)
			{
				if (tblPos < 13)
				{
					out[pos++] = (byte)FREQUENCY_ORDERED_CHARS[tblPos];
				}
				else
				{
					carry = tblPos;
				}
			}
			else
			{
				out[pos++] = (byte)FREQUENCY_ORDERED_CHARS[((carry << 4) + tblPos) - 195];
				carry = -1;
			}
		}
		
		return new String(out, 0, pos);
	}
	
	public static byte[] compress(String message)
	{
		message = message.toLowerCase();
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		int carry = -1;
		
		for (int in = 0; in < message.length(); in++)
		{
			char c = message.charAt(in);
			int tp = 0;
			
			for (int i = 0; i < FREQUENCY_ORDERED_CHARS.length; i++)
			{
				if (c == FREQUENCY_ORDERED_CHARS[i])
				{
					tp = i;
					break;
				}
			}
			
			if (tp > 12)
			{
				tp += 195;
			}
			
			if (carry == -1)
			{
				if (tp < 13)
				{
					carry = tp;
				}
				else
				{
					bytes.add((byte)tp);
				}
			}
			else if (tp < 13)
			{
				bytes.add((byte)((carry << 4) + tp));
				carry = -1;
			}
			else
			{
				bytes.add((byte)((carry << 4) + (tp >> 4)));
				carry = tp & 0xF;
			}
		}
		
		byte[] data = new byte[bytes.size()];
		int out = 0;
		
		for (Byte b : bytes)
		{
			data[out++] = b;
		}
		
		return data;
	}
	
	@Override
	public void parse(DataBuffer buffer)
	{
		this.effect = buffer.get();
		this.color = buffer.get();
		this.message = decompress(buffer.getReverse(buffer.remaining()));
	}
	
	@Override
	public void build(DataBuffer buffer)
	{
		//buffer.putShort(((color & 0xFF) << 8) + (effect & 0xFF));
		buffer.put(color);
		buffer.put(effect);
		buffer.put(rights);
		byte[] data = compress(message);
		buffer.put(data.length);
		buffer.putReverse(data);
	}
	
	@Override
	public int length()
	{
		return -1;
	}
	
	@Override
	public int id()
	{
		return 4;
	}
	
	public void rights(int value)
	{
		rights = value;
	}
	
	public int rights()
	{
		return rights;
	}
	
	public int color()
	{
		return color;
	}
	
	public int effect()
	{
		return effect;
	}
	
	public String message()
	{
		return message;
	}
}
