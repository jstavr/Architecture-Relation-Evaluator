package edu.uci.isr.archstudio4.comp.archipelago.generic.logics.mapping;

import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.SplineAssembly;
import edu.uci.isr.bna4.constants.ArrowheadShape;
import edu.uci.isr.bna4.facets.IHasEndpoints;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public abstract class MapXadlDirectionalSplineLogic
	extends MapXadlSplineLogic{

	public MapXadlDirectionalSplineLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, String fromLinkName, String toLinkName, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		super(xarch, rootObjRef, relativePath, tptl, parentThing, kind);
		automapXLinkToStuckPoint(fromLinkName, "glass", IHasEndpoints.ENDPOINT_1_PROPERTY_NAME, true);
		automapXLinkToStuckPoint(toLinkName, "glass", IHasEndpoints.ENDPOINT_2_PROPERTY_NAME, true);
	}

	@Override
	protected SplineAssembly addAssembly(IBNAModel model, ObjRef objRef, ObjRef[] relativeAncestorRefs){

		SplineAssembly assembly = super.addAssembly(model, objRef, relativeAncestorRefs);
		assembly.getEndpoint2ArrowheadThing().setArrowheadShape(ArrowheadShape.WEDGE);

		return assembly;
	}
}
