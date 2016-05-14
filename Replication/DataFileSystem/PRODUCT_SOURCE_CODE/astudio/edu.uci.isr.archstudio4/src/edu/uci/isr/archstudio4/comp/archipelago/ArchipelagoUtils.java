package edu.uci.isr.archstudio4.comp.archipelago;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import edu.uci.isr.bna4.BNAComposite;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.DefaultCoordinateMapper;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.IThingLogicManager;
import edu.uci.isr.bna4.ZoomUtils;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.assemblies.EndpointAssembly;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.bna4.assemblies.SplineAssembly;
import edu.uci.isr.bna4.assemblies.SplineAssembly;
import edu.uci.isr.bna4.constants.GridDisplayType;
import edu.uci.isr.bna4.facets.IHasAssemblyData;
import edu.uci.isr.bna4.facets.IHasBoundingBox;
import edu.uci.isr.bna4.facets.IHasWorld;
import edu.uci.isr.bna4.logics.tracking.TypedThingTrackingLogic;
import edu.uci.isr.bna4.things.borders.PulsingBorderThing;
import edu.uci.isr.bna4.things.labels.UserNotificationThing;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.bna4.things.utility.NoThing;
import edu.uci.isr.widgets.swt.constants.HorizontalAlignment;
import edu.uci.isr.widgets.swt.constants.VerticalAlignment;
import edu.uci.isr.xarchflat.XArchFlatEvent;

public class ArchipelagoUtils{

	public static Object[] combine(Object[] a1, Object[] a2){
		if(a1 == null){
			a1 = new Object[0];
		}
		if(a2 == null){
			a2 = new Object[0];
		}
		Object[] t = new Object[a1.length + a2.length];
		System.arraycopy(a1, 0, t, 0, a1.length);
		System.arraycopy(a2, 0, t, a1.length, a2.length);
		return t;
	}

	public static final String XARCH_ID_PROPERTY_NAME = "xArchID";

	public static IThing findThing(IBNAModel m, String xArchID){
		IThing[] allThings = m.getAllThings();
		for(IThing t: allThings){
			if(!(t instanceof EnvironmentPropertiesThing)){
				String id = t.getProperty(XARCH_ID_PROPERTY_NAME);
				if(id != null && id.equals(xArchID)){
					return t;
				}
			}
		}
		return null;
	}

	public static IThing[] findAllThings(IBNAModel m, String xArchID){
		List<IThing> results = new ArrayList<IThing>();
		IThing[] allThings = m.getAllThings();
		for(IThing t: allThings){
			String id = t.getProperty(XARCH_ID_PROPERTY_NAME);
			if(id != null && id.equals(xArchID)){
				results.add(t);
			}
		}
		return results.toArray(new IThing[results.size()]);
	}

	public static final String BNA_BOTTOM_ROOT_THING_ID = "$BOTTOM";
	public static final String BNA_NORMAL_ROOT_THING_ID = "$NORMAL";
	public static final String BNA_TOP_ROOT_THING_ID = "$TOP";

	protected static void initBNAModel(IBNAModel m){
		if(m.getThing(BNA_BOTTOM_ROOT_THING_ID) == null){
			IThing bottomRootThing = new NoThing(BNA_BOTTOM_ROOT_THING_ID);
			m.addThing(bottomRootThing);
		}

		if(m.getThing(BNA_NORMAL_ROOT_THING_ID) == null){
			IThing normalRootThing = new NoThing(BNA_NORMAL_ROOT_THING_ID);
			m.addThing(normalRootThing);
		}

		if(m.getThing(BNA_TOP_ROOT_THING_ID) == null){
			IThing topRootThing = new NoThing(BNA_TOP_ROOT_THING_ID);
			m.addThing(topRootThing);
		}
	}

	protected static IThing getRootThing(IBNAModel m, String id){
		IThing rootThing = m.getThing(id);
		if(rootThing == null){
			initBNAModel(m);
			return m.getThing(id);
		}
		return rootThing;
	}

	public static IThing getNormalRootThing(IBNAModel m){
		return getRootThing(m, BNA_NORMAL_ROOT_THING_ID);
	}

