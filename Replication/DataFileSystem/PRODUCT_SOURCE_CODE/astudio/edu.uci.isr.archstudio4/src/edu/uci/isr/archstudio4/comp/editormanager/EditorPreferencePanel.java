package edu.uci.isr.archstudio4.comp.editormanager;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.uci.isr.archstudio4.editors.EditorConstants;
import edu.uci.isr.archstudio4.editors.IEditorManager;
import edu.uci.isr.myx.fw.MyxRegistry;

public class EditorPreferencePanel extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
	private EditorPrefsMyxComponent comp = null;
	private MyxRegistry er = MyxRegistry.getSharedInstance();

	protected IEditorManager editorManager;
	
	protected RadioGroupFieldEditor defaultEditorEditor;
	
	public EditorPreferencePanel(){
		super("Editor Preferences", GRID);
		comp = (EditorPrefsMyxComponent)er.waitForBrick(EditorPrefsMyxComponent.class);
		er.map(comp, this);
		
		setPreferenceStore(comp.prefs);
		editorManager = comp.editorManager;
		setDescription("This panel lets you select ArchStudio 4 Editor preferences, particularly the default editor.");
	}
	
	public void init(IWorkbench workbench){
	}

	protected void createFieldEditors(){
		String[] availableEditors = editorManager.getEditors();
		
		String[][] editorData = new String[availableEditors.length + 1][];
		editorData[0] = new String[]{"[None]", EditorConstants.NO_EDITOR};
		for(int i = 0; i < availableEditors.length; i++){
			editorData[i+1] = new String[]{availableEditors[i], availableEditors[i]};
		}
		
		defaultEditorEditor = new RadioGroupFieldEditor(EditorConstants.PREF_DEFAULT_EDITOR, 
			"Default Editor", 1, editorData, getFieldEditorParent(), true);
		addField(defaultEditorEditor);
	}

}
