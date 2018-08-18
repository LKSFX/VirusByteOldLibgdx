package com.lksfx.virusByte.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.hud.GameHud.MENU;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType.ACTION;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType.ActionListener;

public class VirusLab extends GameStageScreen {
	
	private boolean debug = true;
	
	public VirusLab(VirusByteGame game, boolean premium) {
		super(game, premium);
		initialize();
		MakeWindow();
		back.activeRandomBackground = false;
		normalArray = new VirusInstance[] {VirusInstance.Plague, VirusInstance.Psycho};
		premiumArray = new VirusInstance[] {VirusInstance.Plague, VirusInstance.Energyzer, VirusInstance.Flamebot, VirusInstance.Shockbot};
		currentArray = ( premium ) ? premiumArray : normalArray;
		stopVirus = new ActionListener() {
			@Override
			public void execute(VirusType virus) {
				virus.isOnMove = false;
				virus.validadePoints = false;
				virus.minSpd = 0;
				virus.maxSpd = 0;
			}
		};
		selectVirus( cursorPosition );
	}
	
	private final float spawnInterval = 2f;
	private float spawnTime;
	private VirusInstance[] normalArray, premiumArray, currentArray; 
	private int cursorPosition;
	private VirusType virusOnStage;
	private ActionListener stopVirus;
	
	@Override
	public void render(float delta) {
		super.render(delta);
		if ( virusOnStage == null && !PAUSED && (spawnTime += delta) >= spawnInterval ) {
			spawnTime = 0;
			selectVirus( cursorPosition );
		}
		if ( debug && virus_manager.SCREEN_DEBUG ) {
			VirusInstance instance = currentArray[cursorPosition];
			Debug.debug.screen("total " + instance + " on stage: " + virus_manager.getTotalInstances( instance ) , 10, 420);
		}
	}
	
	
	/**Table with the all the lab HUD stuff*/
	Table labTable;
	
	private void MakeWindow() {
		labTable = hud.insertTableOnMainHUD( new Table() , true );
		float worldWidth = VirusType.WORLD_WIDTH, worldHeight = VirusType.WORLD_HEIGHT;
		Table table1 = new Table(), table2 = new Table();
//		labTable.debug();
//		table1.debug();
//		table2.debug();
		table1.left().top();
		table2.left().top();
		final Array<ImageTextButton> tabList = new Array<ImageTextButton>();
		final ChangeListener TabListener = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if ( actor instanceof ImageTextButton ) {
					ImageTextButton tab = (ImageTextButton) actor;
					if ( tab.isChecked() ) {
						String name = tab.getName();
						Tabs thisTab = ( name == "desc" ) ? Tabs.DESC : (( name == "info" ) ? Tabs.INFO : Tabs.MORE );
						tab.setDisabled( true );
						for ( ImageTextButton tb : tabList ) if ( tb != actor ) tb.setChecked( false );
						updateTab( thisTab );
						Debug.log( name + " selected!" );
					} else {
						tab.setDisabled( false );
					}
				}
			}
		};
		
		// Tabs
		tabList.add( new ImageTextButton("Desc", hud.skin, "window-tab") {{setName("desc"); addListener(TabListener); setChecked(true);}} );
		tabList.add( new ImageTextButton("Info", hud.skin, "window-tab") {{setName("info"); addListener(TabListener);}} );
		tabList.add( new ImageTextButton("", hud.skin, "window-tab") {{setName("more"); addListener(TabListener);}} );
		for ( ImageTextButton tab : tabList ) table2.add( tab );
		
		//Arrow
		ImageButton leftArrow, rightArrow;
		Table topTable = new Table();
		final ChangeListener arrowListener = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if ( actor.getName() == "leftArrow" || actor.getName() == "rightArrow" ) {
					int max = currentArray.length-1;
					cursorPosition += ( actor.getName() == "leftArrow" ) ?  ( (cursorPosition == 0) ? 0 : -1) : ( ( cursorPosition == max ) ? 0 : 1 );
					selectVirus( cursorPosition );
					Debug.log( actor.getName() + " pressed!" );
					event.stop();
				}
			}
		};
		leftArrow = new ImageButton(hud.skin, "seta-left") {{
			setName( "leftArrow" );
			addListener( arrowListener );
		}};
		rightArrow = new ImageButton(hud.skin, "seta-right") {{
			setName( "rightArrow" );
			addListener( arrowListener );
		}};
