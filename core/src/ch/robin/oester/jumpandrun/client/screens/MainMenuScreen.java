package ch.robin.oester.jumpandrun.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ch.robin.oester.jumpandrun.client.ClientStarter;
import ch.robin.oester.jumpandrun.client.network.packets.PlayerLoginPacket;

public class MainMenuScreen implements Screen {
	
	private final ClientStarter client;
	private Stage stage;
	
	private Texture background;
	private Texture backgroundStyle;
	private Skin skin;
	
	private TextButton submit;
	private TextButton register;
	
	private TextField username;
	private TextField password;
	
	private Label infoLabel;
	
	private ButtonListener listener;

	public MainMenuScreen(ClientStarter client, AssetManager man) {
		this.client = client;
		client.setState(GameState.LOGIN);												//at the beginning set the current state to login
		this.stage = new Stage();														//create the empty box
		Gdx.input.setInputProcessor(stage);												//delegate all inputs to this stage
		
		this.background = man.get("background.png", Texture.class);						//load all needed textures and skins
		this.backgroundStyle = man.get("BackgroundStyle.png", Texture.class);			//load the writing on the left
		this.skin = man.get("skins/clean-crispy-ui.json", Skin.class);					//it is like the design of the graphic elements
		
		this.username = new TextField("", skin);										//create user name text field
		username.setSize(150, 50);
		username.setPosition(ClientStarter.WIDTH * 1.5f - 10, 380f);
		
		this.password = new TextField("", skin);
		password.setPasswordMode(true);
		password.setPasswordCharacter('•');												//create password field and replace characters by dots
		password.setSize(150, 50);
		password.setPosition(ClientStarter.WIDTH * 1.5f - 10, 310f);
		
		Label lbl_user = new Label("Benutzername:", skin);								//user name label
		lbl_user.setSize(100, 50);
		lbl_user.setPosition(ClientStarter.WIDTH * 1.5f - 140, 380f);
		
		Label lbl_pass = new Label("Passwort:", skin);									//password label
		lbl_pass.setSize(100, 50);
		lbl_pass.setPosition(ClientStarter.WIDTH * 1.5f - 140, 310f);
		
		this.submit = new TextButton("Login", skin);									//log in button
		submit.setSize(125, 60);
		submit.setPosition(ClientStarter.WIDTH * 1.5f - 145, 220f);
		
		this.register = new TextButton("Registrieren", skin);							//register button
		register.setSize(125, 60);
		register.setPosition(submit.getX() + submit.getWidth() + 40, 220f);
		
		this.infoLabel = new Label("", skin);											//the changeable info label
		infoLabel.setSize(100, 30);
		infoLabel.setColor(1, 0, 0, 1);
		infoLabel.setPosition(ClientStarter.WIDTH * 1.5f - 10, 280);
		
		TextButton information = new TextButton("Informationen", skin);					//the information button
		information.setSize(300, 90);
		information.setPosition(ClientStarter.WIDTH * 1.5f - 
				information.getWidth() / 2, 30f);
		
		stage.addActor(lbl_user);														//add all the actors to the stage
		stage.addActor(username);
		stage.addActor(lbl_pass);
		stage.addActor(password);
		stage.addActor(submit);
		stage.addActor(register);
		stage.addActor(infoLabel);
		stage.addActor(information);
		
		this.listener = new ButtonListener();											//create a new button listener (private class)
		submit.addListener(listener);													//add it to the two buttons
		register.addListener(listener);
	}

	@Override
	public void show() {}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);												//clean-up background
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);										//disable current color
		
		client.getBatch().begin();														//open the canvas to draw on it
		client.getBatch().draw(background, 0, 0, ClientStarter.WIDTH * 3, 				//draw background
				ClientStarter.HEIGHT * 3);
		client.getBatch().draw(backgroundStyle, 60, 200, backgroundStyle.getWidth(), 	//draw the writing
				backgroundStyle.getHeight());
		client.getBatch().end();														//close the canvas
		
		stage.act(delta);																//let the stage act to replace actors
		stage.draw();																	//draw the stage
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
	public void dispose() {
		stage.dispose();																//dispose the stage at the end
	}
	
	public TextButton getSubmit() {
		return submit;																	//get the log in button to activate after
	}
	
	public Label getInfo() {
		return infoLabel;																//get the info label to write on it
	}
	
	public TextButton getRegister() {
		return register;																//get the register button to activate after
	}
	
	private class ButtonListener extends ClickListener {
		
		@Override
		public void clicked(InputEvent event, float x, float y) {						//fired when a connected button is clicked
			String user = username.getText();
			String pass = password.getText();

			if(user.length() == 0) {													//user name has to be between 3 and 15 character
				infoLabel.setText("Benutzername ist leer");
				return;
			} else if(user.length() < 3 || user.length() > 15) {
				infoLabel.setText("Benutzername muss zwischen 3 und 15 Zeichen lang sein");
				return;
			}
			
			if(pass.length() == 0) {													//same rule for password
				infoLabel.setText("Passwort ist leer");
				return;
			} else if(pass.length() < 3 || pass.length() > 15) {
				infoLabel.setText("Passwort muss zwischen 3 und 15 Zeichen lang sein");
				return;
			}
			
			event.getListenerActor().setTouchable(Touchable.disabled);					//disable touched button to connect to the server
			if(!client.isConnected()) {
				if(!client.connect()) {													//if client is not and cannot connect, enable the button
					event.getListenerActor().setTouchable(Touchable.enabled);
					infoLabel.setText("Konnte keine Verbindung zum Server herstellen");
					return;
				}
			}
			
			PlayerLoginPacket packet = new PlayerLoginPacket();							//if the connection is established, send a login packet
			packet.name = user;
			packet.password = pass;
			
			if(event.getListenerActor() == submit) {									//set the clicked button either to log in or register
				packet.login = true;
			}
			
			client.sendTCP(packet);														//send it per TCP
		}
	}
}
