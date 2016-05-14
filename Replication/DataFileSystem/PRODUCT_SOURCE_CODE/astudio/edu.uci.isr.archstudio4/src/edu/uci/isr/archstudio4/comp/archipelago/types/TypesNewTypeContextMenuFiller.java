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

public class TypesNewTypeContextMenuFiller implements IArchipelagoTreeContextMenuFiller{
	protected TreeViewer viewer = null;
	protected ArchipelagoServices AS =  null;
	protected ObjRef xArchRef = null;
	
	public TypesNewTypeContextMenuFiller(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
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
					String elementName = null;
					String descriptiveName = null;
					if(fnType.equals(TypesTreeContentProvider.COMPONENT_TYPES_FOLDER_NODE_TYPE)){
						elementName = "componentType";
						descriptiveName = "Component Type";
					}
					else if(fnType.equals(TypesTreeContentProvider.CONNECTOR_TYPES_FOLDER_NODE_TYPE)){
						elementName = "connectorType";
						descriptiveName = "Connector Type";
					}
					else if(fnType.equals(TypesTreeContentProvider.INTERFACE_TYPES_FOLDER_NODE_TYPE)){
						elementName = "interfaceType";
						descriptiveName = "Inteface Type";
					}
					else{
						return;
					}
					final String felementName = elementName;
					final String fdescriptiveName = descriptiveName;
					IAction newTypeAction = new Action("New " + descriptiveName){
						public void run(){
							createNewType(felementName, fdescriptiveName);
						}
					};
					m.add(newTypeAction);
				}
			}
		}
	}

	protected void createNewType(String elementName, String descriptiveName){
		ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "types");
		ObjRef archTypesRef = AS.xarch.getElement(typesContextRef, "archTypes", xArchRef);
		if(archTypesRef != null){
			ObjRef newTypeRef = AS.xarch.create(typesContextRef, elementName);
			String newID = UIDGenerator.generateUID(elementName);
			AS.xarch.set(newTypeRef, "id", newID);
			XadlUtils.setDescription(AS.xarch, newTypeRef, "[New " + descriptiveName + "]");
			AS.xarch.add(archTypesRef, elementName, newTypeRef);
		}
	}

}
