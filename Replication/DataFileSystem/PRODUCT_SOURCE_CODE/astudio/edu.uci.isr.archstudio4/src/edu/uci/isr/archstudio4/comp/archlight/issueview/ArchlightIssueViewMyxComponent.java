package edu.uci.isr.archstudio4.comp.archlight.issueview;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.uci.isr.archstudio4.comp.archlight.issueadt.ArchlightIssueADTEvent;
import edu.uci.isr.archstudio4.comp.archlight.issueadt.ArchlightIssueADTListener;
import edu.uci.isr.archstudio4.comp.archlight.issueadt.IArchlightIssueADT;
import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.editors.IEditorManager;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ArchlightIssueViewMyxComponent extends AbstractMyxSimpleBrick implements 
ArchlightIssueADTListener, IMyxDynamicBrick{

	public static final IMyxName INTERFACE_NAME_OUT_ISSUEADT = MyxUtils.createName("archlightissueadt");
	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_OUT_RESOURCES = MyxUtils.createName("resources");
	public static final IMyxName INTERFACE_NAME_OUT_PREFS = MyxUtils.createName("preferences");
	public static final IMyxName INTERFACE_NAME_OUT_EDITORMANAGER = MyxUtils.createName("editormanager");
	public static final IMyxName INTERFACE_NAME_IN_ISSUEADTEVENTS = MyxUtils.createName("archlightissueadtevents");

	private MyxRegistry er = MyxRegistry.getSharedInstance();
	
	//protected static ArchlightIssueViewMyxComponent sharedInstance = null;
	protected XArchFlatInterface xarch = null;
	protected IArchlightIssueADT issueadt = null;
	protected IResources resources = null;
	protected IEditorManager editorManager = null;
	protected IPreferenceStore prefs = null;
	
	public ArchlightIssueViewMyxComponent(){
	}
	
	public void begin(){
		er.register(this);
	}
	
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_ISSUEADT)){
			issueadt = (IArchlightIssueADT)serviceObject;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			xarch = (XArchFlatInterface)serviceObject;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_RESOURCES)){
			resources = (IResources)serviceObject;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_EDITORMANAGER)){
			editorManager = (IEditorManager)serviceObject;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_PREFS)){
			prefs = (IPreferenceStore)serviceObject;
		}
	}
	
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject){
		if(interfaceName.equals(INTERFACE_NAME_OUT_ISSUEADT)){
			issueadt = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_XARCH)){
			xarch = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_RESOURCES)){
			resources = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_EDITORMANAGER)){
			editorManager = null;
		}
		else if(interfaceName.equals(INTERFACE_NAME_OUT_PREFS)){
			prefs = null;
		}
	}
	
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject){}
	
	public void end(){
		er.unregister(this);
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_ISSUEADTEVENTS)){
			return this;
		}
		return null;
	}
	
	public void issueADTChanged(ArchlightIssueADTEvent evt){
		Object[] os = er.getObjects(this);
		for(int i = 0; i < os.length; i++){
			if(os[i] instanceof ArchlightIssueADTListener){
				((ArchlightIssueADTListener)os[i]).issueADTChanged(evt);
			}
		}
	}
	
}
