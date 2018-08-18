package com.lksfx.virusByte.gameControl;

import com.badlogic.gdx.math.Vector2;

public class Line2D {
	public Vector2 start, end;
	
	public Line2D(float x1, float y1, float x2, float y2) {
		start = new Vector2(x1, y1);
		end = new Vector2(x2, y2);
	}
	
	public boolean intersectsLine(Line2D l) {
		return MyUtils.linesIntersect(start, end, l.start, l.end);
	}
	
	public void setLine(float x1, float y1, float x2, float y2) {
		start.set(x1, y1);
		end.set(x2, y2);
	}
}