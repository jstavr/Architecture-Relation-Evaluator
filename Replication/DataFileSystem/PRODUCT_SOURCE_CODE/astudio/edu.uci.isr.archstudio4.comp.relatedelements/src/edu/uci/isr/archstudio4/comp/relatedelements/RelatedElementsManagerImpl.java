package edu.uci.isr.archstudio4.comp.relatedelements;

import java.util.ArrayList;
import java.util.List;

import edu.uci.isr.archstudio4.comp.relatedelements.processors.RelatedHintedElementsProcessor;
import edu.uci.isr.archstudio4.comp.relatedelements.processors.RelatedTypeElementsProcessor;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class RelatedElementsManagerImpl implements IRelatedElementsManager{
	
	XArchFlatInterface xArch;
	
	List<IRelatedElementsProcessor> relatedElementsProcessors;
	
	public RelatedElementsManagerImpl(XArchFlatInterface xArch) {
		this.xArch = xArch;
		relatedElementsProcessors = new ArrayList<IRelatedElementsProcessor>();
		RelatedHintedElementsProcessor relatedHintedElementsProcessor = new RelatedHintedElementsProcessor(xArch);
		relatedElementsProcessors.add(relatedHintedElementsProcessor);
		RelatedTypeElementsProcessor relatedTypeElementsProcessor = new RelatedTypeElementsProcessor(xArch);
		relatedElementsProcessors.add(relatedTypeElementsProcessor);
	}
	
	public List<IRelatedElement> getRelatedElements(ObjRef xArchRef,ObjRef objRef) {		
		
		List<IRelatedElement> relatedElements = new ArrayList<IRelatedElement>();
		for(IRelatedElementsProcessor relatedElementsProcessor : relatedElementsProcessors) {
			List<IRelatedElement> currentElements = relatedElementsProcessor.getRelatedElements(objRef);
			if(currentElements != null && currentElements.size() > 0)
			relatedElements.addAll(currentElements);
		}
		return relatedElements;
	}
}
