package edu.uci.isr.archstudio4.comp.archlight.noticeadt;

import java.util.Properties;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;

public class ArchlightNoticeADTMyxComponent extends AbstractMyxSimpleBrick 
implements ArchlightNoticeADTListener, IMyxDynamicBrick{

	public static final IMyxName INTERFACE_NAME_IN_NOTICEADT = MyxUtils.createName("archlightnoticeadt");
	public static final IMyxName INTERFACE_NAME_OUT_NOTICEADT_EVENTS = MyxUtils.createName("archlightnoticeadtevents");
	
	protected ArchlightNoticeADT noticeadt = null;
	protected ArchlightNoticeADTListener noticeadtListener = null;
	
	public ArchlightNoticeADTMyxComponent(){
	}
	
	public void init(){
		ArchlightNoticeADT noticeadtImpl = new ArchlightNoticeADT();
		this.noticeadt = noticeadtImpl;
		noticeadtImpl.addArchlightNoticeADTListener(this);
	}
	
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_NOTICEADT_EVENTS)){
			noticeadtListener = (ArchlightNoticeADTListener)serviceObject;
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_NOTICEADT_EVENTS)){
			noticeadtListener = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){}
	
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_NOTICEADT)){
			return noticeadt;
		}
		return null;
	}
	
	public synchronized void noticeADTChanged(ArchlightNoticeADTEvent evt){
		if(noticeadtListener != null){
			noticeadtListener.noticeADTChanged(evt);
		}
	}
}


