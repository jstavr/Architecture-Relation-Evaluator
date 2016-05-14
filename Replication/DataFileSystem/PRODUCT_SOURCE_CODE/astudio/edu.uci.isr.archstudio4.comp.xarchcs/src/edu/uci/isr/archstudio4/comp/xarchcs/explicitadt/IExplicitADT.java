package edu.uci.isr.archstudio4.comp.xarchcs.explicitadt;

import edu.uci.isr.xarchflat.ObjRef;

public interface IExplicitADT{

	public ObjRef[] getExplicitChangeSetRefs(ObjRef xArchRef);

	public void setExplicitChangeSetRefs(ObjRef xArchRef, ObjRef[] changeSetRefs);

	public void setExplicitADTEventListener(ExplicitADTListener explicitADTEventListener);
}
