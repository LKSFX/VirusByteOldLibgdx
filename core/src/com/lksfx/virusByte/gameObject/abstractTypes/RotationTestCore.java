package com.lksfx.virusByte.gameObject.abstractTypes;

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
import com.lksfx.virusByte.gameObject.abstractTypes.DetectionMask.MaskFormat;

import static com.lksfx.virusByte.gameControl.debug.Debug.debug;

public abstract class RotationTestCore extends VirusCore< VirusType > {
	
	protected float arrowAngle, circleArrow_width, circleArrow_height;
	protected TextureRegion arrow, pointer;
	protected Color pointerColor;
	
	protected float lastRotPos = 0;
	protected float totalRotation, totalRotationToCompleteGesture = 360f;
	protected float rotationAddLastUpdate;
	protected boolean active, circleArrowClockwise;
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
	public boolean rotateLock;
	
	
	// new 
	Vector2 vec2 = new Vector2();
	Vector2 position = new Vector2();
	
	protected boolean isDebugOn;
	protected boolean isGestureCapting;
	protected boolean isClockwise;
	
	float adjacent;
	float opposite;
	float hypotenuse;
	
	float cosine;
	float sine;
	float tangent;
	
	float delta_cos;
	float delta_sin;
	float delta_tan;
	
	float difference;
//	float total_rotation;
	float current_angle;
	float delta_angle;
	
	public RotationTestCore( VirusType virus ) {
		super( virus );
		
		AssetManager manager = VirusByteGame.ASSETS.getAssetManager();
		
//		virus.isOnMove = false;
//		isDebugOn = true;
		if ( VirusType.TUTORIAL ) {
			arrow = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("circle-arrow");
			circleArrow_width = arrow.getRegionWidth();
			circleArrow_height = arrow.getRegionHeight();
			//Debug.log("circle arrow width: " + circleArrow_width + " | height: " + circleArrow_height);
			circleArrowClockwise = MathUtils.randomBoolean();
		}
		maximumDistanceToCenterAxis = 100f;
		minimumDistanceToCenterAxis = 0;
		pointer = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("alpha-circle");
		pointerColor = new Color(1f, 91/255f, 0, .7f);
		
		float circleRadius = father.currentFrameWidth *.35f;
		father.touchMask.setMaskFormat( MaskFormat.CIRCLE );
		father.touchMask.setCircleRadius( circleRadius );
		father.collisionMask.setMaskFormat( MaskFormat.CIRCLE );
		father.collisionMask.setCircleRadius( circleRadius );
		father.getInputControllerList().clear();
		
		father.setInputController( new InputTriggers() {
			@Override
			public boolean pressedTouch( Vector3 mousePos ) {
				
				vec2.set( mousePos.x, mousePos.y );
				if ( father.getPosition().dst( vec2 ) < maximumDistanceToCenterAxis )
					gestureStart( mousePos );
				
				return false;
			}
			
			@Override
			public boolean releaseTouch( Vector3 mousePos ) {
				gestureEnd();
				return false;
			}
			@Override
			public boolean draggedTouch(Vector3 mousePos) {
				vec2.set( mousePos.x, mousePos.y );
				
				if ( isGestureCapting )
					gestureUpdate( mousePos, rotationTorque );
				else {
					if ( father.getPosition().dst( vec2 ) < maximumDistanceToCenterAxis )
						gestureStart( mousePos );
				}
				
				return false;
				
			}
	} );
	}

	@Override
	public void update( float deltaTime ) {

		if ( isGestureCapting ) {
			
			if ( totalRotation > totalRotationToCompleteGesture )
				gestureComplete( isClockwise );
			
			if ( isDebugOn ) {
				String sss = "Adjacent: " + adjacent + "\n" + " Opposite: " + opposite;
				debug.screen(sss, (int)(father.spriteCenter.x - ( 
						(Debug.debug.font.getBounds(sss).width - father.currentFrameWidth)*.5f)), (int)father.spriteCenter.y - 30, false);
				
				sss = "Hypotenuse: " + hypotenuse;
				debug.screen(sss, (int)(father.spriteCenter.x - ( 
						(Debug.debug.font.getBounds(sss).width - father.currentFrameWidth)*.5f)), (int)father.spriteCenter.y - 40, false);
				
				sss = "cos: " + cosine + " " + " sin: " + sine;
				debug.screen(sss, (int)(father.spriteCenter.x - ( 
						(Debug.debug.font.getBounds(sss).width - father.currentFrameWidth)*.5f)), (int)father.spriteCenter.y - 50, false);
			
				sss = " cos dif: " + ( difference );
				debug.screen(sss, (int)(father.spriteCenter.x - ( 
						(Debug.debug.font.getBounds(sss).width - father.currentFrameWidth)*.5f)), (int)father.spriteCenter.y - 60, false);
				
				sss = "rotation direction: " + (( isClockwise ) ? "clockwise" : "anti-clockwise");
				debug.screen(sss, (int)(father.spriteCenter.x - ( 
						(Debug.debug.font.getBounds(sss).width - father.currentFrameWidth)*.5f)), (int)father.spriteCenter.y - 70, false);
				
				sss = "total rotation: " + totalRotation;
				debug.screen(sss, (int)(father.spriteCenter.x - ( 
						(Debug.debug.font.getBounds(sss).width - father.currentFrameWidth)*.5f)), (int)father.spriteCenter.y - 80, false);
				
				sss = "angle: " + current_angle;
				debug.screen(sss, (int)(father.spriteCenter.x - ( 
						(Debug.debug.font.getBounds(sss).width - father.currentFrameWidth)*.5f)), (int)father.spriteCenter.y - 90, false);
			}
		}
		
	}
	
