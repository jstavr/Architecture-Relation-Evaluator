package edu.uci.isr.archstudio4.comp.archlight.noticeview;

import edu.uci.isr.archstudio4.comp.archlight.noticeadt.ArchlightNoticeADTEvent;
import edu.uci.isr.archstudio4.comp.archlight.noticeadt.ArchlightNoticeADTListener;
import edu.uci.isr.archstudio4.comp.archlight.noticeadt.IArchlightNoticeADT;
import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;

public class ArchlightNoticeViewMyxComponent extends AbstractMyxSimpleBrick implements 
ArchlightNoticeADTListener, IMyxDynamicBrick{

	public static final IMyxName INTERFACE_NAME_OUT_NOTICEADT = MyxUtils.createName("archlightnoticeadt");
	public static final IMyxName INTERFACE_NAME_OUT_RESOURCES = MyxUtils.createName("resources");
	public static final IMyxName INTERFACE_NAME_IN_NOTICEADTEVENTS = MyxUtils.createName("archlightnoticeadtevents");

	private MyxRegistry er = MyxRegistry.getSharedInstance();
	
	//protected static ArchlightNoticeViewMyxComponent sharedInstance = null;
	
	protected IArchlightNoticeADT noticeadt = null;
	protected IResources resources = null;
	
	public ArchlightNoticeViewMyxComponent(){
	}
	
	public void begin(){
		er.register(this);
	}
	
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_NOTICEADT)){
			noticeadt = (IArchlightNoticeADT)serviceObject;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_RESOURCES)){
			resources = (IResources)serviceObject;
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_NOTICEADT)){
			noticeadt = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_RESOURCES)){
			resources = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){}
	
	public void end(){
		er.unregister(this);
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_NOTICEADTEVENTS)){
			return this;
		}
		return null;
	}
	
	public void noticeADTChanged(ArchlightNoticeADTEvent evt){
		Object[] os = er.getObjects(this);
		for(int i = 0; i < os.length; i++){
			if(os[i] instanceof ArchlightNoticeADTListener){
				((ArchlightNoticeADTListener)os[i]).noticeADTChanged(evt);
			}
		}
	}
	
}
