package com.lksfx.virusByte.gameControl.debug;

import java.util.Arrays;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ReflectionPool;
import com.lksfx.virusByte.VirusByteGame;

public class Debug {
	public static int RENDER_CALLS; 
	public BitmapFont font;
	public SpriteBatch batch;
	public ScreenLog screen;
	
	private Array<ScreenLog> logList;
	private ScreenLogPool logPool;
	
	/**list that contains maskDebug objects*/
	private Array<DrawMaskBounds> maskDebugList;
	private ReflectionPool<DrawMaskBounds> maskDebugPool;
	
	
	private boolean isBatchInternal;
	public VirusByteGame game;
	
	/**Reference of the debug object of this game, this need be created on the very beginning of the game*/
	public static Debug debug;

	public Debug(VirusByteGame game) {
		this( game, new SpriteBatch(), VirusByteGame.ASSETS.getAssetManager().get("size20.ttf", BitmapFont.class) );
		isBatchInternal = true;
	}

	public Debug(VirusByteGame game, BitmapFont font) {
		this(game, new SpriteBatch(), font);
		isBatchInternal = true;
	}

	public Debug(VirusByteGame game, SpriteBatch batch) {
		this( game, batch, VirusByteGame.ASSETS.getAssetManager().get("size20.ttf", BitmapFont.class) );
	}
	
	public Debug(VirusByteGame game, SpriteBatch batch, BitmapFont font) {
		this.game = game;
		this.batch = batch;
		this.font = font;
		RENDER_CALLS = 0;
		maskDebugList = new Array<Debug.DrawMaskBounds>();
		maskDebugPool = new ReflectionPool<Debug.DrawMaskBounds>(DrawMaskBounds.class);
		logList = new Array<ScreenLog>();
		logPool = new ScreenLogPool(5, 30);
		debug = this;
	}
	
	/**
	 * @param message log to show in the screen
	 * @param position X on screen
	 * @param position Y on screen 
	 */
	public void screen(String msg, float screenX, float screenY) {
		screen(msg, screenX, screenY, true);
	}
	
	public void screen(String msg, float screenX, float screenY, boolean comp) {
		ScreenLog show = logPool.obtain();
		show.msg = msg;
		show.x = screenX;
		
		if ( comp ) {
			show.y = getHeightCompensation() - screenY;
		} 
		else {
			show.y = screenY;
		}
		
		logList.add( show );
	}

	public void fps() {
		ScreenLog fps = logPool.obtain();
		fps.msg = "Fps: " + Gdx.graphics.getFramesPerSecond();
		fps.x = 10;
		fps.y = 40;
		logList.add(fps);
	}
	
	public void showRenderCalls() {
		ScreenLog rc = logPool.obtain();
		rc.msg = "Render Calls: " + RENDER_CALLS;
		rc.x = 10;
		rc.y = 20;
		logList.add(rc);
		RENDER_CALLS = 0;
	}
	
	//inner class
	/** make strings that can be show on some point of the screen */
	public class ScreenLog implements Pool.Poolable {

		private String msg;
		private float x;
		private float y;

		public ScreenLog(String msg, float x, float y) {
			this.msg = msg;
			this.x = x;
			this.y = y;
		}

		public void logScreen(SpriteBatch batch) {
			if ( batch.isDrawing() )
				return;
			font.draw( batch, msg, x, Gdx.graphics.getHeight() - y );
		}

		@Override
		public void reset() {
			// TODO Auto-generated method stub

		}
	}
	
	//inner class
	/**hold pooled logScreen objects*/
	private class ScreenLogPool extends Pool<ScreenLog> {

		public ScreenLogPool(int min, int max) {
			super(min, max);
		}
		
		@Override
		protected ScreenLog newObject() {
			return new ScreenLog("", 0, 0);
		}

	}
	
	public enum SHAPE { CIRCLE, LINE, RECTANGLE, POLYGON, TRIANGLE };
	
	//inner class
	/**draw masks bounds for debug purpose*/
	private static class DrawMaskBounds implements Pool.Poolable {
		public float x, y, w, h;
		public SHAPE drawShapeType;
		public Color color;
		public float[] vertices;
		
		public DrawMaskBounds set(SHAPE drawShapeType , float x, float y, float w, float h, Color color) {
			this.drawShapeType = drawShapeType;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.color = color;
			return this;
		}
		
