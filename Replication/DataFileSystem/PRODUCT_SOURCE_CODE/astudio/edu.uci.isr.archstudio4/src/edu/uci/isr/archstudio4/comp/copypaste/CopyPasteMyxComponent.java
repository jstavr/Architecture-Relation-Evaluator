package edu.uci.isr.archstudio4.comp.copypaste;

import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElementsManager;
import edu.uci.isr.archstudio4.comp.xarchcs.eventmanager.IEventManager;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class CopyPasteMyxComponent extends AbstractMyxSimpleBrick{
	public static final IMyxName INTERFACE_XARCHCS = MyxUtils.createName("xarchcs");
	public static final IMyxName INTERFACE_COPY_PASTE = MyxUtils.createName("copypaste");
	public static final IMyxName INTERFACE_OUT_RELATEDELEMENTS = MyxUtils.createName("relatedelements");
	public static final IMyxName INTERFACE_OUT_EVENT_MANAGER = MyxUtils.createName("eventmanager");
	
	XArchFlatInterface xArch;

	ICopyPasteManager copyPasteManager;
	
	IRelatedElementsManager relatedElementsManager;
	
	IEventManager eventManager;
	
	@Override
	public void init(){
		super.init();
		xArch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_XARCHCS);
		relatedElementsManager = (IRelatedElementsManager)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_OUT_RELATEDELEMENTS);
		eventManager = (IEventManager)MyxUtils.getFirstRequiredServiceObject(this,INTERFACE_OUT_EVENT_MANAGER);
		copyPasteManager = new CopyPasteManagerImpl(xArch,relatedElementsManager,eventManager);
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_COPY_PASTE)){
			return copyPasteManager;
		}
		return null;
	}	
}
