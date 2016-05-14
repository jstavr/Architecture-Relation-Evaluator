package edu.uci.isr.archstudio4.comp.xarchcs.explicitadt;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent.ExplicitEventType;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetListener;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent.ChangeSetEventType;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach.XArchDetachEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach.XArchDetachListener;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatListener;

public class ExplicitADTImpl
	implements IExplicitADT, XArchFileListener, XArchFlatListener, XArchChangeSetListener, XArchDetachListener{

	XArchChangeSetInterface xarch;

	public ExplicitADTImpl(XArchChangeSetInterface xarchcs){
		this.xarch = xarchcs;
	}

	Map<ObjRef, ExplicitInfo> explicitInfos = Collections.synchronizedMap(new HashMap<ObjRef, ExplicitInfo>());

	class ExplicitInfo{

		Object lock = new Object();

		ObjRef xArchRef;
		Set<ObjRef> explicitChangeSetRefs = new HashSet<ObjRef>();

		public ExplicitInfo(ObjRef xArchRef){
			this.xArchRef = xArchRef;
		}
	}

	ExplicitInfo getExplicitInfo(ObjRef xArchRef){
		synchronized(explicitInfos){
			ExplicitInfo ei = explicitInfos.get(xArchRef);
			if(ei == null){
				explicitInfos.put(xArchRef, ei = new ExplicitInfo(xArchRef));
			}
			return ei;
		}
	}

	ExplicitADTListener explicitADTEventListener;

	public void setExplicitADTEventListener(ExplicitADTListener explicitADTEventListener){
		this.explicitADTEventListener = explicitADTEventListener;
	}

	protected void fireExplicitADTEvent(ExplicitEventType type, ObjRef xArchRef, ObjRef[] changeSetRefs, ObjRef objRef){
		explicitADTEventListener.handleExplicitEvent(new ExplicitADTEvent(type, xArchRef, changeSetRefs, objRef));
	}

	public void setExplicitChangeSetRefs(ObjRef xArchRef, ObjRef[] changeSetRefs){
		ExplicitInfo ei = getExplicitInfo(xArchRef);
		synchronized(ei.lock){
			Set<ObjRef> newExplicitChangeSetRefs = new HashSet<ObjRef>(Arrays.asList(changeSetRefs));
			// newExplicitChangeSetRefs.retainAll(new HashSet<ObjRef>(Arrays.asList(xarch.getAppliedChangeSetRefs(xArchRef))));
			if(!ei.explicitChangeSetRefs.equals(newExplicitChangeSetRefs)){
				ei.explicitChangeSetRefs = newExplicitChangeSetRefs;
				fireExplicitADTEvent(ExplicitEventType.UPDATED_EXPLICIT_CHANGE_SETS, xArchRef, newExplicitChangeSetRefs.toArray(new ObjRef[newExplicitChangeSetRefs.size()]), null);
			}
		}
	}

	public ObjRef[] getExplicitChangeSetRefs(ObjRef xArchRef){
		ExplicitInfo ei = getExplicitInfo(xArchRef);
		synchronized(ei.lock){
			return ei.explicitChangeSetRefs.toArray(new ObjRef[ei.explicitChangeSetRefs.size()]);
		}
	}

	public void handleXArchChangeSetEvent(XArchChangeSetEvent evt){
		if(evt.getEventType() == ChangeSetEventType.UPDATED_APPLIED_CHANGE_SETS){
			ExplicitInfo ei = getExplicitInfo(evt.getXArchRef());
			synchronized(ei.lock){
				setExplicitChangeSetRefs(evt.getXArchRef(), ei.explicitChangeSetRefs.toArray(new ObjRef[ei.explicitChangeSetRefs.size()]));
			}
		}
	}

	public void handleXArchFileEvent(XArchFileEvent evt){
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt){
	}

	public void handleDetachEvent(XArchDetachEvent evt){
		fireExplicitADTEvent(ExplicitEventType.UPDATED_EXPLICIT_OBJREF, xarch.getXArch(evt.getObjRef()), null, evt.getObjRef());
	}
}
