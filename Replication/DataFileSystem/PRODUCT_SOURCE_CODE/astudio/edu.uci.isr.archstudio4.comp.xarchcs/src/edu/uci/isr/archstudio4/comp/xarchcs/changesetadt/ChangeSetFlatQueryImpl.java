package edu.uci.isr.archstudio4.comp.xarchcs.changesetadt;

import edu.uci.isr.xarchflat.IXArchInstanceMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;
import edu.uci.isr.xarchflat.XArchPath;

public class ChangeSetFlatQueryImpl
	implements XArchFlatQueryInterface{

	XArchFlatQueryInterface xarch;

	public ChangeSetFlatQueryImpl(XArchFlatQueryInterface xarch){
		this.xarch = xarch;
	}

	public boolean equals(ObjRef ref1, ObjRef ref2){
		throw new UnsupportedOperationException();
	}

	public Object get(ObjRef baseObjectRef, String typeOfThing){
		if(xarch.isInstanceOf(baseObjectRef, "changesets#ElementSegment")){
			String reference = ChangeSetADTImpl.encodeNameReference(ChangeSetADTImpl.capFirstLetter(typeOfThing));
			for(ObjRef changeSegmentRef: xarch.getAll(baseObjectRef, "changeSegment")){
				if(xarch.has(changeSegmentRef, "reference", reference)){
					if(xarch.isInstanceOf(changeSegmentRef, "changesets#AttributeSegment")){
						return xarch.get(changeSegmentRef, "value");
					}
					return changeSegmentRef;
				}
			}
		}
		return null;
	}

	public ObjRef get(ObjRef baseObjectRef, String typeOfThing, String id){
		throw new UnsupportedOperationException();
	}

	public ObjRef[] get(ObjRef baseObjectRef, String typeOfThing, String[] ids){
		throw new UnsupportedOperationException();
	}

	public ObjRef[] getAll(ObjRef baseObjectRef, String typeOfThing){
		throw new UnsupportedOperationException();
	}

	public ObjRef[] getAllAncestors(ObjRef targetObjectRef){
		throw new UnsupportedOperationException();
	}

	public ObjRef[] getAllElements(ObjRef contextObjectRef, String typeOfThing, ObjRef archObjectRef){
		throw new UnsupportedOperationException();
	}

	public ObjRef getByID(ObjRef archRef, String id){
		throw new UnsupportedOperationException();
	}

	public ObjRef getByID(String id){
		throw new UnsupportedOperationException();
	}

	public String[] getContextTypes(){
		throw new UnsupportedOperationException();
	}

	public ObjRef getElement(ObjRef contextObjectRef, String typeOfThing, ObjRef archObjectRef){
		throw new UnsupportedOperationException();
	}

	public String getElementName(ObjRef objRef){
		String reference = (String)xarch.get(objRef, "reference");
		//System.err.println(reference);
		if(!ChangeSetADTImpl.isNameReference(reference)){
			reference = (String)xarch.get(xarch.getParent(objRef), "reference");
		}
		return ChangeSetADTImpl.decodeNameReference(reference);
	}

	public IXArchInstanceMetadata getInstanceMetadata(ObjRef baseObjRef){
		throw new UnsupportedOperationException();
	}

	public ObjRef getParent(ObjRef targetObjectRef){
		ObjRef parentRef = xarch.getParent(targetObjectRef);
		if(xarch.isInstanceOf(parentRef, "changesets#ElementManySegment")){
			parentRef = xarch.getParent(parentRef);
		}
		return parentRef;
	}

	public ObjRef[] getReferences(ObjRef archRef, String id){
		throw new UnsupportedOperationException();
	}

	public String getType(ObjRef baseObjectRef){
		throw new UnsupportedOperationException();
	}

	public IXArchTypeMetadata getTypeMetadata(String type){
		return xarch.getTypeMetadata(type);
	}

	public IXArchTypeMetadata getTypeMetadata(ObjRef baseObjectRef){
		if(xarch.isInstanceOf(baseObjectRef, "changesets#ElementSegment")){
			String type = (String)xarch.get(baseObjectRef, "type");

			/*
			 * Some types were incorrectly recorded as ":XArch" rather than
			 * "#XArch". This corrects the problem.
			 */
			if(type != null){
				type = type.replace(':', '#');
			}

			if(type != null && type.length() > 0){
				return xarch.getTypeMetadata(type);
			}
		}
		return null;
	}

	public ObjRef getXArch(ObjRef baseObjectRef){
		ObjRef[] ancestors = xarch.getAllAncestors(baseObjectRef);
		return ancestors[ancestors.length - 4];
	}

	public XArchPath getXArchPath(ObjRef ref){
		throw new UnsupportedOperationException();
	}

	public boolean has(ObjRef baseObjectRef, String typeOfThing, String valueToCheck){
		throw new UnsupportedOperationException();
	}

	public boolean has(ObjRef baseObjectRef, String typeOfThing, ObjRef valueToCheck){
		throw new UnsupportedOperationException();
	}

	public boolean[] has(ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToCheck){
		throw new UnsupportedOperationException();
	}

	public boolean hasAll(ObjRef baseObjectRef, String typeOfThing, ObjRef[] valuesToCheck){
		throw new UnsupportedOperationException();
	}

	public boolean hasAncestor(ObjRef childRef, ObjRef ancestorRef){
		throw new UnsupportedOperationException();
	}

	public boolean isAssignable(String toType, String fromType){
		return xarch.isAssignable(toType, fromType);
	}

	public boolean isAttached(ObjRef childRef){
		return true;
	}

	public boolean isEqual(ObjRef baseObjectRef, ObjRef thingToCheck){
		throw new UnsupportedOperationException();
	}

	public boolean isEquivalent(ObjRef baseObjectRef, ObjRef thingToCheck){
		throw new UnsupportedOperationException();
	}

	public boolean isInstanceOf(ObjRef baseObjectRef, String type){
		if(xarch.isInstanceOf(baseObjectRef, "changesets#ElementSegment")){
			return isAssignable(type, getTypeMetadata(baseObjectRef).getType());
		}
		return false;
	}

	public ObjRef resolveHref(ObjRef archRef, String href){
		throw new UnsupportedOperationException();
	}

	public ObjRef resolveXArchPath(ObjRef xArchRef, XArchPath xArchPath){
		return XArchPath.resolve(this, xArchRef, xArchPath);
	}

	public ObjRef createContext(ObjRef xArchObject, String contextType){
		return xarch.createContext(xArchObject, contextType);
	}

	public ObjRef getOpenXArch(String uri){
		return xarch.getOpenXArch(uri);
	}

	public ObjRef[] getOpenXArches(){
		return xarch.getOpenXArches();
	}

	public String[] getOpenXArchURIs(){
		return xarch.getOpenXArchURIs();
	}

	public String[] getOpenXArchURLs(){
		return xarch.getOpenXArchURLs();
	}

	public String getXArchURI(ObjRef xArchRef){
		return xarch.getXArchURI(xArchRef);
	}

	public String getXArchURL(ObjRef xArchRef){
		return xarch.getXArchURL(xArchRef);
	}

	public boolean isValidObjRef(ObjRef ref){
		return xarch.isValidObjRef(ref);
	}
}
