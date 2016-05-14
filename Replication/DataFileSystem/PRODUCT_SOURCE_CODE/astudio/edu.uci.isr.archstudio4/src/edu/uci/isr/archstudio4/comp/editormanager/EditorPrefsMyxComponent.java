package edu.uci.isr.archstudio4.comp.editormanager;

import java.util.Properties;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.uci.isr.archstudio4.editors.IEditorManager;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;

public class EditorPrefsMyxComponent extends AbstractMyxSimpleBrick 
implements IMyxDynamicBrick{

	public static final IMyxName INTERFACE_NAME_OUT_EDITORMANAGER = MyxUtils.createName("editormanager");
	public static final IMyxName INTERFACE_NAME_OUT_PREFERENCES = MyxUtils.createName("preferences");
	
	protected IPreferenceStore prefs = null;
	protected IEditorManager editorManager = null;
	
	private MyxRegistry er = MyxRegistry.getSharedInstance();

	public EditorPrefsMyxComponent(){
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
		else if(interfaceName.equals(INTERFACE_NAME_OUT_EDITORMANAGER)){
			editorManager = (IEditorManager)serviceObject;
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_PREFERENCES)){
			prefs = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_EDITORMANAGER)){
			editorManager = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){}
	
	public Object getServiceObject(IMyxName interfaceName){
		return null;
	}
	
}
