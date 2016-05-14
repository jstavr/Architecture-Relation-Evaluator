package edu.uci.isr.archstudio4.comp.xarchcs.xarchcs;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.ChangeSetSyncImpl.RemovedReason;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach.XArchDetachInterface;
import edu.uci.isr.xarchflat.ObjRef;

public class XArchChangeSetExplicitFilterImpl
	extends XArchChangeSetLegacyFilterImpl{

	public XArchChangeSetExplicitFilterImpl(XArchChangeSetInterface xarch, XArchDetachInterface xarchd){
		super(xarch, xarchd);
	}

	@Override
	protected boolean isDetached(ObjRef objRef, Object reason){
		if(RemovedReason.EXPLICITLY_REMOVED == reason || RemovedReason.OVERVIEW_ADDED == reason){
			return false;
		}
		return super.isDetached(objRef, reason);
	}
}
