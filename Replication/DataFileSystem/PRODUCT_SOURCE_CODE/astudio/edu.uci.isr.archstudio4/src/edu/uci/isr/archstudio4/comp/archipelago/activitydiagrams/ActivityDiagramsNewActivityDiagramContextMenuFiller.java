package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams;

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

public class ActivityDiagramsNewActivityDiagramContextMenuFiller
	implements IArchipelagoTreeContextMenuFiller{

	protected TreeViewer viewer = null;
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	public ActivityDiagramsNewActivityDiagramContextMenuFiller(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
		this.viewer = viewer;
		this.AS = AS;
		this.xArchRef = xArchRef;
	}

	public void fillContextMenu(IMenuManager m, Object[] selectedNodes){
		if(selectedNodes != null && selectedNodes.length == 1){
			Object selectedNode = selectedNodes[0];
			if(selectedNode instanceof FolderNode){
				FolderNode fn = (FolderNode)selectedNode;
				String fnType = fn.getType();
				if(fnType != null){
					if(fnType.equals(ActivityDiagramsTreeContentProvider.FOLDER_NODE_TYPE)){
						IAction newActivityDiagramAction = new Action("New Activity Diagram"){

							@Override
							public void run(){
								createNewActivityDiagram();
							}
						};
						m.add(newActivityDiagramAction);
					}
				}
			}
		}
	}

	protected void createNewActivityDiagram(){
		ObjRef activitydiagramsContextRef = AS.xarch.createContext(xArchRef, "activitydiagrams");
		ObjRef newActivityDiagramRef = AS.xarch.createElement(activitydiagramsContextRef, "activityDiagram");
		String newID = UIDGenerator.generateUID("activitydiagram");

		AS.xarch.set(newActivityDiagramRef, "id", newID);
		XadlUtils.setDescription(AS.xarch, newActivityDiagramRef, "[New ActivityDiagram]");
		AS.xarch.add(xArchRef, "object", newActivityDiagramRef);
	}

}
