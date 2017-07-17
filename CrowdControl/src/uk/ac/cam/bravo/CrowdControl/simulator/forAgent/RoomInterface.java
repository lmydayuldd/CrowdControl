package uk.ac.cam.bravo.CrowdControl.simulator.forAgent;

import java.awt.geom.Rectangle2D;
import java.util.List;

public interface RoomInterface {
	  
	public Rectangle2D.Float getBoundingBox();

	public boolean[][] getPassableMap();

	public int getFloor();
	
	public List<DoorInterface> getDoors();
}
