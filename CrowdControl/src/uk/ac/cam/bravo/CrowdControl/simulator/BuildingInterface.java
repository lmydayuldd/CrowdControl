package uk.ac.cam.bravo.CrowdControl.simulator;

import java.awt.geom.Point2D;
import java.util.List;

import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.DoorInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.RoomInterface;


public interface BuildingInterface {
	
	public RoomInterface getOutside();
	
	//return all rooms except the "outside room"
	public List<Room> getRooms();
	
    public List<DoorInterface> getHighLevelPath(RoomInterface start, RoomInterface goal, Point2D.Float currentPos);

}
