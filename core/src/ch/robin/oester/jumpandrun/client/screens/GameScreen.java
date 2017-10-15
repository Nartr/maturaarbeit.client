package ch.robin.oester.jumpandrun.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import ch.robin.oester.jumpandrun.client.ClientStarter;
import ch.robin.oester.jumpandrun.client.scenes.Hud;
import ch.robin.oester.jumpandrun.client.sprites.Mario;
import ch.robin.oester.jumpandrun.client.tools.Player;
import ch.robin.oester.jumpandrun.client.tools.WorldContactListener;
import ch.robin.oester.jumpandrun.client.tools.WorldCreator;

public class GameScreen implements Screen {
	
	private static final float MAX_SPEED = 2f;											//setting to test out best speed
	private static final float JUMP_HEIGHT = 4f;										//test the best jump height
	
	private final ClientStarter client;
	private OrthographicCamera camera;
	private Viewport port;

	private TmxMapLoader loader;
	private TiledMap map;
	private float mapWidth;																//save the map width to calculate "rank annunciation" positions
	private OrthogonalTiledMapRenderer renderer;
	
	private World world;
	
	private WorldCreator creator;
	private Mario player;
	private Hud hud;
	
	private Music music;
	private int rank;
	
	public GameScreen(ClientStarter client, AssetManager man) {							//transfer the asset manager to load assets
		this.client = client;
		
		this.camera = new OrthographicCamera();											//2D camera which saves the to-render position, width and height
		this.port = new FitViewport(													//create a Fit-Viewport which adds black bars on resizing
				ClientStarter.WIDTH / ClientStarter.PIXELS_PER_METRE, 					//create a new coordinate system with meter as measure
				ClientStarter.HEIGHT / ClientStarter.PIXELS_PER_METRE, camera);			//transfer the camera to commit the new dimensions
		camera.position.set(port.getWorldWidth() / 2, port.getWorldHeight() / 2, 0);	//set camera position to middle of screen
		
		this.loader = new TmxMapLoader();												//create a map loader
		
		this.music = man.get("backinblack.mp3", Music.class);							//load the background sound
		music.setLooping(true);															//set it to looping
	}
	
	public void initialize(AssetManager man, String input) {							//fired when a new game starts
		this.rank = 0;																	//reset the rank
		
		String[] split = input.split(":");												//split the given input
		
		this.map = loader.load("tilemap" + split[0] + ".tmx");							//first split is the mapID which is loaded by the map loader
		this.mapWidth = map.getProperties().get("width", Integer.class) * 				//load the map width from the tile map
				ClientStarter.TILE_SIZE /
				ClientStarter.PIXELS_PER_METRE;											//save it as meter measure
	
		this.renderer = new OrthogonalTiledMapRenderer(map, 1 / 						//create the new map renderer
				ClientStarter.PIXELS_PER_METRE);										//transfer the scale value to render correctly

		this.world = new World(new Vector2(0, -10), true);								//create a new world and add gravitation
		
		this.creator = new WorldCreator(world, map);									//create the collision map
		creator.create();
		
		Sprite s = new Sprite(man.get("MarioSprite" + client.getColor() + 				//load the sprite with given colorID
				".png", Texture.class));
		this.player = new Mario(client, world, s, mapWidth);							//create the player and set it inside the map
		
		music.play();																	//start the music
		
		world.setContactListener(new WorldContactListener(client));						//create a contact detector for finish line and jump detection

		this.hud = new Hud(client, split[1], split[2]);									//create the head-up display
	}

	@Override
	public void render(float delta) {													//runs once every frame
		update(delta);																	//call update method
		
		Gdx.gl.glClearColor(0, 0, 0, 1);												//draws background
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);										//disable current drawing color
		
		renderer.render();																//render the tile map
		
		client.getBatch().setProjectionMatrix(camera.combined);							//transmit the current coordinate system to the canvas
		client.getBatch().begin();														//open the canvas to draw on it
		if(client.getState() == GameState.GAME 
				|| client.getState() == GameState.END_GAME) {						
			for(Player all : client.getPlayers()) {
				if(all.isPlaying()) {
					all.draw(client.getBatch());										//draw all players who are not waiting but playing
				}
			}
		}

		player.draw(client.getBatch());													//draw the player on the canvas
		client.getBatch().end();														//close the canvas from other manipulations
		
		if(client.getState() == GameState.GAME || 
				client.getState() == GameState.END_GAME) {
			client.sendUDP(player);														//every second frame the player sends his status to the others
		}
		
