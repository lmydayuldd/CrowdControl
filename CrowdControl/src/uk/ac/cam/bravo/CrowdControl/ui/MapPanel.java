package uk.ac.cam.bravo.CrowdControl.ui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

import uk.ac.cam.bravo.CrowdControl.agent.AgentInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.BuildingPlanI;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.RoomShape;
import uk.ac.cam.bravo.CrowdControl.simulator.forUI.Vertex;

public class MapPanel extends Panel {	
	private static final float BORDER = 500f;
	private static final int DEFAULT_SCALE = 160;
	private static final int MIN_SCALE = DEFAULT_SCALE / 40;
	private static final int MAX_SCALE = 79 * DEFAULT_SCALE / 40;
	private static final int INCR_SCALE = (MAX_SCALE - MIN_SCALE) / 10;
	
	private static final float AGENT_ABSOLUTE_RADIUS = 20f;
	
	private float scale;
	
	private final Color PASSABLE_COLOUR;
	private final Color ROOM_EDGE_COLOUR;
	private final Color ROOM_FILL_COLOUR;
	private final Color AGENT_ALIVE_COLOUR;
	private final Color AGENT_INJURED_COLOUR;
	private final Color AGENT_DEAD_COLOUR;
	
	private final Display display;
	private final LogPanel log;
	private final Combo floorSelector;
	private final Canvas map;
	private final ScrolledComposite scrolls;
	private final Composite scrolledContent;
	private final Scale scaleSlider;
	
	private Transform gcTransform;
	private Rectangle bbox = new Rectangle(0,0,0,0);
	private BuildingPlanI building = null;

	private Integer[] floorNums;
	private int floorIndexSelected;
	
	private List<AgentInterface> agents = new ArrayList<AgentInterface>();
	
	private Image plan;
	
