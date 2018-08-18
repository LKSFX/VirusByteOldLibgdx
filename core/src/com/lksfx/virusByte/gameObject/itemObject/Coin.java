package com.lksfx.virusByte.gameObject.itemObject;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.hud.Inventory;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class Coin extends VirusType {
	
	private int weight = 10;
	private int baseSpeed;
	private boolean isMoveUp = true;
	private int coin_value;
	
	public Coin() {
		this(0, 0);
	}
	
	public Coin(float x, float y) {
		this(x, y, 0, 0, 0, null, false);
	}
	
	public Coin(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		setIndestructible( true );
		setCoinValue( 2 );
		isHostile = false;
		isCollidable = false;
		setInputController( new InputTriggers() {
			@Override
			public boolean pressedTouch(Vector3 mousePos) {
				if ( isOver(mousePos) ) {
					Inventory inv = VirusByteGame.HUD.inventory;
					int cash = inv.getCash();
					inv.setCash( cash + coin_value );
					destroy();
				}
				return super.pressedTouch( mousePos );
			}
		} );
	}

	@Override
	public void setAtlas() {
		AssetManager manager = VirusByteGame.ASSETS.getAssetManager();
		
		TextureRegion[] regions = new TextureRegion[4];
		regions[0] = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion( "coin-big1" );
		regions[1] = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion( "coin-big2" );
		regions[2] = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion( "coin-big3" );
		regions[3] = manager.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion( "coin-big4" );
		
		createAnim(regions, null, .083f);
	}
	
	/** Set the weight of this coin */
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	@Override
	public void setSpeed(int spd) {
		super.setSpeed(spd, spd);
		baseSpeed = speed;
	}
	
	private void setLocalSpeed(int spd) {
		super.setSpeed(spd, spd);
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
	}
	
	@Override
	public void movementUpdate(float deltaTime) {
		if ( isOnMove ) {
			if ( isMoveUp ) {
				moveTo( x, y += speed * deltaTime );
				setLocalSpeed( speed - weight );
				if ( speed <= 0 ) {
					isMoveUp = false;
				}
			} else {
				if ( speed < baseSpeed ) {
					setLocalSpeed( speed + weight * 2 );
					if ( speed > baseSpeed ) setLocalSpeed( baseSpeed );
				}
				moveTo( x, y -= speed * deltaTime );
			}
		}
	}
	
	/** Set the value for this coin */
	public void setCoinValue(int value) {
		coin_value = value;
	}
	
	@Override
	public String getName() {
		return "Large Coin";
	}
}
