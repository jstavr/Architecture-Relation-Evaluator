package edu.uci.isr.archstudio4.comp.versionpruner;

import java.util.Properties;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class VersionPrunerMyxComponent extends AbstractMyxSimpleBrick 
implements IMyxDynamicBrick, IVersionPruner{

	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_IN_VERSIONPRUNER = MyxUtils.createName("versionpruner");
	
	protected XArchFlatInterface xarch = null;
	
	public VersionPrunerMyxComponent(){
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
		if(interfaceName.equals(INTERFACE_NAME_IN_VERSIONPRUNER)){
			return this;
		}
		return null;
	}
	
	public void pruneVersions(String archURI, String targetArchURI) throws InvalidURIException{
		VersionPrunerImpl versionPrunerImpl = new VersionPrunerImpl(xarch);
		versionPrunerImpl.pruneVersions(archURI, targetArchURI);
	}
}
