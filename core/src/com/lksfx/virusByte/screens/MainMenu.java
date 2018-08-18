package com.lksfx.virusByte.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.MyUtils;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.menus.ConfigButton;
import com.lksfx.virusByte.menus.MyButton;

public class MainMenu extends ScreenAdapter {
	private VirusByteGame game;
	private Viewport viewport;
	private SpriteBatch batch;
	
	private Skin skin;
	private Stage mainMenu, mainTitle;
	private boolean titleScreen;
	private VerticalGroup vGroup;
	private HorizontalGroup hGroup;
	private Array<MyButton> vButtons;
	private ArrayMap<String, ConfigButton> configButtons;
	private Texture back;
	private Sprite scanlines;
	private int tab = 0;
	
	public MainMenu(VirusByteGame game, boolean titleScreen) {
		this.game = game;
		this.titleScreen = titleScreen;
		viewport = game.viewport;
		batch = game.batch;
		back = new Texture(Gdx.files.internal("data/sprites/backgrounds/main_menu.png"));
		Texture tex = new Texture(Gdx.files.internal("data/sprites/backgrounds/scanlines.png"));
		tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		scanlines = new Sprite(tex);
		
		skin = new Skin(Gdx.files.internal("data/ui/uimenu.json"));
		skin.add("default", MyUtils.ttfToBitmap("visitor2.ttf", 35));
		mainMenu = new Stage(viewport, batch);
		skin.get("medium", TextButton.TextButtonStyle.class).font = skin.get("default", BitmapFont.class);
		//Debug.log("skin has: "+ skin.get(BitmapFont.class));
		final Color trans = new Color(0f, 0f, 0f, 0f);
		vButtons = new Array<MyButton>() {{
			add(new MyButton("Play", skin, "medium", trans));
			add(new MyButton("Leaderboard", skin, "medium", trans));
			add(new MyButton("Tutorial", skin, "medium", trans));
			add(new MyButton("Effects", skin, "medium", trans));
			//tab 2
			add(new MyButton("Normal", skin, "medium", trans));
			add(new MyButton("Premium", skin, "medium", trans));
			add(new MyButton("Back", skin, "medium", trans));
		}};
		//skin.getFont("default").setMarkupEnabled(true);
		configButtons = new ArrayMap<String, ConfigButton>() {{
			put("sfx", new ConfigButton(skin, "sfx-switch"));
			put("vibration", new ConfigButton(skin, "vibration-switch"));
			put("music", new ConfigButton(skin, "music-switch"));
		}};
		
		vGroup = new VerticalGroup();
		vGroup.setName("vertical");
		//Debug.log("Vgroup: " + vGroup.getFill());
		hGroup = new HorizontalGroup().space(40f);
		//for (int i = 0; i < configButtons.values.length; i++)
		hGroup.addActor(configButtons.get("sfx"));
		hGroup.addActor(configButtons.get("vibration"));
		hGroup.addActor(configButtons.get("music"));
			
		for (int i = 0; i < 4; i++)
			vGroup.addActor(vButtons.get(i));
		Debug.log("vGroup: " + vGroup.findActor("button1"));
		//vGroup.findActor("button1").setWidth(200f);
		//vGroup.addActor(hGroup);
		vGroup.space(25f);
		
		Table table = new Table();
		table.bottom();
		//table.debug(); //set debug mode on
		table.add(vGroup).pad(10f);
		table.row();
		table.add(hGroup).pad(10f).padBottom(20f).padTop(40f);
		hGroup.padTop(40f);
		Debug.log("table: " + table.getCell(vGroup).getMinWidth());
		table.setFillParent(true);
		mainMenu.addActor(table);
		Debug.log("stage: " + mainMenu.getWidth());
		VirusByteGame.MAIN_MULTIPLEX_CONTROLLER.clear();
		
		//scanlines.setU(stage.getViewport().getWorldWidth()/64);
		scanlines.setU2(mainMenu.getViewport().getWorldWidth()/64);
		//scanlines.setV(stage.getViewport().getWorldHeight()/64);
		scanlines.setV2(mainMenu.getViewport().getWorldHeight()/64);
		//
		if (Gdx.files.isLocalStorageAvailable()) {
			if (Gdx.files.local("config.ini").exists()) Gdx.files.local("config.ini").delete();
			Debug.log(Gdx.files.local("config.ini").path());
			checkConfig();
		}
		
		mainTitle = new Stage(viewport, batch);
		Table titleTable = new Table();
		titleTable.setFillParent(true);
		tex = new Texture(Gdx.files.internal("data/sprites/backgrounds/main_title.png"));
		Sprite spr = new Sprite(tex);
		spr.setColor(new Color(0.30f, 0.6f, 0.26f, 1f));
		titleTable.setBackground(new SpriteDrawable(spr));
		titleTable.getBackground();
		Image virusT = new Image(new Texture(Gdx.files.internal("data/sprites/backgrounds/main_title_virus.png"))), 
				byteT = new Image(new Texture(Gdx.files.internal("data/sprites/backgrounds/main_title_byte.png")));
		VerticalGroup vg = new VerticalGroup();
		vg.addActor(virusT);
		vg.addActor(byteT);
		vg.space(40f);
		
		titleTable.add(vg).expandY().bottom().padTop(60f);
		titleTable.row();
		Label tts = new Label("Touch to Start", skin, "visitor20-white") {
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
		titleTable.add(new Label(" Programming by LKSFX\nDesign By Victor Halla", skin, "visitor22-title")).padBottom(50f);
		//titleTable.debug();
		mainTitle.addActor(titleTable);
		
		if (titleScreen) {VirusByteGame.MAIN_MULTIPLEX_CONTROLLER.addProcessor(mainTitle);} else {VirusByteGame.MAIN_MULTIPLEX_CONTROLLER.addProcessor(mainMenu);}
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	    mainMenu.act(delta);
	    mainMenu.getBatch().setColor(1f, 1f, 1f, 1f);
	    mainMenu.getBatch().begin();
	    mainMenu.getBatch().draw(back, 0, 0, mainMenu.getViewport().getWorldWidth(), mainMenu.getViewport().getWorldHeight());
	    mainMenu.getBatch().draw(scanlines, 0, 0, mainMenu.getViewport().getWorldWidth(), mainMenu.getViewport().getWorldHeight());
	    mainMenu.getBatch().end();
	    mainMenu.draw();
	    
	    for (int i = 0; i < vButtons.size; i++) {
	    	if (vButtons.get(i).isChecked()) {
	    		Debug.log(""+vButtons.get(i).getName());
	    		if ( VirusByteGame.SFX ) {
	    			// AUDIO TODO
	    		}
	    		if (vButtons.get(i).getName().equals("Play") || vButtons.get(i).getName().equals("Back") ) browseTab();
	    		if (vButtons.get(i).getName().equals("Normal") ) game.setScreen(new ChallengeGameMode(game, false));
	    		if (vButtons.get(i).getName().equals("Premium") ) game.setScreen(new ChallengeGameMode(game, true));
	    		if (vButtons.get(i).getName().equals("Tutorial") ) game.setScreen(new TutorialScreen(game, true));
	    		vButtons.get(i).setChecked(false);
	    	}
	    }
	    if (Gdx.input.justTouched()) {
	    	for (int i = 0; i < configButtons.size; i++) {
		    	if (configButtons.getValueAt(i).isPressed()) {
		    		Debug.log("button " + configButtons.getKeyAt(i) + " is pressed");
		    		Preferences pref = Gdx.app.getPreferences(VirusByteGame.CONFIG_FILE);
		    		if (!configButtons.getValueAt(i).isChecked()) {
		    			pref.putBoolean(configButtons.getKeyAt(i), true);
		    		} else {
		    			pref.putBoolean(configButtons.getKeyAt(i), false);
		    		}
		    		VirusByteGame.updateConfig();
		    	}
		    }
	    }
	    
	    if (titleScreen) {
	    	mainTitle.act(delta);
	    	mainTitle.draw();
	    	if (Gdx.input.justTouched()) {
	    		mainTitle.addAction( Actions.sequence( Actions.fadeOut(1f), Actions.run(new Runnable() {
	    			@Override
	    			public void run() {
	    				VirusByteGame.MAIN_MULTIPLEX_CONTROLLER.addProcessor(mainMenu);
	    			}
	    		}) ) );
	    	}
	    }
	    
	    Debug.debug.screen(""+ (Gdx.files.local("data").isDirectory() ? true : false), 10, 10);
	    Debug.debug.show();
	}
	
	private void browseTab() {
		if (tab == 0) { //Go to play menu
    		for (int j = 0; j < 4; j++) {
    			vGroup.removeActor(vButtons.get(j));
    		}
    		for (int j = 4; j < 7; j++) {
    			vGroup.addActor(vButtons.get(j));
    		}
    		//hGroup.setVisible(false);
    		vGroup.padBottom(50f);
    		tab = 1;
		} else if(tab == 1) {
			for (int j = 4; j < 7; j++) {
    			vGroup.removeActor(vButtons.get(j));
    		}
			for (int j = 0; j < 4; j++) {
    			vGroup.addActor(vButtons.get(j));
    		}
			hGroup.setVisible(true);
			vGroup.padBottom(0f);
			tab = 0;
		} 
	}
	
	@Override
	public void resize(int width, int height) {
		mainMenu.getViewport().update(width, height, true);
	}

	@Override
	public void dispose() {
		skin.dispose();
		mainMenu.dispose();
	}
	
	private void checkConfig() {
		Preferences pref = Gdx.app.getPreferences(VirusByteGame.CONFIG_FILE);
		configButtons.get("sfx").setChecked( pref.getBoolean("sfx", true) );
		configButtons.get("vibration").setChecked( pref.getBoolean("vibration", true) );
		configButtons.get("music").setChecked( pref.getBoolean("music", true) );
	}
	
	@Override
	public void hide() {
		Debug.log("another screen SET");
		dispose();
	}
	
}
