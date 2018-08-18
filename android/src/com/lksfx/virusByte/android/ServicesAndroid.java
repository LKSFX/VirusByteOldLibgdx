package com.lksfx.virusByte.android;

import com.immersion.uhl.Launcher;
import com.lksfx.virusByte.effects.Immersion;
import com.lksfx.virusByte.gameControl.AdControl;
import com.lksfx.virusByte.gameControl.GoogleInterface;
import com.lksfx.virusByte.gameControl.MultiServices;

public class ServicesAndroid implements MultiServices {
	private ImmersionAndroid imm;
	private AdControl adControl;
	private GoogleInterface googleService;
	/**
	 * 
	 * @param launcher immersion launcher
	 */
	public ServicesAndroid(Launcher launcher, AdControl ads, GoogleInterface googleService) {
		imm = new ImmersionAndroid(launcher);
		adControl = ads;
		this.googleService = googleService;
	}
	
	@Override
	public Immersion getImmersion() {
		return imm;
	}
	
	@Override
	public AdControl getAdControl() {
		return adControl;
	}

	@Override
	public GoogleInterface getGooglePlayService() {
		return googleService;
	}
	
}
