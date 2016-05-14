package edu.uci.isr.archstudio4.comp.relatedelements.processors;

import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElement;
import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElementsProcessor;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class RelatedRationaleElement implements IRelatedElement{

	ObjRef sourceObjRef;
	
	ObjRef relatedObjRef;
	
	XArchFlatInterface xArch;
	
	String description;
	
	IRelatedElementsProcessor relatedRationaleElementsProcessor;
	
	boolean defaultSelection;
	
	
	public RelatedRationaleElement(ObjRef sourceObjRef,ObjRef relatedObjRef,String description,boolean defaultSelection) {
		this.relatedObjRef = relatedObjRef;
		this.description = description;
		this.sourceObjRef = sourceObjRef;
		this.defaultSelection = defaultSelection;
	}

	public String getDescription() {
		return this.description;
	}

	public ObjRef getRelatedObjRef() {
		return relatedObjRef;
	}
	
	public IRelatedElementsProcessor getRelatedElementsProcessor() {
		return this.relatedRationaleElementsProcessor;
	}

	public ObjRef getSourceObjRef() {
		return this.sourceObjRef;
	}

	public ObjRef getTargetReferenceRef(XArchFlatInterface xArch,ObjRef targetXArchRef) {
		return null;
	}	
	
	public boolean isDefaultSelection() {
		return defaultSelection;
	}
}
