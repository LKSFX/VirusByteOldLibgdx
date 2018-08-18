package com.lksfx.virusByte.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.Backgrounds;
import com.lksfx.virusByte.gameControl.MyUtils;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.screens.ChallengeGameMode;
import com.lksfx.virusByte.screens.NewMenu;
import com.lksfx.virusByte.screens.GameStageScreen;
import com.lksfx.virusByte.screens.VirusTestGameScreen;

public class GameOverChallengeMenu extends Menu {
	
	private Table table;
	private GameStageScreen gameScreen;
	
	public GameOverChallengeMenu( Stage stage, Skin skin ) {
		super( stage, skin );
	}
	
	@Override
	public void open() {
		super.open();
		Screen screen = VirusByteGame.GAME.getScreen();
		if ( screen instanceof GameStageScreen ) {
			gameScreen = (GameStageScreen) screen;
		}
		setLayout();
	}
	
	@Override
	public void constructLayout() {
		table = new Table( skin );
		table.setFillParent( true );
		table.debug();
		table.add( "Game Over Menu" ).center();
		addTable( table );
		
		table.addAction( Actions.forever( new RunnableAction() {
			@Override
			public void run() {
				if ( gameScreen != null ) {
					Backgrounds back = gameScreen.back;
					SpriteBatch batch = gameScreen.batch;
					float deltaTime = Gdx.graphics.getDeltaTime();
					
					batch.begin();
					back.update(deltaTime);
					back.renderBackgrounds(batch, deltaTime);
					batch.end();
				}
			};
		} ) );
	}
	
	/** Set this layout on open this menus */
	private void setLayout() {

//		if ( VirusByteGame.ADS_ON ) VirusByteGame.AD_CONTROL.showBanner();
		// SHOW INTERSTITIAL ON MOBILE
		/*Timer.schedule(new Task() {
			@Override
			public void run() {
				VirusByteGame.AD_CONTROL.showOrLoadInterstitial();
			}
		}, 2f);*/
		//clear layers
//		mainTableLayer1.debug();
		table.clearChildren();
		
		//stop music
		VirusByteGame.MC.stopAll();
		//show score
		VerticalGroup vg = new VerticalGroup(); //vertical group
		int totalPoints = gameScreen.virus_manager.pointsManager.totalPoints;
		googleServicesUpdateByScore( totalPoints );
		Preferences pref = Gdx.app.getPreferences( VirusByteGame.CONFIG_FILE );
		MyUtils.checkCrypt(pref);
		// if score is higher than the last computed, update best score
		String mode = "best_score-" + (ChallengeGameMode.PREMIUM ? "extreme_mode" : "normal_mode");
		int lastBestScore = pref.getInteger(mode, totalPoints);
		if ( pref.getInteger(mode, 0) < totalPoints ) {
			pref.putInteger(mode, totalPoints);
			pref.putString("crypt", MyUtils.MD5( totalPoints + VirusByteGame.SALT ) );
			pref.flush();
		} 
		Table scoreTable = new Table();
//		scoreTable.debug();
		scoreTable.add( new Label("Game Over", skin, "visitor32") );
		scoreTable.row();
		scoreTable.add( new Label("Score: " + totalPoints, skin, "visitor25") ).left();
		scoreTable.row();
		LabelStyle lbStyle = new LabelStyle( skin.getFont( "visitor25-bold" ) , Color.WHITE);
		final boolean beatScore = ( totalPoints > lastBestScore );
		scoreTable.add( new Label("Best score: " + pref.getInteger(mode, 0), lbStyle) {
			private float textColorSwitch;
			@Override
			public void act(float delta) {
				super.act(delta);
				if ( beatScore ) {
					if ( (textColorSwitch += delta)  > .1f) {
						textColorSwitch = 0;
						getStyle().fontColor = (getStyle().fontColor.equals(Color.BLACK)) ? Color.WHITE : Color.BLACK;
					}
				}
			};
		});
		vg.addActor(new Label("Game Over", skin, "visitor20"));
		vg.addActor(new Label("Score: " + totalPoints, skin, "visitor20"));
		vg.addActor( new Label("Best score: " + pref.getInteger(mode, totalPoints), skin, "visitor20-aqua-light") );
		vg.space(5f);
		
		Window windowFrame = new Window("", skin);
		Image windowBackgroundColor = new Image( skin.newDrawable("tinted-white", new Color(0, 0, 0, .75f)) );
		Stack window = new Stack();
		window.add( windowBackgroundColor );
		window.add( windowFrame );
		//window.debug();
		
		windowFrame.add(new Label("Try again", skin, "visitor25")).colspan(2).padTop(35f);
		windowFrame.row();
		HorizontalGroup hg = new HorizontalGroup(); //horizontal group
		hg.addActor(new MyButton("yes", skin, "little", 84f, 40f, false, null) {
			@Override
			public void act(float delta) {
				super.act(delta);
				if (isChecked()) {
					com.lksfx.virusByte.gameControl.debug.Debug.log("pressed 'Yes' button");
					if ( gameScreen instanceof VirusTestGameScreen ) {
						// Virus Test Mode
						VirusByteGame.GAME.setScreen(new VirusTestGameScreen(VirusByteGame.GAME, ChallengeGameMode.PREMIUM));
					} else {
						// Normal Mode
						VirusByteGame.GAME.setScreen(new ChallengeGameMode(VirusByteGame.GAME, ChallengeGameMode.PREMIUM));
					}
					setChecked(false);
					VirusByteGame.AD_CONTROL.hideBanner();
				}
			}
		});
		hg.addActor(new MyButton("no", skin, "little", 84f, 40f, false, null) {
			@Override
			public void act(float delta) {
				super.act(delta);
				if (isChecked()) {
					com.lksfx.virusByte.gameControl.debug.Debug.log("pressed 'No' button");
					VirusByteGame.GAME.setScreen(new NewMenu(VirusByteGame.GAME, false));
					VirusByteGame.VIRUS_MANAGER.clearAll();
					setChecked(false);
					VirusByteGame.AD_CONTROL.hideBanner();
				}
			}
		});
		hg.space(20f).padTop(15f);
		windowFrame.add( hg ).expandY().bottom().padBottom(20f);
		Debug.log("TABLE BACKGROUND " + table.getBackground());
		//table.debug();
		table.add( scoreTable ).padTop(150f).padBottom(50f);
		table.row();
		table.add( window ).expand().top().prefSize(320f, 140f);
		
		table.row();
		//add leaderboard button
		MyButton leaderboard_button = new MyButton("Leaderboard", skin, "medium", new Color( Color.ORANGE) ) {
			@Override
			public void act(float delta) {
				if (isChecked()) {
					com.lksfx.virusByte.gameControl.debug.Debug.log("show leaderboard!");
					Timer.schedule(new Task() {
						@Override
						public void run() {
							if ( VirusByteGame.GOOGLE_SERVICES ) 
								VirusByteGame.GOOGLE_PLAY.getLeaderboardGPGS();
						}
					}, .3f);
					setChecked(false);
				}
			}
		};
		//add achievements button
		MyButton achievement_button = new MyButton("Achievements", skin, "medium", Color.ORANGE) {
			@Override
			public void act(float delta) {
				if (isChecked()) {
					com.lksfx.virusByte.gameControl.debug.Debug.log("show achievements!");
					Timer.schedule(new Task() {
						@Override
						public void run() {
							if ( VirusByteGame.GOOGLE_SERVICES ) VirusByteGame.GOOGLE_PLAY.getAchievementsGPGS();
						}
					}, .3f);
					setChecked(false);
				}
			}
		};
		
		table.add(achievement_button).padBottom(15f);
		table.row();
		table.add(leaderboard_button);
		
	}
	
