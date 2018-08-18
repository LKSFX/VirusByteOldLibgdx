package com.lksfx.virusByte.desktop;

import com.badlogic.gdx.Gdx;
import com.lksfx.virusByte.gameControl.AdControl;

public class AdControlDesktop implements AdControl {
	private boolean bannerIsVisible;
	
	@Override
	public void showInterstitial() {
		Gdx.app.log("Ads control:", "showing Interstitial method called");
	}

	@Override
	public void loadInterstitial() {
		Gdx.app.log("Ads control:", "load Interstitial method called");
	}

	@Override
	public void showBanner() {
		Gdx.app.log("Ads control:", "showing banner method called");
		bannerIsVisible = true;
	}

	@Override
	public void hideBanner() {
		Gdx.app.log("Ads control:", "hide banner method called");
		bannerIsVisible = false;
	}

	@Override
	public boolean isInterstitialLoaded() {
		return false;
	}

	@Override
	public boolean isBannerActive() {
		return false;
	}

	@Override
	public boolean isBannerVisible() {
		return bannerIsVisible;
	}

}
