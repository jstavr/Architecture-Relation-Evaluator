package edu.uci.isr.archstudio4.comp.archedit;

import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.editors.AbstractArchstudioEditorMyxComponent;
import edu.uci.isr.archstudio4.launcher.ILaunchData;
import edu.uci.isr.archstudio4.launcher.LaunchData;

public class ArchEditMyxComponent extends AbstractArchstudioEditorMyxComponent{

	public static final String EDITOR_NAME = "ArchEdit";
	public static final String ECLIPSE_EDITOR_ID = "edu.uci.isr.archstudio4.comp.archedit.ArchEditEditor";

	public static final String IMAGE_ARCHEDIT_ICON = "archedit:icon";
	public static final String URL_ARCHEDIT_ICON = "res/archedit-icon-32.gif";

	public ArchEditMyxComponent(){
		super(EDITOR_NAME, ECLIPSE_EDITOR_ID, true);
	}
	
	public void begin(){
		super.begin();
		resources.createImage(IMAGE_ARCHEDIT_ICON, ArchEditMyxComponent.class.getResourceAsStream(URL_ARCHEDIT_ICON));
	}
	
	public ILaunchData getLaunchData(){
		return new LaunchData(ECLIPSE_EDITOR_ID, EDITOR_NAME, 
			"A syntax-directed editor for architecture descriptions", getIcon(), 
			ILaunchData.EDITOR);
	}
	
	public Image getIcon(){
		return resources.getImage(IMAGE_ARCHEDIT_ICON);
	}
}
