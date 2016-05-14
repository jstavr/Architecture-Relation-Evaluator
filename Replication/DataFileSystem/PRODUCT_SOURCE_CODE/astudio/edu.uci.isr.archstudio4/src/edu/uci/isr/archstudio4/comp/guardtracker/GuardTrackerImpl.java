package edu.uci.isr.archstudio4.comp.guardtracker;

import java.util.*;

import edu.uci.isr.archstudio4.comp.booleannotation.IBooleanNotation;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class GuardTrackerImpl implements IGuardTracker{
	protected XArchFlatInterface xarch = null;
	protected IBooleanNotation bni = null;
	
	public GuardTrackerImpl(){
	}
	
	public IBooleanNotation getBooleanNotation(){
		return bni;
	}

	public void setBooleanNotation(IBooleanNotation bni){
		this.bni = bni;
	}

	public XArchFlatInterface getXArchADT(){
		return xarch;
	}

	public void setXArchADT(XArchFlatInterface xarch){
		this.xarch = xarch;
	}

	public String[] getAllGuards(ObjRef xArchRef){
		Set<String> allGuardStrings = new HashSet<String>();
		GuardedDocument doc = getGuardedDocument(xArchRef);
		if(doc != null){
			GuardedNode[] guardedNodes = doc.getAllGuardedNodes();
			for(int i = 0; i < guardedNodes.length; i++){
				String[] guardStrings = guardedNodes[i].getAllGuardStrings();
				for(int j = 0; j < guardStrings.length; j++){
					if(guardStrings[j] != null) allGuardStrings.add(guardStrings[j]);
				}
			}
		}
		String[] guardStrings = (String[])allGuardStrings.toArray(new String[0]);
		Arrays.sort(guardStrings);
		return guardStrings;
	}
	
	public GuardedDocument getGuardedDocument(ObjRef xArchRef){
		return parseDocument(xArchRef);
	}
	
	protected GuardedDocument parseDocument(ObjRef xArchRef){
		GuardedDocument guardedDocument = new GuardedDocument(xArchRef);
		
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef[] archStructureRefs = xarch.getAllElements(typesContextRef, "ArchStructure", xArchRef);
		ObjRef[] archTypesRefs = xarch.getAllElements(typesContextRef, "ArchTypes", xArchRef);
		
		for(int i = 0; i < archStructureRefs.length; i++){
			ObjRef archStructureRef = archStructureRefs[i];
			//Optional Components
			ObjRef[] componentRefs = xarch.getAll(archStructureRef, "component");
			for(int j = 0; j < componentRefs.length; j++){
				ObjRef componentRef = componentRefs[j];
				if(xarch.isInstanceOf(componentRef, "options#OptionalComponent")){
					ObjRef optionalRef = (ObjRef)xarch.get(componentRef, "optional");
					if(optionalRef != null){
						ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
						if(guardRef != null){
							GuardedNode guardedNode = parseOptional(componentRef);
							if(guardedNode != null){
								guardedDocument.putGuardedNode(componentRef, guardedNode);
							}
						}
					}
				}
				
				//Optional interfaces on components
				ObjRef[] interfaceRefs = (ObjRef[])xarch.getAll(componentRef, "interface");
				for(int k = 0; k < interfaceRefs.length; k++){
					ObjRef interfaceRef = interfaceRefs[k];
					if(xarch.isInstanceOf(interfaceRef, "options#OptionalInterface")){
						ObjRef optionalRef2 = (ObjRef)xarch.get(interfaceRef, "optional");
						if(optionalRef2 != null){
							ObjRef guardRef = (ObjRef)xarch.get(optionalRef2, "guard");
							if(guardRef != null){
								GuardedNode guardedNode = parseOptional(interfaceRef);
								if(guardedNode != null){
									guardedDocument.putGuardedNode(interfaceRef, guardedNode);
								}
							}
						}
					}
				}
			}
			//Optional Connectors
			ObjRef[] connectorRefs = xarch.getAll(archStructureRef, "connector");
			for(int j = 0; j < connectorRefs.length; j++){
				ObjRef connectorRef = connectorRefs[j];
				if(xarch.isInstanceOf(connectorRef, "options#OptionalConnector")){
					ObjRef optionalRef = (ObjRef)xarch.get(connectorRef, "optional");
					if(optionalRef != null){
						ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
						if(guardRef != null){
							GuardedNode guardedNode = parseOptional(connectorRef);
							if(guardedNode != null){
								guardedDocument.putGuardedNode(connectorRef, guardedNode);
							}
						}
					}
				}
				//Optional interfaces on connectors
				ObjRef[] interfaceRefs = (ObjRef[])xarch.getAll(connectorRef, "interface");
				for(int k = 0; k < interfaceRefs.length; k++){
					ObjRef interfaceRef = interfaceRefs[k];
					if(xarch.isInstanceOf(interfaceRef, "options#OptionalInterface")){
						ObjRef optionalRef2 = (ObjRef)xarch.get(interfaceRef, "optional");
						if(optionalRef2 != null){
							ObjRef guardRef = (ObjRef)xarch.get(optionalRef2, "guard");
							if(guardRef != null){
								GuardedNode guardedNode = parseOptional(interfaceRef);
								if(guardedNode != null){
									guardedDocument.putGuardedNode(interfaceRef, guardedNode);
								}
							}
						}
					}
				}
			}
			
			//Optional Links
			ObjRef[] linkRefs = xarch.getAll(archStructureRef, "link");
			for(int j = 0; j < linkRefs.length; j++){
				ObjRef linkRef = linkRefs[j];
				if(xarch.isInstanceOf(linkRef, "options#OptionalLink")){
					ObjRef optionalRef = (ObjRef)xarch.get(linkRef, "optional");
					if(optionalRef != null){
						ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
						if(guardRef != null){
							GuardedNode guardedNode = parseOptional(linkRef);
							if(guardedNode != null){
								guardedDocument.putGuardedNode(linkRef, guardedNode);
							}
						}
					}
				}
			}
		}
		
		for(int i = 0; i < archTypesRefs.length; i++){
			ObjRef archTypesRef = archTypesRefs[i];
			//Variant component types
			ObjRef[] componentTypeRefs = xarch.getAll(archTypesRef, "componentType");
			for(int j = 0; j < componentTypeRefs.length; j++){
				ObjRef componentTypeRef = componentTypeRefs[j];
				if(xarch.isInstanceOf(componentTypeRef, "variants#VariantComponentType")){
					ObjRef[] variantRefs = xarch.getAll(componentTypeRef, "variant");
					if(variantRefs.length > 0){
						GuardedNode guardedNode = parseVariant(componentTypeRef);
						if(guardedNode != null){
							guardedDocument.putGuardedNode(componentTypeRef, guardedNode);
						}
					}
				}
				//Optional signatures on component types
				ObjRef[] signatureRefs = (ObjRef[])xarch.getAll(componentTypeRef, "signature");
				for(int k = 0; k < signatureRefs.length; k++){
					ObjRef signatureRef = signatureRefs[k];
					if(xarch.isInstanceOf(signatureRef, "options#OptionalSignature")){
						ObjRef optionalRef2 = (ObjRef)xarch.get(signatureRef, "optional");
						if(optionalRef2 != null){
							ObjRef guardRef = (ObjRef)xarch.get(optionalRef2, "guard");
							if(guardRef != null){
								GuardedNode guardedNode = parseOptional(signatureRef);
								if(guardedNode != null){
									guardedDocument.putGuardedNode(signatureRef, guardedNode);
								}
							}
						}
					}
				}
			}
			
			//Variant connector types
			ObjRef[] connectorTypeRefs = xarch.getAll(archTypesRef, "connectorType");
			for(int j = 0; j < connectorTypeRefs.length; j++){
				ObjRef connectorTypeRef = connectorTypeRefs[j];
				if(xarch.isInstanceOf(connectorTypeRef, "variants#VariantConnectorType")){
					ObjRef[] variantRefs = xarch.getAll(connectorTypeRef, "variant");
					if(variantRefs.length > 0){
						GuardedNode guardedNode = parseVariant(connectorTypeRef);
						if(guardedNode != null){
							guardedDocument.putGuardedNode(connectorTypeRef, guardedNode);
						}
					}
				}
				//Optional signatures on connector types
				ObjRef[] signatureRefs = (ObjRef[])xarch.getAll(connectorTypeRef, "signature");
				for(int k = 0; k < signatureRefs.length; k++){
					ObjRef signatureRef = signatureRefs[k];
					if(xarch.isInstanceOf(signatureRef, "options#OptionalSignature")){
						ObjRef optionalRef2 = (ObjRef)xarch.get(signatureRef, "optional");
						if(optionalRef2 != null){
							ObjRef guardRef = (ObjRef)xarch.get(optionalRef2, "guard");
							if(guardRef != null){
								GuardedNode guardedNode = parseOptional(signatureRef);
								if(guardedNode != null){
									guardedDocument.putGuardedNode(signatureRef, guardedNode);
								}
							}
						}
					}
				}
			}
		}
		
		return guardedDocument;
	}
	
	public GuardedNode parseOptional(ObjRef nodeRef){
		GuardedNode guardedNode = new GuardedNode(nodeRef);
		try{
			ObjRef optionalRef = (ObjRef)xarch.get(nodeRef, "optional");
			if(optionalRef != null){
				ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
				String guardString = bni.booleanGuardToString(optionalRef);
				guardedNode.putGuard(guardRef, guardString);
			}
			return guardedNode;
		}
		catch(Exception e){
			return null;
		}
	}
	
	protected GuardedNode parseVariant(ObjRef nodeRef){
		try{
			ObjRef[] variantRefs = xarch.getAll(nodeRef, "variant");
			if(variantRefs.length == 0){
				return null;
			}
			GuardedNode guardedNode = new GuardedNode(nodeRef);
			for(int i = 0; i < variantRefs.length; i++){
				ObjRef guardRef = (ObjRef)xarch.get(variantRefs[i], "guard");
				String guardString = bni.booleanGuardToString(variantRefs[i]);
				guardedNode.putGuard(guardRef, guardString);
			}
			return guardedNode;
		}
		catch(Exception e){
			return null;
		}
	}
	
	static class GuardedDocument{
		protected ObjRef xArchRef;
		
		//Maps node ObjRefs to GuardedNodes
		protected Map<ObjRef,GuardedNode> guardNodeMap;
		
		public GuardedDocument(ObjRef xArchRef){
			this.xArchRef = xArchRef;
			guardNodeMap = new HashMap<ObjRef,GuardedNode>();
		}
		
		public ObjRef getXArchRef(){
			return xArchRef;
		}
		
		public ObjRef[] getAllNodeRefs(){
			return (ObjRef[])guardNodeMap.keySet().toArray(new ObjRef[0]);
		}
		
		public GuardedNode[] getAllGuardedNodes(){
			return (GuardedNode[])guardNodeMap.values().toArray(new GuardedNode[0]);
		}
		
		public void putGuardedNode(ObjRef nodeRef, GuardedNode guardedNode){
			guardNodeMap.put(nodeRef, guardedNode);
		}
		
		public GuardedNode getGuardedNode(ObjRef nodeRef){
			return (GuardedNode)guardNodeMap.get(nodeRef);
		}
		
		public void removeGuardedNode(ObjRef nodeRef){
			guardNodeMap.remove(nodeRef);
		}
	}
	
	static class GuardedNode{
		protected ObjRef nodeRef;
		
		//Maps guard ObjRefs to guard Strings;
		protected Map<ObjRef,String> guardMap;

		public GuardedNode(ObjRef nodeRef){
			this.nodeRef = nodeRef;
			guardMap = new HashMap<ObjRef,String>();
		}
		
		public ObjRef getNodeRef(){
			return nodeRef;
		}
		
		public void putGuard(ObjRef guardRef, String guardString){
			guardMap.put(guardRef, guardString);
		}
		
		public String getGuardString(ObjRef guardRef){
			return (String)guardMap.get(guardRef);
		}
		
		public void removeGuard(ObjRef guardRef){
			guardMap.remove(guardRef);
		}
		
		public ObjRef[] getAllGuardRefs(){
			return (ObjRef[])guardMap.keySet().toArray(new ObjRef[0]);
		}
		
		public String[] getAllGuardStrings(){
			return (String[])guardMap.values().toArray(new String[0]);
		}
	}
}
