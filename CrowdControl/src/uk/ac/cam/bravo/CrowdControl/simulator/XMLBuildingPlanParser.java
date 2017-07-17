package uk.ac.cam.bravo.CrowdControl.simulator;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import uk.ac.cam.bravo.CrowdControl.simulator.forUI.Furniture;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.FurniturePoly;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.FurnitureShape;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.Vertex;

class XMLBuildingPlanParser {
	
	InputStream rooms;
	InputStream shapes;
	InputStream locations;

	SAXParser saxParser;
	
	private String floorAttr = "level";
	private String floorNameAttr = "name";
	private String roomNameAttr = "name";
	private String edgeTypeAttr = "edgetype";

	//Building information
	public int nFloors;
	public List<Room> roomShapes;
	public Map<Integer, Room> polyIdToRoom;
	public List<FurnitureShape> furnitureShapes;
	public List<Furniture> furnitureLocations;		
	
	private Map<Integer, String> floorNames;
	
	public XMLBuildingPlanParser(InputStream rooms, InputStream shapes, InputStream locations) throws ParserConfigurationException, SAXException {
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
	    saxParser = factory.newSAXParser();
	    nFloors = 0;
	    roomShapes = new ArrayList<Room>();
	    polyIdToRoom = new HashMap<Integer, Room>();
	    furnitureShapes = new ArrayList<FurnitureShape>();
	    furnitureLocations = new ArrayList<Furniture>();
	    floorNames = new TreeMap<Integer, String>();
	    this.rooms = rooms;
	   
	    this.shapes = shapes;
	    this.locations = locations;
	}
	
	public BuildingPlan generateBuildingPlan() throws SAXException, IOException {
		parseRooms();
		parseFurnitureShapes();
		parseFurnitureLoc();

		BuildingPlan plan = new BuildingPlan(nFloors, floorNames, roomShapes, polyIdToRoom, furnitureShapes, furnitureLocations);
		
		return plan;
	}
		
	public void close() throws IOException {
		
		rooms.close();
		shapes.close();
		locations.close();
	}

	private void parseRooms() throws SAXException, IOException {
				
			DefaultHandler handler = new DefaultHandler() {
			
			boolean inFloor = false;
			boolean inRoom = false;
			boolean inPoly = false;
			boolean inVertex = false;
			
			int currFloor = 0;
			String currRoomName;
			int roomId;
			List<Integer> polyIdList = new ArrayList<Integer>();
			List< List<Vertex>> polyList = new LinkedList< List<Vertex>>();
			List<Vertex> currVertexList = new LinkedList<Vertex>();
			Vertex currVertex;
					
			public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
				
				if (qName.equalsIgnoreCase("Map25D")) {
					inFloor = true;
					++nFloors;
					currFloor = Integer.parseInt(attributes.getValue(floorAttr));
					floorNames.put(currFloor, attributes.getValue(floorNameAttr));
				}
				else if (qName.equalsIgnoreCase("Room")) {
					inRoom = true;
					currRoomName = attributes.getValue(roomNameAttr);
					roomId = Integer.parseInt(attributes.getValue("uid"));
					polyIdList = new ArrayList<Integer>();
					polyList = new LinkedList< List<Vertex>>();		
				}
				else if (qName.equalsIgnoreCase("Poly")) {
					inPoly = true;
					int polyId = Integer.parseInt(attributes.getValue("uid"));
					polyIdList.add(polyId);
					currVertexList = new LinkedList<Vertex>();
				}
				else if (qName.equalsIgnoreCase("Vertex")) {
					inVertex = true;
					
					Vertex.EdgeType e = Vertex.EdgeType.valueOf(attributes.getValue(edgeTypeAttr));
					
					int otherPolyId;
					
					if (e.equals(Vertex.EdgeType.connector)) {
						otherPolyId = Integer.parseInt( attributes.getValue("target"));
					}
					else
						otherPolyId = -1;
					
					float x = 100 * Float.parseFloat((attributes.getValue("x")));
					float y = 100 * Float.parseFloat((attributes.getValue("y")));
					
					currVertex = new Vertex(x, y, e, otherPolyId);
				}
			}
			
			public void endElement(String uri, String localName, String qName) throws SAXException {
				
				if (!inVertex && !inPoly && !inRoom && inFloor) {
					inFloor = false;
				}
				else if (!inVertex && !inPoly && inRoom) {
					Room r = new Room(currRoomName, roomId, currFloor, polyList);
					roomShapes.add(r);
					
					for (Integer polyId: polyIdList) {
						polyIdToRoom.put(polyId, r);
					}
					
					polyIdList = null;
					polyList = null;
					inRoom = false;
				}
				else if (!inVertex && inPoly) {
				//	currVertexList = null;
					inPoly = false;
					polyList.add( currVertexList);
					currVertexList = null;
				}
				else if (inVertex) {
					currVertexList.add(currVertex);
					currVertex = null;
					inVertex = false;
				}
			}
			
			public void characters(char ch[], int start, int length) throws SAXException {
				 return;
			 }
		};
		
