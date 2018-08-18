package com.lksfx.virusByte.gameControl.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.HoldableType;

public class Inventory {
	
	private Array<Slot> slotArray;
	private Table moneyTable;
	private int cash;
	private int walletSize = 1000;
	
	public Inventory(GameHud hud) {
		Stage hud_stage = hud.getStage();
		moneyTable = new Table( hud.skin );
//		moneyTable.debug();
		moneyTable.setSize( hud_stage.getWidth() * .18f, hud_stage.getHeight() * .05f );
		moneyTable.setPosition( hud_stage.getWidth() - moneyTable.getWidth(), 0 );
		
		// Create the money display on the right bottom of the screen
		
		TextureRegionDrawable coinDrawable = new TextureRegionDrawable( VirusByteGame.ASSETS.getAssetManager().get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion( "coin-icon" ) );
		Image coinIcon = new Image( coinDrawable );
		final Label lb = new Label("", hud.skin);
		lb.addAction( Actions.forever( Actions.run( new Runnable() {
			int val = -1;
			@Override
			public void run() {
				if ( val != cash ) {
					val = cash;
					String strVal = String.valueOf(val), newStrVal = "";
					int fill = 4 - strVal.length();
					if ( fill > 0 ) {
						for (int i = 0; i < fill; i++) {
							newStrVal = newStrVal.concat( "0" );
						}
					}
					newStrVal = newStrVal.concat( strVal );
					lb.setText( newStrVal + "$" );
				}
			}
		} ) ) );
		float iconWidth = moneyTable.getWidth() * .21f;
		moneyTable.add( coinIcon ).size(iconWidth, iconWidth);
		moneyTable.add( lb );
		Debug.log( "money labe width: "  + " | height: "  );
		hud_stage.addActor( moneyTable );
		
		slotArray = new Array<Slot>();
		for (int i = 0; i < 3; i++) {
			Slot slot = new Slot( hud.skin, i );
			slotArray.add( slot );
			hud_stage.addActor( slot.stack );
		}
		
	}
	
	/**Check if the item released is over some inventory slot
	 * @return true if has put on slot successfully*/
	public boolean releaseOnInvetoryCheck(HoldableType item) {
		boolean success = false;
		Slot emptySlot = null;
		int totalSlots = slotArray.size;
		for (int i = 0; i < totalSlots; i++) {
			Slot slot = slotArray.get(i);
			if ( !slot.hasItem() ) emptySlot = slot;
			if ( slot.isOver(item.x, item.y) ) { //insert item on slot
				Debug.log( "Item inserted on slot!" );
				if ( slot.putItem(item) ) {
					success = true;
				} else { //when the slot is already occupied try to put on another free slot
					success = addItem(i, item, true);
				}
				return success;
			}
		}
		if ( emptySlot != null ) 
			emptySlot.hide();
		return success;
	}
	
	/**Insert the object on slot number
	 * @param forceInsertion when the selected slot already has another item try insert on another free slot
	 * @return true if the object is successfully inserted on slot*/
	public boolean addItem(int slotIndex, HoldableType obj, boolean forceInsertion) {
		int totalSlots = slotArray.size ;
		if ( obj == null || slotIndex >= totalSlots ) return false;
		boolean success = false;
		if ( !(success = slotArray.get(slotIndex).putItem(obj)) && forceInsertion ) {
			for (int j = 0; j < totalSlots; j++) {
				Slot nextSlot = slotArray.get(j);
				if ( nextSlot.hasItem() && nextSlot.itemArray.get(0).equals(obj) ) {
					nextSlot.putItem( obj );
					success = true;
					break;
				}
			}
			if ( !success ) {
				for (int j = 0; j < totalSlots; j++) {
					Slot nextSlot = slotArray.get(j);
					if ( !nextSlot.hasItem() ) {
						nextSlot.putItem( obj );
						success = true;
						break;
					}
				}
			}
		}
		return success;
	}
	
	/**Add cash to the wallet, respecting wallet limit*/
	public void addCash(int value) {
		cash += value;
		if ( cash > walletSize ) cash = walletSize;
	}
	
	/**Remove item from inventory slot
	 * @param instance virus_id of the item object
	 * @param n total to remove
	 * @return total items removed from inventory*/
	public int removeItems(VirusInstance instance, int n) {
		int totalRemoved = 0;
		for (int i = 0; i < slotArray.size; i++) {
			Slot slot = slotArray.get(i);
			if ( slot.hasItem() ) {
				if ( slot.itemArray.peek().virus_id == instance ) {
					while ( slot.hasItem() && n > 0 ) {
						--n;
						totalRemoved += slot.removeItems(1);
					}
					if ( n == 0 ) break;
				}
			}
		}
		return totalRemoved;
	}
	
	/**Remove all items from inventory slots*/
	public void clearInventory() {
		for (int i = 0; i < slotArray.size; i++) {
			Slot slot = slotArray.get(i);
			while ( slot.hasItem() ) {
				slot.removeItems(1);
			}
		}
	}
	
	public void hideAllSlots() {
		for (int i = 0; i < slotArray.size; i++) slotArray.get(i).hide();
	}
	
	/**Hide slot number*/
	public void hideSlot(int n) {
		if ( n >= slotArray.size || n < 0 ) return;
		slotArray.get(n).hide();
	}
	
