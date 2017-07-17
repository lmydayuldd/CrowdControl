package uk.ac.cam.bravo.CrowdControl.agent;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.*;

public class SimpleAgent implements AgentInterface {
	private RoomInterface room;
	private List<ItineraryItemInterface> itinerary;
	private AgentManagerInterface agentManager;
	private int timeLeftInRoom;
	private int timeTotalInRoom;
    private DoorInterface door;
	private Point2D.Float startPosition;
	private static float speed = 10;

	public SimpleAgent(AgentManagerInterface agentManager, RoomInterface room,
			Point2D.Float position, List<ItineraryItemInterface> itinerary) {
		this.room = room;
		this.startPosition = new Point2D.Float(position.x, position.y);
		this.itinerary = new LinkedList<ItineraryItemInterface>(itinerary);
		this.agentManager = agentManager;
        this.door = null;
		UpdateRoute();
	}

	public Point2D.Float GetPosition() {
        Point2D.Float endPosition = startPosition;
        if(door != null)
            endPosition = GetLineMidpoint(door.getRoomCoord(room));
        if(timeTotalInRoom == 0)
            return new Point2D.Float(endPosition.x, endPosition.y);
		float x = (float) (startPosition.getX() * ((float) timeLeftInRoom / timeTotalInRoom) +
				endPosition.getX() * (1 - (float) timeLeftInRoom / timeTotalInRoom));
		float y = (float) (startPosition.getY() * ((float) timeLeftInRoom / timeTotalInRoom) +
				endPosition.getY() * (1 - (float) timeLeftInRoom / timeTotalInRoom));
		return new Point2D.Float(x, y);
	}

	public RoomInterface GetRoom() {
		return room;
	}

	public List<ItineraryItemInterface> GetItinerary() {
		return new ArrayList<ItineraryItemInterface>(itinerary);
	}

	public void SetItinerary(List<ItineraryItemInterface> newItinerary) {
		itinerary = new LinkedList<ItineraryItemInterface>(newItinerary);
	}

	public void Act() {
		if(timeLeftInRoom > 0)
    		timeLeftInRoom--;
		if (timeLeftInRoom <= 0 && door != null) {
			RoomInterface oldRoom = room;
			room = door.getDestination(oldRoom);
			startPosition = GetLineMidpoint(door.getRoomCoord(room));
            door = null;
			UpdateRoute();
			agentManager.updateCurrentRoom(this, oldRoom, room);
		}
	}
	
	protected void UpdateRoute() {
        int waitTime = 0;
        if(door == null && itinerary != null && !itinerary.isEmpty()) {
            ItineraryItemInterface item = itinerary.get(0);
            while(item != null && item.GetRoom() == room) {
                waitTime += item.GetWaitTime();
                itinerary.remove(0);
                if(itinerary.isEmpty())
                    item = null;
                else
                    item = itinerary.get(0);
            }
            if(item != null) {
                List<DoorInterface> suggestedDoors = agentManager.getHighLevelPath(room, item.GetRoom(), GetPosition());
                DoorInterface firstDoor = null;
                while(suggestedDoors != null && !suggestedDoors.isEmpty()) {
                    DoorInterface tryDoor = suggestedDoors.remove(0);
                    if(firstDoor == null)
                    	firstDoor = tryDoor;
                    RoomInterface nextRoom = tryDoor.getDestination(room);
                    // Try not to go outside.
                    if(nextRoom == item.GetRoom() || nextRoom != agentManager.getOutside()) {
                    	door = tryDoor;
                    	break;
                    }
                }
                // If all routes go outside, take the best one.
                if(door == null)
                	door = firstDoor;
            }
        }
        if(door != null) {
        	Point2D.Float endPosition = GetLineMidpoint(door.getRoomCoord(room));
        	if(room == agentManager.getOutside())
        		startPosition = endPosition; // If we are outside, 'teleport' to the entrance.
        	float distanceX = (float) (endPosition.getX() - startPosition.getX());
        	float distanceY = (float) (endPosition.getY() - startPosition.getY());
        	float distance = (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
            timeLeftInRoom = timeTotalInRoom = Math.max(waitTime, (int) (distance / speed));
        }
	}

	public void SetEmergency() {
		itinerary = new ArrayList<ItineraryItemInterface>();
		itinerary.add(new ItineraryItem(agentManager.getOutside(), 0));
	}

	public Point2D.Float GetLastMovement() {
        // Manually-generated method stub
		return new Point2D.Float(0, 0);
	}

	public float GetAverageSpeed() {
        // Manually-generated method stub
		return 0;
	}

	public void ResetAverageSpeed() {
	}
	
	public int timeTakenForItinerary() {
		// Manually-generated method stub
		return 0;
	}

	public Health GetHealth() {
		return Health.Healthy;
	}
	
	protected Point2D.Float GetLineMidpoint(Line2D.Float line) {
		return new Point2D.Float((line.x1 + line.x2) / 2, (line.y1 + line.y2) / 2);
	}
}
