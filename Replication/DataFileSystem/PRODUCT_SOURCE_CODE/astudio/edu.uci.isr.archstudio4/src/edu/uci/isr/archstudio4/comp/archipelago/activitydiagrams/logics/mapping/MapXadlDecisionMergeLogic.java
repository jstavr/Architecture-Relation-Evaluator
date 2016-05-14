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
import edu.uci.isr.bna4.facets.IHasMutableSelected;
import edu.uci.isr.bna4.facets.IRelativeMovable;
import edu.uci.isr.bna4.logics.information.ToolTipLogic;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.bna4.things.essence.PolygonEssenceThing.IPolygonGenerator;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class MapXadlDecisionMergeLogic
	extends
	AbstractAutomapSingleAssemblyXArchRelativePathMappingLogic<PolygonAssembly>{

	public MapXadlDecisionMergeLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind){
		super(xarch, rootObjRef, relativePath, tptl, PolygonAssembly.class, parentThing, kind);
		automapSimpleValue("description", "[no description]", "glass", ToolTipLogic.TOOL_TIP_PROPERTY_NAME, true);
	}

	@Override
	protected PolygonAssembly addAssembly(IBNAModel model, ObjRef objRef, ObjRef[] relativeAncestorRefs){

		PolygonAssembly assembly = new PolygonAssembly(model, parentThing, kind, new IPolygonGenerator(){

			public Point[] calculatePoints(Rectangle r){
				int x1 = r.x;
				int y1 = r.y;
				int x2 = r.x + r.width;
				int y2 = r.y + r.height;
				int xh = (x1 + x2) / 2;
				int yh = (y1 + y2) / 2;

				return new Point[]{new Point(xh, y1), new Point(x2, yh), new Point(xh, y2), new Point(x1, yh), new Point(xh, y1)};
			}
		});
		assembly.getPolygonGlassThing().setBoundingBox(ArchipelagoUtils.findOpenSpotForNewThing(model, 50, 50));
		assembly.getPolygonThing().setColor(new RGB(255, 255, 0));
		assembly.getPolygonBorderThing().setColor(new RGB(0, 0, 0));
		assembly.getPolygonGlassThing().setReferencePointFractionOffset(new float[]{0.5f, 0.5f});

		UserEditableUtils.addEditableQuality(assembly.getPolygonGlassThing(), IHasMutableSelected.USER_MAY_SELECT, IRelativeMovable.USER_MAY_MOVE);
		UserEditableUtils.addEditableQuality(assembly.getPolygonGlassThing(), ToolTipLogic.USER_MAY_EDIT_TOOL_TIP);

		return assembly;
	}
}
