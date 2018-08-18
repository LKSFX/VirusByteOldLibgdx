package com.lksfx.virusByte.gameObject.virusObject;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.DraggableVirus;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.gameObject.pontuation.PointLog;
import com.lksfx.virusByte.gameObject.pontuation.ScreenLogger;

public class InfectorVirus extends DraggableVirus {
	private PointLog logMsg;
	
	//Stub: dir.set(touch).sub(position).nor();
	
	public InfectorVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}
	
	public InfectorVirus(float x, float y) {
		this(x, y, 5, 150, 350, new Color(245/255f, 10/255f, 210/255f, 0.3f), true);
	}
	
	public InfectorVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super( x, y, point, minSpd, maxSpd, trailColor, trailOn );
		if ( TUTORIAL ) {
			pointer = VirusByteGame.ASSETS.getAssetManager().
					get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("pointer");
		}
		setCollisionMaskBounds(0f, 5f, 34f, 44f);
		grabLimit = .4f;  
		virus_id = VirusInstance.Infector;
		PointLog pointLog = VirusByteGame.POINT_MANAGER.pointLogPool.obtain();
		pointLog.set("boom!", x, y);
		logMsg = pointLog;
		velMuitiplier = 5;
		imm = 0;
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("infector1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("infector2");
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("infector-trail");	
		
		createAnim( normal, trail );
		
	}
	
	@Override
	protected void collision(VirusType coll) {
		//==========================//
		if ( !coll.isVirusCollidable ) return;
		if ( coll.isDamagedByCollision ) {
			if ( coll.collisionDamage(this) ) {
				float scale = ( scaleX + scaleY ) *.5f;
				if (speed < 200 || scale < 0.8f) {
	        		destroy(true, true);
	        	} else {
	        		setScale( scale -= .3f );
	        	}
				return;
			}
		}
		destroy(true, true);
		//========================//
	}
	
	@Override
	public ScreenLogger getPointLog() {
		if ( TUTORIAL ) {
			String[] stringA = new String[] {"great", "all right", "right"};
			if ( !logMsg.hit ) return null;
			ScreenLogger log = VirusByteGame.POINT_MANAGER.screenLoggerPool.obtain();
			return log.set(stringA[MathUtils.random(stringA.length-1)], last_x, last_y);
		}
		return logMsg;
	}
	
	@Override
	public void destroy(boolean fatal, boolean hit) {
		PointLog pointLog = VirusByteGame.POINT_MANAGER.pointLogPool.obtain();
		logMsg = pointLog.set(hit ? point : point/2, last_x != 0 ? last_x : x, last_y != 0 ? last_y : y, true, virus_id, hit, comboInterval);
		super.destroy(fatal, hit);
	}
	
	@Override
	public void reset() {
		super.reset();
		logMsg = null;
		setDepth( 5 );
		if (DEBUG) Debug.log( virus_id + " reseted to:");
	}
	
	@Override
	public void resetInPosition(float posX, float posY) {
		super.resetInPosition(posX, posY);
		setScale(.7f + MathUtils.random(.3f));
	}
	
}
