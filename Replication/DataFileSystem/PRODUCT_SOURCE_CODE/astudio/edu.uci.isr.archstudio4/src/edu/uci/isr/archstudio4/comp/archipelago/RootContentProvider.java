package edu.uci.isr.archstudio4.comp.archipelago;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchSite;

import edu.uci.isr.xarchflat.ObjRef;

public class RootContentProvider implements IArchipelagoTreeContentProvider{

	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	public RootContentProvider(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	public Object[] getChildren(Object parentElement, Object[] childrenFromPreviousProvider){
		if(parentElement instanceof IWorkbenchSite){
			return new Object[]{xArchRef};
		}
		return childrenFromPreviousProvider;
	}
	
	public Object getParent(Object element, Object parentFromPreviousProvider){
		return parentFromPreviousProvider;
	}
	
	public boolean hasChildren(Object element, boolean hasChildrenFromPreviousProvider){
		if(element instanceof IWorkbenchSite){
			return true;
		}
		return hasChildrenFromPreviousProvider;
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
	}
	
	public void dispose(){
	}
}
