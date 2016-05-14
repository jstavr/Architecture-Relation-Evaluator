package edu.uci.isr.archstudio4.comp.archlight.prefs;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;

public class ArchlightPrefsMyxComponent extends AbstractMyxSimpleBrick implements IMyxDynamicBrick{
	public static final IMyxName INTERFACE_NAME_OUT_PREFERENCES = MyxUtils.createName("preferences");
	
	protected IPreferenceStore prefs = null;
	
	private MyxRegistry er = MyxRegistry.getSharedInstance();

	public ArchlightPrefsMyxComponent(){
	}
	
	public void begin(){
		er.register(this);
	}
	
	public void end(){
		er.unregister(this);
	}

	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_PREFERENCES)){
			prefs = (IPreferenceStore)serviceObject;
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_PREFERENCES)){
			prefs = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){}
	
	public Object getServiceObject(IMyxName interfaceName){
		return null;
	}
	
}
