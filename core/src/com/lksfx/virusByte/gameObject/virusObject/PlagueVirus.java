package com.lksfx.virusByte.gameObject.virusObject;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.TapableCore;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class PlagueVirus extends VirusType {
	protected int tap;
	protected float circleSize = 0.5f;
//	private TextureRegion circle;
	
	public PlagueVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}
	
	public PlagueVirus(float x, float y) {
		this(x, y, 5, 200, 350, new Color(0f, 0f, 1f, 0.3f));
	}
	
	public PlagueVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor) {
		super(x, y, point, minSpd, maxSpd, trailColor, true);
		/*if ( TUTORIAL ) {
			circle = assetManager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("circle");
		}*/
		isMoneyBack = true;
		setCollisionMaskBounds(0, 4, 50, 44);
		virus_id = VirusInstance.Plague;
		setDefaultVirusCore( new TapableCore( this, 1 ) );
		//move = false;
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("plague1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("plague2");		
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("plague-trail");
		
		createAnim( normal, trail );
		
	}
	
	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		super.draw(batch, deltaTime);
		/*if ( TUTORIAL ) {
			if ( circleSize > 1.5f ) circleSize = .5f;
			circleSize += .05f;
			batch.setColor(1f, 1f, 1f, 0.65f);
			batch.draw(circle, spriteCenter.x +( (currentFrameWidth*.5f) - 16 ), spriteCenter.y +( (currentFrameHeight*.5f) - 16 ), 16f, 16f, 32f, 32f, circleSize, circleSize, 0f);
			batch.setColor(1f, 1f, 1f, 1f);
			String str = "tap: " + (tap-clicks);
			debug.screen(str, (int)(spriteCenter.x - ( (Debug.debug.font.getBounds(str).width - currentFrameWidth) *.5f)), (int)spriteCenter.y - 30, false);
		}*/
	}
	
	@Override
	public void reset() {
		super.reset();
		spawnParasiteChange = .1f;
	}
	
}
