package edu.uci.isr.archstudio4.comp.graphlayout;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.uci.isr.archstudio4.graphlayout.GraphLayoutConstants;
import edu.uci.isr.myx.fw.MyxRegistry;

public class GraphLayoutPreferencePanel extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
	private GraphLayoutPrefsMyxComponent comp = null;
	private MyxRegistry er = MyxRegistry.getSharedInstance();

	protected DirectoryFieldEditor graphvizPathEditor;
	
	public GraphLayoutPreferencePanel(){
		super("Graph Layout Preferences", GRID);
		comp = (GraphLayoutPrefsMyxComponent)er.waitForBrick(GraphLayoutPrefsMyxComponent.class);
		er.map(comp, this);
		
		setPreferenceStore(comp.prefs);
		setDescription("This panel lets you set graph layout preferences, particularly the path to the GraphViz toolkit.");
	}
	
	public void init(IWorkbench workbench){
	}

	protected void createFieldEditors(){
		graphvizPathEditor = new DirectoryFieldEditor(GraphLayoutConstants.PREF_GRAPHVIZ_PATH,
			"Path to Graphviz", getFieldEditorParent());
		addField(graphvizPathEditor);
	}

}
