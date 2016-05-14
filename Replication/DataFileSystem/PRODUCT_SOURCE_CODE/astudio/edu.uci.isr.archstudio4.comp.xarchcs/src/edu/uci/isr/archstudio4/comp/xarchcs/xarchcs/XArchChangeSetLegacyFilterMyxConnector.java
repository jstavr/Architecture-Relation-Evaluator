package edu.uci.isr.archstudio4.comp.xarchcs.xarchcs;

import edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach.XArchDetachFilterImpl;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach.XArchDetachInterface;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatListener;

public class XArchChangeSetLegacyFilterMyxConnector
	extends AbstractMyxSimpleBrick
	implements IMyxDynamicBrick{

	public static final IMyxName INTERFACE_XARCHCS = MyxUtils.createName("xarchcs");
	public static final IMyxName INTERFACE_XARCHFLATEVENTS = MyxUtils.createName("xarchflatevents");
	public static final IMyxName INTERFACE_XARCHD = MyxUtils.createName("xarchd");
	public static final IMyxName INTERFACE_XARCHDETACHEVENTS = MyxUtils.createName("xarchdetachevents");

	public static final IMyxName INTERFACE_XARCHCS_FILTERED = MyxUtils.createName("xarchcs-filtered");
	public static final IMyxName INTERFACE_XARCHFLATEVENTS_FILTERED = MyxUtils.createName("xarchflatevents-filtered");

	XArchChangeSetInterface xarchcs;

	XArchDetachInterface xarchd;

	XArchDetachFilterImpl xarchfilter;

	@Override
	public void init(){
		xarchcs = (XArchChangeSetInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_XARCHCS);
		xarchd = (XArchDetachInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_XARCHD);
		xarchfilter = new XArchChangeSetLegacyFilterImpl(xarchcs, xarchd);
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_XARCHFLATEVENTS)){
			return xarchfilter;
		}
		if(interfaceName.equals(INTERFACE_XARCHDETACHEVENTS)){
			return xarchfilter;
		}
		if(interfaceName.equals(INTERFACE_XARCHCS_FILTERED)){
			return xarchfilter;
		}
		return null;
	}

	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_XARCHFLATEVENTS_FILTERED)){
			xarchfilter.addXArchFlatListener((XArchFlatListener)serviceObject);
		}
	}

	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_XARCHFLATEVENTS_FILTERED)){
			xarchfilter.addXArchFlatListener(null);
		}
	}

	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){
	}
}
