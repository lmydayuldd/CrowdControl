package uk.ac.cam.bravo.CrowdControl.agent;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.DoorInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.RoomInterface;

public class RoomWrapper {
	private RoomInterface room;
	
	private class CachedRoomData {
		public float[][] obstacleDistances;
		public Map<Point, float[][]> goalDistances;
		public CachedRoomData() {
			obstacleDistances = null;
			goalDistances = new Hashtable<Point, float[][]>();
		}
	}
	
	private static Map<RoomInterface, CachedRoomData> cache = new Hashtable<RoomInterface, CachedRoomData>();
	
	public RoomWrapper(RoomInterface room) {this.room = room;}
	public RoomInterface getRoom() {return room;}
	
	public boolean isNull() {
		return room == null;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof RoomWrapper)
			return ((RoomWrapper) obj).room == this.room;
		return false;
	}
	
	public int hashCode() {
		if(room == null)
			return 0;
		return room.hashCode();
	}
	
	public int getFloor() {
		if(room == null)
			return 0;
		return room.getFloor();
	}
	
	public List<DoorInterface> getDoors() {
		if(room == null)
			return new ArrayList<DoorInterface>();
		List<DoorInterface> result = room.getDoors();
		if(result == null)
			return new ArrayList<DoorInterface>();
		return result;
	}
	
	public Rectangle2D.Float getBoundingBox() {
		if(room == null)
			return null;
		Rectangle2D.Float flippedBox = room.getBoundingBox();
		if(flippedBox == null || flippedBox.width == 0 || flippedBox.height == 0)
			return null;
		// Peculiarly, room.getBoundingBox() gives a result flipped in the Y direction.
		// i.e. room.getBoundingBox().y is the maximum, rather than minimum, Y coordinate of the box.
		// We correct for this flip here.
		return new Rectangle2D.Float(flippedBox.x, flippedBox.y - flippedBox.height, flippedBox.width, flippedBox.height);
	}
	
	public int getGridWidth() {
		if(room == null)
			return 1;
		boolean[][] passable = room.getPassableMap();
		if(passable == null || passable.length == 0)
			return 1;
		return passable.length;
	}
	
	public int getGridHeight() {
		if(room == null)
			return 1;
		boolean[][] passable = room.getPassableMap();
		if(passable == null || passable.length == 0 || passable[0] == null || passable[0].length == 0)
			return 1;
		return passable[0].length;
	}
	
	public boolean isSquarePassable(int x, int y) {
		if(x < 0 || y < 0)
			return false;
		boolean[][] passable = room.getPassableMap();
		if(passable == null || passable.length == 0 || passable[0] == null || passable[0].length == 0)
			return x == 0 && y == 0;
		if(x >= passable.length || y >= passable[x].length)
			return false;
		// The result of room.getPassableMap() is flipped in the Y direction, like getBoundingBox().
		// We correct for this flip here.
		return passable[x][passable[x].length - y - 1];
	}
	
	public boolean isPointPassable(Vector2D point) {
		return point != null && isSquarePassable(posToGridX(point.x), posToGridY(point.y));
	}
	
	public float getGridCellWidth() {
		Rectangle2D.Float boundingBox = getBoundingBox();
		if(boundingBox == null)
			return 0;
		return boundingBox.width / getGridWidth();
	}

	public float getGridCellHeight() {
		Rectangle2D.Float boundingBox = getBoundingBox();
		if(boundingBox == null)
			return 0;
		return boundingBox.height / getGridHeight();
	}
	
	public int posToGridX(float posX) {
		if(room == null)
			return 0;
		Rectangle2D.Float boundingBox = getBoundingBox();
		if(boundingBox == null)
			return 0;
		return (int) ((posX - boundingBox.x) / boundingBox.width * getGridWidth());
	}
	
	public int clipGridX(int x) {
		return Math.max(0, Math.min(getGridWidth() - 1, x));
	}

	public int posToGridY(float posY) {
		if(room == null)
			return 0;
		Rectangle2D.Float boundingBox = getBoundingBox();
		if(boundingBox == null)
			return 0;
		return (int) ((posY - boundingBox.y) / boundingBox.height * getGridHeight());
	}
	
	public int clipGridY(int y) {
		return Math.max(0, Math.min(getGridHeight() - 1, y));
	}
	
	public float getObstacleDistance(int gridX, int gridY) {
		int w = getGridWidth();
		int h = getGridHeight();
		if(gridX < 0 || gridX >= w || gridY < 0 || gridY >= h)
			return 0;
		CachedRoomData roomData = getCachedRoomData();
		float[][] distances = roomData.obstacleDistances;
		if(distances == null) {
			distances = new float[w][h];
			for(int x = 0; x < w; ++x) {
				for(int y = 0; y < h; ++y)
					distances[x][y] = isSquarePassable(x, y) ? Float.POSITIVE_INFINITY : 0;
			}
			computeGridDistances(distances, 0);
			roomData.obstacleDistances = distances;
		}
		return distances[gridX][gridY];
	}
	
	public float getGoalDistance(int gridX, int gridY, int goalX, int goalY) {
		int w = getGridWidth();
		int h = getGridHeight();
		if(gridX < 0 || gridX >= w || gridY < 0 || gridY >= h)
			return Float.POSITIVE_INFINITY;
		goalX = clipGridX(goalX);
		goalY = clipGridY(goalY);
		CachedRoomData roomData = getCachedRoomData();
		Point goal = new Point(goalX, goalY);
		float[][] distances = roomData.goalDistances.get(goal);
		if(distances == null) {
			distances = new float[w][h];
			for(int x = 0; x < w; ++x) {
				for(int y = 0; y < h; ++y)
					distances[x][y] = Float.POSITIVE_INFINITY;
			}
			distances[goalX][goalY] = 0;
			computeGridDistances(distances);
			roomData.goalDistances.put(goal, distances);
		}
		return distances[gridX][gridY];
	}
	
	public void computeGridDistances(float[][] distanceGrid) {
		computeGridDistances(distanceGrid, Float.POSITIVE_INFINITY);
	}
	
