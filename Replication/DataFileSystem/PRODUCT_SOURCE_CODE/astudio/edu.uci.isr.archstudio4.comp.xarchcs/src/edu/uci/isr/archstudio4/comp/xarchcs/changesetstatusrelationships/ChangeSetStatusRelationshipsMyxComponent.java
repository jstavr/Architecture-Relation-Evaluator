package edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusrelationships;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetrelationshipmanager.IChangeSetRelationshipManager;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusview.IChangeSetStatusView;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ChangeSetStatusRelationshipsMyxComponent extends AbstractMyxSimpleBrick {

	public static final IMyxName INTERFACE_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_CSADT = MyxUtils.createName("csadt");
	public static final IMyxName INTERFACE_CSSTATUSVIEW = MyxUtils.createName("csstatusview");
	public static final IMyxName INTERFACE_CSRELATIONSHIPMANAGER = MyxUtils.createName("csrelationshipmanager");
	
	public static final IMyxName INTERFACE_CSSTATUSVIEWEVENTS = MyxUtils.createName("csstatusviewevents");
	
	XArchFlatInterface xarch;
	IChangeSetADT changeSetADT;
	IChangeSetStatusView csStatusView;
	IChangeSetRelationshipManager csRelMgr;
	
	ChangeSetStatusRelationshipsImpl statusRelationships;
	
	public void init(){
		super.init();
		xarch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_XARCH);
		changeSetADT = (IChangeSetADT)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_CSADT);
		csStatusView = (IChangeSetStatusView)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_CSSTATUSVIEW);
		csRelMgr = (IChangeSetRelationshipManager)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_CSRELATIONSHIPMANAGER);
		
		statusRelationships = new ChangeSetStatusRelationshipsImpl(xarch, changeSetADT, csStatusView, csRelMgr);
	}
	
	public Object getServiceObject(IMyxName interfaceName) {

		if(interfaceName.equals(INTERFACE_CSSTATUSVIEWEVENTS)){
			return statusRelationships;
		}
		return null;	
	}
	

}
