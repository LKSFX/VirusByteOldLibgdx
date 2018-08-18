package com.lksfx.virusByte.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.VirusByteGame.GraphicsQuality;
import com.lksfx.virusByte.gameControl.PauseImage;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.screens.GameStageScreen;

public class PauseMenu extends Menu {
	
	private Table table;
	private HorizontalGroup hGroup;
	private Array<MyButton> vButtons;
	private VerticalGroup vGroup;
	private ArrayMap<String, ConfigButton> configButtons;
	private Window optionsWindow;
	private GameStageScreen gameScreen;
	private PauseImage pauseImage;
	
	public PauseMenu( Stage stage, Skin skin ) {
		super( stage, skin );
		pauseImage = new PauseImage();
	}
	
	@Override
	public void open() {
		super.open();
		Screen screen = VirusByteGame.GAME.getScreen();
		if ( screen instanceof GameStageScreen ) {
			gameScreen = (GameStageScreen) screen;
			pauseImage.createImage( gameScreen );
			TextureRegionDrawable drawable = new TextureRegionDrawable( new TextureRegion( pauseImage.getTexture() ) );
			table.setBackground( drawable );
		}
	}
	
	@Override
	public void close() {
		super.close();
		pauseImage.dispose();
	}
	
	@Override
	public void constructLayout() {
		table = new Table( skin );
		table.setFillParent( true );
		table.debug();
		addTable(table);
		
		setGroups();
		
		float hGroupScale = 1.25f;
		table.add( vGroup ).pad(10f).expandY().center();
		table.row();
		hGroup.setScale( hGroupScale );
		table.add( hGroup ).size( hGroup.getPrefWidth() * hGroupScale, hGroup.getPrefHeight() * hGroupScale );
		table.row();
		Slider volumeSelector;
		table.add(volumeSelector = new Slider(0f, .8f, .1f, false, skin, "volume-selector") {
			@Override
			public void act(float delta) {
				super.act(delta);
				if ( isDragging() ) {
					VirusByteGame.MASTER_VOLUME = getValue();
					VirusByteGame.MC.setVolume( VirusByteGame.MASTER_VOLUME );
					VirusByteGame.SFX_VOLUME = getValue() == 0 ? 0 : VirusByteGame.MASTER_VOLUME+.1f;
					VirusByteGame.updateVolume();
				}
			}
		} ).padBottom(5).prefSize(volumeSelector.getPrefWidth() * hGroupScale, volumeSelector.getPrefHeight() * hGroupScale);
		volumeSelector.setValue(VirusByteGame.MASTER_VOLUME);
		Debug.log("Paused: table total cells = " + table.getCells().size);
		
	}
	
