package com.lksfx.virusByte.gameObject.pontuation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;

public class PointLog extends ScreenLogger {
	public boolean assign, hit;
	public float addTime; //time to add to combo time interval
	public VirusInstance type;
	public int value = 0;

	public PointLog() {
		this("");
	}

	public PointLog(String msg) {
		super(msg);
	}
	
	public PointLog(int value, float x, float y, boolean assign, boolean hit) {
		this(value, x, y, assign, null, hit, 1f);
	}
	
	public void set(int value, float x, float y, boolean assign, boolean hit) {
		set(value, x, y, assign, null, hit, 1f);
	}
	
	public PointLog(int value, float x, float y, boolean assign, VirusInstance type, boolean hit, float comboInterval) {
		set(value, x, y, assign, type, hit, comboInterval);
	}

	public PointLog(String msg, float x, float y) {
		super(msg, x, y);
	}
	
	public PointLog set(int value, float x, float y, boolean assign, VirusInstance type, boolean hit, float comboInterval) {
		set( String.valueOf( value ), x, y);
		showDuration = 2.5f;
		this.hit = hit;
		this.type = type;
		this.value = value;
		this.assign = assign; 
		addTime = comboInterval;
		return this;
	}
	
	private float textColorSwitch;
	private float textScale = 1f;
	
	@Override
	public boolean show(SpriteBatch batch, BitmapFont font, float delta) {
		if ( (textColorSwitch += delta)  > .1f) {
			textColorSwitch = 0;
			textColor = (textColor.equals(Color.BLACK)) ? Color.WHITE : Color.BLACK;
		}
		font.setScale( textScale += .009f );
		boolean ended = super.show(batch, font, delta);
		font.setScale( 1f );
		return ended;
	}
	
	@Override
	public void reset() {
		super.reset();
		type = null;
		addTime = 0;
		value = 0;
		textColorSwitch = 0;
		textScale = 1f;
		assign = false;
		hit = false;
	}
}
