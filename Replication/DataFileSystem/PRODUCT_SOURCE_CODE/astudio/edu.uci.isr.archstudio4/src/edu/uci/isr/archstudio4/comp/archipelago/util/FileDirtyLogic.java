package edu.uci.isr.archstudio4.comp.archipelago.util;

import java.util.HashSet;
import java.util.Set;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAModelEvent;
import edu.uci.isr.bna4.IBNAModelListener;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.ThingEvent;
import edu.uci.isr.bna4.facets.IHasAnchorPoint;
import edu.uci.isr.bna4.facets.IHasBoundingBox;
import edu.uci.isr.bna4.facets.IHasColor;
import edu.uci.isr.bna4.facets.IHasEndpoints;
import edu.uci.isr.bna4.facets.IHasMidpoints;
import edu.uci.isr.bna4.things.borders.MarqueeBoxBorderThing;
import edu.uci.isr.bna4.things.glass.ReshapeHandleGlassThing;
import edu.uci.isr.bna4.things.shapes.ReshapeHandleThing;
import edu.uci.isr.xarchflat.ObjRef;

public class FileDirtyLogic
	extends AbstractThingLogic
	implements IBNAModelListener{

	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	protected static Set<String> propertyNameSet = new HashSet<String>();

	static{
		propertyNameSet.add(IHasBoundingBox.BOUNDING_BOX_PROPERTY_NAME);
		propertyNameSet.add(IHasAnchorPoint.ANCHOR_POINT_PROPERTY_NAME);
		propertyNameSet.add(IHasMidpoints.MIDPOINTS_PROPERTY_NAME);
		propertyNameSet.add(IHasEndpoints.ENDPOINT_1_PROPERTY_NAME);
		propertyNameSet.add(IHasEndpoints.ENDPOINT_2_PROPERTY_NAME);
		propertyNameSet.add(IHasColor.COLOR_PROPERTY_NAME);
	}

	public FileDirtyLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}

	public void bnaModelChanged(BNAModelEvent evt){
		ThingEvent tevt = evt.getThingEvent();
		if(tevt != null){
			IThing targetThing = tevt.getTargetThing();
			if(targetThing != null){
				if(targetThing instanceof MarqueeBoxBorderThing){
					return;
				}
				if(targetThing instanceof ReshapeHandleThing){
					return;
				}
				if(targetThing instanceof ReshapeHandleGlassThing){
					return;
				}
			}
			Object propertyName = tevt.getPropertyName();
			if(propertyName != null){
				if(propertyNameSet.contains(propertyName)){
					AS.fileman.makeDirty(xArchRef);
				}
			}
		}
	}
}
