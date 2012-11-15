package net.electra.services.game.events.handlers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import net.electra.events.EventHandler;
import net.electra.io.BitBuffer;
import net.electra.io.DataBuffer;
import net.electra.net.events.NetworkEvent;
import net.electra.services.game.entities.Direction;
import net.electra.services.game.entities.Position;
import net.electra.services.game.entities.UpdateMask;
import net.electra.services.game.entities.players.Player;
import net.electra.services.game.entities.players.PlayerRights;
import net.electra.services.game.events.PlayerTickEvent;

public class PlayerUpdateEventHandler extends EventHandler<PlayerTickEvent, Player>
{
	@Override
	@SuppressWarnings("unused")
	public void handle(PlayerTickEvent event, Player player)
	{
		final BitBuffer bits = new BitBuffer();
		final DataBuffer blocks = new DataBuffer();
		int old = player.mask().get();
		player.mask().clear(UpdateMask.PLAYER_CHAT);
		
		if (player.placementRequired() || player.blockRequired() || player.movementRequired())
		{
			bits.put(true);
			updatePlayerState(bits, player, true);
			
			if (player.blockRequired())
			{
				updateBlock(blocks, player);
			}
		}
		else
		{
			bits.put(false);
		}
		
		player.mask().set(old);
		bits.put(8, player.localPlayers().size());
		List<WeakReference<Player>> toRemove = new ArrayList<WeakReference<Player>>();
		
		for (WeakReference<Player> otherRef : player.localPlayers())
		{
			Player other = otherRef.get();
			
			if (other != null && player.position().distance(other.position()) <= 15 && !other.placementRequired())
			{
				if (other.blockRequired() || other.movementRequired())
				{
					bits.put(true);
					updatePlayerState(bits, other, false);
					
					if (other.blockRequired())
					{
						updateBlock(blocks, other);
					}
				}
				else
				{
					bits.put(false);
				}
			}
			else
			{
				bits.put(true);
				bits.put(2, 3);
			}
		}
		
		// TODO: implement this the way it SHOULD be instead of some typical PI-esque implementation
		for (Player other : player.service().players())
		{
			if (other != null && player != other && !player.hasLocalPlayer(other) && player.position().distance(other.position()) <= 15)
			{
				player.localPlayers().add(new WeakReference<Player>(other));
				addNewPlayer(bits, player, other, true, false);
				old = other.mask().get();
				other.mask().add(UpdateMask.PLAYER_APPEARANCE);
				updateBlock(blocks, other);
				other.mask().set(old);
			}
		}
		
		bits.put(11, 2047);
		
		// these network events, like others, are special cases dealing with abnormal circumstances.
		player.client().write(new NetworkEvent()
		{
			public void parse(DataBuffer buffer)
			{
				// no need
			}
			
			public void build(DataBuffer buffer)
			{
				buffer.put(bits.bytes());
				
				if (blocks.length() > 0)
				{
					blocks.flip();
					buffer.put(blocks);
				}
			}
			
			public int length()
			{
				return -2;
			}
			
			public int id()
			{
				return 81;
			}
		});
	}
	
	public void addNewPlayer(BitBuffer buffer, Player current, Player other, boolean update, boolean discardWalking)
	{
		Position delta = current.position().delta(other.position());
		buffer.put(11, other.id() + 1);
		buffer.put(update);
		buffer.put(discardWalking);
		buffer.put(5, delta.y());
		buffer.put(5, delta.x());
	}
	
	public void updateBlock(DataBuffer buffer, Player player)
	{
		if (player.mask().get() >= 256)
		{
			player.mask().set(UpdateMask.PLAYER_WORD_SIZE);
		}
		
		if (player.mask().has(UpdateMask.PLAYER_WORD_SIZE))
		{
			buffer.putShort(player.mask().get());
		}
		else
		{
			buffer.put(player.mask().get());
		}
		
		if (player.mask().has(UpdateMask.PLAYER_CHAT))
		{
			player.message().rights(PlayerRights.ADMIN.id());
			player.message().build(buffer);
		}
		
		if (player.mask().has(UpdateMask.PLAYER_APPEARANCE))
		{
			DataBuffer appearence = new DataBuffer();
			// headpiece, cape, amulet, weapon, chest (or torso), shield, arms (if chest not full cover), legs (or pants),
			// head (if headpiece not full cover), gloves (or hands), boots (or shoes), beard (if gender is not female)
			// haircolor, shirtcolor, pantscolor, bootscolor, skincolor
			int[] equipment 	= { 0, 0, 0, 0, 0,  0, 0,  0,  0, 0,  0,  0 };
			int[] looks 		= { 0, 0, 0, 0, 25, 0, 29, 39, 7, 35, 44, 14 };
			int[] colors		= { 7, 8, 9, 5, 0 };
			int[] movementAnims = { 0x328, 0x337, 0x334, 0x335, 0x336, 0x333, 0x338 };
			// standing, turning, turning around, quarter clockwise, quarter counter clockwise, running
			
			appearence.put(equipment[11]); // gender
			appearence.put(0); // skull
			
			for (int i = 0; i < 12; i++)
			{
				if (equipment[i] > 0)
				{
					if (i == 6 || i == 8 || i == 11) // if the equipment is arms, head, or beard
					{
						appearence.put(0);
						continue;
					}
					
					appearence.putShort(512 + equipment[i]);
				}
				else if (i > 3 && i != 5) // if equipment isn't headpiece, cape, amulet, weapon, or shield
				{
					appearence.putShort(256 + looks[i]);
				}
				else
				{
					appearence.put(0);
				}
			}
			
			for (int i = 0; i < colors.length; i++)
			{
				appearence.put(colors[i]);
			}
			
			for (int i = 0; i < movementAnims.length; i++)
			{
				appearence.putShort(movementAnims[i]);
			}
			
			appearence.putLong(player.usernameHash());
			appearence.put(126); // combat level
			appearence.putShort(0); // skill
			buffer.put(appearence.length());
			appearence.flip();
			buffer.put(appearence);
		}
	}
	
	public void updatePlayerState(BitBuffer buffer, Player player, boolean current)
	{
		if (current)
		{
			if (player.placementRequired())
			{
				buffer.put(2, 3);
				buffer.put(2, player.position().z());
				buffer.put(true);
				buffer.put(player.blockRequired());
				buffer.put(7, player.position().localY());
				buffer.put(7, player.position().localX());
				return;
			}
		}
		
		if (player.firstDirection() == Direction.NONE)
		{
			buffer.put(2, 0);
		}
		else if (player.secondDirection() == Direction.NONE)
		{
			buffer.put(2, 1);
			buffer.put(3, player.firstDirection().value());
			buffer.put(player.blockRequired());
		}
		else
		{
			buffer.put(2, 2);
			buffer.put(3, player.firstDirection().value());
			buffer.put(3, player.secondDirection().value());
			buffer.put(player.blockRequired());
		}
	}
}
