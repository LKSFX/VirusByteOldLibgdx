package com.lksfx.virusByte.gameObject.abstractTypes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.ReflectionPool;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.Backgrounds;
import com.lksfx.virusByte.effects.Immersion;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.debug.Debug.SHAPE;
import com.lksfx.virusByte.gameControl.hud.GameHud;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.DetectionMask.MaskFormat;
import com.lksfx.virusByte.gameObject.itemObject.BombItem;
import com.lksfx.virusByte.gameObject.itemObject.Coin;
import com.lksfx.virusByte.gameObject.itemObject.CoinSmall;
import com.lksfx.virusByte.gameObject.itemObject.SawItem;
import com.lksfx.virusByte.gameObject.pontuation.PointLog;
import com.lksfx.virusByte.gameObject.pontuation.ScreenLogger;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner.SpawnableConfiguration;

public abstract class VirusType extends GameObject implements Comparable<VirusType>, Pool.Poolable {
	/**The sprite of this virus*/
	public TextureAtlas sprite;
	/**The trail texture*/
	public TextureRegion trail;
	
	/**ListenerList for anonymous actions*/
	protected Array<ActionListener> 
		onDraw = new Array<VirusType.ActionListener>(), 
		onDestroy = new Array<VirusType.ActionListener>(),
		onReset = new Array<VirusType.ActionListener>(),
		onUpdate = new Array<VirusType.ActionListener>(); 
	
	public Assets.Particles finalEffectType = Assets.Particles.firework;
	public boolean alive = true, animationLoop = true;
	
//	public Vector2 spriteCenter;
	/** This object define the behavior of this virus */
	private VirusCore< ? extends VirusType > core;
	/** This object can be set only once */
	private VirusCore< ? extends VirusType > defaultCore;
	
	/** trail array, store information about positions*/
	public Array<TrailIndex> trailList; //values used to make a trail for the virus
	
	/** reflection pool that contains and store trail objects */
	public ReflectionPool<TrailIndex> trailPool;
	
	/**configuration object*/
//	private static VirusConfig VIRUS_CONFIG_OBJECT = new VirusConfig();
	
	/**when this field is true the virus is removed silently from the mainStage and draw*/
	public boolean toRemove;
	
	/**identify this virus on the virus manager and on point manager*/
	public VirusManager.VirusInstance virus_id;
	
	public Animation anim_default;
	public int speed = 100, trailSize, minSpd = 80, maxSpd = 150, point = 100, imm = Immersion.BUMP_100;
	public Color spriteColor, trailColor;
	public static boolean COLLISION_MASK_DEBUG = false, TOUCH_MASK_DEBUG = false, DEBUG_SLASH_LINE = false, TRAILS_ACTIVE = true;
	/**Determine if has some virus being in hold*/
	public static boolean HOLDING_VIRUS;
	/**When true, show a line between nearest virus*/
	public static boolean DEBUG_NEAREST = false;
	public float /*elapsedTime = 0, angle = 0f, scaleX = 1f, scaleY = 1f,*/
			comboInterval = 1f;
	public boolean isOnMove = true, isCollidable = true, isDamagedBySaw = true, isDamagedByExplosions = true, isDamagedByCollision = true, isVirusCollidable = true,
			isDamagedByLaser = true, isReached = false, isMoneyBack, isTrailOn, isFinale = false, hit = false, canBeFree = true, isComputeKillOn = true, 
			isDestroyedOnBottom = true, isHostile = true;
	
	/**the quantity that increment/decrement on trail alpha every frame*/
	protected float trailAlphaIncrement = .075f, trailMaxAlpha = .5f;
	
	public static Vector2 SLASH_ORIGIN, SLASH;
	
	/**determine if when the sprite is scaled the touch mask will scalar proportionally*/
	public boolean allowScaleOnTouchMask = true;
	
	public enum Reward {SMALL, LARGE}
	private Reward rewardSize = Reward.LARGE;
	
	public static float WORLD_WIDTH, WORLD_HEIGHT;
	public static VirusManager VIRUS_MANAGER;
	public static Viewport VIEWPORT;
	public static GameHud HUD;
	public static Backgrounds BACK;
	public static Vector3 MOUSE_POS;
	public static int LIFES, GLOBAL_MAX_TRAIL_SIZE;
	public static boolean ACTIVE, TUTORIAL, BOSS_TIME;
	
