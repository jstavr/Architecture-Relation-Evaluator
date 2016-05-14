package edu.uci.isr.archstudio4.comp.archipelago.types;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.util.AbstractEditDirectionLogic;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.glass.EndpointGlassThing;
import edu.uci.isr.xarchflat.ObjRef;

public class TypesEditDirectionLogic extends AbstractEditDirectionLogic{
	
	public TypesEditDirectionLogic(ArchipelagoServices AS, ObjRef xArchRef){
		super(AS, xArchRef);
	}
	
	public boolean matches(IBNAView view, IThing t){
		if(t instanceof EndpointGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				return TypesMapper.isSignatureAssemblyRootThing(pt);
			}
		}
		return false;
	}
	
	public String getXArchID(IBNAView view, IThing t){
		if(t instanceof EndpointGlassThing){
			IThing parentThing = view.getWorld().getBNAModel().getParentThing(t);
			return parentThing.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		}
		return null;
	}
}
