package com.lksfx.virusByte.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.VirusByteGame.GraphicsQuality;
import com.lksfx.virusByte.gameControl.MyUtils;
import com.lksfx.virusByte.gameControl.campaingControl.LevelList;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.menus.ConfigButton;
import com.lksfx.virusByte.menus.MyButton;

public class NewMenu extends ScreenAdapter implements GameScreen {
//	private VirusByteGame game;
	private Viewport viewport;
	private SpriteBatch batch;
	private Skin skin;
	private Stage mainMenuStage, mainTitleStage;
	private boolean titleScreen, scrollOn;
	private VerticalGroup vGroup;
	private HorizontalGroup hGroup;
	private Array<MyButton> vButtonsMainTab;
	private Array<MyButton> vButtonsPlayTab;
	private Array<MyButton> vButtonsCampaignTab;
	private ArrayMap<String, ConfigButton> configButtons;
	private ArrayMap<String, Sprite> back;
	private MainMenuTab currentTab;
	private ChangeListener switchTabListener;
	private LevelList levelList;
	
	public enum MainMenuTab { MAIN_TAB, PLAY_TAB, CAMPAIGN_TAB; }
	
	/**Window that contains particle and graphic quality control*/
	private Window optionsWindow;
	
	/** for the move of the background */
	private float backScrollSpeed = .1f, scrollTimerA, scrollTimerB, binaryAlpha;
	
	private Table menuTable;
	
	public NewMenu( final VirusByteGame game, boolean titleScreen ) {
//		this.game = game;
		this.titleScreen = titleScreen;
		viewport = game.viewport;
		batch = game.batch;
		// setup backgrounds
		back = new ArrayMap<String, Sprite>();
		Sprite color, binary, circuits;
		circuits = new Sprite( new Texture("data/sprites/backgrounds/circuits.png") ); 
		circuits.getTexture().setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		back.put( "circuits", circuits );
		Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		color = new Sprite( new Texture(pixmap) );
		color.getTexture().setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		color.setColor(MyUtils.randomColor());
		color.setAlpha(.9f);
		color.setSize( viewport.getWorldWidth(), viewport.getWorldHeight() );
		binary = new Sprite( new Texture("data/sprites/backgrounds/texture_binary.png") );
		binary.getTexture().setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		binary.setAlpha(.5f);
		back.put( "color", color);
		back.put( "binary", binary);
		scrollOn = true;
		
		// setup skin
		skin = new Skin( Gdx.files.internal("data/ui/newSkin.json") );
		
		//setup stage
		mainTitleStage = new Stage( viewport, batch );
		mainMenuStage = new Stage( viewport, batch );
		
		//Options window
		createOptionsWindow();
		
		switchTabListener = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				playBlip();
				
				if ( actor.getName() != null ) {
					
					// Return to previously tab if it exists
					if ( actor.getName().equals( "BackMainTab" ) ) 
						browseTab( MainMenuTab.MAIN_TAB );
					else if ( actor.getName().equals( "Play" ) || actor.getName().equals( "BackPlayTab" ) ) // go to play menu tab
						browseTab( MainMenuTab.PLAY_TAB );
					else if ( actor.getName().equals( "Campaign" ) )
						browseTab( MainMenuTab.CAMPAIGN_TAB );
					
				}
			}
		};
		
		// Vertical button tab
		createMainTab();
		
		createPlayTab();
		
		createCampaignTab();
		
		//Configuration buttons
		createConfigurationButtons();
		
		checkConfig();
		//setup vertical group
		vGroup = new VerticalGroup();
		vGroup.setName("vertical");
		vGroup.space(20f);
		//setup horizontal group
		hGroup = new HorizontalGroup();
		hGroup.space(25f);
		hGroup.addActor( configButtons.get("sfx") );
		hGroup.addActor( configButtons.get("vibration") );
		hGroup.addActor( configButtons.get("music") );
		hGroup.addActor( configButtons.get("options") );
		
		//add the main table to the mainMenu stage
		mainTitleStage.addActor( makeMainTitleTable() );
		mainMenuStage.addActor( menuTable = new Table() );
		
		//Initiate tab with main menu tab
		browseTab( MainMenuTab.MAIN_TAB );
		
