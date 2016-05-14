package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.TreeViewer;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.FolderNode;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoTreeContextMenuFiller;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class StructureNewStructureContextMenuFiller implements IArchipelagoTreeContextMenuFiller{
	protected TreeViewer viewer = null;
	protected ArchipelagoServices AS =  null;
	protected ObjRef xArchRef = null;
	
	public StructureNewStructureContextMenuFiller(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
		this.viewer = viewer;
		this.AS = AS;
		this.xArchRef = xArchRef;
	}
	
	
	public void fillContextMenu(IMenuManager m, Object[] selectedNodes){
		if((selectedNodes != null) && (selectedNodes.length == 1)){
			Object selectedNode = selectedNodes[0];
			if(selectedNode instanceof FolderNode){
				FolderNode fn = (FolderNode)selectedNode;
				String fnType = fn.getType();
				if(fnType != null){
					if(fnType.equals(StructureTreeContentProvider.FOLDER_NODE_TYPE)){
						IAction newStructureAction = new Action("New Structure"){
							public void run(){
								createNewStructure();
							}
						};
						m.add(newStructureAction);
					}
				}
			}
		}
	}

	protected void createNewStructure(){
		ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "types");
		ObjRef newStructureRef = AS.xarch.createElement(typesContextRef, "archStructure");
		String newID = UIDGenerator.generateUID("archStructure");
		
		AS.xarch.set(newStructureRef, "id", newID);
		XadlUtils.setDescription(AS.xarch, newStructureRef, "[New Structure]");
		AS.xarch.add(xArchRef, "object", newStructureRef);
	}

}
