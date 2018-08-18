package com.lksfx.virusByte.gameObject.virusObject.pepperBros;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameObject.abstractTypes.DetectionMask.MaskFormat;

public class PepperRocket extends PepperBomb {
	
	public PepperRocket() {
		this(VIEWPORT.getWorldWidth()*.5f, 1280f);
	}

	public PepperRocket(float x, float y) {
		this(x, y, 5, 250, 300, new Color(Color.RED));
	}

	public PepperRocket(float x, float y, int point, int minSpd, int maxSpd, Color trailColor) {
		super( x, y, point, minSpd, maxSpd, trailColor );
		collisionMask.setMaskFormat( MaskFormat.TRIANGLE );
		float halfWidth = currentFrameWidth * .35f;
		float halfHeight = currentFrameHeight * .3f;
		collisionMask.setVertices( new float[] { 
				0, -halfHeight,
				halfWidth, halfHeight,
				-halfWidth, halfHeight
		} );
		maxBounces = 0;
		rotate = false;
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] regions = new AtlasRegion[3];
		regions[0] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("pepper-rocket-gaussian1");
		regions[1] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("pepper-rocket-gaussian2");
		regions[2] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("pepper-rocket-gaussian3");
		createAnim(regions, null);
		animGaussian = getAnimation();
		regions = new AtlasRegion[3];
		regions[0] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("pepper-rocket1");
		regions[1] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("pepper-rocket2");
		regions[2] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("pepper-rocket3");
		createAnim( regions, null );
		
	}
	
	
}
