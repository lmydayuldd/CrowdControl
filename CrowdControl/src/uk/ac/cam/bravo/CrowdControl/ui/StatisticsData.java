package uk.ac.cam.bravo.CrowdControl.ui;

import java.io.Serializable;
import java.util.Collection;

import uk.ac.cam.bravo.CrowdControl.simulator.forUI.StatisticsInterface;

public class StatisticsData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final float averageSpeed;
	public final float itineraryEfficiency;
	public final int evacuationDuration;
	public final float micromorts;
	public final int injured;
	public final float avgFatalities;
	public final int totalFatalities;
	
	public StatisticsData(StatisticsInterface stats, int time, float rooms) {
		averageSpeed = stats.getAverageSpeed();
		
		float avg = 0;
		Collection<float[]> effs = stats.getItineraryEfficiency().values();
		for (float[] eff : effs) {
			avg += eff[1] - eff[0];
		}
		itineraryEfficiency = avg / effs.size();
		
		evacuationDuration = stats.getEvacuationDuration(time);
		
		micromorts = stats.getMicromorts();
		injured = stats.getInjured();
		
		avg = 0;
		Collection<Integer> deaths = stats.getDeathsPerRoom().values();
		for (Integer d : deaths) avg += d;
		avgFatalities = avg / rooms;
		totalFatalities = (int) avg;
	}
}
