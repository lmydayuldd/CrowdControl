/*package uk.ac.cam.bravo.CrowdControl.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class RouteGenerator {
	private static class DoorAdjacency {
		public Door d1;
		public Door d2;
		public float distance;
		public DoorAdjacency(Door d1, Door d2, float distance) {
			this.d1 = d1;
			this.d2 = d2;
			this.distance = distance;
		}
	}
	// This class updates doors with their best distance to exits.
	public static void updateDoorDistanceToExit(Room[] rooms) {
		HashMap<Door, List<DoorAdjacency>> edges = new HashMap<Door, List<DoorAdjacency>>();
		PriorityQueue<Door> vertices = new PriorityQueue<Door>();
		
		for (Room r: rooms) {
			for(Door d: r.getDoors()) {
				if(d.isExit()) {
					d.setBestDistanceToExit(0.0f);
				} else {
					d.setBestDistanceToExit(Float.POSITIVE_INFINITY);
				}
				vertices.add(d);
				ArrayList<DoorAdjacency> edgesForThisDoor = new ArrayList<DoorAdjacency>();
				for (Door q: r.getDoors()) {
					if (d != q) {
						edgesForThisDoor.add(new DoorAdjacency(d, q, d.getDistance(q)));
					}
				}
				edges.put(d, edgesForThisDoor);
			}
		}
		
		while(true) {
			Door min = vertices.poll();
			if (min == null) {
				break;
			}
			for (DoorAdjacency edge: edges.get(min)) {
				if (min.getBestDistanceToExit() + edge.distance < edge.d2.getBestDistanceToExit()) {
					edge.d2.setBestDistanceToExit(min.getBestDistanceToExit() +
							edge.distance);
					vertices.remove(edge.d2);
					vertices.add(edge.d2);
				}
			}
			vertices.remove(min);
		}
	}
	
	public static void main(String args[]) {
		ArrayList<Room> rooms = new ArrayList<Room> ();
		Room r1 = new Room(3, 10);
		Room r2 = new Room(3, 3);
		rooms.add(r1);
		rooms.add(r2);
		
		Door d1 = new Door();
		d1.setExit();
		d1.setFirstRoom(r1, new Coord(1, 0));
		
		Door d2 = new Door();
		d2.setFirstRoom(r2, new Coord(1, 0));
		d2.setSecondRoom(r1, new Coord(1, 9));
		
		Door d3 = new Door();
		d3.setExit();
		d3.setFirstRoom(r2, new Coord(2, 2));
		
		Room[] roomsA = new Room[rooms.size()];
		rooms.toArray(roomsA);
		
		updateDoorDistanceToExit(roomsA);
		
		System.out.println(d1.getBestDistanceToExit());
		System.out.println(d2.getBestDistanceToExit());
	}
}*/
