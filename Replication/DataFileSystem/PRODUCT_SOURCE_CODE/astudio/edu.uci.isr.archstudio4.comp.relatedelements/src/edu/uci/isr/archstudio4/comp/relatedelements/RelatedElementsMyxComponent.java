package edu.uci.isr.archstudio4.comp.relatedelements;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class RelatedElementsMyxComponent extends AbstractMyxSimpleBrick{

	public static final IMyxName INTERFACE_XARCHCS = MyxUtils.createName("xarchcs");
	public static final IMyxName INTERFACE_RELATEDELEMENTS = MyxUtils.createName("relatedelements");
	
	XArchFlatInterface xArch;

	RelatedElementsManagerImpl relatedElementsManager;

	@Override
	public void init(){
		super.init();
		xArch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_XARCHCS);
		relatedElementsManager = new RelatedElementsManagerImpl(xArch);
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_RELATEDELEMENTS)){
			return relatedElementsManager;
		}
		return null;
	}
}
