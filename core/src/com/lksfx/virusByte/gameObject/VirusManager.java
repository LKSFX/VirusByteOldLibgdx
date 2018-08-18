package com.lksfx.virusByte.gameObject;

import static com.lksfx.virusByte.gameControl.debug.Debug.debug;

import java.util.Comparator;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.ParticleGenerator;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.ObjectsManager;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameControl.swiper.SwipeHandler;
import com.lksfx.virusByte.gameControl.swiper.mesh.SwipeTriStrip;
import com.lksfx.virusByte.gameObject.RuntimeDependencyLoader.DependencyListener;
import com.lksfx.virusByte.gameObject.abstractTypes.DrawableObject;
import com.lksfx.virusByte.gameObject.abstractTypes.GameObject;
import com.lksfx.virusByte.gameObject.abstractTypes.HoldableType;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType.ACTION;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType.ActionListener;
import com.lksfx.virusByte.gameObject.itemObject.AntivirusItem;
import com.lksfx.virusByte.gameObject.itemObject.BombItem;
import com.lksfx.virusByte.gameObject.itemObject.Coin;
import com.lksfx.virusByte.gameObject.itemObject.LifeItem;
import com.lksfx.virusByte.gameObject.itemObject.SawItem;
import com.lksfx.virusByte.gameObject.pontuation.PointsManager;
import com.lksfx.virusByte.gameObject.virusObject.BotVirus;
import com.lksfx.virusByte.gameObject.virusObject.BugVirus;
import com.lksfx.virusByte.gameObject.virusObject.EnergyzerVirus;
import com.lksfx.virusByte.gameObject.virusObject.FlameBotVirus;
import com.lksfx.virusByte.gameObject.virusObject.FreezeBotVirus;
import com.lksfx.virusByte.gameObject.virusObject.InfectorVirus;
import com.lksfx.virusByte.gameObject.virusObject.NyxelVirus;
import com.lksfx.virusByte.gameObject.virusObject.ParasiteVirus;
import com.lksfx.virusByte.gameObject.virusObject.PlagueVirus;
import com.lksfx.virusByte.gameObject.virusObject.PsychoVirus;
import com.lksfx.virusByte.gameObject.virusObject.ShockBotVirus;
import com.lksfx.virusByte.gameObject.virusObject.SpywareVirus;
import com.lksfx.virusByte.gameObject.virusObject.TrojanVirus;
import com.lksfx.virusByte.gameObject.virusObject.WormVirus;
import com.lksfx.virusByte.gameObject.virusObject.baidu.BaiduBoss;
import com.lksfx.virusByte.gameObject.virusObject.baidu.Spawner;
import com.lksfx.virusByte.gameObject.virusObject.dragon.DragonBoss;
import com.lksfx.virusByte.gameObject.virusObject.octocat.OctocatBoss;
import com.lksfx.virusByte.gameObject.virusObject.pepperBros.PepperBrosBoss;

public class VirusManager extends GameObject implements ObjectsManager, Disposable {
//	private Vector3 mousePos = new Vector3();
	private Vector2 touchPos = new Vector2();
	public boolean DEBUG = false, SCREEN_DEBUG = false, DEBUG_SWIPE = false;
	/**Determine if the inputs will trigger the virus on stage*/
//	private boolean isStageInputsActive = true;
	
	public static ParticleGenerator PART_EFFECTS;
	public PointsManager pointsManager;
	public int totalVirusDestroyed = 0, stage = 1;
	
/*	public InputAdapter normalInputs;
	public GestureDetector gestureInputs;*/
	
	/**the spawner can make effects like grow-in, fade-in etc during the virus spawn*/
	public Spawner spawner;
	
	/**List item objects*/
	public Array<VirusInstance> itemList; 
	
	Array<VirusMaker> virusMakerList;
	public Array<Array<VirusType>> virus_set;
	public Array<VirusType> genericArray, allVirus;
	private Array<DrawableObject> drawArray;
	public Comparator<DrawableObject> depthComparator;
	
	SwipeHandler swipe;
	Texture tex;
	ShapeRenderer shapes;
	SwipeTriStrip tris;
	/**This camera is the original size of the screen, useful to render swipe effect on correct point on screen*/
	OrthographicCamera cam;
	private Color swipeColor;
	/**Determine if the swipe is show*/
	public boolean isDrawingSwipe;
	
