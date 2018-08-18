package com.lksfx.virusByte.gameControl.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.hud.GameHud.Status;
import com.lksfx.virusByte.gameObject.abstractTypes.GameItem;
import com.lksfx.virusByte.gameObject.abstractTypes.HoldableType;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class Slot {
	
	public Array<HoldableType> itemArray = null;
	public ImageButton slotImage;
	private Container<Image> frameIcon;
	private Container<Label> frameLabel;
	private Vector2 onScreenPosition;
	private Vector2 localPos, centerSlot;
	private Label totalItemLabel;
	public Stack stack;
	//private ClickListener clickListener;
	private boolean over, occupied;
	private boolean isOnScreen;
	private boolean toShow, toHide;
	private int id_number;
	
	private RunnableAction onHideComplete, onShowComplete;
	private RepeatAction onTouchAction;
	
	public Slot(Skin skin, int slotNumber) {
		id_number = slotNumber;
		itemArray = new Array<HoldableType>();
		slotImage = new ImageButton(skin, "slot");
		frameIcon = new Container<Image>();
		totalItemLabel = new Label( "1", skin, "visitor20" );
		frameLabel = new Container<Label>();
		frameLabel.align( Align.bottomLeft ).padLeft(8f).padBottom(3f);
		frameLabel.setActor( totalItemLabel );
		stack = new Stack();
		stack.add( slotImage );
		
		onScreenPosition = new Vector2();
		localPos = new Vector2();
		centerSlot = new Vector2();
		
		isOnScreen = true; 
		onTouchAction = new RepeatAction();
		RunnableAction runnable = new RunnableAction();
		runnable.setRunnable( new Runnable() {
			@Override
			public void run() {
				if ( Gdx.input.justTouched() ) {
					
					Vector3 vec3 = VirusByteGame.GAME.obtainVector3();
					vec3.set( Gdx.input.getX(), Gdx.input.getY(), 0 );
					VirusByteGame.HUD_CAMERA.unproject( vec3 );
					
					if ( isOver( vec3.x, vec3.y ) ) {
						getItem();
						Debug.log( "From the slot runnable, over the slot!" );
					}
					
					VirusByteGame.GAME.freeVector( vec3 );
					
				}
			}
		});
		onTouchAction.setAction( runnable );
		onTouchAction.setCount( RepeatAction.FOREVER );
		stack.addAction( onTouchAction );
		onHideComplete = new RunnableAction(); 
		onHideComplete.setRunnable(new Runnable() {
			@Override
			public void run() {
//				Debug.log( "onHideComplete Action!" );
				if ( !stack.isVisible() ) {
					stack.setVisible( true );
					if ( hasItem() ) show();
				}
				toHide = false;
				if ( toShow ) show();
				isOnScreen = false;
 			}
		});
		onShowComplete = new RunnableAction();
		onShowComplete.setRunnable(new Runnable() {
			@Override
			public void run() {
				if ( !stack.getActions().contains(onTouchAction, true) ) stack.addAction(onTouchAction);
//				Debug.log( "onShowComplete Action!" );
				toShow = false;
				if ( toHide ) hide();
				isOnScreen = true;
			}
		});
		stack.setSize(slotImage.getWidth(), slotImage.getHeight());
		instantHide();
		
	}
	
	public boolean putItem(HoldableType obj) {
		boolean result = false;
		if ( !(obj instanceof GameItem) ) return result;
		GameItem item = (GameItem) obj;
		if ( !occupied ) { //disoccupied slot
			obj.onSlotEnter();
			itemArray.add( obj );
			frameIcon.setActor( new Image(item.getIcon()) );
			stack.add(frameIcon);
			stack.add( frameLabel );
			totalItemLabel.setText( ""+1 );
			occupied = true;
			/*Debug.log("item move: " + obj.isOnMove + " | item alive: " + obj.alive);
			Debug.log("item reached: " + obj.isReached + " | item finale: " + obj.isFinale);*/
			result = true;
		} else {
//			Debug.log(" already occupied slot item ");
			if ( itemArray.peek().equals( obj ) && itemArray.size < 99 ) { //if is the same object type, and has less than 99 items already on this slot
				itemArray.add( obj );
				obj.onSlotEnter();
				totalItemLabel.setText( ""+itemArray.size );
				result = true;
			}
		}
		if ( result ) { // item successfully added to slot
			obj.removeFromStage();
			show();
		}
		return result;
	}
	
	/** get and remove the item from the slot */
	public HoldableType getItem() {
		if ( itemArray.size == 0 ) return null;
		if ( VirusByteGame.HUD.state == Status.PAUSED ) return null;
		HoldableType out = itemArray.pop();
		if ( out != null ) {
			out.alive = true;
			centerSlot.set(slotImage.getWidth() * .5f, slotImage.getHeight() * .5f);
			stack.localToStageCoordinates( centerSlot );
			VirusByteGame.VIRUS_MANAGER.addVirus(centerSlot.x, centerSlot.y, out);
			out.onSlotOut();
			out.grab();
			Debug.log("item move: " + out.isOnMove + " | item alive: " + out.alive);
			Debug.log("item reached: " + out.isReached + " | item finale: " + out.isFinale);
		}
		int total = itemArray.size;
		totalItemLabel.setText( total + "" );
		if ( total == 0 ) cleanSlot(); //When haven't more items slot
		return out;
	}
	
	/** return if has an item stored */
	public boolean hasItem() {
		return occupied;
	}
	
	public boolean isOver() {
		return over;
	}
	
	/**@return true if the relative <b>x</b> and <b>y</b> is over this slot area*/
	public boolean isOver(float relativeX, float relativeY) {
		localPos.set(0, 0);
		stack.localToStageCoordinates(localPos); //convert stack coordinate to stage coordinate
		float xx = localPos.x, yy = localPos.y;
//			Debug.log("original position: " + localPos.toString() + " | converted xx: " + xx + " | yy: " + yy );
		if (relativeX > xx && relativeX < xx+stack.getWidth() ) {
			if (relativeY > yy && relativeY < yy+stack.getHeight() ) {
				return true;
			}
		} 
		return false;
	}
	
	public void instantShow() {
		float slotSpacing = slotImage.getHeight() + 40f;
		onScreenPosition.set(VirusType.WORLD_WIDTH - (slotImage.getWidth() + 5f), ((VirusType.WORLD_HEIGHT / 2) - slotSpacing) + (slotSpacing * id_number) );
		stack.setPosition(onScreenPosition.x, onScreenPosition.y);
		toShow = false;
		isOnScreen = true;
	}
	
	/**Show the slot on screen*/
	public void show() {
		if ( toShow || isOnScreen ) return;
		if ( toHide ) {
			toHide = false;
			stack.clearActions();;
		}
		toShow = true;
		stack.clearActions();
		float slotSpacing = slotImage.getHeight() + 40f;
		onScreenPosition.set(VirusType.WORLD_WIDTH - (slotImage.getWidth() + 5f), ((VirusType.WORLD_HEIGHT / 2) - slotSpacing) + (slotSpacing * id_number) );
		stack.addAction( Actions.sequence(Actions.moveTo(onScreenPosition.x, onScreenPosition.y, 1f), onShowComplete) );
//		Debug.log( "Show event triggered!" );
	}
	
	public void instantHide() {
		float slotSpacing = slotImage.getHeight() + 40f;
		onScreenPosition.set(VirusType.WORLD_WIDTH - (slotImage.getWidth() + 5f), ((VirusType.WORLD_HEIGHT / 2) - slotSpacing) + (slotSpacing * id_number) );
		stack.setPosition(onScreenPosition.x + 55f, onScreenPosition.y);
		toHide = false;
		isOnScreen = false;
	}
	
	/**Hide the slot from the screen*/
	public void hide() {
		if ( toHide || !isOnScreen ) return;
		if ( toShow ) {
			toShow = false;
			stack.clearActions();
		}
		toHide = true;
		stack.clearActions();
//		Debug.log( "position slot " + centerSlot.toString() );
		stack.removeAction( onTouchAction );
		float slotSpacing = slotImage.getHeight() + 40f;
		onScreenPosition.set(VirusType.WORLD_WIDTH - (slotImage.getWidth() + 5f), ((VirusType.WORLD_HEIGHT / 2) - slotSpacing) + (slotSpacing * id_number) );
		stack.addAction( Actions.sequence(Actions.moveTo(onScreenPosition.x + 55f , onScreenPosition.y, 1f), onHideComplete) );
	}
	
	/**Remove items from this slot
	 * @param n the number of items to remove
	 * @return total of removed items*/
	public int removeItems(int n) {
		int totalRemoved = 0;
		if ( !hasItem() ) return totalRemoved;
		
		for (int i = 0; i < n; i++) {
			totalRemoved += (itemArray.removeIndex(0) != null) ? 1 : 0;
			totalItemLabel.setText( itemArray.size + "" );
			if ( itemArray.size == 0 ) {
				cleanSlot();
				break;
			}
		}
		
		return totalRemoved;
	}
	
	private void cleanSlot() {
		occupied = false;
		stack.removeActor( frameIcon );
		stack.removeActor( frameLabel );
		frameIcon.remove();
		hide();
	}
	
	public void updateScreenSize() {
		isOnScreen = false;
	}
	
	/**Return if this slot is on the screen or already coming to*/
	public boolean isOnScreen() {
		return ( (isOnScreen | toShow) && ( !toHide ) );
	}
	
	/**Item icon highlight effect*/
	public void highlight() {
		Image image = frameIcon.getActor();
		if ( image != null ) {
			image.clearActions();
			image.setPosition(0, 0);
			image.setScale(1f);
			Debug.log( "Highlight on slot" );
			image.addAction( Actions.sequence( Actions.parallel(Actions.scaleTo(1.2f, 1.2f, .2f), Actions.moveBy(-05f, -.05f, .2f)), 
					Actions.parallel(Actions.scaleTo(1f, 1f, .2f), Actions.moveBy(05f, .05f, .2f)), Actions.moveTo(0, 0) ) );
		}
	}
}
