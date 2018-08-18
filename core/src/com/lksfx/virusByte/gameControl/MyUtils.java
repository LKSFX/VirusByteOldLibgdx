package com.lksfx.virusByte.gameControl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.debug.Debug;

public class MyUtils {
	
	public static int choose(int... values) {
		return values[MathUtils.random(values.length-1)]; 
	}
	
	public static float choose(float... values) {
		return values[MathUtils.random(values.length-1)]; 
	}
	
	public static BitmapFont ttfToBitmap(String patch, int size) {
		patch = String.format("data/fonts/%s", patch);
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(patch));
		FreeTypeFontParameter params = new FreeTypeFontParameter();
		params.size = size;
		params.magFilter = TextureFilter.Linear;
		params.minFilter = TextureFilter.Linear;
		BitmapFont font = generator.generateFont(params);
		generator.dispose();
		Debug.log("font: " + params.size);
		return font;
	}
	
	public static boolean linesIntersect(Vector2 start1, Vector2 end1, Vector2 start2, Vector2 end2) {
		return linesIntersect(start1.x, start1.y, end1.x, end1.y, start2.x, start2.y, end2.x, end2.y);
	}
	
	public static boolean linesIntersect(double x1, double y1, double x2, double y2, double x3,
			double y3, double x4, double y4) {
			/*
			* A = (x2-x1, y2-y1) B = (x3-x1, y3-y1) C = (x4-x1, y4-y1) D = (x4-x3,
			* y4-y3) = C-B E = (x1-x3, y1-y3) = -B F = (x2-x3, y2-y3) = A-B Result
			* is ((AxB) (AxC) <=0) and ((DxE) (DxF) <= 0) DxE = (C-B)x(-B) =
			* BxB-CxB = BxC DxF = (C-B)x(A-B) = CxA-CxB-BxA+BxB = AxB+BxC-AxC
			*/
			x2 -= x1; // A
			y2 -= y1;
			x3 -= x1; // B
			y3 -= y1;
			x4 -= x1; // C
			y4 -= y1;
			double AvB = x2 * y3 - x3 * y2;
			double AvC = x2 * y4 - x4 * y2;
			// Online
			if (AvB == 0.0 && AvC == 0.0) {
			if (x2 != 0.0) {
			return (x4 * x3 <= 0.0)
			|| ((x3 * x2 >= 0.0) && (x2 > 0.0 ? x3 <= x2 || x4 <= x2 : x3 >= x2
			|| x4 >= x2));
			}
			if (y2 != 0.0) {
			return (y4 * y3 <= 0.0)
			|| ((y3 * y2 >= 0.0) && (y2 > 0.0 ? y3 <= y2 || y4 <= y2 : y3 >= y2
			|| y4 >= y2));
			}
			return false;
			}
			double BvC = x3 * y4 - x4 * y3;
			return (AvB * AvC <= 0.0) && (BvC * (AvB + BvC - AvC) <= 0.0);
	}
	
	/**
	* Calculates an MD5 hash.
	* @param s The String you want the hash of.
	* @return The MD5 Hash of the String passed.
	*/
	public static String MD5(String input) {
		String res = "";
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(input.getBytes());
			byte[] md5 = algorithm.digest();
			String tmp = "";
			for (int i = 0; i < md5.length; i++) {
				tmp = (Integer.toHexString(0xFF & md5[i]));
				if (tmp.length() == 1) {
					res += "0" + tmp;
				} else {
					res += tmp;
				}
			}
		} catch (NoSuchAlgorithmException ex) {}
		return res;
	}
	
	public static void checkCrypt(Preferences pref) {
		String crypt = MyUtils.MD5( pref.getInteger("best_score", 0) + VirusByteGame.SALT );
		if ( !crypt.equals( pref.getString("crypt") ) ) {
			pref.remove("best_score");
			pref.flush();
		}
	}
	
	public static Color randomColor() {
		return new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1f);
	}
	
	public static boolean overlaps(Polygon polygon, Circle circle) {
		
		Vector2 vec2A = VirusByteGame.GAME.obtainVector2();
		Vector2 vec2B = VirusByteGame.GAME.obtainVector2();
		Vector2 vec2C = VirusByteGame.GAME.obtainVector2();
		
	    float[] vertices = polygon.getTransformedVertices();
	    Vector2 center = vec2C.set( circle.x, circle.y );
	    float squareRadius = circle.radius * circle.radius;
	    for ( int i = 0; i < vertices.length; i+=2 ) {
	        if ( i == 0 ) {
	            if ( Intersector.intersectSegmentCircle( vec2A.set(vertices[vertices.length - 2], vertices[vertices.length - 1]), vec2B.set(vertices[i], vertices[i + 1]), center, squareRadius ) )
	                return true;
	        } else {
	            if ( Intersector.intersectSegmentCircle( vec2A.set(vertices[i-2], vertices[i-1]), vec2B.set(vertices[i], vertices[i+1]), center, squareRadius ) )
	                return true;
	        }
	    }
	    return polygon.contains( circle.x, circle.y );
	    
	}
	
	/** get the angle relative to a position */
	public static float getAngleBetween( Vector2 pos1, Vector2 pos2 ) {
		float angle = MathUtils.atan2(pos1.y - pos2.y, pos1.x - pos2.x);
		angle = angle * ( 180/MathUtils.PI );
		if(angle < 0) {
			angle = 360 - (-angle);
		}
		return angle;
	}
	
}
