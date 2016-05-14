package edu.uci.isr.archstudio4.comp.xarchcs.explicitadt;

import edu.uci.isr.xarchflat.ObjRef;

public class ExplicitADTEvent{

	public enum ExplicitEventType{
		UPDATED_EXPLICIT_CHANGE_SETS, UPDATED_EXPLICIT_OBJREF
	}

	ExplicitEventType type;

	ObjRef xArchRef;

	ObjRef[] changeSetRefs;

	ObjRef objRef;

	public ExplicitADTEvent(ExplicitEventType type, ObjRef xArchRef, ObjRef[] changeSetRefs, ObjRef objRef){
		this.type = type;
		this.xArchRef = xArchRef;
		this.changeSetRefs = changeSetRefs;
		this.objRef = objRef;
	}

	public ExplicitEventType getEventType(){
		return type;
	}

	public ObjRef getXArchRef(){
		return xArchRef;
	}

	public ObjRef[] getChangeSetRefs(){
		return changeSetRefs;
	}

	public ObjRef getObjRef(){
		return objRef;
	}
}
