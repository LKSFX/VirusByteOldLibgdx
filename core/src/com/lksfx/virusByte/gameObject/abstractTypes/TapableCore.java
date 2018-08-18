package com.lksfx.virusByte.gameObject.abstractTypes;

import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;

public class TapableCore extends VirusCore< VirusType > {
	private int tapsR = 1;
	private int clicks/*, tap*/;
	private float time;
	private final float renewTime = 0.7f;
	
	public TapableCore( VirusType virus, int taps ) {
		super( virus );
		tapsR = taps;
		virus.setInputController( new InputTriggers() {
			@Override
			public boolean justTouched(Vector3 mousePos) {
				if ( father.isOver(mousePos) ) {
					if ( tapsR > 1 ) {
						if ( clicks == 0 ) 
							time = renewTime;
						Debug.log(clicks + " clicks on " + father.virus_id);
						
						clicks++;
						if ( clicks >= tapsR ) {
							clicks = 0;
							father.destroy( true, true );
							return true;
						}
					}
					else {
						Debug.log( "clicked on " + father.virus_id );
						father.destroy(true, true);
						return true;
					}
				}
				
				return false;
			}
		} );
	}
	
	@Override
	public void update(float deltaTime) {
		if ( clicks > 0 ) {
			time -= deltaTime;
			if ( time < 0f ) 
				clicks = 0;
		} 
	}
	
	@Override
	public void reset() {
		super.reset();
		clicks = 0;
		time = renewTime;
	}
}
