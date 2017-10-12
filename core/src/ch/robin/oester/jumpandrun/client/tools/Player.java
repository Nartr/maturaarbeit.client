package ch.robin.oester.jumpandrun.client.tools;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import ch.robin.oester.jumpandrun.client.ClientStarter;

public class Player {
	
	private float x, y;
	private int color;
	private String name;
	private Sprite[] tex;
	private int sprite;
	private boolean right;

	public Player(String name, int color, Texture texture) {							//Player class to save all other players
		this.name = name;																//set their name and color
		this.color = color;
		this.tex = new Sprite[7];														//create the sprite array and save the different states
		for(int i = 0; i < tex.length; i++) {
			tex[i] = new Sprite(texture, i * 16, 0, 16, 16);
			tex[i].setBounds(0, 0, ClientStarter.TILE_SIZE / 							//edit the bounds to the metric system
					ClientStarter.PIXELS_PER_METRE, ClientStarter.TILE_SIZE / 
					ClientStarter.PIXELS_PER_METRE);
		}
		this.sprite = 0;																//set the default sprite to 0 and looking to the right
		this.right = true;
	}
	
	public void reset() {																//reset the player at the end of every game
		x = 0;
		y = 0;
		right = true;
		sprite = 0;
	}

	public void draw(SpriteBatch batch) {												//draw the player on the screen
		if(right && tex[sprite].isFlipX()) {											//flip or flip back the sprite if needed
			tex[sprite].flip(true, false);
		} else if(!right && !tex[sprite].isFlipX()) {
			tex[sprite].flip(true, false);
		}
		batch.draw(tex[sprite], x - tex[sprite].getWidth() / 2, y - 					//draw the texture at the actual position
				tex[sprite].getHeight() / 2, tex[sprite].getWidth(), 
				tex[sprite].getHeight());
	}
	
	public String getName() {
		return name;
	}
	
	public int getColor() {
		return color;
	}
	
	public boolean isPlaying() {														//if the player is waiting, he has coordinates 0|0
		return x != 0;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public void setRight(boolean right) {
		this.right = right;
	}
	
	public void setSprite(int sprite) {
		this.sprite = sprite;
	}
}
