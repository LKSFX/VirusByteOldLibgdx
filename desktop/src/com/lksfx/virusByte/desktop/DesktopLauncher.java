package com.lksfx.virusByte.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.lksfx.virusByte.VirusByteGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "libGdx";
		config.x = 800;
		config.width = 480;
		config.height = 640;
		config.allowSoftwareMode = true;
		//config.foregroundFPS = 18;
		//config.vSyncEnabled = false;
		//config.foregroundFPS = 0;
		new LwjglApplication(new VirusByteGame( new ServicesDesktop() ), config);
	}
}