	public static IThing getBottomRootThing(IBNAModel m){
		return getRootThing(m, BNA_BOTTOM_ROOT_THING_ID);
	}

	public static IThing getTopRootThing(IBNAModel m){
		return getRootThing(m, BNA_TOP_ROOT_THING_ID);
	}

	public static void setNewThingSpot(IBNAModel m, int worldX, int worldY){
		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(m);
		ept.setProperty("#newThingSpot", new Point(worldX, worldY));
	}

	public static Point findOpenSpotForNewThing(IBNAModel m){
		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(m);
		Point p = ept.getProperty("#newThingSpot");
		if(p == null){
			ept.setProperty("#newThingSpot", p = new Point(DefaultCoordinateMapper.DEFAULT_WORLD_WIDTH / 2, DefaultCoordinateMapper.DEFAULT_WORLD_HEIGHT / 2));
		}
		return new Point(p.x += 10, p.y += 10);
	}

	public static Rectangle findOpenSpotForNewThing(IBNAModel model, int width, int height){
		Point p = findOpenSpotForNewThing(model);
		return new Rectangle(p.x, p.y, width, height);
	}

	public static final String EDITOR_PANE_PROPERTY_BNA_COMPOSITE = "bnaComposite";

	public static void setBNAComposite(IArchipelagoEditorPane editorPane, BNAComposite bnaComposite){
		editorPane.setProperty(EDITOR_PANE_PROPERTY_BNA_COMPOSITE, bnaComposite);
	}

	public static BNAComposite getBNAComposite(IArchipelagoEditorPane editorPane){
		return (BNAComposite)editorPane.getProperty(EDITOR_PANE_PROPERTY_BNA_COMPOSITE);
	}

	public static void sendEventToInnerViews(XArchFlatEvent evt, IBNAWorld world, TypedThingTrackingLogic tttl){
		//Ship the event to subviews
		IHasWorld[] worldThings = tttl.getThings(IHasWorld.class);
		if(worldThings != null){
			for(IHasWorld vt: worldThings){
				IBNAWorld innerWorld = vt.getWorld();
				if(innerWorld != null){
					IThingLogicManager innerThingLogicManager = innerWorld.getThingLogicManager();
					if(innerThingLogicManager != null){
						for(IXArchEventHandlerLogic logic: innerThingLogicManager.getThingLogics(IXArchEventHandlerLogic.class)){
							logic.handleXArchFlatEvent(evt, innerWorld);
						}
					}
				}
			}
		}
	}

	public static void showUserNotification(IBNAModel m, String text, int worldX, int worldY){
		UserNotificationThing unt = new UserNotificationThing();
		unt.setText(text);
		unt.setAnchorPoint(new Point(worldX, worldY));
		unt.setVerticalAlignment(VerticalAlignment.MIDDLE);
		unt.setHorizontalAlignment(HorizontalAlignment.CENTER);
		unt.setColor(new RGB(0, 0, 0));
		unt.setSecondaryColor(new RGB(192, 192, 192));
		m.addThing(unt);
	}

	public static void beginTreeCellEditing(TreeViewer viewer, Object allowEditing){
		viewer.setData("allowCellEditing", allowEditing);
		//System.err.println("allowing cell editing on: " + allowEditing);
		viewer.editElement(allowEditing, 0);
		viewer.setData("allowCellEditing", null);
	}

	public static String getClassName(Class c){
		if(c.isArray()){
			Class cc = c.getComponentType();
			return getClassName(cc) + "[]";
		}
		return c.getName();
	}

	public static void applyGridPreferences(ArchipelagoServices AS, IBNAModel bnaModel){
		int gridSpacing = AS.prefs.getInt(ArchipelagoConstants.PREF_GRID_SPACING);
		BNAUtils.setGridSpacing(bnaModel, gridSpacing);

		String gridDisplayTypeString = AS.prefs.getString(ArchipelagoConstants.PREF_GRID_DISPLAY_TYPE);
		if(gridDisplayTypeString == null || gridDisplayTypeString.length() == 0){
			gridDisplayTypeString = GridDisplayType.NONE.name();
		}
		try{
			GridDisplayType gdt = GridDisplayType.valueOf(gridDisplayTypeString);
			if(gdt != null){
				BNAUtils.setGridDisplayType(bnaModel, gdt);
			}
		}
		catch(Exception e){
			BNAUtils.setGridDisplayType(bnaModel, GridDisplayType.NONE);
		}
	}

