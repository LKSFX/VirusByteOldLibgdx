package com.lksfx.virusByte.gameControl.inputs;

import com.badlogic.gdx.math.Vector3;

public interface InputTriggersInterface {
	/** Update trigger continuously every frame when necessary
	 *  @param mousePos touch received position
	 */
	public boolean touchUpdate(Vector3 mousePos);
	
	/** Triggered when a single touch is detected
	 * @param mousePos touch received position
	 */
	public boolean justTouched(Vector3 mousePos);
	
	/** Triggered when a touch and drag
	 * @param mousePos touch received position
	 */
	public boolean draggedTouch(Vector3 mousePos);
	
	/** Triggered when mouse button or finger has pressed screen
	 * @param mousePos touch received position
	 */
	public boolean pressedTouch(Vector3 mousePos); 
	
	/** Triggered when mouse button or finger has released
	 * @param mousePos touch received position
	 */
	public boolean releaseTouch(Vector3 mousePos); 
	
	/** Triggered when mouse button or finger has released
	 * @param mousePos touch received position
	 */
	public boolean doubleTouch(Vector3 mousePos); 
}
