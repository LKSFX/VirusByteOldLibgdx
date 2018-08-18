package com.lksfx.virusByte;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ReflectionPool;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.lksfx.virusByte.effects.Backgrounds;
import com.lksfx.virusByte.effects.Immersion;
import com.lksfx.virusByte.gameControl.AdControl;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.BaseGameEngine;
import com.lksfx.virusByte.gameControl.FPSLimiter;
import com.lksfx.virusByte.gameControl.GoogleInterface;
import com.lksfx.virusByte.gameControl.MultiServices;
import com.lksfx.virusByte.gameControl.MusicControl;
import com.lksfx.virusByte.gameControl.MyUtils;
import com.lksfx.virusByte.gameControl.debug.Console;
import com.lksfx.virusByte.gameControl.debug.Console.ConsoleState;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.hud.GameHud;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.gameObject.pontuation.PointsManager;
import com.lksfx.virusByte.screens.CampaignGameMode;
import com.lksfx.virusByte.screens.CloseApplicationListener;
import com.lksfx.virusByte.screens.ChallengeGameMode;
import com.lksfx.virusByte.screens.FileExplorerScreen;
import com.lksfx.virusByte.screens.GameScreen;
import com.lksfx.virusByte.screens.MusicTestScreen;
import com.lksfx.virusByte.screens.NewMenu;
import com.lksfx.virusByte.screens.NewTutoScreen;
import com.lksfx.virusByte.screens.SceneryTest;
import com.lksfx.virusByte.screens.TutorialScreen;
import com.lksfx.virusByte.screens.VirusLab;
import com.lksfx.virusByte.screens.VirusTestGameScreen;

public class VirusByteGame extends Game {
	public static final boolean PREMIUM = true, ADS_ON = true, GOOGLE_SERVICES = false;
	/**Count the total amount of rounds has been player since the game started, this is more useful for debug purpose.
	 * This is increased every round end.*/
	public static int TOTAL_ROUNDS_PLAYED;
	public static boolean SFX, VIBRATION, MUSIC;
	public static float SFX_VOLUME = .2f, MASTER_VOLUME = .4f;
	public static final String CONFIG_FILE = "virusByte_config", DEBUG_FILE = "virusByte_debug", SALT = "tinho", 
			HIGHSCORE_NORMAL_MODE = "CgkIvuuP864fEAIQBg", HIGHSCORE_PREMIUM_MODE = "CgkIvuuP864fEAIQBw";
	
	public SpriteBatch batch;
	public static Immersion IMM;
	public static AdControl AD_CONTROL;
	public static GoogleInterface GOOGLE_PLAY;
	public ExtendViewport viewport;
	public OrthographicCamera cam;
	public Debug debug;
	public static InputMultiplexer MAIN_MULTIPLEX_CONTROLLER;
	public GestureDetector consoleCaller;
	
	/** Main Base Game Engine Object Source */
	public static VirusByteGame GAME;
	public static BitmapFont FONT;
	public static VirusByteGameUtils UTIL;
	public static Console CONSOLE;
	public static MusicControl MC;
	public static OrthographicCamera MAIN_CAMERA, HUD_CAMERA;
	public static ExtendViewport VIEWPORT;
	public static Vector3 MOUSE_POS;
	public static VirusManager VIRUS_MANAGER;
	public static PointsManager POINT_MANAGER;
	public static Backgrounds BACK;
	public static GameHud HUD;
	public static Assets ASSETS;
	public static BaseGameEngine GAME_ENGINE;
	private FPSLimiter fpsLimiter;
	
	public enum GraphicsQuality { LOW, MEDIUM, HIGH };
	public static GraphicsQuality GRAPHIC_QUALITY = GraphicsQuality.MEDIUM;
	
	//Helper utilities objects
	/** Vector2 pool for utilities */
	private ReflectionPool< Vector2 > vector2Pool;
	private ReflectionPool< Vector3 > vector3Pool;
	
	public VirusByteGame( MultiServices services ) {
		MAIN_MULTIPLEX_CONTROLLER = new InputMultiplexer();
		GAME_ENGINE = new BaseGameEngine();
		GAME = null;
		FONT = null;
		UTIL = null;
		CONSOLE = null;
		MC = null;
		MAIN_CAMERA = null;
		HUD_CAMERA = null;
		VIEWPORT = null;
		MOUSE_POS = null;
		VIRUS_MANAGER = null;
		POINT_MANAGER = null;
		BACK = null;
		HUD = null;
		ASSETS = null;
		IMM = services.getImmersion();
		AD_CONTROL = services.getAdControl();
		GOOGLE_PLAY = services.getGooglePlayService();
		vector2Pool = new ReflectionPool<Vector2>( Vector2.class );
		vector3Pool = new ReflectionPool<Vector3>( Vector3.class );
	}
	
