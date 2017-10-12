package ch.robin.oester.jumpandrun.client.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import ch.robin.oester.jumpandrun.client.ClientStarter;
import ch.robin.oester.jumpandrun.client.screens.GameState;

public class Mario extends Sprite {
	
	private final ClientStarter client;

	private Body body;
	private TextureRegion stand;
	private TextureRegion end;
	private Animation<TextureRegion> run;
	private Animation<TextureRegion> jump;
	
	private State current;
	private State previous;
	private boolean runningRight;
	private float stateTimer;
	
	private TextureRegion before;
	private int actual;
	
	private float mapWidth;
	private boolean feet;
	private int jumps;
	
	public Mario(ClientStarter client, World world, Sprite s, final float mapWidth) {
		super(s);																		//sprite constructor needs a texture
		
		this.client = client;
		
		this.current = State.STANDING;													//set up the current and previous state to standing
		this.previous = State.STANDING;
		this.runningRight = true;														//mario is looking to the right hand side
		this.stateTimer = 0;															//state timer is 0

		this.stand = new TextureRegion(getTexture(), 0, 0, 16, 16);						//load the sub image of the whole mario picture
		
		Array<TextureRegion> frames = new Array<>();
		for(int i = 1; i < 4; i++) {
			frames.add(new TextureRegion(getTexture(), i * 16, 0, 16, 16));				//put sub pictures 1 - 3 into the frames array
		}
		this.run = new Animation<>(0.1f, frames);										//create the run animation from the pictures of the array
		frames.clear();																	//clear the array to use it later
		
		for(int i = 4; i < 6; i++) {
			frames.add(new TextureRegion(getTexture(), i * 16, 0, 16, 16));				//do the same for the jump animation with sub pictures 4 and 5
		}
		this.jump = new Animation<>(0.1f, frames);										//0.1 refers to the length one picture has to be shown
		
		this.end = new TextureRegion(getTexture(), 6 * 16, 0, 16, 16);					//set the last sub image to the celebrating texture
		
		BodyDef bdef = new BodyDef();													//create a body definition
		bdef.position.set(32 / ClientStarter.PIXELS_PER_METRE, 							//set the spawning coordinates 
				32 / ClientStarter.PIXELS_PER_METRE);									//transform them into meter-system
		bdef.type = BodyDef.BodyType.DynamicBody;										//define as dynamic, because mario can move
		this.body = world.createBody(bdef);												//add the body to the world
		
		FixtureDef fdef = new FixtureDef();												//create the fixture definition to calculate collision after
		CircleShape shape = new CircleShape();											//set it to a round shape
		shape.setRadius(7 / ClientStarter.PIXELS_PER_METRE);
		fdef.shape = shape;
		body.createFixture(fdef).setUserData("player");									//set a specific user date to the fixture for the contact 
																						//listener
		
		EdgeShape feet = new EdgeShape();												//create an edge shape, relative to mario's body center
		feet.set(new Vector2(-4 / ClientStarter.PIXELS_PER_METRE, -6 / 
				ClientStarter.PIXELS_PER_METRE), new Vector2(4 / 
				ClientStarter.PIXELS_PER_METRE, -6 / ClientStarter.PIXELS_PER_METRE));  //set it to the feet of mario to detect landings after jumps
		fdef.shape = feet;																//set the fixture definition to this rectangle
		fdef.isSensor = true;															//commit that it doesn't collide
		body.createFixture(fdef).setUserData("feet");									//give it a unique user data
		
		setBounds(0, 0, ClientStarter.TILE_SIZE / ClientStarter.PIXELS_PER_METRE, 		//adapting the meter-system to the bounds of the sprite
				ClientStarter.TILE_SIZE / ClientStarter.PIXELS_PER_METRE);
		setRegion(stand);																//set the region to standing (default)
		
		this.mapWidth = mapWidth;														//set the map width to prevent the player from leaving the map
	}
	
	public void update(float dt) {														//called when the player should update
		setPosition(body.getPosition().x - getWidth() / 2, 								//set the sprite position to the body's position
				body.getPosition().y - getHeight() / 2);
		setRegion(getFrame(dt));														//set the showing region
		
		if(body.getPosition().x < getWidth() / 2) {										//teleport the player back if he wants to leave the map
			body.setTransform(getWidth() / 2, body.getPosition().y, 0);
		} else if(body.getPosition().x > mapWidth - getWidth() / 2) {
			body.setTransform(mapWidth - getWidth() / 2, body.getPosition().y, 0);
		} else if(body.getPosition().y < 0) {											//if he falls through the ground teleport back to start
			body.setTransform(32 / ClientStarter.PIXELS_PER_METRE, 32 / 
					ClientStarter.PIXELS_PER_METRE, 0);
		}
		
		if(feet && body.getLinearVelocity().y == 0) {									//if the feet detect something and the player has no
			jumps = 0;																	//vertical movement, then he can jump
		}
	}
	
	private TextureRegion getFrame(float dt) {											//get the sprite to draw
		current = getState();															//set the current state
		
		TextureRegion region;
		switch (current) {
		case JUMPING:																	//if jumping get the next picture of jumping animation
			region = jump.getKeyFrame(stateTimer);
			actual = 5;																	//set the actual pictureID to 5 for the move packet
			break;
		case RUNNING:
			region = run.getKeyFrame(stateTimer, true);									//if he is running get the next picture of the animation
			if(region != before) {														//if the picture is not the same as before then update
				actual++;																//the current pictureID for the move packet
				if(actual > 3) {
					actual = 1;
				}
				before = region;
			}
			break;
		case CELEBRATING:																//if celebrating, set the region to end and pictureID 6
			region = end;
			actual = 6;
			break;
		default:
			region = stand;
			actual = 0;
			break;
		}
		if((body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {	//if mario is running to the left, then flip the sprite
			region.flip(true, false);
			runningRight = false;
		} else if((body.getLinearVelocity().x > 0 || runningRight) && 					//else flip it back if needed
				region.isFlipX()) {
			region.flip(true, false);
			runningRight = true;
		}
		
		stateTimer = current == previous ? stateTimer + dt : 0;							//if the current picture is the same as before, then add
		previous = current;																//the time since last frame to the current timer else set
																						//it to 0
		return region;
	}
	
	public boolean isRunningRight() {
		return runningRight;
	}
	
	private State getState() {															//get the current state according to the game state
		if(client.getState() == GameState.END_GAME) {									//or the movement of the player
			return State.CELEBRATING;
		}
		if(body.getLinearVelocity().y > 0 || (body.getLinearVelocity().y < 0 && 		//disable jump animation if player is just falling
				previous == State.JUMPING)) {
			return State.JUMPING;
		} else if(body.getLinearVelocity().y < 0) {
			return State.FALLING;
		} else if(body.getLinearVelocity().x != 0) {
			return State.RUNNING;
		} else {
			return State.STANDING;
		}
	}
	
	public Body getBody() {
		return body;
	}

	public boolean canJump() {
		if(client.getState() == GameState.GAME && jumps < 2) {							//give mario the ability to double jump in the game state
			return true;
		} else if(client.getState() == GameState.END_GAME && jumps < 1){				//at the end he can only jump once
			return true;
		}
		return false;
	}

	public void jumped() {
		jumps++;																		//add a jump if player pressed the key up
	}

	public int getSprite() {
		return actual;
	}

	public void setFeetContact(boolean feet) {
		this.feet = feet;																//set the feet contact by the contact listener
	}
}
