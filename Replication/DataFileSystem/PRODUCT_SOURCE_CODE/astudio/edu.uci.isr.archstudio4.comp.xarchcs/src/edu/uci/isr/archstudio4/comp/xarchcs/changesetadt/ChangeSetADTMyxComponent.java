package edu.uci.isr.archstudio4.comp.xarchcs.changesetadt;

import edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach.XArchDetachInterface;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ChangeSetADTMyxComponent
	extends AbstractMyxSimpleBrick{

	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_IN_FILEEVENTS = MyxUtils.createName("xarchfileevents");
	public static final IMyxName INTERFACE_NAME_IN_FLATEVENTS = MyxUtils.createName("xarchflatevents");

	public static final IMyxName INTERFACE_NAME_OUT_XARCHD = MyxUtils.createName("xarchd");

	public static final IMyxName INTERFACE_NAME_IN_CHANGE_SET_ADT = MyxUtils.createName("csadt");

	XArchFlatInterface xarch;
	XArchDetachInterface xarchd;
	ChangeSetADTImpl csadt;

	public Object getServiceObject(IMyxName interfaceName){
		if(csadt == null){
			xarch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_XARCH);
			xarchd = (XArchDetachInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_XARCHD);
			csadt = new ChangeSetADTImpl(xarch, xarchd);
		}
		if(interfaceName.equals(INTERFACE_NAME_IN_CHANGE_SET_ADT)){
			return csadt;
		}
		if(interfaceName.equals(INTERFACE_NAME_IN_FILEEVENTS)){
			return csadt;
		}
		if(interfaceName.equals(INTERFACE_NAME_IN_FLATEVENTS)){
			return csadt;
		}
		return null;
	}

}
