package com.lksfx.virusByte.gameControl;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

public interface ObjectsManager {
	/** Update all containing objects, draws and updates methods
	 * @param batch SpriteBatch to draw everything
	 * @param font BitmapFont to draw any necessary text
	 * @param deltaTIme
	 */
	public void update(SpriteBatch batch, Vector3 mousePos, float deltaTime);
	
	
}
