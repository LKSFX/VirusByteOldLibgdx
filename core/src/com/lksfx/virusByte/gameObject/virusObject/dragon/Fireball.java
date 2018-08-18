package com.lksfx.virusByte.gameObject.virusObject.dragon;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.debug.Debug.SHAPE;
import com.lksfx.virusByte.gameObject.abstractTypes.BounceVirus;
import com.lksfx.virusByte.gameObject.virusObject.dragon.DragonBoss.BALANCE;
import com.lksfx.virusByte.gameObject.virusObject.dragon.Paddle.PADDLE;

public class Fireball extends BounceVirus {
	private boolean debug_circle = false, debug_direction_lines = false;
	/**Define the fire ball sprite color*/
	private int index;
	protected Circle circle;
	private float launchTimer, timeToLaunch = 3f;
	/**When in swing effect center is*/
	private float centerY;
	
	protected boolean isPowerball;
	
	protected Animation gaussianAnim;
	
	public Fireball() {
		this(0, 0, 1);
	}

	public Fireball(float x, float y) {
		this(x, y, 1);
	}
	
	public Fireball(float x, float y, int fireballIndex) {
		this(x, y, 0, 230, 250, new Color( Color.WHITE ), fireballIndex);
	}

	public Fireball(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, int fireballIndex) {
		super(x, y, point, minSpd, maxSpd, trailColor);
		isPowerball = false;
		if ( fireballIndex > 7 ) 
			fireballIndex = 7;
		index = fireballIndex;
		initialize(x, y, point, minSpd, maxSpd, trailColor, isTrailOn);
		circle = new Circle(getPosition(), currentFrameWidth * .4f);
		vectors = new Vector2[3];
		for (int i = 0; i < vectors.length; i++) vectors[i] = new Vector2();
		isOnMove = false;
	}
	
	@Override
	public void bottom_bounce() {}

	@Override
	public void randomize_direction() {
		boolean leftSide = ( x < WORLD_WIDTH * .5f );
//		float min, max;
		
		float xDirection = ( leftSide ) ? MathUtils.random(-.4f, .25f) : MathUtils.random(-.25f, .4f);
		
		Debug.log( "Ball Initialized! | and speed = " + speed );
		dir.set( xDirection , MathUtils.random(-.9f, -.6f) );
		Debug.log( "direction before: " + dir.toString());
		set_direction();
		Debug.log( "direction after: " + dir.toString());
	}
	
	/**When collided with the fire wall paddle, this method is executed.
	 * @param paddle TODO
	 * @param part One of the five parts of the fire wall paddle. THREE is the center*/
	protected void rebounce(Paddle paddle, PADDLE part) {
		Debug.log( "Collision part: " + part );
		// ===== //
		/*if ( part == PADDLE.ONE || part == PADDLE.FIVE ) {
			dir.x = (( part == PADDLE.ONE ) ? -1 : 1) * Math.abs( dir.x );
		} else if ( part == PADDLE.THREE ) {
			dir.x = 0; 
		}
		// ===== //
		float paddleTopY = paddle.collision_mask_bounds.y += paddle.collision_mask_bounds.height;
		float ballBottomY = circle.y - circle.radius;
		float dif = (paddleTopY - ballBottomY) * .075f;
		Vector2 vec = dir.cpy().sub(0, dif);
		float crs = vec.crs( dir );
		Debug.log( "dst: " + dif + " | crs: " + crs);
		
		float yy = -(1f - Math.abs( crs ) ) * dir.y;
		dir.y = yy;*/
		float paddleVelocity = paddle.velocity;
		if ( paddleVelocity != 0 ) {
			// Paddle in move
			boolean positive = ( paddleVelocity > 0 ) ? true : false;
			float sqrt = Math.round( Math.sqrt( Math.signum(paddleVelocity) ) );
			dir.x = dir.x += (paddle.friction * ( (positive) ? sqrt : -sqrt) );
			dir.y = -1f * dir.y;
		} else {
			// Paddle stopped
			if ( part == PADDLE.ONE || part == PADDLE.FIVE ) {
				// Borders
				dir.x = (( part == PADDLE.ONE ) ? -1 : 1) * Math.abs( dir.x );
				dir.y = -1f * dir.y;
			} else if ( part == PADDLE.THREE ) {
				// Center
				dir.x = 0; 
				float xx = dir.x;
				dir.y = -1f * (dir.y + xx);
			} else {
				dir.y = -1f * dir.y;
			}
		}
		Debug.log( "Paddle Velocity: " + paddleVelocity );
		Debug.log( "ball rebounce direction>: " + dir.toString() );
		
		set_direction();
		Debug.log( "push: " + velocityPush.toString() );
		
		lastEdgeContact = Edges.OTHER;
	}
	
