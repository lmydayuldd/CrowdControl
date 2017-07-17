package uk.ac.cam.bravo.CrowdControl.simulator;

import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.DoorInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.RoomInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.RoomShape;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.Vertex;

class Room implements RoomInterface, RoomShape, Comparable<Room> {

	private static final long serialVersionUID = 1L;

	private final int id;
	private final String name;

	private final int floor;

	// List of list vertices where each list represents a polygon for the UI
	private final List<List<Vertex>> vertices;
	private List<DoorInterface> doors;
	private Rectangle2D.Float boundingBox;
	private transient boolean[][] passable;

	// Passable grid size in cm
	private static final int PASSABLE_GRID_SIZE = 20;

	/**
	 * Creates a new Room with the specified name, id, floor and vertices and
	 * computes its bounding box.
	 * 
	 * @param name
	 *            Name of Room
	 * @param id
	 *            Unique identifier as found in XML config file
	 * @param floor
	 *            Number of the floor the room is on
	 * @param vertices
	 *            List of Lists of vertices where each list represents a polygon
	 */
	public Room(String name, int id, int floor, List<List<Vertex>> vertices) {
		this.name = name;
		this.id = id;
		this.floor = floor;
		this.vertices = vertices;
		this.doors = new ArrayList<DoorInterface>();

		// Get bounding box
		float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE, minY = minX, maxY = maxX;

		for (List<Vertex> vlist : vertices) {
			for (Vertex v : vlist) {
				if (minX > v.x)
					minX = v.x;
				if (maxX < v.x)
					maxX = v.x;

				if (minY > v.y)
					minY = v.y;
				if (maxY < v.y)
					maxY = v.y;
			}
		}
		float width = maxX - minX;
		float height = maxY - minY;

		boundingBox = new Rectangle2D.Float(minX, maxY, width, height);
		computeMap();
	}

	/**
	 * Creates a new Room with the specified name, id, floor and vertices and
	 * bounding box.
	 * 
	 * @param name
	 *            Name of Room
	 * @param id
	 *            Unique identifier as found in XML config file
	 * @param floor
	 *            Number of the floor the room is on
	 * @param vertices
	 *            List of Lists of vertices where each list represents a polygon
	 * @param box
	 *            smallest bounding box that encloses the room
	 */
	public Room(String name, int id, int floor, List<List<Vertex>> vertices,
			Rectangle2D.Float box) {
		this.name = name;
		this.id = id;
		this.floor = floor;
		this.vertices = vertices;
		this.doors = new ArrayList<DoorInterface>();
		this.boundingBox = box;
	}

	// this is just to create a dummy room for use in the binary search in
	// buildroom()
	// and the xml parser
	Room(int id) {
		this.id = id;
		this.name = "";
		this.floor = -1;
		vertices = null;
	}

	/**
	 * Returns this room's unique identifier.
	 * 
	 * @return unique identifier
	 */
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<List<Vertex>> getEdges() {
		return vertices;
	}

	@Override
	public int getFloor() {
		return floor;
	}

	@Override
	public Rectangle2D.Float getBoundingBox() {

		return boundingBox;
	}

	@Override
	public boolean[][] getPassableMap() {
		return passable;
	}

	public List<DoorInterface> getDoors() {
		return doors;
	}

