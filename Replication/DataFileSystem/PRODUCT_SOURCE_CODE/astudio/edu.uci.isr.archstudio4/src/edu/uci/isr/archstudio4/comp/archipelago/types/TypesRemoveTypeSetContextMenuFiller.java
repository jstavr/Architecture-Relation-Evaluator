package edu.uci.isr.archstudio4.comp.archipelago.types;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoTreeContextMenuFiller;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class TypesRemoveTypeSetContextMenuFiller implements IArchipelagoTreeContextMenuFiller{
	protected TreeViewer viewer = null;
	protected ArchipelagoServices AS =  null;
	protected ObjRef xArchRef = null;

	public TypesRemoveTypeSetContextMenuFiller(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
		this.viewer = viewer;
		this.AS = AS;
		this.xArchRef = xArchRef;
	}
	
	public void fillContextMenu(IMenuManager m, Object[] selectedNodes){
		if((selectedNodes != null) && (selectedNodes.length == 1)){
			Object selectedNode = selectedNodes[0];
			if(selectedNode instanceof ObjRef){
				final ObjRef targetRef = (ObjRef)selectedNode;
				if((targetRef != null) && (AS.xarch.isInstanceOf(targetRef, "types#ArchTypes"))){
					IAction removeAction = new Action("Remove All Types..."){
						public void run(){
							removeTypeSet();
						}
					};
					m.add(removeAction);
				}
			}
		}
	}
	
	protected void removeTypeSet(){
		boolean b = MessageDialog.openConfirm(viewer.getTree().getShell(), "Confirm Remove All Types", "Are you sure you want to remove ALL types?");
    if(!b) return;
    
    ObjRef archTypesRef = XadlUtils.getArchTypes(AS.xarch, xArchRef);
    if(archTypesRef != null){
    	AS.xarch.remove(xArchRef, "object", archTypesRef);
    }
	}
}
