package uk.ac.cam.bravo.CrowdControl.agent;

import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.RoomInterface;

public class ItineraryItem implements ItineraryItemInterface {
	private RoomInterface room;
	private int time;

	public ItineraryItem(RoomInterface room, int time) {
		this.room = room;
		this.time = time;
	}

	public RoomInterface GetRoom() {
		return room;
	}

	public int GetWaitTime() {
		return time;
	}
}