	private void setGroups() {
		vGroup = new VerticalGroup().space(25f);
		
		// Vertical buttons
		final ChangeListener switchTab = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				playBlip();
			}
		};
		final Color trans = new Color(0f, 0f , 0f, 0f);
		vButtons = new Array<MyButton>() {{
			add( new MyButton("Resume", skin, "medium", trans, false) {{
				addListener(switchTab);
				addListener(new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						if ( gameScreen != null ) gameScreen.unpauseGame();
						com.lksfx.virusByte.gameControl.debug.Debug.log( "Pushed resume button!" );
					}
				});
			}} );
			add( new MyButton("Leaderboard", skin, "medium", trans, false) {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						Timer.schedule(new Task() {
							@Override
							public void run() {
								if ( VirusByteGame.GOOGLE_SERVICES ) VirusByteGame.GOOGLE_PLAY.getLeaderboardGPGS();
								com.lksfx.virusByte.gameControl.debug.Debug.log( "Pushed leaderboard button!" );
							}
						}, .3f);
					}
				} );
			}});
			add( new MyButton("Achievements", skin, "medium", trans, false) {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						if ( VirusByteGame.GOOGLE_SERVICES ) VirusByteGame.GOOGLE_PLAY.getAchievementsGPGS();
						com.lksfx.virusByte.gameControl.debug.Debug.log( "Pushed achievements button!" );
					}
				} );
			}} );
			add( new MyButton("Quit", skin, "medium", trans, false) {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
		    			VirusByteGame.GAME_ENGINE.clearStage();
//		    			VirusByteGame.GAME.setScreen( new NewMenu(VirusByteGame.GAME, false) );
		    			VirusByteGame.GAME.goTo( VirusByteGame.ScreenID.MENU );
					}
				} );
			}} );
			//tab 2
			/*add( new MyButton("Normal", skin, "medium", trans, false) {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						game.setScreen( new GameScreen(game, false) );
					}
				} );
			}});
			add( new MyButton("Premium", skin, "medium", trans, false) {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						game.setScreen( new GameScreen(game, true) );
					}
				} );
			}});
			add( new MyButton("Back", skin, "medium", trans, false) {{
				addListener(switchTab);
			}} );*/
		}};
		
		//Create window
		createOptionsWindow();
				
		//+++++++++++++++++++ Configuration buttons +++++++++++++++++++++++++
		//Configuration buttons
		configButtons = new ArrayMap<String, ConfigButton>() {{
			put("sfx", new ConfigButton(skin, "sfx-switch") {{
				setName( "SFX" );
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						com.lksfx.virusByte.gameControl.debug.Debug.log("button " + getName() + " is pressed");
			    		Preferences pref = Gdx.app.getPreferences( VirusByteGame.CONFIG_FILE );
			    		pref.putBoolean("sfx", isChecked());
			    		pref.flush();
			    		VirusByteGame.updateConfig();
					}
				} );
			}});
			put("vibration", new ConfigButton(skin, "vibration-switch") {{
				setName( "VIBRATION" );
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						com.lksfx.virusByte.gameControl.debug.Debug.log("button " + getName() + " is pressed");
			    		Preferences pref = Gdx.app.getPreferences( VirusByteGame.CONFIG_FILE );
			    		pref.putBoolean("vibration", isChecked());
			    		pref.flush();
			    		VirusByteGame.updateConfig();
					}
				} );
			}});
			put("music", new ConfigButton(skin, "music-switch") {{
				setName( "MUSIC" );
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						com.lksfx.virusByte.gameControl.debug.Debug.log("button " + getName() + " is pressed");
			    		Preferences pref = Gdx.app.getPreferences( VirusByteGame.CONFIG_FILE );
			    		pref.putBoolean("music", isChecked());
			    		pref.flush();
			    		VirusByteGame.updateConfig();
			    		if ( VirusByteGame.MUSIC ) {
			    			if ( gameScreen != null ) {
			    				if ( !gameScreen.isTutorial ) 
			    					VirusByteGame.MC.resume();
			    			}
			    		} else {
			    			if ( VirusByteGame.MC.isPlaying() ) 
			    				VirusByteGame.MC.pause();
			    		}
					}
				} );
			}});
			put("options", new ConfigButton(skin, "options") {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						if ( stage.getActors().contains(optionsWindow, true) ) {
							optionsWindow.remove();
						} else {
							optionsWindow.setPosition(stage.getWidth() * .1f, stage.getHeight()*.3f);
							optionsWindow.debug();
							stage.addActor( optionsWindow );
						}
					}
				} );
			}} );
		}};
		
		//add the three configuration buttons to the horizontal group
		hGroup = new HorizontalGroup();
		hGroup.space(25f);
		hGroup.addActor(configButtons.get("sfx"));
		hGroup.addActor(configButtons.get("vibration"));
		hGroup.addActor(configButtons.get("music"));
		//update .ini configuration file
		Preferences pref = Gdx.app.getPreferences(VirusByteGame.CONFIG_FILE);
		configButtons.get("sfx").setChecked( pref.getBoolean("sfx", true) );
		configButtons.get("vibration").setChecked( pref.getBoolean("vibration", true) );
		configButtons.get("music").setChecked( pref.getBoolean("music", true) );	
		hGroup.addActor( configButtons.get("options") );
		//=================================================================
		// add the entire vButtons array group to the vertical group
		for (MyButton bt : vButtons) {
			vGroup.addActor(bt);
		}
	}
	
	/**Make the options window*/
private void createOptionsWindow() {
		
		optionsWindow = new Window("options", skin, "frame1");
		optionsWindow.setSize( stage.getWidth() *.8f, stage.getHeight()*.6f );
		float windowWidth = optionsWindow.getWidth(), windowHeigth = optionsWindow.getHeight(), topPadding = (windowHeigth/100) * 10, 
				sidesPadding = (windowWidth/100) * 3.6f, bottomPadding = (windowHeigth/100) * 3.6f;
		optionsWindow.pad(topPadding, sidesPadding, bottomPadding, sidesPadding);
		VerticalGroup externalVertivalGroup = new VerticalGroup();
		ScrollPane scrollPane = new ScrollPane(externalVertivalGroup, skin);
		scrollPane.setVariableSizeKnobs( false );
		scrollPane.setScrollingDisabled(true, false);
		externalVertivalGroup.space( windowWidth / 10 );
		optionsWindow.add( scrollPane ).prefSize(windowWidth, windowHeigth).padTop( 15f );
		ClickListener stopTouchDown = new ClickListener() {
			@Override
			public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
				boolean bool = super.touchDown(event, x, y, pointer, button);
				if ( bool )
					event.stop();
				return bool;
			}
		};
		// >>
		// particle volume selector group
		VerticalGroup particleVolumeVGroup = new VerticalGroup();
		Table particleVolumeTable = new Table( skin );
		Container<Table> container = new Container<Table>( particleVolumeTable );
		container.prefSize( windowWidth, windowHeigth / 10 );
