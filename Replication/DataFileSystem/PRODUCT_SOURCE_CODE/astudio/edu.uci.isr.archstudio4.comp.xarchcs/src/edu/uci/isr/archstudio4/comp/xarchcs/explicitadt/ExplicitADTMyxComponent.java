package edu.uci.isr.archstudio4.comp.xarchcs.explicitadt;

import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;

public class ExplicitADTMyxComponent extends AbstractMyxSimpleBrick
	implements IMyxDynamicBrick{

	public static final IMyxName INTERFACE_XARCHCS = MyxUtils.createName("xarchcs");
	public static final IMyxName INTERFACE_XARCHFILEEVENTS = MyxUtils.createName("xarchfileevents");
	public static final IMyxName INTERFACE_XARCHFLATEVENTS = MyxUtils.createName("xarchflatevents");
	public static final IMyxName INTERFACE_XARCHCSEVENTS = MyxUtils.createName("xarchcsevents");
	public static final IMyxName INTERFACE_XARCHDETACHEVENTS = MyxUtils.createName("xarchdetachevents");

	public static final IMyxName INTERFACE_EXPLICITADT = MyxUtils.createName("explicitadt");
	public static final IMyxName INTERFACE_EXPLICITEVENTS = MyxUtils.createName("explicitevents");

	XArchChangeSetInterface xarch;

	IExplicitADT explicitADT;

	@Override
	public void init(){
		super.init();
		xarch = (XArchChangeSetInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_XARCHCS);
		explicitADT = new ExplicitADTImpl(xarch);
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_EXPLICITADT)){
			return explicitADT;
		}
		if(interfaceName.equals(INTERFACE_XARCHFILEEVENTS)){
			return explicitADT;
		}
		if(interfaceName.equals(INTERFACE_XARCHFLATEVENTS)){
			return explicitADT;
		}
		if(interfaceName.equals(INTERFACE_XARCHCSEVENTS)){
			return explicitADT;
		}
		if(interfaceName.equals(INTERFACE_XARCHDETACHEVENTS)){
			return explicitADT;
		}
		return null;
	}

	public void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_EXPLICITEVENTS)){
			explicitADT.setExplicitADTEventListener((ExplicitADTListener)serviceObject);
		}
	}

	public void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_EXPLICITEVENTS)){
			explicitADT.setExplicitADTEventListener(null);
		}
	}

	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){
	}
}
