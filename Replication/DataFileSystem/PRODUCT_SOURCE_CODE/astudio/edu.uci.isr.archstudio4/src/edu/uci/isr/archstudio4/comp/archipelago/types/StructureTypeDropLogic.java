package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.swt.dnd.DropTargetEvent;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.util.AbstractTypeDropLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.bna4.things.glass.EndpointGlassThing;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.widgets.swt.LocalSelectionTransfer;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class StructureTypeDropLogic extends AbstractTypeDropLogic{
	public StructureTypeDropLogic(ArchipelagoServices services, ObjRef xArchRef){
		super(services, xArchRef);
	}
	
	protected boolean acceptDrop(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY, Object data){
		if(t == null){
			//We can drop a component or connector type on nothing, that will cause a stamp-out
			if(data == null){
				return true;
			}
			else if(data instanceof ObjRef){
				if(AS.xarch.isInstanceOf((ObjRef)data, "types#ComponentType")){
					return true;
				}
				else if(AS.xarch.isInstanceOf((ObjRef)data, "types#ConnectorType")){
					return true;
				}
			}
		}
		else if(t instanceof BoxGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				if(StructureMapper.isComponentAssemblyRootThing(pt)){
					if(data == null){
						return true;
					}
					else if(data instanceof ObjRef){
						if(AS.xarch.isInstanceOf((ObjRef)data, "types#ComponentType")){
							return true;
						}
					}
				}
				else if(StructureMapper.isConnectorAssemblyRootThing(pt)){
					if(data == null){
						return true;
					}
					else if(data instanceof ObjRef){
						if(AS.xarch.isInstanceOf((ObjRef)data, "types#ConnectorType")){
							return true;
						}
					}
				}
			}
		}
		else if(t instanceof EndpointGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				if(StructureMapper.isInterfaceAssemblyRootThing(pt)){
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
			//This is a stamp-out drop.
			LocalSelectionTransfer transfer = LocalSelectionTransfer.getInstance();
			if(transfer.isSupportedType(event.currentDataType)){
				Object selection = transfer.nativeToJava(event.currentDataType);
				Object data = getDataFromSelection(selection);
				if((data != null) && (data instanceof ObjRef)){
					ObjRef typeRef = (ObjRef)data;
					String typeID = XadlUtils.getID(AS.xarch, typeRef);
					if(typeID != null){
						EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(view.getWorld().getBNAModel());
						String structureID = ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
						if(structureID != null){
							ObjRef archStructureRef = AS.xarch.getByID(xArchRef, structureID);
							if(archStructureRef != null){
								ArchipelagoUtils.setNewThingSpot(view.getWorld().getBNAModel(), worldX, worldY);
								ObjRef brickRef = XadlUtils.stampOutType(AS.xarch, typeRef, typeID, archStructureRef);
								if(brickRef != null){
									ArchipelagoUtils.showUserNotification(view.getWorld().getBNAModel(), "Type Stamped Out", worldX, worldY);
								}
							}
						}
					}
				}
			}
			return;
		}
		
		ObjRef targetRef = null;
		if(t != null){
			if((t instanceof BoxGlassThing) || (t instanceof EndpointGlassThing)){
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
