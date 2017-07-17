package uk.ac.cam.bravo.CrowdControl.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import uk.ac.cam.bravo.CrowdControl.agent.ParticleAgent;

public class ConfigBox {
	public boolean result;
	
	private static final int OK_BUTTON_WIDTH = 80;
	
	private Text createItem(Shell parent, String label, String start) {
		Label ws = new Label (parent, SWT.NONE);
		ws.setText(label);
		ws.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Text wsText = new Text(parent, SWT.BORDER);
		wsText.setText(start);
		wsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		return wsText;
	}
	
	public ConfigBox(Display display, Shell shell, String title) {
		final Shell dialog = new Shell (shell, SWT.DIALOG_TRIM);
		dialog.setText(title);
		
		dialog.setLayout(new GridLayout(2, false));	
		dialog.setSize(600, 120);
		
		final Text walkspeed = createItem(dialog, UIS.CONFIG_WALK_SPEED, String.valueOf(ParticleAgent.normalSpeed * 0.06f));
		final Text injury = createItem(dialog, UIS.CONFIG_INJURY_PRESSURE, String.valueOf(ParticleAgent.injuryPressure));
		final Text death = createItem(dialog, UIS.CONFIG_DEATH_PRESSURE, String.valueOf(ParticleAgent.deathPressure));
		final Text drag = createItem(dialog, UIS.CONFIG_DRAG_FACTOR, String.valueOf(ParticleAgent.dragFactor));
		final Text interaction = createItem(dialog, UIS.CONFIG_INTERACTION_DISTANCE, String.valueOf(ParticleAgent.interactionDistance));
		
		Button okButton = new Button (dialog, SWT.PUSH);
		okButton.setText (UIS.DIALOG_OK);
		GridData okButtonGridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		okButtonGridData.widthHint = OK_BUTTON_WIDTH;
		okButton.setLayoutData(okButtonGridData);
		
		Button cancelButton = new Button (dialog, SWT.PUSH);
		cancelButton.setText (UIS.DIALOG_CANCEL);
		GridData cancelButtonGridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		cancelButtonGridData.widthHint = OK_BUTTON_WIDTH;
		cancelButton.setLayoutData(cancelButtonGridData);
		
		cancelButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				dialog.dispose();			
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			
		});
		
		okButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					configure(Float.parseFloat(walkspeed.getText()) / 0.06f,
							  Float.parseFloat(drag.getText()),
							  Float.parseFloat(injury.getText()),
							  Float.parseFloat(death.getText()),
							  Float.parseFloat(interaction.getText()));
					result = true;
					dialog.dispose();
				} catch (NumberFormatException e1) {
				}	
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			
		});
		
		dialog.setDefaultButton(okButton);
		dialog.pack ();
		dialog.open ();
		
		while (!dialog.isDisposed()) {
		      if (!display.readAndDispatch()) display.sleep();
		    }
	}
	
	public static void configure(float walkspeed, float drag, float injury, float death, float interaction) {
		ParticleAgent.normalSpeed = walkspeed;
		ParticleAgent.emergencySpeed = ParticleAgent.normalSpeed * 2f;
		ParticleAgent.injuredSpeed = 3f * ParticleAgent.normalSpeed / 5f;
		ParticleAgent.walkForce = ParticleAgent.normalSpeed * 0.5f;
		ParticleAgent.interactionForce = ParticleAgent.walkForce * 50f;
		ParticleAgent.dragFactor = drag;
		ParticleAgent.injuryPressure = injury;
		ParticleAgent.deathPressure = death;
		ParticleAgent.interactionDistance = interaction;
	}
}
