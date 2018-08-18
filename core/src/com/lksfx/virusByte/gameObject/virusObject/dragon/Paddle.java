package com.lksfx.virusByte.gameObject.virusObject.dragon;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.gameObject.abstractTypes.BounceVirus.Edges;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner.SpawnableConfiguration;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner.Transition;

public class Paddle extends VirusType {
	private Animation chargedAnim;
	/**The meta y position of this paddle on the game stage*/
	private float metaYY;
	private boolean charged;
	private float chargedTimer, timeToChargeExplode = 3f;
	
	private boolean pressed;
	private Vector3 mousePos;
	private float deltaDst;
	
	private float lastPaddleX;
	protected float velocity;
	public final float friction = .25f;
	
	public Paddle(float x, float y) {
		this(x, y, 0, 180, 180, new Color( Color.WHITE ), false);
	}
	
	public Paddle(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		setTouchMaskBounds(currentFrameWidth * 1.5f, currentFrameHeight * 1.5f);
		setIndestructible( true );
		metaYY = WORLD_HEIGHT * .05f;
		isDestroyedOnBottom = false;
		isOnMove = false;
		setInputController( new InputTriggers() {
			@Override
			public boolean pressedTouch(Vector3 mousePos) {
				if ( Paddle.this.mousePos == null ) 
					Paddle.this.mousePos = mousePos;
				if ( isOver(mousePos) ) {
					deltaDst = (mousePos.x - Paddle.this.x);
					lastPaddleX = Paddle.this.x;
					pressed = true;
				}
				return false;
			}
			@Override
			public boolean releaseTouch(Vector3 mousePos) {
				pressed = false;
				velocity = 0;
				return super.releaseTouch(mousePos);
			}
		} );
	}

	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		TextureRegion[] normal = new TextureRegion[1];
		normal[0] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( "wall-charging" );
		createAnim(normal, null);
		chargedAnim = getAnimation();
		normal = new TextureRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( "wall1" );
		normal[1] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( "wall2" );
		createAnim( normal, null );
		
	}
	
	/**Position in relation of the center of the paddle, ONE is the center*/
	protected enum PADDLE {ONE, TWO, THREE, FOUR, FIVE};
	
	@Override
	public void update(float deltaTime) {
		super.update( deltaTime );
		
		VirusType collided;
		if ( (collided = collisionDetection( true )) != null ) {
			if ( collided instanceof Fireball ) {
				Fireball fireball = (Fireball) collided;
				// ========= //
				if ( Intersector.overlaps(fireball.circle, collisionMask.getRectangle()) ) {
					float halfWidth = currentFrameWidth *.5f;
					/*float fireballX = fireball.position.x, fireballY = fireball.position.y;*/
					float distanceFromTheLeftOfThePaddle = fireball.x - (x -  halfWidth);
//					Debug.log( "dstFromLeftBorder % currentFrameWidth: " +  distanceFromTheLeftOfThePaddle % (currentFrameHeight / 4) );
					float partW = currentFrameWidth / 5;
					if ( fireball.getLastEdgeContact() != Edges.OTHER ) {
						// ====== //
						if ( distanceFromTheLeftOfThePaddle < partW ) {
							// Extreme left side of the paddle
							fireball.rebounce( this, PADDLE.ONE );
						} else if ( distanceFromTheLeftOfThePaddle >= partW && distanceFromTheLeftOfThePaddle < (partW * 2)) {
							// Left side of the paddle
							fireball.rebounce( this, PADDLE.TWO );
						} else if ( distanceFromTheLeftOfThePaddle >= (partW * 2) && distanceFromTheLeftOfThePaddle < (partW * 3) ) {
							// Center of the paddle
							fireball.rebounce( this, PADDLE.THREE );
						} else if ( distanceFromTheLeftOfThePaddle >= (partW * 3) && distanceFromTheLeftOfThePaddle < (partW * 4) ) {
							// Right side of the paddle
							fireball.rebounce( this, PADDLE.FOUR );
						} else if ( distanceFromTheLeftOfThePaddle >= (partW * 4) && (distanceFromTheLeftOfThePaddle - fireball.circle.radius) < (partW * 5) ) {
							// Extreme right side of the paddle
							fireball.rebounce( this, PADDLE.FIVE );
						}
	 					// ====== //
					}
				}
				// ========= //
			}
		}
		
		if ( pressed ) {
			if ( isOver( mousePos ) ) {
				float halfWidth = currentFrameWidth *.5f;
				moveTo(mousePos.x - deltaDst, y);
				if ( (x + halfWidth) > WORLD_WIDTH ) {
					moveTo(WORLD_WIDTH - halfWidth, y);
				} else if ( (x - halfWidth) < 0 ) {
					moveTo(halfWidth, y);
				}
			}
			velocity = lastPaddleX - x;
		}	
		
		// Reach the meta vertical position in the stage
		if ( y < metaYY ) {
			moveAdd(0, 80 * deltaTime);
		}
		
		if ( charged && (chargedTimer += deltaTime) >= timeToChargeExplode ) {
			shootPowerball();
		}
		lastPaddleX = x;
	}
	
	/**Make this paddle charged*/
	protected void charge() {
		charged = true;
		setAnimation( chargedAnim );
		setFlashOn(Color.WHITE, timeToChargeExplode * 1.2f);
	}
	
	private void shootPowerball() {
		Powerball ball = new Powerball(x, y, true);
		Spawner spawner = VirusByteGame.VIRUS_MANAGER.spawner;
		SpawnableConfiguration config = spawner.getSCS(0f, .020f, 1f, .2f, Transition.GROW_IN_STOPPED_TO_MOVE);
		spawner.addSpawnable( ball, config, 0, 0, getPosition() );
		setAnimation( anim_default );
		chargedTimer = 0;
		charged = false;
	}
	
}