	public VirusManager() {
		// ==== //
		isVisible = false;
		tris = new SwipeTriStrip();
		swipe = new SwipeHandler(15);
		swipe.minDistance = 10;
		swipe.initialDistance = 10;
		tex = VirusByteGame.ASSETS.getAssetManager().get( VirusByteGame.ASSETS.gradientTexture, Texture.class );
		tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		shapes = VirusType.shapeRender;
		cam = new OrthographicCamera();
		cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		swipeColor = new Color(Color.WHITE);
		// ==== //
		spawner = new Spawner(this); //spawner create transition effects
		genericArray = new Array<VirusType>(); //for objects that rarely spawn, this saves unnecessary pools and arrays in the main ENUMERATOR.
		allVirus = new Array<VirusType>();
		drawArray = new Array<DrawableObject>();
		itemList = new Array<VirusManager.VirusInstance>();
		itemList.addAll( VirusInstance.Antivirus, VirusInstance.Bomb, VirusInstance.Saw );
		depthComparator = new Comparator<DrawableObject>() {
			@Override
			public int compare(DrawableObject o1, DrawableObject o2) {
				return (o1.getDepth() - o2.getDepth());
			}
		};
		DependencyListener<TextureAtlas> baiduDependency = new DependencyListener<TextureAtlas>(Assets.Atlas.baiduAtlas.path, TextureAtlas.class);
		// ==== //
		final String imageFolder = "data/sprites/virus/dragon/"; // Image folder that contains dragon sprites
		String[] color = new String[] { "black", "blue", "forest", "gold", "red", "retro", "white" };
		DependencyListener<?>[] array = new DependencyListener<?>[color.length * 2 + 1];
		int i = 0;
		array[i++] = new DependencyListener<TextureAtlas>(Assets.Atlas.dragonAtlas.path,  TextureAtlas.class);
		for (int j = 0; j < color.length; j++) {
			array[i++] = new DependencyListener<Texture>(imageFolder+color[j] + "-scales1.png",  Texture.class);
			array[i++] = new DependencyListener<Texture>(imageFolder+color[j] + "-scales2.png",  Texture.class);
		}
		RuntimeDependencyLoader dragonDependency = new RuntimeDependencyLoader(true, array);
		// === //
		DependencyListener<TextureAtlas> pepperBrosDependency = new DependencyListener<TextureAtlas>(Assets.Atlas.pepperBrosAtlas.path,  TextureAtlas.class);
		DependencyListener<TextureAtlas> octocatDependency = new DependencyListener<TextureAtlas>(Assets.Atlas.octocatAtlas.path,  TextureAtlas.class);
		
		//virusMakerList
		virusMakerList = new Array<VirusManager.VirusMaker>();
		virusMakerList.add( new VirusMaker(VirusInstance.Antivirus, AntivirusItem.class, -1, true, false) );
		virusMakerList.add( new VirusMaker(VirusInstance.Baidu,  BaiduBoss.class, 1, false, false, new RuntimeDependencyLoader(true, baiduDependency)) );
		virusMakerList.add( new VirusMaker(VirusInstance.Bomb, BombItem.class, -2, true, true) );
		virusMakerList.add( new VirusMaker(VirusInstance.Bot, BotVirus.class, 6, true, true) );
		virusMakerList.add( new VirusMaker(VirusInstance.Bug, BugVirus.class, 1, false, false) ); //rarely spawn 
		virusMakerList.add( new VirusMaker(VirusInstance.Dragon,  DragonBoss.class, 1, false, false, dragonDependency) );
		virusMakerList.add( new VirusMaker(VirusInstance.Energyzer, EnergyzerVirus.class, 2, true, true) );
		virusMakerList.add( new VirusMaker(VirusInstance.Flamebot, FlameBotVirus.class, 3, true, true) );
		virusMakerList.add( new VirusMaker(VirusInstance.Freezebot, FreezeBotVirus.class, 3, true, true) );
		virusMakerList.add( new VirusMaker(VirusInstance.Infector, InfectorVirus.class, 3, true, true) );
		virusMakerList.add( new VirusMaker(VirusInstance.Life, LifeItem.class, 5, false, false) );
		virusMakerList.add( new VirusMaker(VirusInstance.Pepperbros,  PepperBrosBoss.class, 1, false, false, new RuntimeDependencyLoader(true, pepperBrosDependency)) );
		virusMakerList.add( new VirusMaker(VirusInstance.Nyxel, NyxelVirus.class, 1, true, false) );
		virusMakerList.add( new VirusMaker(VirusInstance.Octocat, OctocatBoss.class, 1, false, false, new RuntimeDependencyLoader(true, octocatDependency)) );
		virusMakerList.add( new VirusMaker(VirusInstance.Parasite, ParasiteVirus.class, 5, true, false) );
		virusMakerList.add( new VirusMaker(VirusInstance.Plague, PlagueVirus.class, 3, true, true) );
		virusMakerList.add( new VirusMaker(VirusInstance.Psycho, PsychoVirus.class, 3, true, true) );
		virusMakerList.add( new VirusMaker(VirusInstance.Saw, SawItem.class, -3, false, false) );
		virusMakerList.add( new VirusMaker(VirusInstance.Shockbot, ShockBotVirus.class, 15, true, false) );
		virusMakerList.add( new VirusMaker(VirusInstance.Spyware, SpywareVirus.class, 1, true, true) );
		virusMakerList.add( new VirusMaker(VirusInstance.Trojan, TrojanVirus.class, 1, true, false) );
		virusMakerList.add( new VirusMaker(VirusInstance.Worm, WormVirus.class, 2, true, true) );
		
		virus_set = new  Array< Array<VirusType> >() {{
			
			for (int i = 0; i < virusMakerList.size; i++) {
				if ( virusMakerList.get(i).hasArray ) {
					add( virusMakerList.get(i).exclusiveArray );
				}
			}
			
			add( genericArray );
		}}; 
		
		PART_EFFECTS = new ParticleGenerator();
		pointsManager = new PointsManager( VirusByteGame.HUD, VirusByteGame.VIEWPORT, this );
		VirusByteGame.GAME_ENGINE.insertGameObject( pointsManager );
		VirusByteGame.POINT_MANAGER = pointsManager;
		/*
		>>>>>>>>>>>>>>>>>>>>>>Set inputs controllers>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		gestureInputs = new GestureDetector(new GestureAdapter() {
			@Override
			public boolean fling(float velocityX, float velocityY, int button) {
				if ( !isStageInputsActive ) return false; 
				return flingTrigger( velocityX, velocityY, button );
			}
			
			@Override
			public boolean tap(float x, float y, int count, int button) {
				if ( !isStageInputsActive ) return false;
				if (button == Buttons.LEFT) {
					if ( doubleTapTrigger(x, y) ) return true;
					return tapTrigger(x, y);
				}
				return false;
			}
			
		});
		
		normalInputs = new InputAdapter() {
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				isDrawingSwipe = false;
				swipe.touchUp(screenX, screenY, pointer, button);
				releaseTrigger(screenX, screenY);
				return false;
			}
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				if ( !isStageInputsActive ) return false;
				swipe.touchDragged(screenX, screenY, pointer);
				draggedTrigger(screenX, screenY);
				return false;
			}
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				if ( !isStageInputsActive ) return false;
				swipeTimer = 0;
				swipeColor.a = .75f;
				isDrawingSwipe = true;
				swipe.touchDown(screenX, screenY, pointer, button);
				if (button == Buttons.LEFT) {
					pressedTrigger(screenX, screenY);
				}
				return false;
			}
		};
		//gestureDetector.setTapCountInterval(0.8f); //set tap count interval
		gestureInputs.setTapSquareSize(100f);
		
		VirusByteGame.addProcessor( normalInputs );
		VirusByteGame.addProcessor( gestureInputs );
		<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		*/
		setInputController( new InputTriggers() {
			@Override
			public boolean pressedTouch(Vector3 mousePos) {
				swipeTimer = 0;
				swipeColor.a = .75f;
				isDrawingSwipe = true;
				touchPos.set( mousePos.x, mousePos.y );
				VirusByteGame.VIEWPORT.project( touchPos );
				int screenHeight = VirusByteGame.VIEWPORT.getScreenHeight();
				swipe.touchDown( (int)touchPos.x, screenHeight - (int)touchPos.y, 0, 0 );
				VirusType.SLASH_ORIGIN.set( mousePos.x, mousePos.y );
				VirusType.SLASH.set( mousePos.x, mousePos.y );
				if ( Gdx.input.isKeyPressed( Keys.ALT_LEFT ) ) {
					Coin coin = new Coin();
					addVirus( mousePos.x, mousePos.y, coin );
				}
				return false;
			};
			@Override
			public boolean releaseTouch(Vector3 mousePos) {
				isDrawingSwipe = false;
				touchPos.set( mousePos.x, mousePos.y );
				VirusByteGame.VIEWPORT.project( touchPos );
				VirusByteGame.VIEWPORT.project( mousePos );
				int screenHeight = VirusByteGame.VIEWPORT.getScreenHeight();
				swipe.touchUp( (int)touchPos.x, screenHeight - (int)touchPos.y, 0, 0 );
				WormVirus.SLASH.setZero();
				WormVirus.SLASH_ORIGIN.setZero();
				SpywareVirus.SLASH.setZero();
				SpywareVirus.SLASH_ORIGIN.setZero();
				return false;
			};
			@Override
			public boolean draggedTouch(Vector3 mousePos) {
				touchPos.set( mousePos.x, mousePos.y );
				VirusByteGame.VIEWPORT.project( touchPos );
				int screenHeight = VirusByteGame.VIEWPORT.getScreenHeight();
				swipe.touchDragged( (int)touchPos.x, screenHeight - (int)touchPos.y, 0 );
				if ( !VirusType.HOLDING_VIRUS ) 
					VirusType.SLASH.set( mousePos.x, mousePos.y );
				return false;
			};
		} );
	}
	
