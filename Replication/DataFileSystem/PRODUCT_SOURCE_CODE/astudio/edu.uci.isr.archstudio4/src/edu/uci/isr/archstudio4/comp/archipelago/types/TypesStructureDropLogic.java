package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNADragAndDropListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.facets.IHasBoundingBox;
import edu.uci.isr.bna4.things.borders.PulsingBorderThing;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.widgets.swt.LocalSelectionTransfer;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class TypesStructureDropLogic extends AbstractThingLogic implements IBNADragAndDropListener{
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	protected PulsingBorderThing pulser = null;
	
	public TypesStructureDropLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	protected static Object getDataFromSelection(Object selection){
		if(selection != null){
			if(selection instanceof IStructuredSelection){
				IStructuredSelection ss = (IStructuredSelection)selection;
				Object[] allSelected = ss.toArray();
				if(allSelected.length == 0){
					return null;
				}
				else if(allSelected.length == 1){
					return allSelected[0];
				}
				else{
					return allSelected;
				}
			}
		}
		return null;
	}
	
	protected boolean acceptDrop(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY){
		//Two possibilities: either the platform we are on can give us the data
		//or it can't.  If it can, we can make a more educated decision here.
		
		Object data = null;
		LocalSelectionTransfer transfer = LocalSelectionTransfer.getInstance();
		if(transfer.isSupportedType(event.currentDataType)){
			Object o = transfer.nativeToJava(event.currentDataType);
			data = getDataFromSelection(o);
		}
		return acceptDrop(view, event, t, worldX, worldY, data);
	}
	
	protected boolean acceptDrop(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY, Object data){
		if(t instanceof BoxGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				if(TypesMapper.isBrickTypeAssemblyRootThing(pt)){
					if(data == null){
						//the data is not available, assume we can drop
						return true;
					}
					else if(data instanceof ObjRef){
						if(AS.xarch.isInstanceOf((ObjRef)data, "types#ArchStructure")){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public void dragEnter(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY){
		if(acceptDrop(view, event, t, worldX, worldY)){
			event.detail = DND.DROP_LINK;
		}
	}
	
	public void dragLeave(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY){
		if(pulser != null){
			view.getWorld().getBNAModel().removeThing(pulser);
			pulser = null;
		}
	}
	
	public void dragOver(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY){
		if(acceptDrop(view, event, t, worldX, worldY)){
			event.detail = DND.DROP_LINK;
			if((pulser == null) && (t != null) && (t instanceof IHasBoundingBox)){
				pulser = new PulsingBorderThing();
				pulser.setBoundingBox(((IHasBoundingBox)t).getBoundingBox());
				view.getWorld().getBNAModel().addThing(pulser);
			}
			else if((pulser != null) && (t != null) && (t instanceof IHasBoundingBox)){
				pulser.setBoundingBox(((IHasBoundingBox)t).getBoundingBox());
			}
			else if(pulser != null){
				view.getWorld().getBNAModel().removeThing(pulser);
				pulser = null;
			}
		}
		else{
			if(pulser != null){
				view.getWorld().getBNAModel().removeThing(pulser);
				pulser = null;
			}
		}
	}
	
	public void dropAccept(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY){
		if(acceptDrop(view, event, t, worldX, worldY)){
			event.detail = DND.DROP_LINK;
			LocalSelectionTransfer transfer = LocalSelectionTransfer.getInstance();
			for(int i = 0; i < event.dataTypes.length; i++){
				if(transfer.isSupportedType(event.dataTypes[i])){
					event.currentDataType = event.dataTypes[i];
					break;
				}
			}
		}
	}
	
	public void drop(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY){
		if(pulser != null){
			view.getWorld().getBNAModel().removeThing(pulser);
			pulser = null;
		}
		
		if(!acceptDrop(view, event, t, worldX, worldY)){
			return;
		}
		
		boolean b = MessageDialog.openConfirm(BNAUtils.getParentComposite(view).getShell(), "Confirm Set Substructure", "Are you sure you want assign this substructure to this type?");
    if(!b) return;
    
		ObjRef targetRef = null;
		if(t != null){
			if(t instanceof BoxGlassThing){
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
					ObjRef structureRef = (ObjRef)data;
					String structureID = XadlUtils.getID(AS.xarch, structureRef);
					if(structureID != null){
						boolean addSubArchitecture = false;
						ObjRef subArchitectureRef = (ObjRef)AS.xarch.get(targetRef, "subArchitecture");
						if(subArchitectureRef == null){
							addSubArchitecture = true;
							ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "types");
							subArchitectureRef = AS.xarch.create(typesContextRef, "subArchitecture");
						}
						XadlUtils.setXLink(AS.xarch, subArchitectureRef, "archStructure", structureID);
						if(addSubArchitecture){
							AS.xarch.set(targetRef, "subArchitecture", subArchitectureRef);
						}
						ArchipelagoUtils.showUserNotification(view.getWorld().getBNAModel(), "Substructure Assigned", worldX, worldY);
					}
				}
			}
		}
	}

	public void dragOperationChanged(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY){
	}
}
