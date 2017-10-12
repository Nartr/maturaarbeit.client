package ch.robin.oester.jumpandrun.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;

import ch.robin.oester.jumpandrun.client.network.ClientListener;
import ch.robin.oester.jumpandrun.client.network.packets.CountdownPacket;
import ch.robin.oester.jumpandrun.client.network.packets.MessagePacket;
import ch.robin.oester.jumpandrun.client.network.packets.PlayerConnectedPacket;
import ch.robin.oester.jumpandrun.client.network.packets.PlayerDisconnectedPacket;
import ch.robin.oester.jumpandrun.client.network.packets.PlayerHitLine;
import ch.robin.oester.jumpandrun.client.network.packets.PlayerLoginPacket;
import ch.robin.oester.jumpandrun.client.network.packets.PlayerMovePacket;
import ch.robin.oester.jumpandrun.client.network.packets.PlayerRespondPacket;
import ch.robin.oester.jumpandrun.client.network.packets.ServerChangeStatusPacket;
import ch.robin.oester.jumpandrun.client.screens.GameScreen;
import ch.robin.oester.jumpandrun.client.screens.MainMenuScreen;
import ch.robin.oester.jumpandrun.client.screens.WaitingScreen;
import ch.robin.oester.jumpandrun.client.sprites.Mario;
import ch.robin.oester.jumpandrun.client.tools.Player;

public class ClientStarter extends Game {

	public static final int TILE_SIZE = 16;												//tile size of all the tiles
	public static final int WIDTH = 25 * TILE_SIZE;										//basic screen width (25 tiles)
	public static final int HEIGHT = 13 * TILE_SIZE;									//basic screen height (13 tiles)
	public static final float PIXELS_PER_METRE = 100f;									//transform into metric system
	
	private static final int TIMEOUT = 5000, TCP_PORT = 6000, UDP_PORT = 6001;			//waiting time for a response of the server and TCP/UDP port
	private static final String HOST = "91.214.170.124";								//IP of the server
	private static final int MAX_PLAYERS = 5;
	
	private Client client;																//client which can communicate with the server
	private Kryo kryo;																	//manages all the different packets
	private String name;
	private int color;
	private List<Player> players;
	
	private AssetManager man;
	
	private int state;
	private MainMenuScreen menu;
	private WaitingScreen waiting;
	private GameScreen game;
	
	private SpriteBatch batch;
	
	private Long startTime;

	@Override
	public void create () {
		this.man = new AssetManager();													//create an asset manager to load resources
		for(int i = 0; i < MAX_PLAYERS; i++) {
			man.load("MarioSprite" + i + ".png", Texture.class);						//load all mario sprite
		}
		man.load("welcometojungle.mp3", Music.class);									//load the music and all backgrounds
		man.load("background.png", Texture.class);
		man.load("BackgroundStyle.png", Texture.class);
		man.load("WaitingArea.png", Texture.class);
		man.load("skins/clean-crispy-ui.json", Skin.class);
		man.load("Blocks.png", Texture.class);
		man.finishLoading();															//wait until all assets are loaded
		
		this.players = new ArrayList<>();
		
		this.batch = new SpriteBatch();													//create the canvas
		
		this.menu = new MainMenuScreen(this, man);										//create all menus and show the log in screen
		this.waiting = new WaitingScreen(this, man);
		this.game = new GameScreen(this, man);
		setScreen(menu);
		
		createClient();																	//setup the client
	}

	@Override
	public void render () {																//called at rendering
		super.render();																	//delegate render to activ screen
	}
	
	@Override
	public void dispose () {															//called when destroyed
		batch.dispose();																//dispose the canvas and close the connection
		client.close();
	}
	
	@Override
	public void resize(int width, int height) {											//called when resized
		super.resize(width, height);													//resize screen
	}
	
	@Override
	public void resume() {																//called when resumed from pause state
		super.resume();
	}
	
	@Override
	public void pause() {																//called when application is paused
		super.pause();
	}
	
	private void createClient() {
		this.client = new Client();														//create a new client
		
		client.addListener(new ClientListener(this));									//add a packet listener
		
		setupKryo();																	//setup packet manager
		
		client.start();																	//start the client thread asynchronously
	}
	
	public boolean connect() {
		try {
			client.connect(TIMEOUT, HOST, TCP_PORT, UDP_PORT);							//connect to the host with given data
			client.setKeepAliveUDP(5000);												//Sends every 5 seconds a paket to keep the connection
		} catch (IOException e) {
			System.out.println("Client >> Couldn't connect to server");					//print if connection couldn't be established
			return false;
		}
		return true;
	}
	
	private void setupKryo() {
		this.kryo = client.getKryo();
		
		kryo.register(HashMap.class);													//register all classes that want to be sent over to the server
		kryo.register(PlayerLoginPacket.class);
		kryo.register(PlayerRespondPacket.class);
		kryo.register(PlayerConnectedPacket.class);
		kryo.register(PlayerDisconnectedPacket.class);
		kryo.register(MessagePacket.class);
		kryo.register(CountdownPacket.class);
		kryo.register(ServerChangeStatusPacket.class);
		kryo.register(PlayerHitLine.class);
		kryo.register(PlayerMovePacket.class);
	}

	public Player getByName(String name) {												//return a player object if you get his name
		for(Player all : players) {
			if(all.getName().equalsIgnoreCase(name)) {
				return all;
			}
		}
		return null;
	}
	
	public void sendUDP(Mario player) {													//Select all important datas from mario and send it to server
		PlayerMovePacket packet = new PlayerMovePacket();								//via UDP
		packet.name = name;
		packet.x = player.getBody().getPosition().x;
		packet.y = player.getBody().getPosition().y;
		packet.right = player.isRunningRight();
		packet.sprite = player.getSprite();
		client.sendUDP(packet);
	}
	
	public boolean isConnected() {
		return client.isConnected();
	}
	
	public SpriteBatch getBatch() {
		return batch;
	}

	public void sendTCP(Object o) {
		client.sendTCP(o);																//send packet per TCP
	}
	
	public MainMenuScreen getMenu() {
		return menu;
	}
	
	public int getState() {
		return state;
	}
	
	public void setState(int state) {
		this.state = state;
	}
	
	public String getName() {
		return name;
	}
	
	public int getColor() {
		return color;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public List<Player> getPlayers() {
		return players;
	}
	
	public void setPlayers(List<Player> players) {
		this.players = players;
	}
	
	public WaitingScreen getWaiting() {
		return waiting;
	}
	
	public AssetManager getAssetManager() {
		return man;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	
	public Long getStartTime() {
		return startTime;
	}
	
	public GameScreen getGame() {
		return game;
	}
	
	public void resetSpriteBatch() {
		batch = new SpriteBatch();
	}
}
