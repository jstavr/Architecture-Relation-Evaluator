package edu.uci.isr.archstudio4.comp.relatedelements.processors;

import java.util.Map;

import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElement;
import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElementsProcessor;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class RelatedHintedElement implements IRelatedElement{

	ObjRef relatedObjRef;
	
	ObjRef sourceObjRef;
	
	XArchFlatInterface xArch;
	
	String description;
	
	IRelatedElementsProcessor relatedHintedElementsProcessor;
	
	Map<String,String> properties;
	
	boolean defaultSelection;
	
	public RelatedHintedElement(ObjRef sourceObjRef,ObjRef relatedObjRef,String description,IRelatedElementsProcessor relatedHintElementsProcessor,Map<String,String> properties,boolean defaultSelection) {
		this.sourceObjRef = sourceObjRef;
		this.relatedObjRef = relatedObjRef;
		this.description = description;
		this.relatedHintedElementsProcessor = relatedHintElementsProcessor;
		this.properties = properties;
		this.defaultSelection = defaultSelection;
	}
	public String getDescription() {
		return this.description;
	}

	public ObjRef getRelatedObjRef() {
		return relatedObjRef;
	}
	
	public IRelatedElementsProcessor getRelatedElementsProcessor() {
		return this.relatedHintedElementsProcessor;
	}
	
	public Map<String,String> getProperties() {
		return this.properties;
	}
	
	public ObjRef getSourceObjRef() {
		return this.sourceObjRef;
	}
	
	public boolean isDefaultSelection() {
		return defaultSelection;
	}
}
