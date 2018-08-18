package com.lksfx.virusByte.gameObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters.LoadedCallback;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader.TextureAtlasParameter;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;

/**
 * Load a texture at runtime, very useful when a virus depends of a specific texture.
 * The virus only spawn after the texture be loaded.
 * Recommended use only on virus like boss that use an entire texture atlas.
 * */
public class RuntimeDependencyLoader {
	private final boolean debug = true; 
	/*This virus depends of a specific atlas and need wait it load
	private Assets.Atlas textureAtlas;*/
	/**If a virus need be spawned as soon as the texture atlas is loaded that virus stored on a array until can be spawned*/
	private Array<VirusInstance> spawnQueue;
	/**Load state*/
	public boolean isLoaded;
	
	private Array<DependencyListener<?>> dependencyList;
	/**Spawning is required as soon the texture atlas is loaded*/
	private boolean forceSpawning;
	
	public RuntimeDependencyLoader(boolean forceSpawning, DependencyListener<?>... dependencies) {
//		textureAtlas = atlas;
		spawnQueue = new Array<VirusManager.VirusInstance>();
		dependencyList = new Array<RuntimeDependencyLoader.DependencyListener<?>>();
		dependencyList.addAll(dependencies, 0, dependencies.length);
		//for (DependencyListener<?> dependency : dependencies) dependencyList.add( dependency );
		this.forceSpawning = forceSpawning;
		
		//Check if the dependencies for this object is already loaded on assets manager
		final int numberDependency = dependencyList.size;
		int loadedDependecy = 0;
		for (int i = 0; i < numberDependency ; i++) if ( dependencyList.get(i).isLoaded() ) loadedDependecy++;
		isLoaded = (numberDependency == loadedDependecy); 
	}
	
	/**Return true when the dependency asset is loaded*/
	public boolean update(float deltaTime) {
		/*if ( !isLoaded && textureAtlas != null ) {
			if ( Assets.manager.isLoaded(textureAtlas.path, TextureAtlas.class) ) {
				isLoaded = true;
				for (VirusInstance virus : spawnQueue) {
					VirusByteGame.VIRUS_MANAGER.addVirus(virus);
				}
				spawnQueue.clear();
			}
		}*/
		if ( !isLoaded && dependencyList != null ) {
			final int totalDependencys = dependencyList.size;
			int loadedDependencys = 0;
			for (int i = 0; i < totalDependencys; i++) {
				loadedDependencys += ( dependencyList.get(i).isLoaded() ) ? 1 : 0;
			}
			
			if ( loadedDependencys == totalDependencys ) {
				isLoaded = true;
				for (VirusInstance virus : spawnQueue) {
					VirusByteGame.VIRUS_MANAGER.addVirus(virus);
				}
				spawnQueue.clear();
			}
		}
		return isLoaded;
	}
	
	/**Put this dependency to the load queue
	 * @return if true the dependency is already loaded and the virus is not stored*/
	public boolean checkDependecy( VirusInstance instance ) {
		// put the dependency on the queue to load
		if ( !isLoaded ) {
			//Not loaded yet
			if ( forceSpawning ) {
				//Put every dependency to load
				for (int i = 0; i < dependencyList.size; i++) dependencyList.get(i).load();
				if ( !spawnQueue.contains(instance, true) ) spawnQueue.add( instance );
				if ( debug ) Debug.log( instance + " dependencys are not loaded yet! " + spawnQueue.size );
				/*Assets.manager.load(textureAtlas.path, TextureAtlas.class);
				if ( !spawnQueue.contains(instance, true) ) spawnQueue.add( instance );
				if ( debug ) Debug.log( instance + " atlas is not loaded yet! " + spawnQueue.size );*/
			}
			return false;
		} else {
			//Already loaded
			if ( debug ) Debug.log( instance + " dependencys loaded successfully! " + spawnQueue.size );
			return true;
		}
	}
	
	/**Unload unused dependency texture atlas asset*/
	public void unload(VirusInstance instance) {
		/*if ( Assets.manager.isLoaded(textureAtlas.path, TextureAtlas.class) ) {
			Assets.manager.unload(textureAtlas.path);
			if ( debug ) Debug.log( instance + " dependency is unloaded." );
		}*/
		for (int i = 0; i < dependencyList.size; i++) dependencyList.get(i).unload();
		isLoaded = false;
		if ( debug ) Debug.log( instance + " dependency is unloaded." );
	}
	
	/**
	 * Check if a dependency is already load, or when finish load 
	 * @author Lucas
	 */
	public static class DependencyListener<T> {
		private AssetManager assets = VirusByteGame.ASSETS.getAssetManager();
		private boolean debug = true;
		/**Is this dependency terminated of load*/
		private boolean finishedLoad;
		/**Is this asset already on load queue?*/
		private boolean loading;
		private String resourceName;
		private String path;
		private Class<T> classe;
		private AssetDescriptor<?> descriptor;
		
		public DependencyListener(String path, Class<T> type) {
			this.path = path;
			classe = type;
			Pattern p = Pattern.compile( "[\\w\\d-_]+\\.\\w+" );
			Matcher m = p.matcher( path );
			resourceName = ( m.find() ) ? m.group() : "resourceFile";
			
			LoadedCallback callback = new LoadedCallback() {
				@Override
				public void finishedLoading(AssetManager assetManager, String fileName, @SuppressWarnings("rawtypes") Class type) {
					finishedLoad = true;
					if ( debug ) Debug.log( resourceName + " asset is loaded" );
				}
			};
			if ( type == TextureAtlas.class ) {
				TextureAtlasParameter params = new TextureAtlasParameter();
				params.loadedCallback = callback;
				descriptor = new AssetDescriptor<TextureAtlas>(path, TextureAtlas.class,  params);
			} else if ( type == Texture.class ) {
				TextureParameter params = new TextureParameter();
				params.loadedCallback = callback;
				descriptor = new AssetDescriptor<Texture>(path, Texture.class,  params);
			} else {
				Debug.log( "the class type: " + type.toString() + " won't correspond to any available dependecy file type." );
			}
			
			//Check if this dependency is already loaded
			finishedLoad = ( assets.isLoaded(path, classe) ); 
		}
		
		public void load() {
			if ( finishedLoad || loading ) return;
			if ( !assets.isLoaded(path, classe) ) {
				assets.load( descriptor );
				loading = true;
			} else {
				Debug.log( resourceName + " asset already loaded!" );
			}
		}
		
		/**@return true if is the asset loaded*/
		public boolean isLoaded() {
			return finishedLoad;
		}
		
		public void unload() {
			if ( assets.isLoaded(path, classe) ) {
				assets.setReferenceCount(path, 0);
				assets.unload( path );
				if ( debug ) {
					Debug.log( "unloaded " + resourceName );
					boolean bool = assets.isLoaded(path, classe);
					Debug.log( "Is resource loaded on asset manager? " + bool );
					if ( bool ) Debug.log( "The asset " + resourceName + " has how many references? " + assets.getReferenceCount(path) );
				}
				finishedLoad = false;
				loading = false;
			}
		}
		
	}
}
