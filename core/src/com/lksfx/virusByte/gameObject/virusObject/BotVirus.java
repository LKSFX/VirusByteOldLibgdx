package com.lksfx.virusByte.gameObject.virusObject;

import static com.lksfx.virusByte.gameControl.debug.Debug.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.BounceVirus;

public class BotVirus extends BounceVirus {
	private int tap;
	protected float circleSize = .5f, current_time, inactive_time = .5f;
	private TextureRegion circle;
	
	public BotVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}

	public BotVirus(float x, float y) {
		this(x, y, 5, 180, 230, new Color(Color.RED));
	}

	public BotVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor) {
		super(x, y, point, minSpd, maxSpd, trailColor);
		if (TUTORIAL) {
			circle = VirusByteGame.ASSETS.getAssetManager().get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("circle");
			tap = 1;
		}
		isMoneyBack = true;
		setRewardSize( Reward.SMALL );
		setCollisionMaskBounds(32, 32);
		minimumDistanceToTheLastTrailPosition = 10f;
		virus_id = VirusInstance.Bot;
		repositionable = false;
		setInputController( new InputTriggers() {
			@Override
			public boolean justTouched(Vector3 mousePos) {
				if ( current_time < inactive_time ) 
					return false;
				if ( isOver(mousePos) ) {
					Debug.log("clicked on: " + virus_id);
					destroy(true, true);
					return true;
				}
				return false;
			}
		} );
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("bots1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("bots2");
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("bots-trail");
		createAnim(normal, trail, new Vector2(64, 64));
		
	}
	
	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		current_time += deltaTime;
		super.draw( batch, deltaTime );
		if ( TUTORIAL ) {
			
			if ( circleSize > 1.5f ) 
				circleSize = .5f;
			
			circleSize += .05f;
			batch.setColor(1f, 1f, 1f, 0.65f);
			batch.draw( circle, spriteCenter.x +( (currentFrameWidth*.5f) - 16 ), 
					spriteCenter.y +( (currentFrameHeight*.5f) - 16 ), 16f, 16f, 32f, 32f, circleSize, circleSize, 0f );
			batch.setColor( 1f, 1f, 1f, 1f );
			String str = "tap: " + ( tap );
			debug.screen(str, (int)(spriteCenter.x - ( (Debug.debug.font.getBounds(str).width - currentFrameWidth) *.5f)), (int)spriteCenter.y - 30, false);
		}
	}
	
	/*------------------------------------------------*/
	
	/*------------------------------------------------*/
	
	@Override
	public void bottom_bounce() {}
	
	@Override
	public void randomize_direction() {
		boolean outOfPort = ( (y + currentFrameHeight*.7f) > worldHeight );
		dir.set((outOfPort) ? 0f : MathUtils.random(-1f, 1f), (outOfPort) ? 1f : MathUtils.random(-1, 1f) );
		velocityPush.set( dir ).scl( speed );
		movement.set(velocityPush).scl(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void resetInPosition(float posX, float posY) {
		super.resetInPosition( posX, posY );
		current_time = 0;
	}
}
