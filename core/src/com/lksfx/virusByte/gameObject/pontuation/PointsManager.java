package com.lksfx.virusByte.gameObject.pontuation;

import java.util.Iterator;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ReflectionPool;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.hud.GameHud;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.BossType.Bosses;
import com.lksfx.virusByte.gameObject.abstractTypes.GameObject;

public class PointsManager extends GameObject {
	private boolean debug;
	private GameHud hud;
	private BitmapFont font;
	private VirusManager virusManager; 
	private Array<ScreenLogger> pointSet;
	/** Every comboLog object inside this array will be showed on screen as sun as possible in the queue order */
	private Array<ComboLog> combosToShow = new Array<ComboLog>();
	public Array<ScreenLogger> plagueCombo = new Array<ScreenLogger>(), energyzerCombo = new Array<ScreenLogger>(), infectorCombo = new Array<ScreenLogger>()
			, psychoCombo = new Array<ScreenLogger>(), wormCombo = new Array<ScreenLogger>(), botCombo = new Array<ScreenLogger>(), flamebotCombo = new Array<ScreenLogger>(), 
			nyxelCombo= new Array<ScreenLogger>(), spywareCombo = new Array<ScreenLogger>(), parasiteCombo = new Array<ScreenLogger>(), totalCombo = new Array<ScreenLogger>();
	public ArrayMap<VirusInstance, ArrayMap< Array<ScreenLogger>, Float > > 
		comboMap = new ArrayMap<VirusInstance, ArrayMap< Array<ScreenLogger>, Float >>() {{
		put(VirusInstance.Plague, new ArrayMap< Array<ScreenLogger>, Float >() {{ put(plagueCombo, 0f); }});
		put(VirusInstance.Energyzer, new ArrayMap< Array<ScreenLogger>, Float >() {{ put(energyzerCombo, 0f); }});
		put(VirusInstance.Infector, new ArrayMap< Array<ScreenLogger>, Float >() {{ put(infectorCombo, 0f); }});
		put(VirusInstance.Psycho, new ArrayMap< Array<ScreenLogger>, Float >() {{ put(psychoCombo, 0f); }});
		
		put(VirusInstance.Nyxel, new ArrayMap< Array<ScreenLogger>, Float >() {{ put(nyxelCombo, 0f); }});
		put(VirusInstance.Worm, new ArrayMap< Array<ScreenLogger>, Float >() {{ put(wormCombo, 0f); }});
		put(VirusInstance.Bot, new ArrayMap< Array<ScreenLogger>, Float >() {{ put(botCombo, 0f); }});
		put(VirusInstance.Flamebot, new ArrayMap< Array<ScreenLogger>, Float >() {{ put(flamebotCombo, 0f); }});
		put(VirusInstance.Spyware, new ArrayMap< Array<ScreenLogger>, Float >() {{ put(spywareCombo, 0f); }});
		put(VirusInstance.Parasite, new ArrayMap< Array<ScreenLogger>, Float >() {{ put(parasiteCombo, 0f); }});
		
		put(VirusInstance.Virus, new ArrayMap< Array<ScreenLogger>, Float >() {{ put(totalCombo, 0f); }}); //main combo
	}};
	/** this array is on purpose of check if all the bosses are defeated in one single round | boss_type and value total times has defeated in a single round | **/
	private ArrayMap<Bosses, Integer> defeatedBossList = new ArrayMap<Bosses, Integer>();
	
	public int totalPoints, lastCombo;
	private float alertDuration;
	private boolean isAlertActive;
	/**if false, combo are not counted*/
	public boolean COUNTER_ACTIVE = true;
	/**Contains {@link PointLog} objects to reuse, avoiding too much instantiations*/
	public ReflectionPool<PointLog> pointLogPool;
	/**Contains {@link ScreenLogger} objects to reuse, avoiding too much instantiations*/
	public ReflectionPool<ScreenLogger> screenLoggerPool;
	
