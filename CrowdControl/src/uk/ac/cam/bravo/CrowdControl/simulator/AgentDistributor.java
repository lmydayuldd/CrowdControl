package uk.ac.cam.bravo.CrowdControl.simulator;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

class AgentDistributor {
	
	private static class RoomAreaComparator implements Comparator<Room> {
		private static Map<Room, Integer> areas = new HashMap<Room, Integer>();
		public static int getArea(Room r) {
			Integer i;
			int j = 0;
			if ((i = areas.get(r)) == null) {
				boolean[][] passable = r.getPassableMap();
				if (passable != null && passable.length > 2 && passable[0].length > 2) {
					for (boolean[] row : passable) {
						for (boolean b : row) {
							if (b) j += 1;
						}
					}
				}
			} else {
				j = i;
			}
			return j;
		}
		@Override
		public int compare(Room r0, Room r1) {
			if (r0 == r1) return 0;
			int r0area = getArea(r0);
			int r1area = getArea(r1);
			if (r0area > r1area) return 1;
			else if (r0area < r1area) return -1;
			return 0;
		}
	}
	
	private final List<AgentManager.AgentLocation> spositions;
	private final List<Room> allRooms;
	private final int totalAgents;
	
	AgentDistributor(List<AgentManager.AgentLocation> target, List<Room> rooms, int total) {
		spositions = (target == null) ? new ArrayList<AgentManager.AgentLocation>() : target;
		if (rooms == null || rooms.size() == 0) throw new IllegalArgumentException();
			else {
				List<Room> sortedRooms = new ArrayList<Room>(rooms);
				Collections.sort(sortedRooms, new RoomAreaComparator());
				
				allRooms = new ArrayList<Room>();
				int smallestArea = 0;
				for (Room r : sortedRooms) {
					if ((smallestArea = RoomAreaComparator.getArea(r)) > 0) break;
				}
				if (smallestArea <= 0) throw new IllegalArgumentException();
				for (Room r : sortedRooms) {
					int t =  RoomAreaComparator.getArea(r) / smallestArea;
					for (int i = 0; i < t; i++) {
						allRooms.add(r);
					}
				}
			}
		totalAgents = total;
	}
	
	static List<AgentManager.AgentLocation> distributeToRoom(List<AgentManager.AgentLocation> spositions, Room r, int nagents) {
		int i = 0, j = 0;
		boolean[][] passable;
		if ((passable = r.getPassableMap()) != null && passable.length > 2 && passable[0].length > 2) {
			Random rand = new Random();
			try {
				for (;nagents > 0; nagents--) {
					do {
						i = rand.nextInt(passable.length - 2) + 1;
						j = rand.nextInt(passable[i].length - 2) + 1;
					} while (!passable[i][j]);
					Rectangle2D.Float bounds = r.getBoundingBox();
					float x = bounds.x + i * bounds.width / passable.length;
					float y = bounds.y - j * bounds.height / passable[i].length;
					spositions.add(new AgentManager.AgentLocation(
							new Point2D.Float(x, y), r));
				}
			} catch (Exception e) {
				new IllegalArgumentException();
			}
			return spositions;
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	List<AgentManager.AgentLocation> distribute() {

		//Randomly add agents if not enough starting positions are specified
		Random rand = new Random();
		
		for (; spositions.size() < totalAgents;) {
			Room r;
			boolean[][] passable;
			int i = 0, j = 0;
			boolean acceptRoom;
			do {
				int numRooms = allRooms.size() - 1;
				acceptRoom = true;
				r = allRooms.get(rand.nextInt(numRooms));
				
				if ((passable = r.getPassableMap()) != null && passable.length > 2 && passable[0].length > 2) {
					try {
						do {
							i = rand.nextInt(passable.length - 2) + 1;
							j = rand.nextInt(passable[i].length - 2) + 1;
						} while(!passable[i][j]);
					} catch (Exception e) {
						acceptRoom = false;
					}
				} else {
					acceptRoom = false;
				}
				
				if (!acceptRoom) {
					List<Room> removeList = new ArrayList<Room>();
					removeList.add(r);
					allRooms.removeAll(removeList);
				}
			} while (!acceptRoom);
			
			Rectangle2D.Float bounds = r.getBoundingBox();
			float x = bounds.x + i * bounds.width / passable.length;
			float y = bounds.y - j * bounds.height / passable[i].length;
			
			spositions.add(new AgentManager.AgentLocation(new Point2D.Float(x, y), r));
		}
		
		return spositions;
	}
}
