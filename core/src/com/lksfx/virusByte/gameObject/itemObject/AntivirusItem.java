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
import com.lksfx.virusByte.effects.BackgroundRenderer;
import com.lksfx.virusByte.effects.Backgrounds;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.MyUtils;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.GameItem;
import com.lksfx.virusByte.gameObject.abstractTypes.HoldableType;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class AntivirusItem extends HoldableType implements GameItem {
	
	private Animation activeAnim;
	private TextureRegion icon;
	
	//Antivirus attributes
	private Barrier barrier;
	private BackgroundRenderer laserEffect;
	private boolean isActive, isDroped;
	private final float timeToActiveBarrier = .2f;
	private final float laserDuration = 5f;
	private float laserTimer;
	private float timer;
	
	public AntivirusItem() {
		this(MathUtils.random(64, WORLD_WIDTH-64), WORLD_HEIGHT+64);
	}
	
	public AntivirusItem(float x, float y) {
		this(x, y, 0, 100, 250, new Color( Color.BLUE ), false);
	}

	public AntivirusItem(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		virus_id = VirusInstance.Antivirus;
		isCollidable = false;
		isDefaultGrabBehaviour = true;
		isOnMove = false;
	}
	
	@Override
	public void resetInPosition() {
		super.resetInPosition(WORLD_WIDTH * .5f, WORLD_HEIGHT * .5f);
	}

	@Override
	public TextureRegion getIcon() {
		return icon;
	}

	@Override
	public void setAtlas() {
		AssetManager manager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] inactive = new AtlasRegion[2], active = new AtlasRegion[4];
		inactive[0] = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("antivirus1");
		inactive[1] = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("antivirus2");
		active[0] = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("lazer1");
		active[1] = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("lazer2");
		active[2] = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("lazer3");
		active[3] = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("lazer4");
		icon = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("shield-icon");
		
		createAnim(active, null);
		activeAnim = getAnimation();
		
		createAnim(inactive, null);
	}
	
	@Override
	public void release() {
		super.release();
		setGrabAllowed( false );
		isDroped = true;
	}
	
	@Override
	public void onSlotEnter() {
		super.onSlotEnter();
		setGrabAllowed( true );
		isDroped = false;
		timer = 0;
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		if ( isDroped && currentScale == defaultBaseScale ) {
			if ( !isActive ) { //barrier not active yet
				if ( (timer += deltaTime) >= timeToActiveBarrier ) { // active laser barrier
					switchToLaser();
					setSlotPermission( false );
				}
			} else { // when laser barrier is active
				if ( (laserTimer += deltaTime) >= laserDuration ) removeFromStage();
			}
		}
	}
	
	/**Switch the object state to laser mode*/
	private void switchToLaser() {
		isActive = true;
		isVisible = false;
		barrier = new Barrier(WORLD_WIDTH * .5f, y);
		VirusByteGame.VIRUS_MANAGER.addVirus(WORLD_WIDTH * .5f, y, barrier);
		laserEffect = VirusByteGame.BACK.addForeground(Backgrounds.Effect.Laser, 10f, 12); //laser effect with unlimited duration, need be finalized here
		if ( VirusByteGame.SFX ) { 
			// AUDIO TODO
		}
	}
	
	@Override
	public void removeFromStage() {
		if ( isActive && barrier != null ) barrier.removeFromStage();
		super.removeFromStage();
	}
	
	public class Barrier extends VirusType {
		
		public Barrier() {
			this(0, 0);
		}
		
		public Barrier(float x, float y) {
			this(x, y, 0, 100, 250, AntivirusItem.this.trailColor, false);
		}
		
		public Barrier(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
			super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
			float scale = WORLD_WIDTH / currentFrameWidth;
			this.setScale(scale, 1f);
			this.setAlpha( .75f );
			this.setDepth( 15 );
			this.setIndestructible( true );
			this.isCollidable = false;
			this.setCollisionMaskBounds(WORLD_WIDTH, 12f);
			this.isOnMove = false;
		}
		
		@Override
		public void setAtlas() {
			createAnim(activeAnim.getKeyFrames(), null);
		}
		
		@Override
		public void update(float deltaTime) {
			super.update(deltaTime);
			
			setScale(scaleX, MyUtils.choose(1.1f, 1.05f, 1.2f, 1.15f, .9f) );
			VirusType coll = this.collisionDetection( false );
	        if ( coll != null ) {
	        	Debug.log( "collision with " + coll.getName() + " " + coll.id );
	        	coll.laserDamage();
	        }
		}
		
		@Override
		public VirusType collisionDetection(boolean collidableOnly) {
			VirusType result = null;
			Array<VirusType> array = VirusByteGame.VIRUS_MANAGER.allVirus;
			for (int i = 0; i < array.size; i++) {
				VirusType virus = array.get(i);
				if ( virus == this || virus == AntivirusItem.this ) continue;
				if ( collisionMask.collisionDetection( virus.collisionMask ) ) {
					if ( collidableOnly && !virus.isCollidable ) continue;
					result = virus;
					break;
				}
			}
			return result;
		}
		
	}
	
	@Override
	public void destroy(boolean fatal, boolean hit) {
		super.destroy(fatal, hit);
		if ( isActive && laserEffect != null ) laserEffect.terminate();
	}
}
