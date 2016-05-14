package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams;

import org.eclipse.jface.viewers.Viewer;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.FolderNode;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoTreeContentProvider;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchPath;

public class ActivityDiagramsTreeContentProvider
	implements IArchipelagoTreeContentProvider{

	protected static final String FOLDER_NODE_TYPE = "ACTIVITYDIAGRAMS";
	protected FolderNode structureFolderNode = null;

	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	public ActivityDiagramsTreeContentProvider(ArchipelagoServices services, ObjRef xArchRef){
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
			if(refPath.toTagsOnlyString().equals("xArch/activitydiagram")){
				return true;
			}
		}
		return false;
	}

	public Object[] getChildren(Object parentElement, Object[] childrenFromPreviousProvider){
		if(isRootElement(parentElement)){
			if(structureFolderNode == null){
				structureFolderNode = new FolderNode(parentElement, FOLDER_NODE_TYPE, "Activity Diagrams");
			}
			return ArchipelagoUtils.combine(childrenFromPreviousProvider, new Object[]{structureFolderNode});
		}
		else if(isFolderNode(parentElement)){
			FolderNode fn = (FolderNode)parentElement;
			ObjRef xArchRef = (ObjRef)fn.getParent();
			ObjRef activitydiagramsContextRef = AS.xarch.createContext(xArchRef, "activitydiagrams");
			ObjRef[] activitydiagramRefs = AS.xarch.getAllElements(activitydiagramsContextRef, "activityDiagram", xArchRef);
			return ArchipelagoUtils.combine(childrenFromPreviousProvider, activitydiagramRefs);
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