//		particleVolumeTable.debug();
		Slider graphicQualitySlider = new Slider(0, 2, 1, false, skin, "volume-selector");
		graphicQualitySlider.addListener( stopTouchDown );
		int graphicValue = (VirusByteGame.GRAPHIC_QUALITY == GraphicsQuality.HIGH) ? 2 : 
			( VirusByteGame.GRAPHIC_QUALITY == GraphicsQuality.LOW ) ? 0 : 1;
		graphicQualitySlider.setValue( graphicValue );
		String qualityString = ( graphicValue == 2 ) ? "high" : ( graphicValue == 0 ) ? "low" : "medium";
		final Label particleVolumeLabel = new Label( "graphics " + qualityString, skin, "visitor20" );
		final Label particleVolumeIndicator = new Label( ""+(int)graphicQualitySlider.getValue(), skin, "visitor20" ); // show the volume integer value
		graphicQualitySlider.addListener( new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				int graphicValue = (int)((Slider)actor).getValue();
				particleVolumeIndicator.setText( ""+graphicValue );
				String qualityString = ( graphicValue == 2 ) ? "high" : ( graphicValue == 0 ) ? "low" : "medium";
				particleVolumeLabel.setText( "graphics " + qualityString );
				GraphicsQuality quality = ( graphicValue == 2 ) ? GraphicsQuality.HIGH :
					( graphicValue == 0 ) ? GraphicsQuality.LOW : GraphicsQuality.MEDIUM; 
				VirusByteGame.setGraphicQuality( quality );
				
				if ( VirusManager.PART_EFFECTS != null ) 
					VirusManager.PART_EFFECTS.setGraphicsQuality( quality );
				
			}
		} );
		particleVolumeVGroup.addActor( particleVolumeLabel );
		
		float spacing = windowWidth * .01f;
		particleVolumeTable.add().padLeft( spacing ).uniform();
		particleVolumeTable.add( graphicQualitySlider ).width( windowWidth - windowWidth / 3 );
		particleVolumeTable.add( particleVolumeIndicator ).space( spacing ).padRight( spacing ).uniform();
		
		particleVolumeVGroup.addActor( container );
		
		externalVertivalGroup.addActor( particleVolumeVGroup );
		// <<
		// >>
		// another selectors
		VerticalGroup anotherVolumeGroup;
		Table anotherVolumeTable;
		Slider anotherVolumeSlider;
		Label anotherVolumeLabel;
		for ( int i = 0; i < 2; i++ ) {
			anotherVolumeGroup = new VerticalGroup();
			anotherVolumeTable = new Table( skin );
			anotherVolumeSlider = new Slider(150, 300, 25, false, skin, "volume-selector");
			container = new Container<Table>( anotherVolumeTable );
			container.prefSize( windowWidth, windowHeigth / 10 );
			anotherVolumeSlider.addListener( stopTouchDown );
			anotherVolumeLabel = new Label("another volume " + (i+1), skin, "visitor20" );
			
			anotherVolumeTable.add().padLeft( spacing ).uniform();
			anotherVolumeTable.add( anotherVolumeSlider ).width( windowWidth - windowWidth / 3 );
			anotherVolumeTable.add().padRight( spacing ).uniform();
			
			anotherVolumeGroup.addActor( anotherVolumeLabel );
			anotherVolumeGroup.addActor( container );
			
			externalVertivalGroup.addActor( anotherVolumeGroup );
		}
		// <<
		Debug.log( "Window height " + windowHeigth );
		
	}
	
	/**Play blip sound effect*/
	private void playBlip() {
		if ( VirusByteGame.SFX ) {
			// AUDIO TODO
		}
	}

	@Override
	public void dispose() {
		pauseImage.dispose();
	}
	
}
