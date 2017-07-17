package uk.ac.cam.bravo.CrowdControl.simulator.forUI;

import java.io.Serializable;
import java.util.List;

public class FurnitureShape implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final boolean walkable;
	public final int id;
	public final int order;
	
	public final List<FurniturePoly> polys;


	public FurnitureShape(boolean walkable, int id, int order, List<FurniturePoly> polys) {
		this.walkable = walkable;
		this.id = id;
		this.order = order;
		this.polys = polys;
	}
}