	/**Set on/off the inputs triggers on the stage
	 * @param on When <em>false</em>, the stage and all virus will not receive the triggers events*/
	public void setInputTriggersOn( boolean on ) {
//		isStageInputsActive = on;
		for ( int i = allVirus.size-1; i >= 0; i-- )
			allVirus.get( i ).setInputsActive( on );
	}
	
	public enum VirusInstance {Antivirus, Baidu, Bomb, Bot, Bug, Dragon, Energyzer, Flamebot, Freezebot, Infector, 
		Life, Nyxel, Octocat, Parasite, Pepperbros, Plague, Psycho, Saw, Shockbot, Spyware, Trojan, Virus, Worm}
	
	protected class VirusMaker {
		/**Show debug logs only for this VirusMaker class*/
		private boolean InnerDEBUG = false;
		public VirusInstance id;
		public VirusPool pool;
		public Array<VirusType> exclusiveArray;
		public Class<? extends VirusType> type;
		public boolean hasPool, hasArray;
		/**max number of this instance at time on the stage*/
		public int spawnLimit;
		/**total instances this maker added and are current active on stage on the generic or exclusive array*/
		public int totalVirusFromThisMakerOnStage;
		public RuntimeDependencyLoader dependencyLoader;
		
		/**This class make contains the information, pool, and a array to the specific viruas that contains
		 * @param name the name of the virus that this maker will make/create
		 * @param spawnLimit the max number of instances that maker can make.
		 * @param use_pool if true, this maker will use pool
		 * @param useExclusiveArray use a exclusive array to store this type of virus
		 */
		public VirusMaker(VirusInstance id, Class<? extends VirusType> virusClass, int spawnLimit, boolean use_pool, boolean useExclusiveArray) {
			this(id, virusClass, spawnLimit, use_pool, useExclusiveArray, null);
		}
		
