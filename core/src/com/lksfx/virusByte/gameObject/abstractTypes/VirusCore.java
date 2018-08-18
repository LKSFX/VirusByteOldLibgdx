package com.lksfx.virusByte.gameObject.abstractTypes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class VirusCore< T extends VirusType > {
	
	public T father;
	
	public VirusCore( T virus ) {
		father = virus;
	}
	
	public void initialize() {}
	
	public abstract void update( float deltaTime );
	
	public void draw( SpriteBatch batch, float deltaTime ) {}
	
	public void reset() {}
	
}
