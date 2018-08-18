package com.lksfx.virusByte.gameObject.virusObject.pepperBros;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
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
import com.lksfx.virusByte.gameControl.MyUtils;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.BossType;
import com.lksfx.virusByte.gameObject.abstractTypes.DetectionMask.MaskFormat;
import com.lksfx.virusByte.gameObject.abstractTypes.DraggableVirus;
import com.lksfx.virusByte.gameObject.abstractTypes.RotationTestCore;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.gameObject.itemObject.BombItem;
import com.lksfx.virusByte.gameObject.itemObject.SawItem;
import com.lksfx.virusByte.gameObject.pontuation.ScreenLogger;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner.SpawnableConfiguration;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner.Transition;

public class PepperBrosBoss extends BossType {
	
	public Cannon cannons[];
	private Spawner spawner;
	private ReflectionPool<PepperRocket> pepperRocketPool;
	private ReflectionPool<PepperBomb> pepperBombPool;
	private ReflectionPool<Smoke> smokePool;
	private Array<PepperRocket> rockets;
	private Array<PepperBomb> bombs;
	private Array<Smoke> smokes;
	
	private float mainTime;
	/**The time to stay on the screen*/
	private final float timeToStayOnScreen = 300f;
	
	public PepperBrosBoss() {
		this(VIEWPORT.getWorldWidth()*.5f, 1280f);
	}
	
	public PepperBrosBoss(float x, float y) {
		this(x, y, 150, 80, 100, new Color( Color.WHITE ), false, 100f, Bosses.PEPPERBOSS);
	}

