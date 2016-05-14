package edu.uci.isr.archstudio4.comp.preferencesadt;

import java.util.Properties;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import edu.uci.isr.archstudio4.Archstudio4Activator;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;


public class PreferencesADTMyxComponent extends AbstractMyxSimpleBrick 
implements IPropertyChangeListener, IMyxDynamicBrick{

	public static final IMyxName INTERFACE_NAME_IN_PREFS = MyxUtils.createName("preferences");
	public static final IMyxName INTERFACE_NAME_OUT_PROPERTYEVENTS = MyxUtils.createName("propertyevents");

	protected IPreferenceStore prefs = null;
	protected IPropertyChangeListener propertyListener = null;
	
	public PreferencesADTMyxComponent(){
	}
	
	public void init(){
		this.prefs = Archstudio4Activator.getDefault().getPreferenceStore();
		prefs.addPropertyChangeListener(this);
	}
	
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_PROPERTYEVENTS)){
			propertyListener = (IPropertyChangeListener)serviceObject;
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_PROPERTYEVENTS)){
			propertyListener = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){}
	
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_PREFS)){
			return prefs;
		}
		return null;
	}
	
	public synchronized void propertyChange(PropertyChangeEvent event){
		if(propertyListener != null){
			propertyListener.propertyChange(event);
		}
	}
}
