package com.lksfx.virusByte.gameObject.abstractTypes;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;

public abstract class GameObject implements DrawableObject {
	
	public float x, y;
	private Vector2 position;
	private Vector2 position_copy;
	private Vector2 direction;
	public Vector2 spriteCenter;
	private Animation animation;
	private int depth;
	private String objectName = "unnamed";
	public final DetectionMask spriteMask;
	public final DetectionMask collisionMask;
	public final DetectionMask touchMask;
	/**the current frame being displayed*/ 
	public TextureRegion currentFrame;
	
	/** when this variable is true the object is removed from the current stage on the next {@link #mainLateUpdate(float)} */
	private boolean remove;
	/**When true inputs can interact with this object*/
	private boolean isInputActive = true;
	/** If this object is to be draw on render update or not */
	public boolean isVisible = true;
	public boolean allowMove = true;
	public boolean isMoving;
	public float scaleX = 1f;
	public float scaleY = 1f;
	private float angle;
	private float speed;
	protected float elapsedTime;
	
	/**Store input actions to perform during {@link #update(float)} phase*/
	private Array< InputTriggers > inputs;
	
	private float animationVelocity;
	private Animation.PlayMode animationPlayMode;
	
	/**information about current sprite frame*/
	public float currentOriginCenterX, currentOriginCenterY, currentFrameWidth, currentFrameHeight;
	
	public GameObject() {
		position = new Vector2();
		position_copy = new Vector2();
		direction = new Vector2();
		spriteCenter = new Vector2();
		spriteMask = new DetectionMask();
		collisionMask = new DetectionMask();
		touchMask = new DetectionMask();
		animationPlayMode = PlayMode.LOOP;
		animationVelocity = .066f;
		inputs = new Array< InputTriggers > ();
		setInputController( new InputTriggers() );
	}
	
	/** Actions update that any game object must have,
	 * this function call the {@link #update(float)} function that can be override on child,
	 * <p>this function is called every frame</p> */
	public final void mainFixedUpdate( float deltaTime ) {
		
		if ( allowMove ) {
			Vector2 vec2 = VirusByteGame.GAME.obtainVector2();
			
			vec2.set( direction ).scl( speed * deltaTime );
			moveAdd( vec2.x, vec2.y );
			
			VirusByteGame.GAME.freeVector( vec2 );
			isMoving = !direction.isZero();
		} else {
			isMoving = false;
		}
		
		update( deltaTime );
		
	}
	
	/** Actions update that any game object must have,
	 * this function call the {@link #lateUpdate(float)} function that can be override on child,
	 * <p>this function is called every frame after the draw</p> */
	public final void mainLateUpdate( float deltaTime ) {
		lateUpdate( deltaTime );
	}
	
	/** Put the actions and checks in this function for any object behavior */
	public void update( float deltaTime ) {}
	
	/** Put the actions and checks in this function for any object behavior */
	public void lateUpdate( float deltaTime ) {}
	
	/** Add value to the sprite angle of this object */
	public void angleAdd( float value ) {
		angle += value;
		if ( angle < 0 ) 
			angle = 360;
		else if ( angle > 360 )
			angle = 0;
		collisionMask.setAngle( angle );
		touchMask.setAngle( angle );
	}
	
	/**move the amount of pixels*/
	public void moveAdd( float x, float y ) {
		position.add( x, y );
		spriteMask.setPosition( position );
		collisionMask.setPosition( position );
		touchMask.setPosition( position );
		this.x = position.x;
		this.y = position.y;
	}
	
	/**move to position */
	public void moveTo( float x, float y ) {
		position.set( x, y );
		spriteMask.setPosition( position );
		collisionMask.setPosition( position );
		touchMask.setPosition( position );
		this.x = position.x;
		this.y = position.y;
	}
	
	/** Continuous add a movement */
	public void moveTowards( Vector2 vector2, float speed ) {
		direction.set( vector2 ).nor();
		this.speed = speed;
	}
	
	/** Continuous add a movement in direction of an angle */
	public void moveTowards( float degrees, float speed ) {
		float sine = MathUtils.sinDeg( degrees );
		float cosine = MathUtils.cosDeg( degrees );
		Debug.log( "Sine: " + sine + " | cosine: " + cosine );
		moveTowards( new Vector2( cosine, sine ), speed );
	}
	
