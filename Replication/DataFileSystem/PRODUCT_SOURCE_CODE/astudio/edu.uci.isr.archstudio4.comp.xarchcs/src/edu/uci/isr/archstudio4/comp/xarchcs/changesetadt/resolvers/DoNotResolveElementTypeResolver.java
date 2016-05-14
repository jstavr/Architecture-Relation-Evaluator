package edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.resolvers;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IObjRefResolver;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;

public class DoNotResolveElementTypeResolver implements IObjRefResolver {

	protected final IXArchTypeMetadata type;

	public DoNotResolveElementTypeResolver(XArchFlatQueryInterface xarch, String type) {
		this.type = xarch.getTypeMetadata(type);
	}

	public boolean canResolve(String reference) {
		return false;
	}

	public boolean canResolve(XArchFlatQueryInterface xarch, ObjRef objRef) {
		ObjRef parentRef = xarch.getParent(objRef);
		if (parentRef != null) {
			return xarch.isInstanceOf(parentRef, "#XArch") && xarch.isInstanceOf(objRef, type.getType());
		}
		return false;
	}

	public String getReference(XArchFlatQueryInterface xarch, ObjRef objRef) {
		return null;
	}

	public ObjRef resolveObjRef(XArchFlatQueryInterface xarch, String reference, ObjRef parentRef, ObjRef[] childRefs) {
		return null;
	}

	public boolean isOldReference(XArchFlatQueryInterface xarch, ObjRef objRef, String oldReference) {
		return false;
	}
}
