package edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.uci.isr.xarchflat.ObjRef;

/**
 * Maintains a cloneable, many to many mapping of MergeKeys <-> RelationshipRefs
 */
public class MergeKeyAndRelationshipRegistry
	implements Cloneable{

	private Map<MergeKey, Collection<ObjRef>> mergeKeyToRelationshipRefsMap = new HashMap<MergeKey, Collection<ObjRef>>();
	private Map<ObjRef, Collection<MergeKey>> relationshipRefToMergeKeyMap = new HashMap<ObjRef, Collection<MergeKey>>();

	@Override
	protected Object clone(){
		try{
			MergeKeyAndRelationshipRegistry clone = (MergeKeyAndRelationshipRegistry)super.clone();
			clone.mergeKeyToRelationshipRefsMap = new HashMap<MergeKey, Collection<ObjRef>>(mergeKeyToRelationshipRefsMap);
			clone.relationshipRefToMergeKeyMap = new HashMap<ObjRef, Collection<MergeKey>>(relationshipRefToMergeKeyMap);
			return clone;
		}
		catch(CloneNotSupportedException e){
			throw new RuntimeException("This shouldn't happen", e);
		}
	}

	public void add(ObjRef xArchRef, ObjRef relationshipRef, Collection<IChangeSetRelationshipMergeHandler> changeSetRelationshipMergeHandlers){
		for(IChangeSetRelationshipMergeHandler changeSetRelationshipMergeHandler: changeSetRelationshipMergeHandlers){
			MergeKey mergeKey = changeSetRelationshipMergeHandler.getMergeKey(xArchRef, relationshipRef);
			if(mergeKey != null){
				add(mergeKey, relationshipRef);
			}
		}
	}

	private void add(MergeKey mergeKey, ObjRef relationshipRef){
		Collection<ObjRef> relationshipRefs = mergeKeyToRelationshipRefsMap.get(mergeKey);
		if(relationshipRefs == null){
			mergeKeyToRelationshipRefsMap.put(mergeKey, relationshipRefs = new HashSet<ObjRef>());
		}
		relationshipRefs.add(relationshipRef);

		Collection<MergeKey> mergeKeys = relationshipRefToMergeKeyMap.get(relationshipRef);
		if(mergeKeys == null){
			relationshipRefToMergeKeyMap.put(relationshipRef, mergeKeys = new HashSet<MergeKey>());
		}
		mergeKeys.add(mergeKey);
	}

	public void remove(MergeKey mergeKey){
		Collection<ObjRef> relationshipRefs = mergeKeyToRelationshipRefsMap.remove(mergeKey);
		if(relationshipRefs != null){
			for(ObjRef relationshipRef: relationshipRefs){
				Collection<MergeKey> mergeKeys = relationshipRefToMergeKeyMap.get(relationshipRef);
				if(mergeKeys != null){
					mergeKeys.remove(mergeKey);
					if(mergeKeys.size() == 0){
						relationshipRefToMergeKeyMap.remove(relationshipRef);
					}
				}
			}
		}
	}

	public void remove(ObjRef relationshipRef){
		Collection<MergeKey> mergeKeys = relationshipRefToMergeKeyMap.remove(relationshipRef);
		if(mergeKeys != null){
			for(MergeKey mergeKey: mergeKeys){
				Collection<ObjRef> relationshipRefs = mergeKeyToRelationshipRefsMap.get(mergeKey);
				if(relationshipRefs != null){
					relationshipRefs.remove(relationshipRef);
					if(relationshipRefs.size() == 0){
						mergeKeyToRelationshipRefsMap.remove(mergeKey);
					}
				}
			}
		}
	}

	public Set<Map.Entry<MergeKey, Collection<ObjRef>>> getEntries(){
		return Collections.unmodifiableSet(mergeKeyToRelationshipRefsMap.entrySet());
	}

	public void clear(){
		mergeKeyToRelationshipRefsMap.clear();
		relationshipRefToMergeKeyMap.clear();
	}

	public ObjRef[] getRelationships(){
		return relationshipRefToMergeKeyMap.keySet().toArray(new ObjRef[relationshipRefToMergeKeyMap.size()]);
	}
}