	public static void pulseNotify(final IBNAModel m, final IThing t){
		if(t != null && t instanceof IHasBoundingBox){
			final Rectangle bb = ((IHasBoundingBox)t).getBoundingBox();
			if(bb != null){
				Thread pulserThread = new Thread(){

					@Override
					public void run(){
						PulsingBorderThing pbt = new PulsingBorderThing();
						pbt.setBoundingBox(bb);
						m.addThing(pbt);
						try{
							Thread.sleep(6000);
						}
						catch(InterruptedException ie){
						}
						m.removeThing(pbt);
					}
				};
				pulserThread.start();
			}
		}
	}

	public static IThing getGlassThing(IBNAModel m, IThing rootThing){
		if(rootThing != null && rootThing instanceof IHasAssemblyData){
			IHasAssemblyData assemblyRootThing = (IHasAssemblyData)rootThing;
			IAssembly a = assemblyRootThing.getAssembly();
			if(a instanceof BoxAssembly){
				return ((BoxAssembly)a).getBoxGlassThing();
			}
			else if(a instanceof SplineAssembly){
				((SplineAssembly)a).getSplineGlassThing();
			}
			else if(a instanceof SplineAssembly){
				((SplineAssembly)a).getSplineGlassThing();
			}
			else if(a instanceof EndpointAssembly){
				((EndpointAssembly)a).getEndpointGlassThing();
			}
		}
		return null;
	}

	public static IStructuredSelection addToSelection(ISelection os, Object[] thingsToAdd){
		Set<Object> newSelections = new HashSet<Object>();
		if(os != null){
			if(os instanceof IStructuredSelection){
				for(Iterator it = ((IStructuredSelection)os).iterator(); it.hasNext();){
					newSelections.add(it.next());
				}
			}
		}
		for(Object o: thingsToAdd){
			newSelections.add(o);
		}
		return new StructuredSelection(newSelections.toArray());
	}

	public static void addZoomWidget(final BNAComposite bnaComposite, final IBNAView bnaView){
		Listener l = new Listener(){

			public void handleEvent(Event e){
				bnaComposite.forceFocus();
			}
		};

		bnaComposite.addListener(SWT.Activate, l);
		bnaComposite.addListener(SWT.MouseDown, l);

		final Control zoomWidget = ZoomUtils.createZoomWidget(bnaComposite, bnaComposite, bnaView.getCoordinateMapper());
		zoomWidget.setSize(zoomWidget.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		zoomWidget.setLocation(bnaComposite.getClientArea().x + bnaComposite.getClientArea().width - zoomWidget.getSize().x - 1, 1);

		bnaComposite.addControlListener(new ControlListener(){

			public void controlMoved(ControlEvent e){
			}

			public void controlResized(ControlEvent e){
				zoomWidget.setLocation(bnaComposite.getClientArea().x + bnaComposite.getClientArea().width - zoomWidget.getSize().x - 1, 1);
			}
		});

		/*
		 * final IToolBarManager tb =
		 * AS.editor.getActionBars().getToolBarManager(); final
		 * ControlContribution cc = new ControlContribution("ZOOM"){ protected
		 * Control createControl(Composite parent){ final Control zoomWidget =
		 * ZoomUtils.createZoomWidget(parent, bnaComposite,
		 * bnaView.getCoordinateMapper()); return zoomWidget; } }; tb.add(cc);
		 * tb.markDirty(); tb.update(true); bnaComposite.addDisposeListener(new
		 * DisposeListener(){ public void widgetDisposed(DisposeEvent e){
		 * tb.remove(cc); tb.update(true); } });
		 */
	}
}
