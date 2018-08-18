package com.lksfx.virusByte.android;

import com.lksfx.virusByte.effects.Immersion;
import com.immersion.uhl.Launcher;

public class ImmersionAndroid implements Immersion {
	private Launcher m_launcher;
	
	public ImmersionAndroid(Launcher launcher) {
		this.m_launcher = launcher;
	}
	
	@Override
	public void vibrate(int type) {
		try {
			m_launcher.play(type);
		} catch(RuntimeException ex) {}
	}

	@Override
	public void stop() {
		try {
			m_launcher.stop();
		} catch(RuntimeException ex) {}
	}

}
