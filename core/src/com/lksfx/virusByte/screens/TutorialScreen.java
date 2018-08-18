package com.lksfx.virusByte.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.hud.GameHud.MENU;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.gameObject.virusObject.BotVirus;
import com.lksfx.virusByte.gameObject.virusObject.TrojanVirus;
import com.lksfx.virusByte.gameObject.virusObject.WormVirus;
import com.lksfx.virusByte.menus.MyButton;

public class TutorialScreen extends ChallengeGameMode {
	
	Stage tutoStage;
	Table tutoTable, tipTable;
	Label tutoLabel, tipLabel, titleTipLabel, tutoTitle;
	MyButton nextButton, backButton, quitButton;
	Sprite backSprite, tipBackground;
	int progress = 0, stepProgress, stepProgressSize, activeSpawnDuration = 3;
	boolean tutoActiveState, tutoEnd, hintInfo;
	String[] texts = new String[] {"hello this is the tutorial screen, now you will learn how to defeat all this virus", "the next virus can be defeated by taps"
			+ " tap.", " end of tutorial "};
	Array< ArrayMap<String, Spawnable[]> > tutoVirusMap;
	
	
	public TutorialScreen(VirusByteGame game, boolean premium) {
		super(game, premium);
		VirusType.TUTORIAL = true; //set tutorial mode to true on every virus
		isTutorial = true; // tutorial mode on in this game screen from now
		Timer.instance().clear(); //cancel timer
		tutoVirusMap = new Array< ArrayMap<String, Spawnable[]> >();
		tutoVirusMap.add( new ArrayMap<String, Spawnable[]>() {{ put(texts[0], null); }} ); // 0
		if (premium) {
			tutoVirusMap.add( new ArrayMap<String, Spawnable[]>() {{ put("the next virus can be defeated by taps", tapPremiumVirus); }} ); // 1
			tutoVirusMap.add( new ArrayMap<String, Spawnable[]>() {{ put("the next virus can be defeated by drag and push", dragVirus); }} ); // 2
			tutoVirusMap.add( new ArrayMap<String, Spawnable[]>() {{ put("the next virus can be defeated by circulate around", circleVirus); }} ); // 3
			tutoVirusMap.add( new ArrayMap<String, Spawnable[]>() {{ put("the next virus can be defeated by slash on the right point", cutVirus); }} ); // 4
		} else {
			tutoVirusMap.add( new ArrayMap<String, Spawnable[]>() {{ put("the next virus can be defeated by taps", tapNormalVirus); }} ); // 1
			tutoVirusMap.add( new ArrayMap<String, Spawnable[]>() {{ put("the next virus can be defeated by drag and push", dragVirus); }} ); // 2
			tutoVirusMap.add( new ArrayMap<String, Spawnable[]>() {{ put("the next virus can be defeated by circulate around", circleVirus); }} ); // 3
		}
		tutoVirusMap.add( new ArrayMap<String, Spawnable[]>() {{ put(texts[2], null); }} ); // last
		
		back.spriteBack.setAlpha(1f); // opacity 100% on background
		back.especialRandomBackground = false; // no especial effect background
		
		tutoStage = new Stage(viewport, batch);
		tutoTable = new Table();
		tipTable = new Table();
//		tutoTable.setFillParent(true);
		tipTable.setSize(viewport.getWorldWidth()*.9f, viewport.getWorldHeight()*.3f);
		tipTable.setPosition(viewport.getWorldWidth()*.05f, viewport.getWorldHeight()*.65f);
		tipTable.bottom();
		tipTable.setColor(0f, 0f, 0f, 1f);
		tipTable.setVisible(false);
		tutoTable.setSize(viewport.getWorldWidth()*.9f, viewport.getWorldHeight()*.6f);
		tutoTable.setPosition(viewport.getWorldWidth()*.05f, viewport.getWorldHeight()*.2f);
		tutoTable.setColor(0f, 0f, 0f, 1f);
		
		tipLabel = new Label("", hud.skin, "visitor20-white");
		tipLabel.setWrap(true);
		tipLabel.setAlignment(1);
		tutoLabel = new Label(tutoVirusMap.get(progress > tutoVirusMap.size-1 ? tutoVirusMap.size-1 : progress).getKeyAt(0), hud.skin, "visitor20-white");
		tutoTitle = new Label("", hud.skin, "visitor20-white");
		tutoLabel.setWrap(true);
		tutoLabel.setAlignment(1);
		
		tipTable.add( titleTipLabel = new Label("title", hud.skin, "visitor20-white") );
		tipTable.row();
		tipTable.add(tipLabel).prefSize(viewport.getWorldWidth()*.85f, tutoLabel.getMinHeight()).padBottom(10f).bottom();
		
		tutoTable.add(tutoTitle);
		tutoTable.row();
		tutoTable.add(tutoLabel).prefSize(viewport.getWorldWidth()*.85f, tutoLabel.getMinHeight()).expandY().padTop(10f);
		tutoTable.row();
		backButton = new MyButton(progress == 0 ? "exit" : (currentVirusOnStage != null && !tutoActiveState ? "hint" : "back"), hud.skin, "little", 84f, 40f, false, null) {
			@Override
			public void act(float delta) {
				super.act(delta);
				if (isChecked()) {
					if ( getText().toString().equals("back") ) {
						tutoActiveState = false;
						virus_manager.clearAll();
						Timer.instance().clear();
						stepProgress = 0;
						progress--;
						if (progress < 0) progress = 0; 
						tutoLabel.setText(tutoVirusMap.get(progress).getKeyAt(0));
						tutoLabel.setVisible(true);
						if (progress == 0) {
							nextButton.setText("next");
							setText("exit");
						} else {
							nextButton.setText("ok");
						}
						backSprite.setAlpha(.5f);
						tutoTable.setPosition(viewport.getWorldWidth()*.05f, viewport.getWorldHeight()*.2f); //change table to center position
						if (tutoEnd) tutoEnd = false; //reset tuto end
						com.lksfx.virusByte.gameControl.debug.Debug.log("clicked on back button progress: " + progress);
					} else if ( getText().toString().equals("exit") ) {
						VirusByteGame.GAME.setScreen(new NewMenu(VirusByteGame.GAME, false));
					} else {
						if ( getText().toString().equals("hide") && !tutoActiveState) {
							setText("hint");
							tutoLabel.setVisible(false);
							hintInfo = false;
						} else if ( getText().toString().equals("hint") && !tutoActiveState) {
							setText("hide");
							tutoLabel.setVisible(true);
							hintInfo = true;
						}
					}
					
					setChecked(false);
				}
			}
		};
		nextButton = new MyButton(tutoVirusMap.get(progress).getValueAt(0) != null ? "ok" : "next", hud.skin, "little", 84f, 40f, false, null) {
			@Override
			public void act(float delta) {
				super.act(delta);
				if (isChecked()) {
					
					if (getText().toString().equals("next")) {
						tutoActiveState = false;
						virus_manager.clearAll();
						Timer.instance().clear();
						stepProgress = 0;
						progress++;
						tutoLabel.setText(tutoVirusMap.get(progress).getKeyAt(0));
						if (tutoVirusMap.get(progress).getValueAt(0) != null) {
							setText("ok");
							backButton.setText("back");
							backSprite.setAlpha(.5f);
							tutoLabel.setVisible(true);
							tutoTable.setPosition(viewport.getWorldWidth()*.05f, viewport.getWorldHeight()*.2f); //change table to center position
						}
						if (!tutoEnd && progress >= tutoVirusMap.size-1) {
							tutoEnd = true;
							backButton.setText("back");
							setText("end");
							tutoLabel.setVisible(true);
							backSprite.setAlpha(.5f);
							tutoTable.setPosition(viewport.getWorldWidth()*.05f, viewport.getWorldHeight()*.2f); //change table to center position
						}
						tipTable.setVisible(false);
					} else if ( getText().toString().equals("ok") ) {
						if ( (stepProgressSize != 0) && (stepProgress >= stepProgressSize) ) {
							backButton.setText("back");
							virus_manager.totalVirusDestroyed = 0;
							tutoActiveState = true;
							backSprite.setAlpha(0f);
							setTimerToSpawnVirus();
							tutoLabel.setVisible(false);
							setText("next");
						} else {
							backButton.setText("hint");
							stepProgressSize = tutoVirusMap.get(progress).getValueAt(0).length;
							stepProgress = 0;
							//tutoActiveState = true;
							setTimerToSpawnVirus(.5f);
							setText("next");
							backSprite.setAlpha(0f); //set background sprite to alpha 0
							tutoTable.addAction(Actions.moveTo(viewport.getWorldWidth()*.05f, 0)); //change table position
							tutoLabel.setVisible(false);
						}
					} else if ( getText().toString().equals("end") ) {
						VirusByteGame.GAME.setScreen(new NewMenu(VirusByteGame.GAME, false));
					}
					tutoTitle.setText("");
					setChecked(false);
				}
			}
		};
		nextButton.getStyle().fontColor = new Color(1f, 1f, 1f, 1f);
		HorizontalGroup buttonGroup = new HorizontalGroup();
		buttonGroup.addActor(backButton);
		buttonGroup.addActor(nextButton);
		buttonGroup.space(30f);
		tutoTable.add( buttonGroup ).padBottom(10f);
		
		Debug.log("the minimun height of the text label is " + tutoLabel.getMinHeight());
		TextureRegion tex = VirusByteGame.ASSETS.getAssetManager()
				.get( Assets.Atlas.iconsAtlas.path, TextureAtlas.class ).findRegion("lazer4");
		backSprite = new Sprite(tex);
		tipBackground = new Sprite(tex);
		backSprite.setAlpha(0.5f);
		tipBackground.setAlpha(.5f);
		SpriteDrawable backDraw = new SpriteDrawable(backSprite), tipBackDraw = new SpriteDrawable(tipBackground);
		
		tipTable.setBackground(tipBackDraw);
		tutoTable.setBackground(backDraw);
		
		Debug.log(tutoTable.getBackground() != null ? "background exists" : "background is null"); 
		
		tutoStage.addActor(tutoTable);
		tutoStage.addActor(tipTable);
		tutoTable.debug();
		tipTable.debug();
		
		VirusByteGame.addProcessor(tutoStage); //add to inputs processor
	}
	
