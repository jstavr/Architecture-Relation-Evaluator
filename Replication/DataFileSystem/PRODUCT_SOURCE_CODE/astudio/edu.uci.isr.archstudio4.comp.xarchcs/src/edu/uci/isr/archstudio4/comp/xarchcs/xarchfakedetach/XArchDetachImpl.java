package edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach.XArchDetachEvent.DetachEventType;
import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class XArchDetachImpl
	implements XArchDetachInterface, XArchFileListener{

	static final boolean equalz(Object o1, Object o2){
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	static final String capFirstLetter(String s){
		if(s == null || s.length() == 0){
			return s;
		}
		char ch = s.charAt(0);
		if(Character.isUpperCase(ch)){
			return s;
		}
		return Character.toUpperCase(ch) + s.substring(1);
	}

	XArchFlatInterface xarch;

	public XArchDetachImpl(XArchFlatInterface xarch){
		this.xarch = xarch;
	}

	Map<ObjRef, Object> unsynchronizedDetachedRefs = new HashMap<ObjRef, Object>();
	Map<ObjRef, Object> detachedRefs = Collections.synchronizedMap(unsynchronizedDetachedRefs);

	public boolean isDetached(ObjRef objRef){
		return detachedRefs.containsKey(objRef);
	}

	public boolean isAttached(ObjRef objRef){
		ObjRef[] ancestorRefs = xarch.getAllAncestors(objRef);
		for(ObjRef ancestorRef: ancestorRefs){
			if(isDetached(ancestorRef)){
				return false;
			}
		}
		return true;
	}

	public Object getDetachedReason(ObjRef objRef){
		return detachedRefs.get(objRef);
	}

	public Object[] getDetachedReasons(ObjRef[] objRefs){
		synchronized(detachedRefs){
			Object[] reasons = new Object[objRefs.length];
			for(int i = 0; i < objRefs.length; i++){
				reasons[i] = unsynchronizedDetachedRefs.get(objRefs[i]);
			}
			return reasons;
		}
	}

	public void detach(ObjRef thingToDetachRef, Object reason){
		if(reason == null){
			reason = NO_REASON;
		}
		Object oldReason = detachedRefs.put(thingToDetachRef, reason);
		if(oldReason == null){
			fireDetachEvent(DetachEventType.DETACHED, thingToDetachRef, oldReason, reason);
		}
		else if(!equalz(oldReason, reason)){
			fireDetachEvent(DetachEventType.DETACHED_UPDATED, thingToDetachRef, oldReason, reason);
		}
	}

	public void detach(ObjRef[] thingsToDetachRefs, Object reason){
		for(ObjRef thingToDetachRef: thingsToDetachRefs){
			detach(thingToDetachRef, reason);
		}
	}

	public void attach(ObjRef thingToAttachRef){
		Object oldReason = detachedRefs.remove(thingToAttachRef);
		if(!equalz(oldReason, null)){
			fireDetachEvent(DetachEventType.ATTACHED, thingToAttachRef, oldReason, null);
		}
	}

	public void add(ObjRef baseObjectRef, String typeOfThing, ObjRef thingToAddRef){
		if(detachedRefs.containsKey(thingToAddRef)){
			ObjRef parentRef = xarch.getParent(thingToAddRef);
			ObjRef xArchRef = xarch.getXArch(thingToAddRef);
			String elementName = equalz(xArchRef, parentRef) ? "Object" : xarch.getElementName(thingToAddRef);
			if(!equalz(parentRef, baseObjectRef) || !equalz(capFirstLetter(elementName), capFirstLetter(typeOfThing))){
				xarch.remove(parentRef, elementName, thingToAddRef);
				attach(thingToAddRef);
				xarch.add(baseObjectRef, typeOfThing, thingToAddRef);
			}
			else{
				attach(thingToAddRef);
			}
		}
		else{
			xarch.add(baseObjectRef, typeOfThing, thingToAddRef);
		}

		assert Arrays.asList(xarch.getAll(baseObjectRef, typeOfThing)).contains(thingToAddRef);
		assert !detachedRefs.containsKey(thingToAddRef);
	}

	public void add(ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToAddRefs){
		for(ObjRef thingToAddRef: thingsToAddRefs){
			add(baseObjectRef, typeOfThing, thingToAddRef);
		}
	}

	public void set(ObjRef baseObjectRef, String typeOfThing, ObjRef objRef){
		if(detachedRefs.containsKey(objRef)){
			ObjRef parentRef = xarch.getParent(objRef);
			ObjRef xArchRef = xarch.getXArch(objRef);
			String elementName = equalz(xArchRef, parentRef) ? "Object" : xarch.getElementName(objRef);
			if(!equalz(parentRef, baseObjectRef) || !equalz(capFirstLetter(elementName), capFirstLetter(typeOfThing))){
				xarch.clear(parentRef, elementName);
				attach(objRef);
				xarch.set(baseObjectRef, typeOfThing, objRef);
			}
			else{
				attach(objRef);
			}
		}
		else{
			xarch.set(baseObjectRef, typeOfThing, objRef);
		}

		assert equalz(xarch.get(baseObjectRef, typeOfThing), objRef);
		assert !detachedRefs.containsKey(objRef);
	}

	public void attach(ObjRef[] thingsToAttachRefs){
		for(ObjRef thingToAttachRef: thingsToAttachRefs){
			attach(thingToAttachRef);
		}
	}

	List<XArchDetachListener> detachListeners = Collections.synchronizedList(new ArrayList<XArchDetachListener>());

	public void addDetachListener(XArchDetachListener l){
		detachListeners.add(l);
	}

	public void removeDetachListener(XArchDetachListener l){
		detachListeners.remove(l);
	}

	private void fireDetachEvent(DetachEventType eventType, ObjRef objRef, Object oldReason, Object newReason){
		XArchDetachEvent evt = new XArchDetachEvent(eventType, objRef, oldReason, newReason);
		for(XArchDetachListener l: detachListeners.toArray(new XArchDetachListener[detachListeners.size()])){
			l.handleDetachEvent(evt);
		}
	}

	public void handleXArchFileEvent(XArchFileEvent evt){
		switch(evt.getEventType()){
		case XArchFileEvent.XARCH_CLOSED_EVENT:
			for(ObjRef objRef: detachedRefs.keySet().toArray(new ObjRef[0])){
				if(!xarch.isValidObjRef(objRef)){
					detachedRefs.remove(objRef);
				}
			}
		}
	}

	public String serialize(ObjRef xArchRef){
		// remove the elements that are detached before saving
		List<Runnable> restoreRunnables = new ArrayList<Runnable>(detachedRefs.size());
		for(ObjRef objRef: detachedRefs.keySet().toArray(new ObjRef[0])){
			try{
				if(xArchRef.equals(xarch.getXArch(objRef))){
					restoreRunnables.add(remove(xArchRef, objRef));
				}
			}
			catch(Exception e){
			}
		}
		try{
			return xarch.serialize(xArchRef);
		}
		finally{
			for(Runnable runnable: restoreRunnables){
				if(runnable != null){
					runnable.run();
				}
			}
		}
	}

	public void writeToFile(ObjRef xArchRef, String fileName) throws IOException{
		// remove the elements that are detached before saving
		List<Runnable> restoreRunnables = new ArrayList<Runnable>(detachedRefs.size());
		for(ObjRef objRef: detachedRefs.keySet().toArray(new ObjRef[0])){
			try{
				if(xArchRef.equals(xarch.getXArch(objRef))){
					restoreRunnables.add(remove(xArchRef, objRef));
				}
			}
			catch(Exception e){
			}
		}
		try{
			xarch.writeToFile(xArchRef, fileName);
		}
		finally{
			for(Runnable runnable: restoreRunnables){
				if(runnable != null){
					runnable.run();
				}
			}
		}
	}

	public void writeToURL(ObjRef xArchRef, String fileName) throws IOException{
		// remove the elements that are detached before saving
		List<Runnable> restoreRunnables = new ArrayList<Runnable>(detachedRefs.size());
		for(ObjRef objRef: detachedRefs.keySet().toArray(new ObjRef[0])){
			try{
				if(xArchRef.equals(xarch.getXArch(objRef))){
					restoreRunnables.add(remove(xArchRef, objRef));
				}
			}
			catch(Exception e){
			}
		}
		try{
			xarch.writeToURL(xArchRef, fileName);
		}
		finally{
			for(Runnable runnable: restoreRunnables){
				if(runnable != null){
					runnable.run();
				}
			}
		}
	}

	private Runnable remove(ObjRef xArchRef, final ObjRef objRef){
		final ObjRef parentRef = xarch.getParent(objRef);
		if(parentRef != null){
			final String elementName = equalz(xArchRef, parentRef) ? "Object" : xarch.getElementName(objRef);
			final Object reason = detachedRefs.get(objRef);
			if(reason != null){
				attach(objRef);
				IXArchTypeMetadata type = xarch.getTypeMetadata(parentRef);
				IXArchPropertyMetadata prop = type.getProperty(elementName);
				if(prop.getMaxOccurs() > 1){
					xarch.remove(parentRef, elementName, objRef);
					return new Runnable(){

						public void run(){
							xarch.add(parentRef, elementName, objRef);
							detach(objRef, reason);
						}
					};
				}
				else{
					xarch.clear(parentRef, elementName);
					return new Runnable(){

						public void run(){
							xarch.set(parentRef, elementName, objRef);
							detach(objRef, reason);
						}
					};
				}
			}
		}
		return new Runnable(){

			public void run(){
				// do nothing
			}
		};
	}
}
