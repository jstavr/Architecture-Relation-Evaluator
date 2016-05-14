package edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetutils.ChangeSetPathReference;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

/**
 * This class is used to create and compare rationale elements
 * @author Kari
 */

public class RationaleElement {
		
	ChangeSetPathReference source;
	ChangeSetPathReference requires;

	public RationaleElement(ChangeSetPathReference source, ChangeSetPathReference requires) {
		this.source = source;
		this.requires = requires;
	}
	
	public RationaleElement(XArchFlatInterface xarch, ObjRef rationale){
		ObjRef[] sourceObjRefs = xarch.getAll(rationale, "source");
		ObjRef sourceObjRef = sourceObjRefs[0];
		this.source = buildChangeSetPathReference(xarch, sourceObjRef);
		ObjRef[] requiresObjRefs = xarch.getAll(rationale, "requires");
		ObjRef requiresObjRef = requiresObjRefs[0];
		this.requires = buildChangeSetPathReference(xarch, requiresObjRef);
	}

	private ChangeSetPathReference buildChangeSetPathReference(XArchFlatInterface xarch,
			                                                   ObjRef pathReferenceObj) {
		String xArchPath = (String)xarch.get(pathReferenceObj, "xArchPath");
		ObjRef changeSetLink = (ObjRef)xarch.get(pathReferenceObj, "changeSet");
		String csId = (String)xarch.get(changeSetLink, "href");
		assert (csId != null && csId.charAt(0) == '#');
		csId = csId.substring(1);
        ObjRef changeSetRef = xarch.getByID(csId);
        return new ChangeSetPathReference(changeSetRef, xArchPath);
	}

	public boolean equals(Object o) {
		if (o == null) return false;
		if (! (o instanceof RationaleElement)) return false;
		RationaleElement re = (RationaleElement)o;
		return ((source.equals(re.source)) &&
				(requires.equals(re.requires)));
	}
	
	public int hashCode() {
		return source.hashCode() + (31 * requires.hashCode());
	}
	
}