	@Override
	public void render(float delta) {
		tutorialStepCheck(); // this act occur here
		
		super.render(delta);
		if (!PAUSED) { 
			tutoStage.act();
			tutoStage.draw();
		}
		debug.screen("tutorial spawning state: " + tutoActiveState, 10, 160);
	}
	
	private VirusType currentVirusOnStage;
	
	private void tutorialStepCheck() {
		if (currentVirusOnStage != null) {
			//when virus has being defeated
			if (currentVirusOnStage.isFinale) {
				tipTable.setVisible(false);
				setTimerToSpawnVirus(3f);
				//check the virus type and may trigger some action
				if (currentVirusOnStage instanceof WormVirus) {
					WormVirus worm = (WormVirus)currentVirusOnStage;
					if (!worm.damageOnSlash) {
						currentVirusOnStage = null;
						stepProgress++;
					}
					return;
				} else if (currentVirusOnStage instanceof TrojanVirus) {
					virus_manager.clearAll(BotVirus.class);
				}
				
				stepProgress++;
				currentVirusOnStage = null;
				if (stepProgress >= stepProgressSize && !tutoActiveState) {
					tutoLabel.setText("now you have to defeat all virus before they reach the botton screen");
					tutoLabel.setVisible(true);
					backSprite.setAlpha(.5f);
					nextButton.setText("ok");
					backButton.setText("back");
					tutoTitle.setText("");
				}
			}
			
		}
	}
	
