package edu.uci.isr.archstudio4.comp.xarchcs.changesetsync;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchfakedetach.XArchDetachInterface;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ChangeSetSyncMyxComponent extends AbstractMyxSimpleBrick{

	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_IN_FILEEVENTS = MyxUtils.createName("xarchfileevents");
	public static final IMyxName INTERFACE_NAME_IN_FLATEVENTS = MyxUtils.createName("xarchflatevents");
	public static final IMyxName INTERFACE_NAME_OUT_XARCHFD = MyxUtils.createName("xarch-fd");

	public static final IMyxName INTERFACE_NAME_OUT_CHANGE_SET_ADT = MyxUtils.createName("csadt");

	public static final IMyxName INTERFACE_NAME_IN_CHANGE_SET_SYNC = MyxUtils.createName("cssync");

	XArchFlatInterface xarch;

	XArchDetachInterface xarchfd;

	IChangeSetADT csadt;

	ChangeSetSyncImpl cssync;

	public Object getServiceObject(IMyxName interfaceName){
		if(cssync == null){
			xarch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_XARCH);
			xarchfd = (XArchDetachInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_XARCHFD);
			csadt = (IChangeSetADT)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_CHANGE_SET_ADT);
			cssync = new ChangeSetSyncImpl(xarch, xarchfd, csadt);
		}
		if(interfaceName.equals(INTERFACE_NAME_IN_CHANGE_SET_SYNC)){
			return cssync;
		}
		if(interfaceName.equals(INTERFACE_NAME_IN_FILEEVENTS)){
			return cssync;
		}
		if(interfaceName.equals(INTERFACE_NAME_IN_FLATEVENTS)){
			return cssync;
		}
		return null;
	}
}
