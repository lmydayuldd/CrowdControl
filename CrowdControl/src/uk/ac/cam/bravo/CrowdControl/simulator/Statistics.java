/*
 * identification of bottlenecks, an evaluation of the efficiency of 
 * different itineraries, the time taken to evacuate all persons in an emergency and a measure 
 * of the danger persons are exposed to during an emergency. 
 */

package uk.ac.cam.bravo.CrowdControl.simulator;

import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.bravo.CrowdControl.agent.AgentInterface;
import uk.ac.cam.bravo.CrowdControl.agent.Health;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.DoorInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.RoomInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.StatisticsInterface;

public class Statistics implements StatisticsInterface {

	public class BottleneckStats {
		public int duration;
		public int maxCrowdSize;

		public BottleneckStats(int duration, int nAgents) {
			this.duration = duration;
			this.maxCrowdSize = nAgents;
		}

		public void updateCrowdSize(int nAgentsNear) {
			if (nAgentsNear > maxCrowdSize)
				maxCrowdSize = nAgentsNear;
		}
	}

	private static final float SCALING_CONSTANT = 1000000f;
//	private static final int CONGESTION_THRESHOLD = 10;
//	private static final int CONGESTION_TIME_INTERVAL = 500;
//	private static final float VICINITY_RADIUS = 100; //in cm

//	private final int timeStep;
//	private final int MIN_CONGESTION_TIME;
//	private final AgentManager amanager;

	/*
	 * Evacuation statistics
	 */
	private boolean evacuating;
	private int startTime;
	private int evacDuration;
	private float dangerLevel;

	/*
	 * General agent statistics
	 */
	private Set<AgentInterface> initPopulation;
	private float averageSpeed;
	private int agentTotal;
	private int nAgents;
	private int injured = 0;
	/*
	 * Itinerary statistics
	 */
	private ItineraryInfo itineraryInfo;

	
	
	public Statistics(AgentManager amanager, ItineraryInfo itineraries,
			int timeStep, int nDoors) {

//		this.amanager = amanager;
//		this.timeStep = timeStep;
//		this.MIN_CONGESTION_TIME = CONGESTION_TIME_INTERVAL * timeStep;
		this.evacDuration = 0;
		this.evacuating = false;
		this.dangerLevel = 0;

		this.initPopulation = new HashSet<AgentInterface>(
				amanager.getWorldState());
		this.averageSpeed = 0;
		this.agentTotal = amanager.getNumberOfAgents();
		this.nAgents = amanager.getNumberOfAgents();

		this.itineraryInfo = itineraries;
//		this.congestionPoints = new HashMap<Door, BottleneckStats>(nDoors);
	}

	@Override
	public float getAverageSpeed() {
		return averageSpeed;
	}

	@Override
	public Map<Itinerary, float[]> getItineraryEfficiency() {
		return calculateItineraryEfficiency();
	}

	@Override
	public int getEvacuationDuration(int clock) {
		return (evacuating) ? (clock - startTime) : evacDuration;
	}
	
	@Override
	public float getMicromorts() {
		return calculateMicromorts();
	}

	@Override
	public Map<RoomInterface, Integer> getDeathsPerRoom() {
		return calculateDeathsPerRoom();
	}

	public void signalStartEvac(int clock) {
		startTime = clock;
		evacuating = true;
	}

	public void signalEndEvac(int clock) {
		if (evacuating) {
			evacDuration = clock - startTime;
			evacuating = false;
		}
	}

	public void updateSimulationStats(int clock, Set<AgentInterface> agents,
			Iterable<DoorInterface> doors) {
		nAgents = agents.size();

		if (nAgents == 0) {

			if (evacuating) {
				signalEndEvac(clock);
				System.out.println("<Statistics> Time to evacuate: "
						+ evacDuration);
			}
			return;
		}

		calculateAverageSpeed(agents);
		//monitorBottlenecks(doors);
	}
	
	private void calculateAverageSpeed(Set<AgentInterface> agents) {

		float total = 0;
		for (AgentInterface a : agents)
			total += a.GetAverageSpeed();

		averageSpeed = total / nAgents;
	}

	private float calculateMicromorts() {

		int dead = 0;
		injured = 0;
		for (AgentInterface a : initPopulation) {

			Health h = a.GetHealth();
			if (h == Health.Dead)
				++dead;
			else if (h == Health.Injured)
				++injured;
		}
		agentTotal = Math.max(agentTotal, nAgents);
		return (agentTotal == 0) ? 0f : (dangerLevel = (dead * SCALING_CONSTANT / agentTotal));
	}

//	private void monitorBottlenecks(Iterable<DoorInterface> doors) {
////		System.out.println("<Statistics> Monitoring bottlenecks");
//		for (DoorInterface di : doors) {
//
//			Door d = (Door) di;
//			Point2D.Float midpoint = d.getMidpoint();
//
//			int nAgentsNear = amanager.getNumAgentsNear(midpoint, 
//					VICINITY_RADIUS);
//		
////			if (nAgentsNear > 0)
////				System.out.println("<Statistics> Number of agents near door " + d.getId() +  ": " + nAgentsNear );
//			BottleneckStats stats;
//
//			if (nAgentsNear >= CONGESTION_THRESHOLD) {
//
////				System.out.println("<Statistics> Door " + d.getId() +  ": " + nAgentsNear );
//
//				if ((stats = congestionPoints.get(d)) == null)
//					congestionPoints
//							.put(d, new BottleneckStats(0, nAgentsNear));
//				else {
//					stats.duration += timeStep;
//					stats.updateCrowdSize(nAgentsNear);
//				}
//			} else {
//				// if door was a congestionPoint but not for long enough
//				if ((stats = congestionPoints.get(d)) != null
//						&& stats.duration < MIN_CONGESTION_TIME)
//					congestionPoints.remove(d);
//			}
//		}
//	}

	private Map<RoomInterface, Integer> calculateDeathsPerRoom() {
		
		Map<RoomInterface, Integer> deathsPerRoom = new HashMap<RoomInterface, Integer>();
		
		for (AgentInterface a: initPopulation) {
			
			if (a.GetHealth() != Health.Dead)
				continue;
			
			RoomInterface r = a.GetRoom();
			Integer count;
			
			if ((count = deathsPerRoom.get(r)) != null)
				deathsPerRoom.put(r, count +1);
			else
				deathsPerRoom.put(r, 1);		
		}
		
		return deathsPerRoom;
	}
	
	private Map<Itinerary, float[]> calculateItineraryEfficiency() {

		List<Itinerary> itins = itineraryInfo.getItineraries();
		Map<Itinerary, float[]> itinStats = new HashMap<Itinerary, float[]>();

		for (Itinerary i : itins) {

			float idealTime = i.getDuration();
			List<AgentInterface> agents = itineraryInfo
					.getAgentsForItinerary(i);
			float averageTime = 0;

			for (AgentInterface a : agents) {
				averageTime += a.timeTakenForItinerary();
			}

			if (agents.size() > 0)
				averageTime = averageTime / agents.size();

			float[] stats = new float[2];
			stats[0] = idealTime;
			stats[1] = averageTime;

			itinStats.put(i, stats);
		}
		return itinStats;
	}

	@Override
	public int getInjured() {
		return injured;
	}

}
