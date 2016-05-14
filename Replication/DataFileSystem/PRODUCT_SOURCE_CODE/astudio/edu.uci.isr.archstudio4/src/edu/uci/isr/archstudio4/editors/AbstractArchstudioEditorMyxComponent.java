package edu.uci.isr.archstudio4.editors;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.uci.isr.archstudio4.comp.fileman.IFileManagerListener;
import edu.uci.isr.archstudio4.comp.fileman.IFileManager;
import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTListener;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.launcher.ILaunchData;
import edu.uci.isr.archstudio4.launcher.ILaunchable;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;

public abstract class AbstractArchstudioEditorMyxComponent extends AbstractMyxSimpleBrick
	implements XArchFileListener, XArchFlatListener, FocusEditorListener, IFileManagerListener, ILaunchable, ExplicitADTListener{

	public static final IMyxName INTERFACE_NAME_OUT_RESOURCES = MyxUtils.createName("resources");
	public static final IMyxName INTERFACE_NAME_OUT_EDITORMANAGER = MyxUtils.createName("editormanager");
	public static final IMyxName INTERFACE_NAME_OUT_FILEMANAGER = MyxUtils.createName("filemanager");
	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_IN_LAUNCHER = MyxUtils.createName("launcher");
	public static final IMyxName INTERFACE_NAME_IN_FLATEVENTS = MyxUtils.createName("xarchflatevents");
	public static final IMyxName INTERFACE_NAME_IN_FILEEVENTS = MyxUtils.createName("xarchfileevents");
	public static final IMyxName INTERFACE_NAME_IN_FOCUSEDITOREVENTS = MyxUtils.createName("focuseditorevents");
	public static final IMyxName INTERFACE_NAME_IN_FILEMANAGEREVENTS = MyxUtils.createName("filemanagerevents");
	public static final IMyxName INTERFACE_NAME_EXPLICITADT = MyxUtils.createName("explicitadt");
	public static final IMyxName INTERFACE_NAME_EXPLICITEVENTS = MyxUtils.createName("explicitevents");

	protected MyxRegistry er = MyxRegistry.getSharedInstance();
	protected XArchFlatInterface xarch = null;
	protected IFileManager fileman = null;
	protected IEditorManager editorManager = null;
	protected IResources resources = null;
	protected IExplicitADT explicit;

	protected String editorName = null;
	protected String eclipseEditorID = null;
	protected boolean registerWithEditorManager = false;
	protected boolean handleUnattachedXArchFlatEvents = false;

	protected ILaunchData launchData = null;

	public AbstractArchstudioEditorMyxComponent(String editorName, String eclipseEditorID, boolean registerWithEditorManager){
		super();
		this.editorName = editorName;
		this.eclipseEditorID = eclipseEditorID;
		this.registerWithEditorManager = registerWithEditorManager;
	}

	@Override
	public void begin(){
		xarch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_XARCH);
		fileman = (IFileManager)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_FILEMANAGER);
		editorManager = (IEditorManager)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_EDITORMANAGER);
		resources = (IResources)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_RESOURCES);
		explicit = (IExplicitADT)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_EXPLICITADT);

		if(registerWithEditorManager){
			editorManager.registerEditor(editorName, null);
		}
		er.register(this);
	}

	@Override
	public void end(){
		er.unregister(this);
		if(registerWithEditorManager){
			editorManager.unregisterEditor(editorName);
		}
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_FILEEVENTS)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_IN_FLATEVENTS)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_IN_FOCUSEDITOREVENTS)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_IN_FILEMANAGEREVENTS)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_IN_LAUNCHER)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_EXPLICITEVENTS)){
			return this;
		}
		return null;
	}

	public IEditorManager getEditorManager(){
		return editorManager;
	}

	public IFileManager getFileManager(){
		return fileman;
	}

	public IResources getResources(){
		return resources;
	}

	public XArchFlatInterface getXArchADT(){
		return xarch;
	}

	public IExplicitADT getExplicit(){
		return explicit;
	}

	public void handleXArchFileEvent(XArchFileEvent evt){
		Object[] os = er.getObjects(this);
		for(Object element: os){
			if(element instanceof XArchFileListener){
				try{
					((XArchFileListener)element).handleXArchFileEvent(evt);
				}
				catch(Throwable t){
					t.printStackTrace();
				}
			}
		}
	}

	public void setHandleUnattachedXArchFlatEvents(boolean handle){
		this.handleUnattachedXArchFlatEvents = handle;
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt){
		if(!handleUnattachedXArchFlatEvents && !evt.getIsAttached()){
			return;
		}
		Object[] os = er.getObjects(this);
		for(Object element: os){
			if(element instanceof XArchFlatListener){
				try{
					((XArchFlatListener)element).handleXArchFlatEvent(evt);
				}
				catch(Throwable t){
					t.printStackTrace();
				}
			}
		}
	}

	public void focusEditor(String editorName, ObjRef[] refs){
		if(editorName != null && editorName.equals(this.editorName)){
			if(refs.length == 1){
				ObjRef ref = refs[0];
				edu.uci.isr.archstudio4.util.EclipseUtils.focusEditor(xarch, ref, eclipseEditorID, editorName);
			}
		}
	}

	public void fileDirtyStateChanged(ObjRef xArchRef, boolean dirty){
		Object[] os = er.getObjects(this);
		for(Object element: os){
			if(element instanceof IFileManagerListener){
				try{
					((IFileManagerListener)element).fileDirtyStateChanged(xArchRef, dirty);
				}
				catch(Throwable t){
					t.printStackTrace();
				}
			}
		}
	}

	public void fileSaving(ObjRef xArchRef, IProgressMonitor monitor){
		Object[] os = er.getObjects(this);
		for(Object element: os){
			if(element instanceof IFileManagerListener){
				try{
					((IFileManagerListener)element).fileSaving(xArchRef, monitor);
				}
				catch(Throwable t){
					t.printStackTrace();
				}
			}
		}
	}

	public void handleExplicitEvent(ExplicitADTEvent evt){
		Object[] os = er.getObjects(this);
		for(Object element: os){
			if(element instanceof ExplicitADTListener){
				try{
					((ExplicitADTListener)element).handleExplicitEvent(evt);
				}
				catch(Throwable t){
					t.printStackTrace();
				}
			}
		}
	}
}
