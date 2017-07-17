package uk.ac.cam.bravo.CrowdControl.ui;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class VitalsPanel extends Panel {
	private static final int DETAIL_LABEL_HEIGHT = 50;
	private static final GridData LABEL_GRID_DATA = new GridData(SWT.FILL, SWT.CENTER, true, false);
	static {
		LABEL_GRID_DATA.heightHint = 20;
	}
	
	private static final DecimalFormat STATS_FORMAT = new DecimalFormat();
	private static final DecimalFormat SPEED_FORMAT = new DecimalFormat();
	static {
		STATS_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
		STATS_FORMAT.setMaximumFractionDigits(2);
		STATS_FORMAT.setMinimumFractionDigits(2);
		SPEED_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
		SPEED_FORMAT.setMaximumFractionDigits(5);
		SPEED_FORMAT.setMinimumFractionDigits(5);
	}
	
	public static enum Mode {NONE("", SWT.COLOR_BLACK), SIM("Simulation", SWT.COLOR_DARK_GREEN), EVACUATION("Evacuation", SWT.COLOR_RED), REPLAY("Replay", SWT.COLOR_DARK_BLUE);
		public final String modeString;
		public final int colour;
		
		private Mode(String s, int colour) {
			modeString =s ;
			this.colour = colour;
		}
	}
	
	private final Display display;
	
	private final List<Label> clearList = new ArrayList<Label>();
	private final Label mode, speed, currentTime, currentPopulation, avgSpeed, efficiency, evacDuration, micromorts, injured, avgFatalities, totalFatalities;
	private final Button evacuate;
	
	private final Composite vitalsComposite;
	
	private Label createLabel(String text, GridData layoutData, boolean separator, boolean addToClear) {
		Label label = new Label(vitalsComposite, (separator) ? SWT.SEPARATOR | SWT.HORIZONTAL : SWT.NONE); 
		label.setLayoutData(layoutData);
		label.setAlignment(SWT.CENTER);
		label.setText(text);
		if (addToClear) clearList.add(label);
		return label;
	}
	
	public VitalsPanel(Composite parent, int style, String title, Display d) {
		super(parent, style, title, d);
		
		display = d;
		Color detailColour = d.getSystemColor(SWT.COLOR_RED);

		panel.setLayout(new FillLayout());
		ScrolledComposite scrolls = new ScrolledComposite(panel, SWT.V_SCROLL);
		scrolls.setLayout(new FillLayout());
		
		vitalsComposite = new Composite(scrolls, SWT.NONE);
		vitalsComposite.setLayout(new GridLayout(1, false));
		
		createLabel(UIS.CURRENT_MODE, LABEL_GRID_DATA, false, false);
		
		GridData detailGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		detailGridData.heightHint = DETAIL_LABEL_HEIGHT;
		
		mode = createLabel("", detailGridData, false, true);
		
		FontData[] fD = mode.getFont().getFontData();
		for (FontData f : fD) {
			f.setHeight(DETAIL_LABEL_HEIGHT / 2);
			f.setStyle(SWT.BOLD);
		}
		final Font detailFont = new Font(display, fD);
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				detailFont.dispose();
			}
		});
		
		mode.setFont(detailFont);
		mode.setSize(mode.getSize().x, DETAIL_LABEL_HEIGHT);
		
		createLabel("", LABEL_GRID_DATA, true, false);
		createLabel(UIS.CURRENT_TIME, LABEL_GRID_DATA, false, false);
		currentTime = createDetailLabel(detailColour, detailFont, detailGridData);
		
		createLabel("", LABEL_GRID_DATA, true, false);
		createLabel(UIS.CURRENT_SPEED, LABEL_GRID_DATA, false, false);
		speed = createDetailLabel(detailColour, detailFont, detailGridData);
		
		createLabel("", LABEL_GRID_DATA, true, false);
		createLabel(UIS.CURRENT_POPULATION, LABEL_GRID_DATA, false, false);
		currentPopulation = createDetailLabel(detailColour, detailFont, detailGridData);
		
		evacuate = new Button(vitalsComposite, SWT.PUSH);
		GridData evacuateGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		evacuateGridData.heightHint = DETAIL_LABEL_HEIGHT;
		evacuate.setLayoutData(evacuateGridData);
		
		if (!SWT.getPlatform().equals("win32")) {
			evacuate.setBackground(display.getSystemColor(SWT.COLOR_RED));
			evacuate.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
		}
		
		evacuate.setFont(detailFont);
		evacuate.setEnabled(false);
		
		createLabel("", LABEL_GRID_DATA, true, false);
		createLabel(UIS.MOVE_SPEED, LABEL_GRID_DATA, false, false);
		avgSpeed = createDetailLabel(detailColour, detailFont, detailGridData);
		
		createLabel("", LABEL_GRID_DATA, true, false);
		createLabel(UIS.ITINERARY_INEFFICIENCY, LABEL_GRID_DATA, false, false);
		efficiency = createDetailLabel(detailColour, detailFont, detailGridData);
		
		createLabel("", LABEL_GRID_DATA, true, false);
		createLabel(UIS.EVACUATION_DURATION, LABEL_GRID_DATA, false, false);
		evacDuration = createDetailLabel(detailColour, detailFont, detailGridData);
		
		createLabel("", LABEL_GRID_DATA, true, false);
		createLabel(UIS.MICROMORTS, LABEL_GRID_DATA, false, false);
		micromorts = createDetailLabel(detailColour, detailFont, detailGridData);
		
		createLabel("", LABEL_GRID_DATA, true, false);
		createLabel(UIS.INJURED, LABEL_GRID_DATA, false, false);
		injured = createDetailLabel(detailColour, detailFont, detailGridData);
		
		createLabel("", LABEL_GRID_DATA, true, false);
		createLabel(UIS.AVERAGE_FATALITIES, LABEL_GRID_DATA, false, false);
		avgFatalities = createDetailLabel(detailColour, detailFont, detailGridData);
		
		createLabel("", LABEL_GRID_DATA, true, false);
		createLabel(UIS.TOTAL_FATALITIES, LABEL_GRID_DATA, false, false);
		totalFatalities = createDetailLabel(detailColour, detailFont, detailGridData);
		
		createLabel("", LABEL_GRID_DATA, true, false);
		
		vitalsComposite.setSize(vitalsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolls.setExpandHorizontal(true);
		scrolls.setContent(vitalsComposite);
		
	}

	private Label createDetailLabel(Color detailColour, Font detailFont, GridData detailGridData) {
		Label label = createLabel("", detailGridData, false, true);
		label.setForeground(detailColour);
		label.setFont(detailFont);
		label.setSize(label.getSize().x, DETAIL_LABEL_HEIGHT);
		return label;
	}
	
	public void enableEvacuate(boolean enabled) {
		evacuate.setEnabled(enabled);
	}
	
	public void setTime(String s) {
		currentTime.setText(s);
	}
	
	public void setPopulation(String s) {
		currentPopulation.setText(s);
	}
	
	public void setEvacuateText(String s) {
		evacuate.setText(s);
	}
	
	public void removeEvacuateListener(int eventType, Listener handler) {
		evacuate.removeListener(eventType, handler);
	}

	public void addEvacuateListener(int eventType, Listener handler) {
		evacuate.addListener(eventType, handler);
	}
	
	@Override
	public void clear() {
		setMode(Mode.NONE);
		for (Label l : clearList) l.setText("");		
		enableEvacuate(false);
	}
	
	public void setMode(Mode m) {
		mode.setForeground(display.getSystemColor(m.colour));
		mode.setText(m.modeString);
		enableEvacuate(m == Mode.SIM);
	}
	
	public void setStatistics(StatisticsData stats) {
		avgSpeed.setText(SPEED_FORMAT.format(stats.averageSpeed / 10f) + " m/s");
		efficiency.setText(STATS_FORMAT.format(stats.itineraryEfficiency / 10f)  + " s");
		evacDuration.setText(String.valueOf(stats.evacuationDuration / 10f) + " s");
		micromorts.setText(STATS_FORMAT.format(stats.micromorts));
		injured.setText(String.valueOf(stats.injured));
		avgFatalities.setText(SPEED_FORMAT.format(Float.isNaN(stats.avgFatalities) ? 0f : stats.avgFatalities));
		totalFatalities.setText(String.valueOf(stats.totalFatalities));
	}
	
	public void setSpeed(float s) {
		speed.setText(s + "x");
	}
}
