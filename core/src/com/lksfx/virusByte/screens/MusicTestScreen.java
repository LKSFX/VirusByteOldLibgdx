package com.lksfx.virusByte.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;

public class MusicTestScreen extends ScreenAdapter {
	
	SpriteBatch batch;
	Music music;
	private AssetManager manager;
	
	public MusicTestScreen( VirusByteGame game ) {
		batch = game.batch;
		VirusByteGame.ASSETS.getAssetManager();
		manager.load(Assets.Music.fandangos.path, Music.class);
	}
	
	@Override
	public void render(float delta) {
		if ( manager.isLoaded(Assets.Music.fandangos.path, Music.class) ) {
			if ( music == null ) {
				music = manager.get(Assets.Music.fandangos.path, Music.class);
				music.play();
				music.setVolume(0f);
			} else {
				float vol = music.getVolume();
				if ( vol < .8f) music.setVolume( vol += .001f );
			}
		} else {
			manager.update();
		}
	}
	
	@Override
	public void dispose() {
		music.stop();
		music.dispose();
	}
}
