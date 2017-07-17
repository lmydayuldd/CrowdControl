/*
 * This class gathers information from the simulation to transmit them to 
 * the UI in the form of various statistics.
 */

package uk.ac.cam.bravo.CrowdControl.simulator.forUI;

import java.util.Map;
import uk.ac.cam.bravo.CrowdControl.simulator.Itinerary;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.RoomInterface;

public interface StatisticsInterface {

	/***************************************************************************
	 * UI interface
	 ***************************************************************************/

	public float getAverageSpeed();

	// each itinerary has an associated array where, 
	// float[] stats --> stats[0] = ideal itinerary duration
	// stats[1] = actual itinerary duration averaged across all agents on that 
	//itinerary
	public Map<Itinerary, float[]> getItineraryEfficiency();

	public int getEvacuationDuration(int clock);
	
	public float getMicromorts();

	public Map<RoomInterface, Integer> getDeathsPerRoom();
	// public Set<Door> getBottlenecks();

	public int getInjured();
}
