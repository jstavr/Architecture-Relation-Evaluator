package edu.uci.isr.archstudio4.comp.xarchcs.xarchcs;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.ChangeStatus;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.IChangeSetSyncMonitor;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public interface XArchChangeSetInterface
    extends XArchFlatInterface{

	public void enableChangeSets(ObjRef xArchRef, IChangeSetSyncMonitor monitor);

	public boolean getChangeSetsEnabled(ObjRef xArchRef);

	public void setActiveChangeSetRef(ObjRef xArchRef, ObjRef activeChangeSetRef);

	public ObjRef getActiveChangeSetRef(ObjRef xArchRef);

	public void setAppliedChangeSetRefs(ObjRef xArchRef, ObjRef[] changeSetRefs, IChangeSetSyncMonitor monitor);

	public ObjRef[] getAppliedChangeSetRefs(ObjRef xArchRef);

	public ChangeStatus getChangeStatus(ObjRef objRef, ObjRef[] changeSetRefs);

	public ChangeStatus[] getAllChangeStatus(ObjRef objRef, ObjRef[] changeSetRefs);

	public void diffToExternalFile(ObjRef sourceXArchRef, ObjRef targetXArchRef);

	public void diffFromExternalFile(ObjRef sourceXArchRef, ObjRef targetXArchRef);

	public ObjRef[] seperateOutChangeSets(ObjRef sourceXArchRef, String relativePath, IChangeSetSyncMonitor monitor);

	public void moveChange(ObjRef itemRef, ObjRef[] contextChangeSetRefs, ObjRef targetChangeSetRef);

	public boolean getOverviewMode(ObjRef xArchRef);
	
	public void setOverviewMode(ObjRef xArchRef, boolean overviewMode);
}
