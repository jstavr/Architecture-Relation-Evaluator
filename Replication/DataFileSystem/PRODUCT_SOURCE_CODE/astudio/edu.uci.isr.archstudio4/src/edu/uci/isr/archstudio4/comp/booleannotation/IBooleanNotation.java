package edu.uci.isr.archstudio4.comp.booleannotation;


import edu.uci.isr.xarchflat.ObjRef;

public interface IBooleanNotation {

	public ObjRef parseBooleanGuard(String expression, ObjRef xArchRef)
		throws ParseException, TokenMgrError;
	public String booleanGuardToString(ObjRef optionalRef);
	
}
