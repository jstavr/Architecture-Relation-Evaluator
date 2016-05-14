package edu.uci.isr.archstudio4.comp.relatedelements.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElement;
import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElementsProcessor;
import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class RelatedTypeElementsProcessor implements IRelatedElementsProcessor{

	XArchFlatInterface xArch;

	public RelatedTypeElementsProcessor(XArchFlatInterface xArch) {
		this.xArch = xArch;
	}

	public List<IRelatedElement> getRelatedElements(ObjRef objRef) {
		IXArchTypeMetadata typeMetadata = xArch.getTypeMetadata(objRef);
		IXArchPropertyMetadata propertyMetadata = typeMetadata.getProperty("Type");
		if(propertyMetadata != null) {
			String type = propertyMetadata.getType();
			if(type != null && type.equals("instance#XMLLink")) {
				ObjRef typeRef = (ObjRef)xArch.get(objRef,"Type");
				if(typeRef != null) {				
					String href = (String)xArch.get(typeRef,"href");
					if(href != null && !"".equals(href.trim())) {
						String id = href.replaceFirst("#", "");
						ObjRef xArchRef = xArch.getXArch(objRef);
						ObjRef objRefType = xArch.getByID(xArchRef,id);
						if(objRefType != null) {
							String description = (String)xArch.get((ObjRef)xArch.get(objRefType,"Description"),"value");
							IRelatedElement relatedElement = new RelatedTypeElement(objRef,objRefType,description,this,null,false);
							List<IRelatedElement> relatedElements = new ArrayList<IRelatedElement>();
							relatedElements.add(relatedElement);
							return relatedElements;
						}
					}
				}
			}
		}
		return null;
	}

	public ObjRef getTargetReferenceRef(ObjRef targetXArchRef,ObjRef pasteRef,
			IRelatedElement relatedElement,Map<String,ObjRef> oldXArchIDToNewObjRefMap,String newIdSuffix) {

		ObjRef contextRef = xArch.createContext(targetXArchRef, "types");
		ObjRef typesElement = xArch.getElement(contextRef, "ArchTypes",targetXArchRef);
		if(typesElement == null) {
			typesElement = xArch.createElement(contextRef, "ArchTypes");
			xArch.add(targetXArchRef,"object",typesElement);
		}		
		return typesElement;
	}
}
