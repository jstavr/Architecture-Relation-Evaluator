package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.mapping;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.mapping.AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.UserEditableUtils;
import edu.uci.isr.bna4.assemblies.ActorAssembly;
import edu.uci.isr.bna4.assemblies.GenericAssembly;
import edu.uci.isr.bna4.facets.IHasMutableBoundingBox;
import edu.uci.isr.bna4.facets.IHasMutableSelected;
import edu.uci.isr.bna4.facets.IHasMutableText;
import edu.uci.isr.bna4.facets.IHasText;
import edu.uci.isr.bna4.facets.IRelativeMovable;
import edu.uci.isr.bna4.logics.coordinating.MirrorBoundingBoxLogic;
import edu.uci.isr.bna4.logics.coordinating.MoveWithLogic;
import edu.uci.isr.bna4.logics.information.ToolTipLogic;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.bna4.things.labels.BoxedLabelThing;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class MapXadlActorLogic
	extends
	AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic<GenericAssembly>{

	public MapXadlActorLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		super(xarch, rootObjRef, relativePath, tptl, GenericAssembly.class, parentThing, kind);
		automapSimpleValue("description", "[no description]", "label", IHasText.TEXT_PROPERTY_NAME, true);
		automapSimpleValue("description", "[no description]", "glass", ToolTipLogic.TOOL_TIP_PROPERTY_NAME, false);
	}

	@Override
	protected GenericAssembly addAssembly(IBNAModel model, ObjRef objRef, ObjRef[] relativeAncestorRefs){

		int size = 33;

		GenericAssembly assembly = new GenericAssembly(model, parentThing, kind);

		ActorAssembly actor = new ActorAssembly(model, assembly.getRootThing(), kind);
		actor.getGlass().setReferencePointFractionOffset(new float[]{0.5f, 0.0f});

		BoxedLabelThing boxedLabelThing = new BoxedLabelThing();
		model.addThing(boxedLabelThing, assembly.getRootThing());

		BoxGlassThing boxGlassThing = new BoxGlassThing();
		boxGlassThing.setMinBoundingBoxSize(new Point(size, size * 4));
		model.addThing(boxGlassThing, assembly.getRootThing());

		UserEditableUtils.addEditableQuality(boxGlassThing, IHasMutableSelected.USER_MAY_SELECT, IRelativeMovable.USER_MAY_MOVE, IHasMutableBoundingBox.USER_MAY_RESIZE);
		UserEditableUtils.addEditableQuality(boxedLabelThing, IHasMutableText.USER_MAY_EDIT_TEXT);

		assembly.markPart("actor", actor);
		assembly.markPart("label", boxedLabelThing);
		assembly.markPart("glass", boxGlassThing);

		MirrorBoundingBoxLogic.mirrorBoundingBox(boxGlassThing, new Rectangle(0, size * 3, 0, -size * 3), boxedLabelThing);
		MoveWithLogic.moveWith(boxGlassThing, MoveWithLogic.TRACK_BOUNDING_BOX_ONLY, actor.getGlass());

		Point p = ArchipelagoUtils.findOpenSpotForNewThing(model);
		boxGlassThing.setBoundingBox(new Rectangle(p.x, p.y, size * 2, size * 4));
		actor.getGlass().setBoundingBox(new Rectangle(p.x + size - size / 2, p.y, size, size * 3));

		return assembly;
	}
}
