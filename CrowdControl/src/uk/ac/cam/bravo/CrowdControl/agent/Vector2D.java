package uk.ac.cam.bravo.CrowdControl.agent;

import java.awt.geom.Point2D;

/**
 * Wrapper around Point2D.Float, provides convenient functions to manipulate
 * @author Oliver Stannard
 *
 */
public class Vector2D extends Point2D.Float {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new Vector2D set to (0, 0)
	 */
	public Vector2D() {
		this.x = 0f;
		this.y = 0f;
	}
	
	/**
	 * Creates a new Vector2D with given x and y values
	 * @param x The x value of the new Vector2D
	 * @param y The x value of the new Vector2D
	 */
	public Vector2D(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Copy constructor, creates a new Vector2D with the values of a Point2D.Float
	 * @param old Point2D.Float to take values from
	 */
	public Vector2D(Point2D.Float old) {
		this.x = old.x;
		this.y = old.y;
	}
	
	/**
	 * Adds vectors, does not change the value of this Vector2D
	 * @param other Vector to add
	 * @return Result of adding this to other
	 */
	public Vector2D Add(Point2D.Float other) {
		return new Vector2D(this.x + other.x, this.y + other.y);
	}
	
	/**
	 * Subtracts vectors, does not change the value of this Vector2D
	 * @param other Vector to subtract
	 * @return Result of subtracting this from other
	 */
	public Vector2D Subtract(Point2D.Float other) {
		return new Vector2D(this.x - other.x, this.y - other.y);
	}
	
	/**
	 * Scales this vector by factor, does not modify this Vector2D
	 * @param factor Factor to scale by 
	 * @return This vector scaled by factor
	 */
	public Vector2D Scale(float factor) {
		return new Vector2D(this.x * factor, this.y * factor);
	}
	
	/**
	 * Calculates the length of this vector
	 * @return The length of this vector
	 */
	public float Length() {
		return (float) Math.sqrt(x * x + y * y);
	}
	
	/**
	 * Calculates the distance between positions represented as vectors
	 * @param other The vector to calculate the distance to
	 * @return The distance between the two positions
	 */
	public float Distance(Point2D.Float other) {
		return this.Subtract(other).Length();
	}
	
	/**
	 * Calculates the normalised vector of this vector, does not modify this Vector2D 
	 * @return Vector2D with direction of this Vector2D, but length 1
	 */
	public Vector2D Normalise() {
		if (x == 0 && y == 0)
			return new Vector2D();
		else
			return this.Scale(1/this.Length());
	}
	
	/**
	 * Checks for equality of two vector2Ds
	 * @param other vector2D to compare to
	 * @return true if the Vector2Ds are equal, false otherwise
	 */
	public boolean Equals(Point2D.Float other) {
		return (this.x == other.x) && (this.y == other.y);
	}
	
	/**
	 * Checks if the Vector2D is (0,0)
	 * @return true if this Vector2D is (0,0), false otherwise
	 */
	public boolean IsZero() {
		return (this.x == 0) & (this.y == 0);
	}
}
