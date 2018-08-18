package com.lksfx.virusByte.gameObject.virusObject.octocat;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
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
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.BackgroundRenderer;
import com.lksfx.virusByte.effects.BackgroundRenderer.RendererConfig;
import com.lksfx.virusByte.effects.Backgrounds;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.MyUtils;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.BossType;
import com.lksfx.virusByte.gameObject.itemObject.SawItem;

public class OctocatBoss extends BossType {
	private Animation anim_health_good, anim_health_medium, anim_health_low;
	private boolean spawn_bots;
	private float max_inactive_time = 2.5f, inactive_time = 0;
	
	public OctocatBoss() {
		this(VIEWPORT.getWorldWidth()*.5f, 1280f);
	}
	
	public OctocatBoss(float x, float y) {
		this(x, y, 150, 80, 100, new Color( Color.WHITE ), false, 150f, Bosses.OCTOCAT);
	}
	
	public OctocatBoss(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn, float health, Bosses bossType) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn, health, bossType);
		setDepth( 0 );
		virus_id = VirusInstance.Octocat;
		bomb_damage = 1f;
		saw_damage = 5f;
		collision_damage = 10f;
		VirusByteGame.BACK.especialRandomBackground = false;
		//finalize the current especial background active 
		VirusByteGame.BACK.finalizeAllBackEffects();
		
		setBossLifebar();
		//move = false;
		setInputController( new InputTriggers() {
			@Override
			public boolean justTouched(Vector3 mousePos) {
				if ( isOver(mousePos) ) {
					Debug.log("clicked on octocat " + life_percent);
					damage( MyUtils.choose(5f, 3f, 2f) );
					return true;
				}
				return false;
			}
		} );
	}
	
	public void resetInPosition() {
		super.resetInPosition(WORLD_WIDTH*.5f, 1280f);
		Debug.log( "method resetInPosition set default" );
		inactive_time = max_inactive_time;
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.octocatAtlas.path, TextureAtlas.class).findRegion("octocat1-fase1");
		normal[1] = assetManager.get(Assets.Atlas.octocatAtlas.path, TextureAtlas.class).findRegion("octocat2-fase1");		
		
		createAnim(normal, null);
		anim_health_good = anim_default;
		
		normal[0] = assetManager.get(Assets.Atlas.octocatAtlas.path, TextureAtlas.class).findRegion("octocat1-fase2");
		normal[1] = assetManager.get(Assets.Atlas.octocatAtlas.path, TextureAtlas.class).findRegion("octocat2-fase2");
		
		createAnim(normal, null);
		anim_health_medium = anim_default;
		
		normal[0] = assetManager.get(Assets.Atlas.octocatAtlas.path, TextureAtlas.class).findRegion("octocat1-fase3");
		normal[1] = assetManager.get(Assets.Atlas.octocatAtlas.path, TextureAtlas.class).findRegion("octocat2-fase3");
		
		createAnim(normal, null);
		anim_health_low = anim_default;
		setCollisionMaskBounds(1f, 22f, 64f, 64f);
		setTouchMaskBounds(1f, 22f, 64f, 64f);
		
		setAnimation( anim_health_good );
	}
	
	int head_clicks = 0;
	
	public void setBossLifebar() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		HUD.lifebar_table.clearChildren();
		Image bar = new Image( assetManager.get(Assets.Atlas.octocatAtlas.path, TextureAtlas.class).findRegion("octocat-lifebar") );
		fill = new Image( assetManager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("lifebar-fill") );
		fill_height = fill.getHeight();
		Stack stack = new Stack();
		Table fill_table = new Table();
		fill_table.add( fill ).padBottom(47);
		stack.add(fill_table);
		stack.add(bar);
		face = new ImageButton( new TextureRegionDrawable( assetManager.get(Assets.Atlas.octocatAtlas.path, TextureAtlas.class).findRegion("normal-cat-head") ) );
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
	
	//private float saw_collision_time = 0f;
	/**Time to stop on the center of the screen*/
	private float timeToStayStopped = 90f;
	private float textColorSwitch;
	
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (alertOn) {
			//remove the boss alert when
			textColorSwitch += deltaTime;
			if (textColorSwitch > .1f) {
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
		if ( face.isChecked() && head_clicks++ < 10 ) {
			Debug.log("clicked on the cat head! " + head_clicks + " times");
			if ( head_clicks == 10 ) {
				// Show hidden background
				face.getStyle().imageUp = new TextureRegionDrawable( VirusByteGame.ASSETS.getAssetManager()
						.get(Assets.Atlas.octocatAtlas.path, TextureAtlas.class).findRegion("hell-cat-head") ); 
				VirusByteGame.BACK.addForeground(Backgrounds.Effect.Damage, .5f, 15);
				VirusByteGame.BACK.addBackground(hideBack = new BackgroundRenderer( new String[] {Assets.Textures.Hidden1Tex.path}, 
						new Vector2(), new Vector2(), 14, true, true) );
				hideBack.setAlphaAttribute(MathUtils.random(.4f, .1f), .5f);
				hideBack.setSize(WORLD_WIDTH, WORLD_HEIGHT);
			}
			face.setChecked(false);
		}
		// Determine if move or stop
		switch ( state ) {
		case GOOD:
			if ( y <= VIEWPORT.getWorldHeight()*.5f ) {
				//stop on the center of the screen
				isOnMove = false;
			}
			break;
		case MODERATE:
			//if is moving stop
			if ( isOnMove ) isOnMove = false;
			break;
		case BAD:
			// if not moving move again, slow down speed
			if ( !isOnMove ) {
				isOnMove = true;
				setSpeed(45, 65);
			}
			break;
		}
		// When stopped
		if ( !isOnMove ) if (inactive_time > ( timeToStayStopped += deltaTime )) isOnMove = true;
		// Destroy on reach on bottom screen
		if ( (y+currentFrameHeight*.5f) < 0) {
			isReached = true;
			VirusByteGame.BACK.addForeground(Backgrounds.Effect.Damage, 1f, 5);
			destroy();
		}
	}
	
	/*------------------------------------------------*/
	
	/*------------------------------------------------*/
	
	@Override
	public boolean sawDamage(SawItem saw) {
		saw_damage = ( saw_collision_time == 0f ) ? 5f : .3f;
		return super.sawDamage(saw);
	}
	
	@Override
	public void laserDamage() { //damage by second
		damage(.1f);
		Debug.log("Octocat has take damage from laser source");
	}
	
	/**States this boss can be*/
	private enum HealthState {GOOD, MODERATE, BAD} 
	/**The current state this boss is*/
	private HealthState state = HealthState.GOOD;
	
	@Override
	public void damage(float damage) {
		if (health > 0) {
			setFlashOn( Color.RED, 1f );
			health -= damage;
			life_percent = (health / full_health) * 100;
			fill.setHeight( (fill_height / 100) * life_percent );
			if ( ((.01f*dmLastMutation)-(.01*life_percent)) > (dmInterval) ) {
				if (background != null) background.setTextureBrowser(0f, background.scrollSpeedV+=.02f);
				dmLastMutation = life_percent;
			}
		} else {
			destroy(true, true);
		}
		
		if (state == HealthState.GOOD && life_percent < 66f) {
			VIRUS_MANAGER.addVirus(x, y, VirusInstance.Bug);
			state = HealthState.MODERATE;
			setAnimation( anim_health_medium );
		}
		if (state == HealthState.MODERATE && life_percent < 33f) {
			state = HealthState.BAD;
			setAnimation( anim_health_low );
		}
		
		//Only spawn if the total of Shockbots on stage is less than ...
		spawn_bots = ( VIRUS_MANAGER.getTotalInstances( VirusManager.VirusInstance.Shockbot ) > 10 );
		if ( !spawn_bots && MathUtils.randomBoolean(0.2f) ) {
			for (int i = MathUtils.random(1, 3); i > 0; i--) VIRUS_MANAGER.addVirus(x, y, VirusInstance.Shockbot);
		}
		BACK.setBackColor();
	}
	
	/*float last_life_percent = 100f;
	
	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		if (last_life_percent > life_percent) spriteColor.set(1f, 0f, 0f, spriteColor.a);
		super.draw(batch, deltaTime);
		spriteColor.set(1f, 1f, 1f, spriteColor.a);
		last_life_percent = life_percent;
	}*/
	
	/*@Override
	public void sprite_center() {
		currentFrame = anim.getKeyFrame(elapsedTime);
		
		//updates for current information
		currentFrameWidth = currentFrame.getRegionWidth();
		currentFrameHeight = currentFrame.getRegionHeight();
		
		float xx = (sprite_bounds.getX()+(sprite_bounds.width*.5f))-(anim.getKeyFrame(elapsedTime).getRegionWidth()*.48f),
				yy = sprite_bounds.getY()+(sprite_bounds.height*.5f)-(anim.getKeyFrame(elapsedTime).getRegionHeight()*.7f);
		spriteCenter.set(xx, yy);
		
		//updates for current information
		currentOriginCenterX = currentFrameWidth*.5f;
		currentOriginCenterY = currentFrameHeight*.5f;
	}*/
	
	@Override
	public void destroy(boolean fatal, boolean hit) {
		super.destroy(fatal, hit);
		//when this boss reach the bottom screen you lose all lives
		if ( isReached ) {
			if ( ACTIVE ) LIFES -= LIFES;
		} 
		
		if ( hit ) {
			if ( VirusByteGame.SFX ) {
				// audio TODO
			}
			VirusManager.PART_EFFECTS.createEffect(Assets.Particles.explosion, x, y, 2f, 30);
			
			VIRUS_MANAGER.pointsManager.boss_log("Octocat", 1, 5f, point, 
					VirusByteGame.ASSETS.getAssetManager().get(Assets.Atlas.octocatAtlas.path, TextureAtlas.class).findRegion("combo-icon") );
			VIRUS_MANAGER.pointsManager.addDefeatedBossToList( getBossType() ); //add this boss to defeated boss list, for purpose of achievements
			//for debug purpose
			if ( BOSS_DEBUG ) Debug.log("Times this boss has been defeated in this round: " + VIRUS_MANAGER.pointsManager.getBossTotalTimesDefeated( getBossType() ) );
		}
		// remove all Shockbots
		HUD.lifebar_table.clear();
		VirusByteGame.BACK.especialRandomBackground = true;
		background.terminate(); // deactivate especial background
		if ( hideBack != null ) 
			hideBack.finalize();
	}
	
	/*
	private void removeShockbots() {
		Array<VirusType> bots = VirusManager.VirusInstance.Shockbot.array;
		for (int i = 0; i < bots.size; i++) {
			ShockBotVirus bot = (ShockBotVirus) bots.get(i);
			bot.destroy(true, false, true);
		}
	}*/
}
