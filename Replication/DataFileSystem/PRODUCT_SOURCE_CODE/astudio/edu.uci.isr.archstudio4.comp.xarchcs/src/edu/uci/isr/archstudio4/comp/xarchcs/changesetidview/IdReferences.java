package edu.uci.isr.archstudio4.comp.xarchcs.changesetidview;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetidview.ChangeSetIdViewEvent.CSIdViewEventType;
import edu.uci.isr.xarchflat.ObjRef;

/**
 * Maps the id to a collection of change segment objects which reference the object with that id.
 * Generates events when references are added or removed.
 * @author Kari
 *
 */
public class IdReferences {

	/** Maps the id to a collection of change segment objects which reference the object with that id*/
	private Map<String, Collection<ObjRef>> idToRefs;
	
	private ChangeSetIdViewListeners listeners;

	public IdReferences (ChangeSetIdViewListeners listeners){
		this.listeners = listeners;
		idToRefs = new HashMap<String, Collection<ObjRef>>();
	}

	public void add (String id, ObjRef ref, ObjRef xArchRef, boolean isInitialized) {
		// get collection of references to this id
		Collection<ObjRef> refs = idToRefs.get(id);
		if (refs == null) {
			// if no collection exists yet, create one
			refs = new HashSet<ObjRef>();	
		}
		// add the attribute segment ref to the collection of references
		refs.add(ref);
		idToRefs.put(id, refs);
		
		// generate event
		ChangeSetIdViewEvent evt = new ChangeSetIdViewEvent(CSIdViewEventType.REFERENCE_ADDED, id, ref, xArchRef, isInitialized);
		listeners.sendEvent(evt);
	}

	public Collection <ObjRef> get (String id) {
		return (Collection<ObjRef>) idToRefs.get(id);
	}

	public void remove (String id, ObjRef ref, ObjRef xArchRef) {
		Collection<ObjRef> refs = idToRefs.remove(id);
		if (refs != null) {
			// generate event
			ChangeSetIdViewEvent evt = new ChangeSetIdViewEvent(CSIdViewEventType.REFERENCE_REMOVED, id, ref, xArchRef);
			listeners.sendEvent(evt);
		}
	}

	public String[] getAllIds() {
		return (String[])idToRefs.keySet().toArray(new String[0]);
	}

}
