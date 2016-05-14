package edu.uci.isr.archstudio4.comp.archipelago.options;

import org.eclipse.jface.viewers.TreeViewer;

import edu.uci.isr.archstudio4.comp.archipelago.AbstractArchipelagoTreePlugin;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.xarchflat.ObjRef;

public class OptionsTreePlugin extends AbstractArchipelagoTreePlugin{

	public OptionsTreePlugin(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
		
		OptionsEventListener eventListener = new OptionsEventListener(AS, xArchRef);
		AS.eventBus.addArchipelagoEventListener(eventListener);
	}
}
