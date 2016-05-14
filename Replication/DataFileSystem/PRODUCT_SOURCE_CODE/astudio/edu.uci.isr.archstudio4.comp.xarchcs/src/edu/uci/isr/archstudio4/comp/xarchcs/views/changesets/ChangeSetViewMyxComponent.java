package edu.uci.isr.archstudio4.comp.xarchcs.views.changesets;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTListener;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetListener;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatListener;

public class ChangeSetViewMyxComponent extends AbstractMyxSimpleBrick
	implements XArchFileListener, XArchFlatListener, XArchChangeSetListener, ExplicitADTListener{

	public static final IMyxName INTERFACE_NAME_OUT_XARCHCS = MyxUtils.createName("xarchcs");
	public static final IMyxName INTERFACE_NAME_IN_FILEEVENTS = MyxUtils.createName("xarchfileevents");
	public static final IMyxName INTERFACE_NAME_IN_FLATEVENTS = MyxUtils.createName("xarchflatevents");
	public static final IMyxName INTERFACE_NAME_IN_CSEVENTS = MyxUtils.createName("xarchcsevents");

	public static final IMyxName INTERFACE_NAME_OUT_EXPLICITADT = MyxUtils.createName("explicitadt");
	public static final IMyxName INTERFACE_NAME_IN_EXPLICITEVENTS = MyxUtils.createName("explicitevents");
	public static final IMyxName INTERFACE_NAME_OUT_CSADT = MyxUtils.createName("csadt");
	public static final IMyxName INTERFACE_NAME_OUT_CSSYNC = MyxUtils.createName("cssync");

	XArchChangeSetInterface xarch = null;

	IExplicitADT explicit = null;
	
	IChangeSetADT adt = null;

	IChangeSetSync cssync = null;

	MyxRegistry myxr = MyxRegistry.getSharedInstance();

	@Override
	public void begin(){
		xarch = (XArchChangeSetInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_XARCHCS);
		explicit = (IExplicitADT)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_EXPLICITADT);
		adt = (IChangeSetADT)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_CSADT);
		cssync = (IChangeSetSync)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_CSSYNC);
		myxr.register(this);
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_FILEEVENTS)){
			return this;
		}
		if(interfaceName.equals(INTERFACE_NAME_IN_FLATEVENTS)){
			return this;
		}
		if(interfaceName.equals(INTERFACE_NAME_IN_CSEVENTS)){
			return this;
		}
		if(interfaceName.equals(INTERFACE_NAME_IN_EXPLICITEVENTS)){
			return this;
		}
		return null;
	}

	public void handleXArchFileEvent(XArchFileEvent evt){
		for(Object o: myxr.getObjects(this).clone()){
			if(o instanceof XArchFileListener){
				((XArchFileListener)o).handleXArchFileEvent(evt);
			}
		}
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt){
		for(Object o: myxr.getObjects(this).clone()){
			if(o instanceof XArchFlatListener){
				((XArchFlatListener)o).handleXArchFlatEvent(evt);
			}
		}
	}

	public void handleXArchChangeSetEvent(XArchChangeSetEvent evt){
		for(Object o: myxr.getObjects(this).clone()){
			if(o instanceof XArchChangeSetListener){
				((XArchChangeSetListener)o).handleXArchChangeSetEvent(evt);
			}
		}
	}

	public void handleExplicitEvent(ExplicitADTEvent evt){
		for(Object o: myxr.getObjects(this).clone()){
			if(o instanceof ExplicitADTListener){
				((ExplicitADTListener)o).handleExplicitEvent(evt);
			}
		}
	}
}
