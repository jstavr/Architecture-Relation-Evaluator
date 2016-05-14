package edu.uci.isr.archstudio4.comp.xarchcs.changesetutils;

import java.util.LinkedList;
import java.util.List;

import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class OrRelationshipBuilder {
	
	public static final String RELATIONSHIP_PREFIX = "CSRel";
	public static final String RATIONALE_PREFIX = "CSRat";
	
	static Boolean debug = false;
	
	
	
	/**
	 * Builds a change set OR Relationship
	 * 
	 * @param xArchRef
	 * @param orChangeSetPathRefs
	 *            a list of change set path references for or change sets
	 * @param impliesChangeSetPathRef
	 *            a path reference for the the implies change set
	 * @return an instance of an change set or relationship
	 */
	public static ObjRef buildOrRelationship(XArchFlatInterface xarch, ObjRef xArchRef, List<ChangeSetPathReference> orChangeSetPathRefs, ChangeSetPathReference impliesChangeSetPathRef){

		
		if(debug){
			System.out.println("Building Or Relationship");
		}
		ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
		ObjRef orRelationship = xarch.create(changesetsContextRef, "OrRelationship");

		String id = UIDGenerator.generateUID(OrRelationshipBuilder.RELATIONSHIP_PREFIX);
		xarch.set(orRelationship, "id", id);
		xarch.set(orRelationship, "generated", "true");

		// keep a list to check for duplicates in or change sets
		List<ObjRef> orChangeSetRefs = new LinkedList<ObjRef>();
		ObjRef orChangeSet;
		for(ChangeSetPathReference orCSPathRef: orChangeSetPathRefs){
			orChangeSet = orCSPathRef.getChangeSetRef();
			if (!orChangeSetRefs.contains(orChangeSet)) {
				addXMLLink(xarch, changesetsContextRef, orRelationship, "orChangeSet", orChangeSet);
				orChangeSetRefs.add(orChangeSet);
			}
			
		}
		addXMLLink(xarch, changesetsContextRef, orRelationship, "impliesChangeSet", impliesChangeSetPathRef.getChangeSetRef());

		
		// now add rationale to the relationship
		for(ChangeSetPathReference orCSPathRef: orChangeSetPathRefs){
			ObjRef rationale = buildRationale(xarch, changesetsContextRef, impliesChangeSetPathRef, orCSPathRef);
			xarch.add(orRelationship, "rationale", rationale);
		}

		return orRelationship;
		
	}
	
	public static ObjRef buildRationale(XArchFlatInterface xarch, ObjRef changesetsContextRef, ChangeSetPathReference impliesPathRef, ChangeSetPathReference orPathRef) {
		ObjRef rationale = xarch.create(changesetsContextRef, "DependencyRelationshipRationale");
		String ratId = UIDGenerator.generateUID(RATIONALE_PREFIX);
		xarch.set(rationale, "id", ratId);
		
		ObjRef requiresPathReference = buildXADLPathReference(xarch, changesetsContextRef, impliesPathRef);
		xarch.add(rationale, "requires", requiresPathReference);

		ObjRef sourcePathReference = buildXADLPathReference(xarch, changesetsContextRef, orPathRef);
		xarch.add(rationale, "source", sourcePathReference);
		return rationale;
	}

	private static ObjRef buildXADLPathReference(XArchFlatInterface xarch, ObjRef changesetsContextRef, ChangeSetPathReference changesetPathRef) {
		ObjRef pathRef = xarch.create(changesetsContextRef, "PathReference");
		setXMLLink(xarch, changesetsContextRef, pathRef, "changeSet", changesetPathRef.getChangeSetRef());		
		xarch.set(pathRef, "xArchPath", changesetPathRef.getXArchPath());
		
		return pathRef;
	}
	
	private static void setXMLLink (XArchFlatInterface xarch, ObjRef context, ObjRef obj, String linkName, ObjRef linkToObj) {
		ObjRef linkObj = xarch.create(context, "XMLLink");
		xarch.set(linkObj, "type", "simple");
		xarch.set(linkObj, "href", "#" + XadlUtils.getID(xarch, linkToObj));
		xarch.set(obj, linkName, linkObj);
	}
	
	private static void addXMLLink (XArchFlatInterface xarch, ObjRef context, ObjRef obj, String linkName, ObjRef linkToObj) {
		ObjRef linkObj = xarch.create(context, "XMLLink");
		xarch.set(linkObj, "type", "simple");
		xarch.set(linkObj, "href", "#" + XadlUtils.getID(xarch, linkToObj));
		xarch.add(obj, linkName, linkObj);
	}
	
}
