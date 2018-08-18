package com.lksfx.virusByte.gameObject.virusObject;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class ParasiteVirus extends InfectorVirus {
	/**A list of infectable virus with collision contact*/
	VirusInstance[] infectablesList;
	/**A list of virus that can be destroyed by parasites*/
	VirusInstance[] destructibleList;
	
	public ParasiteVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}

	public ParasiteVirus(float x, float y) {
		this(x, y, 5, 150, 250, new Color(77/255f, 1f, 1f, 0.3f), true);
	}

	public ParasiteVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		infectablesList = new VirusInstance[] {VirusInstance.Plague, VirusInstance.Psycho, VirusInstance.Energyzer, VirusInstance.Nyxel, VirusInstance.Spyware};
		destructibleList = new VirusInstance[] {VirusInstance.Bot, VirusInstance.Flamebot, VirusInstance.Shockbot};
		setCollisionMaskBounds(0, 5, 32, 32);
		virus_id = VirusInstance.Parasite;
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("parasite1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("parasite2");
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("parasite-trail");	
		
		createAnim( normal, trail );
		
	}
	
	@Override
	protected void collision(VirusType coll) {
		//==========================//
		if ( !coll.isVirusCollidable ) return;
		for (VirusInstance instance : destructibleList) {
			//check if the collided virus is destructible by this parasite virus
			if ( coll.virus_id == instance ) {
				super.collision( coll );
				return;
			}
		}
		//check if the virus is on the infected list
		if ( MathUtils.randomBoolean(.75f) ) { // 75% infection change of the collided object
			for (VirusInstance instance : infectablesList) {
				if ( coll.virus_id == instance ) {
					//if in the list set collided virus as infected
					if ( MathUtils.randomBoolean(coll.spawnParasiteChange) ) {
						coll.disease();
					} else {
						coll.setFlashOn(Color.WHITE, 4f);
						coll.spawnParasiteChange = 1f;
					}
					break;
				}
			}
		}
		destroy(true, true);
		//========================//
	}
	
	@Override
	public void resetInPosition(float posX, float posY) {
		super.resetInPosition(posX, posY);
		setScale(1f);
	}
	
}
