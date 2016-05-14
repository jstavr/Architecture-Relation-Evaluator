package edu.uci.isr.archstudio4.comp.xarchcs.changesetadt;

import java.util.Map;

import edu.uci.isr.xarchflat.ObjRef;

/*
 * Note: Null references are valid. They mean that a reference for an objref
 * cannot yet be consistently created. Anything that receives a null reference
 * should be ignored.
 */

public interface IChangeSetADT {

	public interface IChangeReference {
	}

	public IChangeReference getElementReference(ObjRef xArchRef, ObjRef objRefAncestors[], boolean excludeDetached);

	public IChangeReference getChangeSegmentReference(ObjRef xArchRef, ObjRef changeSegmentRef);

	public IChangeReference getParentReference(ObjRef xArchRef, IChangeReference elementReference);

	public IChangeReference getAttributeReference(ObjRef xArchRef, IChangeReference parentReference, String attributeName);

	public IChangeReference getElementReference(ObjRef xArchRef, IChangeReference parentReference, String elementName, boolean excludeDetached);

	public IChangeReference getElementReference(ObjRef xArchRef, IChangeReference elementManyReference, ObjRef childRef, boolean excludeDetached);

	public IChangeReference getElementManyReference(ObjRef xArchRef, IChangeReference parentReference, String elementManyName, boolean excludeDetached);

	public IChangeReference getDetachedChildChangeReference(ObjRef xArchRef, IChangeReference parentReference, ObjRef childSegmentRef);

	public ObjRef[] getChangeSegmentRefs(ObjRef xArchRef, ObjRef[] changeSetRefs, IChangeReference changeReference);

	public Map<IChangeReference, ObjRef[]> getChildChangeSegmentRefs(ObjRef xArchRef, IChangeReference parentChangeReference, ObjRef[] parentChangeSegmentRefs);

	public ObjRef[] getChildChangeSegmentRefs(ObjRef xArchRef, IChangeReference parentChangeReference, ObjRef[] parentChangeSegmentRefs,
	        IChangeReference childChangeReference);

	public ObjRef getAttributeSegmentRef(ObjRef xArchRef, ObjRef changeSetRef, IChangeReference attributeReference, ObjRef[] createUsingObjRefAncestors);

	public ObjRef getElementSegmentRef(ObjRef xArchRef, ObjRef changeSetRef, IChangeReference elementReference, ObjRef[] createUsingObjRefAncestors);

	public ObjRef getElementManySegmentRef(ObjRef xArchRef, ObjRef changeSetRef, IChangeReference elementManyReference, ObjRef[] createUsingObjRefAncestors);

	// TODO review those below 

	public void removeChildren(ObjRef xArchRef, ObjRef changeSetRef, IChangeReference elementReference);

	@Deprecated
	public IChangeReference[] getChildReferences(ObjRef xArchRef, ObjRef[] changeSetRefs, IChangeReference parentChangeReference);

	@Deprecated
	public ObjRef getChangeSegmentRef(ObjRef xArchRef, ObjRef changeSetRef, IChangeReference segmentReference);

	// public IChangeReference[] getElementManyReferences(ObjRef xArchRef, ObjRef[] changeSetRefs, IChangeReference elementManyReference);

	public boolean isElementSegmentResolvable(ObjRef xArchRef, ObjRef elementSegmentRef, boolean excludeDetached);

	public String getElementReference(IChangeReference changeReference);

	public String getChangeReferencePath(IChangeReference changeReference);

	public Object[] getDeviation(ObjRef xArchRef, IChangeReference preReference, IChangeReference postReference, ObjRef[] objRefAncestors);

	public void removeAssociatedChanges(ObjRef xArchRef, ObjRef[] changeSetRefs, ObjRef[] objRefAncestors);
}