//		VirusByteGame.MAIN_MULTIPLEX_CONTROLLER.clear(); //clear
		//set the stage to controller
		if ( titleScreen ) {
			VirusByteGame.addProcessor( mainTitleStage );
			mainMenuStage.addAction( Actions.alpha(0) );
			back.get("binary").setAlpha(0);
		} else {
			VirusByteGame.addProcessor( mainMenuStage );
		} 
		//set batch projection to viewport camera matrix
		batch.setProjectionMatrix( viewport.getCamera().combined );
		
		//stop any current playing music
		if ( VirusByteGame.MC.isPlaying() ) 
			VirusByteGame.MC.stopAll();
	}
	
	private void createMainTab() {
		
		final Color trans = new Color(0f, 0f , 0f, 0f);
		vButtonsMainTab = new Array<MyButton>() {{
			add( new MyButton("Play", skin, "medium", trans, false) {{
				addListener( switchTabListener );
				setName( "Play" );
			}} );
			add( new MyButton("Leaderboard", skin, "medium", trans, false) {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						Timer.schedule(new Task() {
							@Override
							public void run() {
								if ( VirusByteGame.GOOGLE_SERVICES ) 
									VirusByteGame.GOOGLE_PLAY.getLeaderboardGPGS();
							}
						}, .3f);
					}
				} );
			}});
			add( new MyButton("Tutorial", skin, "medium", trans, false) {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
//						game.setScreen( new NewTutoScreen(game, true) );
						VirusByteGame.GAME.goTo( VirusByteGame.ScreenID.NEWTUTORIAL );
					}
				} );
			}} );
			add( new MyButton("Quit", skin, "medium", trans, false) {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						onScreenExit(); //exit
						Gdx.app.exit();
					}
				} );
			}} );
		}};
		
	}
	
	private void createPlayTab() {
		
		final Color trans = new Color(0f, 0f , 0f, 0f);
		vButtonsPlayTab = new Array<MyButton>() {{
			//tab 2
			add( new MyButton("Campaing", skin, "medium", trans, false) {{
				/*addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
//						game.setScreen( new CampaingGameMode(game, false) );
						VirusByteGame.GAME.goTo( VirusByteGame.ScreenID.CAMPAING );
					}
				} );*/
				addListener( switchTabListener );
				setName( "Campaign" );
			}});
			add( new MyButton("Challenge", skin, "medium", trans, false) {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
//						game.setScreen( new ChallengeGameMode(game, true) );
						VirusByteGame.GAME.goTo( VirusByteGame.ScreenID.CHALLENGE );
					}
				} );
			}});
			add( new MyButton("Back", skin, "medium", trans, false) {{
				addListener( switchTabListener );
				setName( "BackMainTab" );
			}} );
		}};
		
	}
	
	private void createCampaignTab() {
		
		final Color trans = new Color(0f, 0f , 0f, 0f);
		vButtonsCampaignTab = new Array<MyButton>() {{
			//tab 2
			add( new MyButton("Alpha", skin, "medium", trans, false) {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						
						LevelList alphaList = new LevelList.AlphaInfection();
						alphaList.openLevelSelectWindow( mainMenuStage, skin );
						
					}
				} );
			}});
			add( new MyButton("Beta", skin, "medium", trans, false) {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
					}
				} );
			}});
			add( new MyButton("Gamma", skin, "medium", trans, false) {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
					}
				} );
			}});
			add( new MyButton("Delta", skin, "medium", trans, false) {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
					}
				} );
			}});
			add( new MyButton("Back", skin, "medium", trans, false) {{
				addListener( switchTabListener );
				setName( "BackPlayTab" );
			}} );
		}};
		
	}
	
	private void createConfigurationButtons() {
		
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
					}
				} );
			}});
			put("options", new ConfigButton(skin, "options") {{
				addListener( new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						if ( mainMenuStage.getActors().contains(optionsWindow, true) ) {
							optionsWindow.remove();
						} else {
							optionsWindow.setPosition(mainMenuStage.getWidth() * .1f, mainMenuStage.getHeight()*.3f);
							optionsWindow.debug();
							mainMenuStage.addActor( optionsWindow );
						}
					}
				} );
			}} );
		}};
		
	}
	
	private void setMainTab() {
		
		menuTable.clearChildren();
		
		int totalButtons = vButtonsMainTab.size;
		for ( int j = 0; j < totalButtons; j++ ) 
			vGroup.addActor( vButtonsMainTab.get(j) );
		
		menuTable.bottom();
		Table.debugActorColor = Color.YELLOW;
		//add elements to menu table
		float hGroupScale = 1.25f;
		menuTable.add( vGroup ).pad(10f).expandY().center();
		menuTable.row(); // break line
		hGroup.setScale( hGroupScale );
		menuTable.add( hGroup ).prefSize(hGroup.getPrefWidth() * hGroupScale, hGroup.getPrefHeight() * hGroupScale).padBottom(5);
		menuTable.row(); // break line
		
		menuTable.setFillParent(true);
		
	}
	
	private void setPlayTab() {
		
		menuTable.clearChildren();
		
		int totalButtons = vButtonsPlayTab.size;
		for ( int j = 0; j < totalButtons; j++ ) 
			vGroup.addActor( vButtonsPlayTab.get(j) );
		
		menuTable.bottom();
		Table.debugActorColor = Color.YELLOW;
		//add elements to menu table
		float hGroupScale = 1.25f;
		menuTable.add( vGroup ).pad(10f).expandY().center();
		menuTable.row(); // break line
		hGroup.setScale( hGroupScale );
		menuTable.add( hGroup ).prefSize(hGroup.getPrefWidth() * hGroupScale, hGroup.getPrefHeight() * hGroupScale).padBottom(5);
		menuTable.row(); // break line
		
		menuTable.setFillParent(true);
		
	}
	
	private void setCampaignTab() {
		
		menuTable.clearChildren();
		
		int totalButtons = vButtonsCampaignTab.size;
		for ( int j = 0; j < totalButtons; j++ ) 
			vGroup.addActor( vButtonsCampaignTab.get(j) );
		
		menuTable.bottom();
		Table.debugActorColor = Color.YELLOW;
		//add elements to menu table
		float hGroupScale = 1.25f;
		menuTable.add( vGroup ).pad(10f).expandY().center();
		menuTable.row(); // break line
		hGroup.setScale( hGroupScale );
		menuTable.add( hGroup ).prefSize(hGroup.getPrefWidth() * hGroupScale, hGroup.getPrefHeight() * hGroupScale).padBottom(5);
		menuTable.row(); // break line
		
		menuTable.setFillParent(true);
		
	}
	
	/**Play blip sound effect*/
	public void playBlip() {
		if ( VirusByteGame.SFX ) {
			// AUDIO TODO
		}
	}
	
	/** Construct the options window*/
	private void createOptionsWindow() {
		
		optionsWindow = new Window("options", skin, "frame1");
		optionsWindow.setSize(mainMenuStage.getWidth() *.8f, mainMenuStage.getHeight()*.6f);
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
	
	@Override
	public void render(float delta) {
		
		batch.begin();
		//render backgrounds
		if ( binaryAlpha < .6f ) { 
			if ( !titleScreen ) {
				float alpha = binaryAlpha += .005f; 
				back.get( "binary" ).setAlpha( alpha > .6f ? .6f : alpha ); 
			} 
		}
		
		for ( Sprite bk : back.values() ) 
			bk.draw(batch);
		
		updateBackgrounds( delta );
		batch.end();
		
		mainMenuStage.act(delta);
		mainMenuStage.draw();
		
		// when in title screen
		if ( titleScreen ) {
	    	mainTitleStage.act(delta);
	    	mainTitleStage.draw();
	    	if ( Gdx.input.justTouched() ) {
	    		mainTitleStage.addAction( Actions.sequence( Actions.fadeOut(1f), Actions.run(new Runnable() {
	    			@Override
	    			public void run() {
	    				VirusByteGame.addProcessor( mainMenuStage );
	    				VirusByteGame.removeProcessor( mainTitleStage );
	    				mainMenuStage.addAction( Actions.fadeIn(1) );
	    				titleScreen = false;
	    			}
	    		}) ) );
	    	}
	    }
		
		/*Debug.debug.screen("master volume: " + VirusByteGame.MASTER_VOLUME, 10, 40);
		Debug.debug.show();*/
	}
	
	/**Switch between tabs*/
	public void browseTab( MainMenuTab toTab ) {
		
		if ( currentTab == toTab ) //going to the current tab
			return;
		
		vGroup.clearChildren(); // remove all buttons from vertical groups
		
		switch ( toTab ) {
		case CAMPAIGN_TAB:
			setCampaignTab();
			break;
		case MAIN_TAB:
			// Main menu tab
			setMainTab();
			break;
		case PLAY_TAB:
			//Go to play tab menu
    		setPlayTab();
			break;
		default:
			break;
		}
		
		currentTab = toTab; // update current tab menu
	}
	
	private void updateBackgrounds(float delta) {
		if (!scrollOn) return;
		scrollTimerA += backScrollSpeed*delta;
		Sprite sprite = back.get("binary");
		sprite.setV(scrollTimerA);
		sprite.setV2(scrollTimerA+1f);
		sprite = back.get("circuits");
		scrollTimerB -= backScrollSpeed*delta;
		sprite.setV(scrollTimerB);
		sprite.setV2(scrollTimerB+1f);
	}
	
	/** update config buttons state */
	private void checkConfig() {
		Preferences pref = Gdx.app.getPreferences(VirusByteGame.CONFIG_FILE);
		configButtons.get("sfx").setChecked( pref.getBoolean("sfx", true) );
		configButtons.get("vibration").setChecked( pref.getBoolean("vibration", true) );
		configButtons.get("music").setChecked( pref.getBoolean("music", true) );		
	}
	
	@Override
	public void resize(int width, int height) {
		Viewport view = mainMenuStage.getViewport();
		view.update(width, height, true);
		for (Sprite sprite : back.values()) {
			sprite.setSize(view.getWorldWidth(), view.getWorldHeight());
			sprite.setRegion(0f, 0f, view.getWorldWidth()/sprite.getTexture().getWidth(), view.getWorldHeight()/sprite.getTexture().getHeight());
		}
	}
	
	@Override
	public void dispose() {
		
		for ( Sprite bk : back.values() ) 
			bk.getTexture().dispose();

		if ( levelList != null )
			levelList.dispose();
			
		skin.dispose();
		mainMenuStage.dispose();
		mainTitleStage.dispose();
		
	}
	
	@Override
	public void hide() {
		Debug.log("another screen SET");
		dispose();
	}
	
	/////////////////=====///////////////////////
	
	/*private Table makeMainMenuTable() {
		if ( menuTable == null ) 
			menuTable = new Table(); // create the menu table 
		else 
			menuTable.clearChildren();
		
		menuTable.bottom();
//		menuTable.debug(); //set debug mode on
		Table.debugActorColor = Color.YELLOW;
		//add elements to menu table
		//menuTable.add( ).bottom().left().expandX().uniformX().pad(10f); //just to complete three columns cells
		float hGroupScale = 1.25f;
		menuTable.add( vGroup ).pad(10f).expandY().center();
		menuTable.row(); // break line
		hGroup.setScale( hGroupScale );
		menuTable.add( hGroup ).prefSize(hGroup.getPrefWidth() * hGroupScale, hGroup.getPrefHeight() * hGroupScale).padBottom(5);
		menuTable.row(); // break line
		//menuTable.add().expandX().uniformX(); //just to complete three columns cells
		
//		Debug.log( "table: " );
		menuTable.setFillParent(true);
		//setVolume
		
		return menuTable;
	}*/
	
	private Table makeMainTitleTable() {
		Table titleTable = new Table();
		titleTable.setFillParent(true);
		/*Texture tex = new Texture("data/sprites/backgrounds/main_title.png");
		Sprite spr = new Sprite(tex);
		spr.setColor(new Color(0.30f, 0.6f, 0.26f, 1f));
		titleTable.setBackground(new SpriteDrawable(spr));
		titleTable.getBackground();*/
		/*Image virusT = new Image( new Texture("data/sprites/backgrounds/main_title_virus.png") ), 
				byteT = new Image( new Texture("data/sprites/backgrounds/main_title_byte.png") );
		VerticalGroup vg = new VerticalGroup();
		vg.addActor(virusT);
		vg.addActor(byteT);
		vg.space(40f);
		
		titleTable.add(vg).expandY().bottom().padTop(60f);*/
		Image logo = new Image( skin.getDrawable("main-logo") );
		titleTable.add(logo).expandY().bottom().padTop(60f);
		titleTable.row();
		Label tts = new Label("Touch to Start", skin, "visitor32") {
			@Override
			public void act(float delta) {
				super.act(delta);
			}
		};
		tts.addAction( Actions.forever( Actions.sequence( Actions.fadeOut(0.5f), Actions.fadeIn(0.5f), Actions.delay(0.5f) ) ) );
		new Color();
		tts.setColor(Color.ORANGE);
		titleTable.add(tts).spaceBottom(35f).expandY().bottom();
		titleTable.row();
		titleTable.add( new Label("Design By", skin, "visitor25") );
		titleTable.row();
		//titleTable.add(new Label("2Devic", skin, "visitor22-bold-italic-shadow", "aqua-light")).padBottom(10f);
		titleTable.add( new Image(skin, "2devic-logo") );
		titleTable.row();
		titleTable.add( new Label("Programming by", skin, "visitor25") );
		titleTable.row();
		//titleTable.add(new Label("LKSFX", skin, "visitor22-bold-italic-shadow", "aqua-light"));
		titleTable.add( new Image(skin, "lksfx-logo") );
		titleTable.row();
		titleTable.add( new Image(skin, "paarthunax-logo") ).padBottom(10f);
		
		//titleTable.debug();
		return titleTable;
	}
	
	public void onScreenExit() {
		VirusByteGame.removeProcessor( mainMenuStage );
		VirusByteGame.removeProcessor( mainTitleStage );
	}
	
}