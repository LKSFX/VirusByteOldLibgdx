package com.lksfx.virusByte.menus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.hud.Inventory;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.GameItem;
import com.lksfx.virusByte.gameObject.abstractTypes.HoldableType;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.screens.GameStageScreen;

public class ShopMenu extends Menu {
	
	private Table table;
	private boolean isStockLimited = true;
	private int totalItemsOnStock = 3;
	private Inventory inventory;
	private Stack[] items;
	private EvaluableObject[] evaluableList;
	
	public ShopMenu(Stage stage, Skin skin) {
		super(stage, skin);
		evaluableList = new EvaluableObject[3];
		evaluableList[0] = new EvaluableObject(VirusInstance.Antivirus, 30);
		evaluableList[1] = new EvaluableObject(VirusInstance.Bomb, 10);
		evaluableList[2] = new EvaluableObject(VirusInstance.Saw, 15);
	}

	@Override
	public void constructLayout() {
		table = new Table(skin);
		table.setFillParent( true );
//		table.debug();
		addTable(table);
	}
	
	@Override
	public void open() {
		super.open();
		inventory = VirusByteGame.HUD.inventory;
		Stack stack = new Stack();
		this.table.clear();
//		this.table.debug();
		
		float stageWidth = stage.getWidth();
		float stageHeight = stage.getHeight();
		
//		this.table.setSize(stageWidth, stageHeight);
		this.table.row();
		Table centerTable = new Table(skin), borderTable = new Table(skin);
		Label title = new Label("SHOP", skin, "visitor20");
		borderTable.align( Align.top ).row().spaceBottom(20f);
		borderTable.row();
		borderTable.add( title ).expandX();
		items = new Stack[3];
		Array<VirusInstance> itemList = VirusByteGame.VIRUS_MANAGER.itemList; 
		for (int i = 0; i < itemList.size; i++) {
			items[i] = getItemShowTable(itemList.get(i), centerTable);
			items[i].setName( "father-stack" );
//			centerTable.row();
		}
		MyButton closeButton = new MyButton("close", skin, "medium", stageWidth * .30f, stageHeight * + .06f, false, null);
		closeButton.addListener( new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				((GameStageScreen)VirusByteGame.GAME.getScreen()).unpauseGame();
			}
		} );
		centerTable.add( closeButton );
		Container<Table> container = new Container<Table>();	
		stack.add( centerTable );
		stack.add( container );
		container.setActor( borderTable );
		container.prefWidth(stageHeight).getActor();
		container.align( Align.top );
		this.table.add( stack ).size(stageWidth, stageHeight);
		checkForMoneyAvailability();
