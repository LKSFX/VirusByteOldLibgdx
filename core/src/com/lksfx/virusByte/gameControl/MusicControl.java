package com.lksfx.virusByte.gameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.debug.Debug;

public class MusicControl {
	
	private static boolean DEBUG = true;
	private Array< QueuedMusic > musicList;
	private AssetManager manager;
	public QueuedMusic lastMusicPlaying;
	
	public MusicControl() {
		// create a new music list
		musicList = new Array< QueuedMusic >();
		manager = VirusByteGame.ASSETS.getAssetManager();
		//
	}
	
	public void addToPlaylist(Assets.Music... assets) {
		for ( Assets.Music asset : assets ) {
			QueuedMusic musicToAdd = new QueuedMusic(asset);
			if ( !musicList.contains(musicToAdd, false) ) 
				musicList.add( musicToAdd );
		}
		if (!VirusByteGame.MUSIC) return;
		playRandomMusicOnList();
	}
	
	/** clear the music list  */
	public void clearMusicList() {
		musicList.clear();
	}
	
	public void update() {
		for (QueuedMusic music : musicList) {
			music.update();
//			if (music.toPlay) music.play();
		}
		
//		Debug.debug.screen("Master Volume: " + VirusByteGame.MASTER_VOLUME, 10, 340);
	}
	
	private void playRandomMusicOnList() {
		QueuedMusic songToPlay = musicList.get( MathUtils.random( Math.max(0, musicList.size -1) ) );
		songToPlay.inactiveTime = 0; //reset inactive time
		songToPlay.toPlay = true;
	}
	
	/** avoids that the current playing music is selected again */
	private boolean playRandomMusicOnList(QueuedMusic exception) {
		if (musicList.size == 0) {
			QueuedMusic songToPlay = musicList.get( MathUtils.random( Math.max(0, musicList.size -1) ) );
			songToPlay.inactiveTime = 0; //reset inactive time
			songToPlay.toPlay = true;
			return false;
		}
		QueuedMusic songToPlay = null;
		while ( songToPlay == null || songToPlay.equals(exception) ) {
			songToPlay = musicList.get( MathUtils.random( Math.max(0, musicList.size -1) ) );
		}
		songToPlay.inactiveTime = 0f;
		songToPlay.toPlay = true;
		return true;
	}
	
	public void resume() {
		if (lastMusicPlaying != null) {
			lastMusicPlaying.toPlay = true;
		} else {
			//start a random music in the music list
			//playRandomMusicOnList();
		}
	}
	
	public void pause() {
		for (QueuedMusic music : musicList) {
			if (music.toPlay) lastMusicPlaying = music;
			music.toPlay = false;
		}
		
	}
	
	public void stopAll() {
		for (QueuedMusic music : musicList) {
			lastMusicPlaying = null;
			music.toPlay = false;
		}
	}
	
	public void setVolume(float volume) {
		for (QueuedMusic music : musicList) {
			if (!music.loaded) continue;
			music.music.setVolume(volume);
		}
	}
	
	public int totalMusics() {
		return musicList.size;
	}
	
	/** return true if has some music playing */
	public boolean isPlaying() {
		boolean check = false;
		for (QueuedMusic music : musicList) check = music.isPlaying();
		return check;
	}
	
	private class QueuedMusic {
		private String path, musicName;
		private Music music;
		private boolean toPlay, loaded;
		private float playingTime, inactiveTime;
		
		public QueuedMusic(Assets.Music asset) {
			this.path = asset.path;
			this.musicName = asset.name();
			// check if this music asset is already loaded
			
			if ( DEBUG ) 
				Debug.log("Creating queuedMusic " + musicName + " object"); 
			
			if ( manager.isLoaded(path, Music.class) ) {
				music = manager.get( path, Music.class );
				music.setVolume(0f);
				loaded = true; 
				if ( DEBUG ) Debug.log("this music is already loaded and the internal music object is created and volume set to zero (0)"); 
			} else {
				// in case of not loaded, put in the load queue
				manager.load( path, Music.class );
				if ( DEBUG ) 
					Debug.log("music not loaded yet, put to load queue on asset manager");
			}
		}
		
		public void play() {
			if ( !loaded ) {
				//loading music asset
				if ( manager.isLoaded(path, Music.class) ) {
					music = manager.get( path, Music.class );
					if ( music != null ) {
						loaded = true; 
						music.setLooping(true);
						music.setVolume(0f);
						if ( DEBUG ) 
							Debug.log("music loaded! | 'loaded' var set to 'true' | loop set 'true' | volume set to zero 0f ");
					}
				} else {
					if ( DEBUG ) 
						Debug.log("music asset not loaded yet, waiting the next asset manager update");
				}
			} else {
				//asset loaded, play music in case to play
				if ( toPlay && !music.isPlaying() ) {
					music.setVolume(0f);
					music.play();
					if ( DEBUG ) 
						Debug.log("Inside play method >> music set to play(), volume set to zero 0");
				}
			}
		}
		
		public void update() {
			if ( toPlay ) play();
			// === //
			if ( loaded ) {
				if ( toPlay ) {
					// music is playing mode
					if ( !music.isPlaying() ) {
						music.play();
						if ( DEBUG ) Debug.log("Inside update method >> music set to play() | " + music);
					}
					fadeIn();
					//playingTime += Gdx.graphics.getDeltaTime();
					if (playingTime > 50f) {
						// try start a new song
						if ( playRandomMusicOnList(this) ) {
							//end this song if have another to start
							playingTime = 0;
							toPlay = false;
						} 
					}
				} else {
					// music is inactive state
					if ( music.isPlaying() ) {
						//fade out and pause
						fadeOut();
					} else {
						inactiveTime += Gdx.graphics.getDeltaTime();
						if ( inactiveTime > 10f ) {
							if ( manager.isLoaded(path, Music.class) ) {
								manager.setReferenceCount(path, -1);
								Debug.log("Unloaded " + musicName + " music " + manager.getReferenceCount(path));
								manager.unload(path);
								if ( DEBUG ) 
									Debug.log("music is inactive for more than 10f seconds, unload proccess started");
							}
						}
					}
				}
			}
			// === //
		}
		
		public void fadeOut() {
			float vol = music.getVolume(), setVolume = vol - .005f < 0 ? 0 : vol - 0.002f;
			if (vol > 0) music.setVolume(setVolume);
			if (setVolume == 0) music.pause(); 
		}
		
		public void fadeIn() {
			float vol = music.getVolume() + .001f;
			if (vol < VirusByteGame.MASTER_VOLUME) music.setVolume(vol);
		}
		
		public boolean isPlaying() {
			if (music == null) return false;
			return toPlay;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (getClass() != obj.getClass()) {
		        return false;
		    }
			QueuedMusic other = (QueuedMusic) obj;
			return other.path.equals(path);
		}
		
		@Override
		public int hashCode() {
			int hash = 3;
			hash = 53 * hash + (this.path != null ? this.path.hashCode() : 0);
			return hash;
		}
	}
	
}