//		topTable.debug();
		topTable.add( leftArrow );
		topTable.add().expandX();
		topTable.add( rightArrow );
		
		Stack stack = new Stack();
		stack.add( table1 );
		stack.add( table2 );
//		hud.mainTableLayer3.debug();
		labTable.bottom().padBottom( worldHeight*.05f );
		labTable.add( topTable ).prefWidth(worldWidth * .8f).expandY();
		labTable.row();
		Container<Stack> container = new Container<Stack>( stack );
		container.prefSize(worldWidth*.85f, worldHeight*.5f);
		Cell<Container<Stack>> stackCell = labTable.add( container );//.prefSize(worldWidth*.6f, worldHeight*.5f);
		Debug.log( "Stack is width: " + stackCell.getPrefWidth() + " | height: " + stackCell.getPrefHeight() );
		Debug.log( "Tab height: " + tabList.get(1).getPrefHeight() );
//		float topTabsPadding = ;
		table1.padTop(25);
		
		//Set window
		Window window;
		Cell<Window> windowCell = table1.add( window = new Window("", hud.skin, "frame1") ).prefSize(stackCell.getPrefWidth(), stackCell.getPrefHeight());
//		window.debug();
		window.setMovable(false);
		float windowWidth = windowCell.getPrefWidth(), windowHeigth = windowCell.getPrefHeight(), topPadding = (windowHeigth/100) * 10, 
				sidesPadding = (windowWidth/100) * 3.6f, bottomPadding = (windowHeigth/100) * 3.6f;
		window.pad(topPadding, sidesPadding, bottomPadding, sidesPadding+2f);
		window.left();
		/*String string = "Often appears, but is not a major threat. Are known to damage files and infect the system."
				+ " They may be quick, but a simple tap is able to kill them. "
				+ "They also are weak against all kinds of defensive items."
				+ "They are more rare, but are a big threat. Being a upgraded version of the Plague, it's bigger, uglier and more resistant than its weak version."
				+ " Will overwrite your files if it infects you, so be careful. It's immune to bombs, and to kill it,you must tap it thrice consecutively."
				+ "It corrupts files and its capable of replication, so it's often seen in hordes. To defeat it, you can drag it and throw it away off screen."
				+ " However, you can use it to kill other viruses, making it a useful weapon. It cannot harm certain types of virus though.";*/
		descriptionTable = new Table();
		informationTable = new Table();
		informationTable.setSkin( hud.skin );
		moreTable = new Table();
		ScrollPane panel = new ScrollPane( descriptionTable, hud.skin );
		panel.setScrollingDisabled(true, false);
		panel.setVariableSizeKnobs( false );
		panel.setFadeScrollBars( false );
		panel.setOverscroll(false, false);
		windowPanel = window.add( panel ).prefSize(windowWidth, windowHeigth).fill();