	@Override
	public void spawnVirus() {
		if (!tutoActiveState && stepProgress < stepProgressSize) {
			Debug.log("called spawn once!");
			Spawnable spawn = tutoVirusMap.get(progress).getValueAt(0)[stepProgress];
			VirusInstance virusType = spawn.virusType;
			if (virus_manager.getTotalObjects() < 1) {
				VirusType virus = virus_manager.addVirus(viewport.getWorldWidth()*.5f, 340f, virusType);
				if (virus != null) {
					if (spawn.hint != null) {
						tutoTitle.setText(spawn.name);
						tutoLabel.setText(spawn.hint);
						tutoLabel.setVisible(hintInfo ? true : false);
						/*tipTable.setVisible(true);
						titleTipLabel.setText(spawn.name);
						tipLabel.setText(spawn.hint);*/
					}
					currentVirusOnStage = virus;
					virus.isOnMove = false;
				}
			}
			return;
		}
		
		if (tutoActiveState && !tutoEnd) {
			if (virus_manager.totalVirusDestroyed > activeSpawnDuration) {
				nextButton.setChecked(true);
			}
			Debug.log("called spawn function!");
			Spawnable[] totalInstances = tutoVirusMap.get(progress).getValueAt(0);
			VirusInstance[] instances = new VirusInstance[totalInstances.length];
			for (int i = 0; i < totalInstances.length; i++) {
				instances[i] = totalInstances[i].virusType;
			}
			
			if (instances != null) {
				Debug.log("spawn " + instances.length + " virus");
				virus_manager.addVirus(instances);
			}
			
			setTimerToSpawnVirus();
		}
	}
	
