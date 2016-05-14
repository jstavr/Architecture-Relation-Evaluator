package edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ChangeSetRelationshipManagerMyxComponent extends AbstractMyxSimpleBrick {

	public static final IMyxName INTERFACE_XARCH = MyxUtils.createName("xarch");
	
	public static final IMyxName INTERFACE_CHANGESETRELATIONSHIPMANAGER = MyxUtils.createName("csrelationshipmanager");

	XArchFlatInterface xarch;
	IChangeSetRelationshipManager csRelMgr;
	
	
	public void init(){
		super.init();
		xarch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_XARCH);
		csRelMgr = new ChangeSetRelationshipManagerImpl(xarch);
	}
	
	public Object getServiceObject(IMyxName interfaceName) {

		if(interfaceName.equals(INTERFACE_CHANGESETRELATIONSHIPMANAGER)){
			return csRelMgr;
		}
		return null;	
	}

}
