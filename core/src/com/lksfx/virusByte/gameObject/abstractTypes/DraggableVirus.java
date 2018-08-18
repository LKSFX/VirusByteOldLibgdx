package com.lksfx.virusByte.gameObject.abstractTypes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;

public abstract class DraggableVirus extends VirusType {
	
	protected Vector2 velocityPush = new Vector2();  
	protected Vector2 movement = new Vector2();  
	protected Vector2 dir = new Vector2();
	protected TextureRegion pointer;
	
	protected float pointerSize = 1f;
	protected Vector3 lastMousePos = Vector3.Zero;
	protected int pushPower;
	protected int mouseDeltaDistance;
	protected boolean pushed = false;
	public boolean grabbed = false;
	protected static boolean DEBUG = false; //set debug mode ON or OFF in this virus
	
	protected int velMuitiplier;
	protected float grabLimit = .4f, grabTime, grabDistanceLimit;
	
	protected float last_x = 0f, last_y = 0f;
	/**the position where the drag begin*/
	protected Vector2 dragOriginPosition;
	
	public DraggableVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}
	
	public DraggableVirus(float x, float y) {
		this(x, y, 5, 150, 350, new Color( Color.WHITE ), true);
	}
	
	public DraggableVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		velMuitiplier = 5;
		dragOriginPosition = new Vector2();
		grabDistanceLimit =  150f;
		allowScaleOnTouchMask = false;
		if ( DEBUG ) 
			Debug.log("grabDistanceLimit " + grabDistanceLimit);
		setInputController( new InputTriggers() {
			@Override
			public boolean draggedTouch(Vector3 mousePos) {
				if ( grabbed ) {
					if (grabTime > grabLimit || dragOriginPosition.dst(DraggableVirus.this.x, DraggableVirus.this.y) > grabDistanceLimit) {
						grabbed = false;
						setDepth( 5 );
						setFlashOn(Color.WHITE, .65f);
					}
					moveTo( lastMousePos.x, lastMousePos.y );
					
					mouseDeltaDistance = distanceTo(mousePos, lastMousePos);
					if (mouseDeltaDistance > pushPower) {
//						int push = ( mouseDeltaDistance > 6 ) ? (( mouseDeltaDistance > pushPower ) ? mouseDeltaDistance : pushPower) : 0;
						pushPower = mouseDeltaDistance;
						dir.set(mousePos.x, mousePos.y).sub( DraggableVirus.this.x, DraggableVirus.this.y ).nor();
					}
					//pushPower = mouseDeltaDistance > pushPower ? mouseDeltaDistance : pushPower;
					if (DEBUG) Debug.log("clicked over infector");
				
				
					grabTime += Gdx.graphics.getDeltaTime(); 
				
					//Debug.log("distance from last mousePos: " + mouseDeltaDistance + " | record: " + record);
					lastMousePos.set( mousePos );
				}
				HOLDING_VIRUS = grabbed;
				return false;
			}
			@Override
			public boolean pressedTouch(Vector3 mousePos) { //click unico
				if ( HOLDING_VIRUS ) return false;
				if ( isOver( mousePos ) ) {
					if ( !grabbed ) dragOriginPosition.set( DraggableVirus.this.x, DraggableVirus.this.y ); //update the origin of the drag
					grabbed = true;
					setDepth( 15 );
					mouseDeltaDistance = 0;
					lastMousePos.set( mousePos );
					grabTime = 0;
					if ( isTrailOn ) {
						for ( TrailIndex trail : trailList ) trailPool.free(trail);
						trailList.clear();
					}
					return true;
				}
				return false;
			}
			@Override
			public boolean releaseTouch(Vector3 mousePos) {
				if ( grabbed ) {
					if (pushPower > 15) {
						//move = true;
						float spd = (float)pushPower * velMuitiplier;
						//logMsg = new PointLog(point, position.x, position.y, true, virus_id, true, comboInterval);
						last_x = DraggableVirus.this.x; //record last x position before be pushed off the screen
						last_y = DraggableVirus.this.y; //record last y position before be pushed off the screen
						if (DEBUG) Debug.log("infector push speed: "+spd);
						setTrailSize( Math.min(MathUtils.round(spd/20), 20) );
						velocityPush.set( dir ).scl( spd );
						movement.set(velocityPush).scl(Gdx.graphics.getDeltaTime());
						pushed = true;
						DraggableVirus.this.speed = (int)spd;
					}
					setDepth( 5 );;
					grabbed = false;
					isOnMove = true;
					last_x = DraggableVirus.this.x; //record last x position before be pushed off the screen
					last_y = DraggableVirus.this.y; //record last y position before be pushed off the screen
					HOLDING_VIRUS = false;
				}
				return false;
			}
		} );
	}
	
	@Override
	public void draw(SpriteBatch batch,float deltaTime) {
		if ( !isVisible ) return;
		if ( TUTORIAL ) {
			if ( !pushed ) {
				batch.setColor(1f, 1f, 1f, .65f);
				pointerSize += pointerSize > 3f ? -pointerSize : .05f;
				float xx = x-64f;
				//batch.draw(pointer, xx, position.y-16f, 64f, 16f, 64f, 32f, 1f, 1f, 0f);
				batch.draw(pointer, xx, y-16f, 64f, 16f, 64f, 32f, Math.min(pointerSize, 2f), 1f, 325f);
				batch.draw(pointer, xx, y-16f, 64f, 16f, 64f, 32f, Math.min(pointerSize, 2f), 1f, 270f);
				batch.draw(pointer, xx, y-16f, 64f, 16f, 64f, 32f, Math.min(pointerSize, 2f), 1f, 215f);
			}
		}
		super.draw(batch, deltaTime);
		/*debug_drawings(batch);
			
		if (anim != null) {
			batch.setColor(trailColor);
			for (int i = trailList.size-1; i > 0; i--) {
				float inc = i * .001f;
				trailColor.a = inc > 0.5f? 0.5f : inc;
				batch.draw(trail, trailList.get(i).xx, trailList.get(i).yy, trailList.get(i).centerOriginX, trailList.get(i).centerOriginY,
						trailList.get(i).width, trailList.get(i).height, trailList.get(i).scaleX, trailList.get(i).scaleY, trailList.get(i).angle); //draw trail
			}
			trailColor.a = 0.3f;
			batch.setColor(spriteColor);
			batch.draw(anim.getKeyFrame(elapsedTime, true), spriteCenter.x, spriteCenter.y, currentOriginCenterX, currentOriginCenterY, 
					currentFrameWidth, currentFrameHeight, scaleX, scaleY, angle);
		}*/
	}
	
	@Override
	public void movementUpdate(float deltaTime) {
		if ( pushed ) {
			// when the virus has been grabbed and pushed
			isOnMove = true;
			movement.set( velocityPush ).scl(deltaTime * 3);
			
			moveAdd( movement.x, movement.y );
            
			VirusType coll = collisionDetection(true);
	        if (coll != null) {
	        	if (DEBUG) Debug.log(""+ coll.getPosition());
	        	collision(coll);
	        }
	        if ( !VIEWPORT.getCamera().frustum.boundsInFrustum(x, y, 0f, currentFrameWidth, currentFrameHeight, 0f) ) {
	        	destroy(true, true);
	        }
		} else {
			if (isOnMove) {
				moveTo( x, y -= deltaTime * speed );
			}
		}
	}
	
	protected abstract void collision(VirusType coll);
	
	@Override
	public void updateTrail() {
		if ( grabbed ) return;
		super.updateTrail();
	}
	
	@Override
	public void resetInPosition(float posX, float posY) {
		if (TUTORIAL) pointerSize = 1f;
		super.resetInPosition(posX, posY);
		minimumDistanceToTheLastTrailPosition = 5f;
	}
	
	// ========================== //
	// =======  GETTERS  ======== //
	// ========================== //
	
	public float getPushPower() {
		return pushPower;
	}
	
	// ========================== //
	// ========================== //
	
	@Override
	public void reset() {
		super.reset();
		last_x = 0f;
		last_y = 0f;
		pushed = false;
		grabbed = false;
		pushPower = 0;
	}
}
