package edu.uci.isr.archstudio4.comp.archipelago.statecharts.logics.mapping;

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
import edu.uci.isr.xarchflat.utils.XArchRelativePathTracker.RequiredAttributeFilter;

public class MapXadlStateLogic
	extends AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic<BoxAssembly>{

	public MapXadlStateLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		super(xarch, rootObjRef, relativePath, tptl, BoxAssembly.class, parentThing, kind);
		automapSimpleValue("description", "[no description]", "label", IHasText.TEXT_PROPERTY_NAME, true);
		automapSimpleValue("description", "[no description]", "glass", ToolTipLogic.TOOL_TIP_PROPERTY_NAME, false);
	}

	public MapXadlStateLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, String reqAttrName, String reqAttrValue, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		this(xarch, rootObjRef, relativePath, tptl, parentThing, kind);
		pathTracker.addFilter(new RequiredAttributeFilter(xarch, reqAttrName, reqAttrValue));
	}

	@Override
	protected BoxAssembly addAssembly(IBNAModel model, ObjRef objRef, ObjRef[] relativeAncestorRefs){

		BoxAssembly assembly = new BoxAssembly(model, parentThing, kind);
		assembly.getBoxGlassThing().setBoundingBox(ArchipelagoUtils.findOpenSpotForNewThing(model, 125, 100));
		assembly.getBoxGlassThing().setCornerWidth(45);
		assembly.getBoxGlassThing().setCornerHeight(45);
		assembly.getBoxThing().setColor(new RGB(245, 245, 245));

		UserEditableUtils.addEditableQuality(assembly.getBoxGlassThing(), IHasMutableSelected.USER_MAY_SELECT, IHasMutableBoundingBox.USER_MAY_RESIZE, IRelativeMovable.USER_MAY_MOVE);
		UserEditableUtils.addEditableQuality(assembly.getBoxedLabelThing(), IHasMutableText.USER_MAY_EDIT_TEXT);

		return assembly;
	}
}