	/** verify if the player has achieve something */
	private void googleServicesUpdateByScore(int score) {
		if ( !VirusByteGame.GOOGLE_SERVICES ) return;
		VirusByteGame.GOOGLE_PLAY.submitScoreGPGS(score, ChallengeGameMode.PREMIUM ? VirusByteGame.HIGHSCORE_PREMIUM_MODE : VirusByteGame.HIGHSCORE_NORMAL_MODE);
		if ( ChallengeGameMode.PREMIUM ) return;
		String // achievements depending on game mode.
		achievement1 = "CgkIvuuP864fEAIQAQ",
		achievement2 = "CgkIvuuP864fEAIQAg",
		achievement3 = "CgkIvuuP864fEAIQAw",
		achievement4 = "CgkIvuuP864fEAIQBA",
		achievement5 = "CgkIvuuP864fEAIQBQ";
		int  // scores to achieve
		meta1 = 1000,
		meta2 = 2000,
		meta3 = 3000,
		meta4 = 5000,
		meta5 = 10000;
		if ( score > meta5 ) {
			VirusByteGame.GOOGLE_PLAY.unlockAchievementGPGS(achievement5);
		} else if ( score > meta4 ) {
			VirusByteGame.GOOGLE_PLAY.unlockAchievementGPGS(achievement4);
		} else if ( score > meta3 ) {
			VirusByteGame.GOOGLE_PLAY.unlockAchievementGPGS(achievement3);
		} else if ( score > meta2 ) {
			VirusByteGame.GOOGLE_PLAY.unlockAchievementGPGS(achievement2);
		} else if ( score > meta1 ) {
			VirusByteGame.GOOGLE_PLAY.unlockAchievementGPGS(achievement1);
		}
	}

}
