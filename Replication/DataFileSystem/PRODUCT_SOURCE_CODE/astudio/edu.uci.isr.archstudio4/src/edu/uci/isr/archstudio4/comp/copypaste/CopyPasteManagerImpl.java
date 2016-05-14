package edu.uci.isr.archstudio4.comp.copypaste;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElement;
import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElementsManager;
import edu.uci.isr.archstudio4.comp.xarchcs.eventmanager.IEventManager;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchMetadataUtils;

public class CopyPasteManagerImpl implements ICopyPasteManager{

	class CopiedContext{

		final IXArchTypeMetadata parentType;
		final boolean isElement;
		final String propertyName;

		public CopiedContext(ObjRef objRef){
			ObjRef parentRef = xarch.getParent(objRef);
			if(parentRef == null){
				throw new IllegalArgumentException("Copied objRefs must have a parent");
			}
			this.parentType = xarch.getTypeMetadata(parentRef);
			this.isElement = xarch.getXArch(objRef).equals(parentRef);
			this.propertyName = isElement ? "object" : xarch.getElementName(objRef);
		}
	}

	private static final String CLIPBOARD_URI = "urn://clipboard";

	final protected XArchFlatInterface xarch;



	final protected IRelatedElementsManager relatedElementsManager;
	
	final protected IEventManager eventManager;
	
	static ObjRef copiedXArchRef = null;

	static ObjRef xArchRef = null;

	static Map<ObjRef, CopiedContext> copiedObjRefs = null;

	static Map<ObjRef,CopiedContext> copiedRelatedElements = null;

	static Map<String, ObjRef> copiedIdObjRefs = null;

	static Map<String, Collection<ObjRef>> copiedLinkObjRefs = null;

	static Set<IRelatedElement> relatedElementsList = null;

	static Map<IRelatedElement,ObjRef> relatedElementToDuplicatedRelatedObjRefMap = null;

	static List<ICopiedElementNode> clipBoardNodes = null;

	static List<ObjRef> clipboardObjRefs = null;

	static String copyContextXArchID = null;	

	static Object copiedLock = new Object();

	public CopyPasteManagerImpl(XArchFlatInterface xarch, IRelatedElementsManager relatedElementsManager,IEventManager eventManager){
		this.xarch = xarch;
		this.relatedElementsManager = relatedElementsManager;
		this.eventManager = eventManager;
	}

	public void copy(ObjRef[] objRefs,ObjRef diagramRef){
		synchronized(copiedLock){
			if(xarch.getOpenXArch(CLIPBOARD_URI) != null){
				xarch.close(CLIPBOARD_URI);
			}

			xArchRef = xarch.getXArch(diagramRef);
			copiedXArchRef = xarch.createXArch(CLIPBOARD_URI);
			copiedObjRefs = new HashMap<ObjRef, CopiedContext>();
			copiedIdObjRefs = new HashMap<String, ObjRef>();
			copiedLinkObjRefs = new HashMap<String, Collection<ObjRef>>();
			List<ICopiedElementNode> nodes = new ArrayList<ICopiedElementNode>();
			clipboardObjRefs = new ArrayList<ObjRef>();
			relatedElementsList = new HashSet<IRelatedElement>();
			copiedRelatedElements = new HashMap<ObjRef,CopiedContext>();
			relatedElementToDuplicatedRelatedObjRefMap = new HashMap<IRelatedElement,ObjRef>();
			try {
				copyContextXArchID = (String)xarch.get(diagramRef,"id");
			}
			catch(Exception e){}
			for(ObjRef objRef: objRefs){
				ICopiedElementNode node = new ObjRefNode(xarch,null,objRef);
				CopiedContext copiedContext = new CopiedContext(objRef);
				ObjRef duplicateObjRef = duplicateAndScanElement(copiedXArchRef, objRef, copiedContext.isElement,copiedIdObjRefs, copiedLinkObjRefs,node);
				clipboardObjRefs.add(duplicateObjRef);
				copiedObjRefs.put(duplicateObjRef, copiedContext);
				nodes.add(node);
			}

			String sourceURI = xarch.getXArchURI(xArchRef);
			String targetURI = xarch.getXArchURI(copiedXArchRef);
			for(Map.Entry<String, Collection<ObjRef>> entry: copiedLinkObjRefs.entrySet()){
				String href = entry.getKey();
				if(href != null){
					int pIndex = href.indexOf('#');
					if(pIndex >= 0) {
						String newHref = null;
						String uri = href.substring(0, pIndex);
						String id = href.substring(pIndex + 1);
						if(uri.equals("") || uri.equals(sourceURI)){
							if(copiedIdObjRefs.containsKey(id)){
								newHref = targetURI + "#" + id;
							}
							else{
								newHref = sourceURI + "#" + id;
							}
						}
						if(newHref != null){
							for(ObjRef objRef: entry.getValue()){
								if("simple".equals(xarch.get(objRef, "type"))){
									xarch.set(objRef, "href", newHref);
								}
							}
						}
					}
				}
			}
			clipBoardNodes = nodes;
		}

	}

