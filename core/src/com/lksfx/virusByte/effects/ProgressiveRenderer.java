package com.lksfx.virusByte.effects;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.lksfx.virusByte.gameControl.debug.Debug;

public class ProgressiveRenderer extends BackgroundRenderer {

	public ProgressiveRenderer(Sprite sprite, float scrollSpeed, float duration,
			int depth, boolean fade) {
		super(sprite, scrollSpeed, duration, depth, fade);
		// TODO Auto-generated constructor stub
	}

	public ProgressiveRenderer(String[] paths, Vector2 scrollSpeed,
			Vector2 duration, boolean loadNow) {
		super(paths, scrollSpeed, duration, loadNow);
		// TODO Auto-generated constructor stub
	}

	public ProgressiveRenderer(String[] paths, Vector2 scrollSpeedRange,
			Vector2 durationRange, RendererConfig config, boolean loadNow) {
		super(paths, scrollSpeedRange, durationRange, config, loadNow);
		// TODO Auto-generated constructor stub
	}

	public ProgressiveRenderer(String[] paths, Vector2 scrollSpeedRange,
			Vector2 durationRange, int depth, boolean fade, boolean loadNow) {
		super(paths, scrollSpeedRange, durationRange, depth, fade, loadNow);
		// TODO Auto-generated constructor stub
	}

