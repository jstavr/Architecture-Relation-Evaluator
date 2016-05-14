package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.mapping;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.draw2d.geometry.Rectangle;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.mapping.AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.UserEditableUtils;
import edu.uci.isr.bna4.assemblies.PolygonAssembly;
import edu.uci.isr.bna4.facets.IHasMutableBoundingBox;
import edu.uci.isr.bna4.facets.IHasMutableSelected;
import edu.uci.isr.bna4.facets.IHasMutableText;
import edu.uci.isr.bna4.facets.IHasText;
import edu.uci.isr.bna4.facets.IRelativeMovable;
import edu.uci.isr.bna4.logics.coordinating.MirrorBoundingBoxLogic;
import edu.uci.isr.bna4.logics.information.ToolTipLogic;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.bna4.things.essence.PolygonEssenceThing.IPolygonGenerator;
import edu.uci.isr.bna4.things.labels.BoxedLabelThing;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class MapXadlNoteLogic
	extends
	AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic<PolygonAssembly>{

	public MapXadlNoteLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		super(xarch, rootObjRef, relativePath, tptl, PolygonAssembly.class, parentThing, kind);
		automapSimpleValue("description", "[no description]", "label", IHasText.TEXT_PROPERTY_NAME, true);
		automapSimpleValue("description", "[no description]", "glass", ToolTipLogic.TOOL_TIP_PROPERTY_NAME, false);
	}

	@Override
	protected PolygonAssembly addAssembly(IBNAModel model, ObjRef objRef, ObjRef[] relativeAncestorRefs){

		PolygonAssembly assembly = new PolygonAssembly(model, parentThing, kind, new IPolygonGenerator(){

			public Point[] calculatePoints(Rectangle r){
				int x1 = r.x;
				int y1 = r.y;
				int x2 = r.x + r.width;
				int y2 = r.y + r.height;

				int foldSize = 11;

				return new Point[]{new Point(x1, y1), new Point(x2, y1), new Point(x2, y2 - foldSize), new Point(x2 - foldSize, y2), new Point(x2 - foldSize, y2 - foldSize), new Point(x2, y2 - foldSize), new Point(x2 - foldSize, y2), new Point(x1, y2), new Point(x1, y1)};
			}
		});
		assembly.getPolygonThing().setColor(new RGB(255, 255, 128));
		assembly.getPolygonBorderThing().setColor(new RGB(0, 0, 0));
		assembly.getPolygonGlassThing().setBoundingBox(ArchipelagoUtils.findOpenSpotForNewThing(model, 200, 75));

		BoxedLabelThing boxedLabelThing = new BoxedLabelThing();
		model.addThing(boxedLabelThing, assembly.getPolygonThing());

		MirrorBoundingBoxLogic.mirrorBoundingBox(assembly.getPolygonGlassThing(), new Rectangle(5, 5, -10, -10), boxedLabelThing);

		UserEditableUtils.addEditableQuality(assembly.getPolygonGlassThing(), IHasMutableSelected.USER_MAY_SELECT, IHasMutableBoundingBox.USER_MAY_RESIZE, IRelativeMovable.USER_MAY_MOVE);
		UserEditableUtils.addEditableQuality(boxedLabelThing, IHasMutableText.USER_MAY_EDIT_TEXT);

		assembly.markPart("label", boxedLabelThing);

		return assembly;
	}
}
