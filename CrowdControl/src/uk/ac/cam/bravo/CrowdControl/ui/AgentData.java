package uk.ac.cam.bravo.CrowdControl.ui;

import java.awt.geom.Point2D.Float;
import java.io.Serializable;
import java.util.List;

import uk.ac.cam.bravo.CrowdControl.agent.AgentInterface;
import uk.ac.cam.bravo.CrowdControl.agent.Health;
import uk.ac.cam.bravo.CrowdControl.agent.ItineraryItemInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.DoorInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.RoomInterface;

public class AgentData implements AgentInterface, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final Float position;
	private final RoomInterface room;
	private final Float lastMove;
	private final float avgSpeed;
	private final int timeTaken;
	private final Health health;
	
	private static class DummyRoom implements RoomInterface, Serializable {

		private static final long serialVersionUID = 1L;

		private final int floor;
		
		public DummyRoom(RoomInterface r) {
			floor = r.getFloor();
		}
		
		@Override
		public java.awt.geom.Rectangle2D.Float getBoundingBox() {
			return null;
		}

		@Override
		public boolean[][] getPassableMap() {
			return null;
		}

		@Override
		public int getFloor() {
			return floor;
		}

		@Override
		public List<DoorInterface> getDoors() {
			return null;
		}
	}
	
	public AgentData(AgentInterface a) {
		position = a.GetPosition();
		room = new DummyRoom(a.GetRoom());
		lastMove = a.GetLastMovement();
		avgSpeed = a.GetAverageSpeed();
		timeTaken = a.timeTakenForItinerary();
		health = a.GetHealth();
	}
	
	@Override
	public Float GetPosition() {
		return position;
	}

	@Override
	public RoomInterface GetRoom() {
		return room;
	}

	@Override
	public Float GetLastMovement() {
		return lastMove;
	}

	@Override
	public float GetAverageSpeed() {
		return avgSpeed;
	}

	@Override
	public int timeTakenForItinerary() {
		return timeTaken;
	}

	@Override
	public Health GetHealth() {
		return health;
	}

	@Override public void SetItinerary(List<ItineraryItemInterface> newItinerary) {}
	@Override public void Act() {}
	@Override public void SetEmergency() {}
	@Override
	public List<ItineraryItemInterface> GetItinerary() {
		return null;
	}
	@Override public void ResetAverageSpeed() {}
}
