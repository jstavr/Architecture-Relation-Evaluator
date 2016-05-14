package edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.resolvers;

import java.util.Arrays;

import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;

public class IDResolver extends AbstractResolver{

	public IDResolver(){
		super("ID:");
	}

	@Override
	public ObjRef resolveObjRef(XArchFlatQueryInterface xarch, String reference, ObjRef parentRef, ObjRef[] childRefs){
		String id = decode(reference);
		ObjRef xarchRef = xarch.getXArch(parentRef);
		if(id != null){
			ObjRef resolvedRef = xarch.getByID(xarchRef, id);
			if(Arrays.asList(childRefs).contains(resolvedRef))
				return resolvedRef;
		}
		for(ObjRef childRef: childRefs){
			try{
				if(xarch.has(childRef, "id", id))
					return childRef;
			}
			catch(Exception e){
				// not all children may have ID's, just ignore them if they
				// don't
			}
		}
		return null;
	}

	@Override
	public boolean canResolve(XArchFlatQueryInterface xarch, ObjRef objRef){
		IXArchTypeMetadata type = xarch.getTypeMetadata(objRef);
		IXArchPropertyMetadata prop = type.getProperty("id");
		return prop != null && xarch.isAssignable("instance#Identifier", prop.getType());
	}

	@Override
	protected String getReferenceData(XArchFlatQueryInterface xarch, ObjRef objRef){
		return (String)xarch.get(objRef, "id");
	}
}