/*	// This does not seem to actually improve performance much, and does not properly check passability for diagonals.
	public void computeGridDistances(final float[][] distanceGrid, final float outsideVal) {
		final int w = distanceGrid.length;
		final int h = distanceGrid[0].length;
		class CellDistance implements Comparable<CellDistance> {
			public int x, y;
			public float distance;
			public CellDistance(int x, int y, float distance) {
				this.x = x;
				this.y = y;
				this.distance = distance;
			}
			public CellDistance(int x, int y) {
				this.x = x;
				this.y = y;
				this.distance = x >= 0 && x < w && y >= 0 && y < h ? distanceGrid[x][y] : outsideVal;
			}
			public void addToQueue(PriorityQueue<CellDistance> distanceQueue) {
				if(distance != Float.POSITIVE_INFINITY)
					distanceQueue.add(this);
			}
			public int compareTo(CellDistance other) {
				if(distance < other.distance)
					return -1;
				if(distance > other.distance)
					return 1;
				return x != other.x ? x - other.x : y - other.y;
			}
			public void addNeighbour(int dx, int dy, PriorityQueue<CellDistance> distanceQueue) {
				final int newX = x + dx;
				final int newY = y + dy;
				if(isSquarePassable(newX, newY)) {
					final float newDistance = (float) (distance + Math.hypot(dx, dy));
					if(newDistance < distanceGrid[newX][newY]) {
						distanceGrid[newX][newY] = newDistance;
						new CellDistance(newX, newY, newDistance).addToQueue(distanceQueue);
					}
				}
			}
		}
		final PriorityQueue<CellDistance> distanceQueue = new PriorityQueue<CellDistance>();
		// Add initial distances to queue.
		for(int x = -1; x <= w; ++x) {
			for(int y = -1; y <= h; ++y)
				new CellDistance(x, y).addToQueue(distanceQueue);
		}
		// Iterate on queue.
		while(!distanceQueue.isEmpty()) {
			// Remove the first cell from the queue.
			CellDistance cell = distanceQueue.remove();
			// Check its neighbours.
			cell.addNeighbour(-1, -1, distanceQueue, checkPassable);
			cell.addNeighbour(0, -1, distanceQueue, checkPassable);
			cell.addNeighbour(1, -1, distanceQueue, checkPassable);
			cell.addNeighbour(-1, 0, distanceQueue, checkPassable);
			cell.addNeighbour(1, 0, distanceQueue, checkPassable);
			cell.addNeighbour(-1, 1, distanceQueue, checkPassable);
			cell.addNeighbour(0, 1, distanceQueue, checkPassable);
			cell.addNeighbour(1, 1, distanceQueue, checkPassable);
		}
	}
*/
	
	public void computeGridDistances(float[][] distanceGrid, float outsideVal) {
		// TODO: make this more efficient (priority queue)
		boolean changed = true;
		while (changed) {
			changed = false;
			for(int x = 0; x < distanceGrid.length; ++x) {
				for(int y = 0; y < distanceGrid[x].length; ++y) {
					if (isSquarePassable(x, y)) {
						float min = Float.POSITIVE_INFINITY;
						final int maxX = distanceGrid.length - 1;
						final int maxY = distanceGrid[x].length - 1;
						final float RootTwo = (float) Math.sqrt(2);

						if (isSquarePassable(x-1, y) || isSquarePassable(x, y-1))
							min = Math.min((x > 0 && y > 0 ? distanceGrid[x-1][y-1] : outsideVal) + RootTwo, min);
						min = Math.min((y > 0 ? distanceGrid[x][y-1] : outsideVal) + 1, min);
						if (isSquarePassable(x+1, y) || isSquarePassable(x, y-1))
							min = Math.min((x < maxX && y > 0 ? distanceGrid[x+1][y-1] : outsideVal) + RootTwo, min);
						min = Math.min((x > 0 ? distanceGrid[x-1][y] : outsideVal) + 1, min);
						min = Math.min((x < maxX ? distanceGrid[x+1][y] : outsideVal) + 1, min);
						if (isSquarePassable(x-1, y) || isSquarePassable(x, y+1))
							min = Math.min((x > 0 && y < maxY ? distanceGrid[x-1][y+1] : outsideVal) + RootTwo, min);
						min = Math.min((y < maxY ? distanceGrid[x][y+1] : outsideVal) + 1, min);
						if (isSquarePassable(x+1, y) || isSquarePassable(x, y+1))
							min = Math.min((x < maxX && y < maxY ? distanceGrid[x+1][y+1] : outsideVal) + RootTwo, min);

						if (min < distanceGrid[x][y]) {
							distanceGrid[x][y] = min;
							changed = true;
						}
					}
				}
			}
		}
	}

	private CachedRoomData getCachedRoomData() {
		CachedRoomData roomCache = cache.get(room);
		if(roomCache == null) {
			roomCache = new CachedRoomData();
			cache.put(room, roomCache);
		}
		return roomCache;
	}
}
