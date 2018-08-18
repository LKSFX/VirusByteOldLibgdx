package com.lksfx.virusByte.gameControl.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.screens.ChallengeGameMode;

public class Console implements Disposable {
	
	public enum ConsoleState {ACTIVE, HIDE};
	public ConsoleState currentState;
	
	private Invoker invoker;
	private Stage stage;
	private Table table;
	private Skin skin;
	public Container<TextArea> textDisplayArea;
	private TextField field;
	
	public Console(Viewport viewport, SpriteBatch batch) {
		currentState = ConsoleState.HIDE;
		stage = new Stage(viewport, batch);
		//create the console interface
		table = new Table();
		skin = new Skin( Gdx.files.internal("data/ui/newSkin.json") );
		table.setBackground( skin.newDrawable("tinted-white", 0f, 0f, 0f, .8f) );
		stage.addActor(table);
		setConsoleTable();
		invoker = new Invoker( this );
	}
	
	public void update() {
		switch ( currentState ) {
		case ACTIVE:
			stage.act();
			stage.draw();
			break;
		case HIDE:
			break;
		default:
			break;
		}
	}
	
	/**store the state of the gameScreen active field before last console call*/
	protected boolean isGameScreenActiveOnLastConsoleCall;
	
	/**Open the console*/
	public void call() {
		currentState = ConsoleState.ACTIVE;
		isGameScreenActiveOnLastConsoleCall = ChallengeGameMode.ACTIVE;
		ChallengeGameMode.ACTIVE = false;
		VirusType.ACTIVE = false;
		VirusByteGame.addProcessor( stage );
	}
	
	public void hide() {
		currentState = ConsoleState.HIDE;
		ChallengeGameMode.ACTIVE = isGameScreenActiveOnLastConsoleCall; //reset to default state, that was before console call
		VirusType.ACTIVE = isGameScreenActiveOnLastConsoleCall;
		VirusByteGame.MAIN_MULTIPLEX_CONTROLLER.removeProcessor(stage);
	}
	
	public void show(String string) {
		TextArea area = textDisplayArea.getActor();
		int totalLines = area.getLines();
		area.setText( ">: " + string + ( (area.getText().matches("")) ? "" : "\n" + area.getText()) );
		Debug.log("total lines in text display area " + totalLines + " | lines showing " + area.getLinesShowing());
		//Debug.log("last index of breakLine " + area.getText().lastIndexOf('\n') );
		if ( totalLines > 50 ) area.setText( area.getText().substring(0, area.getText().lastIndexOf('\n')) );
		textPane.setScrollY(0);
	}
	
	private void setConsoleTable() {
//		table.debug();
		table.setFillParent(true);
		table.pad(10f);
		table.align(2);
		
		setConsoleField();
	}
	
	private void setConsoleField() {
		TextFieldStyle fieldStyle = new TextFieldStyle(skin.getFont("consolas18-bold"), Color.WHITE, skin.getDrawable("tinted-thin"), 
				skin.newDrawable("tinted-thin", Color.BLUE), skin.newDrawable("tinted-white", 14/255f, 14/255f, 14/255f, 1f));
		Table fieldTable = new Table();
//		fieldTable.debug();
		fieldTable.setBackground( skin.getDrawable("cmd-field") );
		fieldTable.align(Align.left);
		fieldTable.pad(2f).padLeft(5f).padRight(5f);
		field = new TextField("", fieldStyle);
		
		Cell<Table> cell = table.add(fieldTable).padTop(10f).center();
		fieldTable.add( field ).left().expandX().fillX().padRight(5f);
		field.setName("cmd text field");
		field.setTextFieldListener( new TextField.TextFieldListener() {
			@Override
			public void keyTyped(TextField textField, char c) {
				if ( c == '\r' || c == '\n' ) {
					if ( field.getText().matches("[\\w\\s\\\\.]+") ) invoker.call( field.getText() );//show( field.getText() );
				}
			}
		});
		Value stageWidth = new Value.Fixed(stage.getWidth() - 25f);
		cell.prefWidth( stageWidth );
		
		ImageButton execButton = new ImageButton( skin.getDrawable("cmd-button") );
		execButton.setName("execute button");
		execButton.addListener( new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if ( field.getText().matches("[\\w\\s\\\\.]+") ) invoker.call( field.getText() );//show( field.getText() );
				event.handle();
				Debug.log( "execute button pressed!" );
			}
		} );
		fieldTable.add( execButton );
		fieldTable.setBounds(0, 0, cell.getPrefWidth(), cell.getPrefHeight());
		
		//Debug.log("text field cell is " + cell.getPrefWidth() + " width | " + cell.getMinHeight() + " height");
		//Debug.log("table is " + table.getPadLeft() + " pad width | " + table.getPadRight() + " height");
		
		setConsoleDisplay(fieldStyle, stageWidth);
	}
	
	private ScrollPane textPane;
	
	private void setConsoleDisplay(TextFieldStyle style, Value tableWidth) {
		style.background = null;
		textDisplayArea = new Container<TextArea>( new TextArea("", style) );
		textDisplayArea.getActor().setDisabled(true);
		table.row();
		textPane = new ScrollPane( textDisplayArea );
		textPane.setScrollingDisabled(true, false);
		
		table.add( textPane ).padTop(10f).padBottom(10f).prefWidth( tableWidth ).fill();
		textDisplayArea.prefSize( tableWidth, new Value.Fixed(1000f) );
		textDisplayArea.getActor().setTouchable( Touchable.disabled );
		Debug.log("pane cell height: " + table.getCell( textPane ).getPrefHeight() );
	}
	
	@Override
	public void dispose() {
		stage.dispose();
		skin.dispose();
	}
	
}

