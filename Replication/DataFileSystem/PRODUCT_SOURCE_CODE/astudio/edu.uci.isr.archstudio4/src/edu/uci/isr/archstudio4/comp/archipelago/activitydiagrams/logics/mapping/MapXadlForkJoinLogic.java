package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.mapping;

import org.eclipse.swt.graphics.RGB;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.mapping.AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.UserEditableUtils;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.facets.IHasMutableSelected;
import edu.uci.isr.bna4.facets.IRelativeMovable;
import edu.uci.isr.bna4.logics.information.ToolTipLogic;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class MapXadlForkJoinLogic
	extends
	AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic<BoxAssembly>{

	public MapXadlForkJoinLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		super(xarch, rootObjRef, relativePath, tptl, BoxAssembly.class, parentThing, kind);
		automapSimpleValue("description", "[no description]", "glass", ToolTipLogic.TOOL_TIP_PROPERTY_NAME, true);
	}

	@Override
	protected BoxAssembly addAssembly(IBNAModel model, ObjRef objRef, ObjRef[] relativeAncestorRefs){

		BoxAssembly assembly = new BoxAssembly(model, parentThing, kind);
		assembly.getBoxGlassThing().setBoundingBox(ArchipelagoUtils.findOpenSpotForNewThing(model, 150, 5));
		assembly.getBoxThing().setColor(new RGB(0, 0, 0));
		assembly.getBoxGlassThing().setReferencePointFractionOffset(new float[]{0.5f, 0.5f});

		UserEditableUtils.addEditableQuality(assembly.getBoxGlassThing(), IHasMutableSelected.USER_MAY_SELECT, IRelativeMovable.USER_MAY_MOVE);
		UserEditableUtils.addEditableQuality(assembly.getBoxGlassThing(), ToolTipLogic.USER_MAY_EDIT_TOOL_TIP);

		return assembly;
	}
}
