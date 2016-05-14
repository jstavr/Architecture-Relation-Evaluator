package edu.uci.isr.archstudio4.comp.graphlayout;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.uci.isr.archstudio4.graphlayout.GraphLayout;
import edu.uci.isr.archstudio4.graphlayout.GraphLayoutException;
import edu.uci.isr.archstudio4.graphlayout.GraphLayoutParameters;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public interface ILayoutEngine{
	public String getID();
	public String getDescription();
	
	public GraphLayout layoutGraph(XArchFlatInterface xarch, IPreferenceStore prefs,
		ObjRef rootRef, GraphLayoutParameters params)
		throws GraphLayoutException;
}
