package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.viewers.Viewer;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.FolderNode;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoTreeContentProvider;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchPath;

public class StructureTreeContentProvider implements IArchipelagoTreeContentProvider{
	protected static final String FOLDER_NODE_TYPE = "STRUCTURES";
	protected FolderNode structureFolderNode = null;
	
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	public StructureTreeContentProvider(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	protected boolean isRootElement(Object ref){
		if(ref instanceof ObjRef){
			return AS.xarch.isInstanceOf((ObjRef)ref, "#XArch");
		}
		return false;
	}
	
	protected boolean isFolderNode(Object fn){
		if(fn instanceof FolderNode){
			return ((FolderNode)fn).getType().equals(FOLDER_NODE_TYPE);
		}
		return false;
	}
	
	protected boolean isTargetNode(Object ref){
		if(ref instanceof ObjRef){
			XArchPath refPath = AS.xarch.getXArchPath((ObjRef)ref);
			if(refPath.toTagsOnlyString().equals("xArch/archStructure")){
				return true;
			}
		}
		return false;
	}
	
	public Object[] getChildren(Object parentElement, Object[] childrenFromPreviousProvider){
		if(isRootElement(parentElement)){
			if(structureFolderNode == null){
				structureFolderNode = new FolderNode((ObjRef)parentElement, FOLDER_NODE_TYPE, "Structures");
			}
			return ArchipelagoUtils.combine(childrenFromPreviousProvider, new Object[]{structureFolderNode});
		}
		else if(isFolderNode(parentElement)){
			FolderNode fn = (FolderNode)parentElement;
			ObjRef xArchRef = (ObjRef)((FolderNode)fn).getParent();
			ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "types");
			ObjRef[] archStructureRefs = AS.xarch.getAllElements(typesContextRef, "archStructure", xArchRef);
			return ArchipelagoUtils.combine(childrenFromPreviousProvider, archStructureRefs);
		}
		return childrenFromPreviousProvider;
	}
	
	public boolean hasChildren(Object element, boolean hasChildrenFromPreviousProvider){
		if(isRootElement(element)){
			return true;
		}
		else if(isFolderNode(element)){
			return getChildren(element, null).length > 0;
		}
		return hasChildrenFromPreviousProvider;
	}
	
	public Object getParent(Object element, Object parentFromPreviousProvider){
		if(isTargetNode(element)){
			return structureFolderNode;
		}
		if(isFolderNode(element)){
			return ((FolderNode)element).getParent();
		}
		return parentFromPreviousProvider;
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
	}

	public void dispose(){
	}
	
}
