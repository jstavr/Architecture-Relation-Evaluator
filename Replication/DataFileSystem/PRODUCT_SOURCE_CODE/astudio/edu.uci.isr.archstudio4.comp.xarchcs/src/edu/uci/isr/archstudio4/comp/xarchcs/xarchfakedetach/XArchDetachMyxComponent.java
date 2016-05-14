package edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class XArchDetachMyxComponent extends AbstractMyxSimpleBrick
	implements IMyxDynamicBrick{

	public static final IMyxName INTERFACE_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_FILEEVENTS = MyxUtils.createName("xarchfileevents");

	public static final IMyxName INTERFACE_XARCHD = MyxUtils.createName("xarchd");
	public static final IMyxName INTERFACE_DETACHEVENTS = MyxUtils.createName("xarchdetachevents");

	public XArchDetachMyxComponent(){
	}

	XArchFlatInterface xarch;

	XArchDetachImpl xarchd;

	@Override
	public void init(){
		xarch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_XARCH);
		xarchd = new XArchDetachImpl(xarch);
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_FILEEVENTS)){
			return xarchd;
		}
		if(interfaceName.equals(INTERFACE_XARCHD)){
			return xarchd;
		}
		return null;
	}

	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_DETACHEVENTS)){
			xarchd.addDetachListener((XArchDetachListener)serviceObject);
		}
	}

	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_DETACHEVENTS)){
			xarchd.removeDetachListener((XArchDetachListener)serviceObject);
		}
	}

	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){
	}
}
