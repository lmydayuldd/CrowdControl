package uk.ac.cam.bravo.CrowdControl.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

public class LogPanel extends Panel {
	private final Text log;
	
	public LogPanel(Composite parent, int style, String title, Display d) {
		super(parent, style, title, d);
		
		log = new Text(panel, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		log.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	}
	
	public void appendLog(String s) {
		log.append(s + "\n");
	}

	public void clearLog() {
		log.setText("");
	}
}
