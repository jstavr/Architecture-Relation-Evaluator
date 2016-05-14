package edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager;

import java.util.HashSet;
import java.util.Set;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager.MergeKey.RelationshipType;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager.RelationshipElement.RelationshipElementType;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class LeftMergeHandler
	extends AbstractChangeSetRelationshipMergeHandler{

	Boolean debug = false;

	final String KEYNAME = "LeftMerge";

	/**
	 * Constructor
	 */
	public LeftMergeHandler(XArchFlatInterface xarch){
		super(xarch);
	}

	public String keyName(){
		return KEYNAME;
	}

	// Creates a merge key for a LHS merge.
	// If this signature matches, the LHS can be merged
	// Can only be an OR relationship.
	// The resulting signature contains either all IMPLIES/not-IMPLIES/AND/not-AND elements
	@Override
	public MergeKey getMergeKey(ObjRef xArchRef, ObjRef relationship){
		// collect all elements that are or, or_not, and, and_not and create a set of relationship element types
		Set<RelationshipElement> signature;

		// make sure the have an OR relationship
		RelationshipType relType = getRelationshipType(relationship);
		if(relType != RelationshipType.OR_TYPE){
			return null;
		}

		signature = new HashSet<RelationshipElement>();

		ObjRef[] impliesChangeSets = xarch.getAll(relationship, "impliesChangeSet");
		for(ObjRef impliesChangeSet: impliesChangeSets){
			signature.add(buildRelationshipElement(RelationshipElementType.IMPLIES, impliesChangeSet));
		}

		ObjRef[] impliesNotChangeSets = xarch.getAll(relationship, "impliesNotChangeSet");
		for(ObjRef impliesNotChangeSet: impliesNotChangeSets){
			signature.add(buildRelationshipElement(RelationshipElementType.IMPLIES_NOT, impliesNotChangeSet));
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
