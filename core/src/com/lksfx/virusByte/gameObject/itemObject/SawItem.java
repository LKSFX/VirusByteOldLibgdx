package com.lksfx.virusByte.gameObject.itemObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.debug.Debug.SHAPE;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.BounceVirus.Edges;
import com.lksfx.virusByte.gameObject.abstractTypes.GameItem;
import com.lksfx.virusByte.gameObject.abstractTypes.HoldableType;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class SawItem extends HoldableType implements GameItem {
	
	//Bounce Attributes
	private boolean debug = false, debug_circle = false, debug_insect = true;
	private boolean inCollision;
	private Rectangle insect;
	private Circle circle;
	public Circle collisionPoint;
	public Vector2 movement = new Vector2(), dir = new Vector2(), velocityPush = new Vector2();//, dimensions = new Vector2();
	public boolean insideFrustrum = true, rotate = true;
	public float timeOutFrustrum = 5f, worldWidth = VIEWPORT.getWorldWidth(), worldHeight = VIEWPORT.getWorldHeight();
	private final int limitBorderBounces = 8;
	protected int totalBounces;
	
	//Saw attributes
	private Vector3 lastMousePos = Vector3.Zero.cpy();
	private float mouseDeltaDistance;
	private int rotationSpeed = 300;
	private Animation activeAnim;
	private boolean isActive;
	private TextureRegion icon;
	/**Determine how many enemies this saw can destroy*/
	public float  damageTime = 0, maxDamageCanReceive = .2f;
	
	public SawItem() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}
	
	public SawItem(float x, float y) {
		this(x, y, 0, 100, 250, new Color( Color.BLUE ), true);
	}

	public SawItem(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		virus_id = VirusInstance.Saw;
		isDefaultGrabBehaviour = true;
		insect = new Rectangle();
		circle = new Circle(getPosition(), currentFrameWidth * .35f);
		collisionPoint = new Circle(0, 0, 5f);
		isOnMove = false;
		
		//set trail attributes
		minimumDistanceToTheLastTrailPosition = 20f;
		setTrailSize( 20 );
	}
	
	@Override
	public void resetInPosition() {
		super.resetInPosition(WORLD_WIDTH * .5f, WORLD_HEIGHT * .5f);
	}
	
	@Override
	public void setAtlas() {
		AssetManager manager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] inactive = new AtlasRegion[1], active = new AtlasRegion[2];
		inactive[0] = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("saw-item0");
		active[0] = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("saw-item1");
		active[1] = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("saw-item2");
		AtlasRegion trail = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("saw-trail");
		icon = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("saw-icon");
		
		createAnim(active, trail, new Vector2(44, 44));
		activeAnim = getAnimation();
		
		createAnim(inactive, trail);
	}
	
	private boolean isGrabbed;
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		circle.setPosition( getPosition() );
		if ( isGrabbed ) {
			mouseDeltaDistance = distanceTo( mousePos, lastMousePos );
			dir.set(mousePos.x, mousePos.y).sub( lastMousePos.x, lastMousePos.y ).nor();
			lastMousePos.set(mousePos);
		}
		if (!VIEWPORT.getCamera().frustum.boundsInFrustum(x, y, 0f, currentFrameWidth * .5f, currentFrameHeight * .5f, 0f)) destroy();
		if ( damageTime > maxDamageCanReceive ) explode();
	}
	
	@Override
	public void movementUpdate(float deltaTime) {
		if ( !isOnMove ) return;
		movement.set( velocityPush ).scl( deltaTime );
		moveAdd( movement.x, movement.y );
		
        if ( isActive && rotate ) angleAdd( rotationSpeed * deltaTime );
        
        //Check for collision>>
        inCollision = false;
        VirusType coll = collisionDetection(true);
        if ( coll != null ) {
        	Debug.log(""+ coll.getPosition());
        	//==========================//
			//if ( coll.damageBySaw ) coll.sawDamage( this );
        	if ( !coll.sawDamage( this ) ) {
        		if ( coll.equals( this ) ) { // is collision with another saw object
        			SawItem otherSaw = (SawItem) coll;
        			detectCollision(otherSaw);
        		} else {
        			
        			Rectangle collision = coll.collisionMask.getRectangle();
        			if ( Intersector.intersectRectangles(collision, this.collisionMask.getRectangle(), insect) ) {
        				boolean vertical = false;
        				boolean horizontal = false;
        				boolean isLeft = false;
        				boolean isTop = false;
        				
        				if (insect.x == collision.x) {
        					horizontal = true;
        					isLeft = true;
        				} else if (insect.x + insect.width == collision.x + collision.width) {
        					horizontal = true;
        				}
        				if ( insect.y == collision.y ) {
        					vertical = true;
        				} else if (insect.y + insect.height == collision.y + collision.height) {
        					vertical = true;
        					isTop = true;
        				}
        				
        				if (horizontal && vertical) {
        					if (insect.width > insect.height) {
        						horizontal = false;
        					} else {
        						vertical = false;
        					}
        				}
        				Debug.log( "insect rect: " + insect.toString() );
        				Debug.log( "v = " + vertical + " | h = " + horizontal );
        				
        				if ( horizontal ) {
        					dir.scl(-1, 1);
        					if ( isLeft ) {
        						moveTo(collision.x - collisionMask.getRectangle().width * .5f, y);
        					} else {
        						moveTo(collision.x + collision.width + collisionMask.getRectangle().width * .5f, y);
        					}
        				} else if ( vertical ) {
        					dir.scl(1, -1);
        					if ( isTop ) {
        						moveTo(x, collision.y + collision.height + collisionMask.getRectangle().height * .5f);
        					} else {
        						moveTo(x, collision.y - collisionMask.getRectangle().height * .5f);
        					}
        				}
        				
        				set_direction();
        				lastEdgeContact = Edges.OTHER;
        				inCollision = true;
        			}
        		}
        	}
			//========================//
        }
        //<</
        bounces();
	}
	
	/**generally used when in collision with a boss that can't be destroyed by one collision call.
	 * this limits the saw to cause only a certain time of damage on the boss, then explode*/
	private void explode() {
		if ( VirusByteGame.SFX ) {
			// AUDIO TODO
		}
		VirusManager.PART_EFFECTS.createEffect(Assets.Particles.explosion, x, y, 30);
		destroy(true);
	}
	
	@Override
	public void grab() {
		super.grab();
		isOnMove = false;
		isGrabbed = true;
		isActive = false;
		setAnimation( anim_default );
		lastMousePos.set(mousePos);
	}
	
	@Override
	public void release() {
		super.release();
		randomize_direction();
		isOnMove = true;
		isGrabbed = false;
		isActive = true;
		setAnimation( activeAnim );
	}
	
	@Override
	public void onSlotOut() {
		super.onSlotOut();
		lastEdgeContact = Edges.NONE;
	}
	
	public void randomize_direction() {
		/*boolean outOfPort = ( (position.y + currentFrameHeight*.7f) > worldHeight );
		dir.set((outOfPort) ? 0f : MathUtils.random(-1f, 1f), (outOfPort) ? 1f : MathUtils.random(-1, 1f) );*/
		velocityPush.set( dir ).scl( speed = 66 * (int)Math.sqrt( mouseDeltaDistance ) );
		movement.set( velocityPush ).scl( Gdx.graphics.getDeltaTime() );
		rotationSpeed = (int) (speed * .8f);
		if ( debug ) {
			Debug.log( "mouseDeltaDistance: " + mouseDeltaDistance );
			Debug.log( "direction push: " + dir.toString() );
			Debug.log( "Velocity push: " + velocityPush.toString() );
		}
	}
	
	public void bounces() {
		if ( totalBounces >= limitBorderBounces ) return;
		float halfFrameWidth = ( currentFrameWidth * scaleX )*.5f, halfFrameHeight = ( currentFrameHeight * scaleY )*.5f;
		if (x+halfFrameWidth > WORLD_WIDTH) { right_bounce(); return; } //lateral 
		if (x-halfFrameWidth < 0) { left_bounce(); return; } //lateral
		if (y+halfFrameHeight > WORLD_HEIGHT) { top_bounce(); return; } //top
		if (y-halfFrameHeight < 0) { bottom_bounce(); return; } //bottom
	}
	
	protected Edges lastEdgeContact = Edges.NONE;
	
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
		/*movement.set( velocityPush ).scl( Gdx.graphics.getDeltaTime() );
		moveAdd( movement );*/
		if ( debug ) Debug.log( "velocityPush after: " + velocityPush.toString());
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
	
	private Color shapeColor = new Color(1f, 1f, 1f, .65f);
	private Color collisionPointColor = new Color(1f, 0f, 0f, .75f);
	
	@Override
	public void debug_drawings() {
		super.debug_drawings();
		
//		Debug.log( collisionMask.getRectangle().width + " | " + collisionMask.getRectangle().height );
//		Debug.log( " current scale: " + currentScale );
		
		if ( debug_circle ) {
			Debug.debug.insertShapeToRender(SHAPE.CIRCLE, circle.x, circle.y, circle.radius, circle.radius, shapeColor);
			if ( inCollision ) 
				Debug.debug.insertShapeToRender(SHAPE.CIRCLE, collisionPoint.x, collisionPoint.y, collisionPoint.radius, collisionPoint.radius, collisionPointColor);
		}
		if ( debug_insect ) {
			Debug.debug.insertShapeToRender(SHAPE.RECTANGLE, insect.x, insect.y, insect.width, insect.height, collisionPointColor);
		}
	}
	
	@Override
	public TextureRegion getIcon() {
		return icon;
	}
	
	/**Collision detection with another circle*/
	public void detectCollision(SawItem otherSaw) {
		//>>
		float x = this.x, y = this.y, rad = circle.radius;
		float otherX = otherSaw.x, otherY = otherSaw.y, otherRad = otherSaw.circle.radius;
		
		if (x + rad + otherRad > otherX
		&& x < otherX + rad + otherRad
		&& y + rad + otherRad > otherY
		&& y < otherY + rad + otherRad) {
			if ( distanceTo(this, otherSaw) < rad + otherRad ) {
				setCollisionPoint(this, otherSaw);
				calculateNewVelocity(this, otherSaw);
			}
		}
		//<</
	}
	
	public float distanceTo(SawItem a, SawItem b) {
		float distance = Vector2.dst(a.x, a.y, a.x, a.y);
		if ( distance < 0 ) distance = distance * -1; 
		return distance;
	}
	
	private void setCollisionPoint(SawItem a, SawItem b) {
		inCollision = true;
		
		float 
		x1 = a.x,
		x2 = b.x,
		y1 = a.y,
		y2 = b.y,
		r1 = a.circle.radius,
		r2 = b.circle.radius;
		
		collisionPoint.setPosition( (x1 * r2 + x2 * r1) / (r1 + r2) , (y1 * r2 + y2 * r1) / (r1 + r2) );
	}
	
	private void calculateNewVelocity(SawItem a, SawItem b) {
		float 
		mass1 = a.circle.radius,
		mass2 = b.circle.radius,
		velX1 = a.dir.x,
		velX2 = b.dir.x,
		velY1 = a.dir.y,
		velY2 = b.dir.y;
		
		float 
		newVelX1 = (velX1 * (mass1 - mass2) + (2 * mass2 * velX2)) / (mass1 + mass2),
		newVelX2 = (velX2 * (mass2 - mass1) + (2 * mass1 * velX1)) / (mass1 + mass2),
		newVelY1 = (velY1 * (mass1 - mass2) + (2 * mass2 * velY2)) / (mass1 + mass2),
		newVelY2 = (velY2 * (mass2 - mass1) + (2 * mass1 * velY1)) / (mass1 + mass2);
		Gdx.app.log("Physics", "" + (velX1 * (mass1 - mass2)) );
		Gdx.app.log("Physics", "" + (2 * mass1 * velX2) );
		Gdx.app.log("Physics", "" + newVelX1 );
		float s = 0/20;
		Gdx.app.log("Physics", "" + s);
		
		a.dir.x = newVelX1;
		a.dir.y = newVelY1;
		if ( a.lastEdgeContact != Edges.OTHER ) a.lastEdgeContact = Edges.OTHER;
		a.dir.nor();
		b.dir.x = newVelX2;
		b.dir.y = newVelY2;
		if ( b.lastEdgeContact != Edges.OTHER ) b.lastEdgeContact = Edges.OTHER;
		b.dir.nor();
		a.set_direction();
		b.set_direction();
		
		a.movement.set( a.velocityPush ).scl( Gdx.graphics.getDeltaTime() );
		a.moveAdd( a.movement.x, a.movement.y );
		b.movement.set( b.velocityPush ).scl( Gdx.graphics.getDeltaTime() );
		b.moveAdd( b.movement.x, b.movement.y );
	}
	
	@Override
	public void reset() {
		super.reset();
		damageTime = 0;
	}
}
