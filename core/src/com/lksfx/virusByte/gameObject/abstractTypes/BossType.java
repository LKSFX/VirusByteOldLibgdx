package com.lksfx.virusByte.gameObject.abstractTypes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.BackgroundRenderer;
import com.lksfx.virusByte.gameObject.itemObject.BombItem;
import com.lksfx.virusByte.gameObject.itemObject.SawItem;
import com.lksfx.virusByte.screens.ChallengeGameMode;

public abstract class BossType extends VirusType {
	public float full_health = 150f, health = full_health, life_percent = 100f, bomb_damage, saw_damage, collision_damage, laser_damage;
	protected static final boolean BOSS_DEBUG = false;
	/** When the warning message has on screen this variable is true */
	protected boolean alertOn;
	/** each boss has a type that specify, this is useful to identify the boss for the achievements   **/
	public enum Bosses { OCTOCAT, BAIDU, PEPPERBOSS, DRAGONBOSS }
	
	protected Image fill;
	protected ImageButton face;
	protected float fill_height;
	protected Label warn_label;
	protected BackgroundRenderer background, hideBack;
	
	/**this enumerator has values to identify where direction the movement is going*/
	protected enum Direction {RIGHT, LEFT, UP, DOWN};
	
	/** what type this boss is */
	private Bosses boss_type;
	
	public BossType(float x, float y) {
		this(x, y, 100, 80, 120, new Color( Color.WHITE ), true, 200f, null);
	}
	
	public BossType(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn, float health, Bosses bossType) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		finalEffectType = null;
		repositionable = false; //avoid undesired repositioning
		dmLastMutation = full_health;
		dmInterval = .05f;
		full_health = health;
		this.health = health;
		boss_type = bossType; // declare this boss type
		canBeFree = false; // the boss is not a pooled virus, can't be freed
		BOSS_TIME = true;
	}
	
	@Override
	public void update(float deltaTime) {
		movementUpdate( deltaTime );
		spriteUpdate( deltaTime );
		updateTrail(); // update trail
		
		if ( sawCollisionOcurrenceTime > 0f ) {
			sawCollisionOcurrenceTime -= deltaTime;
		} else {
			if ( saw_collision_time > 0f ) saw_collision_time = 0;
		}
		
		if ( (flsMainTime += deltaTime) > flsDuration ) {
			// End of the flashing time
			flashing = false;
			flsMainTime = 0;
		}
	}
	
	@Override
	public void laserDamage() {
		damage(laser_damage);
	}
	
	@Override
	public void explosionDamage(BombItem bomb) {
		damage(bomb_damage);
	}
	
	@Override
	public boolean collisionDamage(DraggableVirus draggable) {
		damage(collision_damage);
		return false;
	}
	
	protected float saw_collision_time = 0f, sawCollisionOcurrenceTime;
	
	@Override
	public boolean sawDamage(SawItem saw) {
		float deltaTime = Gdx.graphics.getDeltaTime();
		if ( (saw_collision_time += deltaTime) > 0.2f ) saw.damageTime += deltaTime;
		sawCollisionOcurrenceTime = deltaTime * 3;
		damage(saw_damage);
		return false;
	}
	
	protected float dmInterval, dmLastMutation;
	
	public abstract void damage(float damage);
	
	public void reset() {
		super.reset();
		health = full_health;
	}
	
	@Override
	public void resetInPosition() {
		//generally virus is reseted on the center and on extreme high top of screen
		resetInPosition( MathUtils.round( VirusByteGame.VIEWPORT.getWorldWidth()*.5f ), 1280f );
	}
	
	@Override
	public void destroy(boolean fatal, boolean hit) {
		this.isFinale = fatal;
		this.hit = hit;
		alive = false;
		if ( VirusByteGame.SFX ) {
			if ( isReached ) {
				if ( ACTIVE ) {
					// audio TODO
				}
			}
		}
		if ( !canBeFree ) dispose();
		BOSS_TIME = false;
		
		Screen gameScreen = VirusByteGame.GAME.getScreen();
		if ( gameScreen instanceof ChallengeGameMode ) {
			//update nextStage
			ChallengeGameMode game = (ChallengeGameMode)gameScreen;
			if ( game != null ) 
				game.next_boss_time = ChallengeGameMode.stage + game.boss_time_interval;
		}
		
	}
	
	/** return this boss type */
	public Bosses getBossType() {
		return boss_type;
	}
	
}
