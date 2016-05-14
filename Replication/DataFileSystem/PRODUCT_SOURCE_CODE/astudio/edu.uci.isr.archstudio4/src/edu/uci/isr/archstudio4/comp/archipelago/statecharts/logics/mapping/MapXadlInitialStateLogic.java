package edu.uci.isr.archstudio4.comp.archipelago.statecharts.logics.mapping;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.mapping.AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.UserEditableUtils;
import edu.uci.isr.bna4.assemblies.EllipseAssembly;
import edu.uci.isr.bna4.facets.IHasMutableSelected;
import edu.uci.isr.bna4.facets.IRelativeMovable;
import edu.uci.isr.bna4.logics.information.ToolTipLogic;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.utils.XArchRelativePathTracker.RequiredAttributeFilter;

public class MapXadlInitialStateLogic
	extends AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic<EllipseAssembly>{

	public MapXadlInitialStateLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		super(xarch, rootObjRef, relativePath, tptl, EllipseAssembly.class, parentThing, kind);
		automapSimpleValue("description", "[no description]", "glass", ToolTipLogic.TOOL_TIP_PROPERTY_NAME, true);
	}

	public MapXadlInitialStateLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, String reqAttrName, String reqAttrValue, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		this(xarch, rootObjRef, relativePath, tptl, parentThing, kind);
		pathTracker.addFilter(new RequiredAttributeFilter(xarch, reqAttrName, reqAttrValue));
	}

	@Override
	protected EllipseAssembly addAssembly(IBNAModel model, ObjRef objRef, ObjRef[] relativeAncestorRefs){

		EllipseAssembly assembly = new EllipseAssembly(model, parentThing, kind);
		assembly.getEllipseGlassThing().setBoundingBox(ArchipelagoUtils.findOpenSpotForNewThing(model, 26, 26));
		assembly.getEllipseGlassThing().setReferencePointFractionOffset(new float[]{0.5f, 0.5f});

		UserEditableUtils.addEditableQuality(assembly.getEllipseGlassThing(), IHasMutableSelected.USER_MAY_SELECT, IRelativeMovable.USER_MAY_MOVE);
		UserEditableUtils.addEditableQuality(assembly.getEllipseGlassThing(), ToolTipLogic.USER_MAY_EDIT_TOOL_TIP);

		return assembly;
	}
}
