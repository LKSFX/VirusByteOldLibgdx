package com.lksfx.virusByte.gameControl.campaingControl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.screens.CampaignGameMode;

/** Contains a list of levels for a infection type */
public abstract class LevelList implements Disposable {
	
	private String rootPathFolder;
	private String[] levelPathList;
	private Window window;
	private Skin localSkin;
	
	public LevelList( String rootPathFolder ) {
		
		this.rootPathFolder = rootPathFolder;
		
	}
	
	public void setLevels( String[] levelPathList ) {
		this.levelPathList = levelPathList;
	}
	
	
	public Window openLevelSelectWindow( final Stage stage, Skin skin ) {
		
		if ( window == null ) 
			window = new Window( "", skin);
		
		window.setSize( stage.getWidth(), stage.getHeight() * .8f );
		window.setY( stage.getHeight() * .15f );
		float padV = window.getHeight() * .03f, padH = window.getWidth() * .03f;
		window.align( Align.topLeft ).pad( padV, padH, padV, padH );
		
		addFolderLevels( window );
		ClickListener closeListener =  new ClickListener() { // Listen for clicks
			public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
				Debug.log( "clicked on: X =" + event.getStageX() + " | Y = " + event.getStageY() );
				Debug.log( "event: " + event.getTarget() );
				Debug.log( "hit: " + stage.hit(event.getStageX(), event.getStageY(), false) );
				
				if ( !isHitWindow(event.getStageX(), event.getStageY()) ) {
					closeLevelSelectedWindow();
					stage.removeListener( this );
				}
				
				return false;
			};
		};
		stage.addListener( closeListener );
		
		stage.addActor( window );
		
		return window;
	}
	
	public void closeLevelSelectedWindow() {
		
		if ( window != null )
			window.remove();
		if ( localSkin != null )
			localSkin.dispose();
		
	}
	
	private void addFolderLevels( Window window ) {
		
		localSkin = new Skin( Gdx.files.internal( "data/ui/osSkin.json" ) );
		float hSpacing = 5f;
		int totalFolders = levelPathList.length;
		int column = 0;
		
		for ( int i = 0; i < totalFolders; i++ ) {
			final int j = i;
			final String currentLevelPath = rootPathFolder + levelPathList[i];
			
			ImageButton folder = new ImageButton( localSkin.getDrawable("folder-icon") );
			if ( (column + 1) * (folder.getWidth() + hSpacing) > window.getWidth() ) {
				window.row();
				column = 0;
			}
			column++;
				
			folder.addListener(  new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					Debug.log( "clicked on folder "+ j );
					XmlReader xml = new XmlReader();
					try {
						Element levelElement = xml.parse( Gdx.files.internal( currentLevelPath ) );
						if ( VirusByteGame.GAME.goTo( VirusByteGame.ScreenID.CAMPAING ) )
							((CampaignGameMode) VirusByteGame.GAME.getScreen() ).loadLevel( levelElement );
					} catch (Exception e) { }
				}
			}  );
			
			window.add( folder ).spaceLeft( hSpacing );
		}
		
	}
	
	/** @return True if the position is inside level select window */
	private boolean isHitWindow( float x, float y ) {
		boolean result = false;
		if ( window != null ) {
			if ( x > window.getX() && x < window.getX() + window.getWidth() ) {
				if ( y > window.getY() && y < window.getY() + window.getHeight() )
					result = true;
			}
		}
		return result;
	}

	@Override
	public void dispose() {
		closeLevelSelectedWindow();
	}
	
	public static class AlphaInfection extends LevelList {

		public AlphaInfection() {
			super( "data/levels/alpha/" );
			String[] levelPathList = new String[] { "test.xml" }; 
			setLevels( levelPathList );
		}
		
	}
	
}
