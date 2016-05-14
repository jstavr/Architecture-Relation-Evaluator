package edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusview;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ChangeSetStatusViewMyxComponent extends AbstractMyxSimpleBrick implements IMyxDynamicBrick{

	public static final IMyxName INTERFACE_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_CSSYNC= MyxUtils.createName("cssync");
	public static final IMyxName INTERFACE_CSADT = MyxUtils.createName("csadt");
	public static final IMyxName INTERFACE_XARCHFLATEVENTS = MyxUtils.createName("xarchflatevents");
	public static final IMyxName INTERFACE_XARCHFILEEVENTS = MyxUtils.createName("xarchfileevents");

	
	public static final IMyxName INTERFACE_CHANGESETSTATUSVIEW = MyxUtils.createName("csstatusview");
	public static final IMyxName INTERFACE_CHANGESETSTATUSVIEWEVENTS = MyxUtils.createName("csstatusviewevents");

	XArchFlatInterface xarch;
	IChangeSetSync cssync;
	IChangeSetADT csadt;
	
	ChangeSetStatusViewImpl changeSetStatusView;
	
	
	
	public void init(){
		super.init();
		xarch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_XARCH);
		cssync = (IChangeSetSync)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_CSSYNC);
		csadt = (IChangeSetADT)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_CSADT);
		changeSetStatusView = new ChangeSetStatusViewImpl(xarch, cssync, csadt);
	}
	
	public Object getServiceObject(IMyxName interfaceName) {
		if(interfaceName.equals(INTERFACE_XARCHFLATEVENTS)){
			return changeSetStatusView;
		}
		if(interfaceName.equals(INTERFACE_XARCHFILEEVENTS)){
			return changeSetStatusView;
		}
		if(interfaceName.equals(INTERFACE_CHANGESETSTATUSVIEW)){
			return changeSetStatusView;
		}
		return null;	
	}
	
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_CHANGESETSTATUSVIEWEVENTS)){
			changeSetStatusView.getListeners().addListener((IChangeSetStatusViewListener)serviceObject);
		}
	}

	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_CHANGESETSTATUSVIEWEVENTS)){
			changeSetStatusView.getListeners().removeListener((IChangeSetStatusViewListener)serviceObject);
		}
	}

	public void interfaceDisconnected(IMyxName interfaceName,
			Object serviceObject) {
	}
	

}
