package edu.uci.isr.archstudio4.comp.aim;

import edu.uci.isr.myx.fw.IMyxRuntime;
import edu.uci.isr.xarchflat.ObjRef;

public interface IAIM{
	
	public void instantiate(IMyxRuntime myx, String name, ObjRef xArchRef, ObjRef structureRef) throws ArchitectureInstantiationException;
	public void destroy(IMyxRuntime myx, String name);
}
