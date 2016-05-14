package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.FontData;

import edu.uci.isr.archstudio4.comp.archipelago.AbstractArchipelagoTreePlugin;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoTreeContextMenuFiller;
import edu.uci.isr.xarchflat.ObjRef;

public class StructureTreePlugin extends AbstractArchipelagoTreePlugin{

	public StructureTreePlugin(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
		this.contentProvider = new StructureTreeContentProvider(AS, xArchRef);
		this.labelProvider = new StructureTreeLabelProvider(AS);
		this.doubleClickHandler = new StructureDoubleClickHandler(AS);
		this.flatListener = new StructureXArchEventHandler(AS);
		this.fileManagerListener = new StructureFileManagerListener(AS, xArchRef);
		
		this.contextMenuFillers = new IArchipelagoTreeContextMenuFiller[]{
			new StructureNewStructureContextMenuFiller(viewer, AS, xArchRef),
			new StructureEditDescriptionContextMenuFiller(viewer, AS, xArchRef),
			new StructureRemoveContextMenuFiller(viewer, AS, xArchRef)
		};
		this.cellModifiers = new ICellModifier[]{
			new StructureEditDescriptionCellModifier(AS, xArchRef)
		};
		this.editorFocuser = new StructureEditorFocuser(viewer, AS, xArchRef);
		
		initDefaultPreferences(viewer, AS.prefs);
	}
	
	protected void initDefaultPreferences(TreeViewer viewer, IPreferenceStore prefs){
		FontData[] fontDatas = viewer.getTree().getDisplay().getSystemFont().getFontData();
		PreferenceConverter.setDefault(prefs, ArchipelagoTypesConstants.PREF_DEFAULT_FONT, 
			fontDatas);
		PreferenceConverter.setDefault(prefs, ArchipelagoTypesConstants.PREF_DEFAULT_COMPONENT_COLOR, 
			ArchipelagoTypesConstants.DEFAULT_COMPONENT_RGB);
		PreferenceConverter.setDefault(prefs, ArchipelagoTypesConstants.PREF_DEFAULT_CONNECTOR_COLOR, 
			ArchipelagoTypesConstants.DEFAULT_CONNECTOR_RGB);
	}
	
}
