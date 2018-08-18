package com.lksfx.virusByte.screens;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.hud.GameHud.MENU;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.gameObject.virusObject.BugVirus;

public class ChallengeGameMode extends GameStageScreen {
	private int lastLives, lastPoints, lastKills, lastStage;
	
	/**When true, {@link ChallengeGameMode} class will automatically call {@link #spawnVirus()} between an interval of time*/
	protected boolean spawnerActive = true;
	/**Time past since last {@link #spawnVirus()} call*/
	protected float spawnerCurrentTime;
	/**Interval time between, spawning call*/
	protected float spawnerIntervalTime = 1f;
	
	/**define the virus list that can be spawned by the spawnVirus method*/
	protected Array<VirusInstance> spawnList;
	
	public VirusInstance[] normalModeInstances, premiumModeInstances;
	
	public ChallengeGameMode(VirusByteGame game, boolean premium) {
		super(game, premium);
	}
	
	@Override
	public void initialize(VirusByteGame game, boolean premium) {
		super.initialize(game, premium);
		updateValues(); // update last values
		//start music if allowed
//		VirusByteGame.MC.addToPlaylist( Assets.Music.fandangos ); TODO 
		
		spawnList = new Array<VirusManager.VirusInstance>();
		normalModeInstances = new VirusInstance[] {VirusInstance.Plague, VirusInstance.Energyzer, VirusInstance.Infector, VirusInstance.Psycho, 
				VirusInstance.Nyxel}; 
		premiumModeInstances = new VirusInstance[] {VirusInstance.Plague, VirusInstance.Energyzer, VirusInstance.Infector, VirusInstance.Psycho, 
				VirusInstance.Worm, VirusInstance.Flamebot, VirusInstance.Spyware, VirusInstance.Trojan, VirusInstance.Nyxel};
		Debug.log( "premium list " + premiumModeInstances + " | normal list: " + normalModeInstances );
		setSpawnList( (PREMIUM) ? premiumModeInstances : normalModeInstances );
		hud.loadPreviousInventoryStatus();
	}
	
	@Override
	public void render( float delta ) {
		super.render( delta );
		
		if ( spawnerActive && !PAUSED && ( spawnerCurrentTime += gameDelta ) > spawnerIntervalTime ) 
			spawnVirus();
		updateValues();
		
		/*debug.screen("Lives: " + lives, 10, 20);
		debug.screen("Total points: " + virus_manager.pointsManager.totalPoints, 10, 40);
		debug.screen("Total Virus: " + VirusManager.getSize(), 10, 100);*/
		//debug.screen("BossTime " + VirusType.BOSS_TIME, 10, 120);
//		debug.screen("Total Generics: " + virus_manager.genericArray.size, 10, 120);
		/*debug.screen("Total Item on Hand: " + itemManager.items.size, 10, 120);
		debug.screen("World width: " + viewport.getWorldWidth(), 10, 100);
		debug.screen("Word height: " + viewport.getWorldHeight(), 10, 120);*/
		
		if ( lives < 1 && !isGameEnded ) 
			endGameRound();
	}
	
	/**End the game, clear stage, set backgrounds off and call the {@link #gameFinalize()}} method*/
	protected final void endGameRound() {
		virus_manager.clearAll(); // clean all virus from the screen and stage
		gameFinalize();
		isGameEnded = true;
		back.especialRandomBackground = false;
		VirusByteGame.TOTAL_ROUNDS_PLAYED++;
	}
	
	/**Called when the game end generally from the {@link #endGameRound()} */
	protected void gameFinalize() {
		spawnerActive = false;
		hud.inventory.hideAllSlots();
		hud.inventory.save();
		Timer.instance().clear();
		Timer.schedule(new Task() {
			@Override
			public void run() {
				pauseGame( MENU.GAMEOVER_CHALLENGE_MODE );
			}
		}, 1f);
	}
	
	public int boss_time_interval = 3, next_boss_time = boss_time_interval;
	
	public void spawnVirus() {
		int call = Math.min(MathUtils.random(2+stage), 6); //number of virus calls
		
		//==================================//
		if (virus_manager.getTotalObjects() < (Math.min(10, stage+2)) ) {
			if (!VirusType.BOSS_TIME) {
				for (int i = call; i > 0; i--) {
					virus_manager.addVirus( spawnList.get( MathUtils.random(spawnList.size-1) ) ); //the max virus the stage support is 10 at time
				}
			}
			//==================================//
			if ( PREMIUM ) {
				
				if (stage == next_boss_time && !VirusType.BOSS_TIME) {
					//set boss time spawning BOSS
					next_boss_time += boss_time_interval;
					int random = MathUtils.random(3);
					switch ( random ) {
					case 0:
						virus_manager.addVirus( VirusInstance.Octocat );
						break;
					case 1:
						virus_manager.addVirus( VirusInstance.Baidu );
						break;
					case 2:
						virus_manager.addVirus( VirusInstance.Pepperbros );
						break;
					case 3:
						virus_manager.addVirus( VirusInstance.Dragon );
						break;
					}
				}
					//if (virusManager.getSize(VirusManager.VirusInstance.Octocat) < 1) virusManager.addVirus(VirusManager.VirusInstance.Octocat);
			}
			
		}
		
		// at this point the bug virus can appears, rarely
		if ( !VirusType.BOSS_TIME && stage >= 3 ) {
			if ( MathUtils.randomBoolean(0.1f) ) {
				BugVirus bug = new BugVirus();
				if ( !virus_manager.contains(bug, false) ) virus_manager.addVirus(bug);
			}
		}
		
		// Reset time
		spawnerIntervalTime = 1f + MathUtils.random(2f);
		spawnerCurrentTime = 0;
	}
	
	public void setSpawnList(VirusInstance... instances) {
		spawnList.clear();
		spawnList.addAll( instances );
	}
	
	public void updateValues() {
		if (lastLives != lives) {lastLives = lives; hud.hudLives.setText(""+lives);}
		if (lastKills != virus_manager.totalVirusDestroyed) {lastKills = virus_manager.totalVirusDestroyed; hud.hudKills.setText(""+virus_manager.totalVirusDestroyed);}
		if (lastPoints != virus_manager.pointsManager.totalPoints) {lastPoints = virus_manager.pointsManager.totalPoints; hud.hudPoints.setText(""+virus_manager.pointsManager.totalPoints);}
		if (lastStage != virus_manager.stage) {lastStage = virus_manager.stage; hud.hudStage.setText(""+virus_manager.stage);}
	}
	
	@Override
	public void onCloseApplication() {
		hud.inventory.save();
	}
}
