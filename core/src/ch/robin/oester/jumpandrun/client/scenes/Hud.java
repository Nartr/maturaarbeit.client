package ch.robin.oester.jumpandrun.client.scenes;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import ch.robin.oester.jumpandrun.client.ClientStarter;
import ch.robin.oester.jumpandrun.client.tools.Player;

public class Hud implements Disposable {												//head-up display, which can be disposed (deleted)

	private final ClientStarter client;
	
	private Viewport port;
	private Stage stage;																//empty box where you put in the actors
	private Skin skin;																	//represents the style of the graphic elements
	
	private final Label lblwRecord;														//lbl's are just unchangeable labels 
	private final Label lblRecord;
	private final Label lblTime;
	private final Label lblRank;
	
	private Label wRecordLabel;															//the changeable number labels
	private Label recordLabel;
	private Label timeLabel;
	private Label rankLabel;
	
	public Hud(ClientStarter client, String wRecord, String record) {
		this.client = client;
		this.port = new FitViewport(ClientStarter.WIDTH * 2, 							//create the normal Viewport size bigger to get bigger font
				ClientStarter.HEIGHT * 2, new OrthographicCamera());
		this.stage = new Stage(port, client.getBatch());								//create the stage and commit the viewport and the canvas
		this.skin = client.getAssetManager().get
				("skins/clean-crispy-ui.json", Skin.class);								//load the skin
		
		if(wRecord.equals(String.valueOf(Long.MAX_VALUE / 1000.0))) {					//if there is no world record, set it to 0.0
			wRecord = "0.0";
		}
		if(record.equals(String.valueOf(Long.MAX_VALUE / 1000.0))) {					//same with personal best
			record = "0.0";
		}
		
		this.lblwRecord = new Label("WELT REKORD", skin);								//create the final labels
		this.lblRecord = new Label("DEIN REKORD", skin);
		this.lblTime = new Label("ZEIT", skin);
		this.lblRank = new Label("RANG", skin);
		
		this.wRecordLabel = new Label(wRecord, skin);									//create the number labels
		this.recordLabel = new Label(record, skin);	
		this.timeLabel = new Label("0.0", skin);
		this.rankLabel = new Label("1", skin);											//set the rank at the beginning to 1
		
		Table table = new Table();														//create a new table to arrange the labels
		table.top();																	//set it on top of the screen
		table.setFillParent(true);														//fill up the whole screen
		
		table.add(lblwRecord).expandX().padTop(10);										//add the labels with a padding of 10 pixels
		table.add(lblRecord).expandX().padTop(10);										//and expand it in x direction as much as possible
		table.add(lblTime).expandX().padTop(10);										//the table calculates automatically the gaps
		table.add(lblRank).expandX().padTop(10);
		table.row();																	//insert a new row
		table.add(wRecordLabel).expandX();												//add the number labels without padding
		table.add(recordLabel).expandX();
		table.add(timeLabel).expandX();
		table.add(rankLabel).expandX();
		
		stage.addActor(table);															//add the table-actor to the stage
	}

	@Override
	public void dispose() {
		stage.dispose();																//if the HUD has to be disposed, dispose the stage
	}
	
	public void update(float x) {														//the x-value refers to the player's x coordinate
		timeLabel.setText(String.valueOf(
				(System.currentTimeMillis() - client.getStartTime()) / 1000.0));		//update the elapsed time label
		
		int rank = 1;
		for(Player all : client.getPlayers()) {
			double posX = all.getX();
			if(posX > x) {
				rank++;																	//for all players who have more distance covered, add one rank
			}
		}
		rankLabel.setText(String.valueOf(rank));										//set the rank label to the calculated rank
	}
	
	public Stage getStage() {															//get the stage to render it on the game screen
		return stage;
	}
	
	public void setText(String time) {
		timeLabel.setText(time);
	}
}
