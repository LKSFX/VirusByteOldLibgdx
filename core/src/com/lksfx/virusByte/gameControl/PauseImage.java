package com.lksfx.virusByte.gameControl;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.lksfx.virusByte.effects.Backgrounds;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.screens.GameStageScreen;

/**
 * this class is responsible for make a texture copy of the last render before the main render turns off
 * and save that picture to later show
 * */
public class PauseImage implements Disposable {
	private Texture back;
	
	public void createImage(GameStageScreen gameScreen) {
		remountBackScreenImage(gameScreen);
		// ===== //
		Pixmap pixmap = getFrame();
		back = new Texture(pixmap);
		pixmap.dispose();
		// ===== //
	}
	
	public void dispose() {
		if (back != null) back.dispose();
	}
	
	private void remountBackScreenImage(GameStageScreen gameScreen) {
		Debug.log( "StageScreen is: " + gameScreen );
		if ( gameScreen == null ) return;
		SpriteBatch batch = gameScreen.batch;
		Backgrounds back = gameScreen.back;
		VirusManager virus_manager = gameScreen.virus_manager;
		float delta = Gdx.graphics.getDeltaTime();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		back.renderBackgrounds(batch, delta); // render backgrounds
		virus_manager.draw(batch, delta); // update and render virus manager
		back.renderForegrounds(batch, delta); // render foregrounds
		batch.end();
	}
	
	public Texture getTexture() {
		return back;
	}
	
	private Pixmap getFrame() {
		byte[] pixelData = ScreenUtils.getFrameBufferPixels( true );
		Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Format.RGBA8888);
		ByteBuffer pixels = pixmap.getPixels();
		pixels.clear();
		pixels.put(pixelData);
		pixels.position(0);
		
		Pixmap filter = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Format.RGBA8888);
		filter.setColor(0f, 0f, 0f, .5f);
		filter.fill();
		
		pixmap.drawPixmap(filter, 0, 0);
		
		filter.dispose();
		
        return pixmap;
	} 
}
