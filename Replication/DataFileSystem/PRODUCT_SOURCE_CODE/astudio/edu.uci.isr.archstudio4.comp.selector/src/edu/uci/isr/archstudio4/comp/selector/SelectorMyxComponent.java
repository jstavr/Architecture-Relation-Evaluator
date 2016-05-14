package edu.uci.isr.archstudio4.comp.selector;

import java.util.Properties;

import edu.uci.isr.archstudio4.comp.booleaneval.IBooleanEval;
import edu.uci.isr.archstudio4.comp.booleaneval.MissingElementException;
import edu.uci.isr.archstudio4.comp.booleaneval.NoSuchTypeException;
import edu.uci.isr.archstudio4.comp.booleaneval.SymbolTable;
import edu.uci.isr.archstudio4.comp.booleaneval.TypeMismatchException;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class SelectorMyxComponent extends AbstractMyxSimpleBrick 
implements IMyxDynamicBrick, ISelector{

	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_OUT_BOOLEANEVAL = MyxUtils.createName("booleaneval");
	public static final IMyxName INTERFACE_NAME_IN_SELECTOR = MyxUtils.createName("selector");
	
	protected XArchFlatInterface xarch = null;
	protected IBooleanEval booleanEval = null;
	
	public SelectorMyxComponent(){
	}
	
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			xarch = (XArchFlatInterface)serviceObject;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_BOOLEANEVAL)){
			booleanEval = (IBooleanEval)serviceObject;
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			xarch = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_BOOLEANEVAL)){
			booleanEval = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){}
	
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_SELECTOR)){
			return this;
		}
		return null;
	}
	
	public void select(String archURL, String newArchURL, SymbolTable table, String startingID, boolean isStructural) throws InvalidURIException, MissingElementException, NoSuchTypeException, TypeMismatchException, VariantEvaluationException, BrokenLinkException{
		SelectorImpl selectorImpl = new SelectorImpl(xarch, booleanEval);
		selectorImpl.select(archURL, newArchURL, table, startingID, isStructural);
	}
	
}
