package edu.uci.isr.archstudio4.comp.typewrangler;

import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.editors.AbstractArchstudioEditorMyxComponent;
import edu.uci.isr.archstudio4.launcher.ILaunchData;
import edu.uci.isr.archstudio4.launcher.LaunchData;

public class TypeWranglerMyxComponent extends AbstractArchstudioEditorMyxComponent{
	
	public static final String EDITOR_NAME = "Type Wrangler";
	public static final String ECLIPSE_EDITOR_ID = "edu.uci.isr.archstudio4.comp.typewrangler.TypeWranglerEditor";
	
	public static final String IMAGE_TYPEWRANGLER_ICON = "typewrangler:icon";
	public static final String URL_TYPEWRANGLER_ICON = "res/typewrangler-icon-32.gif";
	
	public TypeWranglerMyxComponent(){
		super(EDITOR_NAME, ECLIPSE_EDITOR_ID, true);
	}
	
	public void begin(){
		super.begin();
		resources.createImage(IMAGE_TYPEWRANGLER_ICON, TypeWranglerMyxComponent.class.getResourceAsStream(URL_TYPEWRANGLER_ICON));
	}
	
	public Image getIcon(){
		return resources.getImage(IMAGE_TYPEWRANGLER_ICON);
	}
	
	public ILaunchData getLaunchData(){
		return new LaunchData(ECLIPSE_EDITOR_ID, EDITOR_NAME, "Manage consistency between instances and types", getIcon(), ILaunchData.EDITOR);
	}
	
}
