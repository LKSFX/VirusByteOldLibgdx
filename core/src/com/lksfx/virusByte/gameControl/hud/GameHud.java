package com.lksfx.virusByte.gameControl.hud;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.MyUtils;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.hud.Inventory.InventoryStatus;
import com.lksfx.virusByte.gameObject.pontuation.PointsManager;
import com.lksfx.virusByte.menus.GameOverCampaingMenu;
import com.lksfx.virusByte.menus.GameOverChallengeMenu;
import com.lksfx.virusByte.menus.Menu;
import com.lksfx.virusByte.menus.PauseMenu;
import com.lksfx.virusByte.menus.ShopMenu;
import com.lksfx.virusByte.screens.GameStageScreen;

public class GameHud {
	public static GameHud HUD;
	private GameStageScreen gameScreen;
	public Status state;
	public Skin skin;
	private Stage hud_stage;
	// main table carry all tables and left table most specific the left things of the screen
	private Table left_table = new Table(), center_table = new Table(), right_table = new Table();
	
//	private Menu currentMenu, pauseMenu, gameOverMenu, shopMenu;
	private ArrayMap<MENU, Menu> menuList;
	private Menu currentMenu;
	
	/**The main table where almost every cell is put*/
	private Table mainTableLayer1 = new Table().bottom();
	
	/**Is the table where alerts or other messages are showed*/
	private Table mainTableLayer2 = new Table();
	
	/**Every table in this array will be render on the HUD {@link #hud_stage}*/
	private Array<Table> tableArray = new Array<Table>();
	
	/**Used for any other object outside GameHud. This is the most higher table on the HUD*/
	public Table mainTableLayer3 = new Table();
	
	public Table lifebar_table = new Table(); //this is the table place where the life of the boss is
	public Inventory inventory;
//	public Slot slot; // is the bottom left side of screen where items can be stored 
	private ImageButton pauseButton;
	
	///Alerts
	/** This table show combo and messages on screen */
	private final Table alertsGroup = new Table(); // this table is where the combo and alerts are showed
	private StripLabel stripLabel = new StripLabel(2, 4);
	
	public GameHud( final GameStageScreen gameScreen ) {
		HUD = this;
		this.gameScreen = gameScreen;
		skin = new Skin( Gdx.files.internal("data/ui/newSkin.json") );
		skin.add( "hud-font22", MyUtils.ttfToBitmap("visitor2.ttf", 22), BitmapFont.class );
		ExtendViewport viewport = new ExtendViewport( VirusByteGame.VIEWPORT.getMinWorldWidth(), VirusByteGame.VIEWPORT.getMinWorldHeight(),
				VirusByteGame.VIEWPORT.getMaxWorldWidth(), VirusByteGame.VIEWPORT.getMaxWorldHeight(), VirusByteGame.HUD_CAMERA );
		hud_stage = new Stage( viewport, gameScreen.batch ); //a new stage where everything of the hud interacts 
		skin_config();
		
		leftTableSetup(); //set the left table
		rightTableSetup(); //set the right table
//		center_table.add( alertsGroup ); // insert alerts on center_table
		
		mainTableLayer1.setFillParent( true ); // main table fill entire screen
		mainTableLayer2.setFillParent( true );
		mainTableLayer3.setFillParent( true );
		mainTableLayer1.pad( 10f );
		setGameHud(); // this is where the main_table is set
		//lifebar_table.debug();
		//main_table.debug();
		//center_table.debug();
//		left_table.debug();
		//table.debugCell();
		//alertsGroup.debug();
		
		// LAYER 2 //
//		mainTableLayer2.debug();
		bossAlertCell = mainTableLayer2.add( new Table() ).prefSize(hud_stage.getWidth(), hud_stage.getHeight()*.5f);
		bossAlertCell.row();
		comboAlertCell = mainTableLayer2.add( alertsGroup ).expand().padBottom(10f).bottom();
		
		// LAYER 3 //
		mainTableLayer3.setSkin(skin);
		
		//MENUS
		menuList = new ArrayMap<GameHud.MENU, Menu>();
		menuList.put( MENU.PAUSE, new PauseMenu(hud_stage, skin) );
		menuList.put( MENU.GAMEOVER_CHALLENGE_MODE, new GameOverChallengeMenu(hud_stage, skin) );
		menuList.put( MENU.GAMEOVER_CAMPAING_MODE, new GameOverCampaingMenu(hud_stage, skin) );
		menuList.put( MENU.SHOP, new ShopMenu(hud_stage, skin) );
		
		//insert layer table on the actor
		hud_stage.addActor( mainTableLayer1 );
		hud_stage.addActor( mainTableLayer2 );
		hud_stage.addActor( mainTableLayer3 );
		Debug.log("GameStarted: table total cells = " + mainTableLayer1.getCells().size);
		VirusByteGame.addProcessor( hud_stage ); //add to inputs processor
		
		inventory = new Inventory(this);
	}
	