	public PointsManager(GameHud hud, Viewport viewport, VirusManager virusManager) {
		this.hud = hud;
		this.virusManager = virusManager;
		this.font = VirusByteGame.FONT;
		pointSet = new Array<ScreenLogger>();
		pointLogPool = new ReflectionPool<PointLog>( PointLog.class );
		screenLoggerPool = new ReflectionPool<ScreenLogger>( ScreenLogger.class );
		/*ComboLog ob1 = new ComboLog("", 1, 2f); 
		ComboLog ob2 = new ComboLog("", 1, 2f);
		
		Debug.log("two objects are " + (ob1.equals(ob2)? "equals" : "different"));*/
	}
	
	
	/** Update and draw all points objects active on the screen
	 *  @param batch to draw the points made.
	 *  @param font for draw the points on screen.
	 */ 
	@Override
	public void update( float delta ) {
		
		/*debug.screen("Plague combo: " + plagueCombo.size, 10, 60);
		debug.screen("Energyzer combo: " + energyzerCombo.size, 10, 80);
		debug.screen("Infector combo: " + infectorCombo.size, 10, 100);
		debug.screen("Psycho combo: " + psychoCombo.size, 10, 120);
		debug.screen("Total combo: " + totalCombo.size, 10, 140);*/
	}
	
	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		
		if ( pointSet.size > 0 ) {
			Iterator<ScreenLogger> iterator = pointSet.iterator();
			while ( iterator.hasNext() ) {
				ScreenLogger current = iterator.next();
				if ( !current.show(batch, font, deltaTime) ) {
					iterator.remove();
					if ( current instanceof PointLog ) {
						pointLogPool.free( (PointLog)current );
						if ( debug ) 
							Debug.log( "pointLogPool peak: " + pointLogPool.peak + " | max: " + pointLogPool.max + " | free: " + pointLogPool.getFree() );
					} 
					else if ( current instanceof ScreenLogger ) {
						screenLoggerPool.free( current );
						if ( debug ) 
							Debug.log( "screenLoggerPool peak: " + screenLoggerPool.peak + " | max: " + screenLoggerPool.max + " | free: " + screenLoggerPool.getFree() );
					}
				}
			}
		}
		
