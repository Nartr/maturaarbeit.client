package ch.robin.oester.jumpandrun.client.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import ch.robin.oester.jumpandrun.client.ClientStarter;
import ch.robin.oester.jumpandrun.client.network.packets.CountdownPacket;
import ch.robin.oester.jumpandrun.client.network.packets.MessagePacket;
import ch.robin.oester.jumpandrun.client.network.packets.PlayerConnectedPacket;
import ch.robin.oester.jumpandrun.client.network.packets.PlayerDisconnectedPacket;
import ch.robin.oester.jumpandrun.client.network.packets.PlayerMovePacket;
import ch.robin.oester.jumpandrun.client.network.packets.PlayerRespondPacket;
import ch.robin.oester.jumpandrun.client.network.packets.Reason;
import ch.robin.oester.jumpandrun.client.network.packets.ServerChangeStatusPacket;
import ch.robin.oester.jumpandrun.client.screens.GameState;
import ch.robin.oester.jumpandrun.client.tools.Player;

public class ClientListener extends Listener {
	
	private final ClientStarter client;
	
	public ClientListener(ClientStarter c) {
		this.client = c;
	}

	@Override
	public void connected(Connection connection) {}										//fired when client connects to server
																						//method is not used, because login represents connecting
	
	@Override
	public void disconnected(Connection connection) {									//fired when client disconnects from server
		Gdx.app.postRunnable(() -> {													//switch from network thread to client thread
			Gdx.app.exit();																//close the program
		});
	}
	
	@Override
	public void received(Connection connection, Object object) {						//fired when packet is received
		if(object instanceof PlayerConnectedPacket) {
			PlayerConnectedPacket packet = (PlayerConnectedPacket) object;
			if(client.getName() == null) {												//if client is not yet logged in:
				client.setName(packet.newPlayer);										//set client-name and color
				client.setColor(packet.newPlayerColor);
				for(String name : packet.players.keySet()) {							//go through all online players
					Texture tex = client.getAssetManager().get("MarioSprite" + 			//add their texture
							packet.players.get(name) + ".png", Texture.class);
					client.getPlayers().add(new Player									//add them
							(name, packet.players.get(name), tex)); 		
				}
			} else {																	
				Texture tex = client.getAssetManager().get("MarioSprite" + 
						packet.newPlayerColor + ".png", Texture.class);
				client.getPlayers().add(new Player										//if player already logged in, add the new player
						(packet.newPlayer, packet.newPlayerColor, tex));
			}
		}
		if(object instanceof PlayerDisconnectedPacket) {								
			PlayerDisconnectedPacket packet = (PlayerDisconnectedPacket) object;
			Player p = client.getByName(packet.player);
			client.getPlayers().remove(p);												//if a player disconnects, remove him from player list
		}
		if(object instanceof PlayerMovePacket) {										//if you receive a move packet, update the specific
			PlayerMovePacket packet = (PlayerMovePacket) object;						//player's position and sprite
			Player p = client.getByName(packet.name);
			p.setX(packet.x);
			p.setY(packet.y);
			p.setSprite(packet.sprite);
			p.setRight(packet.right);
		}
		if(object instanceof ServerChangeStatusPacket) {								//received if game state changes by the server
			ServerChangeStatusPacket packet = (ServerChangeStatusPacket) object;
			switch (packet.newState) {
			case GameState.START:
				Gdx.app.postRunnable(() -> {											
					client.getGame().initialize
						(client.getAssetManager(), packet.input);						//input includes worldID, world record and player record
					client.setState(GameState.START);
					client.setScreen(client.getGame());									//if start, set the current screen to game screen
				});
				break;
			case GameState.GAME:
				client.setStartTime(System.currentTimeMillis());						//set the start time to current time stamp
				client.setState(GameState.GAME);
				break;
			case GameState.END_GAME:													//"rank annunciation" state
				Gdx.app.postRunnable(() -> {
					client.setState(GameState.END_GAME);								//if player hasn't finished, update game state
					client.getGame().setRank(Integer.parseInt(packet.input));			//set the rank, included in input
				});
				break;
			case GameState.WAITING:
				Gdx.app.postRunnable(() -> {
					for(Player all : client.getPlayers()) {
						all.reset();													//if server restarts, reset all players
					}
					client.resetSpriteBatch();											//reset sprite batch to clean up the screen and all resources
					client.getWaiting().initialize();									//initialize waiting screen
					client.setScreen(client.getWaiting());								//set actual screen to waiting
				});
				break;
			default:
				break;
			}
		}
		if(object instanceof CountdownPacket) {											//received as spectator or in waiting lobby
			CountdownPacket packet = (CountdownPacket) object;
			client.getWaiting().getCountdown().setText(									//update remaining seconds
					String.valueOf(packet.seconds));
		}
		if(object instanceof MessagePacket) {
			MessagePacket packet = (MessagePacket) object;
			client.getWaiting().getText().setText(packet.message);						//update the console with received console-string
		}
		if(object instanceof PlayerRespondPacket) {										//fired on trying either to login or to register
			PlayerRespondPacket packet = (PlayerRespondPacket) object;
			switch (packet.reason) {
			case Reason.INVALID_USERNAME:
				client.getMenu().getInfo().setText
					("Kein Benutzer auf den Namen registriert");						//print the information/error
				client.getMenu().getSubmit().setTouchable(Touchable.enabled);			//enable the used button
				break;
			case Reason.WRONG_PASWORD:
				client.getMenu().getInfo().setText("Falsches Passwort");
				client.getMenu().getSubmit().setTouchable(Touchable.enabled);
				break;
			case Reason.USER_EXIST:
				client.getMenu().getInfo().setText
					("Dieser Benutzer existiert bereits");
				client.getMenu().getRegister().setTouchable(Touchable.enabled);
				break;
			case Reason.TOO_MANY_PLAYERS:
				client.getMenu().getInfo().setText
					("Zu viele Spieler sind eingeloggt");
				client.getMenu().getRegister().setTouchable(Touchable.enabled);
				client.getMenu().getSubmit().setTouchable(Touchable.enabled);
				break;
			case Reason.ALREADY_LOGGED_IN:
				client.getMenu().getInfo().setText
					("Ein Spieler mit diesem Namen spielt bereits");
				client.getMenu().getSubmit().setTouchable(Touchable.enabled);
				break;
			case Reason.VALID:
				Gdx.app.postRunnable(() -> {
					client.getWaiting().initialize();					
					client.setScreen(client.getWaiting());								//if the log in is valid, change to waiting screen	
				});
				break;
			default:
				break;
			}
		}
	}
}
