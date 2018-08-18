package com.lksfx.virusByte.gameObject.abstractTypes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.lksfx.virusByte.gameControl.debug.Debug;

public abstract class BounceVirus extends VirusType {
	private boolean debug = false;
	public Vector2 movement = new Vector2(), dir = new Vector2(), velocityPush = new Vector2();//, dimensions = new Vector2();
	public boolean insideFrustrum = true, rotate = true;
	public float timeOutFrustrum = 5f, worldWidth = VIEWPORT.getWorldWidth(), worldHeight = VIEWPORT.getWorldHeight();
	protected int totalBounces;
	
	public BounceVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}
	
	public BounceVirus(float x, float y) {
		this(x, y, 10, 200, 350, new Color(0f, 0f, 1f, 0.3f));
	}
	
	public BounceVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor) {
		super(x, y, point, minSpd, maxSpd, trailColor, true);
		trailAlphaIncrement = .045f;
		trailMaxAlpha = .8f;
	}
	
	public void bounces() {
		/*
		 * Deprecated part of code
		 * if (position.x+dimensions.x > worldWidth) { right_bounce(); return; } //lateral 
		if (position.x-dimensions.x < 0) { left_bounce(); return; } //lateral
		if (position.y+dimensions.y > worldHeight) { top_bounce(); return; } //top
		if (position.y-dimensions.y < 0) { bottom_bounce(); return; } //bottom
		*/	
		float halfFrameWidth = ( currentFrameWidth * scaleX )*.5f, halfFrameHeight = ( currentFrameHeight * scaleY )*.5f;
		if (x+halfFrameWidth > WORLD_WIDTH) { right_bounce(); return; } //lateral 
		if (x-halfFrameWidth < 0) { left_bounce(); return; } //lateral
		if (y+halfFrameHeight > WORLD_HEIGHT) { top_bounce(); return; } //top
		if (y-halfFrameHeight < 0) { bottom_bounce(); return; } //bottom
	}
	
	protected Edges lastEdgeContact = Edges.NONE;
	public enum Edges { RIGHT, LEFT, TOP, BOTTOM, OTHER, NONE }
	
	public void top_bounce() {
		if (lastEdgeContact == Edges.TOP) return;
		if ( debug ) {
			Debug.log( "BOUNCE!" );
			Debug.log( "direction before: " + dir.toString());
		}
		dir.y = -dir.y;
		set_direction();
		lastEdgeContact = Edges.TOP;
		totalBounces++;
		if ( debug ) Debug.log( "direction after: " + dir.toString());
	}
	
	public void bottom_bounce() {
		if (lastEdgeContact == Edges.BOTTOM) return; 
		if ( debug ) {
			Debug.log( "BOUNCE!" );
			Debug.log( "direction before: " + dir.toString());
		}
		dir.y = -dir.y;
		set_direction();
		lastEdgeContact = Edges.BOTTOM;
		totalBounces++;
		if ( debug ) Debug.log( "direction after: " + dir.toString());
	}
	
	public void left_bounce() {
		if (lastEdgeContact == Edges.LEFT) return; 
		if ( debug ) {
			Debug.log( "BOUNCE!" );
			Debug.log( "direction before: " + dir.toString());
		}
		dir.x = -dir.x;
		set_direction();
		lastEdgeContact = Edges.LEFT;
		totalBounces++;
		if ( debug ) Debug.log( "direction after: " + dir.toString());
	}
	
	public void right_bounce() {
		if (lastEdgeContact == Edges.RIGHT) return;
		if ( debug ) {
			Debug.log( "BOUNCE!" );
			Debug.log( "direction before: " + dir.toString());
		}
		dir.x = -dir.x;
		set_direction();
		lastEdgeContact = Edges.RIGHT;
		totalBounces++;
		if ( debug ) Debug.log( "direction after: " + dir.toString());
	}
	
	public void set_direction() {
		if ( debug ) Debug.log( "velocityPush before: " + velocityPush.toString());
		velocityPush.set( dir ).scl( speed );
		if ( debug ) Debug.log( "velocityPush after: " + velocityPush.toString());
	}
	
	public abstract void randomize_direction();
	
	@Override
	public void movementUpdate(float deltaTime) {
		if ( !isOnMove ) return;
		movement.set( velocityPush ).scl( deltaTime );
		moveAdd( movement.x, movement.y );
        
        if ( rotate ) 
        	angleAdd( 3f ); 
        /*VirusType coll = collisionDetection(true);
        if (coll != null) {
        	//Debug.log(""+ coll.position);
        	//if (!coll.equals(this)) coll.destroy(true);
        }*/
        bounces();
        //Debug.debug.screen("dir.x: " + dir.x + " | dir.y: " + dir.y, (int)bounds.getX(), (int)bounds.getY() - 30, false);
//        Debug.debug.screen("dir.x: " + dir.x + " | dir.y: " + dir.y, (int) (worldWidth*.25f), (int) (worldHeight*.5f - 30), false);
	}
	
	public Edges getLastEdgeContact() {
		return lastEdgeContact;
	}
	
	@Override
	public void resetInPosition(float posX, float posY) {
		super.resetInPosition( posX, posY );
		randomize_direction();
	}
	
	@Override
	public void setSpeed(int min, int max) {
		super.setSpeed(min, max);
		if ( debug ) Debug.log( "Update on speed: " + speed );
		if ( dir != null && velocityPush != null ) set_direction(); // Update speed of the direction movement
	}
	
	@Override
	public void updateTrail() {
		if ( !TRAILS_ACTIVE || !isTrailOn ) return;
		if (trail == null) return;
		if ( !lastMovementDirection.isCollinear( dir ) ) {
			//Debug.log("is not same direction, reset vectors");
			lastMovementDirection.set( dir );
			lastTrailPosition.set( x, y );
		}
		int currentTrailSize = trailList.size;
		if (trailList.size == 0) {
			TrailIndex trail = trailPool.obtain();
			if ( trail != null ) {
				trail.set(spriteCenter.x, spriteCenter.y, currentOriginCenterX, currentOriginCenterY,
						currentFrameWidth, currentFrameHeight, scaleX, scaleY, getAngle());
				trailList.add(trail);
				lastTrailPosition.set( x, y );
			}
		} else {
			float distanceToLastPosition = Vector2.dst(lastTrailPosition.x, lastTrailPosition.y, x, y);
			if ( distanceToLastPosition > minimumDistanceToTheLastTrailPosition ) {
				TrailIndex trail = trailPool.obtain();
				if ( trail != null ) {
					trail.set(spriteCenter.x, spriteCenter.y, currentOriginCenterX, currentOriginCenterY,
							currentFrameWidth, currentFrameHeight, scaleX, scaleY, getAngle());
					trailList.add(trail);
				}
				lastTrailPosition.set( x, y );
			}
			//Debug.log("distance to last position " + distanceToLastPosition);
		}
		if ( currentTrailSize > trailSize) {
			trailPool.free( trailList.removeIndex(0) );
		}
	}
	
	@Override
	public void reset() {
		super.reset();
		lastEdgeContact = null;
		velocityPush.setZero();
		dir.setZero();
		totalBounces = 0;
		lastEdgeContact = Edges.NONE;
	}
}
