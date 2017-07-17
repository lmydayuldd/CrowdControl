/*
 * TODO:

- Config box
- Progress bars
 */

package uk.ac.cam.bravo.CrowdControl.ui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import uk.ac.cam.bravo.CrowdControl.agent.AgentInterface;
import uk.ac.cam.bravo.CrowdControl.agent.Health;
import uk.ac.cam.bravo.CrowdControl.simulator.Simulator;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.SimulatorInterface;

public class CrowdControl {

	private static final int BASE_SIM_STEP_INTERVAL = 20;
	private static final float[] SIM_STEP_SPEEDS = new float[] {0.05f, 0.1f, 0.25f, 0.5f, 1f, 2f, 4f, 8f, 10f, 100f};
	
	private static final int[] VERTICAL_WEIGHTS = new int[] { 4, 1 };
	private static final int[] VITALS_WEIGHTS = new int[] { 30, 1 };

	private SimulatorInterface sim;
	private SimulationResult simR;
	
	private VitalsPanel vitals;
	private MapPanel map;
	private ControlsPanel controls;
	private volatile boolean stopSimulation = true;

	private Display display;
	
	private volatile int currentTime = 0;
	private volatile int currentSpeedIndex = 6;
	private volatile int currentAbsoluteSpeed = (int) (BASE_SIM_STEP_INTERVAL * SIM_STEP_SPEEDS[currentSpeedIndex]);
	