		resolveCombo( deltaTime );
		showAlerts( batch, font, deltaTime );
		
	}
	
	/**Insert the combo data or the string to show in the screen*/
	public void addPoint( ScreenLogger log ) {
		
		if ( log == null | !COUNTER_ACTIVE )
			return; // return if log is null or combo counter is inactive
		
		if ( log instanceof PointLog ) { // check if this screen logger is a point log object
			
			PointLog point = (PointLog) log; // convert screenLogger to point object
			
			if ( point.assign ) // check is this point is valid
				totalPoints += point.value;
			
			if ( point.type != null ) // check if the point come from a valid object
				addToCombo( point );
			
			if ( debug ) 
				Debug.log( "point log added to point manager" );
			
		}
		
		pointSet.add( log ); //point or message to show in the screen
		
	}
	
	/** Add combo to timer combo checking */
	public void addToCombo( PointLog point ) {
		
		float timeIncrease = point.addTime > 0 ? point.addTime : 1f;  
		
		totalCombo.add( point ); // add point to generic combo
		comboMap.get( VirusInstance.Virus ).setValue(0, timeIncrease); // reset the generic combo timer
		
		if ( !point.hit ) 
			return; //if no especial hit on this point
		
		for ( VirusInstance id : comboMap.keys() ) {
			// check for a key of same type of the point earn
			if ( id.equals( point.type ) ) {
				ArrayMap< Array<ScreenLogger>, Float > map = comboMap.get( id ); // get the combo map of the same point type
				
				if ( debug ) 
					Debug.log("Point inserted to combo " + point.type + " !!!");
				
				map.setValue(0, timeIncrease); // combo timer increase
				map.getKeyAt(0).add( point ); // point object add to array
			}
			
		}
		
	}
	
	public int totalObjects() {
		return pointSet.size;
	}
	
	/** Check for combo completion or times up */
	public void resolveCombo(float delta) {
		
		for ( int i = 0; i < comboMap.size; i++ ) {
			ArrayMap<Array<ScreenLogger>, Float> map = comboMap.getValueAt(i); //get the combo map object
			if ( map.getKeyAt(0).size == 0 ) // empty map object
				continue;
			
			float lastingTimeForCombo = map.getValueAt(0); // lasting time for combo end
			if ( lastingTimeForCombo > 0f ) { // when combo is up
				map.setValue(0, lastingTimeForCombo-delta); // decrease time
			} 
			else {
				comboFinished( map.getKeyAt(0) ); // finish time to increase combo
				//pointsInCombo.clear();
			}
		}
		
	}
	
	/** When the combo end, this function check if the combo made are valid for extra points */
	public void comboFinished( Array<ScreenLogger> lastFinishedCombo ) {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		int size = lastFinishedCombo.size;
		if ( lastFinishedCombo.equals(plagueCombo) ) {
			if ( size > 2 ) {
				ComboLog combo = new ComboLog("Combo", size, 3f, 10, assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("plague-trail") );
				if ( MathUtils.randomBoolean(0.5f) ) virusManager.addVirus(VirusManager.VirusInstance.Life); //50% chance to give a bonus heart life item
				if (!combosToShow.contains(combo, false)) {
					combosToShow.add(combo);
					combosToShow.sort();
				}
			}
		} else if ( lastFinishedCombo.equals(energyzerCombo) ) {
			if ( size > 1 ) {
				ComboLog combo = new ComboLog("Combo", size, 3f, 25, assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("energyzer-trail") );
				if ( MathUtils.randomBoolean(0.75f) ) virusManager.addVirus(VirusManager.VirusInstance.Life); //75% chance to give a bonus heart life item
				if ( !combosToShow.contains(combo, false) ) {
					combosToShow.add( combo );
					combosToShow.sort();
				}
			}
		} else if ( lastFinishedCombo.equals(infectorCombo) ) {
			if ( size > 2 ) {
				ComboLog combo = new ComboLog("Combo", size, 3f, 14, assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("infector-trail") );
				if ( MathUtils.randomBoolean(0.25f) ) virusManager.addVirus(VirusManager.VirusInstance.Life); //25% chance to give a bonus heart life item
				if ( !combosToShow.contains(combo, false) ) {
					combosToShow.add( combo );
					combosToShow.sort();
				}
			}
		} else if ( lastFinishedCombo.equals(psychoCombo) ) {
			if ( size > 2 ) {
				ComboLog combo = new ComboLog("Combo", size, 3f, 10, assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("psycho-trail") );
				if ( MathUtils.randomBoolean(0.5f) ) virusManager.addVirus(VirusManager.VirusInstance.Life); //50% chance to give a bonus heart life item
				if ( !combosToShow.contains(combo, false) ) {
					combosToShow.add( combo );
					combosToShow.sort();
				}
			}
		} else if ( lastFinishedCombo.equals(botCombo) ) {
			if ( size > 2 ) {
				ComboLog combo = new ComboLog("Combo", size, 3f, 10, assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("bots-trail") );
				if ( MathUtils.randomBoolean(0.25f) ) virusManager.addVirus(VirusManager.VirusInstance.Life); //25% chance to give a bonus heart life item
				if ( !combosToShow.contains(combo, false) ) {
					combosToShow.add( combo );
					combosToShow.sort();
				}
			}
		}  else if ( lastFinishedCombo.equals(flamebotCombo) ) {
			if ( size > 2 ) {
				ComboLog combo = new ComboLog("Combo", size, 3f, 10, assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("flamebot-trail") );
				if ( MathUtils.randomBoolean(0.25f) ) virusManager.addVirus(VirusManager.VirusInstance.Life); //25% chance to give a bonus heart life item
				if ( !combosToShow.contains(combo, false) ) {
					combosToShow.add( combo );
					combosToShow.sort();
				}
			}
		} else if ( lastFinishedCombo.equals(wormCombo) ) {
			if ( size > 1 ) {
				ComboLog combo = new ComboLog("Combo", size, 3f, 15, assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("worm-trail") );
				if ( MathUtils.randomBoolean(0.75f) ) virusManager.addVirus(VirusManager.VirusInstance.Life); //75% chance to give a bonus heart life item
				if ( !combosToShow.contains(combo, false) ) {
					combosToShow.add( combo );
					combosToShow.sort();
				}
			}
		} else if ( lastFinishedCombo.equals(nyxelCombo) ) {
			if ( size > 1 ) {
				ComboLog combo = new ComboLog("Combo", size, 3f, 25, assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("nyxel-trail") );
				if ( MathUtils.randomBoolean(0.75f) ) virusManager.addVirus(VirusManager.VirusInstance.Life); //75% chance to give a bonus heart life item
				if ( !combosToShow.contains(combo, false) ) {
					combosToShow.add( combo );
					combosToShow.sort();
				}
			}
		} else if ( lastFinishedCombo.equals(spywareCombo) ) {
			if ( size > 1 ) {
				ComboLog combo = new ComboLog("Combo", size, 3f, 15, assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("spyware-trail") );
				if ( MathUtils.randomBoolean(0.75f) ) virusManager.addVirus(VirusManager.VirusInstance.Life); //75% chance to give a bonus heart life item
				if ( !combosToShow.contains(combo, false) ) {
					combosToShow.add( combo );
					combosToShow.sort();
				}
			}
		} else if ( lastFinishedCombo.equals(parasiteCombo) ) {
			if ( size > 1 ) {
				ComboLog combo = new ComboLog("Combo", size, 3f, 5, assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("parasite-trail") );
				if ( MathUtils.randomBoolean(0.25f) ) virusManager.addVirus(VirusManager.VirusInstance.Life); //25% chance to give a bonus heart life item
				if ( !combosToShow.contains(combo, false) ) {
					combosToShow.add( combo );
					combosToShow.sort();
				}
			}
		}
		
		if ( !isAlertActive ) 
			alertCombo();
		
		lastFinishedCombo.clear();
		
	}
	/** check the comboToShow list and if has some in the queue show */
	private void alertCombo() {
		
		if ( combosToShow.size != 0 ){ // check if has some combo to show
			ComboLog toShow = combosToShow.pop(); // get the first combo on the stack and remove from it
			alertDuration = toShow.duration; //
			lastCombo = toShow.size;
			hud.addAlert( toShow ); // send this comboLog object to the hud for render on screen
			totalPoints += toShow.points;
			if ( debug ) {
				Debug.log("combo queue: " + combosToShow);
				Debug.log("first is: " + toShow);
			}
			if ( toShow.duration > 0f ) 
				isAlertActive = true;
		}
		
	}
	
	public void boss_log( String comboName, int comboSize, float showDuration, int pointsEarned, TextureRegion image ) {
		ComboLog combo = new ComboLog( comboName, comboSize, showDuration, pointsEarned, image );
		combosToShow.add( combo );
		combosToShow.sort();
	}
	
	public void showAlerts(SpriteBatch batch, BitmapFont font, float delta) {
		
		if ( isAlertActive ) {
			alertDuration -= delta;
			if ( alertDuration <= 0f ) {
				isAlertActive = false;
				lastCombo = 0;
				hud.removeAlert();
			}
		} 
		else {
			alertCombo();
		}
		
	}
	
	public class ComboLog implements Comparable<ComboLog> {
		public int size, points;
		public float duration;
		public TextureRegion tex;
		public String msg;
		
		public ComboLog(String msg, int size, float duration, int point) {
			this(msg, size, duration, point, null);
		}
		
		public ComboLog(String msg, int size, float duration, int point, TextureRegion tex) {
			this.msg = msg;
			this.size = size;
			this.duration = duration;
			this.tex = tex;
			this.points = size * point;
		}
		
		@Override
		public int compareTo(ComboLog other) {
			return this.size - other.size;
		}
		
		@Override
		public String toString() {
			return "comboSize : "+size;
		}
		
		@Override
		public boolean equals(Object other) {
			if ( other == null ) return false;
		    if ( other == this ) return true;
		    if ( other instanceof ComboLog ) {
		    	ComboLog comp = ComboLog.class.cast(other);
		    	if ( (this.tex.equals(comp.tex)) && (this.size == comp.size) ) 
		    		return true;
		    }
		    return false;
		}
		
		@Override
		public int hashCode() {
			return String.valueOf(size+tex.hashCode()).hashCode();
		}
	}
	
	/// =============================== ///
	/// === for achievements methods == ///
	/// =============================== ///
	
	/** when some boss is defeated, call this method to compute it for the achievements **/
	public void addDefeatedBossToList( Bosses boss ) {
		// only add the boss one time
		if ( !defeatedBossList.containsKey(boss) ) { //Put this boss on the list of defeated bosses
			defeatedBossList.put(boss, 1); 
		} else { // update the boss number of times defeated on the bosses list
			defeatedBossList.setValue(defeatedBossList.indexOfKey(boss), defeatedBossList.get(boss)+1);
		}
	}
	
	/** return true if all the available bosses are defeated in this round **/
	public boolean isAllBossesDefeatedInThisRound() {
		int 
		totalNumberOfBosses = Bosses.values().length, 
		totalNumberOfDefeatedBosses = defeatedBossList.size;
		return (totalNumberOfBosses == totalNumberOfDefeatedBosses);
	}
	
	/**return the number of total times this boss is defeated in this round **/
	public int getBossTotalTimesDefeated( Bosses boss ) {
		int result = -1;
		if ( defeatedBossList.containsKey(boss) ) 
			result = defeatedBossList.get(boss);
		return result;
	}
}
