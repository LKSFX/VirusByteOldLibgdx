package com.lksfx.virusByte.effects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters.LoadedCallback;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

/**
 * BackRender is a class used to draw everything in the background or foreground
 * @author LKSFX
 */
public class BackgroundRenderer implements Comparable<BackgroundRenderer> {
	private final boolean debug = false;
	public float x, y, scrollTimerV, scrollSpeedV, scrollTimerU, scrollSpeedU, 
		timer, duration, lengthV = 1f, lengthU = 1f, rotation, alpha;
	protected int width, height;
	
	public Vector2 scrollSpeedRange;
	public Vector2 durationRange;
	public Vector2 animationSpeedRange;
	public Vector2 alphaMetaRange;
	public Vector2 alphaOperatorRange;
	
	private String animationName = "null name";
	
	//Actions
	private Array<Action> onLoadAction;
	private Array<Action> onRenderAction;
	private Array<Action> onEndAction;
	
	public boolean isLoaded; //is true when all the frames of this background is loaded
	
	// ============= //
	// all the attributes that can be stored to set when the textures are loaded and animation is created
	// =========== //
	
	public boolean isActive, visible = true, fade;
	protected boolean end;
	protected float alphaMeta = .9f;
	protected float alphaOperator;
	
	protected float elapsedTime;
	protected Color color = new Color(Color.WHITE);
	
	/**the layer that this background will be draw*/
	public int depth;
	
	private AssetManager manager;
	public RendererConfig config;
	
	/**animated sprite instance*/
	protected Animation animation;
	
	/**array with descriptors to load the textures*/
	Array< AssetDescriptor<Texture> > descriptors;
	/**The file path for each animation frame when load*/
	String[] framePaths;
	TextureRegion[] frames; 
 	