		public VirusMaker(VirusInstance id, Class<? extends VirusType> virusClass, int spawnLimit, boolean use_pool, 
				boolean useExclusiveArray, RuntimeDependencyLoader dependency) {
			this.id = id;
			this.spawnLimit = spawnLimit;
			type = virusClass;
			hasPool = use_pool;
			hasArray = useExclusiveArray;
			dependencyLoader = dependency;
			if ( use_pool ) pool = new VirusPool(2, spawnLimit, virusClass);
			if ( useExclusiveArray ) exclusiveArray = new Array<VirusType>();
		}
		
		/**Time limit without use dependency to unload*/
		private final float inactiveDependencyTimeLimit = 10f;
		private float timeToUnload;
		
		/**If this maker has dependency atlas asset to load or unload when necessary or not*/
		public void update(float deltaTime) {
			if ( dependencyLoader != null ) {
				// if this object has dependency, check for loaded and ready atlas 
				if ( dependencyLoader.update(deltaTime) ) {
					//When asset loaded is not in use, then after a time interval, unload it.
					if ( dependencyLoader.isLoaded && totalVirusFromThisMakerOnStage == 0 ) {
						if ( (timeToUnload += deltaTime) > inactiveDependencyTimeLimit ) {
							timeToUnload = 0;
							dependencyLoader.unload( id );
						}
					}
				}
			}	
		}
		
		public VirusType obtain() {
			if ( spawnLimit >= 0 && totalVirusFromThisMakerOnStage >= spawnLimit ) return null;
			//check for dependency 
			if ( dependencyLoader != null && !dependencyLoader.checkDependecy( id ) ) {
				//Has dependency and dependency has not loaded yet
				return null;
			}
			if ( hasPool ) {
				timeToUnload = 0;
				return pool.obtain();
			}
			try {
				timeToUnload = 0;
				return ClassReflection.newInstance( type ); // old form (type.newInstance();)
			} catch (ReflectionException ex) {ex.printStackTrace();}
			
			return null;
		}
		
		/* MAY DEPRECATED
		*//*\*get the total number of instances of this maker current type*//*
		public int getTotalInstances() {
			if ( hasArray ) return exclusiveArray.size;
			int total = 0;
			for ( int i = 0; i < genericArray.size; i++ ) {
				if ( genericArray.get(i).virus_id == id ) total++;
			}
			return total;
		}*/
		
		/**insert the virus inside the stage to act*/
		public void add(VirusType virus) {
			if ( hasArray ) {
				exclusiveArray.add( virus );
			} else {
				genericArray.add( virus );
			}
			totalVirusFromThisMakerOnStage++; //increment value of stage items made by this maker
			if ( InnerDEBUG ) Debug.log( "Virus added to " + id + " Maker | total: " + totalVirusFromThisMakerOnStage );
		}
		
		/**Remove all virus from this virus array, when have array
		 * @return total of removed instances*/
		public int removeAll() {
			int total = 0;
			
			if ( hasArray ) {
				// When this virus or item has as exclusive array
				total = exclusiveArray.size;
				for (VirusType virus : exclusiveArray) virus.removeFromStage();
				/*drawArray.removeAll(exclusiveArray, true); //remove from the draw array
				totalVirusFromThisMakerOnStage -= total;
				exclusiveArray.clear();*/
			} else {
				// When don't have an exclusive array
				for ( int i = 0; i < genericArray.size; i++ ) {
					
					if ( genericArray.get( i ).virus_id == id ) {
						total++;
						genericArray.get( i ).removeFromStage();
					}
					
				} 
				
			}
			if ( InnerDEBUG ) Debug.log( "RemoveAll method called inside " + id + " | total removed: " + total);
			return total;
		}
		
		/*public void clear() {
			if ( hasArray ) exclusiveArray.clear();
			if ( hasPool ) pool.clear();
			Debug.log( "Clear method inside " + id );
		}*/
	}
	
	public VirusMaker getEnum(VirusType virus) {
		return getEnum( virus.virus_id );
	}
	
