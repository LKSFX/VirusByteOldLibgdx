package com.lksfx.virusByte.gameObject.abstractTypes;

import static com.lksfx.virusByte.gameControl.debug.Debug.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;

public abstract class RotationableCore extends VirusCore< VirusType > {
	
	protected float arrowAngle, circleArrow_width, circleArrow_height;
	protected TextureRegion arrow, pointer;
	protected Color pointerColor;
	
	protected float lastRotPos = 0;
	protected float totalRotation, totalRotationToCompleteGesture = 360f, rotAdd;
	protected boolean active, clockwise, circleArrowClockwise;
	/**The maximum distance between the finger or cursor and the center axis*/
	protected float maximumDistanceToCenterAxis;
	/**The minimum distance between the finger or cursor and the center axis*/
	protected float minimumDistanceToCenterAxis;
	/**The rotation weight, smaller is more heavy to rotate*/
	protected float rotationTorque = 1f;
	protected float relativeDistanceToFinger;
	protected float relativeAngle;
	/**Determine allowed rotation direction*/
	protected boolean allowClockwiseRotation = true, allowCounterClockwiseRotation = true;
	/**if the finger is touching the screen or mouse is pressed on screen*/
	protected boolean isPressed;
	public boolean rotateLock;
	
	public RotationableCore( VirusType virus ) {
		super( virus );
		AssetManager manager =  VirusByteGame.ASSETS.getAssetManager();
		
		if ( VirusType.TUTORIAL ) {
			arrow = manager.get( Assets.Atlas.iconsAtlas.path, TextureAtlas.class ).findRegion("circle-arrow");
			circleArrow_width = arrow.getRegionWidth();
			circleArrow_height = arrow.getRegionHeight();
			//Debug.log("circle arrow width: " + circleArrow_width + " | height: " + circleArrow_height);
			circleArrowClockwise = MathUtils.randomBoolean();
		}
		maximumDistanceToCenterAxis = 100f;
		minimumDistanceToCenterAxis = 0;
		pointer = manager.get( Assets.Atlas.iconsAtlas.path, TextureAtlas.class ).findRegion("alpha-circle");
		pointerColor = new Color(1f, 91/255f, 0, .7f);
		virus.setInputController( new InputTriggers() {
			@Override
			public boolean pressedTouch( Vector3 mousePos ) {
				//int distance = VirusType.distanceTo(mousePos, position);
				isPressed = true;
				return false;
			}
			
			@Override
			public boolean releaseTouch( Vector3 mousePos ) {
				gestureEnd();
				isPressed = false;
				relativeDistanceToFinger = 0;
				return false;
			}
			@Override
			public boolean draggedTouch(Vector3 mousePos) {
				relativeDistanceToFinger = Vector2.dst(father.x, father.y, mousePos.x, mousePos.y);
				if ( active ) {
					if (!Gdx.input.isTouched()) {active = false; totalRotation = 0;} //on release touch
					float angle = getAngleRelativeToMouse( mousePos );
					//this.angle = angle;
					gestureUpdate(angle, rotationTorque);
					
					//Debug.log("degrees to mousePos is: " + Math.round(angle));
					
					int distance = VirusType.distanceTo(mousePos, father.getPosition());
					if ( rotateLock || (isPressed && distance 
							> maximumDistanceToCenterAxis || distance < minimumDistanceToCenterAxis) ) {
						gestureEnd();
					}
				} else if ( !rotateLock ) {
					if ( totalRotation == 0 && relativeDistanceToFinger 
							< maximumDistanceToCenterAxis && relativeDistanceToFinger 
							> minimumDistanceToCenterAxis ) {
						gestureStart(mousePos); //start the gesture method update
					}
				}
				return false;
			}
	} );
	}

	@Override
	public void update(float deltaTime) {
		// TODO Auto-generated method stub
		
	}
	
