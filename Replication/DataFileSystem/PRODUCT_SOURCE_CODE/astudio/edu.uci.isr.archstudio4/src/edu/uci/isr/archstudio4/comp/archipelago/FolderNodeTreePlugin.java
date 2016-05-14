package edu.uci.isr.archstudio4.comp.archipelago;

import org.eclipse.jface.viewers.TreeViewer;

import edu.uci.isr.xarchflat.ObjRef;

public class FolderNodeTreePlugin extends AbstractArchipelagoTreePlugin{
	public FolderNodeTreePlugin(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
		this.labelProvider = new FolderNodeLabelProvider(AS);
	}
}
