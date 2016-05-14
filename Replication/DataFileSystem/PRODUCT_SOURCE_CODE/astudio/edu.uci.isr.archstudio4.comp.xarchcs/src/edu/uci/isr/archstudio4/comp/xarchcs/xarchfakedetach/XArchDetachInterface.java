package edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach;

import java.io.IOException;

import edu.uci.isr.xarchflat.ObjRef;

public interface XArchDetachInterface{

	public static final Object NO_REASON = new Object();

	public boolean isDetached(ObjRef objRef);

	public boolean isAttached(ObjRef objRef);

	public Object getDetachedReason(ObjRef objRef);

	public Object[] getDetachedReasons(ObjRef[] objRefs);

	public void detach(ObjRef thingToDetachRef, Object reason);

	public void detach(ObjRef[] thingsToDetachRefs, Object reason);

	public void attach(ObjRef thingToAttachRef);

	public void attach(ObjRef[] thingsToAttachRefs);

	public void add(ObjRef baseObjectRef, String typeOfThing, ObjRef thingToAddRef);

	public void add(ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToAddRefs);

	public void set(ObjRef baseObjectRef, String typeOfThing, ObjRef valueRef);

	public String serialize(ObjRef archRef);

	public void writeToFile(ObjRef archRef, String fileName) throws IOException;

	public void writeToURL(ObjRef archRef, String fileName) throws IOException;
}
