package edu.uci.isr.archstudio4.comp.xarchcs.logics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.ChangeStatus;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTListener;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetListener;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent.ChangeSetEventType;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAModelEvent;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAModelListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.PathDataConstructor;
import edu.uci.isr.bna4.PathDataUtils;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.assemblies.EndpointAssembly;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.bna4.assemblies.MappingAssembly;
import edu.uci.isr.bna4.assemblies.PathAssembly;
import edu.uci.isr.bna4.assemblies.SplineAssembly;
import edu.uci.isr.bna4.facets.IHasAnchorPoint;
import edu.uci.isr.bna4.facets.IHasAssemblyData;
import edu.uci.isr.bna4.facets.IHasBoundingBox;
import edu.uci.isr.bna4.facets.IHasEndpoints;
import edu.uci.isr.bna4.facets.IHasInternalWorldEndpoint;
import edu.uci.isr.bna4.facets.IHasLineStyle;
import edu.uci.isr.bna4.facets.IHasMidpoints;
import edu.uci.isr.bna4.facets.IHasSelected;
import edu.uci.isr.bna4.facets.IHasText;
import edu.uci.isr.bna4.logics.coordinating.MirrorBoundingBoxLogic;
import edu.uci.isr.bna4.logics.coordinating.MirrorValueLogic;
import edu.uci.isr.bna4.logics.coordinating.MoveWithLogic;
import edu.uci.isr.bna4.logics.tracking.AssemblyTrackingLogic;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyPrefixTrackingLogic;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.bna4.things.borders.BoxBorderThing;
import edu.uci.isr.sysutils.DelayedExecuteOnceThread;
import edu.uci.isr.xarchflat.ObjRef;

public class AnnotateExplicitChangeLogic extends AbstractThingLogic implements ExplicitADTListener, XArchChangeSetListener, IBNAModelListener {

	public static final String XARCH_ID_PROPERTY_NAME = "xArchID"; // TODO: use official source

	final ThingPropertyTrackingLogic tptl;

	final ThingPropertyPrefixTrackingLogic tpptl;

	final AssemblyTrackingLogic atl;

	XArchChangeSetInterface xarch;

	IExplicitADT explicit;

	ObjRef xArchRef;

	final Object updateLock = new Object();

	final DelayedExecuteOnceThread delayedThread;

	boolean inExplicitMode = false;

	public AnnotateExplicitChangeLogic(ThingPropertyTrackingLogic tptl, ThingPropertyPrefixTrackingLogic tpptl, AssemblyTrackingLogic atl,
	        XArchChangeSetInterface xarch, IExplicitADT explicit, ObjRef xArchRef) {
		this.tptl = tptl;
		this.tpptl = tpptl;
		this.atl = atl;
		this.xarch = xarch;
		this.explicit = explicit;
		this.xArchRef = xArchRef;
		this.delayedThread = new DelayedExecuteOnceThread(250, new Runnable() {
			public void run() {
				AnnotateExplicitChangeLogic.this.updateAnnotations();
			}
		}, true, true);
	}

	boolean updateAll = false;
	Set<ObjRef> objRefsToUpdate = new HashSet<ObjRef>();
	Set<IAssembly> assembliesToUpdate = new HashSet<IAssembly>();

	private static <T> boolean containsAny(Set<T> set, T[] values) {
		for (T value : values) {
			if (set.contains(value))
				return true;
		}
		return false;
	}

