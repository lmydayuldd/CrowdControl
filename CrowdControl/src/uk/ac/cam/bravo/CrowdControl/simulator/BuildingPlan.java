package uk.ac.cam.bravo.CrowdControl.simulator;

import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.DoorInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.BuildingPlanI;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.Furniture;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.FurnitureShape;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.RoomShape;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.Vertex;

class BuildingPlan implements BuildingPlanI {

	private static final long serialVersionUID = 1L;
	
	private final int nFloors;
	private final Map<Integer, String> floorNames;
	
	//List of rooms sorted by increasing id
	private final List<Room> rooms;
	private transient final Set<DoorInterface> doors;
	private final List<FurnitureShape> furnitureShapes;
	private final List<Furniture> furnitureLocations;

	private final Rectangle2D.Float boundingBox; //bounding box of the base of the whole building (ie. based on the first floor only)
	private final Room outside;
	private final float padding = 100; //controls how large the "outside room" is supposed to be
	

	/**
	 * Constructs a new BuildingPlan with the specified characteristics and calls buildRoom() on all Room objects in rooms.
	 * 
	 * @param nFloors the number of floors in the building.
	 * @param floorNames a mapping from floor numbers to floor names.
	 * @param rooms a list of all rooms in the building
	 * @param polyIdToRoom a mapping from polygon identifiers to the rooms they belong to
	 * @param furnitureShapes a list of all existing furniture shapes
	 * @param furnitureLocations a list of all furniture locations
	 * @throws IOException 
	 */
	public BuildingPlan(int nFloors, Map<Integer, String> floorNames, List<Room> rooms, Map<Integer, Room> polyIdToRoom,
																					List<FurnitureShape> furnitureShapes, 
																					List<Furniture> furnitureLocations) throws IOException {
		this.nFloors = nFloors;
		this.floorNames = floorNames;
		this.rooms = rooms;
		this.doors = new HashSet<DoorInterface>();
		this.furnitureShapes = furnitureShapes;
		this.furnitureLocations = furnitureLocations;

		//work out building bounding box
		float minX = Float.MAX_VALUE, maxY = Float.MIN_VALUE, maxX = maxY, minY = minX;

		for (Room r: rooms) {
			Rectangle2D.Float box = r.getBoundingBox();

			if (minX > box.x)
				minX = box.x;
			
			if (maxY < box.y)
				maxY = box.y;
			
			float x = box.x + box.width;
			float y = box.y - box.height;
			
			if (maxX < x)
				maxX = x;
			
			if (minY > y)
				minY = y;
		}
		float width = maxX - minX;
		float height = maxY - minY;
		
		boundingBox = new Rectangle2D.Float(minX, maxY, width, height);
		
		//sort rooms by increasing id
		Collections.sort(rooms);
		Room lastRoom = rooms.get(rooms.size()-1);
		int outsideId = lastRoom.getId() + 1;
		
		//create an "outside room" and add it to list of rooms
		float X1 = boundingBox.x - padding;
		float Y1 = boundingBox.y + padding;
		width = boundingBox.width + 2 * padding;
		height = boundingBox.height + 2 * padding;
		float X2 = X1 + width;
		float Y2 = Y1 - height;
		
		ArrayList< List<Vertex>> vertices = new ArrayList< List<Vertex>>();
		ArrayList<Vertex> poly = new ArrayList<Vertex>();
		poly.add( new Vertex( X1, Y1, Vertex.EdgeType.wall, -1));
		poly.add( new Vertex( X1, Y2, Vertex.EdgeType.wall, -1));
		poly.add( new Vertex( X2, Y1, Vertex.EdgeType.wall, -1));
		poly.add( new Vertex( X2, Y2, Vertex.EdgeType.wall, -1));
		vertices.add( poly);

		//TODO the outside room should really have more vertices (the inner ones that delimit the building) 
		
		outside = new Room("Outside", outsideId, 0, vertices, new Rectangle2D.Float(X1, Y1, width, height));
		rooms.add(outside);
		//work out connections (doors) for the rooms
		buildRooms(polyIdToRoom);
	}

	@Override
	public int getFloors() {
		return nFloors;
	}

	@Override
	public List<RoomShape> getRooms(int floor) {
		List<RoomShape> r = new ArrayList<RoomShape>();
		
		for (RoomShape rs : rooms) {
			if (rs.getFloor() == floor && !rs.equals(outside))
				r.add(rs);
		}	
		return r;
	}

	/**
	 * Returns a list of all the rooms in the building including the outside room.
	 * 
	 * @return a list of all rooms in the building
	 */
	public List<Room> getAllRooms() {
		return rooms;
	}
	
	/**
	 * Returns the number of rooms in the building including the outside room.
	 * 
	 * @return the number of rooms in the building.
	 */
	public int getNumRooms() {
		return rooms.size();
	}
	
	public Set<DoorInterface> getDoors() {
		return doors;
	}
	
	public int getNumDoors() {
		return doors.size();
	}
	
	@Override
	public List<FurnitureShape> getFurnitureShapes() {
		return furnitureShapes;
	}

	@Override
	public List<Furniture> getFurnitureLocations(int floor) {
		return furnitureLocations;
	}
	
	@Override
	public Rectangle2D.Float getBoundingBox() {
		return boundingBox;
	}

	@Override
	public Map<Integer, String> getFloorNames() {
		return floorNames;
	}
	
	/**
	 * Returns the outside room. 
	 * This is a "room" enclosing the building.
	 * 
	 * @return the outside room.
	 */
	public Room getOutside() {	
		return outside;
	}

	/**
	 * Calls buildRooms on every room in order of increasing id
	 * room.buildRoom(rooms) will work out where the doors are and what
	 * room they lead to
	 * @throws IOException 
	 * 
	 */
	private void buildRooms(Map<Integer, Room> polyIdToRoom) throws IOException {

		for (Room r : rooms) {
			r.buildRoom(polyIdToRoom, rooms, outside);
			List<DoorInterface> ds = r.getDoors();
			
			for (DoorInterface d : ds) 
				doors.add(d);
		}
		
//		writeDebugInfo();
//		Set<Integer> idset = polyIdToRoom.keySet();
//		List<Integer> ids = new ArrayList<Integer>(idset);
//		Collections.sort(ids);
//		System.out.println("Biggest poly id: " + ids.get(ids.size()-1));
	}

	@SuppressWarnings("unused")
	private void writeDebugInfo() throws IOException {
		BufferedWriter writer = new BufferedWriter( new FileWriter("parsertest.txt", false));
		
		writer.write("Building bounding box: " + boundingBox.toString());
		writer.newLine();
		
		writer.write("ROOMS");
		writer.newLine();
		writer.write("Number of rooms: " + rooms.size());
		writer.newLine();
		
		for (Room r : rooms) {
			String info = r.toString();
			
			info.replaceAll("\n", System.getProperty("line.separator"));
			writer.write(info);
			writer.newLine();
		}
		writer.flush();
	}
	
}
