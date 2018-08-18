package com.lksfx.virusByte.gameObject.virusObject.dragon;

import java.util.Iterator;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ReflectionPool;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.BackgroundRenderer;
import com.lksfx.virusByte.effects.BackgroundRenderer.RendererConfig;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.Assets.Particles;
import com.lksfx.virusByte.gameControl.Line2D;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.debug.Debug.SHAPE;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.BossType;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusCore;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner.SpawnableConfiguration;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner.Transition;

public class DragonBoss extends BossType {
	private Animation hurtAnim, tailAnim, dizzyAnim;
	private TextureRegion[] vScaleTextureRegion, hScaleTextureRegion;
	private Body neck, tailA, tailB;
	private float tailA_yy, tailB_yy;
	private float metaYY;
	private Array<Fireball> fireballs;
	private Paddle firewall;
	/**Time to maintain the hurt animation on*/
	private float hurtTimer, hurtEndTime = 1f;
	private boolean isHurt, isDizzy;
	/**Line2d for slash detection*/
	private Line2D slash_line = new Line2D(0f, 0f, 0f, 0f);
	/**With {@link #slash_line} detect a line intersection*/
	private Line2D collision_lineA = new Line2D(0f, 0f, 0f, 0f), collision_lineB = new Line2D(0f, 0f, 0f, 0f);
	/**Determine if the slash is valid*/
	private boolean isSlashValid;
	/**The state of the boss when killed right before be destroyed*/
	private boolean defeated;
	/**Determine if the firewall paddle is already inside the stage*/
	private boolean isFirewallOnTheStage;
	
	private ReflectionPool<Fireball> coloredBallsPool;
	
	private boolean debug_collision_lines;
	
	private enum STATE {ONE, TWO, THREE}
	private STATE state = STATE.ONE;
	
	public DragonBoss() {
		this(VIEWPORT.getWorldWidth()*.5f, 1280f);
	}
	
	public DragonBoss(float x, float y) {
		this(x, y, 150, 80, 100, new Color( Color.WHITE ), false, 100f, Bosses.DRAGONBOSS);
	}

	public DragonBoss(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn, float health, Bosses bossType) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn, health, bossType);
		virus_id = VirusInstance.Dragon;
		neck = new Body(x, y);
		tailA = new Body(x, y, BODYPART.TAIL);
		tailB = new Body(x, y, BODYPART.BOTTOM_TAIL);
		tailA.setTouchMaskBounds(0, 0, 0, 0);
		tailB.setTouchMaskBounds(0, 0, 0, 0);
		tailA.setDepth( 14 );
		tailB.setDepth( -5 );
		neck.setCollisionMaskBounds(5, 0, currentFrameWidth * .35f, 590f);
		neck.setTouchMaskBounds(5, 0, currentFrameWidth * .35f, 590f); // set touch mask same size of the collision mask
		neck.setDepth( 20 );
		setDepth( 25 );
//		collidable = false;
		metaYY = WORLD_HEIGHT * .3f;

		// Damages
		bomb_damage = .1f;
		
		//Fire balls
		coloredBallsPool = new ReflectionPool<Fireball>( Fireball.class );
		fireballs = new Array<Fireball>(); // in use balls, on stage
		
		VirusByteGame.BACK.especialRandomBackground = false;
		
		firewall = new Paddle(0, 0);
