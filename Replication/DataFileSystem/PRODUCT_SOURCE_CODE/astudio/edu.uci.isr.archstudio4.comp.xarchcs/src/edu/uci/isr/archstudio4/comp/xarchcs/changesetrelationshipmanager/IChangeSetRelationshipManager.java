package edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager;

import edu.uci.isr.xarchflat.ObjRef;

/**
 * Interface for ChangeRelationships. Retrieves information about changeset id
 * relationships.
 * 
 * @author Kari
 */
public interface IChangeSetRelationshipManager{

	public void addRelationship(ObjRef XArchRef, ObjRef changeSetRelationship);

	public void removeRelationship(ObjRef XArchRef, ObjRef changeSetRelationship);

	public void waitForCompletion();
}
