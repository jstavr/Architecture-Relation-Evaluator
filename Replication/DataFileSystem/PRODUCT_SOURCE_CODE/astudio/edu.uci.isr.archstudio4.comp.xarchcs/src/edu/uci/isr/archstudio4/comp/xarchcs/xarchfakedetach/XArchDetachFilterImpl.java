package edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatImplDelegate;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;
import edu.uci.isr.xarchflat.XArchPath;

public class XArchDetachFilterImpl
	extends XArchFlatImplDelegate
	implements XArchFlatListener, XArchDetachListener{

	protected XArchDetachInterface xarchd;

	public XArchDetachFilterImpl(XArchFlatInterface xarch, XArchDetachInterface xarchd){
		super(xarch);
		this.xarchd = xarchd;
	}

	List<XArchFlatListener> xArchFlatListeners = Collections.synchronizedList(new ArrayList<XArchFlatListener>());

	public synchronized void addXArchFlatListener(XArchFlatListener xArchFlatListener){
		xArchFlatListeners.add(xArchFlatListener);
	}

	synchronized void fireXArchFlatEvent(XArchFlatEvent evt){
		for(XArchFlatListener xArchFlatListener: xArchFlatListeners){
			xArchFlatListener.handleXArchFlatEvent(evt);
		}
	}

	protected boolean isDetached(ObjRef objRef, Object reason){
		return reason != null;
	}

	boolean isDetached(ObjRef objRef){
		return isDetached(objRef, xarchd.getDetachedReason(objRef));
	}

	boolean wasDetached(XArchDetachEvent evt){
		switch(evt.getEventType()){
		case DETACHED:
			return false;
		case ATTACHED:
		case DETACHED_UPDATED:
			return isDetached(evt.getObjRef(), evt.getOldReason());
		default:
			throw new IllegalArgumentException();
		}
	}

	ObjRef filterObjRef(ObjRef objRef){
		if(isDetached(objRef)){
			return null;
		}
		return objRef;
	}

	ObjRef[] filterObjRefs(ObjRef[] objRefs){
		List<ObjRef> results = new ArrayList<ObjRef>(objRefs.length);
		for(ObjRef objRef: objRefs){
			if(!isDetached(objRef)){
				results.add(objRef);
			}
		}
		return results.toArray(new ObjRef[results.size()]);
	}

	ObjRef[] filterAncestors(ObjRef[] ancestorRefs){
		int endIndex = 1;
		while(endIndex < ancestorRefs.length && !isDetached(ancestorRefs[endIndex])){
			endIndex++;
		}
		if(endIndex == ancestorRefs.length){
			return ancestorRefs;
		}
		ObjRef[] newAncestorRefs = new ObjRef[endIndex];
		System.arraycopy(ancestorRefs, 0, newAncestorRefs, 0, endIndex);
		return newAncestorRefs;
	}

	XArchPath filterPath(XArchPath xArchPath, ObjRef objRef){
		int startIndex = xArchPath.getLength() - 1;
		while(startIndex > 0 && objRef != null && !isDetached(objRef)){
			startIndex--;
			objRef = xarch.getParent(objRef);
		}
		return xArchPath.subpath(startIndex);
	}

	XArchFlatEvent filterFlat(XArchFlatEvent evt){
		ObjRef[] oldSourceAncestors = evt.getSourceAncestors();
		ObjRef[] newSourceAncestors = filterAncestors(oldSourceAncestors);
		int startIndex = oldSourceAncestors.length - newSourceAncestors.length;
		if(startIndex == 0){
			return evt;
		}
		XArchPath newSourcePath = evt.getSourcePath().subpath(startIndex);
		XArchPath newTargetPath = null;
		if(evt.getTarget() instanceof ObjRef){
			newTargetPath = filterPath(evt.getTargetPath(), (ObjRef)evt.getTarget());
		}
		XArchPath sourceTargetPath = evt.getSourceTargetPath().subpath(startIndex);

		return new XArchFlatEvent(evt.getXArchRef(), evt.getSource(), newSourceAncestors, newSourcePath, evt.getEventType(), evt.getSourceType(), evt.getTargetName(), evt.getTarget(), newTargetPath, false, evt.getIsExtra(), sourceTargetPath);
	}

	@Override
	public ObjRef get(ObjRef baseObjectRef, String typeOfThing, String id){
		return filterObjRef(xarch.get(baseObjectRef, typeOfThing, id));
	}

	@Override
	public ObjRef[] get(ObjRef baseObjectRef, String typeOfThing, String[] ids){
		return filterObjRefs(xarch.get(baseObjectRef, typeOfThing, ids));
	}

	@Override
	public Object get(ObjRef baseObjectRef, String typeOfThing){
		Object object = xarch.get(baseObjectRef, typeOfThing);
		return object instanceof ObjRef ? filterObjRef((ObjRef)object) : object;
	}

	@Override
	public ObjRef[] getAll(ObjRef baseObjectRef, String typeOfThing){
		return filterObjRefs(xarch.getAll(baseObjectRef, typeOfThing));
	}

	@Override
	public ObjRef[] getAllAncestors(ObjRef targetObjectRef){
		return filterAncestors(xarch.getAllAncestors(targetObjectRef));
	}

	@Override
	public ObjRef[] getAllElements(ObjRef contextObjectRef, String typeOfThing, ObjRef archObjectRef){
		return filterObjRefs(xarch.getAllElements(contextObjectRef, typeOfThing, archObjectRef));
	}

	@Override
	public ObjRef getByID(ObjRef archRef, String id){
		ObjRef resultRef = xarch.getByID(archRef, id);
		if(resultRef != null && isAttached(resultRef)){
			return resultRef;
		}
		return null;
	}

	@Override
	public ObjRef getByID(String id){
		ObjRef resultRef = xarch.getByID(id);
		if(resultRef != null && isAttached(resultRef)){
			return resultRef;
		}
		return null;
	}

	@Override
	public ObjRef getElement(ObjRef contextObjectRef, String typeOfThing, ObjRef archObjectRef){
		ObjRef[] elementRefs = xarch.getAllElements(contextObjectRef, typeOfThing, archObjectRef);
		elementRefs = filterObjRefs(elementRefs);
		if(elementRefs.length > 0){
			return elementRefs[0];
		}
		return null;
	}

	@Override
	public ObjRef getParent(ObjRef targetObjectRef){
		return isDetached(targetObjectRef) ? null : xarch.getParent(targetObjectRef);
	}

	@Override
	public ObjRef[] getReferences(ObjRef archRef, String id){
		ObjRef objRef = xarch.getByID(archRef, id);
		if(objRef == null || isDetached(objRef)){
			return new ObjRef[0];
		}
		return filterObjRefs(xarch.getReferences(archRef, id));
	}

	@Override
	public XArchPath getXArchPath(ObjRef ref){
		return filterPath(xarch.getXArchPath(ref), ref);
	}

	@Override
	public boolean has(ObjRef baseObjectRef, String typeOfThing, ObjRef valueToCheck){
		return isDetached(valueToCheck) ? false : xarch.has(baseObjectRef, typeOfThing, valueToCheck);
	}

	@Override
	public boolean[] has(ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToCheck){
		boolean[] results = xarch.has(baseObjectRef, typeOfThing, thingsToCheck);
		for(int i = 0; i < results.length; i++){
			if(results[i]){
				if(isDetached(thingsToCheck[i])){
					results[i] = false;
				}
			}
		}
		return results;
	}

	@Override
	public boolean has(ObjRef baseObjectRef, String typeOfThing, String valueToCheck){
		return xarch.has(baseObjectRef, typeOfThing, valueToCheck);
	}

	@Override
	public boolean hasAll(ObjRef baseObjectRef, String typeOfThing, ObjRef[] valuesToCheck){
		for(ObjRef valueRef: valuesToCheck){
			if(isDetached(valueRef)){
				return false;
			}
		}
		return xarch.hasAll(baseObjectRef, typeOfThing, valuesToCheck);
	}

	@Override
	public boolean hasAncestor(ObjRef childRef, ObjRef ancestorRef){
		ObjRef[] allAncestors = xarch.getAllAncestors(childRef);
		int j = 0;
		while(j < allAncestors.length && !ancestorRef.equals(allAncestors[j])){
			j++;
		}
		if(j == allAncestors.length){
			return false;
		}
		for(int i = 0; i < j; i++){
			if(isDetached(allAncestors[i])){
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isAttached(ObjRef objRef){
		if(!xarch.isAttached(objRef)){
			return false;
		}
		ObjRef[] allAncestors = xarch.getAllAncestors(objRef);
		Object[] detachedReasons = xarchd.getDetachedReasons(allAncestors);
		for(int i = 0; i < allAncestors.length; i++){
			if(isDetached(allAncestors[i], detachedReasons[i])){
				return false;
			}
		}
		return true;
	}

	@Override
	public ObjRef resolveHref(ObjRef archRef, String href){
		return filterObjRef(xarch.resolveHref(archRef, href));
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt){
		switch(evt.getEventType()){
		case XArchFlatEvent.REMOVE_EVENT:
			if(isDetached((ObjRef)evt.getTarget())){
				/*
				 * Do not send the event, the objref was already detached.
				 */
				return;
			}
			fireXArchFlatEvent(filterFlat(evt));
			break;
		case XArchFlatEvent.CLEAR_EVENT:
			Object target = evt.getTarget();
			if(target instanceof ObjRef && isDetached((ObjRef)target)){
				/*
				 * Do not send the event, the objref was already detached.
				 */
				return;
			}
			fireXArchFlatEvent(filterFlat(evt));
			break;
		default:
			fireXArchFlatEvent(filterFlat(evt));
			break;
		}
	}

	public void handleDetachEvent(XArchDetachEvent evt){
		ObjRef targetRef = evt.getObjRef();
		boolean isDetached = isDetached(targetRef);
		boolean wasDetached = wasDetached(evt);
		if(wasDetached && !isDetached){
			ObjRef xArchRef = xarch.getXArch(targetRef);
			ObjRef srcRef = xarch.getParent(targetRef);
			if(srcRef == null){
				// in reality it is detached now, so we just ignore the event
				return;
			}
			ObjRef[] srcAncestors = getAllAncestors(srcRef);
			String targetName = getElementName(targetRef);
			XArchPath allPath = xarch.getXArchPath(targetRef);
			XArchPath srcPath = allPath.subpath(0, allPath.getLength() - 1);
			XArchPath targetPath = allPath;
			boolean isAttached = isAttached(srcRef);
			boolean isExtra = false;
			int srcType = XArchFlatEvent.ELEMENT_CHANGED;
			int eventType;
			String actualName = xArchRef.equals(srcRef) ? "Object" : targetName;
			switch(xarch.getTypeMetadata(srcRef).getProperty(actualName).getMetadataType()){
			case IXArchPropertyMetadata.ATTRIBUTE:
				throw new IllegalArgumentException(); // this shouldn't happen
			case IXArchPropertyMetadata.ELEMENT:
				eventType = XArchFlatEvent.SET_EVENT;
				fireXArchFlatEvent(new XArchFlatEvent(xArchRef, srcRef, srcAncestors, srcPath, eventType, srcType, targetName, targetRef, targetPath, isAttached, isExtra, allPath));
				break;
			case IXArchPropertyMetadata.ELEMENT_MANY:
				eventType = XArchFlatEvent.ADD_EVENT;
				fireXArchFlatEvent(new XArchFlatEvent(xArchRef, srcRef, srcAncestors, srcPath, eventType, srcType, targetName, targetRef, targetPath, isAttached, isExtra, allPath));
				break;
			}
		}
		else if(!wasDetached && isDetached){
			ObjRef xArchRef = xarch.getXArch(targetRef);
			ObjRef srcRef = xarch.getParent(targetRef);
			if(srcRef == null){
				// in reality it was detached before, so we just ignore the event
				return;
			}
			ObjRef[] srcAncestors = getAllAncestors(srcRef);
			String targetName = getElementName(targetRef);
			XArchPath allPath = xarch.getXArchPath(targetRef);
			XArchPath srcPath = allPath.subpath(0, allPath.getLength() - 1);
			XArchPath targetPath = allPath.subpath(allPath.getLength() - 1);
			boolean isAttached = isAttached(srcRef);
			boolean isExtra = false;
			int srcType = XArchFlatEvent.ELEMENT_CHANGED;
			int eventType;
			String actualName = xArchRef.equals(srcRef) ? "Object" : targetName;
			switch(xarch.getTypeMetadata(srcRef).getProperty(actualName).getMetadataType()){
			case IXArchPropertyMetadata.ATTRIBUTE:
				throw new IllegalArgumentException(); // this shouldn't happen
			case IXArchPropertyMetadata.ELEMENT:
				eventType = XArchFlatEvent.CLEAR_EVENT;
				fireXArchFlatEvent(new XArchFlatEvent(xArchRef, srcRef, srcAncestors, srcPath, eventType, srcType, targetName, targetRef, targetPath, isAttached, isExtra, allPath));
				break;
			case IXArchPropertyMetadata.ELEMENT_MANY:
				eventType = XArchFlatEvent.REMOVE_EVENT;
				fireXArchFlatEvent(new XArchFlatEvent(xArchRef, srcRef, srcAncestors, srcPath, eventType, srcType, targetName, targetRef, targetPath, isAttached, isExtra, allPath));
				break;
			}
		}
	}
}
