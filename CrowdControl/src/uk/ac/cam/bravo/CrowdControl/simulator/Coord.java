package uk.ac.cam.bravo.CrowdControl.simulator;

public class Coord {
	private int x;
	private int y;
	
	public void setX(int x) {
		this.x = x;
	}
	public int getX() {
		return x;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getY() {
		return y;
	}
	
	public Coord(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public boolean equals(Object otherObj) {
		if (!(otherObj instanceof Coord)) {
			return false;
		} else {
			Coord o = (Coord) otherObj;
			return this.x == o.getX() &&
			this.y == o.getY();
		}
		
	}
	
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + x;
		hash = 31 * hash + y;
		return hash;
	}
}