	/**
	 * Creates the list of doors for this room and where they lead to and the
	 * passableMap for this room. This method need and should only be called
	 * once by the BuildingPlan object after the room is created.
	 * 
	 * @param polyIdToRoom
	 *            a mapping from polygon identifiers to the room's they belong
	 *            to
	 * @param allRooms
	 *            a sorted list of all the rooms in the building
	 * @param outside
	 *            the outside room
	 */
	public void buildRoom(Map<Integer, Room> polyIdToRoom, List<Room> allRooms,
			Room outside) {
		// System.out.println("---Building room " + this.name);

		for (List<Vertex> vlist : vertices) {

			int len = vlist.size();

			for (int i = 0; i < len; ++i) {

				Vertex v = vlist.get(i);

				if (v.edgeType == Vertex.EdgeType.connector) {

					Room otherR = polyIdToRoom.get(v.polyId);

					if (otherR == null) { // this happens if the door leads
											// outside (or if I made a mistake
											// somewhere)

						if (floor == 0) {
							otherR = outside;
//							System.out.println("Unknown target polygon "
//									+ v.polyId + " in room " + this.name
//									+ ". Setting it to lead outside.");
						} else {
//							System.out.println("Unknown target polygon "
//									+ v.polyId + " in room " + this.name
//									+ ". Ignoring it (not on ground floor). "
//									+ this.floor);
							continue;
						}

					}

					// as the rooms are built in to increasing id order, this
					// door will already
					// have been created when the room with the lower id was
					// built
					if (otherR.equals(this) || otherR.id < this.id)
						continue;

					// if (otherR.id == this.id) {
					// System.out.println("Connector target leads to same room!");
					// continue;
					// }

					Door d;
					int index = (i + 1) % len;
					d = new Door(this, otherR, v, vlist.get(index));

					doors.add(d);
					otherR.doors.add(d);
				}
			}
		}
	}

	/**
	 * Computes the passableMap for this room.
	 */
	private void computeMap() {
		Rectangle2D.Float box = getBoundingBox();

		// Width, height of passable grid
		int w = (int) Math.round(Math.ceil(box.width) / PASSABLE_GRID_SIZE);
		int h = (int) Math.round(Math.ceil(box.height) / PASSABLE_GRID_SIZE);

		passable = new boolean[w][h];

		// Create an array of polygons for the room
		Polygon[] roomPolygon = new Polygon[vertices.size()];
		for (int i = 0; i < roomPolygon.length; i++) {
			List<Vertex> vlist = vertices.get(i);
			roomPolygon[i] = new Polygon(getXPoints(vlist), getYPoints(vlist),
					vlist.size());
		}

		// Get the x, y co-ords of the bounding box in cm.
		int x_min = (int) Math.round(box.x + 0.5 * PASSABLE_GRID_SIZE);
		int y_max = (int) Math.round(box.y - 0.5 * PASSABLE_GRID_SIZE);

		// Fill in the grid
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int x = x_min + (i * PASSABLE_GRID_SIZE);
				int y = y_max - (j * PASSABLE_GRID_SIZE);
				// Check to see if the point intersects one of the polygons
				for (int p = 0; p < roomPolygon.length; p++) {
					if (roomPolygon[p].contains(x, y)) {
						passable[i][j] = true;
						break;
					}
				}
			}
		}

	}

	// Gets the y points of the polygon in cm
	private int[] getYPoints(List<Vertex> polygon) {
		int[] points = new int[polygon.size()];
		for (int i = 0; i < polygon.size(); i++) {
			points[i] = (int) (polygon.get(i).y);
		}
		return points;
	}

	// Gets the x points of the polygon in cm
	private int[] getXPoints(List<Vertex> polygon) {
		int[] points = new int[polygon.size()];
		for (int i = 0; i < polygon.size(); i++) {
			points[i] = (int) (polygon.get(i).x);
		}
		return points;
	}

	@Override
	public int compareTo(Room r) {

		if (this.id < r.id)
			return -1;
		if (this.id > r.id)
			return 1;

		return 0;
	}

	// This is commented out because I've just noticed that with the addition of
	// the stairs, room ids in the openroommap aren't unique anymore -.-
	/*
	 * @Override public int hashCode() { final int prime = 31; int result = 1;
	 * result = prime * result + id; return result; }
	 * 
	 * @Override public boolean equals(Object obj) { if (this == obj) { return
	 * true; } if (obj == null) { return false; } if (getClass() !=
	 * obj.getClass()) { return false; } Room other = (Room) obj; if (id !=
	 * other.id) { return false; } return true; }
	 */

	@Override
	public String toString() {
		String info = "<id=" + id + ">" + " Room " + name + " on floor "
				+ floor + "\n" + "Bounding box: " + boundingBox.toString()
				+ "\n" + "List of vertices:\n";

		for (List<Vertex> vlist : vertices) {
			info += "POLY: \n";

			for (Vertex v : vlist)
				info += v.toString() + "\n";
		}

		info += "List of doors:\n";

		for (DoorInterface d : doors) {
			info += d.toString() + "\n";
		}

		return info;
	}
}
