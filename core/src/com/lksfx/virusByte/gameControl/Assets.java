package com.lksfx.virusByte.gameControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader.ParticleEffectParameter;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;

public class Assets {
	private AssetManager manager;
	public final String gradientTexture = "data/particles/gradient.png";
	
	//Sprite
	public enum Atlas {
		iconsAtlas("data/sprites/icons/iconSheet.atlas"), virusAtlas("data/sprites/virus/virusSheet.atlas"),
		octocatAtlas("data/sprites/virus/octocat-boss.atlas"), baiduAtlas("data/sprites/virus/baidu-boss.atlas"),
		pepperBrosAtlas("data/sprites/virus/pepperbros-boss.atlas"), dragonAtlas("data/sprites/virus/dragon/dragon-boss.atlas"),
		hypoA("data/sprites/backgrounds/hypo01.atlas"), itemAtlas("data/sprites/virus/itemSheet.atlas");
		
		public String path;
		
		private Atlas(String patch) {
			this.path = patch;
		}
	}
	
	public enum Textures {
		circuitTexture("data/sprites/backgrounds/circuits.png"), Hidden1Tex("data/sprites/backgrounds/satanic_cat.png"),
		allSeeingEye("data/sprites/backgrounds/all_seeing_eye.png"), binaryTex("data/sprites/backgrounds/texture_binary.png");
		
		public String path;
		
		private Textures(String patch) {
			this.path = patch;
		}
	}
	
	public enum Particles {
		firework("firework", "data/particles/sparks.p", "data/particles"), explosion("explosion", "data/particles/explosion.p", "data/particles"),
		healingRing("healingRing", "data/particles/healingRing.p", "data/particles");
		
		public String path;
		public String type;
		private ParticleEffectParameter params = new ParticleEffectParameter();
		
		private Particles(String type, String patch, String imgPatch) {
			this.path = patch;
			this.type = type;
			params.imagesDir = Gdx.files.internal( imgPatch );
		}
	}
	
	public enum Audio {
		bip("bitclick sfx", "data/audio/sfx/bip.wav"), damage("damage sfx", "data/audio/sfx/damage.wav"), death1("death sfx", "data/audio/sfx/death1.wav"),
		death2("death sfx", "data/audio/sfx/death2.wav"), death3("death sfx", "data/audio/sfx/death3.wav"), 
		explosion("explosion sfx", "data/audio/sfx/explosion.wav"), lazer("lazer sfx", "data/audio/sfx/lazer.wav"), life("life sfx", "data/audio/sfx/life.wav");
		
		public String path;
		public String type;
		
		private Audio(String type, String patch) {
			this.type = type;
			this.path = patch;
		}
	}
	
	public enum Music {
		fandangos("fandangos", "data/audio/music/fandangos.ogg");
		
		public String path;
		public String type;
		
		private Music(String type, String patch) {
			this.type = type;
			this.path = patch;
		}
	}
	
	public enum Fonts {
		visitor20_bold("data/fonts/visitor20_bold.fnt");
		
		public String path;
		
		private Fonts(String patch) {
			this.path = patch;
		}
	}
	
	//Textures
//	public static final String circuitTexture = "data/sprites/backgrounds/circuits.png";
	
	public void dispose() {
		manager.dispose();
	}
	
	public void loadMainFont() {
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader( FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver) );
		manager.setLoader( BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver) );
		
		FreeTypeFontLoaderParameter fontParam = new FreeTypeFontLoaderParameter();
		fontParam.fontFileName = "data/fonts/visitor2.ttf";
		fontParam.fontParameters.size = 20;
		manager.load( "size20.ttf", BitmapFont.class, fontParam );
		for ( Fonts ft : Fonts.values() ) {
			manager.load( ft.path, BitmapFont.class );
		}
	}
	
	public void loadAssets() {
		//sprite atlas
		Atlas[] preLoadAtlas = new Atlas[] { Atlas.iconsAtlas, Atlas.itemAtlas, Atlas.virusAtlas};
		for ( Atlas spriteAtlas : preLoadAtlas ) {
			manager.load( spriteAtlas.path, TextureAtlas.class ); 
		}
		//load gradient particle image
		manager.load( gradientTexture, Texture.class );
		
		//particles
		for ( Particles particle : Particles.values() ) {
			manager.load( particle.path, ParticleEffect.class, particle.params );
		}
		//load sfx
		for ( Audio audio : Audio.values() ) {
			manager.load( audio.path, Sound.class );
		}

		loadMainFont();
	}
	
	/** Constructor
	 * create a new assetManager for the Assets class, might be used only in the aplication creation. 
	 */
	public Assets() {
		manager = new AssetManager();
		loadAssets();
		manager.finishLoading();
	}
	
	public void update() {
		//load assets, if necessary
		if ( manager.getQueuedAssets() > 0 ) {
			manager.update(); //loading assets
			//Debug.log("loading assets on the main asset update");
		}
	}
	
	/** Return the main asset manager from the game assets class */
	public AssetManager getAssetManager() {
		return manager;
	}
	
}
