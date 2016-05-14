package edu.uci.isr.archstudio4.comp.archipelago.statecharts;

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

public class StatechartsNewStatechartContextMenuFiller implements IArchipelagoTreeContextMenuFiller{
	protected TreeViewer viewer = null;
	protected ArchipelagoServices AS =  null;
	protected ObjRef xArchRef = null;
	
	public StatechartsNewStatechartContextMenuFiller(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
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
					if(fnType.equals(StatechartsTreeContentProvider.FOLDER_NODE_TYPE)){
						IAction newStatechartAction = new Action("New Statechart"){
							public void run(){
								createNewStatechart();
							}
						};
						m.add(newStatechartAction);
					}
				}
			}
		}
	}

	protected void createNewStatechart(){
		ObjRef statechartsContextRef = AS.xarch.createContext(xArchRef, "statecharts");
		ObjRef newStatechartRef = AS.xarch.createElement(statechartsContextRef, "statechart");
		String newID = UIDGenerator.generateUID("statechart");
		
		AS.xarch.set(newStatechartRef, "id", newID);
		XadlUtils.setDescription(AS.xarch, newStatechartRef, "[New Statechart]");
		AS.xarch.add(xArchRef, "object", newStatechartRef);
	}

}