	public void addInputController( InputTriggers controller ) {
		if ( controller != null )
			inputs.add( controller );
	}
	
	/** @return the current object sprite angle */
	public float getAngle() {
		return angle;
	}
	
	/** @return this game object animation if already set, else return null */
	public Animation getAnimation() {
		return animation;
	}
	
	@Override
	/** @return the depth of this game object */
	public int getDepth() {
		return depth;
	}
	
	public InputTriggers getInputController() {
		return getInputController( 0 );
	}
	
	public InputTriggers getInputController( int index ) {
		int size = inputs.size;
		if ( index >= size )
			index = size - 1;
		return inputs.get( index );
	}
	
	public Array< InputTriggers > getInputControllerList() {
		return inputs;
	}
	
	/** @return object name */
	public String getName() {
		return objectName;
	}
	
	/** @return this game object position */
	public Vector2 getPosition() {
		return position_copy.set( position );
	}
	
	public float getSpeed() {
		return speed;
	}
	
	/** @return true if this object has animation already set */
	public boolean isAnimationSet() {
		return ( animation != null );
	}
	
	/** @return true when this object is receiving input triggers and interacting with touch and mouse */
	public boolean isInputsActive() {
		return isInputActive;
	}
	
	/** Return true if previously the remove method is called */
	public boolean isToRemoveFromStage() {
		return remove;
	}
	
	/** Set the angle of this sprite object */
	public void setAngle( float angle ) {
		this.angle = angle;
		collisionMask.setAngle( angle );
		touchMask.setAngle( angle );
	}
	
	/** Set the animation for this game object */
	public void setAnimation( Animation animation ) {
		this.animation = animation;
		this.animation.setFrameDuration( animationVelocity );
		this.animation.setPlayMode( animationPlayMode );
	}
	
	/** Set this game object animation speed */
	public void setAnimationVelocity( float velocity ) {
		this.animationVelocity = velocity;
		if ( isAnimationSet() )
			animation.setFrameDuration( animationVelocity );
	}
	
	public void setAnimationPlayMode( PlayMode mode ) {
		this.animationPlayMode = mode;
		if ( isAnimationSet() ) 
			animation.setPlayMode( animationPlayMode );
	}
	
	/** Set a depth for this object */
	public void setDepth( int depth ) {
		this.depth = depth;
	}
	
	/** Define a inputs class receptor */
	public void setInputController( InputTriggers controller ) {
		inputs.clear();
		if ( controller != null )
			inputs.add( controller );
	}
	
	/** Set if this object will interact with touch and mouse inputs when triggered or not */
	public void setInputsActive( boolean active ) {
		isInputActive = active;
	} 
	
	/** Set a name for this object */
	public void setName( String name ) {
		objectName = name;
	}
	
	/**update the position of sprite to draw correctly*/
	public void spriteCentralize() {
		
		currentFrame = animation.getKeyFrame( elapsedTime );
		
		//updates for current information
		currentFrameWidth = currentFrame.getRegionWidth();
		currentFrameHeight = currentFrame.getRegionHeight();
		
		Rectangle sprite_bounds = spriteMask.getRectangle();
		float xx = ( sprite_bounds.getX()+(sprite_bounds.width*.5f) )-(currentFrameWidth*.5f),
				yy = ( sprite_bounds.getY()+(sprite_bounds.height*.5f) )-(currentFrameHeight*.5f);
		spriteCenter.set(xx, yy);
		
		//updates for current information
		currentOriginCenterX = currentFrameWidth*.5f;
		currentOriginCenterY = currentFrameHeight*.5f;
		
	}
	
	/**very important be called once every frame inside update method*/
	public void spriteUpdate( float deltaTime ) {
		elapsedTime += deltaTime;
		spriteCentralize();
	}

	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		
		batch.draw(animation.getKeyFrame(elapsedTime), spriteCenter.x, spriteCenter.y, currentOriginCenterX, currentOriginCenterY,
				currentFrameWidth, currentFrameHeight, scaleX, scaleY, angle);
		
	}
	
	/** Remove this object from the game stage */
	public void remove() {
		remove = true;
	}
	
	/** In case of the children be a pooled object this method can reset some base properties */
	public void mainGameObjectReset() {
		remove = false;
		elapsedTime = 0;
		isInputActive = true;
	}
	
}
