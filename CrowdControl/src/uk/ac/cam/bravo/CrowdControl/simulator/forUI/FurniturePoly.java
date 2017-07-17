package uk.ac.cam.bravo.CrowdControl.simulator.forUI;

import java.io.Serializable;
import java.util.List;

public class FurniturePoly  implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final int edgeColour;
	public final int fillColour;
	public final List<Vertex> vertices;
	
	public FurniturePoly(int edgeColour, int fillColour, List<Vertex> vertices) {
		super();
		this.edgeColour = edgeColour;
		this.fillColour = fillColour;
		this.vertices = vertices;
	}
	
	
}
