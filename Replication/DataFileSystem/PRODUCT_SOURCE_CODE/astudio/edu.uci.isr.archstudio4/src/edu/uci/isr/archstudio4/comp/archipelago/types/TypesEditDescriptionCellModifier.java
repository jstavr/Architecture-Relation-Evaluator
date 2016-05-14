package edu.uci.isr.archstudio4.comp.archipelago.types;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.util.AbstractEditDescriptionCellModifier;
import edu.uci.isr.xarchflat.ObjRef;

public class TypesEditDescriptionCellModifier extends AbstractEditDescriptionCellModifier{

	public TypesEditDescriptionCellModifier(ArchipelagoServices services, ObjRef xArchRef){
		super(services, xArchRef);
	}
	
	public boolean canModify(Object element, String property){
		if((element != null) && (element instanceof ObjRef)){
			ObjRef targetRef = (ObjRef)element;
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
		return false;
	}
}
