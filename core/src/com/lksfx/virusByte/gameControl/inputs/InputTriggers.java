package com.lksfx.virusByte.gameControl.inputs;

import com.badlogic.gdx.math.Vector3;

public class InputTriggers implements InputTriggersInterface {
	
	public InputTriggers( ) {
		
	}

	public boolean touchUpdate( Vector3 mousePos ) {
		return false;
	}
	
	public boolean justTouched( Vector3 mousePos ) {
		return false;
	}
	
	public boolean draggedTouch( Vector3 mousePos ) {
		return false;
	}
	
	public boolean pressedTouch( Vector3 mousePos ) {
		return false;
	}
	
	public boolean releaseTouch( Vector3 mousePos ) {
		return false;
	}
	
	public boolean doubleTouch( Vector3 mousePos ) {
		return false;
	}

}
