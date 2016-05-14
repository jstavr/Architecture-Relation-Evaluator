package edu.uci.isr.archstudio4.comp.archipelago;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.comp.booleannotation.IBooleanNotation;
import edu.uci.isr.archstudio4.comp.copypaste.ICopyPasteManager;
import edu.uci.isr.archstudio4.comp.guardtracker.IGuardTracker;
import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElementsManager;
import edu.uci.isr.archstudio4.comp.selector.ISelector;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioEditorMyxComponent;
import edu.uci.isr.archstudio4.graphlayout.IGraphLayout;
import edu.uci.isr.archstudio4.launcher.ILaunchData;
import edu.uci.isr.archstudio4.launcher.LaunchData;
//import edu.uci.isr.myx.fw.IMyxName;
//import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;

public class ArchipelagoMyxComponent extends AbstractArchstudioEditorMyxComponent{

	public static final String EDITOR_NAME = "Archipelago";
	public static final String ECLIPSE_EDITOR_ID = "edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoEditor";
	
	public static final String IMAGE_ARCHIPELAGO_ICON = "archipelago:icon";
	public static final String URL_ARCHIPELAGO_ICON = "res/archipelago-icon-32.gif";

	public static final IMyxName INTERFACE_NAME_OUT_PREFERENCES = MyxUtils.createName("preferences");
	public static final IMyxName INTERFACE_NAME_OUT_GRAPHLAYOUT = MyxUtils.createName("graphlayout");
	public static final IMyxName INTERFACE_NAME_OUT_BOOLEANNOTATION = MyxUtils.createName("booleannotation");
	public static final IMyxName INTERFACE_NAME_OUT_GUARDTRACKER = MyxUtils.createName("guardtracker");
	public static final IMyxName INTERFACE_NAME_OUT_SELECTOR = MyxUtils.createName("selector");
	public static final IMyxName INTERFACE_NAME_OUT_COPY_PASTE = MyxUtils.createName("copypaste");
	
	protected IPreferenceStore prefs = null;
	protected IGraphLayout graphLayout = null;
	protected IBooleanNotation bni = null;
	protected IGuardTracker guardTracker = null;
	protected ISelector selector = null;
	protected IRelatedElementsManager relatedElementsManager = null;
	protected ICopyPasteManager copyPasteManager = null;
	
	public ArchipelagoMyxComponent(){
		super(EDITOR_NAME, ECLIPSE_EDITOR_ID, true);
	}
	
	public void begin(){
		super.begin();
		prefs = (IPreferenceStore)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_PREFERENCES);
		graphLayout = (IGraphLayout)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_GRAPHLAYOUT);
		bni = (IBooleanNotation)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_BOOLEANNOTATION);
		guardTracker = (IGuardTracker)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_GUARDTRACKER);
		selector = (ISelector)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_SELECTOR);
		copyPasteManager = (ICopyPasteManager)MyxUtils.getFirstRequiredServiceObject(this,INTERFACE_NAME_OUT_COPY_PASTE);
		resources.createImage(IMAGE_ARCHIPELAGO_ICON, ArchipelagoMyxComponent.class.getResourceAsStream(URL_ARCHIPELAGO_ICON));
	}

	public IPreferenceStore getPreferences(){
		return prefs;
	}
	
	public IGraphLayout getGraphLayout(){
		return graphLayout;
	}
	
	public IGuardTracker getGuardTracker(){
		return guardTracker;
	}
	
	public IBooleanNotation getBooleanNotation(){
		return bni;
	}
	
	public ISelector getSelector(){
		return selector;
	}
	
	public Image getIcon(){
		return resources.getImage(IMAGE_ARCHIPELAGO_ICON);
	}
	
	public ILaunchData getLaunchData(){
		return new LaunchData(ECLIPSE_EDITOR_ID, EDITOR_NAME, "A graphical architecture editor", getIcon(), ILaunchData.EDITOR);
	}

	public ICopyPasteManager getCopyPasteManager() {
		return copyPasteManager;
	}
	
}