	public static void initialize(VirusManager virus_manager, Viewport viewport, Backgrounds back, GameHud hud, Vector3 mousePos, boolean active, int lives) {
		VIRUS_MANAGER = virus_manager;
		VIEWPORT = viewport;
		BACK = back;
		HUD = hud;
		MOUSE_POS = mousePos;
		WORLD_WIDTH = viewport.getWorldWidth();
		WORLD_HEIGHT = viewport.getWorldHeight();
		ACTIVE = active;
		LIFES = lives;
		Preferences pref = Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE);
		GLOBAL_MAX_TRAIL_SIZE = pref.getInteger("trails_max_length", 0);
		TRAILS_ACTIVE = pref.getBoolean("trails_active", true);
		SLASH_ORIGIN = new Vector2(0, 0);
		SLASH = new Vector2(0, 0);
	}
	
	public VirusType(float x, float y) {
		this( x, y, 100, 80, 150, new Color( Color.WHITE ), true );
		moveTo( x, y );
	}
	
	/**
	 *  Create an game object like item or virus  for act in the game
	 * @param x position to start
	 * @param y position to start
	 * @param point earned when this object is destroyed 
	 * @param minSpd min object speed
	 * @param maxSpd max object speed
	 * @param trailColor trail color
	 * @param trailOn trail is active or not for this object
	 */
	public VirusType(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		initialize(x, y, point, minSpd, maxSpd, trailColor, trailOn);
	}
	
	/**Determine the number of already created viruses*/
	private static int SPAWNER_NUMBER;
	/**Unique number identification*/
	public final int id = SPAWNER_NUMBER++;
	
	/****/
//	private VirusConfig configuration;
	protected ActionListener configuration;
	
	public void initialize(float x, float y, int point, final int minSpd, final int maxSpd, Color trailColor, boolean trailOn) {
		this.point = point;
		this.minSpd = minSpd;
		this.maxSpd = maxSpd;
		this.trailColor = trailColor;		
		this.isTrailOn = trailOn;
//		configuration = new VirusConfig(minSpd, maxSpd, true);
		configuration = new ActionListener(true) {
			private int min = minSpd, max = maxSpd;
			@Override
			public void execute(VirusType virus) {
				virus.setSpeed(min, max);
				if ( virus.isOnMove ) virus.isOnMove = !( (min & max) == 0 );
//				Debug.log( virus.virus_id + " configuration method executed!" );
			}
		};
		addActionListener(ACTION.ON_RESET, configuration);
		
		shader = VirusByteGame.UTIL.flashShader;
		spriteColor = new Color( Color.WHITE );
		spriteCenter = new Vector2();
		
		lastMovementDirection = new Vector2();
		lastTrailPosition = new Vector2();
		
		setAtlas();
//		setBounds(x, y);
		if (trailOn) { 
			trailList = new Array<VirusType.TrailIndex>();
			trailPool = new ReflectionPool<VirusType.TrailIndex>(TrailIndex.class);
		}
		setSpeed();
		setDepth( 5 );
		setVirusCore( new VirusCore< VirusType >( this ) {
			@Override
			public void update(float deltaTime) {
				
			}
		} );
	}
	
	//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>//
	public void createAnim(TextureRegion[] regions, AtlasRegion trail) {
		Vector2 mask = new Vector2(regions[0].getRegionWidth(), regions[0].getRegionHeight()); //mask same size of the texture frame 0
		createAnim(regions, trail, mask, mask);
	}
	
	public void createAnim(TextureRegion[] regions, AtlasRegion trail, float animSpeed) {
		Vector2 mask = new Vector2(regions[0].getRegionWidth(), regions[0].getRegionHeight()); //mask same size of the texture frame 0
		createAnim(regions, trail, animSpeed, mask, mask);
	}
	
	public void createAnim(TextureRegion[] regions, AtlasRegion trail, AtlasRegion maskRegion) {
		Vector2 mask = new Vector2(maskRegion.getRegionWidth(), maskRegion.getRegionHeight()); //mask same size of the specified texture region
		createAnim(regions, trail, mask, mask);
	}
	
	public void createAnim(TextureRegion[] regions, AtlasRegion trail, Vector2 collisionMask) {
		createAnim(regions, trail, collisionMask, collisionMask); // use the same collision mask as touch mask
	}
	
	public void createAnim(TextureRegion[] regions, AtlasRegion trail, Vector2 collisionMask, Vector2 touchMask) {
		createAnim(regions, trail, 1/15f, collisionMask, touchMask);
	}
	
	public void createAnim(TextureRegion[] regions, AtlasRegion trail, float animSpeed, Vector2 collisionMask, Vector2 touchMask) {
		Array<TextureRegion> keyFrames = new Array<TextureRegion>();
		for (TextureRegion region : regions) 
			keyFrames.add(region);
		
		anim_default = new Animation(animSpeed, keyFrames);
		anim_default.setPlayMode( PlayMode.LOOP );
		setAnimation( anim_default );
		
		spriteMask.setSize(regions[0].getRegionWidth(), regions[0].getRegionHeight());
		spriteMask.setPosition( x, y );
		spriteUpdate( 0 ); //update to avoid currentFrame width/height information with zeros 
		
		setCollisionMaskBounds(0, 0, collisionMask.x, collisionMask.y);
		setTouchMaskBounds(0, 0, touchMask.x, touchMask.y);
		if (!isTrailOn) return;
		if (trail != null) this.trail = trail;
	}
	
	//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<//
	