//		inventory.hideAllSlots();
	}
	
	private Stack getItemShowTable(VirusInstance instance, Table table) {
		final float stageWidth = stage.getWidth();
		final float stageHeight = stage.getHeight();
		final float frameWidth = stageWidth * .62f;
		final float frameHeight = stageHeight * .15f;
		Stack mainStack = new Stack();
		
		VirusType obj = VirusByteGame.VIRUS_MANAGER.obtainVirus(instance, false);
		
		Table frameTable = new Table(skin);
		frameTable.align( Align.right );
		frameTable.setBackground("shop-frame");
		Stack rightSideStack = assemblyRightSideBorder(obj, frameWidth, frameHeight);
		rightSideStack.setName( "rightStack" );
		frameTable.add( rightSideStack ).prefSize( ((Table)rightSideStack.findActor("table2")).getPrefWidth(), frameWidth);
		
		if ( obj instanceof HoldableType ) {
			
		}
		
		final Table backTable = new Table(skin);
		Debug.log( "Stage width: " + stageWidth );
		backTable.align( Align.top );
//		backTable.debug();
		backTable.add( frameTable ).size(frameWidth, frameHeight);
		mainStack.add( backTable );
		
		EvaluableObject evaluableObject = getEvaluable( instance );
		if ( evaluableObject != null ) {
			Table leftFrontTable = assemblyLeftSideBorder(evaluableObject, frameWidth, frameHeight);
			((Label)rightSideStack.findActor("price")).setText( evaluableObject.price + "" );
			mainStack.add( leftFrontTable );
		}
		table.add( mainStack ).prefSize(frameWidth, frameHeight * 1.25f).spaceBottom( 10f );
		table.row();
		return mainStack;
	}
	
	// >>>>>>>>>>>>>>>>>>>>>>>>
	//  ITEM STAND VIEWER TABLE ASSEMBLING //
	
	/**Mount the left side of the item shop frame that contains the name and the buy button*/
	private Table assemblyLeftSideBorder(final EvaluableObject evaluableObject, float frameWidth, float frameHeight) {
		Table leftFrontTable = new Table(skin);
		leftFrontTable.align( Align.left );
//		leftFrontTable.debug();
		
		// Buy button, press to buy respective item on this frame
		ImageTextButton buyButton = new ImageTextButton("buy", skin, "shop-buy-button");
		buyButton.setName( "buy-button" );
		buyButton.addListener( new ChangeListener() {
			int stock = totalItemsOnStock;
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int currentCash = inventory.getCash();
				boolean bool = (actor.getActions().size == 0);
				
				if (bool && currentCash >= evaluableObject.price && buy(evaluableObject) ) {
					stock--;
					currentCash -= evaluableObject.price;
					inventory.setCash( currentCash );
					inventory.getSlotWith( evaluableObject.instance ).highlight();
					checkForMoneyAvailability();
					
					if ( isStockLimited && stock == 0 ) {
						ImageTextButton thisButton = (ImageTextButton)actor;
						
						Actor parent = null;
						Actor iteratorActor = thisButton;
						while ( (parent = iteratorActor.getParent()) != null ) {
							if ( parent.getName() != null && parent.getName().equals("father-stack") ) break;
							iteratorActor = parent;
						}
						
						if ( parent != null && parent.getName().equals("father-stack") ) {
							Stack frame = (Stack)parent;
							Stack stack = (Stack)frame.findActor("rightStack");
							Label priceLabel = (Label)stack.findActor("price");
							priceLabel.setText( "sold" );
							priceLabel.setColor( new Color( Color.RED ) );
							((Image)stack.findActor("coin-icon")).remove();
							Image colorLabel = (Image)stack.findActor("colorLabel");
							colorLabel.setDrawable( skin.getDrawable("shop-white-label") );
						}
						
//						thisButton.setText("Sold\nOut");
						thisButton.setDisabled( true );
						thisButton.getLabel().setColor( new Color( Color.GRAY ) );
					}
					
					float xx = actor.getX(), yy = actor.getOriginY();
					actor.addAction( 
							Actions.sequence( Actions.moveTo(xx, yy - 5, .05f), 
							Actions.moveTo(xx, yy, .1f) )
						);
				}
				
			}
		} );
		
		Label itemNameLabel = new Label(evaluableObject.instance.toString(), skin, "visitor20");
		leftFrontTable.add().width(frameWidth * .5f);
		leftFrontTable.row();
		leftFrontTable.add( itemNameLabel ).expandY();
		leftFrontTable.row();
		leftFrontTable.add( buyButton ).size(frameWidth * .35f, frameHeight * .85f).bottom();
		return leftFrontTable;
	}
	
	/**Mount the right side of the item shop frame, this side contains the item icon, shadow, color label
	 * and the price*/
	private Stack assemblyRightSideBorder(VirusType itemObj, float frameWidth, float frameHeight) {
		Stack rightSideStack = new Stack();
		Table rightTable1 = new Table(skin);
		rightTable1.setName( "table1" );
		Table rightTable2 = new Table(skin);
//		rightTable1.debug();
//		rightTable2.debug();
		rightTable2.setName( "table2" );
		TextureRegion region = VirusByteGame.ASSETS.getAssetManager()
				.get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion( "coin-icon" ); // coin icon texture region
		TextureRegionDrawable drawable = new TextureRegionDrawable( region ); // make coin icon drawable with texture region
		Image coinIcon = new Image( drawable );
		coinIcon.setName( "coin-icon" );
		Label priceLabel = new Label(000 + "$", skin, "visitor20");
		Table priceTable = new Table();
		priceTable.add( coinIcon );
		priceTable.add( priceLabel ).space(5f);
		priceLabel.setName( "price" );
		rightTable1.align( Align.top );
		rightTable2.align( Align.bottomRight );
//		rightTable1.debug();
		
		if ( itemObj instanceof GameItem ) {
			Stack iconStack= new Stack(); // this stack will contains the container and the icon shadow
			drawable = new TextureRegionDrawable( itemObj.anim_default.getKeyFrames()[itemObj.getAnimation().getKeyFrames().length-1] ); // item image icon
			Image image = new Image( drawable ); 
			Image iconShadow = new Image(skin, "shop-item-shadow"); // Item circle shadow
			Table container = new Table(); // container that accommodate item icon and shadow icon
			container.add( image );
			container.setSize(70f, 70f);
			
			Table sw = new Table(); //this is an exclusive Table just for the shadow icon
			sw.align( Align.bottom );
			sw.add( iconShadow ).padBottom(3f); 
			
			iconStack.add( sw );
			iconStack.add( container );
			rightTable1.add( iconStack ).size(70f, frameHeight * .65f);
		}
		
		Stack boardLbStack = new Stack();
		Image colorLabel = new Image(skin, "shop-blue-label");
		colorLabel.setName( "colorLabel" );
		float labelWidth = (frameWidth - 224f) / 224f;
		float labelHeight = (frameHeight - 94f) / 94f;
		Debug.log( "current frame width> " + frameWidth + " | label width: " + labelWidth );
		Debug.log( "current frame height> " + frameHeight + " | label height: " + labelHeight );
		boardLbStack.addActor( colorLabel );
		boardLbStack.add( priceTable );
		rightTable2.add( boardLbStack ).size(colorLabel.getWidth() * (1 + labelWidth), colorLabel.getHeight() * (1 + labelHeight));
		
		rightSideStack.add( rightTable2 );
		rightSideStack.add( rightTable1 );
		return rightSideStack;
	}
	
	// <<<<<<<<<<<<<<<<<<<<<<<
	
	private void checkForMoneyAvailability() {
		int currentCash = inventory.getCash();
		Stack lastStack = null;
		
		for (int i = 0; i < items.length; i++) {
			Stack frame = items[i];
			Stack stack = (Stack)frame.findActor("rightStack");
			Label priceLabel = (Label)stack.findActor("price");
			Pattern pattern = Pattern.compile( "\\d+" );
			Matcher match = pattern.matcher(priceLabel.getText().toString());
			
			if ( match.find() ) {
				if ( Integer.valueOf( match.group(0) ) > currentCash ) {
					Image colorLabel = (Image)stack.findActor("colorLabel");
					colorLabel.setDrawable( skin.getDrawable("shop-red-label") );
					((ImageTextButton)frame.findActor("buy-button")).getLabel().setColor( new Color( Color.GRAY ) );
					Debug.log( "Item frame " + i + " isn't available to buy with the current money" );
				} else {
					Debug.log( "Item frame " + i + " is available to buy with the current money" );
				} 
			}
			if ( stack == lastStack ) Debug.log( "Item frame " + i + " stack is the same of the last frame stack" );
			lastStack = stack;
		}
	}
	
	/**Return the equivalent evaluable object to get price and other informations*/
	private EvaluableObject getEvaluable(VirusInstance instance) {
		EvaluableObject result = null;
		for (int i = 0; i < evaluableList.length; i++) {
			EvaluableObject obj = evaluableList[i];
			if ( obj.instance == instance ) {
				result = obj;
				break;
			}
		}
		return result;
	}
	
	/**Try buy product 
	 * @return <b>true</b> if successfully buy item, else <b>false</b> if not have empty or available slot for that item*/
	public boolean buy(EvaluableObject eval) {
		boolean result = false;
		HoldableType itemObj = (HoldableType)VirusByteGame.VIRUS_MANAGER.obtainVirus(eval.instance, false);
		result = inventory.addItem(0, itemObj, true);
		return result;
	}
	
	public class EvaluableObject {
		public int price; //define item price on shop
		public VirusInstance instance;
		
		public EvaluableObject(VirusInstance instance, int price) {
			this.price = price;
			this.instance = instance;
		}
	}
	
}
