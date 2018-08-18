package com.lksfx.virusByte.gameObject.abstractTypes;

import static com.lksfx.virusByte.gameControl.debug.Debug.debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Polygon;

public class TestGameObject extends GameObject {
	
	Polygon polygon;
	
	public TestGameObject() {
		super();
		moveTo( VirusType.WORLD_WIDTH * .5f, VirusType.WORLD_HEIGHT * .5f );
		polygon = new Polygon();
		polygon.setVertices( new float[] { 
			 0, -50, 
			 50, 50, 
			 -50, 50, 
		} );
		polygon.setPosition( x, y );
		polygon.setRotation( 35f );
		isVisible = false;
		moveTowards( 320, 100 );
	}
	
	@Override
	public void update(float deltaTime) {
		
		polygon.setPosition( x, y );
		debug.insertPolygonToRender( true, polygon.getTransformedVertices(), Color.WHITE );
		
	}

}
