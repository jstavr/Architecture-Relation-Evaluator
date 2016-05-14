package edu.uci.isr.archstudio4.comp.relatedelements.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElement;
import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElementsProcessor;
import edu.uci.isr.xarchflat.IXArchInstanceMetadata;
import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class RelatedHintedElementsProcessor implements IRelatedElementsProcessor{

	private XArchFlatInterface xArch;

	public RelatedHintedElementsProcessor(XArchFlatInterface xArch) {
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
					if(xArch.isInstanceOf(parentRef, "hints3#HintedElement")) {

						Map<String,String> properties = new HashMap<String,String>();
						ObjRef parentContextRef = xArch.getParent(parentRef);
						ObjRef hintedBundleRef = xArch.getParent(parentContextRef);
						if(xArch.isInstanceOf(hintedBundleRef,"hints3#HintBundle")) {
							ObjRef targetLinkRef = (ObjRef)xArch.get(parentContextRef,"Target");
							String href = (String)xArch.get(targetLinkRef,"href");
							if(href != null && !"".equals(href.trim())) {
								href = href.replaceFirst("#", "");
							}
							properties.put("parentContextRefID", href);
							IXArchTypeMetadata hintedBundleRefTypeMetadata = xArch.getTypeMetadata(hintedBundleRef);
							IXArchPropertyMetadata[] metaDataProperties = hintedBundleRefTypeMetadata.getProperties();
							for(IXArchPropertyMetadata metaDataProperty : metaDataProperties) {
								if(metaDataProperty.getMetadataType() == IXArchPropertyMetadata.ATTRIBUTE) {
									String propertyValue = (String)xArch.get(hintedBundleRef,metaDataProperty.getName());
									if(propertyValue != null && !"".equals(propertyValue.trim())) {
										properties.put(metaDataProperty.getName(), propertyValue);
									}
								}
							}						
							RelatedHintedElement relatedHintedElement = new RelatedHintedElement(objRef,parentRef,"Hints",this,properties,true);
							relatedElements.add(relatedHintedElement);
						}
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
		if(relatedElement instanceof RelatedHintedElement) {
			RelatedHintedElement relatedHintedElement = (RelatedHintedElement)relatedElement;
			ObjRef hintsContextRef = xArch.createContext(targetXArchRef, "hints3");
			ObjRef renderingHints = xArch.getElement(hintsContextRef, "RenderingHints3", targetXArchRef);
			ObjRef[] hintBundleRefs = xArch.getAll(renderingHints,"HintedElement");
			ObjRef requiredHintBundleRef = null;
			Map<String,String> properties = relatedHintedElement.getProperties();
			String oldXArchID = properties.get("parentContextRefID");
			ObjRef newObjRef = oldXArchIDToNewObjRefMap.get(oldXArchID);
			String newXArchID = null;
			if(newObjRef != null) {
				newXArchID = (String)xArch.get(newObjRef, "Id") + newIdSuffix;
			}
			else {
				newXArchID = (String)xArch.get(pasteRef,"id");
			}
			boolean found = false;
			if(hintBundleRefs != null && hintBundleRefs.length > 0) {
				for(ObjRef hintBundleRef : hintBundleRefs) {
					boolean sameValues = true;
					for(String propertyName : properties.keySet()) {
						try{
							String propertyValue = (String)xArch.get(hintBundleRef, propertyName);
							if(!properties.get(propertyName).equals(propertyValue)) {
								sameValues = false;
								break;
							}
						}catch(Exception e){}
					}
					if(sameValues) {
						found = true;
						requiredHintBundleRef = hintBundleRef;
						break;
					}
				}
			}
			if(found) {
				ObjRef[] refs = xArch.getAll(requiredHintBundleRef,"HintedElement");
				if(refs != null && refs.length > 0) {
					for(ObjRef parentHintedElementRef : refs) {
						ObjRef targetRef = (ObjRef)xArch.get(parentHintedElementRef,"target");
						if(targetRef != null) {
							String href = (String)xArch.get(targetRef,"href");
							if(href != null && !"".equals(href.trim())) {
								href = href.replaceFirst("#", "");
								if(href.equals(newXArchID)) {
									return parentHintedElementRef;
								}
							}
						}
					}
				}

			}
			else {
				requiredHintBundleRef = xArch.create(hintsContextRef, "HintBundle");
				for(String propertyName : properties.keySet()) {
					xArch.set(requiredHintBundleRef,propertyName,properties.get(propertyName));
				}
				xArch.set(renderingHints,"HintedElement",requiredHintBundleRef);
			}

			ObjRef parentHintedElementRef = xArch.create(hintsContextRef,"HintedElement");
			ObjRef targetRef = xArch.create(hintsContextRef,"XMLLink");
			xArch.set(targetRef,"href","#"+newXArchID);
			xArch.set(parentHintedElementRef,"Target",targetRef);
			xArch.add(requiredHintBundleRef, "HintedElement", parentHintedElementRef);
			return parentHintedElementRef;
		}
		return null;
	}
}
