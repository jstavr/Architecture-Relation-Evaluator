package edu.uci.isr.archstudio4.comp.xarchcs.changesetidview;

import edu.uci.isr.xarchflat.ObjRef;


public class ChangeSetIdViewEvent {
	
	public enum CSIdViewEventType{
		CREATOR_ADDED, CREATOR_REMOVED, REFERENCE_ADDED, REFERENCE_REMOVED, INITIALIZED
	}

	private CSIdViewEventType eventType;

	private String id;	// an id for an element that was created within a changeset

	private ObjRef objRef;	// this is either the change set that creates the object (CREATOR_ADDED/CREATOR_REMOVED)
	                        // with the above id or a change set that references that object (REFERENCE_ADDED/REFERENCE_REMOVED). 
	
	private ObjRef xArchRef;
	
	private boolean isInitialized;


	/**
	 * This constructor is only used to create an INITIALIZED event
	 * @param xArchRef
	 */
	public ChangeSetIdViewEvent (ObjRef xArchRef){
		this.eventType = CSIdViewEventType.INITIALIZED;
		this.id = null;
		this.objRef = null;
		this.xArchRef = xArchRef;
		this.isInitialized = true;
	}
	
	public ChangeSetIdViewEvent(CSIdViewEventType eventType, String id, ObjRef objRef, ObjRef xArchRef, boolean isInitialized){
		this.eventType = eventType;
		this.id = id;
		this.objRef = objRef;
		this.xArchRef = xArchRef;
		this.isInitialized = isInitialized;
	}

	public ChangeSetIdViewEvent(CSIdViewEventType eventType, String id, ObjRef objRef, ObjRef xArchRef){
		this.eventType = eventType;
		this.id = id;
		this.objRef = objRef;
		this.xArchRef = xArchRef;
		this.isInitialized = true;
	}

	public CSIdViewEventType getEventType() {
		return eventType;
	}


	public String getId() {
		return id;
	}


	public ObjRef getObjRef() {
		return objRef;
	}
	
	
	public ObjRef getXArchRef() {
		return xArchRef;
	}

	
	public boolean getIsInitialized() {
		return isInitialized;
	}
	
}
