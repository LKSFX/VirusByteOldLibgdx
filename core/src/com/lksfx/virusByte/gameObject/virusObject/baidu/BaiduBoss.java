package com.lksfx.virusByte.gameObject.virusObject.baidu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.badlogic.gdx.utils.ArrayMap;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.BackgroundRenderer;
import com.lksfx.virusByte.effects.BackgroundRenderer.RendererConfig;
import com.lksfx.virusByte.effects.Backgrounds;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.BossType;
import com.lksfx.virusByte.gameObject.abstractTypes.DraggableVirus;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.gameObject.itemObject.BombItem;
import com.lksfx.virusByte.gameObject.itemObject.SawItem;
import com.lksfx.virusByte.gameObject.pontuation.ScreenLogger;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner.SpawnableConfiguration;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner.Transition;

public class BaiduBoss extends BossType {
	/** head animations **/
	private ArrayMap<String, Animation> head_iddle, head_blink, head_mouth, head_hurt;
	private Animation /*anim_armor, */anim_hand;
	private Spawner spawner;
	private static final boolean DEBUG = false;
	
//	private ShaderProgram shader;
	/** control the head movement in relation to the origin */
	public Vector2 relativePosition;
	/**determine the current direction movement*/
	private Direction vDirection;
	
	/**head details*/
	public enum Heads {
		CYAN("cyan-head", -100, 143, -18f, -17f), PINK("pink-head", 0, 143, 0f, -11f), GREEN("green-head", +100, 143, +18f, -17f);
		
		String name;
		float xx, yy, mouthCenterX, mouthCenterY;
		/**
		 * 
		 * @param name the name used to identify this head, like to arrayMap
		 * @param xx the horizontal position base of the head in relation to the center of the armor
		 * @param yy the vertical position base of the head in relation to the center of the armor
		 * @param mouthCenterX the horizontal center of the head mouth in relation of the head center 
		 * @param mouthCenterY the vertical center of the head mouth in relation of the head center
		 */
		private Heads(String name, int xx, int yy, float mouthCenterX, float mouthCenterY) {
			this.name = name;
			this.xx = xx;
			this.yy = yy;
			this.mouthCenterX = mouthCenterX;
			this.mouthCenterY = mouthCenterY;
		}
	}
	
	/**array with all the three heads */
	public Head[] heads;
	
	/**array with the two hands */
	private Hand[] hands;
	
	public BaiduBoss() {
		this(VIEWPORT.getWorldWidth()*.5f, 1280f);
	}
	
	public BaiduBoss(float x, float y) {
		this(x, y, 150, 80, 100, new Color( Color.WHITE ), false, 100f, Bosses.BAIDU);
	}
	
