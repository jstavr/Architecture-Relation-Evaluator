package edu.uci.isr.archstudio4.comp.booleannotation;

import java.util.Properties;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class BooleanNotationMyxComponent extends AbstractMyxSimpleBrick 
implements IMyxDynamicBrick{

	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_IN_BOOLEANNOTATION = MyxUtils.createName("booleannotation");
	
	protected XArchFlatInterface xarch = null;
	protected BooleanNotationImpl booleanNotationImpl = null;
	
	public BooleanNotationMyxComponent(){
	}
	
	public void init(){
		booleanNotationImpl = new BooleanNotationImpl();
	}

	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			xarch = (XArchFlatInterface)serviceObject;
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			xarch = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){}
	
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_BOOLEANNOTATION)){
			return booleanNotationImpl;
		}
		return null;
	}
	
	public class BooleanNotationImpl implements IBooleanNotation{
		public String booleanGuardToString(ObjRef optionalRef) {
			return BooleanGuardConverter.booleanGuardToString(xarch, optionalRef);
		}
		
		public ObjRef parseBooleanGuard(String expression, ObjRef xArchRef)
		throws ParseException, TokenMgrError{
			return BooleanGuardConverter.parseBooleanGuard(xarch, expression, xArchRef);
		}
	}

}
