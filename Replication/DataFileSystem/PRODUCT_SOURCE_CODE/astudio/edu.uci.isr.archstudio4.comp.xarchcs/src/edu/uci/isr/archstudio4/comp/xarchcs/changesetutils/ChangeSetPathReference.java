package edu.uci.isr.archstudio4.comp.xarchcs.changesetutils;

import edu.uci.isr.xarchflat.ObjRef;

public class ChangeSetPathReference {
	
	// A reference to a change set
	final private ObjRef changeSetRef;
	
	// A specific path to a segment within the above change set
	final private String xArchPath;
	
	public ChangeSetPathReference(ObjRef changeSetRef, String xArchPath) {
		super();
		if(changeSetRef == null)
			throw new NullPointerException();
		this.changeSetRef = changeSetRef;
		this.xArchPath = xArchPath;
	}
	
	public ObjRef getChangeSetRef() {
		return changeSetRef;
	}
	
	public String getXArchPath() {
		return xArchPath;
	}
	
	public boolean equals (Object o) {
		if (o == null) return false;
		if (! (o instanceof ChangeSetPathReference)) return false;
		ChangeSetPathReference cspr = (ChangeSetPathReference) o;
		return ((changeSetRef.equals(cspr.getChangeSetRef())) &&
				(xArchPath.equals(cspr.getXArchPath())));
	}
	
	public int hashCode() {
		return xArchPath.hashCode() + (31 * changeSetRef.hashCode());
	}
}