	public void draw( SpriteBatch batch, float deltaTime ) {
		String sss = "Rotation: " + MathUtils.round( totalRotation );
		debug.screen(sss, (int)(father.spriteCenter.x - ( 
				(Debug.debug.font.getBounds(sss).width - father.currentFrameWidth)*.5f)), (int)father.spriteCenter.y - 30, false);
		if ( VirusType.TUTORIAL ) {
			if (arrowAngle > 360) arrowAngle = 0;
			if (arrowAngle < 0) arrowAngle = 360f;
			arrowAngle = circleArrowClockwise ? arrowAngle-2f : arrowAngle+2f;
			batch.setColor(1f, 1f, 1f, 0.65f);
			batch.draw(arrow, father.spriteCenter.x-((circleArrow_width+10)*.265f), father.spriteCenter.y-(circleArrow_height*.24f), 
					(circleArrow_width+10)*.5f, circleArrow_height*.5f, circleArrow_width, circleArrow_height, circleArrowClockwise ? -(.8f) : .8f, .8f, arrowAngle);
			batch.setColor(1f, 1f, 1f, 1f);
			String str = "Rotation: " + MathUtils.round( totalRotation );
			debug.screen(str, (int)(father.spriteCenter.x - ( 
					(Debug.debug.font.getBounds(str).width - father.currentFrameWidth)*.5f)), (int)father.spriteCenter.y - 30, false);
		}
	}
	
	protected void gestureStart(Vector3 mousePos) {
		totalRotation = 0;
		lastRotPos = getAngleRelativeToMouse(mousePos);
		active = true;
	}
	
	protected void gestureEnd() {
		active = false;
		totalRotation = 0;
	}
	
	protected void gestureUpdate(float angle, float torque) {
		if ( totalRotation >= totalRotationToCompleteGesture ) 
			gestureComplete( clockwise );
		if ( totalRotation == 0 ) 
			clockwise = (angle < lastRotPos) ? true : false;
		
		if ( !clockwise ) {
			//rotation on counterClockwise
			if ( angle > lastRotPos ) {
				rotAdd = ( angle - lastRotPos ) * torque;
				//Debug.log("Rotation add: " + rotAdd);
				if ( allowCounterClockwiseRotation && rotAdd < 60 ) {
					totalRotation += rotAdd;
					father.angleAdd( rotAdd );
				}
				//Debug.log("totalRotation: " + totalRotation);
				lastRotPos = angle;
			} else {
				//if ((lastRotPos > 340) && (angle < 20)) lastRotPos = angle;
				if ( angle < lastRotPos ) {
					/*totalRotation -= rotAdd;
					if (totalRotation < 0) totalRotation = 0;*/
					//Debug.log("totalRotation: " + totalRotation);
					if ( lastRotPos > 340 ) {
						if (angle < 20) {
							lastRotPos = angle;
						} else {
							totalRotation = 0;
							clockwise = !clockwise;
						}
					} else {
						totalRotation = 0;
						clockwise = !clockwise;
					}
				}
			}
		} else {
			//rotation in clockwise direction
			if ( angle < lastRotPos ) {
				rotAdd = Math.abs( ( angle - lastRotPos) * torque);
				//Debug.log("last rotation: " + lastRotPos);
				if ( allowClockwiseRotation && rotAdd < 60 ) {
					totalRotation += rotAdd;
					father.angleAdd( -rotAdd );
				}
				//Debug.log("totalRotation: " + totalRotation);
				lastRotPos = angle;
			} else {
				//if ((lastRotPos > 340) && (angle < 20)) lastRotPos = angle;
				if ( angle > lastRotPos ) {
					/*totalRotation -= rotAdd;
					if (totalRotation < 0) totalRotation = 0;*/
					//Debug.log("totalRotation: " + totalRotation);
					if ( lastRotPos < 20 ) {
						if ( angle > 340 ) {
							lastRotPos = angle;
						} else {
							totalRotation = 0;
							clockwise = !clockwise;
						}
					} else {
						totalRotation = 0;
						clockwise = !clockwise;
					}
				}
			}
		}
	}
	
	/**
	 * @param clockwise true for clockwise, false for counterclockwise*/
	protected abstract void gestureComplete(boolean clockwise);
	
	/**get the angle relative to finger/mouse position*/
	protected float getAngleRelativeToMouse(Vector3 mousePos) {
		float angle = MathUtils.atan2(mousePos.y - father.y, mousePos.x - father.x);
		angle = angle*(180/MathUtils.PI);
		if(angle < 0) {
			angle = 360 - (-angle);
		}
		return (relativeAngle = angle);
	}
	
	@Override
	public void reset() {
		if ( VirusType.TUTORIAL ) 
			circleArrowClockwise = MathUtils.randomBoolean();
		totalRotation = 0;
		relativeAngle = 0;
		isPressed = false;
		father.setAngle( 0 );
		Debug.log( father.virus_id + " called reset() method | angle: " + father.getAngle() );
	}
	
}
