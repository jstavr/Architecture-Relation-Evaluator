package edu.uci.isr.archstudio4.comp.aimlauncher;

import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.comp.aim.IAIM;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioEditorMyxComponent;
import edu.uci.isr.archstudio4.launcher.ILaunchData;
import edu.uci.isr.archstudio4.launcher.LaunchData;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;

public class AIMLauncherMyxComponent extends AbstractArchstudioEditorMyxComponent{

	public static final String EDITOR_NAME = "AIM Launcher";
	public static final String ECLIPSE_EDITOR_ID = "edu.uci.isr.archstudio4.comp.aim.AIMLauncherEditor";

	public static final String IMAGE_AIMLAUNCHER_ICON = "aimlauncher:icon";
	public static final String URL_AIMLAUNCHER_ICON = "res/aimlauncher-icon-32.gif";
	public static final String IMAGE_AIMLAUNCHER_GO_ICON = "aimlauncher:go";
	public static final String URL_AIMLAUNCHER_GO_ICON = "res/icon-go.gif";
	public static final String IMAGE_AIMLAUNCHER_STOP_ICON = "aimlauncher:stop";
	public static final String URL_AIMLAUNCHER_STOP_ICON = "res/icon-stop.gif";

	public static final IMyxName INTERFACE_NAME_OUT_AIM = MyxUtils.createName("aim");
	
	protected IAIM aim = null;
	
	public AIMLauncherMyxComponent(){
		super(EDITOR_NAME, ECLIPSE_EDITOR_ID, true);
	}
	
	public void begin(){
		super.begin();
		aim = (IAIM)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_AIM);
		resources.createImage(IMAGE_AIMLAUNCHER_ICON, AIMLauncherMyxComponent.class.getResourceAsStream(URL_AIMLAUNCHER_ICON));
		resources.createImage(IMAGE_AIMLAUNCHER_GO_ICON, AIMLauncherMyxComponent.class.getResourceAsStream(URL_AIMLAUNCHER_GO_ICON));
		resources.createImage(IMAGE_AIMLAUNCHER_STOP_ICON, AIMLauncherMyxComponent.class.getResourceAsStream(URL_AIMLAUNCHER_STOP_ICON));
	}
	
	public ILaunchData getLaunchData(){
		return new LaunchData(ECLIPSE_EDITOR_ID, EDITOR_NAME, 
			"Architecture Instantiation Manager", getIcon(), 
			ILaunchData.EDITOR);
	}
	
	public Image getIcon(){
		return resources.getImage(IMAGE_AIMLAUNCHER_ICON);
	}

	public IAIM getAIM(){
		return aim;
	}
}
