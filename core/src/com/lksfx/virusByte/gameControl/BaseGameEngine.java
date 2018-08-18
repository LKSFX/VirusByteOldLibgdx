package com.lksfx.virusByte.gameControl;

import java.util.Comparator;
import java.util.Iterator;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.abstractTypes.GameObject;

public class BaseGameEngine {
	
	private Array<GameObject> gameObjects;
	private Comparator<GameObject> depthComparator;
	private Vector3 mousePos;
	
	private InputAdapter normalInputs;
	private GestureDetector gestureInputs;
	/**Determine if the inputs will trigger on this stage*/
	private boolean isStageInputsActive = true;
	
	public BaseGameEngine() {
		gameObjects = new Array<GameObject>();
		mousePos = new Vector3();
		depthComparator = new Comparator<GameObject>() {
			@Override
			public int compare(GameObject o1, GameObject o2) {
				return (o1.getDepth() - o2.getDepth());
			}
		};
		/*>>>>>>>>>>>>>>>>>>>>>>Set inputs controllers>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
		gestureInputs = new GestureDetector(new GestureAdapter() {
			/*@Override
			public boolean fling(float velocityX, float velocityY, int button) {
				if ( !isStageInputsActive ) return false; 
				return flingTrigger( velocityX, velocityY, button );
			}*/
			
			@Override
			public boolean tap(float x, float y, int count, int button) {
				if ( !isStageInputsActive ) 
					return false;
				if ( button == Buttons.LEFT ) {
					if ( doubleTapTrigger(x, y) ) return true;
					return tapTrigger(x, y);
				}
				return false;
			}
			
		});
		
		normalInputs = new InputAdapter() {
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				releaseTrigger( screenX, screenY );
				return false;
			}
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				if ( !isStageInputsActive ) 
					return false;
				draggedTrigger( screenX, screenY );
				return false;
			}
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				if ( !isStageInputsActive ) 
					return false;
				if ( button == Buttons.LEFT )
					pressedTrigger( screenX, screenY );
				return false;
			}
		};
		//gestureDetector.setTapCountInterval(0.8f); //set tap count interval
		gestureInputs.setTapSquareSize( 100f );
		
		VirusByteGame.addProcessor( normalInputs );
		VirusByteGame.addProcessor( gestureInputs );
		/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/
	}
	
	public void clearStage() {
		int arraySize = gameObjects.size;
		int total = 0;
		
		for ( int i = 0; i < arraySize; i++ ) {
			GameObject obj = gameObjects.get( i );
			
			obj.remove();
			total++;
		}
		gameObjects.clear();
		
		Debug.log( "Total of " + total + " game objects removed from stage on exit." );
	}
	
	public void draw( SpriteBatch batch, float deltaTime ) {
		int arraySize = gameObjects.size;
		
		for ( int i = 0; i < arraySize; i++ ) {
			GameObject obj = gameObjects.get( i );
			
			if ( obj.getDepth() >= 50 )
				continue;
			
			if ( obj.isVisible ) 
				obj.draw( batch, deltaTime );
		}
		
	}
	
	public void drawAbove( SpriteBatch batch, float deltaTime ) {
		
		boolean logDrawOrder = false;
		for (int i = 0; i < gameObjects.size; i++) {
			GameObject obj = gameObjects.get(i);
			
			if ( obj.getDepth() < 50 )
				continue;
			
			//draw > render
			if ( obj.isVisible )
				obj.draw(batch, deltaTime);
			
			if ( logDrawOrder ) 
				Debug.log( "Drawing >> " + obj.getName() );
		}
		
	}
	
	public void fixedUpdate( float deltaTime ) {
		gameObjects.sort( depthComparator );
		int arraySize = gameObjects.size;
		
		for ( int i = 0; i < arraySize; i++ ) {
			GameObject obj = gameObjects.get( i );
			obj.mainFixedUpdate( deltaTime );
		}
		
	}
	
	public void lateUpdate( float deltaTime ) {
		Iterator<GameObject> gameObjectIterator = gameObjects.iterator();
		
		while ( gameObjectIterator.hasNext() ) {
			GameObject obj = gameObjectIterator.next();
			obj.mainLateUpdate( deltaTime );
			if ( obj.isToRemoveFromStage() ) {
				gameObjectIterator.remove();
				obj.mainGameObjectReset();
			}
		}
	}
	
	public void insertGameObject( GameObject object ) {
		gameObjects.add( object );
	}
	
	public void removeGameObject( GameObject object ) {
		gameObjects.removeValue( object, true );
	}
	
	/*====================Triggers======================*/
	private boolean tapTrigger(float x, float y) {
		mousePos.set( x, y, 0 );
		VirusByteGame.VIEWPORT.unproject( mousePos );
		//Debug.log("screenX: " + x + " screenY: " + y + " worldX: " + mousePos.x + " worldY: " + mousePos.y);
		
		for ( int i = gameObjects.size-1; i >= 0; i-- ) {
			if ( !gameObjects.get( i ).isInputsActive() )
				continue;
			Array< InputTriggers > controllerList = gameObjects.get( i ).getInputControllerList();
			int controllerListSize = controllerList.size;
			for ( int j = 0; j < controllerListSize; j++ )
				if ( controllerList.get( j ).justTouched( mousePos ) )
					return true;
		}
		
		return false;
	}
	
	/*private boolean flingTrigger(float velocityX, float velocityY, int button) {
		
		for ( int i = gameObjects.size-1; i >= 0; i-- ) {
			if ( gameObjects.get( i ).flingTouch( velocityX, velocityY, button ) )
				return true;
		}
		
		return false;
	}*/
	
	private boolean draggedTrigger(float x, float y) {
		mousePos.set( x, y, 0 );
		VirusByteGame.VIEWPORT.unproject( mousePos );
		
		for (int i = gameObjects.size-1; i >= 0; i--) {
			if ( !gameObjects.get( i ).isInputsActive() )
				continue;
			Array< InputTriggers > controllerList = gameObjects.get( i ).getInputControllerList();
			int controllerListSize = controllerList.size;
			for ( int j = 0; j < controllerListSize; j++ )
				controllerList.get( j ).draggedTouch( mousePos );
		}
		
		return false;
	}
	
	public boolean pressedTrigger(float x, float y) {
		mousePos.set( x, y, 0 );
		VirusByteGame.VIEWPORT.unproject( mousePos );
		
		for ( int i = gameObjects.size-1; i >= 0; i-- ) {
			if ( !gameObjects.get( i ).isInputsActive() )
				continue;
			Array< InputTriggers > controllerList = gameObjects.get( i ).getInputControllerList();
			int controllerlistSize = controllerList.size;
			for ( int j = 0; j < controllerlistSize; j++ )
				controllerList.get( j ).pressedTouch( mousePos );
		}
		
		return false;
	}
	
	public void releaseTrigger(float x, float y) {
		mousePos.set( x, y, 0 );
		VirusByteGame.VIEWPORT.unproject( mousePos );
		
		for (int i = gameObjects.size-1; i >= 0; i--) {
			if ( !gameObjects.get( i ).isInputsActive() )
				continue;
			Array< InputTriggers > controllerList = gameObjects.get( i ).getInputControllerList();
			int controllerListSize = controllerList.size;
			for ( int j = 0; j < controllerListSize; j++ )
				controllerList.get( j ).releaseTouch( mousePos );
		}
		
	}
	
	private boolean doubleTapTrigger(float x, float y) {
		mousePos.set( x, y, 0 );
		VirusByteGame.VIEWPORT.unproject( mousePos );
		
		for ( int i = gameObjects.size-1; i >= 0; i-- ) {
			if ( !gameObjects.get( i ).isInputsActive() )
				continue;
			Array< InputTriggers > controllerList = gameObjects.get( i ).getInputControllerList();
			int controllerListSize = controllerList.size;
			for ( int j = 0; j < controllerListSize; j++ )
				if ( controllerList.get( j ).doubleTouch( mousePos ) ) 
					return true;
		}
		
		return false;
	}
	/*=====================================================*/
	
}
