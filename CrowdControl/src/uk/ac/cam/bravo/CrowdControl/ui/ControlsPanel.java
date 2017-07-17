package uk.ac.cam.bravo.CrowdControl.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;

public class ControlsPanel  extends Panel{
	public static final int BUTTON_BEGIN = 0;
	public static final int BUTTON_REWIND = 1;
	public static final int BUTTON_PLAYPAUSE = 2;
	public static final int BUTTON_FASTFORWARD = 3;
	public static final int BUTTON_END = 4;
	
	private static final ImageData PLAY_IMAGE = new ImageData(ControlsPanel.class.getResourceAsStream("images/play.png"));
	private static final ImageData PAUSE_IMAGE = new ImageData(ControlsPanel.class.getResourceAsStream("images/pause.png"));
	private static final ImageData[] BUTTON_IMAGES = new ImageData[] {new ImageData(ControlsPanel.class.getResourceAsStream("images/begin.png")),
																	  new ImageData(ControlsPanel.class.getResourceAsStream("images/rewind.png")),
																	  PLAY_IMAGE,
																	  new ImageData(ControlsPanel.class.getResourceAsStream("images/ff.png")),
																	  new ImageData(ControlsPanel.class.getResourceAsStream("images/end.png"))};
	
	public final int BUTTONS = BUTTON_IMAGES.length;
	
	private final Composite controls;
	private final Composite progressComposite;
	private final Label medprogress, maxprogress;
	private final Scale progress;
	private final List<Button> buttons = new ArrayList<Button>(BUTTON_IMAGES.length);
	
	private boolean paused = true;
	
	private final Image play_image;
	private final Image pause_image;
	
	public ControlsPanel(Composite parent, int style, String title, Display d) {
		super(parent, style, title, d);
		
	    controls = new Composite(panel, SWT.NONE);
	    controls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    FillLayout controlsFillLayout = new FillLayout();
	    controlsFillLayout.type = SWT.VERTICAL;
	    controls.setLayout(controlsFillLayout);
		
	    progressComposite = new Composite(controls, SWT.NONE);
	    progressComposite.setLayout(new FormLayout());
	    
	    Label minprogress = new Label(progressComposite, SWT.NONE);
	    minprogress.setText("0");
	    
	    medprogress = new Label(progressComposite, SWT.NONE);
	    medprogress.setAlignment(SWT.CENTER);
	    medprogress.setText("0");
	    
	    maxprogress = new Label(progressComposite, SWT.NONE);
	    maxprogress.setText("0");
	    maxprogress.setAlignment(SWT.RIGHT);
	    
	    progress = new Scale(progressComposite, SWT.NONE);
	    progress.setMaximum(0);
	    
	    FormData minData = new FormData();
	    minData.left = new FormAttachment(0, 11);
	    
	    FormData maxData = new FormData();
	    maxData.right = new FormAttachment(100, -11);
	    maxData.left = new FormAttachment(80, 0);
	    
	    FormData medData = new FormData();
	    medData.left = new FormAttachment(40, 0);
	    medData.right = new FormAttachment(60, 0);
	    
	    FormData progressData = new FormData();
	    progressData.top = new FormAttachment(medprogress,1);
	    progressData.left = new FormAttachment(0, 0);
	    progressData.right = new FormAttachment(100, 0);
	    progressData.bottom = new FormAttachment(100, 0);
	    
	    minprogress.setLayoutData(minData);
	    medprogress.setLayoutData(medData);
	    maxprogress.setLayoutData(maxData);
	    progress.setLayoutData(progressData);
	    
		Composite buttonsComposite = new Composite(controls, SWT.NONE);
		buttonsComposite.setLayout(new FillLayout());
		play_image = new Image(display, PLAY_IMAGE);
		for (ImageData id : BUTTON_IMAGES) {
			final Image i = (id == PLAY_IMAGE) ? play_image : new Image(display, id);
			addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					i.dispose();
				}
			});
			Button b = new Button(buttonsComposite, SWT.NONE);
			b.setImage(i);
			buttons.add(b);
		}
		
		pause_image = new Image(display, PAUSE_IMAGE);
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				pause_image.dispose();
			}
		});
	}
	
	public void updateProgress(int max, int pos) {
		progress.setMaximum(max);
		maxprogress.setText(String.valueOf(max));
		medprogress.setText(String.valueOf(max / 2));
		progress.setSelection(pos);
		layout();
	}
	
	public int getProgress() {
		return progress.getSelection();
	}
	
	public int maximiseProgress() {
		int p = progress.getMaximum();
		progress.setSelection(p);
		return p;
	}
	
	public int minimiseProgress() {
		int p = progress.getMinimum();
		progress.setSelection(p);
		return p;
	}
	
	public void addProgressListener(int eventType, Listener listener) {
		progress.addListener(eventType, listener);
	}
	
	public void removeProgressListener(int eventType, Listener listener) {
		progress.removeListener(eventType, listener);
	}
	
	public void addButtonListener(int button, int eventType, Listener listener) throws IndexOutOfBoundsException {
		buttons.get(button).addListener(eventType, listener);
	}
	
	public void removeBarListener(int button, int eventType, Listener listener) throws IndexOutOfBoundsException {
		buttons.get(button).removeListener(eventType, listener);
	}
	
	public Button getButton(int id) {
		return buttons.get(id);
	}
	
	public void togglePlayPauseButton() {
		buttons.get(BUTTON_PLAYPAUSE).setImage((paused) ? pause_image : play_image);
		paused = !paused;
	}
	
	@Override
	public void clear() {
		medprogress.setText("0");
		maxprogress.setText("0");
		progress.setMaximum(0);
		progress.setSelection(0);
	}
}
