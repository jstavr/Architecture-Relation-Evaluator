package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TreeViewer;

import edu.uci.isr.archstudio4.comp.archipelago.AbstractArchipelagoTreePlugin;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoTreeContextMenuFiller;
import edu.uci.isr.xarchflat.ObjRef;

public class ActivityDiagramsTreePlugin extends AbstractArchipelagoTreePlugin{

	public ActivityDiagramsTreePlugin(TreeViewer viewer, ArchipelagoServices AS, ObjRef xArchRef){
		this.contentProvider = new ActivityDiagramsTreeContentProvider(AS, xArchRef);
		this.labelProvider = new ActivityDiagramsTreeLabelProvider(AS);
		this.doubleClickHandler = new ActivityDiagramsDoubleClickHandler(AS);
		this.flatListener = new ActivityDiagramsXArchEventHandler(AS);
		this.fileManagerListener = new ActivityDiagramsFileManagerListener(AS, xArchRef);
		
		this.contextMenuFillers = new IArchipelagoTreeContextMenuFiller[]{
			new ActivityDiagramsNewActivityDiagramContextMenuFiller(viewer, AS, xArchRef),
			new ActivityDiagramsEditDescriptionContextMenuFiller(viewer, AS, xArchRef),
			new ActivityDiagramsRemoveContextMenuFiller(viewer, AS, xArchRef)
		};
		this.cellModifiers = new ICellModifier[]{
			new ActivityDiagramsEditDescriptionCellModifier(AS, xArchRef)
		};
		
		initDefaultPreferences(viewer, AS.prefs);
	}
	
	protected void initDefaultPreferences(TreeViewer viewer, IPreferenceStore prefs){
	}
	
}
