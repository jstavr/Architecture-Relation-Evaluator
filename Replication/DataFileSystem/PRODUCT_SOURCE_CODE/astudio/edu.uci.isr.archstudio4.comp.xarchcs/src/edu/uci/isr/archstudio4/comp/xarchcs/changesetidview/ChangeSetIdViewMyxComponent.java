package edu.uci.isr.archstudio4.comp.xarchcs.changesetidview;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ChangeSetIdViewMyxComponent extends AbstractMyxSimpleBrick implements IMyxDynamicBrick{

	public static final IMyxName INTERFACE_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_XARCHFILEEVENTS = MyxUtils.createName("xarchfileevents");
	public static final IMyxName INTERFACE_XARCHFLATEVENTS = MyxUtils.createName("xarchflatevents");
	
	public static final IMyxName INTERFACE_CHANGESETIDVIEWEVENTS = MyxUtils.createName("csidviewevents");
	public static final IMyxName INTERFACE_CHANGESETIDVIEW = MyxUtils.createName("csidview");

	XArchFlatInterface xarch;
	
	ChangeSetIdViewImpl changeSetIdView;
	
	public void init(){
		super.init();
		xarch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_XARCH);
		changeSetIdView = new ChangeSetIdViewImpl(xarch);
	}
	
	public Object getServiceObject(IMyxName interfaceName) {

		if(interfaceName.equals(INTERFACE_XARCHFILEEVENTS)){
			return changeSetIdView;
		}
		if(interfaceName.equals(INTERFACE_XARCHFLATEVENTS)){
			return changeSetIdView;
		}
		if(interfaceName.equals(INTERFACE_CHANGESETIDVIEW)){
			return changeSetIdView;
		}
		return null;	
	}
	
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_CHANGESETIDVIEWEVENTS)){
			changeSetIdView.getListeners().addListener((IChangeSetIdViewListener)serviceObject);
		}
	}

	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_CHANGESETIDVIEWEVENTS)){
			changeSetIdView.getListeners().removeListener((IChangeSetIdViewListener)serviceObject);
		}
	}

	public void interfaceDisconnected(IMyxName interfaceName,
			Object serviceObject) {
		// TODO Auto-generated method stub	
	}

}