	/**load inventory status from last game play*/
	public void loadPreviousInventoryStatus() {
		//Check for inventory status
		FileHandle file = Gdx.files.local("data/inventory.vb");
		if ( file.exists() ) {
			Json json = new Json();
			InventoryStatus lastInventoryStatus = json.fromJson(InventoryStatus.class, file);
			inventory.load( lastInventoryStatus );
		} 
	}
	
	private void skin_config() {
		skin.add("default", MyUtils.ttfToBitmap("visitor2.ttf", 35));
		skin.get("medium", TextButton.TextButtonStyle.class).font = skin.get("default", BitmapFont.class);
		//skin.getFont("visitor20-bold-italic").getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		Iterator<Entry<String, BitmapFont>> fonts = skin.getAll(BitmapFont.class).iterator();
		while (fonts.hasNext()) {
			fonts.next().value.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
	}
	
	public Label hudPoints, hudKills, hudLives, hudStage;
	
	private void leftTableSetup() {
		LabelStyle style = new LabelStyle(skin.getFont("hud-font22"), new Color( Color.WHITE )); //style for all labels
		hudPoints = new Label("0", style);
		hudKills = new Label("0", style);
		hudLives = new Label("0", style);
		hudStage = new Label("0", style);
		
		Table tb1 = new Table();
//		tb1.debug();
		//lives hud
		tb1.add( new Image( skin.getDrawable("small-battery") ) );
		tb1.add( new Label( "lives: ", style ) );
		tb1.add( hudLives );
		tb1.row();
		//kills hud
		tb1.add( new Image( skin.getDrawable("small-mobicon") ) );
		tb1.add( new Label("kills: ", style) );
		tb1.add( hudKills );
		tb1.row();
		//points hud
		Table tb2 = new Table();
		tb2.add( new Label("pts: ", style) ).left();
		tb2.add( hudPoints );
		tb2.row();
		//stage hud
		tb2.add( new Label("stage: ", style) );
		tb2.add( hudStage );
		
		//vg.space(5f);
		left_table.add( /*vg*/tb1 ).top().left().expandX(); //hud buttons is inside left_table and upper to lifebar_table
		left_table.row();
		left_table.add( /*vg*/tb2 ).top().left().expand(); //hud buttons is inside left_table and upper to lifebar_table
		left_table.row();
		
		left_table.add( lifebar_table ).left().expandY().top(); //life is inside left_table, here's go the boss health bar
	}
	
	/** this container is useful to use in item tutorial */
	public Container<Image> slotPointer;
	
	private void rightTableSetup() {
		//slot create
		VerticalGroup vg = new VerticalGroup();
		Sprite seta_spr = new Sprite( VirusByteGame.ASSETS.getAssetManager().get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("pointer") );
		seta_spr.rotate90(true);
		seta_spr.flip(true, false);
		seta_spr.setSize(44f, 64f);
		Image seta = new Image( new SpriteDrawable( seta_spr ) );
		vg.addActor( slotPointer = new Container<Image>( seta ) );
		slotPointer.setVisible(false);
//		slot = new Slot(); //create a new slot
//		vg.addActor( slot );
		vg.space( 5f );
		//create pause button, util for hatml game
		pauseButton = new ImageButton(skin, "pause");
		pauseButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Debug.log( "pause button presset!" );
				if ( state == Status.PLAYING ) {
					setMainLayoutVisible( false );
					gameScreen.pauseGame(null);
				} else {
					gameScreen.unpauseGame();
				}
				event.stop();
			}
		});
		right_table.add(pauseButton).top().right().expand();
		right_table.row();
		right_table.add( vg ).bottom().right().expand().minSize(54f);
	}
	
	public enum Status { PAUSED, PLAYING, GAMEOVER }
	
	public void update(float delta) {
		hud_stage.act( delta );
	    hud_stage.draw();
	}
	
	/**Action evoked when the game screen is paused
	 * @param menu TODO*/
	public void pauseGame( MENU menu ) {
		setMainLayoutVisible( false );
		insertMenuLayout( menu );
		setPauseHud();
	}
	
	/**Action evoked when the game screen is un-paused*/
	public void unpauseGame() {
		setMainLayoutVisible( true );
		removeMenuLayout();
		setGameHud();
	}
	/** set the main_table components and inner tables for game play */
	private void setGameHud() {
		//clear layers
		mainTableLayer1.clearChildren();
		
		mainTableLayer1.row().uniformX();
		
		mainTableLayer1.add(left_table).expand().fill();
		
		center_table.align( Align.bottom );
		//mainTableLayer2.add( center_table ).fill().prefWidth(200f).align(Align.center);//.prefSize(200f, 50f).align(Align.center).bottom();
		//main_table.add(slot).bottom().right().expand().minSize(54f);
		
		mainTableLayer1.add(right_table).expand().fill();
		state = Status.PLAYING;
		Debug.log("SetGameHud(): table total cells = " + mainTableLayer1.getCells().size);
	}
	
	private void setPauseHud() {
		state = Status.PAUSED;
	}
	
	/**Set the main layout visible or not*/
	private void setMainLayoutVisible(boolean bool) {
		mainTableLayer1.setVisible( bool );
		mainTableLayer2.setVisible( bool );
		mainTableLayer3.setVisible( bool );
	}
	
	public enum MENU { PAUSE, GAMEOVER_CHALLENGE_MODE, GAMEOVER_CAMPAING_MODE, SHOP }
	
	/**Insert a menu layout to the stage*/
	private void insertMenuLayout( MENU state ) {
		
		if ( menuList.containsKey(state) ) {
			currentMenu = menuList.get( state );
			currentMenu.open();
		}
		
	}
	
	/**Remove any menu layout added previously*/
	private void removeMenuLayout() {
		if ( currentMenu != null ) {
			currentMenu.close();
			currentMenu = null;
		}
	}
	
	public void setEndGameHud(final boolean virusTestMode) { //set the end game screen
		
		state = Status.GAMEOVER;
	}
	
	/**Insert a table from another game object to the main hud stage*/
	public Table insertTableOnMainHUD(Table table, boolean fillParent) {
		tableArray.add( table );
		updateTablesOrder();
		table.setFillParent( fillParent );
		return table;
	}
	
	/**Remove a table inserted on the HUD*/
	public void removeTableOnTheHud(Table table) {
		if ( tableArray.contains(table, true) ) {
			tableArray.removeValue(table, true);
			updateTablesOrder();
		}
	}
	
	/**Call this every time that the {@link #tableArray} has been modified*/
	private void updateTablesOrder() {
		hud_stage.clear();
		hud_stage.addActor( mainTableLayer1 );
		hud_stage.addActor( mainTableLayer2 );
		for (Table tb : tableArray) hud_stage.addActor( tb );
		hud_stage.addActor( mainTableLayer3 );
	}
	
	/// ============================================== ///
	/// ============================================== ///
	/// ============================================== ///
	// COMBO ALERT //
	public Cell<Table> comboAlertCell;
	
	public void addAlert( PointsManager.ComboLog comboLog ) {
		alertsGroup.clearChildren();
		PooledLabel label = stripLabel.obtain( comboLog.points + " pts" );
		
		if ( comboLog.tex != null ) { // if this comboLog has a texture to render on screen
			Image icon = new Image( comboLog.tex );
			icon.setColor(1f, 1f, 1f, 0.7f);
			
			Debug.log("POSITION: " + alertsGroup.getX(Align.center) + " "+ alertsGroup.getY(Align.center));
			PooledLabel lb = stripLabel.obtain( "X"+comboLog.size ), // the size of the combo
						pointToShowLabel = stripLabel.obtain( comboLog.msg ); // some extra message to show
			alertsGroup.stack( icon, lb ).minSize(64f, 64f);
					
			alertsGroup.row();
			alertsGroup.stack( label, pointToShowLabel );
			
			pointToShowLabel.setAlignment( Align.center, Align.center );
			label.setAlignment( Align.center, Align.center );
			lb.setAlignment( Align.center, Align.center );
			
//			pointToShowLabel.setPosition( 32f, 0f );
			lb.addAction( Actions.moveTo(22f, 0f, 0.5f) );
			label.addAction( Actions.moveTo(0f, 100f, 1f) );
			//if (mainTableLayer1.getCell(alertsGroup) != null) mainTableLayer1.getCell(alertsGroup).bottom();//.prefWidth(label.getPrefWidth()).bottom();
//			return;
		} else { //when haven't texture to show 
			Debug.log("POSITION: " + alertsGroup.getX(Align.center) + " "+ alertsGroup.getY(Align.center));
			PooledLabel lb = stripLabel.obtain( "X"+comboLog.size ), // the size of the combo
						pointsLabel = stripLabel.obtain( comboLog.msg ); // some extra message to show
			alertsGroup.stack( lb ).minSize( 64f, 64f );
					
			alertsGroup.row();
			alertsGroup.stack( label, new Container<PooledLabel>(pointsLabel) );
			
			pointsLabel.setPosition( 32f, 0f );
			lb.addAction( Actions.moveTo(22f, 0f, 0.5f) );
			label.addAction( Actions.moveTo(0f, 100f, 1f) );
			//if (mainTableLayer1.getCell(alertsGroup) != null) mainTableLayer1.getCell(alertsGroup).bottom();//.prefWidth(label.getPrefWidth()).bottom();
		}
	}
	
	// ================ //
	// BOSS ALERT //
	public Cell<Table> bossAlertCell;
	
	/** this alert appears in the same place of the combo and show one message that's tell, the boss is coming */
	public void addBossAlert(Table table) {
		bossAlertCell.setActor( table );
	}
	
	/** when previously called the addBossAlert use this method to remove the alert and return the combo display back to the right place */
	public void removeBossAlert() {
		bossAlertCell.clearActor();
	}
	
	// ============ //
	
	/** Remove combo or other alert from screen */
	public void removeAlert() {
		alertsGroup.clearChildren();
	}
	
	
	/** Pool of stripped lightened labels to show alert combo */
	public class StripLabel extends Pool<PooledLabel> {
		public StripLabel(int min, int max) {
			super(min, max);
		}
		
		@Override
		protected PooledLabel newObject() {
			return new PooledLabel("", skin, "visitor32");
		}
		
		public PooledLabel obtain(String msg) {
			PooledLabel label = super.obtain();
			label.setText(msg);
			return label;
		}
	}
	
	public class PooledLabel extends Label implements Poolable {
		
		public PooledLabel(CharSequence text, Skin skin, String fontName) {
			super(text, skin, fontName);
			//Debug.log("position stripLabel: " + getOriginX() + " " + getOriginY());
		}
		
		private float timer = 0.05f;
		private Color white = new Color(1f, 1f, 1f, 0.5f), black = new Color(0f, 0f, 0f, 0.5f);
		
		@Override
		public void act(float delta) {
			super.act(delta);
			timer -= delta;
			
			if ( timer < 0f ) {
				timer = 0.05f;
				setColor(getColor().equals(white) ? black : white );
			}
		}
		
		@Override
		public boolean remove() {
			//stripLabel.free(this);
			if ( super.remove() ) {
				stripLabel.free(this);
				return true;
			}
			return false;
		}
		
		@Override
		public void reset() {
			setText("");
			setPosition(0f, 0f);
		}
		
	}
	
	public Stage getStage() {
		return hud_stage;
	}
	
	public void dispose() {
		// Dispose all menus from list
		for ( int i = 0; i < menuList.size; i++ )
			menuList.getValueAt( i ).dispose();
		
		skin.dispose();
		hud_stage.dispose();
		VirusByteGame.removeProcessor( hud_stage );
	}
}
