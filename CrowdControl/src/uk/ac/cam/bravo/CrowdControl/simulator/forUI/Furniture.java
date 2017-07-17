package uk.ac.cam.bravo.CrowdControl.simulator.forUI;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.io.Serializable;

public class Furniture implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final int shapeId;
	public final Point2D.Float location;
	public final int floor;
	
	public Furniture(int shapeId, Float location, int floor) {
		super();
		this.shapeId = shapeId;
		this.location = location;
		this.floor = floor;
	}
	
	
}
