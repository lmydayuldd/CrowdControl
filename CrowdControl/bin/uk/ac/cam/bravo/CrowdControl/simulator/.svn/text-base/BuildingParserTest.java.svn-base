/*
 * This tests XMLBuildingPlanParser with a small test building. The test files are /src/roomdata.xml, 
 * /src/furnitureshapes.xml and /src/furnitureloc.xml. For the moment, furnitureshapes and furnitureloc are empty 
 * and this isn't really a unit test, it outputs a text file parsertest.txt in the project root folder.
 */

package uk.ac.cam.bravo.CrowdControl.simulator;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

class BuildingParserTest {

	public static void main(String args[]) {
				
		try {
			InputStream roomF = new FileInputStream("src/roomdata.xml");
			InputStream shapeF = new FileInputStream("src/furnitureshapes.xml");
			InputStream locF = new FileInputStream("src/furnitureloc.xml");
			InputStream itin = new FileInputStream("src/itineraries.xml");


			XMLBuildingPlanParser parser = new XMLBuildingPlanParser(roomF, shapeF, locF);
			BuildingPlan plan = parser.generateBuildingPlan();
			parser.close();
			List<Room> rooms = plan.getAllRooms();

			XMLItineraryParser p = new XMLItineraryParser(itin);
			ItineraryInfo itineraries = p.generateItineraries(rooms);
			
			BufferedWriter writer = new BufferedWriter( new FileWriter("parsertest.txt", false));
			
			writer.write("Building bounding box: " + plan.getBoundingBox().toString());
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
			
			writer.write("ITINERARIES");
			writer.newLine();
			
			for (Itinerary i : itineraries.getItineraries()) {
				String info = i.toString();

				info.replaceAll("\n", System.getProperty("line.separator"));
				writer.write(info);
				writer.newLine();
			}
			
			writer.write("START POSITIONS");
			writer.newLine();
			for (AgentManager.AgentLocation loc : itineraries.getStartPositions()) {
				String info = "Position " + loc.coord + " in room " + ((Room)loc.room).getName() + "\n";

				info.replaceAll("\n", System.getProperty("line.separator"));
				writer.write(info);
				writer.newLine();
			}
			
			writer.flush();		
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}