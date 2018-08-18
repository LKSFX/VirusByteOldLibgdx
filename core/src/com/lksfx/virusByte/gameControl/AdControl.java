package com.lksfx.virusByte.gameControl;

public interface AdControl {
	
	public void showInterstitial();
	
	public void loadInterstitial();
	
	public void showBanner();
	
	public void hideBanner();
	
	public boolean isInterstitialLoaded();
	
	public boolean isBannerActive();
	
	public boolean isBannerVisible();
}