	public boolean canPaste(ObjRef parentRef){
		synchronized(copiedLock){
			if(parentRef != null && copiedXArchRef != null && xarch.isValidObjRef(copiedXArchRef)){
				IXArchTypeMetadata parentType = xarch.getTypeMetadata(parentRef);

				for(Map.Entry<ObjRef, CopiedContext> entry: copiedObjRefs.entrySet()){
					ObjRef copiedRef = entry.getKey();
					CopiedContext copiedContext = entry.getValue();
					IXArchPropertyMetadata prop = parentType.getProperty(copiedContext.propertyName);
					if(prop != null && xarch.isAssignable(prop.getType(), xarch.getTypeMetadata(copiedRef).getType())){
						return true;
					}
				}
			}
		}
		return false;
	}

	public ICopiedElementNode[] getClipboardNodes() {
		if(clipBoardNodes != null) {
			return clipBoardNodes.toArray(new ICopiedElementNode[clipBoardNodes.size()]);
		}
		return new ICopiedElementNode[0];
	}
	
	public void paste(ObjRef parentRef,ICopiedElementNode[] nodes){
		synchronized(copiedLock){
			eventManager.closeValve();
			Map<String,ObjRef> oldXArchIDToNewObjRefMap = new HashMap<String,ObjRef>();
			oldXArchIDToNewObjRefMap.putAll(copiedIdObjRefs);
			oldXArchIDToNewObjRefMap.put(copyContextXArchID,null);

			ObjRef pasteXArchRef = xarch.getXArch(parentRef);
			IXArchTypeMetadata parentType = xarch.getTypeMetadata(parentRef);
			boolean isElement = xarch.getXArch(parentRef).equals(parentRef);
			String newIdSuffix = "-" + UIDGenerator.generateUID();

			for(Map.Entry<ObjRef, CopiedContext> entry: copiedObjRefs.entrySet()){
				ObjRef copiedRef = entry.getKey();
				CopiedContext copiedContext = entry.getValue();
				IXArchPropertyMetadata prop = parentType.getProperty(copiedContext.propertyName);
				ObjRef newlyDuplicatedRef = null;
				if(prop != null && xarch.isAssignable(prop.getType(), xarch.getTypeMetadata(copiedRef).getType())){
					switch(prop.getMetadataType()){
					case IXArchPropertyMetadata.ATTRIBUTE:
						break;

					case IXArchPropertyMetadata.ELEMENT:
						newlyDuplicatedRef = duplicateAndAdjustElement(pasteXArchRef, copiedRef, isElement, copiedIdObjRefs, copiedLinkObjRefs, newIdSuffix);
						xarch.set(parentRef, prop.getName(), newlyDuplicatedRef);
						break;

					case IXArchPropertyMetadata.ELEMENT_MANY:
						newlyDuplicatedRef = duplicateAndAdjustElement(pasteXArchRef, copiedRef, isElement, copiedIdObjRefs, copiedLinkObjRefs, newIdSuffix);
						xarch.add(parentRef, prop.getName(), newlyDuplicatedRef);
						break;
					}
				}
			}


			///////////////////////////////////////////////////////////////////////////////////
			if(nodes != null && nodes.length > 0) {
				for(ICopiedElementNode node : nodes){

					pasteRelatedElements(parentRef,node,oldXArchIDToNewObjRefMap,newIdSuffix);
				}
			}
			eventManager.openValve();
		}
	}

