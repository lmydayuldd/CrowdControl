/* 
 * The AgentManager will keep track of the positions of agents and update them. 
 *  Agents also use it for higher level pathfinding and neighbour finding.
 */

package uk.ac.cam.bravo.CrowdControl.simulator;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.bravo.CrowdControl.agent.AgentInterface;
import uk.ac.cam.bravo.CrowdControl.agent.ParticleAgent;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.AgentManagerInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.DoorInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.RoomInterface;

class AgentManager implements AgentManagerInterface {

	// Class that stores the location data of an agent
	public static class AgentLocation {
		Point2D.Float coord;
		RoomInterface room;

		public AgentLocation(Point2D.Float coord, RoomInterface r) {
			this.coord = coord;
			this.room = r;
		}
	}

	private final Building building;

	private final HashSet<AgentInterface> agentSet;
	private final Map<Room, List<AgentInterface>> roomOccupants;

	// Used for agent neighbour search
	private final AgentGrid buckets;

	// Used to save the agent positions and rooms before their positions are
	// updated
	private final Map<AgentInterface, AgentLocation> prevAgentPos;

	private int nAgents;
	private final ItineraryInfo itineraries;

	private boolean isEmergency = false;
	
	public AgentManager(Simulator simulator, Building building,
			ItineraryInfo itineraries) {

		this.building = building;
		this.itineraries = itineraries;

		this.agentSet = new HashSet<AgentInterface>(nAgents);
		this.roomOccupants = new HashMap<Room, List<AgentInterface>>(
				building.getNumRooms());
		this.prevAgentPos = new HashMap<AgentInterface, AgentLocation>(nAgents);

		List<Room> rooms = building.getRooms();

		for (Room r : rooms) {
			roomOccupants.put(r, new ArrayList<AgentInterface>());
		}

		createAgents();

		Rectangle2D.Float buildingBox = building.getBoundingBox();
		this.buckets = new AgentGrid(agentSet, buildingBox);
	}

	// TODO implement a method to clear the agentmanager
	public int getNumberOfAgents() {
		return nAgents;
	}

	public Set<AgentInterface> getWorldState() {
		return agentSet;
	}

	public Map<Room, List<AgentInterface>> getRoomOccupants() {
		return roomOccupants;
	}

	public void createAgents() {

		int nItins = itineraries.getNumItineraries();
		List<AgentLocation> startPositions = itineraries.getStartPositions();
		int startPosIndex = 0;
		for (int itinIndex = 0; itinIndex < nItins; ++itinIndex) {

			Itinerary currentIt = itineraries.getItineraries().get(itinIndex);
			int total = currentIt.getnAgents();
			int n = 0;

			while (n < total && startPosIndex < startPositions.size()) {
				AgentLocation loc = startPositions.get(startPosIndex);
				AgentInterface a = new ParticleAgent(this, loc.room, loc.coord,
						currentIt.getItineraryItemList());
				System.out.println("<AgentManager> Created new agent at "
						+ a.GetPosition() + " in room "
						+ ((Room) a.GetRoom()).getName() + " with itinerary uid " + currentIt.getId());

				agentSet.add(a);
				prevAgentPos.put(a, loc);
				roomOccupants.get(loc.room).add(a);
				itineraries.addAgentToItinerary(currentIt, a);
				++startPosIndex;
				++n;
			}
		}
	}

	public void updateAgents(int timeStep, boolean isEmergency) {

		if (agentSet.size() == 0)
			return;
		
		// save previous agent coordinates
		for (AgentInterface a : agentSet) {
			prevAgentPos
					.put(a, new AgentLocation(a.GetPosition(), a.GetRoom()));
		}

		RoomInterface outside = building.getOutside();
		Iterator<AgentInterface> it = agentSet.iterator();

		while (it.hasNext()) {
			AgentInterface agent = it.next();

			if (isEmergency && !this.isEmergency) {
				agent.SetEmergency();
			}
				
			// this call may result in a call to updateCurrentRoom by the
			// agent
			agent.Act();
		}

		it = agentSet.iterator();

		while (it.hasNext()) {
			AgentInterface agent = it.next();
			if (agent.GetRoom() == outside) {
				it.remove();
				continue;
			}
			buckets.moveAgent(prevAgentPos.get(agent).coord, agent);
		}
		
		if (isEmergency) this.isEmergency = true;
	}

	public void updateCurrentRoom(AgentInterface agent, RoomInterface prevRoom,
			RoomInterface newRoom) {}

	public Iterable<AgentInterface> getAgentsNear(RoomInterface room,
			Point2D.Float position, float radius) {

		Iterable<AgentInterface> agents = buckets.findAgentsNear(position,
				radius, prevAgentPos, false);

		Iterator<AgentInterface> it = agents.iterator();

		// filter out agents that aren't in the same room
		while (it.hasNext()) {
			AgentInterface a = it.next();

			if (prevAgentPos.get(a).room != room)
				it.remove();
		}
		return agents;
	}

	public int getNumAgentsNear(Point2D.Float position, float radius) {

		List<AgentInterface> agents = buckets.findAgentsNear(position, radius,
				null, true);
		return agents.size();
	}

	public List<DoorInterface> getHighLevelPath(RoomInterface start, RoomInterface goal, Point2D.Float currentPos) {
		return building.getHighLevelPath(start, goal, currentPos);
	}

	@Override
	public RoomInterface getOutside() {
		return building.getOutside();
	}

	@Override
	public RoomInterface getOutside(AgentInterface a) {
		RoomInterface currentRoom = a.GetRoom();
		RoomInterface outside = building.getOutside();
		List<DoorInterface> exits = building.getOutside().getDoors();
		DoorInterface target = null;
		Point2D.Float pos = a.GetPosition();
		double minDistance = Double.MAX_VALUE;
		for (DoorInterface door : exits) {
			if (currentRoom == door.getDestination(outside)) return outside;
			Point2D.Float midpoint = door.getMidpoint();
			double dis = Point2D.distance(pos.x, pos.y, midpoint.x, midpoint.y);
			if (dis < minDistance) {
				minDistance = dis;
				target = door;
			}
		}
		return target.getDestination(outside);
	}
}