	private void updateAnnotations() {
		final boolean doUpdateAll;
		final Set<ObjRef> doObjRefsToUpdate;
		final Set<IAssembly> doAssembliesToUpdate;

		synchronized (updateLock) {
			assembliesToUpdate.remove(null);
			
			doUpdateAll = updateAll;
			doObjRefsToUpdate = new HashSet<ObjRef>(objRefsToUpdate);
			doAssembliesToUpdate = new HashSet<IAssembly>(assembliesToUpdate);

			updateAll = false;
			objRefsToUpdate.clear();
			assembliesToUpdate.clear();
		}

		final IBNAModel model = getBNAModel();
		if (model != null) {
			model.beginBulkChange();
			try {
				final boolean doObjRefsToUpdateIsEmpty = doObjRefsToUpdate.isEmpty();
				if (doUpdateAll || !doObjRefsToUpdateIsEmpty) {
					doAssembliesToUpdate.addAll(Arrays.asList(atl.getAllAssemblies()));
				}
				if (doAssembliesToUpdate.size() > 0) {
					ObjRef[] explicitChangeSetRefs = explicit.getExplicitChangeSetRefs(xArchRef);
					inExplicitMode = explicitChangeSetRefs.length > 0;
					for (IAssembly a : doAssembliesToUpdate) {
						String id = a.getRootThing().getProperty(XARCH_ID_PROPERTY_NAME);
						if (id != null) {
							ObjRef objRef = xarch.getByID(xArchRef, id);
							if (objRef != null
							        && (doObjRefsToUpdateIsEmpty || doObjRefsToUpdate.contains(objRef) || containsAny(doObjRefsToUpdate, xarch
							                .getAllAncestors(objRef)))) {
								update(a, xarch.getChangeStatus(objRef, explicitChangeSetRefs));
							}
						}
					}
				}
			}
			finally {
				model.endBulkChange();
			}
		}
	}

	public void handleXArchChangeSetEvent(XArchChangeSetEvent evt) {
		if (evt.getEventType() == ChangeSetEventType.UPDATED_ENABLED) {
			// TODO: enable the logic only when change sets are used
			synchronized (updateLock) {
				updateAll = true;
			}
			delayedThread.execute();
		}
	}

	public void handleExplicitEvent(ExplicitADTEvent evt) {
		switch (evt.getEventType()) {
		case UPDATED_EXPLICIT_CHANGE_SETS: {
			synchronized (updateLock) {
				updateAll = true;
			}
			delayedThread.execute();
			break;
		}
		case UPDATED_EXPLICIT_OBJREF: {
			synchronized (updateLock) {
				objRefsToUpdate.add(evt.getObjRef());
			}
			delayedThread.execute();
			break;
		}
		}
	}

	@Override
	public void init() {
		super.init();
		synchronized (updateLock) {
			updateAll = true;
		}
		delayedThread.execute();
	}

	public void bnaModelChanged(BNAModelEvent evt) {
		switch (evt.getEventType()) {
		case THING_ADDED: {
			IThing t = evt.getTargetThing();
			synchronized (updateLock) {
				assembliesToUpdate.add(AssemblyUtils.getAssemblyWithRoot(t));
				assembliesToUpdate.add(AssemblyUtils.getAssemblyWithPart(t));
			}
			delayedThread.execute();
			break;
		}
		case THING_CHANGED: {
			IThing t = evt.getTargetThing();
			Object propertyName = evt.getThingEvent().getPropertyName();
			if (XARCH_ID_PROPERTY_NAME.equals(propertyName) || IHasAssemblyData.ASSEMBLY_PROPERTY_NAME.equals(propertyName)) {
				synchronized (updateLock) {
					assembliesToUpdate.add(AssemblyUtils.getAssemblyWithRoot(t));
					assembliesToUpdate.add(AssemblyUtils.getAssemblyWithPart(t));
				}
				delayedThread.execute();
			}
			break;
		}
		}
	}