	private void pasteRelatedElements(ObjRef parentRef,ICopiedElementNode node,Map<String,ObjRef> oldXArchIDToNewObjRefMap,String newIdSuffix) {
		ObjRef pasteXArchRef = xarch.getXArch(parentRef);
		if(node.isSelected()) {
			if(node instanceof RelatedElementNode) {
				RelatedElementNode relatedElementTreeNode = (RelatedElementNode)node;
				IRelatedElement relatedElement = relatedElementTreeNode.getRelatedElement();
				ObjRef newParentRef = relatedElement.getRelatedElementsProcessor().getTargetReferenceRef(pasteXArchRef,parentRef,relatedElement,oldXArchIDToNewObjRefMap,newIdSuffix);
				IXArchTypeMetadata newParentType = xarch.getTypeMetadata(newParentRef);
				boolean isElement = xarch.getXArch(newParentRef).equals(newParentRef);

				ObjRef copiedRef = relatedElementToDuplicatedRelatedObjRefMap.get(relatedElement);
				CopiedContext copiedContext = copiedRelatedElements.get(copiedRef);
				IXArchPropertyMetadata prop = newParentType.getProperty(copiedContext.propertyName);
				ObjRef newlyDuplicatedRef = null;

				if(prop != null && xarch.isAssignable(prop.getType(), xarch.getTypeMetadata(copiedRef).getType())){
					switch(prop.getMetadataType()){
					case IXArchPropertyMetadata.ATTRIBUTE:
						break;

					case IXArchPropertyMetadata.ELEMENT:
						newlyDuplicatedRef = duplicateAndAdjustElement(pasteXArchRef, copiedRef, isElement, copiedIdObjRefs, copiedLinkObjRefs, newIdSuffix);
						xarch.set(newParentRef, prop.getName(), newlyDuplicatedRef);
						break;

					case IXArchPropertyMetadata.ELEMENT_MANY:
						newlyDuplicatedRef = duplicateAndAdjustElement(pasteXArchRef, copiedRef, isElement, copiedIdObjRefs, copiedLinkObjRefs, newIdSuffix);
						xarch.add(newParentRef, prop.getName(), newlyDuplicatedRef);
						break;
					}
				}
			}
			List<ICopiedElementNode> children = node.getChildren();
			if(children != null && children.size() > 0) {
				for(ICopiedElementNode child : children) {
					pasteRelatedElements(parentRef, child, oldXArchIDToNewObjRefMap, newIdSuffix);
				}
			}
		}
	}

	private ObjRef duplicateAndScanElement(ObjRef clipboardXArchRef, ObjRef sourceRef, boolean isElement,Map<String, ObjRef> idObjRefs, Map<String, Collection<ObjRef>> linkObjRefs,ICopiedElementNode node){

		IXArchTypeMetadata type = xarch.getTypeMetadata(sourceRef);
		ObjRef contextRef = xarch.createContext(clipboardXArchRef, XArchMetadataUtils.getTypeContext(type.getType()));
		ObjRef copiedRef = isElement ? xarch.createElement(contextRef, XArchMetadataUtils.getTypeName(type.getType())) : xarch.create(contextRef, XArchMetadataUtils.getTypeName(type.getType()));
		List<IRelatedElement> relatedElements = relatedElementsManager.getRelatedElements(xarch.getXArch(sourceRef), sourceRef);
		if(relatedElements != null && relatedElements.size() > 0) {
			for(IRelatedElement relatedElement : relatedElements) {
				if(relatedElementsList.add(relatedElement)) {
					RelatedElementNode relatedElementTreeNode = new RelatedElementNode(node,relatedElement);
					node.addChild(relatedElementTreeNode);
					ObjRef relatedObjRef = relatedElement.getRelatedObjRef();
					CopiedContext newCopiedContext = new CopiedContext(relatedObjRef);
					ObjRef newDuplicateObjRef = duplicateAndScanElement(copiedXArchRef, relatedObjRef, newCopiedContext.isElement,copiedIdObjRefs, copiedLinkObjRefs,relatedElementTreeNode);
					relatedElementToDuplicatedRelatedObjRefMap.put(relatedElement,newDuplicateObjRef);
					clipboardObjRefs.add(newDuplicateObjRef);
					copiedRelatedElements.put(newDuplicateObjRef, newCopiedContext);
				}
			}
		}


		for(IXArchPropertyMetadata prop: type.getProperties()){
			switch(prop.getMetadataType()){

			case IXArchPropertyMetadata.ATTRIBUTE:
				String value = (String)xarch.get(sourceRef, prop.getName());
				if(value != null){
					if("id".equals(prop.getName())){
						idObjRefs.put(value, copiedRef);
					}
					else if("href".equals(prop.getName()) && xarch.isInstanceOf(copiedRef, "instance#XMLLink")){
						Collection<ObjRef> links = linkObjRefs.get(value);
						if(links == null){
							linkObjRefs.put(value, links = new HashSet<ObjRef>());
						}
						links.add(copiedRef);
					}

					xarch.set(copiedRef, prop.getName(), value);
				}
				break;

			case IXArchPropertyMetadata.ELEMENT:
				ObjRef elementRef = (ObjRef)xarch.get(sourceRef, prop.getName());
				if(elementRef != null){
					ObjRefNode objRefTreeNode = null;
					if(node != null && xarch.getTypeMetadata(elementRef).getProperty("Id") != null) {						
						objRefTreeNode = new ObjRefNode(xarch,node,elementRef);
						node.addChild(objRefTreeNode);

					}
					ObjRef duplicatedElement = duplicateAndScanElement(clipboardXArchRef, elementRef, false,idObjRefs, linkObjRefs,objRefTreeNode);
					clipboardObjRefs.add(duplicatedElement);
					xarch.set(copiedRef, prop.getName(), duplicatedElement);
				}
				break;

			case IXArchPropertyMetadata.ELEMENT_MANY:
				for(ObjRef elementManyRef: xarch.getAll(sourceRef, prop.getName())){
					ObjRefNode objRefTreeNode = null;
					if(node != null && xarch.getTypeMetadata(elementManyRef).getProperty("Id") != null) {
						objRefTreeNode = new ObjRefNode(xarch,node,elementManyRef);
						node.addChild(objRefTreeNode);			
					}
					ObjRef duplicatedElement = duplicateAndScanElement(clipboardXArchRef, elementManyRef, false,idObjRefs, linkObjRefs,objRefTreeNode);
					clipboardObjRefs.add(duplicatedElement);
					xarch.add(copiedRef, prop.getName(),duplicatedElement);
				}
				break;
			}
		}
		return copiedRef;
	}

