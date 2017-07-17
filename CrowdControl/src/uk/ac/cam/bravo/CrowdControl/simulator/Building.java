package uk.ac.cam.bravo.CrowdControl.simulator;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.DoorInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.RoomInterface;


@SuppressWarnings("serial")
class Building implements BuildingInterface, Serializable {
	
	//Used by the UI
	private final BuildingPlan buildingPlan; 
	
	//Used for path finding
	private final BuildingGraph buildingGraph;
		
	//constructs a new Building from XML files
	public Building(List<InputStream> buildingFiles) throws ParserConfigurationException, SAXException, IOException, MissingConfigFilesException {
		
		if (buildingFiles.size() < 3) {
			throw new MissingConfigFilesException(3, buildingFiles.size());
		}
		
		XMLBuildingPlanParser parser = new XMLBuildingPlanParser(buildingFiles.get(0),
																 buildingFiles.get(1), 
																 buildingFiles.get(2));
		buildingPlan = parser.generateBuildingPlan();
		buildingGraph = new BuildingGraph(buildingPlan.getAllRooms());
	}
	

	public BuildingPlan getBuildingPlan() {
		return buildingPlan;
	}
	
	public RoomInterface getOutside() {

		return buildingPlan.getOutside();
	}
	
	public Rectangle2D.Float getBoundingBox() {
		
		return buildingPlan.getBoundingBox();
	}

	@Override
	public List<Room> getRooms() {
		return buildingPlan.getAllRooms();
	}
	
	public int getNumRooms() {
		return buildingPlan.getNumRooms();
	}

	@Override
	public List<DoorInterface> getHighLevelPath(RoomInterface start, RoomInterface goal, Point2D.Float position) {
		return buildingGraph.findPath((Room) start, (Room) goal, position);
	}


	public int getNumDoors() {
		return buildingPlan.getNumDoors();
	}


	public Iterable<DoorInterface> getDoors() {
		return buildingPlan.getDoors();
	}
}
