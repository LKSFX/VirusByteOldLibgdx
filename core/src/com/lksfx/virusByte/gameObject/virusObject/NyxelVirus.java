package com.lksfx.virusByte.gameObject.virusObject;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.Immersion;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.TapableCore;

public class NyxelVirus extends PlagueVirus {
	
	public NyxelVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}

	public NyxelVirus(float x, float y) {
		this(x, y, 20, 150, 250, new Color( Color.RED ));
	}

	public NyxelVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor) {
		super(x, y, point, minSpd, maxSpd, trailColor);
		isMoneyBack = true;
		setCollisionMaskBounds(54, 50);
		virus_id = VirusInstance.Nyxel;
		comboInterval = 2f;
		isDamagedByCollision = false;
		isDamagedByExplosions = false;
		imm = Immersion.TRIPLE_STRONG_CLICK_66;
		tap = 3;
		setDefaultVirusCore( new TapableCore( this, 3 ) );
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("nyxel1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("nyxel2");
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("nyxel-trail");
		
		createAnim( normal, trail );
		
	}
	
	/*------------------------------------------------*/
	
	/*------------------------------------------------*/
	
}
