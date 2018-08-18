package com.lksfx.virusByte.screens;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.Backgrounds;
import com.lksfx.virusByte.gameControl.Assets.Particles;
import com.lksfx.virusByte.gameControl.BaseGameEngine;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.hud.GameHud;
import com.lksfx.virusByte.gameControl.hud.GameHud.MENU;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

/**Main game stage screen, control the main render on the game stage like background, foreground, virus and HUD render*/
public abstract class GameStageScreen extends ScreenAdapter implements CloseApplicationListener {
	
	public SpriteBatch batch;
	public Debug debug;
	public static Boolean PREMIUM, PAUSED, ACTIVE = false;
	public boolean isGameEnded = false, isTutorial;
	public static int lives, stage = 1;
	public GameHud disposable_hud; //this reference is used just to dispose the hud without any error
	public Backgrounds disposable_back; //this reference is used just to dispose the hud without any error
	
	public VirusByteGame game;
	public OrthographicCamera main_camera;
	public ExtendViewport viewport;
	public Vector3 mouse_pos;
	public VirusManager virus_manager;
	/**This is the stage {@link Backgrounds} instance that control background and foreground render*/
	public Backgrounds back;
	public GameHud hud;
	
	private BaseGameEngine gameEngine;
	
	public float gameSpeed = 1f;
	public float gameDelta;
	
	public GameStageScreen(VirusByteGame game, boolean premium) {
		initialize( game, premium );
	}
	
	public void initialize( VirusByteGame game, boolean premium ) {
		ACTIVE = Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE).getBoolean("active", false);
		PREMIUM = premium;
		PAUSED = false;
		VirusType.BOSS_TIME = false; //update this static variable BOSS_TIME to false
//		VirusByteGame.MAIN_MULTIPLEX_CONTROLLER.clear(); //clear input processors
		//VirusByteGame.CONSOLE.call(); // call console on start
		lives = 1;
		stage = 1;
		batch = game.batch;
		debug = Debug.debug;
		
		VirusByteGame.MAIN_CAMERA = game.cam;
		VirusByteGame.VIEWPORT = game.viewport;
		VirusByteGame.HUD = new GameHud( this );
		
		VirusByteGame.MOUSE_POS = new Vector3(0f, 0f, 0f);
		Debug.log("viewport height: " + VirusByteGame.VIEWPORT.getWorldHeight());
		Debug.log("blend mode: "+ batch.isBlendingEnabled() + " | " + GL20.GL_SRC_ALPHA + "/" + GL20.GL_ONE_MINUS_SRC_ALPHA);
		VirusByteGame.VIRUS_MANAGER = new VirusManager();
		gameEngine = VirusByteGame.GAME_ENGINE;
		gameEngine.clearStage(); // clear stage, to avoid objects from an prior running
		VirusByteGame.BACK = new Backgrounds( batch );
		VirusType.initialize( VirusByteGame.VIRUS_MANAGER, VirusByteGame.VIEWPORT, VirusByteGame.BACK, 
				VirusByteGame.HUD, VirusByteGame.MOUSE_POS, ACTIVE, lives ); //initialize static fields on virus class
		VirusType.TUTORIAL = false; //set tutorial mode for virus and item objects

		disposable_hud = VirusByteGame.HUD;
		disposable_back = VirusByteGame.BACK;
		
		
		this.game = VirusByteGame.GAME;
		main_camera = VirusByteGame.MAIN_CAMERA;
		viewport = VirusByteGame.VIEWPORT;
		mouse_pos = VirusByteGame.MOUSE_POS;
		virus_manager = VirusByteGame.VIRUS_MANAGER;
		back = VirusByteGame.BACK;
		hud = VirusByteGame.HUD;
		
