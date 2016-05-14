package edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.resolvers;

import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;

public class ElementTypeResolver extends AbstractResolver{

	protected static String capFirstLetter(String s){
		if(s == null || s.length() == 0){
			return s;
		}
		char ch = s.charAt(0);
		if(Character.isUpperCase(ch)){
			return s;
		}
		return Character.toUpperCase(ch) + s.substring(1);
	}

	private static final boolean equalz(Object o1, Object o2){
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	public ElementTypeResolver(){
		super("ElementType:");
	}

	@Override
	public boolean canResolve(XArchFlatQueryInterface xarch, ObjRef objRef){
		ObjRef parentRef = xarch.getParent(objRef);
		return parentRef == null ? false : xarch.isInstanceOf(parentRef, "#XArch");
	}

	@Override
	protected String getReferenceData(XArchFlatQueryInterface xarch, ObjRef objRef){
		IXArchTypeMetadata type = xarch.getTypeMetadata(objRef);
		if(xarch.isAssignable("changesets#archChangeSets", type.getType())){
			return null;
		}
		return xarch.getTypeMetadata(objRef).getType();
	}
}
