package edu.uci.isr.archstudio4.comp.archipelago;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreeViewer;

import edu.uci.isr.bna4.constants.GridDisplayType;
import edu.uci.isr.xarchflat.ObjRef;

public class RootTreePlugin extends AbstractArchipelagoTreePlugin{
	public RootTreePlugin(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
		this.contentProvider = new RootContentProvider(AS, xArchRef);
		this.labelProvider = new RootLabelProvider(AS);
		
		final TreeViewer fviewer = viewer;
		this.editorFocuser = new IArchipelagoEditorFocuser(){
			public void focusEditor(String editorName, ObjRef[] refs){
				fviewer.setSelection(null);
			}
		};
		initDefaultPreferences(AS.prefs);
	}
	
	protected void initDefaultPreferences(IPreferenceStore prefs){
		prefs.setDefault(ArchipelagoConstants.PREF_ANTIALIAS_GRAPHICS, false);
		prefs.setDefault(ArchipelagoConstants.PREF_ANTIALIAS_TEXT, false);
		prefs.setDefault(ArchipelagoConstants.PREF_DECORATIVE_GRAPHICS, false);
		prefs.setDefault(ArchipelagoConstants.PREF_GRID_SPACING, 25);
		prefs.setDefault(ArchipelagoConstants.PREF_GRID_DISPLAY_TYPE, GridDisplayType.NONE.name());
	}

}
