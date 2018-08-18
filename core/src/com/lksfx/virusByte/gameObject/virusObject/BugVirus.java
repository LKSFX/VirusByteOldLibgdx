package com.lksfx.virusByte.gameObject.virusObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.BounceVirus;

public class BugVirus extends BounceVirus {
	
	private float timeToOut = 8f, time;
	
	public BugVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}
	
	public BugVirus(float x, float y) {
		super( x, y, 30, 600, 700, new Color(1f, 0f, 156/255f, 1f) );
		virus_id = VirusInstance.Bug;
		timeToOut = timeToOut + MathUtils.random(8f);
		rotate = false;
		// Immunity against everything
		isCollidable = false;
		isDamagedByCollision = false;
		isDamagedByExplosions = false;
		isDamagedByLaser = false;
		isDamagedBySaw = false;
		setInputController( new InputTriggers() {
			@Override
			public boolean justTouched( Vector3 mousePos ) {
				if ( isOver(mousePos) ) {
					Debug.log("clicked on bug");
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
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("bug1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("bug2");		
		
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("bug-trail");	
		createAnim(normal, trail);
		
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		time += deltaTime;
		if ( time > timeToOut+1f ) destroy();
	}
	
	/*------------------------------------------------*/
	/*@Override
	public boolean justTouched(Vector3 mousePos) {
		if (isOver(mousePos)) {
			Debug.log("clicked on bug");
			destroy(true, true);
			return true;
		}
		return false;
	}*/
	/*------------------------------------------------*/
	
	@Override
	public void bounces() {
		if ( time < timeToOut ) super.bounces();
	}
	
	@Override
	public void randomize_direction() {
//		boolean outOfPort = ( (position.y + currentFrameHeight*.7f) > worldHeight );
//		dir.set(MathUtils.random( (outOfPort) ? 0f : -( 1f+MathUtils.random(.4f) ), (outOfPort) ? 1f : .1f + MathUtils.random( .4f) ), MathUtils.random(-.9f, -.6f ) );
		float xDirection = MathUtils.random(.1f, .25f);
		dir.set( (MathUtils.randomBoolean()) ? xDirection : -xDirection , MathUtils.random(.6f, .9f) );
		velocityPush.set( dir ).scl( speed );
		movement.set(velocityPush).scl(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void resetInPosition() {
		resetInPosition(WORLD_WIDTH*.5f, WORLD_HEIGHT + 320f);
	}
	
	@Override
	public void destroy(boolean fatal, boolean hit) {
		super.destroy(fatal, hit);
		if ( hit ) VirusByteGame.VIRUS_MANAGER.addVirus(VirusManager.VirusInstance.Life);
	}
}
