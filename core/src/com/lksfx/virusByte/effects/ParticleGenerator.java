package com.lksfx.virusByte.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.ReflectionPool;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.VirusByteGame.GraphicsQuality;
import com.lksfx.virusByte.gameControl.Assets;
import com.lksfx.virusByte.gameControl.debug.Debug;
import com.lksfx.virusByte.gameObject.abstractTypes.GameObject;

public class ParticleGenerator {
	private boolean debug;
	private boolean fireworksActive = true;
	private ReflectionPool<EffectDrawOnStage> drawablePool;
	public int maxSparks = 300, minSparks = 150;
	public ParticleEffectPool fireworks;
	public ParticleEffectPool explosions;
	public ParticleEffectPool healingRing;
	public Array<EffectDrawOnStage> fireList;
	
	public ParticleGenerator() {
		AssetManager manager = VirusByteGame.ASSETS.getAssetManager();
		
		ParticleEffect eff = new ParticleEffect( manager.get(Assets.Particles.firework.path, ParticleEffect.class) ); //effect create
		ParticleEffect eff2 = new ParticleEffect( manager.get(Assets.Particles.explosion.path, ParticleEffect.class) );
		ParticleEffect eff3 = new ParticleEffect( manager.get(Assets.Particles.healingRing.path, ParticleEffect.class) );
		
		drawablePool = new ReflectionPool<EffectDrawOnStage>( EffectDrawOnStage.class );
		fireworks = new ParticleEffectPool(eff, 20, 40); //fireworks particles pool
		explosions = new ParticleEffectPool(eff2, 4, 8); //explosion particle pool
		healingRing = new ParticleEffectPool(eff3, 4, 8); //explosion particle pool
		fireList = new Array<EffectDrawOnStage>();
		
		maxSparks = Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE).getInteger("particle_max_sparks", 300);
		minSparks = Gdx.app.getPreferences(VirusByteGame.DEBUG_FILE).getInteger("particle_min_sparks", 150);
		
		setGraphicsQuality( VirusByteGame.GRAPHIC_QUALITY );
		
	}
	
	/*public void show( SpriteBatch batch, float delta ) {
		
		if (Gdx.input.justTouched()) { //create a new fire on touch
			createEffect(mousePos.x, mousePos.y);
		}
		
		for ( EffectDrawOnStage fire : fireList ) {//update animation
			if ( fire.isComplete() ) { //effect is completed
				drawablePool.free( fire );
				VirusByteGame.GAME_ENGINE.removeGameObject( fire );
				fireList.removeValue( fire, true );
			} else {
				fire.draw(batch, delta); //draw active effect
			}
		}
		
		Debug.log( "particle generator show!" );
		//debug();
	}*/

	public void createEffect(Assets.Particles type, int x, int y, int depth) {
		createEffect(type, (float)x, (float)y, depth);
	}
	
	public void createEffect(Assets.Particles type, float x, float y, int depth) {
		createEffect(type, x, y, 1f, depth);
	}
	
	public void createEffect(Assets.Particles type, float x, float y, float scale, int depth) {
		createEffect(type, x, y, scale, null, depth);
	}
	
	/**Create effect of a type in a position of the stage with scale and color
	 * @param depth TODO*/
	public void createEffect(Assets.Particles type, float x, float y, float scale, Color color, int depth) {
		PooledEffect effect = getEffectByType(type, scale, color);
		//if null, cancel the effect
		if ( effect == null )  
			return;
		
		effect.setPosition(x, y);
		effect.allowCompletion();
		effect.start();
		
		EffectDrawOnStage fire = drawablePool.obtain();
		fire.initialize(drawablePool, fireList);
		
		fireList.add( fire.set(effect, depth) );
		VirusByteGame.GAME_ENGINE.insertGameObject( fire );
	}
	