	void update(IAssembly targetAssembly, ChangeStatus changeStatus) {
		IBNAModel model = getBNAModel();

		if (targetAssembly instanceof BoxAssembly) {
			updateBoundingBoxAnnotation(model, targetAssembly, changeStatus, ((BoxAssembly) targetAssembly).getBoxBorderThing(), ((BoxAssembly) targetAssembly)
			        .getBoxGlassThing(), 25);
		}
		else if (targetAssembly instanceof EndpointAssembly) {
			updateBoundingBoxAnnotation(model, targetAssembly, changeStatus, ((EndpointAssembly) targetAssembly).getDirectionalLabelThing(),
			        ((EndpointAssembly) targetAssembly).getEndpointGlassThing(), 15);
		}
		else if (targetAssembly instanceof SplineAssembly) {
			updateSplineAnnotation(model, targetAssembly, changeStatus, ((SplineAssembly) targetAssembly).getSplineThing(), ((SplineAssembly) targetAssembly)
			        .getSplineGlassThing(), 5);
		}
		else if (targetAssembly instanceof MappingAssembly) {
			updateMappingAnnotation(model, targetAssembly, changeStatus, ((MappingAssembly) targetAssembly).getMappingThing(),
			        ((MappingAssembly) targetAssembly).getMappingGlassThing(), 5);
		}
		else if (targetAssembly.getPart("glass") instanceof IHasBoundingBox) {
			updateBoundingBoxAnnotation(model, targetAssembly, changeStatus, targetAssembly.getPart("glass"), targetAssembly.getPart("glass"), 25);
		}
		else {
			System.err.println("Cannot annotate: " + targetAssembly);
		}
	}

