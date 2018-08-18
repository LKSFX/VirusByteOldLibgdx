package com.lksfx.virusByte.gameObject.virusObject.baidu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.abstractTypes.BounceVirus;
import com.lksfx.virusByte.gameObject.pontuation.ScreenLogger;
import com.lksfx.virusByte.gameObject.virusObject.baidu.BaiduBoss.Heads;

public class BaiduEgg extends BounceVirus {
	
	private BaiduBoss.Heads colorType;
	
	public BaiduEgg() {
		this(0, 0);
	}
	
	public BaiduEgg(float x, float y) {
		this(x, y, Heads.CYAN);
	}
	
	public BaiduEgg(float x, float y, BaiduBoss.Heads colorType) {
		this(x, y, 0, 180, 180, new Color( 93f/255f, 0f/255f, 255f/255f, 1f));
		this.colorType = colorType;
		isDamagedByExplosions = false;
		minimumDistanceToTheLastTrailPosition = 12f;
		canBeFree = false;
		isComputeKillOn = false;
		setDepth( 10 );
		initialize(x, y, point, minSpd, maxSpd, trailColor, isTrailOn);
	}
	
	public BaiduEgg(float x, float y, int point, int minSpd, int maxSpd, Color trailColor) {
		super(x, y, point, minSpd, maxSpd, trailColor);
		setInputController( new InputTriggers() {
			@Override
			public boolean justTouched(Vector3 mousePos) {
				if ( isOver(mousePos) ) {
					//Debug.log("clicked on egg");
					destroy(true, true);
					return true;
				}
				return false;
			}
		} );
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[3];
		normal[0] = assetManager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("cyan-egg");
		normal[1] = assetManager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("pink-egg");
		normal[2] = assetManager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("green-egg");
		AtlasRegion trail = assetManager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("egg-trail");
		createAnim( normal, trail );
		
	}
	
	/*------------------------------------------------*/
	
	/*------------------------------------------------*/
	
	@Override 
	public void randomize_direction() {
		//Debug.log("movement vector: " + movement.toString());
		//Debug.log("velocityPush vector: " + velocityPush.toString());
		//Debug.log("dir vector: " + dir.toString());
		float x = 0, y = 0, xx = 0, yy = 0;
		
		if ( colorType == Heads.CYAN ) {
			//Debug.log("egg spawned on left side of screen");
			x = -1f; xx = -.3f;
			y = -1f; yy = -.3f;
		} 
		else if ( colorType == Heads.PINK ) {
			//Debug.log("egg spawned on center of screen");
			x = -.25f; xx = .25f;
			y = -1f; yy = 0;
		} 
		else if ( colorType == Heads.GREEN ) {
			//Debug.log("egg spawned on right side of screen");
			x = 1f; xx = .3f;
			y = -1f; yy = -.3f;
		}
		//Debug.log("value of x: " + x + " | value of xx: " + xx);
		dir.set( MathUtils.random(x, xx), MathUtils.random(y, yy) ).nor();
		velocityPush.set( dir ).scl( speed );
	}
	
	boolean isInUse;
	
	@Override
	public void resetInPosition( float posX, float posY ) {
		super.resetInPosition( posX, posY );
		isInUse = true;
	}
	@Override
	public void destroy( boolean fatal, boolean hit ) {
		super.destroy( fatal, hit );
		isInUse = false;
	}
	
	@Override
	public ScreenLogger getPointLog() {
		return null;
	}
	
}
