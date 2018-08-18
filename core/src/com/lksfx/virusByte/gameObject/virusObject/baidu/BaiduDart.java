package com.lksfx.virusByte.gameObject.virusObject.baidu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameObject.abstractTypes.DraggableVirus;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.gameObject.pontuation.ScreenLogger;
import com.lksfx.virusByte.gameObject.virusObject.baidu.BaiduBoss.Heads;

public class BaiduDart extends DraggableVirus {
	
	private Heads head;
	
	public BaiduDart() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}
	
	public BaiduDart(float x, float y) {
		this(x, y, 5, 150, 200, new Color( Color.WHITE ), true);
	}
	
	public BaiduDart(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		this.head = Heads.values()[ MathUtils.random(2) ];
		initialize(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		velMuitiplier = 5;
		dragOriginPosition = new Vector2();
		grabDistanceLimit =  (float) Math.sqrt( WORLD_WIDTH + WORLD_HEIGHT ) * 4;
		allowScaleOnTouchMask = false;
		setTouchMaskBounds(64, 64);
		canBeFree = false;
	}
	
	@Override
	protected void collision(VirusType coll) {
		//==========================//
		if (!coll.isVirusCollidable) return;
		if ( coll.isDamagedByCollision ) coll.collisionDamage( this );
		destroy(true, true);
		//========================//
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager manager = VirusByteGame.ASSETS.getAssetManager();
		
		String dartName = ( head == Heads.CYAN ) ? "cyan" : ( (head == Heads.PINK) ? "pink" : "green" ); 
		dartName += "-dardo";
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion(dartName+"1");
		normal[1] = manager.get(Assets.Atlas.baiduAtlas.path, TextureAtlas.class).findRegion(dartName+"2");
		
		createAnim( normal, null, new Vector2(44, 44) );
	}
	
	public Heads getHeadColorType() {
		return head;
	}
	
	@Override
	public ScreenLogger getPointLog() {
		return null;
	}
}