	private ObjRef duplicateAndAdjustElement(ObjRef targetXArchRef, ObjRef sourceRef, boolean isElement, Map<String, ObjRef> idObjRefs, Map<String, Collection<ObjRef>> linkObjRefs, String newIdSuffix){

		IXArchTypeMetadata type = xarch.getTypeMetadata(sourceRef);
		ObjRef contextRef = xarch.createContext(targetXArchRef, XArchMetadataUtils.getTypeContext(type.getType()));
		ObjRef copiedRef = isElement ? xarch.createElement(contextRef, XArchMetadataUtils.getTypeName(type.getType())) : xarch.create(contextRef, XArchMetadataUtils.getTypeName(type.getType()));

		for(IXArchPropertyMetadata prop: type.getProperties()){
			switch(prop.getMetadataType()){

			case IXArchPropertyMetadata.ATTRIBUTE:
				String value = (String)xarch.get(sourceRef, prop.getName());
				if(value != null){
					if("id".equals(prop.getName())){
						value = value + newIdSuffix;
					}
					xarch.set(copiedRef, prop.getName(), value);
				}
				break;

			case IXArchPropertyMetadata.ELEMENT:
				ObjRef elementRef = (ObjRef)xarch.get(sourceRef, prop.getName());
				if(elementRef != null){
					ObjRef duplicatedAndAdjustedElement = duplicateAndAdjustElement(targetXArchRef, elementRef, false, idObjRefs, linkObjRefs, newIdSuffix); 
					xarch.set(copiedRef, prop.getName(), duplicatedAndAdjustedElement);
				}
				break;

			case IXArchPropertyMetadata.ELEMENT_MANY:
				for(ObjRef elementManyRef: xarch.getAll(sourceRef, prop.getName())){
					ObjRef duplicatedAndAdjustedElement = duplicateAndAdjustElement(targetXArchRef, elementManyRef, false, idObjRefs, linkObjRefs, newIdSuffix);					
					xarch.add(copiedRef, prop.getName(), duplicatedAndAdjustedElement);
				}
				break;
			}
		}

		String sourceURI = xarch.getXArchURI(xarch.getXArch(sourceRef));
		String targetURI = xarch.getXArchURI(targetXArchRef);
		if(xarch.isInstanceOf(copiedRef, "instance#XMLLink")){
			if("simple".equals(xarch.get(copiedRef, "type"))){
				String href = (String)xarch.get(copiedRef, "href");
				if(href != null){
					int pIndex = href.indexOf('#');
					if(pIndex >= 0){
						String newHref = null;
						String uri = href.substring(0, pIndex);
						String id = href.substring(pIndex + 1);
						if(uri.equals("") || uri.equals(sourceURI)){
							newHref = "#" + id + newIdSuffix;
						}
						else if(uri.equals(targetURI)){
							newHref = "#" + id;
						}
						if(newHref != null){
							xarch.set(copiedRef, "href", newHref);
						}
					}
				}
			}
		}
		return copiedRef;
	}
}
