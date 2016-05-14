package edu.uci.isr.archstudio4.comp.archipelago.variants;

import org.eclipse.jface.viewers.TreeViewer;

import edu.uci.isr.archstudio4.comp.archipelago.AbstractArchipelagoTreePlugin;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.xarchflat.ObjRef;

public class VariantsTreePlugin extends AbstractArchipelagoTreePlugin{

	public VariantsTreePlugin(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
		VariantsEventListener eventListener = new VariantsEventListener(AS, xArchRef);
		AS.eventBus.addArchipelagoEventListener(eventListener);
	}
}