	public BackgroundRenderer(Sprite sprite, float scrollSpeed, float duration, int depth, boolean fade) {
		width = sprite.getRegionWidth();
		height = sprite.getRegionHeight();
		this.animation = new Animation(0f, sprite);
		onLoadAction = new Array<BackgroundRenderer.Action>();
		onRenderAction = new Array<BackgroundRenderer.Action>();
		onEndAction = new Array<BackgroundRenderer.Action>();
		this.scrollSpeedV = scrollSpeed;
		this.duration = duration;
		this.depth = depth;
		this.fade = fade;
		isLoaded = true;
		isActive = true;
	}
	/////////////// A1 //////////////////
	public BackgroundRenderer(String[] paths, Vector2 scrollSpeed, Vector2 duration, boolean loadNow) {
		this.scrollSpeedRange = scrollSpeed;
		this.durationRange = duration;
		this.animationSpeedRange = new Vector2(1/15f, 1/15f);
		this.alphaMetaRange = new Vector2(.7f, 1f);
		this.alphaOperatorRange = new Vector2(.001f, .05f);
		float 
		s1 = (MathUtils.randomBoolean())? MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y) : -MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y),
		s2 = (MathUtils.randomBoolean())? MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y) : -MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y),
		d = MathUtils.random(durationRange.x, durationRange.y);
		initialize(paths, s1, s2, d, 0, true, null, loadNow);
	}
	
	public void set(float hScrollSpeed, float vScrollSpeed, float duration) {
		initialize(framePaths, hScrollSpeed, vScrollSpeed, duration, 0, true, null, true);
	}
	////////////////////////////////////
	
	////////////// A2 //////////////////
	public BackgroundRenderer(String[] paths, Vector2 scrollSpeedRange, Vector2 durationRange, RendererConfig config, boolean loadNow) {
		this(paths, scrollSpeedRange, durationRange, 0, true, config, loadNow);
	}
	
	public void set(float hScrollSpeed, float vScrollSpeed, float duration, RendererConfig config) {
		set(vScrollSpeed, hScrollSpeed, duration, 0, true, config);
	}
	//////////////////////////////////
	
	///////////// A3 /////////////////
	public BackgroundRenderer(String[] paths, Vector2 scrollSpeedRange, Vector2 durationRange, int depth, boolean fade, boolean loadNow) {
		this.scrollSpeedRange = scrollSpeedRange;
		this.durationRange = durationRange;
		this.animationSpeedRange = new Vector2(1/15f, 1/15f);
		this.alphaMetaRange = new Vector2(.7f, 1f);
		this.alphaOperatorRange = new Vector2(.001f, .05f);
		float 
		s1 = (MathUtils.randomBoolean())? MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y) : -MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y),
		s2 = (MathUtils.randomBoolean())? MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y) : -MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y),
		d = MathUtils.random(durationRange.x, durationRange.y);
		initialize(paths, s1, s2, d, depth, fade, null, loadNow);
	}
	
	public void set(float hScrollSpeed, float vScrollSpeed, float duration, int depth, boolean fade) {
		initialize(framePaths, hScrollSpeed, vScrollSpeed, duration, depth, fade, null, true);
	}
	///////////////////////////////
	
	//////////// A4 ///////////////
	public BackgroundRenderer(String[] paths, Vector2 scrollSpeed, Vector2 duration, int depth, boolean fade, RendererConfig config, boolean loadNow) {
		this.scrollSpeedRange = scrollSpeed;
		this.durationRange = duration;
		this.animationSpeedRange = (config != null) ? new Vector2( config.animSpeedRange ) : new Vector2(1/15f, 1/15f);
		this.alphaMetaRange = (config != null) ? new Vector2( config.alphaMetaRange ) : new Vector2(.7f, 1f);
		this.alphaOperatorRange = (config != null) ? new Vector2( config.alphaOperatorRange ) : new Vector2(.001f, .05f);
		float 
		s1 = (MathUtils.randomBoolean())? MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y) : -MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y),
		s2 = (MathUtils.randomBoolean())? MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y) : -MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y),
		d = MathUtils.random(durationRange.x, durationRange.y);
		initialize(paths, s1, s2, d, depth, fade, config, loadNow);
	}
	
	public void set(float hScrollSpeed, float vScrollSpeed, float duration, int depth, boolean fade, RendererConfig config) {
		initialize(framePaths, hScrollSpeed, vScrollSpeed, duration, depth, fade, config, true);
	}
	
	/**Load effect with all default values*/
	public void set() {
		float 
		s1 = (MathUtils.randomBoolean())? MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y) : -MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y),
		s2 = (MathUtils.randomBoolean())? MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y) : -MathUtils.random(scrollSpeedRange.x, scrollSpeedRange.y),
		d = MathUtils.random(durationRange.x, durationRange.y);
		initialize(framePaths, s1, s2, d, depth, fade, null, true);
	}
	/////////////////////////////
	
	public void initialize(String[] paths, float hScrollSpeed, float vScrollSpeed, float duration, 
			int depth, boolean fade, RendererConfig config, boolean loadNow) {
		if ( onLoadAction == null ) onLoadAction = new Array<BackgroundRenderer.Action>();
		if ( onRenderAction == null ) onRenderAction = new Array<BackgroundRenderer.Action>();
		if ( onEndAction == null ) onEndAction = new Array<BackgroundRenderer.Action>();
		
		// Warning> this part of code run every time this animation is started
		scrollSpeedU = hScrollSpeed;
		scrollSpeedV = vScrollSpeed;
		this.duration = duration;
		this.depth = depth;
		this.fade = fade;
		
		//>> set configuration object
		boolean bool = ( config != null );
		this.config =  ( bool ) ? new RendererConfig( config ) : (( this.config != null ) ? this.config : new RendererConfig());
//		Debug.log( "config is not null? " + (bool || this.config != null) );
//		Debug.log( "is config.visible = " + this.config.visible );
		//<</
		
		manager = VirusByteGame.ASSETS.getAssetManager();
		if ( loadNow ) {
			toLoad(paths);
			for (int i = 0; i < onLoadAction.size; i++) onLoadAction.get(i).execute( this ); //Execute onLoad actions
		} else {
			//>> put here things that are to happen only when this object is instantiated
			//inside this brackets the code happen only once, on instantiation
			Pattern pattern = Pattern.compile( "(?<=\\/)[^\\d\\.]+" );
			Matcher matcher = pattern.matcher( paths[0] );
			if ( matcher.find() ) {
				animationName = matcher.group();
			}
			//<</
		}
		framePaths = paths;
	}
	
	/**the background folder inside application*/
	private final String pathFolder = "data/sprites/backgrounds/hypo/";
	
	/**create descriptors and put them to load in assetManager*/
	private void toLoad(String[] pathNames) {
		if ( isLoaded ) return;
		//check if directory exists
		isActive = (pathNames != null) ? true : false;
		if ( !isActive ) {
			if ( debug ) Debug.log( "not active on ToLoad Method!s" );
			return;
		}
		
		descriptors = new Array< AssetDescriptor<Texture> >(pathNames.length);
		frames = new TextureRegion[pathNames.length];
		
		TextureParameter params = new TextureLoader.TextureParameter();
		params.magFilter = TextureFilter.Linear;
		params.minFilter = TextureFilter.Linear;
		params.wrapU = TextureWrap.Repeat;
		params.wrapV = TextureWrap.Repeat;
		
		// CALLBACK START
		params.loadedCallback = new LoadedCallback() {
			@Override
			public void finishedLoading(AssetManager assetManager, String fileName, @SuppressWarnings("rawtypes") Class type) {
				for ( AssetDescriptor<Texture> desc : descriptors ) {
					if ( fileName.equals( desc.fileName ) && type.equals( Texture.class ) ) {
						if ( debug ) Debug.log("the " + desc.fileName + " background texture has loaded.");
						
						Pattern pattern = Pattern.compile( "[0-9]{1,2}(?=\\.(?:png|jpg))" );
						Matcher matcher = pattern.matcher(fileName);
						if ( matcher.find() ) {
							//File has index
							Debug.log( "matcher is " + matcher.group() );
							int frameIndex = Integer.valueOf( matcher.group() ); //(descriptors.size > 1) ? Integer.valueOf( fileName.replaceAll("[^0-9]", "") ) : 1 ;
							if ( debug ) Debug.log("frameIndex " + (frameIndex-1));
							frames[frameIndex-1] = new TextureRegion( assetManager.get(fileName,  Texture.class) );
						} else {
							//File without index
							// in this case the assortment of the frames is randomized
							for ( int i = 0; i < frames.length; i++ ) {
								if ( frames[i] == null ) {
									frames[i] = new TextureRegion( assetManager.get(fileName,  Texture.class) );
									break; //only one time on array
								}
							}
						}
						
						if ( !isLoaded ) {
							for ( int i = 0; i < frames.length; i++ ) {
								if (frames[i] == null) {
									if ( debug ) Debug.log( "frame " + i + " is null< " );
									return;
								}
							}
							isLoaded = true; //check if all the frames are loaded
							Debug.log( "number of frames is " + frames.length );
						}
					}
					
					if ( isLoaded ) {
						/*if ( config != null && config.wrapmode == TextureWrap.ClampToEdge ) {
							for (TextureRegion tex : frames) {
								tex.setRegionWidth( (int)VirusByteGame.VIEWPORT.getWorldWidth() );
								tex.setRegionHeight( (int)VirusByteGame.VIEWPORT.getWorldHeight() );
							}
						}*/
						float animSpd = (animationSpeedRange.x == animationSpeedRange.y) ? animationSpeedRange.x 
								: MathUtils.random(animationSpeedRange.x, animationSpeedRange.y);
						animSpd = ( temp_animSpd != 0 ) ? temp_animSpd : animSpd;
						width = Math.round( config.getAnimationSize().x );
						height = Math.round( config.getAnimationSize().y );
						animation = new Animation(animSpd, frames);
						animation.setPlayMode( config.playmode );
						visible = config.visible;
//						sprite.setAlpha(alpha);
						timer = 0; // set the time to zero to begin draw the texture
						temp_animSpd = 0; // reset this value every on load complete
					}
				}
			}
		};
		//CALLBACK END
		
		//put frames to load
		/// START OF LOOP
		for (int i = 0; i < pathNames.length; i++) {
			//counting occurrence of character with loop
			
			String path = pathNames[i];
			
			//check how many slashs have the path to determine if this is absolute or not
			boolean isAbsolute = path.contains(".png");
			//int slashCount = 0;
			/*for ( int j = 0; j < path.length() ; j++) {
				slashCount += ( path.charAt(j) == '/') ? 1 : 0;
			}*/
			
			path = (isAbsolute) ? path : pathFolder + path + ".png"; 
			descriptors.add( new AssetDescriptor<Texture>(path, Texture.class, params ) ); 
			
			if ( manager.isLoaded(path, Texture.class) ) {
				//file already loaded
				frames[i] = new TextureRegion( manager.get(path, Texture.class) );
				continue;
			}
			manager.load(descriptors.get(i));
			if ( debug ) Debug.log("asset put to load " );
		}
		/// END OF LOOP
		
	}
	
	public void draw(SpriteBatch batch, float deltaTime) {
		if ( isLoaded ) {
			elapsedTime += deltaTime;
			// when the texture is already loaded
			// the texture is draw on screen
			if ( end ) {
				// when the background render time end, fade out alpha
				if ( fade ) { // fade out active
					alpha -= alphaOperator;
					color.a = alpha;
//					sprite.getKeyFrame( elapsedTime ).setAlpha(alpha);
					if ( alpha < 0 ) 
						finalize();
				} else {
					visible = false;
					finalize();
				}
			} else {
				if ( alpha < alphaMeta ) {
					// slowly increase the alpha of this sprite 
					if ( fade ) {
						alpha += alphaOperator;
						if ( alpha > 1f ) alpha = 1f; 
//						sprite.getKeyFrame(  ).setAlpha(alpha);
						color.a = alpha;
					} else {
						if ( alphaMeta != 0 ) {
							alpha = alphaMeta;
							color.a = alpha;
//							sprite.setAlpha(alpha);
						}
					}						
				}
				if ( duration > 0 && timer > duration ) 
					terminate();
			}
			//draw the sprite if the sprite is not null
//			if ( duration != 0 ) Debug.log( animationName + " animation life: " + timer );
			
			if ( visible ) {
				batch.setColor(color);
				TextureRegion region = animation.getKeyFrame(elapsedTime);
				if ( (rotation += 10 * deltaTime) > 360f ) rotation = 0;
				region.setRegionWidth( width );
				region.setRegionHeight( height );
				batch.draw(region, x, y);
				/*batch.draw(region.getTexture(), x, y, 0f, 0f, width, height, 
						1f, 1f, 0f, region.getRegionX(), region.getRegionY(), width, height, false, false);*/
				batch.setColor(1f, 1f, 1f, 1f);
//				Debug.log( "width: " + width + " | height: " + height + " alpha: " + alpha );
			}
			//vertical texture scroll
			if (scrollSpeedV != 0) {
//				if ( descriptors != null ) Debug.log("scroll V speed " + scrollSpeedV);
				scrollTimerV += scrollSpeedV*deltaTime;
				for (TextureRegion region : animation.getKeyFrames()) {
					region.setV(scrollTimerV);
					region.setV2(scrollTimerV+lengthV);
				}
			}
			// horizontal texture scroll
			if (scrollSpeedU != 0) {
//				if ( descriptors != null ) Debug.log("scroll U speed " + scrollSpeedU);
				scrollTimerU += scrollSpeedU*deltaTime;
				for (TextureRegion region : animation.getKeyFrames()) {
					region.setU(scrollTimerU);
					region.setU2(scrollTimerU+lengthU);
				}
			}
			
			timer += deltaTime;
			for (int i = 0; i < onRenderAction.size; i++) onRenderAction.get(i).execute( this );
		}
	}
	
	/**Return the name of this animation*/
	public String getName() {
		return animationName;
	}
	
	
	/** set the alpha value for increment and decrement, fade in/out
	 * @param alphaMeta is the max total amount of alpha to fill in the fade in
	 * @param alphaOperator is the amount to increase and decrease in the fade in/out each step*/
	public void setAlphaAttribute(float alphaMeta, float alphaOperator) {
		this.alphaMeta = alphaMeta;
		this.alphaOperator = alphaOperator;
	}
	
	private float temp_animSpd;
	
	public void setAnimationSpeed(float spd) {
		temp_animSpd = spd;
		if ( animation != null ) {
			animation.setFrameDuration( spd );
		}
	}
	
	public void setTextureBrowser(float horizontalSpeed, float verticalSpeed) {
		scrollSpeedV = verticalSpeed;
		scrollSpeedU = horizontalSpeed;
	}
	
	public void setSize(float width, float height) {
		this.width = Math.round( width );
		this.height = Math.round( height );
		if ( animation != null ) {
			TextureRegion[] regions = animation.getKeyFrames();
			for (int i = 0; i < regions.length; i++) {
				regions[i].setRegionWidth( this.width );
				regions[i].setRegionHeight( this.width );
			}
		}
	}
	
	public void setPosition(float xx, float yy) {
		x = xx;
		y = yy;
		if (animation != null) {
//			sprite.setPosition(x, y);
		}
	}
	
	public void setRegion(float u, float v, float u2, float v2) {
		if (animation == null) return;
//		sprite.setRegion(u, v, u2, v2);
	}
	
	/**Set the animation color*/
	public void setColor(Color color) {
		this.color = color;
	}
	
	/**Set the animation alpha*/
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	
	/**Set <b>end = true</b> and if <b>fade</b> is activated execute an {fade out} effect until end this animation*/
	public void terminate() {
		if ( end ) 
			return;
		for ( int i = 0; i < onEndAction.size; i++ ) 
			onEndAction.get(i).execute(this);
		end = true;
	}
	
	@Override
	public int compareTo(BackgroundRenderer other) {
		return this.depth - other.depth;
	}
	
	/** ends the use of this background */
	public void finalize() {
		if ( fade && alpha > 0) 
			return;
		dispose();
		isActive = false;
	}
	
	public void dispose() {
		if ( descriptors == null || descriptors.size == 0 ) return;
			
		for ( AssetDescriptor<Texture> desc : descriptors ) {
			
			// only if the asset patch are been used
			String assetPatch = desc.fileName;
			if ( manager.isLoaded(assetPatch) ) {
				int currentTextureReferences = manager.getReferenceCount(assetPatch);
				manager.setReferenceCount(assetPatch, currentTextureReferences-1);
				if ( currentTextureReferences-1 <= 0 ) {
					if ( VirusByteGame.BACK.unloadBackgroundTextureAssets(assetPatch) ) {
						if ( debug ) Debug.log("background asset not used unloaded!");
						if ( debug ) Debug.log("the " + assetPatch + " background texture has unloaded.");
					}
				}
			}
			
		} 
		
		visible = false;
	}
	
	/**Reset all status to default, to reutilization*/
	public void resetAll() {
		// Reset floats
		x = 0;
		y = 0;
		scrollTimerV = 0;
		scrollSpeedV = 0; 
		scrollTimerU = 0; 
		scrollSpeedU = 0; 
		timer = 0; 
		duration = 0; 
		temp_animSpd = 0;
		lengthV = 1f;
		lengthU = 1f; 
		alpha = 0;
		alphaMeta = .9f; 
		alphaOperator = 0;
		// Reset booleans
		isLoaded = false;
		isActive = false; 
		visible = true; 
		fade = false;
		end = false;
		// Reset Integers
		depth = 0;
		// Reset objects
		manager = null;
		config = null;
		animation = null;
		descriptors = null;
		frames = null; 
	}
	
	/**Reset only the necessary but not the main configuration
	 * @return this background*/
	public BackgroundRenderer reset() {
		// boolean values
		isLoaded = false;
		isActive = true;
		visible = true;
		end = false;
		// float values
		timer = 0;
		alpha = 0;
		temp_animSpd = 0;
		alphaMeta = MathUtils.random(alphaMetaRange.x, alphaMetaRange.y);
		alphaOperator = MathUtils.random(alphaOperatorRange.x, alphaOperatorRange.y);
		duration = MathUtils.random(durationRange.x, durationRange.y);
		return this;
	}
	
	// ===== Action ===== //
	/**Add an action to execute when this animation is load*/
	public void addOnLoadAction(Action action) {
		onLoadAction.add( action );
		if ( debug ) 
			Debug.log( "action added on " + getName() + " animation " );
	}
	
	/**Add an action to execute every frame when rendering*/
	public void addOnRenderAction(Action action) {
		onRenderAction.add( action );
	}
	
	/**Add an action to execute when this animation end*/
	public void addOnEndAction(Action action) {
		onEndAction.add( action );
		if ( debug ) Debug.log( "action added on " + getName() + " animation" );
	}
	
	public void removeAction(Action action) {
		if ( !onLoadAction.removeValue(action, true) ) {
			if ( !onEndAction.removeValue(action, true) ) {
				onRenderAction.removeValue(action, true);
			}
		}
	}
	
	public void removeAllActions() {
		onLoadAction.clear();
		onEndAction.clear();
	}
	
	/**Interface for actions*/
	public static interface Action {
		public void execute(BackgroundRenderer render);
	}
	// <<<<<<<<<<<<<<<<<< //
	
	/**
	 *  This class pass all the necessary configuration information for the AnimatedSpriteBackground
	 * @author LKSFX
	 */
	public static class RendererConfig {
		public float width, height, originX, originY;
		public Vector2 animSpeedRange;
		public Vector2 alphaMetaRange;
		public Vector2 alphaOperatorRange;
		public boolean animated;
		public boolean visible;
		public Animation.PlayMode playmode;
		public Texture.TextureWrap wrapmode;
		
		public RendererConfig() {
			this(VirusType.WORLD_WIDTH, VirusType.WORLD_HEIGHT, 0, 0, new Vector2(.066f, .066f), new Vector2 (.7f, 1f), new Vector2(.001f, .05f),
					Animation.PlayMode.LOOP, TextureWrap.Repeat, false, true);
		}
		
		public RendererConfig(boolean animated, float animSpeed) {
			this(VirusType.WORLD_WIDTH, VirusType.WORLD_HEIGHT, 0, 0, new Vector2(animSpeed, animSpeed), new Vector2 (.7f, 1f), new Vector2(.001f, .05f),
					Animation.PlayMode.LOOP, TextureWrap.Repeat, animated, true);
		}
		
		public RendererConfig(boolean animated, float animSpeed, float width, float height) {
			this(width, height, 0, 0, new Vector2(animSpeed, animSpeed), new Vector2 (.7f, 1f), new Vector2(.001f, .05f),
					Animation.PlayMode.LOOP, TextureWrap.Repeat, animated, true);
		}
		
		public RendererConfig(RendererConfig cf) {
			this(cf.width, cf.height, cf.originX, cf.originY, new Vector2( cf.animSpeedRange ),
					new Vector2( cf.alphaMetaRange ), new Vector2( cf.alphaOperatorRange ), cf.playmode, cf.wrapmode, cf.animated, cf.visible);
		}
		
		/**
		 * 
		 * @param playmode the animation play mode 
		 * @param wrapmode the wrap type in the texture
		 * @param animated if this background is animated
		 * @param visible TODO
		 * @param regionWith the width of each square if tiled
		 * @param regionHeight the height of each square if tiled
		 * @param regionOriginX the x origin inside the texture
		 * @param regionOriginY the y origin inside the texture
		 * @param spacingU the horizontal space between tiles
		 * @param spacingV the vertical space between tiles
		 * @param animSpeed speed of the animation
		 */
		public RendererConfig(float width, float height,
				float originX, float originY, Vector2 animSpeedRange, Vector2 alphaMetaRange, Vector2 alphaOperatorRange,
				PlayMode playmode, TextureWrap wrapmode, boolean animated, boolean visible) {
			this.width = width;
			this.height = height;
			this.originX = originX;
			this.originY = originY;
			this.animSpeedRange = animSpeedRange;
			this.alphaMetaRange = alphaMetaRange;
			this.alphaOperatorRange = alphaOperatorRange;
			this.animated = animated;
			this.playmode = playmode;
			this.wrapmode = wrapmode;
			this.visible = visible;
		}
		
		private Vector2 vec = new Vector2();
		
		/**Return the size of this animation, if not defined return the size of the viewport(width, height)*/
		public Vector2 getAnimationSize() {
			return vec.set(( width != 0 ) ? width : VirusType.WORLD_WIDTH, ( height != 0 ) ? height : VirusType.WORLD_HEIGHT);
		}
	}
	
}
