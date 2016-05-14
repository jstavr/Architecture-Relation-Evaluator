package edu.uci.isr.archstudio4.comp.graphlayout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.uci.isr.archstudio4.comp.graphlayout.graphviz.DotLayoutEngine;
import edu.uci.isr.archstudio4.graphlayout.GraphLayout;
import edu.uci.isr.archstudio4.graphlayout.GraphLayoutException;
import edu.uci.isr.archstudio4.graphlayout.GraphLayoutParameters;
import edu.uci.isr.archstudio4.graphlayout.IGraphLayout;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class GraphLayoutMyxComponent extends AbstractMyxSimpleBrick implements IMyxDynamicBrick{

	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_OUT_PREFERENCES = MyxUtils.createName("preferences");
	public static final IMyxName INTERFACE_NAME_IN_GRAPHLAYOUT = MyxUtils.createName("graphlayout");
	
	protected XArchFlatInterface xarch = null;
	protected IPreferenceStore prefs = null;
	
	protected GraphLayoutImpl graphLayoutImpl = null;
	
	public GraphLayoutMyxComponent(){
	}
	
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			xarch = (XArchFlatInterface)serviceObject;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_PREFERENCES)){
			prefs = (IPreferenceStore)serviceObject;
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			xarch = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_PREFERENCES)){
			prefs = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){
	}
	
	public void init(){
		graphLayoutImpl = new GraphLayoutImpl();
		graphLayoutImpl.addLayoutEngine(new DotLayoutEngine());
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_GRAPHLAYOUT)){
			return graphLayoutImpl;
		}
		return null;
	}

	public class GraphLayoutImpl implements IGraphLayout{
		protected List<ILayoutEngine> engineList = new ArrayList<ILayoutEngine>();
		
		public void addLayoutEngine(ILayoutEngine engine){
			engineList.add(engine);
		}
		
		public void removeLayoutEngine(ILayoutEngine engine){
			engineList.remove(engine);
		}
		
		public ILayoutEngine[] getAllLayoutEngines(){
			return engineList.toArray(new ILayoutEngine[engineList.size()]);
		}
		
		public ILayoutEngine getLayoutEngine(String id){
			ILayoutEngine[] engines = getAllLayoutEngines();
			for(int i = 0; i < engines.length; i++){
				if(engines[i].getID().equals(id)){
					return engines[i];
				}
			}
			return null;
		}
		
		
		public String[] getEngineIDs(){
			ILayoutEngine[] engines = getAllLayoutEngines();
			String[] engineIDs = new String[engines.length];
			for(int i = 0; i < engines.length; i++){
				engineIDs[i] = engines[i].getID();
			}
			return engineIDs;
		}
		
		public String getEngineDescription(String engineID){
			ILayoutEngine engine = getLayoutEngine(engineID);
			if(engine == null){
				return null;
			}
			return engine.getDescription();
		}
		
		public GraphLayout layoutGraph(String engineID, ObjRef rootRef, GraphLayoutParameters params) throws GraphLayoutException{
			ILayoutEngine engine = getLayoutEngine(engineID);
			if(engine == null){
				throw new GraphLayoutException("No graph layout engine with ID: " + engineID + " exists.");
			}
			return engine.layoutGraph(xarch, prefs, rootRef, params);
		}
	}
}