//		VirusByteGame.VIRUS_MANAGER.addVirus(WORLD_WIDTH * .5f, -(WORLD_HEIGHT * .05f), firewall);
		//finalize the current especial background active 
		VirusByteGame.BACK.finalizeAllBackEffects();
		VirusByteGame.VIRUS_MANAGER.addVirus(x, WORLD_HEIGHT * 1.57f, tailA);
		VirusByteGame.VIRUS_MANAGER.addVirus(x, WORLD_HEIGHT * 1.57f, tailB);
		VirusByteGame.VIRUS_MANAGER.addVirus(x, y, neck);
		setTouchMaskBounds(0, -20, 64, 64);
		setCollisionMaskBounds(0, -10, 44f, 75f);
		setBossLifebar();
		
		setDefaultVirusCore( new VirusCore<VirusType>( this ) {
			@Override
			public void initialize() {
				setInputController( new InputTriggers() {
					@Override
					public boolean justTouched( Vector3 mousePos ) {
						boolean bool = false;
						if ( isOver(mousePos) ) {
//							destroy();
							Debug.log( "touch on Dragon" );
							bool = true;
						}
						return bool;
					}
					@Override
					public boolean releaseTouch(Vector3 mousePos) {
						isSlashValid = true;
						return super.releaseTouch(mousePos);
					}
					@Override
					public boolean draggedTouch( Vector3 mousePos ) {
						if ( !isSlashValid || !isDizzy ) 
							return false;
						if ( !VirusByteGame.VIRUS_MANAGER.isDrawingSwipe ) 
							return false;
						
						// ===== //
						float lenght_y  = Math.abs( mousePos.y - SLASH_ORIGIN.y );
						
						if ( lenght_y < 100 ) {
							slash_line.setLine(SLASH_ORIGIN.x, SLASH_ORIGIN.y, SLASH.x, SLASH.y);
							
							// ==== //
							if ( slash_line.intersectsLine(collision_lineA) && slash_line.intersectsLine(collision_lineB) ) {
								// === //
								Debug.log("Slash line intersect Dragon " + Intersector.distanceSegmentPoint(SLASH_ORIGIN, SLASH, getPosition()));
								if ( !neck.isOver(mousePos) ) {
									damage( 5f );
									isSlashValid = false;
								}
								// === //
							}
							// ==== //
						}
						// ===== //
						
						return false;
					}
				} );
			}
			@Override
			public void update(float deltaTime) {
				// TODO Auto-generated method stub
				
			}
		} );
	}
	
	@Override
	public void resetInPosition() {
		resetInPosition( MathUtils.round( VirusByteGame.VIEWPORT.getWorldWidth()*.5f ), 1280f );
		float y = (this.y > WORLD_HEIGHT) ? this.y : WORLD_HEIGHT * 1.4f;
		tailA.resetInPosition(this.x, y);
		tailB.resetInPosition(this.x, y + 128f);
		tailA.setCollisionMaskBounds(0, -64f, 800f, 50f);
		tailB.setCollisionMaskBounds(0, 0, 0, 0);
		tailA.setSpeed( speed - 30 );
		tailB.setSpeed( speed - 30 );
		float xx = this.x, yy = this.y + 64;
		xx += ( neck.isSpriteInverted() ) ? -7 : 7;
		tailA_yy = WORLD_HEIGHT * 1.05f;
		tailB_yy = tailA_yy - 48;
		neck.moveTo(xx, yy);
	}
	
	public void setBossLifebar() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		HUD.lifebar_table.clearChildren();
		Image bar = new Image( assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion("bossbar") );
		fill = new Image( assetManager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("lifebar-fill") );
		fill_height = fill.getHeight();
		Stack stack = new Stack();
		Table fill_table = new Table();
		fill_table.add( fill ).padBottom(47);
		stack.add(fill_table);
		stack.add(bar);
		face = new ImageButton( new TextureRegionDrawable( assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion("dragon-head") ) );
		Container<ImageButton> container = new Container<ImageButton>(face).bottom();
		stack.add(container);
		
		HUD.lifebar_table.add(stack).pad(3f);
		
		// === make the warning table to show on main hud === //
		Table warning_table = new Table();
		warn_label = new Label("boss time!", HUD.skin, "visitor32-bold", Color.BLACK);
		warning_table.add(warn_label).expand().bottom();
		HUD.addBossAlert(warning_table); //show an alert on the screen: boss is coming
		// === === //
		alertOn = true;
		Debug.log("info: " + face.getOriginY() );
	}
	
	private final String imageFolderPath = "data/sprites/virus/dragon/";
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		// A list with all available dragon colors
		String[] color = new String[] { "black", "blue", "forest", "gold", "red", "retro", "white" };
		int n = MathUtils.random(color.length-1); //pick a random color
		Debug.log( color[n] + " selected!" );
		AtlasRegion[] normal = new AtlasRegion[1];
		// Make scales animation
		Texture 
		tex1 = assetManager.get(imageFolderPath+color[n]+"-scales1.png", Texture.class), 
		tex2 = assetManager.get(imageFolderPath+color[n]+"-scales2.png", Texture.class);
		
		tex1.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		tex1.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		tex2.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		tex2.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		vScaleTextureRegion = new TextureRegion[] { new TextureRegion( tex1 ), new TextureRegion( tex2 ) };
		hScaleTextureRegion = new TextureRegion[] { new TextureRegion( tex1), new TextureRegion( tex2 ) };
		for (int i = 0; i < hScaleTextureRegion.length; i++) hScaleTextureRegion[i].flip(false, true);
		tailAnim = new Animation( .15f, vScaleTextureRegion );
		
		// Make head hurt animation
		Debug.log( "Dragon atlas is " + assetManager.isLoaded(Assets.Atlas.dragonAtlas.path, TextureAtlas.class) );
		normal[0] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( color[n]+"-head-hurt" );		
		createAnim( normal, null, .15f );
		hurtAnim = getAnimation();
		// Dizzy animation
		normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( "dizzy1" );
		normal[1] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( "dizzy2" );
		createAnim( normal, null );
		dizzyAnim = getAnimation();
		// Default animation
		normal[0] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( color[n]+"-head1" );
		normal[1] = assetManager.get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion( color[n]+"-head2" );
		createAnim( normal, null, .15f );
		
	}
	
	/**Determine if the dragon head already reach the main vertical spot on stage screen*/
	private boolean reachedOnSpot;
	/**Determine in what direction the swing balance goes: Up or Down*/
	private BALANCE balance = BALANCE.TOP;
	protected enum BALANCE {DOWN, TOP, NONE};
	
	/**Timer and time interval to call the evoke fireball's method*/
	private float evokeTimer, evokeIntervalCall = 1.5f;
	
	/**The current number of collision with fireball's computed in the dragon body*/
	private int totalCollisionWithFireballs;
	
	/**The number of collisions interval for the appearance of the next {@link Powerball}*/
	private int collisionsIntervalToNextPowerball = 10;
	
	/**The max number of fireball's instances inside the stage*/
	private int maxNumberOfFireballOnStage = 10;
	
	/**Control the random move duration in the up/down swing*/
	private float timeToSwingMove, swingMoveTimer;
	
	/**Switch between swing on/off state*/
	private boolean swing;
	
	private float textColorSwitch;
	private float defTimer;
	private final float defInterval = .20f;
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		if ( !reachedOnSpot && y <= metaYY ) reachedOnSpot = true;
		isOnMove = ( reachedOnSpot ) ? false : true;
		if ( alertOn && !reachedOnSpot ) {
			textColorSwitch += deltaTime;
			if ( textColorSwitch > .1f ) {
				textColorSwitch = 0;
				warn_label.getStyle().fontColor = warn_label.getStyle().fontColor.equals(Color.BLACK) ? Color.WHITE : Color.BLACK;
			}
			//remove the boss alert when
			if ( y < WORLD_HEIGHT * .6f ) {
				alertOn = false;
				HUD.removeBossAlert();
				RendererConfig config = new RendererConfig();
				config.wrapmode = TextureWrap.ClampToEdge;
				VirusByteGame.BACK.addBackground(background = new BackgroundRenderer( new String[] {Assets.Textures.binaryTex.path}, 
						new Vector2(.2f, .2f), new Vector2(), 15, true, config, true) ); // add background
				background.setAlphaAttribute(.65f, .005f);
				background.setTextureBrowser(0f, .1f);
				background.setSize(WORLD_WIDTH, WORLD_HEIGHT);
			}
			//>>
			if ( !isFirewallOnTheStage ) {
				if ( y < WORLD_HEIGHT ) {
					VirusByteGame.VIRUS_MANAGER.addVirus(WORLD_WIDTH * .5f, -(WORLD_HEIGHT * .05f), firewall);
					isFirewallOnTheStage = true;
				}
			}
			//>>
		}
		// ===== //
		if ( reachedOnSpot && !isDizzy && !defeated ) {
			//>>
			if ( swing ) {
				// === //
				switch (balance) {
				case DOWN:
					moveAdd(0, -speed * deltaTime);
					if ( y <= metaYY ) balance = BALANCE.TOP;
					break;
				case TOP:
					moveAdd(0, speed * deltaTime);
					if ( y >= WORLD_HEIGHT - 50f ) balance = BALANCE.DOWN;
					break;
				default:
					break;
				}
				// === //
			}
			//>>
			if ( (swingMoveTimer += deltaTime) >= timeToSwingMove ) {
				swingMoveTimer = 0;
				swing = !swing;
				timeToSwingMove = MathUtils.random(( swing ) ? 3f : 1f, ( swing ) ? 10f : 3f );
				if ( swing ) metaYY = MathUtils.random(WORLD_HEIGHT * .3f, WORLD_HEIGHT * .7f);
			}
			//>>
			if ( (evokeTimer += deltaTime) >= evokeIntervalCall ) 
				evokeFireball();
		}
		// ===== //
		
		if (defeated) {
			if ( (defTimer += deltaTime) > defInterval ) {
				defTimer = 0;
				if ( tailB.removeBodyPart() ) {
					if ( tailA.removeBodyPart() ) {
						if ( neck.removeBodyPart() ) destroy(true, true);
					}
				}
			}
		}
		
		//Active fire balls
		Iterator<Fireball> iterator = fireballs.iterator();
		while ( iterator.hasNext() ) {
			Fireball ball = iterator.next();
			if ( !ball.alive ) {
				if ( !VirusByteGame.VIRUS_MANAGER.contains(ball, true) ) {
					iterator.remove();
					if ( !ball.isPowerball ) coloredBallsPool.free( ball );
				}
			}
		}
		
		// Neck Position
		neck.moveTo(x, y + 340f);
		
		if ( isHurt && (hurtTimer += deltaTime) >= hurtEndTime ) {
			if ( !defeated ) setAnimation( anim_default );
			isDizzy = false;
		}
		
		// ===== //
		VirusType collided;
		if ( (collided = collisionDetection(true)) != null ) {
			if ( collided instanceof Fireball ) {
				Fireball ball = (Fireball) collided;
				if ( Intersector.overlaps(ball.circle, collisionMask.getRectangle()) ) {
					if ( ball instanceof Powerball ) {
						Powerball powerball = (Powerball) ball;
						hurt( powerball.isOnChargedPaddleState );
					} else {
						hurt( false );
					}
					ball.collisionWithDragonEvent();
					totalCollisionWithFireballs++;
				}
			}
		}
		// ===== //
		float halfW = 60f;
		collision_lineA.setLine(x - halfW, y + 50, x - halfW, y + 640); //set collisionLine A
		collision_lineB.setLine(x + halfW, y + 50, x + halfW, y + 640); //set collisionLine B
	}
	
	private Spawner spawner = VirusByteGame.VIRUS_MANAGER.spawner; 
	private int evokeMinSpeed = 330, evokeMaxSpeed = 350;
	
	private void evokeFireball() {
		evokeTimer = 0;
		if ( fireballs.size > maxNumberOfFireballOnStage || isDizzy ) return;
		boolean powerball = ( totalCollisionWithFireballs >= collisionsIntervalToNextPowerball ); 
		float leftMinX, leftMaxX, rightMinX, rightMaxX;
		float vMin, vMax;
		vMin = WORLD_HEIGHT * .35f;
		vMax = WORLD_HEIGHT - vScaleTextureRegion[0].getRegionWidth();
		leftMinX = 32f;
		leftMaxX = x - vScaleTextureRegion[0].getRegionWidth() * .65f;
		rightMinX = x + vScaleTextureRegion[0].getRegionWidth() * .65f;
		rightMaxX = WORLD_WIDTH - 32;
		float xx, yy = MathUtils.random(vMin, vMax);
		xx = ( MathUtils.randomBoolean() ) ? MathUtils.random(leftMinX , leftMaxX) : MathUtils.random(rightMinX , rightMaxX);
		Fireball fireball = ( powerball ) ? new Powerball(xx, yy, false) : coloredBallsPool.obtain().setBallColor( MathUtils.random(1, 7) );
		SpawnableConfiguration config = spawner.getSCS_basic2(.05f, 1f, 1f, Transition.GROW_IN_STOPPED);
		spawner.addSpawnable(fireball, config, xx, yy);
		/*int minSpd = (state == STATE.ONE) ? 230 : ((state == STATE.TWO) ? 330 : 430);
		int maxSpd = (state == STATE.ONE) ? 250 : ((state == STATE.TWO) ? 350 : 450);*/
		int minSpd = evokeMinSpeed;
		int maxSpd = evokeMaxSpeed;
		fireball.setSpeed(minSpd, maxSpd);
		fireballs.add( fireball );
		if ( powerball ) {
			Debug.log( "Fireball evoked" );
			totalCollisionWithFireballs = 0;
		}
		Debug.log( "ball on pool | free: " + coloredBallsPool.getFree() + " | peak: " + coloredBallsPool.peak + " | max: " + coloredBallsPool.max + " | on stage: " + fireballs.size );
	}
	
	@Override
	public void damage(float damage) {
		if ( defeated ) return;
		
		if ( health > 0 ) {
			setFlashOn( Color.RED, 1f );
			health -= damage;
			life_percent = (health / full_health) * 100;
			fill.setHeight( (fill_height / 100) * life_percent );
			if ( ((.01f*dmLastMutation)-(.01*life_percent)) > (dmInterval) ) {
				if (background != null) background.setTextureBrowser(0f, background.scrollSpeedV+=.02f);
				dmLastMutation = life_percent;
			}
		} else {
			defeated = true;
			setFlashOn(Color.RED, 5f);
			setAnimation( hurtAnim );
			for (int i = 0; i < fireballs.size; i++) fireballs.get(i).destroy();
//			destroy(true, true);
		}
		
		if ( state == STATE.ONE && life_percent < 60f ) {
			state = STATE.TWO;
//			evokeIntervalCall = 3f;
			maxNumberOfFireballOnStage = 15;
			collisionsIntervalToNextPowerball = 15;
		} else if ( state == STATE.TWO && life_percent < 30f ) {
			state = STATE.THREE;
//			evokeIntervalCall = 1.5f;
			maxNumberOfFireballOnStage = 20;
			collisionsIntervalToNextPowerball = 20;
		}
		
		int incSpeed = Math.round( damage );
		evokeMinSpeed += incSpeed;
		evokeMaxSpeed += incSpeed;
		evokeIntervalCall -= incSpeed * .005f;
		Debug.log( "Dragon evoke fireball interval is: " + evokeIntervalCall ); 
		
		BACK.setBackColor();
	}
	
	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		super.draw(batch, deltaTime);
		if ( isDizzy ) {
			batch.draw(dizzyAnim.getKeyFrame(elapsedTime), x - 40, y + 35f);
		}
	}
	
	/**Change the head animation for a instant
	 * @param powerball if true the collision will make the dragon dizzy by a period of time*/
	public void hurt(boolean powerball) {
		totalCollisionWithFireballs += (!powerball) ? 1 : 0;
		setFlashOn();
		if ( isDizzy ) return;
		hurtEndTime = ( powerball ) ? 5f : 1f;
		isDizzy = powerball;
		setAnimation( hurtAnim );
		hurtTimer = 0;
		isHurt = true;
	}
	
	@Override
	public void setFlashOn(Color color, float duration) {
		if ( flashing && flsColor != Color.WHITE ) return;
		super.setFlashOn(color, duration);
		neck.setFlashOn(color, duration);
		tailA.setFlashOn(color, duration);
		tailB.setFlashOn(color, duration);
	}
	
	@Override
	public void destroy(boolean fatal, boolean hit) {
		super.destroy(fatal, hit);
		neck.destroy();
		tailA.destroy();
		tailB.destroy();
		firewall.removeFromStage();
		for (Fireball ball : fireballs) ball.removeFromStage();
		
		if ( hit ) {
			VIRUS_MANAGER.pointsManager.boss_log("Dragon", 1, 5f, point, 
					VirusByteGame.ASSETS.getAssetManager().get(Assets.Atlas.dragonAtlas.path, TextureAtlas.class).findRegion("combo-icon") );
			VIRUS_MANAGER.pointsManager.addDefeatedBossToList( getBossType() ); //add this boss to defeated boss list, for purpose of achievements
			//for debug purpose
			if ( BOSS_DEBUG ) Debug.log("Times this boss has been defeated in this round: " + VIRUS_MANAGER.pointsManager.getBossTotalTimesDefeated( getBossType() ) );
		}
		
		coloredBallsPool.clear(); // clear this pool
		// remove all Shockbots
		HUD.lifebar_table.clear();
		VirusByteGame.BACK.especialRandomBackground = true;
		if ( background != null ) 
			background.finalize(); // deactivate especial background
		
	}
	
	@Override
	public void debug_drawings() {
		
		if ( debug_collision_lines ) {
			Debug.debug.insertShapeToRender(SHAPE.LINE, collision_lineA.start.x, collision_lineA.start.y, collision_lineA.end.x, collision_lineA.end.y, Color.CYAN);
			Debug.debug.insertShapeToRender(SHAPE.LINE, collision_lineB.start.x, collision_lineB.start.y, collision_lineB.end.x, collision_lineB.end.y, Color.CYAN);
		}
		
		super.debug_drawings();
	}
	
	
	// =============================== //
	// =========== BODY ============== //
	// =============================== //
	private enum BODYPART {TAIL, BOTTOM_TAIL, NECK};
		
	private class Body extends VirusType {
		private BODYPART part;
		
		public Body(float x, float y) {
			this(x, y, BODYPART.NECK);
		}
		
		public Body(float x, float y, BODYPART part) {
			super(x, y);
			setIndestructible( true );
			this.part = part;
			isOnMove = false;
		}
		
		@Override
		public void update(float deltaTime) {
			super.update(deltaTime);
			
			if ( part == BODYPART.BOTTOM_TAIL ) {
				isOnMove = ( y > tailB_yy );
			} else if ( part == BODYPART.TAIL ) {
				isOnMove = ( y > tailA_yy );
			}
			
			VirusType collided;
			if ( (collided = collisionDetection(true)) != null ) {
				if ( collided instanceof Fireball ) {
//						VirusManager.PART_EFFECTS.createEffect(Particles.firework, collided.position.x, collided.position.y);
					hurt( false );
					((Fireball) collided).collisionWithDragonEvent();
				}
			}
			
		}
		
		@Override
		public void setAtlas() {
			createAnim(tailAnim.getKeyFrames(), null, .16f);
		}
		
		private int scroll;
		private float upTailOffset, belowTailOffset;
		private int neckHeight = 640;
		
		@Override
		public void draw(SpriteBatch batch, float deltaTime) {
			//super.draw(batch, deltaTime);
			if ( isVisible ) {
				debug_drawings();
				
				boolean finishShader = false;
				if ( flashing && (flsTime += deltaTime) > flsInterval ) {
					// Draw the sprite with a colored flash effect
					batch.setShader(shader);
					batch.setColor( flsColor );
					finishShader = true;
				}
				
				scroll += (!isDizzy && !defeated) ? 1 : 0;
				if ( part == BODYPART.BOTTOM_TAIL ) {
					//Tail
					if ( !finishShader ) batch.setColor( Color.GRAY );
					batch.draw(getAnimation().getKeyFrame( elapsedTime ).getTexture(), belowTailOffset, 
							y, 0f, 0f, 120, (int)WORLD_WIDTH, 1f, 1f, 
							-90f, 0, -scroll, 120, -(int)WORLD_WIDTH, false, false);
				} else if ( part == BODYPART.TAIL ) {
					if ( !finishShader ) batch.setColor( Color.WHITE );
					batch.draw(getAnimation().getKeyFrame( elapsedTime ).getTexture(), upTailOffset, 
							y, 0f, 0f, 120, (int)WORLD_WIDTH, 1f, 1f, 
							-90f, 0, -scroll, 120, (int)WORLD_WIDTH, false, false);
				} else {
					// Neck
					batch.draw(getAnimation().getKeyFrame( elapsedTime ).getTexture(), DragonBoss.this.x - 55f, 
							DragonBoss.this.spriteMask.getRectangle().y + 75, 120, neckHeight, 0, 0, 120, neckHeight, false, false);
				}
				
				if ( finishShader ) {
					batch.setShader(null); //default
					flsTime = 0;
				}
			}
		}
		
		/**Remove a part of the body, and return when all parts are removed from the screen.
		 * This method composes part of the dragon defeat animation */
		public boolean removeBodyPart() {
			float spriteHeight = getAnimation().getKeyFrame(0).getRegionHeight();
			float lastX = 0, lastY = 0;
			
			if ( part == BODYPART.TAIL ) {
				if ( upTailOffset > WORLD_WIDTH ) return true;
				// ==== //
				lastY = tailA_yy - 100f;
				upTailOffset += spriteHeight;
				lastX = upTailOffset - (spriteHeight * .5f);
				// ==== //
			} else if ( part == BODYPART.BOTTOM_TAIL ) {
				if ( belowTailOffset > WORLD_WIDTH ) return true;
				lastY = tailA_yy -48f - 100f;
				belowTailOffset += spriteHeight;
				lastX = belowTailOffset - (spriteHeight * .5f);
			} else {
				if ( neckHeight == 0 ) return true; 
				neckHeight -= MathUtils.round( spriteHeight );
				lastX = x;
				lastY = DragonBoss.this.spriteMask.getRectangle().y + 75 + neckHeight + (spriteHeight * .5f);
				if ( neckHeight < 0) neckHeight = 0; 
			}
			VirusManager.PART_EFFECTS.createEffect(Particles.explosion, lastX, lastY, getDepth() + 1);
			return false;
		}
		
		/**return true if the sprite is inverted*/
		private boolean isSpriteInverted() {
			return false;
		}
		
		@Override
		public String getName() {
			return "Dragon body";
		}
		
	}
	// =============================== //
	// =============================== //
	// =============================== //
	
}
