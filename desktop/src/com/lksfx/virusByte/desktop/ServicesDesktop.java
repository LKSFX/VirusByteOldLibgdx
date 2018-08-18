package com.lksfx.virusByte.desktop;

import com.lksfx.virusByte.effects.Immersion;
import com.lksfx.virusByte.gameControl.AdControl;
import com.lksfx.virusByte.gameControl.GoogleInterface;
import com.lksfx.virusByte.gameControl.MultiServices;

public class ServicesDesktop implements MultiServices {
	
	@Override
	public Immersion getImmersion() {
		return new ImmersionDesktop();
	}
	
	@Override
	public AdControl getAdControl() {
		return new AdControlDesktop();
	}

	@Override
	public GoogleInterface getGooglePlayService() {
		return new GoogleServicesDesktop();
	}
	
}
