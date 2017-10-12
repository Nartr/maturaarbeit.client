package ch.robin.oester.jumpandrun.client.tools;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import ch.robin.oester.jumpandrun.client.ClientStarter;
import ch.robin.oester.jumpandrun.client.network.packets.PlayerHitLine;
import ch.robin.oester.jumpandrun.client.screens.GameState;

public class WorldContactListener implements ContactListener {
	
	private final ClientStarter client;
	
	public WorldContactListener(ClientStarter client) {
		this.client = client;
	}

	@Override
	public void beginContact(Contact contact) {
		Fixture fixA = contact.getFixtureA();											//contact is always between to fixtures, a and b
		Fixture fixB = contact.getFixtureB();

		if(fixA.getUserData() == "player" || fixB.getUserData() == "player") {			//if a fixture has the unique data player and the other one
			Fixture object = fixA.getUserData() == "player" ? fixB : fixA;				//the line data
			if(object.getUserData() == "line" && client.getState() == 
					GameState.GAME) {
				PlayerHitLine packet = new PlayerHitLine();								//then send a hit line packet to the server with your
				packet.time = System.currentTimeMillis() - client.getStartTime();		//personal time
				client.sendTCP(packet);
				client.setState(GameState.END_GAME);									//set the game state to end game
				client.getGame().getHud().setText(String.valueOf(packet.time / 1000.0));
			}
		}
		if(fixA.getUserData() == "feet" || fixB.getUserData() == "feet") {				//if the player has contact at the feet then reset jump amount
			client.getGame().getPlayer().setFeetContact(true);
		}
	}

	@Override
	public void endContact(Contact contact) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		
		if(fixA.getUserData() == "feet" || fixB.getUserData() == "feet") {
			client.getGame().getPlayer().setFeetContact(false);							//set the feet contact to false because the player is jumping
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {}
}
