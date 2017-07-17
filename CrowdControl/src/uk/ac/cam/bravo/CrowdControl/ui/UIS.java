package uk.ac.cam.bravo.CrowdControl.ui;

public class UIS {
	public static final String SHELL_TITLE = "Crowd Control";
	public static final String MAP = "Building Map";
	
	public static final String MENU_FILE_TITLE = "&File";
	public static final String MENU_FILE_LOADSIMCONFIG = "Load Simulation &Configuration";
	public static final String MENU_FILE_LOADSIMRESULT = "Load Simulation &Result";
	public static final String MENU_FILE_SAVESIMRESULT = "&Save Simulation Result";
	public static final String MENU_FILE_EXIT = "&Exit";
	public static final String MENU_FILE_CLEAR = "&Clear";
	
	public static final String MENU_SIM_TITLE = "&Simulator";
	public static final String MENU_SIM_RUN = "&Run";
	public static final String MENU_SIM_STOP = "&Stop";	
	
	public static final String VITALS_TITLE = "Information";
	public static final String CURRENT_MODE = "Mode";
	public static final String CURRENT_SPEED = "Simulation Speed";
	public static final String CURRENT_TIME = "Current Time";
	public static final String CURRENT_POPULATION = "Current Population";
	public static final String EVACUATE = "Evacuate";
	public static final String MOVE_SPEED = "Average Movement Speed";
	public static final String ITINERARY_INEFFICIENCY = "Average Itinerary Overtime";
	public static final String EVACUATION_DURATION = "Evacuation Duration";
	public static final String MICROMORTS = "Micromorts";
	public static final String INJURED = "Total Injured";
	public static final String AVERAGE_FATALITIES = "Average Fatality Per Room";
	public static final String TOTAL_FATALITIES = "Total Fatalities";
	
	public static final String DIALOG_OK = "&OK";
	public static final String DIALOG_CANCEL = "&CANCEL";
	public static final String DIALOG_TITLE = "Error";
	public static final String DIALOG_LOAD_TEXT = "Unable to load file.";
	public static final String DIALOG_SAVE_TEXT = "Unable to save simulation.";
	public static final String DIALOG_NO_SAVE_TEXT = "No results are available to save.";
	
	public static final String LOG = "Log";
	
	public static final String CONTROLS = "Simulation Controls";
	
	public static final String USAGE = "Usage: CrowdControl <Building File> <Furniture Definitions> <Furniture Locations File> <Itinerary File>";
	
	public static final String FLOOR = "Floor: ";
	
	public static final String ARGUMENTS_ERROR_1 = "Usage: <Input File> <Output File> <Steps> [Evacuation Time] \n"
													+ "Or Usage: <Input File> <Output File> <Steps> [Evacuation Time] <Walk Speed> <Drag Factor> <Injury Force> <Death Force> <Interaction Distance>";
	public static final String ARGUMENTS_ERROR_2 = "Or do not include arguments to invoke the GUI";
	
	public static final String CONFIG_TITLE = "Agent Parameters";
	public static final String CONFIG_WALK_SPEED = "&Walk speed (m/s):";
	public static final String CONFIG_INJURY_PRESSURE = "&Injury pressure:";
	public static final String CONFIG_DEATH_PRESSURE = "&Death pressure:";
	public static final String CONFIG_DRAG_FACTOR = "D&rag factor:";
	public static final String CONFIG_INTERACTION_DISTANCE = "I&nteraction distance (cm):";
}
