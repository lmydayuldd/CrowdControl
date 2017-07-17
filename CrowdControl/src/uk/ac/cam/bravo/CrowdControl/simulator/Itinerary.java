package uk.ac.cam.bravo.CrowdControl.simulator;

import java.util.List;

import uk.ac.cam.bravo.CrowdControl.agent.ItineraryItemInterface;

public class Itinerary {
	
	private final int id;	
	private final int nAgents;
	private final List<ItineraryItemInterface> itinerary;
	private int totalTime = 0;
	
	public Itinerary(int nAgents, int id, List<ItineraryItemInterface> itinerary) {
		super();
		this.nAgents = nAgents;
		this.id = id;
		this.itinerary = itinerary;
		
		for (ItineraryItemInterface i: itinerary) {
			this.totalTime += i.GetWaitTime();
		}
	}
	
	public int getId() {
		return id;
	}

	public int getnAgents() {
		return nAgents;
	}

	public List<ItineraryItemInterface> getItineraryItemList() {
		return itinerary;
	}

	public int getDuration() {
		return totalTime;
	}
	
	@Override
	public String toString() {
		String info = "<" + id + "> Itinerary (nAgents=" + nAgents + "): \n";
		
		for (ItineraryItemInterface i: itinerary) {
			String roomName = ((Room) i.GetRoom()).getName();
			int id = ((Room) i.GetRoom()).getId();
			int time = i.GetWaitTime();
			info += "- Room=" + roomName + "(" + id + ")" + " waitTime=" + time + "\n";
		}
		
		return info;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Itinerary other = (Itinerary) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}
}
