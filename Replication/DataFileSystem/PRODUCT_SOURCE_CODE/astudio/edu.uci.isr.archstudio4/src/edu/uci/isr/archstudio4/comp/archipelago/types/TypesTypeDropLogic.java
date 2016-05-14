package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.swt.dnd.DropTargetEvent;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.util.AbstractTypeDropLogic;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.glass.EndpointGlassThing;
import edu.uci.isr.widgets.swt.LocalSelectionTransfer;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class TypesTypeDropLogic extends AbstractTypeDropLogic{
	public TypesTypeDropLogic(ArchipelagoServices services, ObjRef xArchRef){
		super(services, xArchRef);
	}
	
	protected boolean acceptDrop(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY, Object data){
		if(t == null){
			return false;
		}
		else if(t instanceof EndpointGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				if(TypesMapper.isSignatureAssemblyRootThing(pt)){
					if(data == null){
						return true;
					}
					else if(data instanceof ObjRef){
						if(AS.xarch.isInstanceOf((ObjRef)data, "types#InterfaceType")){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public void drop(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY){
		if(pulser != null){
			view.getWorld().getBNAModel().removeThing(pulser);
			pulser = null;
		}
		
		if(!acceptDrop(view, event, t, worldX, worldY)){
			return;
		}
		
		if(t == null){
			return;
		}
		
		ObjRef targetRef = null;
		if(t != null){
			if(t instanceof EndpointGlassThing){
				IThing pt = view.getWorld().getBNAModel().getParentThing(t);
				if(pt != null){
					String xArchID = pt.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
					if(xArchID != null){
						targetRef = AS.xarch.getByID(xArchRef, xArchID);
					}
				}
			}
		}
		
		if(targetRef != null){
			LocalSelectionTransfer transfer = LocalSelectionTransfer.getInstance();
			if(transfer.isSupportedType(event.currentDataType)){
				Object selection = transfer.nativeToJava(event.currentDataType);
				Object data = getDataFromSelection(selection);
				if((data != null) && (data instanceof ObjRef)){
					ObjRef typeRef = (ObjRef)data;
					String typeID = XadlUtils.getID(AS.xarch, typeRef);
					if(typeID != null){
						XadlUtils.setXLink(AS.xarch, targetRef, "type", typeID);
						ArchipelagoUtils.showUserNotification(view.getWorld().getBNAModel(), "Type Assigned", worldX, worldY);
					}
				}
			}
		}
	}

}
