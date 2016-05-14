package edu.uci.isr.archstudio4.comp.archipelago.types;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.graphlayout.GraphLayout;
import edu.uci.isr.archstudio4.graphlayout.GraphLayoutException;
import edu.uci.isr.archstudio4.graphlayout.GraphLayoutParameters;
import edu.uci.isr.archstudio4.graphlayout.gui.GraphLayoutDialog;
import edu.uci.isr.archstudio4.util.ArchstudioResources;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.assemblies.EndpointAssembly;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.bna4.assemblies.SplineAssembly;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.bna4.things.glass.EndpointGlassThing;
import edu.uci.isr.bna4.things.glass.SplineGlassThing;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.widgets.swt.BSpline;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xadlutils.XadlUtils.LinkInfo;
import edu.uci.isr.xarchflat.ObjRef;

public class StructureGraphLayoutLogic extends AbstractThingLogic implements IBNAMenuListener {

	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	public StructureGraphLayoutLogic(ArchipelagoServices services, ObjRef xArchRef) {
		this.AS = services;
		this.xArchRef = xArchRef;
	}

	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY) {
		if (t != null) {
			return;
		}

		final IBNAView fview = view;
		final int fworldX = worldX;
		final int fworldY = worldY;

		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(view.getWorld().getBNAModel());
		String structureXArchID = ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		if (structureXArchID != null) {
			final ObjRef structureRef = AS.xarch.getByID(xArchRef, structureXArchID);
			if (structureRef != null && AS.xarch.isInstanceOf(structureRef, "types#ArchStructure")) {
				IAction layoutAction = new Action("Automatic Layout...") {

					@Override
					public void run() {
						doLayout(fview, structureRef, fworldX, fworldY);
					}

					@Override
					public ImageDescriptor getImageDescriptor() {
						return AS.resources.getImageDescriptor(ArchstudioResources.ICON_STRUCTURE);
					}
				};
				m.add(layoutAction);
				m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		}
	}

	protected void doLayoutInJob(final IBNAView view, final ObjRef structureRef, final int worldX, final int worldY, final String engineID,
	        final GraphLayoutParameters glp) {
		Job job = new Job("Laying Out") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				doLayout(view, structureRef, worldX, worldY, engineID, glp);
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.setPriority(Job.SHORT);
		job.schedule();
		/*
		 * try{ job.join(); } catch(InterruptedException ie){}
		 */
	}

	protected void doLayout(IBNAView view, ObjRef structureRef, int worldX, int worldY) {
		GraphLayoutDialog gld = new GraphLayoutDialog(BNAUtils.getParentComposite(view).getShell());
		GraphLayoutParameters glp = gld.open(AS.graphLayout);
		if (glp == null) {
			return;
		}
		String engineID = (String) glp.getProperty("engineID");
		if (engineID == null) {
			return;
		}
		doLayoutInJob(view, structureRef, worldX, worldY, engineID, glp);
		BNAUtils.getParentComposite(view).forceFocus();
	}

	protected void doLayout(final IBNAView view, ObjRef structureRef, int worldX, int worldY, String engineID, GraphLayoutParameters glp) {
		IBNAModel model = view.getWorld().getBNAModel();
		model.beginBulkChange();
		try {
			GraphLayout gl = AS.graphLayout.layoutGraph(engineID, structureRef, glp);
			applyGraphLayout(view, structureRef, gl, glp, worldX, worldY);
		}
		catch (final GraphLayoutException gle) {
			SWTWidgetUtils.async(BNAUtils.getParentComposite(view), new Runnable() {

				public void run() {
					MessageDialog.openError(BNAUtils.getParentComposite(view).getShell(), "Error", gle.getMessage());
				}
			});
		}
		finally {
			model.endBulkChange();
		}
	}

	protected void applyGraphLayout(IBNAView view, ObjRef structureRef, GraphLayout gl, GraphLayoutParameters glp, int worldX, int worldY) {
		for (int i = 0; i < gl.getNumNodes(); i++) {
			GraphLayout.Node node = gl.getNodeAt(i);
			String nodeID = node.getNodeId();
			Rectangle nodeBounds = node.getBounds();
			nodeBounds.x += worldX;
			nodeBounds.y += worldY;

			IThing nodeRootThing = ArchipelagoUtils.findThing(view.getWorld().getBNAModel(), nodeID);
			IAssembly assembly = AssemblyUtils.getAssemblyWithRoot(nodeRootThing);
			if (assembly instanceof BoxAssembly) {
				BoxGlassThing bgt = ((BoxAssembly) assembly).getBoxGlassThing();
				if (bgt != null) {
					bgt.setBoundingBox(nodeBounds);
				}
			}
		}
		if (!getDontMoveInterfaces(glp)) {
			repositionInterfaces(view, structureRef, gl, glp, worldX, worldY);
		}
		if (!getDontRouteLinks(glp)) {
			routeLinks(view, structureRef, gl, glp, worldX, worldY);
		}
		else {
			unrouteLinks(view, structureRef, gl, glp, worldX, worldY);
		}
	}

	protected void repositionInterfaces(IBNAView view, ObjRef structureRef, GraphLayout gl, GraphLayoutParameters glp, int worldX, int worldY) {
		Map<String, List<Point>> interfaceIDtoPointListMap = new HashMap<String, List<Point>>();

		for (int i = 0; i < gl.getNumEdges(); i++) {
			GraphLayout.Edge edge = gl.getEdgeAt(i);
			String linkXArchID = edge.getEdgeId();

			ObjRef linkRef = AS.xarch.getByID(xArchRef, linkXArchID);
			if (linkRef != null) {
				LinkInfo linkInfo = XadlUtils.getLinkInfo(AS.xarch, linkRef, true);
				ObjRef interface1Ref = linkInfo.getPoint1Target();
				if (interface1Ref != null && AS.xarch.isInstanceOf(interface1Ref, "types#Interface")) {
					String interface1ID = XadlUtils.getID(AS.xarch, interface1Ref);
					if (interface1ID != null) {
						if (edge.getNumPoints() > 0) {
							Point interface1Point = edge.getPointAt(0);
							List<Point> pointList = interfaceIDtoPointListMap.get(interface1ID);
							if (pointList == null) {
								pointList = new ArrayList<Point>();
								interfaceIDtoPointListMap.put(interface1ID, pointList);
							}
							pointList.add(interface1Point);
						}
					}
				}

				ObjRef interface2Ref = linkInfo.getPoint2Target();
				if (interface2Ref != null && AS.xarch.isInstanceOf(interface2Ref, "types#Interface")) {
					String interface2ID = XadlUtils.getID(AS.xarch, interface2Ref);
					if (interface2ID != null) {
						if (edge.getNumPoints() > 0) {
							Point interface2Point = edge.getPointAt(edge.getNumPoints() - 1);
							List<Point> pointList = interfaceIDtoPointListMap.get(interface2ID);
							if (pointList == null) {
								pointList = new ArrayList<Point>();
								interfaceIDtoPointListMap.put(interface2ID, pointList);
							}
							pointList.add(interface2Point);
						}
					}
				}
			}
		}

		for (String interfaceXArchID : interfaceIDtoPointListMap.keySet()) {
			IThing nodeRootThing = ArchipelagoUtils.findThing(view.getWorld().getBNAModel(), interfaceXArchID);
			IAssembly assembly = AssemblyUtils.getAssemblyWithRoot(nodeRootThing);
			if (assembly instanceof EndpointAssembly) {
				EndpointGlassThing egt = ((EndpointAssembly) assembly).getEndpointGlassThing();
				if (egt != null) {
					List<Point> pointList = interfaceIDtoPointListMap.get(interfaceXArchID);
					if (pointList.size() > 0) {
						//Average the points
						Point p = new Point(0, 0);
						for (Point p2 : pointList) {
							p.x += p2.x;
							p.y += p2.y;
						}
						p.x /= pointList.size();
						p.y /= pointList.size();

						p.x += worldX;
						p.y += worldY;

						egt.setAnchorPoint(p);
					}
				}
			}
		}
	}

	protected void routeLinks(IBNAView view, ObjRef structureRef, GraphLayout gl, GraphLayoutParameters glp, int worldX, int worldY) {
		for (int i = 0; i < gl.getNumEdges(); i++) {
			GraphLayout.Edge edge = gl.getEdgeAt(i);
			String edgeID = edge.getEdgeId();

			Point[] controlPoints = null;
			if (edge.getNumPoints() > 3) {
				controlPoints = new Point[edge.getNumPoints() - 2];
				for (int j = 1; j < edge.getNumPoints() - 1; j++) {
					controlPoints[j - 1] = edge.getPointAt(j);
					controlPoints[j - 1].x += worldX;
					controlPoints[j - 1].y += worldY;
				}
			}

			//The points returned by dot are actually B-spline control points;
			//we can convert these into rectilinear waypoints to simulate a curve.
			Point[] midpoints = BNAUtils.toPoints(BSpline.bspline(BNAUtils.toSwtPoints(controlPoints), 2));
			midpoints = optimizePoints(midpoints);

			IThing linkRootThing = ArchipelagoUtils.findThing(view.getWorld().getBNAModel(), edgeID);
			IAssembly assembly = AssemblyUtils.getAssemblyWithRoot(linkRootThing);
			if (assembly instanceof SplineAssembly) {
				SplineGlassThing sgt = ((SplineAssembly) assembly).getSplineGlassThing();
				if (sgt != null) {
					sgt.setMidpoints(midpoints);
				}
			}
		}
	}

	protected static Point[] optimizePoints(Point[] points) {
		List<Point> optimizedPoints = new ArrayList<Point>();
		Point lastPoint = null;

		for (Point element : points) {
			if (lastPoint == null) {
				optimizedPoints.add(element);
				lastPoint = element;
			}
			else {
				double dist = Point2D.distance(lastPoint.x, lastPoint.y, element.x, element.y);
				if (dist < 12.5) {
					continue;
				}
				optimizedPoints.add(element);
				lastPoint = element;
			}
		}
		return optimizedPoints.toArray(new Point[optimizedPoints.size()]);
	}

	protected void unrouteLinks(IBNAView view, ObjRef structureRef, GraphLayout gl, GraphLayoutParameters glp, int worldX, int worldY) {
		for (int i = 0; i < gl.getNumEdges(); i++) {
			GraphLayout.Edge edge = gl.getEdgeAt(i);
			String edgeID = edge.getEdgeId();

			IThing linkRootThing = ArchipelagoUtils.findThing(view.getWorld().getBNAModel(), edgeID);
			IAssembly assembly = AssemblyUtils.getAssemblyWithRoot(linkRootThing);
			if (assembly instanceof SplineAssembly) {
				SplineGlassThing sgt = ((SplineAssembly) assembly).getSplineGlassThing();
				if (sgt != null) {
					sgt.setMidpoints(null);
				}
			}
		}
	}

	protected static boolean getDontMoveInterfaces(GraphLayoutParameters glp) {
		Object o = glp.getProperty("dontMoveInterfaces");
		if (o == null) {
			return false;
		}
		if (!(o instanceof Boolean)) {
			return false;
		}
		return ((Boolean) o).booleanValue();
	}

	protected static boolean getDontRouteLinks(GraphLayoutParameters glp) {
		Object o = glp.getProperty("dontRouteLinks");
		if (o == null) {
			return false;
		}
		if (!(o instanceof Boolean)) {
			return false;
		}
		return ((Boolean) o).booleanValue();
	}
}
