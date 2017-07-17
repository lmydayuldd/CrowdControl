package uk.ac.cam.bravo.CrowdControl.simulator;

public class MissingConfigFilesException extends Exception {

	private static final long serialVersionUID = 1L;

	private String message;
	
	MissingConfigFilesException(int total, int nfiles) {
		int m = total - nfiles;
		message = "Missing " + m + "XML files.";
	}
	@Override
	public String getMessage() {
		return message;
	}

}
