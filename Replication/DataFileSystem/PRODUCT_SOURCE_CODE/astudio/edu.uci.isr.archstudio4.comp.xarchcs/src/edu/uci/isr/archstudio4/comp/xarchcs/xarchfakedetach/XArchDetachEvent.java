package edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach;

import edu.uci.isr.xarchflat.ObjRef;

public class XArchDetachEvent{

	public enum DetachEventType{
		DETACHED, DETACHED_UPDATED, ATTACHED
	}

	DetachEventType eventType;

	ObjRef objRef;

	Object oldReason;

	Object newReason;

	public XArchDetachEvent(DetachEventType eventType, ObjRef objRef, Object oldReason, Object newReason){
		this.eventType = eventType;
		this.objRef = objRef;
		this.oldReason = oldReason;
		this.newReason = newReason;
	}

	public DetachEventType getEventType(){
		return eventType;
	}

	public ObjRef getObjRef(){
		return objRef;
	}

	public Object getOldReason(){
		return oldReason;
	}

	public Object getNewReason(){
		return newReason;
	}
}
