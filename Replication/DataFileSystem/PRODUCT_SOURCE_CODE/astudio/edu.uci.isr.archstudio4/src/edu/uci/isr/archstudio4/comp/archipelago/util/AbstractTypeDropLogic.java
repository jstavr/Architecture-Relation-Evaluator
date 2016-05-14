package edu.uci.isr.archstudio4.comp.archipelago.util;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.IBNADragAndDropListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.facets.IHasBoundingBox;
import edu.uci.isr.bna4.things.borders.PulsingBorderThing;
import edu.uci.isr.widgets.swt.LocalSelectionTransfer;
import edu.uci.isr.xarchflat.ObjRef;

public abstract class AbstractTypeDropLogic extends AbstractThingLogic implements IBNADragAndDropListener{
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	protected PulsingBorderThing pulser = null;
	
	public AbstractTypeDropLogic(ArchipelagoServices services, ObjRef xArchRef){
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
	
	protected abstract boolean acceptDrop(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY, Object data);
	
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
	
	public void dragOperationChanged(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY){
	}
	
	public abstract void drop(IBNAView view, DropTargetEvent event, IThing t, int worldX, int worldY);
}