	/**return the virusMaker object in charge of this type of virus*/
	public VirusMaker getEnum(VirusInstance id) {
		for ( int i = 0; i < virusMakerList.size; i++ ) {
			if ( virusMakerList.get(i).id == id ) {
				//Debug.log( "getEnum successful returned " + virusMakerList.get(i).id );
				return virusMakerList.get(i);
			}
		}
		return null;
	}
	
	@Override
	public void update(SpriteBatch batch, Vector3 mousePos, float deltaTime) {
		spawner.update(deltaTime); //update spawner effects
		for ( int i = 0; i < virusMakerList.size; i++ ) virusMakerList.get(i).update(deltaTime); // update all the makers
		for (int i = 0; i < virus_set.size; i++ ) 
		{
			if ( virus_set.get(i).size > 0 ) 
			{
				// ================================= //
				Iterator<VirusType> virus = virus_set.get(i).iterator();
				while ( virus.hasNext() ) 
				{
					VirusType current = virus.next();
					
					if ( current.alive ) {
//						current.update( deltaTime );
					} else {
						if (current.isFinale == true) {
							pointsManager.addPoint( current.getPointLog() );
							PART_EFFECTS.createEffect( current.getFinalEffectType(), current.x, current.y, 30 );
							
							if ( !(current instanceof HoldableType) ) {
								// is a virus
								if ( current.isComputeKillOn ) totalVirusDestroyed++; //increment total enemies defeats
								if ( totalVirusDestroyed > 20*stage ) stage++;
								VirusByteGame.BACK.setBackColor(); //change background color only when is a death virus
							}
						} else if ( current.toRemove ) {
							//remove this object silently from the stage
							current.toRemove = false; //reset field to re-use.
						} else {
							if ( !current.isReached ) { //not reached to the end of screen
								
							}
						}
						virus.remove();
						allVirus.removeValue( current, true );
//						drawArray.removeValue( current, true ); // remove from draw array
						freeVirusBackToPool( current );
						current.remove();
					}
				}
				// ================================== //
			}
		}
		
		/*pointsManager.update(batch, updateFont, deltaTime);
		PART_EFFECTS.show(batch, deltaTime);*/
		
		if ( SCREEN_DEBUG ) {
			debug.screen("Total Virus: " + getTotalObjects(), 10, 300);
			debug.screen("Virus destroyed: " + totalVirusDestroyed, 10, 320);
//			debug.screen("Items on hand: " + VirusByteGame.ITEM_MANAGER.items.size, 10, 340);
			debug.screen("total generic virus: " + genericArray.size, 10, 360);
			debug.screen("total objects in the draw array: " + drawArray.size, 10, 380);
			debug.screen("total spawnables on transition: " + spawner.spawningList.size, 10, 400);
			debug.screen("total firelist: " + PART_EFFECTS.fireList.size, 10, 420);
		}
	}
	
	/** When pool is available, return the virus back to it.
	 * @param currentVirus when this virus is from some pool, release back to pool. */
	public void freeVirusBackToPool(VirusType currentVirus) {
		VirusMaker virusMaker = getEnum( currentVirus );
		if ( virusMaker != null ) {
			if ( virusMaker.hasPool && currentVirus.canBeFree ) {
				//Debug.log("virus free " + virusMaker.id.toString());
				virusMaker.pool.free( currentVirus ); //release virus back to pool
			}
			virusMaker.totalVirusFromThisMakerOnStage--; //decrement when the virus is removed from the stage
		}
	}
	
