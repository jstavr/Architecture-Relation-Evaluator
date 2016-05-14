package edu.uci.isr.archstudio4.comp.archipelago;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchSite;

import edu.uci.isr.archstudio4.comp.booleannotation.IBooleanNotation;
import edu.uci.isr.archstudio4.comp.copypaste.ICopyPasteManager;
import edu.uci.isr.archstudio4.comp.fileman.IFileManager;
import edu.uci.isr.archstudio4.comp.guardtracker.IGuardTracker;
import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElementsManager;
import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.comp.selector.ISelector;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.graphlayout.IGraphLayout;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ArchipelagoServices{

	public IArchipelagoEventBus eventBus = null;
	public IArchipelagoEditorPane editor = null;
	public IArchipelagoTreeNodeDataCache treeNodeDataCache = null;
	public XArchFlatInterface xarch = null;
	public IFileManager fileman = null;
	public IResources resources = null;
	public IPreferenceStore prefs = null;
	public IGraphLayout graphLayout = null;
	public IBooleanNotation booleanNotation = null;
	public IGuardTracker guardTracker = null;
	public ISelector selector = null;
	public XArchChangeSetInterface xarchcs = null;
	public IExplicitADT explicit = null;
	public IWorkbenchSite workbenchSite = null;
	public ICopyPasteManager copyPasteManager = null;

	public ArchipelagoServices(IArchipelagoEventBus eventBus, IArchipelagoEditorPane editor, IArchipelagoTreeNodeDataCache treeNodeDataCache, XArchFlatInterface xarch, IResources resources, IFileManager fileman, IPreferenceStore prefs, IGraphLayout graphLayout, IBooleanNotation booleanNotation, IGuardTracker guardTracker, ISelector selector, XArchChangeSetInterface xarchcs, IExplicitADT explicit, IWorkbenchSite workbenchSite,ICopyPasteManager copyPasteManager){
		this.eventBus = eventBus;
		this.editor = editor;
		this.treeNodeDataCache = treeNodeDataCache;
		this.xarch = xarch;
		this.resources = resources;
		this.fileman = fileman;
		this.prefs = prefs;
		this.graphLayout = graphLayout;
		this.booleanNotation = booleanNotation;
		this.guardTracker = guardTracker;
		this.selector = selector;
		this.xarchcs = xarchcs;
		this.explicit = explicit;
		this.workbenchSite = workbenchSite;
		this.copyPasteManager = copyPasteManager;
	}
}
