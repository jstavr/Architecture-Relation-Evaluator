package edu.uci.isr.archstudio4.comp.xarchcs.changesetidview;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetidview.ChangeSetIdViewEvent.CSIdViewEventType;
import edu.uci.isr.xarchflat.ObjRef;

/**
 * Maps Ids to the change segment that creates them object with that id.
 * Generates events when ids are added or removed.
 * 
 * @author Kari
 *
 */
public class IdCreators {
		
	/** Maps the id to the change segment that creates the object with that id*/
	private Map<String, ObjRef> idToCreator;
	
	private ChangeSetIdViewListeners listeners;

	public IdCreators (ChangeSetIdViewListeners listeners){
		this.listeners = listeners;
		idToCreator = new HashMap<String, ObjRef>();
	}

	public void add (String id, ObjRef creator, ObjRef xArchRef, boolean isInitialized) {
		idToCreator.put(id, creator);

		// generate event
		ChangeSetIdViewEvent evt = new ChangeSetIdViewEvent(CSIdViewEventType.CREATOR_ADDED, id, creator, xArchRef, isInitialized);
		listeners.sendEvent(evt);
	}

	public ObjRef get (String id) {
		return (ObjRef) idToCreator.get(id);
	}

	public void remove (String id, ObjRef xArchRef) {
		ObjRef creator = idToCreator.remove(id);
		if(creator != null){
			// generate event
			ChangeSetIdViewEvent evt = new ChangeSetIdViewEvent(CSIdViewEventType.CREATOR_REMOVED, id, creator, xArchRef);
			listeners.sendEvent(evt);
		}
	}

	public String[] getAllIds() {
		return (String[])idToCreator.keySet().toArray(new String[0]);
	}	

}