	public MapPanel(Composite parent, int style, String title, Display d, LogPanel log) {
		super(parent, style, title, d);
		
		this.log = log;
		
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (plan != null) plan.dispose();
			}
		});
		
	    display = d;
	    
	    PASSABLE_COLOUR = new Color(display, new RGB(0x66, 0, 0));
	    ROOM_EDGE_COLOUR = new Color(display, new RGB(0x66, 0x66, 0xF0));
	    ROOM_FILL_COLOUR = new Color(display, new RGB(0xCC, 0xCC, 0xFF));
	    AGENT_ALIVE_COLOUR = display.getSystemColor(SWT.COLOR_RED);
	    AGENT_INJURED_COLOUR = display.getSystemColor(SWT.COLOR_YELLOW);
	    AGENT_DEAD_COLOUR = display.getSystemColor(SWT.COLOR_BLACK);
	    
	    panel.setLayout(new GridLayout(2, false));
	    
	    Label floorLabel = new Label(panel, SWT.NONE);
	    floorLabel.setText(UIS.FLOOR);
	    floorLabel.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false, 1, 1));
	    
	    floorSelector = new Combo(panel, SWT.READ_ONLY);
	    floorSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
	    
	    scaleSlider = new Scale(panel, SWT.VERTICAL);
	    scaleSlider.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, true, 1, 1));
		scaleSlider.setMaximum(MAX_SCALE);
		scaleSlider.setMinimum(MIN_SCALE);
	    scaleSlider.setPageIncrement(INCR_SCALE);
	    scaleSlider.setIncrement(INCR_SCALE);

	    scrolls = new ScrolledComposite(panel, SWT.NO_REDRAW_RESIZE | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		scrolledContent = new Composite(scrolls, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE);
		scrolledContent.setLayout(new FillLayout());

	    map = new Canvas(scrolledContent, SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED);
	    map.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
	    
	    scrolls.setContent(scrolledContent);
	    scrolls.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
	    
	    map.setSize(0,0);
	    scrolledContent.setSize(0,0);
	    
	    map.addPaintListener(new PaintListener() {
	        public void paintControl(PaintEvent e) {
	            if (building != null) {
	            	e.gc.setTransform(null);
	            	e.gc.drawImage(plan, 0, 0);
	            	e.gc.setTransform(gcTransform);
	            	drawAgents(e.gc, floorIndexSelected);
	            }
	            
	        }
	    });
	    
	    map.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (building == null) return;
				Transform gcTransformInverse = new Transform(display);
				gcTransformInverse.multiply(gcTransform);
				gcTransformInverse.invert();
				float[] xy = new float[] {e.x, e.y};
				gcTransformInverse.transform(xy);
				MapPanel.this.log.appendLog("(" + xy[0] + ", " + xy[1] + ")");
			}
	    });
	}

	private void drawAgents(GC gc, int floor) {
		int agentDiameter = (int) (AGENT_ABSOLUTE_RADIUS * 2f);
		for (AgentInterface a : agents) {
			if (a.GetRoom().getFloor() == floor) {
				switch (a.GetHealth()) {
					case Healthy: gc.setBackground(AGENT_ALIVE_COLOUR); break;
					case Injured: gc.setBackground(AGENT_INJURED_COLOUR); break;
					case Dead: gc.setBackground(AGENT_DEAD_COLOUR); break;
					default: gc.setBackground(AGENT_ALIVE_COLOUR); break;
				}
				Point2D.Float aPos = a.GetPosition();
				gc.fillOval((int) (aPos.x - AGENT_ABSOLUTE_RADIUS), (int) (aPos.y +  - AGENT_ABSOLUTE_RADIUS), agentDiameter, agentDiameter);
			}
		}
	}
	
	private void drawBuilding(GC gc, int floor) {
		gc.setTransform(gcTransform);
		gc.setForeground(ROOM_EDGE_COLOUR);
		gc.setBackground(ROOM_FILL_COLOUR);
		if (building == null) return;
		List<RoomShape> rooms = building.getRooms(floor);
		for (RoomShape r : rooms) {
	
//			Rectangle2D.Float box = r.getBoundingBox();
//			boolean[][] passable = r.getPassableMap();
//			int width = passable.length;
//			int height = passable[0].length;
//			
//			for(int i = 0; i<width; i++) {
//				for(int j = 0; j<height; j++) {
//					if(passable[i][j]) {
//						
//						// Draw box
//						gc.setBackground(PASSABLE_COLOUR);
//						int old_alpha = gc.getAlpha();
//						gc.setAlpha(100);
//						int x = (int) box.x + (i * 20);
//						int y = (int) box.y - (j * 20);
//						gc.fillRectangle(x, y, 20, 20);
//						gc.setAlpha(old_alpha);
//					}
//				}
//			}

			
			
			List<List<Vertex>> vertexlist = r.getEdges();
			for (List<Vertex> vertices : vertexlist) {
				int s = vertices.size();
				int[] points = new int[s << 1];
				for (int i = 0, j = 0; j < s; j++) {
					Vertex v = vertices.get(j);
					Vertex w = vertices.get((j + 1) % s);
					gc.setLineAttributes(new LineAttributes(v.edgeType.lineWidth / scale));
					gc.drawLine((int) v.x, (int) v.y, (int) w.x, (int) w.y);
					points[i++] = (int) v.x;
					points[i++] = (int) v.y;
				}
				gc.fillPolygon(points);
			}
		}
	}
	
	private void changeScale(Rectangle2D.Float bounding) {
		Point mapSize = scrolls.getSize();
		Point originalScroll = scrolls.getOrigin();
		
		originalScroll.x += mapSize.x / 2;
		originalScroll.y += mapSize.y / 2;
		
		float originalScale = scale;
		scale = (float) Math.sqrt(1d / (scaleSlider.getSelection()));
		float scaleProp = scale / originalScale;
		
		bbox = new Rectangle(0, 0, (int) (scale * (bounding.width + 2f * BORDER)), (int) (scale * (bounding.height + 2f * BORDER)));
		map.setSize(bbox.width, bbox.height);
		scrolledContent.setSize(bbox.width, bbox.height);
		
		Point scrollTo = new Point((int) (originalScroll.x * scaleProp) - mapSize.x / 2, (int) (originalScroll.y * scaleProp) - mapSize.y / 2);
			
		// This prevents the scrolls redrawing during the setOrigin operation. By redrawing later, using scrolls.redraw(),
		// at the same time as map.redraw(), this reduces the visible left to right flicker
		scrolls.setRedraw(false);
		scrolls.setOrigin(scrollTo);
		scrolls.setRedraw(true);
		
		gcTransform = new Transform(display);
		gcTransform.scale(scale, scale);
		gcTransform.translate(0, bounding.height + BORDER);
		gcTransform.multiply(new Transform(display, 1, 0, 0, -1, 0, 0)); // Reflect about x-axis
		gcTransform.translate(BORDER - bounding.x, bounding.height - bounding.y);

		changePlan();
		map.redraw();
	}
	
	private void changePlan() {
		if (plan != null) plan.dispose();
		plan = new Image(display, bbox.width, bbox.height);
		GC gc = new GC(plan);
		gc.setTransform(gcTransform);
		drawBuilding(gc, floorIndexSelected);
	}
	
	public void setBuildingPlan(BuildingPlanI b) {
		scale = (float) Math.sqrt(1d / DEFAULT_SCALE);
		building = b;
		scaleSlider.setSelection((MIN_SCALE + MAX_SCALE) / 2);
		scale = (float) Math.sqrt(1d / DEFAULT_SCALE);
		scrolls.setOrigin(0, 0);
		
		if (building == null) return;
		
		Map<Integer, String> floorNames = building.getFloorNames();
		int floors = building.getFloors();
		String[] fns = new String[floors];
		floorNums = floorNames.keySet().toArray(new Integer[floors]);
		floorSelector.setItems(floorNames.values().toArray(fns));
		floorSelector.select(floorIndexSelected = 0);
		floorSelector.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				floorIndexSelected = floorNums[floorSelector.getSelectionIndex()];
				changePlan();
				map.redraw();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}			
		});
		
		scaleSlider.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (building != null) changeScale(building.getBoundingBox());
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		changeScale(building.getBoundingBox());
	}
	
	public void setAgents(List<AgentInterface> agents) {
		this.agents = (agents == null) ? new ArrayList<AgentInterface>() : agents;
	}
	
	public void updateMap() {
		map.redraw();
		map.update();
	}
	
	@Override
	public void clear() {
		setBuildingPlan(null);
		floorSelector.setItems(new String[0]);
		setAgents(null);
		
		if (plan != null) plan.dispose();
		plan = new Image(display, 1,1);
		GC plangc = new GC(plan);
		plangc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		plangc.fillRectangle(0, 0, 1, 1);
		
		updateMap();
	}
}
