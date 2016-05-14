package edu.uci.isr.archstudio4.comp.xarchcs.xarchcs;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.ChangeStatus;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.IChangeSetSyncMonitor;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatImplDelegate;

public class XArchChangeSetImplDelegate
    extends XArchFlatImplDelegate
    implements XArchChangeSetInterface{

	protected final XArchChangeSetInterface xarch;

	public XArchChangeSetImplDelegate(XArchChangeSetInterface xarch){
		super(xarch);
		this.xarch = xarch;
	}

	public void enableChangeSets(ObjRef archRef, IChangeSetSyncMonitor monitor){
		xarch.enableChangeSets(archRef, monitor);
	}

	public ObjRef getActiveChangeSetRef(ObjRef archRef){
		return xarch.getActiveChangeSetRef(archRef);
	}

	public ObjRef[] getAppliedChangeSetRefs(ObjRef archRef){
		return xarch.getAppliedChangeSetRefs(archRef);
	}

	public boolean getChangeSetsEnabled(ObjRef archRef){
		return xarch.getChangeSetsEnabled(archRef);
	}

	public ChangeStatus getChangeStatus(ObjRef objRef, ObjRef[] changeSetRefs){
		return xarch.getChangeStatus(objRef, changeSetRefs);
	}

	public void setActiveChangeSetRef(ObjRef archRef, ObjRef activeChangeSetRef){
		xarch.setActiveChangeSetRef(archRef, activeChangeSetRef);
	}

	public void setAppliedChangeSetRefs(ObjRef archRef, ObjRef[] changeSetRefs, IChangeSetSyncMonitor monitor){
		xarch.setAppliedChangeSetRefs(archRef, changeSetRefs, monitor);
	}

	public void diffToExternalFile(ObjRef sourceXArchRef, ObjRef targetXArchRef){
		xarch.diffToExternalFile(sourceXArchRef, targetXArchRef);
	}

	public void diffFromExternalFile(ObjRef sourceXArchRef, ObjRef targetXArchRef){
		xarch.diffFromExternalFile(sourceXArchRef, targetXArchRef);
	}

	public void moveChange(ObjRef itemRef, ObjRef[] contextChangeSetRefs, ObjRef targetChangeSetRef){
		xarch.moveChange(itemRef, contextChangeSetRefs, targetChangeSetRef);
	}

	public ObjRef[] seperateOutChangeSets(ObjRef sourceXArchRef, String relativePath, IChangeSetSyncMonitor monitor){
		return xarch.seperateOutChangeSets(sourceXArchRef, relativePath, monitor);
	}

	public boolean getOverviewMode(ObjRef xArchRef) {
	    return xarch.getOverviewMode(xArchRef);
    }

	public void setOverviewMode(ObjRef xArchRef, boolean overviewMode) {
	    xarch.setOverviewMode(xArchRef, overviewMode);
    }

	public ChangeStatus[] getAllChangeStatus(ObjRef objRef, ObjRef[] changeSetRefs) {
	    return xarch.getAllChangeStatus(objRef, changeSetRefs);
    }
	
	
}