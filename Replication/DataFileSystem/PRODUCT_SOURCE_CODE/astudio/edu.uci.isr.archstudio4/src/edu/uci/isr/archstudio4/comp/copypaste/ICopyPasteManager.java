package edu.uci.isr.archstudio4.comp.copypaste;

import edu.uci.isr.xarchflat.ObjRef;

public interface ICopyPasteManager {

	public void copy(ObjRef[] objRefs,ObjRef diagramRef);
	public void paste(ObjRef parentRef,ICopiedElementNode[] nodes);
	public boolean canPaste(ObjRef parentRef);
	public ICopiedElementNode[] getClipboardNodes();
}
