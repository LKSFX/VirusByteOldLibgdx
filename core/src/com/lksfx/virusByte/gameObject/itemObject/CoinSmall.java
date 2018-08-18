package com.lksfx.virusByte.gameObject.itemObject;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;

public class CoinSmall extends Coin {

	public CoinSmall() {
		this(0, 0);
	}

	public CoinSmall(float x, float y) {
		super(x, y);
	}

	public CoinSmall(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		setCoinValue( 1 );
		setWeight( 5 );
	}
	
	@Override
	public void setAtlas() {
		AssetManager manager = VirusByteGame.ASSETS.getAssetManager();
		
		TextureRegion[] regions = new TextureRegion[4];
		regions[0] = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion( "coin-small1" );
		regions[1] = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion( "coin-small2" );
		regions[2] = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion( "coin-small3" );
		regions[3] = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion( "coin-small4" );
		
		createAnim(regions, null, .083f);
	}
	
	@Override
	public String getName() {
		return "Small Coin";
	}
}
