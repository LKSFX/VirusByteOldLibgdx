package com.lksfx.virusByte.gameObject.virusObject;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.Backgrounds.Animations;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;

public class FreezeBotVirus extends FlameBotVirus {
	private Animation animInactive;
	private boolean active;
	private float timer;
	private final float interval = 1f;
	
	public FreezeBotVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}

	public FreezeBotVirus(float x, float y) {
		this(x, y, 5, 210, 250, new Color(77/255f, 228/255f, 1f, 0.3f));
	}

	public FreezeBotVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor) {
		super(x, y, point, minSpd, maxSpd, trailColor);
		isMoneyBack = true;
		setRewardSize( Reward.SMALL );
		virus_id = VirusInstance.Freezebot;
		active = true;
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("freeze-bot-sick1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("freeze-bot-sick2");
		createAnim(normal, null);
		animInactive = getAnimation();
		
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("freeze-bot1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("freeze-bot2");
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("freeze-bot-trail");
		
		createAnim( normal, trail, new Vector2(64, 64) );
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if ( (timer += deltaTime) >= interval ) {
			active = !active;
			setAnimation( ( active ) ? anim_default : animInactive );
			timer = 0;
		}
	}
	
	@Override
	public void destroy(boolean fatal, boolean hit) {
		super.destroy(fatal, hit);
		if ( hit && active ) {
			if ( !VirusByteGame.BACK.isBackgroundActive(Animations.Frozen) ) {
				VirusByteGame.BACK.setBackground( Animations.Frozen );
			}
		}
	}
}
