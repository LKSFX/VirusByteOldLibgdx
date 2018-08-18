package com.lksfx.virusByte.effects;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.BackgroundRenderer.Action;
import com.lksfx.virusByte.effects.BackgroundRenderer.RendererConfig;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.MyUtils;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class Backgrounds {
	public Texture background; 
	public Sprite tileBack, spriteBack, hide_background1, genericScreenSizeSprite;
	public Color backColor;
	public BackgroundRenderer circuitBackground, colorBackground, hide_background;
	public enum Layer {BACKGROUND, FOREGROUND};
	
	public enum Mode {
		MENU("main menu"), GAMESCREEN("Game mode");
		
		String menuName;
		String pass;
		
		private Mode(String menuName) {
			this.menuName = menuName;
		}
	}
	
	private float worldWidth, worldHeight;
	private ShaderProgram shader;
	private Array<BackgroundRenderer> backgrounds, foregrounds;
	private String circuitTexture = Assets.Textures.circuitTexture.path, hiddenTexture = Assets.Textures.allSeeingEye.path;
	
	public Backgrounds( SpriteBatch batch ) {
		worldWidth = VirusByteGame.VIEWPORT.getWorldWidth();
		worldHeight = VirusByteGame.VIEWPORT.getWorldHeight();
		backgrounds = new Array< BackgroundRenderer >(); // this array store all the background objects to render
		foregrounds = new Array< BackgroundRenderer >(); //this array store all the foregrounds objects to render
		genericScreenSizeSprite = makeSpriteShape(worldWidth+100f, worldHeight);
		boolean hasToLoadAssets = false;
		
		AssetManager manager = VirusByteGame.ASSETS.getAssetManager();
		
		// === load necessary assets === //
		if ( !manager.isLoaded(circuitTexture, Texture.class) ) {
			manager.load(circuitTexture, Texture.class); 
			hasToLoadAssets = true;
		}
		
		if ( !manager.isLoaded(hiddenTexture, Texture.class) ) {
			manager.load(hiddenTexture, Texture.class); 
			hasToLoadAssets = true;
		}
		
		if ( hasToLoadAssets ) {
			Debug.log("the background images has been loaded.");
			manager.finishLoading();
		}
		
		// ===                      === //
		background = manager.get(circuitTexture, Texture.class); //load background texture
		hide_background1 = new Sprite( manager.get(hiddenTexture, Texture.class), 0, 0, 480, 640); //hide image 
//		hide_background1.setAlpha(0.15f);
		background.setWrap(TextureWrap.Repeat, TextureWrap.Repeat); //set this texture to wrap repeatedly
		spriteBack = new Sprite(background, 0, 0, 1024, 1024);
		backColor = new Color(125f/255f, 245/255f, 125f/255f, 0.9f);
		shader = new ShaderProgram(Gdx.files.internal("data/shaders/customShader.vsh"), Gdx.files.internal("data/shaders/customShader.fsh"));
//		Debug.log(shader.isCompiled() ? "custom shaders has compiled!!" : shader.getLog());
		ShaderProgram.pedantic = false;
		shader.begin();
		shader.setUniformf("u_resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		shader.end();
		
		spriteBack.setAlpha(0.9f);
		tileBack = new Sprite( genericScreenSizeSprite );
		tileBack.setColor(backColor);
		
		//random position UV of the spriteBack *circuit texture*
		rotateCircuitTexture = MathUtils.randomBoolean();
		if ( rotateCircuitTexture )spriteBack.rotate(90);
		circuitBackground =  new BackgroundRenderer(spriteBack, 0, 0, 5, false);
		colorBackground = new BackgroundRenderer(tileBack, 0, 0, 10, false);
		hide_background = new BackgroundRenderer(hide_background1, 0, 0, 0, false);
		//create and add the three main backgrounds to the back array
		addBackground( colorBackground );
		addBackground( circuitBackground );
		addBackground( hide_background );
		if (hide_background != null) hide_background.setPosition((VirusByteGame.VIEWPORT.getWorldWidth()*.5f)-(hide_background1.getWidth()*.5f), 0);
		float worldWidth = VirusByteGame.VIEWPORT.getWorldWidth();
		circuitBackground.setPosition( MathUtils.random( -(spriteBack.getWidth() - worldWidth), 0), 0);
		//circuitBackground.lengthV = .8f; //set the vertical draw range
		//circuitBackground.lengthU = .9f; //set the horizontal draw range
		
		setBackColor();
		setAnimationLists();
		Preferences prefs = Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE);
		activeRandomBackground = prefs.getBoolean("animations-active", true);
		/*for (Mode val : Mode.values())
			Debug.log("Modes: "+ val.menuName);*/
	}
	/**When the circuit texture is rotated 90 degrees this variable is true*/
	boolean rotateCircuitTexture;
	final boolean scrollCircutTexture = true;
	/**Activate or deactivate random background animations*/
	public boolean activeRandomBackground;
	
	public void update(float deltaTime) {
		//update stage
		int gameStage = VirusByteGame.VIRUS_MANAGER.stage;
		if ( scrollCircutTexture && stage != gameStage ) {
			/*if ( rotateCircuitTexture ) {
				circuitBackground.scrollSpeedU = -(.025f * gameStage);
			} else {
			}*/
			circuitBackground.scrollSpeedV = .025f * gameStage;
			stage = gameStage;
			
		}
		
		displayTimer += deltaTime;
		
		//hide_background1.draw(batch);
		//make/show a especial background animation
		if ( activeRandomBackground && stage >= 1 && displayTimer >= espBackDisplayTime) {
			if ( especialRandomBackground ) 
				randomBackground();
			espBackDisplayTime = displayTimer + ( 10f+MathUtils.random(40f) ); //interval between show back/foreground effects
		}
		if ( Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) ) {
			/*if ( Gdx.input.isKeyJustPressed(Keys.E) || (Gdx.app.getType() == ApplicationType.Android && Gdx.input.justTouched() ) ) {
				BackRender back;
				VirusByteGame.BACK.addBackground(back = new Backgrounds.BackRender(Assets.Textures.binaryTex, 0, 0, 15, true) ); // add background
				back.setAlphaAttribute(.65f, .005f);
				back.setSize(worldWidth, worldHeight);
				//addForeground( new ForeEffectRender(makeSpriteShape(worldWidth, worldHeight), Effect.lazer, 1f, 1) );
				BackgroundRenderer back;
				RendererConfig config = new RendererConfig(true, 1/15f);
				addForeground(back = new BackgroundRenderer(hypo, new Vector2(), new Vector2(15f, 15f), 15, true, config, true) );
				back.setAlphaAttribute(.9f, .001f);
				back.setTextureBrowser(.1f, .2f);
			}*/
			/*if ( Gdx.input.isKeyJustPressed(Keys.I) ) {
				//debug information
				if (loadedAssets.size > 0) {
					for (AssetDescriptor<Texture> assetName : loadedAssets) {
						Debug.log("asset name: " + assetName.fileName + " | total references: " + Assets.manager.getReferenceCount(assetName.fileName));
					}
				}
			}*/
			/*if ( Gdx.input.isKeyJustPressed(Keys.P) ) removeBackground( Animations.Frozen );
			if ( Gdx.input.isKeyJustPressed(Keys.I) ) setBackground( Animations.Frozen );
			if ( Gdx.input.isKeyJustPressed(Keys.C) ) setBackColor();
			if ( Gdx.input.isKeyJustPressed( Keys.U ) ) VirusByteGame.HUD.inventory.hideAllSlots();
			if ( Gdx.input.isKeyJustPressed( Keys.Y ) ) VirusByteGame.HUD.inventory.showAllSlots();*/
		} 
		
		//if (buffer_font != null) buffer_font.draw(batch, "00010101", 10f, 50f); // test buffer font
		//Debug.debug.screen("total backgrounds to render is " + backgrounds.size, 10, 440);
		//Debug.debug.screen("total foregrounds to render is " + foregrounds.size, 10, 460);
	}
	
	/** when the especial random background is active random backgrounds are randomly showed during the game */
	public boolean especialRandomBackground = true; // this var is pre activated
	public Array<BackgroundRenderer> especialBackroundsToRender = new Array<BackgroundRenderer>(); //can be accessed by any where
	private float displayTimer, espBackDisplayTime = MathUtils.random(50f);
	//effects
	public enum Animations {Beatz, Black, Box, Flower, Frozen, Frozen2, Frozen3, Dark, Digit, Dots, Galaxy, 
		Hex, Hypo, Lines, Illuminati, Ingang, Optical, Rainbow, Stars, Terror, Voptical, Ways}
	// the texture path is composed by {folder + fileName}
	private String[] hypo = new String[] {"hypoA/hypoA1" ,"hypoA/hypoA2", "hypoA/hypoA3", "hypoA/hypoA4"},
			hex = new String[] {"hex/hex1" ,"hex/hex2", "hex/hex3", "hex/hex4", "hex/hex5", "hex/hex6"},
			ilu = new String[] {"ilu/ilu1" ,"ilu/ilu2", "ilu/ilu3", "ilu/ilu4", "ilu/ilu5", "ilu/ilu6", "ilu/ilu7"},
			way = new String[] {"way/way1" ,"way/way2", "way/way3", "way/way4"},
			box = new String[] {"box/box1" ,"box/box2", "box/box3", "box/box4"},
			dark = new String[] {"darkD/dark-D1" ,"darkD/dark-D2", "darkD/dark-D3", "darkD/dark-D4"},
			rainbow = new String[] {"rainbow/rainbow1" ,"rainbow/rainbow2", "rainbow/rainbow3", "rainbow/rainbow4",
									"rainbow/rainbow5", "rainbow/rainbow6", "rainbow/rainbow7", "rainbow/rainbow8"},
			flower = new String[] {"flower/flower1", "flower/flower2", "flower/flower3", "flower/flower4"},
			lines = new String[] {"lines/lines_blue", "lines/lines_green", "lines/lines_red"},
			optical = new String[] {"others/optical"},
			verticalOptical = new String[] {"verticaloptical/opt1", "verticaloptical/opt2", "verticaloptical/opt3", "verticaloptical/opt4"},
			stars = new String[] {"stars/star1", "stars/star2", "stars/star3", "stars/star4"},
			terror = new String[] {"terror/td1", "terror/td2", "terror/td3", "terror/td4"},
			galaxy = new String[] {"galaxy/galaxy1", "galaxy/galaxy2", "galaxy/galaxy3", "galaxy/galaxy4"},
			digit = new String[] {"digit/digit1", "digit/digit2", "digit/digit3", "digit/digit4", "digit/digit5"},
			dots = new String[] {"dots/dots1", "dots/dots2", "dots/dots3", "dots/dots4", "dots/dots5"},
			ingang = new String[] {"ingang/ing1", "ingang/ing2", "ingang/ing3", "ingang/ing4", "ingang/ing5"},
			frozen1 = new String[] {"frozen/frozenA"},
			frozen2 = new String[] {"frozen/frozenB"},
			frozen3 = new String[] {"frozen/frozenC"},
			beatz = new String[] {"beatz/beatz1", "beatz/beatz2", "beatz/beatz3", "beatz/beatz4", "beatz/beatz5"};
	private ArrayMap<Animations, BackgroundRenderer> backgroundEffects, foregroundEffects;
	/**Background or foreground that can be picked randomly on game stage*/
	private Animations[] rndBackgrounds, rndForegrounds;
	
	/**This method need be called once on the {@link Backgrounds} instantiation }*/
	private void setAnimationLists() {
		BackgroundRenderer background; // for eventually use
		RendererConfig config = new RendererConfig(true, 1/15f); //set animation speed
		config.width = VirusType.WORLD_WIDTH;
		config.height = VirusType.WORLD_HEIGHT;
		Debug.log( "from setAnimationLists() background, config.width " + config.width + " | config.height: " + config.height );
		// BACKGROUNDS
		backgroundEffects = new ArrayMap<Backgrounds.Animations, BackgroundRenderer>();
		config.alphaOperatorRange.set(.001f, .010f);
		//>> 27/02/2015
		backgroundEffects.put(Animations.Beatz, new BackgroundRenderer(beatz, new Vector2(.05f, .3f), new Vector2(10f, 25f), 15, true, config, false));
		backgroundEffects.put(Animations.Digit, new BackgroundRenderer(digit, new Vector2(.05f, .3f), new Vector2(10f, 25f), 15, true, config, false));
		backgroundEffects.put(Animations.Dots, new BackgroundRenderer(dots, new Vector2(.05f, .3f), new Vector2(10f, 25f), 15, true, config, false));
		backgroundEffects.put(Animations.Ingang, new BackgroundRenderer(ingang, new Vector2(.05f, .3f), new Vector2(10f, 25f), 15, true, config, false));
		//<</
		backgroundEffects.put( Animations.Hypo,  new BackgroundRenderer( hypo, new Vector2(.05f, .3f), new Vector2(10f, 25f), 15, true, config, false) );
		config.animSpeedRange.set(.066f, .1f);
		backgroundEffects.put( Animations.Hex, new BackgroundRenderer( hex, new Vector2(.05f, .3f), new Vector2(10f, 25f), 15, true, config, false) );
		config.animSpeedRange.set(.066f, .066f); //default
		backgroundEffects.put( Animations.Box, new BackgroundRenderer( box, new Vector2(.05f, .3f), new Vector2(10f, 25f), 15, true, config, false) );
		backgroundEffects.put( Animations.Dark, new BackgroundRenderer( dark, new Vector2(.05f, .3f), new Vector2(10f, 25f), 15, true, config, false) );
		backgroundEffects.put( Animations.Rainbow, new BackgroundRenderer( rainbow, new Vector2(.05f, .3f), new Vector2(10f, 25f), 15, true, config, false) );
		backgroundEffects.put( Animations.Ways, new BackgroundRenderer( way, new Vector2(.05f, .3f), new Vector2(10f, 25f), 15, true, config, false) );
		config.animSpeedRange.set(.066f, .15f);
		backgroundEffects.put( Animations.Illuminati, new BackgroundRenderer( ilu, new Vector2(.05f, .3f), new Vector2(10f, 25f), 15, true, config, false) );
		backgroundEffects.put( Animations.Flower, new BackgroundRenderer( flower, new Vector2(.05f, .3f), new Vector2(10f, 25f), 15, true, config, false) );
		config.animSpeedRange.set(.066f, .066f); //default
		backgroundEffects.put( Animations.Lines, new BackgroundRenderer( lines, new Vector2(.05f, .3f), new Vector2(10f, 25f), 15, true, config, false) );
		config.alphaOperatorRange.set(.001f, .05f);
		backgroundEffects.put( Animations.Optical, new BackgroundRenderer( optical, new Vector2(), new Vector2(15f, 30f), 15, true, config, false) );
		backgroundEffects.put( Animations.Voptical, new BackgroundRenderer( verticalOptical, new Vector2(), new Vector2(15f, 30f), 15, true, config, false) );
		config.alphaOperatorRange.set(.001f, .007f);
		
		backgroundEffects.put( Animations.Galaxy, background = new ProgressiveRenderer( galaxy, new Vector2(.05f, .09f), new Vector2(15f, 30f), 15, true, config, false));
		background.addOnLoadAction( new Action() {
			@Override
			public void execute(BackgroundRenderer render) {
				BackgroundRenderer back = setBackground(Animations.Black, 0, render.duration, 0, 0, render.depth-1, true);
				back.setAlphaAttribute(1f, .05f);
			}
		} );
		config.alphaMetaRange.set(1f, 1f); //total opacity
		
		//>> especial case
		backgroundEffects.put( Animations.Stars, background = new ProgressiveRenderer(stars, new Vector2(.05f, .09f), new Vector2(15f, 20f), 15, true, config, false) );
		background.addOnLoadAction( new Action() {
			@Override
			public void execute(BackgroundRenderer render) {
				BackgroundRenderer back = setBackground(Animations.Black, 0, render.duration, 0, 0, render.depth-1, true);
				back.setAlphaAttribute(1f, .05f);
			}
		} );
		//<</
		
		// Freeze screen background and foreground >>
		config.alphaOperatorRange.set(.025f, .025f);
		config.alphaMetaRange.set(1f, 1f);
		backgroundEffects.put(Animations.Frozen, background = new BackgroundRenderer(frozen1, new Vector2(), new Vector2(15, 15), 15, true, config, false));
		background.addOnLoadAction( new Action() {
			@Override
			public void execute(BackgroundRenderer render) {
				setForeground(Animations.Frozen2, 0, render.duration, 0, 0, render.depth-1, true);
				setForeground(Animations.Frozen3, 0, render.duration, 0, 0, render.depth, true);
				VirusByteGame.VIRUS_MANAGER.setInputTriggersOn(false);
			}
		} );
		background.addOnRenderAction( new Action() { // tap five times on screen to break the ice
			private int totalTouches;
			@Override
			public void execute(BackgroundRenderer render) {
				if ( Gdx.input.justTouched() ) {
					if ( render.alpha >= render.alphaMeta && ++totalTouches == 5 ) {
						removeBackground( Animations.Frozen );
						totalTouches = 0;
					}
				}
			}
		} );
		background.addOnEndAction( new Action() {
			@Override
			public void execute(BackgroundRenderer render) {
				removeBackground( Animations.Frozen2 );
				removeBackground( Animations.Frozen3 );
				VirusByteGame.VIRUS_MANAGER.setInputTriggersOn(true);
			}
		} );
		config.alphaMetaRange.set(.7f, .7f);
		backgroundEffects.put(Animations.Frozen2, new BackgroundRenderer(frozen2, new Vector2(), new Vector2(15, 15), 15, false, config, false));
		config.alphaMetaRange.set(.15f, .15f);
		backgroundEffects.put(Animations.Frozen3, new BackgroundRenderer(frozen3, new Vector2(), new Vector2(15, 15), 15, false, config, false));
		//>>/
		
		config.alphaMetaRange.set(.7f, 1f);
		config.alphaOperatorRange.set(.001f, .010f);
		backgroundEffects.put( Animations.Black, new BackgroundRenderer(new String[] {"others/black"}, new Vector2(), new Vector2(15f, 25f), 15, false, config, false) );
		config.alphaMetaRange.set(.7f, 1f); //default
		config.alphaOperatorRange.set(.001f, .010f); //default
		// FOREGROUNDS
		foregroundEffects = new ArrayMap<Backgrounds.Animations, BackgroundRenderer>();
		foregroundEffects.put( Animations.Terror, new BackgroundRenderer(terror, new Vector2(.05f, .3f), new Vector2(15f, 25f), 15, true, config, false) );
		foregroundEffects.put( Animations.Hypo,  new BackgroundRenderer( hypo, new Vector2(.05f, .3f), new Vector2(5f, 15f), 15, true, config, false) );
		foregroundEffects.put( Animations.Dark, new BackgroundRenderer( dark, new Vector2(.05f, .3f), new Vector2(15f, 15f), 15, true, config, false) );
		// SET THE RANDOM SELECTABLES
		rndBackgrounds = new Animations[] {Animations.Beatz, Animations.Box, Animations.Flower, Animations.Dark, Animations.Digit, Animations.Dots, 
				Animations.Galaxy, Animations.Hex, Animations.Hypo, Animations.Lines, Animations.Illuminati, Animations.Ingang, Animations.Rainbow, Animations.Stars, 
				Animations.Ways};
		rndForegrounds = new Animations[] {Animations.Terror};
	}
	
	private int stage;
	
	/** Generate and add a random back/foreground animation to display,
	 * is only set if not has another background on the render already running */
	public void randomBackground() {
		if ( isActiveEspecialBackEffects() ) return;
		BackgroundRenderer background;
		//RendererConfig config = new RendererConfig(true, 1/15f); //set animation speed
		boolean fg = stage < 6 ? false : MathUtils.randomBoolean(.5f);
		if ( fg ) {
			//Foreground effect
			background = foregroundEffects.get( rndForegrounds[MathUtils.random(rndForegrounds.length-1)] );
			background.reset().set();
			addForeground( background );
		} else {
			//Background effect
			background = backgroundEffects.get( rndBackgrounds[MathUtils.random(rndBackgrounds.length-1)] );
			background.reset().set();
			addBackground( background );
		}
		//background.setAlphaAttribute(.9f, MathUtils.random(.005f) );
		//background.setTextureBrowser( MathUtils.random(-.3f, .3f), MathUtils.random(-.3f, .3f) );
		especialBackroundsToRender.add( background ); // insert background to especial list then can be removed any time.
	}
	
	public BackgroundRenderer setBackground(Animations animation) {
		BackgroundRenderer background = backgroundEffects.get( animation );
		if ( background == null ) background = foregroundEffects.get( animation ); //try find the animation inside foregrounds list
//		if ( background == null || background.active ) return null;
		if ( !background.isActive ) { //if this background is not active
			if ( isActiveEspecialBackEffects() ) finalizeAllBackEffects();
			background.reset();
		} else { //if this background is already active on
			background.timer = 0; //reset timer
		}
		background.set();
		addBackground( background );
		especialBackroundsToRender.add( background ); // insert background to especial list then can be removed any time.
		return background;
	}
	
	public BackgroundRenderer setBackground(BackgroundRenderer background) {
		background.reset().set();
		addBackground( background );
		especialBackroundsToRender.add( background ); // insert background to especial list then can be removed any time.
		return background;
	}
	
	/**Set the background if has another background on the render is terminated
	 * @param animationSpeed in milliseconds
	 * @param duration is how many seconds the animation will be showed until fade out
	 * @param hScroll speed of the horizontal texture scroll
	 * @param vScroll speed of the vertical texture scroll
	 * @param depth layer where the animation will be renderer
	 * @param fade if animation will make a fade in and out */
	public BackgroundRenderer setBackground(Animations animation, float animationSpeed, float duration, float hScroll, float vScroll, int depth, boolean fade) {
		BackgroundRenderer background = backgroundEffects.get( animation );
		if ( background == null ) background = foregroundEffects.get( animation ); //try find the animation inside foregrounds list
		return setAnimation(background, animationSpeed, duration, hScroll, vScroll, depth, fade, Layer.BACKGROUND);
	}
	
	public BackgroundRenderer setForeground(Animations animation, float animationSpeed, float duration, float hScroll, float vScroll, int depth, boolean fade) {
		BackgroundRenderer background = foregroundEffects.get( animation );
		if ( background == null ) background = backgroundEffects.get( animation ); //try find the animation inside foregrounds list
		return setAnimation(background, animationSpeed, duration, hScroll, vScroll, depth, fade, Layer.FOREGROUND);
	}

	private BackgroundRenderer setAnimation(BackgroundRenderer animation, float animationSpeed, float duration, float hScroll, float vScroll, int depth, boolean fade, Layer layer) {
		if ( !animation.isActive ) { //if this background is not active
			//if ( isActiveEspecialBackEffects() ) finalizeAllBackEffects();
			animation.reset();
		} else { //if this background is already active on
			animation.timer = 0; //reset timer
		}
		animation.set(hScroll, vScroll, duration, depth, fade);
		animation.setAnimationSpeed( animationSpeed );
		if ( layer == Layer.BACKGROUND ) {
			addBackground( animation );
		} else {
			addForeground( animation );
		}
		//background.setAlphaAttribute(.9f, MathUtils.random(.005f) ); //set meta alpha
		if ( hScroll != 0 || vScroll != 0 ) animation.setTextureBrowser(hScroll, vScroll);
		if ( !especialBackroundsToRender.contains(animation, true) ) especialBackroundsToRender.add( animation ); // insert background to especial list then can be removed any time.
		return animation;
	}
	
	/**Terminate all active effects inside especial background renderer*/
	public void finalizeAllBackEffects() {
		for (BackgroundRenderer renderer : especialBackroundsToRender) if ( renderer.isActive ) renderer.finalize();
	}
	
	/**
	 * @return true if has some active effect background*/
	public boolean isActiveEspecialBackEffects() {
		for (BackgroundRenderer renderer : especialBackroundsToRender) {
			if ( renderer.isActive ) return true;
		}
		return false;
	}
	
	/** render all backgrounds in order of depth */
	public void renderBackgrounds(SpriteBatch batch, float deltaTime) {
		if (backgrounds.size == 0) return;
		//batch.setShader(shader);
		Iterator<BackgroundRenderer> back = backgrounds.iterator();
		while ( back.hasNext() ) {
			BackgroundRenderer render = back.next();
			if ( !render.isActive ) {
				especialBackroundsToRender.removeValue(render, true);
				back.remove();
				continue;
			}
			render.draw(batch, deltaTime);
		}
		//batch.setShader(null);
	}
	
	/** render all foreground in order of depth */
	public void renderForegrounds(SpriteBatch batch, float deltaTime) {
		if (foregrounds.size == 0) return;
		Iterator<BackgroundRenderer> back = foregrounds.iterator();
		while (back.hasNext()) {
			BackgroundRenderer render = back.next();
			if ( !render.isActive ) {
				especialBackroundsToRender.removeValue(render, true);
				back.remove();
				continue;
			}
			render.draw(batch, deltaTime);
		}
	}
	
	/// =====================================================================  ////
	/// ========================== ADD and REMOVE ===========================  ////
	/// =====================================================================  ////
	private Array< AssetDescriptor<Texture> > loadedAssets = new Array< AssetDescriptor<Texture> >();
	
	/** add a background object texture to render */
	public void addBackground(BackgroundRenderer background) {
		if ( backgrounds.contains(background, true) ) return;
		backgrounds.add( background );
		backgrounds.sort(); // order by depth
		if ( background.descriptors != null ) loadedAssets.addAll(background.descriptors); //add the background loaded texture asset to the list
	}
	
	public BackgroundRenderer addForeground(Effect effectType, float duration, int depth) {
		return addForeground( new ForegroundColorEffects(genericScreenSizeSprite, effectType, duration, depth) );
	}
	
	/** add a foreground object texture to render */
	public BackgroundRenderer addForeground(BackgroundRenderer foreground) {
		if ( foregrounds.contains(foreground, true) ) return foreground;
		foregrounds.add( foreground );
		foregrounds.sort();
		if (foreground.descriptors != null ) loadedAssets.addAll(foreground.descriptors); //add the foreground loaded texture asset to the list
		return foreground;
	}
	
	/**Finalize Animation*/
	public void removeBackground(Animations animation) {
		BackgroundRenderer background = backgroundEffects.get( animation );
		if ( background != null && background.isActive ) background.terminate();
	}
	
	public void removeForeground(Animations animation) {
		BackgroundRenderer background = foregroundEffects.get( animation );
		if ( background != null && background.isActive ) background.terminate();
	}
	
	/**Check if the animation is current active on the render*/
	public boolean isBackgroundActive(Animations animation) {
		BackgroundRenderer background = backgroundEffects.get( animation );
		if ( background == null ) background = backgroundEffects.get( animation );
		return ( background == null ) ? false : background.isActive;
	}
	
	public boolean unloadBackgroundTextureAssets(String assetName) {
		AssetDescriptor<Texture> desc = null;
		Iterator<AssetDescriptor<Texture>> iterator = loadedAssets.iterator();
		while ( iterator.hasNext() ) {
			desc = iterator.next();
			if ( desc.fileName.equals( assetName ) ) {
				iterator.remove();
			}
		}
		if ( VirusByteGame.ASSETS.getAssetManager().isLoaded(assetName) ) 
			VirusByteGame.ASSETS.getAssetManager().unload(assetName);
		return ( desc == null) ? false : true;
	}
	
	
	public enum Effect { Healing, Damage, Flash, Laser, Acquiring }
	
	public static class ForegroundColorEffects extends BackgroundRenderer {
		private int swap = 0, swapLaserColor = 6, laserSwapTime = 1;
		private Effect effectType;
		private Color[] pallete;
		
		public ForegroundColorEffects(Sprite sprite, Effect type, float duration, int depth) {
			super(sprite, 0, duration, depth, false);
			setAlphaAttribute(0, 0);
			Color color;
			switch ( type ) {
			case Healing:
				color = new Color( Color.GREEN );
				break;
			case Damage:
				color = new Color( Color.RED );
				break;
			case Flash:
				color = new Color( Color.WHITE );
				break;
			case Laser:
				pallete = new Color[4];
				pallete[0] = new Color( Color.BLUE );
				pallete[1] = new Color( Color.RED );
				pallete[2] = new Color( Color.WHITE );
				pallete[3] = new Color( Color.BLACK );
				color = pallete[0];
				break;
			case Acquiring:
				color = new Color( Color.BLUE );
				break;
			default: color = new Color( Color.WHITE ); 
			}
			setColor(color);
			effectType = type;
			setSize(VirusType.WORLD_WIDTH, VirusType.WORLD_HEIGHT);
		}
		
		@Override
		public void draw(SpriteBatch batch, float deltaTime) {
			//float alpha 
			
			if ( effectType == Effect.Laser ) {
				
				if (swapLaserColor++ > 5) {
					swapLaserColor = 0;
					switch (laserSwapTime)
					{
					case 0:
						setColor( pallete[0] );
						break;
					case 1:
						setColor( pallete[1] );
						break;
					case 2:
						setColor( pallete[2] );
						break;
					case 3:
						setColor( pallete[3] );
						break;
					}
					laserSwapTime = laserSwapTime == 3 ? 0 : laserSwapTime+1;
					
					alpha = (alpha < .5f) ? MyUtils.choose(0.6f, 0.8f) : MyUtils.choose(0.3f, 0.4f); 
				}
			} else {
				if ( swap++ > 5 ) {
					swap = 0;
					alpha = (alpha < .5f) ?  MyUtils.choose(0.6f, 0.8f) : MyUtils.choose(0.3f, 0.4f); 
				}
			}
			color.a = alpha;
			super.draw(batch, deltaTime);
		}
		
	}
	
	/**
	 * Make a new sprite shape used generally used to wrap the screen
	 * with a colored shape
	 * @param width sprite width
	 * @param height sprite height
	 * @return Sprite with the parameters size
	 */
	private Sprite makeSpriteShape(float width, float height) {
		Pixmap tile = new Pixmap(64, 64, Format.RGBA8888);
		tile.setColor(1f, 1f, 1f, 1f);
		tile.fill();
		Texture tex = new Texture(tile);
		tile.dispose();
		tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Sprite spr = new Sprite(tex);
		spr.setSize(width, height);
		return spr; 
	}
	
	/** Define a new random back wrapper shape color  */
	public void setBackColor() {
		setBackColor(MathUtils.random(255), MathUtils.random(255), MathUtils.random(255));
	}
	
	/**
	 * Set a new back wrapper shape color with parameters 
	 * @param r red parameter
	 * @param g green parameter
	 * @param b blue parameter
	 */
	public void setBackColor(int r, int g, int b) {
		setBackColor(r/255f, g/255f, b/255f);
	}
	
	public void setBackColor(float r, float g, float b) {
		backColor.set(r, g, b, .9f);
		tileBack.setColor(backColor);
		colorBackground.setColor( backColor ); // now is this that really change the color of the background
	}
	
	public void dispose() {
		//spriteBack.getTexture().dispose();
		genericScreenSizeSprite.getTexture().dispose();
		for (BackgroundRenderer back : backgrounds) back.dispose();
		for (BackgroundRenderer back : foregrounds) back.dispose();
		//Assets.manager.unload(circuitTexture);
		//Assets.manager.unload(hiddenTexture);
		shader.dispose();
	}
	
	/**Resize every background and foreground on render*/
	public void resize(float width, float height) {
		// Update world size
		worldWidth = VirusByteGame.VIEWPORT.getWorldWidth();
		worldHeight = VirusByteGame.VIEWPORT.getWorldHeight();
		genericScreenSizeSprite.setSize(worldWidth+100f, worldHeight);
		colorBackground.setSize(worldWidth, worldHeight);
		if ( worldWidth > 1024 || worldHeight > 1024) {
			circuitBackground.setSize( (worldWidth>1024)?worldWidth:1024f, (worldHeight>1024)?worldHeight:1024f);
			circuitBackground.setPosition(0, 0);
		}
//		circuitBackground.setSize(width, spriteBack.getHeight());
		// center hide background
		if (hide_background != null) hide_background.setPosition((VirusByteGame.VIEWPORT.getWorldWidth()*.5f)-(hide_background1.getWidth()*.5f), 0);
	}
}
