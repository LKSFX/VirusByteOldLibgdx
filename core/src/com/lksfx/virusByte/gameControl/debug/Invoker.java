package com.lksfx.virusByte.gameControl.debug;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.Backgrounds;
import com.lksfx.virusByte.effects.Backgrounds.Animations;
import com.lksfx.virusByte.gameControl.hud.GameHud.MENU;
import com.lksfx.virusByte.gameControl.hud.GameHud.Status;
import com.lksfx.virusByte.gameControl.hud.Inventory;
import com.lksfx.virusByte.gameObject.VirusManager;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.HoldableType;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.screens.ChallengeGameMode;
import com.lksfx.virusByte.screens.VirusTestGameScreen;

public class Invoker {
	public static interface CallMethod {
		public void invoke(String... args);
	}
	
	private ArrayMap<String, CallMethod> methodArray;
	private Console cmd;
	
	public Invoker(Console console) {
		methodArray = new ArrayMap<String, Invoker.CallMethod>();
		cmd = console;
		
		//clear console text area
		methodArray.put("clear", new CallMethod() {
			@Override
			public void invoke(String... args) {
				cmd.textDisplayArea.getActor().setText("");
			}
		});
		
		//background methods invoker
		methodArray.put("background", new CallMethod() {
			@Override
			public void invoke(String... args) {
				int totalArgs = args.length;
				if ( totalArgs > 1 && args[1].equals("color") ) {
					if (totalArgs == 6) {
						if ( args[3].matches("\\d+") && args[4].matches("\\d+") && args[5].matches("\\d+") ) {
							int[] compoment = new int[3];
							for ( int i = 3; i < totalArgs; i++ ) compoment[i-3] = Integer.valueOf(args[i]);
							for ( int val : compoment ) {
								if ( val> 255 || val < 0 ) {
									cmd.show( "component out of bounds! 0 to 255" ); 
									return;
								}
							}
							VirusByteGame.BACK.setBackColor(compoment[0], compoment[1], compoment[2]);
							cmd.show( "background color set to:  r [" + args[3] + "] g [" + args[4] + "] b [" + args[5] + "]" );
							return;
						}
					} else if (totalArgs == 4) {
						String msg = "", color = args[3].toUpperCase();
						try {
							Field field = ClassReflection.getField( Color.class, color );
							Color component = (Color) field.get( new Object() );
							VirusByteGame.BACK.setBackColor(component.r, component.g, component.b);
							msg = "background color set to: r [" + component.r*255 + "] g [" + component.g*255 + "] b [" + component.b*255 + "]";
							cmd.show( msg.replaceAll("(?<=\\d\\.)0(?=])", "") );
							return;
						} catch (ReflectionException e) {e.printStackTrace();}
					} 
				}
				cmd.show( args[0] );
			}
		});
		
		//particles method invoker
		methodArray.put("particle", new CallMethod() {
			@Override
			public void invoke(String... args) {
				int totalArgs = args.length;
				//set min/max particles on screen method 
				if ( args[1].equals("sparks") ) {
					if ( !VirusByteGame.GAME.getScreen().getClass().equals( ChallengeGameMode.class ) ) {cmd.show( "cant set particles on menu screen" ); return;}
					int minValue = 150, maxValue = 300;
					if ( totalArgs == 3 && args[2].equals("default") ) {
						//set the default value
						if ( VirusManager.PART_EFFECTS != null ) {
							VirusManager.PART_EFFECTS.minSparks = minValue;
							VirusManager.PART_EFFECTS.maxSparks = maxValue;
							cmd.show( "spark particles range is set to default" );
						}
					} else if ( totalArgs == 4 &&  args[2].matches("\\d+") && args[3].matches("\\d+")) {
						minValue = Integer.valueOf( args[2] );
						maxValue = Integer.valueOf( args[3] );
						if ( minValue > maxValue ) {cmd.show( "min value cant be higher than max value" ); return;}
						if ( VirusManager.PART_EFFECTS != null ) {
							VirusManager.PART_EFFECTS.minSparks = minValue;
							VirusManager.PART_EFFECTS.maxSparks = maxValue;
							cmd.show( "spark particles range is set to MIN: " + minValue + " | MAX: " + maxValue );
						}
					} else {
						cmd.show( "invalide arguments" ); 
						return;
					}
					Preferences pref = Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE);
					pref.putInteger("particle_max_sparks", minValue);
					pref.putInteger("particle_min_sparks", maxValue);
					pref.flush();
				}
			}
		});
		
