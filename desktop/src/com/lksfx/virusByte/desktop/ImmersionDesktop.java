package com.lksfx.virusByte.desktop;

import com.badlogic.gdx.Gdx;
import com.lksfx.virusByte.effects.Immersion;

public class ImmersionDesktop implements Immersion {
	
	@Override
	public void vibrate(int type) {
		if ( Immersion.DEGUG ) Gdx.app.log("Immersion class: ", "requested to start vibration Type ");
	}

	@Override
	public void stop() {
		if ( Immersion.DEGUG ) Gdx.app.log("Immersion class: ", "requested to stop vibration");
	}

}
