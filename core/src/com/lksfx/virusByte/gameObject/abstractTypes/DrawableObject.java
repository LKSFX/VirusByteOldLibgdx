package com.lksfx.virusByte.gameObject.abstractTypes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface DrawableObject {
	
	public int getDepth();
	
	public void draw(SpriteBatch batch, float deltaTime);
	
	public String getName();
}
