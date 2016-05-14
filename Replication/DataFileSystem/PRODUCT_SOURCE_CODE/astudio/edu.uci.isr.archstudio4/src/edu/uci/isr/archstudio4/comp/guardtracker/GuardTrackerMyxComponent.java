package edu.uci.isr.archstudio4.comp.guardtracker;

import edu.uci.isr.archstudio4.comp.booleannotation.IBooleanNotation;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class GuardTrackerMyxComponent extends AbstractMyxSimpleBrick implements IMyxDynamicBrick{

	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_OUT_BOOLEANNOTATION = MyxUtils.createName("booleannotation");
	public static final IMyxName INTERFACE_NAME_IN_GUARDTRACKER = MyxUtils.createName("guardtracker");
	
	protected XArchFlatInterface xarch = null;
	protected IBooleanNotation bni = null;
	
	protected GuardTrackerImpl guardTrackerImpl = null;
	
	public GuardTrackerMyxComponent(){
		guardTrackerImpl = new GuardTrackerImpl();
	}
	
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			xarch = (XArchFlatInterface)serviceObject;
			guardTrackerImpl.setXArchADT(xarch);
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_BOOLEANNOTATION)){
			bni = (IBooleanNotation)serviceObject;
			guardTrackerImpl.setBooleanNotation(bni);
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			xarch = null;
			guardTrackerImpl.setXArchADT(null);
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_BOOLEANNOTATION)){
			bni = null;
			guardTrackerImpl.setBooleanNotation(null);
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){
	}
	
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_GUARDTRACKER)){
			return guardTrackerImpl;
		}
		return null;
	}
}
