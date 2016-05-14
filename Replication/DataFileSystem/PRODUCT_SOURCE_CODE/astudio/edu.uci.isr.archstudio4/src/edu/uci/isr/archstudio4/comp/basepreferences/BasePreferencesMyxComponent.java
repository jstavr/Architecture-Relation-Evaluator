package edu.uci.isr.archstudio4.comp.basepreferences;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;

public class BasePreferencesMyxComponent extends AbstractMyxSimpleBrick 
implements IMyxDynamicBrick{

	public static final IMyxName INTERFACE_NAME_OUT_PREFERENCES = MyxUtils.createName("preferences");
	public static final IMyxName INTERFACE_NAME_OUT_RESOURCES = MyxUtils.createName("resources");
	
	private MyxRegistry er = MyxRegistry.getSharedInstance();

	protected IResources resources = null;
	protected IPreferenceStore preferences = null;
	
	public BasePreferencesMyxComponent(){
	}
	
	public void begin(){
		er.register(this);
	}
	
	public void end(){
		er.unregister(this);
	}
	
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_PREFERENCES)){
			preferences = (IPreferenceStore)serviceObject;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_RESOURCES)){
			resources = (IResources)serviceObject;
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_PREFERENCES)){
			preferences = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_RESOURCES)){
			resources = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){}
	
	public Object getServiceObject(IMyxName interfaceName){
		return null;
	}
	
}
