package edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusview;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusview.IChangeSetStatusView.ChangeSetStatusData;
import edu.uci.isr.xarchflat.ObjRef;


public class ChangeSetStatusViewEvent {
	
	public enum CSStatusViewEventType{
		STATUS_UPDATED
	}
	
	private CSStatusViewEventType eventType;

	private ObjRef xArchRef;
	
	private String xArchPath;
	
	private ChangeSetStatusData statusData;
	
	
	public CSStatusViewEventType getEventType() {
		return eventType;
	}

	public ObjRef getXArchRef() {
		return xArchRef;
	}
	
	public String getXArchPath() {
		return xArchPath;
	}
	
	public ChangeSetStatusData getStatusData() {
		return statusData;
	}

	public ChangeSetStatusViewEvent(CSStatusViewEventType eventType,
			ObjRef xArchRef, String xArchPath, ChangeSetStatusData statusData) {
		super();
		this.eventType = eventType;
		this.xArchRef = xArchRef;
		this.xArchPath = xArchPath;
		this.statusData = statusData;
	}

}
