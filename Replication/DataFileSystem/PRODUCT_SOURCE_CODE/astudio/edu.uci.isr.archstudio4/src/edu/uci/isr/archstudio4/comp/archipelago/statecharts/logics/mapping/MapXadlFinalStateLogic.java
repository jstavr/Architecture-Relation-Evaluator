package edu.uci.isr.archstudio4.comp.archipelago.statecharts.logics.mapping;

import org.eclipse.draw2d.geometry.Rectangle;

import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.EllipseAssembly;
import edu.uci.isr.bna4.logics.coordinating.MirrorBoundingBoxLogic;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class MapXadlFinalStateLogic
	extends MapXadlInitialStateLogic{

	public MapXadlFinalStateLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		super(xarch, rootObjRef, relativePath, tptl, parentThing, kind);
	}

	public MapXadlFinalStateLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, String reqAttrName, String reqAttrValue, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		super(xarch, rootObjRef, relativePath, reqAttrName, reqAttrValue, tptl, parentThing, kind);
	}

	@Override
	protected EllipseAssembly addAssembly(IBNAModel model, ObjRef objRef, ObjRef[] relativeAncestorRefs){

		EllipseAssembly assembly = super.addAssembly(model, objRef, relativeAncestorRefs);
		MirrorBoundingBoxLogic.mirrorBoundingBox(assembly.getEllipseGlassThing(), new Rectangle(4, 4, -8, -8), assembly.getEllipseThing());
		assembly.getEllipseBorderThing().setLineWidth(2);

		return assembly;
	}
}
