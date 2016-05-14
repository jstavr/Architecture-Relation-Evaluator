package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.viewers.TreeViewer;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.util.AbstractRemoveContextMenuFiller;
import edu.uci.isr.xarchflat.ObjRef;

public class StructureRemoveContextMenuFiller extends AbstractRemoveContextMenuFiller{

	public StructureRemoveContextMenuFiller(TreeViewer viewer, ArchipelagoServices services, ObjRef xArchRef){
		super(viewer, services, xArchRef);
	}
	
	protected boolean matches(Object node){
		if(node != null){
			if(node instanceof ObjRef){
				ObjRef targetRef = (ObjRef)node;
				if(AS.xarch.isInstanceOf(targetRef, "types#ArchStructure")){
					return true;
				}
			}
		}
		return false;
	}
	
	protected void remove(ObjRef targetRef){
		if(targetRef != null){
			if(AS.xarch.isInstanceOf(targetRef, "types#ArchStructure")){
				AS.xarch.remove(xArchRef, "Object", targetRef);
				return;
			}
		}
		super.remove(targetRef);
	}
}
