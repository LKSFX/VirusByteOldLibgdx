package com.lksfx.virusByte.gameObject.itemObject;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class LifeItem extends VirusType {
	public Assets.Particles effect = Assets.Particles.healingRing;
	
	public LifeItem() {
		this(MathUtils.random(64, WORLD_WIDTH-64), WORLD_HEIGHT+64);
	}
	
	public LifeItem(float x, float y) {
		super(x, y, 0, 100, 250, null, false);
		setInputController( new InputTriggers() {
			@Override
			public boolean justTouched(Vector3 mousePos) {
				if ( isOver(mousePos) ) {
					Debug.log("clicked on life");
					if ( ACTIVE ) 
						LIFES++;
					if ( VirusByteGame.SFX ) {
						// AUDIO TODO
					}
					destroy(true);
					return true;
				}
				return false;
			}
		} );
	}
	
	@Override
	public void setAtlas() {
		AssetManager manager = VirusByteGame.ASSETS.getAssetManager();
		
		TextureRegion[] inactive = new TextureRegion[1];
		inactive[0] = manager.get(Assets.Atlas.itemAtlas.path, TextureAtlas.class).findRegion("battery-item");
		
		createAnim( inactive, null );
		setCollisionMaskBounds(42, 42);
		setTouchMaskBounds(42, 42);
	}
	
	/*------------------------------------------------*/
	
	/*------------------------------------------------*/
	
	@Override
	public Assets.Particles getFinalEffectType() {
		return effect;
	}
}
