package com.lksfx.virusByte.gameObject.abstractTypes;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.lksfx.virusByte.gameControl.MyUtils;

public class DetectionMask {
	
	public enum Offset { CENTER, LEFT_BOTTOM };
	public enum MaskFormat { CIRCLE, RECTANGLE, POLYGON, TRIANGLE };
	private MaskFormat maskFormat = MaskFormat.RECTANGLE;
	private Offset offset;
	private Rectangle rectangle;
	private Rectangle rectangle_copy;
	private Circle circle;
	private Polygon polygon;
	private Vector2 scale;
	private Vector2 position;
	private Vector2 offsetPosition;
	private Vector2 size;
	private float[] rectVerts;
	private float radius;
	
	private static Polygon helperPolygon = new Polygon();
	
	public DetectionMask(  ) {
		rectangle = new Rectangle();
		rectangle_copy = new Rectangle();
		circle = new Circle();
		polygon = new Polygon();
		position = new Vector2();
		scale = new Vector2(1 , 1);
		offsetPosition = new Vector2();
		size = new Vector2();
		offset = Offset.CENTER;
		rectVerts = new float[8];
	}
	
	/**@param rect if true the collision check will be against two rectangles else two circles
	 * @return object that contains the collided object */
	public boolean collisionDetection( DetectionMask otherMask ) {
		boolean result = false;
		
		if ( maskFormat == MaskFormat.RECTANGLE ) { 
			// When this mask is a rectangle
			if ( otherMask.maskFormat == MaskFormat.CIRCLE )
				result = Intersector.overlaps( otherMask.circle, rectangle );
			else if ( otherMask.maskFormat == MaskFormat.RECTANGLE )
				result = Intersector.overlaps( otherMask.rectangle, rectangle );
			else if ( otherMask.maskFormat == MaskFormat.TRIANGLE || otherMask.maskFormat == MaskFormat.POLYGON ) {
				// Transform this rectangle mask in a polygon and check for collision
				Polygon poly = helperPolygon;
				poly.setVertices( rectVerts );
				poly.setPosition( position.x, position.y );
				result = Intersector.overlapConvexPolygons( poly, otherMask.polygon );
			}
		}
		else if ( maskFormat == MaskFormat.CIRCLE ) {
			// When this mask is a circle
			if ( otherMask.maskFormat == MaskFormat.CIRCLE )
				result = Intersector.overlaps( circle, otherMask.circle );
			else if ( otherMask.maskFormat == MaskFormat.RECTANGLE )
				result = Intersector.overlaps( circle, otherMask.rectangle );
			else if ( otherMask.maskFormat == MaskFormat.TRIANGLE || otherMask.maskFormat == MaskFormat.POLYGON )
				result = MyUtils.overlaps( otherMask.polygon, circle );
		}
		else if ( maskFormat == MaskFormat.POLYGON || maskFormat == MaskFormat.TRIANGLE ) {
			// When this mask is a Polygon or a triangle
			if ( otherMask.maskFormat == MaskFormat.CIRCLE )
				result = MyUtils.overlaps( polygon, otherMask.circle );
			else if ( otherMask.maskFormat == MaskFormat.TRIANGLE || otherMask.maskFormat == MaskFormat.POLYGON )
				result = Intersector.overlapConvexPolygons( polygon, otherMask.polygon );
			else if ( otherMask.maskFormat == MaskFormat.RECTANGLE ) {
				// Transform the other object rectangle mask in a polygon and check for collision
				Polygon poly = helperPolygon;
				poly.setVertices( otherMask.rectVerts );
				poly.setPosition( otherMask.position.x, otherMask.position.y );
				result = Intersector.overlapConvexPolygons( poly, polygon );
			}
		}
		
		return result;
	}
	
	/** @return true if <b>xx</b> and <b>yy</b> is over collision mask */
	public boolean isOver( float xx, float yy ) {
		boolean bool = false;
		
		/*if ( xx > rectangle.x && xx < rectangle.x + rectangle.width ) {
			if ( yy > rectangle.y && yy < rectangle.y + rectangle.height ) {
				bool = true;
			}
		}*/
		if ( maskFormat == MaskFormat.RECTANGLE )
			bool = rectangle.contains( xx, yy );
		else if ( maskFormat == MaskFormat.CIRCLE )
			bool = circle.contains( xx, yy );
		else if ( maskFormat == MaskFormat.POLYGON || maskFormat == MaskFormat.TRIANGLE )
			bool = polygon.contains( xx, yy );
			
		return bool;
	}
	
	/** @return a circle representation of the size and on the position of this mask */
	public Circle getCircle() {
		return circle;
	}
	
	public MaskFormat getMaskFormat() {
		return maskFormat;
	}
	
	/** @return a rectangle representation of the size and on the position of this mask */
	public Rectangle getRectangle() {
		return rectangle_copy.set( rectangle );
	}
	
	/** Only work for Triangle or Polygon shape */
	public void setAngle( float degrees ) {
		polygon.setRotation( degrees );
	}
	
	/**Set the detection mask format*/
	public void setMaskFormat( MaskFormat format ) {
		maskFormat = format;
	}
	
	/** Set offset relative to CENTER or LEFT_BOTTOM */
	public void setOffset( Offset offset ) {
		this.offset = offset;
	}
	
	/** Set offset */
	public void setOffset( float xx, float yy ) {
		offsetPosition.set(xx, yy);
	}
	
	public Polygon getPolygon() {
		return polygon;
	}
	
	public void setPosition( Vector2 position ) {
		setPosition( position.x, position.y );
	}
	
	/** Set this mask position */
	public void setPosition( float xx, float yy ) {
		position.set( xx, yy ).add( offsetPosition );
		
		switch ( maskFormat ) {
		case CIRCLE:
			circle.setRadius( radius );
			break;
		case POLYGON:
			break;
		case RECTANGLE:
			rectangle.setSize(size.x * scale.x, size.y * scale.y);
			break;
		case TRIANGLE:
			break;
		default:
			break;
		}
		
		if ( offset == Offset.CENTER ) {
			rectangle.setCenter( position.x, position.y );
			circle.setPosition( position.x, position.y );
			polygon.setPosition( position.x, position.y );
		} else { 
			rectangle.setPosition( position.x, position.y );
			circle.setPosition( position.x - radius, position.y - radius );
			polygon.setPosition( position.x, position.y );
		}
		
	}
	
	/** Set this mask scale to x and y */
	public void setScale( float scaleX, float scaleY ) {
		scale.set( scaleX, scaleY );
		updateRectVerts();
	}
	
	/** Set this rectangle mask size */
	public void setSize( float xx, float yy ) {
		size.set(xx, yy);
		updateRectVerts();
	}
	
	public void setCircleRadius( float radius ) {
		this.radius = radius;
	}
	
	/** Set polygon vertices. Only work for Polygon or Triangle, when triangle only the first six indices will be considered */
	public void setVertices( float[] vertices  ) {
		polygon.setVertices( vertices );
	}
	
	private void updateRectVerts() {
		
		float halfW = (size.x * scale.x) / 2;
		float halfH = (size.y * scale.y) / 2;
		
		// vertex 1
		rectVerts[0] = -halfW;
		rectVerts[1] = -halfH;
		// vertex 2
		rectVerts[2] = halfW;
		rectVerts[3] = -halfH;
		// vertex 3
		rectVerts[4] = halfW;
		rectVerts[5] = halfH;
		// vertex 4
		rectVerts[6] = -halfW;
		rectVerts[7] = halfH;
		
	}
	
}
