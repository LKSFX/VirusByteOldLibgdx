package com.lksfx.virusByte.gameControl;

import java.nio.IntBuffer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.BufferUtils;

public class DrawLines extends ApplicationAdapter {
	private ShapeRenderer render;
	private FrameBuffer surface;
	private Texture paint;
	private int lastxx;
	private int lastyy;
	private float screenWidth = (float)Gdx.graphics.getWidth();
	private float screenHeight = (float)Gdx.graphics.getHeight();
	public SpriteBatch batch;
	
	public DrawLines() {
		render = new ShapeRenderer();
		surface = new FrameBuffer(Format.RGBA8888, (int)screenWidth, (int)screenHeight, false);
		Gdx.app.log("DrawLines Obj: ", "" + batch);
	}
	
	public void draw() {
		//camera.update();
		drawWithNodes();
	}
	
	public void draw(SpriteBatch batch) {
		if (this.batch == null) this.batch = batch;
		drawWithPixmap(batch);
	}
	
	private void drawWithNodes() {
		render.begin(ShapeType.Line);
		render.line(0, 320, 480, 320);
		render.end();
	}
	
	private void drawWithPixmap(Batch batch) { //draw in a pixel map 
		int xx = Gdx.input.getX(), yy = Gdx.input.getY();
		if (lastxx == 0) {lastxx = xx; lastyy = yy;}
		
		surface.begin();//buffer
		render.begin(ShapeType.Line);
		setlineWidth(3);
		render.line(lastxx, lastyy, xx, yy);
		render.end();
		surface.end();
		
		lastxx = xx; 
		lastyy = yy;
	}
	
	private void setlineWidth(int width) {
		IntBuffer buffer = BufferUtils.newIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL20.GL_ALIASED_LINE_WIDTH_RANGE, buffer);
		if (width > buffer.get(1)) {
			width = buffer.get(1);
		} else if (width < 1) {
			width = 1;
		}
		Gdx.gl.glLineWidth((float)width);
	}
	
	/**
	 * Clear the surface buffer, change line color and set the start line point
	 */
	public void startDraw() {
		surface.begin();
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		surface.end();
		render.setColor((float)Math.random(), (float)Math.random(), (float)Math.random(), 1f);
		lastxx = 0;
		lastyy = 0;
	}
	
	@Override
	public void dispose() {
		render.dispose();
		paint.dispose();
		surface.dispose();
	}
	
	
	/**
	 * Show the painted surface on the screen
	 */
	public void show() {
		if (batch != null) {
			batch.begin();
			batch.draw(surface.getColorBufferTexture(), 0f, 0f);
			batch.end();
		}
	}
}