	@Override
	public void create() {
		ASSETS = new Assets();
		updateConfig(false);
		Gdx.input.setCatchBackKey( true );
		TOTAL_ROUNDS_PLAYED = 0;
		UTIL = new VirusByteGameUtils();
		MC = new MusicControl(); // music control
		fpsLimiter = new FPSLimiter(30);
		//VirusManager.VirusInstance.initialize();
		
//		Assets.initialize(); //start creating a new manager object, note more necessary
//		Assets.manager.finishLoading();
		
		Array<String> names = ASSETS.getAssetManager().getAssetNames();
		Debug.log("manager assets: " + names);
		
		batch = new SpriteBatch();
		
		cam = new OrthographicCamera(480f, 640f);
		HUD_CAMERA = new OrthographicCamera();
		FONT = ASSETS.getAssetManager().get( "size20.ttf", BitmapFont.class );
		viewport = new ExtendViewport(320f, 640f, cam);
		VIEWPORT = viewport;
		viewport.update( Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true );
		debug = new Debug(this, batch);
		CONSOLE = new Console(viewport, batch); //for in-game debug
		consoleCaller = new GestureDetector( new GestureAdapter() {
			@Override
			public boolean longPress(float x, float y) {
				boolean openConsole = (x < Gdx.graphics.getHeight()*.2f && y > Gdx.graphics.getHeight() - Gdx.graphics.getHeight()*.2f);
//					for ( int i = 0; i < 2; i++ ) openConsole = (Gdx.input.getY(i) < viewport.getWorldHeight()*.2f);
				if ( openConsole ) {
					if ( CONSOLE.currentState == ConsoleState.HIDE ) {
						CONSOLE.call();
					} else {
						CONSOLE.hide();
					}
					return true;
				}
				return false;
			}
		});
		Gdx.input.setInputProcessor( MAIN_MULTIPLEX_CONTROLLER );
		
		GAME = this;
		
		// Load Boot File
		Preferences pref = Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE);
		int defVal = 2, val = (pref.getInteger("boot", defVal) > 10) ? defVal : pref.getInteger("boot");
		goTo( val );
		
	}
	
	public static class ScreenID {
		public final static int CAMPAING = 0, CHALLENGE = 1, TITLE = 2, MENU = 3,
				TUTORIAL = 4, NEWTUTORIAL = 5, MUSICTEST = 6, VIRUSTEST = 7, SCENERYTEST = 8, VIRUSLAB = 9, FEXPLORER = 10 ;
	}
	
	/** @return true if the level has successfully switched */
	public boolean goTo( int levelID ) {
		
		Screen deltaGameScreen = getScreen();
		
		switch ( levelID ) //0: UI test mode - // 1: Screen paint test mode - // 2: Tests mode //3: main gameplay screen
		{
		case 0:
			this.setScreen( new CampaignGameMode(this, true) ); //start on game normal mode
			break;
		case 1:
			this.setScreen( new ChallengeGameMode(this, true) ); // start on game premium mode
			break;
		case 2:
			this.setScreen( new NewMenu(this, true) ); //start on menu
			break;
		case 3:
			this.setScreen( new NewMenu(this, false) ); //start on menu
			break;
		case 4:
			this.setScreen( new TutorialScreen(this, true) ); // start on old tutorial screen
			break;
		case 5:
			this.setScreen( new NewTutoScreen(this, true) ); // start on new tutorial screen
			break;
		case 6:
			this.setScreen( new MusicTestScreen(this) ); // start on music tutorial screen
			break;
		case 7:
			this.setScreen( new VirusTestGameScreen(this, true) );
			break;
		case 8:
			this.setScreen( new SceneryTest( this ) );
			break;
		case 9:
			this.setScreen( new VirusLab(this, true) );
		case 10:
			this.setScreen( new FileExplorerScreen() );
			break;
		}
		
		boolean successfull = ( deltaGameScreen != getScreen() );
		if ( successfull ) { // check if the screen is actually switched
			if ( deltaGameScreen instanceof GameScreen ) { 
				( (GameScreen)deltaGameScreen ).onScreenExit(); // call method that care with any necessary input processor remotion etc
			}
		}
		
		return successfull;
	}
	
	@Override
	public void setScreen(Screen screen) {
		super.setScreen(screen);
		VirusByteGame.addProcessor( consoleCaller ); //set the caller
		if ( CONSOLE.currentState == Console.ConsoleState.ACTIVE ) 
			CONSOLE.call(); // if console is open, update console with call()
	}
	
	@Override
	public void render() {
		
		Gdx.gl.glClearColor( 0f, 0f, 0f, 1f );
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
		
		fpsLimiter.delay();
		if ( Gdx.input.isKeyJustPressed( Keys.BACK ) ) {
			if ( getScreen() instanceof CloseApplicationListener ) ((CloseApplicationListener)getScreen()).onCloseApplication();
			Gdx.app.exit();
		}
		super.render();
		debug.fps();
		debug.showRenderCalls();
		debug.show();
		ASSETS.update();
		CONSOLE.update();
		MC.update(); 
	}
	
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
		batch.setProjectionMatrix( viewport.getCamera().combined );
		VirusType.WORLD_WIDTH = viewport.getWorldWidth();
		super.resize(width, height);
	}
	
	@Override
	public void pause() {
		super.pause();
		MC.pause();
	}
	
	@Override
	public void resume() {
		super.resume();
		MC.resume();
	}
	
	public static void updateConfig() {
		updateConfig(true);
	}
	
	public static void updateConfig(boolean vib) {
		Preferences pref = Gdx.app.getPreferences( CONFIG_FILE );
		boolean last_vibration_state = VIBRATION;
		SFX = pref.getBoolean("sfx", true);
		VIBRATION = pref.getBoolean("vibration", true);
		MUSIC = pref.getBoolean("music", true);
		
		// load graphic quality
		// 0 = low, 1 = medium, 2 = high
		int graphic_value = pref.getInteger("graphic_quality", 1);
		switch ( graphic_value ) {
		case 0:
			GRAPHIC_QUALITY = GraphicsQuality.LOW;
			break;
		case 1:
			GRAPHIC_QUALITY = GraphicsQuality.MEDIUM;
			break;
		case 2:
			GRAPHIC_QUALITY = GraphicsQuality.HIGH;
			break;
		default:
			GRAPHIC_QUALITY = GraphicsQuality.MEDIUM;
			break;
		}
		
		//updateVolume
		MASTER_VOLUME = pref.getFloat("master-volume", MASTER_VOLUME);
		SFX_VOLUME = MASTER_VOLUME+.1f;
		
		MyUtils.checkCrypt(pref);
		
		Debug.log("On game: SFX: " + SFX + " | VIBRATION: " + VIBRATION + " | MUSIC: " + MUSIC);
		Debug.log("On file: SFX: " + pref.getBoolean("sfx", true) + " | VIBRATION: " + pref.getBoolean("vibration", true)
				+ " | MUSIC: " + pref.getBoolean("music", true));
		
		if ( vib && last_vibration_state != VIBRATION && VIBRATION ) 
			VirusByteGame.IMM.vibrate(Immersion.FAST_PULSE_100);
	}
	
	public static void setGraphicQuality( GraphicsQuality quality ) {
		Preferences pref = Gdx.app.getPreferences( CONFIG_FILE );
		
		switch ( quality ) {
		case HIGH:
			pref.putInteger("graphic_quality", 2);
			break;
		case LOW:
			pref.putInteger("graphic_quality", 0);
			break;
		case MEDIUM:
			pref.putInteger("graphic_quality", 1);
			break;
		default:
			pref.putInteger("graphic_quality", 1);
			break;
		}
		
		pref.flush();
		GRAPHIC_QUALITY = quality;
		
	}
	
	/** update the volume, call this every time the volume has changed */
	public static void updateVolume() {
		Preferences pref = Gdx.app.getPreferences(CONFIG_FILE);
		pref.putFloat("master-volume", MASTER_VOLUME);
		pref.flush();
	}
	
	/**insert processor to the main processor manager*/
	public static void addProcessor( InputProcessor processor ) {
		int size = MAIN_MULTIPLEX_CONTROLLER.size();
		if ( size  == 0 ) {
			MAIN_MULTIPLEX_CONTROLLER.addProcessor( processor ); 
			return;
		}
		if ( MAIN_MULTIPLEX_CONTROLLER.getProcessors().contains( processor, true ) )
			return;
		MAIN_MULTIPLEX_CONTROLLER.addProcessor( 0, processor );
		/*Array<InputProcessor> processorArray = new Array<InputProcessor>( MAIN_MULTIPLEX_CONTROLLER.getProcessors() );
		processorArray.add( processor );
		MAIN_MULTIPLEX_CONTROLLER.clear();
		for (InputProcessor input : processorArray ) MAIN_MULTIPLEX_CONTROLLER.addProcessor(input);*/
	}
	
	/** Remove an already inserted processor from the main multiplexer */
	public static void removeProcessor( InputProcessor processor ) {
		if ( processor != null )
			MAIN_MULTIPLEX_CONTROLLER.removeProcessor( processor );
	}
	
	@Override
	public void dispose() {
		getScreen().dispose();
		UTIL.dispose();
		CONSOLE.dispose();
		batch.dispose();
		debug.dispose();
		ASSETS.dispose();
	}
	
	public class VirusByteGameUtils implements Disposable {
		/**This shader is used to create colored flash effects on textures*/
		public ShaderProgram flashShader;
		
		public VirusByteGameUtils() {
			flashShader = new ShaderProgram( Gdx.files.internal("data/shaders/tintShader.vsh"), Gdx.files.internal("data/shaders/tintShader.fsh") );
			Debug.log("shader is compiled? " + (( flashShader.isCompiled() )? "yes" : flashShader.getLog()));
		}
		
		public void dispose() {
			flashShader.dispose();
		}
	}
	
	// UTILITIES FUNCTIONS
	
	public Vector2 obtainVector2() {
		return vector2Pool.obtain();
	}
	
	public Vector3 obtainVector3() {
		return vector3Pool.obtain();
	}
	
	public void freeVector( Vector2 vector2 ) {
		vector2Pool.free( vector2 );
	}
	
	public void freeVector( Vector3 vector3 ) {
		vector3Pool.free( vector3 );
	}
	
}
