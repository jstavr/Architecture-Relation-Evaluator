package edu.uci.isr.archstudio4.comp.archipelago.options;

import edu.uci.isr.xarchflat.InvalidOperationException;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class OptionsUtils{
	private OptionsUtils(){}
	
	public static boolean isOptional(XArchFlatInterface xarch, ObjRef ref){
		try{
			ObjRef optionalRef = (ObjRef)xarch.get(ref, "optional");
			if(optionalRef != null){
				ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
				if(guardRef != null){
					return true;
				}
			}
		}
		catch(Exception ioe){}
		return false;
	}

}
