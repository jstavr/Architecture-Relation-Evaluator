package edu.uci.isr.archstudio4.comp.archipelago.prefs.types;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.uci.isr.archstudio4.comp.archipelago.types.ArchipelagoTypesConstants;
import edu.uci.isr.myx.fw.MyxRegistry;

public class ArchipelagoTypesPreferencePanel extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
	private ArchipelagoTypesPrefsMyxComponent comp = null;
	private MyxRegistry er = MyxRegistry.getSharedInstance();
	
	protected FontFieldEditor defaultBrickFontEditor;
	protected ColorFieldEditor defaultComponentColorEditor;
	protected ColorFieldEditor defaultConnectorColorEditor;
	protected ColorFieldEditor defaultComponentTypeColorEditor;
	protected ColorFieldEditor defaultConnectorTypeColorEditor;
	
	public ArchipelagoTypesPreferencePanel(){
		super("Archipelago Structure and Types Preferences", FLAT);
		comp = (ArchipelagoTypesPrefsMyxComponent)er.waitForBrick(ArchipelagoTypesPrefsMyxComponent.class);
		er.map(comp, this);
		
		setPreferenceStore(comp.prefs);
		setDescription("This panel lets you set structure and types preferences for Archipelago.");
	}
	
	public void init(IWorkbench workbench){
	}

	protected void createFieldEditors(){
		defaultBrickFontEditor = new FontFieldEditor(ArchipelagoTypesConstants.PREF_DEFAULT_FONT, 
			"Default Font:", getFieldEditorParent());
		addField(defaultBrickFontEditor);
		
		defaultComponentColorEditor = new ColorFieldEditor(ArchipelagoTypesConstants.PREF_DEFAULT_COMPONENT_COLOR, 
			"Default Component Color:", getFieldEditorParent());
		addField(defaultComponentColorEditor);
		
		defaultConnectorColorEditor = new ColorFieldEditor(ArchipelagoTypesConstants.PREF_DEFAULT_CONNECTOR_COLOR, 
			"Default Connector Color:", getFieldEditorParent());
		addField(defaultConnectorColorEditor);
		
		defaultComponentTypeColorEditor = new ColorFieldEditor(ArchipelagoTypesConstants.PREF_DEFAULT_COMPONENT_TYPE_COLOR, 
			"Default Component Type Color:", getFieldEditorParent());
		addField(defaultComponentTypeColorEditor);
		
		defaultConnectorTypeColorEditor = new ColorFieldEditor(ArchipelagoTypesConstants.PREF_DEFAULT_CONNECTOR_TYPE_COLOR, 
			"Default Connector Type Color:", getFieldEditorParent());
		addField(defaultConnectorTypeColorEditor);
	}

}
