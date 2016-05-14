package edu.uci.isr.archstudio4.comp.xarchcs.eventmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetListener;
import edu.uci.isr.myx.conn.IEventRegulatorValve;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;

public class XArchCSEventManagerConnector extends AbstractMyxSimpleBrick implements IMyxDynamicBrick,XArchChangeSetListener,IEventManager {

	public static final IMyxName XARCHCSEVENTS_INTERFACE_NAME = MyxUtils.createName("xarchcsevents");
	public static final IMyxName EVENT_REGULATORS_INTERFACE_NAME = MyxUtils.createName("eventregulators");
	public static final IMyxName EVENT_MANAGER_INTERFACE_NAME = MyxUtils.createName("eventmanager");

	protected Object[] eventRegulators = new Object[0];

	public Object getServiceObject(IMyxName interfaceName) {
		if(interfaceName.equals(XARCHCSEVENTS_INTERFACE_NAME)){
			return this;
		}
		else if(interfaceName.equals(EVENT_MANAGER_INTERFACE_NAME)) {
			return this;
		}
		return null;
	}

	public void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(EVENT_REGULATORS_INTERFACE_NAME)){
			List<Object> l = new ArrayList<Object>(Arrays.asList(eventRegulators));
			l.add(serviceObject);
			eventRegulators = l.toArray(new Object[l.size()]);
		}
	}

	public void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(EVENT_REGULATORS_INTERFACE_NAME)){
			List<Object> l = new ArrayList<Object>(Arrays.asList(eventRegulators));
			l.remove(serviceObject);
			eventRegulators = l.toArray(new Object[l.size()]);
			if(eventRegulators.length == 0){
				eventRegulators = null;
			}
		}
	}

	public void interfaceDisconnected(IMyxName interfaceName,
			Object serviceObject) {

	}

	public void handleXArchChangeSetEvent(XArchChangeSetEvent evt) {
		if(eventRegulators != null && eventRegulators.length > 0) {
			for(Object obj : eventRegulators) {
				if(obj instanceof XArchChangeSetListener) {
					XArchChangeSetListener listener = (XArchChangeSetListener)obj;
					listener.handleXArchChangeSetEvent(evt);
				}
			}
		}
	}

	public void openValve() {
		synchronized(this) {
			if(eventRegulators != null && eventRegulators.length > 0) {
				for(Object obj : eventRegulators) {
					if(obj instanceof IEventRegulatorValve) {
						IEventRegulatorValve eventRegulatorValve = (IEventRegulatorValve)obj;
						eventRegulatorValve.openValve();
					}
				}
			}
		}
	}

	public void closeValve() {
		synchronized(this) {
			if(eventRegulators != null && eventRegulators.length > 0) {
				for(Object obj : eventRegulators) {
					if(obj instanceof IEventRegulatorValve) {
						IEventRegulatorValve eventRegulatorValve = (IEventRegulatorValve)obj;
						eventRegulatorValve.closeValve();
					}
				}
			}		
		}
	}
}
