package edu.uci.isr.archstudio4.comp.archlight.issueadt;

import java.util.ArrayList;
import java.util.List;

import edu.uci.isr.archstudio4.archlight.ArchlightIssue;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;

public class ArchlightIssueADTMyxComponent extends AbstractMyxSimpleBrick implements ArchlightIssueADTListener, IMyxDynamicBrick, XArchFileListener {

	public static final IMyxName INTERFACE_NAME_IN_FILEEVENTS = MyxUtils.createName("xarchfileevents");
	public static final IMyxName INTERFACE_NAME_IN_ISSUEADT = MyxUtils.createName("archlightissueadt");
	public static final IMyxName INTERFACE_NAME_OUT_ISSUEADT_EVENTS = MyxUtils.createName("archlightissueadtevents");

	protected ArchlightIssueADT issueadt = null;
	protected ArchlightIssueADTListener issueadtListener = null;

	public ArchlightIssueADTMyxComponent() {
	}

	public void init() {
		ArchlightIssueADT issueadtImpl = new ArchlightIssueADT();
		this.issueadt = issueadtImpl;
		issueadtImpl.addArchlightIssueADTListener(this);
	}

	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject) {
		if (interfaceName.equals(INTERFACE_NAME_OUT_ISSUEADT_EVENTS)) {
			issueadtListener = (ArchlightIssueADTListener) serviceObject;
		}
	}

	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject) {
		if (interfaceName.equals(INTERFACE_NAME_OUT_ISSUEADT_EVENTS)) {
			issueadtListener = null;
		}
	}

	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject) {
	}

	public Object getServiceObject(IMyxName interfaceName) {
		if (interfaceName.equals(INTERFACE_NAME_IN_ISSUEADT)) {
			return issueadt;
		}
		else if (interfaceName.equals(INTERFACE_NAME_IN_FILEEVENTS)) {
			return this;
		}
		return null;
	}

	public synchronized void issueADTChanged(ArchlightIssueADTEvent evt) {
		if (issueadtListener != null) {
			issueadtListener.issueADTChanged(evt);
		}
	}

	public void removeIssues(ObjRef xArchRef) {
		List<ArchlightIssue> issues = new ArrayList<ArchlightIssue>();
		for (ArchlightIssue issue : issueadt.getAllIssues()) {
			if(xArchRef.equals(issue.getDocumentRef()))
				issues.add(issue);
		}
		issueadt.removeIssues(issues.toArray(new ArchlightIssue[issues.size()]));
	}

	public void handleXArchFileEvent(XArchFileEvent evt) {
		switch (evt.getEventType()) {
		case XArchFileEvent.XARCH_CLOSED_EVENT:
			removeIssues(evt.getXArchRef());
		}
	}
}
