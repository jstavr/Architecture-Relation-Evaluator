package edu.uci.isr.archstudio4.graphlayout;

import edu.uci.isr.xarchflat.ObjRef;

public interface IGraphLayout{
	public String[] getEngineIDs();
	public String getEngineDescription(String engineID);
	
	public GraphLayout layoutGraph(String engineID, 
		ObjRef rootRef, GraphLayoutParameters params)
		throws GraphLayoutException;
}
