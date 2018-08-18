package com.lksfx.virusByte.menus;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public abstract class Menu implements Disposable {
	
	private Array<Table> tableList;
	protected Stage stage;
	protected Skin skin;
	
	public Menu(Stage stage, Skin skin) {
		this.stage = stage;
		this.skin = skin;
		tableList = new Array<Table>();
		constructLayout();
	}
	
	/**Construct the layout for this menu*/
	public abstract void constructLayout();
	
	/**Open the menu, add the layout on top of the stage*/
	public void open() {
		for ( Table table : tableList ) {
			stage.addActor( table );
		}
	}
	
	/**Close the menu, remove the layout components from the stage*/
	public void close() {
		for (Table table : tableList) {
			table.remove();
		}
	}
	
	/**Insert this table to render*/
	protected final void addTable( Table table ) {
		tableList.add( table );
	}
	
	public void dispose() {}
}
