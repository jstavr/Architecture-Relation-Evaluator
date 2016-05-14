package edu.uci.isr.archstudio4.comp.basepreferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.myx.fw.MyxRegistry;

public class ArchStudioPreferencePanel extends PreferencePage implements IWorkbenchPreferencePage{

	public static final String IMAGE_PREFERENCES_BANNER = "archstudio:preferences/banner";
	
	private BasePreferencesMyxComponent comp = null;
	private MyxRegistry er = MyxRegistry.getSharedInstance();
	
	protected IResources resources = null;
	protected IPreferenceStore preferences = null;
	
	public ArchStudioPreferencePanel(){
		super("ArchStudio 4 Preferences");
		comp = (BasePreferencesMyxComponent)er.waitForBrick(BasePreferencesMyxComponent.class);
		er.map(comp, this);
		resources = comp.resources;
		preferences = comp.preferences;
		resources.createImage(IMAGE_PREFERENCES_BANNER, ArchStudioPreferencePanel.class.getResourceAsStream("res/banner.png"));
	}

	public void init(IWorkbench workbench){
	}

	protected Control createContents(Composite parent){
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(1, false));
		
		Label lBanner = new Label(c, SWT.BORDER);
		lBanner.setImage(resources.getImage(IMAGE_PREFERENCES_BANNER));
		
		Label lText = new Label(c, SWT.LEFT);
		lText.setText("Select a sub-node for options.");
		return c;
	}

}
