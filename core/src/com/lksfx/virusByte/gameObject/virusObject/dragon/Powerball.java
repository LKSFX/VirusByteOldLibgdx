package com.lksfx.virusByte.gameObject.virusObject.dragon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.Assets.Particles;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner.SpawnableConfiguration;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner.Transition;
import com.lksfx.virusByte.gameObject.virusObject.dragon.Paddle.PADDLE;

public class Powerball extends Fireball {
	
	protected boolean isOnChargedPaddleState;
	private boolean collidedWithPaddle;
	private Paddle paddle;
	
	public Powerball(float x, float y, boolean isOnPaddle) {
		this(x, y, 0,  isOnPaddle ? 400 : 180, isOnPaddle ? 400 : 180, new Color( Color.WHITE ));
		isOnChargedPaddleState = isOnPaddle;
		isOnMove = isOnPaddle;
	}

	public Powerball(float x, float y, int point, int minSpd, int maxSpd, Color trailColor) {
		super(x, y, point, minSpd, maxSpd, trailColor, 0);
		isPowerball = true;
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		TextureRegion[] normal = new TextureRegion[1];
		normal[0] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( "gwhite-fireball" );
		createAnim(normal, null);
		gaussianAnim = getAnimation();
		normal[0] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( "white-fireball" );
		createAnim( normal, null );
		Debug.log( "Powerball on stage" );
		
	}
	
	@Override
	protected void rebounce(Paddle paddle, PADDLE part) {
		if ( !isOnChargedPaddleState && !collidedWithPaddle ) {
			this.paddle = paddle;
			collidedWithPaddle = true;
			paddle.charge();
			Spawner spawner = VirusByteGame.VIRUS_MANAGER.spawner;
			SpawnableConfiguration config = spawner.getSCS(1f, .01f, .05f, 0, Transition.SHRINK_OUT);
			spawner.addSpawnable(this, config, x, y);
			destroy();
		}
		lastEdgeContact = Edges.OTHER;
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if ( collidedWithPaddle ) {
			isOnMove = ( y <= paddle.y ) ? false : true;
			moveTo(paddle.x, y);
			if ( ((scaleX + scaleY) * .5f) < .2f ) destroy();
		}
	}
	
	@Override
	protected void updateMovementPermission(float deltaTime) {
		if ( !isOnChargedPaddleState && !collidedWithPaddle ) super.updateMovementPermission(deltaTime);
	}
	
	@Override
	public void randomize_direction() {
		if ( isOnChargedPaddleState )  {
			dir.set(0 , 1f);
			velocityPush.set( dir ).scl( speed ); 
			movement.set( velocityPush ).scl( Gdx.graphics.getDeltaTime() );
		} else {
			super.randomize_direction();
		}
	}
	
	@Override
	protected void collisionWithDragonEvent() {
		if ( isOnChargedPaddleState ) {
			VirusManager.PART_EFFECTS.createEffect(Particles.explosion, x, y, 30);
			if ( VirusByteGame.SFX ) {
				// audio TODO
			}
		}
		super.collisionWithDragonEvent();
	}
}
