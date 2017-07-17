package uk.ac.cam.bravo.CrowdControl.simulator;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;

import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.DoorInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.RoomInterface;

public class Door implements DoorInterface/*, Comparable<Door>*/{
	
	private static final long serialVersionUID = 1L;

	private static transient int doorId = 0;
	
	private transient final int id = ++doorId;
	
	private transient final Room room1;
	private transient final Room room2;
	private transient final Point2D.Float endPoint1;
	private transient final Point2D.Float endPoint2;
	
	//Used to work out "pressure" on door
	private int nCurrAgents;
	
	@Override
	public Line2D.Float getRoomCoord(RoomInterface r) {
		return new Line2D.Float(endPoint1, endPoint2);
	}
	
	public Door(Room room1, Room room2, Point2D.Float endPoint1, Point2D.Float endPoint2) {
		super();
		this.room1 = room1;
		this.room2 = room2;
		this.endPoint1 = endPoint1;
		this.endPoint2 = endPoint2;
	}
	
	public int getId() {
		return id;
	}
		
	@Override
	public  RoomInterface getDestination(RoomInterface r) {
		
		if (room1.equals((Room) r))
				return room2;
		
		return room1;
	}
	

	@Override
	public String toString() {
		String info = "<id=" + id + "> Door with endpoints " + endPoint1.toString() + " and " + endPoint2.toString() + "\n"
						+ "Connects " + room1.getName() + " and " + room2.getName();
					
		return info;
	}

	@Override
	public Float getMidpoint() {
		Point2D.Float mid = new Point2D.Float();
		mid.x = (endPoint1.x + endPoint2.x) * 0.5f;
		mid.y = (endPoint1.y + endPoint2.y) * 0.5f;
		return mid;
	}

	
}