	public PepperBrosBoss(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn, float health, Bosses bossType) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn, health, bossType);
		cannons = new Cannon[] { new Cannon(0, 0, 0, 50, 60, null, false, CannonType.STORM), new Cannon(0, 0, 0, 50, 60, null, false, CannonType.MYDOOM) };
		for (Cannon cnd : cannons) 
			VirusByteGame.VIRUS_MANAGER.addVirus( cnd );
		full_health = cannons[0].full_health + cannons[1].full_health;
		pepperRocketPool = new ReflectionPool<PepperRocket>( PepperRocket.class );
		pepperBombPool = new ReflectionPool<PepperBomb>( PepperBomb.class );
		smokePool = new ReflectionPool<Smoke>( Smoke.class );
		rockets = new Array<PepperRocket>();
		bombs = new Array<PepperBomb>();
		smokes = new Array<Smoke>();
		spawner = VirusByteGame.VIRUS_MANAGER.spawner;
		virus_id = VirusInstance.Pepperbros;
		isCollidable = false;
		isVisible = false;
		isOnMove = false;
		VirusByteGame.BACK.especialRandomBackground = false;
		//finalize the current especial background active 
		VirusByteGame.BACK.finalizeAllBackEffects();
		setBossLifebar();
	}
	
	@Override
	public void resetInPosition() {
		resetInPosition( MathUtils.round( VirusByteGame.VIEWPORT.getWorldWidth()*.5f ), 1080f );
		for (Cannon cannon : cannons) {
			cannon.resetInPosition(x, y);
		}
	}
	
	float textColorSwitch;
	
	public void setBossLifebar() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		HUD.lifebar_table.clearChildren();
		Image bar = new Image( assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("newboss-lifebar") );
		fill = new Image( assetManager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("lifebar-fill") );
		fill_height = fill.getHeight();
		Stack stack = new Stack();
		Table fill_table = new Table();
		fill_table.add( fill ).padBottom(47);
		stack.add(fill_table);
		stack.add(bar);
		face = new ImageButton( new TextureRegionDrawable( assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("newboss-head") ) );
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
		//Debug.log("info: " + face.getOriginY() );
	}
	
	private boolean reachedOnMainSpot;
	private enum VPOS {
		UPON(480f), BELOW(440f), MIDDLE(440f), OUT(-125f);
		public float yy;
		VPOS(float y) {
			yy = y;
		}
	}
	/**If the main movement (up/down) is active on the boss*/
	private boolean swingMovement = true;
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if ( alertOn ) {
			//remove the boss alert when
			textColorSwitch += deltaTime;
			if ( textColorSwitch > .1f ) {
				textColorSwitch = 0;
				warn_label.getStyle().fontColor = warn_label.getStyle().fontColor.equals(Color.BLACK) ? Color.WHITE : Color.BLACK;
			}
			
			for (Cannon cnd : cannons) {
				if ( cnd.y < WORLD_HEIGHT ) {
					alertOn = false;
					HUD.removeBossAlert();
					RendererConfig config = new RendererConfig();
					config.wrapmode = TextureWrap.ClampToEdge;
					VirusByteGame.BACK.addBackground(background = new BackgroundRenderer( new String[] {Assets.Textures.binaryTex.path}, 
							new Vector2(.2f, .2f), new Vector2(), 15, true, config, true) ); // add background
					background.setAlphaAttribute(.65f, .005f);
					background.setTextureBrowser(0f, .1f);
					background.setSize(WORLD_WIDTH, WORLD_HEIGHT);
					break;
				}
			}
		}
		
		// Check if the two bosses reached the main spot 
		if ( !reachedOnMainSpot ) {
			if ( cannons[0].isOnSpot && cannons[1].isOnSpot  ) reachedOnMainSpot = true;
		} else if ( (mainTime += deltaTime) < timeToStayOnScreen ) {
			// Begin movement >>>>>
			if ( swingMovement ) {
				Cannon storm = cannons[0], doom = cannons[1];
				if ( !storm.dizzy && !doom.dizzy && storm.isOnSpot == storm.isOnSpot && storm.verticalPosition == doom.verticalPosition ) {
					storm.verticalPosition = ( storm.verticalPosition != VPOS.UPON ) ? VPOS.UPON : VPOS.BELOW;
					storm.isOnSpot = false;
					doom.verticalPosition = ( doom.isOnSpot )? ( ( doom.verticalPosition == VPOS.BELOW )? VPOS.UPON : VPOS.BELOW ) : doom.verticalPosition; 
				} else {
					// Storm check
					if ( storm.isOnSpot ) {
						switch ( storm.verticalPosition ) {
						case BELOW:
							if ( doom.dizzy || (doom.verticalPosition == VPOS.UPON && doom.isOnSpot) ) {
								//Debug.log( "storm reach the bottom" );
								storm.verticalPosition = VPOS.UPON;
								storm.isOnSpot = false;
							}
							break;
						case UPON:
							if ( doom.dizzy || (doom.verticalPosition == VPOS.BELOW && doom.isOnSpot) ) {
								//Debug.log( "storm reach the top" );
								storm.verticalPosition = VPOS.BELOW;
								storm.isOnSpot = false;
							}
							break;
						default:
							break;
						}
					}
					
					if ( doom.isOnSpot ) {
						switch ( doom.verticalPosition ) {
						case BELOW:
							if ( storm.dizzy || (storm.verticalPosition == VPOS.UPON && storm.isOnSpot) ) {
								//Debug.log( "doom reach the bottom" );
								doom.verticalPosition = VPOS.UPON;
								doom.isOnSpot = false;
							}
							break;
						case UPON:
							if ( storm.dizzy || (storm.verticalPosition == VPOS.BELOW && storm.isOnSpot) ) {
								//Debug.log( "doom reach the top" );
								doom.verticalPosition = VPOS.BELOW;
								doom.isOnSpot = false;
							}
							break;
						default:
							break;
						}
					}
				}
			}
			// Begin movement <<<<<
		} else {
			for (Cannon cnd : cannons) {
				cnd.canShoot = false;
				cnd.verticalPosition = VPOS.OUT;
			}
			if ( cannons[0].isReached || cannons[1].isReached ) destroy();
		}
		
		// Rockets update
		Iterator<PepperRocket> iterator1 = rockets.iterator();
		while ( iterator1.hasNext() ) {
			PepperRocket rocket = iterator1.next();
			if ( !rocket.alive && !VirusByteGame.VIRUS_MANAGER.contains(rocket, true)  ) {
				pepperRocketPool.free(rocket);
				iterator1.remove();
			}
		}
		// Bombs update
		Iterator<PepperBomb> iterator2 = bombs.iterator();
		while ( iterator2.hasNext() ) {
			PepperBomb bomb = iterator2.next();
			if ( !bomb.alive && !VirusByteGame.VIRUS_MANAGER.contains(bomb, true) ) {
				pepperBombPool.free(bomb);
				iterator2.remove();
			}
		}
		// Smokes update
		Iterator<Smoke> iterator3 = smokes.iterator();
		while ( iterator3.hasNext() ) {
			Smoke smoke = iterator3.next();
			if ( !smoke.alive && !VirusByteGame.VIRUS_MANAGER.contains(smoke, true)  ) {
				smokePool.free(smoke);
				iterator3.remove();
			}
		}