	void updateBoundingBoxAnnotation(IBNAModel model, IAssembly targetAssembly, ChangeStatus changeStatus, IThing parentThing, IThing glassThing, int size) {
		IAssembly changeAnnotation = AssemblyUtils.getAssemblyWithRoot(model.getChildThings(parentThing), ChangeStatus.class);
		ChangeStatus changeAnnotationStatus = changeAnnotation == null ? null : (ChangeStatus) changeAnnotation.getKind();

		switch (changeStatus) {
		case ADDED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, true, false);
			break;
		case REMOVED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, false, false);
			break;
		case MODIFIED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, true, false);
			break;
		case UNMODIFIED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, true, false);
			break;
		case DETACHED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), false, false, false);
			break;
		case UNADDED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, false, true);
			break;
		default:
			throw new IllegalArgumentException();
		}

		if (!changeStatus.equals(changeAnnotationStatus) || !inExplicitMode) {
			if (changeAnnotation != null) {
				changeAnnotation.remove(true);
				changeAnnotation = null;
			}

			PathDataConstructor pdc = null;
			RGB color = null;

			switch (changeStatus) {
			case ADDED:
				pdc = PathDataUtils.createUnitPlus(0.35f);
				pdc.translate(-0.5f, -0.5f);
				pdc.scale(size, size);
				color = new RGB(0, 255, 255);
				break;
			case REMOVED:
				pdc = PathDataUtils.createUnitX(0.3f, 0.35f);
				pdc.translate(-0.5f, -0.5f);
				pdc.scale(size, size);
				color = new RGB(255, 0, 0);
				break;
			case MODIFIED:
				pdc = new PathDataConstructor();
				pdc.moveTo(0.5f, 0);
				pdc.lineTo(1, 1);
				pdc.lineTo(0, 1);
				pdc.close();
				pdc.translate(-0.5f, -0.5f);
				pdc.scale(size, size);
				color = new RGB(255, 0, 255);
				break;
			case UNMODIFIED:
				return;
			case DETACHED:
				return;
			case UNADDED:
				return;
			}

			if (pdc != null) {
				changeAnnotation = new PathAssembly(model, parentThing, changeStatus);
				((PathAssembly) changeAnnotation).getPathGlassThing().setPathData(pdc.getPathData());
				if (color != null) {
					((PathAssembly) changeAnnotation).getPathThing().setColor(color);
				}

				if (glassThing instanceof IHasBoundingBox) {
					Rectangle r = ((IHasBoundingBox) glassThing).getBoundingBox();
					((PathAssembly) changeAnnotation).getPathGlassThing().setAnchorPoint(new Point(r.x, r.y));
					MoveWithLogic.moveWith(glassThing, MoveWithLogic.TRACK_BOUNDING_BOX_ONLY, ((PathAssembly) changeAnnotation).getPathGlassThing());
				}
			}
			else {
				changeAnnotation = new BoxAssembly(model, parentThing, changeStatus);
				((BoxAssembly) changeAnnotation).getBoxThing().setColor(new RGB(255, 255, 255));
				((BoxAssembly) changeAnnotation).getBoxBorderThing().setLineStyle(IHasLineStyle.LINE_STYLE_DASH);

				if (targetAssembly.getPart("label") instanceof IHasText) {
					MirrorValueLogic.mirrorValue(targetAssembly.getPart("label"), IHasText.TEXT_PROPERTY_NAME, ((BoxAssembly) changeAnnotation)
					        .getBoxedLabelThing());
				}
				if (glassThing instanceof IHasBoundingBox) {
					MirrorBoundingBoxLogic.mirrorBoundingBox((IHasBoundingBox) glassThing, new Rectangle(0, 0, 0, 0), ((BoxAssembly) changeAnnotation)
					        .getBoxGlassThing());
				}
				if (AssemblyUtils.getAssemblyWithPart(glassThing) instanceof BoxAssembly) {
					MirrorValueLogic.mirrorValue(((BoxAssembly) AssemblyUtils.getAssemblyWithPart(glassThing)).getBoxBorderThing(),
					        BoxBorderThing.COUNT_PROPERTY_NAME, ((BoxAssembly) changeAnnotation).getBoxBorderThing());
				}
			}
		}
	}

	void updateSplineAnnotation(IBNAModel model, IAssembly targetAssembly, ChangeStatus changeStatus, IThing parentThing, IThing glassThing, int lineWidth) {
		SplineAssembly changeAnnotation = AssemblyUtils.getAssemblyWithRoot(model.getChildThings(parentThing), ChangeStatus.class);
		ChangeStatus changeAnnotationStatus = changeAnnotation == null ? ChangeStatus.UNMODIFIED : (ChangeStatus) changeAnnotation.getKind();

		switch (changeStatus) {
		case ADDED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, true, false);
			break;
		case REMOVED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, false, false);
			break;
		case MODIFIED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, true, false);
			break;
		case UNMODIFIED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, true, false);
			break;
		case DETACHED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), false, false, false);
			break;
		case UNADDED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, false, true);
			break;
		default:
			throw new IllegalArgumentException();
		}

		if (!changeStatus.equals(changeAnnotationStatus)) {
			if (changeAnnotation != null) {
				changeAnnotation.remove(true);
				changeAnnotation = null;
			}

			RGB color;
			int lineStyle;

			switch (changeStatus) {
			case ADDED:
				color = new RGB(0, 255, 255);
				lineStyle = IHasLineStyle.LINE_STYLE_SOLID;
				break;
			case REMOVED:
				color = new RGB(255, 0, 0);
				lineStyle = IHasLineStyle.LINE_STYLE_DOT;
				break;
			case MODIFIED:
				color = new RGB(255, 0, 255);
				lineStyle = IHasLineStyle.LINE_STYLE_SOLID;
				break;
			case UNMODIFIED:
				return;
			case DETACHED:
				return;
			case UNADDED:
				return;
			default:
				throw new IllegalArgumentException();
			}

			changeAnnotation = new SplineAssembly(model, parentThing, changeStatus);
			changeAnnotation.getSplineThing().setColor(color);
			changeAnnotation.getSplineThing().setLineWidth(lineWidth);
			changeAnnotation.getSplineThing().setLineStyle(lineStyle);

			if (glassThing instanceof IHasEndpoints) {
				MirrorValueLogic.mirrorValue(glassThing, IHasEndpoints.ENDPOINT_1_PROPERTY_NAME, changeAnnotation.getSplineGlassThing());
				MirrorValueLogic.mirrorValue(glassThing, IHasEndpoints.ENDPOINT_2_PROPERTY_NAME, changeAnnotation.getSplineGlassThing());
			}
			if (glassThing instanceof IHasMidpoints) {
				MirrorValueLogic.mirrorValue(glassThing, IHasMidpoints.MIDPOINTS_PROPERTY_NAME, changeAnnotation.getSplineGlassThing());
			}
		}
	}

	void updateMappingAnnotation(IBNAModel model, IAssembly targetAssembly, ChangeStatus changeStatus, IThing parentThing, IThing glassThing, int lineWidth) {
		MappingAssembly changeAnnotation = AssemblyUtils.getAssemblyWithRoot(model.getChildThings(parentThing), ChangeStatus.class);
		ChangeStatus changeAnnotationStatus = changeAnnotation == null ? ChangeStatus.UNMODIFIED : (ChangeStatus) changeAnnotation.getKind();

		switch (changeStatus) {
		case ADDED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, true, false);
			break;
		case REMOVED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, false, false);
			break;
		case MODIFIED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, true, false);
			break;
		case UNMODIFIED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, true, false);
			break;
		case DETACHED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), false, false, false);
			break;
		case UNADDED:
			updateAttributes(model, AssemblyUtils.getAssemblyWithPart(glassThing), true, false, true);
			break;
		default:
			throw new IllegalArgumentException();
		}

		if (!changeStatus.equals(changeAnnotationStatus)) {
			if (changeAnnotation != null) {
				changeAnnotation.remove(true);
				changeAnnotation = null;
			}

			RGB color;
			int lineStyle;

			switch (changeStatus) {
			case ADDED:
				color = new RGB(0, 255, 255);
				lineStyle = IHasLineStyle.LINE_STYLE_SOLID;
				break;
			case REMOVED:
				color = new RGB(255, 0, 0);
				lineStyle = IHasLineStyle.LINE_STYLE_DOT;
				break;
			case MODIFIED:
				color = new RGB(255, 0, 255);
				lineStyle = IHasLineStyle.LINE_STYLE_SOLID;
				break;
			case UNMODIFIED:
				return;
			case DETACHED:
				return;
			case UNADDED:
				return;
			default:
				throw new IllegalArgumentException();
			}

			changeAnnotation = new MappingAssembly(model, parentThing, changeStatus);
			changeAnnotation.getMappingThing().setColor(color);
			changeAnnotation.getMappingThing().setLineWidth(lineWidth);
			changeAnnotation.getMappingThing().setLineStyle(lineStyle);

			MirrorValueLogic.mirrorValue(glassThing, IHasAnchorPoint.ANCHOR_POINT_PROPERTY_NAME, changeAnnotation.getMappingGlassThing());
			MirrorValueLogic.mirrorValue(glassThing, IHasInternalWorldEndpoint.INTERNAL_ENDPOINT_PROPERTY_NAME, changeAnnotation.getMappingGlassThing());
			MirrorValueLogic.mirrorValue(glassThing, IHasInternalWorldEndpoint.INTERNAL_ENDPOINT_WORLD_THING_ID_PROPERTY_NAME, changeAnnotation
			        .getMappingGlassThing());
		}
	}

	void updateAttributes(IBNAModel model, IAssembly assembly, boolean visible, boolean editable, boolean obscure) {
		if(assembly != null){
			for (IThing t : assembly.getParts()) {
				if (t.getProperty(XARCH_ID_PROPERTY_NAME) == null) {
					updateAttributes(model, t, visible, editable, obscure);
				}
			}
		}
	}

	void updateAttributes(IBNAModel model, IThing thing, boolean visible, boolean editable, boolean obscure) {
		if (thing != null) {
			thing.setProperty(IBNAView.HIDE_THING_PROPERTY_NAME, !visible);
			thing.setProperty(IBNAView.BACKGROUND_THING_PROPERTY_NAME, !editable);
			thing.setProperty(IBNAView.OBSCURE_THING_PROPERTY_NAME, obscure);
			if (!editable) {
				if (thing instanceof IHasSelected) {
					thing.setProperty(IHasSelected.SELECTED_PROPERTY_NAME, false);
				}
			}
		}
	}
}
