package edu.uci.isr.archstudio4.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import edu.uci.isr.xarchflat.ObjRef;

public class HasObjRefUtil {
    public static ObjRef[] getObjRefs(ISelection s){
		List<ObjRef> objRefs = new ArrayList<ObjRef>();
		if(s instanceof StructuredSelection){
			@SuppressWarnings("unchecked")
			Iterator<Object> i = ((StructuredSelection)s).iterator();
			while(i.hasNext()){
				Object o = i.next();
				if(o instanceof ObjRef)
					objRefs.add((ObjRef)o);
				else if (o instanceof IHasObjRef)
					objRefs.add(((IHasObjRef)o).getRef());
			}
		}
		return objRefs.toArray(new ObjRef[objRefs.size()]);
	}
}