	private Shell makeGUI(final Display display) {
		this.display = display;
		Shell shell = new Shell(display);
		shell.setText(UIS.SHELL_TITLE);

		shell.setLayout(new FillLayout());

		SashForm sashShell = new SashForm(shell, SWT.HORIZONTAL);

		createMenuBar(shell, SWT.BAR);

		final Composite leftComposite = new Composite(sashShell, SWT.NONE);
		SashForm rightComposite = new SashForm(sashShell, SWT.VERTICAL);
		leftComposite.setLayout(new GridLayout());

		sashShell.setWeights(VERTICAL_WEIGHTS);

		vitals = new VitalsPanel(rightComposite, SWT.NONE, UIS.VITALS_TITLE, display);
		vitals.setEvacuateText(UIS.EVACUATE);

		vitals.addEvacuateListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (sim != null) {
					sim.setSimulationMode(true);
				}
			}
		});

		map = new MapPanel(leftComposite, SWT.NONE, UIS.MAP, display, new LogPanel(rightComposite, SWT.NONE, UIS.LOG, display));
		map.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		controls = new ControlsPanel(leftComposite, SWT.NONE, UIS.CONTROLS, display);
		controls.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false, 1, 1));

		controls.addButtonListener(ControlsPanel.BUTTON_PLAYPAUSE, SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				buttonPlayPauseEventHandler();
			}
		});

		controls.addButtonListener(ControlsPanel.BUTTON_BEGIN, SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				currentTime = controls.minimiseProgress();
				updateUI();
			}
		});
		
		controls.addButtonListener(ControlsPanel.BUTTON_END, SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				currentTime = controls.maximiseProgress();
				updateUI();
			}
		});
		
		controls.addButtonListener(ControlsPanel.BUTTON_FASTFORWARD, SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (currentSpeedIndex > 0) currentAbsoluteSpeed = (int) (BASE_SIM_STEP_INTERVAL * SIM_STEP_SPEEDS[--currentSpeedIndex]);
				if (simR != null) vitals.setSpeed(1f / SIM_STEP_SPEEDS[currentSpeedIndex]);
			}
		});
		
		controls.addButtonListener(ControlsPanel.BUTTON_REWIND, SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (currentSpeedIndex < SIM_STEP_SPEEDS.length - 1) currentAbsoluteSpeed = (int) (BASE_SIM_STEP_INTERVAL * SIM_STEP_SPEEDS[++currentSpeedIndex]);
				if (simR != null) vitals.setSpeed(1f / SIM_STEP_SPEEDS[currentSpeedIndex]);
			}
		});
		
		controls.addProgressListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				currentTime = controls.getProgress();
				updateUI();
			}
		});

		rightComposite.setWeights(VITALS_WEIGHTS);

		return shell;
	}

	private void loadSimulationResult(File f) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
		SimulationResult r = (SimulationResult) in.readObject();
		clear();
		simR = r;
		
		map.setBuildingPlan(simR.getBuildingPlan());
		
		List<AgentInterface> agents = simR.getAgentsAt(0);
		
		map.setAgents(agents);
		controls.updateProgress(simR.available(), 0);
		
		vitals.setMode(VitalsPanel.Mode.REPLAY);
		vitals.setTime("0 s");
		vitals.setPopulation(String.valueOf(agents.size()));
		vitals.setSpeed(1f / SIM_STEP_SPEEDS[currentSpeedIndex]);
		vitals.layout();
	}

	private void loadSimulation(JarFile simConfig, Shell parent) throws IOException {
		if (!(new ConfigBox(display, parent, UIS.CONFIG_TITLE)).result) return;
		try {
			InputStream[] inputs = new InputStream[4];

			Enumeration<JarEntry> entries = simConfig.entries();
			while (entries.hasMoreElements()) {
				JarEntry e = entries.nextElement();
				if (e.getName().toLowerCase().endsWith(".xml"))
					inputs[Integer.parseInt(e.getName().substring(0, 1))] = simConfig.getInputStream(e);
			}
			clear();
			sim = new Simulator(Arrays.asList(inputs));
			simR = new SimulationResult(sim.getBuildingPlan());
			vitals.enableEvacuate(true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException();
		}

		map.setBuildingPlan(sim.getBuildingPlan());
		List<AgentInterface> agents = sim.getWorldState();
		simR.addAgents(agents, false);
		simR.addStats(sim.getStatistics());
		map.setAgents(agents);

		vitals.setMode(VitalsPanel.Mode.SIM);
		vitals.setTime(String.valueOf(sim.getTimestamp() / 10f) + " s");
		vitals.setPopulation(String.valueOf(agents.size()));
		vitals.setSpeed(1f / SIM_STEP_SPEEDS[currentSpeedIndex]);
		vitals.layout();
	}

	private void saveSimulation(File f) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
		out.writeObject(simR);
		out.flush();
		out.close();
	}
	
	private void buttonPlayPauseEventHandler() {
		// controls.getButton(ControlsPanel.BUTTON_PLAYPAUSE).setEnabled(false);
		if (simR != null) {
			controls.togglePlayPauseButton();
			if (stopSimulation) {
				stopSimulation = false;
				display.syncExec(simUpdater);
			} else {
				stopSimulation = true;
			}
		}
	}
	
	private void stop() {
		if (!stopSimulation) {
			buttonPlayPauseEventHandler();
			stopSimulation = true;
			while(display.readAndDispatch());
		}
	}
	
	private void clear() {
		stop();
		
		currentTime = 0;
		
		sim = null;
		simR = null;
		
		map.clear();
		vitals.clear();
		vitals.enableEvacuate(false);
		controls.clear();
	}
	
	private void updateUI() {
		if (sim == null && simR == null) return;
		for (; simR.available() < currentTime;) {
			if (sim == null) {
				stop();
				currentTime = simR.available();
				break;
			} else {
				simR.addAgents(sim.doSimulationStep(), sim.getIsEmergency());
				simR.addStats(sim.getStatistics());
			}
		}
		
		List<AgentInterface> agents = simR.getAgentsAt(currentTime);
		
		vitals.setTime(String.valueOf(currentTime / 10f) + " s");
		vitals.setPopulation(new Integer(agents.size()).toString());
		vitals.setMode((currentTime >= simR.getEvacuationStart()) ? VitalsPanel.Mode.EVACUATION : (sim == null) ? VitalsPanel.Mode.REPLAY : VitalsPanel.Mode.SIM);
		StatisticsData stats = simR.getStatsAt(currentTime);
		vitals.setStatistics(stats);
		vitals.layout();
		
		controls.updateProgress(simR.available(), currentTime);
		
		map.setAgents(agents);
		map.updateMap();
		
		if (sim != null) {
			for (AgentInterface agent : agents) {
				if (agent.GetHealth() == Health.Healthy
						|| agent.GetHealth() == Health.Injured)
					return;
			}
			sim = null;
		}
	}
	
	private final Runnable simUpdater = new Runnable() {
		@Override
		public void run() {
			try {
				if (!stopSimulation) {
					updateUI();
					currentTime++;
					display.timerExec(currentAbsoluteSpeed, simUpdater);
				}

			} catch (SWTException ex) {
				// TODO: This is inelegant - do it properly later
			}
		}
	};

	public void runGUI() {
		Display display = new Display();
		Shell shell = makeGUI(display);

		shell.setMaximized(true);
		shell.open();
		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// If no more entries in the event queue
				display.sleep();
			}
		}
		display.dispose();
	}

	private void createMenuBar(final Shell parent, int style) {

		Menu menuBar = new Menu(parent, style);

		MenuItem cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeFileMenu.setText(UIS.MENU_FILE_TITLE);
		Menu fileMenu = new Menu(parent, SWT.DROP_DOWN);
		cascadeFileMenu.setMenu(fileMenu);

		MenuItem file_loadsimconfig = new MenuItem(fileMenu, SWT.PUSH);
		file_loadsimconfig.setText(UIS.MENU_FILE_LOADSIMCONFIG);
		MenuItem file_loadsimresult = new MenuItem(fileMenu, SWT.PUSH);
		file_loadsimresult.setText(UIS.MENU_FILE_LOADSIMRESULT);
		MenuItem file_savesimresult = new MenuItem(fileMenu, SWT.PUSH);
		file_savesimresult.setText(UIS.MENU_FILE_SAVESIMRESULT);
		MenuItem file_clearItem = new MenuItem(fileMenu, SWT.PUSH);
		file_clearItem.setText(UIS.MENU_FILE_CLEAR);
		MenuItem file_exitItem = new MenuItem(fileMenu, SWT.PUSH);
		file_exitItem.setText(UIS.MENU_FILE_EXIT);

		MenuItem cascadeSimMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeSimMenu.setText(UIS.MENU_SIM_TITLE);
		Menu simMenu = new Menu(parent, SWT.DROP_DOWN);
		cascadeSimMenu.setMenu(simMenu);

		MenuItem sim_runItem = new MenuItem(simMenu, SWT.PUSH);
		sim_runItem.setText(UIS.MENU_SIM_RUN);
		MenuItem sim_stopItem = new MenuItem(simMenu, SWT.PUSH);
		sim_stopItem.setText(UIS.MENU_SIM_STOP);

		parent.setMenuBar(menuBar);

		file_loadsimconfig.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				File f;
				if ((f = fileDialog(parent, SWT.OPEN, new String[] { "Jar file (*.jar)", "All Files (*)" }, new String[] { "*.jar" })) != null) {
					try {
						loadSimulation(new JarFile(f), parent);
					} catch (IOException e1) {
						e1.printStackTrace();
						new Dialog(parent, UIS.DIALOG_TITLE, UIS.DIALOG_LOAD_TEXT);
					}
					System.out.println("Room data set:" + f.getName());
				}
			}
		});

		file_loadsimresult.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				File f;
				if ((f = fileDialog(parent, SWT.SAVE, new String[] { "Sim file (*.sim)", "All Files (*)" }, new String[] { "*.sim" })) != null) {
					try {
						loadSimulationResult(f);
					} catch (Exception e1) {
						e1.printStackTrace();
						new Dialog(parent, UIS.DIALOG_TITLE, UIS.DIALOG_LOAD_TEXT);
					}
				}
			}
		});
		
		file_savesimresult.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (simR == null) {
					new Dialog(parent, UIS.DIALOG_TITLE, UIS.DIALOG_NO_SAVE_TEXT);
					return;
				}
				File f;
				if ((f = fileDialog(parent, SWT.SAVE, new String[] { "Sim file (*.sim)", "All Files (*)" }, new String[] { "*.sim" })) != null) {
					try {
						saveSimulation(f);
					} catch (IOException e1) {
						e1.printStackTrace();
						new Dialog(parent, UIS.DIALOG_TITLE, UIS.DIALOG_SAVE_TEXT);
					}
				}
			}
		});
		
		file_clearItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clear();
			}
		});

		file_exitItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((Shell) parent).getDisplay().dispose();
				System.exit(0);
			}
		});

		sim_runItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (stopSimulation)
					buttonPlayPauseEventHandler();
			}
		});
		
		sim_stopItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!stopSimulation)
					buttonPlayPauseEventHandler();
			}
		});
	}

	private File fileDialog(Shell shell, int type, String[] filterNames, String[] filterExtensions) {
		FileDialog dialog = new FileDialog(shell, type);

		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);

		String path = dialog.open();

		if (path != null) {
			return new File(path);
		}
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		PrintStream log = System.out;
		try {
			switch (args.length) {
				case 0:
					new CrowdControl().runGUI();
					break;
				case 9:
					ConfigBox.configure(Float.parseFloat(args[4]) / 0.06f, Float.parseFloat(args[5]), Float.parseFloat(args[6]), Float.parseFloat(args[7]), Float.parseFloat(args[8]));
				case 4:
					System.setOut(new PrintStream(new OutputStream() {
						@Override public void write(int b) throws IOException {}
					}));
					CommandLineSimulator cmdsim = new CommandLineSimulator(args[0], args[1], log);
					cmdsim.run(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
					cmdsim.close();
					break;
				default:
					System.out.println(UIS.ARGUMENTS_ERROR_1);
					System.out.println(UIS.ARGUMENTS_ERROR_2);
			}
		} catch (IOException e) {
			log.println("Cannot read file:");
			log.println(e.getMessage());
		} catch (NumberFormatException e) {
			log.println("Invalid arguments!");
		} catch (Exception e) {
			log.println("Error!");
			e.printStackTrace(log);
		}
	}
}
