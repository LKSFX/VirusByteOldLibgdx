package com.lksfx.virusByte.gameObject.virusObject.pepperBros;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.BounceVirus;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.gameObject.pontuation.ScreenLogger;

public class PepperBomb extends BounceVirus implements Poolable {
	protected Animation animGaussian;
	protected boolean exploded;
	protected int maxBounces = 1;
	/**This game object is the ejector and has immunity against this project*/
	private int ejectorID;
	
	public PepperBomb() {
		this(VIEWPORT.getWorldWidth()*.5f, 1280f);
	}

	public PepperBomb(float x, float y) {
		this(x, y, 5, 250, 300, new Color(Color.RED));
	}

	public PepperBomb(float x, float y, int point, int minSpd, int maxSpd, Color trailColor) {
		super(x, y, point, minSpd, maxSpd, trailColor);
		setCollisionMaskBounds( 32, 32 );
		setTouchMaskBounds( currentFrameWidth*1.5f, currentFrameHeight * 1.5f );
		isComputeKillOn = false;
		setInputController( new InputTriggers() {
			@Override
			public boolean justTouched( Vector3 mousePos ) {
				if ( isOver( mousePos ) ) {
					destroy( true, true );
					return true;
				}
				return false;
			}
		} );
	}
	
	@Override
	public void randomize_direction() {
		boolean outOfPort = ( (y + currentFrameHeight*.7f) > worldHeight );
		dir.set((outOfPort) ? 0f : MathUtils.random(-1f, 1f), (outOfPort) ? 1f : MathUtils.random(-1, 1f) );
		velocityPush.set( dir ).scl( speed );
		movement.set(velocityPush).scl(Gdx.graphics.getDeltaTime());
	}
	
	public void setAngleDirection(float angleDirection, float spriteAngle) {
		setAngle( spriteAngle );
		float cosseno = MathUtils.cosDeg(angleDirection), seno = MathUtils.sinDeg(angleDirection);
		dir.set(cosseno, seno);
		velocityPush.set( dir ).scl( speed );
		movement.set(velocityPush).scl(Gdx.graphics.getDeltaTime());
	}

	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] regions = new AtlasRegion[3];
		regions[0] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("pepper-bomb-gaussian1");
		regions[1] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("pepper-bomb-gaussian2");
		regions[2] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("pepper-bomb-gaussian3");
		createAnim(regions, null);
		animGaussian = getAnimation();
		regions = new AtlasRegion[3];
		regions[0] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("pepper-bomb1");
		regions[1] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("pepper-bomb2");
		regions[2] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("pepper-bomb3");
		createAnim( regions, null );
		
	}
	
	/** set the ejector for this projectile */
	public void setEjector( int id ) {
		ejectorID = id;
	}
	
	private boolean toExplode;
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		VirusType coll = collisionDetection( true );
		if ( coll != null && coll.virus_id == VirusInstance.Pepperbros ) {
			if ( totalBounces >= 1 || (ejectorID != 0 && ejectorID != coll.id) ) {
				toExplode = true;
				coll.explosionDamage( null ); //causes damage on the other boss
				destroy();
			} else {
				Debug.log( "collision with: " + coll.id + " | ejectorID = " + ejectorID );
			}
		}
		if ( !VIEWPORT.getCamera().frustum.boundsInFrustum(x, y, 0f, currentFrameWidth, currentFrameHeight, 0f) ) 
			destroy();
	}
	
	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		if (!isVisible) return;
		super.draw(batch, deltaTime);
		batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE);
		batch.draw(animGaussian.getKeyFrame(elapsedTime), spriteCenter.x, spriteCenter.y, currentOriginCenterX, currentOriginCenterY,
				currentFrameWidth, currentFrameHeight, scaleX, scaleY, getAngle());
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); //default
	}
	
	/*------------------------------------------------*/
	
	/*------------------------------------------------*/
	
	//>>>>>>>>>>>>>>>>>>>>>>>
	
	@Override
	public void top_bounce() {
		if ( totalBounces >= maxBounces ) 
			return;
		super.top_bounce();
	}
	
	@Override
	public void bottom_bounce() {  }
	
	@Override
	public void right_bounce() {
		if ( totalBounces >= maxBounces ) 
			return;
		super.right_bounce();
	}
	
	@Override
	public void left_bounce() {
		if ( totalBounces >= maxBounces ) 
			return;
		super.left_bounce();
	}
	
	// <<<<<<<<<<<<<<<<<<<<<<<
	
	private void explosionEffect() {
		/*VirusManager.effect.createEffect(Assets.Particles.explosion, position.x, position.y, 1f);
		if (VirusByteGame.SFX) {
			long sfx = GameSound.EXPLOSION.play();
			GameSound.EXPLOSION.setVolume(sfx, VirusByteGame.SFX_VOLUME );
		}
		if (VirusByteGame.VIBRATION) VirusByteGame.IMM.vibrate(Immersion.EXPLOSION5);*/
		exploded = true;
		isVisible = false;
	}
	
	@Override
	public void destroy(boolean fatal, boolean hit) {
		super.destroy(fatal, hit);
		boolean bool = ( !VIEWPORT.getCamera().frustum.boundsInFrustum(x, y, 0f, currentFrameWidth*.5f, currentFrameHeight*.5f, 0f) );
		if ( bool || toExplode ) 
			explosionEffect();
	}
	
	@Override
	public ScreenLogger getPointLog() {
		return null;
	}
	
	@Override
	public void reset() {
		super.reset();
		toExplode = false;
		exploded = false;
		isVisible = true;
//		ejectorID = 0;
	}
	
}
