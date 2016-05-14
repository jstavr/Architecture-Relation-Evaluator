package edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager;


public class RelationshipElement {
		
	public enum RelationshipElementType{
		OR, OR_NOT, AND, AND_NOT, IMPLIES, IMPLIES_NOT,
	}
	
	RelationshipElementType relElemType;
	String changeSetId;

	public RelationshipElement(RelationshipElementType relElemType,
			String changeSetId) {
		this.relElemType = relElemType;
		this.changeSetId = changeSetId;
	}

	public boolean equals(Object o) {
		if (o == null) return false;
		if (! (o instanceof RelationshipElement)) return false;
		RelationshipElement re = (RelationshipElement)o;
		return ((changeSetId.equals(re.changeSetId)) &&
				(relElemType.equals(re.relElemType)));
	}
	
	public int hashCode() {
		return changeSetId.hashCode() + (31 * relElemType.hashCode());
	}
}
