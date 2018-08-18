package com.lksfx.virusByte.screens;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.hud.GameHud.MENU;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class VirusTestGameScreen extends ChallengeGameMode {

	public VirusTestGameScreen(VirusByteGame game, boolean premium) {
		super( game, premium );
		spawnList.clear();
//		back.activeRandomBackground = false;
		//virus_manager.pointsManager.COUNTER_ACTIVE = false; //Wont count points in this game mode
		hud.mainTableLayer3.top();
		hud.mainTableLayer3.add( "Virus Test Mode" );
		addInstancesToSpawningList( VirusInstance.Plague );
//		VirusByteGame.GAME_ENGINE.insertGameObject( new TestGameObject() );
//		callLimit = 1;
//		VirusByteGame.CONSOLE.call();
		spawnLimit = 4;
		//animation set galaxy .5 15 0 0 true
		/*for (int i = 0; i < 3; i++) hud.inventory.addItem(2, new NewSaw(), false);
		for (int i = 0; i < 3; i++) hud.inventory.addItem(1, new NewBomb(), false);	
		for (int i = 0; i < 3; i++) hud.inventory.addItem(0, new NewAntivirus(), false);*/
	}
	
	/**Interval between {@link spawnVirus} call*/
	public float spawnInterval = 3f;
	
	/**Number of virus called in a spawn*/
	public int swarmCall = 3;
	
	/**Maximum of virus on stage*/
	public int spawnLimit = 3;
	
	/**The max number of the spawnVirus call*/
	public int call, callLimit = 0;
	
	@Override
	public void spawnVirus() {
		if ( callLimit != 0 && call++ >= callLimit /*|| VirusByteGame.TOTAL_ROUNDS_PLAYED > 0*/ ) return;
		int totalVirusOnStage = virus_manager.getTotalVirus();
		
		for (int i = Math.min( swarmCall, spawnLimit ); i > 0; i--) {
			// ===== //
			if ( spawnList.size > 0 ) { //if the list has at least one element
				if ( totalVirusOnStage < spawnLimit ) { //Check first if the stage is not full
					VirusInstance virus = spawnList.get( MathUtils.random(spawnList.size-1) );
					if ( virus == null ) continue; // pass this iteration if virus is null
					boolean isBoss = (virus == VirusInstance.Baidu || virus == VirusInstance.Dragon || virus == VirusInstance.Pepperbros || virus == VirusInstance.Octocat);
					if ( isBoss && VirusType.BOSS_TIME ) {
						if ( spawnList.size == 1 ) 
							break;
						continue;
					}
					totalVirusOnStage++; //increase plus one in the total virus on the stage
					virus_manager.addVirus( virus ); //the max virus the stage support is 10 at time
				}
			}
			// ===== //
		}
		
		/*if ( MathUtils.randomBoolean(.2f) && VirusByteGame.VIRUS_MANAGER.getSize(VirusInstance.Saw) < 1 ) {
			VirusByteGame.VIRUS_MANAGER.addVirus(VirusInstance.Saw);
		}*/
		
//		endGameRound();
		
		spawnerIntervalTime = spawnInterval;
		spawnerCurrentTime = 0f;
	}
	
	/**Add instances to spawn list*/
	public void addInstancesToSpawningList(VirusInstance... instances) {
		spawnList.addAll(instances);
	}
	
	/**remove virus from the spawning list*/
	public void removeInstancesFromSpawningList(VirusInstance... instances) {
		for (VirusInstance instance : instances) {
			spawnList.removeValue(instance, false);
		}
	}
	
	/**Remove all virus on the spawning list*/
	public void removeAllInstancesFromSpawningList() {
		spawnList.clear();
	}
	
	@Override
	protected void gameFinalize() {
		spawnerActive = false;
		Timer.instance().clear();
		Timer.schedule(new Task() {
			@Override
			public void run() {
				pauseGame( MENU.GAMEOVER_CHALLENGE_MODE );
			}
		}, 1f);
	}
}
