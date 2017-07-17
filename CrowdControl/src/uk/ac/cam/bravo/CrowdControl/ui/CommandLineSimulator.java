package uk.ac.cam.bravo.CrowdControl.ui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import uk.ac.cam.bravo.CrowdControl.agent.AgentInterface;
import uk.ac.cam.bravo.CrowdControl.agent.Health;
import uk.ac.cam.bravo.CrowdControl.simulator.Simulator;

public class CommandLineSimulator {

	private final PrintStream log;
	private final ObjectOutputStream out;
	private Simulator sim;

	public CommandLineSimulator(String input, String output, PrintStream log) throws IOException {
		this.log = log;
		sim = loadInput(new JarFile(input));
		out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(output))));
		log.println("Opened " + input + " and " + output);
	}
	
	private Simulator loadInput(JarFile simConfig) throws IOException {
		try {
			InputStream[] inputs = new InputStream[4];

			Enumeration<JarEntry> entries = simConfig.entries();
			while (entries.hasMoreElements()) {
				JarEntry e = entries.nextElement();
				if (e.getName().toLowerCase().endsWith(".xml"))
					inputs[Integer.parseInt(e.getName().substring(0, 1))] = simConfig.getInputStream(e);
			}
			return new Simulator(Arrays.asList(inputs));
		} catch (Exception e) {
			throw new IOException();
		}
	}
	
	public void run(int steps, int evac) throws IOException {
		SimulationResult r = new SimulationResult(sim.getBuildingPlan());
		log.print("Simulating: 0%<");
		int incr = steps / 10;
		r.addAgents(sim.getWorldState(), sim.getIsEmergency());
		r.addStats(sim.getStatistics());
		for (int i = 1; i < steps + 1; i++) {
			if (sim == null) break;
			if (i == evac) sim.setSimulationMode(true);
			List<AgentInterface> agents = sim.doSimulationStep();
			r.addAgents(agents, sim.getIsEmergency());
			r.addStats(sim.getStatistics());
			if (i % incr == 0) log.print("=");
			boolean finish = true;
			for (AgentInterface agent : agents) {
				if (agent.GetHealth() == Health.Healthy
						|| agent.GetHealth() == Health.Injured)
					{
						finish = false;
						break;
					}
			}
			if (finish) sim = null;
		}
		log.println(">100%");
		log.print("Writing results...");
		out.writeObject(r);
		log.println("Done!");
	}
	
	public void close() throws IOException {
		out.flush();
		out.close();
	}
}
