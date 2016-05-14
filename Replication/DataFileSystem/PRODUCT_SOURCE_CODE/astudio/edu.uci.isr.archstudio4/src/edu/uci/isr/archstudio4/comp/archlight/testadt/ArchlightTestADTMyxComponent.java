package edu.uci.isr.archstudio4.comp.archlight.testadt;

import java.util.Properties;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;

public class ArchlightTestADTMyxComponent extends AbstractMyxSimpleBrick 
implements ArchlightTestADTListener, IMyxDynamicBrick{

	public static final IMyxName INTERFACE_NAME_IN_TESTADT = MyxUtils.createName("archlighttestadt");
	public static final IMyxName INTERFACE_NAME_OUT_TESTADT_EVENTS = MyxUtils.createName("archlighttestadtevents");
	
	protected ArchlightTestADT testadt = null;
	protected ArchlightTestADTListener testadtListener = null;
	
	public ArchlightTestADTMyxComponent(){
	}
	
	public void init(){
		ArchlightTestADT testadtImpl = new ArchlightTestADT();
		this.testadt = testadtImpl;
		testadtImpl.addArchlightTestADTListener(this);
	}
	
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_TESTADT_EVENTS)){
			testadtListener = (ArchlightTestADTListener)serviceObject;
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_TESTADT_EVENTS)){
			testadtListener = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){}
	
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_TESTADT)){
			return testadt;
		}
		return null;
	}
	
	public synchronized void testADTChanged(ArchlightTestADTEvent evt){
		if(testadtListener != null){
			testadtListener.testADTChanged(evt);
		}
	}
}


