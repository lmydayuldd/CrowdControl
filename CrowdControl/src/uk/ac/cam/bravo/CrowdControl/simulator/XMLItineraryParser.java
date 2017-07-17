package uk.ac.cam.bravo.CrowdControl.simulator;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import uk.ac.cam.bravo.CrowdControl.agent.ItineraryItem;
import uk.ac.cam.bravo.CrowdControl.agent.ItineraryItemInterface;

class XMLItineraryParser {

	private SAXParser saxParser;
	private InputStream itin;
	
	private List<Itinerary> itineraries;
	private List<AgentManager.AgentLocation> spositions;
	private int totalAgents;

	public XMLItineraryParser(InputStream itin) throws ParserConfigurationException, SAXException {
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
	    saxParser = factory.newSAXParser();
	    this.itin = itin;
	    this.itineraries = new ArrayList<Itinerary>();
	    this.spositions = new ArrayList<AgentManager.AgentLocation>();
	    this.totalAgents = 0;
	}

	public ItineraryInfo generateItineraries(List<Room> allRooms) throws SAXException, IOException {
		
		parseItineraries(allRooms);
		getStartPositions(allRooms);
		
		return new ItineraryInfo(itineraries, spositions, totalAgents);
	}
	
	public List<AgentManager.AgentLocation> getStartPositions(List<Room> allRooms) {
		return spositions = new AgentDistributor(spositions, allRooms, totalAgents).distribute();
	}

	public void close() throws IOException {
		itin.close();
	}

	private void parseItineraries(final List<Room> allRooms) throws SAXException, IOException {
			
			DefaultHandler handler = new DefaultHandler() {
				
				//boolean inItin = false;
				//boolean inRoom = false;
				List<ItineraryItemInterface> rooms = new ArrayList<ItineraryItemInterface>();
				int id;
				int nagents;
								
				public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
					
					if (qName.equalsIgnoreCase("Itinerary")) {
						//inItin = true;
						id = Integer.parseInt(attributes.getValue("uid"));
						nagents = Integer.parseInt(attributes.getValue("nagents"));
						totalAgents += nagents;
						rooms = new LinkedList<ItineraryItemInterface>();
					}
					if (qName.equalsIgnoreCase("Room")) {
						
						//inRoom = true;
						int roomid = Integer.parseInt(attributes.getValue("uid"));					
						int index = Collections.binarySearch(allRooms, new Room(roomid));

						if (index < 0) {
							System.out.println("<Itinerary parsing> No room with id " + roomid + ". Ignored.");
							return;
						}
						
						Room r = allRooms.get(index);
						int time = Integer.parseInt(attributes.getValue("time"));					
						rooms.add( new ItineraryItem(r, time));
					}
					if (qName.equalsIgnoreCase("Position")) {
						int roomid = Integer.parseInt(attributes.getValue("uid"));		
						float x = 100 * Float.parseFloat(attributes.getValue("x"));	
						float y = 100 * Float.parseFloat(attributes.getValue("y"));					
						Point2D.Float p = new Point2D.Float(x, y);
						int index = Collections.binarySearch(allRooms, new Room(roomid));
						
						if (index < 0) {
							System.out.println("<Itinerary parsing> No room with id " + roomid + ". Start position ignored.");
							return;
						}
						
						Room r = allRooms.get(index);
						spositions.add( new AgentManager.AgentLocation(p, r));
					}
					if (qName.equalsIgnoreCase("StartRoom")) {
						int roomid = Integer.parseInt(attributes.getValue("uid"));
						int nagents = Integer.parseInt(attributes.getValue("nagents"));
						spositions = AgentDistributor.distributeToRoom(spositions, allRooms.get(roomid), nagents);
					}
				}
				
				public void endElement(String uri, String localName, String qName) throws SAXException {

					if	(qName.equalsIgnoreCase("Itinerary")) {
							itineraries.add( new Itinerary(nagents, id, rooms));
					}

				}
				
				public void characters(char ch[], int start, int length) throws SAXException {
					return;
				}
			};
			
			saxParser.parse(itin, handler);
		}
}
