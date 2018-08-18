package com.lksfx.virusByte.effects;

public interface Immersion {
	
	public static final boolean DEGUG = false;
	
	public static final int BUMP_100 = 6, EXPLOSION5 = 77, FAST_PULSE_100 = 45, DOUBLE_STRONG_CLICK_33 = 17,
			TRIPLE_STRONG_CLICK_66 = 22;
	
	public void vibrate(int type);
	
	public void stop();
}