		saxParser.parse(rooms, handler);
	}

	private void parseFurnitureShapes() throws SAXException, IOException {
				
		DefaultHandler handler = new DefaultHandler() {
			
			boolean inPoly1 = false;
			boolean inPoly2 = false;
			boolean inVertex = false;
			
			int order;
			boolean walkable;
			int id;
			
			FurniturePoly poly;	
			int edgeColour;
			int fillColour;
			List<FurniturePoly> polyList = new LinkedList<FurniturePoly>();

			List<Vertex> vertexList = new LinkedList<Vertex>();
			Vertex vertex;
					
			public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
				
				if (qName.equalsIgnoreCase("Poly") && attributes.getIndex("category") != -1) {
			
					inPoly1 = true;
					walkable = isWalkable(attributes.getValue("category"));
					order = Integer.parseInt(attributes.getValue("ordering"));
					id = Integer.parseInt(attributes.getValue("item_def_id"));
					polyList = new LinkedList<FurniturePoly>();
				}
				else if (qName.equalsIgnoreCase("Poly")) {
					
					inPoly2 = true;
					String edgeCol = attributes.getValue("edge_colour").substring(2);
					String fillCol = attributes.getValue("fill_colour").substring(2);
					edgeColour = Integer.parseInt(edgeCol, 16);
					fillColour = Integer.parseInt(fillCol, 16);
					vertexList = new LinkedList<Vertex>();
				}
	
				else if (qName.equalsIgnoreCase("Vertex")) {
					
					inVertex = true;
					float x = Float.parseFloat((attributes.getValue("x")));
					float y = Float.parseFloat((attributes.getValue("y")));
					vertex = new Vertex(x, y, Vertex.EdgeType.furniture, -1);
				}
			}
			
			public void endElement(String uri, String localName, String qName) throws SAXException {
				
				if (!inVertex && !inPoly2 && inPoly1) {
					
					furnitureShapes.add( new FurnitureShape(walkable, id, order, polyList));
					
					polyList = null;
					inPoly1 = false;
				}
				else if (!inVertex && inPoly2) {
					
					poly = new FurniturePoly(edgeColour, fillColour, vertexList);
					polyList.add(poly);
					
					vertexList = null;
					poly = null;
					inPoly2 = false;
				}
				else if (inVertex) {
					
					vertexList.add(vertex);					
					vertex = null;
					inVertex = false;
				}
			}
			
			public void characters(char ch[], int start, int length) throws SAXException {
				return;
			}
		};
		
		saxParser.parse(shapes, handler);
	}
	
	private void parseFurnitureLoc() throws SAXException, IOException {
		
		DefaultHandler handler = new DefaultHandler() {
			
			public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
				
				if (qName.equalsIgnoreCase("Item") && attributes.getValue("deleted").equals("false")) {
			
					float x = 100 * Float.parseFloat(attributes.getValue("x"));
					float y = 100 * Float.parseFloat(attributes.getValue("y"));
					Point2D.Float point = new Point2D.Float(x, y);
					int id = Integer.parseInt(attributes.getValue("item_def_id"));
					int floor = Integer.parseInt(attributes.getValue("floor"));
					furnitureLocations.add( new Furniture(id, point, floor));
				}
			}
			
			public void endElement(String uri, String localName, String qName) throws SAXException {
				return;
			}
			
			public void characters(char ch[], int start, int length) throws SAXException {
				return;
			}
		};
		
		saxParser.parse(locations, handler);
	}
	
	private boolean isWalkable(String category) {
		
		boolean walkable = 
			!category.equalsIgnoreCase("floor mounted") &&
			!category.equalsIgnoreCase("wall");
		
		return walkable;
	}
}
