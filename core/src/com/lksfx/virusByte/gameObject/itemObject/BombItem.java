package com.lksfx.virusByte.gameObject.itemObject;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.Immersion;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.GameItem;
import com.lksfx.virusByte.gameObject.abstractTypes.HoldableType;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class BombItem extends HoldableType implements GameItem {
	
	private boolean debug;
	private Animation activeAnim;
	private TextureRegion icon;
	
	//Bomb attributes
	private final float timeToExplode = 1.5f, timeToCauseDamage = .5f;
	private float damageTimer, explosionRange;
	private float timer;
	private boolean isActive;
	private boolean exploded;
	
	public BombItem() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}
	
	public BombItem(float x, float y) {
		this(x, y, 0, 100, 250, new Color( Color.BLUE ), false);
	}

	public BombItem(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		virus_id = VirusInstance.Bomb;
		explosionRange = (WORLD_WIDTH + WORLD_HEIGHT) / 4;
		isDefaultGrabBehaviour = true;
		isCollidable = false;
		isOnMove = false;
	}
	
	@Override
	public void resetInPosition() {
		super.resetInPosition( WORLD_WIDTH * .5f, WORLD_HEIGHT * .5f );
	}

	@Override
	public void setAtlas() {
		AssetManager manager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] inactive = new AtlasRegion[1], active = new AtlasRegion[4];
		inactive[0] = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("bomb0");
		active[0] = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("bomb1");
		active[1] = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("bomb2");
		active[2] = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("bomb3");
		active[3] = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("bomb2");
		icon = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("bomb-icon");
		
		createAnim(active, null);
		activeAnim = getAnimation();
		
		createAnim(inactive, null);
	}

	@Override
	public TextureRegion getIcon() {
		return icon;
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		if ( !exploded ) {
			//While not Exploded
			if ( isActive ) {
				if ( (timer += deltaTime) > timeToExplode ) explosionEffect(); //timer count down to explode
			}
		} else {
			//When explode
			if ( (damageTimer += deltaTime) < timeToCauseDamage ) {
				causeExplosionDamage();
			} else {
				removeFromStage();
			}
		}
	}
	
	@Override
	public void grab() {
		super.grab();
		setAnimation( activeAnim );
		isActive = true;
	}
	
	@Override
	public void onSlotEnter() {
		super.onSlotEnter();
		timer = 0;
	}
	
	private void explosionEffect() {
		float scale = (scaleX + scaleY) * .5f;
		VirusManager.PART_EFFECTS.createEffect(Assets.Particles.explosion, x, y, scale, 30);
		if ( VirusByteGame.SFX ) {
			if ( debug ) 
				Debug.log("AUDIO PLAY: ");
			//AUDIO TODO
		}
		if ( VirusByteGame.VIBRATION ) VirusByteGame.IMM.vibrate( Immersion.EXPLOSION5 );
		setSlotPermission( false );
		exploded = true;
		isVisible = false;
	}
	
	private void causeExplosionDamage() {
		float scale = (scaleX + scaleY) * .5f;
		if ( scale > 1.5f ) return;
		int i, j;
		for (i = 0; i < VirusByteGame.VIRUS_MANAGER.virus_set.size; i++)
		{
			Array<VirusType> virusArray = VirusByteGame.VIRUS_MANAGER.virus_set.get(i);
			for (j = 0; j < virusArray.size; j++)
			{
				VirusType virus = virusArray.get(j);
				if ( getPosition().dst( virus.getPosition() ) < explosionRange ) {
					if ( !(VIEWPORT.getCamera().frustum.boundsInFrustum(virus.x, virus.y, 0f, virus.currentFrameWidth*.5f,
							virus.currentFrameHeight*.5f, 0f)) ) continue;
					//==========================//
					if ( virus.isDamagedByExplosions ) virus.explosionDamage( this );
					//========================//
				}
			}
		}
	}
	
	/** @return true when this bomb is exploded */
	public boolean isExploded() {
		return exploded;
	}
}
