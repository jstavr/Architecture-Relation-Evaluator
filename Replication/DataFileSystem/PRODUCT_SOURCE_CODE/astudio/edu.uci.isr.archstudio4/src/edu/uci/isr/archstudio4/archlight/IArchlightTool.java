package edu.uci.isr.archstudio4.archlight;

import edu.uci.isr.xarchflat.ObjRef;

public interface IArchlightTool{
	public String getToolID();
	public void runTests(ObjRef documentRef, String[] testUIDs);
	public void reloadTests();
}