		//virus trail control
		methodArray.put("trails", new CallMethod() {
			@Override
			public void invoke(String... args) {
				int totalArgs = args.length;
				if ( totalArgs != 3 ) {cmd.show( "incorrect number of arguments" ); return;}
				if ( args[1].equals("maxlength") ) {
					if ( !args[2].matches("\\d+") ) {
						if ( args[2].equals( "default" ) ) {
							cmd.show( "trails max length is set back to default values" );
							VirusType.GLOBAL_MAX_TRAIL_SIZE = 0;
							Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE).putInteger("trails_max_length", 0);
							Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE).putBoolean("trails_active", true);
							Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE).flush();
						} else {
							cmd.show( args[2] + "  ...incorrect argument " );
						}
						return;
					}
					int length = Integer.valueOf( args[2] );
					if ( length <= 0 ) {
						VirusType.TRAILS_ACTIVE = false; 
						cmd.show( "trails deactivated!" );
					} else {
						VirusType.GLOBAL_MAX_TRAIL_SIZE = length;
						VirusType.TRAILS_ACTIVE = true;
						cmd.show( "trails max length set to " + length );
					}
					Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE).putInteger("trails_max_length", length);
					Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE).putBoolean("trails_active", ( length > 0 ) );
					Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE).flush();
				}
			}
		});
		
		//set where game must begin on start
		methodArray.put("game", new CallMethod() {
			@Override
			public void invoke(String... args) {
				int totalArgs = args.length;
				if ( totalArgs == 3) {
					// three arguments 
					if ( args[1].equals( "boot" ) || args[1].equals("go") && args[1].matches("\\w+") ) {
						
						//set game boot screen
						if ( !args[2].matches("^(normal|premium|title|menu|oldtutorial|tutorial|musictest|virustest|viruslab|fexplorer)$") ) {
							//don't match one of the screen options
							cmd.show("[" + args[2] + "] ...no valid game boot screen match this argument! " ); return;
						}
						String[] str = new String[] { "normal", "premium", "title", "menu", "oldtutorial", "tutorial", "musictest", "virustest", 
								"scenerytest", "viruslab", "fexplorer" };
						int i;
						for ( i = 0; i < str.length; i++ ) {
							if ( str[i].equals( args[2] ) ) 
								break;
						}
						
						if ( args[1].equals("go") ) {
							
							VirusByteGame.GAME.goTo( i );
							cmd.show( "Teleporting to " + str[i] + " screen" );
							
						} else {
							
							Preferences pref = Gdx.app.getPreferences( VirusByteGame.DEBUG_FILE );
							pref.putInteger("boot", i);
							pref.flush();
							cmd.show( "Game now will boot on " + str[i] + " screen" );
							
						}
						// ========== // 
					} else if ( args[1].equals( "active" ) && args[1].matches("\\w+") ) {
						//set game active or inactive
						if ( args[2].matches("^(true|false)$") ) {
							// set the value (true or false)
							boolean bool = Boolean.valueOf( args[2] );
							cmd.isGameScreenActiveOnLastConsoleCall = bool;
							cmd.show( ((bool)? "active":"inactive") + " mode is set, now you " + ((bool)?"will":"dont") + " suffer damage" );
							Preferences pref = Gdx.app.getPreferences( VirusByteGame.DEBUG_FILE );
							pref.putBoolean("active", bool);
							pref.flush(); //save on debug file
						} else {
							// invalid true or false argument
							cmd.show( args[2]+ "  ...invalid argument, must be true or false" );
						}
						// =========//
					} else if ( args[1].equals("clear") && args[2].equals("scores") ) {
						Preferences prefs = Gdx.app.getPreferences( VirusByteGame.CONFIG_FILE );
						String str = "best_score-";
						String[] modes = new String[] {str + "extreme_mode", str + "normal_mode"};
						for ( String mode : modes ) prefs.remove( mode );
						prefs.flush();
						cmd.show( "cleared all best scores stored!" );
					} else {
						// invalid, second argument
						cmd.show( args[1] +" ...invalid argument" );
						return;
					}
					
				} else {
					//invalid number of arguments 
					cmd.show( "invalid number of arguments!" ); return;
				}
				
			}
		});
		
		// set virus on virus test mode
		methodArray.put("spawner", new CallMethod() {
			@Override
			public void invoke(String... args) {
				VirusTestGameScreen gameScreen = ( VirusByteGame.GAME.getScreen().getClass().equals( VirusTestGameScreen.class ) )? (VirusTestGameScreen)VirusByteGame.GAME.getScreen()  : null;
				if ( gameScreen == null ) {
					cmd.show( "The spawner method can only be invoked on the virustest mode!" );
					return;
				}
				int totalArgs = args.length;
				if ( totalArgs >= 2 ) {
					String method = args[1];
					if ( method.matches("^set$") ) {
						//set spawner values
						if ( totalArgs != 4 ) {
							cmd.show( "invalide number of arguments " );
							return;
						}
						int value = 0;
						if ( args[3].matches("^[0-9]+$") ) {
							if ( !args[3].matches("^[0-9]{1,2}") ) {
								cmd.show( "["+args[3]+"] invalid number argument, can be max 2 digits value" );
								return;
							}
							value = Integer.valueOf( args[3] );
						} else {
							//not number argument 4
							cmd.show( "[" + args[3] + "] is not a integer value!" );
							return;
						}
						if ( args[2].matches("^interval$") ) {
							//set the interval between swarm calls
							gameScreen.spawnInterval = (float)value;
							cmd.show( "the inverval between virus spawning swarms is set to [" + value + "] seconds" );
						} else if ( args[2].matches("^limit$") ) {
							//set the limit of spawned virus on the stage
							gameScreen.spawnLimit = value;
							cmd.show( "total limit of spawned virus on the game stage is set to [" + value +"]" );
						} else if ( args[2].matches("^calls$") ) {
							//the number of virus calls in a swarm call
							gameScreen.swarmCall = value;
							cmd.show( "total virus to spawn in a swarm is set to [" + value + "]");
						}
					} else if ( method.matches("^add$") || method.matches("^remove$") ) {
						//Add or remove virus from the spawning list
						if ( totalArgs == 2 ) {
							// invalid number of arguments to add or remove
							cmd.show( ">"+method+"< empty arguments! Need at least one virus as argument to " + (method.matches("^add$") ?"add!" : "remove!") );
							return;
						} else if ( totalArgs == 3 && args[2].matches("^all$") ) {
							// remove all virus
							gameScreen.removeAllInstancesFromSpawningList();
							cmd.show( "removed all virus from the spawner!" );
							return;
						}
						boolean bool = true;
						String[] virusInstaces = new String[args.length-2];
						for (int i = 2; i < args.length; i++) {
							if ( !args[i].matches("[a-z]+") ) bool = false;
							virusInstaces[i-2] = args[i].replaceFirst("^\\w", args[i].toUpperCase().charAt(0)+"" ); // check if some virus name contains unsupported characters
						}
						if ( bool ) {
							// allowed characters checked
							// instance names converted first letter to uppercase
							Debug.log( Arrays.toString( virusInstaces ) );
							VirusInstance[] instanceList = new VirusInstance[virusInstaces.length];
							Field field;
							try {
								for ( int i = 0; i < virusInstaces.length; i++ ) {
									field = ClassReflection.getDeclaredField(VirusInstance.class, virusInstaces[i]);
									instanceList[i] = (VirusInstance)field.get( new Object() );
								}
								String finalMsg = ""; 
								for ( String instance : virusInstaces ) finalMsg += "["+instance + "] ";
								if ( method.matches("^add$") ) {
									// spawner add plague infector
									gameScreen.addInstancesToSpawningList(instanceList); // Add virus to the spawning list
									cmd.show( finalMsg + " added to the spawner." );
								} else if ( method.matches("^remove$") ) {
									// remove instances from the spawning list
									// spawner remove plague infector
									gameScreen.removeInstancesFromSpawningList(instanceList);
									cmd.show( finalMsg + " removed from the spawner." );
								}
							} catch (ReflectionException ex) {
								//when the virus doens't exists
								String stackMsg = "" + ex.getMessage();
								for (String string : virusInstaces) {
									//check the wrong virus name
									if ( stackMsg.contains(string) ) {
										stackMsg = "[" + string.toLowerCase() +  "] is an invalid virus";
										break;
									}
								}
								cmd.show( stackMsg );
								Debug.log( "stack message:" + ex.getMessage() );
							}
							Debug.log("" + Arrays.toString(instanceList) );
						}
					} else {
						cmd.show( "invalid arguments to >spawner< method" );
						return;
					}
				} 
			}
		});
		
		// Backgrounds control
		methodArray.put("animation", new CallMethod() {
			@Override
			public void invoke(String... args) {
				if ( !(VirusByteGame.GAME.getScreen() instanceof ChallengeGameMode) ){
					//when not inside the game stage
					cmd.show( "The " + args[0] + " method can't be invoked outside gameplay screen!" );
					return;
				}
				int totalArgs = args.length;
				if ( totalArgs >= 3 ) {
					Backgrounds back = VirusByteGame.BACK;
					boolean isSet = args[1].matches( "^set$" ), isRemove = args[1].matches("^remove$");
					if ( isSet || isRemove ) {
						//set the background with default configuration
						if ( args[2].matches( "^[a-z0-9]+$" ) ) {
							Field field;
							Animations animation;
							try {
								field = ClassReflection.getDeclaredField(Animations.class, args[2].replaceFirst("^\\w", args[2].toUpperCase().charAt(0)+"" ));
								animation = (Animations)field.get( new Object() );
								
								float
								animationSpeed = 1/15f,
								duration = 15f,
								hScroll = 0,
								vScroll = 0;
								boolean 
								fade = false;
								if ( isSet && totalArgs > 3) {
									String errorMsg = "";
									if ( totalArgs == 5 ) { // 5 arguments
										//Invalid arguments checking
										if ( !args[3].matches("^(?:[0-9]{0,1}\\.[0-9]{1,3}|[0-9]{1,2})$") ) errorMsg += "[" + args[3] + "] is a invalid argument value";
										if ( !args[4].matches("^(?:true|false)$") ) errorMsg += " , [" + args[4] + "] is invalid argument, can be only true or false";
										
										// set respective values
										if ( errorMsg.matches("") ) { // no errors
											duration = Float.valueOf( args[3] );
											fade = Boolean.valueOf( args[4] );
										}
									} else if ( totalArgs == 7 ) { // 7 arguments
										//Invalid arguments checking 
										if ( !args[3].matches("^(?:[0-9]{0,1}\\.[0-9]{1,3}|[0-9]{1,2})$") ) errorMsg += "[" + args[3] + "] is a invalid argument value";
										if ( !args[4].matches("^(?:[0-9]{0,1}\\.[0-9]{1,3}|[0-9]{1,2})$") ) errorMsg += " , [" + args[4] + "] is a invalid argument value";
										if ( !args[5].matches("^(?:[0-9]{0,1}\\.[0-9]{1,3}|[0-9]{1,2})$") ) errorMsg += " , [" + args[5] + "] is a invalid argument value";
										if ( !args[6].matches("^(?:true|false)$") ) errorMsg += " , [" + args[6] + "] is invalid argument, can be only true or false";
										
										// set respective values
										if ( errorMsg.matches("") ) { // no errors
											duration = Float.valueOf( args[3] );
											hScroll = Float.valueOf( args[4] );
											vScroll = Float.valueOf( args[5] );
											fade = Boolean.valueOf( args[6] );
										}
									} else if ( totalArgs == 8 ) { // arguments 
										//Invalid arguments checking 
										if ( !args[3].matches("^(?:[0-9]{0,1}\\.[0-9]{1,3}|[0-9]{1,2})$") ) errorMsg += "[" + args[3] + "] is a invalid argument value";
										if ( !args[4].matches("^(?:[0-9]{0,1}\\.[0-9]{1,3}|[0-9]{1,2})$") ) errorMsg += " , [" + args[4] + "] is a invalid argument value";
										if ( !args[5].matches("^(?:[0-9]{0,1}\\.[0-9]{1,3}|[0-9]{1,2})$") ) errorMsg += " , [" + args[5] + "] is a invalid argument value";
										if ( !args[6].matches("^(?:[0-9]{0,1}\\.[0-9]{1,3}|[0-9]{1,2})$") ) errorMsg += " , [" + args[6] + "] is a invalid argument value";
										if ( !args[7].matches("^(?:true|false)$") ) errorMsg += " , [" + args[7] + "] is invalid argument, can be only true or false";
										
										// set respective values
										if ( errorMsg.matches("") ) { // no errors
											animationSpeed = Float.valueOf( args[3] );
											duration = Float.valueOf( args[4] );
											hScroll = Float.valueOf( args[5] );
											vScroll = Float.valueOf( args[6] );
											fade = Boolean.valueOf( args[7] );
										}
									}
									
									if ( !errorMsg.matches("") ) {
										//invalid arguments error
										cmd.show( errorMsg );
										return;
									}
 								} else {
// 									if ( back.isActiveEspecialBackEffects() ) back.finalizeAllBackEffects();
 									if ( isSet ) {
 										back.setBackground( animation ); // Add virus to the spawning list
 										cmd.show( args[2] + " set as animation." );
 									} else if ( isRemove ) {
 										back.removeBackground( animation );
 										cmd.show( args[2] + " animation removed." );
 									}
 									return;
 								}
//								if ( back.isActiveEspecialBackEffects() ) back.finalizeAllBackEffects();
								back.setBackground(animation, animationSpeed, duration, hScroll, vScroll, 15, fade); // Add virus to the spawning list
								cmd.show( args[2] + " set as animation." );
							} catch (ReflectionException ex) {
								//when the virus doens't exists
								cmd.show( "[" + args[2] + "] is an invalid animation!" );
								Debug.log( "stack message:" + ex.getMessage() );
							}
							return;
						}
						
					} else if ( args[1].matches( "^active$" ) ) {
						// activate or deactivate background animations
						if ( args[2].matches( "^true|false$" ) ) {
							boolean bool = Boolean.valueOf(args[2]);
							Preferences prefs = Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE);
							prefs.putBoolean("animations-active", bool);
							prefs.flush();
							back.activeRandomBackground = bool;
							if ( !bool ) back.finalizeAllBackEffects();
							cmd.show( "Background animations " + ((bool)? "activated":"deactivated"));
							return;
						}
						cmd.show( "[" +args[2]+ "] invalid argument, must be [true] or [false]" );
						return;
					}
				}
				cmd.show( "Invalid number of arguments for " + args[0] +" method.");
			}
		});
		
		//Inventory
		methodArray.put("inventory", new CallMethod() {
			@Override
			public void invoke(String... args) {
				int totalArgs = args.length;
				
				if ( totalArgs == 4 || totalArgs == 3 ) {
					String itemName = args[2];
					String method = args[1];
					int number = 0;
					if ( totalArgs == 4 ) { // check if the fourth argument is a valid number
						if ( !args[3].matches( "^[\\d]{1,2}$" ) ) {
							cmd.show( "[" + args[3] + "] is an invalid number!" );
							return;
						} else {
							number = Integer.valueOf( args[3] );
						}
					}
					if ( itemName.matches( "^[a-z0-9]+$" ) ) {
						Field field;
						VirusInstance itemInstance;
						try {
							field = ClassReflection.getDeclaredField(VirusInstance.class, itemName.replaceFirst("^\\w", itemName.toUpperCase().charAt(0)+"" ));
							itemInstance = (VirusInstance)field.get( new Object() );
							boolean isItem = VirusByteGame.VIRUS_MANAGER.itemList.contains(itemInstance, true);
							if ( isItem ) { // check if it's a valid item instance object
								Inventory inventory = VirusByteGame.HUD.inventory;
								if ( method.equals("add") ) {
									HoldableType item = (HoldableType)VirusByteGame.VIRUS_MANAGER.obtainVirus(itemInstance, false);
									if ( inventory.addItem(0, item, true) ) {
										if ( --number > 0 ) {
											for (int i = 0; i < number; i++) {
												item = (HoldableType)VirusByteGame.VIRUS_MANAGER.obtainVirus(itemInstance, false);
												inventory.addItem(0, item, true);
											}
										}
										cmd.show( itemName + " item has been inserted!" );
										return;
									} else {
										cmd.show( "wont have a free slot to insert the item!" );
										return;
									}
								} else if ( method.equals("remove") ) {
									int totalRemoved = inventory.removeItems(itemInstance, (number <= 0) ? 1 : number);
									cmd.show( "removed " + totalRemoved + " [" + itemName + "] from inventory." );
									return;
								} else {
									cmd.show( "[" + method + "] is an invalid method!" );
									return;
								}
							}
						} catch (ReflectionException ex) {
							Debug.log( "stack message:" + ex.getMessage() );
						}
					}
					//when the item instance argument doens't exists
					cmd.show( "[" + itemName + "] is an invalid item argument!" );
					return;
				} else if ( totalArgs == 2 ) {
					String method = args[1];
					if ( method.equals("clear") ) {
						VirusByteGame.HUD.inventory.clearInventory();
						cmd.show( "all items removed from inventory." );
						return;
					}
				}
				
				cmd.show( "invalid number of arguments" );
			}
		});
		//Shop
		methodArray.put("shop", new CallMethod() {
			@Override
			public void invoke(String... args) {
				Screen currentScreen = VirusByteGame.GAME.getScreen();
				if ( !(currentScreen instanceof ChallengeGameMode) ) {
					cmd.show( "This method can be evoked only from the game stage screen." );
					return;
				}
				ChallengeGameMode gameScreen = (ChallengeGameMode) currentScreen;
				int totalArgs = args.length;
				if ( totalArgs > 1 ) {
					String method = args[1];
					if ( method.equals("money") && totalArgs == 3 ) {
						if ( args[2].matches("[\\d]{1,3}") ) {
							int value = Integer.valueOf( args[2] );
							VirusByteGame.HUD.inventory.setCash( value );
							cmd.show( "Cash set to [" + value + "$]" );
						} else {
							cmd.show( "invalid argument [" + args[2] + "] number" );
						}
						return;
					}
					else if ( method.equals("open") ) {
						//Open shop menu
						if ( gameScreen.hud.state != Status.PAUSED ) {
							gameScreen.pauseGame( MENU.SHOP );
							cmd.show( "Shop screen menu opened." );
						} else {
							cmd.show( "Can't open shop because a menu screen is already open." );
						}
						return;
					}
				}
				cmd.show( "invalid argument number" );
			}
		});
	}
	
	public void call(String string) {
		String[] args = string.split("\\s");
		Debug.log( Arrays.toString( args ) + " arguments of the method");
		if ( args == null || args.length == 0 ) return;
		if ( methodArray.containsKey(args[0]) ) methodArray.get(args[0]).invoke( args );
	}
}
