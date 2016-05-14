package edu.uci.isr.archstudio4.comp.changesetsviewer;

import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetListener;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioEditorMyxComponent;
import edu.uci.isr.archstudio4.launcher.ILaunchData;
import edu.uci.isr.archstudio4.launcher.LaunchData;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;

public class ChangeSetsViewerMyxComponent extends AbstractArchstudioEditorMyxComponent
	implements XArchChangeSetListener{

	public static final String EDITOR_NAME = "Change Sets Viewer";
	public static final String ECLIPSE_EDITOR_ID = "edu.uci.isr.archstudio4.comp.changesetsviewer.ChangeSetsViewerEditor";

	public static final String IMAGE_ARCHEDIT_ICON = "archedit:icon";
	public static final String URL_ARCHEDIT_ICON = "res/archedit-icon-32.gif";

	public ChangeSetsViewerMyxComponent(){
		super(EDITOR_NAME, ECLIPSE_EDITOR_ID, true);
	}

	public static final IMyxName CSADT_INTERFACE = MyxUtils.createName("csadt");
	public static final IMyxName CSSYNC_INTERFACE = MyxUtils.createName("cssync");
	public static final IMyxName XARCHCSEVENTS_INTERFACE = MyxUtils.createName("xarchcsevents");

	IChangeSetADT csadt;
	IChangeSetSync cssync;

	@Override
	public void begin(){
		super.begin();
		csadt = (IChangeSetADT)MyxUtils.getFirstRequiredServiceObject(this, CSADT_INTERFACE);
		cssync = (IChangeSetSync)MyxUtils.getFirstRequiredServiceObject(this, CSSYNC_INTERFACE);
		resources.createImage(IMAGE_ARCHEDIT_ICON, ChangeSetsViewerMyxComponent.class.getResourceAsStream(URL_ARCHEDIT_ICON));
	}

	public ILaunchData getLaunchData(){
		return new LaunchData(ECLIPSE_EDITOR_ID, EDITOR_NAME, "A viewer for change set content", getIcon(), ILaunchData.EDITOR);
	}

	public Image getIcon(){
		return resources.getImage(IMAGE_ARCHEDIT_ICON);
	}

	public IChangeSetADT getChangeSetADT(){
		return csadt;
	}

	public IChangeSetSync getChangeSetSync(){
		return cssync;
	}

	@Override
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(XARCHCSEVENTS_INTERFACE)){
			return this;
		}
		return super.getServiceObject(interfaceName);
	}

	public void handleXArchChangeSetEvent(XArchChangeSetEvent evt){
		Object[] os = er.getObjects(this);
		for(Object element: os){
			if(element instanceof XArchChangeSetListener){
				try{
					((XArchChangeSetListener)element).handleXArchChangeSetEvent(evt);
				}
				catch(Throwable t){
					t.printStackTrace();
				}
			}
		}
	}

}
