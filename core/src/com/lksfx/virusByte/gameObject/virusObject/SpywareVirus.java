package com.lksfx.virusByte.gameObject.virusObject;

import static com.lksfx.virusByte.gameControl.debug.Debug.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.Line2D;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameControl.debug.Debug.SHAPE;
import com.lksfx.virusByte.gameControl.inputs.InputTriggers;
import com.lksfx.virusByte.gameObject.VirusManager.VirusInstance;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class SpywareVirus extends VirusType {
	private int tap;
	private float tutoBarFill, circleSize = 0.5f;
	private boolean tutoBarDirection;
	private TextureRegion circle;
	
	private Animation anim_eyes;
	private float timeToWait = 2f, timeWaiting = 0f;
	private Line2D slash_line = new Line2D(0f, 0f, 0f, 0f);
	private Line2D collision_lineA = new Line2D(0f, 0f, 0f, 0f), collision_lineB = new Line2D(0f, 0f, 0f, 0f);
	private boolean visible = false;
	
	public SpywareVirus() {
		this(MathUtils.random(64, WORLD_WIDTH-64), MathUtils.random(WORLD_HEIGHT+640 , WORLD_HEIGHT+64));
	}
	
	public SpywareVirus(float x, float y) {
		this(x, y, 15, 80, 110, new Color(115/255f, 71/255f, 191/255f, 0.3f));
	}
	
	public SpywareVirus(float x, float y, int point, int minSpd, int maxSpd, Color trailColor) {
		super(x, y, point, minSpd, maxSpd, trailColor, true);
		if (TUTORIAL) {
			circle = VirusByteGame.ASSETS.getAssetManager().
					get(Assets.Atlas.iconsAtlas.path, TextureAtlas.class).findRegion("circle");
			tap = 1;
		}
		isMoneyBack = true;
		virus_id = VirusInstance.Spyware;
		comboInterval = 2f;
		setInputController( new InputTriggers() {
			@Override
			public boolean justTouched(Vector3 mousePos) {
				if ( isOver(mousePos) ) {
					if (!visible) {
						setCollisionMaskBounds(64f, 64f);
						setTouchMaskBounds(64f, 32f);
						isCollidable = true;
						visible = true;
					}
				}
				return false;
			}
			@Override
			public boolean draggedTouch(Vector3 mousePos) {
				if ( !visible ) return false;
				if ( !VirusByteGame.VIRUS_MANAGER.isDrawingSwipe ) return false;
				
				if ( VirusType.distanceTo(mousePos, getPosition()) < 100 ) {
					float lenght_x = Math.abs(mousePos.x - SLASH_ORIGIN.x);
					
					//Debug.log("distance to origin: " + length_to_slash_origin);
					if (lenght_x < 50) {
						// ==== //
						slash_line.setLine(SLASH_ORIGIN.x, SLASH_ORIGIN.y, SLASH.x, SLASH.y);
						
						if ( slash_line.intersectsLine(collision_lineA) && slash_line.intersectsLine(collision_lineB) ) {
							Debug.log("Slash line intersect worm " + Intersector.distanceSegmentPoint(SLASH_ORIGIN, SLASH, getPosition()));
							if ( !isOver(mousePos) ) {
								destroy(true, true);
								Debug.log("destroyed by a slash");
							}
						}
					}
					// ==== //
				}
				
				return false;
			}
			@Override
			public boolean releaseTouch(Vector3 mousePos) {
				return false;
			}
		} );
	}
	
	@Override
	public void setAtlas() {
		
		AssetManager assetManager = VirusByteGame.ASSETS.getAssetManager();
		
		AtlasRegion[] normal = new AtlasRegion[2], eyesAnimation = new AtlasRegion[2];
		normal[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("spyware1");
		normal[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("spyware2");		
		eyesAnimation[0] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("spyware1-eye");
		eyesAnimation[1] = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("spyware2-eye");
		
		AtlasRegion trail = assetManager.get(Assets.Atlas.virusAtlas.path, TextureAtlas.class).findRegion("spyware-trail");
		
		createAnim( normal, trail );
		anim_eyes = new Animation( anim_default.getFrameDuration(), eyesAnimation );
		
	}
	
	@Override
	public void update(float deltaTime) {
		super.update( deltaTime );
		
		float hh = y + 32;
		Rectangle sprite_bounds = spriteMask.getRectangle();
		collision_lineA.setLine(sprite_bounds.getX()+currentFrameWidth*.3f, hh,
				sprite_bounds.getX()+currentFrameWidth*.7f, hh);
		
		hh = y - 32f;
		collision_lineB.setLine(sprite_bounds.getX()+currentFrameWidth*.3f, hh,
				sprite_bounds.getX()+currentFrameWidth*.7f, hh);
	}
	
	@Override
	public void movementUpdate(float deltaTime) {
		if ( visible ) {
			if ( spriteColor.a < 1f ) 
				spriteColor.a += .05f;
			if ( isOnMove ) 
				isOnMove = false;
			timeWaiting += deltaTime;
			if (timeWaiting > timeToWait) {
				timeWaiting = 0f;
				visible = false;
				isCollidable = false;
				if ( !TUTORIAL || (TUTORIAL  && canBeFree) ) isOnMove = true;
				setCollisionMaskBounds(0, 0, 32f, 32f);
			}
			isVirusCollidable = true;		
		} else {
			if (spriteColor.a > .06f) spriteColor.a -= .05f;
			isVirusCollidable = false;
		}
	}
	
	private boolean debugLines = true;
	
	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		if (!isVisible) return;
		debug_drawings();
		if ( isAnimationSet() ) {
			if (isTrailOn && visible) {
				for (int i = trailList.size-1; i > 0; i--) {
					float inc = i * 0.075f;
					trailColor.a = inc > 0.5f? 0.5f : inc;
					batch.setColor(trailColor);
					batch.draw(trail, trailList.get(i).xx, trailList.get(i).yy, trailList.get(i).centerOriginX, trailList.get(i).centerOriginY,
							trailList.get(i).width, trailList.get(i).height, trailList.get(i).scaleX, trailList.get(i).scaleY, trailList.get(i).angle); //draw trail
				}
				trailColor.a = 0.3f;
			}
			batch.setColor(spriteColor);
			batch.draw(getAnimation().getKeyFrame(elapsedTime, true), spriteCenter.x, spriteCenter.y);
			batch.setColor(1f, 1f, 1f, 0.3f);
			batch.draw(anim_eyes.getKeyFrame(elapsedTime, true), spriteCenter.x, spriteCenter.y);
			batch.setColor(1f, 1f, 1f, 1f);
		}
		if ( debugLines ) {
			// the horizontal line
			Debug.debug.insertShapeToRender(SHAPE.LINE, collision_lineA.start.x, collision_lineA.start.y, collision_lineA.end.x, collision_lineA.end.y, Color.CYAN);
			Debug.debug.insertShapeToRender(SHAPE.LINE, collision_lineB.start.x, collision_lineB.start.y, collision_lineB.end.x, collision_lineB.end.y, Color.CYAN);
		}
		if ( TUTORIAL ) {
			//in tutorial mode
			if (!visible) {
				if (circleSize > 1.5f) circleSize = .5f;
				circleSize += .05f;
				batch.setColor(1f, 1f, 1f, 0.65f);
				batch.draw(circle, spriteCenter.x +( (currentFrameWidth*.5f) - 16 ), spriteCenter.y +( (currentFrameHeight*.5f) - 16 ), 16f, 16f, 32f, 32f, circleSize, circleSize, 0f);
				batch.setColor(1f, 1f, 1f, 1f);
				tutoBarFill = 0f;
				String str = "tap: " + (tap);
				debug.screen(str, (int)(spriteCenter.x - ( (Debug.debug.font.getBounds(str).width - currentFrameWidth) *.5f)), (int)spriteCenter.y - 30, false);
			} else {
				batch.end();
				
				if (tutoBarFill > 128f) tutoBarFill = 0f;
				tutoBarFill += 2;
				Gdx.gl.glEnable(GL20.GL_BLEND);
				shapeRender.setColor(1f, 1f, 1f, .6f);
				shapeRender.begin(ShapeRenderer.ShapeType.Filled);
				float fill = Math.min(tutoBarFill, 64f);
				shapeRender.rect(x-7, y + (tutoBarDirection ? -32f : +32), 14f, tutoBarDirection ? fill : -fill);
				shapeRender.end();
				shapeRender.setColor(1f, 1f, 1f, 1f);
				Gdx.gl.glDisable(GL20.GL_BLEND);
				String str = "cut!";
				debug.screen(str, (int)(spriteCenter.x - ( (Debug.debug.font.getBounds(str).width - currentFrameWidth)*.5f)), (int)spriteCenter.y - 30, false);
				
				batch.begin();
			}
		}
	}
	
	@Override
	public void resetInPosition(float posX, float posY) {
		super.resetInPosition(posX, posY);
		setCollisionMaskBounds(32f, 32f);
		setTouchMaskBounds(40f, 40f);
		spriteColor.a = .05f;
	}
	
	@Override
	public void reset() {
		super.reset();
		if (TUTORIAL) tutoBarDirection = MathUtils.randomBoolean();
		isOnMove = true;
		visible = false;
	}
}
