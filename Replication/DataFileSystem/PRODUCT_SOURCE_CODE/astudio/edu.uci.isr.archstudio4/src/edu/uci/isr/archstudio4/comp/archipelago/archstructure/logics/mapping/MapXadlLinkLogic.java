package edu.uci.isr.archstudio4.comp.archipelago.archstructure.logics.mapping;

import org.eclipse.swt.SWT;

import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.mapping.BNAPath;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.mapping.MapXadlNondirectionalSplineLogic;
import edu.uci.isr.archstudio4.comp.archipelago.options.OptionsUtils;
import edu.uci.isr.bna4.BNAModelEvent;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.SplineAssembly;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchPath;

public class MapXadlLinkLogic extends MapXadlNondirectionalSplineLogic {

	public MapXadlLinkLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, ThingPropertyTrackingLogic tptl, IThing parentThing, Object kind) {
		super(xarch, rootObjRef, relativePath, tptl, parentThing, kind);
		automapLinkPointsToSplineEndpoints("glass", true);
		addTagsOnlyPrefixMapping("optional", new ISingleAssemblyMapping<SplineAssembly>() {
			public void updateAssembly(IBNAModel model, ObjRef objRef, SplineAssembly assembly, XArchFlatEvent evt, XArchPath relativeSourceTargetPath) {
				assembly.getSplineThing().setLineStyle(OptionsUtils.isOptional(MapXadlLinkLogic.this.xarch, objRef) ? SWT.LINE_DASH : SWT.LINE_SOLID);
			}

			public void storeAssemblyData(IBNAModel model, ObjRef objRef, SplineAssembly assembly, BNAModelEvent evt, BNAPath relativeBNAPath) {
			}
		});
	}
}
