package edu.uci.isr.archstudio4.comp.archipelago.statecharts;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.util.AbstractEditDescriptionCellModifier;
import edu.uci.isr.xarchflat.ObjRef;

public class StatechartsEditDescriptionCellModifier extends AbstractEditDescriptionCellModifier{

	public StatechartsEditDescriptionCellModifier(ArchipelagoServices services, ObjRef xArchRef){
		super(services, xArchRef);
	}
	
	public boolean canModify(Object element, String property){
		if((element != null) && (element instanceof ObjRef)){
			ObjRef targetRef = (ObjRef)element;
			if(AS.xarch.isInstanceOf(targetRef, "statecharts#Statechart")){
				return true;
			}
		}
		return false;
	}
}
