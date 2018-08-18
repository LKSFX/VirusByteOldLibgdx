package com.lksfx.virusByte.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
	
public class MyButton extends TextButton {
	private ShapeRenderer renderer;
	private Color black = new Color(0f, 0f, 0f, 0.7f), activeColor; 
	private boolean shadowActive;
	
	public MyButton(String name, Skin skin) {
		this(name, skin.get(TextButtonStyle.class), 250f, 75f, true, Color.ORANGE);
	}
	
	public MyButton(String name, Skin skin, String styleName, Color color) {
		this(name, skin.get(styleName, TextButtonStyle.class), 250f, 75f, true, color);
	}
	
	public MyButton(String name, Skin skin, String styleName, Color color, boolean shadow) {
		this(name, skin.get(styleName, TextButtonStyle.class), 250f, 75f, shadow, color);
	}
	
	public MyButton(String name, Skin skin, String styleName) {
		this(name, skin.get(styleName, TextButtonStyle.class), 250f, 75f, true, Color.ORANGE);
	}
	
	public MyButton(String name, Skin skin, String styleName, float width, float height, boolean shadow, Color color) {
		this(name, skin.get(styleName, TextButtonStyle.class), width, height, shadow, color);
	}
	
	public MyButton(String text, TextButtonStyle style, float width, float height, boolean shadow, Color color) {
		super(text, style);
		
		addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				//button.setText("You clicked the button");
				setPosition(getX(), getY()-5);
				return true;
			}
			@Override
		 	public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				setPosition(getX(), getY()+5);
			}
			
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				//button1.setPosition(button1.getX(), button1.getY()+5);
			}
		});
		getCells().get(0).prefWidth(width).minHeight(height);
		
		activeColor = color;
		shadowActive = shadow;
		renderer = new ShapeRenderer();
		setName(text);
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
	    if (shadowActive) {
	    batch.end();
	    
	    renderer.setProjectionMatrix(batch.getProjectionMatrix());
	    renderer.setTransformMatrix(batch.getTransformMatrix());
	    renderer.translate(getX(), getY(), 0);
	    
	    //draw back shadow
	    Gdx.gl.glEnable(GL20.GL_BLEND);
	    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	    renderer.begin(ShapeRenderer.ShapeType.Filled);
	    renderer.setColor(isPressed() ? activeColor : black);
	    renderer.rect(0, isPressed() ? 0 : -5, getWidth(), getHeight());
	    renderer.end();
	    Gdx.gl.glDisable(GL20.GL_BLEND);
	    
	    batch.begin();
	    }
	    super.draw(batch, parentAlpha);
	}
}