		public DrawMaskBounds setPolygon( boolean isTriangle, float[] array, Color color ) {
			drawShapeType = isTriangle ? SHAPE.TRIANGLE : SHAPE.POLYGON;
			vertices = array;
			this.color = color;
			return this;
		}
		
		@Override
		public void reset() {}
	}

	public static void log(String log) {
		Gdx.app.log("VirusByte: ", log);
	}

	public static void log(String stats, String log) {
		Gdx.app.log(stats, log);
	}
	
	private ShapeRenderer shapeRender = new ShapeRenderer(); //actually used only for debug mode
	
	/**Insert this bounds to draw*/
	public void insertShapeToRender(SHAPE drawShapeType, float x, float y, float w, float h, Color color) {
		DrawMaskBounds toDraw = maskDebugPool.obtain();
		if ( toDraw != null ) 
			maskDebugList.add( toDraw.set(drawShapeType, x, y, w, h, color) );
	}
	
	/** Insert this polygon to draw */
	public void insertPolygonToRender( boolean isTriangle, float[] vertices, Color color ) {
		DrawMaskBounds toDraw = maskDebugPool.obtain();
		if ( toDraw != null ) 
			maskDebugList.add( toDraw.setPolygon( isTriangle, vertices, color ) );
	}
	
	private void shapeRenderer() {
		if ( maskDebugList.size > 0 ) {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			shapeRender.setProjectionMatrix(batch.getProjectionMatrix());
			Iterator<DrawMaskBounds> iterator = maskDebugList.iterator();
			// Draw filled shape
			shapeRender.begin( ShapeType.Filled );
			
			while ( iterator.hasNext() ) {
				DrawMaskBounds bounds = iterator.next();
				if ( bounds.drawShapeType == SHAPE.RECTANGLE ) {
					shapeRender.setColor( bounds.color );
					shapeRender.rect( bounds.x, bounds.y, bounds.w, bounds.h );
					iterator.remove();
					maskDebugPool.free( bounds );
				} 
				else if ( bounds.drawShapeType == SHAPE.CIRCLE ) {
					shapeRender.setColor( bounds.color );
					shapeRender.circle( bounds.x, bounds.y, bounds.w );
					iterator.remove();
					maskDebugPool.free( bounds );
				}
				else if ( bounds.drawShapeType == SHAPE.TRIANGLE ) {
					shapeRender.setColor( bounds.color );
					float[] verts = bounds.vertices;
					shapeRender.triangle( verts[0], verts[1], verts[2], verts[3], verts[4], verts[5] );
					iterator.remove();
					maskDebugPool.free( bounds );
				}
			}
			
			shapeRender.end();
			if ( maskDebugList.size == 0 ) 
				return; // method return/end, if not have more objects to render in the list.
			iterator = maskDebugList.iterator();
			shapeRender.begin( ShapeType.Line );
			
			// Draw line
			while ( iterator.hasNext() ) {
				DrawMaskBounds bounds = iterator.next();
				if ( bounds.drawShapeType == SHAPE.LINE ) {
					shapeRender.setColor(bounds.color);
					shapeRender.line(bounds.x, bounds.y, bounds.w, bounds.h);
					iterator.remove();
					maskDebugPool.free( bounds );
				}
				else if ( bounds.drawShapeType == SHAPE.POLYGON ) {
					shapeRender.setColor( bounds.color );
					shapeRender.polygon( bounds.vertices );
					iterator.remove();
					maskDebugPool.free( bounds );
					Debug.log( "Draw polygon: " + Arrays.toString( bounds.vertices ) );
				}
			}
			
			shapeRender.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);
		} 
	}
	
	public void show() {
		shapeRenderer();
		
		batch.begin();
		/*
		 * String log = "used pooled log: " + logList.size + " | free to use: "
		 * + logPool.getFree() + " | peak of use: " + logPool.peak + "/" +
		 * logPool.max; log(log);
		 */
		Iterator<ScreenLog> msg = logList.iterator();
		while ( msg.hasNext() ) {
			ScreenLog show = msg.next();
			font.draw(batch, show.msg, show.x, show.y);
			msg.remove();
			logPool.free(show);
		}
		batch.end();
	}

	public void dispose() {
		if ( isBatchInternal )
			batch.dispose();
		
		shapeRender.dispose();
	}
	
	private int getHeightCompensation() {
		if (isBatchInternal) {
			return Gdx.graphics.getHeight();
		} else {
			return (int) game.viewport.getWorldHeight();
		}
	}
}
