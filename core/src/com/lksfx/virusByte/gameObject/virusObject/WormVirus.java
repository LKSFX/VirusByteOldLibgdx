package com.lksfx.virusByte.gameObject.virusObject;

import static com.lksfx.virusByte.gameControl.debug.Debug.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.effects.Backgrounds;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.Line2D;
import com.lksfx.virusByte.gameControl.MyUtils;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.debug.Debug.SHAPE;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;
import com.lksfx.virusByte.gameObject.pontuation.PointLog;
import com.lksfx.virusByte.gameObject.pontuation.ScreenLogger;

public class WormVirus extends VirusType {
	private Animation anim_on, anim_off;
	private float switchTime = 0.6f, stateTime = switchTime, tutoBarFill;
	private Line2D slash_line = new Line2D(0f, 0f, 0f, 0f);
	private Line2D collision_lineA = new Line2D(0f, 0f, 0f, 0f), collision_lineB = new Line2D(0f, 0f, 0f, 0f);
	public boolean damageOnSlash;
	private boolean active, tutoBarDirection, isPressed;
	private Vector3 mousePos;
	
	public WormVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}
	
	public WormVirus(float x, float y) {
		this(x, y, 13, 100, 140, new Color( Color.YELLOW ), true);
	}
	
	public WormVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor, boolean trailOn) {
		super(x, y, point, minSpd, maxSpd, trailColor, trailOn);
		isMoneyBack = true;
		virus_id = VirusInstance.Worm;
		switchTime = MyUtils.choose(/*0.3f, 0.4f,*/ 0.5f, 0.6f, 0.7f, 0.8f, 0.9f);
		if (TUTORIAL) switchTime = 1.2f;
		comboInterval = 2f;  
		setInputController( new InputTriggers() {
			@Override
			public boolean pressedTouch(Vector3 mousePos) {
				WormVirus.this.mousePos = mousePos;
				isPressed = true;
				return super.pressedTouch(mousePos);
			}
			@Override
			public boolean draggedTouch(Vector3 mousePos) {
				if ( !VirusByteGame.VIRUS_MANAGER.isDrawingSwipe ) return false;
				
				if ( VirusType.distanceTo(mousePos, getPosition()) < 100 ) {
					// === //
					float lenght_y  = Math.abs(mousePos.y - SLASH_ORIGIN.y);
					
					//Debug.log("distance to origin: " + length_to_slash_origin);
					if (lenght_y < 100) {
						slash_line.setLine(SLASH_ORIGIN.x, SLASH_ORIGIN.y, SLASH.x, SLASH.y);
						
						if ( slash_line.intersectsLine(collision_lineA)  && slash_line.intersectsLine(collision_lineB) ) {
							Debug.log("Slash line intersect worm " + Intersector.distanceSegmentPoint(SLASH_ORIGIN, SLASH, getPosition()));
							destroy(true, true);
						}
					}
					// === //
				}
				return false;
			}
			@Override
			public boolean releaseTouch(Vector3 mousePos) {
				isPressed = false;
				damageOnSlash = false;
				return false;
			}
		} );
		//move = false;
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("worm1-off");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("worm2-off");		
		
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("worm-trail");	
		createAnim(normal, trail);
		anim_off = anim_default;
		
		Array<AtlasRegion> keyFrames = new Array<AtlasRegion>();
		keyFrames.add(assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("worm1-on"));
		keyFrames.add(assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("worm2-on"));
		
		setCollisionMaskBounds( 24f, currentFrameHeight );
		setTouchMaskBounds( 24f, currentFrameHeight );
		anim_on = new Animation( 1/15f, keyFrames );
		anim_on.setPlayMode( PlayMode.LOOP );
		
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		stateTime -= deltaTime;
		if ( stateTime <= 0f ) {
			active = !active;
			setAnimation( active ? anim_on : anim_off );
			stateTime = switchTime; //reset timer
		}
		
		// When finger or button pressed
		if ( isPressed ) {
			if ( active && !HOLDING_VIRUS ) {
				// ==== //
				if ( isOver(mousePos) ) {
					if ( VirusByteGame.SFX ) {
						// AUDIO TODO
					}
					VirusByteGame.BACK.addForeground(Backgrounds.Effect.Damage, 1f, 5);
					if (ACTIVE && !TUTORIAL) LIFES--;
					damageOnSlash = true;
				}
				// ==== //
			}
		}
		
		float halfw = currentFrameWidth * .35f;
		Rectangle sprite_bounds = spriteMask.getRectangle();
		collision_lineA.setLine(x - halfw, sprite_bounds.getY()+currentFrameHeight*.1f, x - halfw, sprite_bounds.getY()+currentFrameHeight*.9f);
		collision_lineB.setLine(x + halfw, sprite_bounds.getY()+currentFrameHeight*.1f, x + halfw, sprite_bounds.getY()+currentFrameHeight*.9f);
	}
	
	public void draw(SpriteBatch batch, float deltaTime) {
		super.draw(batch, deltaTime);
		
		if ( TUTORIAL ) {
			batch.end();
			if (tutoBarFill > 128f) tutoBarFill = 0f;
			tutoBarFill += 2;
			if (active) {
				tutoBarFill = 0f;
			} else {
				String str = "cut!";
				debug.screen(str, (int)(spriteCenter.x - ( (Debug.debug.font.getBounds(str).width - currentFrameWidth)*.5f)), (int)spriteCenter.y - 30, false);
			}
			Gdx.gl.glEnable(GL20.GL_BLEND);
			shapeRender.setColor(1f, 1f, 1f, .6f);
			shapeRender.begin(ShapeRenderer.ShapeType.Filled);
			float fill = Math.min(tutoBarFill, 64f);
			shapeRender.rect(x + (tutoBarDirection ? -32f : +32), y, tutoBarDirection ? fill : -fill, 14f);
			shapeRender.end();
			shapeRender.setColor(1f, 1f, 1f, 1f);
			Gdx.gl.glDisable(GL20.GL_BLEND);
			batch.begin();
		}
		
	}
	
	private boolean debugLines = true;
	
	@Override
	public void debug_drawings() {
		super.debug_drawings();
		if ( debugLines ) {
			// The vertical line
			Debug.debug.insertShapeToRender(SHAPE.LINE, collision_lineA.start.x, collision_lineA.start.y, collision_lineA.end.x, collision_lineA.end.y, Color.CYAN);
			Debug.debug.insertShapeToRender(SHAPE.LINE, collision_lineB.start.x, collision_lineB.start.y, collision_lineB.end.x, collision_lineB.end.y, Color.CYAN);
		}
	}
	
	/*------------------------------------------------*/
	/*@Override
	public boolean pressedTouch(Vector3 mousePos) {
		this.mousePos = mousePos;
		isPressed = true;
		return super.pressedTouch(mousePos);
	}
	
	@Override
	public boolean draggedTouch(Vector3 mousePos) {
		if ( !VirusByteGame.VIRUS_MANAGER.isDrawingSwipe ) return false;
		
		if ( VirusType.distanceTo(mousePos, getPosition()) < 100 ) {
			// === //
			float lenght_y  = Math.abs(mousePos.y - SLASH_ORIGIN.y);
			
			//Debug.log("distance to origin: " + length_to_slash_origin);
			if (lenght_y < 100) {
				slash_line.setLine(SLASH_ORIGIN.x, SLASH_ORIGIN.y, SLASH.x, SLASH.y);
				
				if ( slash_line.intersectsLine(collision_lineA)  && slash_line.intersectsLine(collision_lineB) ) {
					Debug.log("Slash line intersect worm " + Intersector.distanceSegmentPoint(SLASH_ORIGIN, SLASH, getPosition()));
					destroy(true, true);
				}
			}
			// === //
		}
		return false;
	}
	
	@Override
	public boolean releaseTouch(Vector3 mousePos) {
		isPressed = false;
		damageOnSlash = false;
		return false;
	}*/
	/*------------------------------------------------*/
	
	public ScreenLogger getPointLog() {
		if ( TUTORIAL ) {
			String[] string;
			if (damageOnSlash) {
				string = new String[] {"No!", "bad", "wrong"};
			} else {
				string = new String[] {"great", "all right", "right"};
			}
			if ( !hit ) return null;
			ScreenLogger log = VirusByteGame.POINT_MANAGER.screenLoggerPool.obtain();
			return log.set(string[MathUtils.random(string.length-1)], x, y);
		}
		PointLog pointLog = VirusByteGame.POINT_MANAGER.pointLogPool.obtain();
		return pointLog.set(hit ? point : point/2, x, y, true, virus_id, hit, comboInterval);
	}
	
	@Override
	public void reset() {
		/*Debug.log("collision mask relative offset position " + collision_mask_offset.toString());
		Debug.log("collision mask position X " + collision_mask_bounds.getX() + " | Y " + collision_mask_bounds.getY());*/
		super.reset();
		switchTime = MyUtils.choose(/*0.3f, 0.4f, 0.5f, 0.6f,*/ 0.7f, 0.8f, 0.9f);
		if (TUTORIAL) {
			tutoBarDirection = MathUtils.randomBoolean();
			switchTime = 1.2f;
		}
		damageOnSlash = false;
		isPressed = false;
	}
	
	private VirusType nearestBroVirus;
	
	@Override
	public void position_resolver() {
		if (!repositionable || ( y+(currentFrameHeight*.5f)) < WORLD_HEIGHT ) return;
		Debug.log("resolver appyed to worm virus " + currentFrameHeight*.5f);
		float distance_tolerance = 200f;
//		if (nearestVirus == null) return;
		boolean free = false;
		while (!free) {
			nearestVirus = nearest_virus();
			nearestBroVirus = nearest_virus(this.getClass());
			if (nearestBroVirus != null) {
				if (distanceTo(x, nearestBroVirus.y, x, y) < distance_tolerance) {
					if (speed < nearestBroVirus.speed) {
						moveAdd(0f, distance_tolerance);
					} else {
						nearestBroVirus.moveAdd(0f, distance_tolerance);
					}
//					nearestBroVirus = nearest_virus(this.getClass());
					continue;
				}
			}
			if (nearestVirus != null) {
				if ( distanceTo(nearestVirus.getPosition(), getPosition()) < 60 ) {
					if (MathUtils.isEqual(nearestVirus.speed, speed, 25)) {
						moveAdd(currentFrameWidth+32f, 32f);
						if ( x > WORLD_WIDTH-64f ) x = 64f;
					} else {
						if (speed < nearestVirus.speed) {
							moveAdd(0, currentFrameHeight+32f);
						} else {
							nearestVirus.moveAdd(0, currentFrameHeight+32f);
						}
					}
//					nearestVirus = nearest_virus();
					continue;
				}
			}
			free = true;
		}
	}

}
