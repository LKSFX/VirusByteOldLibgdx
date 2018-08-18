package com.lksfx.virusByte.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.campaingControl.LevelController;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.hud.GameHud.MENU;

public class CampaignGameMode extends GameStageScreen {
	
	private LevelController controller;

	public CampaignGameMode( VirusByteGame game, boolean premium ) {
		super( game, premium );
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void initialize( VirusByteGame game, boolean premium ) {
		super.initialize( game, premium );
		
		controller = new LevelController();
//		controller.loadLevel( "test" );
//		save();
//		checkFolders();
		
		
		hud.loadPreviousInventoryStatus();
	}
	
	public void loadLevel(  ) {
		controller.loadLevel( "test" );
	}
	
	public void loadLevel( Element levelElement ) {
		controller.loadLevel( levelElement );
	}
	
	@Override
	public void render(float delta) {
		super.render( delta );
		
		if ( !PAUSED && !isGameEnded )
			controller.update( delta, this );
		
	}
	
	/**End the game, clear stage, set backgrounds off and call the {@link #gameFinalize()}} method*/
	public final void endLevel() {
		
		virus_manager.clearAll(); // clean all virus from the screen and stage
		gameFinalize();
		isGameEnded = true;
		
	}
	
	/** @return current level controller */
	public LevelController getLevelController() {
		return controller;
	}
	
	/**Called when the game end generally from the {@link #endLevel()} */
	protected void gameFinalize() {
		hud.inventory.hideAllSlots();
		hud.inventory.save();
		Timer.instance().clear();
		Timer.schedule( new Task() {
			@Override
			public void run() {
				pauseGame( MENU.GAMEOVER_CAMPAING_MODE );
			}
		}, 1f );
	}
	
	@Override
	public void onCloseApplication() {
		// TODO Auto-generated method stub
	}
	
	/**Save inventory*/
	public void save() {
//		FileHandle file = Gdx.files.local("data/inventory.vb");
		Json json = new Json();
		json.setUsePrototypes( false );
		String str = json.toJson( controller );
		Debug.log( json.prettyPrint( str ) );
		/*str = json.prettyPrint( str );
		file.writeString(str, false);*/
	}
	
	public void checkFolders() {
		
		FileHandle external = Gdx.files.external( "" );
		
		for ( int i = 0; i < external.list().length; i++ ) {
			Debug.log( "" + external.list()[i].path() );
		}
		
		if ( external.isDirectory() )
			Debug.log( "" + external.list().length );
		else 
			Debug.log( "" + Gdx.files.getExternalStoragePath() );
		
		Debug.log( "" + external.path() );
		
	}

}