/*	private void debug() {
		log("firework used: " + fireList.size + " | free to use: " + fireworks.getFree()
				+ " | peak of use: " + fireworks.peak + "/" + fireworks.max);
	}*/
	
	private PooledEffect getEffectByType(Assets.Particles type, float scale, Color color) {
		if (type == null) return null;
		PooledEffect eff = null;
		
		if ( type.equals( Assets.Particles.firework ) ) {
			if ( !fireworksActive ) return null;
			eff = fireworks.obtain();
			setFireworkEmmiter( eff.getEmitters(), color );
		} else if ( type.equals( Assets.Particles.explosion) ) {
			eff = explosions.obtain();
			float emitScale = eff.getEmitters().get(0).getScale().getHighMax(); 
			emitScale = emitScale/100;
			eff.scaleEffect(scale/emitScale);
			//eff.scaleEffect(scale);
			if ( debug ) Debug.log("EXPLOSION! scale: " + emitScale);
			VirusByteGame.BACK.addForeground(Backgrounds.Effect.Flash, .5f, 10);
		} else if ( type.equals( Assets.Particles.healingRing ) ) {
			eff = healingRing.obtain();
			if (eff != null && color != null) for (ParticleEmitter emit : eff.getEmitters()) emit.getTint().setColors( new float[] {color.r, color.g, color.b} );
			VirusByteGame.BACK.addForeground(Backgrounds.Effect.Healing, .5f, 5);
		}
		return eff;
	}
	
	/**Set the quantity of particles in a burst*/
	private void setFireworkEmmiter(Array<ParticleEmitter> emitters, Color color) {
		// === //
		for (ParticleEmitter emit : emitters) {
			float[] fColor = (color != null) ? new float[]{color.r, color.g, color.b} :
				new float[]{MathUtils.random(255f)/255f, MathUtils.random(255f)/255f, MathUtils.random(255f)/255f};
			emit.getTint().setColors( fColor );
			emit.setMaxParticleCount( maxSparks / emitters.size );
			emit.setMinParticleCount( minSparks / emitters.size );
			//Debug.log("emmiter min: " + emit.getMinParticleCount());
//			Debug.log( "Particle image path: " + emit.getImagePath() );
		}
		// === //
	}
	
	/** Set the particle volume and quality based on graphic configuration */
	public void setGraphicsQuality( GraphicsQuality quality ) {
		
		switch ( quality ) {
		case HIGH:
			minSparks = 150;
			maxSparks = 300;
			Debug.log( "spark particles range is set to high" );
			break;
		case LOW:
			minSparks = 30;
			maxSparks = 60;
			Debug.log( "spark particles range is set to low" );
			break;
		case MEDIUM:
			minSparks = 100;
			maxSparks = 200;
			Debug.log( "spark particles range is set to medium" );
			break;
		default:
			break;
		}
		
	}
	
	public static class EffectDrawOnStage extends GameObject implements Poolable {
		private int depth = 30;
		private PooledEffect effect;
		private ReflectionPool<EffectDrawOnStage> drawablePool;
		public Array<EffectDrawOnStage> fireList;
		
		public EffectDrawOnStage() {
			super();
		}
		
		public EffectDrawOnStage set(PooledEffect effect, int depth) {
			this.effect = effect;
			this.depth = depth;
			return this;
		}

		@Override
		public void reset() {
			depth = 30;
			effect.free();
			effect = null;
		}

		@Override
		public int getDepth() {
			return depth;
		}
		
		public boolean isComplete() {
			return effect.isComplete();
		}
		
		@Override
		public void lateUpdate(float deltaTime) {
			if ( effect.isComplete() ) {
				drawablePool.free( this );
				VirusByteGame.GAME_ENGINE.removeGameObject( this );
				fireList.removeValue( this, true );
			}
		}
		
		@Override
		public void draw(SpriteBatch batch, float deltaTime) {
			effect.draw(batch, deltaTime);
		}
		
		public void initialize( ReflectionPool<EffectDrawOnStage> drawablePool, Array<EffectDrawOnStage> fireList ) {
			this.drawablePool = drawablePool;
			this.fireList = fireList;
		}
		
		@Override
		public String getName() {
			return "part effect";
		}
	}
}
