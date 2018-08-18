package com.lksfx.virusByte.gameObject.virusObject;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.RotationTestCore;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class EnergyzerVirus extends VirusType {
	private Animation bloomEffect;
	private boolean isGlow;
	
	public EnergyzerVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}
	
	public EnergyzerVirus(float x, float y) {
		this(x, y, 10, 100, 200, new Color(77/255f, 255/255f, 255/255f, 0.3f), true);
	}
	
	public EnergyzerVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		isMoneyBack = true;
		setCollisionMaskBounds(40, 40);
		virus_id = VirusInstance.Energyzer;
		comboInterval = 2f;
		setDefaultVirusCore( new EnergyzerCore( this ) {
			@Override
			public void update(float deltaTime) {
				super.update( deltaTime );
				isGlow = ( totalRotation > 40 ) ? true : false;
			};
		} );
		//move = false;
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("energyzer1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("energyzer2");		
		
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("energyzer-trail");	
		createAnim( normal, trail );
		
		Array<AtlasRegion> keyFrames = new Array<AtlasRegion>();
		keyFrames.add(assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("energyzer-light1"));
		keyFrames.add(assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("energyzer-light2"));
		
		bloomEffect = new Animation( 1/15f, keyFrames );
	}
	
	@Override
	public void draw(SpriteBatch batch,float deltaTime) {
		if ( !isVisible ) 
			return;		
		super.draw(batch, deltaTime);
		if ( isGlow ) {
			batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE);
			batch.draw(bloomEffect.getKeyFrame(elapsedTime, true), spriteCenter.x, spriteCenter.y, currentOriginCenterX, currentOriginCenterY, 
					currentFrameWidth, currentFrameHeight, scaleX, scaleY, getAngle());
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); //default
		}
	}
	
	/*@Override
	public void resetInPosition(float posX, float posY) {
		super.resetInPosition( WORLD_WIDTH * .5f, WORLD_HEIGHT * .5f );
	}*/
	
	public static class EnergyzerCore extends RotationTestCore {
		
		public EnergyzerCore( VirusType virus ) {
			super( virus );
			
			AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
			
			if ( TUTORIAL ) {
				arrow = assetManager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("circle-arrow");
				circleArrow_width = arrow.getRegionWidth();
				circleArrow_height = arrow.getRegionHeight();
				//Debug.log("circle arrow width: " + circleArrow_width + " | height: " + circleArrow_height);
				circleArrowClockwise = MathUtils.randomBoolean();
			}
			totalRotationToCompleteGesture = 340f;
			//pointer
			pointer = assetManager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("alpha-circle");
		}
		
		@Override
		protected void gestureComplete( boolean clockwise ) {
			father.destroy( true, true );
		}
		
	}

}
