package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.FluidSim;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		FluidSim sim=new FluidSim();
		config.title = "fluid simulation";
		config.width =	sim.totalWidth;
		config.height = sim.simSize;
		new LwjglApplication(new FluidSim(), config);
	}
}
