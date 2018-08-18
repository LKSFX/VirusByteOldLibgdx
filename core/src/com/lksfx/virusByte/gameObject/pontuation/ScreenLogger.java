package com.lksfx.virusByte.gameObject.pontuation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Pool.Poolable;

/**Show something on the screen by a determined amount of time*/
public class ScreenLogger implements Poolable {
	public boolean alive = true;
	/**Time to show in screen*/
	protected float showDuration = 5f;
	/**Time to show the text/message/point on screen*/
	protected float time;
	protected Color textColor = new Color( Color.WHITE );
	private String msg = "";
	public float x, y;
	
	public ScreenLogger() {
		this("");
	}
	
	public ScreenLogger(String msg) {
		this(msg, Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
	}
	
	public ScreenLogger(String msg, float x, float y) {
		set(msg, x,y);
	}
	
	public ScreenLogger set(String msg, float x, float y) {
		this.msg = msg;
		this.x = x;
		this.y = y;
		return this;
	}
	
	public boolean show(SpriteBatch batch, BitmapFont font, float delta) {
		font.setColor(textColor.r, textColor.g, textColor.b, -(( time / showDuration ) * 1f) );
		font.draw(batch, msg, x - (font.getBounds(msg).width*.5f), y);
		font.setColor(1f, 1f, 1f, 1f);
		if ( (time += delta) > showDuration ) destroy();
		return alive;
	}
	
	private void destroy() {
		alive = false;
	}

	@Override
	public void reset() {
		alive = true;
		time = 0;
	}
}
