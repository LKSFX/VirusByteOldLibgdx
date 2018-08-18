package com.lksfx.virusByte.gameObject.virusObject;

import static com.lksfx.virusByte.gameControl.debug.Debug.debug;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class PsychoVirus extends VirusType {
	private int clicks, tap;
	private float time, renewTime = .7f, circleSize = .5f;
	private TextureRegion circle;
	
	public PsychoVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640, WORLD_HEIGHT+64));
	}
	
	public PsychoVirus(float x, float y) {
		this(x, y, 7, 150, 250, new Color(1f, 242f/255f, 0f, 0.3f), true);
	}
	
	public PsychoVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		if ( TUTORIAL ) {
			circle = VirusByteGame.ASSETS.getAssetManager()
					.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("circle");
			tap = 2;
		}
		isMoneyBack = true;
		setCollisionMaskBounds(50, 50);
		virus_id = VirusInstance.Psycho;
		imm = 17;
		setInputController( new InputTriggers() {
			@Override
			public boolean doubleTouch( Vector3 mousePos ) {
				if ( isOver(mousePos) ) {
					if ( clicks == 0 ) 
						time = renewTime;
					Debug.log(clicks + " clicks on psycho");
					clicks++;
					if ( clicks >= 2 ) {
						clicks = 0;
						destroy(true, true);
						//if (VirusByteGame.VIBRATION) VirusByteGame.IMM.vibrate(17);
						return true;
					}
				}
				return false;
			}
		} );
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("psycho1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("psycho2");		
		
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("psycho-trail");	
		createAnim( normal, trail );
		
	}
	
	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		super.draw(batch, deltaTime);
		if ( TUTORIAL ) {
			if ( circleSize > 1.5f ) 
				circleSize = .5f;
			circleSize += .05f;
			batch.setColor( 1f, 1f, 1f, 0.65f );
			batch.draw(circle, spriteCenter.x +( (currentFrameWidth*.5f) - 16 ), spriteCenter.y +( (currentFrameHeight*.5f) - 16 ), 16f, 16f, 32f, 32f, circleSize, circleSize, 0f);
			batch.setColor( 1f, 1f, 1f, 1f );
			String str = "tap: " + (tap-clicks);
			debug.screen(str, (int)(spriteCenter.x - ( (Debug.debug.font.getBounds(str).width - currentFrameWidth) *.5f)), (int)spriteCenter.y - 30, false);
		}
	}
	
	/*------------------------------------------------*/
	
	/*------------------------------------------------*/
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (clicks > 0) {
			time -= deltaTime;
			if (time < 0f) clicks = 0;
		} 
	}
	
/*	@Override
	public void destroy(boolean finale, boolean hit) {
		if ( !TUTORIAL ) {
			if ( position.y > 220f && hit ) {
				if ( MathUtils.randomBoolean(0.1f) ) {
					// 10% change of parasite spawn when the kill occurs up to 220 of height from the bottom border
					SpawnableConfiguration config = VirusByteGame.VIRUS_MANAGER.spawner.getSCS_basic2(.030f, .9f, .6f, Spawner.Transition.GROW_IN_STOPPED_TO_MOVE);
					VirusByteGame.VIRUS_MANAGER.spawner.addSpawnable(VirusInstance.Parasite, config, position.x, position.y);
					//VIRUS_MANAGER.addVirus(position.x, position.y, VirusManager.VirusInstance.Parasite);
				}
			}
		}
		
		super.destroy(finale, hit);
	}*/
	
	@Override
	public void reset() {
		super.reset();
		spawnParasiteChange = .1f;
		clicks = 0;
		time = renewTime;
	}
}