		client.getBatch().setProjectionMatrix(hud.getStage().getCamera().combined);		//only render what the HUD-camera sees
		hud.getStage().draw();															//draw the HUD-stage
	}

	@Override
	public void resize(int width, int height) {
		port.update(width, height);														//resize the viewport (disabled)
	}
	
	@Override
	public void show() {}																//fired when game screen is set to the current screen
	
	@Override
	public void pause() {}																//fired if the client pauses (disabled)

	@Override
	public void resume() {}																//fired when the client resumes (disabled)

	@Override
	public void hide() {																//fired when the screen changes
		map.dispose();																	//dispose all attributes connected to the map
		renderer.dispose();																//because the map may change in the next round
		music.stop();
		hud.dispose();
	}

	@Override
	public void dispose() {																//on closing the application, dispose all connected elements
		map.dispose();
		renderer.dispose();
		world.dispose();
		music.dispose();
		hud.dispose();
	}
	
	public void update(float delta) {													//update with time since last frame
		handleInput(delta);																//first handle the inputs by the player
		
		world.step(1/60f, 6, 2);														//calculate physics of bodies 1/60 is the time it should take
		
		player.update(delta);															//update the player
		
		camera.position.x = player.getBody().getPosition().x;							//normally take the player as center of the camera
		if(camera.position.x < port.getWorldWidth() / 2) {								//but if he is at the end, block the move
			camera.position.x = port.getWorldWidth() / 2;
		} else if(camera.position.x > mapWidth - ClientStarter.WIDTH / 
				ClientStarter.PIXELS_PER_METRE / 2) {
			camera.position.x = mapWidth - ClientStarter.WIDTH / 
					ClientStarter.PIXELS_PER_METRE / 2;
		}

		camera.update();																//update now the camera position
		
		renderer.setView(camera);														//set render-view to what the camera sees
		
		if(client.getState() == GameState.GAME) {
			hud.update(player.getBody().getPosition().x);								//if game state is playing, update the HUD
		}
	}

	public void setRank(int rank) {														//set the rank at the end of the game
		this.rank = rank;
		float posX = mapWidth - ((ClientStarter.TILE_SIZE + rank * 2) * 				//calculate the position of the player dependent from his rank
				ClientStarter.TILE_SIZE) / ClientStarter.PIXELS_PER_METRE;
		if(rank == 1) {
			posX -= 2 * ClientStarter.TILE_SIZE / ClientStarter.PIXELS_PER_METRE;		//change the place of the calculated first and second rank
		} else if(rank == 2) {
			posX += 2 * ClientStarter.TILE_SIZE / ClientStarter.PIXELS_PER_METRE;
		}
		player.getBody().setLinearVelocity(0, -1);										//add a small velocity down to the player
		player.getBody().setTransform(posX + player.getWidth() / 2, 					//teleport him to calculated position
				ClientStarter.HEIGHT / ClientStarter.PIXELS_PER_METRE, 0);
	}
	
	private void handleInput(float delta) {
		if(client.getState() == GameState.END_GAME) {
			if(Gdx.input.isKeyJustPressed(Input.Keys.UP) && player.canJump()) {			//in the end-game state, only one jump dependent from rank
				if(rank != 0) {															//allowed
					if(rank < 4) {
						player.getBody().applyLinearImpulse(new Vector2(0, 4f - rank)	//add him a linear impulse upwards
								, player.getBody().getWorldCenter(), true);
					}
				} else {
					player.getBody().applyLinearImpulse(new Vector2(0, JUMP_HEIGHT), 
							player.getBody().getWorldCenter(), true);
				}
				player.jumped();														//tell player that he jumped
			}
		} else if(client.getState() == GameState.GAME) {
			if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
				if(player.canJump()) {													//in the game state, two jumps are allowed
					player.getBody().applyLinearImpulse(new Vector2(0, JUMP_HEIGHT), 
							player.getBody().getWorldCenter(), true);
					player.jumped();
				}
			}
			if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) && 
					player.getBody().getLinearVelocity().x <= MAX_SPEED) {
				player.getBody().applyLinearImpulse(new Vector2(0.1f, 0), 				//apply linear impulse forward if velocity is not to big
						player.getBody().getWorldCenter(), true);						//apply to body center that there is no rotation
			}
			if(Gdx.input.isKeyPressed(Input.Keys.LEFT) && 
					player.getBody().getLinearVelocity().x >= -MAX_SPEED) {
				player.getBody().applyLinearImpulse(new Vector2(-0.1f, 0),				//impulse is responsible for the 
						player.getBody().getWorldCenter(), true);
			}
		}
	}
	
	public Mario getPlayer() {
		return player;
	}
	
	public Hud getHud() {
		return hud;
	}
}
