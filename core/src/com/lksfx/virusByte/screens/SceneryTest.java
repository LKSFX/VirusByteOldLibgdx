package com.lksfx.virusByte.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;

public class SceneryTest extends ScreenAdapter {
	
	private SpriteBatch batch;
	private Viewport viewport;
	
	public SceneryTest(VirusByteGame game) {
		batch = game.batch;
		viewport = game.viewport;
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix( viewport.getCamera().combined );
		batch.begin();
		batch.draw(VirusByteGame.ASSETS.getAssetManager().get(Assets.Atlas.virusAtlas.path, TextureAtlas.class)
				.findRegion("energyzer1"), 100, 100);
		batch.end();
	}
	
	
	@Override
	public void dispose() {
		
	}
}
