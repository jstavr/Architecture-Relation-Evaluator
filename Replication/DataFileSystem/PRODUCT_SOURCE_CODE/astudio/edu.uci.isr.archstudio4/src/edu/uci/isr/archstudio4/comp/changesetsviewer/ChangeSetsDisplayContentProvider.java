package edu.uci.isr.archstudio4.comp.changesetsviewer;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT.IChangeReference;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.xarchflat.ObjRef;

public class ChangeSetsDisplayContentProvider
	implements ITreeContentProvider{

	XArchChangeSetInterface xArch;
	ObjRef xArchRef;
	IChangeSetADT csadt;

	public ChangeSetsDisplayContentProvider(XArchChangeSetInterface xArch, ObjRef xArchRef, IChangeSetADT csadt){
		this.xArch = xArch;
		this.xArchRef = xArchRef;
		this.csadt = csadt;
	}

	public Object[] getElements(Object inputElement){
		return new IChangeReference[]{csadt.getElementReference(xArchRef, new ObjRef[]{xArchRef}, false)};
	}

	public Object getParent(Object element){
		if(element instanceof IChangeReference){
			IChangeReference cRef = (IChangeReference)element;
			return csadt.getParentReference(xArchRef, cRef);
		}
		return null;
	}

	public Object[] getChildren(Object parentElement){
		if(parentElement instanceof IChangeReference){
			IChangeReference cRef = (IChangeReference)parentElement;
			return csadt.getChildReferences(xArchRef, xArch.getAppliedChangeSetRefs(xArchRef), cRef);
		}
		return new IChangeReference[0];
	}

	public boolean hasChildren(Object parentElement){
		return true;
	}

	public void dispose(){
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
	}
}
