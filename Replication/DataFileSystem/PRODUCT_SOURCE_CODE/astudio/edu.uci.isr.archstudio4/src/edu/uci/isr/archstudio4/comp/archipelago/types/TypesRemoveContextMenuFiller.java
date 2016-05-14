package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.viewers.TreeViewer;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.util.AbstractRemoveContextMenuFiller;
import edu.uci.isr.xarchflat.ObjRef;

public class TypesRemoveContextMenuFiller extends AbstractRemoveContextMenuFiller{

	public TypesRemoveContextMenuFiller(TreeViewer viewer, ArchipelagoServices services, ObjRef xArchRef){
		super(viewer, services, xArchRef);
	}
	
	protected boolean matches(Object node){
		if(node != null){
			if(node instanceof ObjRef){
				ObjRef targetRef = (ObjRef)node;
				if(AS.xarch.isInstanceOf(targetRef, "types#ComponentType")){
					return true;
				}
				if(AS.xarch.isInstanceOf(targetRef, "types#ConnectorType")){
					return true;
				}
				if(AS.xarch.isInstanceOf(targetRef, "types#InterfaceType")){
					return true;
				}
			}
		}
		return false;
	}
}
