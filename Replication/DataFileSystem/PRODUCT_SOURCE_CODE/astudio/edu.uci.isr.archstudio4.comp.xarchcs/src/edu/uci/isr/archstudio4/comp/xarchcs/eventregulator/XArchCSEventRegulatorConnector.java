package edu.uci.isr.archstudio4.comp.xarchcs.eventregulator;

import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetListener;
import edu.uci.isr.myx.conn.EventRegulatorConnector;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;

public class XArchCSEventRegulatorConnector
	extends EventRegulatorConnector
	implements XArchChangeSetListener{

	public static final IMyxName XARCHCSEVENTS_INTERFACE_NAME = MyxUtils.createName("xarchcsevents");

	public XArchCSEventRegulatorConnector(){
		super();
	}

	@Override
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(XARCHCSEVENTS_INTERFACE_NAME)){
			return this;
		}
		else{
			return super.getServiceObject(interfaceName);
		}
	}

	public void handleXArchChangeSetEvent(XArchChangeSetEvent evt){
		switch(evt.getEventType()){
		case UPDATING_APPLIED_CHANGE_SETS:
			closeValve();
			break;
		case UPDATED_APPLIED_CHANGE_SETS:
			openValve();
			break;
		}
	}
}