	/**When the screen change size update camera matrix*/
	public void resize(int width, int height) {
		cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	private float swipeTimer;
	private final float swipeInterval = .5f;
	
	/** draw everything */
	public void draw( SpriteBatch batch, float deltaTime ) {
		
		if ( drawArray.size != 0 ) {
			drawArray.sort( depthComparator );
			
			boolean logDrawOrder = false;
			for (int i = 0; i < drawArray.size; i++) {
				DrawableObject drawable = drawArray.get(i);
				if ( drawable.getDepth() >= 50 ) continue;
				drawable.draw(batch, deltaTime);
				if ( logDrawOrder ) Debug.log( "Drawing >> " + drawable.getName() );
			}
		}
		
//		PART_EFFECTS.show( batch, deltaTime );
		
	}
	
	/**This draw the item above everything on stage, even the hud*/
	public void drawAbove(SpriteBatch batch, float deltaTime) {
		boolean logDrawOrder = false;
		for (int i = 0; i < drawArray.size; i++) { // draw only objects above 50 depth
			DrawableObject drawable = drawArray.get(i);
			if ( drawable.getDepth() < 50 ) continue;
			drawable.draw(batch, deltaTime);
			if ( logDrawOrder ) Debug.log( "Drawing >> " + drawable.getName() );
		}
		
		if ( isDrawingSwipe ) {
			batch.end();
			
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			tex.bind();
			//the endcap scale
//		tris.endcap = 5f;
			//the thickness of the line
			tris.thickness = 15f;
			//generate the triangle strip from our path
			tris.update( swipe.path() );
			
			//the vertex color for tinting i.e. for opacity
			tris.color = swipeColor;
			
			//render the triangles to the screen
			tris.draw( cam.combined );
			if ( DEBUG_SWIPE ) debugSwipe();
			
			batch.begin();
			if ( (swipeTimer += deltaTime) >= swipeInterval ) {
				if ( swipeColor.a != 0) {
					swipeColor.a -= 0.05f;
					if ( swipeColor.a <= 0 ) {
						isDrawingSwipe = false;
						swipeColor.a = 0;
					}
				}
			}
		}
	}
	
	private void debugSwipe() {
		shapes.setProjectionMatrix( cam.combined );
		
		Array<Vector2> input = swipe.input();
		//draw the raw input
		shapes.begin(ShapeType.Line);
		shapes.setColor(Color.GRAY);
		for (int i=0; i<input.size-1; i++) {
		Vector2 p = input.get(i);
		Vector2 p2 = input.get(i+1);
		shapes.line(p.x, p.y, p2.x, p2.y);
		}
		shapes.end();
		//draw the smoothed and simplified path
		shapes.begin(ShapeType.Line);
		shapes.setColor(Color.RED);
		Array<Vector2> out = swipe.path();
		for (int i=0; i<out.size-1; i++) {
		Vector2 p = out.get(i);
		Vector2 p2 = out.get(i+1);
		shapes.line(p.x, p.y, p2.x, p2.y);
		}
		shapes.end();
		//render our perpendiculars
		shapes.begin(ShapeType.Line);
		Vector2 perp = new Vector2();
		for (int i=1; i<input.size-1; i++) {
		Vector2 p = input.get(i);
		Vector2 p2 = input.get(i+1);
		shapes.setColor(Color.LIGHT_GRAY);
		perp.set(p).sub(p2).nor();
		perp.set(perp.y, -perp.x);
		perp.scl(10f);
		shapes.line(p.x, p.y, p.x+perp.x, p.y+perp.y);
		perp.scl(-1f);
		shapes.setColor(Color.BLUE);
		shapes.line(p.x, p.y, p.x+perp.x, p.y+perp.y);
		}
		shapes.end();
	}
	
	public void debugManagerArrays() {
		String virusNames = "";
		for (int i = 0; i < genericArray.size; i++) {
			virusNames += genericArray.get(i).getName()+", ";
		}
		Debug.log( "generic array contains: " + virusNames );
		
		virusNames = "";
		
		for ( int i = 0; i < allVirus.size; i++ ) {
			virusNames += allVirus.get( i ).getName()+" ";
			virusNames += ": move = "+allVirus.get( i ).isOnMove + ", ";
		}
		
		Debug.log( "manager contains: " + virusNames );
	}
	
	/**remove all virus from updateArray and drawArray*/
	public int clearAll() {
		return clearAll(null);
	}
	
	/**removes all virus from updateArray and drawArray
	 * @param type if specified remove only the correspondent type of virus, when null remove all
	 * @return total number of instances removed from stage
	 * */
	public int clearAll(Class<? extends VirusType> type) {
		int removed = 0;
		
		if ( type != null ) {
			// ============================================ //
			//removing only a specific type of virus from update and draw array 
			VirusType virus = null;
			for (int i = 0; i < virus_set.size; i++) {
				if ( virus_set.get(i).size == 0 ) continue;
				Iterator<VirusType> set = virus_set.get( i ).iterator();
				// == //
				while ( set.hasNext() ) {
					virus = set.next();
					// Check if this virus is instance of the current class type to remove
					if ( type.isInstance(virus) ) {
						//When this virus object is not null and have an ID
						if ( virus != null && virus.virus_id != null ) {
							removed += getEnum( virus.virus_id ).removeAll();  
							break;
						} else {
							allVirus.removeValue( virus, true ); // remove from the virus array
							drawArray.removeValue( virus, true ); //remove from the draw array
							VirusByteGame.GAME_ENGINE.removeGameObject( virus );
							set.remove(); //remove from update array					
						}
					}
				}
				// == //
			}
			
			return removed;
			// ============================================ //
		}
		// removing all virus from all update and draw array
		for (int i = 0; i < virusMakerList.size; i++) {
			removed += virusMakerList.get(i).removeAll(); // remove all virus inside makers
		}
		for (int i = 0; i < genericArray.size; i++) {
			genericArray.get(i).removeFromStage();
			removed++;
		}
		
		return removed;
	}
	
	/**@return total objects on this manager*/
	public int getTotalObjects() {
		int val = 0;
		for ( Array<VirusType> virusArray : virus_set ) {
			val += virusArray.size;
		} 
		return val;
	}
	
	/**@return total virus objects on this manager except {@link ShockBotVirus}*/
	public int getTotalVirus() {
		return getTotalVirus(false);
	}
	
	/**@param bool if false subtract the shockbot from the total
	 * @return total of virus on the manager*/
	public int getTotalVirus(boolean abolute) {
		int result = 0;
		int totalMakers = virusMakerList.size;
		for (int i = 0; i < totalMakers; i++) {
			VirusMaker maker = virusMakerList.get( i );
			if ( maker.spawnLimit > 0 ) result += maker.totalVirusFromThisMakerOnStage;
		}
		if ( !abolute ) result -= getEnum( VirusInstance.Shockbot ).totalVirusFromThisMakerOnStage;
		return result;
	}
	
	/**get the number of instances of determined type*/
	public int getTotalInstances(VirusInstance type) {
		int total = 0;
		for (int i = 0; i < virusMakerList.size; i++) {
			if ( virusMakerList.get(i).id == type ) 
				return virusMakerList.get(i).totalVirusFromThisMakerOnStage;
		}
		//int val = type.array.size;
		return total;
	}
	
	/** return if the item or virus is on the scene */
	public boolean contains(VirusType virus, boolean identity) {
		boolean result = false;
		for (Array<VirusType> virusArray : virus_set) {
			result = virusArray.contains(virus, identity);
			if ( result == true ) break;
		}
		return result;
	}
	
	// ====================================== //
	// == REMOVE SOME ITEM OBJECT THE SCENE = //
	// ====================================== //
	
	/**remove objects of the same specified instance class
	 * @return total removed objects*/
	public int removeAllObjectsOfType(VirusType virus) {
		if ( virus.virus_id == null ) 
			return 0; // virus don't have id
		return removeAllObjectsOfType( virus.virus_id );
	}
	
	/**remove objects of the specified VirusInstace type
	 * @return total removed objects*/
	public int removeAllObjectsOfType(VirusInstance type) {
		return getEnum( type ).removeAll();
	}
	
	// ====================================== //
	// ====================================== //
	// ====================================== //
	
	private VirusType getVirus(VirusInstance... types) {
		VirusType virus = null;
		virus = getEnum( types[MathUtils.random(0, types.length-1)] ).obtain(); 
		return virus;
	}
	
	/**return a virus from the maker object
	 * @param acceptNull if true, the return can be null else the return will force the object to return not null */
	public VirusType obtainVirus(VirusInstance instance, boolean acceptNull) {
		if ( acceptNull && getTotalVirus() > 10) return null; //if can return null, limit of virus in swarm )
		return getEnum( instance ).obtain();
	}
	
	public VirusInstance getVirusName(VirusType virus) {
		return getEnum( virus ).id;
	}
	
	/*========================================Add a new virus on the screen===============================================*/
	public VirusInstance[] randomInstances = new VirusInstance[] {VirusInstance.Plague, VirusInstance.Psycho},
			normalInstances = new VirusInstance[] {VirusInstance.Plague, VirusInstance.Psycho},
			premiumInstances = new VirusInstance[] {VirusInstance.Plague, VirusInstance.Psycho};
	
	public VirusType addVirus() {
		return addVirus(randomInstances);
	}
	
	/** add and return virus */
	public VirusType addVirus(VirusInstance... types) {
		VirusType virus = getVirus( types );
		if ( virus != null ) {
			virus.resetInPosition();
			joinVirus( virus ); //insert virus inside an array
			if ( DEBUG ) {
				VirusMaker virusMaker = getEnum( virus );
				Debug.log(virusMaker.id + "Pool >> Active: " + virusMaker.totalVirusFromThisMakerOnStage + " | Free: " + 
				virusMaker.pool.getFree() + " | Peak: " + virusMaker.pool.peak +"/" + virusMaker.pool.max);
			}
		}
		return virus;
	}
	
	/**Add pick a virus from a array and add to stage with default configuration*/
	public VirusType addVirus(float posX, float posY, VirusInstance... types) {
		VirusType virus = getVirus( types );
		return (virus == null) ? null : addVirus(posX, posY, virus);
	}
	
	/**Add pick a virus from a array and add to stage with specific configuration*/
	public VirusType addVirus(float posX, float posY, ActionListener config, VirusInstance... types) {
		VirusType virus = getVirus( types );
		if ( virus != null ) virus.addActionListener(ACTION.ON_RESET, config);
		return (virus == null) ? null : addVirus(posX, posY, virus);
	}
	
	public VirusType addVirus(float posX, float posY, VirusInstance type, ActionListener config) {
		VirusType virus = getVirus( type );
		if ( virus != null ) virus.addActionListener(ACTION.ON_RESET, config);
		return addVirus(posX, posY, virus);
	}
	
	/** Add a virus object to the stage 
	 * @param posX 
	 * @param posY
	 * @param virus The virus object to insert on the stage 
	 * */
	public VirusType addVirus(float posX, float posY, VirusType virus) {
		if ( virus != null ) {
			virus.resetInPosition(posX, posY);
			joinVirus( virus );
		}
		return virus;
	}
	
	/** add a virus object to the stage */
	public VirusType addVirus(VirusType virus) {
		if (virus != null) {
			virus.resetInPosition();
			joinVirus( virus ); // insert the virus inside an array
		}
		return virus;
	}
	
	/* add a virus object to the stage 
	public VirusType addVirus(float posX, float posY, VirusType virus) {
		return addVirus(posX, posY, virus, virus.getConfig());
	}*/
	
	/**insert the virus inside a respective array to updates and render operations*/
	public void joinVirus( VirusType virus ) {
		boolean inserted = false;
		for ( VirusMaker virusMaker : virusMakerList ) {
			if ( virusMaker.id == virus.virus_id ) {
				virusMaker.add( virus );
				inserted = true;
				break;
			}
		}
		
		if ( !inserted ) 
			genericArray.add(virus); //when not compatible with any virusMaker
		
		allVirus.add( virus );
//		drawArray.add( virus ); //insert virus to the draw array
		VirusByteGame.GAME_ENGINE.insertGameObject( virus );
	}
	
	/*================================================================================================================*/
	
/*	private Viewport getViewport() {
		return VirusByteGame.VIEWPORT;
	}*/
	
	/*====================Triggers======================*/
	/*private boolean tapTrigger(float x, float y) {
		mousePos.set( x, y, 0 );
		getViewport().unproject( mousePos );
		//Debug.log("screenX: " + x + " screenY: " + y + " worldX: " + mousePos.x + " worldY: " + mousePos.y);
		
		for ( int i = allVirus.size-1; i >= 0; i-- ) {
			Array< InputTriggers > controllerList = allVirus.get( i ).getInputControllerList();
			int controllerListSize = controllerList.size;
			for ( int j = 0; j < controllerListSize; j++ )
				if ( controllerList.get( j ).justTouched( mousePos ) )
					return true;
		}
		
		return false;
	}
	
	private boolean flingTrigger(float velocityX, float velocityY, int button) {
		
		for ( int i = allVirus.size-1; i >= 0; i-- ) {
			if ( allVirus.get( i ).flingTouch( velocityX, velocityY, button ) )
				return true;
		}
		
		return false;
	}
	
	private boolean draggedTrigger(float x, float y) {
		mousePos.set( x, y, 0 );
		getViewport().unproject( mousePos );
		
		for (int i = allVirus.size-1; i >= 0; i--) {
			Array< InputTriggers > controllerList = allVirus.get( i ).getInputControllerList();
			int controllerListSize = controllerList.size;
			for ( int j = 0; j < controllerListSize; j++ )
				controllerList.get( j ).draggedTouch( mousePos );
		}
		
		if ( !VirusType.HOLDING_VIRUS ) 
			VirusType.SLASH.set( mousePos.x, mousePos.y );
		
		return false;
	}
	
	public boolean pressedTrigger(float x, float y) {
		mousePos.set( x, y, 0 );
		getViewport().unproject( mousePos );
		
		for ( int i = allVirus.size-1; i >= 0; i-- ) {
			Array< InputTriggers > controllerList = allVirus.get( i ).getInputControllerList();
			int controllerlistSize = controllerList.size;
			for ( int j = 0; j < controllerlistSize; j++ )
				controllerList.get( j ).pressedTouch( mousePos );
		}
		
		VirusType.SLASH_ORIGIN.set( mousePos.x, mousePos.y );
		VirusType.SLASH.set( mousePos.x, mousePos.y );
		
		if ( Gdx.input.isKeyPressed( Keys.ALT_LEFT ) ) {
			Coin coin = new Coin();
			addVirus( mousePos.x, mousePos.y, coin );
		}
		
		return false;
	}
	
	public void releaseTrigger(float x, float y) {
		mousePos.set( x, y, 0 );
		getViewport().unproject( mousePos );
		
		for (int i = allVirus.size-1; i >= 0; i--) {
			Array< InputTriggers > controllerList = allVirus.get( i ).getInputControllerList();
			int controllerListSize = controllerList.size;
			for ( int j = 0; j < controllerListSize; j++ )
				controllerList.get( j ).releaseTouch( mousePos );
		}
		
		WormVirus.SLASH.setZero();
		WormVirus.SLASH_ORIGIN.setZero();
		SpywareVirus.SLASH.setZero();
		SpywareVirus.SLASH_ORIGIN.setZero();
	}
	
	private boolean doubleTapTrigger(float x, float y) {
		mousePos.set( x, y, 0 );
		getViewport().unproject( mousePos );
		
		for ( int i = allVirus.size-1; i >= 0; i-- ) {
			Array< InputTriggers > controllerList = allVirus.get( i ).getInputControllerList();
			int controllerListSize = controllerList.size;
			for ( int j = 0; j < controllerListSize; j++ )
				if ( controllerList.get( j ).doubleTouch( mousePos ) ) 
					return true;
		}
		
		return false;
	}*/
	
	/*=====================================================*/
	
	/** Need be called every time the game is in pause screen */
	public void pauseManager() {
		for ( int i = allVirus.size-1; i >= 0; i-- ) {
			allVirus.get( i ).setInputsActive( false );
		}
		/*VirusByteGame.MAIN_MULTIPLEX_CONTROLLER.removeProcessor( normalInputs );
		VirusByteGame.MAIN_MULTIPLEX_CONTROLLER.removeProcessor( gestureInputs );*/
	}
	/** Need be called when the game is un-paused */
	public void unpauseManager() {
		/*VirusByteGame.addProcessor( normalInputs );
		VirusByteGame.addProcessor( gestureInputs );*/
		for ( int i = allVirus.size-1; i >= 0; i-- ) {
			allVirus.get( i ).setInputsActive( true );
		}
	}
	
	@Override
	public void dispose() {
		tris.dispose();
	}
}