	public BaiduBoss(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn, float health, Bosses bossType) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn, health, bossType);
		virus_id = VirusInstance.Baidu;
		setDepth( 0 );
		bomb_damage = .1f;
		saw_damage = .5f;
		VirusByteGame.BACK.especialRandomBackground = false;
		VirusByteGame.BACK.finalizeAllBackEffects();
		
		setBossLifebar();
		vDirection = Direction.DOWN;
		relativePosition = new Vector2(0, 0);
		heads = new Head[3];
		heads[0] = new Head(0, 0, Heads.CYAN);
		heads[1] = new Head(0, 0, Heads.PINK);
		heads[2] = new Head(0, 0, Heads.GREEN);
		for (Head head : heads) VirusByteGame.VIRUS_MANAGER.addVirus(head.x, y, head);
		hands = new Hand[2];
		hands[0] = new Hand(0, 0, -140, false);
		hands[1] = new Hand(0, 0, +140, true);
		for (Hand hand : hands) VirusByteGame.VIRUS_MANAGER.addVirus(hand);
		
		spawner = new Spawner();
		shader = VirusByteGame.UTIL.flashShader;
		isCollidable = false;
		targetHeightOfTheScree = WORLD_HEIGHT-240; // initially the target point is the middle of the screen
		setInputController( new InputTriggers() {
			@Override
			public boolean justTouched(Vector3 mousePos) {
				if ( isOver(mousePos) ) {
					if ( DEBUG ) 
						Debug.log("clicked on baidu armort");
					setFlashOn(); //white effect
					return true;
				}
				return false;
			}
		} );
	}
	
	@Override
	public void resetInPosition(float posX, float posY) {
		
		super.resetInPosition(posX, posY);
		
		for (Head head : heads) 
			head.resetInPosition(posX+head.thisHead.xx, posY+head.thisHead.yy);
		
		for ( Hand hand : hands ) 
			hand.resetInPosition(posX+hand.origin_xx, posY+hand.origin_yy);
	};
	
	float eggSpawnInterval = 3f, eggSpawnTimer;
	int headSpawnPointer;
	/**true when the boss reach the right spot on scenery to stay*/
	boolean isInStandSpot;
	
	/**body armor relative movement speed**/
	int vMovementSpeed = 15;
	final int origin_yy = -240;
	float textColorSwitch;
	/**Time limit to stay on the screen*/
	private final float timeToStayOnScreen = 180f; // three minutes
	/**Time the Baidu is stopped*/
	private float timeStaying;
	/**The point of the screen to reach and stay*/
	private float targetHeightOfTheScree;
	
	@Override
	public void update(float deltaTime) {
		super.update( deltaTime );
		if ( alertOn ) {
			//remove the boss alert when
			textColorSwitch += deltaTime;
			if ( textColorSwitch > .1f ) {
				textColorSwitch = 0;
				warn_label.getStyle().fontColor = warn_label.getStyle().fontColor.equals(Color.BLACK) ? Color.WHITE : Color.BLACK;
			}
			if ( y < WORLD_HEIGHT ) {
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
		}
		spawner.update(deltaTime);
		//before reach on the stand spot
		if ( !isInStandSpot ) {
			if ( y < targetHeightOfTheScree ) {
				//reach the spot to stay on stage
				if ( y < 0 ) {
					//destroy, is out of the screen
					for ( Head head : heads )
						head.destroy(); // remove heads
					for ( Hand hand : hands )
						hand.destroy(); // remove hands 
					VirusByteGame.BACK.addForeground( Backgrounds.Effect.Damage, 1f, 5 ); // damage effect
					isReached = true;
					destroy();
				} else {
					isOnMove = false;
					isInStandSpot = true;
					setFlashOn();
				}
			}
		} else {
			// body armor movement
			if ( !defeated ) {
				float vMove = 0;
				switch ( vDirection ) {
				case UP:
					if ( relativePosition.y >= 15 ) vDirection = Direction.DOWN;
					vMove = vMovementSpeed;
					break;
				case DOWN:
					if ( relativePosition.y <= -5 ) vDirection = Direction.UP;
					vMove = -vMovementSpeed;
					break;
				default:
				}
				relativePosition.add(0, vMove * deltaTime);
				moveTo(x,  (WORLD_HEIGHT + origin_yy) + relativePosition.y );
			}
			
			//Continue at screen end
			if ( (timeStaying += deltaTime) > timeToStayOnScreen ) {
				isOnMove = true;
				isInStandSpot = false;
				targetHeightOfTheScree = -currentFrameHeight * 1.4f;
				Debug.log( "time to move! " + isOnMove );
			}
		}
		
		// the body parts have movement only until the boss is defeated
		if ( !defeated ) {
			// head follow the main 
			for (Head head : heads) head.moveTo(x + head.thisHead.xx + head.relativePosition.x, y + head.thisHead.yy + head.relativePosition.y);
			//hand movements
			for (Hand hand : hands) hand.moveTo(x + hand.origin_xx + hand.relativePosition.x, y + hand.origin_yy + hand.relativePosition.y);
		}
		
		if ( isInStandSpot && (eggSpawnTimer += deltaTime) >= eggSpawnInterval ) {
			if ( headSpawnPointer >=  heads.length ) headSpawnPointer = 0;
			heads[(headSpawnPointer++)].spawningStart();
			eggSpawnTimer = 0;
		}
		
		if ( (flsMainTime += deltaTime) > flsDuration ) {
			//after the last final damage flash animation the boss is destroyed 
			if ( defeated ) {
				for (Head head : heads) head.destroy(true, true);
				for (Hand hand : hands) hand.destroy(true, true);
				destroy(true, true);
			}
		}
	}
	
	/*------------------------------------------------*/
	
	/*------------------------------------------------*/
	
	/*
	private boolean flashing;
	private Color flashColor = new Color( Color.WHITE );
	private float flsTime, flsInterval= .1f, flashingDuration = 1.3f, flashingMainTime;*/
	/**is true when the boss is defeated**/
	private boolean defeated;
	
	private enum BossStage {stage1, stage2, stage3}
	private BossStage currentBossStage = BossStage.stage1;
	
	@Override
	public boolean sawDamage(SawItem saw) {
		return true;
	}
	
	@Override
	public void damage(float damage) {
		if ( defeated ) return;
		if ( damage > 0f ) setFlashOn( Color.RED, 1.3f );
		if (health > 0) {
			health -= damage;
			life_percent = (health / full_health) * 100;
			fill.setHeight(fill_height / 100 * life_percent);
			if ( ((.01f*dmLastMutation)-(.01*life_percent)) > (dmInterval) ) {
				if (background != null) background.setTextureBrowser(0f, background.scrollSpeedV+=.02f);
				dmLastMutation = life_percent;
			}
			switch ( currentBossStage ) {
			case stage1:
				if ( health < full_health/3 * 2 ) {
					currentBossStage = BossStage.stage2;
					eggSpawnInterval = 2f;
				}
				break;
			case stage2:
				if ( health < full_health/3 ) {
					currentBossStage = BossStage.stage3;
					eggSpawnInterval = 1f;
				}
				break;
			default:
				break;
			}
			Debug.log("health> " + health + " | lifePercent> " + life_percent);
		} else {
				defeated = true; //turns the boss defeated
				flsMainTime = 0; //reset flash timer 
				flsDuration = 2f;
				setFlashOn( Color.RED, 1.3f );
				Debug.log("total eggs removed from stage: " + VirusByteGame.VIRUS_MANAGER.removeAllObjectsOfType( heads[0].eggs[0] ) );
				//set hurting animation for the heads
				for ( Head head : heads ) head.hurtStart();
		}
	}
	
	@Override
	public void setFlashOn(Color color, float duration) {
		flsColor.set(color.r, color.g, color.b, spriteColor.a );
		flsMainTime = 0;
		flashing = true;
		for (int i = 0; i < heads.length; i++) heads[i].setFlashOn( color, 1.3f );
		for (int i = 0; i < hands.length; i++) hands[i].setFlashOn( color, 1.3f );
	}
	
	public void setBossLifebar() {
		
		AssetManager manager = VirusByteGame.ASSETS.getAssetManager();
		
		HUD.lifebar_table.clearChildren();
		Image bar = new Image( manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("baidu-lifebar") );
		fill = new Image( manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("lifebar-fill") );
		fill_height = fill.getHeight();
		Stack stack = new Stack();
		Table fill_table = new Table();
		fill_table.add(fill).padBottom(47);
		stack.add(fill_table);
		stack.add(bar);
		face = new ImageButton( new TextureRegionDrawable( manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("baidu-head") ) );
		Container<ImageButton> container = new Container<ImageButton>(face).bottom();
		stack.add(container);
		
		HUD.lifebar_table.add(stack).pad(3f);
		
		// === make the warning table to show on main hud === //
		Table warning_table = new Table();
		warn_label = new Label("boss time!", HUD.skin, "visitor32-bold", Color.BLACK);
		warning_table.add( warn_label ).expand().bottom();
		HUD.addBossAlert( warning_table ); //show an alert on the screen: boss is coming
		// === === //
		alertOn = true;
		if ( DEBUG ) Debug.log("info: " + face.getOriginY() );
	}
	
	/// ==================================== ///
	/// ==============  HEAD  ============== ///
	/// ==================================== ///
	
	public class Head extends BossType {
		public Heads thisHead;
		/** control the head movement in relation to the origin */
		public Vector2 relativePosition;
		private BaiduEgg[] eggs;
		/**determine the current direction movement*/
		private Direction vDirection;
		
		public Head(float x, float y, Heads head) {
			super(x, y);
			this.thisHead = head;
			initialize(x, y, 0, 0, 0, null, false);
			relativePosition = new Vector2(0, 0);
			timeToBlink = blinkMinimumInterval+MathUtils.random(blinkRandomIntervalRange); //set a random value to the next blink animation time
			collision_damage = 25f;
			laser_damage = .05f;
			bomb_damage = .3f;
			eggs = new BaiduEgg[2];
			for (int i = 0; i < eggs.length; i++) {
				eggs[i] = new BaiduEgg(0, 0, thisHead) {
					@Override
					public void bottom_bounce() {}
					
					@Override
					public ScreenLogger getPointLog() {
						SpawnableConfiguration config = spawner.getSCS_basic3(.05f, .2f, Transition.GROW_IN_STOPPED_TO_MOVE);
						VirusType spawn = getEggContent();
						if ( spawn != null ) spawner.addSpawnable(spawn, config, x, y);
						return null;
					}
				};
			}
			vDirection = Direction.UP;
			spriteColor = BaiduBoss.this.spriteColor;
			isOnMove = false;
			setInputController( new InputTriggers() {
				@Override
				public boolean justTouched( Vector3 mousePos ) {
					if ( isOver(mousePos) ) {
						if ( DEBUG ) 
							Debug.log("clicked on baidu " + thisHead.name);
						setFlashOn(); //white effect
						return true;
					}
					return false;
				}
			} );
		}
		
		int vMovementSpeed = 20;
		float 
		/** used to control blink animation */
		timeToBlink, blinkIntervalTime, blinkRandomIntervalRange = 4f, blinkMinimumInterval = 3f,
		/** used to control mouth animation*/
		openMouthTime, openMouthLimitTime = 1.2f,
		/**used to control hurt animation*/
		hurtTime, hurtLimitTime = .9f;
		boolean blinking, openMouth, hurting;
		
		@Override
		public void update(float deltaTime) {
			super.update(deltaTime);
			if ( blinking ) {
				if ( getAnimation().isAnimationFinished(elapsedTime) ) blinkStop();
			} else {
				if ( (blinkIntervalTime += deltaTime) >= timeToBlink ) blinkStart();  
			}
			if ( openMouth ) {
				if ( (openMouthTime += deltaTime) > openMouthLimitTime ) spawningStop();
			}
			if ( hurting ) {
				if ( (hurtTime += deltaTime) > hurtLimitTime && !defeated ) hurtStop();
			}
			// head movement
			float vMove = 0;
			switch ( vDirection ) {
			case UP:
				if ( relativePosition.y >= 15 ) vDirection = Direction.DOWN;
				vMove = vMovementSpeed;
				break;
			case DOWN:
				if ( relativePosition.y <= -5 ) vDirection = Direction.UP;
				vMove = -vMovementSpeed;
				break;
			default:
			}
			relativePosition.add(0, vMove * deltaTime);
			
			/*int lastVerticalDirection = vMovementSpeed;
			vMovementSpeed = (relativePosition.y >= 15 || relativePosition.y <= -5) ? -vMovementSpeed : vMovementSpeed; 
			float 
			vMove = deltaTime * ( vMovementSpeed * ( (lastVerticalDirection == vMovementSpeed) ? 1 : 2 ) );
			relativePosition.add(0, vMove);*/
		}
		
		/*------------------------------------------------*/
		
		/*------------------------------------------------*/
		
		/**start blinking animation */
		private void blinkStart() {
			if ( defeated ) return;
			if ( openMouth ) {
				timeToBlink = blinkMinimumInterval+MathUtils.random(blinkRandomIntervalRange); //set a random value to the next blink animation time
				return;
			}
			setAnimation( head_blink.get(thisHead.name) );
			if ( DEBUG ) Debug.log("start blink animation on " + thisHead.name);
			elapsedTime = 0;
			blinking = true;
		}
		
		/**stop blinking animation and change animation to idle */
		private void blinkStop() {
			setAnimation( anim_default );
			timeToBlink = blinkMinimumInterval+MathUtils.random(blinkRandomIntervalRange); //set a random value to the next blink animation time
			blinkIntervalTime = 0; //reset time to zero
			blinking = false;
		}
		
		/**spawning egg, open mouth animation */
		public void spawningStart() {
			if ( defeated ) return;
			if ( openMouth ) return;
			BaiduEgg egg;
			// Check if an egg is available to spawn
			if ( (egg = getFreeEgg()) == null ) return; 
			if ( blinking ) blinkStop(); //if blinking, stop blinking
			if ( hurting ) hurtStop();
			//Debug.log("relative mouth position of " + thisHead.name + " is x: " + thisHead.mouthCenterX + " and y: " + thisHead.mouthCenterY);
			egg.reset();
			SpawnableConfiguration config = spawner.getSCS(0f, .020f, 1f, .2f, Transition.GROW_IN_STOPPED_TO_MOVE);
			spawner.addSpawnable( egg, config, thisHead.mouthCenterX, thisHead.mouthCenterY, getPosition());
			setAnimation( head_mouth.get(thisHead.name) );
			openMouth = true;
		}
		
		private void spawningStop() {
			setAnimation( anim_default );
			openMouthTime = 0; //reset time to zero
			openMouth = false;
		}
		
		/**when hit, hurt animation start*/
		public void hurtStart() {
			if ( openMouth ) spawningStop();
			if ( blinking ) blinkStop();
			setAnimation( head_hurt.get(thisHead.name) );
			hurting = true;
		}
		
		public void hurtStop() {
			setAnimation( anim_default );
			hurtTime = 0;
			hurting = false;
		}
		
		/**Return a free egg if available, else return null*/
		private BaiduEgg getFreeEgg() {
			for (BaiduEgg egg : eggs) {
				if (!egg.isInUse) return egg;
			}
			return null;
		}
		
		/**enable the flash effect on the boss*/
		public void setFlashOn() {
			BaiduBoss.this.setFlashOn( Color.WHITE, 1.3f );
		}
		
		/**this permits evaluate the damage based with the power of the dart push*/
		private final float baseDamageMultiplicator = .1295f;
		
		@Override
		public boolean collisionDamage(DraggableVirus draggable) {
			if ( draggable instanceof BaiduDart ) {
				BaiduDart dart = (BaiduDart) draggable;
				//Debug.log("movement push of dart on collision was " + dart.getPushPower() * baseDamageMultiplicator);
				if ( thisHead == dart.getHeadColorType()) {
					damage( dart.getPushPower() * baseDamageMultiplicator );
				} else {
					setFlashOn();
				}
			}
			return true;
		}
		
		@Override
		public boolean sawDamage(SawItem saw) {
			saw_damage = (saw_collision_time == 0) ? 3f : .1f;
			return super.sawDamage(saw);
		}
		
		@Override
		public void explosionDamage(BombItem bomb) {
			float distanceToBomb = Vector2.dst(x, y, bomb.x, bomb.y);
			Debug.log( "distance of " + thisHead.name + " ToBomb " + distanceToBomb  );
			if ( distanceToBomb > 100f ) return;
			damage( bomb_damage );
		}
		
		@Override
		public void damage(float damage) {
			hurtStart();
			BaiduBoss.this.damage(damage);
		}
		
		@Override
		public void setAtlas() {
			if (thisHead != null) createAnim(head_iddle.get(thisHead.name).getKeyFrames(), null, new Vector2(74, 54));
		}
		
		@Override
		public ScreenLogger getPointLog() {
			return null;
		}
	}
	
	/// ==================================== ///
	/// ==================================== ///
	
	public VirusType getEggContent() {
		VirusType obj;
		VirusManager manager = VirusByteGame.VIRUS_MANAGER;
		int randomInteger = MathUtils.random(1, 100);
		// >>>>>>>>>>>>
		if ( currentBossStage == BossStage.stage3 ) {
				obj = new BaiduDart();
		} else {
			if ( randomInteger <= 35 ) { // 35% change of virus
				//return a virus
				int random = MathUtils.random(3);
				switch ( random ) {
				case 0:
					obj = manager.obtainVirus( VirusInstance.Plague, true );
					break;
				case 1:
					obj = manager.obtainVirus( VirusInstance.Psycho, true );
					break;
				case 2:
					obj = manager.obtainVirus( VirusInstance.Energyzer, true );
					break;
				case 3:
					obj = manager.obtainVirus( VirusInstance.Worm, true );
					break;
				default:
					obj = manager.obtainVirus( VirusInstance.Worm, true );
					break;
				}
				
			} else if ( randomInteger <= 95 ) { // 50% change of dart
				//return a dart
				obj = new BaiduDart();
			} /*else if ( randomInteger <= 95 ) { // 10% change of bomb
				//return a bomb
				obj = manager.obtainVirus( VirusInstance.Bomb, true );
			}*/ else { // 5% change of life
				//return a battery
				obj = manager.obtainVirus( VirusInstance.Life, true );
			}
		}
		// <<<<<<<<<
		return obj;
	}
	
	/// ==================================== ///
	/// ==============  HAND  ============== ///
	/// ==================================== ///
	
	public class Hand extends BossType {
		public final int origin_yy = -33;
		private int origin_xx;
		/** control the hand movement in relation to the origin */
		public Vector2 relativePosition;
		
		/**determine the current direction movement*/
		private Direction hDirection, vDirection;
		
		public Hand(float x, float y, int xx, boolean invert) {
			super(x, y);
			if ( invert ) setInvertedAtlas();
			origin_xx = xx;
			relativePosition = new Vector2(0, 0);
			spriteColor = BaiduBoss.this.spriteColor;
			vDirection = Direction.UP;
			hDirection = Direction.LEFT;
			isOnMove = false;
			setInputController( new InputTriggers() {
				@Override
				public boolean justTouched(Vector3 mousePos) {
					if ( isOver(mousePos) ) {
						//Damage when touch on hands
						if ( DEBUG ) Debug.log("Clicked on baidu hand");
						VirusByteGame.BACK.addForeground(Backgrounds.Effect.Damage, 1f, 5);
						if ( ACTIVE ) {
							if ( VirusByteGame.SFX ) {
								// audio TODO
							}
							LIFES--;
						}
						return true;
					}
					return false;
				}
			} );
		}
		
		public int hMovementSpeed = 50, vMovementSpeed = 20;
		
		@Override
		public void update(float deltaTime) {
			super.update(deltaTime);
			
			//hand movement
			float hMove = 0, vMove = 0,
			leftLimiter = ( origin_xx < 0 )? -100 : -75, 
			rightLimiter = ( origin_xx < 0 )? 75 : 100;
			
			switch ( hDirection ) {
			case LEFT:
				if ( relativePosition.x <= leftLimiter ) hDirection = Direction.RIGHT;
				hMove = -hMovementSpeed;
				break;
			case RIGHT:
				if ( relativePosition.x >= rightLimiter ) hDirection = Direction.LEFT;
				hMove = hMovementSpeed;
				break;
			default:
			}
			
			switch ( vDirection ) {
			case UP:
				if ( relativePosition.y >= 25 ) vDirection = Direction.DOWN;
				vMove = vMovementSpeed;
				break;
			case DOWN:
				if ( relativePosition.y <= -5 ) vDirection = Direction.UP;
				vMove = -vMovementSpeed;
				break;
			default:
			}
			relativePosition.add(hMove * deltaTime, vMove * deltaTime);
			
		}
		
		/*------------------------------------------------*/
		
		/*------------------------------------------------*/
		
		@Override
		public void damage(float damage) {}

		@Override
		public void setAtlas() {
			createAnim(anim_hand.getKeyFrames(), null, new Vector2(44, 44));
		}
		
		private void setInvertedAtlas() {
			TextureRegion[] regions = new TextureRegion[anim_default.getKeyFrames().length];
			for (int i = 0; i < regions.length; i++) regions[i] = new TextureRegion( anim_default.getKeyFrames()[i] );
			for (TextureRegion region : regions) region.flip(true, false);
			createAnim(regions, null, new Vector2(44, 44));
		}
		
		@Override
		public ScreenLogger getPointLog() {
			return null;
		}
	}
	
	/// ==================================== ///
	/// ==================================== ///
	
	@Override
	public void setAtlas() {
		
		AssetManager manager = VirusByteGame.ASSETS.getAssetManager();
		
		// Head Blink
		AtlasRegion[] normal = new AtlasRegion[3];
		head_blink = new ArrayMap<String, Animation>();
		//cyan head blink
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("cyan-head-blink1");
		normal[1] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("cyan-head-blink2");		
		normal[2] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("cyan-head-blink1");
		createAnim( normal, null, .15f );
		head_blink.put( Heads.CYAN.name, anim_default );
		//pink head blink
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("pink-head-blink1");
		normal[1] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("pink-head-blink2");
		normal[2] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("pink-head-blink1");
		createAnim( normal, null, .15f );
		head_blink.put( Heads.PINK.name, anim_default );
		//green head blink
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("green-head-blink1");
		normal[1] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("green-head-blink2");
		normal[2] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("green-head-blink1");
		createAnim( normal, null, .15f );
		head_blink.put(Heads.GREEN.name, anim_default);
		
		for ( Animation anim : head_blink.values() ) 
			anim.setPlayMode( PlayMode.LOOP );
		
		
		// Head Open Mouth
		normal = new AtlasRegion[2];
		head_mouth = new ArrayMap<String, Animation>();
		//cyan head open mouth
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("cyan-head-open-mouth1");
		normal[1] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("cyan-head-open-mouth2");
		createAnim( normal, null );
		head_mouth.put(Heads.CYAN.name, anim_default);
		//pink head open mouth
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("pink-head-open-mouth1");
		normal[1] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("pink-head-open-mouth2");
		createAnim( normal, null );
		head_mouth.put(Heads.PINK.name, anim_default);
		//green head open mouth
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("green-head-open-mouth1");
		normal[1] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("green-head-open-mouth2");
		createAnim( normal, null );
		head_mouth.put(Heads.GREEN.name, anim_default);
		
		
		// Head Idle
		head_iddle = new ArrayMap<String, Animation>();
		//cyan head open mouth
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("cyan-head-iddle1");
		normal[1] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("cyan-head-iddle2");
		createAnim(normal, null);
		head_iddle.put(Heads.CYAN.name, anim_default);
		//pink head open mouth
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("pink-head-iddle1");
		normal[1] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("pink-head-iddle2");
		createAnim(normal, null);
		head_iddle.put(Heads.PINK.name, anim_default);
		//green head open mouth
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("green-head-iddle1");
		normal[1] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("green-head-iddle2");
		createAnim(normal, null);
		head_iddle.put(Heads.GREEN.name, anim_default);
		
		
		// Head Hurt
		normal = new AtlasRegion[1];
		head_hurt = new ArrayMap<String, Animation>();
		//cyan head hurt
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("cyan-head-hurt");
		createAnim(normal, null);
		head_hurt.put(Heads.CYAN.name, anim_default);
		//pink head hurt
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("pink-head-hurt");
		createAnim(normal, null);
		head_hurt.put(Heads.PINK.name, anim_default);
		//green head hurt
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("green-head-hurt");
		createAnim(normal, null);
		head_hurt.put( Heads.GREEN.name, anim_default );
		
		
		// Hands
		normal = new AtlasRegion[3];
		//left hand animation
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("hand1");
		normal[1] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("hand2");
		normal[2] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("hand3");
		createAnim(normal, null);
		anim_hand = anim_default;
		
		
		// Armor 
		//armor animation
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("armor1");
		normal[1] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("armor2");
		normal[2] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("armor3");
		createAnim(normal, null, new Vector2(180, 200) );
		//anim_armor = anim_default;
	}
	
	@Override
	public void destroy(boolean fatal, boolean hit) {
		super.destroy(fatal, hit);
		//when this boss reach the bottom screen you lose all lives
		if ( isReached ) {
			if ( ACTIVE ) LIFES -= LIFES;
		}
		
		if ( hit ) {
			if ( VirusByteGame.SFX ) {
				// AUDIO TODO 
			}
			VirusManager.PART_EFFECTS.createEffect(Assets.Particles.explosion, x, y, 2f, 30);
			
			VIRUS_MANAGER.pointsManager.boss_log( "Baidu", 1, 5f, point,
					VirusByteGame.ASSETS.getAssetManager().get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion("combo-icon") );
			VIRUS_MANAGER.pointsManager.addDefeatedBossToList( getBossType() ); //add this boss to defeated boss list, for purpose of achievements
			//for debug purpose
			if ( BOSS_DEBUG ) Debug.log("Times this boss has been defeated in this round: " + VIRUS_MANAGER.pointsManager.getBossTotalTimesDefeated( getBossType() ) );
		}
		
		HUD.lifebar_table.clear();
		VirusByteGame.BACK.especialRandomBackground = true;
		background.finalize(); // deactivate especial background
	}
	
}