/*	public void setAnimation(float blind_animation_change) {
		if (anim_blind != null) {
			if (MathUtils.randomBoolean(blind_animation_change)) {
				anim = anim_blind;
				blind = true;
			} else {
				anim = anim_default;
				blind = false;
			}
		}
	}*/
	
	/**set alpha for this virus sprite*/
	public void setAlpha(float alpha) {
		if (alpha > 1f) alpha = 1f;
		if (alpha < 0f) alpha = 0f;
		this.spriteColor.a = alpha;
	}
	
	/**set sprite scale homogeneous*/
	public void setScale(float scale) {
		setScale(scale, scale);
	}
	
	public void setScale(float scaleX, float scaleY) {
		scaleX = (scaleX < 0f) ? 0f : scaleX;
		scaleY = (scaleY < 0f) ? 0f : scaleY;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		setCollisionMaskScale( scaleX, scaleY );
	}
	
	
	public void setSpeed() {
		setSpeed(minSpd, maxSpd);
	}
	
	public void setSpeed(int spd) {
		setSpeed(spd, spd);
	}
	
	public void setSpeed(int min, int max) {
		speed = (min != max) ? MathUtils.random(min, max) : max;
		if (!isTrailOn) return;
		setTrailSize( Math.min(MathUtils.round(speed/30), 5) );
		//if ( GLOBAL_MAX_TRAIL_SIZE != 0 && trailSize > GLOBAL_MAX_TRAIL_SIZE ) trailSize = GLOBAL_MAX_TRAIL_SIZE;
		if ( trailList != null ) {
			trailPool.freeAll( trailList );
			trailList.clear();
		}
		//Debug.log("virusSpeed: " + speed);
	}
	
	/** Set this virus size, based on it the size of coin can be determined when this virus is {{@link #isMoneyBack} is <b>true</b> */
	public void setRewardSize( Reward size ) {
		this.rewardSize = size;
	}
	
	protected void setTrailSize( int length ) {
		if ( GLOBAL_MAX_TRAIL_SIZE != 0 && length > GLOBAL_MAX_TRAIL_SIZE ) length = GLOBAL_MAX_TRAIL_SIZE;
		trailSize = length;
		//Debug.log( "trail size is " + trailSize );
	}
	
	/** when an object is set indestrutible > true.
	 *  it can't suffer any type of damage like by bombs, saw, collision, or even laser */
	public void setIndestructible(boolean bool) {
		isDamagedByCollision = !bool;
		isDamagedByExplosions = !bool;
		isDamagedByLaser = !bool;
		isDamagedBySaw = !bool;
	}
	
	//========================================//
	//set bounds
	// COLLISION MASK //
	/**change the collision mask size by the texture size passed*/
	public void setCollisionMaskBounds(TextureRegion mask) {
		float width = mask.getRegionWidth(), height = mask.getRegionHeight();
		setCollisionMaskBounds(width, height);
	}
	
	/**change the collision mask size*/
	public void setCollisionMaskBounds(float width, float height) {
		collisionMask.setSize( width, height );
		setCollisionMaskScale( scaleX, scaleY );
	}
	
	/** @param xOffset the horizontal offset of the collision mask in relation of the center of the sprite
	 * @param yOffset the vertical offset of the collision mask in relation of the center of the sprite
	 * @param width 
	 * @param height
	 */
	public void setCollisionMaskBounds(float xOffset, float yOffset, float width, float height) {
		collisionMask.setOffset( xOffset, yOffset );
		collisionMask.setSize( width, height );
		setCollisionMaskScale( scaleX, scaleY );
	}
	
	public void setCollisionMaskScale(float scaleX, float scaleY) {
		collisionMask.setScale( scaleX, scaleY );
	}
		
	//============================================//
	// TOUCH MASK //
	
	/**change the collision mask size by the texture size passed*/
	public void setTouchMaskBounds(TextureRegion mask) {
		float width = mask.getRegionWidth(), height = mask.getRegionHeight();
		setTouchMaskBounds(width, height);
	}
	
	/**change the collision mask size*/
	public void setTouchMaskBounds(float width, float height) {
		touchMask.setSize(width, height);
		setTouchMaskScale(scaleX, scaleY);
	}
	
	/** @param xOffset the horizontal offset of the collision mask in relation of the center of the sprite
	 * @param yOffset the vertical offset of the collision mask in relation of the center of the sprite
	 * @param width of {@link #touch_mask_size } 
	 * @param height of {@link #touch_mask_size } 
	 */
	public void setTouchMaskBounds(float xOffset, float yOffset, float width, float height) {
		touchMask.setOffset( xOffset, yOffset );
		touchMask.setSize(width, height);
		setTouchMaskScale(scaleX, scaleY);
	}
	
	public void setTouchMaskScale(float scaleX, float scaleY) {
		if ( allowScaleOnTouchMask )
			touchMask.setScale( scaleX, scaleY );
	}
	
	/** Set this virus behavior 
	 * @param virusCore if null and {@link #defaultCore} is not null, default core is set*/
	public void setVirusCore( VirusCore< ? extends VirusType > virusCore ) {
		if ( virusCore != null ) {
			this.core = virusCore;
			this.core.initialize();
		}
		else if ( defaultCore != null )
			this.core = defaultCore;
	}
	
	/** Can be set only once, set this as default and current core */
	public void setDefaultVirusCore( VirusCore< ? extends VirusType > virusCore ) {
		if ( defaultCore == null )
			defaultCore = virusCore;
		setVirusCore( virusCore );
	}
	
	public VirusCore< ? extends VirusType > getDefaultVirusCore() {
		return defaultCore;
	}
	
	//================================//
	
	public enum Offset {LEFT_BOTTOM, CENTER}
	/**offset generally used to collision mask is default centralized*/
	public Offset offset = Offset.CENTER;
	
	//=========================================//
	
	public void moveTo(Vector2 vec2) {
		moveTo(vec2.x, vec2.y);
	}
	
	public abstract void setAtlas();
	
	public static ShapeRenderer shapeRender = new ShapeRenderer(); //actually used only for debug mode
	// >>>>>>>>>> FLASH EFFECT >>>>>>>>>>>>>>>>
	/**Make colored effects with this shader*/
	protected ShaderProgram shader;
	/**Determine when flash effect occurs*/
	protected boolean flashing;
	/**Determine the color of the flash effect*/
	protected Color flsColor = new Color( Color.WHITE );
	/**Timers to count during flash effect*/
	protected float flsTime, flsInterval= .1f, flsDuration = 1.3f, flsMainTime;
	
	/**enable the flash effect on this virus with default color white*/
	public void setFlashOn() {
		setFlashOn( Color.WHITE, 1.3f );
	}
	
	/**enable the flash effect on this virus
	 * @param duration how many time the flash effect will render*/
	public void setFlashOn(Color color, float duration) {
		flsColor.set(color.r, color.g, color.b, spriteColor.a );
		flsDuration = duration;
		flsMainTime = 0;
		flashing = true;
	}
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	
	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		if ( !isVisible ) 
			return;
		
		debug_drawings();
		
		if ( isAnimationSet() ) {
			if (isTrailOn) {
				for (int i = trailList.size-1; i >= 0; i--) {
					float inc = (i+1) * trailAlphaIncrement;
					trailColor.a = (inc > trailMaxAlpha) ? trailMaxAlpha : inc;
					batch.setColor( trailColor );
					batch.draw(trail, trailList.get(i).xx, trailList.get(i).yy, trailList.get(i).centerOriginX, trailList.get(i).centerOriginY,
							trailList.get(i).width, trailList.get(i).height, trailList.get(i).scaleX, trailList.get(i).scaleY, trailList.get(i).angle); //draw trail
				}
				trailColor.a = 0.3f;
			}
			
			if ( flashing && (flsTime += deltaTime) > flsInterval ) {
				// Draw the sprite with a colored flash effect
				batch.setShader( shader );
				batch.setColor( flsColor );
				super.draw(batch, deltaTime);
				/*batch.draw(anim.getKeyFrame(elapsedTime), spriteCenter.x, spriteCenter.y, currentOriginCenterX, currentOriginCenterY,
						currentFrameWidth, currentFrameHeight, scaleX, scaleY, angle);*/
				batch.setShader(null); //default
				flsTime = 0;
			} else {
				// Draw the sprite normally
				batch.setColor(spriteColor);
				super.draw(batch, deltaTime);
				/*batch.draw(anim.getKeyFrame(elapsedTime) , spriteCenter.x, spriteCenter.y, currentOriginCenterX, currentOriginCenterY,
						currentFrameWidth, currentFrameHeight, scaleX, scaleY, angle);*/
			}
			
			core.draw(batch, deltaTime);
			
			/*if ( trailOn ) {
				String sss = "trailSize: " + trailList.size;
				debug.screen(sss, (int)(spriteCenter.x - ( (Debug.debug.font.getBounds(sss).width - currentFrameWidth)*.5f)), (int)spriteCenter.y - 30, false);
			}*/
		}
	}
	
	private Color collShapeColor = new Color(1, 1, 1, .7f), touchShapeColor = new Color(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b, .5f);
	
	public void debug_drawings() {
		if ( COLLISION_MASK_DEBUG ) 
			detectionMaskDebug( collisionMask, collShapeColor );
		
		if ( TOUCH_MASK_DEBUG ) 
			detectionMaskDebug( touchMask, touchShapeColor );
		
		if ( DEBUG_NEAREST ) {
			if ( nearestVirus != null ) {
				Debug.debug.insertShapeToRender(SHAPE.LINE, nearestVirus.x, nearestVirus.y, 
						x, y, Color.WHITE);
			}
			nearestVirus = nearest_virus();
		}
		
		if ( DEBUG_SLASH_LINE ) {
			Debug.debug.insertShapeToRender(SHAPE.LINE, SLASH_ORIGIN.x, SLASH_ORIGIN.y, SLASH.x, SLASH.y, Color.RED);
		}
	}
	
	public static void detectionMaskDebug( DetectionMask detectionMask, Color shapeColor ) {
		
		MaskFormat format = detectionMask.getMaskFormat();
		
		if ( format  == MaskFormat.CIRCLE ) { // draw a circle
			Circle collision_mask_bounds = detectionMask.getCircle();
			Debug.debug.insertShapeToRender(SHAPE.CIRCLE, collision_mask_bounds.x, collision_mask_bounds.y, collision_mask_bounds.radius,
					collision_mask_bounds.radius, shapeColor);
		}
		else if ( format == MaskFormat.RECTANGLE ) { // draw a rectangle
			Rectangle collision_mask_bounds = detectionMask.getRectangle();
			Debug.debug.insertShapeToRender( SHAPE.RECTANGLE, collision_mask_bounds.getX(), collision_mask_bounds.getY(), collision_mask_bounds.getWidth(),
					collision_mask_bounds.getHeight(), shapeColor );
		}
		else if ( format == MaskFormat.TRIANGLE ) {
			Polygon polygon = detectionMask.getPolygon();
			Debug.debug.insertPolygonToRender( true, polygon.getTransformedVertices(), shapeColor );
		}
		else if ( format == MaskFormat.POLYGON ) {
			Polygon polygon = detectionMask.getPolygon();
			Debug.debug.insertPolygonToRender( false, polygon.getTransformedVertices(), shapeColor );
		}
		
	}
	
	/**information about current frame*/
