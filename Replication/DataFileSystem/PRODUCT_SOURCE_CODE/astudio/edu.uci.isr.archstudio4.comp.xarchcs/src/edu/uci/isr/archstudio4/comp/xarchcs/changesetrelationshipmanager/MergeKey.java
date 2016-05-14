package edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager;

import java.util.Set;

public class MergeKey{

	public enum RelationshipType{
		OR_TYPE, AND_TYPE
	}

	IChangeSetRelationshipMergeHandler mergeHandler;
	Set<RelationshipElement> signature;
	RelationshipType relType;

	public MergeKey(IChangeSetRelationshipMergeHandler mergeHandler, Set<RelationshipElement> signature, RelationshipType relType){
		this.mergeHandler = mergeHandler;
		this.signature = signature;
		this.relType = relType;
	}

	@Override
	public int hashCode(){
		return relType.hashCode() + 31 * signature.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(o instanceof MergeKey){
			MergeKey mk = (MergeKey)o;
			return signature.equals(mk.signature) && relType.equals(mk.relType);
		}
		return false;
	}

	public IChangeSetRelationshipMergeHandler getMergeHandler(){
		return mergeHandler;
	}
}
