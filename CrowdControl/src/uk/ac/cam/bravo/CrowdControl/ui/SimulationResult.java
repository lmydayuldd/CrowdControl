package uk.ac.cam.bravo.CrowdControl.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uk.ac.cam.bravo.CrowdControl.agent.AgentInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.BuildingPlanI;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.StatisticsInterface;

public class SimulationResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private BuildingPlanI plan;

	private boolean evacuationStarted = false;
	private int evacuationStart = Integer.MAX_VALUE;

	private List<StatisticsData> statdata = new ArrayList<StatisticsData>();
	private List<List<AgentInterface>> agents = new ArrayList<List<AgentInterface>>();
	
	private final int totalRooms;
	
	public SimulationResult(BuildingPlanI plan) {
		this.plan = plan;
		int tr = 0;
		for (int i = 0; i < plan.getFloors(); i++) {
			tr += plan.getRooms(i).size();
		}
		totalRooms = tr;
	}
	
	public List<AgentInterface> getAgentsAt(int time) {
		return agents.get(time);
	}
	
	public void addAgents(List<AgentInterface> as, boolean emergency) {
		List<AgentInterface> agentsAtTime = new ArrayList<AgentInterface>(as.size());
		for (AgentInterface a : as) agentsAtTime.add(new AgentData(a));
		agents.add(agentsAtTime);
		if (emergency && !evacuationStarted) {
			evacuationStarted = true;
			evacuationStart = available();
		}
	}
	
//	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
//		out.writeObject(plan);
//		out.writeBoolean(evacuationStarted);
//		out.writeInt(evacuationStart);
//		out.flush();
//		
//		out.writeObject(statdata);
//		out.writeInt(agents.size());
//		out.flush();
//		
//		for (List<AgentInterface> l : agents) {
//			out.writeObject(l);
//			out.flush();
//		}
//	}
//	
//	@SuppressWarnings("unchecked")
//	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
//		plan = (BuildingPlanI) in.readObject();
//		evacuationStarted = in.readBoolean();
//		evacuationStart = in.readInt();
//		
//		statdata = (List<StatisticsData>) in.readObject();
//		
//		agents = new ArrayList<List<AgentInterface>>();
//		for (int i = in.readInt(); i > 0; i--) {
//			agents.add((List<AgentInterface>) in.readObject());
//		}
//	}

	public int available() {
		return agents.size() - 1;
	}
	
	public BuildingPlanI getBuildingPlan() {
		return plan;
	}
	
	public int getEvacuationStart() {
		return evacuationStart;
	}
	
	public void addStats(StatisticsInterface stats) {
		statdata.add(new StatisticsData(stats, available(), totalRooms));
	}
	
	public StatisticsData getStatsAt(int time) {
		return statdata.get(time);
	}
}
