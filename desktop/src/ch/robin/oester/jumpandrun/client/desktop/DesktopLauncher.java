package ch.robin.oester.jumpandrun.client.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import ch.robin.oester.jumpandrun.client.ClientStarter;

public class DesktopLauncher {
	public static void main (String[] arg) {											//starting class on desktop
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();		//edit the configuration settings
		cfg.title = "Jump and Run";														//set title
		cfg.width = ClientStarter.WIDTH * 3;											//set dimensions
		cfg.height = ClientStarter.HEIGHT * 3;
		cfg.resizable = false;
		new LwjglApplication(new ClientStarter(), cfg);
	}
}