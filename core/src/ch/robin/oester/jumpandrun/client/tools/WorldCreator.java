package ch.robin.oester.jumpandrun.client.tools;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import ch.robin.oester.jumpandrun.client.ClientStarter;

public class WorldCreator {

	private World world;
	private TiledMap map;
	
	public WorldCreator(World world, TiledMap map) {
		this.world = world;																//creator needs a world and the tile map
		this.map = map;
	}

	public void create() {
		BodyDef bdef = new BodyDef();													//create the characteristics of a new body
		PolygonShape shape = new PolygonShape();										//set it to a polygon shape
		FixtureDef fdef = new FixtureDef();												//create the fixture to calculate collision after
		Body body;																		//generate a blank body
		
		for(MapObject o : map.getLayers().get(3).getObjects().getByType(				//these are not all bricks but the bricks objects
				RectangleMapObject.class)) {											//go through all brick-objects inside the tile map layer
			Rectangle rect = ((RectangleMapObject) o).getRectangle();					//get them as a rectangle
			bdef.type = BodyDef.BodyType.StaticBody;									//define as a static body, they don't move
			bdef.position.set((rect.getX() + rect.getWidth() / 2) / 					//set their positions according to the metric coordinate system
					ClientStarter.PIXELS_PER_METRE, 									//the position of the center is the position of the object
					(rect.getY() + rect.getHeight() / 2) / 
					ClientStarter.PIXELS_PER_METRE);
			body = world.createBody(bdef);												//create the body inside the world
			shape.setAsBox((rect.getWidth() / 2) / 										//set the shape as a rectangular box with metric width and height
					ClientStarter.PIXELS_PER_METRE, (rect.getHeight() / 2) / 
					ClientStarter.PIXELS_PER_METRE);
			fdef.shape = shape;															//set the fixture shape
			body.createFixture(fdef);													//add the fixture to the body
		}
		
		for(MapObject o : map.getLayers().get(4).getObjects().getByType(				//go through all line-objects
				RectangleMapObject.class)) {											//create them identical to the brick objects
			Rectangle rect = ((RectangleMapObject) o).getRectangle();
			bdef.type = BodyDef.BodyType.StaticBody;
			bdef.position.set((rect.getX() + rect.getWidth() / 2) / 
					ClientStarter.PIXELS_PER_METRE, 
					(rect.getY() + rect.getHeight() / 2) / 
					ClientStarter.PIXELS_PER_METRE);
			body = world.createBody(bdef);
			shape.setAsBox((rect.getWidth() / 2) / 
					ClientStarter.PIXELS_PER_METRE, (rect.getHeight() / 2) / 
					ClientStarter.PIXELS_PER_METRE);
			fdef.shape = shape;
			fdef.isSensor = true;														//but make them non-collisionable
			body.createFixture(fdef).setUserData("line");;								//add a unique data to it
		}
	}
}
