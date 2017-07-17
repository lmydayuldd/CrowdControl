package uk.ac.cam.bravo.CrowdControl.agent;

import java.awt.geom.Point2D;
import java.util.List;

import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.RoomInterface;

public interface AgentInterface {
	/**
	 * 
	 * @return The current position of the agent, it the building-wide coordinate system
	 */
	public Point2D.Float GetPosition();
	
	/**
	 * 
	 * @return The room the agent is currently in
	 */
	public RoomInterface GetRoom();
	
	/**
	 * 
	 * @return The Itinerary that the agent is currently following
	 */
	public List<ItineraryItemInterface> GetItinerary();
	
	/**
	 * Changes the itinerary that the agent is following
	 * @param newItinerary New itinerary
	 */
	public void SetItinerary(List<ItineraryItemInterface> newItinerary);
	
	/**
	 * Runs simulation for one step, updates position of this agent
	 */
	public void Act();
	
	/**
	 * Puts the agent into emergency mode, it will then try to exit the building
	 */
	public void SetEmergency();
	
	/**
	 * Gets the velocity of the movement during the last time step
	 * @return Velocity of last movement
	 */
	public Point2D.Float GetLastMovement();
	
	/**
	 * Gets the average speed of the agent since it was last reset
	 * @return Average speed since last reset
	 */
	public float GetAverageSpeed();
	
	/**
	 * Resets the internal values used to calculate average speed to zero
	 */
	public void ResetAverageSpeed();
	
	public int timeTakenForItinerary();
	/**
	 * Gets the current health of the agent
	 * @return The health of the agent
	 */
	public Health GetHealth();
}
