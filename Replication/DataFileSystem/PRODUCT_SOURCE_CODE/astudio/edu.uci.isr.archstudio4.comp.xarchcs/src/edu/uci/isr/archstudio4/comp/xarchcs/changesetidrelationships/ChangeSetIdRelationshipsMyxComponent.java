package edu.uci.isr.archstudio4.comp.xarchcs.changesetidrelationships;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetidview.IChangeSetIdView;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager.IChangeSetRelationshipManager;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ChangeSetIdRelationshipsMyxComponent extends AbstractMyxSimpleBrick {

	public static final IMyxName INTERFACE_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_CSADT = MyxUtils.createName("csadt");
	public static final IMyxName INTERFACE_CSIDVIEW = MyxUtils.createName("csidview");
	public static final IMyxName INTERFACE_CSRELATIONSHIPMANAGER = MyxUtils.createName("csrelationshipmanager");
	
	public static final IMyxName INTERFACE_CSIDVIEWEVENTS = MyxUtils.createName("csidviewevents");
	
	XArchFlatInterface xarch;
	IChangeSetADT changeSetADT;
	IChangeSetIdView csIdView;
	IChangeSetRelationshipManager csRelMgr;
	
	ChangeSetIdRelationshipsImpl idRelationships;
	
	public void init(){
		super.init();
		xarch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_XARCH);
		changeSetADT = (IChangeSetADT)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_CSADT);
		csIdView = (IChangeSetIdView)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_CSIDVIEW);
		csRelMgr = (IChangeSetRelationshipManager)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_CSRELATIONSHIPMANAGER);
		
		idRelationships = new ChangeSetIdRelationshipsImpl(xarch, changeSetADT, csIdView, csRelMgr);
	}
	
	public Object getServiceObject(IMyxName interfaceName) {

		if(interfaceName.equals(INTERFACE_CSIDVIEWEVENTS)){
			return idRelationships;
		}
		return null;	
	}
	

}
