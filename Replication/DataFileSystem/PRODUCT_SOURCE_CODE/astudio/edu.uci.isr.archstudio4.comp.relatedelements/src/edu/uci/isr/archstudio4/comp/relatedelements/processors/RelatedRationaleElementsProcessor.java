package edu.uci.isr.archstudio4.comp.relatedelements.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElement;
import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElementsProcessor;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class RelatedRationaleElementsProcessor implements IRelatedElementsProcessor{
	XArchFlatInterface xArch;
	
	public RelatedRationaleElementsProcessor(XArchFlatInterface xArch) {
		this.xArch = xArch;
	}
	
	public List<IRelatedElement> getRelatedElements(ObjRef objRef) {
		
		List<IRelatedElement> relatedElements = new ArrayList<IRelatedElement>();
				
		IXArchTypeMetadata typeMetadata = xArch.getTypeMetadata(objRef);
		if(typeMetadata.getProperty("id") != null) {
		
			String xArchID = (String)xArch.get(objRef,"id");
			ObjRef[] references = xArch.getReferences(xArch.getXArch(objRef), xArchID);
			if(references != null) {
				for(ObjRef reference : references) {
					ObjRef parentRef = xArch.getParent(reference);
					if(xArch.isInstanceOf(parentRef, "Rationale")) {
						RelatedRationaleElement relatedRationaleElement = new RelatedRationaleElement(objRef,parentRef,"Rationale",false);
						relatedElements.add(relatedRationaleElement);
					}
				}
			}
		}
		
		if(relatedElements != null && relatedElements.size() > 0) {
			return relatedElements;
		}
		else {
			return null;
		}
		
	}

	public ObjRef getTargetReferenceRef(ObjRef targetXArchRef,ObjRef pasteRef,IRelatedElement relatedElement,Map<String,ObjRef> oldXArchIDToNewObjRefMap,String newIdSuffix) {
		return null;
	}	
}
