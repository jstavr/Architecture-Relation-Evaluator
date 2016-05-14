package edu.uci.isr.archstudio4.comp.xarchcs.views.changesets;

import edu.uci.isr.xarchflat.ObjRef;

public interface IChangeSetState {

	public enum EChangeSetState {
		APPLIED, IMPLIED, UNAPPLIED, EXCLUDED
	}

	public void addChangeSetStateListener(IChangeSetStateListener listener);

	public void removeChangeSetStateListener(IChangeSetStateListener listener);

	public ObjRef getXArchRef();

	public void setXArchRef(ObjRef xArchRef);

	public EChangeSetState getChangeSetState(ObjRef changeSetRef);

	public void setChangeSetState(ObjRef changeSetRef, EChangeSetState changeSetState);

	public boolean isExplicit(ObjRef changeSetRef);

	public void setExplicit(ObjRef changeSetRef, boolean explicit);
}
