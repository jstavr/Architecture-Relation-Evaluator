package edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager;

import java.util.HashSet;
import java.util.Set;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager.MergeKey.RelationshipType;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager.RelationshipElement.RelationshipElementType;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class RightMergeHandler
	extends AbstractChangeSetRelationshipMergeHandler{

	Boolean debug = false;

	final String KEYNAME = "RightMerge";

	/**
	 * Constructor
	 */
	public RightMergeHandler(XArchFlatInterface xarch){
		super(xarch);
	}

	public String keyName(){
		return KEYNAME;
	}

	// Creates a merge key for a RHS merge.
	// If this signature matches, the RHS can be merged
	// Can be OR|AND relationship, signature must also match on relationship type
	// The resulting signature contains either all OR/not-OR/AND/not-AND elements
	@Override
	public MergeKey getMergeKey(ObjRef xArchRef, ObjRef relationship){
		// collect all elements that are or, or_not, and, and_not and create a set of relationship element types
		Set<RelationshipElement> signature;

		RelationshipType relType = getRelationshipType(relationship);
		if(relType == null){
			return null;
		}

		signature = new HashSet<RelationshipElement>();

		ObjRef[] orChangeSets = xarch.getAll(relationship, "orChangeSet");
		for(ObjRef orChangeSet: orChangeSets){
			signature.add(buildRelationshipElement(RelationshipElementType.OR, orChangeSet));
		}

		ObjRef[] orNotChangeSets = xarch.getAll(relationship, "orNotChangeSet");
		for(ObjRef orNotChangeSet: orNotChangeSets){
			signature.add(buildRelationshipElement(RelationshipElementType.OR_NOT, orNotChangeSet));
		}

		ObjRef[] andChangeSets = xarch.getAll(relationship, "andChangeSet");
		for(ObjRef andChangeSet: andChangeSets){
			signature.add(buildRelationshipElement(RelationshipElementType.AND, andChangeSet));
		}

		ObjRef[] andNotChangeSets = xarch.getAll(relationship, "andNotChangeSet");
		for(ObjRef andNotChangeSet: andNotChangeSets){
			signature.add(buildRelationshipElement(RelationshipElementType.AND_NOT, andNotChangeSet));
		}

		if(signature.isEmpty()){
			return null;
		}

		return new MergeKey(this, signature, relType);
	}

}
