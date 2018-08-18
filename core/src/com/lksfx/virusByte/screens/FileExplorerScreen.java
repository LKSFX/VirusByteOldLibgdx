package com.lksfx.virusByte.screens;

import java.io.IOException;
import java.io.StringWriter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.campaingControl.LevelController;
import com.lksfx.virusByte.gameControl.debug.Debug;

public class FileExplorerScreen extends ScreenAdapter implements GameScreen {
	
	private String currentFolder = "";
	private String lastFolder = "";
	private Stage localStage;
	private Skin fileExplorerSkin;
	private Skin skin;
	private ScrollPane scrollPane;
	
	public FileExplorerScreen() {
		
//		SpriteBatch batch = VirusByteGame.GAME.batch;
		loadLastExplorerPosition();
		fileExplorerSkin = new Skin( Gdx.files.internal( "data/ui/fileExplorerSkin.json" ) );
		skin = new Skin( Gdx.files.internal( "data/ui/newSkin.json" ) );
		localStage = new Stage( VirusByteGame.VIEWPORT, VirusByteGame.GAME.batch );
		
		scrollPane = makeTable();
		scrollPane.setBounds(0, 50, localStage.getWidth(), localStage.getHeight() - 100);
		scrollPane.setScrollingDisabled( true, false );
		
		localStage.addActor( scrollPane );
		VirusByteGame.addProcessor( localStage );
	}

	@Override
	public void render( float delta ) {
		
		localStage.act();
		localStage.draw();
		
	}
	
	@Override
	public void dispose() {
		fileExplorerSkin.dispose();
		skin.dispose();
		localStage.dispose();
		super.dispose();
	}
	
	@Override
	public void onScreenExit() {
		VirusByteGame.removeProcessor( localStage );
	}
	
	private ScrollPane makeTable( ) {
		Table table = new Table();
//		table.debug();
		ScrollPane pane = new ScrollPane( table, skin );
		pane.setVariableSizeKnobs( false );
		table.align( Align.top );
		
		listFolder( table, null );
		
		return pane;
	}
	
	private void listFolder( Table table, FileHandle handle ) {
		
		if ( handle == null ) {
			if ( Gdx.files.external( currentFolder ).list().length == 0 )
				handle = Gdx.files.local( currentFolder );
			else
				handle = Gdx.files.external( currentFolder );
		
		}
		
		if ( handle.isDirectory() ) {
			
			openFolder( table, handle );
			Debug.log( "Folder open with " + handle.list().length + " folders" );
			
		} 
		else  {
			
			if ( handle.extension().equals("xml") && LevelController.isValidLevelFile(handle) ) {
				if ( VirusByteGame.GAME.goTo( VirusByteGame.ScreenID.CAMPAING ) ) {
					try {
						((CampaignGameMode) VirusByteGame.GAME.getScreen() ).loadLevel( new XmlReader().parse(handle) );
					} catch (IOException e) { e.printStackTrace(); }
				}
			}
			else
				Debug.log( "this '" + handle.extension() + "' file can't be open!" );
		}
		
	}
	
	private void openFolder( final Table table, final FileHandle handle ) {
		
		table.clear();
		int totalFolders = handle.list().length;
		float generalWidth = localStage.getWidth() * .9f;
		TextButton button;
		Table hGroup;
		
		if ( handle.parent() != null ) {
			button = new TextButton( "... ", fileExplorerSkin );
			hGroup = new Table();
			hGroup.setBackground( fileExplorerSkin.getDrawable("labelButton") );
			hGroup.padRight( 10f );
			button.addListener( new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					listFolder( table, handle.parent() );
				}
			} );
			hGroup.add( button ).expandX().fillX();
			final Image icon = new Image( fileExplorerSkin.getDrawable("returnIcon") );
			hGroup.add( icon );
			table.add( hGroup ).width( generalWidth );
			table.row();
		}
		
		for ( int i = 0; i < totalFolders; i++ ) {
			final FileHandle innerHandle = handle.list()[i];
			hGroup = new Table();
			hGroup.setBackground( fileExplorerSkin.getDrawable("labelButton") );
			hGroup.padRight( 10f );
			button = new TextButton( innerHandle.nameWithoutExtension(), fileExplorerSkin );
			button.getLabel().setEllipsis( true );
			button.addListener( new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					listFolder( table, innerHandle );
				}
			} );
			hGroup.add( button ).expandX().fillX();
			String iconName = innerHandle.isDirectory() ? "directoryIcon" : "fileIcon"; // checking if this file is a directory or an file
			if ( innerHandle.extension().equals("xml") ) // check if this file is a runnable XML level file
				iconName = "playIcon";
			final Image icon = new Image( fileExplorerSkin.getDrawable(iconName) );
			hGroup.add( icon );
			table.add( hGroup ).width( generalWidth );
			table.row();
		}
		
		saveCurrentExplorerPosition( handle );
		
	}
	
	private void saveCurrentExplorerPosition( FileHandle handle ) {
		
		lastFolder = handle.path();
		Debug.log( "" );
		StringWriter writer = new StringWriter();
		
		try {
			
			@SuppressWarnings("resource")
			XmlWriter xml = new XmlWriter( writer );
			xml.element( "Save" )
				.element( "FileExplorer" )
					.attribute("lastFolder", lastFolder)
				.pop()
			.pop();
			
			Debug.log( writer.toString() );
			Gdx.files.local( "data/save.xml" ).writeString( writer.toString(), false );
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private void loadLastExplorerPosition() {
		
		FileHandle handle = Gdx.files.local( "data/save.xml" );
		boolean saveFileExists = handle.exists();
		
		if ( saveFileExists ) {
			try {
				Element element;
				element = new XmlReader().parse( handle );
				Element explorerElement = element.getChildByName( "FileExplorer" );
				if ( explorerElement != null ) {
					lastFolder = explorerElement.get( "lastFolder", "" );
					currentFolder = lastFolder;
					Debug.log( "loaded path is: " + currentFolder );
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
