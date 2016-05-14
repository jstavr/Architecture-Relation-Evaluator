package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.TreeViewer;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoTreeContextMenuFiller;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class TypesNewTypeSetContextMenuFiller implements IArchipelagoTreeContextMenuFiller{
	protected TreeViewer viewer = null;
	protected ArchipelagoServices AS =  null;
	protected ObjRef xArchRef = null;
	
	public TypesNewTypeSetContextMenuFiller(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
		this.viewer = viewer;
		this.AS = AS;
		this.xArchRef = xArchRef;
	}
	
	
	public void fillContextMenu(IMenuManager m, Object[] selectedNodes){
		if((selectedNodes != null) && (selectedNodes.length == 1)){
			Object selectedNode = selectedNodes[0];
			if((selectedNode != null) && (selectedNode.equals(xArchRef))){
				IAction newTypeSetAction = new Action("Create Type Set"){
					public void run(){
						createNewTypeSet();
					}
				};
				ObjRef archTypesRef = XadlUtils.getArchTypes(AS.xarch, xArchRef);
				if(archTypesRef != null){
					newTypeSetAction.setEnabled(false);
				}
				
				m.add(newTypeSetAction);
			}
		}
	}
	
	protected void createNewTypeSet(){
		ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "types");
		ObjRef archTypesRef = AS.xarch.createElement(typesContextRef, "archTypes");
		AS.xarch.add(xArchRef, "object", archTypesRef);
	}

}
