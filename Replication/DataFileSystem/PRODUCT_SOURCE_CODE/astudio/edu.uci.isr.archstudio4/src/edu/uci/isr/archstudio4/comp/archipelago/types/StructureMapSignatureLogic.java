package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.util.AbstractXadlSelectorLogic;
import edu.uci.isr.archstudio4.util.ArchstudioResources;
import edu.uci.isr.archstudio4.util.XadlTreeUtils;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.glass.EndpointGlassThing;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class StructureMapSignatureLogic extends AbstractXadlSelectorLogic{

	public StructureMapSignatureLogic(ArchipelagoServices services, ObjRef xArchRef){
		super(services, xArchRef);
	}
	
	public boolean matches(IBNAView view, IThing t){
		if(t instanceof EndpointGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				return StructureMapper.isInterfaceAssemblyRootThing(pt);
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

	public Object getInitialValue(ObjRef eltRef){
		if(eltRef != null){
			ObjRef sigRef = XadlUtils.resolveXLink(AS.xarch, eltRef, "signature");
			return sigRef;
		}
		return null;
	}
	
	public String getMenuItemString(){
		return "Map to Signature...";
	}
	
	public ImageDescriptor getMenuItemIcon(ObjRef eltRef){
		return AS.resources.getImageDescriptor(ArchstudioResources.ICON_INTERFACE);
	}
	
	public ObjRef getRootRef(ObjRef eltRef){
		if(eltRef != null){
			ObjRef brickRef = AS.xarch.getParent(eltRef);
			if(brickRef != null){
				ObjRef typeRef = XadlUtils.resolveXLink(AS.xarch, brickRef, "type");
				return typeRef;
			}
		}
		return null;
	}
	
	public int getFlags(ObjRef eltRef){
		return XadlTreeUtils.ANY_TYPE | XadlTreeUtils.ANY_SIGNATURE;
	}
	
	public void setValue(ObjRef eltRef, Object newValue){
		if(newValue instanceof ObjRef){
			ObjRef newRef = (ObjRef)newValue;
			if(AS.xarch.isInstanceOf(newRef, "types#Signature")){
				String signatureID = XadlUtils.getID(AS.xarch, newRef);
				if(signatureID != null){
					XadlUtils.setXLink(AS.xarch, eltRef, "signature", signatureID);
				}
			}
		}
	}
	
	protected IAction[] getActions(IBNAView view, IThing t, int worldX, int worldY){
		IAction[] actions = super.getActions(view, t, worldX, worldY);
		if((actions == null) || (actions.length == 0)) return actions;
		
		String eltXArchID = getXArchID(view, t);
		if(eltXArchID != null){
			ObjRef eltRef = AS.xarch.getByID(xArchRef, eltXArchID);
			if(eltRef != null){
				ObjRef parentRef = AS.xarch.getParent(eltRef);
				if(parentRef != null){
					ObjRef typeRef = XadlUtils.resolveXLink(AS.xarch, parentRef, "type");
					if(typeRef != null){
						if(AS.xarch.isInstanceOf(typeRef, "types#ComponentType")){
							return actions;
						}
						else if(AS.xarch.isInstanceOf(typeRef, "types#ConnectorType")){
							return actions;
						}
					}
				}
			}
		}
		
		for(IAction action : actions){
			action.setEnabled(false);
		}
		return actions;
	}
	
}
