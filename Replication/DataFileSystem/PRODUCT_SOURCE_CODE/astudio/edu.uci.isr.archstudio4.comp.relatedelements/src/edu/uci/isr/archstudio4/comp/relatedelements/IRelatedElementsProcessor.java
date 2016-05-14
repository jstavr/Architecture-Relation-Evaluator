package edu.uci.isr.archstudio4.comp.relatedelements;

import java.util.List;
import java.util.Map;

import edu.uci.isr.xarchflat.ObjRef;

public interface IRelatedElementsProcessor {
	List<IRelatedElement> getRelatedElements(ObjRef objRef);
	ObjRef getTargetReferenceRef(ObjRef targetXArchRef,ObjRef pasteRef,IRelatedElement relatedElement, Map<String,ObjRef> oldXArchIDToNewObjRefMap,String newIdSuffix);
}
