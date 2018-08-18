package com.lksfx.virusByte.gameObject.virusObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.Backgrounds;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.BounceVirus;
import com.lksfx.virusByte.gameObject.pontuation.ScreenLogger;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner.SpawnableConfiguration;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner.Transition;

public class ShockBotVirus extends BounceVirus {
	private float interval = 1f, inactive = interval;
	/**Max time alive on stage*/
	private final float maxAliveTime = 10f;
	/**Total time alive on stage*/
	private float currentAliveTime;
	/**True when the time alive end, shrink until scale 0 then remove from stage*/
	private boolean shrinking;
	
	public ShockBotVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}

	public ShockBotVirus(float x, float y) {
		this(x, y, 25, 250, 350, new Color(77/255f, 1f, 1f, 1f));
	}

	public ShockBotVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor) {
		super(x, y, point, minSpd, maxSpd, trailColor);
		repositionable = false;
		virus_id = VirusInstance.Shockbot;
		setInputController( new InputTriggers() {
			@Override
			public boolean justTouched(Vector3 mousePos) {
				if ( inactive > 0f ) return false;
				if ( isOver(mousePos) ) {
					Debug.log("clicked on shockbot");
					VirusByteGame.BACK.addForeground(Backgrounds.Effect.Damage, 1f, 5);
					if ( ACTIVE ) {
						if ( VirusByteGame.SFX ) {
							// AUDIO TODO
						}
						LIFES--;
					}
					destroy(true, true);
					return true;
				}
				return false;
			}
		} );
	}
	
	@Override
	public void resetInPosition(float posX, float posY) {
		super.resetInPosition(posX, posY);
		setCollisionMaskBounds(32, 32);
		setScale(1f);
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("shockbot1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("shockbot2");		
		
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("shockbot-trail");	
		createAnim( normal, trail );
		
	}
	
	@Override
	public void set_direction() {
		velocityPush.set(dir).scl(speed);
	}
	
	@Override
	public void randomize_direction() {
		boolean outOfPort = ( (y + currentFrameHeight*.7f) > worldHeight );
		dir.set((outOfPort) ? 0f : MathUtils.random(-1f, 1f), (outOfPort) ? 1f : MathUtils.random(-1, 1f) );
		velocityPush.set( dir ).scl( speed );
		movement.set(velocityPush).scl(Gdx.graphics.getDeltaTime());
	}
	
	/*------------------------------------------------*/
	
	/*------------------------------------------------*/
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		inactive -= deltaTime;
		if ( !shrinking ) {
			if ( isOnMove && (currentAliveTime += deltaTime) >= maxAliveTime ) {
				shrinking = true;
				Spawner spawner = VirusByteGame.VIRUS_MANAGER.spawner;
				SpawnableConfiguration config = spawner.getSCS(1f, .01f, .05f, 0, Transition.SHRINK_OUT);
				spawner.addSpawnable(this, config, x, y);
			}
		} else {
			if ( ((scaleX + scaleY) * .5f) < .1f ) destroy(); 
		}
	}
	
	@Override
	public void destroy() {
		destroy(false, false, true);
	}
	
	@Override
	public void destroy(boolean fatal, boolean hit) {
		destroy(fatal, hit, false);
	}
	
	/**@param if true the time of inactivity will be ignored and the virus will be sure destroyed*/
	public void destroy(boolean fatal, boolean hit, boolean ignore) {
		if ( !ignore && inactive > 0f ) return;
		this.isFinale = fatal;
		this.hit = hit;
		alive = false;
		for (ActionListener action : onDestroy) action.execute(this);
		clearAllActionListeners();
	}
	
	@Override
	public Assets.Particles getFinalEffectType() {
		return hit ? finalEffectType : null;
	}
	
	@Override
	public ScreenLogger getPointLog() {
		return null;
	}
	
	@Override
	public void reset() {
		super.reset();
		currentAliveTime = 0;
		inactive = interval;
		shrinking = false;
	}
}
