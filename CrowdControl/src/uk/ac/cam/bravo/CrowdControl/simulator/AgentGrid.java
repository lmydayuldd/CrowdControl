package uk.ac.cam.bravo.CrowdControl.simulator;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.cam.bravo.CrowdControl.agent.AgentInterface;

class AgentGrid {
	 
	public class AgentList {
		public List<AgentInterface> agents = new ArrayList<AgentInterface>();
		public int col;
		public int row;
		
		AgentList(int col, int row) {
			this.col = col;
			this.row = row;
		}
	}

	private static final int BUCKET_SIZE = 1000;
	private final AgentList[][] buckets;
	private final int width;
	private final int height;
	private final Rectangle2D.Float boundingBox;

	public AgentGrid(Iterable<AgentInterface> agents, Rectangle2D.Float boundingBox) {
		
		this.width = (int) boundingBox.width / BUCKET_SIZE;
		this.height = (int) boundingBox.height / BUCKET_SIZE;
		this.boundingBox = boundingBox;
		this.buckets = new AgentList[width][height];
		
//		System.out.println("<AgentGrid> w=" + width + " h=" + height);
		
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				buckets[i][j] = new AgentList(i, j);
			}
		}
		
		//initialise grid
		for (AgentInterface a: agents)
			addAgent(a);	
		
		/*for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				System.out.println("Agents in bucket " + i + " " + j);
				for (AgentInterface a: buckets[i][j].agents)
					System.out.println(normalizePoint(a.GetPosition()));
			}
		}*/
	}

	public List<AgentInterface> findAgentsNear(Point2D.Float position, float radius, 
																		Map<AgentInterface, AgentManager.AgentLocation> prevPos,
																		boolean useCurrAgentPos) {
		
		//Coordinates of square bounding the circle (position, radius).

		float X1 = position.x - radius;		
		float Y1 = position.y - radius;
		float X2 = position.x + radius;
		float Y2 = position.y + radius;
		
		float minX = boundingBox.x;
		float minY = boundingBox.y - boundingBox.height;
		float maxX = boundingBox.x + boundingBox.width;
		float maxY = boundingBox.y;
		
		if (X1 < minX)
			X1 = minX;
		if (Y1 < minY)
			Y1 = minY;
		if (X2 >= maxX)
			X2 = maxX - 1;
		if (Y2 >= maxY)
			Y2 = maxY - 1;

		Point2D.Float p1 = normalizePoint( new Point2D.Float(X1, Y1));
		Point2D.Float p2 = normalizePoint( new Point2D.Float(X2, Y2));

		//buckets of upper left and bottom right corners of square
		AgentList upLeft = getNode(p1);
		AgentList bottomRight = getNode(p2);

		List<AgentInterface> agents = new ArrayList<AgentInterface>();
		
		for(int i = upLeft.col; i <= bottomRight.col; ++i) 
			for (int j = upLeft.row; j <= bottomRight.row; ++j) 
				agents.addAll(buckets[i][j].agents);
		
		double sqrR = Math.pow(radius, 2);
		Iterator<AgentInterface> it = agents.iterator();
		
		//filter out the points that aren't actually in the circle and if there is one, 
		//the point at position 
		while (it.hasNext()) {
			AgentInterface a = it.next();
			Point2D.Float p;
			
			if (!useCurrAgentPos)
				p = prevPos.get(a).coord;
			else
				p = a.GetPosition();

			if (p.equals(position)) {
				it.remove();
				continue;
			}
			
			double d =  (Math.pow((p.x - position.x), 2) + Math.pow((p.y - position.y), 2));
			
			//Not within radius of position
			if (d > sqrR) 
				it.remove();	
		}
		
		return agents;
	}
	
	public AgentList getNode(Point2D.Float p) {
		
		int col  = (int) p.x / BUCKET_SIZE;
		int row = (int) p.y / BUCKET_SIZE;

		//need the extra condition on the p.x and p.y for world widths that aren't divisible by bucketSize. 
		//In that case, the buckets col = width - 1 are actually larger than the bucketSize and contain all 
		//points such that p.x in [bucketSize * width, worldWidth] and p.y in [bucketSize * height, worldHeight]
		if (col >= width && p.x < boundingBox.width) 
			col = width - 1;				  
											  
		if (row >= height && p.y < boundingBox.height)
			row = height - 1;
		
		return buckets[col][row];

	}

	private void addAgent(AgentInterface a) {
		try {
			AgentList n = getNode( normalizePoint(a.GetPosition()));
			n.agents.add(a);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Not adding agent at position " + a.GetPosition() + ": out of bounds!");		
		}
	}
	
	private Point2D.Float normalizePoint(Point2D.Float p) {
		Point2D.Float normp = new Point2D.Float();
		normp.x = p.x - boundingBox.x;
		normp.y = p.y - (boundingBox.y - boundingBox.height);
		return normp;
	}
	
	public void moveAgent(Point2D.Float prevPos, AgentInterface a) {
		AgentList n = getNode( normalizePoint(prevPos));
		n.agents.remove(a);
		addAgent(a);
	}
}