	public void draw( SpriteBatch batch, float deltaTime ) {
		
		/*String sss = "Rotation: " + MathUtils.round( totalRotation );
		debug.screen(sss, (int)(father.spriteCenter.x - ( 
				(Debug.debug.font.getBounds(sss).width - father.currentFrameWidth)*.5f)), (int)father.spriteCenter.y - 30, false);*/
		
	}
	
	protected void gestureStart( Vector3 mousePos ) {
		
		if ( rotateLock ) // When rotation is locked
			return;
		
		vec2.set( mousePos.x, mousePos.y );
		position.set( father.getPosition() );
		
		adjacent = MathUtils.round( vec2.x - position.x );
		opposite = MathUtils.round( vec2.y - position.y );
		float adjacent2 = adjacent * adjacent;
		float opposite2 = opposite * opposite;
		float total = adjacent2 + opposite2;
		if ( total != 0 ) 
			hypotenuse = (float)Math.sqrt( total );
		else 
			hypotenuse = 0;
		updateTrigonometry();
		current_angle = MathUtils.atan2( opposite, adjacent ) * 180 / MathUtils.PI;
		current_angle = Math.abs( current_angle );
		
		cosine = MathUtils.round( cosine * 100 );
		
		isGestureCapting = true;
		
		delta_cos = cosine;
		delta_sin = sine;
		delta_tan = tangent;
		delta_angle = current_angle;
		
		difference = 0;
	}
	
	protected void gestureUpdate( Vector3 mousePos, float torque ) {
		
		vec2.set( mousePos.x, mousePos.y );
		position.set( father.getPosition() );
		
		adjacent = MathUtils.round( vec2.x - position.x );
		opposite = MathUtils.round( vec2.y - position.y );
		float adjacent2 = adjacent * adjacent;
		float opposite2 = opposite * opposite;
		float total = adjacent2 + opposite2;
		if ( total != 0 ) 
			hypotenuse = (float)Math.sqrt( total );
		else 
			hypotenuse = 0;
		
		updateTrigonometry();
		current_angle = MathUtils.atan2( opposite, adjacent ) * 180 / MathUtils.PI;
		current_angle = Math.abs( current_angle );
		
		cosine = MathUtils.round( cosine * 100 );
		boolean boolIntA = ( difference > 0 );
		difference = cosine - delta_cos;
		boolean boolIntB = ( difference > 0 );
		
		if ( delta_cos != cosine && boolIntA == boolIntB ) {
			
			boolean delta_isClockwise = isClockwise;
			if ( delta_sin > 0 ) { //check rotation direction
				isClockwise = ( difference > 0 );
			}
			else {
				isClockwise = ( difference < 0 );
			}
			if ( delta_isClockwise != isClockwise )
				totalRotation = 0;
			
			boolean rotationHasBeenAdded = false;
			if ( (isClockwise && allowClockwiseRotation) || (!isClockwise && allowCounterClockwiseRotation)  ) {
				if ( !father.touchMask.isOver(mousePos.x, mousePos.y) ) { // can't touch the TOUCH MASK 
					// the torque influence on the rotation
					float angleToAdd = Math.abs( MathUtils.round( current_angle - delta_angle ) ) * torque;
					totalRotation += Math.abs( angleToAdd );
					father.angleAdd( ( isClockwise ) ? -angleToAdd : angleToAdd );
					rotationAddLastUpdate = angleToAdd; // for checking purpose
					rotationHasBeenAdded = true;
//					Debug.log( "delta_angle: " + delta_angle + " | angle: " + current_angle + " | angleTo add: " + angleToAdd );
				}
			} 
			
			if ( !rotationHasBeenAdded ) // if the rotation has not been added in this cycle
				rotationAddLastUpdate = 0; // for checking update purpose
			
		}
		
		delta_cos = cosine;
		delta_sin = sine;
		delta_tan = tangent;
		delta_angle = current_angle;
		
		// Gesture ends if rotation is locked or finger distance to the father object center axis is more than the maximum allowed
		if ( rotateLock || father.getPosition().dst( vec2 ) > maximumDistanceToCenterAxis )
			gestureEnd();
		//Debug.log( "delta_cos: " + delta_cos + " | cosine: " + cosine + " | delta sine: " + delta_sin + " | difference: " + difference + " | isClockwise: " + isClockwise );
		
	}
	
	protected void gestureEnd() {
		isGestureCapting = false;
		rotationAddLastUpdate = 0;
		totalRotation = 0;
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
	
	private void updateTrigonometry() {
		
		int count = 0;
		
		if ( opposite != 0 ) { // set sine
			sine = opposite / hypotenuse;
			count++;
		}
		else 
			sine = 0;
		
		if ( adjacent != 0 ) { // set cosine
			cosine = adjacent / hypotenuse;
			count++;
		}
		else 
			cosine = 0;
			
		if ( count == 2 )
			tangent = opposite / adjacent; 
		
	}
	
	@Override
	public void reset() {
		if ( VirusType.TUTORIAL ) 
			circleArrowClockwise = MathUtils.randomBoolean();
		totalRotation = 0;
		relativeAngle = 0;
		isGestureCapting = false;
		father.setAngle( 0 );
		Debug.log( father.virus_id + " called reset() method | angle: " + father.getAngle() );
	}
	
}
