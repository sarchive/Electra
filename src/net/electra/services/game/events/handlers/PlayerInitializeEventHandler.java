package net.electra.services.game.events.handlers;

import net.electra.Settings;
import net.electra.events.EventHandler;
import net.electra.services.game.entities.UpdateMask;
import net.electra.services.game.entities.players.Player;
import net.electra.services.game.events.CloseInterfacesEvent;
import net.electra.services.game.events.PlayerInitializeEvent;
import net.electra.services.game.events.SendMessageEvent;
import net.electra.services.game.events.SetPrivacySettingsEvent;
import net.electra.services.game.events.SetTabInterfaceEvent;

public class PlayerInitializeEventHandler extends EventHandler<PlayerInitializeEvent, Player>
{
	@Override
	public void handle(PlayerInitializeEvent event, Player player)
	{
		player.client().write(new SendMessageEvent("Welcome to " + Settings.SERVER_NAME + "."));
		player.client().write(new SetPrivacySettingsEvent((byte)0, (byte)0, (byte)0));
		player.client().write(new CloseInterfacesEvent());
		player.client().write(new SetTabInterfaceEvent((short)5855, (byte)0)); // TODO: do this better
		player.client().write(new SetTabInterfaceEvent((short)3917, (byte)1));
		player.client().write(new SetTabInterfaceEvent((short)638, (byte)2));
		player.client().write(new SetTabInterfaceEvent((short)3213, (byte)3));
		player.client().write(new SetTabInterfaceEvent((short)1644, (byte)4));
		player.client().write(new SetTabInterfaceEvent((short)5608, (byte)5));
		player.client().write(new SetTabInterfaceEvent((short)1151, (byte)6));
		player.client().write(new SetTabInterfaceEvent((short)-1, (byte)7));
		player.client().write(new SetTabInterfaceEvent((short)5065, (byte)8));
		player.client().write(new SetTabInterfaceEvent((short)5715, (byte)9));
		player.client().write(new SetTabInterfaceEvent((short)2449, (byte)10));
		player.client().write(new SetTabInterfaceEvent((short)904, (byte)11));
		player.client().write(new SetTabInterfaceEvent((short)147, (byte)12));
		player.client().write(new SetTabInterfaceEvent((short)-1, (byte)13));
		player.mask().set(UpdateMask.PLAYER_APPEARANCE);
	}
}