	public ProgressiveRenderer(String[] paths, Vector2 scrollSpeed, Vector2 duration, 
			int depth, boolean fade, RendererConfig config, boolean loadNow) {
		super(paths, scrollSpeed, duration, depth, fade, config, loadNow);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void set(float hScrollSpeed, float vScrollSpeed, float duration,
			int depth, boolean fade) {
		super.set(hScrollSpeed, vScrollSpeed, duration, depth, fade);
		Debug.log( "reseted progressive renderer" );
	}
	
	@Override
	public void initialize(String[] paths, float hScrollSpeed, float vScrollSpeed, float duration, 
			int depth, boolean fade, RendererConfig config, boolean loadNow) {
		super.initialize(paths, hScrollSpeed, vScrollSpeed, duration, depth, fade, config, loadNow);
		progressiveAlphaOperator = .020f;
	}
	
	private float alphaA, alphaB, timeA, timeB, progressiveAlphaOperator;
	private float minAlpha = .5f;
	private Wave stateA = Wave.STATIC, stateB = Wave.STATIC;
	private enum Wave {INC, DEC, STATIC}
	
	@Override
	public void draw(SpriteBatch batch, float deltaTime) {
		if ( isLoaded ) {
			elapsedTime += deltaTime;
			// when the texture is already loaded
			// the texture is draw on screen
			if ( end ) {
				// when the background render time end, fade out alpha
				if ( fade ) { // fade out active
					alpha -= alphaOperator;
					if ( alpha < 0 ) alpha = 0f;
					if ( alpha == 0f && alphaA == 0f && alphaB == 0f ) finalize();
				} else {
					visible = false;
					finalize();
				}
			} else {
				// when this animation has not ended yet
				if ( alpha < alphaMeta ) {
					// slowly increase the alpha of this sprite 
					if ( fade ) {
						alpha += alphaOperator;
					} else {
						if ( alphaMeta != 0 ) {
							alpha = alphaMeta;
						}
					}						
				}
				if (duration > 0 && timer > duration) end = true;
			}
			
			// current rendering
			minAlpha = alpha *.3f;
			float frameTime = animation.getFrameDuration();
			float operator = progressiveAlphaOperator;
			// A
			switch ( stateA ) {
			case INC:
				if ( (alphaA += operator) >= alpha ) {
					alphaA = alpha; //no more than 1
					stateA = Wave.DEC;
					if ( stateB == Wave.STATIC ) stateB = Wave.INC; // start increment B alpha
				}
				break;
			case DEC:
				if ( (alphaA -= (operator+(operator*.25f))) <=  minAlpha ) {
					alphaA = minAlpha; //no less than zero
					timeA += frameTime; //increment B time
					stateA = Wave.STATIC;
				}
				break;
			default:
				break;
			}
			// B
			switch ( stateB ) {
			case INC:
				if ( (alphaB += operator) >= alpha ) {
					alphaB = alpha; //no more than 1
					stateB = Wave.DEC;
					timeB = timeA+frameTime; //increment A time
					if ( stateA == Wave.STATIC ) stateA = Wave.INC; //start increment A alpha
				}
				break;
			case DEC:
				if ( (alphaB -= (operator+(operator*.25f))) <= minAlpha ) {
					alphaB = minAlpha; //no less than zero
					stateB = Wave.STATIC;
				}
				break;
			default:
				break;
			}
			
			if ( stateA == Wave.STATIC && stateB == Wave.STATIC ) { //When both states A and B are static restart cycle
				stateA = Wave.INC;
//				Debug.log( "both states are STATIC. initialing timeA " );
			}
			
			/*Debug.log( "TimeA: " + timeA + " | TimeB: " + timeB + " | frameDuration: " + frameTime);
			Debug.log( "Animation life: " + timer );
			Debug.log( "progressiveOperator: " + progressiveAlphaOperator );*/
			
			if ( visible ) {
				// A
				/*sprite.setTime(timeA);
				sprite.setAlpha(alphaA);
				sprite.draw(batch);*/
				color.a = alphaA;
				batch.setColor( color );
				TextureRegion region = animation.getKeyFrame(timeA);
				region.setRegionWidth( width );
				region.setRegionHeight( height );
				batch.draw(region, x, y);
				/*batch.draw(, x, y, 0f, 0f, width, height, 
						1f, 1f, 0f, 0, 0, width, height, false, false);*/
				// B
				/*sprite.setTime(timeB);
				sprite.setAlpha(alphaB);
				sprite.draw(batch);*/
				color.a = alphaB;
				batch.setColor( color );
				region = animation.getKeyFrame(timeB);
				region.setRegionWidth( width );
				region.setRegionHeight( height );
				batch.draw(region, x, y);
				/*batch.draw(animation.getKeyFrame(timeA).getTexture(), x, y, 0f, 0f, width, height, 
						1f, 1f, 0f, 0, 0, width, height, false, false);*/
				batch.setColor(1, 1, 1, 1);
			}
			
			if (scrollSpeedV != 0) {
//				if ( descriptors != null ) Debug.log("scroll V speed " + scrollSpeedV);
				scrollTimerV += scrollSpeedV*deltaTime;
				for (TextureRegion region : animation.getKeyFrames()) {
					region.setV(scrollTimerV);
					region.setV2(scrollTimerV+lengthV);
				}
				/*animation.getKeyFrame(elapsedTime).setV(scrollTimerV);
				animation.getKeyFrame(elapsedTime).setV2(scrollTimerV+scrollVCompensation+lengthV);*/
			}
			// horizontal texture scroll
			if (scrollSpeedU != 0) {
//				if ( descriptors != null ) Debug.log("scroll U speed " + scrollSpeedU);
				scrollTimerU += scrollSpeedU*deltaTime;
				for (TextureRegion region : animation.getKeyFrames()) {
					region.setV(scrollTimerU);
					region.setV2(scrollTimerU+lengthU);
				}
				/*animation.getKeyFrame(elapsedTime).setU(scrollTimerU);
				animation.getKeyFrame(elapsedTime).setU2(scrollTimerU+scrollVCompensation+lengthU);*/
			}
			
			timer += deltaTime;
		}
	}
	
	@Override
	public BackgroundRenderer reset() {
		stateA = Wave.STATIC;
		stateB = Wave.STATIC;
		alphaA = 0f;
		alphaB = 0f;
		timeA = 0f;
		timeB = 0f;
		return super.reset();
	}
}
