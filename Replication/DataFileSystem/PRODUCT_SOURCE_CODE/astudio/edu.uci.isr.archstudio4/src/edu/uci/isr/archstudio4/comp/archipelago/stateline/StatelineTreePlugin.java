package edu.uci.isr.archstudio4.comp.archipelago.stateline;

import org.eclipse.jface.viewers.TreeViewer;

import edu.uci.isr.archstudio4.comp.archipelago.AbstractArchipelagoTreePlugin;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.xarchflat.ObjRef;

public class StatelineTreePlugin extends AbstractArchipelagoTreePlugin{

	public StatelineTreePlugin(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){

		StatelineEventListener eventListener = new StatelineEventListener(AS, xArchRef);
		AS.eventBus.addArchipelagoEventListener(eventListener);
	}
	
}