//		Debug.log( "total bombs: " + bombs.size );
	}
	
	@Override
	public void damage( float damage ) {
		
		health = cannons[0].health + cannons[1].health;
		if ( health > 0 ) {
			life_percent = (health / full_health) * 100;
			fill.setHeight( (fill_height / 100) * life_percent );
			if ( ((.01f*dmLastMutation)-(.01*life_percent)) > (dmInterval) ) {
				if (background != null) background.setTextureBrowser(0f, background.scrollSpeedV+=.02f);
				dmLastMutation = life_percent;
			}
		}
		else {
			PepperBrosBoss.this.destroy(true, true);
		}
	}

	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("storm-iddle1");
		normal[1] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("storm-iddle2");		
		createAnim( normal, null, .15f );
		
	}
	
	private enum CannonType {
		STORM("storm", -100, false), MYDOOM("mydoom", 100f, true);
		/**The position to reach on the stage from the center*/
		public float metaX;
		public String pathName;
		public boolean clockwise;
		
		private CannonType(String pathName, float metaX, boolean clockwise) {
			this.pathName = pathName;
			this.metaX = metaX;
			this.clockwise = clockwise;
		}
		
	};
		
	private class Cannon extends VirusType {
		private CannonType cndType;
		private RotationTestCore defaultCore;
		private float full_health = 75f, health = full_health;
		private Animation animStoppedIddle, animShoot, animDizzy, animGauss, animPeppers;
		private float lastValidAngle;
		/**Shot animation control values*/
		private float shootInvervalTime = 3f, shootTimer;
		/**Timers to control rotation movements*/
		private float afterRotateSleepTime, rotationResetSpd, autoRotationSpeed = 1.2f;
		private final float sleepOnAngleTime = 1f;
		/**Control the valid angle variation*/
		private float timeToChangeValidAngle, timeBwtChangeValidAngle = 7f;
		private float damageOnRotationCompletion = 5f; // backup value: 5f;
		private VPOS verticalPosition = VPOS.MIDDLE;
		/**Is vulnerable, by rotation*/
		private boolean dizzy;
		private boolean alu;
		/** This variable is useful for debug */
		private boolean allowAnyDirectionRotation = true;
		/**True when allowed to shoot*/
		private boolean canShoot;
		
		private Vector2 metaPosition;
		
		public Cannon(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn, CannonType cndType) {
			super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
			this.cndType = cndType;
			metaPosition = new Vector2(cndType.metaX, verticalPosition.yy);
			finalEffectType = null;
			setAtlas();
			setCollisionMaskBounds( currentFrameWidth*.65f, currentFrameHeight*.45f );
			collisionMask.setMaskFormat( MaskFormat.CIRCLE );
			collisionMask.setCircleRadius( currentFrameWidth*.40f );
			collisionMask.setOffset( 0, ( cndType == CannonType.MYDOOM ) ? 10 : -5 );
			touchMask.setOffset( 0, ( cndType == CannonType.MYDOOM ) ? 10 : -5 );
			
			virus_id = VirusInstance.Pepperbros;
			animShoot.setPlayMode( PlayMode.NORMAL );
			canShoot = true;
			isOnMove = false;
			
			defaultCore = new RotationTestCore( this ) {
				@Override
				public void initialize() {
					rotationTorque = .2f;
					minimumDistanceToCenterAxis = (currentFrameWidth + currentFrameHeight) * .1f;
					maximumDistanceToCenterAxis = (currentFrameWidth + currentFrameHeight) * .4f;
					addInputController( new InputTriggers() {
						@Override
						public boolean releaseTouch(Vector3 mousePos) {
							if ( timeRotation >= timeToRotate ) 
								rotateLock = false;
							timeRotation = 0;
							return super.releaseTouch(mousePos);
						}
					} );
				}
				@Override
				public void update(float deltaTime) {
					alu = ( Cannon.this.cndType.clockwise == isClockwise && totalRotation > 10 ) ? true : false;
					
					float angle = getAngle();
					if ( wasHit ) { // when this pepper has been hit
						float torque = angleToAdd * .5f;
						angleTotalAdded += torque;
						angleAdd( isClockwise ? torque : -torque );
						rotateLock = true;
						if ( angleTotalAdded > 1080f && (angle > 330f || angle < 15f) ) {
							angleTotalAdded = 0;
							shootTimer = 0;
							staticDizzy = true;
							rotateLock = false;
							wasHit = false;
						}
					} else {
						if ( shootTimer > shootInvervalTime && canShoot && !dizzy ) 
							shoot();
					}
					
					// When screen is been touched
					if ( isGestureCapting && ( timeRotation += deltaTime ) > timeToRotate ) {
						timeToChangeValidAngle = sleepOnAngleTime;
						rotateLock = true;
					}
					
				}
				@Override
				protected void gestureStart( Vector3 mousePos ) {
					for ( Cannon cnd : cannons ) {
						if ( cnd != Cannon.this ) 
							cnd.defaultCore.rotateLock = true;
					}
						
					if ( staticDizzy ) 
						staticDizzy = false;
					
					isBeingRotating = true; //this cannon is in rotation
					
					if ( !dizzy && !allowAnyDirectionRotation ) {
						if ( Cannon.this.cndType == CannonType.MYDOOM ) {
							allowClockwiseRotation = false;
						} else {
							allowCounterClockwiseRotation = false;
						}
					} else {
						allowClockwiseRotation = true;
						allowCounterClockwiseRotation = true;
					}
					super.gestureStart(mousePos);
					
				}
				@Override
				protected void gestureUpdate(Vector3 mousePos, float torque) {
					if ( !dizzy && getAnimation() != animStoppedIddle && !shooting ) 
						setAnimation( animStoppedIddle );
					/*float cAngle = this.angle - 90f;
					cAngle += (cAngle < 0) ? -360f : 0;*/
					
					rotationTorque = ( dizzy ) ? 1f : .2f; // light weight when in dizzy stage
					super.gestureUpdate( mousePos, torque );
					if ( dizzy ) { // when dizzy
						
//						Debug.log( "total rotation during dizzy: " + totalRotationDuringDizzyState );
						if ( ( totalRotationDuringDizzyState += rotationAddLastUpdate ) > 360f) {
							gestureComplete( isClockwise );
							totalRotationDuringDizzyState = 0;
						}
						
					}
				}
				@Override
				protected void gestureEnd() {
					if ( isBeingRotating ) {
						isBeingRotating = false;
						afterRotateSleepTime = 0;
						positionModifiedUpdate();
						if ( getAnimation() == animStoppedIddle ) 
							setAnimation( anim_default );
						for (Cannon cnd : cannons) 
							cnd.defaultCore.rotateLock = false;
					}
					super.gestureEnd();
				}
				@Override
				protected void gestureComplete( boolean clockwise ) {
					totalRotation = 0;
					if ( ( totalRotationDuringDizzyState >= 360 ) && Cannon.this.cndType.clockwise == clockwise ) 
						damage( damageOnRotationCompletion ); 
				}
			};
			
			setDefaultVirusCore( defaultCore );
			
		}
		
		@Override
		public void setAtlas() {
			
			AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
			
			if ( cndType != null ) {
				Debug.log( cndType + " to load now." );
				//Dizzy animation
				AtlasRegion[] normal = new AtlasRegion[2];
				normal[0] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion( cndType.pathName + "-dizzy1" );
				normal[1] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion( cndType.pathName + "-dizzy2" );
				createAnim(normal, null, .15f);
				animDizzy = getAnimation();
				//Gaussian animation
				normal = new AtlasRegion[2];
				normal[0] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion( cndType.pathName + "-gaussian1" );
				normal[1] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion( cndType.pathName + "-gaussian2" );
				createAnim(normal, null, .15f);
				animGauss = getAnimation();
				//Dizzy animation
				normal = new AtlasRegion[2];
				normal[0] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion( "dizzy1" );
				normal[1] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion( "dizzy2" );
				createAnim(normal, null, .15f);
				animPeppers = getAnimation();
				//shot animation
				normal = new AtlasRegion[4];
				normal[0] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion( cndType.pathName + "-charge1" );
				normal[1] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion( cndType.pathName + "-charge2" );
				normal[2] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion( cndType.pathName + "-charge3" );
				normal[3] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion( cndType.pathName + "-shot" );
				createAnim(normal, null, .15f);
				animShoot = getAnimation();
				//idle animation
				normal = new AtlasRegion[2];
				normal[0] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion( cndType.pathName + "-iddle1" );
				createAnim(new AtlasRegion[] { normal[0] }, null, .15f);
				animStoppedIddle = getAnimation();
				normal[1] = assetManager.get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion( cndType.pathName + "-iddle2" );		
				createAnim(normal, null, .15f);
//				spriteMask.setSize(xx, yy);
			}
		}
		
		@Override
		public void draw(SpriteBatch batch, float deltaTime) {
			if ( !isVisible ) 
				return;
			super.draw(batch, deltaTime);
			float angle = getAngle();
			if ( dizzy ) {
				if ( isBeingRotating && alu/*cndType.clockwise == clockwise && totalRotation > 10*/ ) {
					batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE);
					batch.draw(animGauss.getKeyFrame(elapsedTime), spriteCenter.x, spriteCenter.y, currentOriginCenterX, currentOriginCenterY,
							currentFrameWidth, currentFrameHeight, scaleX, scaleY, angle);
					batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); //default
				}
				if ( staticDizzy ) {
					float cnd_angle = angle + 90; // cannon angle correction
					cnd_angle += ( cnd_angle > 360f ) ? -360 : 0; 
					Rectangle sprite_bounds = spriteMask.getRectangle();
					float 
					r = (( cndType == CannonType.MYDOOM ) ? 86f : 71f),
					xx = ( sprite_bounds.getX() + currentOriginCenterX - 50f ) + r * MathUtils.cosDeg(cnd_angle), 
					yy = ( sprite_bounds.getY() + currentOriginCenterY - 37.5f ) + r * MathUtils.sinDeg(cnd_angle);
					batch.draw(animPeppers.getKeyFrame(elapsedTime), xx, yy, 50f, 37.5f, 100, 75, scaleX, scaleY, angle);
				}
			}
			/*batch.end();
			float angle = this.angle -90;
			angle += (angle < 0) ? 360f : 0;
			float 
			r = 73f,
			xx = (sprite_bounds.getX() + currentOriginCenterX) + r * MathUtils.cosDeg(angle),
			yy = (sprite_bounds.getY() + currentOriginCenterY) + r * MathUtils.sinDeg(angle);
			;
			shapeRender.begin(ShapeType.Filled);
			shapeRender.circle(xx, yy, 5f);
			shapeRender.end();
			batch.begin();*/
		}
		
		private boolean isBeingRotating, isOnSpot, shooting, staticDizzy;
		/**After be hit the boss rotate about 3-5 times until reach the dizzy state*/
		private float angleToAdd = 50f, angleTotalAdded;
		/**When was hit by a pepper rocket or bomb*/
		private boolean wasHit;
		private float dizzyTime, totalRotationDuringDizzyState;
		private final float dizzySleepLimit = 20f;
		
		@Override
		public void update(float deltaTime) {
			super.update(deltaTime);
			final float angle = getAngle();
			if ( shooting ) {
				if ( animShoot.isAnimationFinished( elapsedTime ) ) {
					if ( !dizzy ) 
						setAnimation( anim_default );
					isEjectedProject = false;
					shooting = false;
					ejectSmoke();
				} else {
					if ( !isEjectedProject && elapsedTime >= (animShoot.getAnimationDuration() * .8f ) ) {
						if ( state == STATE.FURY ) {
							// Triple shot on fury state
							ejectTripleProjectile();
						} else {
							ejectProjectile();
						}
					}
				}
			} else if ( !isBeingRotating ) {
				/*if ( resetPosition && (afterRotateSleepTime += deltaTime) > sleepOnAngleTime ) {
				}*/
				if ( !dizzy && (Math.abs( angle - lastValidAngle )) > 3f ) {
					if ( (afterRotateSleepTime += deltaTime) > sleepOnAngleTime ) {
						angleAdd( rotationResetSpd );
					}
				}
				if ( (timeToChangeValidAngle += deltaTime) > timeBwtChangeValidAngle ) {
					//Change the cannon direction between intervals
					float minA, minB, maxA, maxB;
					boolean leftSideOfStage = ( x  < (WORLD_WIDTH * .5f) );
					minA = leftSideOfStage ? 350f : 325f;
					minB = 360f;
					maxA = 0f;
					maxB = leftSideOfStage ? 35f : 20f;
					
					lastValidAngle = MyUtils.choose(MathUtils.random(minA, minB), MathUtils.random(maxA, maxB));
					positionModifiedUpdate();
					timeToChangeValidAngle = 0;
				}
				shootTimer += deltaTime;
			}
			//Hit by a pepper
			// Twist
			/*if ( wasHit ) {
				float torque = (angleToAdd * .5f);
				angleTotalAdded += torque;
				angleAdd( ( cndType.clockwise ) ? torque : -torque );
				rotateLock = true;
				if ( angleTotalAdded > 1080f && (angle > 330f || angle < 15f) ) {
					angleTotalAdded = 0;
					shootTimer = 0;
					staticDizzy = true;
					rotateLock = false;
					wasHit = false;
				}
			} else {
				if ( shootTimer > shootInvervalTime && canShoot && !dizzy ) 
					shoot();
			}*/
			//Dizzy time check
			if ( dizzy ) {
				// Dizzy
				if ( ( dizzyTime += deltaTime ) > dizzySleepLimit  ) {
					/// End dizzy state, return to normal state
					//Debug.log( "Dizzy ended: " );
					dizzy = false;
					wasHit = false;
					staticDizzy = false;
					timeToChangeValidAngle = 0f;
					dizzyTime = 0;
					setAnimation( anim_default );
					positionModifiedUpdate();
				}
				isOnSpot = false;
			} else {
				// Not Dizzy
				// Position on stage
				boolean horizontalPositionIsRight = true, verticalPositionIsRight = true;
				if ( verticalPosition != VPOS.OUT && Math.abs( (x - ((WORLD_WIDTH * .5f) + metaPosition.x)) ) > 3f ) {
					if ( x < ((WORLD_WIDTH * .5f) + metaPosition.x) ) {
						//Go right
						moveAdd((speed * deltaTime), 0);
					} else {
						//Go left
						moveAdd(-(speed * deltaTime), 0);
					}
					horizontalPositionIsRight = false;
				}
				if (  Math.abs( y - verticalPosition.yy ) > 3f ) {
					if ( verticalPosition.yy > y ) {
						//Go up
						moveAdd(0, (speed * deltaTime) );
					} else {
						//Go down
						moveAdd(0, -(speed * deltaTime));
					}
					verticalPositionIsRight = false;
				}
				isOnSpot = (horizontalPositionIsRight && verticalPositionIsRight);
//				Debug.log( cndType + " sub vertical" );
			}
//			if ( cndType == CannonType.MYDOOM )  Debug.log( "angle: " + angle );
			
			if ( sawCollisionOcurrenceTime > 0f ) {
				sawCollisionOcurrenceTime -= deltaTime;
			} else {
				if ( saw_collision_time > 0f ) 
					saw_collision_time = 0;
			}
			
			/*if ( isPressed && (timeRotation += Gdx.graphics.getDeltaTime() ) > timeToRotate ) {
				timeToChangeValidAngle = sleepOnAngleTime;
				rotateLock = true;
			}*/
			
//			Debug.log( cndType + " isOnSpot? " + isOnSpot);
		}
		
		/**Shoot animation*/
		private void shoot() {
			shootTimer = 0f;
			
			if ( dizzy || rockets.size > 1 || bombs.size > 1 ) 
				return; // stop shot action if is >> dizzy or already shoot 
			
			setAnimation( animShoot );
			elapsedTime = 0;
			shooting = true;
		}
		
		/**Determine if the project is already launched during the charge animation*/
		private boolean isEjectedProject;
		
		/**Launch the project*/
		private void ejectProjectile() {
			// SHOOT start
			boolean bool = !isUnique;
			float angle = getAngle() - 90f;
			angle += (angle < 0) ? 360 : 0;
			Rectangle sprite_bounds = spriteMask.getRectangle();
			/*Debug.log( "Rect x:" + sprite_bounds.getX() + " | y: " + sprite_bounds.getY() + 
					" | width: " + sprite_bounds.getWidth() + " | height: " + sprite_bounds.getHeight() );*/
			float
			r = 73f,
			xx = (sprite_bounds.getX() + currentOriginCenterX) + r * MathUtils.cosDeg(angle),
			yy = (sprite_bounds.getY() + currentOriginCenterY) + r * MathUtils.sinDeg(angle);
			if ( bool ) {
				PepperRocket rocket = pepperRocketPool.obtain();
				if ( rocket != null ) {
					rockets.add( rocket );
					rocket.reset();
					SpawnableConfiguration config = spawner.getSCS(.3f, .020f, 1f, .5f, Transition.GROW_IN_MOVE);
					spawner.addSpawnable( rocket, config, xx, yy);
					rocket.setEjector( Cannon.this.id );
					rocket.setAngleDirection( angle, getAngle() );
				}
			} else {
				PepperBomb bomb = pepperBombPool.obtain();
				if ( bomb != null ) {
					bombs.add(bomb);
					bomb.reset();
					SpawnableConfiguration config = spawner.getSCS(.3f, .020f, 1f, .5f, Transition.GROW_IN_MOVE);
					spawner.addSpawnable( bomb, config, xx, yy);
					bomb.setEjector( Cannon.this.id );
					bomb.setAngleDirection( angle, getAngle() );
				}
			}
			isEjectedProject = true;
			// SHOOT END
		}
		
		/**Eject three projectiles*/
		private void ejectTripleProjectile() {
			// SHOOT start
			boolean bool = !isUnique;
			float angleAperture = 25f;
			float[] dir = new float[3], angle = new float[3];
			float r = 73f;
			float xx , yy;
			//First projectile
			int a = 0;
			dir[a] = getAngle() - 90f;
			dir[a] += ( dir[a] < 0 ) ? 360f : 0;
			angle[a] = getAngle();
			Rectangle sprite_bounds = this.spriteMask.getRectangle();
			xx = (sprite_bounds.getX() + currentOriginCenterX) + r * MathUtils.cosDeg(dir[a]);
			yy = (sprite_bounds.getY() + currentOriginCenterY) + r * MathUtils.sinDeg(dir[a]);
			//Second projectile
			a++;
			dir[a] = ( dir[a-1] - angleAperture );
			angle[a] = ( angle[a-1] - angleAperture );
			dir[a] += ( dir[a] < 0 ) ? 360f : 0;
			angle[a] += ( angle[a] < 0 ) ? 360f : 0;
			//Third projectile
			a++;
			dir[a] = ( dir[a-1] + (2*angleAperture) );
			angle[a] = ( angle[a-1] + (2*angleAperture) );
			dir[a] += ( dir[a] > 360 ) ? -360f : 0;
			angle[a] += ( angle[a] > 360 ) ? -360f : 0;
			if ( bool ) {
				for (int i = 0; i < 3; i++) {
					PepperRocket rocket = pepperRocketPool.obtain();
					if ( rocket != null ) {
						rockets.add( rocket );
						rocket.reset();
						SpawnableConfiguration config = spawner.getSCS(.3f, .020f, 1f, .5f, Transition.GROW_IN_MOVE);
						spawner.addSpawnable( rocket, config, xx, yy);
						rocket.setEjector( Cannon.this.id );
						rocket.setAngleDirection(dir[i], angle[i]);
					}
					/*if ( cndType == CannonType.MYDOOM ) 
						Debug.log( "direction: " + dir[i] + " | angle: " + angle[i] );*/
				}
			} else {
				for (int i = 0; i < 3; i++) {
					PepperBomb bomb = pepperBombPool.obtain();
					if ( bomb != null ) {
						bombs.add( bomb );
						bomb.reset();
						SpawnableConfiguration config = spawner.getSCS(.3f, .020f, 1f, .5f, Transition.GROW_IN_MOVE);
						spawner.addSpawnable( bomb, config, xx, yy);
						bomb.setEjector( Cannon.this.id );
						bomb.setAngleDirection(dir[i], angle[i]);
					}
				}
			}
			isEjectedProject = true;
			// SHOOT END
		}
		
		private void ejectSmoke() {
			// Smoke effect
			Smoke smoke;
			float angle = getAngle() - 90f;
			angle += (angle < 0) ? 360 : 0;
			Rectangle sprite_bounds = spriteMask.getRectangle();
			float
			r = 80f,
			xx = (sprite_bounds.getX() + currentOriginCenterX) + r * MathUtils.cosDeg(angle),
			yy = (sprite_bounds.getY() + currentOriginCenterY) + r * MathUtils.sinDeg(angle);
			if ( (smoke = smokePool.obtain()) != null ) {
				SpawnableConfiguration config = spawner.getSCS(1f, .0320f, 2f, 2.5f, Transition.GROW_IN_MOVE);
				spawner.addSpawnable( smoke, config, xx, yy);
			}
		}
		
		//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		
		/**Time to rotate when not in dizzy state*/
		private float timeRotation;
		/**The limit time to rotate the twin boss before block rotation*/
		private final float timeToRotate = 3f;
		
		/**This method control the rotation, to return to origin angle*/
		private void positionModifiedUpdate() {
			float restAngle = (lastValidAngle-getAngle() + 360) % 360; 
			rotationResetSpd = ( restAngle < 180 ) ? autoRotationSpeed : -autoRotationSpeed;
			if ( cndType == CannonType.MYDOOM ) {
				//Debug.log( "Angle: " + angle );
				//Debug.log(cndType + " | last angle: " + angle + " | restAngle: " + restAngle + " | last valid angle: " + lastValidAngle);
			}
		}
		
		//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		
		// ====================================== //
		// =============  DAMAGES =============== //
		// ====================================== //
		
		@Override
		public void explosionDamage(BombItem bomb) {
			
			if ( bomb == null ) {
				//Debug.log( cndType + " is damaged by a pepper!" );
				
				// shoot cause damage
				if ( !wasHit && !dizzy ) {
					totalRotationDuringDizzyState = 0; //rotation incremented during dizzy state
					setAnimation( animDizzy ); // set animation to dizzy
					wasHit = true;
					dizzy = true;
				}
			}
			
		}
		
		protected float saw_collision_time = 0f, sawCollisionOcurrenceTime;
		
		@Override
		public boolean sawDamage(SawItem saw) {
			float deltaTime = Gdx.graphics.getDeltaTime();
			if ( (saw_collision_time += deltaTime) > 0.2f )
				saw.damageTime += deltaTime;
			sawCollisionOcurrenceTime = deltaTime * 3;
			damage( saw_damage );
			return false;
		}
		
		@Override
		public void laserDamage() {
			
		}
		
		@Override
		public boolean collisionDamage(DraggableVirus draggable) {
			
			return false;
		}
		
		private STATE state = STATE.PASSIVE;
		
		// damage inside cannon instance
		private void damage( float damage ) {
			
			if ( damage <= 0 )
				return;
			
			setFlashOn( Color.RED, 1f );
			health -= damage;
			if ( state == STATE.PASSIVE && health <= 35f ) // when life is low the boss get more aggressive
				state = STATE.FURY;
			 
			PepperBrosBoss.this.damage( damage );
			
			if ( health <= 0 ) { // this cannon is defeated
				destroy( true, true );
				// destroy the core boss if this is the last boss part
				/*if ( isUnique )
					PepperBrosBoss.this.destroy(true, true);*/
			}
			
			
		}
		
		// ====================================== //
		// ====================================== //
		
		@Override
		public void resetInPosition(float posX, float posY) {
			super.resetInPosition( (WORLD_WIDTH*.5f) + cndType.metaX, WORLD_HEIGHT);
		}
		
		/**update the position of sprite to draw correctly*/
		public void spriteCentralize() {
			super.spriteCentralize();
			//updates for current information
			currentOriginCenterX = 77f;
			currentOriginCenterY = 86f;
		}
		
		@Override
		public ScreenLogger getPointLog() {
			return null;
		}
		
		/**True if is the last stand on the stage*/
		private boolean isUnique;
		
		@Override
		public void destroy(boolean fatal, boolean hit) {
			super.destroy(fatal, hit);
			if ( !isReached ) {
				VirusManager.PART_EFFECTS.createEffect(Assets.Particles.explosion, x, y, 2f, 30);
			}
			
			for ( Cannon cnd : cannons ) {
				cnd.defaultCore.rotateLock = false;
				if ( cnd != this ) {
					cnd.isUnique = true;
					cnd.metaPosition.x = 0;
				}
			}
		}
		
	}
	
	private enum STATE { PASSIVE, FURY }
	
	@Override
	public void destroy(boolean fatal, boolean hit) {
		super.destroy(fatal, hit);
		if ( cannons[0].isReached || cannons[1].isReached ) {
			for ( Cannon cnd : cannons ) {
				if ( cnd.alive ) {
					cnd.isReached = true;
					cnd.destroy();
				}
			}
			if ( ACTIVE ) 
				LIFES -= LIFES;
		} 
		
		for ( PepperRocket rocket : rockets ) 
			rocket.removeFromStage();
		
		for ( PepperBomb bomb : bombs ) 
			bomb.removeFromStage();
		
		if ( hit ) {
			VIRUS_MANAGER.pointsManager.boss_log("Pepper Bros", 1, 5f, point, 
					VirusByteGame.ASSETS.getAssetManager().get(Assets.Atlas.pepperBrosAtlas.path, TextureAtlas.class).findRegion("combo-icon") );
			VIRUS_MANAGER.pointsManager.addDefeatedBossToList( getBossType() ); //add this boss to defeated boss list, for purpose of achievements
			//for debug purpose
			if ( BOSS_DEBUG ) 
				Debug.log("Times this boss has been defeated in this round: " + VIRUS_MANAGER.pointsManager.getBossTotalTimesDefeated( getBossType() ) );
		}
		
		// remove all Shockbots
		if ( HUD != null )
			HUD.lifebar_table.clear();
		
		background.terminate(); // deactivate especial background
		
		VirusByteGame.BACK.especialRandomBackground = true;
	}
}
