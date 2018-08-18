package com.lksfx.virusByte.gameControl.campaingControl;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.screens.CampaignGameMode;
import com.lksfx.virusByte.screens.GameStageScreen;

public class LevelController {
	
	public ArrayMap< Integer, Element > eventMap;
	public boolean active;
	private float levelElapsedTime;
	/**Store level xml element for restart level*/
	private Element lvElement;
	private float deltaElapsedTime = -1;
	private float levelDuration = -1;
	private String levelName = "";
	
	public LevelController() {
		eventMap = new ArrayMap<Integer, XmlReader.Element>();
	}
	
	/** Load a level from the internal resource levels folder */
	public void loadLevel( String path ) {
		
		XmlReader levelXML = new XmlReader();
		try {
			
			Element levelElement = levelXML.parse( Gdx.files.internal( "data/levels/" + path + ".xml" ) );
			lvElement = levelElement;
			extractLevelInformation( levelElement );
			organizeEventList( levelElement );
			
		} catch (IOException e) { e.printStackTrace(); }
		
	}
	
	/** Load another external level file */
	public void loadLevel( Element levelElement ) {
		
		lvElement = levelElement;
		extractLevelInformation( levelElement );
		organizeEventList( levelElement );
		
	}
	
	private void extractLevelInformation( Element levelElement ) {
		levelName = levelElement.getAttribute( "levelName", "" );
		levelDuration = levelElement.getFloatAttribute( "duration", -1 );
		Debug.log( "Level duration: " + levelDuration );
	}
	
	private void organizeEventList( Element element ) {
		
		Element timeLineElement = element.getChildByName( "TimeLine" );
		int totalEventElements = timeLineElement.getChildCount(); // number of event elements
		
		for ( int i = 0; i < totalEventElements; i++ ) {
			
			Element event = timeLineElement.getChild( i );
			int timeToTriggerEvent = event.getIntAttribute( "triggerTime", -1 ); // get the time of this event occurrence
			if ( timeToTriggerEvent >= 0 ) // check if the time occurrence is valid
				eventMap.put( timeToTriggerEvent, event );
			
		}
		
	}
	
	public void update( float deltaTime, GameStageScreen gameStageScreen ) {
		
		levelElapsedTime += deltaTime;
		
		int roundedTime = Math.round( levelElapsedTime );
		
		//This avoid call this array map seeking every unnecessary frame
		if ( roundedTime != deltaElapsedTime ) {
			
			if ( eventMap.containsKey( roundedTime ) )
				eventTrigger( eventMap.get(roundedTime), gameStageScreen );
			
		}
		
		Debug.debug.screen("elapsed time: " + roundedTime, 10, 100);
		deltaElapsedTime = roundedTime;
		
		//Check for the end of the game
		if ( levelDuration > 0 && levelElapsedTime > levelDuration ) {
			
			int totalVirus = gameStageScreen.virus_manager.getTotalVirus( true ); // total virus instances active on game stage
			
			if ( totalVirus == 0 ) {
				if ( gameStageScreen instanceof CampaignGameMode ) // end game level
					(( CampaignGameMode ) gameStageScreen).endLevel();
			}
			
		}
		
	}
	
	/** Trigger respective event. */
	private void eventTrigger( Element event, GameStageScreen gameStageScreen ) {
		
		spawnVirusEvent( event, gameStageScreen );
		Debug.log( "Method called every second " + Math.round(levelElapsedTime) );
		
	}
	
	private void spawnVirusEvent( Element event, GameStageScreen gameStageScreen ) {
		
		Array<Element> spawnEventList = event.getChildrenByName( "SpawnVirus" );
		
		for ( int i = 0; i < spawnEventList.size; i++ ) {
			
			Element spawnEvent = spawnEventList.get( i );
			String virusName = spawnEvent.getAttribute( "virus", "" );
			
			if ( !virusName.equals( "" ) ) {
				
				try {
					
					VirusInstance virusType = (VirusInstance)ClassReflection.getField( VirusInstance.class, virusName ).get( new Object() );
					
					if ( spawnEvent.getChildByName("Position") != null ) { // When the virus have a specified position to spawn
						float xScreenPercentage = spawnEvent.getChildByName("Position").getFloatAttribute("x") / 100;
						float yScreenPercentage = spawnEvent.getChildByName("Position").getFloatAttribute("y") / 100;
						
						//Spawn virus on the position
						VirusType virus = gameStageScreen.virus_manager.addVirus( VirusType.WORLD_WIDTH * xScreenPercentage , VirusType.WORLD_HEIGHT * yScreenPercentage, virusType );
						
						if ( !spawnEvent.getAttribute("speed", "").equals( "" ) ) // set virus speed
							virus.setSpeed( spawnEvent.getIntAttribute( "speed" ) );
					}
					else	
						gameStageScreen.virus_manager.addVirus( virusType );
					
				} catch (ReflectionException e) { e.printStackTrace(); }
				
			}
			
		}
		
	}
	
	public String getLevelName() {
		return levelName;
	}
	
	public Element getLevelElement() {
		return lvElement;
	}
	
	public static boolean isValidLevelFile( FileHandle file ) {
		boolean result = false;
		
		try {
			Element xmlElement = new XmlReader().parse( file );
			result = ( file.extension().equals("xml") && xmlElement.getChildByName("TimeLine") != null );
			Debug.log( xmlElement.toString() );
		} catch (IOException e) { e.printStackTrace(); }
		
		return result;
	}
	
}