	/**Hide slot with items with same instance stored
	 *  @param instance if some slot have item of same instance stored hide it*/
	public void hideSlot(VirusInstance instance) {
		for (int i = 0; i < slotArray.size; i++) {
			Slot slot = slotArray.get(i);
			if ( slot.hasItem() && slot.itemArray.peek().virus_id == instance ) {
				slot.hide();
				continue;
			}
		}
	}
	
	public void showAllSlots() {
		for (int i = 0; i < slotArray.size; i++) slotArray.get(i).show();
	}
	
	/**Show slot number*/
	public void showSlot(int n) {
		if ( n >= slotArray.size || n < 0 ) return;
		slotArray.get(n).show();
	}
	
	/**Show slot with items with same instance stored
	 * @param instance if some slot have item of same instance stored show it*/
	public void showSlot(VirusInstance instance) {
		for (int i = 0; i < slotArray.size; i++) {
			Slot slot = slotArray.get(i);
			if ( slot.hasItem() && slot.itemArray.peek().virus_id == instance ) {
				slot.show();
				continue;
			}
		}
	}
	
	/**@return true if inventory contains the current item instance*/
	public boolean contains(VirusInstance instance) {
		return (getSlotWith(instance) != null);
	}
	
	/**@return the slot that contains the instance or null*/
	public Slot getSlotWith(VirusInstance instance) {
		Slot result = null;
		for (int i = 0; i < slotArray.size; i++) {
			Slot slot = slotArray.get(i);
			if ( slot.hasItem() && slot.itemArray.peek().virus_id == instance ) {
				result = slot;
				break;
			}
		}
		return result;
	}
	
	/**@return total cash available to use in shop*/
	public int getCash() {
		return cash;
	}
	
	/**set cash value, respecting the wallet limit*/
	public void setCash(int value) {
		cash = value;
		if ( cash > walletSize ) cash = walletSize;
	}
	
	/**Update when screen change size*/
	public void resizeScreenUpdate() {
		Stage stage = VirusByteGame.HUD.getStage();
		resizeScreenUpdate( stage );
		moneyTable.setPosition(stage.getWidth() - moneyTable.getWidth(), 0);
	}
	
	public void resizeScreenUpdate(Stage stage) {
		/*float stageWidth = stage.getWidth();
		float stageHeight = stage.getHeight();
		float slotSpacing = 40f;
		for (int i = 0; i < slotArray.size; i++) {
			Slot slot = slotArray.get(i);
			Stack stack = slot.stack;
			stack.setVisible( false );
			float slotWidth = slot.slotImage.getWidth();
			float slotHeight = slot.slotImage.getHeight();
			float yy = (stageHeight * .5f) - (slotSpacing + slotHeight);
			stack.setSize(slotWidth, slotHeight);
			yy += i * (slotHeight + slotSpacing);
			stack.setPosition(stageWidth - (slotWidth * .5f + 5f), yy, Align.center);
			slot.updateScreenSize();
			slot.hide();
		}*/
		for (int i = 0; i < slotArray.size; i++) {
			Slot slot = slotArray.get(i);
			slot.instantHide();
			if ( slot.hasItem() ) 
				slot.show();
		}
	}
	
	/**Save inventory*/
	public void save() {
		FileHandle file = Gdx.files.local("data/inventory.vb");
		InventoryStatus status = new InventoryStatus( this );
		Json json = new Json();
		String str = json.toJson( status );
		Debug.log( str );
		str = json.prettyPrint( str );
		file.writeString(str, false);
	}
	
	/**Load to previous inventory status*/
	public void load(InventoryStatus status) {
		setCash( status.money );
		clearInventory();
		for ( int i = 0; i < slotArray.size; i++ ) {
			if ( i >= status.items.size ) break;
			Slot slot = slotArray.get(i);
			SlotStatus slotStatus = status.items.get(i);
			for ( int j = 0; j < slotStatus.totalItems; j++ ) {
				HoldableType item = (HoldableType)VirusByteGame.VIRUS_MANAGER.obtainVirus(slotStatus.itemInstance, false);
				slot.putItem( item );
			}
		}
	}
	
	/**Just for test save inventory system*/
	public void debugSavedInventory() {
		Debug.log( "/////// inventory loaded file ///////" );
		FileHandle file = Gdx.files.local("data/inventory.vb");
		if ( file.exists() ) {
			String str = file.readString();
			JsonValue root = new JsonReader().parse( str );
			Debug.log( root.toString() );
			/*Json json = new Json();
			Person person = json.fromJson(Person.class, file);
			Debug.log( person.toString() );*/
		}
		Debug.log( "/////// ///////////////////// ///////" );
	}
	
	public static class InventoryStatus {
		public int money;
		public Array<SlotStatus> items;
		
		public InventoryStatus() {
			
		}
		public InventoryStatus(Inventory inventory) {
			
			items = new Array<SlotStatus>();
			money = inventory.cash;
			Array<Slot> array = inventory.slotArray;
			for ( int i = 0; i < array.size; i++ ) {
				Slot slot = array.get(i);
				if ( slot.hasItem() ) {
					VirusInstance instance = slot.itemArray.peek().virus_id;
					int totalItems = slot.itemArray.size;
					items.add( new SlotStatus(instance, totalItems) );
				}
			}
			
		}
	}
	public static class SlotStatus {
		public VirusInstance itemInstance;
		public int totalItems;
		public SlotStatus() {}
		public SlotStatus(VirusInstance instance, int total) {
			itemInstance = instance;
			totalItems = total;
		}
	}
}
