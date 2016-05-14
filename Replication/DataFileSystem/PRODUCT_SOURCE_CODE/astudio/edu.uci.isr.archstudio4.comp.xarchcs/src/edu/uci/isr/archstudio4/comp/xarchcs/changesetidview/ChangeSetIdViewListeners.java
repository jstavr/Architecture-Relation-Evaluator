package edu.uci.isr.archstudio4.comp.xarchcs.changesetidview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class ChangeSetIdViewListeners  {
	
	List<IChangeSetIdViewListener> listeners;
	
	public ChangeSetIdViewListeners () {
		listeners = Collections.synchronizedList(new ArrayList<IChangeSetIdViewListener>());
	}
	public void addListener(IChangeSetIdViewListener l){
		listeners.add(l);
	}

	public void removeListener(IChangeSetIdViewListener l){
		listeners.remove(l);
	}

	public void sendEvent(ChangeSetIdViewEvent evt){
		for(IChangeSetIdViewListener l: listeners.toArray(new IChangeSetIdViewListener[listeners.size()])){
			l.handleCSIdViewEvent(evt);
		}
	}
}
