package edu.uci.isr.archstudio4.comp.xarchcs.xarchcs;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach.XArchDetachInterface;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class XArchChangeSetMyxComponent
	extends AbstractMyxSimpleBrick
	implements IMyxDynamicBrick{

	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_IN_FILEEVENTS = MyxUtils.createName("xarchfileevents");
	public static final IMyxName INTERFACE_NAME_IN_FLATEVENTS = MyxUtils.createName("xarchflatevents");

	public static final IMyxName INTERFACE_NAME_OUT_CHANGE_SET_ADT = MyxUtils.createName("csadt");

	public static final IMyxName INTERFACE_NAME_OUT_CHANGE_SET_SYNC = MyxUtils.createName("cssync");

	public static final IMyxName INTERFACE_NAME_OUT_DETACH = MyxUtils.createName("xarchd");

	public static final IMyxName INTERFACE_NAME_IN_XARCHCS = MyxUtils.createName("xarchcs");
	public static final IMyxName INTERFACE_NAME_OUT_CSEVENTS = MyxUtils.createName("xarchcsevents");

	XArchFlatInterface xarch;

	IChangeSetADT csadt;

	IChangeSetSync cssync;

	XArchChangeSetImpl xarchcs;

	XArchDetachInterface xarchd;

	@Override
	public void init(){
		xarch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_XARCH);
		csadt = (IChangeSetADT)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_CHANGE_SET_ADT);
		cssync = (IChangeSetSync)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_CHANGE_SET_SYNC);
		xarchd = (XArchDetachInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_DETACH);

		xarchcs = new XArchChangeSetImpl(xarch, csadt, cssync, xarchd);
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_XARCHCS)){
			return xarchcs;
		}
		if(interfaceName.equals(INTERFACE_NAME_IN_FILEEVENTS)){
			return xarchcs;
		}
		if(interfaceName.equals(INTERFACE_NAME_IN_FLATEVENTS)){
			return xarchcs;
		}
		return null;
	}

	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_CSEVENTS)){
			xarchcs.addXArchChangeSetListener((XArchChangeSetListener)serviceObject);
		}
	}

	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_CSEVENTS)){
			xarchcs.removeXArchChangeSetListener(null);
		}
	}

	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){
	}
}
