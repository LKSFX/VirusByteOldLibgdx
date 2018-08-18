package com.lksfx.virusByte.gameObject.virusObject.pepperBros;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class Smoke extends VirusType {
	
	public Smoke() {
		this(VIEWPORT.getWorldWidth()*.5f, 1280f);
	}
	
	public Smoke(float x, float y) {
		this(x, y, 5, 180, 230, new Color(Color.RED), false);
	}

	public Smoke(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		setAnimationVelocity( .033f );
	}

	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] regions = new AtlasRegion[7];
		regions[0] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("smoke1");
		regions[1] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("smoke2");
		regions[2] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("smoke3");
		regions[3] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("smoke4");
		regions[4] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("smoke5");
		regions[5] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("smoke6");
		regions[6] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("smoke7");
		createAnim( regions, null );
		
	}
	
	@Override
	public void resetInPosition(float posX, float posY) {
		super.resetInPosition(posX, posY);
		setAnimationPlayMode( PlayMode.NORMAL );
	}
	
	@Override
	public void update(float deltaTime) {
		isOnMove = false; //ever stopped
		super.update(deltaTime);
	}
	
	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		super.draw(batch, deltaTime);
		if ( getAnimation().isAnimationFinished(elapsedTime) ) destroy();
	}
	
}
