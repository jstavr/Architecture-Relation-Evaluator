package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TreeViewer;

import edu.uci.isr.archstudio4.comp.archipelago.AbstractArchipelagoTreePlugin;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoTreeContextMenuFiller;
import edu.uci.isr.xarchflat.ObjRef;

public class TypesTreePlugin extends AbstractArchipelagoTreePlugin{

	public TypesTreePlugin(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
		this.contentProvider = new TypesTreeContentProvider(AS, xArchRef);
		this.labelProvider = new TypesTreeLabelProvider(AS);
		this.doubleClickHandler = new TypesDoubleClickHandler(AS);
		this.flatListener = new TypesXArchEventHandler(AS, xArchRef);
		this.dragSourceListener = new TypesTreeDragSourceListener(AS, xArchRef);
		this.contextMenuFillers = new IArchipelagoTreeContextMenuFiller[]{
			new TypesNewTypeSetContextMenuFiller(viewer, AS, xArchRef),
			new TypesRemoveTypeSetContextMenuFiller(viewer, AS, xArchRef),
			new TypesNewTypeContextMenuFiller(viewer, AS, xArchRef),
			new TypesEditDescriptionContextMenuFiller(viewer, AS, xArchRef),
			new TypesRemoveContextMenuFiller(viewer, AS, xArchRef)
		};
		this.cellModifiers = new ICellModifier[]{
			new TypesEditDescriptionCellModifier(AS, xArchRef)
		};
		this.fileManagerListener = new TypesFileManagerListener(AS, xArchRef);
		this.editorFocuser = new TypesEditorFocuser(viewer, AS, xArchRef);
		
		initDefaultPreferences(AS.prefs);
	}
	
	protected void initDefaultPreferences(IPreferenceStore prefs){
		PreferenceConverter.setDefault(prefs, ArchipelagoTypesConstants.PREF_DEFAULT_COMPONENT_TYPE_COLOR, 
			ArchipelagoTypesConstants.DEFAULT_COMPONENT_TYPE_RGB);
		PreferenceConverter.setDefault(prefs, ArchipelagoTypesConstants.PREF_DEFAULT_CONNECTOR_TYPE_COLOR, 
			ArchipelagoTypesConstants.DEFAULT_CONNECTOR_TYPE_RGB);
	}

}