		gameEngine.insertGameObject( virus_manager );
	}
	
	float cameraAngle;
	float hudCameraAngle;
	
	@Override
	public void render(float delta) {
		delta *= gameSpeed;
		gameDelta = delta;
		
		cameraAngle = 10 * delta;
		
//		main_camera.rotate( cameraAngle );
//		main_camera.zoom = .75f;
		
		mouse_pos.x = Gdx.input.getX();
		mouse_pos.y = Gdx.input.getY();
		//debug.screen("on screen: x: " + mousePos.x + " y: " + mousePos.y , 10, 20);
		
		main_camera.unproject( mouse_pos );
		//debug.screen("on world: x: " + mousePos.x + " y: " + mousePos.y , 10, 40);
		debugChecking();
		
		//draw(); //update everything //draw everything
		if ( !PAUSED ) {
			// GAME PLAY
			main_camera.update();
			batch.setProjectionMatrix( main_camera.combined );
			batch.begin();
			back.update(delta);
			back.renderBackgrounds(batch, delta); // render backgrounds
			virus_manager.update(batch, mouse_pos, delta); // update and render virus manager
			gameEngine.fixedUpdate( delta );
//			virus_manager.draw(batch, delta);
			gameEngine.draw( batch, delta );
			back.renderForegrounds(batch, delta); // render foregrounds
			Debug.RENDER_CALLS += batch.renderCalls;
			batch.end();
			
			if ( Gdx.input.isKeyJustPressed( Keys.ESCAPE ) ) {
				if ( !isGameEnded ) pauseGame(null);
			}
			
			hud.update( delta ); // hub ui and pause ui
			
			batch.setProjectionMatrix( main_camera.combined );
			batch.begin();
			virus_manager.drawAbove( batch, delta );
			gameEngine.drawAbove( batch, delta );
			Debug.RENDER_CALLS += batch.renderCalls;
			batch.end();
			gameEngine.lateUpdate( delta );
		} else {
			// GAME PAUSED
			hud.update( delta ); // hub UI and pause UI
			if ( Gdx.input.isKeyJustPressed(Keys.ESCAPE) ) {
				unpauseGame();
			}
		}
		
//		debug.screen("Holding virus: " + VirusType.HOLDING_VIRUS , 10, 420);
		stage = virus_manager.stage;
		
		lives = VirusType.LIFES;
		
	}
	
	/**This method set the pause screen, stop the default stage rendering
	 * @param menu TODO*/
	public void pauseGame(MENU menu) {
		if ( PAUSED ) 
			return;
		Timer.instance().clear();
		virus_manager.pauseManager();
		PAUSED = true;
		Debug.log( "GamePaused: " + PAUSED );
		hud.pauseGame( (menu == null) ? MENU.PAUSE : menu );
	}
	
	
	/**Unpause the stage screen, close pause menu, resume the default stage rendering*/
	public void unpauseGame() {
		if ( !PAUSED ) return;
		//Timer.instance().clear();
		virus_manager.unpauseManager();
		PAUSED = false;
		Debug.log( "Game Unpaused: " + PAUSED );
		hud.unpauseGame();
	}
	
	@Override
	public void resize(int width, int height) {
		virus_manager.resize(width, height);
		back.resize(viewport.getWorldWidth(), viewport.getWorldHeight());
		hud.inventory.resizeScreenUpdate();
	}

	@Override
	public void dispose() {
		for ( Array<VirusType> array : virus_manager.virus_set ) {
			for ( VirusType obj : array ) 
				obj.dispose();
		}
		virus_manager.dispose();
		disposable_back.dispose();
		disposable_hud.dispose();
	}
	
	@Override
	public void hide() {
		Debug.log("another screen SET");
		dispose();
	}
	
	@Override
	public void pause() {
		if ( Gdx.app.getType() == ApplicationType.Desktop ) 
			return;
		if (!PAUSED && !isGameEnded) {
			Debug.log("Game screen is paused!");
			pauseGame(null);
		}
	}
	
	private void debugChecking() {
		
		if ( Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) ) {
			if ( Gdx.input.isKeyJustPressed( Keys.R ) ) {
				// Remove all virus from stage 
				Debug.log("total virus removed from stage: " + VirusByteGame.VIRUS_MANAGER.clearAll() );
			} else if ( Gdx.input.isKeyJustPressed( Keys.S ) ) {
				// Shop menu
				pauseGame( MENU.SHOP );
			} else if ( Gdx.input.isKeyJustPressed( Keys.G ) ) {
				// GameOver menu
				pauseGame( MENU.GAMEOVER_CHALLENGE_MODE );
			} else if ( Gdx.input.isKeyJustPressed( Keys.MINUS ) ) {
				// Reduce Life
				VirusType.LIFES--;
				Debug.log( "Reduce lives" );
			} else if ( Gdx.input.isKeyJustPressed( Keys.PLUS ) ) {
				// Increase Life
				VirusType.LIFES++;
				Debug.log( "Increase lives" );
			} else if ( Gdx.input.isKeyJustPressed( Keys.O ) ) {
				//Save inventory
				hud.inventory.save();
			} else if ( Gdx.input.isKeyJustPressed( Keys.I ) ) {
				//Load and debug inventory saved file
				hud.inventory.debugSavedInventory();
			}
			if ( Gdx.input.isKeyPressed( Keys.NUM_1 ) ) {
				if ( Gdx.input.justTouched() ) 
					VirusManager.PART_EFFECTS.createEffect(Particles.explosion, mouse_pos.x, mouse_pos.y, -5);
			} else if ( Gdx.input.isKeyPressed( Keys.NUM_2 ) ) {
				if ( Gdx.input.justTouched() ) 
					VirusManager.PART_EFFECTS.createEffect(Particles.firework, mouse_pos.x, mouse_pos.y, 1f, Color.RED, -5);
			} else if ( Gdx.input.isKeyPressed( Keys.NUM_3 ) ) {
				if ( Gdx.input.justTouched() ) 
					VirusManager.PART_EFFECTS.createEffect(Particles.healingRing, mouse_pos.x, mouse_pos.y, 1f, Color.YELLOW, -5);
			} 
			if ( Gdx.input.isKeyJustPressed( Keys.F1 ) ) {
				virus_manager.debugManagerArrays();
			}
			if ( Gdx.input.isKeyJustPressed( Keys.F2 ) ) {
				Debug.log( "total input processors: " + VirusByteGame.MAIN_MULTIPLEX_CONTROLLER.size() );
			}
		}
		
	}
	
}
