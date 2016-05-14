package edu.uci.isr.archstudio4.comp.xarchcs.xarchcs;

import java.util.Arrays;

import edu.uci.isr.xarchflat.ObjRef;

public class XArchChangeSetEvent
	implements java.io.Serializable{

	private static final long serialVersionUID = 6992529007468267679L;

	public static enum ChangeSetEventType{

		/**
		 * Indicates that change sets have been enabled/disabled.
		 */
		UPDATED_ENABLED,

		/**
		 * Indicates that a new active change set has been applied.
		 */
		UPDATED_ACTIVE_CHANGE_SET,

		/**
		 * Indicates that new change sets are going to be applied. <b>Note:</b>
		 * this is only useful if listening to event synchronously, otherwise,
		 * listen for {@link #UPDATED_APPLIED_CHANGE_SETS}.
		 */
		UPDATING_APPLIED_CHANGE_SETS,

		/**
		 * Indicates that new change sets have been applied.
		 */
		UPDATED_APPLIED_CHANGE_SETS
	}

	ChangeSetEventType eventType;

	ObjRef xArchRef;

	boolean enabled;

	ObjRef activeChangeSetRef;

	ObjRef[] appliedChangeSetRefs;

	public XArchChangeSetEvent(ChangeSetEventType eventType, ObjRef xArchRef, boolean enabled, ObjRef activeChangeSetRef, ObjRef[] appliedChangeSetRefs){
		this.eventType = eventType;
		this.xArchRef = xArchRef;
		this.enabled = enabled;
		this.activeChangeSetRef = activeChangeSetRef;
		this.appliedChangeSetRefs = appliedChangeSetRefs;
	}

	public ChangeSetEventType getEventType(){
		return eventType;
	}

	public ObjRef getXArchRef(){
		return xArchRef;
	}

	public boolean isEnabled(){
		return enabled;
	}

	public ObjRef getActiveChangeSet(){
		return activeChangeSetRef;
	}

	public ObjRef[] getAppliedChangeSets(){
		return appliedChangeSetRefs;
	}

	@Override
	public String toString(){
		return "EventType=" + eventType + ", xArchRef=" + xArchRef + ", enabled=" + enabled + ", activeChangeSetRef=" + activeChangeSetRef + ", appliedChangeSetRefs=" + Arrays.asList(appliedChangeSetRefs);
	}
}
