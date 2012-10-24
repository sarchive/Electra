package net.electra.services.login.events.handlers;

import net.electra.Settings;
import net.electra.events.EventHandler;
import net.electra.math.ISAACRandomSequencer;
import net.electra.net.DisconnectReason;
import net.electra.net.events.NetworkEvent;
import net.electra.services.Service;
import net.electra.services.game.GameService;
import net.electra.services.game.entities.players.Player;
import net.electra.services.game.entities.players.PlayerRights;
import net.electra.services.game.events.PlayerInitializeEvent;
import net.electra.services.login.LoginResponse;
import net.electra.services.login.PotentialPlayer;
import net.electra.services.login.events.ConnectEvent;
import net.electra.services.login.events.ConnectedEvent;
import net.electra.services.login.events.ErrorEvent;
import net.electra.services.login.events.ReconnectedEvent;

public class ConnectEventHandler extends EventHandler<ConnectEvent, PotentialPlayer>
{
	@Override
	public void handle(ConnectEvent event, PotentialPlayer context)
	{
		LoginResponse response = LoginResponse.INCOMPLETE_LOGIN;
		PlayerRights rights = PlayerRights.ADMIN;
		NetworkEvent outbound = null;
		Player completed = null;
		
		if (event.username().isEmpty() || event.password().isEmpty())
		{
			response = LoginResponse.WRONG_PASSWORD;
		}
		else if (event.clientVersion() < Settings.MINIMUM_CLIENT_VERSION)
		{
			response = LoginResponse.CLIENT_UPDATED;
		}
		else
		{
			Player onlineAlready = context.service().server().<GameService>service(Service.GAME).player(event.username());
			
			if (onlineAlready != null)
			{
				if (event.reconnecting() && onlineAlready.uid() == event.uid())
				{
					response = LoginResponse.PICKED_UP_SESSION; // connection from the same client (uid)
				}
				else
				{
					response = LoginResponse.ALREADY_LOGGED_IN;
				}
			}
			else
			{
				response = LoginResponse.FINALIZE_LOGIN;
			}
		}
		
		if (response == LoginResponse.FINALIZE_LOGIN || response == LoginResponse.PICKED_UP_SESSION)
		{
			context.username(event.username());
			context.uid(event.uid());
			completed = context.service().completeLogin(context);
			
			if (completed == null)
			{
				response = LoginResponse.WORLD_FULL;
			}
			else
			{
				if (response == LoginResponse.FINALIZE_LOGIN)
				{
					outbound = new ConnectedEvent(rights.id(), false);
				}
				else
				{
					outbound = new ReconnectedEvent();
				}
			}
		}
		
		if (outbound == null)
		{
			outbound = new ErrorEvent(response.code());
		}
		
		context.client().write(outbound);

		if (outbound instanceof ErrorEvent)
		{
			System.out.println("Client disconnecting: " + response);
			context.client().disconnect(DisconnectReason.INVALID_LOGIN);
		}
		else
		{
			int[] keys = event.randomSeed();
			context.client().in().sequencer(new ISAACRandomSequencer(keys));
			
			for (int i = 0; i < keys.length; i++)
			{
				keys[i] += 50;
			}
			
			context.client().out().sequencer(new ISAACRandomSequencer(keys));
			
			if (outbound instanceof ConnectedEvent)
			{
				completed.fire(new PlayerInitializeEvent());
			}
		}
	}
}
