package com.lksfx.virusByte.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.XmlReader;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.Backgrounds;
import com.lksfx.virusByte.gameControl.campaingControl.LevelController;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.screens.CampaignGameMode;
import com.lksfx.virusByte.screens.GameStageScreen;
import com.lksfx.virusByte.screens.NewMenu;
import com.lksfx.virusByte.screens.NewMenu.MainMenuTab;

public class GameOverCampaingMenu extends Menu {
	
	private Table table;
	private Skin localSkin;
	private GameStageScreen gameScreen;
	
	public GameOverCampaingMenu(Stage stage, Skin skin) {
		super( stage, skin );
	}

	@Override
	public void constructLayout() {
		table = new Table();
		table.setFillParent( true );
		table.debug();
		localSkin = new Skin( Gdx.files.internal("data/ui/osSkin.json") );
		Pixmap pixmap = new Pixmap( Gdx.graphics.getWidth(), (int)(Gdx.graphics.getHeight() * .2f), Pixmap.Format.RGBA8888 );
		pixmap.setColor( Color.WHITE );
		pixmap.fill();
		
		Texture texture = new Texture( pixmap );
		localSkin.add( "white-sheet", texture, Texture.class );
		
		pixmap.dispose();
		
		table.addAction( Actions.forever( new RunnableAction() {
			@Override
			public void run() {
				if ( gameScreen != null ) {
					Backgrounds back = gameScreen.back;
					SpriteBatch batch = gameScreen.batch;
					float deltaTime = Gdx.graphics.getDeltaTime();
					
					batch.begin();
					back.update(deltaTime);
					back.renderBackgrounds(batch, deltaTime);
					batch.end();
				}
			};
		} ) );
		
		addTable( table );
		
	}
	
	private void setLayout() {
		
		table.clearChildren();
		
		table.add( getResultTable() ).size( stage.getWidth() * .8f, stage.getHeight() * .3f );
		
	}
	
	private Table getResultTable() {
		Table table = new Table();
//		table.debug();
		table.setBackground( localSkin.newDrawable("white-sheet", new Color(Color.BLACK)) );
		
		Screen currentGameScreen = VirusByteGame.GAME.getScreen();
		String levelName = "Test de menu";
		
		if ( currentGameScreen instanceof CampaignGameMode ) {
			CampaignGameMode gameScreen = ( CampaignGameMode ) currentGameScreen;
			LevelController levelController = gameScreen.getLevelController();
			
			levelName = levelController.getLevelName();
			
		}
		
		table.add( new Label( levelName, skin, "visitor20" ) );
		table.row();
		getLevelStatsBar( table.add(  ), 100 );
		setMenuButtons( table );
		
		return table;
	}
	
	private void getLevelStatsBar( final Cell<?> cell, final float percentageCompletion ) {
		final Image bar = new Image( localSkin.newDrawable( "white-sheet", new Color(Color.GREEN) ) );
		float maxBarSize = stage.getWidth() * .4f;
		float percentage = percentageCompletion / 100;
		final float barWidth = maxBarSize * percentage;
		final float barHeight = stage.getHeight() * .1f;
		final Label label = new Label( "", skin, "visitor20" );
		label.setAlignment( Align.center );
		
		bar.setSize( 0, barHeight );
		bar.setAlign( Align.center );
		
		bar.addAction( Actions.forever(Actions.run( new Runnable() {
			float size = 0;
			@Override
			public void run() {
				size += .025f;
				bar.setSize( barWidth * size, barHeight );
				Debug.log( "bar size: " + size );
				if ( size >= 1 ) {
					bar.clearActions();
					bar.setSize( barWidth, barHeight );
					label.setText( (int)percentageCompletion + "%" );
				}
			}
		}) ) );
		
		Stack stack = new Stack();
		stack.add( bar );
		stack.add( label );
		cell.setActor( stack ).size( barWidth, bar.getHeight() );
		
	}
	
	private void setMenuButtons(Table table ) {
		
		table.row();
		
//		Image folder = new Image( localSkin, "folder-icon" );
		
		HorizontalGroup hGroup = new HorizontalGroup();
		hGroup.addActor( new HomeButton() );
		hGroup.addActor( new RestartButton() );
		hGroup.addActor( new ReturnButton() );
		
		table.add( hGroup ).space( 5f, 5f, 5f, 5f );
		
	}
	
	@Override
	public void open() {
		super.open();
		Screen screen = VirusByteGame.GAME.getScreen();
		if ( screen instanceof GameStageScreen ) {
			gameScreen = (GameStageScreen) screen;
		}
		setLayout();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		localSkin.dispose();
	}
	
	/** Home Button */
	private class HomeButton extends Container<Stack> {
		
		public HomeButton(  ) {
			super();
			
			Stack stack = new Stack();
			float width = stage.getWidth() * .15f;
			float height = stage.getHeight() * .08f;
			
			ImageButton imgButton = new ImageButton( localSkin.getDrawable("folder-icon") );
			imgButton.addListener( new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					Debug.log( "clicked on home folder button" );
					if ( VirusByteGame.GAME.goTo( VirusByteGame.ScreenID.MENU ) ) {
						((NewMenu) VirusByteGame.GAME.getScreen()).browseTab( MainMenuTab.CAMPAIGN_TAB );
					}
				}
			} );
			stack.add( imgButton );
			Image icon = new Image( localSkin.getDrawable("home-icon") );
			icon.setTouchable( Touchable.disabled );
			stack.add( icon );
			setActor( stack );
			maxSize(width, height);
			
		}
		
	}
	
	/** Return Button */
	private class ReturnButton extends Container<Stack> {
		
		public ReturnButton() {
			super();
			
			Stack stack = new Stack();
			float width = stage.getWidth() * .15f;
			float height = stage.getHeight() * .08f;
			
			ImageButton imgButton = new ImageButton( localSkin.getDrawable("folder-icon") );
			imgButton.addListener( new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					Debug.log( "clicked on return folder button" );
				}
			} );
			stack.add( imgButton );
			Image icon = new Image( localSkin.getDrawable("arrow-icon") );
			icon.setTouchable( Touchable.disabled );
			stack.add( icon );
			setActor( stack );
			maxSize(width, height);
			
		}
		
	}
	
	/** Restart Button */
	private class RestartButton extends Container<Stack> {
		
		public RestartButton() {
			super();
			
			Stack stack = new Stack();
			float width = stage.getWidth() * .15f;
			float height = stage.getHeight() * .08f;
			
			ImageButton imgButton = new ImageButton( localSkin.getDrawable("folder-icon") );
			imgButton.addListener( new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					Debug.log( "clicked on restart folder button" );
					
					if ( gameScreen instanceof CampaignGameMode ) { // if in a campaign mode
						LevelController lvController = ((CampaignGameMode)gameScreen).getLevelController();
						XmlReader.Element lvElement = lvController.getLevelElement();
						if ( VirusByteGame.GAME.goTo( VirusByteGame.ScreenID.CAMPAING ) ) 
								((CampaignGameMode) VirusByteGame.GAME.getScreen() ).loadLevel( lvElement );
					}
					
				}
			} );
			stack.add( imgButton );
			Image icon = new Image( localSkin.getDrawable("restart-icon") );
			icon.setTouchable( Touchable.disabled );
			stack.add( icon );
			setActor( stack );
			maxSize(width, height);
			
		}
		
	}
	
	
}
