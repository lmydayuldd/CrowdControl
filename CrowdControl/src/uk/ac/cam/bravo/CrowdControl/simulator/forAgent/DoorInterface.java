package uk.ac.cam.bravo.CrowdControl.simulator.forAgent;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;

public interface DoorInterface extends Serializable {
	
	public Line2D.Float getRoomCoord(RoomInterface r);

    public RoomInterface getDestination(RoomInterface r);

	public Point2D.Float getMidpoint();

}
