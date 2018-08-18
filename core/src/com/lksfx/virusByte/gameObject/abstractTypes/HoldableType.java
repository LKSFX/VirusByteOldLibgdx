package com.lksfx.virusByte.gameObject.abstractTypes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;

public abstract class HoldableType extends VirusType {
	
	/**Determine if this object is on hand (grabbed) or not*/
	private boolean isHold;
	/**Position of the finger or cursor in the stage*/
	protected Vector3 mousePos;
	/**Define the default behavior of grab when pressed over and release when (release trigger)*/
	protected boolean isDefaultGrabBehaviour;
	/**Determine if this object can be stored on inventory slot or not*/
	private boolean isStorable = true;
	/**if can be grab*/
	private boolean isGrabAllowed = true;
	private int depth;
	
	//Scales 
	protected float currentScale;
	protected float maxIncreasedScale = .5f;
	protected float defaultBaseScale = 1f;
	protected float liftSpeed = .5f;
	protected float dropSpeed = .5f;
	protected boolean isReachPeakScale;
	/**if this object is rendering on the top render call, thats mean above the {@link GameHud} and other renders*/
	private boolean isOnTopRender;
	/**Save the original depth defined by {@link #setDepth(int)}*/
	private int baseDepth;
	
	public HoldableType(float x, float y) {
		this(x, y, 100, 80, 150, new Color( Color.WHITE ), true);
	}
	
	public HoldableType(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		setIndestructible( true );
		currentScale = defaultBaseScale;
		baseDepth = getDepth();
		setInputController( new InputTriggers() {
			@Override
			public boolean pressedTouch(Vector3 mousePos) {
				if ( isDefaultGrabBehaviour && !HOLDING_VIRUS && isOver(mousePos) ) grab();
				return super.pressedTouch(mousePos);
			}
			
			@Override
			public boolean releaseTouch(Vector3 mousePos) {
				if ( isDefaultGrabBehaviour && isHold ) release();
				return super.releaseTouch(mousePos);
			}
		} );
	}
	
	@Override
	public void setScale(float scaleX, float scaleY) {
		super.setScale( scaleX, scaleY );
		currentScale = (scaleX + scaleY) * .5f;
	}
	
	public void grab() {
		if ( !isGrabAllowed ) return;
		HOLDING_VIRUS = true;
		mousePos = VirusByteGame.MOUSE_POS;
		isHold = true;
	}
	
	public void release() {
		HOLDING_VIRUS = false;
		isHold = false;
	}
	
	/**Method called when this object enter inventory slot*/
	public void onSlotEnter() {}
	
	/**Method called when this object out from inventory slot*/
	public void onSlotOut() {
		setScale( defaultBaseScale+maxIncreasedScale );
		isOnTopRender = true;
		int invertDepth = ( getDepth() < 0 ) ? -getDepth() : 0;
		setDepth( 50 + invertDepth );
	}
	
	public boolean isGrabbed() {
		return isHold;
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		if ( isHold ) {
			moveTo( mousePos.x, mousePos.y );
			float metaScale = defaultBaseScale+maxIncreasedScale;
			if ( !isReachPeakScale && currentScale < metaScale ) {
				setScale( currentScale + (liftSpeed * deltaTime) );
				isReachPeakScale = ( currentScale >= metaScale );
				if ( !isOnTopRender && currentScale > metaScale * .75f ) {
					isOnTopRender = true;
					int negative = ( getDepth() > 0 ) ? 0 : getDepth();
					setDepth( 50 + negative );
				}
			}
		} 
		else {
			if ( currentScale > defaultBaseScale ) {
				float metaScale = defaultBaseScale+maxIncreasedScale;
				setScale( currentScale - ( (liftSpeed+dropSpeed) * deltaTime ) );
				if ( currentScale < defaultBaseScale ) 
					setScale( defaultBaseScale );
				if ( isOnTopRender && currentScale < metaScale * .75f ) { // return to default depth
					if ( isStorable ) 
						VirusByteGame.HUD.inventory.releaseOnInvetoryCheck( this );
					isOnTopRender = false;
					setDepth( baseDepth );
				}
				isReachPeakScale = false;
			}
			else {
				baseDepth = depth;
			}
		}
		
	}
	
	/*@Override
	public boolean pressedTouch(Vector3 mousePos) {
		if ( isDefaultGrabBehaviour && !HOLDING_VIRUS && isOver(mousePos) ) grab();
		return super.pressedTouch(mousePos);
	}
	
	@Override
	public boolean releaseTouch(Vector3 mousePos) {
		if ( isDefaultGrabBehaviour && isHold ) release();
		return super.releaseTouch(mousePos);
	}*/
	
	@Override
	public void setDepth(int value) {
		super.setDepth( value );
		this.depth = value;
	}
	
	/**set if this object can be stored or not*/
	public void setSlotPermission(boolean bool) {
		isStorable = bool;
	}
	
	/**@return true if this object can be stored on the inventory*/
	public boolean getSlotPermission() {
		return isStorable;
	}
	
	/**Set if this object can be grab*/
	public void setGrabAllowed(boolean bool) {
		isGrabAllowed = bool;
	}
	
}