	private TextureRegion[] textureRegion;
	
	@Override
	public void setAtlas() {
		if ( index == 0 ) 
			return;
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		textureRegion = new TextureRegion[1];
		textureRegion[0] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( "gfireball"+index );
		createAnim(textureRegion, null);
		gaussianAnim = getAnimation();
		textureRegion[0] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( "fireball"+index );
		createAnim( textureRegion, null );
		
	}
	
	public Fireball setBallColor( int indexColor ) {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		textureRegion[0] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( "gfireball"+indexColor );
		createAnim(textureRegion, null);
		gaussianAnim = getAnimation();
		textureRegion[0] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( "fireball"+indexColor );
		createAnim( textureRegion, null );
		
		return this;
	}
	
	private BALANCE swingState = BALANCE.NONE;
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		circle.setPosition( getPosition() );
		updateMovementPermission(deltaTime);
		
		if ( (glowTimer += deltaTime) >= glowTimeInverval ) {
			glowTimer = 0;
			isGlow = !isGlow;
		}
	}
	
	protected void updateMovementPermission(float deltaTime) {
		if ( (launchTimer += deltaTime) >= timeToLaunch ) {
			isOnMove = true;
		} 
		else {
			switch ( swingState ) {
			case DOWN:
				if ( y > centerY-5f) {
					moveAdd(0, -(25 * deltaTime));
				} else {
					swingState = BALANCE.TOP;
				}
				break;
			case TOP:
				if ( y < centerY+5f) {
					moveAdd(0, 25 * deltaTime);
				} else {
					swingState = BALANCE.DOWN;
				}
				break;
			default:
				swingState = BALANCE.DOWN;
				break;
			}
		}
	}
	
	private boolean isGlow;
	private float glowTimer;
	private float glowAlpha = .6f;
	private final float glowTimeInverval = .2f;
	
	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		super.draw(batch, deltaTime);
		if ( !isVisible || !isGlow) return;
		batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE);
		float mul = .76f;
		float radio = currentFrameWidth*mul;
		float center = radio * .5f;
		float xx = x - center, yy = y - center;
		batch.setColor(1f, 1f, 1f, glowAlpha);
		batch.draw(gaussianAnim.getKeyFrame(elapsedTime), xx, yy, center, center, 
				radio, radio, scaleX, scaleY, getAngle());
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); //default
		batch.setColor(1f, 1f, 1f, 1f);
	}
	
	private Color shapeColor = new Color(1, 1, 1, .5f);
	private Vector2[] vectors;
	
	@Override
	public void debug_drawings() {
		super.debug_drawings();
		
		if ( debug_direction_lines ) {
			if ( !isOnMove ) {
				Rectangle collision_mask_bounds = collisionMask.getRectangle();
				float xx = collision_mask_bounds.x, yy = collision_mask_bounds.y, ww = collision_mask_bounds.width, hh = collision_mask_bounds.height;
				vectors[0].set(xx, yy + hh).scl( velocityPush ).scl( 300f );
				vectors[1].set(xx + ww, yy).scl( velocityPush ).scl( 300f );
				vectors[2].set( getPosition() ).scl( velocityPush ).scl( 300f );
				Debug.debug.insertShapeToRender(SHAPE.LINE, xx, yy + hh, vectors[0].x, vectors[0].y, Color.RED);
				Debug.debug.insertShapeToRender(SHAPE.LINE, xx + ww, yy, vectors[1].x, vectors[1].y, Color.RED);
				Debug.debug.insertShapeToRender(SHAPE.LINE, x, y, vectors[2].x, vectors[2].y, Color.RED);
			}
		}
		
		if ( debug_circle ) {
			Debug.debug.insertShapeToRender(SHAPE.CIRCLE, circle.x, circle.y, circle.radius, circle.radius, shapeColor);
		}
	}
	
	@Override
	public void resetInPosition(float posX, float posY) {
		super.resetInPosition(posX, posY);
		centerY = posY;
	}
	
	@Override
	public void reset() {
		super.reset();
		isOnMove = false;
		launchTimer = 0;
		swingState = BALANCE.NONE;
	}
	
	/**Method evoked when collide with the dragon boss*/
	protected void collisionWithDragonEvent() {
		destroy();
	}
}
