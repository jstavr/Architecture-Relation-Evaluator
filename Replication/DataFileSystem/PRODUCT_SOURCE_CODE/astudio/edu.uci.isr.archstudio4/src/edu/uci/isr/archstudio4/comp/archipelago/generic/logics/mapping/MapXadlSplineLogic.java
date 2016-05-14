package edu.uci.isr.archstudio4.comp.archipelago.generic.logics.mapping;

import org.eclipse.draw2d.geometry.Point;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.UserEditableUtils;
import edu.uci.isr.bna4.assemblies.SplineAssembly;
import edu.uci.isr.bna4.facets.IHasMutableEndpoints;
import edu.uci.isr.bna4.facets.IHasMutableMidpoints;
import edu.uci.isr.bna4.facets.IHasMutableSelected;
import edu.uci.isr.bna4.facets.IRelativeMovable;
import edu.uci.isr.bna4.logics.information.ToolTipLogic;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public abstract class MapXadlSplineLogic
	extends
	AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic<SplineAssembly>{

	public MapXadlSplineLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		super(xarch, rootObjRef, relativePath, tptl, SplineAssembly.class, parentThing, kind);
		automapSimpleValue("description", "[no description]", "glass", ToolTipLogic.TOOL_TIP_PROPERTY_NAME, true);
	}

	@Override
	protected SplineAssembly addAssembly(IBNAModel model, ObjRef objRef, ObjRef[] relativeAncestorRefs){

		SplineAssembly assembly = new SplineAssembly(model, parentThing, kind);
		Point p = ArchipelagoUtils.findOpenSpotForNewThing(model);
		assembly.getSplineGlassThing().setEndpoint1(new Point(p.x - 50, p.y - 50));
		assembly.getSplineGlassThing().setEndpoint2(new Point(p.x + 50, p.y + 50));

		UserEditableUtils.addEditableQuality(assembly.getSplineGlassThing(), IHasMutableSelected.USER_MAY_SELECT, IRelativeMovable.USER_MAY_MOVE, IHasMutableMidpoints.USER_MAY_ADD_MIDPOINTS, IHasMutableMidpoints.USER_MAY_MOVE_MIDPOINTS, IHasMutableMidpoints.USER_MAY_REMOVE_MIDPOINTS, IHasMutableEndpoints.USER_MAY_MOVE_ENDPOINT1, IHasMutableEndpoints.USER_MAY_MOVE_ENDPOINT2, IHasMutableEndpoints.USER_MAY_RESTICK_ENDPOINT1, IHasMutableEndpoints.USER_MAY_RESTICK_ENDPOINT2);
		UserEditableUtils.addEditableQuality(assembly.getSplineGlassThing(), ToolTipLogic.USER_MAY_EDIT_TOOL_TIP);

		return assembly;
	}
}