//	public float currentOriginCenterX, currentOriginCenterY, currentFrameWidth, currentFrameHeight;
	
	@Override
	public void update(float deltaTime) {
		core.update( deltaTime );
		movementUpdate( deltaTime );
		spriteUpdate( deltaTime );
		updateTrail(); // update trail
		
		if ( isDestroyedOnBottom && (y+currentFrameHeight*.5f) < 0) {
			isReached = true;
			if ( ACTIVE && isHostile ) {
				VirusByteGame.BACK.addForeground(Backgrounds.Effect.Damage, 1f, 5);
				if ( !TUTORIAL ) LIFES--;
			}
			destroy();
		}
		
		if ( flashing ) {
			if ( (flsMainTime += deltaTime) > flsDuration ) {
				// End of the flashing time
				flashing = false;
				flsMainTime = 0;
			}
		} else {
			//about to die
			if ( disease ) {
				destroy(true);
			}
		}
		
	}
	
	/**When true, the virus can be repositioned when not inside the view port yet. Good to maintain a certain distance between each virus*/
	public boolean repositionable = true;
	/**The nearest virus*/
	public VirusType nearestVirus;
	
	public void distance_debug() {
		nearestVirus = nearest_virus();
		if ( nearestVirus == null ) Debug.log("distance is null");
		//debug_nearest = true;
	}
	
	/** Set or reset the position of the virus to the better and free place*/
	public void position_resolver() {
		if (!repositionable || (y+(currentFrameHeight*.5f)) < WORLD_HEIGHT) return;
		nearestVirus = nearest_virus();
		if (nearestVirus == null) return;
		boolean free = false;
		while (!free) {
			if (distanceTo(nearestVirus.getPosition(), getPosition()) < 60) {
				if (MathUtils.isEqual(nearestVirus.speed, speed, 25)) {
					moveAdd(currentFrameWidth+32f, 32f);
					if (x > WORLD_WIDTH-64f) x = 64f;
				} else {
					if (speed < nearestVirus.speed) {
						moveAdd(0, currentFrameHeight+32f);
					} else {
						nearestVirus.moveAdd(0, currentFrameHeight+32f);
					}
				}
				nearestVirus = nearest_virus();
				continue;
			}
			free = true;
		}
	}
	
	/**update of the pattern movement of this virus*/
	public void movementUpdate(float deltaTime) {
		if ( isOnMove ) {
			moveTo( x, y - speed * deltaTime );
		}
	}
	
	public boolean flingTouch(float velocityX, float velocityY, int button) {
		return false;
	}
	/*--------------------------------------------*/
	
	/**determine the minimum distance to the last trail position*/
	protected float minimumDistanceToTheLastTrailPosition = 5f;
	/** trailUpdate utility fields */
	protected Vector2 lastMovementDirection, lastTrailPosition;
	
	/** Update trail array positions */
	public void updateTrail() {
		if ( !TRAILS_ACTIVE || !isTrailOn ) return;
		if (trail == null) return;
		float distanceToLastPosition = Vector2.dst(lastTrailPosition.x, lastTrailPosition.y, x, y);
		if ( distanceToLastPosition > minimumDistanceToTheLastTrailPosition ) {
			TrailIndex trail = trailPool.obtain();
			if ( trail != null ) {
				trail.set(spriteCenter.x, spriteCenter.y, currentOriginCenterX, currentOriginCenterY,
						currentFrameWidth, currentFrameHeight, scaleX, scaleY, getAngle());
				trailList.add(trail);
				lastTrailPosition.set( x, y );
			}
		}
		if ( trailList.size > trailSize ) {
			trailPool.free( trailList.removeIndex(0) );
		}
	}
	
	public void destroy() {
		destroy(false, false);
	}
	
	public void destroy(boolean fatal) {
		destroy(fatal, false);
	}
	
	/**The change of that virus spawn an parasite when defeated*/
	public float spawnParasiteChange;
	
	public void destroy(boolean fatal, boolean hit) {
		this.isFinale = fatal;
		this.hit = hit;
		alive = false;
		VirusManager manager = VirusByteGame.VIRUS_MANAGER;
		
		if ( VirusByteGame.SFX ) {
			//AUDIO TODO
			if ( isReached ) {
				if ( ACTIVE ) {
					
				}
			} else if ( hit ) {
				
			}
		}
		if ( hit ) { 
			if ( VirusByteGame.VIBRATION ) 
				if (imm > 0) VirusByteGame.IMM.vibrate(imm);
		}
		if ( !canBeFree ) 
			dispose(); //dispose any exclusive resource
		
		//>>>>>>>> Spawn a parasite ?
		if ( !TUTORIAL ) {
			if ( hit && (y > 220f) || (spawnParasiteChange == 1f) ) {
				if ( MathUtils.randomBoolean(spawnParasiteChange) ) {
					// ?% change of parasite spawn when the kill occurs up to 220 of height from the bottom border
					SpawnableConfiguration config = manager.spawner.getSCS_basic2(.030f, .9f, .6f, Spawner.Transition.GROW_IN_STOPPED_TO_MOVE);
					manager.spawner.addSpawnable(VirusInstance.Parasite, config, x, y);
					//VIRUS_MANAGER.addVirus(position.x, position.y, VirusManager.VirusInstance.Parasite);
				}
			}
		}
		
		// earn a coin
		if ( hit && isMoneyBack ) {
			Coin coin;
			
			switch ( rewardSize ) {
			case SMALL:
				if ( MathUtils.randomBoolean( .60f ) ) break; // change to not create coin
				coin = new CoinSmall();
				manager.addVirus( x, y, coin );
				coin.setSpeed( speed );
				coin.isOnMove = true;
				break;
			case LARGE:
				if ( MathUtils.randomBoolean( .60f ) ) break; // change to not create coin
				coin = ( MathUtils.randomBoolean( .60f ) ) ? new CoinSmall() : new Coin();
				manager.addVirus( x, y, coin );
				coin.setSpeed( speed );
				coin.isOnMove = true;
				break;
			default:
				break;
			}
			
		}
		
		// <<<<<<<<<<<<<<<<<<<<<<<<
		for (ActionListener action : onDestroy) action.execute(this);
		clearAllActionListeners();
	}
	
	public boolean isOver(Vector3 mousePos) {
		return touchMask.isOver(mousePos.x, mousePos.y);
	}
	
	/**detects a collision and return the virus
	 * @param collidableOnly if true, return only collidable objects*/
	public VirusType collisionDetection( boolean collidableOnly ) {
		
		VirusType result = null;
		Array<VirusType> array = VirusByteGame.VIRUS_MANAGER.allVirus;
		for (int i = array.size-1; i >= 0; i--) {
			VirusType virus = array.get(i);
			
			if ( virus == this ) // exclude this object self
				continue;
			
			if ( collisionMask.collisionDetection( virus.collisionMask ) ) {
				if ( collidableOnly && !virus.isCollidable ) 
					continue;
				result = virus;
				break;
			}
		}
		return result;
		
	}
	
	@Override
	public int compareTo(VirusType virus) {
		return this.getDepth() - virus.getDepth();
	}
	
	// ================================================== //
	// ================ DAMAGES  >>>>>>>>>>>>>>>>>>>>>>>>>>
	
	public void laserDamage() {
		if ( !isDamagedByLaser ) return;
		destroy(true);
	}
	
	public void explosionDamage(BombItem bomb) {
		if ( !isDamagedByExplosions ) return;
		destroy(true);
	}
	
	/** return if the target object has been destroyed 
	 * @param draggable*/
	public boolean collisionDamage(DraggableVirus draggable) {
		if ( isCollidable ) {
			destroy(true);
			return true;
		}
		return false;
	}
	
	private boolean disease;
	
	/**Occurs when the virus theoretically has been infected twice.
	 * The virus will blink RED and suddenly be destroyed*/
	public void disease() {
		setFlashOn( Color.RED, .65f );
		disease = true;
	}
	
	/**return true if the collided virus has been destroyed*/
	public boolean sawDamage(SawItem saw) {
		if ( !isDamagedBySaw ) return false;
		destroy(true);
		return true;
	}
	
	// <<<<<<<<<<<<<<<<<<<<<<<<< DAMAGES ================ ||
	// ================================================== //	
	
	public static int distanceTo(float x1, float y1, Vector2 pos2) {
		return distanceTo(x1, y1, pos2.x, pos2.y);
	}
	
	public static int distanceTo(Vector2 pos1, float x2, float y2) {
		return distanceTo(pos1.x, pos1.y, x2, y2);
	}
	
	public static int distanceTo(Vector2 pos1, Vector2 pos2) {
		return distanceTo(pos1.x, pos1.y, pos2.x, pos2.y);
	}
	
	/**
	 * Distance between mousePosition and point
	 * @param pos1 mousePos
	 * @param pos2 Vector2 virusPos 
	 */
	public static int distanceTo(Vector3 pos1, Vector2 pos2) {
		return distanceTo(pos1.x, pos1.y, pos2.x, pos2.y);
	}
	
	public static int distanceTo(Vector3 pos1, Vector3 pos2) {
		return distanceTo(pos1.x, pos1.y, pos2.x, pos2.y);
	}
	
	public static int distanceTo(float x1, float y1, float x2, float y2) {
		return (int)Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}
	
	public <T extends VirusType> VirusType nearest_virus() {
		return nearest_virus(null);
	}
	
	/**Return a nearest virus of a determined type
	 * @param type if null find the nearest virus of any type*/
	public <T extends VirusType> VirusType nearest_virus(Class<T> type) {
		VirusType nearest = null;
		int nearest_distance = 1000, dst_to_this_virus;
		
		for (int i = 0; i < VirusByteGame.VIRUS_MANAGER.virus_set.size; i++) 
		{
			Array<VirusType> array = VirusByteGame.VIRUS_MANAGER.virus_set.get(i);
			if (array.size == 0) continue;
			for (int j = 0; j < array.size; j++)
			{
				VirusType virus = array.get(j);
				if (virus == this) continue;
				dst_to_this_virus = distanceTo(x, y, virus.x, virus.y);
				if (dst_to_this_virus < nearest_distance) {
					nearest_distance = dst_to_this_virus;
					if (type == null) {
						nearest = virus;
						continue;
					}
					if ( type.isInstance(virus) ) nearest = virus;
				}
			}
		}
		//Debug.log( "nearest is " + ( (nearest == null) ? "null" : nearest.virus_id.toString() ) );
		return nearest;
	}
	
	public void reset() {
		//reset states
		if ( TUTORIAL ) 
			isOnMove = true;
		core.reset(); // reset core attributes 
		flashing = false;
		disease = false;
		alive = true;
		isFinale = false;
		hit = false;
		flsMainTime = 0;
		spawnParasiteChange = 0;
		trailSize = 0;
		isReached = false;
		if ( isTrailOn ) {
			trailPool.freeAll( trailList );
			trailList.clear();
		}
	}
	
	/**When true, get point when this virus is destroyed*/
	public boolean validadePoints = true;
	
	/**if this (virus/object) earn any point value or message when (killed/removed/defeated) from here
	 * you can pass any string to show on screen through PointLog class*/
	public ScreenLogger getPointLog() {
		if ( !validadePoints ) return null;
		if ( TUTORIAL ) {
			String[] string = new String[] {"great", "all right", "right"};
			if ( !hit ) return null;
			ScreenLogger log = VirusByteGame.POINT_MANAGER.screenLoggerPool.obtain();
			return log.set(string[MathUtils.random(string.length-1)], x, y);
		}
		PointLog pointLog = VirusByteGame.POINT_MANAGER.pointLogPool.obtain();
		return pointLog.set( hit ? point : point/2, x, y, true, virus_id, hit, comboInterval);
	}
	
	// ==================================================== //
	// ====== reset to a certain position with ============ //
	// ==================================================== //
	
	public void resetInPosition() {
		resetInPosition(MathUtils.random(64f, WORLD_WIDTH-64f), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}
	
/*	@Override
	public void resetInPosition(float posX, float posY) {
		resetInPosition(posX, posY);
	}*/
	
	public void resetInPosition(float posX, float posY) {
		moveTo( posX, posY ); 
		lastTrailPosition.set( getPosition() );
		spriteUpdate( Gdx.graphics.getDeltaTime() );
		
		/*setSpeed(config.minSpd, config.maxSpd);
		move = !((config.minSpd & config.maxSpd) == 0);*/
		
		// ============ //
		 // action //
		position_resolver();
		for (ActionListener action : onReset) action.execute(this);
		// =========== //
		
		//Debug trail pool
		/*Debug.log( virus_id + " " + id + " trailActive: " + trailOn + " | trailPoolFree: " + trailPool.getFree() +
				" | trailPoolPeak: " + trailPool.peak + " | trailPoolMax: " + trailPool.max );*/
	}
	
	/**An anonymous action that can occur inside an event on the virus*/
	public static abstract class ActionListener {
		public boolean persist;
		
		public ActionListener() {
			this( false );
		}
		
		public ActionListener(boolean persist) {
			this.persist = persist;
		}
		
		public abstract void execute(VirusType virus);
	}
	
	public enum ACTION {ON_DRAW, ON_DESTROY, ON_RESET, ON_UPDATE}
	
	public void addActionListener(ACTION type, ActionListener action) {
		Array<ActionListener> actions;
		switch ( type ) {
		case ON_DESTROY:
			actions = onDestroy;
			break;
		case ON_DRAW:
			actions = onDraw;
			break;
		case ON_UPDATE:
			actions = onUpdate;
			break;
		case ON_RESET:
			actions = onReset;
			break;
		default:
			actions = onDestroy;
			break;
		}
		if ( !actions.contains(action, true) ) actions.add( action );
	}
	
	public void removeActionListener(ActionListener action) {
		if ( onDestroy.removeValue(action, true) ) return;
		if ( onDraw.removeValue(action, true) ) return;
		if ( onReset.removeValue(action, true) ) return;
		if ( onUpdate.removeValue(action, true) ) return;
	}
	
	public void clearAllActionListeners() {
		for (ActionListener action : onDestroy) if ( !action.persist ) onDestroy.removeValue(action, true);
		for (ActionListener action : onDraw) if ( !action.persist ) onDraw.removeValue(action, true);
		for (ActionListener action : onReset) if ( !action.persist ) onReset.removeValue(action, true);
		for (ActionListener action : onUpdate) if ( !action.persist ) onUpdate.removeValue(action, true);
	}
	
	public Assets.Particles getFinalEffectType() {
		return finalEffectType;
	}
	
	/// ======================= ///
	/// ===== trail class ===== ///
	/// ======================= ///
	public static class TrailIndex implements Poolable {
		
		public float xx, yy, centerOriginX, centerOriginY, width, height, scaleX, scaleY, angle;
		
		public TrailIndex() {}
		
		public void set(float xx, float yy, float originX, float originY, float width, float height, float scaleX, float scaleY, float angle) {
			this.xx = xx;
			this.yy = yy;
			this.centerOriginX = originX;
			this.centerOriginY = originY;
			this.width = width;
			this.height = height;
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			this.angle = angle;
		}
		
		@Override
		public void reset() {
			xx = 0;
			yy = 0;
			centerOriginX = 0;
			centerOriginY = 0;
			width = 0;
			height = 0;
			scaleX = 0;
			scaleY = 0;
			angle = 0;
		}
	}
	/// ======================= ///
	/// ======================= ///
	/// ======================= ///
	
	public void dispose() {
		
	}
	
	public void removeFromStage() {
		this.toRemove = true;
		destroy();
	}
	
	// compare implementation
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if ( obj instanceof VirusType && obj.getClass().equals( this.getClass() ) ) {
			VirusType castObj = (VirusType) obj;
			if (castObj.virus_id == this.virus_id) return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hashCode = 17;
		hashCode += virus_id.hashCode();
		hashCode += getClass().hashCode();
		return hashCode;
	}
	
	@Override
	public String getName() {
		return ( virus_id != null ) ? virus_id.toString() : "unnamed" ;
	}
}
