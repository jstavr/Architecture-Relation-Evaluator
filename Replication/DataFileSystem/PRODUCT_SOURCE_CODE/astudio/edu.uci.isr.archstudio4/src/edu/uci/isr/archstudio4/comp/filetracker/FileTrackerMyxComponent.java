package edu.uci.isr.archstudio4.comp.filetracker;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;

public class FileTrackerMyxComponent extends AbstractMyxSimpleBrick implements XArchFileListener, XArchFlatListener{

	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_IN_FLATEVENTS = MyxUtils.createName("xarchflatevents");
	public static final IMyxName INTERFACE_NAME_IN_FILEEVENTS = MyxUtils.createName("xarchfileevents");
	
	private MyxRegistry er = MyxRegistry.getSharedInstance();
	protected XArchFlatInterface xarch = null;
	
	public FileTrackerMyxComponent(){
	}
	
	public void begin(){
		xarch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_XARCH);
		er.register(this);
	}
	
	public void end(){
		er.unregister(this);
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_FILEEVENTS)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_IN_FLATEVENTS)){
			return this;
		}
		return null;
	}
	
	public void handleXArchFileEvent(XArchFileEvent evt){
		//System.out.println("got file event: " + evt);
		Object[] os = er.getObjects(this);
		//System.out.println("workbench parts len = " + os.length);
		for(int i = 0; i < os.length; i++){
			if(os[i] instanceof XArchFileListener){
				((XArchFileListener)os[i]).handleXArchFileEvent(evt);
			}
		}
	}
	
	public void handleXArchFlatEvent(XArchFlatEvent evt){
		Object[] os = er.getObjects(this);
		for(int i = 0; i < os.length; i++){
			if(os[i] instanceof XArchFlatListener){
				((XArchFlatListener)os[i]).handleXArchFlatEvent(evt);
			}
		}
	}

}
