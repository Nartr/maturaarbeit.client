package ch.robin.oester.jumpandrun.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import ch.robin.oester.jumpandrun.client.ClientStarter;
import ch.robin.oester.jumpandrun.client.network.packets.MessagePacket;
import ch.robin.oester.jumpandrun.client.tools.Player;

public class WaitingScreen implements Screen {
	
	private final ClientStarter client;
	private Stage stage;
	
	private Skin skin;
	private Texture background;
	private TextureRegion[] blocks;
	
	private TextField chat;
	
	private Label[] names;
	private Label text;
	private Label countdown;
	
	
	public WaitingScreen(ClientStarter client, AssetManager man) {
		this.client = client;
		this.stage = new Stage();														//create the empty box
		
		this.skin = man.get("skins/clean-crispy-ui.json", Skin.class);					//load the skin
		this.background = man.get("WaitingArea.png", Texture.class);					//load the background picture
		Texture blockImage = man.get("Blocks.png", Texture.class);						//load the block image
		this.blocks = new TextureRegion[blockImage.getWidth() / 30];					//create the width-dependent array
		this.names = new Label[blocks.length];
		for(int i = 0; i < blocks.length; i++) {
			blocks[i] = new TextureRegion(blockImage, i * 30, 0, 30, 30);				//generate the sub-images
			names[i] = new Label("", skin);												//generate the name labels
			names[i].setSize(100, 30);
			names[i].setPosition(520, 360 - i * 40);
			stage.addActor(names[i]);
		}
		
		this.text = new Label("", skin);												//text area called console
		text.setWrap(true);																//automatically wrap at the end of a line
		text.setAlignment(Align.topLeft);												//start writing at top left
		ScrollPane pane = new ScrollPane(text, skin);									//create the scrolling pane
		pane.setSize(385, 492);
		pane.setPosition(20, 115);
		pane.setForceScroll(false, true);												//set a scroller on the y axes
		pane.setSmoothScrolling(true);
		pane.setFadeScrollBars(false);													//remove the setting that the scroller can disappear
		
		this.chat = new TextField("", skin);											//create the chat field
		chat.setSize(300, 60);
		chat.setPosition(16, 25);
		
		TextButton send = new TextButton("Senden", skin);								//create the send button
		send.setSize(70, 60);
		send.setPosition(335, 25);
		
		this.countdown = new Label("", skin);											//create the countdown label on the upper right corner
		countdown.setFontScale(3.5f);
		countdown.setAlignment(Align.right);
		countdown.setSize(100, 70);
		countdown.setPosition(1000, 516);
		
		stage.addActor(countdown);														//add the actors to the stage
		stage.addActor(send);
		stage.addActor(chat);
		stage.addActor(pane);
		
		send.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(chat.getText().length() > 0 && chat.getText().length() < 100) {		//if the send button is clicked, flush the message
					MessagePacket packet = new MessagePacket();							//but only if the text length is smaller than 100 characters
					packet.message = chat.getText();
					client.sendTCP(packet);
				}
				chat.setText("");
			}
		});
	}
	
	public void initialize() {
		client.setState(GameState.WAITING);												//every restart, initialize the waiting screen 
		
		Gdx.input.setInputProcessor(stage);												//delegate all inputs to this stage
	}

	@Override
	public void show() {}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);												//clears the background
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		client.getBatch().begin();														//open the canvas to draw
		client.getBatch().draw(background, 0, 0, background.getWidth(), 				//draw the background
				background.getHeight());
		int i = 0;
		for(Player player : client.getPlayers()) {
			client.getBatch().draw(blocks[player.getColor()], 480, 360 - i * 40);		//go through all players and create their block and name tag
			names[i].setText(player.getName());
			i++;
		}
		client.getBatch().draw(blocks[client.getColor()], 480, 360 - i * 40);			//draw your own block and name at the bottom
		names[i].setText(client.getName());
		client.getBatch().end();
		
		stage.act(delta);																//let the stage act
		stage.draw();
		
		for(Label l : names) {															//clear the names, to prevent bugs on disconnecting
			l.setText("");
		}
	}
	
	public Label getText() {
		return text;
	}
	
	public Label getCountdown() {
		return countdown;
	}

	@Override
	public void resize(int width, int height) {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void hide() {}

	@Override
	public void dispose() {}
}
