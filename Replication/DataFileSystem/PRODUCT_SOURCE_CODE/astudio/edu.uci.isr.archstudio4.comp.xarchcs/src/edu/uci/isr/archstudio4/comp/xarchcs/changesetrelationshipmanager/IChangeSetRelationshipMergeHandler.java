package edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager;

import java.util.Collection;

import edu.uci.isr.xarchflat.ObjRef;

public interface IChangeSetRelationshipMergeHandler{

	public String keyName();

	public MergeKey getMergeKey(ObjRef xArchRef, ObjRef elementSet);

	public ObjRef doMerge(ObjRef xArchRef, Collection<ObjRef> relationshipList);

}
