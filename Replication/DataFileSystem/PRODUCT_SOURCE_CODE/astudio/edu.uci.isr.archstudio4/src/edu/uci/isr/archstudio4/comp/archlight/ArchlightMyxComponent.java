package edu.uci.isr.archstudio4.comp.archlight;

import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.archlight.IArchlightTool;
import edu.uci.isr.archstudio4.comp.archlight.testadt.IArchlightTestADT;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioEditorMyxComponent;
import edu.uci.isr.archstudio4.launcher.ILaunchData;
import edu.uci.isr.archstudio4.launcher.LaunchData;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;

public class ArchlightMyxComponent extends AbstractArchstudioEditorMyxComponent{

	public static final String EDITOR_NAME = "Archlight";
	public static final String ECLIPSE_EDITOR_ID = "edu.uci.isr.archstudio4.comp.archlight.ArchlightEditor";
	
	public static final String IMAGE_ARCHLIGHT_ICON = "archlight:icon";
	public static final String URL_ARCHLIGHT_ICON = "res/archlight-icon-32.gif";

	public static final IMyxName INTERFACE_NAME_OUT_TESTADT = MyxUtils.createName("archlighttestadt");
	public static final IMyxName INTERFACE_NAME_OUT_TOOLS = MyxUtils.createName("archlighttools");
	
	protected IArchlightTestADT testadt = null;
	protected IArchlightTool tools = null;
	
	public ArchlightMyxComponent(){
		super(EDITOR_NAME, ECLIPSE_EDITOR_ID, false);
	}
	
	public void begin(){
		super.begin();
		testadt = (IArchlightTestADT)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_TESTADT);
		tools = (IArchlightTool)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_TOOLS);
		resources.createImage(IMAGE_ARCHLIGHT_ICON, ArchlightMyxComponent.class.getResourceAsStream(URL_ARCHLIGHT_ICON));
	}

	public IArchlightTestADT getTestADT(){
		return testadt;
	}

	public IArchlightTool getTools(){
		return tools;
	}
	
	public Image getIcon(){
		return resources.getImage(IMAGE_ARCHLIGHT_ICON);
	}
	
	public ILaunchData getLaunchData(){
		return new LaunchData(ECLIPSE_EDITOR_ID, EDITOR_NAME, "An Architecture Analysis Framework", getIcon(), ILaunchData.EDITOR);
	}
}
