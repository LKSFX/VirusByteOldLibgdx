package com.lksfx.virusByte.gameObject.virusObject;

import static com.lksfx.virusByte.gameControl.debug.Debug.debug;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class TrojanVirus extends VirusType {
	private int clicks, tap;
	private float time, renewTime = 0.7f, circleSize = .5f;
	private TextureRegion circle;
	
	public TrojanVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}
	
	public TrojanVirus(float x, float y) {
		this(x, y, 7, 150, 230, new Color(77/255f, 1f, 1f, 0.3f));
	}
	
	public TrojanVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor) {
		super(x, y, point, minSpd, maxSpd, trailColor, true);
		if (TUTORIAL) {
			circle = VirusByteGame.ASSETS.getAssetManager().
					get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("circle");
			tap = 2;
		}
		isMoneyBack = true;
		setCollisionMaskBounds(32, 64);
		virus_id = VirusInstance.Trojan;
		canSpawnBots = true;
		setInputController( new InputTriggers() {
			@Override
			public boolean doubleTouch(Vector3 mousePos) {
				if ( isOver(mousePos) ) {
					clicks++;
					Debug.log(clicks + " clicks on psycho");
					time = renewTime;
					if ( clicks >= 2 ) {
						clicks = 0;
						destroy(true, true);
						spawn();
						return true;
					}
				}
				return false;
			}
		} );
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("trojan1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("trojan2");		
		
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("trojan-trail");	
		createAnim( normal, trail );
		
	}
	
	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		super.draw(batch, deltaTime);
		if (TUTORIAL) {
			if (circleSize > 1.5f) circleSize = .5f;
			circleSize += .05f;
			batch.setColor(1f, 1f, 1f, 0.65f);
			batch.draw(circle, spriteCenter.x +( (currentFrameWidth*.5f) - 16 ), spriteCenter.y +( (currentFrameHeight*.5f) - 16 ), 16f, 16f, 32f, 32f, circleSize, circleSize, 0f);
			batch.setColor(1f, 1f, 1f, 1f);
			String str = "tap: " + (tap-clicks);
			debug.screen(str, (int)(spriteCenter.x - ( (Debug.debug.font.getBounds(str).width - currentFrameWidth) *.5f)), (int)spriteCenter.y - 30, false);
		}
	}
	
	/*------------------------------------------------*/
	/*@Override
	public boolean doubleTouch(Vector3 mousePos) {
		if ( isOver(mousePos) ) {
			clicks++;
			Debug.log(clicks + " clicks on psycho");
			time = renewTime;
			if ( clicks >= 2 ) {
				clicks = 0;
				destroy(true, true);
				spawn();
				return true;
			}
		}
		return false;
	}*/
	/*------------------------------------------------*/
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if ( clicks > 0 ) {
			time -= deltaTime;
			if ( time < 0f ) clicks = 0;
		} 
	}
	
	/**Determine if this Trojan will spawn some Bots when destroyed by a hit*/
	private boolean canSpawnBots;
	
	/**Spawn Bots*/
	public void spawn() {
		if ( !canSpawnBots ) return;
		for (int i = 3; i > 0; i--) VIRUS_MANAGER.addVirus(x, y, VirusManager.VirusInstance.Bot); 
	}
	
	@Override
	public void resetInPosition(float posX, float posY) {
		super.resetInPosition(posX, posY);
//		canSpawnBots = config.spawnChilds;
	}
	
	@Override
	public void reset() {
		super.reset();
		clicks = 0;
		time = renewTime;
	}
}
