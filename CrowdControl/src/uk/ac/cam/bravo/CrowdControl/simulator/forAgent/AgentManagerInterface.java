
package uk.ac.cam.bravo.CrowdControl.simulator.forAgent;

import java.awt.geom.Point2D;
import java.util.List;

import uk.ac.cam.bravo.CrowdControl.agent.AgentInterface;

/* The AgentManager will keep track of the positions of agents and update them. 
*  Agents also use it for higher level pathfinding and neighbour finding.
*/

public interface AgentManagerInterface {

    /***************************************************************************
    * Agent interface
    ***************************************************************************/

    /*Return a set of agents that are currently close to agent a and how close they are
    * how close
    * we need them will have to be decided by the agent team.
    *
    * @param agent an Agent object  
    *
    * @return set of agents that are neighbours of agent
    */
    public Iterable<AgentInterface> getAgentsNear(RoomInterface room, Point2D.Float position, float radius);

    /* Return the set of all paths that will lead the agent from a given start room to
    * a given goal room. A path is simply an ordered list/queue? of rooms to go 
    * through.
    *
    * @param start the start room
    * 
    * @param goal the goal room
    *
    * @return set of doors leading from start to goal
    */
    public List<DoorInterface> getHighLevelPath(RoomInterface start, RoomInterface goal, Point2D.Float currentPos);

    public void updateCurrentRoom(AgentInterface agent, RoomInterface prevRoom, RoomInterface newRoom);
    
    public RoomInterface getOutside();

    public RoomInterface getOutside(AgentInterface a);
}
