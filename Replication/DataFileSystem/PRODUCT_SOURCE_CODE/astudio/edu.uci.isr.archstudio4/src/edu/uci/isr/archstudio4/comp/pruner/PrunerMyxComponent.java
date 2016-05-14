package edu.uci.isr.archstudio4.comp.pruner;

import java.util.Properties;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class PrunerMyxComponent extends AbstractMyxSimpleBrick 
implements IMyxDynamicBrick, IPruner{

	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_IN_PRUNER = MyxUtils.createName("pruner");
	
	protected XArchFlatInterface xarch = null;
	
	public PrunerMyxComponent(){
	}
	
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			xarch = (XArchFlatInterface)serviceObject;
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			xarch = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){}
	
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_PRUNER)){
			return this;
		}
		return null;
	}
	
	public void prune(String archURI, String targetArchURI, String startingID, boolean isStructural) throws InvalidURIException, InvalidElementIDException, edu.uci.isr.archstudio4.comp.pruner.MissingElementException, MissingAttributeException, BrokenLinkException{
		PrunerImpl prunerImpl = new PrunerImpl(xarch);
		prunerImpl.prune(archURI, targetArchURI, startingID, isStructural);
	}
	
}
