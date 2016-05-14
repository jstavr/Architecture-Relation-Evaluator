package edu.uci.isr.archstudio4.comp.booleaneval;

import java.util.Properties;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class BooleanEvalMyxComponent extends AbstractMyxSimpleBrick 
implements IMyxDynamicBrick{

	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_IN_BOOLEANEVAL = MyxUtils.createName("booleaneval");
	
	protected XArchFlatInterface xarch = null;
	protected BooleanEvalImpl booleanEvalImpl = null;
	
	public BooleanEvalMyxComponent(){
	}
	
	public void init(){
	}

	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			xarch = (XArchFlatInterface)serviceObject;
			booleanEvalImpl = new BooleanEvalImpl(xarch);
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			booleanEvalImpl = null;
			xarch = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){}
	
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_BOOLEANEVAL)){
			return booleanEvalImpl;
		}
		return null;
	}
	
}