	private void setTimerToSpawnVirus() {
		setTimerToSpawnVirus( (MathUtils.random() * 2) + 1f ); 
	}
	
	private void setTimerToSpawnVirus(float time) {
		//Reset tasked
		Debug.log("timer schedule set");
		Timer.schedule(new Task() {
			@Override
			public void run() {
				spawnVirus();
			}
		}, time);
	}
	
	public void pauseGame(MENU menu) {
		super.pauseGame(menu);
		tutoTable.setVisible(false);
	}
	
	public void unpauseGame() {
		super.unpauseGame();
		tutoTable.setVisible(true);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		tutoStage.dispose();
	}
	
	private ArrayMap<String, String[]> hints = new ArrayMap<String, String[]>() {{
		put("plague", new String [] {"plague", "It's a plague and will appear very commonly."});
		put("infector", new String [] {"infector", "A infection virus. Throw it away or in other virus to kill then."});
		put("bot", new String [] {"bot", "A virus that comes inside Trojan, it's very weak."});
		put("flamebot", new String [] {"flamebot" , "A flaming bot, it has a fire tail that blocks your vision."});
		put("trojan", new String [] {"trojan" , "The roman horse, when killed will spawn three Bots."});
		put("nyxel", new String [] {"nyxel" , "A stronger variant of the Plague virus. It's immune to bombs."});
		put("parasite", new String [] {"parasite" , "It infects other viruses. When the host virus is killed it will spawn. "});
		put("energyzer", new String [] {"energyzer" , "You need to generate it to overcharge and kill it."});
		put("spyware", new String [] {"spyware" , "Will be partially invisible. You need to detect it first to be slashed verticaly."});
		put("worm", new String [] {"worm" , "A virus taht will shock you if touched in wrong moment. Slash it horizontaly when not charged."});
		put("psycho", new String [] {"psycho" , "A psychotic virus larva."});
	}};
	
	private Spawnable[] tapPremiumVirus = new Spawnable[] {new Spawnable( hints.get("plague"),VirusInstance.Plague, 0f), new Spawnable(hints.get("bot"), VirusInstance.Bot),
			new Spawnable( hints.get("flamebot"), VirusInstance.Bot, 1f),
			new Spawnable( hints.get("psycho"), VirusInstance.Psycho), new Spawnable( hints.get("nyxel"), VirusInstance.Plague, 1f), new Spawnable( hints.get("trojan"), VirusInstance.Trojan)},
			tapNormalVirus = new Spawnable[] {new Spawnable( hints.get("plague"), VirusInstance.Plague, 0f), new Spawnable( hints.get("psycho"), VirusInstance.Psycho)},
			dragVirus = new Spawnable[] {new Spawnable( hints.get("infector"), VirusInstance.Infector), new Spawnable( hints.get("parasite"), VirusInstance.Infector, 1f)},
			circleVirus = new Spawnable[] {new Spawnable( hints.get("energyzer"), VirusInstance.Energyzer)},
			cutVirus = new Spawnable[] {new Spawnable( hints.get("worm"), VirusInstance.Worm), new Spawnable( hints.get("spyware"), VirusInstance.Spyware)};
	
	private class Spawnable {
		public String hint, name;
		public VirusInstance virusType;
		
		/*Spawnable(VirusInstance virusType) {
			this(null, virusType);
		}
		
		Spawnable(VirusInstance virusType, float blind) {
			this(null, virusType, new Float(blind));
		}*/
		
		Spawnable(String[] msg, VirusInstance virusType) {
			this(msg, virusType, null);
		}
		
		Spawnable(String[] msg, VirusInstance virusType, float blind) {
			this(msg, virusType, new Float(blind));
		}
		
		Spawnable(String[] msg, VirusInstance virusType, Float blind) {
			name = msg[0];
			hint = msg[1];
			this.virusType = virusType;
		}
		
	}
	
}
