package com.lksfx.virusByte.gameObject.virusObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;

public class FlameBotVirus extends BotVirus {

	public FlameBotVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}

	public FlameBotVirus(float x, float y) {
		this(x, y, 5, 210, 250, new Color(1f, 77/255f, 0f, 0.3f));
	}

	public FlameBotVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor) {
		super(x, y, point, minSpd, maxSpd, trailColor);
		isMoneyBack = true;
		setRewardSize( Reward.SMALL );
		virus_id = VirusInstance.Flamebot;
		minimumDistanceToTheLastTrailPosition = 20f;
		trailAlphaIncrement = .3f;
		trailMaxAlpha = .9f;
		setTrailSize( 20 );
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("flamebot1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("flamebot2");
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("flamebot-trail");
		
		createAnim( normal, trail, new Vector2(64, 64) );
		
	}
	
	@Override
	public void bottom_bounce() {}
	
	@Override
	public void right_bounce() {
		dir.add( -( (dir.x)+( 1f+MathUtils.random(.2f) ) ), 0 ).nor();
		set_direction();
	}
	
	@Override
	public void left_bounce() {
		dir.add( ( (-dir.x)+( 1f+MathUtils.random(.2f) ) ), 0).nor();
		set_direction();
	}
	
	@Override
	public void randomize_direction() {
		dir.set(MathUtils.random(-( 2f+MathUtils.random(.6f) ), .2f+MathUtils.random(.6f) ), MathUtils.random(.3f, .8f) );
		velocityPush.set( dir ).scl( speed );
		movement.set(velocityPush).scl( Gdx.graphics.getDeltaTime() );
	}
	
	@Override
	public void resetInPosition(float posX, float posY) {
		super.resetInPosition(posX, posY);
		current_time = 0;
	}
}
