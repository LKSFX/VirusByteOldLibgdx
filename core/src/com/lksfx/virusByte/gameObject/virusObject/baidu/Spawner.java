package com.lksfx.virusByte.gameObject.virusObject.baidu;

import java.util.Iterator;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
/**
 * Spawner automatic put the virus on the stage, and create a transition effect
 */
public class Spawner {
	public Array<Spawnable> spawningList;
	private VirusManager virusManager;
	
	/**fade transitions type*/
	public enum Transition {FADE_IN, FADE_IN_STOPPED, FADE_IN_STOPPED_TO_MOVE, GROW_IN_MOVE, GROW_IN_STOPPED, GROW_IN_STOPPED_TO_MOVE, SHRINK_OUT};
	
	public SpawnableConfiguration configuration;
	
	/**ATENTION! this constructor can only before the virus manager has been created on the main gameScreen*/
	public Spawner() {
		this( VirusByteGame.VIRUS_MANAGER );
	}
	
	public Spawner(VirusManager manager) {
		virusManager = manager;
		spawningList = new Array<Spawner.Spawnable>();
		configuration = new SpawnableConfiguration();
	}
	
	public void addSpawnable(VirusInstance instance, SpawnableConfiguration config, float x, float y) {
		VirusType virus = virusManager.obtainVirus( instance, true );//instance.obtain();
		if ( virus == null ) return;
		addSpawnable(virus, config, x, y, null);
	}
	
	public void addSpawnable(VirusInstance instance, SpawnableConfiguration config, float x, float y, Vector2 relativePosition) {
		VirusType virus = virusManager.obtainVirus( instance, true );//instance.obtain();
		if ( virus == null ) return;
		addSpawnable(virus, config, x, y, relativePosition);
	}
	
	public void addSpawnable(VirusType obj, SpawnableConfiguration config, float x, float y) {
		addSpawnable(obj, config, x, y, null);
	}
	
	public void addSpawnable(VirusType obj, SpawnableConfiguration config, float x, float y, Vector2 relativePosition) {
		spawningList.add( new Spawnable(obj, config, x, y, relativePosition) );
	}
	
	public void update(float deltaTime) {
		Iterator<Spawnable> iterator = spawningList.iterator();
		while ( iterator.hasNext() ) {
			Spawnable obj = iterator.next();
			obj.update( deltaTime );
			if ( obj.hasEnded() ) {
				obj.finish();
				iterator.remove();
			}
		}
	}
	
	// ==== =============== ==== //
	// ==== Spawnable class ==== //
	// ==== =============== ==== //
	
	private class Spawnable {
		@SuppressWarnings("unused")
		public boolean isInUse, lastMoveState, isRelativePosition;
		VirusType gameObj;
		Transition transition;
		Vector2 relativeToThisPosition;
		public float x, y, value, incValue = .020f, metaToFinalValue = 1f, metaToMotionValue = .2f;
		
		/** 
		 * @param relativePosition when set, the xx and yy position of this spawnable object will be in relation to this vector2 
		 */
		public Spawnable(VirusType gameObj, SpawnableConfiguration config, float x, float y, Vector2 relativePosition) {
			initialize(gameObj, config, x, y, relativePosition);
		}
		
		private void initialize(VirusType gameObj, SpawnableConfiguration config, float x, float y, Vector2 relativePosition) {
			this.gameObj = gameObj;
			// through configuration spawner object //
			this.value = config.initialValue;
			this.incValue = config.incrementValueBy;
			this.metaToFinalValue = config.metaToFinalValue;
			this.metaToMotionValue = config.metaToMotionValue;
			this.transition = config.transition;
			config.toDefault(); //reset values to default on configuration object
			// =================================== //
			this.relativeToThisPosition = relativePosition;
			this.x = x;
			this.y = y;
			//Debug.log("transition: " + (config.transition == null ? "is null" : "is not null") );
			isInUse = true;
			lastMoveState = gameObj.isOnMove;
			
			switch ( transition ) {
			case FADE_IN:
				gameObj.setAlpha(value);
				break;
			case FADE_IN_STOPPED:
				gameObj.setAlpha(value);
				gameObj.isOnMove = false; //stop the object until the transition is completed
				break;
			case FADE_IN_STOPPED_TO_MOVE:
				gameObj.setAlpha(value);
				isRelativePosition = ( relativePosition != null ) ? true : false;
				gameObj.isOnMove = false; //stop the object until the transition is completed
				break;
			case GROW_IN_MOVE:
				gameObj.setScale(value);
				break;
			case GROW_IN_STOPPED:
				gameObj.setScale(value);
				isRelativePosition = ( relativePosition != null ) ? true : false;
				gameObj.isOnMove = false; //stop the object until the transition is completed
				break;
			case GROW_IN_STOPPED_TO_MOVE:
				gameObj.setScale(value);
				isRelativePosition = ( relativePosition != null ) ? true : false;
				gameObj.isOnMove = false; //stop the object until the transition is completed
				break;
			case SHRINK_OUT:
				isRelativePosition = false;
				gameObj.setScale( value ); 
				gameObj.isOnMove = true;
				break;
			default:
				break;
			}
			// if already not in the virus manager then add
			if ( !VirusByteGame.VIRUS_MANAGER.contains(gameObj, true) ) VirusByteGame.VIRUS_MANAGER.addVirus(x, y, gameObj);
		}
		
