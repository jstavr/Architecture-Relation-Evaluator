package edu.uci.isr.archstudio4.comp.archipelago.statecharts;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.FontData;

import edu.uci.isr.archstudio4.comp.archipelago.AbstractArchipelagoTreePlugin;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoTreeContextMenuFiller;
import edu.uci.isr.xarchflat.ObjRef;

public class StatechartsTreePlugin extends AbstractArchipelagoTreePlugin{

	public StatechartsTreePlugin(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
		this.contentProvider = new StatechartsTreeContentProvider(AS, xArchRef);
		this.labelProvider = new StatechartsTreeLabelProvider(AS);
		this.doubleClickHandler = new StatechartsDoubleClickHandler(AS);
		this.flatListener = new StatechartsXArchEventHandler(AS);
		this.fileManagerListener = new StatechartsFileManagerListener(AS, xArchRef);
		
		this.contextMenuFillers = new IArchipelagoTreeContextMenuFiller[]{
			new StatechartsNewStatechartContextMenuFiller(viewer, AS, xArchRef),
			new StatechartsEditDescriptionContextMenuFiller(viewer, AS, xArchRef),
			new StatechartsRemoveContextMenuFiller(viewer, AS, xArchRef)
		};
		this.cellModifiers = new ICellModifier[]{
			new StatechartsEditDescriptionCellModifier(AS, xArchRef)
		};
		//this.editorFocuser = new StructureEditorFocuser(viewer, AS, xArchRef);
		
		initDefaultPreferences(viewer, AS.prefs);
	}
	
	protected void initDefaultPreferences(TreeViewer viewer, IPreferenceStore prefs){
		//FontData[] fontDatas = viewer.getTree().getDisplay().getSystemFont().getFontData();
		//PreferenceConverter.setDefault(prefs, ArchipelagoTypesConstants.PREF_DEFAULT_FONT, 
		//	fontDatas);
		//PreferenceConverter.setDefault(prefs, ArchipelagoTypesConstants.PREF_DEFAULT_COMPONENT_COLOR, 
		//	ArchipelagoTypesConstants.DEFAULT_COMPONENT_RGB);
		//PreferenceConverter.setDefault(prefs, ArchipelagoTypesConstants.PREF_DEFAULT_CONNECTOR_COLOR, 
		//	ArchipelagoTypesConstants.DEFAULT_CONNECTOR_RGB);
	}
	
}
