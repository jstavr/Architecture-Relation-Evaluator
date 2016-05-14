package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.viewers.Viewer;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.FolderNode;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoTreeContentProvider;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchPath;

public class TypesTreeContentProvider implements IArchipelagoTreeContentProvider{
	protected static final String COMPONENT_TYPES_FOLDER_NODE_TYPE = "COMPONENTTYPES";
	protected static final String CONNECTOR_TYPES_FOLDER_NODE_TYPE = "CONNECTORTYPES";
	protected static final String INTERFACE_TYPES_FOLDER_NODE_TYPE = "INTERFACETYPES";
	
	protected FolderNode componentTypesFolderNode = null;
	protected FolderNode connectorTypesFolderNode = null;
	protected FolderNode interfaceTypesFolderNode = null;
	
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	public TypesTreeContentProvider(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	protected boolean isRootElement(Object ref){
		if(ref instanceof ObjRef){
			return AS.xarch.isInstanceOf((ObjRef)ref, "#XArch");
		}
		return false;
	}
	
	protected boolean isTypesNode(Object ref){
		return isTargetNode(ref, "xArch/archTypes");
	}
	
	protected boolean isComponentTypesFolderNode(Object fn){
		if(fn instanceof FolderNode){
			return ((FolderNode)fn).getType().equals(COMPONENT_TYPES_FOLDER_NODE_TYPE);
		}
		return false;
	}
	
	protected boolean isConnectorTypesFolderNode(Object fn){
		if(fn instanceof FolderNode){
			return ((FolderNode)fn).getType().equals(CONNECTOR_TYPES_FOLDER_NODE_TYPE);
		}
		return false;
	}
	
	protected boolean isInterfaceTypesFolderNode(Object fn){
		if(fn instanceof FolderNode){
			return ((FolderNode)fn).getType().equals(INTERFACE_TYPES_FOLDER_NODE_TYPE);
		}
		return false;
	}
	
	private boolean isTargetNode(Object ref, String pathStart){
		if(ref instanceof ObjRef){
			XArchPath refPath = AS.xarch.getXArchPath((ObjRef)ref);
			if(refPath.toTagsOnlyString().equals(pathStart)){
				return true;
			}
		}
		return false;
	}
	
	protected boolean isComponentTypeNode(Object o){
		return isTargetNode(o, "xArch/archStructure/componentType");
	}
	
	protected boolean isConnectorTypeNode(Object o){
		return isTargetNode(o, "xArch/archStructure/connectorType");
	}
	
	protected boolean isInterfaceTypeNode(Object o){
		return isTargetNode(o, "xArch/archStructure/interfaceType");
	}
	
	public Object[] getChildren(Object parentElement, Object[] childrenFromPreviousProvider){
		if(isRootElement(parentElement)){
			ObjRef typesContextRef = AS.xarch.createContext((ObjRef)parentElement, "types");
			ObjRef archTypesRef = AS.xarch.getElement(typesContextRef, "archTypes", (ObjRef)parentElement);
			if(archTypesRef != null){
				return ArchipelagoUtils.combine(childrenFromPreviousProvider, new Object[]{archTypesRef});
			}
		}
		else if(isTypesNode(parentElement)){
			if(componentTypesFolderNode == null){
				componentTypesFolderNode = new FolderNode(parentElement, COMPONENT_TYPES_FOLDER_NODE_TYPE, "Component Types");
			}
			if(connectorTypesFolderNode == null){
				connectorTypesFolderNode = new FolderNode(parentElement, CONNECTOR_TYPES_FOLDER_NODE_TYPE, "Connector Types");
			}
			if(interfaceTypesFolderNode == null){
				interfaceTypesFolderNode = new FolderNode(parentElement, INTERFACE_TYPES_FOLDER_NODE_TYPE, "Interface Types");
			}
			return ArchipelagoUtils.combine(childrenFromPreviousProvider, new Object[]{componentTypesFolderNode, connectorTypesFolderNode, interfaceTypesFolderNode});
		}
		else if(isComponentTypesFolderNode(parentElement)){
			ObjRef archTypesRef = (ObjRef)((FolderNode)parentElement).getParent();
			ObjRef[] componentTypes = AS.xarch.getAll(archTypesRef, "componentType");
			return ArchipelagoUtils.combine(childrenFromPreviousProvider, componentTypes);
		}
		else if(isConnectorTypesFolderNode(parentElement)){
			ObjRef archTypesRef = (ObjRef)((FolderNode)parentElement).getParent();
			ObjRef[] connectorTypes = AS.xarch.getAll(archTypesRef, "connectorType");
			return ArchipelagoUtils.combine(childrenFromPreviousProvider, connectorTypes);
		}
		else if(isInterfaceTypesFolderNode(parentElement)){
			ObjRef archTypesRef = (ObjRef)((FolderNode)parentElement).getParent();
			ObjRef[] interfaceTypes = AS.xarch.getAll(archTypesRef, "interfaceType");
			return ArchipelagoUtils.combine(childrenFromPreviousProvider, interfaceTypes);
		}
		return childrenFromPreviousProvider;
	}
	
	public boolean hasChildren(Object element, boolean hasChildrenFromPreviousProvider){
		if(hasChildrenFromPreviousProvider) return true;
		Object[] children = getChildren(element, null);
		if((children != null) && (children.length > 0)) return true;
		return hasChildrenFromPreviousProvider;
	}
	
	public Object getParent(Object element, Object parentFromPreviousProvider){
		if(isComponentTypeNode(element)){
			return componentTypesFolderNode;
		}
		if(isConnectorTypeNode(element)){
			return connectorTypesFolderNode;
		}
		if(isInterfaceTypeNode(element)){
			return interfaceTypesFolderNode;
		}
		if(isComponentTypesFolderNode(element)){
			return ((FolderNode)element).getParent();
		}
		if(isConnectorTypesFolderNode(element)){
			return ((FolderNode)element).getParent();
		}
		if(isInterfaceTypesFolderNode(element)){
			return ((FolderNode)element).getParent();
		}
		if(isTypesNode(element)){
			return AS.xarch.getXArch((ObjRef)element);
		}
		return parentFromPreviousProvider;
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
	}

	public void dispose(){
	}
	
}
