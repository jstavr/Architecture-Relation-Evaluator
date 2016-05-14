package edu.uci.isr.archstudio4.comp.xarchadt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.IContentStore;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatImpl;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;
import edu.uci.isr.xarchflat.utils.XArchFlatInterfaceSynchronizer;

public class XArchADTMyxComponent
    extends AbstractMyxSimpleBrick
    implements XArchFlatListener, XArchFileListener, IMyxDynamicBrick{

	public static final IMyxName INTERFACE_NAME_IN_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_OUT_FILEEVENTS = MyxUtils.createName("xarchfileevents");
	public static final IMyxName INTERFACE_NAME_OUT_FLATEVENTS = MyxUtils.createName("xarchflatevents");
	public static final IMyxName INTERFACE_NAME_OUT_CONTENT_STORE = MyxUtils.createName("contentstore");

	protected XArchFlatImpl xarchimpl = null;
	protected XArchFlatInterface xarch = null;
	protected Collection<XArchFlatListener> flatListeners = Collections.synchronizedCollection(new ArrayList<XArchFlatListener>());
	protected Collection<XArchFileListener> fileListeners = Collections.synchronizedCollection(new ArrayList<XArchFileListener>());

	public XArchADTMyxComponent(){
		this.xarchimpl = new XArchFlatImpl();
		this.xarch = new XArchFlatInterfaceSynchronizer(xarchimpl);
	}

	@Override
	public void init(){
		xarchimpl.setEmitUnattachedEvents(false);
		xarchimpl.addXArchFileListener(this);
		xarchimpl.addXArchFlatListener(this);
	}

	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_FILEEVENTS)){
			fileListeners.add((XArchFileListener)serviceObject);
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_FLATEVENTS)){
			flatListeners.add((XArchFlatListener)serviceObject);
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_CONTENT_STORE)){
			xarchimpl.addContentStore((IContentStore)serviceObject);
		}
	}

	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_FILEEVENTS)){
			fileListeners.remove(serviceObject);
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_FLATEVENTS)){
			flatListeners.remove(serviceObject);
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_CONTENT_STORE)){
			xarchimpl.removeContentStore((IContentStore)serviceObject);
		}
	}

	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_XARCH)){
			return xarch;
		}
		return null;
	}

	public void handleXArchFileEvent(XArchFileEvent evt){
		synchronized(fileListeners){
			for(XArchFileListener fileListener: fileListeners){
				fileListener.handleXArchFileEvent(evt);
			}
		}
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt){
		synchronized(flatListeners){
			for(XArchFlatListener flatListener: flatListeners){
				flatListener.handleXArchFlatEvent(evt);
			}
		}
	}
}