//		insertText( string );
	}
	
	private Cell<ScrollPane> windowPanel;
	private Table descriptionTable, informationTable, moreTable;
	private enum Tabs {DESC, INFO, MORE}
	public Tabs currentTab = Tabs.DESC;
	
	private void setDescriptionTab(String string) {
		float windowWidth = windowPanel.getPrefWidth() - VirusType.WORLD_WIDTH * .1f;
		descriptionTable.clear();
		Table table = descriptionTable;
		table/*.debug()*/.top().center().padLeft(3f);
		TextArea text = new TextArea(string, hud.skin);
		text.setDisabled( true );
		BitmapFont font = text.getStyle().font;
		TextBounds textBounds = font.getWrappedBounds( text.getText(), windowWidth);
		float 
			textWidth = textBounds.width, 
			textHeight = textBounds.height * 1.1f,
			totalHeight = textHeight;
		Debug.log( "WindowWidth: " + windowWidth + " | Text bounds> width: " +  textWidth + " | height: " + textHeight );
		table.add( text ).prefSize( windowWidth + VirusType.WORLD_WIDTH * .1f, totalHeight )/*.expandX()*/.fill();
	}
	
	private void setInformationTab(String name, int minCombo, int threatLv) {
//		float windowWidth = windowPanel.getPrefWidth();
		HorizontalGroup hg = new HorizontalGroup();
		HorizontalGroup innerG;
		informationTable.clear();
		Table table = informationTable;
		table/*.debug()*/.top().left().padLeft(3f);
		table.defaults().spaceBottom( 20f ).expandX().left();
		table.add( "Scientific Name: ", "visitor20" ).space(0, 0, 0, 0);
		table.row();
		table.add( name, "visitor20" ).colspan(2).center();
		table.row();
		hg.addActor( new Label("Minimum Combo: ", hud.skin, "visitor20") );
		innerG = new HorizontalGroup();
		innerG.space(5f);
		innerG.addActor( new Label( "x", hud.skin ) );
		innerG.addActor( new Label(""+minCombo, hud.skin, "visitor20") );
		hg.addActor( innerG );
		hg.space(20f);
		table.add( hg );
		table.row();
		hg = new HorizontalGroup();
		hg.addActor( new Label("Threat: ", hud.skin, "visitor20") );
		hg.space( 20f );
		innerG = new HorizontalGroup();
		for (int i = 0; i < 5; i++) {
			Drawable icon = hud.skin.newDrawable("threat-icon", ( i <= threatLv ) ? Color.CYAN : Color.WHITE );
			innerG.addActor( new ImageButton(icon) );
		}
		hg.addActor( innerG );
		table.add( hg ); 
	}
	
	@Override
	public void pauseGame(MENU menu) {
		labTable.setVisible( false );
		super.pauseGame(menu);
	}
	
	@Override
	public void unpauseGame() {
		labTable.setVisible( true );
		super.unpauseGame();
	}
	
	private void updateTab(Tabs tab) {
		if ( descriptionTable == null || informationTable == null || moreTable == null ) return;
		ScrollPane panel = windowPanel.getActor();
		switch ( tab ) {
		case DESC:
			panel.setWidget( descriptionTable );
			break;
		case INFO:
			panel.setWidget( informationTable );
			break;
		case MORE:
			panel.setWidget( moreTable );
			break;
		default:
			panel.setWidget( descriptionTable );
			break;
		}
		Debug.log( "description table width: " + descriptionTable.getWidth() + " | height: " + descriptionTable.getHeight() );
		currentTab = tab;
	}
	
	/**Select a virus to put on the stage*/
	public void selectVirus(int n) {
		if ( n >= currentArray.length || n < 0) return;
		virus_manager.clearAll( ( virusOnStage != null && virusOnStage.virus_id == currentArray[n] ) ? virusOnStage.getClass() : null );
		virusOnStage = virus_manager.addVirus(VirusType.WORLD_WIDTH * .5f, VirusType.WORLD_HEIGHT * .8f, stopVirus, currentArray[n]);
		virusOnStage.addActionListener(ACTION.ON_DESTROY, new ActionListener() {
			@Override
			public void execute(VirusType virus) {
				if ( virusOnStage == virus ) virusOnStage = null;
				Debug.log( "VIRALAAAA" );
			}
		});
		if ( labMap.containsKey( currentArray[n] ) ) labMap.get( currentArray[n] ).setShowCase();
	}
	
	private ArrayMap<VirusInstance, VirusCase> labMap;
	
	private void initialize() {
		labMap = new ArrayMap<VirusInstance, VirusCase>();
		String
		antivirus = "",
		bomb = "",
		bot = "",
		energyzer = "A virus capable of providing energy to other virus by spinning. It normally is very slow and do not harm your device, but it can make other virus stronger. To kill it, you can rotate it, causing it to overpower and die, or you can simply use items.",
		flamebot = "",
		infector = "It corrupts files and its capable of replication, so it's often seen in hordes. To defeat it, you can drag it and throw it away off screen. However, you can use it to kill other viruses, making it a useful weapon. It cannot harm certain types of virus though.",
		nyxel = "They are more rare, but are a big threat. Being a upgraded version of the Plague, it's bigger, uglier and more resistant than its weak version. Will overwrite your files if it infects you, so be careful. It's immune to bombs, and to kill it,you must tap it thrice consecutively.",
		parasite = "It is similar to Infectonator, but it infects other viruses and spawn when the host is killed. You need to drag him off screen to kill it, and unlike Infector you won't be liking throwing it on other viruses. If you do, it will infect the virus and may be harder to get rid of it. However, if a virus is already infected by a Parasite, it will explode killing all the Parasites.",
		plague = "Often appears, but is not a major threat. Are known to damage files and infect the system. They may be quick, but a simple tap is able to kill them. They also are weak against all kinds of defensive items.",
		psycho = "This virus is known by its ability to revert its colours. It will change file formats if it infects. It's a little tougher than Plague, but still, not a big threat. To kill it, you must tap it twice consecutively, items also work well.",
		saw = "",
		shockbot = "",
		spyware = "",
		trojan = "",
		worm = "";
		labMap.put( VirusInstance.Antivirus, new VirusCase("", antivirus, 0, 0) );
		labMap.put( VirusInstance.Bomb, new VirusCase("", bomb, 0, 0) );
		labMap.put( VirusInstance.Bot, new VirusCase("", bot, 0, 0) );
		labMap.put( VirusInstance.Energyzer, new VirusCase("Adenoviros generator", energyzer, 2, 3) );
		labMap.put( VirusInstance.Flamebot, new VirusCase("", flamebot, 0, 0) );
		labMap.put( VirusInstance.Infector, new VirusCase("Bacteriophagos infectoris", infector, 3, 2) );
		labMap.put( VirusInstance.Nyxel, new VirusCase("Ixodus Komosutrus", nyxel, 2, 4) );
		labMap.put( VirusInstance.Parasite, new VirusCase("Bacteriophagos parasitus", parasite, 2, 3) );
		labMap.put( VirusInstance.Plague, new VirusCase("Argasidus pestis", plague + " " + energyzer, 3, 1) );
		labMap.put( VirusInstance.Psycho, new VirusCase("", psycho, 3, 2) );
		labMap.put( VirusInstance.Saw, new VirusCase("", saw, 0, 0) );
		labMap.put( VirusInstance.Shockbot, new VirusCase("", shockbot, 0, 0) );
		labMap.put( VirusInstance.Spyware, new VirusCase("", spyware, 0, 0) );
		labMap.put( VirusInstance.Trojan, new VirusCase("", trojan, 0, 0) );
		labMap.put( VirusInstance.Worm, new VirusCase("", worm, 0, 0) );
	}
	
	private class VirusCase {
		String name, desc;
		int minCombo, threatLv;
		
		public VirusCase(String name, String desc, int minCombo, int threatLv) {
			this.name = name;
			this.desc = desc;
			this.minCombo = minCombo;
			this.threatLv = threatLv;
		}
		
		public void setShowCase() {
			Debug.log( " setShowCase() " );
			setDescriptionTab( desc );
			setInformationTab(name, minCombo, threatLv);
		}
	}
	
	@Override
	public void onCloseApplication() {
		// TODO Auto-generated method stub
		
	}
}
