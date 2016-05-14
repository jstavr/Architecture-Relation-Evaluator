package edu.uci.isr.archstudio4.comp.relatedelements;

import java.util.List;

import edu.uci.isr.xarchflat.ObjRef;

public interface IRelatedElementsManager {
	List<IRelatedElement> getRelatedElements(ObjRef xArchRef,ObjRef objRef);
}
