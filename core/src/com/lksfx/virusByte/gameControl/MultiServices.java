package com.lksfx.virusByte.gameControl;

import com.lksfx.virusByte.effects.Immersion;

/**
 * This interface allow services only available on android like
 * immersions, admob and google play services
 * @author LucasOli
 *
 */
public interface MultiServices {
	
	/** get immersion object, various types of vibration */
	public Immersion getImmersion();
	
	/** get AdControl object, only available on android */
	public AdControl getAdControl();
	
	/** get google play services instance by platform **/
	public GoogleInterface getGooglePlayService();
}