		public void update(float deltaTime) {
			switch ( transition ) {
			case FADE_IN:
				gameObj.setAlpha( value += incValue );
				break;
			case FADE_IN_STOPPED:
				if ( isRelativePosition ) gameObj.moveTo(relativeToThisPosition.x + x, relativeToThisPosition.y + y);
				gameObj.setAlpha(value += incValue);
				break;
			case FADE_IN_STOPPED_TO_MOVE:
				gameObj.setAlpha( value += incValue );
				if ( value < metaToMotionValue && isRelativePosition ) gameObj.moveTo(relativeToThisPosition.x + x, relativeToThisPosition.y + y);
				gameObj.isOnMove = (value > metaToMotionValue);
				break;
			case GROW_IN_MOVE:
				gameObj.setScale( value += incValue );
				gameObj.isOnMove = true;
				break;
			case GROW_IN_STOPPED:
				if ( isRelativePosition ) gameObj.moveTo(relativeToThisPosition.x + x, relativeToThisPosition.y + y);
				gameObj.setScale( value += incValue );
				break;
			case GROW_IN_STOPPED_TO_MOVE:
				gameObj.setScale( value += incValue );
				if ( value < metaToMotionValue && isRelativePosition ) gameObj.moveTo(relativeToThisPosition.x + x, relativeToThisPosition.y + y);
				gameObj.isOnMove = (value > metaToMotionValue);
				/*Debug.log("update Grow in stoped and move, value: " + value + " | incrementBy: " + incValue
						+ " | metaTOfinal: " + metaToFinalValue);*/
				break;
			case SHRINK_OUT:
				gameObj.setScale( value -= incValue );
				break;
			default:
				break;
			}
		}
		
		public boolean hasEnded() {
			if ( transition == Transition.SHRINK_OUT ) {
				return ( value <= metaToFinalValue );
			}
			return ( value >= metaToFinalValue );
		}
		
		public void finish() {
			isInUse = false;
			gameObj.isOnMove = lastMoveState;
		}
	}
	
	/** this class let you configure the virus spawning animation*/
	public class SpawnableConfiguration {
		public Transition transition;
		public float initialValue, incrementValueBy = .020f, metaToFinalValue = 1f, metaToMotionValue = .2f;
		
		private SpawnableConfiguration() {
			toDefault();
		}
		
		/**
		 *  set all configuration values
		 * @param initialValue initial value of the transition, depends of the transition type
		 * @param incrementValueBy the float quantity to increment every frame
		 * @param metaToFinalValue the final value meta to reach and end the Spawnable object
		 * @param metaToMotionValue when transition is GROW_IN_STOPED_TO_MOVE, is useful to set in what value quantity need reach start to start to move  
		 * @param transition the type of transition the Spawnable object will perform
		 */
		public void set(float initialValue, float incrementValueBy, float metaToFinalValue, float metaToMotionValue, Transition transition) {
			this.initialValue = initialValue;
			this.incrementValueBy = incrementValueBy;
			this.metaToFinalValue = metaToFinalValue;
			this.metaToMotionValue = metaToMotionValue;
			this.transition = transition;
		}
				
		/**set all the values inside this configuration object to default state*/
		public void toDefault() {
			transition = def_transition;
			initialValue = def_initialValue;
			incrementValueBy = def_incrementValueBy;
			metaToFinalValue = def_metaToFinalValue;
			metaToMotionValue = def_metaToMotionValue;
		}
		
		private final Transition def_transition = Transition.GROW_IN_STOPPED_TO_MOVE;
		private final float def_initialValue = 0f, def_incrementValueBy = .020f,
				def_metaToFinalValue = 1f, def_metaToMotionValue = .2f;
	}
	
	/** set and return the spawner configuration object to use
	  * @param initialValue initial value of the transition, depends of the transition type
	  * @param incrementValueBy the float quantity to increment every frame
	  * @param metaToFinalValue the final value meta to reach and end the Spawnable object
	  * @param metaToMotionValue when transition is GROW_IN_STOPED_TO_MOVE, is useful to set in what value quantity need reach start to start to move  
	  * @param transition the type of transition the Spawnable object will perform
	  * */
	public SpawnableConfiguration getSCS(float initialValue, float incrementValueBy, float metaToFinalValue, 
			float metaToMotionValue, Transition transition) {
		configuration.set(initialValue, incrementValueBy, metaToFinalValue, metaToMotionValue, transition);
		return this.configuration;
	}
	
	/** @param metaToFinalValue the final value meta to reach and end the Spawnable object
	 * @param metaToMotionValue when transition is GROW_IN_STOPED_TO_MOVE, is useful to set in what value quantity need reach start to start to move  
	 */
	public SpawnableConfiguration getSCS_basic1(float metaToFinalValue, float metaToMotionValue, Transition transition) {
		return getSCS( configuration.def_initialValue, configuration.def_incrementValueBy, 
				metaToFinalValue, metaToMotionValue, transition);
	}
	
	/**
	 * @param incrementValue value to increment every frame
	 * @param metaToMotionValue when transition is GROW_IN_STOPED_TO_MOVE, is useful to set in what value quantity need reach start to start to move  
	 */
	public SpawnableConfiguration getSCS_basic2(float incrementValue, float metaToFinalValue, float metaToMotionValue, Transition transition) {
		return getSCS( configuration.def_initialValue, incrementValue, metaToFinalValue, metaToMotionValue, transition);
	}
	
	/**@param incrementValue value to increment every frame
	 * @param metaToMotionValue when transition is GROW_IN_STOPED_TO_MOVE, is useful to set in what value quantity need reach start to start to move  
	 */
	public SpawnableConfiguration getSCS_basic3(float incrementValue, float metaToMotionValue, Transition transition) {
		return getSCS( configuration.def_initialValue, incrementValue, configuration.def_metaToFinalValue, metaToMotionValue, transition);
	}
}
