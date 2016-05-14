package edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.resolvers;

import java.util.HashSet;
import java.util.Set;

import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;

public class NoResolver extends AbstractResolver{

	public NoResolver(){
		super("None:");
	}

	@Override
	public boolean canResolve(XArchFlatQueryInterface xArch, ObjRef objRef){
		return true;
	}

	@Override
	protected String getReferenceData(XArchFlatQueryInterface xArch, ObjRef objRef){
		warn(xArch, objRef);
		return "";
	}

	static Set<String> warnedTypes = new HashSet<String>();
	
	static void warn(XArchFlatQueryInterface xArch, ObjRef objRef){
		IXArchTypeMetadata type = xArch.getTypeMetadata(objRef);
		if(warnedTypes.add(type.getType())){
			System.err.println("Warning: No resolver is available for type <" + type.getType() + "> (ex: <" + objRef + ">)");
		}
	}
}
