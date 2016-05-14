package edu.uci.isr.archstudio4.comp.archipelago.statecharts.logics.mapping;

import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.mapping.MapXadlDirectionalSplineLogic;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class MapXadlTransitionLogic
	extends MapXadlDirectionalSplineLogic{

	public MapXadlTransitionLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		super(xarch, rootObjRef, relativePath, "fromState", "toState", tptl, parentThing, kind);
	}
}
