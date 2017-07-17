package uk.ac.cam.bravo.CrowdControl.simulator.forUI;

import java.util.List;

import uk.ac.cam.bravo.CrowdControl.agent.AgentInterface;

/*
* The Simulator class runs the simulation and interacts with the UI.
* Has an instance of the AgentManager clas, an instance of the Building
* class and an instance of the Statistics class. 
*/

public interface SimulatorInterface {
    
    /***************************************************************************
    * UI interface
    ***************************************************************************/

	public List<AgentInterface> doSimulationStep();

   /* public void startSimulation();  // Option to start simulation with an initial agent state
    
    public void pauseSimulation();

    public void stopSimulation();

    
    * This writes the entire simulation to a file as well as the statistics 
    * associated with it.
    
    
    public boolean saveSimulation(String filename);*/

    public void replaySimulation(String filename);

    public void setSimulationMode(boolean isEmergency);

    //public void setTimeStep(int timeStep);

    public BuildingPlanI getBuildingPlan();

    public List<AgentInterface> getWorldState();    

    public StatisticsInterface getStatistics();

    public int getTimestamp();
    
    public boolean getIsEmergency();
}
