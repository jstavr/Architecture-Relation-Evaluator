package edu.uci.isr.archstudio4.comp.relatedelements;

import java.util.List;

import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public interface IRelatedElement {

	ObjRef getSourceObjRef();
	ObjRef getRelatedObjRef();
	String getDescription();
	IRelatedElementsProcessor getRelatedElementsProcessor();
	boolean isDefaultSelection();
}
