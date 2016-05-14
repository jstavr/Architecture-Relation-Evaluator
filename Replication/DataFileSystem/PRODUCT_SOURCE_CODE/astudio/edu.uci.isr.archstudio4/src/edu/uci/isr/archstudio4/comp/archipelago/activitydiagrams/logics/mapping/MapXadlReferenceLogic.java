package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.mapping;

import org.eclipse.swt.graphics.RGB;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.mapping.AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.UserEditableUtils;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.facets.IHasMutableBoundingBox;
import edu.uci.isr.bna4.facets.IHasMutableSelected;
import edu.uci.isr.bna4.facets.IHasMutableText;
import edu.uci.isr.bna4.facets.IHasText;
import edu.uci.isr.bna4.facets.IRelativeMovable;
import edu.uci.isr.bna4.logics.information.ToolTipLogic;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class MapXadlReferenceLogic
	extends
	AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic<BoxAssembly>{

	public MapXadlReferenceLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		super(xarch, rootObjRef, relativePath, tptl, BoxAssembly.class, parentThing, kind);
		automapSimpleValue("description", "[no description]", "label", IHasText.TEXT_PROPERTY_NAME, true);
		automapSimpleValue("description", "[no description]", "glass", ToolTipLogic.TOOL_TIP_PROPERTY_NAME, false);
	}

	@Override
	protected BoxAssembly addAssembly(IBNAModel model, ObjRef objRef, ObjRef[] relativeAncestorRefs){

		BoxAssembly assembly = new BoxAssembly(model, parentThing, kind);
		assembly.getBoxGlassThing().setBoundingBox(ArchipelagoUtils.findOpenSpotForNewThing(model, 125, 100));
		assembly.getBoxGlassThing().setCornerHeight(25);
		assembly.getBoxGlassThing().setCornerWidth(25);
		assembly.getBoxThing().setColor(new RGB(188, 143, 143));

		UserEditableUtils.addEditableQuality(assembly.getBoxGlassThing(), IHasMutableSelected.USER_MAY_SELECT, IHasMutableBoundingBox.USER_MAY_RESIZE, IRelativeMovable.USER_MAY_MOVE);
		UserEditableUtils.addEditableQuality(assembly.getBoxedLabelThing(), IHasMutableText.USER_MAY_EDIT_TEXT);

		return assembly;
	}
}
