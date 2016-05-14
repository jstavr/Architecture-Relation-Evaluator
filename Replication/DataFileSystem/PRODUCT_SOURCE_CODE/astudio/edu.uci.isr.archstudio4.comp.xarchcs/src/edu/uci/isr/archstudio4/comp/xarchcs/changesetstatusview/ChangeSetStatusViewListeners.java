package edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class ChangeSetStatusViewListeners  {
	
	List<IChangeSetStatusViewListener> listeners;
	
	public ChangeSetStatusViewListeners () {
		listeners = Collections.synchronizedList(new ArrayList<IChangeSetStatusViewListener>());
	}
	public void addListener(IChangeSetStatusViewListener l){
		listeners.add(l);
	}

	public void removeListener(IChangeSetStatusViewListener l){
		listeners.remove(l);
	}

	public void sendEvent(ChangeSetStatusViewEvent evt){
		for(IChangeSetStatusViewListener l: listeners.toArray(new IChangeSetStatusViewListener[listeners.size()])){
			l.handleCSStatusViewEvent(evt);
		}
	}
}
